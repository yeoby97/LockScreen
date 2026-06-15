package com.example.lockscreencopy.ui.sketch

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.AiTextSlot
import kotlin.math.roundToInt

/**
 * 생성된 이미지와 함께 텍스트 슬롯을 표시하고, 사용자가 각 슬롯을 손가락으로
 * 드래그해 실제 오브젝트 위치에 맞출 수 있게 해주는 오버레이.
 *
 * 확인 → 조정된 slots로 위젯 최종 생성.
 * 취소 → 생성 결과를 버리고 종료.
 */
data class PendingSketch(
    val bitmap: Bitmap,
    val initialSlots: List<AiTextSlot>,
    val widgetRect: Rect,
    val widthDp: Float,
    val heightDp: Float,
)

private val slotTextShadow = Shadow(
    color = Color.Black.copy(alpha = 0.55f),
    offset = Offset(0f, 1f),
    blurRadius = 4f,
)

@Composable
fun SlotAdjustOverlay(
    pending: PendingSketch,
    onConfirm: (adjustedSlots: List<AiTextSlot>) -> Unit,
    onCancel: () -> Unit,
) {
    var slots by remember { mutableStateOf(pending.initialSlots) }
    val density = LocalDensity.current

    val widthPx = pending.widgetRect.right - pending.widgetRect.left
    val heightPx = pending.widgetRect.bottom - pending.widgetRect.top

    BackHandler { onCancel() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
    ) {
        Text(
            text = "텍스트를 드래그해 원하는 위치로 맞추세요",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp, start = 24.dp, end = 24.dp),
        )

        // 이미지 + 드래그 가능한 텍스트 슬롯
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        pending.widgetRect.left.roundToInt(),
                        pending.widgetRect.top.roundToInt(),
                    )
                }
                .size(
                    width = with(density) { widthPx.toDp() },
                    height = with(density) { heightPx.toDp() },
                ),
        ) {
            Image(
                bitmap = pending.bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )

            slots.forEachIndexed { index, slot ->
                val slotXDp = with(density) { (widthPx * slot.xRatio).toDp() }
                val slotYDp = with(density) { (heightPx * slot.yRatio).toDp() }
                val slotWDp = with(density) { (widthPx * slot.widthRatio).toDp() }
                val slotHDp = with(density) { (heightPx * slot.heightRatio).toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = slotXDp, y = slotYDp)
                        .size(width = slotWDp, height = slotHDp)
                        .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                        .pointerInput(index, widthPx, heightPx) {
                            detectDragGestures { change, drag ->
                                change.consume()
                                slots = slots.toMutableList().also { list ->
                                    val s = list[index]
                                    val newX = (s.xRatio + drag.x / widthPx)
                                        .coerceIn(0f, (1f - s.widthRatio).coerceAtLeast(0f))
                                    val newY = (s.yRatio + drag.y / heightPx)
                                        .coerceIn(0f, (1f - s.heightRatio).coerceAtLeast(0f))
                                    list[index] = s.copy(xRatio = newX, yRatio = newY)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val baseSp = 14f
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = slot.value,
                            color = Color.White,
                            fontSize = (baseSp * slot.fontScale).sp,
                            fontWeight = if (slot.role == "main") FontWeight.Bold else FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(shadow = slotTextShadow),
                        )
                        if (slot.label.isNotBlank()) {
                            Spacer(Modifier.height(1.dp))
                            Text(
                                text = slot.label,
                                color = Color.White.copy(alpha = 0.68f),
                                fontSize = (baseSp * 0.68f * slot.fontScale).sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(shadow = slotTextShadow),
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onCancel) {
                Text("취소", color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp)
            }
            Button(onClick = { onConfirm(slots) }) {
                Text("확인", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
