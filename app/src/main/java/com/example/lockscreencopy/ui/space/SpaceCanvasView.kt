package com.example.lockscreencopy.ui.space

import android.appwidget.AppWidgetHost
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.lockscreencopy.model.SpaceItemLayout
import com.example.lockscreencopy.ui.widget.ResizeHandles

/**
 * 위젯 공간의 캔버스를 그린다.
 *
 * 핵심: 멤버는 항상 "원래(풀) 크기"로 배치/렌더되고, 캔버스 전체를 [graphicsLayer] 로
 * [displayScale] 만큼 시각적으로만 축소한다(잠금화면 메인 박스와 동일한 방식).
 * 따라서 위젯은 작은 컨테이너에 맞춰 레이아웃을 다시 짜지(reflow) 않고, float 모드처럼
 * 모양 그대로 스케일만 작아진다. 같은 배치를 확장 뷰(≈1)와 버블(축소)이 동일하게 보여준다.
 *
 * graphicsLayer 안쪽이므로 드래그/리사이즈 델타는 캔버스 로컬(풀) 좌표로 들어온다 →
 * displayScale 로 나눌 필요 없이 그대로(px→dp만 변환) 적용한다.
 *
 * @param interactive   true면 드래그/리사이즈/선택/빼기 가능(확장 뷰). false면 표시 전용(버블).
 * @param compactContent 멤버 내용을 가볍게 그릴지(버블 썸네일). 확장 뷰는 false 로 실물 렌더링.
 */
@Composable
fun SpaceCanvasView(
    members: List<SpaceMember>,
    layouts: Map<String, SpaceItemLayout>,
    appWidgetHost: AppWidgetHost?,
    displayScale: Float,
    interactive: Boolean,
    compactContent: Boolean,
    showFrame: Boolean,
    modifier: Modifier = Modifier,
    contentBounds: Rect? = null,
    onDragMember: (String, Offset) -> Unit = { _, _ -> },
    onResizeMember: (String, Float, Float, Float, Float) -> Unit = { _, _, _, _, _ -> },
    onRemoveMember: (String) -> Unit = {},
) {
    val densityScale = LocalDensity.current.density
    var selectedUid by remember { mutableStateOf<String?>(null) }

    // 표시 영역(뷰포트): 지정 시 그 영역만, 없으면 전체 캔버스.
    val viewLeft = contentBounds?.left ?: 0f
    val viewTop = contentBounds?.top ?: 0f
    val viewW = contentBounds?.width ?: SpaceCanvas.WIDTH_DP
    val viewH = contentBounds?.height ?: SpaceCanvas.HEIGHT_DP

    // 바깥 박스는 축소된 발자국(footprint)만 차지하고, 안쪽 풀사이즈 캔버스를 graphicsLayer 로 축소.
    Box(
        modifier = modifier.size(
            width = (viewW * displayScale).dp,
            height = (viewH * displayScale).dp,
        ),
    ) {
        Box(
            modifier = Modifier
                .size(width = SpaceCanvas.WIDTH_DP.dp, height = SpaceCanvas.HEIGHT_DP.dp)
                .graphicsLayer {
                    scaleX = displayScale
                    scaleY = displayScale
                    transformOrigin = TransformOrigin(0f, 0f)
                    // 뷰포트 좌상단이 footprint 좌상단(0,0)에 오도록 평행이동
                    translationX = -viewLeft.dp.toPx() * displayScale
                    translationY = -viewTop.dp.toPx() * displayScale
                }
                .then(
                    if (showFrame) Modifier
                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
                    else Modifier,
                )
                .then(
                    if (interactive) Modifier.pointerInput(Unit) {
                        detectTapGestures(onTap = { selectedUid = null })
                    } else Modifier,
                ),
        ) {
            members.forEach { member ->
                val layout = layouts[member.uid] ?: defaultSpaceLayout(members.indexOf(member))
                val (baseW, baseH) = member.baseSizeDp(densityScale)
                val wDp = baseW * layout.scaleX
                val hDp = baseH * layout.scaleY

                Box(
                    modifier = Modifier
                        .offset(x = layout.offset.x.dp, y = layout.offset.y.dp)
                        .size(width = wDp.dp, height = hDp.dp),
                ) {
                    SpaceMemberView(
                        member = member,
                        appWidgetHost = appWidgetHost,
                        compact = compactContent,
                        modifier = Modifier.fillMaxSize(),
                    )

                    if (interactive) {
                        // 실 위젯(AndroidView)이 터치를 가로채지 못하도록 위에 투명 캡처 레이어
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .pointerInput(member.uid) {
                                    detectTapGestures(onTap = {
                                        selectedUid = if (selectedUid == member.uid) null else member.uid
                                    })
                                }
                                .pointerInput(member.uid) {
                                    detectDragGestures { change, drag ->
                                        change.consume()
                                        // graphicsLayer 로컬(풀) 좌표 → 캔버스 dp 델타
                                        onDragMember(
                                            member.uid,
                                            Offset(drag.x / densityScale, drag.y / densityScale),
                                        )
                                    }
                                }
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = if (selectedUid == member.uid) 0.9f else 0.5f),
                                    RoundedCornerShape(12.dp),
                                ),
                        )
                        if (selectedUid == member.uid) {
                            ResizeHandles { dx, dy, ax, ay -> onResizeMember(member.uid, dx, dy, ax, ay) }
                        }
                        RemoveBadge { onRemoveMember(member.uid) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.RemoveBadge(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 6.dp, y = (-6).dp)
            .size(22.dp)
            .clip(CircleShape)
            .background(Color(0xFFFF453A))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.Close,
            contentDescription = "공간에서 빼기",
            tint = Color.White,
            modifier = Modifier.size(12.dp),
        )
    }
}
