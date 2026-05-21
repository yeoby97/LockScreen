package com.example.lockscreencopy.ui.llm

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
)

private data class TrayInstance(val instanceId: String, val widget: LockWidget)
private data class FloatingInstance(val instanceId: String, val widget: LockWidget)

@Composable
fun LlmSuggestionOverlay(
    suggestion: LlmSuggestionResult,
    onCancel: () -> Unit,
    onConfirm: (LlmCommitResult) -> Unit,
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // 트레이 후보 인스턴스화 (중복 허용)
    val trayInstances = remember(suggestion) {
        val byId = suggestion.selected.trayCandidates().associateBy { it.id }
        suggestion.recommendation.tray.mapIndexedNotNull { idx, id ->
            byId[id]?.let { TrayInstance("tray_${idx}_$id", it) }
        }
    }
    val floatingInstances = remember(suggestion) {
        val byId = suggestion.selected.floatingCandidates().associateBy { it.id }
        suggestion.recommendation.floating.mapIndexedNotNull { idx, id ->
            byId[id]?.let { FloatingInstance("float_${idx}_$id", it) }
        }
    }
    val shortcutById = remember(suggestion) {
        suggestion.selected.shortcutCandidates().associateBy { it.id }
    }
    val leftCandidate = suggestion.recommendation.left?.let { shortcutById[it] }
    val rightCandidate = suggestion.recommendation.right?.let { shortcutById[it] }

    val trayPicked = remember(suggestion) { mutableStateMapOf<String, Boolean>() }
    val floatingPicked = remember(suggestion) { mutableStateMapOf<String, Boolean>() }
    val floatingOffsets = remember(suggestion) { mutableStateMapOf<String, Offset>() }
    var leftPicked by remember(suggestion) { mutableStateOf(false) }
    var rightPicked by remember(suggestion) { mutableStateOf(false) }

    val trayUsed = trayInstances.sumOf { if (trayPicked[it.instanceId] == true) it.widget.spanCount() else 0 }

    // 자유 배치 후보 기본 위치 (스크린 중앙 부근에 격자처럼 흩뿌림)
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
            TextButton(onClick = onCancel) { Text("취소", color = Color.White) }
            Text("AI 추천 - 원하는 위젯을 탭하세요", color = Color.White,
                fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Button(onClick = {
                val tray = trayInstances.filter { trayPicked[it.instanceId] == true }
                    .map { PlacedWidget(uid = it.instanceId, widget = it.widget) }
                val floats = floatingInstances.filter { floatingPicked[it.instanceId] == true }
                    .map {
                        val base = baseFreeOffsets[it.instanceId] ?: Offset.Zero
                        val adj = floatingOffsets[it.instanceId] ?: Offset.Zero
                        FloatingWidget(uid = it.instanceId, widget = it.widget, offset = base + adj)
                    }
                onConfirm(
                    LlmCommitResult(
                        tray = tray,
                        floating = floats,
                        left = if (leftPicked) leftCandidate else null,
                        right = if (rightPicked) rightCandidate else null,
                    ),
                )
            }) { Text("확인") }
        }

        // 트레이 영역 후보 (상단에 횡렬로)
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
                    color = Color.White, fontSize = 11.sp,
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
                onTap = { floatingPicked[inst.instanceId] = !picked },
                onDrag = { delta ->
                    floatingOffsets[inst.instanceId] = (floatingOffsets[inst.instanceId] ?: Offset.Zero) + delta
                },
            )
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

@Composable
private fun TrayGhostCell(widget: LockWidget, picked: Boolean, onTap: () -> Unit) {
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
        WidgetCell(widget = widget, modifier = Modifier.fillMaxSize())
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
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
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
                if (picked) detectDragGestures { change, drag ->
                    change.consume(); onDrag(drag)
                }
            },
    ) {
        WidgetCell(widget = widget, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun ShortcutGhost(shortcut: BottomShortcut, picked: Boolean, onTap: () -> Unit) {
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
            is BottomShortcut.System -> Icon(
                shortcut.icon, contentDescription = shortcut.label,
                tint = Color(0xFF424242), modifier = Modifier.size(28.dp),
            )
            is BottomShortcut.App -> {
                val bmp = remember(shortcut.id) { shortcut.drawable?.toBitmapSafe() }
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = shortcut.label,
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                    )
                } else {
                    Text(
                        shortcut.label.take(1),
                        color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
