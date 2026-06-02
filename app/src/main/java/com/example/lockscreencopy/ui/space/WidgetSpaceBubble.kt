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
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlin.math.sqrt
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

/**
 * 위젯별 물리 상태.
 * dx/dy = 기본 위치 대비 추가 오프셋(dp), vx/vy = 선속도(dp/s),
 * rotation = 현재 각도(degrees), angularVel = 각속도(deg/s)
 */
private data class PhysicsItem(
    val dx: Float = 0f,
    val dy: Float = 0f,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val rotation: Float = 0f,
    val angularVel: Float = 0f,
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

    var physicsItems by remember(members.size) { mutableStateOf(List(members.size) { PhysicsItem() }) }
    var physicsActive by remember { mutableStateOf(false) }
    // 버블 내용 영역의 displayScale — BoxWithConstraints 에서 캡처
    var capturedDisplayScale by remember { mutableStateOf(1f) }
    val lastTapTimeRef = remember { LongArray(1) { 0L } }

    /**
     * 터치 위치(px, 버블 Box 로컬 좌표)를 캔버스 dp로 변환한 뒤,
     * 각 위젯에 방향별 충격량(impulse)과 회전 충격량(torque)을 가한다.
     * 이미 날아가는 중에 다시 탭하면 에너지가 누적돼 더 세게 튕김.
     */
    fun kick(touchPx: Offset) {
        val tx = touchPx.x / densityScale / capturedDisplayScale.coerceAtLeast(0.01f)
        val ty = touchPx.y / densityScale / capturedDisplayScale.coerceAtLeast(0.01f)

        physicsItems = physicsItems.mapIndexed { i, item ->
            val member = members.getOrNull(i) ?: return@mapIndexed item
            val layout = layouts[member.uid] ?: defaultSpaceLayout(i)
            val (baseW, baseH) = member.baseSizeDp(densityScale)
            val wDp = baseW * layout.scaleX
            val hDp = baseH * layout.scaleY

            // 위젯 중심 (현재 물리 오프셋 포함)
            val cx = layout.offset.x + item.dx + wDp / 2f
            val cy = layout.offset.y + item.dy + hDp / 2f

            // 터치 → 위젯 중심 방향으로 밀어내는 힘
            val rX = cx - tx
            val rY = cy - ty
            val dist = sqrt(rX * rX + rY * rY).coerceAtLeast(15f)
            val forceMag = (4500f / dist).coerceIn(250f, 900f)
            val forceX = (rX / dist) * forceMag
            val forceY = (rY / dist) * forceMag

            // 회전 충격량: 터치가 위젯 중심 기준 얼마나 옆에 있는지
            // 오른쪽 가장자리 터치 → normRX 크게 양수 → 시계 방향(+) 빙글
            val normRX = ((tx - cx) / (wDp / 2f).coerceAtLeast(1f)).coerceIn(-2.5f, 2.5f)
            val normRY = ((ty - cy) / (hDp / 2f).coerceAtLeast(1f)).coerceIn(-2.5f, 2.5f)
            val angularImpulse = normRX * forceMag * 0.6f - normRY * forceMag * 0.2f

            item.copy(
                vx = item.vx + forceX,
                vy = item.vy + forceY,
                angularVel = item.angularVel + angularImpulse,
            )
        }
        physicsActive = true
    }

    // 물리 시뮬레이션 루프 (physicsActive 가 true 로 바뀔 때마다 (재)시작)
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

                // 위치 스프링 + 공기 저항
                val springK = 5f
                val ax = -springK * item.dx
                val ay = -springK * item.dy
                var newVx = (item.vx + ax * dt) * exp(-2.5f * dt)
                var newVy = (item.vy + ay * dt) * exp(-2.5f * dt)
                var newDx = item.dx + newVx * dt
                var newDy = item.dy + newVy * dt

                // 벽 충돌 — 60% 탄성 반사 (클립 해제 상태이므로 약간 넉넉하게)
                val minX = -layout.offset.x - wDp * 0.4f
                val maxX = SpaceCanvas.WIDTH_DP - layout.offset.x - wDp * 0.6f
                val minY = -layout.offset.y - hDp * 0.4f
                val maxY = SpaceCanvas.HEIGHT_DP - layout.offset.y - hDp * 0.6f
                if (newDx < minX) { newDx = minX; newVx = abs(newVx) * 0.6f }
                if (newDx > maxX) { newDx = maxX; newVx = -abs(newVx) * 0.6f }
                if (newDy < minY) { newDy = minY; newVy = abs(newVy) * 0.6f }
                if (newDy > maxY) { newDy = maxY; newVy = -abs(newVy) * 0.6f }

                // 각도 스프링 (0°로 복귀) + 각속도 감쇠
                val angularSpringK = 3f
                val angularAccel = -angularSpringK * item.rotation
                val newAngularVel = (item.angularVel + angularAccel * dt) * exp(-3f * dt)
                val newRotation = item.rotation + newAngularVel * dt

                item.copy(
                    dx = newDx, dy = newDy, vx = newVx, vy = newVy,
                    rotation = newRotation, angularVel = newAngularVel,
                )
            }

            val settled = physicsItems.all {
                abs(it.vx) < 3f && abs(it.vy) < 3f &&
                        abs(it.dx) < 0.8f && abs(it.dy) < 0.8f &&
                        abs(it.angularVel) < 5f && abs(it.rotation) < 1f
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
                // 물리 활성 중엔 clip 해제 → 위젯이 버블 밖으로 튀어나올 수 있음
                .then(if (!physicsActive) Modifier.clip(BubbleShape) else Modifier)
                // clip 없어도 배경/테두리는 shape 인자로 직접 그림
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.20f),
                            Color.White.copy(alpha = 0.07f),
                        ),
                    ),
                    BubbleShape,
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
                    capturedDisplayScale = scale
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
            // - 첫 번째 탭 → 열기
            // - 빠른 연속 탭(450ms 이내) or 길게 누르기 → 물리 발동 (터치 위치 전달)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(space.id) {
                        detectTapGestures(
                            onTap = { offset ->
                                val now = System.currentTimeMillis()
                                if (now - lastTapTimeRef[0] < 450L) {
                                    kick(offset)
                                } else {
                                    onTap()
                                }
                                lastTapTimeRef[0] = now
                            },
                            onLongPress = { offset -> kick(offset) },
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
 * 버블 전용 캔버스.
 * 평상시: sine 파형 둥둥 애니메이션.
 * 물리 활성 시: [physicsItems]의 오프셋·회전이 추가로 적용되어 위젯들이 튕기며 돌아간다.
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
                    .requiredSize(width = wDp.dp, height = hDp.dp)
                    // 회전은 위젯 중심 기준 (TransformOrigin 기본값 = 0.5, 0.5)
                    .graphicsLayer { rotationZ = physics.rotation },
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
