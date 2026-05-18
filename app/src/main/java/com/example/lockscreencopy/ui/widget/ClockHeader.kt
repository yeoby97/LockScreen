package com.example.lockscreencopy.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ClockHeader(modifier: Modifier = Modifier) {
    val now = Calendar.getInstance().time
    val time = SimpleDateFormat("hh:mm", Locale.getDefault()).format(now)
    val date = SimpleDateFormat("M월 d일 EEEE", Locale.KOREAN).format(now)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = time,
            color = Color.White, fontSize = 64.sp,
            fontWeight = FontWeight.Light, letterSpacing = (-2).sp,
        )
        Text(
            text = date,
            color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
