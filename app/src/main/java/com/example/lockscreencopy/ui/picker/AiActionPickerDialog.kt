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

enum class AiActionChoice { LlmLayout, Sketch }

@Composable
fun AiActionPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (AiActionChoice) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 8.dp) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "AI 위젯 도우미", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = Color.Black, modifier = Modifier.padding(bottom = 16.dp),
                )
                Button(
                    onClick = { onSelect(AiActionChoice.LlmLayout) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) { Text("AI 위젯 배치") }
                Button(
                    onClick = { onSelect(AiActionChoice.Sketch) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) { Text("AI 스케치 (직접 위치 지정)") }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) { Text("취소") }
            }
        }
    }
}
