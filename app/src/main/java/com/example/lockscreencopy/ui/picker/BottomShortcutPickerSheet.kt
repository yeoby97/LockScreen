package com.example.lockscreencopy.ui.picker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.data.loadInstalledApps
import com.example.lockscreencopy.data.systemShortcuts
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.ui.theme.LockTokens
import com.example.lockscreencopy.ui.widget.toBitmapSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class Tab { SYSTEM, APPS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomShortcutPickerSheet(
    onDismiss: () -> Unit,
    onClear: (() -> Unit)? = null,
    onSelected: (BottomShortcut) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var tab by remember { mutableStateOf(Tab.SYSTEM) }
    var apps by remember { mutableStateOf<List<BottomShortcut.App>>(emptyList()) }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) { loadInstalledApps(context) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LockTokens.SheetBg,
        shape = LockTokens.SheetShape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "바로가기 선택",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LockTokens.TextPrimary,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
                if (onClear != null) {
                    TextButton(onClick = onClear) {
                        Text("제거", color = LockTokens.Accent)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TabChip("기본 기능", tab == Tab.SYSTEM) { tab = Tab.SYSTEM }
                TabChip("앱", tab == Tab.APPS) { tab = Tab.APPS }
            }

            when (tab) {
                Tab.SYSTEM -> SystemGrid(onSelected)
                Tab.APPS -> AppsGrid(apps, onSelected)
            }
        }
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) LockTokens.Accent else LockTokens.GlassWhiteSoft
    val fg = if (selected) Color.White else LockTokens.TextSecondary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    ) {
        Text(label, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SystemGrid(onSelected: (BottomShortcut) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = Modifier.fillMaxWidth().height(360.dp),
    ) {
        items(systemShortcuts) { sc ->
            Column(
                modifier = Modifier.fillMaxWidth().clickable { onSelected(sc) }
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape)
                        .background(LockTokens.SheetSurfaceHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(sc.icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    sc.label,
                    fontSize = 11.sp,
                    color = LockTokens.TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun AppsGrid(apps: List<BottomShortcut.App>, onSelected: (BottomShortcut) -> Unit) {
    if (apps.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(360.dp), contentAlignment = Alignment.Center) {
            Text("앱 목록을 불러오는 중...", color = LockTokens.OnSheetSecondary)
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = Modifier.fillMaxWidth().height(360.dp),
    ) {
        items(apps) { app ->
            Column(
                modifier = Modifier.fillMaxWidth().clickable { onSelected(app) }
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val bmp = remember(app.id) { app.drawable?.toBitmapSafe() }
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape)
                        .background(LockTokens.SheetSurfaceHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = app.label,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    app.label,
                    fontSize = 11.sp,
                    color = LockTokens.TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}
