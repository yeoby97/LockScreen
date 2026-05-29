package com.example.lockscreencopy.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(10.dp)

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
    // 리사이즈 시 텍스트/패딩 스케일 인수 (0.6~1.8 클램프)
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
        // 템플릿 기반 카드 렌더링
        when (widget.templateType) {
            AiWidgetTemplateType.GLASS_INFO ->
                GlassInfoTemplate(widget, scaleFactor, isFloating, onSlotClick)
            AiWidgetTemplateType.STICKER ->
                StickerTemplate(widget, scaleFactor, isFloating, onSlotClick)
            AiWidgetTemplateType.LABEL_BOARD ->
                LabelBoardTemplate(widget, scaleFactor, isFloating, onSlotClick)
        }

        // 편집 모드 테두리
        if (isFloating) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(1.dp, Color.White.copy(alpha = 0.65f), CardShape),
            )
        }

        if (isFloating && isSelected) ResizeHandles(onResize)
        if (isFloating) DeleteBadge(onClick = onDelete)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GLASS_INFO: 반투명 글래스 카드. 왼쪽에 정보, 오른쪽에 장식 이미지.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GlassInfoTemplate(
    widget: AiSketchWidget,
    scaleFactor: Float,
    isFloating: Boolean,
    onSlotClick: ((AiTextSlot) -> Unit)?,
) {
    val slots = widget.textSlots
    val mainSlot = slots.firstOrNull { it.role == "main" } ?: slots.firstOrNull()
    val subSlots = slots.filter { it != mainSlot }.take(3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CardShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A2A4A).copy(alpha = 0.82f),
                        Color(0xFF0D1B2E).copy(alpha = 0.90f),
                    ),
                ),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = (12 * scaleFactor).dp, vertical = (10 * scaleFactor).dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 왼쪽: 정보 영역
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                if (mainSlot != null) {
                    SlotClickableBox(slot = mainSlot, isFloating = isFloating, onSlotClick = onSlotClick) {
                        Column {
                            Text(
                                text = mainSlot.value,
                                color = Color.White,
                                fontSize = (22f * scaleFactor).sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (mainSlot.label.isNotBlank()) {
                                Text(
                                    text = mainSlot.label,
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = (11f * scaleFactor).sp,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }

                if (subSlots.isNotEmpty()) {
                    Spacer(Modifier.height((4 * scaleFactor).dp))
                    Column(verticalArrangement = Arrangement.spacedBy((4 * scaleFactor).dp)) {
                        subSlots.forEach { slot ->
                            SlotClickableBox(slot = slot, isFloating = isFloating, onSlotClick = onSlotClick) {
                                InfoChip(slot = slot, scaleFactor = scaleFactor)
                            }
                        }
                    }
                }
            }

            // 오른쪽: 장식 이미지
            if (widget.decorationBitmap != null) {
                Spacer(Modifier.width((8 * scaleFactor).dp))
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width((70 * scaleFactor).dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        bitmap = widget.decorationBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            } else {
                DecorationPlaceholder(size = (56 * scaleFactor).dp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STICKER: 장식 이미지를 상단 중앙에 크게. 텍스트는 하단 칩 행.
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StickerTemplate(
    widget: AiSketchWidget,
    scaleFactor: Float,
    isFloating: Boolean,
    onSlotClick: ((AiTextSlot) -> Unit)?,
) {
    val slots = widget.textSlots

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CardShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2C2C54).copy(alpha = 0.88f),
                        Color(0xFF1A1A3E).copy(alpha = 0.95f),
                    ),
                ),
            ),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val totalH = maxHeight
            val chipAreaH = if (slots.isNotEmpty()) (48 * scaleFactor).dp else 0.dp
            val decoAreaH = totalH - chipAreaH - (8 * scaleFactor).dp

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 상단: 장식 이미지
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(decoAreaH),
                    contentAlignment = Alignment.Center,
                ) {
                    if (widget.decorationBitmap != null) {
                        Image(
                            bitmap = widget.decorationBitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding((8 * scaleFactor).dp),
                        )
                    } else {
                        DecorationPlaceholder(size = (72 * scaleFactor).dp)
                    }
                }

                // 하단: 정보 칩 행
                if (slots.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = (8 * scaleFactor).dp, vertical = (4 * scaleFactor).dp),
                        horizontalArrangement = Arrangement.spacedBy((4 * scaleFactor).dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy((4 * scaleFactor).dp),
                    ) {
                        slots.forEach { slot ->
                            SlotClickableBox(slot = slot, isFloating = isFloating, onSlotClick = onSlotClick) {
                                InfoChip(slot = slot, scaleFactor = scaleFactor)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LABEL_BOARD: 메모장/표지판 형태. 텍스트가 주인공, 장식은 상단 우측 모서리.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LabelBoardTemplate(
    widget: AiSketchWidget,
    scaleFactor: Float,
    isFloating: Boolean,
    onSlotClick: ((AiTextSlot) -> Unit)?,
) {
    val slots = widget.textSlots

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CardShape)
            .background(Color(0xFFFFF8EE).copy(alpha = 0.92f)),
    ) {
        // 헤더 라인 상단
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((3 * scaleFactor).dp)
                .background(Color(0xFFE8A857).copy(alpha = 0.85f)),
        )

        // 우상단 모서리 장식 이미지
        if (widget.decorationBitmap != null) {
            Image(
                bitmap = widget.decorationBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size((48 * scaleFactor).dp)
                    .padding(top = (6 * scaleFactor).dp, end = (6 * scaleFactor).dp),
            )
        }

        // 정보 목록
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = (12 * scaleFactor).dp,
                    end = if (widget.decorationBitmap != null) (52 * scaleFactor).dp else (12 * scaleFactor).dp,
                    top = (10 * scaleFactor).dp,
                    bottom = (10 * scaleFactor).dp,
                ),
            verticalArrangement = Arrangement.spacedBy((6 * scaleFactor).dp),
        ) {
            if (slots.isEmpty()) {
                Text(
                    text = "정보 없음",
                    color = Color(0xFF5A4A3A).copy(alpha = 0.5f),
                    fontSize = (12f * scaleFactor).sp,
                )
            } else {
                slots.forEach { slot ->
                    SlotClickableBox(slot = slot, isFloating = isFloating, onSlotClick = onSlotClick) {
                        LabelBoardRow(slot = slot, scaleFactor = scaleFactor)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 공통 컴포넌트
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InfoChip(slot: AiTextSlot, scaleFactor: Float) {
    Box(
        modifier = Modifier
            .clip(ChipShape)
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = (8 * scaleFactor).dp, vertical = (3 * scaleFactor).dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = slot.value,
                color = Color.White,
                fontSize = (13f * scaleFactor).sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
private fun LabelBoardRow(slot: AiTextSlot, scaleFactor: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
    ) {
        if (slot.label.isNotBlank()) {
            Text(
                text = slot.label,
                color = Color(0xFF7A6A5A),
                fontSize = (11f * scaleFactor).sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.width((52 * scaleFactor).dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = ":",
                color = Color(0xFF7A6A5A).copy(alpha = 0.6f),
                fontSize = (11f * scaleFactor).sp,
                modifier = Modifier.padding(end = (4 * scaleFactor).dp),
            )
        }
        Text(
            text = slot.value,
            color = Color(0xFF2A1A0A),
            fontSize = (13f * scaleFactor).sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DecorationPlaceholder(size: androidx.compose.ui.unit.Dp) {
    Icon(
        Icons.Filled.AutoAwesome,
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.20f),
        modifier = Modifier.size(size),
    )
}

/** 슬롯 클릭 가능 영역 래퍼. isFloating일 때(편집 모드)는 클릭 비활성화. */
@Composable
private fun SlotClickableBox(
    slot: AiTextSlot,
    isFloating: Boolean,
    onSlotClick: ((AiTextSlot) -> Unit)?,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .then(
                if (!isFloating && onSlotClick != null) {
                    Modifier.pointerInput(slot.label) {
                        detectTapGestures { onSlotClick(slot) }
                    }
                } else Modifier,
            ),
    ) {
        content()
    }
}
