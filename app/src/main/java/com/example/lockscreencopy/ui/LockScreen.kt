package com.example.lockscreencopy.ui

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.lockscreencopy.R
import com.example.lockscreencopy.model.AddTarget
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.FavoriteAppsLayout
import com.example.lockscreencopy.model.FloatingWidget
import com.example.lockscreencopy.model.HostedAppWidget
import com.example.lockscreencopy.model.PlacedWidget
import com.example.lockscreencopy.model.WidgetSize
import com.example.lockscreencopy.data.handleSystemAction
import com.example.lockscreencopy.data.launchAppShortcut
import com.example.lockscreencopy.ui.llm.GhostInstance
import com.example.lockscreencopy.ui.llm.LlmAppStrip
import com.example.lockscreencopy.ui.llm.LlmFloatingGhost
import com.example.lockscreencopy.ui.llm.LlmTrayGhostRow
import com.example.lockscreencopy.ui.llm.ShortcutRecommendationBadge
import com.example.lockscreencopy.ui.llm.ghostFloatingOffset
import com.example.lockscreencopy.ui.picker.BottomShortcutPickerSheet
import com.example.lockscreencopy.ui.picker.FavoriteAppsPickerScreen
import com.example.lockscreencopy.ui.picker.FavoriteAppsSettingsSheet
import com.example.lockscreencopy.ui.picker.LlmLayoutSheet
import com.example.lockscreencopy.ui.picker.LlmSuggestionResult
import com.example.lockscreencopy.ui.picker.LockWidgetPickerSheet
import com.example.lockscreencopy.ui.picker.RealWidgetPickerSheet
import com.example.lockscreencopy.ui.picker.ShortcutChoice
import com.example.lockscreencopy.ui.picker.ShortcutPickerDialog
import com.example.lockscreencopy.ui.widget.BottomShortcutButton
import com.example.lockscreencopy.ui.widget.ClockHeader
import com.example.lockscreencopy.ui.widget.FavoriteAppsDisplay
import com.example.lockscreencopy.ui.widget.FloatingWidgetItem
import com.example.lockscreencopy.ui.widget.HostedWidgetItem
import com.example.lockscreencopy.ui.widget.LockStarBar
import com.example.lockscreencopy.ui.widget.ResizeHandles
import com.example.lockscreencopy.ui.widget.WidgetSlotRow
import com.example.lockscreencopy.ui.theme.LockScreenCopyTheme
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.math.roundToInt

private enum class ShortcutSide { LEFT, RIGHT }

@Composable
fun LockScreen(
    onUnlock: () -> Unit,
    hostedWidgets: SnapshotStateList<HostedAppWidget> = mutableStateListOf(),
    appWidgetHost: AppWidgetHost? = null,
    appWidgetManager: AppWidgetManager? = null,
    onRealWidgetSelected: (AppWidgetProviderInfo) -> Unit = {},
    onRemoveHosted: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var isFloating by remember { mutableStateOf(false) }
    var showShortcutPopup by remember { mutableStateOf(false) }
    var showLockWidgetPicker by remember { mutableStateOf(false) }
    var showRealWidgetPicker by remember { mutableStateOf(false) }

    var favoriteApps by remember { mutableStateOf(listOf<BottomShortcut>()) }
    var favoriteAppsEnabled by remember { mutableStateOf(true) }
    var favoriteAppsLayout by remember { mutableStateOf(FavoriteAppsLayout.BOTTOM_LEFT) }
    var showFavoriteSettings by remember { mutableStateOf(false) }
    var showFavoritePicker by remember { mutableStateOf(false) }

    var leftShortcut by remember { mutableStateOf<BottomShortcut?>(null) }
    var rightShortcut by remember { mutableStateOf<BottomShortcut?>(null) }
    var pickingShortcutSide by remember { mutableStateOf<ShortcutSide?>(null) }

    fun activateShortcut(shortcut: BottomShortcut) {
        when (shortcut) {
            is BottomShortcut.System -> handleSystemAction(context, shortcut.action)
            is BottomShortcut.App -> launchAppShortcut(context, shortcut)
        }
    }

    var slotWidgets by remember { mutableStateOf<List<PlacedWidget>>(emptyList()) }
    var floatingWidgets by remember { mutableStateOf<List<FloatingWidget>>(emptyList()) }
    var selectedFloatingUid by remember { mutableStateOf<String?>(null) }
    var addTarget by remember { mutableStateOf(AddTarget.SLOT) }

    var showLlmSheet by remember { mutableStateOf(false) }
    var llmSuggestion by remember { mutableStateOf<LlmSuggestionResult?>(null) }

    var addCounter by remember { mutableStateOf(0) }

    var clockOffset by remember { mutableStateOf(Offset.Zero) }
    var clockNaturalPosition by remember { mutableStateOf(Offset.Zero) }
    var greenBoxOffset by remember { mutableStateOf(Offset.Zero) }
    var savedClockOffset by remember { mutableStateOf(Offset.Zero) }
    var clockScale by remember { mutableStateOf(1f) }
    var savedClockScale by remember { mutableStateOf(1f) }
    var favoriteAppsOffset by remember { mutableStateOf(Offset.Zero) }
    var savedFavoriteAppsOffset by remember { mutableStateOf(Offset.Zero) }

    BackHandler(enabled = isFloating) {
        clockOffset = savedClockOffset
        clockScale = savedClockScale
        favoriteAppsOffset = savedFavoriteAppsOffset
        greenBoxOffset = Offset.Zero
        selectedFloatingUid = null
        isFloating = false
        llmSuggestion = null
    }

    LaunchedEffect(llmSuggestion) {
        if (llmSuggestion != null && !isFloating) {
            clockOffset = savedClockOffset
            isFloating = true
        }
    }

    var activeLlmAppId by remember(llmSuggestion) { mutableStateOf<String?>(null) }
    val consumedGhostKeys = remember(llmSuggestion) { mutableStateListOf<String>() }
    val ghostOriginByUid = remember(llmSuggestion) { mutableStateMapOf<String, String>() }
    var leftShortcutRecDismissed by remember(llmSuggestion) { mutableStateOf(false) }
    var rightShortcutRecDismissed by remember(llmSuggestion) { mutableStateOf(false) }

    fun releaseGhostFor(uid: String) {
        val key = ghostOriginByUid.remove(uid) ?: return
        consumedGhostKeys.remove(key)
    }

    val leftRecommendation = llmSuggestion?.let { s ->
        s.recommendation.left?.let { id -> s.selected.shortcutCandidates().firstOrNull { it.id == id } }
    }
    val rightRecommendation = llmSuggestion?.let { s ->
        s.recommendation.right?.let { id -> s.selected.shortcutCandidates().firstOrNull { it.id == id } }
    }

    val activeApp = llmSuggestion?.selected?.widgetApps?.firstOrNull { it.id == activeLlmAppId }
    val trayGhosts: List<GhostInstance> = if (llmSuggestion != null && activeApp != null) {
        llmSuggestion!!.recommendation.tray.mapIndexedNotNull { idx, id ->
            activeApp.widgets.firstOrNull { it.id == id }?.let { w ->
                GhostInstance("tray_${activeApp.id}_${idx}_$id", w)
            }
        }
    } else emptyList()
    val floatingGhosts: List<GhostInstance> = if (llmSuggestion != null && activeApp != null) {
        llmSuggestion!!.recommendation.floating.mapIndexedNotNull { idx, id ->
            activeApp.widgets.firstOrNull { it.id == id }?.let { w ->
                GhostInstance("float_${activeApp.id}_${idx}_$id", w)
            }
        }
    } else emptyList()
    val appsWithRecs = llmSuggestion?.let { s ->
        s.selected.widgetApps.filter { app ->
            s.recommendation.tray.any { id -> app.widgets.any { it.id == id } } ||
                s.recommendation.floating.any { id -> app.widgets.any { it.id == id } }
        }
    } ?: emptyList()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    fun resizeFloating(uid: String, dx: Float, dy: Float, ax: Float, ay: Float) {
        floatingWidgets = floatingWidgets.map { fw ->
            if (fw.uid != uid) fw else resizeFloatingWidget(fw, dx, dy, ax, ay, density)
        }
    }

    fun resizeHosted(uid: String, dx: Float, dy: Float, ax: Float, ay: Float) {
        val idx = hostedWidgets.indexOfFirst { it.uid == uid }
        if (idx != -1) hostedWidgets[idx] = resizeHostedWidget(hostedWidgets[idx], dx, dy, ax, ay, density.density)
    }

    val scale by animateFloatAsState(
        targetValue = if (isFloating) 0.7f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing), label = "scale",
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isFloating) 30.dp else 0.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing), label = "corner",
    )
    val blurRadius by animateDpAsState(
        targetValue = if (isFloating) 20.dp else 0.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing), label = "blur",
    )
    val editAlpha by animateFloatAsState(
        targetValue = if (isFloating) 1f else 0f,
        animationSpec = tween(300, delayMillis = 200, easing = FastOutSlowInEasing), label = "editAlpha",
    )

    val screenWidthPx = with(density) { screenWidth.toPx() }
    val screenHeightPx = with(density) { screenHeight.toPx() }

    val slotGap = 8.dp
    val slotSize = screenWidth * 0.1f

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.images),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
                .then(if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier),
        )

        Box(
            modifier = Modifier.fillMaxSize()
                .graphicsLayer {
                    scaleX = scale; scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0.2f)
                }
                .clip(RoundedCornerShape(cornerRadius))
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        val t0 = System.currentTimeMillis()
                        val released = try {
                            withTimeout(500) { tryAwaitRelease(); true }
                        } catch (_: TimeoutCancellationException) {
                            clockOffset = savedClockOffset
                            isFloating = true; false
                        }
                        if (!isFloating && released && System.currentTimeMillis() - t0 >= 500) {
                            clockOffset = savedClockOffset
                            isFloating = true
                        }
                    })
                },
        ) {
            Image(
                painter = painterResource(id = R.drawable.images),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EditModeTopBar(
                        visible = isFloating,
                        alpha = editAlpha,
                        onConfirm = {
                            savedClockOffset = clockOffset
                            savedClockScale = clockScale
                            savedFavoriteAppsOffset = favoriteAppsOffset
                            greenBoxOffset = Offset.Zero
                            selectedFloatingUid = null
                            isFloating = false
                            llmSuggestion = null
                        },
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                    Column(
                        modifier = Modifier
                            .onGloballyPositioned { coords ->
                                if (!isFloating) {
                                    val pos = coords.positionInRoot()
                                    clockNaturalPosition = Offset(
                                        pos.x + coords.size.width / 2f,
                                        pos.y + coords.size.height / 2f,
                                    )
                                }
                            }
                            .offset {
                                val comp = clockCompensation(clockNaturalPosition, scale, density.density, screenWidthPx, screenHeightPx)
                                IntOffset(
                                    (clockOffset.x + comp.x).roundToInt(),
                                    (clockOffset.y + comp.y).roundToInt(),
                                )
                            }
                            .pointerInput(isFloating) {
                                if (isFloating) detectDragGestures { change, drag ->
                                    change.consume(); clockOffset += drag
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color.White.copy(alpha = 0.7f * editAlpha), RoundedCornerShape(12.dp))
                                .padding(8.dp),
                        ) {
                            ClockHeader(scale = clockScale)
                            if (isFloating) {
                                Box(modifier = Modifier.matchParentSize().graphicsLayer { alpha = editAlpha }) {
                                    ResizeHandles { dx, dy, _, _ ->
                                        val delta = (dx + dy) / 2f
                                        clockScale = (clockScale + delta).coerceIn(0.5f, 2.0f)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        WidgetSlotRow(
                            placedWidgets = slotWidgets,
                            isFloating = isFloating,
                            slotSize = slotSize,
                            slotGap = slotGap,
                            onRemove = { uid ->
                                slotWidgets = slotWidgets.filter { it.uid != uid }
                                releaseGhostFor(uid)
                            },
                            onAdd = {
                                addTarget = AddTarget.SLOT
                                showLockWidgetPicker = true
                            },
                        )
                        if (isFloating && trayGhosts.any { it.key !in consumedGhostKeys }) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val trayUsed = slotWidgets.sumOf {
                                if (it.widget.size == WidgetSize.WIDE) 2 else 1
                            }
                            LlmTrayGhostRow(
                                ghosts = trayGhosts,
                                consumed = consumedGhostKeys.toSet(),
                                trayUsedSpan = trayUsed,
                                slotSize = slotSize,
                                slotGap = slotGap,
                                onTap = { ghost ->
                                    val needed = if (ghost.widget.size == WidgetSize.WIDE) 2 else 1
                                    if (trayUsed + needed <= 4) {
                                        addCounter++
                                        val newUid = "${ghost.widget.id}_$addCounter"
                                        slotWidgets = slotWidgets + PlacedWidget(
                                            uid = newUid,
                                            widget = ghost.widget,
                                        )
                                        consumedGhostKeys += ghost.key
                                        ghostOriginByUid[newUid] = ghost.key
                                    }
                                },
                            )
                        }
                    }

                }

                if (isFloating) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer { alpha = editAlpha }
                            .padding(bottom = screenHeight * 0.05f)
                            .offset { IntOffset(greenBoxOffset.x.roundToInt(), greenBoxOffset.y.roundToInt()) }
                            .pointerInput(isFloating) {
                                detectDragGestures { c, d -> c.consume(); greenBoxOffset += d }
                            }
                            .pointerInput(isFloating) {
                                detectTapGestures(onTap = { showShortcutPopup = true })
                            }
                            .clip(RoundedCornerShape(30.dp))
                            .border(2.dp, Color.LightGray, RoundedCornerShape(30.dp)),
                    ) {
                        LockStarBar()
                    }
                }
            }

            floatingWidgets.forEach { placed ->
                FloatingWidgetItem(
                    placed = placed,
                    isFloating = isFloating,
                    isSelected = selectedFloatingUid == placed.uid,
                    onSelectToggle = {
                        selectedFloatingUid = if (selectedFloatingUid == placed.uid) null else placed.uid
                    },
                    onDrag = { drag ->
                        floatingWidgets = floatingWidgets.map {
                            if (it.uid == placed.uid) it.copy(offset = it.offset + drag) else it
                        }
                    },
                    onResize = { dx, dy, ax, ay -> resizeFloating(placed.uid, dx, dy, ax, ay) },
                    onDelete = {
                        if (selectedFloatingUid == placed.uid) selectedFloatingUid = null
                        floatingWidgets = floatingWidgets.filter { it.uid != placed.uid }
                        releaseGhostFor(placed.uid)
                    },
                )
            }

            if (isFloating) {
                floatingGhosts.forEachIndexed { idx, ghost ->
                    if (ghost.key in consumedGhostKeys) return@forEachIndexed
                    val ghostOffset = ghostFloatingOffset(idx, screenWidthPx, screenHeightPx)
                    LlmFloatingGhost(
                        ghost = ghost,
                        offset = ghostOffset,
                        onTap = {
                            addCounter++
                            val newUid = "${ghost.widget.id}_$addCounter"
                            floatingWidgets = floatingWidgets + FloatingWidget(
                                uid = newUid,
                                widget = ghost.widget,
                                offset = ghostOffset,
                            )
                            consumedGhostKeys += ghost.key
                            ghostOriginByUid[newUid] = ghost.key
                            selectedFloatingUid = newUid
                        },
                    )
                }
            }

            if (favoriteAppsEnabled && favoriteApps.isNotEmpty()) {
                val favAlign = when (favoriteAppsLayout) {
                    FavoriteAppsLayout.BOTTOM_LEFT -> Alignment.BottomStart
                    FavoriteAppsLayout.BOTTOM_RIGHT -> Alignment.BottomEnd
                    FavoriteAppsLayout.LEFT_VERTICAL -> Alignment.CenterStart
                }
                val favPad = when (favoriteAppsLayout) {
                    FavoriteAppsLayout.BOTTOM_LEFT -> Modifier.padding(start = 16.dp, bottom = screenHeight * 0.13f)
                    FavoriteAppsLayout.BOTTOM_RIGHT -> Modifier.padding(end = 16.dp, bottom = screenHeight * 0.13f)
                    FavoriteAppsLayout.LEFT_VERTICAL -> Modifier.padding(start = 16.dp)
                }
                Box(
                    modifier = Modifier
                        .align(favAlign)
                        .then(favPad)
                        .offset { IntOffset(favoriteAppsOffset.x.roundToInt(), favoriteAppsOffset.y.roundToInt()) }
                        .then(
                            if (isFloating) Modifier
                                .graphicsLayer { alpha = editAlpha }
                                .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                .padding(6.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures { change, drag ->
                                        change.consume(); favoriteAppsOffset += drag
                                    }
                                }
                            else Modifier,
                        ),
                ) {
                    FavoriteAppsDisplay(favorites = favoriteApps, layout = favoriteAppsLayout)
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = screenHeight * 0.04f),
            ) {
                BottomShortcutButton(
                    shortcut = leftShortcut,
                    isEditing = isFloating,
                    onClick = {
                        if (isFloating) pickingShortcutSide = ShortcutSide.LEFT
                        else leftShortcut?.let { activateShortcut(it) }
                    },
                )
                if (leftRecommendation != null &&
                    leftShortcut?.id != leftRecommendation.id &&
                    !leftShortcutRecDismissed
                ) {
                    ShortcutRecommendationBadge(
                        shortcut = leftRecommendation,
                        onAccept = { leftShortcut = leftRecommendation },
                        onCancel = { leftShortcutRecDismissed = true },
                        modifier = Modifier.offset(y = (-70).dp),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = screenHeight * 0.04f),
            ) {
                BottomShortcutButton(
                    shortcut = rightShortcut,
                    isEditing = isFloating,
                    onClick = {
                        if (isFloating) pickingShortcutSide = ShortcutSide.RIGHT
                        else rightShortcut?.let { activateShortcut(it) }
                    },
                )
                if (rightRecommendation != null &&
                    rightShortcut?.id != rightRecommendation.id &&
                    !rightShortcutRecDismissed
                ) {
                    ShortcutRecommendationBadge(
                        shortcut = rightRecommendation,
                        onAccept = { rightShortcut = rightRecommendation },
                        onCancel = { rightShortcutRecDismissed = true },
                        modifier = Modifier.offset(y = (-70).dp),
                    )
                }
            }

            hostedWidgets.forEach { hosted ->
                key(hosted.uid) {
                    HostedWidgetItem(
                        hosted = hosted,
                        isFloating = isFloating,
                        isSelected = selectedFloatingUid == hosted.uid,
                        appWidgetHost = appWidgetHost,
                        onSelectToggle = {
                            selectedFloatingUid = if (selectedFloatingUid == hosted.uid) null else hosted.uid
                        },
                        onDrag = { drag ->
                            val idx = hostedWidgets.indexOfFirst { it.uid == hosted.uid }
                            if (idx != -1) {
                                val hw = hostedWidgets[idx]
                                hostedWidgets[idx] = hw.copy(offset = hw.offset + drag)
                            }
                        },
                        onResize = { dx, dy, ax, ay -> resizeHosted(hosted.uid, dx, dy, ax, ay) },
                        onDelete = {
                            if (selectedFloatingUid == hosted.uid) selectedFloatingUid = null
                            onRemoveHosted(hosted.uid)
                        },
                    )
                }
            }
        }

        if (showShortcutPopup) {
            ShortcutPickerDialog(
                onDismiss = { showShortcutPopup = false },
                onSelect = { choice ->
                    showShortcutPopup = false
                    when (choice) {
                        ShortcutChoice.RealWidget -> showRealWidgetPicker = true
                        ShortcutChoice.FavoriteApp -> showFavoriteSettings = true
                        ShortcutChoice.Text -> {}
                    }
                },
            )
        }

        if (showLockWidgetPicker) {
            LockWidgetPickerSheet(
                onDismiss = { showLockWidgetPicker = false },
                onWidgetSelected = { widget ->
                    addCounter++
                    if (addTarget == AddTarget.SLOT) {
                        val needed = if (widget.size == WidgetSize.WIDE) 2 else 1
                        val used = slotWidgets.sumOf { if (it.widget.size == WidgetSize.WIDE) 2 else 1 }
                        if (used + needed <= 4) {
                            slotWidgets = slotWidgets + PlacedWidget(
                                uid = "${widget.id}_$addCounter",
                                widget = widget,
                            )
                        }
                    } else {
                        floatingWidgets = floatingWidgets + FloatingWidget(
                            uid = "${widget.id}_$addCounter",
                            widget = widget,
                            offset = Offset(80f + addCounter * 20f, 280f + addCounter * 20f),
                        )
                    }
                    showLockWidgetPicker = false
                },
            )
        }

        if (showFavoriteSettings && !showFavoritePicker) {
            FavoriteAppsSettingsSheet(
                enabled = favoriteAppsEnabled,
                onEnabledChange = { favoriteAppsEnabled = it },
                favorites = favoriteApps,
                layout = favoriteAppsLayout,
                onLayoutChange = { favoriteAppsLayout = it },
                onOpenPicker = { showFavoritePicker = true },
                onDismiss = { showFavoriteSettings = false },
            )
        }

        if (showFavoritePicker) {
            FavoriteAppsPickerScreen(
                initial = favoriteApps,
                onClose = { showFavoritePicker = false },
                onApply = { picked ->
                    favoriteApps = picked
                    showFavoritePicker = false
                },
            )
        }

        pickingShortcutSide?.let { side ->
            BottomShortcutPickerSheet(
                onDismiss = { pickingShortcutSide = null },
                onClear = {
                    if (side == ShortcutSide.LEFT) leftShortcut = null else rightShortcut = null
                    pickingShortcutSide = null
                },
                onSelected = { sc ->
                    if (side == ShortcutSide.LEFT) leftShortcut = sc else rightShortcut = sc
                    pickingShortcutSide = null
                },
            )
        }

        if (!isFloating && llmSuggestion == null) {
            FloatingActionButton(
                onClick = { showLlmSheet = true },
                containerColor = Color(0xFF4DAAED),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = screenHeight * 0.18f),
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = "AI 위젯 배치")
            }
        }

        if (showLlmSheet) {
            LlmLayoutSheet(
                onDismiss = { showLlmSheet = false },
                onResult = { result ->
                    showLlmSheet = false
                    llmSuggestion = result
                },
            )
        }

        if (llmSuggestion != null) {
            LlmAppStrip(
                apps = appsWithRecs,
                selectedAppId = activeLlmAppId,
                onSelect = { activeLlmAppId = it },
                onClose = { llmSuggestion = null },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp, top = 60.dp, bottom = 60.dp)
                    .graphicsLayer { alpha = editAlpha },
            )
        }

        if (showRealWidgetPicker && appWidgetManager != null) {
            RealWidgetPickerSheet(
                appWidgetManager = appWidgetManager,
                onDismiss = { showRealWidgetPicker = false },
                onSelect = { info ->
                    showRealWidgetPicker = false
                    onRealWidgetSelected(info)
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LockScreenPreview() {
    LockScreenCopyTheme { LockScreen(onUnlock = {}) }
}

// scale 변화로 인한 시계 위치 이동을 상쇄하는 보정 offset 역산
// S = P + (N + O - P) * s = N  →  O = (N - P)(1 - s) / s
private fun clockCompensation(
    naturalPos: Offset,
    scale: Float,
    density: Float,
    screenWidthPx: Float,
    screenHeightPx: Float,
): Offset {
    if (scale == 1f) return Offset.Zero
    val pivotX = screenWidthPx * 0.5f
    val pivotY = screenHeightPx * 0.2f
    return Offset(
        x = (naturalPos.x - pivotX) * (1f - scale) / scale,
        y = (naturalPos.y - pivotY) * (1f - scale) / scale,
    )
}

@Composable
private fun EditModeTopBar(visible: Boolean, alpha: Float = 1f, onConfirm: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Button(onClick = {}, enabled = visible) { Text("배경화면") }
        Button(onClick = onConfirm, enabled = visible) { Text("확인") }
    }
}
