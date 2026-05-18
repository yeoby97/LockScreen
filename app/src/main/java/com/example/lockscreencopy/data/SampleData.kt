package com.example.lockscreencopy.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import com.example.lockscreencopy.model.AppItem
import com.example.lockscreencopy.model.LockWidget
import com.example.lockscreencopy.model.WidgetApp
import com.example.lockscreencopy.model.WidgetSize

val lockWidgetApps = listOf(
    WidgetApp("weather", "날씨", Icons.Filled.WbSunny, Color(0xFF4DAAED), listOf(
        LockWidget("w_rain",  "weather", "강수 확률",       WidgetSize.SMALL, Icons.Filled.WaterDrop, Color.White,           "88%"),
        LockWidget("w_temp",  "weather", "현재 온도와 날씨", WidgetSize.SMALL, Icons.Filled.CloudQueue, Color.White,          "23°"),
        LockWidget("w_uv",    "weather", "자외선",          WidgetSize.SMALL, Icons.Filled.WbSunny,   Color(0xFFFFB300),    "7"),
        LockWidget("w_sun",   "weather", "일출과 일몰",      WidgetSize.SMALL, null,                  Color.White,           "오전\n7:45"),
        LockWidget("w_temp2", "weather", "현재 온도와 날씨", WidgetSize.WIDE,  Icons.Filled.CloudQueue, Color.White,         "23°", "도시 이름"),
        LockWidget("w_dust",  "weather", "미세/초미세먼지",  WidgetSize.WIDE,  Icons.Filled.Air,       Color.White,           "15 / 38"),
    )),
    WidgetApp("clock", "시계", Icons.Filled.AccessTime, Color(0xFF636366), listOf(
        LockWidget("c_alarm", "clock", "곧 울릴 알람", WidgetSize.SMALL, Icons.Filled.Alarm,      Color.White, "오전\n6:00"),
        LockWidget("c_world", "clock", "세계시각",     WidgetSize.SMALL, Icons.Filled.AccessTime, Color.White, "오전\n6:30", "런던"),
    )),
    WidgetApp("calendar", "캘린더", Icons.Filled.CalendarMonth, Color(0xFF30B080), listOf(
        LockWidget("cal_today",  "calendar", "오늘",     WidgetSize.SMALL, null,                       Color.White, "목\n14"),
        LockWidget("cal_dday",   "calendar", "디데이",   WidgetSize.SMALL, null,                       Color.White, "19",   "일 남음"),
        LockWidget("cal_next",   "calendar", "다음 일정", WidgetSize.SMALL, Icons.Filled.CalendarMonth, Color.White),
        LockWidget("cal_next2",  "calendar", "다음 일정", WidgetSize.WIDE,  Icons.Filled.CalendarMonth, Color.White, "내일", "스승의날"),
        LockWidget("cal_dday2",  "calendar", "디데이",   WidgetSize.WIDE,  null,                       Color.White, "내 생일", "19일 남음"),
        LockWidget("cal_today2", "calendar", "오늘",     WidgetSize.WIDE,  null,                       Color.White, "14",   "5월 목요일"),
    )),
    WidgetApp("battery", "배터리", Icons.Filled.BatteryFull, Color(0xFF30B0C7), listOf(
        LockWidget("bat1", "battery", "배터리 상태(원형)", WidgetSize.SMALL, Icons.Filled.BatteryFull, Color.White, "53"),
        LockWidget("bat2", "battery", "배터리 상태(원형)", WidgetSize.WIDE,  Icons.Filled.BatteryFull, Color.White, "92", "53"),
    )),
    WidgetApp("health", "삼성 헬스", Icons.Filled.Favorite, Color(0xFF32D74B), listOf(
        LockWidget("h1", "health", "일일 활동", WidgetSize.SMALL, Icons.Filled.Favorite, Color(0xFF32D74B)),
        LockWidget("h2", "health", "일일 활동", WidgetSize.WIDE,  Icons.Filled.Favorite, Color(0xFF32D74B), "4,350", "76 / 458"),
    )),
    WidgetApp("wellbeing", "디지털 웰빙", Icons.Filled.MonitorHeart, Color(0xFF32D74B), listOf(
        LockWidget("dw1", "wellbeing", "사용 시간",  WidgetSize.WIDE, Icons.Filled.MonitorHeart,    Color(0xFF32D74B), "3시간 26분", "사용 시간"),
        LockWidget("dw2", "wellbeing", "앱 타이머", WidgetSize.WIDE, Icons.Filled.HourglassEmpty, Color(0xFF32D74B), "1시간 45분 남음", "인터넷"),
    )),
    WidgetApp("reminder", "리마인더", Icons.Filled.CheckCircle, Color(0xFF7C3AED), listOf(
        LockWidget("r1", "reminder", "카테고리",     WidgetSize.SMALL, Icons.Filled.CheckCircle, Color(0xFF7C3AED), "3"),
        LockWidget("r2", "reminder", "전체 리마인더", WidgetSize.WIDE,  Icons.Filled.CheckCircle, Color(0xFF7C3AED), "숙제 / 운동", "티켓 구매"),
    )),
    WidgetApp("navermap", "네이버지도", Icons.Filled.Map, Color(0xFF03C75A), listOf(
        LockWidget("nm1", "navermap", "[버스/지하철] 도착정보 목록", WidgetSize.WIDE,  Icons.Filled.DirectionsBus, Color(0xFF03C75A), "정자역 신사", "3분 (12:33)"),
        LockWidget("nm2", "navermap", "[지하철] 도착정보",          WidgetSize.WIDE,  Icons.Filled.Train,         Color(0xFF03C75A), "정자역", "신사행 3분"),
        LockWidget("nm3", "navermap", "[대중교통] 집/회사",         WidgetSize.WIDE,  Icons.Filled.Home,          Color(0xFF7C3AED), "1시간 25분", "네이버.아데나루체"),
        LockWidget("nm4", "navermap", "[버스] 도착정보 B",          WidgetSize.WIDE,  Icons.Filled.DirectionsBus, Color(0xFF32D74B), "네이버.아데나루체", "곧 도착 · 13분"),
        LockWidget("nm5", "navermap", "[네이버지도] 바로가기",       WidgetSize.WIDE,  Icons.Filled.Search,        Color(0xFF03C75A), "네이버지도", "장소, 버스, 지하철 검색"),
        LockWidget("nm6", "navermap", "[버스] 도착정보 A",          WidgetSize.SMALL, Icons.Filled.DirectionsBus, Color(0xFF32D74B), "곧 도착", "13분"),
        LockWidget("nm7", "navermap", "[자동차] 집으로",             WidgetSize.SMALL, Icons.Filled.Home,          Color(0xFF7C3AED), "1시간\n25분", "집으로"),
        LockWidget("nm8", "navermap", "[자동차] 회사로",             WidgetSize.SMALL, Icons.Filled.Business,      Color(0xFF7C3AED), "34분",       "회사로"),
        LockWidget("nm9", "navermap", "[자동차] 최근 목적지",        WidgetSize.SMALL, Icons.Filled.LocationOn,    Color(0xFF8E8E93), "광화문역"),
    )),
    WidgetApp("ytmusic", "YouTube Music", Icons.Filled.PlayCircle, Color(0xFFFF0000), listOf(
        LockWidget("ym1", "ytmusic", "최근 재생",    WidgetSize.WIDE,  Icons.Filled.MusicNote, Color.White,    "365", "Charli xcx"),
        LockWidget("ym2", "ytmusic", "지금 재생 중", WidgetSize.WIDE,  Icons.Filled.PlayArrow, Color.White,    "Highway", "Shaboozey"),
        LockWidget("ym3", "ytmusic", "턴테이블",     WidgetSize.SMALL, Icons.Filled.PlayArrow, Color.White,    "재생"),
    )),
    WidgetApp("routines", "모드 및 루틴", Icons.Filled.Schedule, Color(0xFF5B5EA6), listOf(
        LockWidget("rt1", "routines", "루틴", WidgetSize.SMALL, Icons.Filled.Schedule, Color(0xFF5B5EA6)),
        LockWidget("rt2", "routines", "루틴", WidgetSize.WIDE,  Icons.Filled.Schedule, Color(0xFF5B5EA6), "회의"),
    )),
    WidgetApp("interpreter", "통역", Icons.Filled.Language, Color(0xFF4A90E2), listOf(
        LockWidget("int1", "interpreter", "대화 통역", WidgetSize.SMALL, Icons.Filled.Language, Color.White, "통역"),
        LockWidget("int2", "interpreter", "실시간 통역", WidgetSize.WIDE, Icons.Filled.Language, Color.White, "실시간 통역", "대화 모드"),
    )),
    WidgetApp("camera", "카메라", Icons.Filled.CameraAlt, Color(0xFF424242), listOf(
        LockWidget("cam1", "camera", "카메라 실행", WidgetSize.SMALL, Icons.Filled.CameraAlt, Color.White, "카메라"),
        LockWidget("cam2", "camera", "빠른 촬영", WidgetSize.WIDE, Icons.Filled.CameraAlt, Color.White, "카메라", "빠른 촬영"),
    )),
    WidgetApp("voice_recorder", "음성 녹음", Icons.Filled.Mic, Color(0xFFE53935), listOf(
        LockWidget("rec1", "voice_recorder", "녹음 시작", WidgetSize.SMALL, Icons.Filled.Mic, Color.White, "녹음"),
        LockWidget("rec2", "voice_recorder", "음성 녹음", WidgetSize.WIDE, Icons.Filled.Mic, Color.White, "음성 녹음", "탭하여 녹음 시작"),
    )),
    WidgetApp("samsung_news", "삼성 뉴스", Icons.Filled.Article, Color(0xFF1E88E5), listOf(
        LockWidget("news1", "samsung_news", "주요 뉴스", WidgetSize.SMALL, Icons.Filled.Article, Color.White, "뉴스"),
        LockWidget("news2", "samsung_news", "오늘의 뉴스", WidgetSize.WIDE, Icons.Filled.Article, Color.White, "오늘의 뉴스", "주요 소식 보기"),
    )),
    WidgetApp("device_care", "디바이스 케어", Icons.Filled.Settings, Color(0xFF00A86B), listOf(
        LockWidget("care1", "device_care", "기기 상태", WidgetSize.SMALL, Icons.Filled.Settings, Color.White, "좋음"),
        LockWidget("care2", "device_care", "디바이스 케어", WidgetSize.WIDE, Icons.Filled.Settings, Color.White, "기기 상태 좋음", "배터리 · 저장공간"),
    )),
    WidgetApp("gallery", "갤러리", Icons.Filled.PhotoLibrary, Color(0xFFE91E63), listOf(
        LockWidget("gal1", "gallery", "최근 사진", WidgetSize.SMALL, Icons.Filled.PhotoLibrary, Color.White, "사진"),
        LockWidget("gal2", "gallery", "갤러리", WidgetSize.WIDE, Icons.Filled.PhotoLibrary, Color.White, "최근 사진", "갤러리 바로가기"),
    )),
)

val defaultAppList = listOf(
    AppItem("phone",    "전화",    Icons.Filled.Phone,                Color(0xFF4CAF50)),
    AppItem("message",  "메시지",   Icons.AutoMirrored.Filled.Message, Color(0xFF2196F3)),
    AppItem("camera",   "카메라",   Icons.Filled.CameraAlt,            Color(0xFF424242)),
    AppItem("gallery",  "갤러리",   Icons.Filled.PhotoLibrary,         Color(0xFFE91E63)),
    AppItem("internet", "인터넷",   Icons.Filled.Language,             Color(0xFF03A9F4)),
    AppItem("music",    "음악",    Icons.Filled.MusicNote,            Color(0xFF9C27B0)),
    AppItem("youtube",  "유튜브",   Icons.Filled.PlayCircle,           Color(0xFFF44336)),
    AppItem("kakao",    "카카오톡", Icons.AutoMirrored.Filled.Chat,    Color(0xFFFBC02D)),
    AppItem("calendar", "캘린더",   Icons.Filled.CalendarMonth,        Color(0xFF3F51B5)),
    AppItem("email",    "이메일",   Icons.Filled.Email,                Color(0xFFFF5722)),
    AppItem("maps",     "지도",    Icons.Filled.Map,                  Color(0xFF4CAF50)),
    AppItem("contacts", "연락처",   Icons.Filled.Contacts,             Color(0xFF009688)),
    AppItem("settings", "설정",    Icons.Filled.Settings,             Color(0xFF424242)),
)
