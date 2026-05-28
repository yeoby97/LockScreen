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
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.R
import com.example.lockscreencopy.model.AddTarget
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.FavoriteAppsLayout
import com.example.lockscreencopy.model.FloatingWidget
import com.example.lockscreencopy.model.HostedAppWidget
import com.example.lockscreencopy.model.NudgeDisplayMode
import com.example.lockscreencopy.model.PlacedWidget
import com.example.lockscreencopy.model.WidgetSize
import com.example.lockscreencopy.data.GeminiClient
import com.example.lockscreencopy.data.LlmCatalog
import com.example.lockscreencopy.data.RealAppWidgets
import com.example.lockscreencopy.data.buildLlmCatalog
import com.example.lockscreencopy.data.handleSystemAction
import com.example.lockscreencopy.data.hasUsageStatsPermission
import com.example.lockscreencopy.data.launchAppShortcut
import com.example.lockscreencopy.data.loadInstalledApps
import com.example.lockscreencopy.data.loadWeeklyUsage
import com.example.lockscreencopy.data.NotificationRepository
import com.example.lockscreencopy.data.isNotificationListenerEnabled
import com.example.lockscreencopy.data.openNotificationListenerSettings
import com.example.lockscreencopy.data.openUsageAccessSettings
import com.example.lockscreencopy.data.sampleNotifications
import com.example.lockscreencopy.data.topUsedAppsWithGap
import com.example.lockscreencopy.ui.notification.NotificationPermissionBanner
import com.example.lockscreencopy.ui.notification.NudgeNotificationDisplay
import com.example.lockscreencopy.ui.llm.GhostInstance
import com.example.lockscreencopy.ui.llm.LlmAppStrip
import com.example.lockscreencopy.ui.llm.LlmRealWidgetGhost
import com.example.lockscreencopy.ui.llm.RectBounds
import com.example.lockscreencopy.ui.llm.ShortcutRecommendationBadge
import com.example.lockscreencopy.ui.llm.StripAppEntry
import com.example.lockscreencopy.ui.llm.placeGhostRects
import com.example.lockscreencopy.ui.llm.realWidgetGhostKey
import com.example.lockscreencopy.ui.sketch.SketchModeOverlay
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.math.roundToInt

private enum class ShortcutSide { LEFT, RIGHT }

private data class SketchCandidate(
    val packageName: String,
    val appLabel: String,
    val appIcon: android.graphics.drawable.Drawable?,
    val provider: AppWidgetProviderInfo,
)

private data class SketchSuggestion(
    val rect: Rect,
    val userQuery: String,
    val candidates: List<SketchCandidate>,
)

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
    val dummyNotifications = remember { sampleNotifications() }
    var useDummyNotifications by remember { mutableStateOf(false) }
    val notifications = if (useDummyNotifications) dummyNotifications else realNotifications
    var nudgeDisplayMode by remember { mutableStateOf(NudgeDisplayMode.CARD) }

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
    var sketchSuggestion by remember { mutableStateOf<SketchSuggestion?>(null) }
    var sketchActiveAppId by remember(sketchSuggestion) { mutableStateOf<String?>(null) }
    var sketchCatalog by remember { mutableStateOf<LlmCatalog?>(null) }
    var sketchPendingComponent by remember { mutableStateOf<String?>(null) }
    val sketchScope = rememberCoroutineScope()

    LaunchedEffect(sketchMode) {
        if (sketchMode && sketchCatalog == null) {
            sketchCatalog = withContext(Dispatchers.IO) { buildLlmCatalog(context) }
        }
    }

    var addCounter by remember { mutableStateOf(0) }

    var clockOffset by remember { mutableStateOf(Offset.Zero) }
    var clockNaturalPosition by remember { mutableStateOf(Offset.Zero) }

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
        sketchSuggestion = null
        sketchActiveAppId = null
    }

    LaunchedEffect(llmSuggestion) {
        if (llmSuggestion != null && !isFloating) {
            clockOffset = savedClockOffset
            isFloating = true
        }
    }

    LaunchedEffect(sketchSuggestion) {
        if (sketchSuggestion != null && !isFloating) {
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
            if (sketchPendingComponent == comp) sketchPendingComponent = null
        }
        cancelledRealComponents.clear()
    }

    // 스케치로 추가한 위젯이 실제로 hostedWidgets 에 들어왔으면 자동으로 편집(floating) 모드로 전환
    // 해서 사용자가 바로 크기 조절/삭제할 수 있게 한다.
    LaunchedEffect(hostedWidgets.size, sketchPendingComponent) {
        val pending = sketchPendingComponent ?: return@LaunchedEffect
        val just = hostedWidgets.firstOrNull {
            it.providerInfo.provider.flattenToShortString() == pending
        } ?: return@LaunchedEffect
        sketchPendingComponent = null
        if (!isFloating) {
            clockOffset = savedClockOffset
            isFloating = true
        }
        selectedFloatingUid = just.uid
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

    val scale by animateFloatAsState(
        targetValue = if (isFloating) 0.7f else 1f,
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
                verticalArrangement = Arrangement.SpaceBetween,
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
                            sketchSuggestion = null
                            sketchActiveAppId = null
                        },
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                    Column(
                        modifier = Modifier
                            .onGloballyPositioned { coords ->
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

                Spacer(modifier = Modifier.height(12.dp))
                if (!hasNotificationPermission) {
                    NotificationPermissionBanner(
                        onOpenSettings = { openNotificationListenerSettings(context) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    NudgeNotificationDisplay(
                        notifications = notifications,
                        mode = nudgeDisplayMode,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (isFloating) {
                    Box(
                        modifier = Modifier
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
                            .clip(RoundedCornerShape(30.dp))
                            .border(2.dp, Color.LightGray, RoundedCornerShape(30.dp)),
                    ) {
                        LockStarBar()
                    }
                }
            }

            floatingWidgets.forEach { placed ->
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

            // 스케치로 그린 사각형 안에 추천 ghost 표시. 스케일 박스 내부라서 floating
            // 모드 진입 시 자동으로 0.7배로 축소되어 다른 위젯들과 같은 비율로 보임.
            sketchSuggestion?.let { s ->
                val active = sketchActiveAppId?.let { id ->
                    s.candidates.firstOrNull { it.packageName == id }
                } ?: s.candidates.first()
                val offset = Offset(s.rect.left, s.rect.top)
                LlmRealWidgetGhost(
                    info = active.provider,
                    offset = offset,
                    width = with(density) { (s.rect.right - s.rect.left).toDp() },
                    height = with(density) { (s.rect.bottom - s.rect.top).toDp() },
                    onTap = {
                        val component = active.provider.provider.flattenToShortString()
                        sketchSuggestion = null
                        sketchActiveAppId = null
                        sketchPendingComponent = component
                        onRealWidgetSelected(active.provider, offset)
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

            hostedWidgets.forEach { hosted ->
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
        }

        if (showShortcutPopup) {
            ShortcutPickerDialog(
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

        if (!isFloating && llmSuggestion == null && !sketchMode && sketchSuggestion == null) {
            FloatingActionButton(
                onClick = { showAiChooser = true },
                containerColor = Color(0xFF4DAAED),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = screenHeight * 0.18f),
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = "AI 위젯 도우미")
            }
        }

        if (isFloating) {
            NudgeDisplayModeButton(
                currentMode = nudgeDisplayMode,
                onModeChange = { nudgeDisplayMode = it },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = screenHeight * 0.28f)
                    .graphicsLayer { alpha = editAlpha },
            )
            NotificationSourceButton(
                useDummy = useDummyNotifications,
                onToggle = { useDummyNotifications = !useDummyNotifications },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = screenHeight * 0.40f)
                    .graphicsLayer { alpha = editAlpha },
            )
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

        sketchSuggestion?.let { s ->
            val active = sketchActiveAppId?.let { id ->
                s.candidates.firstOrNull { it.packageName == id }
            } ?: s.candidates.first()
            LlmAppStrip(
                apps = s.candidates.map { c ->
                    StripAppEntry(
                        id = c.packageName,
                        name = c.appLabel,
                        iconBg = Color(0xFF424242),
                        iconBitmap = c.appIcon?.toBitmapSafe(),
                        realPackages = listOf(c.packageName),
                    )
                },
                selectedAppId = active.packageName,
                onSelect = { pkg -> sketchActiveAppId = pkg ?: s.candidates.first().packageName },
                onClose = {
                    sketchSuggestion = null
                    sketchActiveAppId = null
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp, top = 60.dp, bottom = 60.dp)
                    .graphicsLayer { alpha = editAlpha },
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
                onConfirm = { rect, query ->
                    val cat = sketchCatalog
                    if (cat == null) {
                        sketchError = "카탈로그를 불러오는 중입니다. 잠시 후 다시 시도하세요."
                        return@SketchModeOverlay
                    }
                    sketchError = null
                    sketchLoading = true
                    sketchScope.launch {
                        try {
                            val ids = GeminiClient.recommendApps(query, cat.firstStepEntries())
                            val selected = cat.resolveSelectedApps(ids)
                            val pmRef = context.packageManager
                            val floatingPrompt = selected.floatingEntriesForPrompt(pmRef)
                            if (floatingPrompt.isEmpty()) {
                                sketchError = "AI가 추천한 실제 위젯이 없어요. 더 구체적으로 입력해 보세요."
                                sketchLoading = false
                                return@launch
                            }
                            val rec = GeminiClient.recommendWidgets(
                                userQuery = query,
                                trayCandidates = emptyList(),
                                floatingCandidates = floatingPrompt,
                                shortcutCandidates = emptyList(),
                            )
                            val recComponents = rec.floating.toHashSet()
                            val picked: List<SketchCandidate> = selected.realApps.flatMap { ra: RealAppWidgets ->
                                ra.providers
                                    .filter { it.provider.flattenToShortString() in recComponents }
                                    .map { info ->
                                        SketchCandidate(
                                            packageName = ra.packageName,
                                            appLabel = ra.appLabel,
                                            appIcon = ra.appIcon,
                                            provider = info,
                                        )
                                    }
                            }.distinctBy { it.packageName }
                            if (picked.isEmpty()) {
                                sketchError = "추천된 위젯을 찾을 수 없어요."
                                sketchLoading = false
                                return@launch
                            }
                            sketchSuggestion = SketchSuggestion(
                                rect = rect,
                                userQuery = query,
                                candidates = picked,
                            )
                            sketchActiveAppId = picked.first().packageName
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
        Button(onClick = {}, enabled = visible) { Text("배경화면") }
        Button(onClick = onConfirm, enabled = visible) { Text("확인") }
    }
}

@Composable
private fun NudgeDisplayModeButton(
    currentMode: NudgeDisplayMode,
    onModeChange: (NudgeDisplayMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nextMode = when (currentMode) {
        NudgeDisplayMode.CARD -> NudgeDisplayMode.ICON
        NudgeDisplayMode.ICON -> NudgeDisplayMode.DOT
        NudgeDisplayMode.DOT  -> NudgeDisplayMode.CARD
    }
    val modeLabel = when (currentMode) {
        NudgeDisplayMode.CARD -> "카드"
        NudgeDisplayMode.ICON -> "아이콘"
        NudgeDisplayMode.DOT  -> "점"
    }
    val nudgePurple = Color(0xFF9B78FF)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FloatingActionButton(
            onClick = { onModeChange(nextMode) },
            containerColor = nudgePurple,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = "넛지 알림 표시 방식")
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                text = "넛지: $modeLabel",
                color = Color.White,
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun NotificationSourceButton(
    useDummy: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dummyOrange = Color(0xFFFF9500)
    val liveGreen = Color(0xFF34C759)
    val color = if (useDummy) dummyOrange else liveGreen
    val label = if (useDummy) "더미" else "실시간"
    val icon = if (useDummy) Icons.Filled.Science else Icons.Filled.NotificationsActive

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FloatingActionButton(
            onClick = onToggle,
            containerColor = color,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(icon, contentDescription = "알림 소스 전환")
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 10.sp,
            )
        }
    }
}
