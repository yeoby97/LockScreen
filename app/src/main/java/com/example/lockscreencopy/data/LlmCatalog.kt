package com.example.lockscreencopy.data

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.LockWidget
import com.example.lockscreencopy.model.WidgetApp
import com.example.lockscreencopy.model.WidgetSize

data class LlmAppEntry(
    val id: String,
    val name: String,
    val kind: Kind,
) {
    enum class Kind { WIDGET_APP, SYSTEM_SHORTCUT, INSTALLED_APP }
}

data class LlmCatalog(
    val widgetApps: List<WidgetApp>,
    val systemShortcuts: List<BottomShortcut.System>,
    val installedApps: List<BottomShortcut.App>,
    val realWidgetProviders: List<AppWidgetProviderInfo> = emptyList(),
) {
    fun firstStepEntries(): List<LlmAppEntry> {
        val out = ArrayList<LlmAppEntry>(widgetApps.size + systemShortcuts.size + installedApps.size)
        widgetApps.forEach { out += LlmAppEntry(it.id, it.name, LlmAppEntry.Kind.WIDGET_APP) }
        systemShortcuts.forEach { out += LlmAppEntry(it.id, it.label, LlmAppEntry.Kind.SYSTEM_SHORTCUT) }
        installedApps.forEach { out += LlmAppEntry(it.id, it.label, LlmAppEntry.Kind.INSTALLED_APP) }
        return out
    }

    fun resolveSelectedApps(ids: List<String>): SelectedFirstStep {
        val idSet = ids.toHashSet()
        val apps = widgetApps.filter { it.id in idSet }
        val sys = systemShortcuts.filter { it.id in idSet }
        val inst = installedApps.filter { it.id in idSet }
        return SelectedFirstStep(apps, sys, inst)
    }
}

data class SelectedFirstStep(
    val widgetApps: List<WidgetApp>,
    val systemShortcuts: List<BottomShortcut.System>,
    val installedApps: List<BottomShortcut.App>,
) {
    fun trayCandidates(): List<LockWidget> = widgetApps.flatMap { it.widgets }
    fun floatingCandidates(): List<LockWidget> = widgetApps.flatMap { it.widgets }
    fun shortcutCandidates(): List<BottomShortcut> {
        val out = ArrayList<BottomShortcut>(systemShortcuts.size + installedApps.size)
        out.addAll(systemShortcuts)
        out.addAll(installedApps)
        return out
    }
}

suspend fun buildLlmCatalog(context: Context): LlmCatalog {
    val installed = runCatching { loadInstalledApps(context) }.getOrElse { emptyList() }
    val realWidgets = runCatching {
        AppWidgetManager.getInstance(context).installedProviders
    }.getOrElse { emptyList() }
    return LlmCatalog(
        widgetApps = lockWidgetApps,
        systemShortcuts = systemShortcuts,
        installedApps = installed,
        realWidgetProviders = realWidgets,
    )
}

fun LockWidget.spanCount(): Int = if (size == WidgetSize.WIDE) 2 else 1
