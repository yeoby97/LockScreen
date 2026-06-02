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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private val BubbleWidth = 104.dp
private val BubbleShape = RoundedCornerShape(20.dp)

private val FloatSeeds = listOf(
    Triple(3200, 2800, 0.00f),
    Triple(2700, 3500, 0.37f),
    Triple(3800, 2400, 0.61f),
    Triple(2500, 3100, 0.84f),
    Triple(3300, 2600, 0.15f),
)

/** 물리 시뮬레이션용 위젯별 상태. dx/dy = 기본 위치 대비 추가 오프셋(dp), vx/vy = 속도(dp/s) */
private data class PhysicsItem(
    val dx: Float = 0f,
    val dy: Float = 0f,
    val vx: Float = 0f,
    val vy: Float = 0f,
)

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
    val densityScale = LocalDensity.current.density

    // 멤버 수가 바뀌면 물리 상태 초기화
    var physicsItems by remember(members.size) { mutableStateOf(List(members.size) { PhysicsItem() }) }
    var physicsActive by remember { mutableStateOf(false) }
    // 연속 탭 감지 (recomposition 없이 갱신)
    val lastTapTimeRef = remember { LongArray(1) { 0L } }

    /** 현재 속도에 랜덤 충격(impulse)을 더해 물리를 시작/재점화한다. */
    fun kick() {
        physicsItems = physicsItems.mapIndexed { i, item ->
            val baseAngle = (i.toFloat() / members.size.coerceAtLeast(1)) * 2f * PI.toFloat()
            val scatter = (Random.nextFloat() - 0.5f) * PI.toFloat()
            val angle = baseAngle + scatter
            val speed = 200f + Random.nextFloat() * 250f
            // 이미 날아가는 중이면 속도를 더해 더 세게 튕김
            item.copy(
                vx = item.vx + cos(angle).toFloat() * speed,
                vy = item.vy + sin(angle).toFloat() * speed,
            )
        }
        physicsActive = true
    }

    // 물리 루프 — physicsActive 가 true 가 될 때마다 (재)시작
    LaunchedEffect(physicsActive) {
        if (!physicsActive) return@LaunchedEffect
        var lastMs = withFrameMillis { it }

        while (true) {
            val nowMs = withFrameMillis { it }
            val dt = ((nowMs - lastMs) / 1000f).coerceIn(0f, 0.05f)
            lastMs = nowMs

            physicsItems = physicsItems.mapIndexed { i, item ->
                val member = members.getOrNull(i) ?: return@mapIndexed item
                val layout = layouts[member.uid] ?: defaultSpaceLayout(i)
                val (baseW, baseH) = member.baseSizeDp(densityScale)
                val wDp = baseW * layout.scaleX
                val hDp = baseH * layout.scaleY

                // 스프링: 원래 위치(dx=0, dy=0)로 당기는 힘
                val springK = 5f
                val ax = -springK * item.dx
                val ay = -springK * item.dy

                // 속도 갱신 + 공기 저항(지수 감쇠)
                var newVx = (item.vx + ax * dt) * exp(-2.5f * dt)
                var newVy = (item.vy + ay * dt) * exp(-2.5f * dt)
                var newDx = item.dx + newVx * dt
                var newDy = item.dy + newVy * dt

                // 캔버스 벽 충돌 — 60% 탄성 반사
                val minX = -layout.offset.x
                val maxX = SpaceCanvas.WIDTH_DP - layout.offset.x - wDp
                val minY = -layout.offset.y
                val maxY = SpaceCanvas.HEIGHT_DP - layout.offset.y - hDp
                if (newDx < minX) { newDx = minX; newVx = abs(newVx) * 0.6f }
                if (newDx > maxX) { newDx = maxX; newVx = -abs(newVx) * 0.6f }
                if (newDy < minY) { newDy = minY; newVy = abs(newVy) * 0.6f }
                if (newDy > maxY) { newDy = maxY; newVy = -abs(newVy) * 0.6f }

                item.copy(dx = newDx, dy = newDy, vx = newVx, vy = newVy)
            }

            // 모두 정착하면 원위치 복귀 후 중단
            val settled = physicsItems.all {
                abs(it.vx) < 3f && abs(it.vy) < 3f &&
                        abs(it.dx) < 0.8f && abs(it.dy) < 0.8f
            }
            if (settled) {
                physicsItems = List(members.size) { PhysicsItem() }
                physicsActive = false
                break
            }
        }
    }

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
                        physicsItems = physicsItems,
                    )
                }
            }

            // 제스처 캡처 레이어
            // - 일반 탭 → 열기
            // - 빠른 연속 탭(450ms 내) or 길게 누르기 → 물리 시작
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(space.id) {
                        detectTapGestures(
                            onTap = {
                                val now = System.currentTimeMillis()
                                if (now - lastTapTimeRef[0] < 450L) {
                                    kick()
                                } else {
                                    onTap()
                                }
                                lastTapTimeRef[0] = now
                            },
                            onLongPress = { kick() },
                        )
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
 * 버블 전용 캔버스. 평상시엔 sine 파형 둥둥 애니메이션,
 * 물리 활성 시엔 [physicsItems] 의 추가 오프셋이 더해져 벽에 튕기며 날아다닌다.
 */
@Composable
private fun AnimatedBubbleCanvas(
    members: List<SpaceMember>,
    layouts: Map<String, SpaceItemLayout>,
    appWidgetHost: AppWidgetHost?,
    displayScale: Float,
    physicsItems: List<PhysicsItem>,
) {
    val densityScale = LocalDensity.current.density
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_float")

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
            val floatAmpX = remember(wDp) { (wDp * 0.06f).coerceIn(3f, 8f) }
            val floatAmpY = remember(hDp) { (hDp * 0.06f).coerceIn(3f, 8f) }
            val floatDx = sin(progX * 2.0 * PI).toFloat() * floatAmpX
            val floatDy = sin(progY * 2.0 * PI).toFloat() * floatAmpY

            val physics = physicsItems.getOrNull(i) ?: PhysicsItem()

            Box(
                modifier = Modifier
                    .offset(
                        x = (layout.offset.x + floatDx + physics.dx).dp,
                        y = (layout.offset.y + floatDy + physics.dy).dp,
                    )
                    .requiredSize(width = wDp.dp, height = hDp.dp),
            ) {
                SpaceMemberView(
                    member = member,
                    appWidgetHost = appWidgetHost,
                    compact = false,
                    modifier = Modifier.requiredSize(width = wDp.dp, height = hDp.dp),
                )
            }
        }
    }
}

private fun Modifier.scaledBubbleCanvas(scale: Float): Modifier = layout { measurable, _ ->
    val placeable = measurable.measure(androidx.compose.ui.unit.Constraints())
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
