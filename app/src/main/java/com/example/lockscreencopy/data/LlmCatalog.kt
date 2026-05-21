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

data class LlmAppEntry(
    val id: String,
    val name: String,
    val kind: Kind,
) {
    enum class Kind { WIDGET_APP, REAL_WIDGET_APP, SYSTEM_SHORTCUT, INSTALLED_APP }
}

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
    fun firstStepEntries(): List<LlmAppEntry> {
        val out = ArrayList<LlmAppEntry>(
            widgetApps.size + realApps.size + systemShortcuts.size + installedApps.size,
        )
        widgetApps.forEach { out += LlmAppEntry(it.id, it.name, LlmAppEntry.Kind.WIDGET_APP) }
        realApps.forEach {
            out += LlmAppEntry(it.firstStepId, it.appLabel, LlmAppEntry.Kind.REAL_WIDGET_APP)
        }
        systemShortcuts.forEach { out += LlmAppEntry(it.id, it.label, LlmAppEntry.Kind.SYSTEM_SHORTCUT) }
        installedApps.forEach { out += LlmAppEntry(it.id, it.label, LlmAppEntry.Kind.INSTALLED_APP) }
        return out
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
