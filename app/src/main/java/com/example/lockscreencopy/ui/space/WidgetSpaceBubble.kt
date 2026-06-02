package com.example.lockscreencopy.ui.space

import android.appwidget.AppWidgetHost
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.SpaceItemLayout
import com.example.lockscreencopy.model.WidgetSpace
import com.example.lockscreencopy.ui.widget.DeleteBadge
import kotlin.math.roundToInt

private val BubbleWidth = 100.dp
private val BubbleShape = RoundedCornerShape(22.dp)

/**
 * 접힌 위젯 공간 — 캔버스 비율(세로)에 맞춘 유리 버블. 내부에는 확장 뷰와 동일한 배치가
 * 그대로 축소되어(위젯 실제 모습 그대로) 프레임을 꽉 채운다. 이름은 폴더처럼 아래에 표시.
 *
 * - 평상시: 탭하면 [onTap] 으로 확장.
 * - float(편집) 모드: 드래그로 이동, 우상단 삭제 배지 노출.
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
            .width(BubbleWidth)
            .pointerInput(space.id) {
                detectTapGestures(onTap = { onTap() })
            }
            .pointerInput(isFloating, space.id) {
                if (isFloating) detectDragGestures { change, drag ->
                    change.consume(); onDrag(drag)
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 유리 본체 — 캔버스 비율로, 내부 배치가 프레임을 꽉 채움
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
        ) {
            if (members.isEmpty()) {
                Text(
                    "빈 공간",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val scale = maxWidth.value / SpaceCanvas.WIDTH_DP
                    SpaceCanvasView(
                        members = members,
                        layouts = layouts,
                        appWidgetHost = appWidgetHost,
                        displayScale = scale,
                        interactive = false,
                        compactContent = false,
                        showFrame = false,
                    )
                }
            }

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
