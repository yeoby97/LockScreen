package com.example.lockscreencopy.ui.llm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.LockWidget
import com.example.lockscreencopy.model.WidgetApp
import com.example.lockscreencopy.ui.picker.LlmSuggestionResult
import com.example.lockscreencopy.ui.widget.WidgetCell

private val PanelDark = Color(0xE61C1C1E)
private val CardDark = Color(0xFF2C2C2E)
private val NestedDark = Color(0xFF1C1C1E)
private val TrayChip = Color(0xFF4DAAED)
private val FloatChip = Color(0xFF7C3AED)
private val SideChip = Color(0xFFFFA000)

@Composable
fun LlmSidePanel(
    suggestion: LlmSuggestionResult,
    onAddTrayWidget: (LockWidget) -> Unit,
    onAddFloatingWidget: (LockWidget) -> Unit,
    onApplyLeftShortcut: (BottomShortcut) -> Unit,
    onApplyRightShortcut: (BottomShortcut) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rec = suggestion.recommendation
    val trayIds = rec.tray
    val floatingIds = rec.floating
    val apps = suggestion.selected.widgetApps

    val trayByApp = remember(suggestion) {
        apps.associate { app ->
            app.id to trayIds.mapNotNull { id -> app.widgets.firstOrNull { it.id == id } }
        }
    }
    val floatingByApp = remember(suggestion) {
        apps.associate { app ->
            app.id to floatingIds.mapNotNull { id -> app.widgets.firstOrNull { it.id == id } }
        }
    }

    val shortcutCandidates = suggestion.selected.shortcutCandidates()
    val leftRec = rec.left?.let { id -> shortcutCandidates.firstOrNull { it.id == id } }
    val rightRec = rec.right?.let { id -> shortcutCandidates.firstOrNull { it.id == id } }

    var expandedAppId by remember { mutableStateOf<String?>(null) }
    var shortcutsExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .width(160.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PanelDark)
            .padding(horizontal = 8.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "AI 추천",
                color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Close, contentDescription = "닫기",
                    tint = Color.White, modifier = Modifier.size(14.dp),
                )
            }
        }
        Text(
            "\"${suggestion.userQuery}\"",
            color = Color(0xFFB0B0B5), fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )

        Column(
            modifier = Modifier
                .heightIn(max = 520.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            apps.forEach { app ->
                val trays = trayByApp[app.id].orEmpty()
                val floats = floatingByApp[app.id].orEmpty()
                if (trays.isEmpty() && floats.isEmpty()) return@forEach
                AppCard(
                    app = app,
                    trayWidgets = trays,
                    floatingWidgets = floats,
                    expanded = expandedAppId == app.id,
                    onToggle = {
                        expandedAppId = if (expandedAppId == app.id) null else app.id
                    },
                    onAddTray = onAddTrayWidget,
                    onAddFloating = onAddFloatingWidget,
                )
            }
            if (leftRec != null || rightRec != null) {
                ShortcutsCard(
                    left = leftRec,
                    right = rightRec,
                    expanded = shortcutsExpanded,
                    onToggle = { shortcutsExpanded = !shortcutsExpanded },
                    onApplyLeft = onApplyLeftShortcut,
                    onApplyRight = onApplyRightShortcut,
                )
            }
        }
    }
}

@Composable
private fun AppCard(
    app: WidgetApp,
    trayWidgets: List<LockWidget>,
    floatingWidgets: List<LockWidget>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddTray: (LockWidget) -> Unit,
    onAddFloating: (LockWidget) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardDark),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(app.iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    app.icon, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(14.dp),
                )
            }
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    app.name, color = Color.White, fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    "${trayWidgets.size + floatingWidgets.size}개 추천",
                    color = Color(0xFF8E8E93), fontSize = 9.sp,
                )
            }
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null, tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
        if (expanded) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                trayWidgets.forEachIndexed { idx, w ->
                    WidgetEntry(
                        widget = w, instanceKey = "t_${app.id}_${idx}_${w.id}",
                        kindLabel = "트레이", kindColor = TrayChip,
                        onAdd = { onAddTray(w) },
                    )
                }
                floatingWidgets.forEachIndexed { idx, w ->
                    WidgetEntry(
                        widget = w, instanceKey = "f_${app.id}_${idx}_${w.id}",
                        kindLabel = "자유", kindColor = FloatChip,
                        onAdd = { onAddFloating(w) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetEntry(
    widget: LockWidget,
    instanceKey: String,
    kindLabel: String,
    kindColor: Color,
    onAdd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NestedDark)
            .clickable { onAdd() }
            .padding(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(kindColor)
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
                Text(
                    kindLabel, color = Color.White, fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                widget.name, color = Color.White, fontSize = 10.sp,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Filled.Add, contentDescription = "추가",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(6.dp)),
        ) {
            WidgetCell(widget = widget, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ShortcutsCard(
    left: BottomShortcut?,
    right: BottomShortcut?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onApplyLeft: (BottomShortcut) -> Unit,
    onApplyRight: (BottomShortcut) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardDark),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "바로가기", color = Color.White, fontSize = 11.sp,
                fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f),
            )
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null, tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
        if (expanded) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                left?.let { ShortcutEntry(it, "좌측") { onApplyLeft(it) } }
                right?.let { ShortcutEntry(it, "우측") { onApplyRight(it) } }
            }
        }
    }
}

@Composable
private fun ShortcutEntry(shortcut: BottomShortcut, sideLabel: String, onApply: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NestedDark)
            .clickable { onApply() }
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(SideChip)
                .padding(horizontal = 4.dp, vertical = 1.dp),
        ) {
            Text(
                sideLabel, color = Color.White, fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(6.dp))
        when (shortcut) {
            is BottomShortcut.System -> Icon(
                shortcut.icon, contentDescription = shortcut.label,
                tint = Color.White, modifier = Modifier.size(16.dp),
            )
            is BottomShortcut.App -> Icon(
                Icons.Filled.Add, contentDescription = null,
                tint = Color.White, modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            shortcut.label, color = Color.White, fontSize = 10.sp,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Filled.Add, contentDescription = "적용",
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(14.dp),
        )
    }
}

/**
 * 좌/우 하단 바로가기 버튼 위치에 겹쳐서 추천을 표시. 탭하면 적용.
 */
@Composable
fun ShortcutRecommendationBadge(
    shortcut: BottomShortcut,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xCC4DAAED))
            .border(2.dp, SideChip, CircleShape)
            .clickable(onClick = onAccept),
        contentAlignment = Alignment.Center,
    ) {
        when (shortcut) {
            is BottomShortcut.System -> Icon(
                shortcut.icon, contentDescription = shortcut.label,
                tint = Color.White, modifier = Modifier.size(26.dp),
            )
            is BottomShortcut.App -> Icon(
                Icons.Filled.Add, contentDescription = shortcut.label,
                tint = Color.White, modifier = Modifier.size(26.dp),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(14.dp)
                .clip(CircleShape)
                .background(SideChip),
            contentAlignment = Alignment.Center,
        ) {
            Text("★", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}
