package com.example.lockscreencopy.ui.picker

import androidx.compose.runtime.Composable
import com.example.lockscreencopy.ui.theme.GlassChoiceDialog

enum class AiActionChoice { LlmLayout, Sketch }

@Composable
fun AiActionPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (AiActionChoice) -> Unit,
) {
    GlassChoiceDialog(
        title = "AI 위젯 도우미",
        onDismiss = onDismiss,
        options = listOf(
            "AI 위젯 배치" to { onSelect(AiActionChoice.LlmLayout) },
            "AI 스케치 (직접 위치 지정)" to { onSelect(AiActionChoice.Sketch) },
        ),
    )
}
