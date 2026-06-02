package com.example.lockscreencopy.ui.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.ui.theme.LockTokens

@Composable
fun BottomShortcutButton(
    shortcut: BottomShortcut?,
    isEditing: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {},
) {
    if (shortcut == null && !isEditing) return

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(LockTokens.DockBg)
            .border(1.dp, LockTokens.Border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (shortcut) {
            null -> Icon(
                Icons.Filled.Add, contentDescription = "추가",
                tint = LockTokens.TextSecondary, modifier = Modifier.size(26.dp),
            )
            is BottomShortcut.System -> Icon(
                shortcut.icon, contentDescription = shortcut.label,
                tint = shortcut.tint, modifier = Modifier.size(28.dp),
            )
            is BottomShortcut.App -> {
                val bitmap = remember(shortcut.id) { shortcut.drawable?.toBitmapSafe() }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = shortcut.label,
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                    )
                } else {
                    Icon(
                        Icons.Filled.Add, contentDescription = shortcut.label,
                        tint = Color.White, modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

internal fun Drawable.toBitmapSafe(): Bitmap? {
    if (this is BitmapDrawable && bitmap != null) return bitmap
    val w = intrinsicWidth.takeIf { it > 0 } ?: 96
    val h = intrinsicHeight.takeIf { it > 0 } ?: 96
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    setBounds(0, 0, w, h)
    draw(canvas)
    return bmp
}
