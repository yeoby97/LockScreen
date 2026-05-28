package com.example.lockscreencopy.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.AiSketchWidget
import java.util.Calendar
import kotlin.math.roundToInt

@Composable
fun AiSketchWidgetItem(
    widget: AiSketchWidget,
    isFloating: Boolean,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onDrag: (Offset) -> Unit,
    onResize: (Float, Float, Float, Float) -> Unit,
    onDelete: () -> Unit,
) {
    val widthDp = (widget.widthDp * widget.scaleX).dp
    val heightDp = (widget.heightDp * widget.scaleY).dp

    Box(
        modifier = Modifier
            .offset { IntOffset(widget.offset.x.roundToInt(), widget.offset.y.roundToInt()) }
            .width(widthDp)
            .height(heightDp)
            .clip(RoundedCornerShape(16.dp))
            .pointerInput(isFloating, widget.uid) {
                if (isFloating) detectTapGestures(onTap = { onSelectToggle() })
            }
            .pointerInput(isFloating, widget.uid) {
                if (isFloating) detectDragGestures { change, drag ->
                    change.consume(); onDrag(drag)
                }
            },
    ) {
        // 배경: AI 생성 이미지 or 기본 반투명 배경
        if (widget.imageBitmap != null) {
            Image(
                bitmap = widget.imageBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            // 텍스트 가독성을 위한 반투명 오버레이
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1C1C2E).copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        // 정보 텍스트 오버레이
        InfoTextOverlay(
            items = widget.infoItems,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
        )

        // 편집 모드 테두리
        if (isFloating) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
            )
        }

        if (isFloating && isSelected) ResizeHandles(onResize)

        if (isFloating) DeleteBadge(onClick = onDelete)
    }
}

@Composable
private fun InfoTextOverlay(items: List<String>, modifier: Modifier = Modifier) {
    if (items.isEmpty()) return
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val spacing = if (items.size > 2) 4.dp else 8.dp
        Spacer(modifier = Modifier.weight(1f))
        items.forEach { item ->
            val label = item.trim()
            val value = sampleValueForInfo(label)
            Text(
                text = value,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(spacing))
        }
    }
}

private fun sampleValueForInfo(item: String): String {
    val lower = item.lowercase()
    return when {
        lower.contains("날씨") || lower.contains("weather") -> "맑음"
        lower.contains("온도") || lower.contains("기온") || lower.contains("temp") -> "23°C"
        lower.contains("습도") || lower.contains("humidity") -> "45%"
        lower.contains("시간") || lower.contains("time") -> {
            val cal = Calendar.getInstance()
            "%d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }
        lower.contains("날짜") || lower.contains("date") -> {
            val cal = Calendar.getInstance()
            "${cal.get(Calendar.MONTH) + 1}월 ${cal.get(Calendar.DAY_OF_MONTH)}일"
        }
        lower.contains("걸음") || lower.contains("step") -> "4,350"
        lower.contains("배터리") || lower.contains("battery") -> "72%"
        lower.contains("미세먼지") || lower.contains("dust") -> "좋음 15"
        lower.contains("자외선") || lower.contains("uv") -> "보통 5"
        lower.contains("강수") || lower.contains("rain") -> "10%"
        lower.contains("일출") || lower.contains("sunrise") -> "06:12"
        lower.contains("일몰") || lower.contains("sunset") -> "19:48"
        lower.contains("캘린더") || lower.contains("일정") || lower.contains("calendar") -> "일정 없음"
        else -> "--"
    }
}

