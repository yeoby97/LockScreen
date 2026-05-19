package com.example.lockscreencopy.ui.picker

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.data.lockWidgetApps
import com.example.lockscreencopy.model.LockWidget
import com.example.lockscreencopy.model.WidgetApp
import com.example.lockscreencopy.model.WidgetSize
import com.example.lockscreencopy.ui.widget.SmallWidgetContent
import com.example.lockscreencopy.ui.widget.WideWidgetContent

private sealed class PickerStep {
    data object AppList : PickerStep()
    data class WidgetList(val app: WidgetApp) : PickerStep()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockWidgetPickerSheet(
    onDismiss: () -> Unit,
    onWidgetSelected: (LockWidget) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var step by remember { mutableStateOf<PickerStep>(PickerStep.AppList) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E),
    ) {
        when (val s = step) {
            is PickerStep.AppList -> AppListStep(onAppSelected = { step = PickerStep.WidgetList(it) })
            is PickerStep.WidgetList -> WidgetListStep(
                app = s.app,
                onBack = { step = PickerStep.AppList },
                onWidgetSelected = onWidgetSelected,
            )
        }
    }
}

@Composable
private fun AppListStep(onAppSelected: (WidgetApp) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text(
            "위젯", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        LazyColumn {
            items(lockWidgetApps) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAppSelected(app) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(app.iconBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(app.icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(app.name, color = Color.White, fontSize = 17.sp, modifier = Modifier.weight(1f))
                    Text(app.widgets.size.toString(), color = Color(0xFF8E8E93), fontSize = 16.sp)
                }
                Divider(
                    color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 68.dp),
                )
            }
        }
    }
}

@Composable
private fun WidgetListStep(
    app: WidgetApp,
    onBack: () -> Unit,
    onWidgetSelected: (LockWidget) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.KeyboardArrowLeft, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(app.iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(app.icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(
                app.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(app.widgets.size.toString(), color = Color(0xFF8E8E93), fontSize = 16.sp)
        }

        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().height(400.dp),
        ) {
            items(
                items = app.widgets,
                span = { widget -> GridItemSpan(if (widget.size == WidgetSize.WIDE) 2 else 1) },
            ) { widget ->
                WidgetPreviewItem(widget = widget, onClick = { onWidgetSelected(widget) })
            }
        }
    }
}

@Composable
private fun WidgetPreviewItem(widget: LockWidget, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2C2C2E))
            .clickable { onClick() }
            .padding(bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF3A3A3C)),
            contentAlignment = Alignment.Center,
        ) {
            when (widget.size) {
                WidgetSize.SMALL -> SmallWidgetContent(widget)
                WidgetSize.WIDE  -> WideWidgetContent(widget)
            }
        }
        Text(
            widget.name,
            color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Text(
            if (widget.size == WidgetSize.SMALL) "1x1" else "2x1",
            color = Color(0xFF8E8E93), fontSize = 11.sp,
        )
    }
}
