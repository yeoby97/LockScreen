package com.example.lockscreencopy.data

data class DummyLlmCase(
    val title: String,
    val description: String,
    val userQuery: String,
    val selectedAppIds: List<String>,
    val recommendation: LlmRecommendation,
)

// 다양한 케이스: 트레이만, 자유배치만, 바로가기만, 가득찬 트레이, 중복 위젯,
// 그리고 5~7개 앱이 동시 추천되는 복잡한 시나리오 포함.
val dummyLlmCases: List<DummyLlmCase> = listOf(
    DummyLlmCase(
        title = "케이스 1: 운동할 때",
        description = "헬스 + 음악 + 손전등 (자유배치 + 바로가기)",
        userQuery = "운동할 때 필요한 거 배치해줘",
        selectedAppIds = listOf("health", "ytmusic", "clock", "sys_flashlight", "sys_sound"),
        recommendation = LlmRecommendation(
            tray = listOf("c_alarm", "ym3"),
            floating = listOf("h2", "ym2"),
            left = "sys_sound",
            right = "sys_flashlight",
        ),
    ),
    DummyLlmCase(
        title = "케이스 2: 출근길",
        description = "캘린더 + 네이버지도 + 위치/QR (앱 3개, 위젯 7개)",
        userQuery = "출근길에 필요한 거 보여줘",
        selectedAppIds = listOf("calendar", "navermap", "clock", "weather", "sys_location", "sys_qr"),
        recommendation = LlmRecommendation(
            tray = listOf("cal_today", "cal_next", "c_world"),
            floating = listOf("nm3", "nm1", "nm5", "cal_next2"),
            left = "sys_location",
            right = "sys_qr",
        ),
    ),
    DummyLlmCase(
        title = "케이스 3: 잠자기 전",
        description = "알람 + 다크모드 + 방해금지 + 디지털웰빙",
        userQuery = "잘 때 필요한 거",
        selectedAppIds = listOf("clock", "wellbeing", "battery", "reminder", "sys_dnd", "sys_dark_mode"),
        recommendation = LlmRecommendation(
            tray = listOf("c_alarm", "bat1", "r1"),
            floating = listOf("dw1", "dw2"),
            left = "sys_dnd",
            right = "sys_dark_mode",
        ),
    ),
    DummyLlmCase(
        title = "케이스 4: 트레이만 (SMALL 4개)",
        description = "작은 위젯 4개로 트레이 가득 채우기",
        userQuery = "날씨랑 시간 한눈에",
        selectedAppIds = listOf("weather", "clock"),
        recommendation = LlmRecommendation(
            tray = listOf("w_temp", "w_rain", "w_uv", "c_alarm"),
            floating = emptyList(),
            left = null,
            right = null,
        ),
    ),
    DummyLlmCase(
        title = "케이스 5: 트레이만 (WIDE 2개)",
        description = "와이드 위젯 2개로 트레이 가득",
        userQuery = "날씨 자세히",
        selectedAppIds = listOf("weather"),
        recommendation = LlmRecommendation(
            tray = listOf("w_temp2", "w_dust"),
            floating = emptyList(),
            left = null,
            right = null,
        ),
    ),
    DummyLlmCase(
        title = "케이스 6: 자유배치만 (5개)",
        description = "트레이/바로가기 없이 자유배치 5개",
        userQuery = "큰 위젯만 여러 개",
        selectedAppIds = listOf("navermap", "calendar", "health", "ytmusic"),
        recommendation = LlmRecommendation(
            tray = emptyList(),
            floating = listOf("nm5", "cal_next2", "h2", "ym1", "nm3"),
            left = null,
            right = null,
        ),
    ),
    DummyLlmCase(
        title = "케이스 7: 바로가기만",
        description = "좌/우 바로가기만 (위젯 없음)",
        userQuery = "하단 바로가기만 추천",
        selectedAppIds = listOf("sys_flashlight", "sys_qr"),
        recommendation = LlmRecommendation(
            tray = emptyList(),
            floating = emptyList(),
            left = "sys_flashlight",
            right = "sys_qr",
        ),
    ),
    DummyLlmCase(
        title = "케이스 8: 모든 영역 풀세팅",
        description = "5개 앱, 트레이 가득 + 자유배치 + 좌우 바로가기",
        userQuery = "전체적으로 다 채워줘",
        selectedAppIds = listOf(
            "weather", "calendar", "battery", "navermap", "health",
            "sys_flashlight", "sys_sound",
        ),
        recommendation = LlmRecommendation(
            tray = listOf("w_temp", "cal_today", "bat1", "h1"),
            floating = listOf("nm2", "cal_next2", "h2"),
            left = "sys_sound",
            right = "sys_flashlight",
        ),
    ),
    DummyLlmCase(
        title = "케이스 9: 중복 위젯 추천",
        description = "같은 위젯 ID가 여러 번 (인스턴스 분리 테스트)",
        userQuery = "같은 위젯 여러 개 쓰기",
        selectedAppIds = listOf("clock", "weather"),
        recommendation = LlmRecommendation(
            tray = listOf("c_world", "c_world", "w_temp", "w_uv"),
            floating = listOf("w_temp2", "w_temp2"),
            left = null,
            right = null,
        ),
    ),
    DummyLlmCase(
        title = "케이스 10: 회의/업무 (앱 6개)",
        description = "캘린더 + 리마인더 + 녹음 + 통역 + 방해금지 + 절전",
        userQuery = "회의할 때 필요한 거",
        selectedAppIds = listOf(
            "calendar", "reminder", "voice_recorder", "interpreter", "clock",
            "sys_dnd", "sys_power_saving",
        ),
        recommendation = LlmRecommendation(
            tray = listOf("cal_dday", "r1", "rec1", "c_world"),
            floating = listOf("cal_next2", "r2", "rec2", "int2"),
            left = "sys_dnd",
            right = "sys_power_saving",
        ),
    ),
    DummyLlmCase(
        title = "케이스 11: 여행 (앱 7개)",
        description = "지도/통역/카메라/갤러리/날씨/배터리/위치",
        userQuery = "여행 갈 때 쓸 거",
        selectedAppIds = listOf(
            "navermap", "interpreter", "camera", "gallery", "weather", "battery",
            "sys_flashlight", "sys_location",
        ),
        recommendation = LlmRecommendation(
            tray = listOf("int1", "cam1", "gal1", "bat1"),
            floating = listOf("nm5", "int2", "cam2", "w_temp2"),
            left = "sys_location",
            right = "sys_flashlight",
        ),
    ),
    DummyLlmCase(
        title = "케이스 12: 트레이 혼합 (WIDE + SMALL×2)",
        description = "와이드 1개 + 작은 위젯 2개로 4칸",
        userQuery = "혼합 트레이",
        selectedAppIds = listOf("weather", "calendar"),
        recommendation = LlmRecommendation(
            tray = listOf("w_temp2", "cal_today", "cal_dday"),
            floating = emptyList(),
            left = null,
            right = null,
        ),
    ),
    DummyLlmCase(
        title = "케이스 13: 좌측만",
        description = "우측 없이 좌측 바로가기만",
        userQuery = "왼손잡이용 단일 바로가기",
        selectedAppIds = listOf("camera", "sys_qr"),
        recommendation = LlmRecommendation(
            tray = listOf("cam1"),
            floating = listOf("cam2"),
            left = "sys_qr",
            right = null,
        ),
    ),
    DummyLlmCase(
        title = "케이스 14: 미디어 감상",
        description = "음악 + 갤러리 + 뉴스 (자유배치 위주)",
        userQuery = "느긋하게 쉴 때",
        selectedAppIds = listOf("ytmusic", "gallery", "samsung_news", "weather", "sys_dnd"),
        recommendation = LlmRecommendation(
            tray = listOf("ym3", "w_temp"),
            floating = listOf("ym1", "ym2", "gal2", "news2"),
            left = "sys_dnd",
            right = null,
        ),
    ),
    DummyLlmCase(
        title = "케이스 15: 건강 관리 풀세팅",
        description = "헬스 + 웰빙 + 알람 + 리마인더 + 절전 (모든 영역 가득)",
        userQuery = "건강 챙기는 일상",
        selectedAppIds = listOf(
            "health", "wellbeing", "clock", "reminder", "weather", "battery",
            "sys_power_saving", "sys_dnd",
        ),
        recommendation = LlmRecommendation(
            tray = listOf("h1", "c_alarm", "r1", "bat1"),
            floating = listOf("h2", "dw1", "dw2", "r2"),
            left = "sys_power_saving",
            right = "sys_dnd",
        ),
    ),
    DummyLlmCase(
        title = "케이스 16: 빈 추천 (앱만 선택)",
        description = "트레이/자유배치/바로가기 모두 비어있음",
        userQuery = "아무 위젯도 배치하지 마",
        selectedAppIds = listOf("weather"),
        recommendation = LlmRecommendation(
            tray = emptyList(),
            floating = emptyList(),
            left = null,
            right = null,
        ),
    ),
)

/**
 * 더미 케이스를 실제 카탈로그에 매핑해 LlmSuggestionResult 와 동일한 시뮬레이션 결과를 만든다.
 */
fun DummyLlmCase.toSelectedFirstStep(catalog: LlmCatalog): SelectedFirstStep =
    catalog.resolveSelectedApps(selectedAppIds)
