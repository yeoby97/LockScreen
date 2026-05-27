package com.example.lockscreencopy

import android.app.Activity
import android.app.KeyguardManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.example.lockscreencopy.model.HostedAppWidget
import com.example.lockscreencopy.ui.LockScreen
import com.example.lockscreencopy.ui.resolveWidgetSizeDp
import com.example.lockscreencopy.ui.theme.LockScreenCopyTheme

class LockScreenActivity : ComponentActivity() {
    private lateinit var keyguardManager: KeyguardManager
    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetHost: AppWidgetHost

    private val hostedWidgets: SnapshotStateList<HostedAppWidget> = mutableStateListOf()
    private val pendingOffsetsByWidgetId = mutableMapOf<Int, Offset>()
    private val pendingComponentsByWidgetId = mutableMapOf<Int, String>()
    // 바인딩 거부로 취소된 위젯 component (LockScreen 이 관찰해 추천 ghost 복원)
    private val cancelledComponents: SnapshotStateList<String> = mutableStateListOf()
    // bind/configure 화면으로 넘어간 위젯 id. 취소 시 result.data 가 null 이라 이 값으로 복구한다.
    private var inFlightWidgetId: Int = -1

    private val bindWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val id = resolveResultWidgetId(result.data)
        inFlightWidgetId = -1
        if (id == -1) return@registerForActivityResult
        if (result.resultCode == Activity.RESULT_OK) configureOrAdd(id)
        else cancelPending(id)
    }

    private val configureWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val id = resolveResultWidgetId(result.data)
        inFlightWidgetId = -1
        if (id == -1) return@registerForActivityResult
        if (result.resultCode == Activity.RESULT_OK) addHostedWidget(id)
        else cancelPending(id)
    }

    // 취소 시 result.data 가 null 이므로 진행 중 id 로 폴백한다.
    private fun resolveResultWidgetId(data: Intent?): Int {
        val fromData = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        return if (fromData != -1) fromData else inFlightWidgetId
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, HOST_ID)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
        setContent {
            LockScreenCopyTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LockScreen(
                        onUnlock = { unlockDevice() },
                        hostedWidgets = hostedWidgets,
                        appWidgetHost = appWidgetHost,
                        appWidgetManager = appWidgetManager,
                        onRealWidgetSelected = ::onProviderSelected,
                        onRemoveHosted = ::removeHosted,
                        cancelledRealComponents = cancelledComponents,
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

    private fun resolveWidgetSizeDp(info: AppWidgetProviderInfo): Pair<Int, Int> =
        resolveWidgetSizeDp(info, resources.displayMetrics.density)

    private fun sizeOptionsBundle(minWdp: Int, minHdp: Int): Bundle = Bundle().apply {
        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, minWdp)
        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, minHdp)
        putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minWdp)
        putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minHdp)
    }

    private fun onProviderSelected(info: AppWidgetProviderInfo, preferredOffset: Offset) {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        pendingOffsetsByWidgetId[appWidgetId] = preferredOffset
        pendingComponentsByWidgetId[appWidgetId] = info.provider.flattenToShortString()
        val (minWdp, minHdp) = resolveWidgetSizeDp(info)
        val options = sizeOptionsBundle(minWdp, minHdp)

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
            inFlightWidgetId = appWidgetId
            bindWidgetLauncher.launch(bindIntent)
        } catch (_: ActivityNotFoundException) {
            inFlightWidgetId = -1
            Toast.makeText(this, "위젯 바인딩을 처리할 수 없습니다", Toast.LENGTH_SHORT).show()
            cancelPending(appWidgetId)
        }
    }

    private fun configureOrAdd(appWidgetId: Int) {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: run {
            cancelPending(appWidgetId); return
        }
        val configure = info.configure
        if (configure == null) {
            addHostedWidget(appWidgetId)
            return
        }
        val configureIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
            component = configure
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        try {
            inFlightWidgetId = appWidgetId
            configureWidgetLauncher.launch(configureIntent)
        } catch (_: Exception) {
            inFlightWidgetId = -1
            addHostedWidget(appWidgetId)
        }
    }

    private fun addHostedWidget(appWidgetId: Int) {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return
        val (minWdp, minHdp) = resolveWidgetSizeDp(info)
        val options = sizeOptionsBundle(minWdp, minHdp)
        try { appWidgetManager.updateAppWidgetOptions(appWidgetId, options) } catch (_: Exception) {}
        val d = resources.displayMetrics.density
        val preferredOffset = pendingOffsetsByWidgetId.remove(appWidgetId) ?: Offset(200f, 600f)
        pendingComponentsByWidgetId.remove(appWidgetId)
        hostedWidgets.add(
            HostedAppWidget(
                uid = "hosted_${appWidgetId}_${System.currentTimeMillis()}",
                appWidgetId = appWidgetId,
                providerInfo = info,
                widthPx = (minWdp * d).toInt(),
                heightPx = (minHdp * d).toInt(),
                offset = preferredOffset,
            ),
        )
    }

    // 바인딩/설정 취소 시 위젯 ID 정리 + 소비됐던 추천 ghost 복원 신호
    private fun cancelPending(appWidgetId: Int) {
        pendingComponentsByWidgetId.remove(appWidgetId)?.let { cancelledComponents.add(it) }
        pendingOffsetsByWidgetId.remove(appWidgetId)
        appWidgetHost.deleteAppWidgetId(appWidgetId)
    }

    private fun removeHosted(uid: String) {
        val item = hostedWidgets.firstOrNull { it.uid == uid } ?: return
        appWidgetHost.deleteAppWidgetId(item.appWidgetId)
        hostedWidgets.remove(item)
    }

    companion object { const val HOST_ID = 1024 }
}
