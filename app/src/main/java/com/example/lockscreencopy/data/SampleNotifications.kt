package com.example.lockscreencopy.data

import com.example.lockscreencopy.model.NotificationItem
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

/** 데모용: 다가오는 [day] 요일 [hour]시 정각의 epoch millis (항상 미래로). */
private fun nextDayAt(day: DayOfWeek, hour: Int): Long {
    val now = ZonedDateTime.now()
    var t = now.with(TemporalAdjusters.nextOrSame(day))
        .withHour(hour).withMinute(0).withSecond(0).withNano(0)
    if (!t.isAfter(now)) t = t.plusWeeks(1)
    return t.toInstant().toEpochMilli()
}

/** 데모용: 다가오는 [month]월 [day]일 [hour]시의 epoch millis (지났으면 내년). */
private fun nextDateAt(month: Int, day: Int, hour: Int): Long {
    val now = ZonedDateTime.now()
    var t = now.withMonth(month).withDayOfMonth(day)
        .withHour(hour).withMinute(0).withSecond(0).withNano(0)
    if (!t.isAfter(now)) t = t.plusYears(1)
    return t.toInstant().toEpochMilli()
}

fun sampleNotifications(): List<NotificationItem> = listOf(
    NotificationItem(
        id = "notif_1",
        appName = "카카오 오픈채팅",
        title = "서울 맛집 탐방방 (234)",
        body = "민준: 이번 토요일 저녁 6시에 강남역 신상 한우집 어때요? 소고기 코스 3만원대래요 ㅎㅎ",
        timeLabel = "방금",
        hasNudge = true,
        nudgeLabel = "일정 추가",
        nudgeActions = listOf("일정 추가", "지도 열기"),
        mapQuery = "강남역 신상 한우집",
        eventStartMillis = nextDayAt(DayOfWeek.SATURDAY, 18),
    ),
    NotificationItem(
        id = "notif_2",
        appName = "카카오톡 단체방",
        title = "개발자 스터디 모임 (12)",
        body = "지호: 다음 주 화요일 오후 7시에 판교 스타벅스 2층으로 모여요! 참석 여부 댓글로 알려주세요",
        timeLabel = "3분 전",
        hasNudge = true,
        nudgeLabel = "일정 추가",
        nudgeActions = listOf("일정 추가", "지도 열기"),
        mapQuery = "판교 스타벅스",
        eventStartMillis = nextDayAt(DayOfWeek.TUESDAY, 19),
    ),
    NotificationItem(
        id = "notif_3",
        appName = "배달의민족",
        title = "주문 접수 완료",
        body = "주문하신 음식이 접수되었습니다. 예상 도착 시간 30~40분",
        timeLabel = "8분 전",
        hasNudge = false,
    ),
    NotificationItem(
        id = "notif_4",
        appName = "카카오 오픈채팅",
        title = "제주 여행 계획방 (89)",
        body = "서연: 7월 14일(토) ~ 16일(월) 2박3일로 제주도 어때요? 항공권 지금 사면 왕복 10만원이에요!",
        timeLabel = "21분 전",
        hasNudge = true,
        nudgeLabel = "일정 추가",
        nudgeActions = listOf("일정 추가"),
        eventStartMillis = nextDateAt(7, 14, 9),
    ),
)
