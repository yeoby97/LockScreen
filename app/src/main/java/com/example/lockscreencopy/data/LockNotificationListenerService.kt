package com.example.lockscreencopy.data

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.lockscreencopy.model.NotificationItem
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LockNotificationListenerService : NotificationListenerService() {

    private companion object {
        const val TAG = "NudgeAnalyzer"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onListenerConnected() {
        Log.i(TAG, "리스너 연결됨. API 키 설정=${NudgeAnalyzer.hasApiKey()}")
        // 첫 넛지 전에도 화면 표시(Nano/Cloud)가 채워지도록 엔진을 미리 확인한다.
        scope.launch { NudgeAnalyzer.refreshEngine() }
        val pairs = try {
            activeNotifications.mapNotNull { sbn -> sbn.toItem()?.let { it to sbn.postTime } }
        } catch (_: Exception) {
            emptyList()
        }
        NotificationRepository.reset(pairs.map { it.first })
        pairs.forEach { (item, postMillis) -> refineNudge(item, postMillis) }
    }

    override fun onListenerDisconnected() {
        Log.i(TAG, "리스너 연결 해제됨")
        NotificationRepository.reset(emptyList())
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val item = sbn.toItem()
        if (item == null) {
            Log.d(TAG, "알림 무시(시스템/진행중/비밀/제목없음): ${sbn.packageName}")
            return
        }
        Log.i(TAG, "알림 수신: app='${item.appName}' title='${item.title}' body='${item.body}'")
        NotificationRepository.add(item)
        refineNudge(item, sbn.postTime)
    }

    /**
     * 일반 알림으로 먼저 표시한 메시지를 AI로 분석해, 넛지면 결과를 채워 갱신한다.
     * 단톡방 폭주를 고려해, 값싼 1차 필터([NudgeAnalyzer.isCandidate])를 통과한
     * 메시지만 실제 분석(API 호출) 대상으로 삼는다.
     * [postMillis]는 "2시간 뒤" 같은 상대 시각 계산의 기준이 되는 메시지 수신 시각이다.
     */
    private fun refineNudge(item: NotificationItem, postMillis: Long) {
        if (!NudgeAnalyzer.isCandidate(item.title, item.body)) {
            Log.d(TAG, "1차 필터 제외(너무 짧음): title='${item.title}' body='${item.body}'")
            return
        }
        scope.launch {
            val refined = NudgeAnalyzer.analyze(item.title, item.body, postMillis)
            if (refined.hasNudge != item.hasNudge ||
                refined.nudgeLabel != item.nudgeLabel ||
                refined.actions != item.nudgeActions ||
                refined.mapQuery != item.mapQuery ||
                refined.eventStartMillis != item.eventStartMillis
            ) {
                Log.i(TAG, "넛지 갱신: id=${item.id} hasNudge=${refined.hasNudge} label='${refined.nudgeLabel}' actions=${refined.actions} mapQuery='${refined.mapQuery}' startMillis=${refined.eventStartMillis}")
                NotificationRepository.updateNudge(item.id, refined)
            } else {
                Log.d(TAG, "넛지 변경 없음: id=${item.id} (hasNudge=${refined.hasNudge})")
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
