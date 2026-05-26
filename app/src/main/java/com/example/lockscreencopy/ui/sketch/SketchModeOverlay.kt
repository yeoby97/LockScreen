package com.example.lockscreencopy.ui.sketch

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
 * 사용자가 직접 위치/크기를 스케치한 뒤 위젯 이름을 입력하면
 * 해당 텍스트로 AI 위젯을 추천받게 해주는 오버레이.
 *
 * Phase 1: 화면 어둡게 + 드래그로 직사각형 영역 지정
 * Phase 2: 직사각형 중앙에 텍스트 입력칸 + 확인/다시그리기 버튼
 */
@Composable
fun SketchModeOverlay(
    loading: Boolean,
    error: String?,
    onCancel: () -> Unit,
    onConfirm: (rect: Rect, query: String) -> Unit,
) {
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }
    var rect by remember { mutableStateOf<Rect?>(null) }
    var query by remember { mutableStateOf("") }

    val density = LocalDensity.current
    val minSidePx = with(density) { 140.dp.toPx() }

    BackHandler(enabled = !loading) {
        if (rect != null) {
            rect = null
            query = ""
        } else {
            onCancel()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .then(
                if (rect == null && !loading) Modifier.pointerInput(Unit) {
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
                text = if (dragStart == null) {
                    "위젯을 놓을 영역을 대각선으로 드래그하세요"
                } else {
                    "드래그 중..."
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
                        .border(2.dp, Color(0xFF4DAAED), RoundedCornerShape(8.dp)),
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
            val w = r.right - r.left
            val h = r.bottom - r.top
            Box(
                modifier = Modifier
                    .offset { IntOffset(r.left.roundToInt(), r.top.roundToInt()) }
                    .size(
                        width = with(density) { w.toDp() },
                        height = with(density) { h.toDp() },
                    )
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(2.dp, Color(0xFF4DAAED), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        placeholder = {
                            Text(
                                "위젯 이름/용도 (예: 날씨, 음악)",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                            )
                        },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            enabled = !loading,
                            onClick = {
                                rect = null
                                query = ""
                            },
                        ) { Text("다시 그리기", color = Color.White, fontSize = 12.sp) }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            enabled = !loading && query.isNotBlank(),
                            onClick = { onConfirm(r, query) },
                        ) {
                            Text(if (loading) "검색 중..." else "확인", fontSize = 12.sp)
                        }
                    }
                    if (loading) {
                        Spacer(Modifier.height(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                        )
                    }
                    error?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            it,
                            color = Color(0xFFFF6B6B),
                            fontSize = 11.sp,
                        )
                    }
                }
            }

            // 사각형 밖에도 취소 버튼 노출 (직사각형이 작거나 화면 가장자리일 때)
            TextButton(
                onClick = onCancel,
                enabled = !loading,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            ) { Text("취소", color = Color.White, fontSize = 14.sp) }
        }
    }
}
