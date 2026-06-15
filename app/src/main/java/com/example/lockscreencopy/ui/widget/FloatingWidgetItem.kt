package com.example.lockscreencopy.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.lockscreencopy.model.FloatingWidget
import com.example.lockscreencopy.model.WidgetSize
import com.example.lockscreencopy.ui.theme.LockTokens
import kotlin.math.roundToInt

@Composable
fun FloatingWidgetItem(
    placed: FloatingWidget,
    isFloating: Boolean,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onDrag: (Offset) -> Unit,
    onResize: (Float, Float, Float, Float) -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(placed.offset.x.roundToInt(), placed.offset.y.roundToInt()) }
            .width((if (placed.widget.size == WidgetSize.WIDE) 180.dp else 100.dp) * placed.scaleX)
            .height(100.dp * placed.scaleY)
            .pointerInput(isFloating, placed.uid) {
                if (isFloating) detectTapGestures(onTap = { onSelectToggle() })
            }
            .pointerInput(isFloating, placed.uid) {
                if (isFloating) detectDragGestures { change, drag ->
                    change.consume(); onDrag(drag)
                }
            },
    ) {
        WidgetCell(widget = placed.widget, modifier = Modifier.fillMaxSize())

        if (isFloating) {
            Box(
                modifier = Modifier.matchParentSize()
                    .border(
                        1.dp,
                        if (isSelected) LockTokens.BorderStrong else LockTokens.Border,
                        RoundedCornerShape(14.dp),
                    ),
            )
        }

        if (isFloating && isSelected) ResizeHandles(onResize)

        if (isFloating) DeleteBadge(onClick = onDelete)
    }
}

@Composable
fun BoxScope.DeleteBadge(onClick: () -> Unit) {
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
            imageVector = Icons.Filled.Close,
            contentDescription = "삭제",
            tint = Color.White,
            modifier = Modifier.size(12.dp),
        )
    }
}
