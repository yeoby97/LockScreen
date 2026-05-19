package com.example.lockscreencopy.ui.widget

import android.appwidget.AppWidgetHost
import android.os.Bundle
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.lockscreencopy.model.HostedAppWidget
import kotlin.math.roundToInt

@Composable
fun HostedWidgetItem(
    hosted: HostedAppWidget,
    isFloating: Boolean,
    isSelected: Boolean,
    appWidgetHost: AppWidgetHost?,
    onSelectToggle: () -> Unit,
    onDrag: (Offset) -> Unit,
    onResize: (Float, Float, Float, Float) -> Unit,
    onDelete: () -> Unit,
) {
    val density = LocalDensity.current
    val baseWidthDp = with(density) { hosted.widthPx.toDp() }
    val baseHeightDp = with(density) { hosted.heightPx.toDp() }

    Box(
        modifier = Modifier
            .offset { IntOffset(hosted.offset.x.roundToInt(), hosted.offset.y.roundToInt()) }
            .width(baseWidthDp * hosted.scaleX)
            .height(baseHeightDp * hosted.scaleY)
            .pointerInput(isFloating, hosted.uid) {
                if (isFloating) detectTapGestures(onTap = { onSelectToggle() })
            }
            .pointerInput(isFloating, hosted.uid) {
                if (isFloating) detectDragGestures { change, drag ->
                    change.consume(); onDrag(drag)
                }
            },
    ) {
        appWidgetHost?.let { host ->
            AndroidView(
                factory = { ctx ->
                    host.createView(ctx, hosted.appWidgetId, hosted.providerInfo).apply {
                        setAppWidget(hosted.appWidgetId, hosted.providerInfo)
                    }
                },
                update = { view ->
                    val d = view.resources.displayMetrics.density
                    val wDp = (hosted.widthPx / d * hosted.scaleX).toInt()
                    val hDp = (hosted.heightPx / d * hosted.scaleY).toInt()
                    try {
                        view.updateAppWidgetSize(Bundle(), wDp, hDp, wDp, hDp)
                    } catch (_: Exception) {}
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // 편집 상태에서 실제 위젯 클릭 차단 + 드래그 허용용 투명 레이어
        if (isFloating) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .pointerInput(hosted.uid) {
                        detectDragGestures { change, drag ->
                            change.consume(); onDrag(drag)
                        }
                    },
            )
        }

        if (isFloating && isSelected) ResizeHandles(onResize)

        if (isFloating) DeleteBadge(onClick = onDelete)
    }
}
