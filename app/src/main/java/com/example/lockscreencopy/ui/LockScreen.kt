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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.lockscreencopy.R
import com.example.lockscreencopy.model.AddTarget
import com.example.lockscreencopy.model.AppItem
import com.example.lockscreencopy.model.FloatingWidget
import com.example.lockscreencopy.model.HostedAppWidget
import com.example.lockscreencopy.model.PlacedWidget
import com.example.lockscreencopy.model.WidgetSize
import com.example.lockscreencopy.data.defaultAppList
import com.example.lockscreencopy.ui.picker.AppWidgetBottomSheet
import com.example.lockscreencopy.ui.picker.LockWidgetPickerSheet
import com.example.lockscreencopy.ui.picker.RealWidgetPickerSheet
import com.example.lockscreencopy.ui.picker.ShortcutChoice
import com.example.lockscreencopy.ui.picker.ShortcutPickerDialog
import com.example.lockscreencopy.ui.widget.AddedAppsRow
import com.example.lockscreencopy.ui.widget.ClockHeader
import com.example.lockscreencopy.ui.widget.FloatingWidgetItem
import com.example.lockscreencopy.ui.widget.HostedWidgetItem
import com.example.lockscreencopy.ui.widget.LockStarBar
import com.example.lockscreencopy.ui.widget.WidgetSlotRow
import com.example.lockscreencopy.ui.theme.LockScreenCopyTheme
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.math.roundToInt

@Composable
fun LockScreen(
    onUnlock: () -> Unit,
    hostedWidgets: SnapshotStateList<HostedAppWidget> = mutableStateListOf(),
    appWidgetHost: AppWidgetHost? = null,
    appWidgetManager: AppWidgetManager? = null,
    onRealWidgetSelected: (AppWidgetProviderInfo) -> Unit = {},
    onRemoveHosted: (String) -> Unit = {},
) {
    var isFloating by remember { mutableStateOf(false) }
    var showShortcutPopup by remember { mutableStateOf(false) }
    var showAppWidgetSheet by remember { mutableStateOf(false) }
    var showLockWidgetPicker by remember { mutableStateOf(false) }
    var showRealWidgetPicker by remember { mutableStateOf(false) }
    var addedApps by remember { mutableStateOf(listOf<AppItem>()) }

    var slotWidgets by remember { mutableStateOf<List<PlacedWidget>>(emptyList()) }
    var floatingWidgets by remember { mutableStateOf<List<FloatingWidget>>(emptyList()) }
    var selectedFloatingUid by remember { mutableStateOf<String?>(null) }
    var addTarget by remember { mutableStateOf(AddTarget.SLOT) }

    var addCounter by remember { mutableStateOf(0) }

    var clockOffset by remember { mutableStateOf(Offset.Zero) }
    var greenBoxOffset by remember { mutableStateOf(Offset.Zero) }
    var savedClockOffset by remember { mutableStateOf(Offset.Zero) }

    BackHandler(enabled = isFloating) {
        clockOffset = savedClockOffset
        greenBoxOffset = Offset.Zero
        selectedFloatingUid = null
        isFloating = false
    }

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
                            isFloating = true; false
                        }
                        if (!isFloating && released && System.currentTimeMillis() - t0 >= 500) {
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
                        onConfirm = {
                            savedClockOffset = clockOffset
                            greenBoxOffset = Offset.Zero
                            selectedFloatingUid = null
                            isFloating = false
                        },
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                    Column(
                        modifier = Modifier
                            .offset { IntOffset(clockOffset.x.roundToInt(), clockOffset.y.roundToInt()) }
                            .pointerInput(isFloating) {
                                if (isFloating) detectDragGestures { change, drag ->
                                    change.consume(); clockOffset += drag
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ClockHeader()
                        Spacer(modifier = Modifier.height(16.dp))
                        WidgetSlotRow(
                            placedWidgets = slotWidgets,
                            isFloating = isFloating,
                            slotSize = slotSize,
                            slotGap = slotGap,
                            onRemove = { uid -> slotWidgets = slotWidgets.filter { it.uid != uid } },
                            onAdd = {
                                addTarget = AddTarget.SLOT
                                showLockWidgetPicker = true
                            },
                        )
                    }

                    if (addedApps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(screenHeight * 0.03f))
                        AddedAppsRow(apps = addedApps)
                    }
                }

                if (isFloating) {
                    Box(
                        modifier = Modifier
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
                    },
                )
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
                        ShortcutChoice.AppWidget -> {
                            addTarget = AddTarget.FLOATING
                            showLockWidgetPicker = true
                        }
                        ShortcutChoice.RealWidget -> showRealWidgetPicker = true
                        ShortcutChoice.FavoriteApp -> showAppWidgetSheet = true
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

        if (showAppWidgetSheet) {
            AppWidgetBottomSheet(
                apps = defaultAppList,
                onDismiss = { showAppWidgetSheet = false },
                onAppSelected = { app ->
                    if (addedApps.none { it.id == app.id }) addedApps = addedApps + app
                    showAppWidgetSheet = false
                },
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

@Composable
private fun EditModeTopBar(visible: Boolean, onConfirm: () -> Unit) {
    if (!visible) {
        Spacer(modifier = Modifier.height(48.dp))
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Button(onClick = {}) { Text("배경화면") }
        Button(onClick = onConfirm) { Text("확인") }
    }
}
