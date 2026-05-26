package com.example.lockscreencopy.ui.picker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

enum class ShortcutChoice { RealWidget, FavoriteApp, Sketch, Text }

@Composable
fun ShortcutPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (ShortcutChoice) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 8.dp) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "추가할 항목 선택", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = Color.Black, modifier = Modifier.padding(bottom = 16.dp),
                )
                Button(
                    onClick = { onSelect(ShortcutChoice.RealWidget) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) { Text("앱 위젯") }
                Button(
                    onClick = { onSelect(ShortcutChoice.FavoriteApp) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) { Text("즐겨찾는 앱") }
                Button(
                    onClick = { onSelect(ShortcutChoice.Sketch) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) { Text("AI 스케치") }
                Button(
                    onClick = { onSelect(ShortcutChoice.Text) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) { Text("글 넣기") }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) { Text("취소") }
            }
        }
    }
}
