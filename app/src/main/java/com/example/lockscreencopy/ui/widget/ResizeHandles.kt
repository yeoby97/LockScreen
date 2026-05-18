package com.example.lockscreencopy.ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

enum class Corner { TopStart, TopEnd, BottomStart, BottomEnd }
enum class Edge { Start, End, Top, Bottom }

@Composable
fun BoxScope.ResizeCornerHandle(
    corner: Corner,
    alignment: Alignment,
    onResize: (Float, Float) -> Unit,
) {
    val handleSize = 24.dp
    Box(
        modifier = Modifier
            .align(alignment)
            .offset(
                x = if (alignment == Alignment.TopStart || alignment == Alignment.BottomStart) (-handleSize / 2) else (handleSize / 2),
                y = if (alignment == Alignment.TopStart || alignment == Alignment.TopEnd) (-handleSize / 2) else (handleSize / 2),
            )
            .size(handleSize)
            .pointerInput(corner) {
                detectDragGestures { change, drag ->
                    change.consume()
                    val (dx, dy) = when (corner) {
                        Corner.TopStart    -> -drag.x to -drag.y
                        Corner.TopEnd      ->  drag.x to -drag.y
                        Corner.BottomStart -> -drag.x to  drag.y
                        Corner.BottomEnd   ->  drag.x to  drag.y
                    }
                    onResize(dx / 400f, dy / 400f)
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val stroke = 4.dp.toPx()
            val len = size.minDimension
            val cap = StrokeCap.Round
            when (corner) {
                Corner.TopStart -> {
                    drawLine(Color.White, Offset(0f, 0f),   Offset(len, 0f), stroke, cap)
                    drawLine(Color.White, Offset(0f, 0f),   Offset(0f, len), stroke, cap)
                }
                Corner.TopEnd -> {
                    drawLine(Color.White, Offset(len, 0f), Offset(0f, 0f),   stroke, cap)
                    drawLine(Color.White, Offset(len, 0f), Offset(len, len), stroke, cap)
                }
                Corner.BottomStart -> {
                    drawLine(Color.White, Offset(0f, len), Offset(len, len), stroke, cap)
                    drawLine(Color.White, Offset(0f, len), Offset(0f, 0f),   stroke, cap)
                }
                Corner.BottomEnd -> {
                    drawLine(Color.White, Offset(len, len), Offset(0f, len), stroke, cap)
                    drawLine(Color.White, Offset(len, len), Offset(len, 0f), stroke, cap)
                }
            }
        }
    }
}

@Composable
fun BoxScope.ResizeEdgeHandle(
    edge: Edge,
    alignment: Alignment,
    onResize: (Float, Float) -> Unit,
) {
    val handleThickness = 20.dp
    val handleLength = 40.dp
    Box(
        modifier = Modifier
            .align(alignment)
            .then(
                when (edge) {
                    Edge.Start, Edge.End -> Modifier.width(handleThickness).height(handleLength)
                    Edge.Top, Edge.Bottom -> Modifier.width(handleLength).height(handleThickness)
                },
            )
            .pointerInput(edge) {
                detectDragGestures { change, drag ->
                    change.consume()
                    when (edge) {
                        Edge.Start  -> onResize(-drag.x / 400f, 0f)
                        Edge.End    -> onResize( drag.x / 400f, 0f)
                        Edge.Top    -> onResize(0f, -drag.y / 400f)
                        Edge.Bottom -> onResize(0f,  drag.y / 400f)
                    }
                }
            },
    )
}

@Composable
fun BoxScope.ResizeHandles(onResize: (Float, Float, Float, Float) -> Unit) {
    ResizeCornerHandle(Corner.TopStart,    Alignment.TopStart)    { dx, dy -> onResize(dx, dy, 1f, 1f) }
    ResizeCornerHandle(Corner.TopEnd,      Alignment.TopEnd)      { dx, dy -> onResize(dx, dy, 0f, 1f) }
    ResizeCornerHandle(Corner.BottomStart, Alignment.BottomStart) { dx, dy -> onResize(dx, dy, 1f, 0f) }
    ResizeCornerHandle(Corner.BottomEnd,   Alignment.BottomEnd)   { dx, dy -> onResize(dx, dy, 0f, 0f) }
    ResizeEdgeHandle(Edge.Start,  Alignment.CenterStart)   { dx, _ -> onResize(dx, 0f, 1f, 0.5f) }
    ResizeEdgeHandle(Edge.End,    Alignment.CenterEnd)     { dx, _ -> onResize(dx, 0f, 0f, 0.5f) }
    ResizeEdgeHandle(Edge.Top,    Alignment.TopCenter)     { _, dy -> onResize(0f, dy, 0.5f, 1f) }
    ResizeEdgeHandle(Edge.Bottom, Alignment.BottomCenter)  { _, dy -> onResize(0f, dy, 0.5f, 0f) }
}
