package com.example.lockscreencopy.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.ui.theme.LockTokens

private val BarShape = RoundedCornerShape(26.dp)

@Composable
fun LockStarBar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(BarShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2A3A8F), Color(0xFF141E5A)),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.18f), BarShape)
            .height(60.dp)
            .width(220.dp)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LockTokens.GlassWhite)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("12:45", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Color(0xFFFF4D8D),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Text("LockStar", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
