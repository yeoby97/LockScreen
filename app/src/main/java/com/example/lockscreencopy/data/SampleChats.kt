package com.example.lockscreencopy.data

import com.example.lockscreencopy.model.AppNotificationGroup
import com.example.lockscreencopy.model.ChatMessage
import com.example.lockscreencopy.model.ChatRoom
import com.example.lockscreencopy.model.MessageNudge
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

/** 다가오는 [day] 요일 [hour]시 정각의 epoch millis (항상 미래). */
private fun nextDayAt(day: DayOfWeek, hour: Int): Long {
    val now = ZonedDateTime.now()
    var t = now.with(TemporalAdjusters.nextOrSame(day))
        .withHour(hour).withMinute(0).withSecond(0).withNano(0)
    if (!t.isAfter(now)) t = t.plusWeeks(1)
    return t.toInstant().toEpochMilli()
}

/** 내일 [hour]시 정각의 epoch millis. */
private fun tomorrowAt(hour: Int): Long =
    ZonedDateTime.now().plusDays(1)
        .withHour(hour).withMinute(0).withSecond(0).withNano(0)
        .toInstant().toEpochMilli()

private var msgSeq = 0
private fun msg(sender: String, text: String, time: String, nudge: MessageNudge? = null) =
    ChatMessage(id = "m${msgSeq++}", sender = sender, text = text, timeLabel = time, nudge = nudge)

/**
 * 데모용 채팅 더미. 앱 → 채팅방 → 메시지 3계층.
 * 단톡방 폭주(잡담 다수) 속에 행동 예측(넛지) 메시지가 섞여 있는 모습을 보여준다.
 * 방마다 메시지 수와 넛지 수를 다양하게 구성했다.
 */
fun sampleChats(): List<AppNotificationGroup> {
    msgSeq = 0
    return listOf(
        // ── 카카오톡: 채팅방 3개 (메시지 11 / 8 / 4, 넛지 2 / 1 / 1) ──
        AppNotificationGroup(
            appName = "카카오톡",
            rooms = listOf(
                ChatRoom(
                    id = "kt_bunge",
                    roomName = "동아리 번개방",
                    appName = "카카오톡",
                    messages = listOf(
                        msg("지호", "다들 오늘 저녁 시간 되시는 분?", "12분 전"),
                        msg("수아", "저는 저녁부터 가능해요!", "11분 전"),
                        msg("현우", "ㅇㅋㅇㅋ 콜", "10분 전"),
                        msg(
                            "민준",
                            "그럼 이번 토요일 저녁 6시에 강남역 신상 한우집 어때요? 소고기 코스 3만원대래요 ㅎㅎ",
                            "8분 전",
                            MessageNudge(
                                label = "일정 추가",
                                actions = listOf("일정 추가", "지도 열기"),
                                mapQuery = "강남역 신상 한우집",
                                eventStartMillis = nextDayAt(DayOfWeek.SATURDAY, 18),
                            ),
                        ),
                        msg("지호", "오 좋아요 한우 ㅠㅠ", "7분 전"),
                        msg("수아", "예약은 누가 하나요", "6분 전"),
                        msg("민준", "제가 할게요~", "5분 전"),
                        msg("현우", "굿굿 인원 몇명이죠", "4분 전"),
                        msg("지호", "지금 4명이요", "3분 전"),
                        msg(
                            "민준",
                            "그리고 끝나고 홍대 가서 가볍게 2차 ㄱㄱ",
                            "방금",
                            MessageNudge(
                                label = "지도 열기",
                                actions = listOf("지도 열기"),
                                mapQuery = "홍대입구역",
                            ),
                        ),
                        msg("현우", "ㅋㅋㅋㅋ 좋습니다", "방금"),
                    ),
                ),
                ChatRoom(
                    id = "kt_danggn",
                    roomName = "김당근 (아이폰 거래)",
                    appName = "카카오톡",
                    messages = listOf(
                        msg("김당근", "안녕하세요! 아이폰 14 보고 연락드려요", "30분 전"),
                        msg("김당근", "상태 괜찮나요?", "29분 전"),
                        msg("김당근", "직거래 되면 좋을 것 같아요", "28분 전"),
                        msg(
                            "김당근",
                            "혹시 토요일 오후 2시에 서울숲역 2번 출구에서 거래 가능하세요?",
                            "27분 전",
                            MessageNudge(
                                label = "일정 추가",
                                actions = listOf("일정 추가", "지도 열기"),
                                mapQuery = "서울숲역 2번 출구",
                                eventStartMillis = nextDayAt(DayOfWeek.SATURDAY, 14),
                            ),
                        ),
                        msg("김당근", "충전기도 같이 주시는 거죠?", "26분 전"),
                        msg("김당근", "넵 알겠습니다", "25분 전"),
                        msg("김당근", "그럼 토요일에 봬요!", "24분 전"),
                        msg("김당근", "혹시 모르니 연락처 저장해둘게요", "22분 전"),
                    ),
                ),
                ChatRoom(
                    id = "kt_mom",
                    roomName = "엄마",
                    appName = "카카오톡",
                    messages = listOf(
                        msg("엄마", "밥은 먹었니", "1시간 전"),
                        msg(
                            "엄마",
                            "내일 오후 3시에 너 치과 예약해놨어 잊지 말고 가",
                            "55분 전",
                            MessageNudge(
                                label = "일정 추가",
                                actions = listOf("일정 추가"),
                                eventStartMillis = tomorrowAt(15),
                            ),
                        ),
                        msg("엄마", "끝나고 전화해", "54분 전"),
                        msg("엄마", "알겠지?", "54분 전"),
                    ),
                ),
            ),
        ),
        // ── 텔레그램: 채팅방 2개 (메시지 16 / 6, 넛지 0 / 1) ──
        AppNotificationGroup(
            appName = "텔레그램",
            rooms = listOf(
                ChatRoom(
                    id = "tg_coin",
                    roomName = "코인 차트방 (1.2k)",
                    appName = "텔레그램",
                    messages = listOf(
                        msg("Alex", "오늘 비트 흐름 좋네요", "20분 전"),
                        msg("민트", "그러게요 ㅎㅎ", "19분 전"),
                        msg("Alex", "이더도 같이 오르는 중", "19분 전"),
                        msg("코린이", "저 어제 물렸어요 ㅠㅠ", "18분 전"),
                        msg("민트", "존버하세요", "18분 전"),
                        msg("Alex", "ㅋㅋㅋ 존버가 답", "17분 전"),
                        msg("차트왕", "지금 차트 보면 지지선 단단함", "15분 전"),
                        msg("민트", "오 분석 감사", "14분 전"),
                        msg("코린이", "다들 어디서 보세요?", "12분 전"),
                        msg("Alex", "트레이딩뷰요", "11분 전"),
                        msg("차트왕", "거래량도 슬슬 붙는 듯", "9분 전"),
                        msg("민트", "기대되네요", "8분 전"),
                        msg("코린이", "감사합니다 공부 더 할게요", "6분 전"),
                        msg("Alex", "화이팅입니다", "4분 전"),
                        msg("차트왕", "다들 성투하세요", "2분 전"),
                        msg("민트", "🚀🚀🚀", "1분 전"),
                    ),
                ),
                ChatRoom(
                    id = "tg_proj",
                    roomName = "프로젝트 A 팀",
                    appName = "텔레그램",
                    messages = listOf(
                        msg("Jay", "내일 회의 관련 공유드려요", "40분 전"),
                        msg("Jay", "원래 오전 10시였는데", "39분 전"),
                        msg(
                            "Jay",
                            "내일 오전 11시로 회의 시간 변경됐습니다. 참고 부탁드려요",
                            "38분 전",
                            MessageNudge(
                                label = "일정 추가",
                                actions = listOf("일정 추가"),
                                eventStartMillis = tomorrowAt(11),
                            ),
                        ),
                        msg("소라", "넵 확인했습니다", "36분 전"),
                        msg("Jay", "자료 미리 보고 오세요", "35분 전"),
                        msg("소라", "감사합니다 👍", "34분 전"),
                    ),
                ),
            ),
        ),
    )
}
