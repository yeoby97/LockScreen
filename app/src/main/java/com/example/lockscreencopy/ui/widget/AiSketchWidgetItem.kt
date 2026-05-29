package com.example.lockscreencopy.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.AiSketchWidget
import com.example.lockscreencopy.model.AiTextSlot
import com.example.lockscreencopy.model.AiWidgetTemplateType
import kotlin.math.min
import kotlin.math.roundToInt

private val WidgetShape = RoundedCornerShape(20.dp)
private val GlassPanelShape = RoundedCornerShape(14.dp)
private val GlassChipShape = RoundedCornerShape(10.dp)

@Composable
fun AiSketchWidgetItem(
    widget: AiSketchWidget,
    isFloating: Boolean,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onDrag: (Offset) -> Unit,
    onResize: (Float, Float, Float, Float) -> Unit,
    onDelete: () -> Unit,
    onSlotClick: ((AiTextSlot) -> Unit)? = null,
) {
    val widthDp = (widget.widthDp * widget.scaleX).dp
    val heightDp = (widget.heightDp * widget.scaleY).dp
    val scaleFactor = min(widget.scaleX, widget.scaleY).coerceIn(0.6f, 1.8f)

    Box(
        modifier = Modifier
            .offset { IntOffset(widget.offset.x.roundToInt(), widget.offset.y.roundToInt()) }
            .width(widthDp)
            .height(heightDp)
            .pointerInput(isFloating, widget.uid) {
                if (isFloating) detectTapGestures(onTap = { onSelectToggle() })
            }
            .pointerInput(isFloating, widget.uid) {
                if (isFloating) detectDragGestures { change, drag ->
                    change.consume(); onDrag(drag)
                }
            },
    ) {
        // 위젯 본체 — 둥근 모서리로 클립
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(WidgetShape),
        ) {
            // ① Full Bleed 배경 이미지
            if (widget.decorationBitmap != null) {
                Image(
                    bitmap = widget.decorationBitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                // 이미지 없을 때 그라디언트 플레이스홀더
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1A237E), Color(0xFF4A148C)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(40.dp),
                    )
                }
            }

            // ② 가독성을 위한 하단 그라디언트 스크림
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.45f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.55f),
                            ),
                        ),
                    ),
            )

            // ③ 글래스 텍스트 패널 (템플릿별 레이아웃)
            when (widget.templateType) {
                AiWidgetTemplateType.GLASS_INFO ->
                    GlassInfoOverlay(widget.textSlots, scaleFactor, isFloating, onSlotClick)
                AiWidgetTemplateType.STICKER ->
                    StickerOverlay(widget.textSlots, scaleFactor, isFloating, onSlotClick)
                AiWidgetTemplateType.LABEL_BOARD ->
                    LabelBoardOverlay(widget.textSlots, scaleFactor, isFloating, onSlotClick)
            }
        }

        // 편집 모드 테두리 (clip 바깥에서 그려야 완전히 보임)
        if (isFloating) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(1.dp, Color.White.copy(alpha = 0.65f), WidgetShape),
            )
        }

        if (isFloating && isSelected) ResizeHandles(onResize)
        if (isFloating) DeleteBadge(onClick = onDelete)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GLASS_INFO: 하단 왼쪽 큰 정보 + 오른쪽 작은 칩들
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GlassInfoOverlay(
    slots: List<AiTextSlot>,
    scaleFactor: Float,
    isFloating: Boolean,
    onSlotClick: ((AiTextSlot) -> Unit)?,
) {
    val mainSlot = slots.firstOrNull { it.role == "main" } ?: slots.firstOrNull()
    val subSlots = slots.filter { it != mainSlot }.take(3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding((10 * scaleFactor).dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy((8 * scaleFactor).dp),
        ) {
            // 왼쪽: 메인 정보 글래스 패널
            if (mainSlot != null) {
                SlotClickBox(slot = mainSlot, isFloating = isFloating, onSlotClick = onSlotClick) {
                    GlassPanel(modifier = Modifier.wrapContentSize()) {
                        Column {
                            Text(
                                text = mainSlot.value,
                                color = Color.White,
                                fontSize = (24f * scaleFactor).sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (mainSlot.label.isNotBlank()) {
                                Text(
                                    text = mainSlot.label,
                                    color = Color.White.copy(alpha = 0.72f),
                                    fontSize = (11f * scaleFactor).sp,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }

            // 오른쪽: 서브 정보 칩들 (세로 스택)
            if (subSlots.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy((4 * scaleFactor).dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    subSlots.forEach { slot ->
                        SlotClickBox(slot = slot, isFloating = isFloating, onSlotClick = onSlotClick) {
                            GlassChip(slot = slot, scaleFactor = scaleFactor)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STICKER: 하단 전체 너비 글래스 바 (칩 가로 나열)
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StickerOverlay(
    slots: List<AiTextSlot>,
    scaleFactor: Float,
    isFloating: Boolean,
    onSlotClick: ((AiTextSlot) -> Unit)?,
) {
    if (slots.isEmpty()) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = (10 * scaleFactor).dp, vertical = (10 * scaleFactor).dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    (8 * scaleFactor).dp,
                    Alignment.CenterHorizontally,
                ),
                verticalArrangement = Arrangement.spacedBy((4 * scaleFactor).dp),
            ) {
                slots.forEach { slot ->
                    SlotClickBox(slot = slot, isFloating = isFloating, onSlotClick = onSlotClick) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = slot.value,
                                color = Color.White,
                                fontSize = (15f * scaleFactor).sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                            )
                            if (slot.label.isNotBlank()) {
                                Text(
                                    text = slot.label,
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = (10f * scaleFactor).sp,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LABEL_BOARD: 하단 글래스 패널에 레이블:값 목록
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LabelBoardOverlay(
    slots: List<AiTextSlot>,
    scaleFactor: Float,
    isFloating: Boolean,
    onSlotClick: ((AiTextSlot) -> Unit)?,
) {
    if (slots.isEmpty()) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding((10 * scaleFactor).dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy((5 * scaleFactor).dp)) {
                slots.forEach { slot ->
                    SlotClickBox(slot = slot, isFloating = isFloating, onSlotClick = onSlotClick) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (slot.label.isNotBlank()) {
                                Text(
                                    text = slot.label,
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = (11f * scaleFactor).sp,
                                    modifier = Modifier.width((56 * scaleFactor).dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.width((4 * scaleFactor).dp))
                            }
                            Text(
                                text = slot.value,
                                color = Color.White,
                                fontSize = (13f * scaleFactor).sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 공통 글래스 컴포넌트
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 글래스모피즘 패널.
 * Full Bleed 배경 이미지 위에 반투명 레이어를 올리면 배경이 비쳐 frosted glass처럼 보인다.
 * Compose의 BlurEffect는 자신의 콘텐츠만 흐리게 하므로 진짜 backdrop blur 대신
 * 반투명(white 18%) + 밝은 테두리 조합으로 글래스 느낌을 낸다.
 */
@Composable
private fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(GlassPanelShape)
            .background(Color.White.copy(alpha = 0.18f))
            .border(1.dp, Color.White.copy(alpha = 0.40f), GlassPanelShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        content()
    }
}

@Composable
private fun GlassChip(slot: AiTextSlot, scaleFactor: Float) {
    Box(
        modifier = Modifier
            .clip(GlassChipShape)
            .background(Color.White.copy(alpha = 0.18f))
            .border(1.dp, Color.White.copy(alpha = 0.30f), GlassChipShape)
            .padding(horizontal = (8 * scaleFactor).dp, vertical = (3 * scaleFactor).dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = slot.value,
                color = Color.White,
                fontSize = (12f * scaleFactor).sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            if (slot.label.isNotBlank()) {
                Text(
                    text = slot.label,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = (9f * scaleFactor).sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SlotClickBox(
    slot: AiTextSlot,
    isFloating: Boolean,
    onSlotClick: ((AiTextSlot) -> Unit)?,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.then(
            if (!isFloating && onSlotClick != null) {
                Modifier.pointerInput(slot.label) {
                    detectTapGestures { onSlotClick(slot) }
                }
            } else Modifier,
        ),
    ) { content() }
}
