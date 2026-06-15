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
    /** 소속된 위젯 공간 id. null이면 잠금화면에 자유 배치된 상태. */
    val spaceId: String? = null,
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
    /** 소속된 위젯 공간 id. null이면 잠금화면에 자유 배치된 상태. */
    val spaceId: String? = null,
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
    /** 소속된 위젯 공간 id. null이면 잠금화면에 자유 배치된 상태. */
    val spaceId: String? = null,
)

/**
 * 위젯 공간 내부에서 멤버 위젯 하나의 배치 정보.
 * [offset]은 공간 캔버스(dp) 기준 좌상단 좌표이며, 스케일은 위젯 기본 크기에 곱해진다.
 * 잠금화면 자유 배치 좌표(위젯 자체의 offset/scale)와는 독립적이라, 공간에서 빼면
 * 원래 잠금화면 위치로 그대로 복귀한다.
 */
data class SpaceItemLayout(
    val offset: Offset,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
)

/**
 * 여러 위젯을 하나로 묶어 보관하는 "위젯 공간".
 *
 * 홈스크린의 앱 폴더처럼, 잠금화면에서는 작은 투명(유리/비눗방울) 버블로 표시되고
 * 탭하면 확장되어 내부 위젯들을 자유롭게 배치/크기조절하며 관찰할 수 있다.
 * 버블(접힘)에서도 [layouts]에 저장된 배치 그대로 축소되어 보인다.
 * 홈스크린 폴더와 달리 위젯이 1개만 남아도 공간은 유지되며, 삭제는 전용 버튼으로만 한다.
 */
data class WidgetSpace(
    val id: String,
    val name: String,
    /** 접힌 버블의 위치(스케일 Box 로컬 좌표). */
    val offset: Offset,
    /** 멤버 uid → 공간 캔버스 내 배치. */
    val layouts: Map<String, SpaceItemLayout> = emptyMap(),
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
    /**
     * 이 알림(채팅방)에 쌓인 안읽은 메시지들(오래된 → 최신).
     * 채팅 앱은 MessagingStyle로 최근 여러 줄을 함께 보내므로, 더미 데이터처럼
     * 방 하나에 여러 메시지를 쭈루룩 보여줄 수 있다. 넛지는 메시지별로 채워진다.
     * 비어 있으면 [body]/[title] 한 줄로 대체된다.
     */
    val messages: List<ChatMessage> = emptyList(),
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
