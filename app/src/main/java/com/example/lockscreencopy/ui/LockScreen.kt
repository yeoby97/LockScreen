package com.example.lockscreencopy.ui

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.R
import com.example.lockscreencopy.model.AddTarget
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.FavoriteAppsLayout
import com.example.lockscreencopy.model.FloatingWidget
import com.example.lockscreencopy.model.HostedAppWidget
import com.example.lockscreencopy.model.PlacedWidget
import com.example.lockscreencopy.model.WidgetSize
import com.example.lockscreencopy.data.DataSourceResolver
import com.example.lockscreencopy.data.GeminiClient
import com.example.lockscreencopy.data.handleSystemAction
import com.example.lockscreencopy.data.hasUsageStatsPermission
import com.example.lockscreencopy.data.launchAppShortcut
import com.example.lockscreencopy.data.loadInstalledApps
import com.example.lockscreencopy.data.loadWeeklyUsage
import com.example.lockscreencopy.data.NotificationRepository
import com.example.lockscreencopy.data.isNotificationListenerEnabled
import com.example.lockscreencopy.data.openNotificationListenerSettings
import com.example.lockscreencopy.data.openUsageAccessSettings
import com.example.lockscreencopy.data.executeChatNudgeAction
import com.example.lockscreencopy.data.sampleChats
import com.example.lockscreencopy.data.toAppGroups
import com.example.lockscreencopy.data.topUsedAppsWithGap
import com.example.lockscreencopy.model.AiSketchWidget
import com.example.lockscreencopy.model.WidgetSpace
import com.example.lockscreencopy.model.ChatMessage
import com.example.lockscreencopy.ui.notification.ChatNotificationStack
import com.example.lockscreencopy.ui.notification.NotificationPermissionBanner
import com.example.lockscreencopy.ui.llm.GhostInstance
import com.example.lockscreencopy.ui.llm.LlmAppStrip
import com.example.lockscreencopy.ui.llm.LlmRealWidgetGhost
import com.example.lockscreencopy.ui.llm.RectBounds
import com.example.lockscreencopy.ui.llm.ShortcutRecommendationBadge
import com.example.lockscreencopy.ui.llm.StripAppEntry
import com.example.lockscreencopy.ui.llm.placeGhostRects
import com.example.lockscreencopy.ui.llm.realWidgetGhostKey
import com.example.lockscreencopy.ui.sketch.PendingSketch
import com.example.lockscreencopy.ui.sketch.SketchModeOverlay
import com.example.lockscreencopy.ui.sketch.SlotAdjustOverlay
import com.example.lockscreencopy.model.SpaceItemLayout
import com.example.lockscreencopy.ui.space.SpaceCanvas
import com.example.lockscreencopy.ui.space.SpaceMember
import com.example.lockscreencopy.ui.space.baseSizeDp
import com.example.lockscreencopy.ui.space.resizeSpaceItem
import com.example.lockscreencopy.ui.space.SpaceSketchOverlay
import com.example.lockscreencopy.ui.space.WidgetSpaceBubble
import com.example.lockscreencopy.ui.space.WidgetSpaceExpanded
import com.example.lockscreencopy.ui.widget.AiSketchWidgetItem
import com.example.lockscreencopy.ui.widget.toBitmapSafe
import com.example.lockscreencopy.ui.picker.AiActionChoice
import com.example.lockscreencopy.ui.picker.AiActionPickerDialog
import com.example.lockscreencopy.ui.picker.BottomShortcutPickerSheet
import com.example.lockscreencopy.ui.picker.FavoriteAppsPickerScreen
import com.example.lockscreencopy.ui.picker.FavoriteAppsSettingsSheet
import com.example.lockscreencopy.ui.picker.LlmLayoutSheet
import com.example.lockscreencopy.ui.picker.LlmSuggestionResult
import com.example.lockscreencopy.ui.picker.LockWidgetPickerSheet
import com.example.lockscreencopy.ui.picker.RealWidgetPickerSheet
import com.example.lockscreencopy.ui.picker.ShortcutChoice
import com.example.lockscreencopy.ui.picker.ShortcutPickerDialog
import com.example.lockscreencopy.ui.widget.BottomShortcutButton
import com.example.lockscreencopy.ui.widget.ClockHeader
import com.example.lockscreencopy.ui.widget.FavoriteAppsDisplay
import com.example.lockscreencopy.ui.widget.FloatingWidgetItem
import com.example.lockscreencopy.ui.widget.HostedWidgetItem
import com.example.lockscreencopy.ui.widget.LockStarBar
import com.example.lockscreencopy.ui.widget.ResizeHandles
import com.example.lockscreencopy.ui.widget.WidgetSlotRow
import com.example.lockscreencopy.ui.theme.LockScreenCopyTheme
import com.example.lockscreencopy.ui.theme.LockTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.math.roundToInt
import android.graphics.BitmapFactory
import android.widget.Toast
import com.example.lockscreencopy.model.AiTextSlot
import com.example.lockscreencopy.model.InfoSource

private enum class ShortcutSide { LEFT, RIGHT }

@Composable
fun LockScreen(
    onUnlock: () -> Unit,
    hostedWidgets: SnapshotStateList<HostedAppWidget> = mutableStateListOf(),
    appWidgetHost: AppWidgetHost? = null,
    appWidgetManager: AppWidgetManager? = null,
    onRealWidgetSelected: (AppWidgetProviderInfo, Offset) -> Unit = { _, _ -> },
    onRemoveHosted: (String) -> Unit = {},
    cancelledRealComponents: SnapshotStateList<String> = mutableStateListOf(),
) {
    val context = LocalContext.current
    var isFloating by remember { mutableStateOf(false) }
    var showShortcutPopup by remember { mutableStateOf(false) }
    var showLockWidgetPicker by remember { mutableStateOf(false) }
    var showRealWidgetPicker by remember { mutableStateOf(false) }

    val realNotifications by NotificationRepository.notifications.collectAsState()
    val dummyChatGroups = remember { sampleChats() }
    var notificationMode by remember { mutableStateOf(NotificationMode.DUMMY) }

    var hasNotificationPermission by remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = isNotificationListenerEnabled(context)
            }
        }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs) }
    }

    var favoriteApps by remember { mutableStateOf(listOf<BottomShortcut>()) }
    var favoriteAppsEnabled by remember { mutableStateOf(true) }
    var favoriteAppsLayout by remember { mutableStateOf(FavoriteAppsLayout.BOTTOM_LEFT) }
    var favoriteAppsUsageSort by remember { mutableStateOf(false) }
    var weeklyUsage by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var installedApps by remember { mutableStateOf<List<BottomShortcut.App>>(emptyList()) }
    var showFavoriteSettings by remember { mutableStateOf(false) }
    var showFavoritePicker by remember { mutableStateOf(false) }

    LaunchedEffect(favoriteAppsUsageSort) {
        if (favoriteAppsUsageSort) {
            if (!hasUsageStatsPermission(context)) {
                openUsageAccessSettings(context)
                weeklyUsage = emptyMap()
                installedApps = emptyList()
            } else {
                weeklyUsage = withContext(Dispatchers.IO) { loadWeeklyUsage(context) }
                installedApps = withContext(Dispatchers.IO) { loadInstalledApps(context) }
            }
        }
    }

    val displayedFavorites = if (favoriteAppsUsageSort && weeklyUsage.isNotEmpty()) {
        topUsedAppsWithGap(installedApps, weeklyUsage)
    } else {
        favoriteApps
    }

    var leftShortcut by remember { mutableStateOf<BottomShortcut?>(null) }
    var rightShortcut by remember { mutableStateOf<BottomShortcut?>(null) }
    var pickingShortcutSide by remember { mutableStateOf<ShortcutSide?>(null) }

    fun activateShortcut(shortcut: BottomShortcut) {
        when (shortcut) {
            is BottomShortcut.System -> handleSystemAction(context, shortcut.action)
            is BottomShortcut.App -> launchAppShortcut(context, shortcut)
        }
    }

    var slotWidgets by remember { mutableStateOf<List<PlacedWidget>>(emptyList()) }
    var floatingWidgets by remember { mutableStateOf<List<FloatingWidget>>(emptyList()) }
    var selectedFloatingUid by remember { mutableStateOf<String?>(null) }
    var addTarget by remember { mutableStateOf(AddTarget.SLOT) }

    var showLlmSheet by remember { mutableStateOf(false) }
    var showAiChooser by remember { mutableStateOf(false) }
    var llmSuggestion by remember { mutableStateOf<LlmSuggestionResult?>(null) }

    var sketchMode by remember { mutableStateOf(false) }
    var sketchLoading by remember { mutableStateOf(false) }
    var sketchError by remember { mutableStateOf<String?>(null) }
    var aiSketchWidgets by remember { mutableStateOf<List<AiSketchWidget>>(emptyList()) }
    var aiSketchCounter by remember { mutableStateOf(0) }
    var pendingSketchAdjust by remember { mutableStateOf<PendingSketch?>(null) }
    val sketchScope = rememberCoroutineScope()

    // 위젯 공간(여러 위젯을 묶는 유리 버블)
    var widgetSpaces by remember { mutableStateOf<List<WidgetSpace>>(emptyList()) }
    var spaceSketchMode by remember { mutableStateOf(false) }
    // null = 새 공간 생성, 그 외 = 해당 공간에 위젯 추가
    var spaceSketchTargetId by remember { mutableStateOf<String?>(null) }
    var expandedSpaceId by remember { mutableStateOf<String?>(null) }
    var spaceCounter by remember { mutableStateOf(0) }

    var addCounter by remember { mutableStateOf(0) }

    var clockOffset by remember { mutableStateOf(Offset.Zero) }
    var clockNaturalPosition by remember { mutableStateOf(Offset.Zero) }
    var clockColumnHeightDp by remember { mutableStateOf(0.dp) }

    // 점유영역 실측용 좌표 (스케일 Box 기준 프레임 + 각 UI 요소)
    var contentRootCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var clockBoundsCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var slotRowCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var bottomBarCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var greenBoxOffset by remember { mutableStateOf(Offset.Zero) }
    var savedClockOffset by remember { mutableStateOf(Offset.Zero) }
    var clockScale by remember { mutableStateOf(1f) }
    var savedClockScale by remember { mutableStateOf(1f) }
    var favoriteAppsOffset by remember { mutableStateOf(Offset.Zero) }
    var savedFavoriteAppsOffset by remember { mutableStateOf(Offset.Zero) }

    BackHandler(enabled = isFloating) {
        clockOffset = savedClockOffset
        clockScale = savedClockScale
        favoriteAppsOffset = savedFavoriteAppsOffset
        greenBoxOffset = Offset.Zero
        selectedFloatingUid = null
        isFloating = false
        llmSuggestion = null
    }

    LaunchedEffect(llmSuggestion) {
        if (llmSuggestion != null && !isFloating) {
            clockOffset = savedClockOffset
            isFloating = true
        }
    }

    var activeLlmAppId by remember(llmSuggestion) { mutableStateOf<String?>(null) }
    val consumedGhostKeys = remember(llmSuggestion) { mutableStateListOf<String>() }
    val ghostOriginByUid = remember(llmSuggestion) { mutableStateMapOf<String, String>() }
    /** 실제 위젯 ghost 를 탭한 직후 ~ HostedAppWidget 이 생성되기까지의 component 목록 */
    val pendingRealComponents = remember(llmSuggestion) { mutableStateListOf<String>() }
    var leftShortcutRecDismissed by remember(llmSuggestion) { mutableStateOf(false) }
    var rightShortcutRecDismissed by remember(llmSuggestion) { mutableStateOf(false) }

    /** LLM 세션 시작 시점의 좌/우 바로가기 스냅샷 (취소 시 복원용) */
    val preLlmLeftShortcut = remember(llmSuggestion) { leftShortcut }
    val preLlmRightShortcut = remember(llmSuggestion) { rightShortcut }

    fun releaseGhostFor(uid: String) {
        val key = ghostOriginByUid.remove(uid) ?: return
        consumedGhostKeys.remove(key)
    }

    // 바인딩/설정을 거부해 위젯이 실제로 설치되지 않은 경우, 탭 시 소비했던 ghost 를 추천 목록에 복원.
    LaunchedEffect(cancelledRealComponents.size) {
        if (cancelledRealComponents.isEmpty()) return@LaunchedEffect
        cancelledRealComponents.forEach { comp ->
            consumedGhostKeys.remove(realWidgetGhostKey(comp))
            pendingRealComponents.remove(comp)
        }
        cancelledRealComponents.clear()
    }

    // 새로 추가된 hosted widget 이 pending real ghost 의 결과면 uid → ghost key 매핑 기록.
    // 이후 사용자가 해당 hosted widget 을 삭제하면 releaseGhostFor 로 ghost 복원.
    LaunchedEffect(hostedWidgets.size, pendingRealComponents.size) {
        if (pendingRealComponents.isEmpty()) return@LaunchedEffect
        hostedWidgets.forEach { hw ->
            if (hw.uid in ghostOriginByUid) return@forEach
            val comp = hw.providerInfo.provider.flattenToShortString()
            val pendingIdx = pendingRealComponents.indexOf(comp)
            if (pendingIdx >= 0) {
                ghostOriginByUid[hw.uid] = realWidgetGhostKey(comp)
                pendingRealComponents.removeAt(pendingIdx)
            }
        }
    }

    val leftRecommendation = llmSuggestion?.let { s ->
        s.recommendation.left?.let { id -> s.selected.shortcutCandidates().firstOrNull { it.id == id } }
    }
    val rightRecommendation = llmSuggestion?.let { s ->
        s.recommendation.right?.let { id -> s.selected.shortcutCandidates().firstOrNull { it.id == id } }
    }

    // mock 앱 + 실제 앱을 이름 기반으로 매칭해 하나의 스트립 엔트리로 머지.
    // 매칭되면 같은 엔트리에서 트레이 ghost(mock)와 자유 ghost(real)이 동시에 노출됨.
    val stripEntries: List<StripAppEntry> = llmSuggestion?.let { s ->
        val mockWithRec = s.selected.widgetApps.filter { app ->
            s.recommendation.tray.any { id -> app.widgets.any { it.id == id } }
        }
        val realWithRec = s.selected.realApps.filter { ra ->
            ra.providers.any { it.provider.flattenToShortString() in s.recommendation.floating }
        }
        fun normalize(text: String): String =
            text.lowercase().filter { it.isLetterOrDigit() }
        val takenRealPackages = HashSet<String>()
        buildList {
            mockWithRec.forEach { mockApp ->
                val mockKey = normalize(mockApp.name)
                val match = realWithRec.firstOrNull { ra ->
                    ra.packageName !in takenRealPackages && run {
                        val n = normalize(ra.appLabel)
                        mockKey.isNotEmpty() && (n.contains(mockKey) || mockKey.contains(n))
                    }
                }
                if (match != null) takenRealPackages += match.packageName
                add(
                    StripAppEntry(
                        id = mockApp.id,
                        name = mockApp.name,
                        iconBg = mockApp.iconBg,
                        iconVector = mockApp.icon,
                        mockAppId = mockApp.id,
                        realPackages = listOfNotNull(match?.packageName),
                    ),
                )
            }
            realWithRec.forEach { ra ->
                if (ra.packageName in takenRealPackages) return@forEach
                add(
                    StripAppEntry(
                        id = ra.firstStepId,
                        name = ra.appLabel,
                        iconBg = Color(0xFF424242),
                        iconBitmap = ra.appIcon?.toBitmapSafe(),
                        realPackages = listOf(ra.packageName),
                    ),
                )
            }
        }
    } ?: emptyList()

    val activeEntry = stripEntries.firstOrNull { it.id == activeLlmAppId }
    val activeMockApp = activeEntry?.mockAppId?.let { mid ->
        llmSuggestion?.selected?.widgetApps?.firstOrNull { it.id == mid }
    }
    val activeRealApps = activeEntry?.realPackages?.mapNotNull { pkg ->
        llmSuggestion?.selected?.realApps?.firstOrNull { it.packageName == pkg }
    } ?: emptyList()

    // 트레이 ghost: 선택된 엔트리에 mock 앱이 매칭돼 있을 때
    val trayGhosts: List<GhostInstance> = if (llmSuggestion != null && activeMockApp != null) {
        llmSuggestion!!.recommendation.tray.mapIndexedNotNull { idx, id ->
            activeMockApp.widgets.firstOrNull { it.id == id }?.let { w ->
                GhostInstance("tray_${activeMockApp.id}_${idx}_$id", w)
            }
        }
    } else emptyList()

    // 자유 ghost: 선택된 엔트리에 실제 앱이 매칭돼 있을 때
    val realFloatingGhosts: List<AppWidgetProviderInfo> =
        if (llmSuggestion != null && activeRealApps.isNotEmpty()) {
            val recComponents = llmSuggestion!!.recommendation.floating.toHashSet()
            activeRealApps.flatMap { ra ->
                ra.providers.filter { it.provider.flattenToShortString() in recComponents }
            }
        } else emptyList()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    fun resizeFloating(uid: String, dx: Float, dy: Float, ax: Float, ay: Float) {
        floatingWidgets = floatingWidgets.map { fw ->
            if (fw.uid != uid) fw else resizeFloatingWidget(fw, dx, dy, ax, ay, density)
        }
    }

    fun resizeHosted(uid: String, dx: Float, dy: Float, ax: Float, ay: Float) {
        val idx = hostedWidgets.indexOfFirst { it.uid == uid }
        if (idx != -1) hostedWidgets[idx] = resizeHostedWidget(hostedWidgets[idx], dx, dy, ax, ay, density.density)
    }

    fun resizeAiSketch(uid: String, dx: Float, dy: Float, ax: Float, ay: Float) {
        aiSketchWidgets = aiSketchWidgets.map { w ->
            if (w.uid != uid) w else resizeAiSketchWidget(w, dx, dy, ax, ay, density)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isFloating) 0.77f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing), label = "scale",
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isFloating) 30.dp else 0.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing), label = "corner",
    )
    val blurRadius by animateDpAsState(
        targetValue = if (isFloating) 20.dp else 0.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing), label = "blur",
    )
    val editAlpha by animateFloatAsState(
        targetValue = if (isFloating) 1f else 0f,
        animationSpec = tween(300, delayMillis = 200, easing = FastOutSlowInEasing), label = "editAlpha",
    )

    val screenWidthPx = with(density) { screenWidth.toPx() }
    val screenHeightPx = with(density) { screenHeight.toPx() }
    val slotGap = 8.dp
    val slotSize = screenWidth * 0.1f

    val occupiedMarginPx = with(density) { 8.dp.toPx() }
    // 스케일 Box 로컬 공간(= ghost offset 공간)으로 자식 bounds 변환. scale/transformOrigin 무관.
    fun measuredBounds(child: LayoutCoordinates?): RectBounds? {
        val root = contentRootCoords ?: return null
        if (!root.isAttached || child == null || !child.isAttached) return null
        val r: Rect = root.localBoundingBoxOf(child, clipBounds = false)
        if (r.width <= 0f || r.height <= 0f) return null
        return RectBounds(
            left = r.left - occupiedMarginPx,
            top = r.top - occupiedMarginPx,
            right = r.right + occupiedMarginPx,
            bottom = r.bottom + occupiedMarginPx,
        )
    }

    fun currentOccupiedRects(): List<RectBounds> = buildList {
        val slotSizePx = with(density) { slotSize.toPx() }
        val slotGapPx = with(density) { slotGap.toPx() }
        // 시계: 실측 우선, 첫 프레임 등 미측정 시 추정 분수로 폴백
        add(
            measuredBounds(clockBoundsCoords)
                ?: RectBounds(screenWidthPx * 0.2f, screenHeightPx * 0.08f, screenWidthPx * 0.8f, screenHeightPx * 0.3f),
        )
        // 슬롯행(트레이)
        add(
            measuredBounds(slotRowCoords) ?: run {
                val slotRowWidth = slotSizePx * 4f + slotGapPx * 3f
                val slotRowLeft = (screenWidthPx - slotRowWidth) / 2f
                val slotRowTop = screenHeightPx * 0.32f
                RectBounds(slotRowLeft, slotRowTop, slotRowLeft + slotRowWidth, slotRowTop + slotSizePx)
            },
        )
        // 하단 LockStarBar/바로가기
        add(
            measuredBounds(bottomBarCoords)
                ?: RectBounds(screenWidthPx * 0.12f, screenHeightPx * 0.78f, screenWidthPx * 0.88f, screenHeightPx * 0.96f),
        )
        addAll(floatingWidgets.map { placed ->
            val width = with(density) {
                (if (placed.widget.size == WidgetSize.WIDE) 180.dp else 100.dp).toPx() * placed.scaleX
            }
            val height = with(density) { 100.dp.toPx() * placed.scaleY }
            RectBounds(
                left = placed.offset.x,
                top = placed.offset.y,
                right = placed.offset.x + width,
                bottom = placed.offset.y + height,
            )
        })
        addAll(hostedWidgets.map { hosted ->
            RectBounds(
                left = hosted.offset.x,
                top = hosted.offset.y,
                right = hosted.offset.x + hosted.widthPx * hosted.scaleX,
                bottom = hosted.offset.y + hosted.heightPx * hosted.scaleY,
            )
        })
    }

    // ghost 는 위젯이 실제로 호스트될 때의 크기와 정확히 일치해야 한다 — 그래야
    // 사용자가 ghost 를 탭한 직후 위젯이 같은 자리/크기로 자리잡는다.
    fun ghostSizePx(info: AppWidgetProviderInfo): Pair<Float, Float> {
        val (wDp, hDp) = resolveWidgetSizeDp(info, density.density)
        return with(density) { wDp.dp.toPx() to hDp.dp.toPx() }
    }

    // 수동 추가(피커) 위젯도 ghost 와 동일하게 이미 배치된 항목을 피해 자리 잡도록 계산
    fun availableOffsetFor(info: AppWidgetProviderInfo): Offset {
        val rect = placeGhostRects(
            ghostSizes = listOf(ghostSizePx(info)),
            screenWidthPx = screenWidthPx,
            screenHeightPx = screenHeightPx,
            occupied = currentOccupiedRects(),
        ).first()
        return Offset(rect.left, rect.top)
    }

    // ── 위젯 공간(스케치 묶음) 유틸 ────────────────────────────────
    // 스케치 오버레이는 화면(스크린) 좌표를 쓰지만 위젯은 스케일 Box 로컬 좌표에 배치되므로,
    // 같은 변환(pivot 0.5,0.2 · scale)의 역변환으로 좌표계를 맞춘다.
    fun screenRectToLocal(screen: Rect): Rect {
        val pivotX = screenWidthPx * 0.5f
        val pivotY = screenHeightPx * 0.2f
        val s = if (scale == 0f) 1f else scale
        fun cvt(x: Float, y: Float) = Offset(
            pivotX + (x - pivotX) / s,
            pivotY + (y - pivotY) / s,
        )
        val tl = cvt(screen.left, screen.top)
        val br = cvt(screen.right, screen.bottom)
        return Rect(tl.x, tl.y, br.x, br.y)
    }

    fun floatingRect(fw: FloatingWidget): Rect {
        val w = with(density) {
            (if (fw.widget.size == WidgetSize.WIDE) 180.dp else 100.dp).toPx()
        } * fw.scaleX
        val h = with(density) { 100.dp.toPx() } * fw.scaleY
        return Rect(fw.offset.x, fw.offset.y, fw.offset.x + w, fw.offset.y + h)
    }

    fun hostedRect(hw: HostedAppWidget): Rect = Rect(
        hw.offset.x,
        hw.offset.y,
        hw.offset.x + hw.widthPx * hw.scaleX,
        hw.offset.y + hw.heightPx * hw.scaleY,
    )

    fun aiRect(w: AiSketchWidget): Rect {
        val wp = with(density) { (w.widthDp * w.scaleX).dp.toPx() }
        val hp = with(density) { (w.heightDp * w.scaleY).dp.toPx() }
        return Rect(w.offset.x, w.offset.y, w.offset.x + wp, w.offset.y + hp)
    }

    // 화면 좌표 사각형에 겹치는, 아직 어느 공간에도 속하지 않은 자유 위젯 개수
    fun freeWidgetsInRect(screenRect: Rect): Int {
        val local = screenRectToLocal(screenRect)
        var n = 0
        n += floatingWidgets.count { it.spaceId == null && floatingRect(it).overlaps(local) }
        n += hostedWidgets.count { it.spaceId == null && hostedRect(it).overlaps(local) }
        n += aiSketchWidgets.count { it.spaceId == null && aiRect(it).overlaps(local) }
        return n
    }

    fun membersOf(spaceId: String): List<SpaceMember> = buildList {
        floatingWidgets.filter { it.spaceId == spaceId }.forEach { add(SpaceMember.Floating(it)) }
        hostedWidgets.filter { it.spaceId == spaceId }.forEach { add(SpaceMember.Hosted(it)) }
        aiSketchWidgets.filter { it.spaceId == spaceId }.forEach { add(SpaceMember.Ai(it)) }
    }

    // 위젯의 잠금화면 위치를 스케치 영역 기준 상대좌표로 환산 → 공간 캔버스(dp) 좌표로 매핑.
    // 사용자가 그린 배치(상대적 위치/크기)를 공간 안에서도 그대로 유지한다.
    fun seedLayout(rectPx: Rect, local: Rect, scaleX: Float, scaleY: Float): SpaceItemLayout {
        val lw = (local.right - local.left).coerceAtLeast(1f)
        val lh = (local.bottom - local.top).coerceAtLeast(1f)
        val nx = ((rectPx.left - local.left) / lw).coerceIn(0f, 1f)
        val ny = ((rectPx.top - local.top) / lh).coerceIn(0f, 1f)
        return SpaceItemLayout(
            offset = Offset(nx * SpaceCanvas.WIDTH_DP, ny * SpaceCanvas.HEIGHT_DP),
            scaleX = scaleX,
            scaleY = scaleY,
        )
    }

    // 영역에 겹친 자유 위젯들을 공간 [id]에 편입하고, 각 멤버의 공간 캔버스 배치를 계산해 반환.
    fun assignFreeWidgetsToSpace(local: Rect, id: String): Map<String, SpaceItemLayout> {
        val layouts = linkedMapOf<String, SpaceItemLayout>()
        floatingWidgets.forEach { fw ->
            if (fw.spaceId == null && floatingRect(fw).overlaps(local)) {
                layouts[fw.uid] = seedLayout(floatingRect(fw), local, fw.scaleX, fw.scaleY)
            }
        }
        hostedWidgets.forEach { hw ->
            if (hw.spaceId == null && hostedRect(hw).overlaps(local)) {
                layouts[hw.uid] = seedLayout(hostedRect(hw), local, hw.scaleX, hw.scaleY)
            }
        }
        aiSketchWidgets.forEach { w ->
            if (w.spaceId == null && aiRect(w).overlaps(local)) {
                layouts[w.uid] = seedLayout(aiRect(w), local, w.scaleX, w.scaleY)
            }
        }
        if (layouts.isEmpty()) return emptyMap()

        val ids = layouts.keys
        floatingWidgets = floatingWidgets.map { if (it.uid in ids) it.copy(spaceId = id) else it }
        aiSketchWidgets = aiSketchWidgets.map { if (it.uid in ids) it.copy(spaceId = id) else it }
        for (i in hostedWidgets.indices) {
            if (hostedWidgets[i].uid in ids) hostedWidgets[i] = hostedWidgets[i].copy(spaceId = id)
        }
        return layouts
    }

    // 스케치 확정: target이 있으면 그 공간에 담고, 없으면 새 공간 생성
    fun confirmSpaceSketch(screenRect: Rect, targetId: String?) {
        val local = screenRectToLocal(screenRect)
        if (targetId != null) {
            val added = assignFreeWidgetsToSpace(local, targetId)
            if (added.isNotEmpty()) {
                widgetSpaces = widgetSpaces.map {
                    if (it.id == targetId) it.copy(layouts = it.layouts + added) else it
                }
            }
        } else {
            spaceCounter++
            val id = "space_$spaceCounter"
            val layouts = assignFreeWidgetsToSpace(local, id)
            if (layouts.isEmpty()) {
                spaceCounter--
                return
            }
            // 버블은 그려진 영역(로컬 좌표) 중심에 놓는다
            val bubbleWpx = with(density) { 100.dp.toPx() }
            val bubbleHpx = with(density) { 150.dp.toPx() }
            val cx = (local.left + local.right) / 2f - bubbleWpx / 2f
            val cy = (local.top + local.bottom) / 2f - bubbleHpx / 2f
            widgetSpaces = widgetSpaces + WidgetSpace(
                id = id,
                name = "공간 $spaceCounter",
                offset = Offset(cx, cy),
                layouts = layouts,
            )
        }
        spaceSketchMode = false
        spaceSketchTargetId = null
    }

    fun renameSpace(id: String, name: String) {
        widgetSpaces = widgetSpaces.map { if (it.id == id) it.copy(name = name) else it }
    }

    fun spaceMemberByUid(uid: String): SpaceMember? {
        floatingWidgets.firstOrNull { it.uid == uid }?.let { return SpaceMember.Floating(it) }
        hostedWidgets.firstOrNull { it.uid == uid }?.let { return SpaceMember.Hosted(it) }
        aiSketchWidgets.firstOrNull { it.uid == uid }?.let { return SpaceMember.Ai(it) }
        return null
    }

    // 멤버 위젯 1개를 공간에서 빼 잠금화면 자유 배치로 복귀(원래 위치 유지) + 레이아웃 제거
    fun removeMemberFromSpace(spaceId: String, uid: String) {
        floatingWidgets = floatingWidgets.map { if (it.uid == uid) it.copy(spaceId = null) else it }
        aiSketchWidgets = aiSketchWidgets.map { if (it.uid == uid) it.copy(spaceId = null) else it }
        for (i in hostedWidgets.indices) {
            if (hostedWidgets[i].uid == uid) hostedWidgets[i] = hostedWidgets[i].copy(spaceId = null)
        }
        widgetSpaces = widgetSpaces.map {
            if (it.id == spaceId) it.copy(layouts = it.layouts - uid) else it
        }
    }

    // 공간 캔버스 내 멤버 이동(델타는 캔버스 dp). 캔버스 경계 안으로 클램프.
    fun dragSpaceMember(spaceId: String, uid: String, deltaDp: Offset) {
        widgetSpaces = widgetSpaces.map { sp ->
            if (sp.id != spaceId) return@map sp
            val cur = sp.layouts[uid] ?: return@map sp
            val member = spaceMemberByUid(uid) ?: return@map sp
            val (bw, bh) = member.baseSizeDp(density.density)
            val w = bw * cur.scaleX
            val h = bh * cur.scaleY
            val nx = (cur.offset.x + deltaDp.x).coerceIn(0f, (SpaceCanvas.WIDTH_DP - w).coerceAtLeast(0f))
            val ny = (cur.offset.y + deltaDp.y).coerceIn(0f, (SpaceCanvas.HEIGHT_DP - h).coerceAtLeast(0f))
            sp.copy(layouts = sp.layouts + (uid to cur.copy(offset = Offset(nx, ny))))
        }
    }

    fun resizeSpaceMember(spaceId: String, uid: String, dSX: Float, dSY: Float, aX: Float, aY: Float) {
        widgetSpaces = widgetSpaces.map { sp ->
            if (sp.id != spaceId) return@map sp
            val cur = sp.layouts[uid] ?: return@map sp
            val member = spaceMemberByUid(uid) ?: return@map sp
            val (bw, bh) = member.baseSizeDp(density.density)
            sp.copy(layouts = sp.layouts + (uid to resizeSpaceItem(cur, bw, bh, dSX, dSY, aX, aY)))
        }
    }

    // 공간 삭제: 멤버 위젯들은 잠금화면 자유 배치로 되돌리고(원래 위치 유지) 버블만 제거
    fun deleteSpace(id: String) {
        floatingWidgets = floatingWidgets.map { if (it.spaceId == id) it.copy(spaceId = null) else it }
        aiSketchWidgets = aiSketchWidgets.map { if (it.spaceId == id) it.copy(spaceId = null) else it }
        for (i in hostedWidgets.indices) {
            if (hostedWidgets[i].spaceId == id) hostedWidgets[i] = hostedWidgets[i].copy(spaceId = null)
        }
        widgetSpaces = widgetSpaces.filter { it.id != id }
        if (expandedSpaceId == id) expandedSpaceId = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.images),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
                .then(if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier),
        )

        Box(
            modifier = Modifier.fillMaxSize()
                .graphicsLayer {
                    scaleX = scale; scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0.2f)
                }
                // graphicsLayer 안쪽이어야 자식(ghost)과 같은 pre-scale 공간이 됨
                .onGloballyPositioned { contentRootCoords = it }
                .clip(RoundedCornerShape(cornerRadius))
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        val t0 = System.currentTimeMillis()
                        val released = try {
                            withTimeout(500) { tryAwaitRelease(); true }
                        } catch (_: TimeoutCancellationException) {
                            clockOffset = savedClockOffset
                            isFloating = true; false
                        }
                        if (!isFloating && released && System.currentTimeMillis() - t0 >= 500) {
                            clockOffset = savedClockOffset
                            isFloating = true
                        }
                    })
                },
        ) {
            Image(
                painter = painterResource(id = R.drawable.images),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EditModeTopBar(
                        visible = isFloating,
                        alpha = editAlpha,
                        onConfirm = {
                            savedClockOffset = clockOffset
                            savedClockScale = clockScale
                            savedFavoriteAppsOffset = favoriteAppsOffset
                            greenBoxOffset = Offset.Zero
                            selectedFloatingUid = null
                            isFloating = false
                            llmSuggestion = null
                        },
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                    Column(
                        modifier = Modifier
                            .onGloballyPositioned { coords ->
                                clockColumnHeightDp = with(density) { coords.size.height.toDp() }
                                if (!isFloating) {
                                    val pos = coords.positionInRoot()
                                    clockNaturalPosition = Offset(
                                        pos.x + coords.size.width / 2f,
                                        pos.y + coords.size.height / 2f,
                                    )
                                }
                            }
                            .offset {
                                val comp = clockCompensation(clockNaturalPosition, scale, density.density, screenWidthPx, screenHeightPx)
                                IntOffset(
                                    (clockOffset.x + comp.x).roundToInt(),
                                    (clockOffset.y + comp.y).roundToInt(),
                                )
                            }
                            .pointerInput(isFloating) {
                                if (isFloating) detectDragGestures { change, drag ->
                                    change.consume(); clockOffset += drag
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .onGloballyPositioned { clockBoundsCoords = it }
                                .border(1.dp, Color.White.copy(alpha = 0.7f * editAlpha), RoundedCornerShape(12.dp))
                                .padding(8.dp),
                        ) {
                            ClockHeader(scale = clockScale)
                            if (isFloating) {
                                Box(modifier = Modifier.matchParentSize().graphicsLayer { alpha = editAlpha }) {
                                    ResizeHandles { dx, dy, _, _ ->
                                        val delta = (dx + dy) / 2f
                                        clockScale = (clockScale + delta).coerceIn(0.5f, 2.0f)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        WidgetSlotRow(
                            placedWidgets = slotWidgets,
                            isFloating = isFloating,
                            slotSize = slotSize,
                            slotGap = slotGap,
                            modifier = Modifier.onGloballyPositioned { slotRowCoords = it },
                            onRemove = { uid ->
                                slotWidgets = slotWidgets.filter { it.uid != uid }
                                releaseGhostFor(uid)
                            },
                            onAdd = {
                                addTarget = AddTarget.SLOT
                                showLockWidgetPicker = true
                            },
                            trayGhosts = trayGhosts,
                            consumedGhostKeys = consumedGhostKeys.toSet(),
                            onGhostTap = { ghost ->
                                addCounter++
                                val newUid = "${ghost.widget.id}_$addCounter"
                                slotWidgets = slotWidgets + PlacedWidget(uid = newUid, widget = ghost.widget)
                                consumedGhostKeys += ghost.key
                                ghostOriginByUid[newUid] = ghost.key
                            },
                        )
                    }

                }

                Spacer(Modifier.weight(1f))
            }

            if (isFloating) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onGloballyPositioned { bottomBarCoords = it }
                        .graphicsLayer { alpha = editAlpha }
                        .padding(bottom = screenHeight * 0.05f)
                        .offset { IntOffset(greenBoxOffset.x.roundToInt(), greenBoxOffset.y.roundToInt()) }
                        .pointerInput(isFloating) {
                            detectDragGestures { c, d -> c.consume(); greenBoxOffset += d }
                        }
                        .pointerInput(isFloating) {
                            detectTapGestures(onTap = { showShortcutPopup = true })
                        }
                        .clip(RoundedCornerShape(26.dp))
                        .border(1.dp, LockTokens.BorderSoft, RoundedCornerShape(26.dp)),
                ) {
                    LockStarBar()
                }
            }

            floatingWidgets.filter { it.spaceId == null }.forEach { placed ->
                FloatingWidgetItem(
                    placed = placed,
                    isFloating = isFloating,
                    isSelected = selectedFloatingUid == placed.uid,
                    onSelectToggle = {
                        selectedFloatingUid = if (selectedFloatingUid == placed.uid) null else placed.uid
                    },
                    onDrag = { drag ->
                        floatingWidgets = floatingWidgets.map {
                            if (it.uid == placed.uid) it.copy(offset = it.offset + drag) else it
                        }
                    },
                    onResize = { dx, dy, ax, ay -> resizeFloating(placed.uid, dx, dy, ax, ay) },
                    onDelete = {
                        if (selectedFloatingUid == placed.uid) selectedFloatingUid = null
                        floatingWidgets = floatingWidgets.filter { it.uid != placed.uid }
                        releaseGhostFor(placed.uid)
                    },
                )
            }

            // 자유 배치 영역 ghost: 실제 설치된 앱의 위젯만 사용 (활성 실제 앱의 추천만)
            if (isFloating) {
                val visibleRealGhosts = realFloatingGhosts.filter {
                    realWidgetGhostKey(it.provider.flattenToShortString()) !in consumedGhostKeys
                }
                val plannedGhostRects = placeGhostRects(
                    ghostSizes = visibleRealGhosts.map { ghostSizePx(it) },
                    screenWidthPx = screenWidthPx,
                    screenHeightPx = screenHeightPx,
                    occupied = currentOccupiedRects(),
                )
                visibleRealGhosts.forEachIndexed { idx, info ->
                    val component = info.provider.flattenToShortString()
                    val key = realWidgetGhostKey(component)
                    val rect = plannedGhostRects[idx]
                    val offset = Offset(rect.left, rect.top)
                    LlmRealWidgetGhost(
                        info = info,
                        offset = offset,
                        width = with(density) { (rect.right - rect.left).toDp() },
                        height = with(density) { (rect.bottom - rect.top).toDp() },
                        onTap = {
                            consumedGhostKeys += key
                            pendingRealComponents += component
                            onRealWidgetSelected(info, offset)
                        },
                    )
                }
            }

            // AI 스케치로 생성된 이미지 위젯 목록 표시
            aiSketchWidgets.filter { it.spaceId == null }.forEach { w ->
                AiSketchWidgetItem(
                    widget = w,
                    isFloating = isFloating,
                    isSelected = selectedFloatingUid == w.uid,
                    onSelectToggle = {
                        selectedFloatingUid = if (selectedFloatingUid == w.uid) null else w.uid
                    },
                    onDrag = { drag ->
                        aiSketchWidgets = aiSketchWidgets.map {
                            if (it.uid == w.uid) it.copy(offset = it.offset + drag) else it
                        }
                    },
                    onResize = { dx, dy, ax, ay -> resizeAiSketch(w.uid, dx, dy, ax, ay) },
                    onDelete = {
                        if (selectedFloatingUid == w.uid) selectedFloatingUid = null
                        aiSketchWidgets = aiSketchWidgets.filter { it.uid != w.uid }
                    },
                    onSlotClick = { slot ->
                        val sourceTag = if (slot.source == InfoSource.REAL) "REAL" else "SAMPLE"
                        val msg = "[${slot.label}] ${slot.value}  ($sourceTag)"
                        android.util.Log.d("AiSlot", msg)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                )
            }

            if (favoriteAppsEnabled && displayedFavorites.isNotEmpty()) {
                val favAlign = when (favoriteAppsLayout) {
                    FavoriteAppsLayout.BOTTOM_LEFT -> Alignment.BottomStart
                    FavoriteAppsLayout.BOTTOM_RIGHT -> Alignment.BottomEnd
                    FavoriteAppsLayout.LEFT_VERTICAL -> Alignment.CenterStart
                }
                val favPad = when (favoriteAppsLayout) {
                    FavoriteAppsLayout.BOTTOM_LEFT -> Modifier.padding(start = 16.dp, bottom = screenHeight * 0.13f)
                    FavoriteAppsLayout.BOTTOM_RIGHT -> Modifier.padding(end = 16.dp, bottom = screenHeight * 0.13f)
                    FavoriteAppsLayout.LEFT_VERTICAL -> Modifier.padding(start = 16.dp)
                }
                Box(
                    modifier = Modifier
                        .align(favAlign)
                        .then(favPad)
                        .offset { IntOffset(favoriteAppsOffset.x.roundToInt(), favoriteAppsOffset.y.roundToInt()) }
                        .then(
                            if (isFloating) Modifier
                                .graphicsLayer { alpha = editAlpha }
                                .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                .padding(6.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures { change, drag ->
                                        change.consume(); favoriteAppsOffset += drag
                                    }
                                }
                            else Modifier,
                        ),
                ) {
                    FavoriteAppsDisplay(favorites = displayedFavorites, layout = favoriteAppsLayout)
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = screenHeight * 0.04f),
            ) {
                BottomShortcutButton(
                    shortcut = leftShortcut,
                    isEditing = isFloating,
                    onClick = {
                        if (isFloating) pickingShortcutSide = ShortcutSide.LEFT
                        else leftShortcut?.let { activateShortcut(it) }
                    },
                )
                if (leftRecommendation != null && !leftShortcutRecDismissed) {
                    val applied = leftShortcut?.id == leftRecommendation.id
                    ShortcutRecommendationBadge(
                        shortcut = leftRecommendation,
                        applied = applied,
                        onToggle = {
                            leftShortcut = if (applied) null else leftRecommendation
                        },
                        onCancel = {
                            leftShortcut = preLlmLeftShortcut
                            leftShortcutRecDismissed = true
                        },
                        modifier = Modifier.offset(y = (-70).dp),
                        cancelAlignment = Alignment.TopEnd,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = screenHeight * 0.04f),
            ) {
                BottomShortcutButton(
                    shortcut = rightShortcut,
                    isEditing = isFloating,
                    onClick = {
                        if (isFloating) pickingShortcutSide = ShortcutSide.RIGHT
                        else rightShortcut?.let { activateShortcut(it) }
                    },
                )
                if (rightRecommendation != null && !rightShortcutRecDismissed) {
                    val applied = rightShortcut?.id == rightRecommendation.id
                    ShortcutRecommendationBadge(
                        shortcut = rightRecommendation,
                        applied = applied,
                        onToggle = {
                            rightShortcut = if (applied) null else rightRecommendation
                        },
                        onCancel = {
                            rightShortcut = preLlmRightShortcut
                            rightShortcutRecDismissed = true
                        },
                        modifier = Modifier.offset(y = (-70).dp),
                        cancelAlignment = Alignment.TopStart,
                    )
                }
            }

            hostedWidgets.filter { it.spaceId == null }.forEach { hosted ->
                key(hosted.uid) {
                    HostedWidgetItem(
                        hosted = hosted,
                        isFloating = isFloating,
                        isSelected = selectedFloatingUid == hosted.uid,
                        appWidgetHost = appWidgetHost,
                        onSelectToggle = {
                            selectedFloatingUid = if (selectedFloatingUid == hosted.uid) null else hosted.uid
                        },
                        onDrag = { drag ->
                            val idx = hostedWidgets.indexOfFirst { it.uid == hosted.uid }
                            if (idx != -1) {
                                val hw = hostedWidgets[idx]
                                hostedWidgets[idx] = hw.copy(offset = hw.offset + drag)
                            }
                        },
                        onResize = { dx, dy, ax, ay -> resizeHosted(hosted.uid, dx, dy, ax, ay) },
                        onDelete = {
                            if (selectedFloatingUid == hosted.uid) selectedFloatingUid = null
                            releaseGhostFor(hosted.uid)
                            onRemoveHosted(hosted.uid)
                        },
                    )
                }
            }

            // 위젯 공간(접힌 유리 버블)
            widgetSpaces.forEach { space ->
                WidgetSpaceBubble(
                    space = space,
                    members = membersOf(space.id),
                    layouts = space.layouts,
                    appWidgetHost = appWidgetHost,
                    isFloating = isFloating,
                    onTap = { expandedSpaceId = space.id },
                    onDrag = { drag ->
                        widgetSpaces = widgetSpaces.map {
                            if (it.id == space.id) it.copy(offset = it.offset + drag) else it
                        }
                    },
                    onDelete = { deleteSpace(space.id) },
                )
            }

            if (notificationMode != NotificationMode.NONE) {
                val notifTopPadding = 16.dp + screenHeight * 0.05f + clockColumnHeightDp + 12.dp
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = notifTopPadding, start = 16.dp, end = 16.dp),
                ) {
                    val onNudgeAction: (String, String, ChatMessage) -> Unit =
                        { action, roomName, message ->
                            executeChatNudgeAction(context, action, roomName, message)
                        }
                    when (notificationMode) {
                        NotificationMode.DUMMY -> ChatNotificationStack(
                            appGroups = dummyChatGroups,
                            modifier = Modifier.fillMaxWidth(),
                            onNudgeAction = onNudgeAction,
                        )
                        NotificationMode.REAL -> if (!hasNotificationPermission) {
                            NotificationPermissionBanner(
                                onOpenSettings = { openNotificationListenerSettings(context) },
                            )
                        } else {
                            ChatNotificationStack(
                                appGroups = realNotifications.toAppGroups(),
                                modifier = Modifier.fillMaxWidth(),
                                onNudgeAction = onNudgeAction,
                            )
                        }
                        NotificationMode.NONE -> Unit
                    }
                }
            }
        }

        // LockStar 바 위에 직접 카드 표시 — 화면 오버레이/애니메이션 없음
        BackHandler(enabled = showShortcutPopup) { showShortcutPopup = false }
        if (showShortcutPopup) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    // 플로팅 스케일(0.77) 여백 = 화면 17% + 8dp → 바 가리지 않는 위치
                    .padding(horizontal = screenWidth * 0.17f + 8.dp)
                    .padding(bottom = screenHeight * 0.28f),
            ) {
                ShortcutPickerDialog(
                    modifier = Modifier.fillMaxWidth(),
                    onDismiss = { showShortcutPopup = false },
                    onSelect = { choice ->
                        showShortcutPopup = false
                        when (choice) {
                            ShortcutChoice.RealWidget -> showRealWidgetPicker = true
                            ShortcutChoice.FavoriteApp -> showFavoriteSettings = true
                            ShortcutChoice.Text -> {}
                        }
                    },
                )
            }
        }

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
                                widget = widget,
                            )
                        }
                    } else {
                        floatingWidgets = floatingWidgets + FloatingWidget(
                            uid = "${widget.id}_$addCounter",
                            widget = widget,
                            offset = Offset(80f + addCounter * 20f, 280f + addCounter * 20f),
                        )
                    }
                    showLockWidgetPicker = false
                },
            )
        }

        if (showFavoriteSettings && !showFavoritePicker) {
            FavoriteAppsSettingsSheet(
                enabled = favoriteAppsEnabled,
                onEnabledChange = { favoriteAppsEnabled = it },
                favorites = favoriteApps,
                layout = favoriteAppsLayout,
                onLayoutChange = { favoriteAppsLayout = it },
                onOpenPicker = { showFavoritePicker = true },
                onDismiss = { showFavoriteSettings = false },
                usageSortEnabled = favoriteAppsUsageSort,
                onUsageSortChange = { favoriteAppsUsageSort = it },
            )
        }

        if (showFavoritePicker) {
            FavoriteAppsPickerScreen(
                initial = favoriteApps,
                onClose = { showFavoritePicker = false },
                onApply = { picked ->
                    favoriteApps = picked
                    showFavoritePicker = false
                },
            )
        }

        pickingShortcutSide?.let { side ->
            BottomShortcutPickerSheet(
                onDismiss = { pickingShortcutSide = null },
                onClear = {
                    if (side == ShortcutSide.LEFT) leftShortcut = null else rightShortcut = null
                    pickingShortcutSide = null
                },
                onSelected = { sc ->
                    if (side == ShortcutSide.LEFT) leftShortcut = sc else rightShortcut = sc
                    pickingShortcutSide = null
                },
            )
        }

        if (!isFloating && llmSuggestion == null && !sketchMode) {
            FloatingActionButton(
                onClick = { showAiChooser = true },
                containerColor = LockTokens.Accent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = screenHeight * 0.18f),
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = "AI 위젯 도우미")
            }
        }

        // 편집(float) 모드 하단 버튼 Row — floating 화면 아래 빈 공간에 배치
        if (isFloating) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = screenHeight * 0.05f)
                    .graphicsLayer { alpha = editAlpha },
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.Top,
            ) {
                if (!spaceSketchMode && expandedSpaceId == null) {
                    FloatingActionButton(
                        onClick = { spaceSketchTargetId = null; spaceSketchMode = true },
                        containerColor = LockTokens.Accent,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(42.dp),
                    ) {
                        Icon(
                            Icons.Filled.CreateNewFolder,
                            contentDescription = "위젯 공간 만들기",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                NotificationSourceButton(
                    mode = notificationMode,
                    onCycle = { notificationMode = notificationMode.next() },
                    fabSize = 42.dp,
                )
            }
        }

        if (showAiChooser) {
            AiActionPickerDialog(
                onDismiss = { showAiChooser = false },
                onSelect = { choice ->
                    showAiChooser = false
                    when (choice) {
                        AiActionChoice.LlmLayout -> showLlmSheet = true
                        AiActionChoice.Sketch -> {
                            sketchError = null
                            sketchMode = true
                        }
                    }
                },
            )
        }

        if (showLlmSheet) {
            LlmLayoutSheet(
                onDismiss = { showLlmSheet = false },
                onResult = { result ->
                    showLlmSheet = false
                    llmSuggestion = result
                },
            )
        }

        if (llmSuggestion != null) {
            LlmAppStrip(
                apps = stripEntries,
                selectedAppId = activeLlmAppId,
                onSelect = { activeLlmAppId = it },
                onClose = { llmSuggestion = null },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp, top = 60.dp, bottom = 60.dp)
                    .graphicsLayer { alpha = editAlpha },
            )
        }

        if (showRealWidgetPicker && appWidgetManager != null) {
            RealWidgetPickerSheet(
                appWidgetManager = appWidgetManager,
                onDismiss = { showRealWidgetPicker = false },
                onSelect = { info ->
                    showRealWidgetPicker = false
                    onRealWidgetSelected(info, availableOffsetFor(info))
                },
            )
        }

        pendingSketchAdjust?.let { pending ->
            SlotAdjustOverlay(
                pending = pending,
                onConfirm = { adjustedSlots ->
                    aiSketchCounter++
                    aiSketchWidgets = aiSketchWidgets + AiSketchWidget(
                        uid = "ai_sketch_$aiSketchCounter",
                        imageBitmap = pending.bitmap,
                        textSlots = adjustedSlots,
                        offset = Offset(pending.widgetRect.left, pending.widgetRect.top),
                        widthDp = pending.widthDp,
                        heightDp = pending.heightDp,
                    )
                    if (!isFloating) {
                        clockOffset = savedClockOffset
                        isFloating = true
                    }
                    pendingSketchAdjust = null
                },
                onCancel = { pendingSketchAdjust = null },
            )
        }

        if (sketchMode) {
            SketchModeOverlay(
                loading = sketchLoading,
                error = sketchError,
                onCancel = {
                    sketchMode = false
                    sketchLoading = false
                    sketchError = null
                },
                onConfirm = { rect, infoQuery, imageShape ->
                    sketchError = null
                    sketchLoading = true
                    sketchScope.launch {
                        try {
                            val aspectRatio = (rect.right - rect.left) / (rect.bottom - rect.top)

                            // 1. LLM이 자연어 정보 입력 → (레이블, 샘플값) 쌍 목록으로 파싱
                            val parsedItems = GeminiClient.parseInfoItems(infoQuery)
                            if (parsedItems.isEmpty()) {
                                sketchError = "정보 항목을 파악할 수 없어요. 다시 입력해 보세요."
                                sketchLoading = false
                                return@launch
                            }

                            // 1b. 실제 데이터 소스 해석 (배터리/시간/날짜는 실제값, 나머지는 샘플값 유지)
                            val resolvedItems = DataSourceResolver.resolve(context, parsedItems)

                            // 2. text pad 위치가 있는 위젯 스킨 장면 설계
                            val scene = GeminiClient.designSketchScene(
                                infoItems = resolvedItems,
                                imageShape = imageShape,
                                aspectRatio = aspectRatio,
                            )
                            val slots = scene.slots

                            // 3. 투명 배경 + 글자 없는 이미지 생성 (텍스트는 앱이 슬롯 위에 얹음)
                            val imageBytes = GeminiClient.generateWidgetImage(scene.imagePrompt)
                            val bitmap = withContext(Dispatchers.Default) {
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            }

                            // 4. 생성 결과를 슬롯 보정 대기 상태로 저장 — 바로 확정하지 않음
                            val widthDp = with(density) { (rect.right - rect.left).toDp().value }
                            val heightDp = with(density) { (rect.bottom - rect.top).toDp().value }
                            pendingSketchAdjust = PendingSketch(
                                bitmap = bitmap,
                                initialSlots = slots,
                                widgetRect = rect,
                                widthDp = widthDp,
                                heightDp = heightDp,
                            )
                            sketchLoading = false
                            sketchMode = false
                        } catch (t: Throwable) {
                            sketchLoading = false
                            sketchError = t.message ?: t.toString()
                        }
                    }
                },
            )
        }

        // 위젯 공간 만들기/추가 — 영역 스케치 오버레이 (float 모드에서 진입)
        if (spaceSketchMode) {
            SpaceSketchOverlay(
                countInRect = { screenRect -> freeWidgetsInRect(screenRect) },
                onCancel = { spaceSketchMode = false; spaceSketchTargetId = null },
                onConfirm = { screenRect -> confirmSpaceSketch(screenRect, spaceSketchTargetId) },
                addMode = spaceSketchTargetId != null,
            )
        }

        // 펼쳐진 위젯 공간 보기
        expandedSpaceId?.let { sid ->
            val space = widgetSpaces.firstOrNull { it.id == sid }
            if (space != null) {
                WidgetSpaceExpanded(
                    space = space,
                    members = membersOf(sid),
                    appWidgetHost = appWidgetHost,
                    isFloating = isFloating,
                    onClose = { expandedSpaceId = null },
                    onDelete = { deleteSpace(sid) },
                    onRename = { newName -> renameSpace(sid, newName) },
                    onRemoveMember = { uid -> removeMemberFromSpace(sid, uid) },
                    onDragMember = { uid, deltaDp -> dragSpaceMember(sid, uid, deltaDp) },
                    onResizeMember = { uid, dx, dy, ax, ay -> resizeSpaceMember(sid, uid, dx, dy, ax, ay) },
                    onAddWidgets = {
                        expandedSpaceId = null
                        spaceSketchTargetId = sid
                        spaceSketchMode = true
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LockScreenPreview() {
    LockScreenCopyTheme { LockScreen(onUnlock = {}) }
}

// scale 변화로 인한 시계 위치 이동을 상쇄하는 보정 offset 역산
// S = P + (N + O - P) * s = N  →  O = (N - P)(1 - s) / s
private fun clockCompensation(
    naturalPos: Offset,
    scale: Float,
    density: Float,
    screenWidthPx: Float,
    screenHeightPx: Float,
): Offset {
    if (scale == 1f) return Offset.Zero
    val pivotX = screenWidthPx * 0.5f
    val pivotY = screenHeightPx * 0.2f
    return Offset(
        x = (naturalPos.x - pivotX) * (1f - scale) / scale,
        y = (naturalPos.y - pivotY) * (1f - scale) / scale,
    )
}

@Composable
private fun EditModeTopBar(visible: Boolean, alpha: Float = 1f, onConfirm: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        EditPill(text = "배경화면", emphasized = false, enabled = visible, onClick = {})
        EditPill(text = "확인", emphasized = true, enabled = visible, onClick = onConfirm)
    }
}

/** One UI 잠금화면 편집 모드의 반투명 유리 pill 버튼. 벽지가 비치도록 translucent. */
@Composable
private fun EditPill(text: String, emphasized: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(LockTokens.ShapeXL)
            .background(if (emphasized) LockTokens.GlassWhiteStrong else LockTokens.DockBg)
            .border(1.dp, LockTokens.Border, LockTokens.ShapeXL)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = LockTokens.TextPrimary,
            fontSize = 14.sp,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
private fun NotificationSourceButton(
    mode: NotificationMode,
    onCycle: () -> Unit,
    modifier: Modifier = Modifier,
    fabSize: Dp = 48.dp,
) {
    val dummyOrange = Color(0xFFFF9500)
    val liveGreen = Color(0xFF34C759)
    val offGray = Color(0xFF8E8E93)

    val (color, label, icon) = when (mode) {
        NotificationMode.DUMMY -> Triple(dummyOrange, "더미", Icons.Filled.Science)
        NotificationMode.REAL  -> Triple(liveGreen,  "실시간", Icons.Filled.NotificationsActive)
        NotificationMode.NONE  -> Triple(offGray,    "없음", Icons.Filled.NotificationsOff)
    }

    FloatingActionButton(
        onClick = onCycle,
        containerColor = color,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = modifier.size(fabSize),
    ) {
        Icon(icon, contentDescription = "알림 모드 전환", modifier = Modifier.size(20.dp))
    }
}

private enum class NotificationMode {
    DUMMY, REAL, NONE;
    fun next() = entries[(ordinal + 1) % entries.size]
}
