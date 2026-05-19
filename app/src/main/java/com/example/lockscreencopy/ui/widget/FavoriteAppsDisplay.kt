package com.example.lockscreencopy.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.example.lockscreencopy.model.FavoriteAppsLayout

private val ItemSize = 44.dp
private val LockIndicatorSize = 36.dp
private val Gap = 12.dp

@Composable
fun FavoriteAppsDisplay(
    favorites: List<BottomShortcut>,
    layout: FavoriteAppsLayout,
) {
    if (favorites.isEmpty()) return
    when (layout) {
        FavoriteAppsLayout.BOTTOM_LEFT -> HorizontalStrip(favorites, lockFirst = true)
        FavoriteAppsLayout.BOTTOM_RIGHT -> HorizontalStrip(favorites.reversed(), lockFirst = false)
        FavoriteAppsLayout.LEFT_VERTICAL -> VerticalStrip(favorites.reversed())
    }
}

@Composable
private fun HorizontalStrip(items: List<BottomShortcut>, lockFirst: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Gap),
    ) {
        if (lockFirst) LockDot()
        items.forEach { sc -> ShortcutIcon(sc) }
        if (!lockFirst) LockDot()
    }
}

@Composable
private fun VerticalStrip(items: List<BottomShortcut>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Gap),
    ) {
        items.forEach { sc -> ShortcutIcon(sc) }
        LockDot()
    }
}

@Composable
private fun LockDot() {
    Box(
        modifier = Modifier
            .size(LockIndicatorSize)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Composable
private fun ShortcutIcon(sc: BottomShortcut) {
    when (sc) {
        is BottomShortcut.System -> Box(
            modifier = Modifier
                .size(ItemSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(sc.icon, sc.label, tint = Color(0xFF424242), modifier = Modifier.size(24.dp))
        }
        is BottomShortcut.App -> {
            val bmp = remember(sc.id) { sc.drawable?.toBitmapSafe() }
            Box(
                modifier = Modifier.size(ItemSize).clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = sc.label,
                        modifier = Modifier.size(ItemSize).clip(CircleShape),
                    )
                } else {
                    Box(
                        modifier = Modifier.size(ItemSize).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f)),
                    )
                }
            }
        }
    }
}
