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
import com.example.lockscreencopy.ui.theme.LockScreenCopyTheme

class LockScreenActivity : ComponentActivity() {
    private lateinit var keyguardManager: KeyguardManager
    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetHost: AppWidgetHost

    private val hostedWidgets: SnapshotStateList<HostedAppWidget> = mutableStateListOf()

    // AI 추천에서 실제 앱 위젯을 여러 개 한 번에 선택할 수 있으므로 순차 처리용 큐
    private val pendingProviderQueue = ArrayDeque<AppWidgetProviderInfo>()
    private var widgetFlowInProgress = false

    private val bindWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val id = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

        if (result.resultCode == Activity.RESULT_OK && id != -1) {
            configureOrAdd(id)
        } else {
            if (id != -1) appWidgetHost.deleteAppWidgetId(id)
            finishWidgetFlow()
        }
    }

    private val configureWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val id = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

        if (result.resultCode == Activity.RESULT_OK && id != -1) {
            addHostedWidget(id)
        } else {
            if (id != -1) appWidgetHost.deleteAppWidgetId(id)
        }

        finishWidgetFlow()
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    LockScreen(
                        onUnlock = { unlockDevice() },
                        hostedWidgets = hostedWidgets,
                        appWidgetHost = appWidgetHost,
                        appWidgetManager = appWidgetManager,
                        onRealWidgetSelected = ::onProviderSelected,
                        onRemoveHosted = ::removeHosted,
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            appWidgetHost.startListening()
        } catch (_: Exception) {
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            appWidgetHost.stopListening()
        } catch (_: Exception) {
        }
    }

    private fun unlockDevice() {
        Toast.makeText(this, "Device unlocked!", Toast.LENGTH_SHORT).show()
    }

    private fun resolveWidgetSizeDp(info: AppWidgetProviderInfo): Pair<Int, Int> {
        val d = resources.displayMetrics.density
        val cellDp = 70

        var wDp = (info.minWidth / d).toInt()
        var hDp = (info.minHeight / d).toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (info.targetCellWidth > 0) wDp = maxOf(wDp, info.targetCellWidth * cellDp)
            if (info.targetCellHeight > 0) hDp = maxOf(hDp, info.targetCellHeight * cellDp)
        }

        if (wDp <= 0) wDp = (info.minResizeWidth / d).toInt()
        if (hDp <= 0) hDp = (info.minResizeHeight / d).toInt()

        if (wDp <= 0) wDp = 110
        if (hDp <= 0) hDp = 110

        return wDp to hDp
    }

    private fun sizeOptionsBundle(minWdp: Int, minHdp: Int): Bundle = Bundle().apply {
        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, minWdp)
        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, minHdp)
        putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minWdp)
        putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minHdp)
    }

    private fun onProviderSelected(info: AppWidgetProviderInfo) {
        if (widgetFlowInProgress) {
            pendingProviderQueue.addLast(info)
            return
        }

        widgetFlowInProgress = true
        startProviderFlow(info)
    }

    private fun startProviderFlow(info: AppWidgetProviderInfo) {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        val (minWdp, minHdp) = resolveWidgetSizeDp(info)
        val options = sizeOptionsBundle(minWdp, minHdp)

        val bound = try {
            appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider, options)
        } catch (_: Exception) {
            false
        }

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
            finishWidgetFlow()
        }
    }

    private fun configureOrAdd(appWidgetId: Int) {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: run {
            appWidgetHost.deleteAppWidgetId(appWidgetId)
            finishWidgetFlow()
            return
        }

        val configure = info.configure

        if (configure == null) {
            addHostedWidget(appWidgetId)
            finishWidgetFlow()
            return
        }

        val configureIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
            component = configure
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        try {
            configureWidgetLauncher.launch(configureIntent)
        } catch (_: Exception) {
            addHostedWidget(appWidgetId)
            finishWidgetFlow()
        }
    }

    private fun addHostedWidget(appWidgetId: Int) {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return
        val (minWdp, minHdp) = resolveWidgetSizeDp(info)
        val options = sizeOptionsBundle(minWdp, minHdp)

        try {
            appWidgetManager.updateAppWidgetOptions(appWidgetId, options)
        } catch (_: Exception) {
        }

        val d = resources.displayMetrics.density

        hostedWidgets.add(
            HostedAppWidget(
                uid = "hosted_${appWidgetId}_${System.currentTimeMillis()}",
                appWidgetId = appWidgetId,
                providerInfo = info,
                widthPx = (minWdp * d).toInt(),
                heightPx = (minHdp * d).toInt(),
                offset = Offset(200f + hostedWidgets.size * 30f, 600f + hostedWidgets.size * 30f),
            ),
        )
    }

    private fun finishWidgetFlow() {
        widgetFlowInProgress = false

        if (pendingProviderQueue.isNotEmpty()) {
            val next = pendingProviderQueue.removeFirst()
            onProviderSelected(next)
        }
    }

    private fun removeHosted(uid: String) {
        val item = hostedWidgets.firstOrNull { it.uid == uid } ?: return
        appWidgetHost.deleteAppWidgetId(item.appWidgetId)
        hostedWidgets.remove(item)
    }

    companion object {
        const val HOST_ID = 1024
    }
}
