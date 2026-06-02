package com.example.lockscreencopy.ui.picker

import androidx.compose.runtime.Composable
import com.example.lockscreencopy.ui.theme.GlassChoiceDialog

enum class ShortcutChoice { RealWidget, FavoriteApp, Text }

@Composable
fun ShortcutPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (ShortcutChoice) -> Unit,
) {
    GlassChoiceDialog(
        title = "추가할 항목 선택",
        onDismiss = onDismiss,
        options = listOf(
            "앱 위젯" to { onSelect(ShortcutChoice.RealWidget) },
            "즐겨찾는 앱" to { onSelect(ShortcutChoice.FavoriteApp) },
            "글 넣기" to { onSelect(ShortcutChoice.Text) },
        ),
    )
}
