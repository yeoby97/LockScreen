package com.example.lockscreencopy.ui.sketch

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.AiSketchWidget
import com.example.lockscreencopy.model.AiTextSlot
import com.example.lockscreencopy.model.AiWidgetTemplateType
import com.example.lockscreencopy.ui.widget.AiSketchWidgetItem

/**
 * AI 생성 장식 이미지 + 템플릿 타입을 담는 스케치 대기 상태.
 */
data class PendingSketch(
    val bitmap: Bitmap,
    val templateType: AiWidgetTemplateType,
    val initialSlots: List<AiTextSlot>,
    val widgetRect: Rect,
    val widthDp: Float,
    val heightDp: Float,
)

/**
 * 생성된 위젯 미리보기를 표시하고 사용자가 확인 또는 취소하게 한다.
 * 템플릿 기반이므로 슬롯 드래그 조정은 없고, 최종 배치 전 미리보기 용도.
 */
@Composable
fun SlotAdjustOverlay(
    pending: PendingSketch,
    onConfirm: (adjustedSlots: List<AiTextSlot>) -> Unit,
    onCancel: () -> Unit,
) {
    BackHandler { onCancel() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
    ) {
        Text(
            text = "위젯 미리보기 — 이렇게 배치됩니다",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp, start = 24.dp, end = 24.dp),
        )

        // 템플릿 기반 위젯 미리보기 (실제 AiSketchWidgetItem 재사용)
        val previewWidget = AiSketchWidget(
            uid = "preview",
            templateType = pending.templateType,
            decorationBitmap = pending.bitmap,
            textSlots = pending.initialSlots,
            offset = Offset(
                pending.widgetRect.left,
                pending.widgetRect.top,
            ),
            widthDp = pending.widthDp,
            heightDp = pending.heightDp,
        )

        AiSketchWidgetItem(
            widget = previewWidget,
            isFloating = false,
            isSelected = false,
            onSelectToggle = {},
            onDrag = {},
            onResize = { _, _, _, _ -> },
            onDelete = {},
            onSlotClick = null,
        )

        // 하단 버튼
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onCancel) {
                Text("다시 만들기", color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp)
            }
            Button(onClick = { onConfirm(pending.initialSlots) }) {
                Text("배치하기", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
