package com.example.lockscreencopy.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.LockWidget
import com.example.lockscreencopy.model.WidgetSize

@Composable
fun WidgetCell(widget: LockWidget, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF3A3A3C)),
        contentAlignment = Alignment.Center,
    ) {
        when (widget.size) {
            WidgetSize.SMALL -> SmallWidgetContent(widget)
            WidgetSize.WIDE  -> WideWidgetContent(widget)
        }
    }
}

@Composable
fun SmallWidgetContent(widget: LockWidget) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(6.dp),
    ) {
        widget.icon?.let {
            Icon(it, null, tint = widget.iconTint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
        }
        if (widget.mainValue.isNotEmpty()) {
            Text(
                widget.mainValue,
                color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, lineHeight = 16.sp,
            )
        }
        if (widget.subValue.isNotEmpty()) {
            Text(
                widget.subValue,
                color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun WideWidgetContent(widget: LockWidget) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        widget.icon?.let {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(it, null, tint = widget.iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(8.dp))
        }
        Column {
            if (widget.mainValue.isNotEmpty()) {
                Text(
                    widget.mainValue,
                    color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            if (widget.subValue.isNotEmpty()) {
                Text(
                    widget.subValue,
                    color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
