package com.example.lockscreencopy.ui.space

import android.appwidget.AppWidgetHost
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.geometry.Offset
import com.example.lockscreencopy.model.AiSketchWidget
import com.example.lockscreencopy.model.FloatingWidget
import com.example.lockscreencopy.model.HostedAppWidget
import com.example.lockscreencopy.model.SpaceItemLayout
import com.example.lockscreencopy.model.WidgetSize
import com.example.lockscreencopy.ui.widget.AiSketchStatic
import com.example.lockscreencopy.ui.widget.WidgetCell

/** 위젯 공간의 가상 캔버스 크기(dp). 확장 뷰·버블 모두 이 좌표계를 공유한다. */
object SpaceCanvas {
    const val WIDTH_DP = 300f
    const val HEIGHT_DP = 440f
}

/** 멤버 위젯의 기본(스케일 1) 크기를 dp 로 반환. Hosted 는 px→dp 변환에 [densityScale] 사용. */
fun SpaceMember.baseSizeDp(densityScale: Float): Pair<Float, Float> = when (this) {
    is SpaceMember.Floating ->
        (if (widget.widget.size == WidgetSize.WIDE) 180f else 100f) to 100f
    is SpaceMember.Ai -> widget.widthDp to widget.heightDp
    is SpaceMember.Hosted -> (widget.widthPx / densityScale) to (widget.heightPx / densityScale)
}

/**
 * 공간 캔버스(dp 좌표계) 안에서 멤버 리사이즈. 잠금화면 ResizeMath 와 동일한 방식
 * (앵커 기준 스케일 증감 + 반대편 고정)을 dp 단위로 적용한다.
 */
fun resizeSpaceItem(
    layout: SpaceItemLayout,
    baseWidthDp: Float,
    baseHeightDp: Float,
    deltaScaleX: Float,
    deltaScaleY: Float,
    anchorX: Float,
    anchorY: Float,
): SpaceItemLayout {
    val minSX = (60f / baseWidthDp).coerceIn(0.2f, 1f)
    val minSY = (60f / baseHeightDp).coerceIn(0.2f, 1f)
    val newSX = (layout.scaleX + deltaScaleX).coerceIn(minSX, 3f)
    val newSY = (layout.scaleY + deltaScaleY).coerceIn(minSY, 3f)
    val realDX = newSX - layout.scaleX
    val realDY = newSY - layout.scaleY
    val dwDp = baseWidthDp * realDX
    val dhDp = baseHeightDp * realDY
    return layout.copy(
        scaleX = newSX,
        scaleY = newSY,
        offset = Offset(layout.offset.x - dwDp * anchorX, layout.offset.y - dhDp * anchorY),
    )
}

/**
 * 위젯 공간에 담길 수 있는 멤버. 잠금화면 위 세 종류 위젯을 하나의 타입으로 추상화한다.
 * [aspectW]/[aspectH]는 종횡비 계산에만 쓰이므로 단위(dp/px)는 멤버 내부에서만 일치하면 된다.
 */
sealed interface SpaceMember {
    val uid: String
    val aspectW: Float
    val aspectH: Float

    data class Floating(val widget: FloatingWidget) : SpaceMember {
        override val uid get() = widget.uid
        override val aspectW get() = if (widget.widget.size == WidgetSize.WIDE) 180f else 100f
        override val aspectH get() = 100f
    }

    data class Hosted(val widget: HostedAppWidget) : SpaceMember {
        override val uid get() = widget.uid
        override val aspectW get() = widget.widthPx.toFloat().coerceAtLeast(1f)
        override val aspectH get() = widget.heightPx.toFloat().coerceAtLeast(1f)
    }

    data class Ai(val widget: AiSketchWidget) : SpaceMember {
        override val uid get() = widget.uid
        override val aspectW get() = widget.widthDp.coerceAtLeast(1f)
        override val aspectH get() = widget.heightDp.coerceAtLeast(1f)
    }
}

/**
 * 공간 안에서 멤버 위젯의 내용만(편집 크롬 없이) 주어진 [modifier] 크기에 맞춰 그린다.
 *
 * @param compact true면 버블 썸네일용 가벼운 표현. 실 위젯(AndroidView)·텍스트 슬롯 인플레이트를
 *                생략해 작은 격자에서도 가볍고 깔끔하게 보이도록 한다.
 */
@Composable
fun SpaceMemberView(
    member: SpaceMember,
    appWidgetHost: AppWidgetHost?,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    when (member) {
        is SpaceMember.Floating ->
            WidgetCell(widget = member.widget.widget, modifier = modifier)

        is SpaceMember.Ai ->
            AiSketchStatic(widget = member.widget, modifier = modifier, showSlots = !compact)

        is SpaceMember.Hosted -> {
            val host = appWidgetHost
            if (compact || host == null) {
                Box(
                    modifier = modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF3A3A3C)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Widgets,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.75f),
                    )
                }
            } else {
                val hosted: HostedAppWidget = member.widget
                AndroidView(
                    factory = { ctx ->
                        host.createView(ctx, hosted.appWidgetId, hosted.providerInfo).apply {
                            setAppWidget(hosted.appWidgetId, hosted.providerInfo)
                        }
                    },
                    update = { view ->
                        val d = view.resources.displayMetrics.density
                        val wDp = (hosted.widthPx / d).toInt()
                        val hDp = (hosted.heightPx / d).toInt()
                        try {
                            view.updateAppWidgetSize(Bundle(), wDp, hDp, wDp, hDp)
                        } catch (_: Exception) {}
                    },
                    modifier = modifier.fillMaxSize(),
                )
            }
        }
    }
}
