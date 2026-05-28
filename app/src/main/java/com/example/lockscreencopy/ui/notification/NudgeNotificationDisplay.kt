package com.example.lockscreencopy.ui.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.NotificationItem
import com.example.lockscreencopy.model.NudgeDisplayMode

private val nudgePurple = Color(0xFF9B78FF)
private val nudgeCyan = Color(0xFF4FC3F7)
private val nudgeGradient = Brush.linearGradient(listOf(nudgePurple, nudgeCyan))
private val cardShape = RoundedCornerShape(18.dp)
private val chipShape = RoundedCornerShape(50)

// ───────────────────────────────────────
//  진입점: 모드별 분기
// ───────────────────────────────────────

@Composable
fun NudgeNotificationDisplay(
    notifications: List<NotificationItem>,
    mode: NudgeDisplayMode,
    modifier: Modifier = Modifier,
) {
    when (mode) {
        NudgeDisplayMode.CARD -> CardModeList(notifications, modifier)
        NudgeDisplayMode.ICON -> IconModeRow(notifications, modifier)
        NudgeDisplayMode.DOT  -> DotModeIndicator(notifications, modifier)
    }
}

// ───────────────────────────────────────
//  CARD 모드
// ───────────────────────────────────────

@Composable
private fun CardModeList(notifications: List<NotificationItem>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        notifications.forEach { item ->
            if (item.hasNudge) {
                NudgeFloatingCard(item, Modifier.fillMaxWidth())
            } else {
                NormalCard(item, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun NudgeFloatingCard(item: NotificationItem, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "nudge_float")
    val rawFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "float_y",
    )
    val floatMult by animateFloatAsState(
        targetValue = if (expanded) 0f else 1f,
        animationSpec = tween(300),
        label = "float_mult",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = rawFloat * floatMult
                scaleX = 1.07f
                scaleY = 1.07f
            }
            .shadow(elevation = 12.dp, shape = cardShape, clip = false)
            .clip(cardShape)
            .border(1.dp, nudgeGradient, cardShape)
            .background(Color(0xCC2A2A2C))
            .clickable { expanded = !expanded }
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(item.appName, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                Text(item.timeLabel, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                item.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!expanded) {
                Spacer(Modifier.height(6.dp))
                NudgeTeaserBadge(item.nudgeActions.firstOrNull() ?: item.nudgeLabel)
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(animationSpec = tween(200)),
            ) {
                Column {
                    Spacer(Modifier.height(6.dp))
                    Text(item.body, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, lineHeight = 17.sp)
                    if (item.nudgeActions.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item.nudgeActions.forEach { action -> NudgeActionChip(action) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NudgeTeaserBadge(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(chipShape)
            .background(nudgePurple.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = nudgePurple, modifier = Modifier.size(11.dp))
        Spacer(Modifier.width(4.dp))
        Text("$label 제안 · 탭해서 확인", color = nudgePurple, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun NudgeActionChip(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(chipShape)
            .background(nudgePurple.copy(alpha = 0.2f))
            .border(1.dp, nudgePurple.copy(alpha = 0.6f), chipShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = nudgePurple, modifier = Modifier.size(11.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, color = nudgePurple, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun NormalCard(item: NotificationItem, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = cardShape, clip = false)
            .clip(cardShape)
            .background(Color(0x993A3A3C))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(item.appName, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                Text(item.timeLabel, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(item.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.body, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ───────────────────────────────────────
//  ICON 모드
// ───────────────────────────────────────

@Composable
private fun IconModeRow(notifications: List<NotificationItem>, modifier: Modifier = Modifier) {
    if (notifications.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        notifications.take(6).forEach { item ->
            NotificationIconItem(item)
        }
    }
}

@Composable
private fun NotificationIconItem(item: NotificationItem) {
    val infiniteTransition = rememberInfiniteTransition(label = "icon_pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "border_alpha",
    )

    val iconShape = RoundedCornerShape(12.dp)
    val borderModifier = if (item.hasNudge) {
        Modifier.border(
            width = 2.dp,
            brush = Brush.linearGradient(
                listOf(nudgePurple.copy(alpha = borderAlpha), nudgeCyan.copy(alpha = borderAlpha)),
            ),
            shape = iconShape,
        )
    } else {
        Modifier
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .then(borderModifier)
                .clip(iconShape)
                .background(Color(0x882A2A2C))
                .padding(if (item.hasNudge) 3.dp else 0.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = item.appName,
                tint = if (item.hasNudge) nudgePurple else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = item.appName.take(4),
            color = if (item.hasNudge) nudgePurple else Color.White.copy(alpha = 0.6f),
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ───────────────────────────────────────
//  DOT 모드
// ───────────────────────────────────────

@Composable
private fun DotModeIndicator(notifications: List<NotificationItem>, modifier: Modifier = Modifier) {
    val hasNudge = notifications.any { it.hasNudge }

    val infiniteTransition = rememberInfiniteTransition(label = "dot_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_scale",
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_alpha",
    )

    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        notifications.forEach { item ->
            if (item.hasNudge && hasNudge) {
                // 넛지 알림 — 빨간 발광 점
                Box(contentAlignment = Alignment.Center) {
                    // 외부 글로우
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .graphicsLayer {
                                scaleX = glowScale
                                scaleY = glowScale
                                alpha = glowAlpha
                            }
                            .clip(CircleShape)
                            .background(Color(0xFFFF3B30).copy(alpha = 0.5f)),
                    )
                    // 내부 점
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF3B30)),
                    )
                }
            } else {
                // 일반 알림 — 흰색 점
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.5f)),
                )
            }
        }
    }
}

// ───────────────────────────────────────
//  권한 안내 배너
// ───────────────────────────────────────

@Composable
fun NotificationPermissionBanner(onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xCC2A2A2C))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "알림 접근 권한이 필요합니다",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(containerColor = nudgePurple),
            border = BorderStroke(0.dp, Color.Transparent),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text("설정", color = Color.White, fontSize = 12.sp)
        }
    }
}
