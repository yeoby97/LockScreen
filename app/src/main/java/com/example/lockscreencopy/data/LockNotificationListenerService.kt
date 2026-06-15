package com.example.lockscreencopy.data

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.lockscreencopy.model.ChatMessage
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
     * 방 안의 각 메시지를 AI로 분석해, 넛지면 해당 메시지에 결과를 붙여 갱신한다.
     * 단톡방 폭주를 고려해, 값싼 1차 필터([NudgeAnalyzer.isCandidate])를 통과한
     * 메시지만 실제 분석(Nano/API 호출) 대상으로 삼는다.
     * [postMillis]는 "2시간 뒤" 같은 상대 시각 계산의 기준이 되는 메시지 수신 시각이다.
     */
    private fun refineNudge(item: NotificationItem, postMillis: Long) {
        scope.launch {
            val analyzed = item.messages.map { m ->
                if (!NudgeAnalyzer.isCandidate(m.sender, m.text)) {
                    m
                } else {
                    val refined = NudgeAnalyzer.analyze(m.sender, m.text, postMillis)
                    Log.i(TAG, "메시지 분석: id=${item.id} text='${m.text}' hasNudge=${refined.hasNudge} label='${refined.nudgeLabel}'")
                    m.copy(nudge = refined.toMessageNudge())
                }
            }
            NotificationRepository.updateMessages(item.id, analyzed)
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
        val body = extras.getString(Notification.EXTRA_BIG_TEXT)
            ?: extras.getString(Notification.EXTRA_TEXT)
            ?: ""
        val appName = NotificationRepository.resolveAppName(applicationContext, packageName)

        // 채팅 앱은 MessagingStyle로 최근 안읽은 메시지들을 함께 보낸다.
        // 이를 읽어 방 하나에 여러 줄이 쌓여 보이도록 한다(더미 데이터와 동일한 모습).
        val style = runCatching {
            NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
        }.getOrNull()

        val roomName = style?.conversationTitle?.toString()?.takeIf { it.isNotBlank() }
            ?: extras.getString(Notification.EXTRA_TITLE)?.takeIf { it.isNotBlank() }
            ?: return null

        val messages: List<ChatMessage> = style?.messages?.mapNotNull { m ->
            val text = m.text?.toString()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            ChatMessage(
                id = "$key#${m.timestamp}",
                sender = m.person?.name?.toString().orEmpty(),
                text = text,
                timeLabel = formatRelativeTime(m.timestamp),
            )
        }.orEmpty().ifEmpty {
            // MessagingStyle이 아니거나 비었으면 본문 한 줄로 대체.
            val single = body.ifBlank { roomName }
            listOf(ChatMessage(id = "$key#single", sender = "", text = single, timeLabel = formatRelativeTime(postTime)))
        }

        // 넛지는 전적으로 AI(refineNudge)가 메시지별로 채운다. 도착 시점엔 일반 메시지로 표시.
        return NotificationItem(
            id = key,
            appName = appName,
            title = roomName,
            body = body,
            timeLabel = formatRelativeTime(postTime),
            messages = messages,
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
