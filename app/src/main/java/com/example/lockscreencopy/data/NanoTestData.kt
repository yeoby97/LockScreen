package com.example.lockscreencopy.data

import com.example.lockscreencopy.model.ChatMessage
import com.example.lockscreencopy.model.NotificationItem

private var ntSeq = 0
private fun ntMsg(sender: String, text: String, time: String) =
    ChatMessage(id = "nt_m${ntSeq++}", sender = sender, text = text, timeLabel = time)

/**
 * Nano 테스트 모드용 원본 알림(채팅방).
 *
 * 실시간 알림 접근 권한 없이도 넛지 분석 파이프라인(Nano → 클라우드 폴백)을 검증하기 위한
 * 가짜 알림이다. 더미 데이터처럼 방 하나에 여러 메시지를 쌓아두되, 넛지는 일부러 비워둔다.
 * 즉 화면에 넛지가 뜨면 그건 곧 'Nano(또는 폴백 API)가 직접 찾아낸 것'이다.
 *
 * 행동으로 이어지는 메시지(약속/장소)와 단순 잡담을 섞어, 모델이 둘을 구분하는지 본다.
 */
fun nanoTestNotifications(): List<NotificationItem> {
    ntSeq = 0
    return listOf(
        NotificationItem(
            id = "nano_test_1",
            appName = "Nano 테스트",
            title = "동네 친구들 (5)",
            body = "",
            timeLabel = "방금",
            messages = listOf(
                ntMsg("수빈", "다들 이번 주말에 시간 돼?", "12분 전"),
                ntMsg("현우", "난 토요일 저녁만 가능", "11분 전"),
                ntMsg("수빈", "이번 주 토요일 저녁 7시에 홍대 곱창집에서 모이자!", "9분 전"),
                ntMsg("민지", "오 콜콜 ㅋㅋㅋ", "8분 전"),
                ntMsg("현우", "예약은 누가 함?", "방금"),
            ),
        ),
        NotificationItem(
            id = "nano_test_2",
            appName = "Nano 테스트",
            title = "회사 점심팟 (8)",
            body = "",
            timeLabel = "3분 전",
            messages = listOf(
                ntMsg("팀장님", "내일 점심 같이 하실 분?", "10분 전"),
                ntMsg("주임", "저요!", "9분 전"),
                ntMsg("팀장님", "그럼 내일 12시에 강남 교보타워 1층 로비에서 봅시다", "8분 전"),
                ntMsg("주임", "넵 알겠습니다", "7분 전"),
            ),
        ),
        NotificationItem(
            id = "nano_test_3",
            appName = "Nano 테스트",
            title = "주말 등산 모임 (21)",
            body = "",
            timeLabel = "5분 전",
            messages = listOf(
                ntMsg("민재", "다들 아침 일찍 출발하죠", "20분 전"),
                ntMsg("소연", "좋아요", "19분 전"),
                ntMsg("민재", "가는 길에 김밥천국 들렀다 갈까요?", "18분 전"),
                ntMsg("소연", "ㅋㅋ 김밥 좋죠", "17분 전"),
            ),
        ),
        NotificationItem(
            id = "nano_test_4",
            appName = "Nano 테스트",
            title = "수다방 (102)",
            body = "",
            timeLabel = "7분 전",
            messages = listOf(
                ntMsg("지원", "ㅋㅋㅋㅋ 그거 진짜 웃기다", "6분 전"),
                ntMsg("하늘", "나도 봤어 ㅠㅠ", "6분 전"),
                ntMsg("지원", "오늘 날씨 좋네", "5분 전"),
                ntMsg("하늘", "ㅇㅇ 산책가고싶다", "5분 전"),
            ),
        ),
    )
}

/**
 * Nano 테스트 모드 실행기.
 *
 * 가짜 알림을 [NotificationRepository]에 채운 뒤, 실시간 모드와 동일하게 [NudgeAnalyzer]로
 * 메시지를 한 줄씩 분석해 넛지를 갱신한다. 어떤 엔진(Nano/Cloud)이 쓰였는지는 화면 상단 칩에 표시된다.
 */
object NanoTestRunner {
    suspend fun run() {
        val items = nanoTestNotifications()
        NotificationRepository.reset(items)
        // 분석 전, 현재 엔진을 미리 표시(확인 중 → Nano/Cloud).
        NudgeAnalyzer.refreshEngine()
        for (item in items) {
            val analyzed = item.messages.map { m ->
                if (!NudgeAnalyzer.isCandidate(m.sender, m.text)) m
                else m.copy(nudge = NudgeAnalyzer.analyze(m.sender, m.text).toMessageNudge())
            }
            NotificationRepository.updateMessages(item.id, analyzed)
        }
    }
}
