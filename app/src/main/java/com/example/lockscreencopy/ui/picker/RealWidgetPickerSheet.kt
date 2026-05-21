package com.example.lockscreencopy.ui.picker

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.ui.widget.toBitmapSafe
import kotlin.math.roundToInt

private data class WidgetItem(
    val providerInfo: AppWidgetProviderInfo,
    val packageName: String,
    val appLabel: String,
    val appIcon: Drawable?,
    val widgetLabel: String,
)

private enum class WidgetFilter(val label: String) {
    All("전체"),
    Frequent("자주 사용"),
    Recent("최근 사용"),
    HasWidgets("위젯 보유 앱"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealWidgetPickerSheet(
    appWidgetManager: AppWidgetManager,
    onDismiss: () -> Unit,
    onSelect: (AppWidgetProviderInfo) -> Unit,
) {
    val ctx = LocalContext.current
    val pm = ctx.packageManager
    val densityDpi = LocalDensity.current.density.let { (it * 160f).roundToInt() }

    val widgets = remember(appWidgetManager) {
        appWidgetManager.installedProviders
            .map { info ->
                val pkg = info.provider.packageName
                val (appLabel, appIcon) = appInfo(pm, pkg)
                val widgetLabel = runCatching { info.loadLabel(pm).toString() }
                    .getOrDefault(info.provider.shortClassName)
                WidgetItem(info, pkg, appLabel, appIcon, widgetLabel)
            }
            .sortedWith(compareBy<WidgetItem> { it.appLabel.lowercase() }.thenBy { it.widgetLabel.lowercase() })
    }

    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(WidgetFilter.All) }
    var multiSelectMode by rememberSaveable { mutableStateOf(false) }
    var selectedKeys by remember { mutableStateOf(setOf<String>()) }
    var previewTarget by remember { mutableStateOf<WidgetItem?>(null) }

    val filteredWidgets = remember(widgets, query, filter) {
        widgets.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.appLabel.contains(query, ignoreCase = true) ||
                item.widgetLabel.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                WidgetFilter.All -> true
                WidgetFilter.Frequent -> true
                WidgetFilter.Recent -> true
                WidgetFilter.HasWidgets -> true
            }
            matchesQuery && matchesFilter
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
            Text(
                "앱 위젯",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFFE5E5EA)) },
                placeholder = { Text("앱명/위젯명 검색", color = Color(0xFFC7C7CC)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WidgetFilter.values().forEach { option ->
                    FilterChip(
                        selected = filter == option,
                        onClick = { filter = option },
                        label = { Text(option.label) },
                    )
                }
            }

            if (filteredWidgets.isEmpty()) {
                Text(
                    "검색 결과가 없습니다",
                    color = Color(0xFFE5E5EA),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 168.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(560.dp)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filteredWidgets, key = { it.providerInfo.provider.flattenToShortString() }) { item ->
                        val key = item.providerInfo.provider.flattenToShortString()
                        WidgetPreviewCard(
                            item = item,
                            ctx = ctx,
                            densityDpi = densityDpi,
                            selected = selectedKeys.contains(key),
                            onClick = {
                                if (multiSelectMode) {
                                    selectedKeys = if (selectedKeys.contains(key)) selectedKeys - key else selectedKeys + key
                                } else {
                                    onSelect(item.providerInfo)
                                }
                            },
                            onLongClick = {
                                multiSelectMode = true
                                previewTarget = item
                            },
                        )
                    }
                }
            }
        }
    }

    previewTarget?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { previewTarget = null },
            containerColor = Color(0xFF1C1C1E),
        ) {
            WidgetPreviewCard(
                item = item,
                ctx = ctx,
                densityDpi = densityDpi,
                selected = false,
                onClick = { onSelect(item.providerInfo) },
                onLongClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                previewHeight = 220.dp,
            )
        }
    }
}

private fun appInfo(pm: PackageManager, pkg: String): Pair<String, Drawable?> {
    return try {
        val info = pm.getApplicationInfo(pkg, 0)
        pm.getApplicationLabel(info).toString() to runCatching { pm.getApplicationIcon(info) }.getOrNull()
    } catch (_: Exception) {
        pkg to null
    }
}

@Composable
private fun WidgetPreviewCard(
    item: WidgetItem,
    ctx: android.content.Context,
    densityDpi: Int,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewHeight: androidx.compose.ui.unit.Dp = 120.dp,
) {
    val previewBmp = remember(item.providerInfo.provider.flattenToShortString()) {
        val preview = runCatching { item.providerInfo.loadPreviewImage(ctx, densityDpi) }.getOrNull()
            ?: runCatching { item.providerInfo.loadIcon(ctx, densityDpi) }.getOrNull()
        preview?.toBitmapSafe()
    }
    val appIconBmp = remember(item.packageName) { item.appIcon?.toBitmapSafe() }
    val sizeText = "${item.providerInfo.minWidth}×${item.providerInfo.minHeight}"

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF2C2C2E))
            .border(1.dp, if (selected) Color(0xFF64D2FF) else Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(48.dp)) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                if (appIconBmp != null) {
                    Image(bitmap = appIconBmp.asImageBitmap(), contentDescription = item.appLabel, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Filled.Widgets, null, tint = Color(0xFFE5E5EA), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.appLabel, color = Color.White, fontSize = 14.sp, maxLines = 1)
                Text(item.widgetLabel, color = Color(0xFFE5E5EA), fontSize = 12.sp, maxLines = 1)
            }
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF64D2FF), modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(previewHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            if (previewBmp != null) {
                Image(
                    bitmap = previewBmp.asImageBitmap(),
                    contentDescription = item.widgetLabel,
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                )
            } else {
                Icon(Icons.Filled.Widgets, contentDescription = null, tint = Color(0xFFE5E5EA), modifier = Modifier.size(36.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(sizeText, color = Color(0xFFD1D1D6), fontSize = 12.sp)
    }
}
