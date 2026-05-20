package com.example.lockscreencopy.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
private val animSpec = tween<Float>(durationMillis = 280, easing = FastOutSlowInEasing)
private val sizeSpec = tween<androidx.compose.ui.unit.IntSize>(durationMillis = 280, easing = FastOutSlowInEasing)

@Composable
fun FavoriteAppsDisplay(
    favorites: List<BottomShortcut>,
    layout: FavoriteAppsLayout,
) {
    if (favorites.isEmpty()) return
    var collapsed by remember { mutableStateOf(false) }

    val items = when (layout) {
        FavoriteAppsLayout.BOTTOM_LEFT -> favorites
        FavoriteAppsLayout.BOTTOM_RIGHT, FavoriteAppsLayout.LEFT_VERTICAL -> favorites.reversed()
    }
    val toggle = { collapsed = !collapsed }

    when (layout) {
        FavoriteAppsLayout.BOTTOM_LEFT -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Gap),
        ) {
            LockDot(onClick = toggle)
            AnimatedVisibility(
                visible = !collapsed,
                enter = expandHorizontally(animationSpec = sizeSpec) + fadeIn(animationSpec = animSpec),
                exit = shrinkHorizontally(animationSpec = sizeSpec) + fadeOut(animationSpec = animSpec),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Gap),
                ) { items.forEach { ShortcutIcon(it) } }
            }
        }
        FavoriteAppsLayout.BOTTOM_RIGHT -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Gap),
        ) {
            AnimatedVisibility(
                visible = !collapsed,
                enter = expandHorizontally(animationSpec = sizeSpec) + fadeIn(animationSpec = animSpec),
                exit = shrinkHorizontally(animationSpec = sizeSpec) + fadeOut(animationSpec = animSpec),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Gap),
                ) { items.forEach { ShortcutIcon(it) } }
            }
            LockDot(onClick = toggle)
        }
        FavoriteAppsLayout.LEFT_VERTICAL -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Gap),
        ) {
            AnimatedVisibility(
                visible = !collapsed,
                enter = expandVertically(animationSpec = sizeSpec) + fadeIn(animationSpec = animSpec),
                exit = shrinkVertically(animationSpec = sizeSpec) + fadeOut(animationSpec = animSpec),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Gap),
                ) { items.forEach { ShortcutIcon(it) } }
            }
            LockDot(onClick = toggle)
        }
    }
}

@Composable
private fun LockDot(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(LockIndicatorSize)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.25f))
            .clickable(onClick = onClick),
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
