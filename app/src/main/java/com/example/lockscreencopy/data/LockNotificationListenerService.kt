package com.example.lockscreencopy.data

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.lockscreencopy.model.NotificationItem
import java.util.concurrent.TimeUnit

class LockNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        val items = try {
            activeNotifications.mapNotNull { it.toItem() }
        } catch (_: Exception) {
            emptyList()
        }
        NotificationRepository.reset(items)
    }

    override fun onListenerDisconnected() {
        NotificationRepository.reset(emptyList())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val item = sbn.toItem() ?: return
        NotificationRepository.add(item)
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
        val (hasNudge, nudgeLabel, nudgeActions) = detectNudge(title, body)

        return NotificationItem(
            id = key,
            appName = appName,
            title = title,
            body = body,
            timeLabel = formatRelativeTime(postTime),
            hasNudge = hasNudge,
            nudgeLabel = nudgeLabel,
            nudgeActions = nudgeActions,
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
