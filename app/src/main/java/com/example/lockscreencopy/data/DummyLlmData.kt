package com.example.lockscreencopy.data

/**
 * 더미 케이스의 구조:
 * - 트레이는 권한 없는 영역이므로 mock LockWidget id 를 그대로 사용
 * - 자유 배치 영역은 더미 위젯을 절대 사용하지 않음 (recommendation.floating 은 항상 빈 리스트).
 *   대신 realWidgetCount 로 런타임에 실제 설치된 AppWidgetProviderInfo 를 N 개 샘플링해
 *   recommendation.floating(component 문자열 목록)에 주입
 * - 좌/우 바로가기는 mock 시스템 바로가기 + 설치 앱에서 LLM 가 그대로 선택
 */
data class DummyLlmCase(
    val title: String,
    val description: String,
    val userQuery: String,
    val selectedAppIds: List<String>,
    val recommendation: LlmRecommendation,
    val realWidgetCount: Int = 0,
)

val dummyLlmCases: List<DummyLlmCase> = listOf(
    DummyLlmCase(
        title = "케이스 1: 운동할 때",
        description = "트레이=알람/음악 + 자유=실제 위젯 3 + 좌우 바로가기",
        userQuery = "운동할 때 필요한 거 배치해줘",
        selectedAppIds = listOf("clock", "ytmusic", "sys_flashlight", "sys_sound"),
        recommendation = LlmRecommendation(
            tray = listOf("c_alarm", "ym3"),
            floating = emptyList(),
            left = "sys_sound",
            right = "sys_flashlight",
        ),
        realWidgetCount = 3,
    ),
    DummyLlmCase(
        title = "케이스 2: 출근길",
        description = "트레이=캘린더/시계 + 자유=실제 위젯 4 + 위치/QR",
        userQuery = "출근길에 필요한 거 보여줘",
        selectedAppIds = listOf("calendar", "clock", "sys_location", "sys_qr"),
        recommendation = LlmRecommendation(
            tray = listOf("cal_today", "cal_next", "c_world"),
            floating = emptyList(),
            left = "sys_location",
            right = "sys_qr",
        ),
        realWidgetCount = 4,
    ),
    DummyLlmCase(
        title = "케이스 3: 잠자기 전",
        description = "트레이=알람/배터리/리마인더 + 자유=실제 위젯 2 + DND/다크모드",
        userQuery = "잘 때 필요한 거",
        selectedAppIds = listOf("clock", "battery", "reminder", "sys_dnd", "sys_dark_mode"),
        recommendation = LlmRecommendation(
            tray = listOf("c_alarm", "bat1", "r1"),
            floating = emptyList(),
            left = "sys_dnd",
            right = "sys_dark_mode",
        ),
        realWidgetCount = 2,
    ),
    DummyLlmCase(
        title = "케이스 4: 트레이만 (SMALL 4개)",
        description = "작은 mock 위젯 4개로 트레이 가득. 자유/바로가기 없음",
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
        description = "와이드 mock 위젯 2개로 트레이 가득",
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
        title = "케이스 6: 자유 배치만 (실제 위젯 6)",
        description = "트레이/바로가기 없이 실제 설치 앱 위젯 6개만",
        userQuery = "큰 위젯만 여러 개",
        selectedAppIds = emptyList(),
        recommendation = LlmRecommendation(
            tray = emptyList(),
            floating = emptyList(),
            left = null,
            right = null,
        ),
        realWidgetCount = 6,
    ),
    DummyLlmCase(
        title = "케이스 7: 바로가기만",
        description = "좌/우 바로가기만",
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
        description = "트레이 가득 + 실제 위젯 5 + 좌우 바로가기",
        userQuery = "전체적으로 다 채워줘",
        selectedAppIds = listOf(
            "weather", "calendar", "battery", "health",
            "sys_flashlight", "sys_sound",
        ),
        recommendation = LlmRecommendation(
            tray = listOf("w_temp", "cal_today", "bat1", "h1"),
            floating = emptyList(),
            left = "sys_sound",
            right = "sys_flashlight",
        ),
        realWidgetCount = 5,
    ),
    DummyLlmCase(
        title = "케이스 9: 중복 트레이 위젯",
        description = "같은 mock 위젯 ID가 트레이에 여러 번 (인스턴스 분리 테스트)",
        userQuery = "같은 위젯 여러 개 쓰기",
        selectedAppIds = listOf("clock", "weather"),
        recommendation = LlmRecommendation(
            tray = listOf("c_world", "c_world", "w_temp", "w_uv"),
            floating = emptyList(),
            left = null,
            right = null,
        ),
        realWidgetCount = 2,
    ),
    DummyLlmCase(
        title = "케이스 10: 회의/업무",
        description = "트레이=캘린더/리마인더/녹음 + 실제 위젯 4 + DND/절전",
        userQuery = "회의할 때 필요한 거",
        selectedAppIds = listOf(
            "calendar", "reminder", "voice_recorder", "clock",
            "sys_dnd", "sys_power_saving",
        ),
        recommendation = LlmRecommendation(
            tray = listOf("cal_dday", "r1", "rec1", "c_world"),
            floating = emptyList(),
            left = "sys_dnd",
            right = "sys_power_saving",
        ),
        realWidgetCount = 4,
    ),
    DummyLlmCase(
        title = "케이스 11: 여행",
        description = "트레이=통역/카메라/갤러리/배터리 + 실제 위젯 6 + 손전등/위치",
        userQuery = "여행 갈 때 쓸 거",
        selectedAppIds = listOf(
            "interpreter", "camera", "gallery", "battery",
            "sys_flashlight", "sys_location",
        ),
        recommendation = LlmRecommendation(
            tray = listOf("int1", "cam1", "gal1", "bat1"),
            floating = emptyList(),
            left = "sys_location",
            right = "sys_flashlight",
        ),
        realWidgetCount = 6,
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
        description = "우측 없이 좌측 바로가기 + 실제 위젯 1",
        userQuery = "왼손잡이용 단일 바로가기",
        selectedAppIds = listOf("camera", "sys_qr"),
        recommendation = LlmRecommendation(
            tray = listOf("cam1"),
            floating = emptyList(),
            left = "sys_qr",
            right = null,
        ),
        realWidgetCount = 1,
    ),
    DummyLlmCase(
        title = "케이스 14: 미디어 감상",
        description = "트레이=음악/날씨 + 실제 위젯 4 + DND",
        userQuery = "느긋하게 쉴 때",
        selectedAppIds = listOf("ytmusic", "weather", "sys_dnd"),
        recommendation = LlmRecommendation(
            tray = listOf("ym3", "w_temp"),
            floating = emptyList(),
            left = "sys_dnd",
            right = null,
        ),
        realWidgetCount = 4,
    ),
    DummyLlmCase(
        title = "케이스 15: 건강 관리 풀세팅",
        description = "트레이=헬스/알람/리마인더/배터리 + 실제 위젯 4 + 절전/DND",
        userQuery = "건강 챙기는 일상",
        selectedAppIds = listOf(
            "health", "clock", "reminder", "weather", "battery",
            "sys_power_saving", "sys_dnd",
        ),
        recommendation = LlmRecommendation(
            tray = listOf("h1", "c_alarm", "r1", "bat1"),
            floating = emptyList(),
            left = "sys_power_saving",
            right = "sys_dnd",
        ),
        realWidgetCount = 4,
    ),
    DummyLlmCase(
        title = "케이스 16: 빈 추천",
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
    DummyLlmCase(
        title = "케이스 17: 실제 앱 위젯 풀세팅 (8개)",
        description = "트레이 작게 + 실제 설치 앱 위젯 8개로 자유 영역 가득",
        selectedAppIds = listOf("clock"),
        userQuery = "내가 깐 앱들 위젯 많이",
        recommendation = LlmRecommendation(
            tray = listOf("c_alarm", "c_world"),
            floating = emptyList(),
            left = null,
            right = null,
        ),
        realWidgetCount = 8,
    ),
)

fun DummyLlmCase.toSelectedFirstStep(catalog: LlmCatalog): SelectedFirstStep =
    catalog.resolveSelectedApps(selectedAppIds)
