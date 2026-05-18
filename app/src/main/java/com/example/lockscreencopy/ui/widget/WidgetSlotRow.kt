package com.example.lockscreencopy.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lockscreencopy.model.PlacedWidget
import com.example.lockscreencopy.model.WidgetSize

@Composable
fun WidgetSlotRow(
    placedWidgets: List<PlacedWidget>,
    isFloating: Boolean,
    slotSize: Dp,
    slotGap: Dp,
    onRemove: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val frame = if (isFloating) {
        Modifier
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .clickable { onAdd() }
    } else Modifier

    Row(
        horizontalArrangement = Arrangement.spacedBy(slotGap, Alignment.CenterHorizontally),
        modifier = Modifier.then(frame).fillMaxWidth(0.5f).height(slotSize + 2.dp),
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
    }
}
