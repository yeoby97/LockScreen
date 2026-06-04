package com.example.lockscreencopy.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BarShape = RoundedCornerShape(26.dp)
private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

@Composable
fun LockStarBar(modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf(timeFmt.format(Date())) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            currentTime = timeFmt.format(Date())
        }
    }

    Box(
        modifier = modifier
            .clip(BarShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2A3A8F), Color(0xFF141E5A)),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.18f), BarShape)
            .height(54.dp)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = Color(0xFFFF4D8D),
                modifier = Modifier.size(11.dp),
            )
            Text(
                text = "LockStar",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
