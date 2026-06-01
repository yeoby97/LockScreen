package com.example.lockscreencopy.ui.space

import android.appwidget.AppWidgetHost
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.WidgetSpace
import com.example.lockscreencopy.ui.widget.DeleteBadge
import kotlin.math.roundToInt

private val BubbleSize = 104.dp
private val BubbleShape = RoundedCornerShape(28.dp)

/**
 * 접힌 위젯 공간 — 비눗방울/유리 같은 반투명 버블 안에 멤버 위젯들이 작게 들어가 있다.
 *
 * - 평상시: 탭하면 [onTap] 으로 확장.
 * - float(편집) 모드: 드래그로 이동, 우상단 삭제 배지 노출. 탭하면 확장되어 내부를 편집.
 */
@Composable
fun WidgetSpaceBubble(
    space: WidgetSpace,
    members: List<SpaceMember>,
    appWidgetHost: AppWidgetHost?,
    isFloating: Boolean,
    onTap: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(space.offset.x.roundToInt(), space.offset.y.roundToInt()) }
            .size(BubbleSize)
            .pointerInput(space.id) {
                detectTapGestures(onTap = { onTap() })
            }
            .pointerInput(isFloating, space.id) {
                if (isFloating) detectDragGestures { change, drag ->
                    change.consume(); onDrag(drag)
                }
            },
    ) {
        // 유리/비눗방울 본체: 위→아래로 옅어지는 반투명 화이트 + 외곽선
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(BubbleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.08f),
                        ),
                    ),
                )
                .border(1.dp, Color.White.copy(alpha = 0.55f), BubbleShape)
                .padding(7.dp),
        ) {
            Column(Modifier.fillMaxSize()) {
                MiniGrid(
                    members = members,
                    appWidgetHost = appWidgetHost,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = space.name,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (isFloating) DeleteBadge(onClick = onDelete)
    }
}

/** 버블 내부 2×2 썸네일 격자. 4개 초과 시 마지막 칸은 +N 배지. */
@Composable
private fun MiniGrid(
    members: List<SpaceMember>,
    appWidgetHost: AppWidgetHost?,
    modifier: Modifier = Modifier,
) {
    val shown = members.take(4)
    val overflow = members.size - shown.size
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        for (rowStart in 0 until 2) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                for (col in 0 until 2) {
                    val idx = rowStart * 2 + col
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        val isLastShownCell = idx == 3 && overflow > 0
                        when {
                            isLastShownCell -> Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "+${overflow + 1}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            idx < shown.size -> Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                            ) {
                                SpaceMemberView(
                                    member = shown[idx],
                                    appWidgetHost = appWidgetHost,
                                    compact = true,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
