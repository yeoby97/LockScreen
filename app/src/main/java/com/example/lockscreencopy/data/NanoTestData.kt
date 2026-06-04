package com.example.lockscreencopy.data

import com.example.lockscreencopy.model.NotificationItem

/**
 * Nano 테스트 모드용 원본 메시지.
 *
 * 실시간 알림 접근 권한 없이도 넛지 분석 파이프라인(Nano → 클라우드 폴백)을 검증하기 위한
 * 가짜 알림이다. 여기 항목들은 일부러 넛지를 비워(hasNudge=false) 두어, 실제로 AI가
 * 분석해 채우도록 한다. 즉 화면에 넛지가 뜨면 그건 곧 'Nano(또는 폴백 API)가 찾아낸 것'이다.
 *
 * 행동으로 이어지는 메시지(약속/장소)와 단순 잡담을 섞어, 모델이 둘을 구분하는지 본다.
 */
fun nanoTestNotifications(): List<NotificationItem> = listOf(
    NotificationItem(
        id = "nano_test_1",
        appName = "Nano 테스트",
        title = "동네 친구들 (5)",
        body = "수빈: 이번 주 토요일 저녁 7시에 홍대 곱창집에서 모이자! 다들 시간 되지?",
        timeLabel = "방금",
    ),
    NotificationItem(
        id = "nano_test_2",
        appName = "Nano 테스트",
        title = "회사 점심팟 (8)",
        body = "팀장님: 내일 12시에 강남 교보타워 1층 로비에서 봅시다",
        timeLabel = "방금",
    ),
    NotificationItem(
        id = "nano_test_3",
        appName = "Nano 테스트",
        title = "주말 등산 모임 (21)",
        body = "민재: 우리 김밥천국 들렀다 갈까요?",
        timeLabel = "1분 전",
    ),
    NotificationItem(
        id = "nano_test_4",
        appName = "Nano 테스트",
        title = "수다방 (102)",
        body = "지원: ㅋㅋㅋㅋ 그거 진짜 웃기다 ㅠㅠ 나도 봤어",
        timeLabel = "2분 전",
    ),
    NotificationItem(
        id = "nano_test_5",
        appName = "Nano 테스트",
        title = "가족방 (4)",
        body = "엄마: 저녁에 뭐 먹고싶어? 마트 가는 길이야",
        timeLabel = "3분 전",
    ),
)

/**
 * Nano 테스트 모드 실행기.
 *
 * 가짜 알림을 [NotificationRepository]에 채운 뒤, 실시간 모드와 동일하게 [NudgeAnalyzer]로
 * 한 건씩 분석해 넛지를 갱신한다. 어떤 엔진(Nano/Cloud)이 쓰였는지는 화면 상단 칩에 표시된다.
 */
object NanoTestRunner {
    suspend fun run() {
        val items = nanoTestNotifications()
        NotificationRepository.reset(items)
        // 분석 전, 현재 엔진을 미리 표시(확인 중 → Nano/Cloud).
        NudgeAnalyzer.refreshEngine()
        for (item in items) {
            if (!NudgeAnalyzer.isCandidate(item.title, item.body)) continue
            val refined = NudgeAnalyzer.analyze(item.title, item.body)
            NotificationRepository.updateNudge(item.id, refined)
        }
    }
}
