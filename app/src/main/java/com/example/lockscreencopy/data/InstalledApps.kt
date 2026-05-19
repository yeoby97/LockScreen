package com.example.lockscreencopy.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.lockscreencopy.model.BottomShortcut

fun loadInstalledApps(context: Context): List<BottomShortcut.App> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val resolveInfos = pm.queryIntentActivities(intent, 0)
    val seen = HashSet<String>()
    val self = context.packageName
    return resolveInfos
        .mapNotNull { ri ->
            val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
            if (pkg == self) return@mapNotNull null
            if (!seen.add(pkg)) return@mapNotNull null
            val label = ri.loadLabel(pm)?.toString() ?: pkg
            val drawable = runCatching { ri.loadIcon(pm) }.getOrNull()
            BottomShortcut.App(
                id = "app_$pkg",
                label = label,
                packageName = pkg,
                drawable = drawable,
            )
        }
        .sortedBy { it.label.lowercase() }
}

fun launchAppShortcut(context: Context, app: BottomShortcut.App) {
    val pm = context.packageManager
    val intent = pm.getLaunchIntentForPackage(app.packageName)
    if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
