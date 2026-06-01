package com.example.lockscreencopy.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.example.lockscreencopy.model.ChatMessage
import com.example.lockscreencopy.model.NotificationItem
import java.util.concurrent.TimeUnit

/**
 * 채팅방 안의 특정 메시지(넛지)에 대한 액션 실행.
 * 일정 제목은 방 이름, 설명/검색은 그 메시지 본문·예측값을 사용한다.
 */
fun executeChatNudgeAction(context: Context, action: String, roomName: String, message: ChatMessage) {
    val nudge = message.nudge ?: return
    val intent = when (action) {
        "일정 추가" -> Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
            putExtra(CalendarContract.Events.TITLE, roomName)
            putExtra(CalendarContract.Events.DESCRIPTION, message.text)
            if (nudge.eventStartMillis > 0L) {
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, nudge.eventStartMillis)
                putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    nudge.eventStartMillis + TimeUnit.HOURS.toMillis(1),
                )
            }
        }
        "지도 열기" -> nudge.mapQuery.takeIf { it.isNotBlank() }
            ?.let { Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(it)}")) }
        else -> null
    } ?: return
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

fun executeNudgeAction(context: Context, action: String, item: NotificationItem) {
    val intent = when (action) {
        "일정 추가" -> buildCalendarIntent(item)
        "지도 열기" -> buildMapsIntent(item)
        else -> null
    } ?: return
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

private fun buildCalendarIntent(item: NotificationItem): Intent =
    Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
        putExtra(CalendarContract.Events.TITLE, item.title)
        putExtra(CalendarContract.Events.DESCRIPTION, item.body)
        // AI가 시작 시각을 예측했으면 시작/종료(기본 1시간)까지 채워서 연다.
        if (item.eventStartMillis > 0L) {
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, item.eventStartMillis)
            putExtra(
                CalendarContract.EXTRA_EVENT_END_TIME,
                item.eventStartMillis + TimeUnit.HOURS.toMillis(1),
            )
        }
    }

private fun buildMapsIntent(item: NotificationItem): Intent? {
    // 지도 검색어는 AI가 정제한 장소명(mapQuery)만 사용한다.
    val query = item.mapQuery.takeIf { it.isNotBlank() } ?: return null
    return Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}"))
}
