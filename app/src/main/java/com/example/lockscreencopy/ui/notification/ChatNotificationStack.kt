package com.example.lockscreencopy.ui.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.AppNotificationGroup
import com.example.lockscreencopy.model.ChatMessage
import com.example.lockscreencopy.model.ChatRoom

private val nudgePurple = Color(0xFF9B78FF)
private val nudgeCyan = Color(0xFF4FC3F7)
private val nudgeGradient = Brush.linearGradient(listOf(nudgePurple, nudgeCyan))
private val cardShape = RoundedCornerShape(20.dp)
private val cardBg = Color(0xCC2A2A2C)
private val chipShape = RoundedCornerShape(50)

/**
 * 갤럭시식 3단 알림 스택.
 *  A) 접힘  : 앱별 겹침 스택(최신 메시지 1개 + 뒤에 쌓인 방 표시)
 *  B) 펼침  : 앱 헤더로 그룹된 채팅방 카드 목록 (상태바 내린 느낌)
 *  C) 방 펼침: 한 방의 메시지를 한 카드 컬럼으로. [전체]/[행동만] 필터.
 */
@Composable
fun ChatNotificationStack(
    appGroups: List<AppNotificationGroup>,
    modifier: Modifier = Modifier,
    onNudgeAction: (action: String, roomName: String, message: ChatMessage) -> Unit = { _, _, _ -> },
    onOpenChat: (ChatRoom) -> Unit = {},
) {
    if (appGroups.isEmpty()) return
    var shadeOpen by remember { mutableStateOf(false) }

    if (!shadeOpen) {
        CollapsedStacks(appGroups, modifier) { shadeOpen = true }
    } else {
        ExpandedShade(appGroups, modifier, onNudgeAction, onOpenChat) { shadeOpen = false }
    }
}

// ── State A: 앱별 겹침 스택 ────────────────────────────────────────
@Composable
private fun CollapsedStacks(
    appGroups: List<AppNotificationGroup>,
    modifier: Modifier,
    onOpen: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        appGroups.forEach { group ->
            AppStackCard(
                group = group,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen() },
            )
        }
    }
}

@Composable
private fun AppStackCard(group: AppNotificationGroup, modifier: Modifier = Modifier) {
    val peek = (group.roomCount - 1).coerceIn(0, 2)
    val room = group.topRoom ?: return
    Box(modifier) {
        // 뒤에 겹쳐 쌓인 방들(아래로 살짝 삐져나와 보임)
        for (depth in peek downTo 1) {
            Box(
                Modifier
                    .matchParentSize()
                    .padding(horizontal = (depth * 8).dp)
                    .offset(y = (depth * 5).dp)
                    .clip(cardShape)
                    .background(cardBg.copy(alpha = (0.55f - depth * 0.13f).coerceAtLeast(0.22f))),
            )
        }
        AppTopCard(group, room)
    }
}

@Composable
private fun AppTopCard(group: AppNotificationGroup, room: ChatRoom) {
    val latest = room.latest
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(8.dp, cardShape, clip = false)
            .clip(cardShape)
            .then(if (group.nudgeCount > 0) Modifier.border(1.dp, nudgeGradient, cardShape) else Modifier)
            .background(cardBg)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(group.appName, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Text(latest?.timeLabel.orEmpty(), color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
        }
        Spacer(Modifier.height(3.dp))
        Text(
            room.roomName,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(1.dp))
        Text(
            messagePreview(latest),
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (group.nudgeCount > 0) NudgeBadge("행동 ${group.nudgeCount}건")
            Spacer(Modifier.weight(1f))
            Text("채팅방 ${group.roomCount}개 · 탭하면 펼치기", color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp)
        }
    }
}

// ── State B + C: 펼친 목록 ────────────────────────────────────────
@Composable
private fun ExpandedShade(
    appGroups: List<AppNotificationGroup>,
    modifier: Modifier,
    onNudgeAction: (String, String, ChatMessage) -> Unit,
    onOpenChat: (ChatRoom) -> Unit,
    onClose: () -> Unit,
) {
    var expandedRoomId by remember { mutableStateOf<String?>(null) }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        item(key = "handle") { ShadeHandle(onClose) }
        appGroups.forEach { group ->
            item(key = "hdr_${group.appName}") { AppHeader(group) }
            items(group.rooms, key = { it.id }) { room ->
                RoomCard(
                    room = room,
                    expanded = expandedRoomId == room.id,
                    onToggle = { expandedRoomId = if (expandedRoomId == room.id) null else room.id },
                    onNudgeAction = onNudgeAction,
                    onOpenChat = onOpenChat,
                )
            }
        }
    }
}

@Composable
private fun ShadeHandle(onClose: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable { onClose() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(chipShape)
                .background(Color.White.copy(alpha = 0.35f)),
        )
    }
}

@Composable
private fun AppHeader(group: AppNotificationGroup) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(14.dp)
                .clip(chipShape)
                .background(nudgePurple),
        )
        Spacer(Modifier.width(7.dp))
        Text(group.appName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(7.dp))
        Text("채팅방 ${group.roomCount}개", color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp)
        if (group.nudgeCount > 0) {
            Spacer(Modifier.width(7.dp))
            NudgeBadge("${group.nudgeCount}")
        }
    }
}

@Composable
private fun RoomCard(
    room: ChatRoom,
    expanded: Boolean,
    onToggle: () -> Unit,
    onNudgeAction: (String, String, ChatMessage) -> Unit,
    onOpenChat: (ChatRoom) -> Unit,
) {
    var nudgeOnly by remember { mutableStateOf(false) }
    val hasNudge = room.nudgeCount > 0
    val arrowRotation by animateFloatAsState(if (expanded) 90f else 0f, tween(250), label = "arrow")

    Column(
        Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .then(if (hasNudge) Modifier.border(1.dp, nudgeGradient, cardShape) else Modifier)
            .background(cardBg),
    ) {
        // 헤더(탭하면 방 펼침/접힘)
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        room.roomName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("${room.messageCount}", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                    if (hasNudge) {
                        Spacer(Modifier.width(6.dp))
                        NudgeBadge("${room.nudgeCount}")
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    messagePreview(room.latest),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (expanded) "접기" else "펼치기",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(22.dp)
                    .rotate(arrowRotation),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(tween(200)),
            exit = shrinkVertically() + fadeOut(tween(150)),
        ) {
            Column(Modifier.fillMaxWidth()) {
                Divider()
                if (hasNudge) {
                    FilterToggle(nudgeOnly) { nudgeOnly = it }
                    Divider()
                }
                val shown = if (nudgeOnly) room.messages.filter { it.nudge != null } else room.messages
                shown.forEach { m ->
                    MessageRow(room.roomName, m, onNudgeAction)
                }
                // 원문(전체 대화)으로 점프
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChat(room) }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("채팅 열기", color = nudgeCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = nudgeCyan,
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterToggle(nudgeOnly: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip("전체", selected = !nudgeOnly) { onChange(false) }
        FilterChip("✨ 행동만", selected = nudgeOnly) { onChange(true) }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        color = if (selected) Color.White else Color.White.copy(alpha = 0.55f),
        fontSize = 11.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(chipShape)
            .clickable(onClick = onClick)
            .background(if (selected) nudgePurple.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun MessageRow(
    roomName: String,
    message: ChatMessage,
    onNudgeAction: (String, String, ChatMessage) -> Unit,
) {
    val isNudge = message.nudge != null
    Column(
        Modifier
            .fillMaxWidth()
            .background(if (isNudge) nudgePurple.copy(alpha = 0.10f) else Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (message.sender.isNotBlank()) {
                Text(message.sender, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
            } else {
                Spacer(Modifier.weight(1f))
            }
            Text(message.timeLabel, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
        }
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Top) {
            if (isNudge) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = nudgePurple,
                    modifier = Modifier
                        .padding(top = 2.dp, end = 4.dp)
                        .size(12.dp),
                )
            }
            Text(
                message.text,
                color = Color.White.copy(alpha = if (isNudge) 0.95f else 0.82f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
        if (isNudge) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                message.nudge!!.actions.forEach { action ->
                    ActionChip(action) { onNudgeAction(action, roomName, message) }
                }
            }
        }
    }
}

@Composable
private fun ActionChip(label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(chipShape)
            .clickable(onClick = onClick)
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
private fun NudgeBadge(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(chipShape)
            .background(nudgePurple.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = nudgePurple, modifier = Modifier.size(10.dp))
        Spacer(Modifier.width(3.dp))
        Text(text, color = nudgePurple, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Color.White.copy(alpha = 0.08f)),
    )
}

private fun messagePreview(message: ChatMessage?): String {
    if (message == null) return ""
    return if (message.sender.isBlank()) message.text else "${message.sender}: ${message.text}"
}
