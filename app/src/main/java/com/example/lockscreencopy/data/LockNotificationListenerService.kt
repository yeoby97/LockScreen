package com.example.lockscreencopy.data

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.lockscreencopy.model.NotificationItem
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LockNotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onListenerConnected() {
        val items = try {
            activeNotifications.mapNotNull { it.toItem() }
        } catch (_: Exception) {
            emptyList()
        }
        NotificationRepository.reset(items)
        items.forEach { refineNudge(it) }
    }

    override fun onListenerDisconnected() {
        NotificationRepository.reset(emptyList())
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val item = sbn.toItem() ?: return
        NotificationRepository.add(item)
        refineNudge(item)
    }

    /**
     * 일반 알림으로 먼저 표시한 메시지를 AI로 분석해, 넛지면 결과를 채워 갱신한다.
     * 단톡방 폭주를 고려해, 값싼 1차 필터([NudgeAnalyzer.isCandidate])를 통과한
     * 메시지만 실제 분석(API 호출) 대상으로 삼는다.
     */
    private fun refineNudge(item: NotificationItem) {
        if (!NudgeAnalyzer.isCandidate(item.title, item.body)) return
        scope.launch {
            val refined = NudgeAnalyzer.analyze(item.title, item.body)
            if (refined.hasNudge != item.hasNudge ||
                refined.nudgeLabel != item.nudgeLabel ||
                refined.actions != item.nudgeActions ||
                refined.mapQuery != item.mapQuery
            ) {
                NotificationRepository.updateNudge(item.id, refined)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationRepository.remove(sbn.key)
    }

    private fun StatusBarNotification.toItem(): NotificationItem? {
        if (packageName == applicationContext.packageName) return null
        if (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return null
        if (notification.visibility == Notification.VISIBILITY_SECRET) return null

        val extras = notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE)?.takeIf { it.isNotBlank() }
            ?: return null
        val body = extras.getString(Notification.EXTRA_BIG_TEXT)
            ?: extras.getString(Notification.EXTRA_TEXT)
            ?: ""
        val appName = NotificationRepository.resolveAppName(applicationContext, packageName)

        // 넛지는 전적으로 AI(refineNudge)가 채운다. 도착 시점엔 일반 알림으로 표시.
        return NotificationItem(
            id = key,
            appName = appName,
            title = title,
            body = body,
            timeLabel = formatRelativeTime(postTime),
        )
    }

    private fun formatRelativeTime(postTime: Long): String {
        val diff = System.currentTimeMillis() - postTime
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "방금"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}분 전"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}시간 전"
            else -> "${TimeUnit.MILLISECONDS.toDays(diff)}일 전"
        }
    }
}
