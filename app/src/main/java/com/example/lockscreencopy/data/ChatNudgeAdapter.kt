package com.example.lockscreencopy.data

import com.example.lockscreencopy.model.AppNotificationGroup
import com.example.lockscreencopy.model.ChatMessage
import com.example.lockscreencopy.model.ChatRoom
import com.example.lockscreencopy.model.MessageNudge
import com.example.lockscreencopy.model.NotificationItem

/** [NudgeResult]를 메시지에 붙일 [MessageNudge]로 변환한다. 넛지가 아니면 null. */
fun NudgeResult.toMessageNudge(): MessageNudge? =
    if (!hasNudge) null
    else MessageNudge(
        label = nudgeLabel,
        actions = actions,
        mapQuery = mapQuery,
        eventStartMillis = eventStartMillis,
    )

/**
 * 실시간(REAL)·Nano 테스트 공통 변환기.
 *
 * 각 알림을 "여러 메시지를 가진 채팅방"으로 보고 앱 이름으로 묶어
 * [AppNotificationGroup]을 만든다. 채팅 앱이 MessagingStyle로 보낸 안읽은 메시지들은
 * [NotificationItem.messages]에 담겨 있어, 더미 데이터처럼 방 하나에 여러 줄이 쌓여 보인다.
 * messages가 비어 있으면 [NotificationItem.body]/[NotificationItem.title] 한 줄로 대체한다.
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
                    messages = item.messages.ifEmpty {
                        listOf(
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
                        )
                    },
                )
            },
        )
    }
