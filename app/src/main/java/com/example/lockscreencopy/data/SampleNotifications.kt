package com.example.lockscreencopy.data

import com.example.lockscreencopy.model.NotificationItem

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
    ),
    NotificationItem(
        id = "notif_2",
        appName = "카카오톡 단체방",
        title = "개발자 스터디 모임 (12)",
        body = "지호: 다음 주 화요일 오후 7시에 판교 스타벅스 2층으로 모여요! 참석 여부 댓글로 알려주세요",
        timeLabel = "3분 전",
        hasNudge = true,
        nudgeLabel = "일정 추가",
        nudgeActions = listOf("일정 추가"),
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
    ),
)
