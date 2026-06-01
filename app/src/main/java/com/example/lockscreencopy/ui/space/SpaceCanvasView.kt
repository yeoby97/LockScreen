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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.lockscreencopy.model.SpaceItemLayout
import com.example.lockscreencopy.ui.widget.ResizeHandles

/**
 * 위젯 공간의 캔버스를 그린다. 멤버는 각자의 [SpaceItemLayout](캔버스 dp 좌표·스케일)대로
 * 자유 배치되며, [displayScale]만 달리하면 같은 배치를 확장 뷰(≈1)와 버블(축소) 양쪽에서
 * 동일하게 보여줄 수 있다.
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
    onDragMember: (String, Offset) -> Unit = { _, _ -> },
    onResizeMember: (String, Float, Float, Float, Float) -> Unit = { _, _, _, _, _ -> },
    onRemoveMember: (String) -> Unit = {},
) {
    val densityScale = LocalDensity.current.density
    var selectedUid by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .size(
                width = (SpaceCanvas.WIDTH_DP * displayScale).dp,
                height = (SpaceCanvas.HEIGHT_DP * displayScale).dp,
            )
            .then(
                if (showFrame) Modifier
                    .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
                else Modifier,
            )
            // 빈 곳 탭 시 선택 해제
            .then(
                if (interactive) Modifier.pointerInput(Unit) {
                    detectTapGestures(onTap = { selectedUid = null })
                } else Modifier,
            ),
    ) {
        members.forEach { member ->
            val layout = layouts[member.uid] ?: defaultLayout(members.indexOf(member))
            val (baseW, baseH) = member.baseSizeDp(densityScale)
            val wDp = baseW * layout.scaleX * displayScale
            val hDp = baseH * layout.scaleY * displayScale

            Box(
                modifier = Modifier
                    .offset(
                        x = (layout.offset.x * displayScale).dp,
                        y = (layout.offset.y * displayScale).dp,
                    )
                    .size(width = wDp.dp, height = hDp.dp)
                    .then(
                        if (interactive) Modifier
                            .pointerInput(member.uid) {
                                detectTapGestures(onTap = {
                                    selectedUid = if (selectedUid == member.uid) null else member.uid
                                })
                            }
                            .pointerInput(member.uid, displayScale) {
                                detectDragGestures { change, drag ->
                                    change.consume()
                                    // 화면 px → 캔버스 dp 델타로 변환
                                    val dx = drag.x / densityScale / displayScale
                                    val dy = drag.y / densityScale / displayScale
                                    onDragMember(member.uid, Offset(dx, dy))
                                }
                            }
                        else Modifier,
                    ),
            ) {
                SpaceMemberView(
                    member = member,
                    appWidgetHost = appWidgetHost,
                    compact = compactContent,
                    modifier = Modifier.fillMaxSize(),
                )

                if (interactive) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
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

private fun defaultLayout(index: Int): SpaceItemLayout =
    SpaceItemLayout(offset = Offset(20f + index * 16f, 20f + index * 16f))

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
