package com.example.lockscreencopy.model

import android.appwidget.AppWidgetProviderInfo
import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class WidgetSize { SMALL, WIDE }

enum class AddTarget { SLOT, FLOATING }

enum class FavoriteAppsLayout { BOTTOM_LEFT, LEFT_VERTICAL, BOTTOM_RIGHT }

data class LockWidget(
    val id: String,
    val appId: String,
    val name: String,
    val size: WidgetSize,
    val icon: ImageVector? = null,
    val iconTint: Color = Color.White,
    val mainValue: String = "",
    val subValue: String = "",
)

data class PlacedWidget(
    val uid: String,
    val widget: LockWidget,
)

data class FloatingWidget(
    val uid: String,
    val widget: LockWidget,
    val offset: Offset,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
)

data class WidgetApp(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val iconBg: Color,
    val widgets: List<LockWidget>,
)

data class AppItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val tint: Color,
)

enum class SystemAction {
    SOUND, FLASHLIGHT, AIRPLANE, MOBILE_DATA, POWER_SAVING,
    DARK_MODE, DO_NOT_DISTURB, QR_SCAN, LOCATION,
}

sealed class BottomShortcut {
    abstract val id: String
    abstract val label: String

    data class System(
        override val id: String,
        override val label: String,
        val action: SystemAction,
        val icon: ImageVector,
        val tint: Color = Color.White,
    ) : BottomShortcut()

    data class App(
        override val id: String,
        override val label: String,
        val packageName: String,
        val drawable: Drawable? = null,
    ) : BottomShortcut()
}

data class HostedAppWidget(
    val uid: String,
    val appWidgetId: Int,
    val providerInfo: AppWidgetProviderInfo,
    val widthPx: Int,
    val heightPx: Int,
    val offset: Offset,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
)

/** 정보 값의 출처. REAL=실제 시스템 데이터, SAMPLE=샘플/더미 값. */
enum class InfoSource { REAL, SAMPLE }

/** 사용자 입력 정보 항목을 실제 데이터 소스로 해석한 결과. */
data class ResolvedInfoItem(
    val label: String,
    val value: String,
    val source: InfoSource,
)

/**
 * 위젯 이미지 위에 오버레이될 텍스트 슬롯 하나.
 * 위치/크기는 모두 위젯 너비·높이 대비 0.0~1.0 상대 비율.
 */
data class AiTextSlot(
    val label: String,          // 정보 범주 레이블 (예: "온도")
    val value: String,          // 표시할 값 (예: "23°C")
    val role: String,           // "title" | "main" | "sub" | "extra"
    val xRatio: Float,          // 좌측 시작 x 비율
    val yRatio: Float,          // 상단 시작 y 비율
    val widthRatio: Float,
    val heightRatio: Float,
    val fontScale: Float = 1f,
    val anchorObject: String = "",              // 이 슬롯이 붙어있는 이미지 오브젝트 (디버깅/개선용)
    val source: InfoSource = InfoSource.SAMPLE, // 값 출처
)

/** AI 스케치로 생성된 이미지 위젯. Imagen 이미지 + 슬롯 기반 텍스트 오버레이. */
data class AiSketchWidget(
    val uid: String,
    val imageBitmap: android.graphics.Bitmap?,
    val textSlots: List<AiTextSlot>,
    val offset: Offset,
    val widthDp: Float,
    val heightDp: Float,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
)

data class NotificationItem(
    val id: String,
    val appName: String,
    val title: String,
    val body: String,
    val timeLabel: String,
    val hasNudge: Boolean = false,
    val nudgeLabel: String = "",
    val nudgeActions: List<String> = emptyList(),
    /** 지도 검색에 사용할 정제된 장소명. AI가 발신자명/잡설을 제외하고 추출한다. */
    val mapQuery: String = "",
    /** AI가 예측한 일정 시작 시각(epoch millis). 0이면 시간 미상. */
    val eventStartMillis: Long = 0L,
)

// ───────────────────────────────────────────────────────────────
//  갤럭시식 알림: 앱 → 채팅방 → 메시지 계층 구조
// ───────────────────────────────────────────────────────────────

/** 메시지 한 줄에 대해 AI가 예측한 행동(넛지). */
data class MessageNudge(
    val label: String,                  // "일정 추가" | "지도 열기"
    val actions: List<String>,
    val mapQuery: String = "",          // 지도 검색용 정제 장소명
    val eventStartMillis: Long = 0L,    // 일정 시작 시각. 0이면 미상
)

/** 채팅방 안에서 쌓인 메시지 한 줄. [nudge]가 null이 아니면 행동 예측된 메시지. */
data class ChatMessage(
    val id: String,
    val sender: String,                 // 방 안 발신자 이름 (없으면 빈 문자열)
    val text: String,
    val timeLabel: String,
    val nudge: MessageNudge? = null,
)

/** 같은 대화방에서 쌓인 안읽은 메시지 묶음. */
data class ChatRoom(
    val id: String,
    val roomName: String,               // 방 이름(단톡방 제목) 또는 상대 이름
    val appName: String,
    val messages: List<ChatMessage>,    // 오래된 → 최신 순
) {
    val latest: ChatMessage? get() = messages.lastOrNull()
    val messageCount: Int get() = messages.size
    val nudgeCount: Int get() = messages.count { it.nudge != null }
}

/** 같은 앱에서 온 채팅방들의 묶음. 잠금화면에서 겹침 스택 1개 단위. */
data class AppNotificationGroup(
    val appName: String,
    val rooms: List<ChatRoom>,          // 최신 방이 앞(index 0)
) {
    val topRoom: ChatRoom? get() = rooms.firstOrNull()
    val roomCount: Int get() = rooms.size
    val nudgeCount: Int get() = rooms.sumOf { it.nudgeCount }
}
