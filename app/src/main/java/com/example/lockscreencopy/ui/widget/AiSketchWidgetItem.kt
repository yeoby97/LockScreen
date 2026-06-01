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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.AiSketchWidget
import com.example.lockscreencopy.model.AiTextSlot
import kotlin.math.min
import kotlin.math.roundToInt

private val CornerShape = RoundedCornerShape(16.dp)
private val textShadow = Shadow(
    color = Color.Black.copy(alpha = 0.55f),
    offset = Offset(0f, 1f),
    blurRadius = 4f,
)

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

    // Outer Box — 인터랙션 + 삭제/리사이즈 담당. clip 없음 → X 버튼 잘리지 않음.
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
        // Inner Box — 이미지 + 슬롯 텍스트.
        // 투명 PNG가 배경화면에 그대로 녹아들도록 카드 배경/딤(dim)을 두지 않는다.
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (widget.imageBitmap != null) {
                Image(
                    bitmap = widget.imageBitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                // 이미지 없을 때 기본 배경
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1C1C2E).copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier.size(38.dp),
                    )
                }
            }

            // 슬롯 기반 텍스트 오버레이
            // isFloating=false일 때만 슬롯 클릭 활성화 (편집 모드에선 위젯 전체 드래그가 우선)
            SlotTextOverlay(
                slots = widget.textSlots,
                scaleX = widget.scaleX,
                scaleY = widget.scaleY,
                isInteractive = !isFloating,
                onSlotClick = onSlotClick,
            )
        }

        // 편집 모드 테두리 — clip 바깥에서 그려야 완전히 보임
        if (isFloating) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(1.dp, Color.White.copy(alpha = 0.65f), CornerShape),
            )
        }

        if (isFloating && isSelected) ResizeHandles(onResize)
        if (isFloating) DeleteBadge(onClick = onDelete)
    }
}

/**
 * 편집 크롬(테두리/삭제/리사이즈) 없이 이미지 + 슬롯 텍스트만 채워 그리는 정적 표현.
 * 위젯 공간(버블/확장 뷰) 안에서 AI 스케치 위젯을 미리보기로 보여줄 때 사용.
 */
@Composable
fun AiSketchStatic(
    widget: AiSketchWidget,
    modifier: Modifier = Modifier,
    showSlots: Boolean = true,
) {
    Box(modifier = modifier) {
        if (widget.imageBitmap != null) {
            Image(
                bitmap = widget.imageBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1C1C2E).copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.22f),
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        if (showSlots) {
            SlotTextOverlay(
                slots = widget.textSlots,
                scaleX = widget.scaleX,
                scaleY = widget.scaleY,
            )
        }
    }
}

/**
 * 슬롯 좌표(0.0~1.0 비율)를 `BoxWithConstraints`로 실제 dp 로 변환해 텍스트를 배치.
 * 이미지 생성 프롬프트의 negative space 위치와 1:1 대응하도록 설계됨.
 */
@Composable
private fun SlotTextOverlay(
    slots: List<AiTextSlot>,
    scaleX: Float = 1f,
    scaleY: Float = 1f,
    isInteractive: Boolean = false,
    onSlotClick: ((AiTextSlot) -> Unit)? = null,
) {
    if (slots.isEmpty()) return
    // 위젯 리사이즈 시 텍스트 크기를 자연스럽게 따라가되 너무 크거나 작아지지 않도록 clamp
    val scaleFactor = min(scaleX, scaleY).coerceIn(0.65f, 1.8f)
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        slots.forEach { slot ->
            val x = maxWidth * slot.xRatio
            val y = maxHeight * slot.yRatio
            val w = maxWidth * slot.widthRatio
            val h = maxHeight * slot.heightRatio
            val baseSp = 14f

            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .width(w)
                    .height(h)
                    .then(
                        if (isInteractive && onSlotClick != null) {
                            Modifier.pointerInput(slot.label) {
                                detectTapGestures { onSlotClick(slot) }
                            }
                        } else Modifier,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    val valueFontSize = (baseSp * slot.fontScale * scaleFactor).sp
                    val labelFontSize = (baseSp * 0.68f * slot.fontScale * scaleFactor).sp
                    val valueFontWeight = when (slot.role) {
                        "main" -> FontWeight.Bold
                        else -> FontWeight.SemiBold
                    }

                    Text(
                        text = slot.value,
                        color = Color.White,
                        fontSize = valueFontSize,
                        fontWeight = valueFontWeight,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(shadow = textShadow),
                    )
                    if (slot.label.isNotBlank()) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = slot.label,
                            color = Color.White.copy(alpha = 0.68f),
                            fontSize = labelFontSize,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(shadow = textShadow),
                        )
                    }
                }
            }
        }
    }
}
