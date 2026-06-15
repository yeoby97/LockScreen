package com.example.lockscreencopy.data

import com.example.lockscreencopy.model.AiTextSlot

/**
 * 정보 항목 수 + 가로/세로 비율에 따라 텍스트 슬롯의 상대좌표 레이아웃을 결정.
 * LLM 의존 없이 순수 규칙 기반으로 동작하므로 fallback 으로도 안전.
 */
fun designSlotLayout(
    items: List<Pair<String, String>>,
    aspectRatio: Float,
): List<AiTextSlot> {
    val isLandscape = aspectRatio > 1.3f
    return when (items.size) {
        0 -> emptyList()
        1 -> singleSlot(items[0])
        2 -> if (isLandscape) twoSlotsHorizontal(items) else twoSlotsVertical(items)
        3 -> if (isLandscape) threeSlotsHorizontal(items) else threeSlotsVertical(items)
        else -> fourSlotsGrid(items.take(4))
    }
}

// ── 1개 ──────────────────────────────────────────────────────────────────────

private fun singleSlot(item: Pair<String, String>) = listOf(
    slot(item, "main", xR = 0.08f, yR = 0.28f, wR = 0.84f, hR = 0.44f, fs = 1.7f),
)

// ── 2개 ──────────────────────────────────────────────────────────────────────

private fun twoSlotsHorizontal(items: List<Pair<String, String>>) = listOf(
    slot(items[0], "main", xR = 0.04f, yR = 0.16f, wR = 0.44f, hR = 0.68f, fs = 1.35f),
    slot(items[1], "main", xR = 0.52f, yR = 0.16f, wR = 0.44f, hR = 0.68f, fs = 1.35f),
)

private fun twoSlotsVertical(items: List<Pair<String, String>>) = listOf(
    slot(items[0], "main", xR = 0.08f, yR = 0.10f, wR = 0.84f, hR = 0.38f, fs = 1.45f),
    slot(items[1], "sub",  xR = 0.08f, yR = 0.55f, wR = 0.84f, hR = 0.34f, fs = 1.1f),
)

// ── 3개 ──────────────────────────────────────────────────────────────────────

private fun threeSlotsVertical(items: List<Pair<String, String>>) = listOf(
    slot(items[0], "title", xR = 0.08f, yR = 0.05f, wR = 0.84f, hR = 0.24f, fs = 0.85f),
    slot(items[1], "main",  xR = 0.08f, yR = 0.31f, wR = 0.84f, hR = 0.34f, fs = 1.45f),
    slot(items[2], "sub",   xR = 0.08f, yR = 0.68f, wR = 0.84f, hR = 0.25f, fs = 0.85f),
)

private fun threeSlotsHorizontal(items: List<Pair<String, String>>) = listOf(
    slot(items[0], "main",  xR = 0.04f, yR = 0.14f, wR = 0.30f, hR = 0.72f, fs = 1.15f),
    slot(items[1], "main",  xR = 0.35f, yR = 0.14f, wR = 0.30f, hR = 0.72f, fs = 1.15f),
    slot(items[2], "sub",   xR = 0.66f, yR = 0.14f, wR = 0.30f, hR = 0.72f, fs = 1.0f),
)

// ── 4개 ──────────────────────────────────────────────────────────────────────

private fun fourSlotsGrid(items: List<Pair<String, String>>) = listOf(
    slot(items[0], "extra", xR = 0.04f, yR = 0.07f, wR = 0.44f, hR = 0.38f, fs = 1.05f),
    slot(items[1], "extra", xR = 0.52f, yR = 0.07f, wR = 0.44f, hR = 0.38f, fs = 1.05f),
    slot(items[2], "extra", xR = 0.04f, yR = 0.55f, wR = 0.44f, hR = 0.38f, fs = 1.05f),
    slot(items[3], "extra", xR = 0.52f, yR = 0.55f, wR = 0.44f, hR = 0.38f, fs = 1.05f),
)

// ── helper ────────────────────────────────────────────────────────────────────

private fun slot(
    item: Pair<String, String>,
    role: String,
    xR: Float, yR: Float, wR: Float, hR: Float,
    fs: Float,
) = AiTextSlot(
    label = item.first,
    value = item.second,
    role = role,
    xRatio = xR, yRatio = yR,
    widthRatio = wR, heightRatio = hR,
    fontScale = fs,
)
