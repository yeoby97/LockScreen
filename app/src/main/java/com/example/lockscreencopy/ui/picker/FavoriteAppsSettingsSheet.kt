package com.example.lockscreencopy.ui.picker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.FavoriteAppsLayout
import com.example.lockscreencopy.ui.widget.toBitmapSafe

const val MAX_FAVORITES = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteAppsSettingsSheet(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    favorites: List<BottomShortcut>,
    layout: FavoriteAppsLayout,
    onLayoutChange: (FavoriteAppsLayout) -> Unit,
    onOpenPicker: () -> Unit,
    onDismiss: () -> Unit,
    usageSortEnabled: Boolean,
    onUsageSortChange: (Boolean) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF2C2C2E),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "즐겨찾는 앱 보이기",
                    color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF1976D2),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF5A5A5C),
                    ),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("즐겨찾는 앱", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable(onClick = onOpenPicker)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val filled = favorites.take(MAX_FAVORITES)
                    filled.forEach { sc -> FavoriteSlot(sc) }
                    repeat(MAX_FAVORITES - filled.size) { EmptyFavoriteSlot() }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "사용시간 기반 동기화",
                        color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "최근 일주일 사용시간 순으로 표시 (간격이 크면 일부만 노출)",
                        color = Color(0xFF8E8E93), fontSize = 12.sp,
                    )
                }
                Switch(
                    checked = usageSortEnabled,
                    onCheckedChange = onUsageSortChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF1976D2),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF5A5A5C),
                    ),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("정렬", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LayoutOption(
                    selected = layout == FavoriteAppsLayout.BOTTOM_LEFT,
                    onClick = { onLayoutChange(FavoriteAppsLayout.BOTTOM_LEFT) },
                    modifier = Modifier.weight(1f),
                ) { LayoutPreviewBottomLeft() }
                LayoutOption(
                    selected = layout == FavoriteAppsLayout.LEFT_VERTICAL,
                    onClick = { onLayoutChange(FavoriteAppsLayout.LEFT_VERTICAL) },
                    modifier = Modifier.weight(1f),
                ) { LayoutPreviewLeftVertical() }
                LayoutOption(
                    selected = layout == FavoriteAppsLayout.BOTTOM_RIGHT,
                    onClick = { onLayoutChange(FavoriteAppsLayout.BOTTOM_RIGHT) },
                    modifier = Modifier.weight(1f),
                ) { LayoutPreviewBottomRight() }
            }
        }
    }
}

@Composable
private fun FavoriteSlot(sc: BottomShortcut) {
    when (sc) {
        is BottomShortcut.System -> Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(sc.icon, null, tint = Color(0xFF424242), modifier = Modifier.size(22.dp))
        }
        is BottomShortcut.App -> {
            val bmp = remember(sc.id) { sc.drawable?.toBitmapSafe() }
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(), contentDescription = sc.label,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                )
            } else {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White))
            }
        }
    }
}

@Composable
private fun EmptyFavoriteSlot() {
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape)
            .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Add, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun LayoutOption(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val border = if (selected) Color(0xFFB39DDB) else Color.White.copy(alpha = 0.15f)
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(if (selected) 2.dp else 1.dp, border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) { content() }
}

@Composable
private fun LayoutPreviewBottomLeft() {
    Box(modifier = Modifier.fillMaxSize().padding(start = 4.dp, bottom = 4.dp)) {
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            repeat(3) { Dot() }
        }
    }
}

@Composable
private fun LayoutPreviewLeftVertical() {
    Box(modifier = Modifier.fillMaxSize().padding(start = 4.dp)) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            repeat(3) { Dot() }
        }
    }
}

@Composable
private fun LayoutPreviewBottomRight() {
    Box(modifier = Modifier.fillMaxSize().padding(end = 4.dp, bottom = 4.dp)) {
        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            repeat(3) { Dot() }
        }
    }
}

@Composable
private fun Dot() {
    Box(
        modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.5f)),
    )
}
