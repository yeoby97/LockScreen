package com.example.lockscreencopy.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val timeFmtClock = SimpleDateFormat("HH:mm", Locale.getDefault())
private val dateFmt = SimpleDateFormat("M월 d일 EEEE", Locale.KOREAN)

@Composable
fun ClockHeader(modifier: Modifier = Modifier, scale: Float = 1f) {
    var now by remember { mutableStateOf(Calendar.getInstance().time) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            now = Calendar.getInstance().time
        }
    }

    val time = timeFmtClock.format(now)
    val date = dateFmt.format(now)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = time,
            color = Color.White,
            fontSize = (96 * scale).sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-2 * scale).sp,
        )
        Text(
            text = date,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = (16 * scale).sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = (2 * scale).dp),
        )
    }
}
