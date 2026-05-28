package com.example.lockscreencopy.ui

import android.appwidget.AppWidgetProviderInfo
import android.os.Build
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.lockscreencopy.model.AiSketchWidget
import com.example.lockscreencopy.model.FloatingWidget
import com.example.lockscreencopy.model.HostedAppWidget
import com.example.lockscreencopy.model.WidgetSize

/**
 * 위젯이 실제로 호스트될 때의 크기(dp). ghost(추천 미리보기) 크기를 이 값에
 * 맞춰야 사용자가 ghost 를 탭한 직후 자리/크기가 변하지 않는다.
 */
internal fun resolveWidgetSizeDp(info: AppWidgetProviderInfo, densityScale: Float): Pair<Int, Int> {
    val cellDp = 70
    var wDp = (info.minWidth / densityScale).toInt()
    var hDp = (info.minHeight / densityScale).toInt()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (info.targetCellWidth > 0) wDp = maxOf(wDp, info.targetCellWidth * cellDp)
        if (info.targetCellHeight > 0) hDp = maxOf(hDp, info.targetCellHeight * cellDp)
    }
    if (wDp <= 0) wDp = (info.minResizeWidth / densityScale).toInt()
    if (hDp <= 0) hDp = (info.minResizeHeight / densityScale).toInt()
    if (wDp <= 0) wDp = 110
    if (hDp <= 0) hDp = 110
    return wDp to hDp
}

internal fun resizeFloatingWidget(
    fw: FloatingWidget,
    deltaX: Float,
    deltaY: Float,
    anchorX: Float,
    anchorY: Float,
    density: Density,
): FloatingWidget {
    val baseW = if (fw.widget.size == WidgetSize.WIDE) 180.dp else 100.dp
    val baseH = 100.dp
    val minScaleX = (80f / baseW.value).coerceIn(0.3f, 1f)
    val minScaleY = (80f / baseH.value).coerceIn(0.3f, 1f)
    val newScaleX = (fw.scaleX + deltaX).coerceIn(minScaleX, 2.5f)
    val newScaleY = (fw.scaleY + deltaY).coerceIn(minScaleY, 2.5f)
    val realDeltaX = newScaleX - fw.scaleX
    val realDeltaY = newScaleY - fw.scaleY
    val dwPx = with(density) { baseW.toPx() } * realDeltaX
    val dhPx = with(density) { baseH.toPx() } * realDeltaY
    return fw.copy(
        scaleX = newScaleX,
        scaleY = newScaleY,
        offset = Offset(fw.offset.x - dwPx * anchorX, fw.offset.y - dhPx * anchorY),
    )
}

internal fun resizeAiSketchWidget(
    widget: AiSketchWidget,
    deltaX: Float,
    deltaY: Float,
    anchorX: Float,
    anchorY: Float,
    density: Density,
): AiSketchWidget {
    val minScaleX = (80f / widget.widthDp).coerceIn(0.2f, 1f)
    val minScaleY = (80f / widget.heightDp).coerceIn(0.2f, 1f)
    val newScaleX = (widget.scaleX + deltaX).coerceIn(minScaleX, 3f)
    val newScaleY = (widget.scaleY + deltaY).coerceIn(minScaleY, 3f)
    val realDeltaX = newScaleX - widget.scaleX
    val realDeltaY = newScaleY - widget.scaleY
    val dwPx = with(density) { widget.widthDp.dp.toPx() } * realDeltaX
    val dhPx = with(density) { widget.heightDp.dp.toPx() } * realDeltaY
    return widget.copy(
        scaleX = newScaleX,
        scaleY = newScaleY,
        offset = Offset(widget.offset.x - dwPx * anchorX, widget.offset.y - dhPx * anchorY),
    )
}

internal fun resizeHostedWidget(
    hw: HostedAppWidget,
    deltaX: Float,
    deltaY: Float,
    anchorX: Float,
    anchorY: Float,
    densityScale: Float,
): HostedAppWidget {
    val baseWDp = hw.widthPx / densityScale
    val baseHDp = hw.heightPx / densityScale
    val minScaleX = (80f / baseWDp).coerceIn(0.2f, 1f)
    val minScaleY = (80f / baseHDp).coerceIn(0.2f, 1f)
    val maxScaleX = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        if (hw.providerInfo.maxResizeWidth > 0) (hw.providerInfo.maxResizeWidth / densityScale) / baseWDp else 3.0f
    } else 3.0f
    val maxScaleY = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        if (hw.providerInfo.maxResizeHeight > 0) (hw.providerInfo.maxResizeHeight / densityScale) / baseHDp else 3.0f
    } else 3.0f
    val newScaleX = (hw.scaleX + deltaX).coerceIn(minScaleX, maxScaleX.coerceAtLeast(minScaleX))
    val newScaleY = (hw.scaleY + deltaY).coerceIn(minScaleY, maxScaleY.coerceAtLeast(minScaleY))
    val realDeltaX = newScaleX - hw.scaleX
    val realDeltaY = newScaleY - hw.scaleY
    val dwPx = hw.widthPx * realDeltaX
    val dhPx = hw.heightPx * realDeltaY
    return hw.copy(
        scaleX = newScaleX,
        scaleY = newScaleY,
        offset = Offset(hw.offset.x - dwPx * anchorX, hw.offset.y - dhPx * anchorY),
    )
}
