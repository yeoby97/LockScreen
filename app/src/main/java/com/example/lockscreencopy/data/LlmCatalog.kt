package com.example.lockscreencopy.data

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.LockWidget
import com.example.lockscreencopy.model.WidgetApp
import com.example.lockscreencopy.model.WidgetSize

data class RealAppWidgets(
    val packageName: String,
    val appLabel: String,
    val appIcon: Drawable?,
    val providers: List<AppWidgetProviderInfo>,
) {
    /** 첫 단계용 ID(앱 단위) — 예: "real:com.google.android.calendar" */
    val firstStepId: String get() = "real:$packageName"
}

data class RealWidgetEntry(
    val component: String,    // info.provider.flattenToShortString()
    val label: String,
    val packageName: String,
    val appLabel: String,
    val sizeText: String,
)

data class LlmCatalog(
    val widgetApps: List<WidgetApp>,
    val realApps: List<RealAppWidgets>,
    val systemShortcuts: List<BottomShortcut.System>,
    val installedApps: List<BottomShortcut.App>,
) {
    /**
     * 첫 단계(앱 선별) 없이 전체 카탈로그를 위젯 후보로 그대로 펼친다.
     * LLM 에게 모든 위젯을 한 번에 보여 주고 위젯 단위로 직접 고르게 하기 위함.
     */
    fun allCandidates(): SelectedFirstStep =
        SelectedFirstStep(widgetApps, realApps, systemShortcuts, installedApps)

    /**
     * 위젯 단위 추천 결과(LlmRecommendation)를 실제 앱/위젯 객체로 환원한다.
     * 추천에 포함된 트레이 위젯/실제 위젯 component/바로가기 id 를 가진 항목만 남긴다.
     */
    fun resolveRecommendation(rec: LlmRecommendation): SelectedFirstStep {
        val trayIds = rec.tray.toHashSet()
        val floatingComponents = rec.floating.toHashSet()
        val shortcutIds = listOfNotNull(rec.left, rec.right).toHashSet()
        val apps = widgetApps.filter { app -> app.widgets.any { it.id in trayIds } }
        val reals = realApps.filter { ra ->
            ra.providers.any { it.provider.flattenToShortString() in floatingComponents }
        }
        val sys = systemShortcuts.filter { it.id in shortcutIds }
        val inst = installedApps.filter { it.id in shortcutIds }
        return SelectedFirstStep(apps, reals, sys, inst)
    }

    fun resolveSelectedApps(ids: List<String>): SelectedFirstStep {
        val idSet = ids.toHashSet()
        val apps = widgetApps.filter { it.id in idSet }
        val reals = realApps.filter { it.firstStepId in idSet }
        val sys = systemShortcuts.filter { it.id in idSet }
        val inst = installedApps.filter { it.id in idSet }
        return SelectedFirstStep(apps, reals, sys, inst)
    }
}

data class SelectedFirstStep(
    val widgetApps: List<WidgetApp>,
    val realApps: List<RealAppWidgets> = emptyList(),
    val systemShortcuts: List<BottomShortcut.System>,
    val installedApps: List<BottomShortcut.App>,
) {
    /** 트레이 후보: mock LockWidget (권한 없는 영역이므로 더미 사용) */
    fun trayCandidates(): List<LockWidget> = widgetApps.flatMap { it.widgets }

    /** 자유 배치 후보: 실제 설치된 앱의 AppWidgetProviderInfo */
    fun floatingCandidates(): List<AppWidgetProviderInfo> = realApps.flatMap { it.providers }

    fun shortcutCandidates(): List<BottomShortcut> {
        val out = ArrayList<BottomShortcut>(systemShortcuts.size + installedApps.size)
        out.addAll(systemShortcuts)
        out.addAll(installedApps)
        return out
    }

    /** Gemini 프롬프트용으로 라벨/크기를 미리 풀어둔 자유 후보 */
    fun floatingEntriesForPrompt(pm: PackageManager): List<RealWidgetEntry> =
        realApps.flatMap { ra ->
            ra.providers.map { info ->
                val label = runCatching { info.loadLabel(pm).toString() }
                    .getOrDefault(info.provider.shortClassName)
                RealWidgetEntry(
                    component = info.provider.flattenToShortString(),
                    label = label,
                    packageName = ra.packageName,
                    appLabel = ra.appLabel,
                    sizeText = "${info.minWidth}x${info.minHeight}",
                )
            }
        }
}

suspend fun buildLlmCatalog(context: Context): LlmCatalog {
    val installed = runCatching { loadInstalledApps(context) }.getOrElse { emptyList() }
    val pm = context.packageManager
    val realProviders = runCatching {
        AppWidgetManager.getInstance(context).installedProviders
    }.getOrElse { emptyList() }
    val realApps = realProviders
        .groupBy { it.provider.packageName }
        .map { (pkg, infos) ->
            val (label, icon) = runCatching {
                val ai = pm.getApplicationInfo(pkg, 0)
                pm.getApplicationLabel(ai).toString() to
                    runCatching { pm.getApplicationIcon(ai) }.getOrNull()
            }.getOrDefault(pkg to null)
            RealAppWidgets(pkg, label, icon, infos)
        }
        .sortedBy { it.appLabel.lowercase() }
    return LlmCatalog(
        widgetApps = lockWidgetApps,
        realApps = realApps,
        systemShortcuts = systemShortcuts,
        installedApps = installed,
    )
}

fun LockWidget.spanCount(): Int = if (size == WidgetSize.WIDE) 2 else 1
