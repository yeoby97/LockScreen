package com.example.lockscreencopy

import android.app.Activity
import android.app.KeyguardManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.sqrt
import androidx.compose.ui.graphics.StrokeCap
import com.example.lockscreencopy.ui.theme.LockScreenCopyTheme


class LockScreenActivity : ComponentActivity() {
    private lateinit var keyguardManager: KeyguardManager
    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetHost: AppWidgetHost

    private val hostedWidgets: SnapshotStateList<HostedAppWidget> = mutableStateListOf()

    private val bindWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val id = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        if (result.resultCode == Activity.RESULT_OK && id != -1) {
            configureOrAdd(id)
        } else if (id != -1) {
            appWidgetHost.deleteAppWidgetId(id)
        }
    }

    private val configureWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val id = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        if (result.resultCode == Activity.RESULT_OK && id != -1) {
            addHostedWidget(id)
        } else if (id != -1) {
            appWidgetHost.deleteAppWidgetId(id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, HOST_ID)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
        setContent {
            LockScreenCopyTheme() {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LockScreen(
                        onUnlock = { unlockDevice() },
                        hostedWidgets = hostedWidgets,
                        appWidgetHost = appWidgetHost,
                        appWidgetManager = appWidgetManager,
                        onRealWidgetSelected = { info -> onProviderSelected(info) },
                        onRemoveHosted = { uid -> removeHosted(uid) },
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        try { appWidgetHost.startListening() } catch (_: Exception) {}
    }

    override fun onStop() {
        super.onStop()
        try { appWidgetHost.stopListening() } catch (_: Exception) {}
    }

    private fun unlockDevice() {
        Toast.makeText(this, "Device unlocked!", Toast.LENGTH_SHORT).show()
    }

    private fun onProviderSelected(info: AppWidgetProviderInfo) {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        val d = resources.displayMetrics.density
        val minWdp = (info.minWidth / d).toInt().coerceAtLeast(40)
        val minHdp = (info.minHeight / d).toInt().coerceAtLeast(40)
        val options = Bundle().apply {
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, minWdp)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, minHdp)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minWdp)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minHdp)
        }
        val bound = try {
            appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider, options)
        } catch (_: Exception) { false }

        if (bound) {
            configureOrAdd(appWidgetId)
            return
        }

        val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options)
        }
        try {
            bindWidgetLauncher.launch(bindIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "위젯 바인딩을 처리할 수 없습니다", Toast.LENGTH_SHORT).show()
            appWidgetHost.deleteAppWidgetId(appWidgetId)
        }
    }

    private fun configureOrAdd(appWidgetId: Int) {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: run {
            appWidgetHost.deleteAppWidgetId(appWidgetId); return
        }
        val configure = info.configure
        if (configure != null) {
            val configureIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            try {
                configureWidgetLauncher.launch(configureIntent)
            } catch (_: Exception) {
                addHostedWidget(appWidgetId)
            }
        } else {
            addHostedWidget(appWidgetId)
        }
    }

    private fun addHostedWidget(appWidgetId: Int) {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return
        val d = resources.displayMetrics.density
        val minWdp = (info.minWidth / d).toInt().coerceAtLeast(40)
        val minHdp = (info.minHeight / d).toInt().coerceAtLeast(40)
        val options = Bundle().apply {
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, minWdp)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, minHdp)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minWdp)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minHdp)
        }
        try { appWidgetManager.updateAppWidgetOptions(appWidgetId, options) } catch (_: Exception) {}
        hostedWidgets.add(
            HostedAppWidget(
                uid = "hosted_${appWidgetId}_${System.currentTimeMillis()}",
                appWidgetId = appWidgetId,
                providerInfo = info,
                offset = Offset(200f, 600f),
                scale = 1f,
            )
        )
    }

    private fun removeHosted(uid: String) {
        val item = hostedWidgets.firstOrNull { it.uid == uid } ?: return
        appWidgetHost.deleteAppWidgetId(item.appWidgetId)
        hostedWidgets.remove(item)
    }

    companion object { const val HOST_ID = 1024 }
}

data class HostedAppWidget(
    val uid: String,
    val appWidgetId: Int,
    val providerInfo: AppWidgetProviderInfo,
    val offset: Offset,
    val scale: Float = 1f,
)

// ============================================================
// 데이터 모델
// ============================================================

enum class WidgetSize { SMALL, WIDE }  // 1x1, 2x1

data class LockWidget(
    val id: String,
    val appId: String,
    val name: String,
    val size: WidgetSize,
    val icon: ImageVector? = null,
    val iconTint: Color = Color.White,
    val mainValue: String = "",
    val subValue: String = ""
)

data class PlacedWidget(
    val uid: String,
    val widget: LockWidget,
)

data class FloatingWidget(
    val uid: String,
    val widget: LockWidget,
    val offset: Offset,
    val scale: Float = 1f
)

enum class AddTarget{
    SLOT,
    FLOATING
}

data class WidgetApp(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val iconBg: Color,
    val widgets: List<LockWidget>
)

data class AppItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val tint: Color
)

// ============================================================
// 위젯 앱 데이터 (스크린샷 기반)
// ============================================================

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

// ============================================================
// 메인 화면
// ============================================================

@Composable
fun LockScreen(
    onUnlock: () -> Unit,
    hostedWidgets: SnapshotStateList<HostedAppWidget> = mutableStateListOf(),
    appWidgetHost: AppWidgetHost? = null,
    appWidgetManager: AppWidgetManager? = null,
    onRealWidgetSelected: (AppWidgetProviderInfo) -> Unit = {},
    onRemoveHosted: (String) -> Unit = {},
) {
    var isFloating by remember { mutableStateOf(false) }
    var showShortcutPopup by remember { mutableStateOf(false) }
    var showAppWidgetSheet by remember { mutableStateOf(false) }
    var showLockWidgetPicker by remember { mutableStateOf(false) }
    var showRealWidgetPicker by remember { mutableStateOf(false) }
    var addedApps by remember { mutableStateOf(listOf<AppItem>()) }

    var slotWidgets by remember { mutableStateOf<List<PlacedWidget>>(emptyList()) }
    var floatingWidgets by remember { mutableStateOf<List<FloatingWidget>>(emptyList()) }
    var selectedFloatingUid by remember { mutableStateOf<String?>(null) }
    var addTarget by remember { mutableStateOf(AddTarget.SLOT) }


    var addCounter by remember { mutableStateOf(0) }

    var clockOffset by remember { mutableStateOf(Offset.Zero) }
    var greenBoxOffset by remember { mutableStateOf(Offset.Zero) }
    var savedClockOffset by remember { mutableStateOf(Offset.Zero) }

    BackHandler(enabled = isFloating) {
        clockOffset = savedClockOffset
        greenBoxOffset = Offset.Zero
        selectedFloatingUid = null
        isFloating = false
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    fun resizeFloating(uid: String, delta: Float) {
        floatingWidgets = floatingWidgets.map { fw ->
            if (fw.uid != uid) return@map fw
            val newScale = (fw.scale + delta).coerceIn(0.7f, 2.5f)
            val realDelta = newScale - fw.scale
            val baseW = if (fw.widget.size == WidgetSize.WIDE) 180.dp else 100.dp
            val baseH = 100.dp
            val dwPx = with(density) { baseW.toPx() } * realDelta
            val dhPx = with(density) { baseH.toPx() } * realDelta
            fw.copy(
                scale = newScale,
                offset = Offset(fw.offset.x - dwPx / 2f, fw.offset.y - dhPx / 2f)
            )
        }
    }

    fun resizeHosted(uid: String, delta: Float) {
        val idx = hostedWidgets.indexOfFirst { it.uid == uid }
        if (idx == -1) return
        val hw = hostedWidgets[idx]
        val newScale = (hw.scale + delta).coerceIn(0.5f, 3.0f)
        val realDelta = newScale - hw.scale
        val dwPx = hw.providerInfo.minWidth * realDelta
        val dhPx = hw.providerInfo.minHeight * realDelta
        hostedWidgets[idx] = hw.copy(
            scale = newScale,
            offset = Offset(hw.offset.x - dwPx / 2f, hw.offset.y - dhPx / 2f),
        )
    }

    val scale by animateFloatAsState(
        targetValue = if (isFloating) 0.7f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing), label = "scale"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isFloating) 30.dp else 0.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing), label = "corner"
    )
    val blurRadius by animateDpAsState(
        targetValue = if (isFloating) 20.dp else 0.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing), label = "blur"
    )

    // 슬롯 크기 계산 (4칸 기준, 간격 8dp * 3)
    val slotGap = 8.dp
    val slotSize = (screenWidth * 0.1f)

    Box(modifier = Modifier.fillMaxSize()) {

        // 바깥 배경 (floating 시 blur)
        Image(
            painter = painterResource(id = R.drawable.images),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
                .then(if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier)
        )

        // 안쪽 (정상 배경 + 컨텐츠)
        Box(
            modifier = Modifier.fillMaxSize()
                .graphicsLayer {
                    scaleX = scale; scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0.2f)
                }
                .clip(RoundedCornerShape(cornerRadius))
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        val t0 = System.currentTimeMillis()
                        val released = try {
                            withTimeout(500) { tryAwaitRelease(); true }
                        } catch (e: TimeoutCancellationException) { isFloating = true; false }
                        if (!isFloating && released && System.currentTimeMillis() - t0 >= 500)
                            isFloating = true
                    })
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.images),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 상단
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isFloating) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(onClick = {}) { Text("배경화면") }
                            Button(onClick = {
                                savedClockOffset = clockOffset
                                greenBoxOffset = Offset.Zero
                                selectedFloatingUid = null
                                isFloating = false
                            }) { Text("확인") }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(48.dp))
                    }

                    Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                    // 시계 + 날짜 + 위젯 슬롯
                    Column(
                        modifier = Modifier
                            .offset { IntOffset(clockOffset.x.roundToInt(), clockOffset.y.roundToInt()) }
                            .pointerInput(isFloating) {
                                if (isFloating) detectDragGestures { change, drag ->
                                    change.consume(); clockOffset += drag
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
                        Text(
                            text = timeFormat.format(Calendar.getInstance().time),
                            color = Color.White, fontSize = 64.sp,
                            fontWeight = FontWeight.Light, letterSpacing = (-2).sp
                        )
                        val dateFormat = SimpleDateFormat("M월 d일 EEEE", Locale.KOREAN)
                        Text(
                            text = dateFormat.format(Calendar.getInstance().time),
                            color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        // ─── 위젯 슬롯 Row ───
                        Spacer(modifier = Modifier.height(16.dp))
                        WidgetSlotRow(
                            placedWidgets = slotWidgets,
                            isFloating = isFloating,
                            slotSize = slotSize,
                            slotGap = slotGap,
                            onRemove = { uid -> slotWidgets = slotWidgets.filter { it.uid != uid } },
                            onAdd = {
                                addTarget = AddTarget.SLOT
                                showLockWidgetPicker = true
                            }
                        )
                    }

                    if (addedApps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(screenHeight * 0.03f))
                        AddedAppsRow(apps = addedApps)
                    }
                }

                // 하단 LockStar 바
                Box(
                    modifier = Modifier
                        .padding(bottom = screenHeight * 0.05f)
                        .offset { IntOffset(greenBoxOffset.x.roundToInt(), greenBoxOffset.y.roundToInt()) }
                        .pointerInput(isFloating) {
                            if (isFloating) detectDragGestures { c, d -> c.consume(); greenBoxOffset += d }
                        }
                        .pointerInput(isFloating) {
                            if (isFloating) detectTapGestures(onTap = { showShortcutPopup = true })
                        }
                        .clip(RoundedCornerShape(30.dp))
                        .border(if (isFloating) 2.dp else 0.dp, Color.LightGray, RoundedCornerShape(30.dp))
                        .background(Color(0xFF101A4D))
                        .height(60.dp)
                        .width(220.dp)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.10f))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "12:45",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFFF4D8D),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            "LockStar",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 선택위젯 독립 box (inner Box 안에 두어 floating 스케일/위치 변화에 따라 함께 변형)
            floatingWidgets.forEach { placed ->
                val isSelected = selectedFloatingUid == placed.uid
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                placed.offset.x.roundToInt(),
                                placed.offset.y.roundToInt()
                            )
                        }
                        .width((if (placed.widget.size == WidgetSize.WIDE) 180.dp else 100.dp) * placed.scale)
                        .height(100.dp * placed.scale)
                        .pointerInput(isFloating, placed.uid) {
                            if (isFloating) {
                                detectTapGestures(onTap = {
                                    selectedFloatingUid =
                                        if (selectedFloatingUid == placed.uid) null else placed.uid
                                })
                            }
                        }
                        .pointerInput(isFloating, placed.uid) {
                            if (isFloating) {
                                detectDragGestures { change, drag ->
                                    change.consume()
                                    floatingWidgets = floatingWidgets.map {
                                        if (it.uid == placed.uid) it.copy(offset = it.offset + drag) else it
                                    }
                                }
                            }
                        }
                ) {
                    WidgetCell(
                        widget = placed.widget,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isFloating && isSelected) {
                        ResizeCornerHandle(Corner.TopStart,    Alignment.TopStart)    { d -> resizeFloating(placed.uid, d) }
                        ResizeCornerHandle(Corner.TopEnd,      Alignment.TopEnd)      { d -> resizeFloating(placed.uid, d) }
                        ResizeCornerHandle(Corner.BottomStart, Alignment.BottomStart) { d -> resizeFloating(placed.uid, d) }
                        ResizeCornerHandle(Corner.BottomEnd,   Alignment.BottomEnd)   { d -> resizeFloating(placed.uid, d) }
                    }

                    if (isFloating) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF453A))
                                .clickable {
                                    if (selectedFloatingUid == placed.uid) selectedFloatingUid = null
                                    floatingWidgets = floatingWidgets.filter { it.uid != placed.uid }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "삭제",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            // 실제 시스템 앱 위젯 호스팅
            hostedWidgets.forEach { hosted ->
                key(hosted.uid) {
                    val isSelected = selectedFloatingUid == hosted.uid
                    val baseWidthDp = with(density) { hosted.providerInfo.minWidth.toDp() }
                    val baseHeightDp = with(density) { hosted.providerInfo.minHeight.toDp() }
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    hosted.offset.x.roundToInt(),
                                    hosted.offset.y.roundToInt(),
                                )
                            }
                            .width(baseWidthDp * hosted.scale)
                            .height(baseHeightDp * hosted.scale)
                            .pointerInput(isFloating, hosted.uid) {
                                if (isFloating) {
                                    detectTapGestures(onTap = {
                                        selectedFloatingUid =
                                            if (selectedFloatingUid == hosted.uid) null else hosted.uid
                                    })
                                }
                            }
                            .pointerInput(isFloating, hosted.uid) {
                                if (isFloating) {
                                    detectDragGestures { change, drag ->
                                        change.consume()
                                        val idx = hostedWidgets.indexOfFirst { it.uid == hosted.uid }
                                        if (idx != -1) {
                                            val hw = hostedWidgets[idx]
                                            hostedWidgets[idx] = hw.copy(offset = hw.offset + drag)
                                        }
                                    }
                                }
                            }
                    ) {
                        appWidgetHost?.let { host ->
                            AndroidView(
                                factory = { ctx ->
                                    host.createView(
                                        ctx, hosted.appWidgetId, hosted.providerInfo
                                    ).apply {
                                        setAppWidget(hosted.appWidgetId, hosted.providerInfo)
                                    }
                                },
                                update = { view ->
                                    val d = view.resources.displayMetrics.density
                                    val wDp = (hosted.providerInfo.minWidth / d * hosted.scale).toInt()
                                    val hDp = (hosted.providerInfo.minHeight / d * hosted.scale).toInt()
                                    try {
                                        view.updateAppWidgetSize(Bundle(), wDp, hDp, wDp, hDp)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (isFloating && isSelected) {
                            ResizeCornerHandle(Corner.TopStart,    Alignment.TopStart)    { d -> resizeHosted(hosted.uid, d) }
                            ResizeCornerHandle(Corner.TopEnd,      Alignment.TopEnd)      { d -> resizeHosted(hosted.uid, d) }
                            ResizeCornerHandle(Corner.BottomStart, Alignment.BottomStart) { d -> resizeHosted(hosted.uid, d) }
                            ResizeCornerHandle(Corner.BottomEnd,   Alignment.BottomEnd)   { d -> resizeHosted(hosted.uid, d) }
                        }

                        if (isFloating) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-6).dp)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF453A))
                                    .clickable {
                                        if (selectedFloatingUid == hosted.uid) selectedFloatingUid = null
                                        onRemoveHosted(hosted.uid)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "삭제",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 항목 선택 팝업
        if (showShortcutPopup) {
            ShortcutPickerDialog(
                onDismiss = { showShortcutPopup = false },
                onSelect = { selected ->
                    showShortcutPopup = false
                    when (selected) {
                        "app_widget" -> {
                            addTarget= AddTarget.FLOATING
                            showLockWidgetPicker = true
                        }
                        "real_widget" -> showRealWidgetPicker = true
                        "favorite_app" -> showAppWidgetSheet = true
                        "text" -> {}
                    }
                }
            )
        }

        // 잠금화면 위젯 선택 (2단계 바텀시트)
        if (showLockWidgetPicker) {
            LockWidgetPickerSheet(
                onDismiss = { showLockWidgetPicker = false },
                onWidgetSelected = { widget ->
                    addCounter++

                    if (addTarget == AddTarget.SLOT) {
                        val needed = if (widget.size == WidgetSize.WIDE) 2 else 1
                        val used = slotWidgets.sumOf { if (it.widget.size == WidgetSize.WIDE) 2 else 1 }

                        if (used + needed <= 4) {
                            slotWidgets = slotWidgets + PlacedWidget(
                                uid = "${widget.id}_$addCounter",
                                widget = widget
                            )
                        }
                    } else {
                        floatingWidgets = floatingWidgets + FloatingWidget(
                            uid = "${widget.id}_$addCounter",
                            widget = widget,
                            offset = Offset(
                                x = 80f + addCounter * 20f,
                                y = 280f + addCounter * 20f
                            )
                        )
                    }

                    showLockWidgetPicker = false
                }
            )
        }

        // 앱 위젯 바텀시트
        if (showAppWidgetSheet) {
            AppWidgetBottomSheet(
                apps = defaultAppList,
                onDismiss = { showAppWidgetSheet = false },
                onAppSelected = { app ->
                    if (addedApps.none { it.id == app.id }) addedApps = addedApps + app
                    showAppWidgetSheet = false
                }
            )
        }

        // 실제 시스템 위젯 피커
        if (showRealWidgetPicker && appWidgetManager != null) {
            RealWidgetPickerSheet(
                appWidgetManager = appWidgetManager,
                onDismiss = { showRealWidgetPicker = false },
                onSelect = { info ->
                    showRealWidgetPicker = false
                    onRealWidgetSelected(info)
                }
            )
        }
    }
}

// ============================================================
// 플로팅 위젯 크기 조절 코너 핸들
// ============================================================

enum class Corner { TopStart, TopEnd, BottomStart, BottomEnd }

@Composable
fun BoxScope.ResizeCornerHandle(
    corner: Corner,
    alignment: Alignment,
    onResize: (Float) -> Unit,
) {
    val handleSize = 24.dp
    Box(
        modifier = Modifier
            .align(alignment)
            .offset(
                x = if (alignment == Alignment.TopStart || alignment == Alignment.BottomStart) (-handleSize / 2) else (handleSize / 2),
                y = if (alignment == Alignment.TopStart || alignment == Alignment.TopEnd) (-handleSize / 2) else (handleSize / 2)
            )
            .size(handleSize)
            .pointerInput(corner) {
                detectDragGestures { change, drag ->
                    change.consume()
                    val invSqrt2 = 1f / sqrt(2f)
                    val projection = when (corner) {
                        Corner.TopStart    -> (-drag.x - drag.y) * invSqrt2
                        Corner.TopEnd      -> ( drag.x - drag.y) * invSqrt2
                        Corner.BottomStart -> (-drag.x + drag.y) * invSqrt2
                        Corner.BottomEnd   -> ( drag.x + drag.y) * invSqrt2
                    }
                    onResize(projection / 200f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val stroke = 4.dp.toPx()
            val len = size.minDimension
            val cap = StrokeCap.Round
            when (corner) {
                Corner.TopStart -> {
                    drawLine(Color.White, Offset(0f, 0f),   Offset(len, 0f), stroke, cap)
                    drawLine(Color.White, Offset(0f, 0f),   Offset(0f, len), stroke, cap)
                }
                Corner.TopEnd -> {
                    drawLine(Color.White, Offset(len, 0f), Offset(0f, 0f),   stroke, cap)
                    drawLine(Color.White, Offset(len, 0f), Offset(len, len), stroke, cap)
                }
                Corner.BottomStart -> {
                    drawLine(Color.White, Offset(0f, len), Offset(len, len), stroke, cap)
                    drawLine(Color.White, Offset(0f, len), Offset(0f, 0f),   stroke, cap)
                }
                Corner.BottomEnd -> {
                    drawLine(Color.White, Offset(len, len), Offset(0f, len), stroke, cap)
                    drawLine(Color.White, Offset(len, len), Offset(len, 0f), stroke, cap)
                }
            }
        }
    }
}

// ============================================================
// 위젯 슬롯 Row (4칸)
// ============================================================

@Composable
fun WidgetSlotRow(
    placedWidgets: List<PlacedWidget>,
    isFloating: Boolean,
    slotSize: Dp,
    slotGap: Dp,
    onRemove: (String) -> Unit,
    onAdd: () -> Unit
) {
    val usedSlots = placedWidgets.sumOf { if (it.widget.size == WidgetSize.WIDE) 2 else 1 }
    val round = if(isFloating) Modifier.border(1.dp,Color.LightGray, shape = RoundedCornerShape(8.dp)).clickable { onAdd() }
    else Modifier
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = slotGap, alignment = Alignment.CenterHorizontally),
        modifier = Modifier.then(round).fillMaxWidth(0.5f).height(slotSize + 2.dp)
    ) {

        placedWidgets.forEach { placed ->
            val span = if (placed.widget.size == WidgetSize.WIDE) 2 else 1
            Box(modifier = Modifier.width(slotSize * span)) {
                WidgetCell(
                    widget = placed.widget,
                    modifier = Modifier.fillMaxWidth().height(slotSize)
                )
                // 편집모드 삭제 버튼 (좌상단 빨간 원)
                if (isFloating) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF453A))
                            .clickable { onRemove(placed.uid) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "제거",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// 위젯 셀 (실제 잠금화면에 표시되는 위젯)
// ============================================================

@Composable
fun WidgetCell(widget: LockWidget, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF3A3A3C)),
        contentAlignment = Alignment.Center
    ) {
        when (widget.size) {
            WidgetSize.SMALL -> SmallWidgetContent(widget)
            WidgetSize.WIDE  -> WideWidgetContent(widget)
        }
    }
}

@Composable
fun SmallWidgetContent(widget: LockWidget) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(6.dp)
    ) {
        widget.icon?.let {
            Icon(it, null, tint = widget.iconTint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
        }
        if (widget.mainValue.isNotEmpty()) {
            Text(
                widget.mainValue,
                color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, lineHeight = 16.sp
            )
        }
        if (widget.subValue.isNotEmpty()) {
            Text(
                widget.subValue,
                color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WideWidgetContent(widget: LockWidget) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        widget.icon?.let {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(it, null, tint = widget.iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(8.dp))
        }
        Column {
            if (widget.mainValue.isNotEmpty()) {
                Text(
                    widget.mainValue,
                    color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            if (widget.subValue.isNotEmpty()) {
                Text(
                    widget.subValue,
                    color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================================
// 위젯 선택 바텀시트 (2단계: 앱 목록 → 위젯 목록)
// ============================================================

sealed class PickerStep {
    data object AppList : PickerStep()
    data class WidgetList(val app: WidgetApp) : PickerStep()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockWidgetPickerSheet(
    onDismiss: () -> Unit,
    onWidgetSelected: (LockWidget) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var step by remember { mutableStateOf<PickerStep>(PickerStep.AppList) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E)
    ) {
        when (val s = step) {
            is PickerStep.AppList -> {
                // 1단계: 앱 목록
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        "위젯", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                    LazyColumn {
                        items(lockWidgetApps) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { step = PickerStep.WidgetList(app) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 앱 아이콘 (컬러 원형 배경)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(app.iconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(app.icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(app.name, color = Color.White, fontSize = 17.sp, modifier = Modifier.weight(1f))
                                Text(
                                    app.widgets.size.toString(),
                                    color = Color(0xFF8E8E93), fontSize = 16.sp
                                )
                            }
                            Divider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 68.dp))
                        }
                    }
                }
            }

            is PickerStep.WidgetList -> {
                // 2단계: 위젯 목록
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    // 헤더 (뒤로가기 + 앱 정보)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable { step = PickerStep.AppList },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.KeyboardArrowLeft, null,
                                tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(s.app.iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(s.app.icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(s.app.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f))
                        Text(s.app.widgets.size.toString(), color = Color(0xFF8E8E93), fontSize = 16.sp)
                    }

                    Spacer(Modifier.height(8.dp))

                    // 위젯 그리드 미리보기 (1x1 = 1칸, 2x1 = 2칸)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        items(
                            items = s.app.widgets,
                            span = { widget -> GridItemSpan(if (widget.size == WidgetSize.WIDE) 2 else 1) }
                        ) { widget ->
                            WidgetPreviewItem(widget = widget, onClick = { onWidgetSelected(widget) })
                        }
                    }
                }
            }
        }
    }
}

// 위젯 미리보기 아이템 (2단계에서 보여주는 각 위젯)
@Composable
fun WidgetPreviewItem(widget: LockWidget, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2C2C2E))
            .clickable { onClick() }
            .padding(bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 위젯 미리보기 박스
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF3A3A3C)),
            contentAlignment = Alignment.Center
        ) {
            when (widget.size) {
                WidgetSize.SMALL -> SmallWidgetContent(widget)
                WidgetSize.WIDE  -> WideWidgetContent(widget)
            }
        }
        Text(
            widget.name,
            color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            if (widget.size == WidgetSize.SMALL) "1x1" else "2x1",
            color = Color(0xFF8E8E93), fontSize = 11.sp
        )
    }
}

// ============================================================
// 실제 시스템 앱 위젯 피커 (설치된 위젯 목록)
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealWidgetPickerSheet(
    appWidgetManager: AppWidgetManager,
    onDismiss: () -> Unit,
    onSelect: (AppWidgetProviderInfo) -> Unit,
) {
    val ctx = LocalContext.current
    val pm = ctx.packageManager
    val grouped = remember(appWidgetManager) {
        appWidgetManager.installedProviders
            .groupBy { it.provider.packageName }
            .toList()
            .sortedBy { (pkg, _) ->
                try {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString().lowercase()
                } catch (_: Exception) { pkg }
            }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text(
                "실제 앱 위젯",
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            if (grouped.isEmpty()) {
                Text(
                    "설치된 위젯이 없습니다",
                    color = Color(0xFF8E8E93), fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
            LazyColumn(modifier = Modifier.fillMaxWidth().height(500.dp)) {
                grouped.forEach { (pkg, infos) ->
                    val appLabel = try {
                        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                    } catch (_: Exception) { pkg }
                    item(key = "header_$pkg") {
                        Text(
                            appLabel,
                            color = Color(0xFF8E8E93), fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(infos, key = { it.provider.flattenToShortString() }) { info ->
                        val label = try { info.loadLabel(pm).toString() } catch (_: Exception) { info.provider.shortClassName }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(info) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Text(
                                "${info.minWidth}×${info.minHeight}",
                                color = Color(0xFF8E8E93), fontSize = 12.sp
                            )
                        }
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }
        }
    }
}

// ============================================================
// 항목 선택 다이얼로그 (초록 바 탭)
// ============================================================

@Composable
fun ShortcutPickerDialog(onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 8.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("추가할 항목 선택", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = Color.Black, modifier = Modifier.padding(bottom = 16.dp))
                Button(onClick = { onSelect("app_widget") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text("앱 위젯 (샘플)") }
                Button(onClick = { onSelect("real_widget") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text("실제 앱 위젯") }
                Button(onClick = { onSelect("favorite_app") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text("즐겨찾는 앱") }
                Button(onClick = { onSelect("text") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text("글 넣기") }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) { Text("취소") }
            }
        }
    }
}

// ============================================================
// 앱 위젯 바텀시트
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppWidgetBottomSheet(apps: List<AppItem>, onDismiss: () -> Unit, onAppSelected: (AppItem) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color.White) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Text("앱 선택", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black,
                modifier = Modifier.padding(vertical = 12.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxWidth().height(400.dp)) {
                items(apps) { app ->
                    Column(
                        modifier = Modifier.fillMaxWidth().clickable { onAppSelected(app) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                            .background(app.tint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(app.icon, null, tint = app.tint, modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(app.name, fontSize = 12.sp, color = Color.Black, textAlign = TextAlign.Center, maxLines = 1)
                    }
                }
            }
        }
    }
}

// ============================================================
// 추가된 앱 Row
// ============================================================

@Composable
fun AddedAppsRow(apps: List<AppItem>) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
        apps.take(8).forEach { app ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.85f)), contentAlignment = Alignment.Center) {
                    Icon(app.icon, null, tint = app.tint, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(app.name, fontSize = 10.sp, color = Color.White, maxLines = 1)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LockScreenPreview() {
    LockScreenCopyTheme() { LockScreen(onUnlock = {}) }
}