package com.example.lockscreencopy.ui.llm

import androidx.compose.ui.geometry.Offset

data class RectBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

private fun RectBounds.overlaps(other: RectBounds): Boolean =
    left < other.right && right > other.left && top < other.bottom && bottom > other.top

fun findAvailableGhostOffset(
    index: Int,
    ghostWidthPx: Float,
    ghostHeightPx: Float,
    screenWidthPx: Float,
    screenHeightPx: Float,
    occupied: List<RectBounds>,
): Offset {
    val margin = 24f
    val minX = margin
    val maxX = (screenWidthPx - ghostWidthPx - margin).coerceAtLeast(minX)
    val minY = screenHeightPx * 0.22f
    val maxY = (screenHeightPx - ghostHeightPx - margin).coerceAtLeast(minY)
    val stepX = (ghostWidthPx + 20f).coerceAtLeast(80f)
    val stepY = (ghostHeightPx + 20f).coerceAtLeast(80f)

    val cols = (((maxX - minX) / stepX).toInt() + 1).coerceAtLeast(1)
    val rows = (((maxY - minY) / stepY).toInt() + 1).coerceAtLeast(1)
    val total = cols * rows
    val start = if (total == 0) 0 else index % total

    repeat(total) { turn ->
        val linear = (start + turn) % total
        val row = linear / cols
        val col = linear % cols
        val x = (minX + col * stepX).coerceIn(minX, maxX)
        val y = (minY + row * stepY).coerceIn(minY, maxY)
        val candidate = RectBounds(x, y, x + ghostWidthPx, y + ghostHeightPx)
        if (occupied.none { it.overlaps(candidate) }) return Offset(x, y)
    }
    return Offset(minX, minY)
}

fun placeGhostRects(
    ghostSizes: List<Pair<Float, Float>>,
    screenWidthPx: Float,
    screenHeightPx: Float,
    occupied: List<RectBounds>,
): List<RectBounds> {
    val taken = occupied.toMutableList()
    val placed = mutableListOf<RectBounds>()
    ghostSizes.forEachIndexed { idx, (w, h) ->
        val offset = findAvailableGhostOffset(
            index = idx,
            ghostWidthPx = w,
            ghostHeightPx = h,
            screenWidthPx = screenWidthPx,
            screenHeightPx = screenHeightPx,
            occupied = taken,
        )
        val rect = RectBounds(offset.x, offset.y, offset.x + w, offset.y + h)
        placed += rect
        taken += rect
    }
    return placed
}
