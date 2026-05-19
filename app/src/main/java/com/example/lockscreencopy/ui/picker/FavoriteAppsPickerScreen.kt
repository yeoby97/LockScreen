package com.example.lockscreencopy.ui.picker

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.data.loadInstalledApps
import com.example.lockscreencopy.data.systemShortcuts
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.ui.widget.toBitmapSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FavoriteAppsPickerScreen(
    initial: List<BottomShortcut>,
    onClose: () -> Unit,
    onApply: (List<BottomShortcut>) -> Unit,
) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<BottomShortcut.App>>(emptyList()) }
    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) { loadInstalledApps(context) }
    }

    val selected = remember { initial.toMutableStateList() }
    var query by remember { mutableStateOf("") }

    val available: List<BottomShortcut> = remember(apps) {
        systemShortcuts + apps
    }
    val filtered by remember(query, available) {
        derivedStateOf {
            if (query.isBlank()) available
            else available.filter { it.label.contains(query, ignoreCase = true) }
        }
    }

    fun isSelected(sc: BottomShortcut) = selected.any { it.id == sc.id }
    fun toggle(sc: BottomShortcut) {
        val idx = selected.indexOfFirst { it.id == sc.id }
        if (idx >= 0) selected.removeAt(idx)
        else if (selected.size < MAX_FAVORITES) selected.add(sc)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, "뒤로",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp).clickable(onClick = onClose),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("바로가기", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f))
                Text("${selected.size}/$MAX_FAVORITES", color = Color(0xFF8E8E93), fontSize = 14.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "적용", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onApply(selected.toList()) },
                )
            }

            // Top: selected row
            SelectedRow(selected = selected, onRemove = { sc -> selected.removeAll { it.id == sc.id } })

            Spacer(modifier = Modifier.height(8.dp))

            // Grid: available
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                items(filtered, key = { it.id }) { sc ->
                    PickerCell(
                        shortcut = sc,
                        selected = isSelected(sc),
                        onClick = { toggle(sc) },
                    )
                }
            }

            // Search bar
            SearchBar(query = query, onQueryChange = { query = it })
        }
    }
}

@Composable
private fun SelectedRow(
    selected: List<BottomShortcut>,
    onRemove: (BottomShortcut) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        selected.forEach { sc ->
            Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                ShortcutBubble(sc, selected = false, modifier = Modifier.size(56.dp))
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                        .clickable { onRemove(sc) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Remove, "제거", tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
        repeat(MAX_FAVORITES - selected.size) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape)
                    .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, null, tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun PickerCell(
    shortcut: BottomShortcut,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(18.dp))
                .background(if (selected) Color.White.copy(alpha = 0.08f) else Color.Transparent)
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) Color(0xFFB39DDB) else Color.Transparent,
                    shape = RoundedCornerShape(18.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            ShortcutBubble(shortcut, selected = selected, modifier = Modifier.size(56.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            shortcut.label,
            color = if (selected) Color.White else Color(0xFFB0B0B0),
            fontSize = 12.sp,
            textAlign = TextAlign.Center, maxLines = 1,
        )
    }
}

@Composable
private fun ShortcutBubble(sc: BottomShortcut, selected: Boolean, modifier: Modifier = Modifier) {
    when (sc) {
        is BottomShortcut.System -> Box(
            modifier = modifier.clip(CircleShape).background(Color(0xFF64B5F6)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(sc.icon, sc.label, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        is BottomShortcut.App -> {
            val bmp = remember(sc.id) { sc.drawable?.toBitmapSafe() }
            Box(modifier = modifier.clip(CircleShape), contentAlignment = Alignment.Center) {
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = sc.label,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)))
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF2C2C2E))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, null, tint = Color(0xFF8E8E93), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text("찾아보기", color = Color(0xFF8E8E93), fontSize = 15.sp)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 15.sp),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                Icons.Filled.Close, "지우기", tint = Color(0xFF8E8E93),
                modifier = Modifier.size(20.dp).clickable { onQueryChange("") },
            )
        }
    }
}
