package com.example.lockscreencopy.data

import com.example.lockscreencopy.model.AppNotificationGroup
import com.example.lockscreencopy.model.ChatMessage
import com.example.lockscreencopy.model.ChatRoom
import com.example.lockscreencopy.model.MessageNudge
import com.example.lockscreencopy.model.NotificationItem

/**
 * 실시간(REAL) 모드 변환기.
 *
 * 현재 리스너는 알림당 최신 한 줄(EXTRA_TEXT/BIG_TEXT)만 읽으므로, 알림 하나를
 * "메시지 1개를 가진 채팅방"으로 본 뒤 앱 이름으로 묶어 [AppNotificationGroup]을 만든다.
 * (추후 리스너가 MessagingStyle의 EXTRA_MESSAGES를 읽어 쌓인 메시지 목록을 채우면,
 *  같은 채팅방의 [ChatRoom.messages]가 여러 줄로 확장된다 — UI는 그대로 동작.)
 *
 * 저장소가 최신순을 유지하므로 groupBy의 등장 순서가 곧 최신순이다.
 */
fun List<NotificationItem>.toAppGroups(): List<AppNotificationGroup> =
    groupBy { it.appName }.map { (appName, items) ->
        AppNotificationGroup(
            appName = appName,
            rooms = items.map { item ->
                ChatRoom(
                    id = item.id,
                    roomName = item.title,
                    appName = appName,
                    messages = listOf(
                        ChatMessage(
                            id = item.id,
                            sender = "",
                            text = item.body.ifBlank { item.title },
                            timeLabel = item.timeLabel,
                            nudge = if (item.hasNudge) {
                                MessageNudge(
                                    label = item.nudgeLabel,
                                    actions = item.nudgeActions,
                                    mapQuery = item.mapQuery,
                                    eventStartMillis = item.eventStartMillis,
                                )
                            } else {
                                null
                            },
                        ),
                    ),
                )
            },
        )
    }
