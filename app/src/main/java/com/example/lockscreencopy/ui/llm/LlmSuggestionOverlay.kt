package com.example.lockscreencopy.ui.llm

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.data.spanCount
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.FloatingWidget
import com.example.lockscreencopy.model.LockWidget
import com.example.lockscreencopy.model.PlacedWidget
import com.example.lockscreencopy.model.WidgetSize
import com.example.lockscreencopy.ui.picker.LlmSuggestionResult
import com.example.lockscreencopy.ui.widget.WidgetCell
import com.example.lockscreencopy.ui.widget.toBitmapSafe
import kotlin.math.roundToInt

data class LlmCommitResult(
    val tray: List<PlacedWidget>,
    val floating: List<FloatingWidget>,
    val left: BottomShortcut?,
    val right: BottomShortcut?,
    val realWidgets: List<AppWidgetProviderInfo> = emptyList(),
)

private data class TrayInstance(
    val instanceId: String,
    val widget: LockWidget,
)

private data class FloatingInstance(
    val instanceId: String,
    val widget: LockWidget,
)

private data class RecommendedAppChoice(
    val app: BottomShortcut.App,
    val providers: List<AppWidgetProviderInfo>,
)

private enum class AppPlacementMode {
    NONE,
    SHORTCUT,
    WIDGET,
    BOTH,
}

@Composable
fun LlmSuggestionOverlay(
    suggestion: LlmSuggestionResult,
    appWidgetManager: AppWidgetManager? = null,
    onCancel: () -> Unit,
    onConfirm: (LlmCommitResult) -> Unit,
) {
    val context = LocalContext.current
    val pm = context.packageManager

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // 트레이 후보 인스턴스화. 같은 위젯이 여러 번 추천되어도 각각 다른 인스턴스로 취급.
    val trayInstances = remember(suggestion) {
        val byId = suggestion.selected.trayCandidates().associateBy { it.id }

        suggestion.recommendation.tray.mapIndexedNotNull { idx, id ->
            byId[id]?.let {
                TrayInstance(
                    instanceId = "tray_${idx}_$id",
                    widget = it,
                )
            }
        }
    }

    val floatingInstances = remember(suggestion) {
        val byId = suggestion.selected.floatingCandidates().associateBy { it.id }

        suggestion.recommendation.floating.mapIndexedNotNull { idx, id ->
            byId[id]?.let {
                FloatingInstance(
                    instanceId = "float_${idx}_$id",
                    widget = it,
                )
            }
        }
    }

    val shortcutById = remember(suggestion) {
        suggestion.selected.shortcutCandidates().associateBy { it.id }
    }

    val leftCandidate = suggestion.recommendation.left?.let { shortcutById[it] }
    val rightCandidate = suggestion.recommendation.right?.let { shortcutById[it] }

    // AI가 추천한 설치 앱에 대해:
    // 같은 앱을 "바로가기"로 둘지, "실제 위젯"으로 둘지, "둘 다" 둘지 선택하게 한다.
    val recommendedAppChoices = remember(suggestion, appWidgetManager) {
        suggestion.selected.installedApps.map { app ->
            val providers = appWidgetManager
                ?.installedProviders
                ?.filter { it.provider.packageName == app.packageName }
                .orEmpty()

            RecommendedAppChoice(
                app = app,
                providers = providers,
            )
        }
    }

    val trayPicked = remember(suggestion) { mutableStateMapOf<String, Boolean>() }
    val floatingPicked = remember(suggestion) { mutableStateMapOf<String, Boolean>() }
    val floatingOffsets = remember(suggestion) { mutableStateMapOf<String, Offset>() }

    var leftPicked by remember(suggestion) { mutableStateOf(false) }
    var rightPicked by remember(suggestion) { mutableStateOf(false) }

    val appPlacementModes = remember(suggestion) { mutableStateMapOf<String, AppPlacementMode>() }
    val selectedProviderIndex = remember(suggestion) { mutableStateMapOf<String, Int>() }

    val trayUsed = trayInstances.sumOf {
        if (trayPicked[it.instanceId] == true) it.widget.spanCount() else 0
    }

    // 자유 배치 후보 기본 위치. 화면 중앙 부근에 격자처럼 배치.
    val baseFreeOffsets = remember(suggestion) {
        val w = with(density) { screenWidth.toPx() }
        val h = with(density) { screenHeight.toPx() }

        floatingInstances.mapIndexed { idx, inst ->
            val col = idx % 2
            val row = idx / 2

            inst.instanceId to Offset(
                x = w * 0.15f + col * w * 0.42f,
                y = h * 0.38f + row * h * 0.10f,
            )
        }.toMap()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f)),
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onCancel) {
                Text("취소", color = Color.White)
            }

            Text(
                "AI 추천 - 원하는 항목을 선택하세요",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )

            Button(
                onClick = {
                    val tray = trayInstances
                        .filter { trayPicked[it.instanceId] == true }
                        .map {
                            PlacedWidget(
                                uid = it.instanceId,
                                widget = it.widget,
                            )
                        }

                    val floats = floatingInstances
                        .filter { floatingPicked[it.instanceId] == true }
                        .map {
                            val base = baseFreeOffsets[it.instanceId] ?: Offset.Zero
                            val adj = floatingOffsets[it.instanceId] ?: Offset.Zero

                            FloatingWidget(
                                uid = it.instanceId,
                                widget = it.widget,
                                offset = base + adj,
                            )
                        }

                    val realWidgets = ArrayList<AppWidgetProviderInfo>()
                    val appShortcuts = ArrayList<BottomShortcut.App>()

                    recommendedAppChoices.forEach { choice ->
                        val mode = appPlacementModes[choice.app.id]
                            ?: defaultPlacementMode(choice)

                        when (mode) {
                            AppPlacementMode.NONE -> Unit

                            AppPlacementMode.SHORTCUT -> {
                                appShortcuts += choice.app
                            }

                            AppPlacementMode.WIDGET -> {
                                choice.selectedProvider(selectedProviderIndex[choice.app.id])
                                    ?.let { realWidgets += it }
                            }

                            AppPlacementMode.BOTH -> {
                                appShortcuts += choice.app
                                choice.selectedProvider(selectedProviderIndex[choice.app.id])
                                    ?.let { realWidgets += it }
                            }
                        }
                    }

                    var finalLeft = if (leftPicked) leftCandidate else null
                    var finalRight = if (rightPicked) rightCandidate else null

                    appShortcuts.forEach { shortcut ->
                        if (finalLeft == null) {
                            finalLeft = shortcut
                        } else if (finalRight == null) {
                            finalRight = shortcut
                        }
                    }

                    onConfirm(
                        LlmCommitResult(
                            tray = tray,
                            floating = floats,
                            left = finalLeft,
                            right = finalRight,
                            realWidgets = realWidgets,
                        ),
                    )
                },
            ) {
                Text("확인")
            }
        }

        // 트레이 영역 후보
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = screenHeight * 0.22f)
                .fillMaxWidth(0.95f),
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "트레이 후보 (사용 $trayUsed / 4)",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    trayInstances.forEach { inst ->
                        val picked = trayPicked[inst.instanceId] == true

                        TrayGhostCell(
                            widget = inst.widget,
                            picked = picked,
                            onTap = {
                                if (picked) {
                                    trayPicked[inst.instanceId] = false
                                } else {
                                    val cost = inst.widget.spanCount()

                                    if (trayUsed + cost <= 4) {
                                        trayPicked[inst.instanceId] = true
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }

        // 자유 배치 후보
        floatingInstances.forEach { inst ->
            val base = baseFreeOffsets[inst.instanceId] ?: Offset.Zero
            val drag = floatingOffsets[inst.instanceId] ?: Offset.Zero
            val picked = floatingPicked[inst.instanceId] == true

            FloatingGhost(
                widget = inst.widget,
                picked = picked,
                offset = base + drag,
                onTap = {
                    floatingPicked[inst.instanceId] = !picked
                },
                onDrag = { delta ->
                    floatingOffsets[inst.instanceId] =
                        (floatingOffsets[inst.instanceId] ?: Offset.Zero) + delta
                },
            )
        }

        // AI 추천 설치 앱: 바로가기 / 위젯 / 둘 다 선택 패널
        if (recommendedAppChoices.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = screenHeight * 0.19f)
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Black.copy(alpha = 0.58f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(18.dp),
                    )
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    "추천 앱 배치 방식",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    "앱별로 바로가기, 실제 위젯, 둘 다 중에서 고를 수 있어요.",
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                )

                recommendedAppChoices.forEach { choice ->
                    val mode = appPlacementModes[choice.app.id]
                        ?: defaultPlacementMode(choice)

                    val providerIdx = selectedProviderIndex[choice.app.id] ?: 0

                    RecommendedAppChoiceCard(
                        choice = choice,
                        mode = mode,
                        selectedProviderIndex = providerIdx,
                        packageManager = pm,
                        onModeChange = { newMode ->
                            appPlacementModes[choice.app.id] = newMode
                        },
                        onProviderSelected = { index ->
                            selectedProviderIndex[choice.app.id] = index
                        },
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // 좌측 바로가기 후보
        if (leftCandidate != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = screenHeight * 0.13f),
            ) {
                ShortcutGhost(
                    shortcut = leftCandidate,
                    picked = leftPicked,
                    onTap = { leftPicked = !leftPicked },
                )
            }
        }

        // 우측 바로가기 후보
        if (rightCandidate != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = screenHeight * 0.13f),
            ) {
                ShortcutGhost(
                    shortcut = rightCandidate,
                    picked = rightPicked,
                    onTap = { rightPicked = !rightPicked },
                )
            }
        }
    }
}

private fun defaultPlacementMode(choice: RecommendedAppChoice): AppPlacementMode {
    return if (choice.providers.isNotEmpty()) {
        AppPlacementMode.WIDGET
    } else {
        AppPlacementMode.SHORTCUT
    }
}

private fun RecommendedAppChoice.selectedProvider(index: Int?): AppWidgetProviderInfo? {
    if (providers.isEmpty()) return null

    val safeIndex = (index ?: 0).coerceIn(0, providers.lastIndex)
    return providers[safeIndex]
}

@Composable
private fun TrayGhostCell(
    widget: LockWidget,
    picked: Boolean,
    onTap: () -> Unit,
) {
    val width = if (widget.size == WidgetSize.WIDE) 96.dp else 48.dp

    Box(
        modifier = Modifier
            .width(width)
            .height(48.dp)
            .graphicsLayer { alpha = if (picked) 1f else 0.35f }
            .border(
                width = if (picked) 2.dp else 1.dp,
                color = if (picked) Color(0xFF4DAAED) else Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable { onTap() },
    ) {
        WidgetCell(
            widget = widget,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun FloatingGhost(
    widget: LockWidget,
    picked: Boolean,
    offset: Offset,
    onTap: () -> Unit,
    onDrag: (Offset) -> Unit,
) {
    val w = if (widget.size == WidgetSize.WIDE) 180.dp else 100.dp

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    offset.x.roundToInt(),
                    offset.y.roundToInt(),
                )
            }
            .width(w)
            .height(100.dp)
            .graphicsLayer { alpha = if (picked) 1f else 0.35f }
            .border(
                width = if (picked) 2.dp else 1.dp,
                color = if (picked) Color(0xFF4DAAED) else Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
            )
            .pointerInput(picked) {
                detectTapGestures(onTap = { onTap() })
            }
            .pointerInput(picked) {
                if (picked) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        onDrag(drag)
                    }
                }
            },
    ) {
        WidgetCell(
            widget = widget,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun RecommendedAppChoiceCard(
    choice: RecommendedAppChoice,
    mode: AppPlacementMode,
    selectedProviderIndex: Int,
    packageManager: PackageManager,
    onModeChange: (AppPlacementMode) -> Unit,
    onProviderSelected: (Int) -> Unit,
) {
    val app = choice.app
    val iconBmp = remember(app.id) {
        app.drawable?.toBitmapSafe()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp),
            )
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                if (iconBmp != null) {
                    Image(
                        bitmap = iconBmp.asImageBitmap(),
                        contentDescription = app.label,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape),
                    )
                } else {
                    Text(
                        app.label.take(1),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    app.label,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    if (choice.providers.isEmpty()) {
                        "제공 위젯 없음 · 바로가기만 가능"
                    } else {
                        "${choice.providers.size}개 실제 위젯 사용 가능"
                    },
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 10.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlacementChip(
                label = "안 넣기",
                selected = mode == AppPlacementMode.NONE,
                onClick = { onModeChange(AppPlacementMode.NONE) },
            )

            PlacementChip(
                label = "바로가기",
                selected = mode == AppPlacementMode.SHORTCUT,
                onClick = { onModeChange(AppPlacementMode.SHORTCUT) },
            )

            PlacementChip(
                label = "위젯",
                selected = mode == AppPlacementMode.WIDGET,
                enabled = choice.providers.isNotEmpty(),
                onClick = { onModeChange(AppPlacementMode.WIDGET) },
            )

            PlacementChip(
                label = "둘 다",
                selected = mode == AppPlacementMode.BOTH,
                enabled = choice.providers.isNotEmpty(),
                onClick = { onModeChange(AppPlacementMode.BOTH) },
            )
        }

        if (
            choice.providers.isNotEmpty() &&
            (mode == AppPlacementMode.WIDGET || mode == AppPlacementMode.BOTH)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "사용할 위젯",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                choice.providers.take(4).forEachIndexed { index, provider ->
                    val providerLabel = remember(provider.provider.flattenToShortString()) {
                        runCatching {
                            provider.loadLabel(packageManager).toString()
                        }.getOrDefault(provider.provider.shortClassName)
                    }

                    ProviderChip(
                        label = providerLabel,
                        selected = selectedProviderIndex == index,
                        onClick = { onProviderSelected(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlacementChip(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                when {
                    !enabled -> Color.White.copy(alpha = 0.06f)
                    selected -> Color(0xFF4DAAED)
                    else -> Color.White.copy(alpha = 0.13f)
                },
            )
            .border(
                width = 1.dp,
                color = when {
                    !enabled -> Color.White.copy(alpha = 0.08f)
                    selected -> Color(0xFF4DAAED)
                    else -> Color.White.copy(alpha = 0.18f)
                },
                shape = RoundedCornerShape(999.dp),
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 9.dp, vertical = 5.dp),
    ) {
        Text(
            label,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.32f),
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun ProviderChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) Color.White.copy(alpha = 0.22f)
                else Color.White.copy(alpha = 0.08f),
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) Color(0xFF4DAAED)
                else Color.White.copy(alpha = 0.13f),
                shape = RoundedCornerShape(10.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Text(
            text = label.take(18),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun ShortcutGhost(
    shortcut: BottomShortcut,
    picked: Boolean,
    onTap: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer { alpha = if (picked) 1f else 0.4f }
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (picked) 0.95f else 0.55f))
            .border(
                width = if (picked) 3.dp else 1.dp,
                color = if (picked) Color(0xFF4DAAED) else Color.White.copy(alpha = 0.6f),
                shape = CircleShape,
            )
            .clickable { onTap() },
        contentAlignment = Alignment.Center,
    ) {
        when (shortcut) {
            is BottomShortcut.System -> {
                Icon(
                    shortcut.icon,
                    contentDescription = shortcut.label,
                    tint = Color(0xFF424242),
                    modifier = Modifier.size(28.dp),
                )
            }

            is BottomShortcut.App -> {
                val bmp = remember(shortcut.id) {
                    shortcut.drawable?.toBitmapSafe()
                }

                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = shortcut.label,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                    )
                } else {
                    Text(
                        shortcut.label.take(1),
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
