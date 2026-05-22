package com.example.lockscreencopy.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lockscreencopy.model.PlacedWidget
import com.example.lockscreencopy.model.WidgetSize
import com.example.lockscreencopy.ui.llm.GhostInstance

private val GhostBorder = Color(0xFFFFA000)

@Composable
fun WidgetSlotRow(
    placedWidgets: List<PlacedWidget>,
    isFloating: Boolean,
    slotSize: Dp,
    slotGap: Dp,
    onRemove: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    trayGhosts: List<GhostInstance> = emptyList(),
    consumedGhostKeys: Set<String> = emptySet(),
    onGhostTap: (GhostInstance) -> Unit = {},
) {
    val frame = if (isFloating) {
        Modifier
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .clickable { onAdd() }
    } else Modifier

    // 빈 칸에 들어갈 수 있는 ghost만 순서대로 추림
    val usedSpan = placedWidgets.sumOf { if (it.widget.size == WidgetSize.WIDE) 2 else 1 }
    val fittedGhosts = buildList<GhostInstance> {
        var remaining = 4 - usedSpan
        for (ghost in trayGhosts.filter { it.key !in consumedGhostKeys }) {
            val span = if (ghost.widget.size == WidgetSize.WIDE) 2 else 1
            if (remaining >= span) { add(ghost); remaining -= span }
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(slotGap, Alignment.CenterHorizontally),
        modifier = modifier.then(frame).fillMaxWidth(0.5f).height(slotSize + 2.dp),
    ) {
        placedWidgets.forEach { placed ->
            val span = if (placed.widget.size == WidgetSize.WIDE) 2 else 1
            Box(modifier = Modifier.width(slotSize * span)) {
                WidgetCell(
                    widget = placed.widget,
                    modifier = Modifier.fillMaxWidth().height(slotSize),
                )
                if (isFloating) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF453A))
                            .clickable { onRemove(placed.uid) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "제거",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }
        // LLM 추천 ghost를 빈 트레이 슬롯 위치에 표시
        if (isFloating) {
            fittedGhosts.forEach { ghost ->
                val span = if (ghost.widget.size == WidgetSize.WIDE) 2 else 1
                Box(
                    modifier = Modifier
                        .width(slotSize * span)
                        .height(slotSize)
                        .graphicsLayer { alpha = 0.55f }
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, GhostBorder, RoundedCornerShape(12.dp))
                        .clickable { onGhostTap(ghost) },
                ) {
                    WidgetCell(widget = ghost.widget, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
