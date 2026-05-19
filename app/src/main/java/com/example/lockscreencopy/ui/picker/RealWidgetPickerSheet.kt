package com.example.lockscreencopy.ui.picker

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealWidgetPickerSheet(
    appWidgetManager: AppWidgetManager,
    onDismiss: () -> Unit,
    onSelect: (AppWidgetProviderInfo) -> Unit,
) {
    val ctx = LocalContext.current
    val pm = ctx.packageManager
    val grouped = remember(appWidgetManager) {
        appWidgetManager.installedProviders
            .groupBy { it.provider.packageName }
            .toList()
            .sortedBy { (pkg, _) ->
                try {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString().lowercase()
                } catch (_: Exception) { pkg }
            }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text(
                "실제 앱 위젯",
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            if (grouped.isEmpty()) {
                Text(
                    "설치된 위젯이 없습니다",
                    color = Color(0xFF8E8E93), fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp),
                )
            }
            LazyColumn(modifier = Modifier.fillMaxWidth().height(500.dp)) {
                grouped.forEach { (pkg, infos) ->
                    val appLabel = try {
                        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                    } catch (_: Exception) { pkg }
                    item(key = "header_$pkg") {
                        Text(
                            appLabel,
                            color = Color(0xFF8E8E93), fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    items(infos, key = { it.provider.flattenToShortString() }) { info ->
                        val label = try { info.loadLabel(pm).toString() } catch (_: Exception) { info.provider.shortClassName }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(info) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(label, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Text(
                                "${info.minWidth}×${info.minHeight}",
                                color = Color(0xFF8E8E93), fontSize = 12.sp,
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
    }
}
