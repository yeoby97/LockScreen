package com.example.lockscreencopy.ui.picker

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

private data class WidgetGroup(
    val packageName: String,
    val appLabel: String,
    val appIcon: Drawable?,
    val providers: List<AppWidgetProviderInfo>,
)

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

    val groups = remember(appWidgetManager) {
        appWidgetManager.installedProviders
            .groupBy { it.provider.packageName }
            .map { (pkg, infos) ->
                val (label, icon) = appInfo(pm, pkg)
                WidgetGroup(pkg, label, icon, infos)
            }
            .sortedBy { it.appLabel.lowercase() }
    }
    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 48.dp),
        ) {
            item {
                Text(
                    "앱 위젯",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            if (groups.isEmpty()) {
                item {
                    Text(
                        "설치된 위젯이 없습니다",
                        color = Color(0xFF8E8E93), fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            items(groups, key = { it.packageName }) { group ->
                val isOpen = expanded[group.packageName] ?: false
                AppHeaderRow(
                    group = group,
                    isOpen = isOpen,
                    onToggle = { expanded[group.packageName] = !isOpen },
                )
                if (isOpen) {
                    WidgetPreviewRow(
                        providers = group.providers,
                        pm = pm,
                        ctx = ctx,
                        densityDpi = densityDpi,
                        onSelect = onSelect,
                    )
                }
                Divider(
                    color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        }
    }
}

private fun appInfo(pm: PackageManager, pkg: String): Pair<String, Drawable?> {
    return try {
        val info = pm.getApplicationInfo(pkg, 0)
        val label = pm.getApplicationLabel(info).toString()
        val icon = runCatching { pm.getApplicationIcon(info) }.getOrNull()
        label to icon
    } catch (_: Exception) {
        pkg to null
    }
}

@Composable
private fun AppHeaderRow(group: WidgetGroup, isOpen: Boolean, onToggle: () -> Unit) {
    val iconBmp = remember(group.packageName) { group.appIcon?.toBitmapSafe() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            if (iconBmp != null) {
                Image(
                    bitmap = iconBmp.asImageBitmap(),
                    contentDescription = group.appLabel,
                    modifier = Modifier.size(32.dp).clip(CircleShape),
                )
            } else {
                Icon(Icons.Filled.Widgets, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(group.appLabel, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "${group.providers.size}개 위젯",
                color = Color(0xFF8E8E93), fontSize = 12.sp,
            )
        }
        Icon(
            if (isOpen) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null, tint = Color(0xFF8E8E93),
        )
    }
}

@Composable
private fun WidgetPreviewRow(
    providers: List<AppWidgetProviderInfo>,
    pm: PackageManager,
    ctx: android.content.Context,
    densityDpi: Int,
    onSelect: (AppWidgetProviderInfo) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(providers, key = { it.provider.flattenToShortString() }) { info ->
            WidgetPreviewCard(info = info, pm = pm, ctx = ctx, densityDpi = densityDpi, onClick = { onSelect(info) })
        }
    }
}

@Composable
private fun WidgetPreviewCard(
    info: AppWidgetProviderInfo,
    pm: PackageManager,
    ctx: android.content.Context,
    densityDpi: Int,
    onClick: () -> Unit,
) {
    val previewBmp = remember(info.provider.flattenToShortString()) {
        val preview: Drawable? = runCatching { info.loadPreviewImage(ctx, densityDpi) }.getOrNull()
            ?: runCatching { info.loadIcon(ctx, densityDpi) }.getOrNull()
        preview?.toBitmapSafe()
    }
    val label = remember(info.provider.flattenToShortString()) {
        runCatching { info.loadLabel(pm).toString() }.getOrDefault(info.provider.shortClassName)
    }
    val sizeText = "${info.minWidth}×${info.minHeight}"

    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF2C2C2E))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(110.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.06f)),
            contentAlignment = Alignment.Center,
        ) {
            if (previewBmp != null) {
                Image(
                    bitmap = previewBmp.asImageBitmap(),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                )
            } else {
                Icon(Icons.Filled.Widgets, null, tint = Color(0xFF8E8E93), modifier = Modifier.size(36.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
        Text(sizeText, color = Color(0xFF8E8E93), fontSize = 11.sp)
    }
}
