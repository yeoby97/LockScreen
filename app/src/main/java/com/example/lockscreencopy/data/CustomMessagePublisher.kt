package com.example.lockscreencopy.data

import com.example.lockscreencopy.model.ChatMessage
import com.example.lockscreencopy.model.NotificationItem

/**
 * floating 모드에서 사용자가 직접 입력한 메시지를 "앱 알림"처럼 발행하는 발행기.
 *
 * 실제 카카오톡 등 외부 앱의 알림 없이도 넛지 시스템을 시험해볼 수 있도록,
 * 입력한 한 줄을 채팅방 알림([NotificationItem])으로 만들어 [NotificationRepository]에 넣고,
 * 실시간 모드([LockNotificationListenerService.refineNudge]) / Nano 테스트 모드
 * ([NanoTestRunner])와 **완전히 동일한** 온디바이스 우선 분석([NudgeAnalyzer.analyze])으로
 * 넛지를 채운다. 즉 여기서 뜨는 넛지도 곧 '현재 온디바이스(Nano→폴백) 엔진이 찾아낸 것'이다.
 */
object CustomMessagePublisher {

    private const val APP_NAME = "테스트 알림"
    private var seq = 0

    /**
     * 입력 메시지를 알림으로 발행하고 넛지를 분석해 붙인다.
     *
     * 실시간 알림과 동일하게 도착 시점엔 일반 메시지로 먼저 표시한 뒤,
     * 온디바이스 우선 분석 결과를 제자리에서 갱신한다.
     */
    suspend fun publish(text: String, roomName: String = "넛지 테스트") {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val now = System.currentTimeMillis()
        val id = "custom_${now}_${seq++}"
        val message = ChatMessage(
            id = "$id#msg",
            sender = "",
            text = trimmed,
            timeLabel = "방금",
        )
        val item = NotificationItem(
            id = id,
            appName = APP_NAME,
            title = roomName,
            body = trimmed,
            timeLabel = "방금",
            messages = listOf(message),
        )

        // ① 도착 시점엔 일반 메시지로 먼저 표시(실시간 알림과 동일한 흐름).
        NotificationRepository.add(item)

        // ② 현재 엔진(Nano/Cloud)을 화면에 미리 표시.
        NudgeAnalyzer.refreshEngine()

        // ③ 실시간 모드와 동일 경로로 온디바이스 우선 분석 → 넛지 갱신.
        val analyzed = if (!NudgeAnalyzer.isCandidate(message.sender, message.text)) {
            listOf(message)
        } else {
            val nudge = NudgeAnalyzer.analyze(message.sender, message.text, now).toMessageNudge()
            listOf(message.copy(nudge = nudge))
        }
        NotificationRepository.updateMessages(id, analyzed)
    }
}
