package com.example.lockscreencopy.ui.space

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 위젯 공간을 만들기 위한 영역 스케치 오버레이.
 *
 * 화면을 어둡게 깔고 사용자가 대각선으로 직사각형을 그리면, 그 영역과 겹치는 위젯들을
 * 골라 공간으로 묶을지 확인한다. (겹침으로 자동 생성하지 않고 스케치로만 만드는 이유는
 * 잠금화면에서 위젯이 겹칠 일이 잦아 의도치 않은 묶음이 생기는 걸 막기 위함.)
 *
 * @param countInRect 화면 좌표 사각형에 겹치는(공간에 안 속한) 자유 위젯 개수.
 * @param onConfirm   확정 시 화면 좌표 사각형 전달. 좌표 변환·선택은 호출부가 담당.
 */
@Composable
fun SpaceSketchOverlay(
    countInRect: (Rect) -> Int,
    onCancel: () -> Unit,
    onConfirm: (Rect) -> Unit,
    addMode: Boolean = false,
) {
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }
    var rect by remember { mutableStateOf<Rect?>(null) }

    val density = LocalDensity.current
    val minSidePx = with(density) { 100.dp.toPx() }

    BackHandler {
        if (rect != null) rect = null else onCancel()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .then(
                if (rect == null) Modifier.pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragStart = offset
                            dragCurrent = offset
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragCurrent = change.position
                        },
                        onDragEnd = {
                            val s = dragStart
                            val c = dragCurrent
                            if (s != null && c != null) {
                                val left = min(s.x, c.x)
                                val top = min(s.y, c.y)
                                val right = max(s.x, c.x)
                                val bottom = max(s.y, c.y)
                                if (right - left >= minSidePx && bottom - top >= minSidePx) {
                                    rect = Rect(left, top, right, bottom)
                                }
                            }
                            dragStart = null
                            dragCurrent = null
                        },
                        onDragCancel = {
                            dragStart = null
                            dragCurrent = null
                        },
                    )
                } else Modifier,
            ),
    ) {
        if (rect == null) {
            Text(
                text = when {
                    dragStart != null -> "드래그 중..."
                    addMode -> "이 공간에 담을 위젯들을 감싸도록 드래그하세요"
                    else -> "묶을 위젯들을 감싸도록 영역을 드래그하세요"
                },
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp, start = 16.dp, end = 16.dp),
            )
            val s = dragStart
            val c = dragCurrent
            if (s != null && c != null) {
                val left = min(s.x, c.x)
                val top = min(s.y, c.y)
                val w = abs(c.x - s.x)
                val h = abs(c.y - s.y)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(left.roundToInt(), top.roundToInt()) }
                        .size(
                            width = with(density) { w.toDp() },
                            height = with(density) { h.toDp() },
                        )
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(2.dp, Color(0xFF7AC0FF), RoundedCornerShape(20.dp)),
                )
            }
            TextButton(
                onClick = onCancel,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
            ) { Text("취소", color = Color.White, fontSize = 14.sp) }
        }

        rect?.let { r ->
            val count = countInRect(r)
            // 그려진 영역을 유리 버블 느낌으로 강조
            Box(
                modifier = Modifier
                    .offset { IntOffset(r.left.roundToInt(), r.top.roundToInt()) }
                    .size(
                        width = with(density) { (r.right - r.left).toDp() },
                        height = with(density) { (r.bottom - r.top).toDp() },
                    )
                    .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(24.dp))
                    .border(2.dp, Color(0xFF7AC0FF), RoundedCornerShape(24.dp)),
            )

            // 확인 패널 — 하단 중앙
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp, start = 24.dp, end = 24.dp)
                    .background(Color(0xFF1C1C1E).copy(alpha = 0.92f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = when {
                        count == 0 -> "영역 안에 담을 위젯이 없어요. 다시 그려보세요."
                        addMode -> "이 영역의 위젯 ${count}개를 공간에 담을까요?"
                        else -> "이 영역의 위젯 ${count}개로 공간을 만들까요?"
                    },
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.size(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { rect = null }) {
                        Text("다시 그리기")
                    }
                    Button(
                        onClick = { onConfirm(r) },
                        enabled = count > 0,
                        modifier = Modifier.defaultMinSize(minWidth = 96.dp),
                    ) {
                        Text(if (addMode) "담기" else "만들기", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
