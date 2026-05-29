package com.example.lockscreencopy.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.example.lockscreencopy.model.NotificationItem

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
    }

private fun buildMapsIntent(item: NotificationItem): Intent? {
    // 지도 검색어는 AI가 정제한 장소명(mapQuery)만 사용한다.
    val query = item.mapQuery.takeIf { it.isNotBlank() } ?: return null
    return Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}"))
}
