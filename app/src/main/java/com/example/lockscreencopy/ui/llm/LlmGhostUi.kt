package com.example.lockscreencopy.ui.llm

import android.appwidget.AppWidgetProviderInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.LockWidget
import com.example.lockscreencopy.model.WidgetSize
import com.example.lockscreencopy.ui.widget.WidgetCell
import com.example.lockscreencopy.ui.widget.toBitmapSafe
import kotlin.math.roundToInt

private val SideChip = Color(0xFFFFA000)
private val GhostBorder = Color(0xFFFFA000)

data class GhostInstance(val key: String, val widget: LockWidget)

/** 우측 스트립에 표시할 앱 한 칸. mock 앱(vector icon) / 실제 앱(bitmap icon) 모두 지원. */
data class StripAppEntry(
    val id: String,
    val name: String,
    val iconBg: Color,
    val iconVector: ImageVector? = null,
    val iconBitmap: Bitmap? = null,
)

/**
 * 잠금화면 우측의 세로 앱 아이콘 스트립. 추천된 앱들의 아이콘만 나열되며,
 * 하나를 누르면 해당 앱의 위젯들이 잠금화면에 투명 ghost 로 표시된다.
 */
@Composable
fun LlmAppStrip(
    apps: List<StripAppEntry>,
    selectedAppId: String?,
    onSelect: (String?) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(56.dp)
            .heightIn(max = 520.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xCC1C1C1E))
            .padding(vertical = 10.dp, horizontal = 6.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .clickable { onClose() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Close, contentDescription = "닫기",
                tint = Color.White, modifier = Modifier.size(16.dp),
            )
        }
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.2f)),
        )
        apps.forEach { app ->
            val selected = selectedAppId == app.id
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(app.iconBg)
                    .then(
                        if (selected) Modifier.border(2.5.dp, SideChip, CircleShape)
                        else Modifier,
                    )
                    .clickable { onSelect(if (selected) null else app.id) },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    app.iconBitmap != null -> Image(
                        bitmap = app.iconBitmap.asImageBitmap(),
                        contentDescription = app.name,
                        modifier = Modifier.size(30.dp).clip(CircleShape),
                    )
                    app.iconVector != null -> Icon(
                        app.iconVector, contentDescription = app.name,
                        tint = Color.White, modifier = Modifier.size(22.dp),
                    )
                    else -> Icon(
                        Icons.Filled.Widgets, contentDescription = app.name,
                        tint = Color.White, modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

/**
 * 트레이 영역 바로 아래에 추천 위젯 ghost 를 한 줄로 표시. 탭하면 실제 트레이에 배치된다.
 * 트레이는 권한이 없어서 mock LockWidget 을 사용.
 */
@Composable
fun LlmTrayGhostRow(
    ghosts: List<GhostInstance>,
    consumed: Set<String>,
    trayUsedSpan: Int,
    slotSize: Dp,
    slotGap: Dp,
    onTap: (GhostInstance) -> Unit,
) {
    val visible = ghosts.filter { it.key !in consumed }
    if (visible.isEmpty()) return
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "추천 트레이 위젯",
            color = SideChip, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(slotGap, Alignment.CenterHorizontally),
        ) {
            visible.forEach { ghost ->
                val span = if (ghost.widget.size == WidgetSize.WIDE) 2 else 1
                val disabled = trayUsedSpan + span > 4
                Box(
                    modifier = Modifier
                        .width(slotSize * span)
                        .height(slotSize)
                        .graphicsLayer { alpha = if (disabled) 0.25f else 0.55f }
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, GhostBorder, RoundedCornerShape(12.dp))
                        .clickable(enabled = !disabled) { onTap(ghost) },
                ) {
                    WidgetCell(widget = ghost.widget, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

/**
 * 좌/우 하단 바로가기 버튼 바로 위에 투명 ghost 로 추천 표시.
 * - 본체 탭: 적용 (onAccept)
 * - 우상단 X 탭: 추천 취소 (onCancel)
 */
@Composable
fun ShortcutRecommendationBadge(
    shortcut: BottomShortcut,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(56.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = 0.55f }
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.25f))
                .border(1.5.dp, GhostBorder, CircleShape)
                .clickable(onClick = onAccept),
            contentAlignment = Alignment.Center,
        ) {
            when (shortcut) {
                is BottomShortcut.System -> Icon(
                    shortcut.icon, contentDescription = shortcut.label,
                    tint = Color.White, modifier = Modifier.size(26.dp),
                )
                is BottomShortcut.App -> Icon(
                    Icons.Filled.Close, contentDescription = shortcut.label,
                    tint = Color.White, modifier = Modifier.size(26.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF453A))
                .clickable { onCancel() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Close, contentDescription = "취소",
                tint = Color.White, modifier = Modifier.size(10.dp),
            )
        }
    }
}

/**
 * 화면 좌표 격자에서 idx 번째 자유 ghost 의 기본 위치.
 */
fun ghostFloatingOffset(idx: Int, screenWidthPx: Float, screenHeightPx: Float): Offset =
    Offset(
        x = screenWidthPx * 0.15f + (idx % 2) * screenWidthPx * 0.42f,
        y = screenHeightPx * 0.34f + (idx / 2) * screenHeightPx * 0.12f,
    )

/**
 * 자유 배치 영역의 ghost — 실제 설치된 앱의 AppWidgetProviderInfo. 더미 위젯은 사용하지 않는다.
 * providerInfo 의 preview/icon 을 반투명으로 띄우고, 탭하면 실제 위젯 바인딩 흐름을 시작.
 */
@Composable
fun LlmRealWidgetGhost(
    info: AppWidgetProviderInfo,
    offset: Offset,
    onTap: () -> Unit,
) {
    val ctx = LocalContext.current
    val densityDpi = (LocalDensity.current.density * 160f).roundToInt()
    val componentKey = info.provider.flattenToShortString()
    val previewBmp = remember(componentKey) {
        val drawable: Drawable? = runCatching { info.loadPreviewImage(ctx, densityDpi) }.getOrNull()
            ?: runCatching { info.loadIcon(ctx, densityDpi) }.getOrNull()
        drawable?.toBitmapSafe()
    }
    val label = remember(componentKey) {
        runCatching { info.loadLabel(ctx.packageManager).toString() }
            .getOrDefault(info.provider.shortClassName)
    }
    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .width(150.dp)
            .height(110.dp)
            .graphicsLayer { alpha = 0.55f }
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .border(1.5.dp, GhostBorder, RoundedCornerShape(16.dp))
            .clickable { onTap() }
            .padding(6.dp),
    ) {
        if (previewBmp != null) {
            Image(
                bitmap = previewBmp.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Filled.Widgets, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(28.dp),
                )
                Text(
                    label, color = Color.White, fontSize = 10.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

/**
 * 실제 앱 위젯 ghost 의 ID(consumed 추적용).
 */
fun realWidgetGhostKey(component: String): String = "real_$component"
