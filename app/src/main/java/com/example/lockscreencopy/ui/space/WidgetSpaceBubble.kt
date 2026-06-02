package com.example.lockscreencopy.ui.space

import android.appwidget.AppWidgetHost
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.SpaceItemLayout
import com.example.lockscreencopy.model.WidgetSpace
import com.example.lockscreencopy.ui.widget.DeleteBadge
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

private val BubbleWidth = 104.dp
private val BubbleShape = RoundedCornerShape(20.dp)

// 위젯마다 다른 float 파라미터를 주기 위한 시드 배열
private val FloatSeeds = listOf(
    Triple(3200, 2800, 0.00f),
    Triple(2700, 3500, 0.37f),
    Triple(3800, 2400, 0.61f),
    Triple(2500, 3100, 0.84f),
    Triple(3300, 2600, 0.15f),
)

/**
 * 접힌 위젯 공간 버블. 내부 위젯들이 서로 다른 위상으로 둥둥 떠다니는 애니메이션을 보여준다.
 */
@Composable
fun WidgetSpaceBubble(
    space: WidgetSpace,
    members: List<SpaceMember>,
    layouts: Map<String, SpaceItemLayout>,
    appWidgetHost: AppWidgetHost?,
    isFloating: Boolean,
    onTap: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .offset { IntOffset(space.offset.x.roundToInt(), space.offset.y.roundToInt()) }
            .width(BubbleWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(SpaceCanvas.ASPECT)
                .clip(BubbleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.20f),
                            Color.White.copy(alpha = 0.07f),
                        ),
                    ),
                )
                .border(1.dp, Color.White.copy(alpha = 0.5f), BubbleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (members.isEmpty()) {
                Text(
                    "빈 공간",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                )
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val scale = maxWidth.value / SpaceCanvas.WIDTH_DP
                    AnimatedBubbleCanvas(
                        members = members,
                        layouts = layouts,
                        appWidgetHost = appWidgetHost,
                        displayScale = scale,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(space.id) {
                        detectTapGestures(onTap = { onTap() })
                    }
                    .pointerInput(isFloating, space.id) {
                        if (isFloating) detectDragGestures { change, drag ->
                            change.consume(); onDrag(drag)
                        }
                    },
            )

            if (isFloating) DeleteBadge(onClick = onDelete)
        }

        Spacer(Modifier.height(3.dp))
        Text(
            text = space.name,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * 버블 전용 캔버스 — 각 위젯이 고유한 주기/위상으로 X·Y 방향 sine 파형을 따라
 * 둥둥 떠다니는 것처럼 보인다. [displayScale] 로 전체 캔버스를 축소해 버블에 꽉 채운다.
 */
@Composable
private fun AnimatedBubbleCanvas(
    members: List<SpaceMember>,
    layouts: Map<String, SpaceItemLayout>,
    appWidgetHost: AppWidgetHost?,
    displayScale: Float,
) {
    val densityScale = LocalDensity.current.density
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_float")

    // 멤버별 0→1 진행 애니메이션 (duration 이 달라 서로 어긋남)
    val progresses = members.mapIndexed { i, member ->
        val seed = FloatSeeds[i % FloatSeeds.size]
        val progress by infiniteTransition.animateFloat(
            initialValue = seed.third,
            targetValue = seed.third + 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = seed.first, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "float_${member.uid}",
        )
        // Y 는 살짝 다른 주기
        val progressY by infiniteTransition.animateFloat(
            initialValue = seed.third + 0.5f,
            targetValue = seed.third + 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = seed.second, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "floatY_${member.uid}",
        )
        Pair(progress, progressY)
    }

    Box(
        modifier = Modifier
            .scaledBubbleCanvas(displayScale)
            .requiredSize(width = SpaceCanvas.WIDTH_DP.dp, height = SpaceCanvas.HEIGHT_DP.dp),
    ) {
        members.forEachIndexed { i, member ->
            val layout = layouts[member.uid] ?: defaultSpaceLayout(i)
            val (baseW, baseH) = member.baseSizeDp(densityScale)
            val wDp = baseW * layout.scaleX
            val hDp = baseH * layout.scaleY

            val (progX, progY) = progresses[i]
            // sine 파형으로 ±floatAmp dp 범위 내에서 부드럽게 왕복
            val floatAmpX = remember(wDp) { (wDp * 0.06f).coerceIn(3f, 8f) }
            val floatAmpY = remember(hDp) { (hDp * 0.06f).coerceIn(3f, 8f) }
            val dxDp = sin(progX * 2.0 * PI).toFloat() * floatAmpX
            val dyDp = sin(progY * 2.0 * PI).toFloat() * floatAmpY

            Box(
                modifier = Modifier
                    .offset(
                        x = (layout.offset.x + dxDp).dp,
                        y = (layout.offset.y + dyDp).dp,
                    )
                    .requiredSize(width = wDp.dp, height = hDp.dp),
            ) {
                SpaceMemberView(
                    member = member,
                    appWidgetHost = appWidgetHost,
                    compact = false,
                    modifier = Modifier
                        .requiredSize(width = wDp.dp, height = hDp.dp),
                )
            }
        }
    }
}

private fun Modifier.scaledBubbleCanvas(scale: Float): Modifier = layout { measurable, _ ->
    val placeable = measurable.measure(
        androidx.compose.ui.unit.Constraints()
    )
    val w = (placeable.width * scale).roundToInt().coerceAtLeast(0)
    val h = (placeable.height * scale).roundToInt().coerceAtLeast(0)
    layout(w, h) {
        placeable.placeWithLayer(0, 0) {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, 0f)
        }
    }
}
