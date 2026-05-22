package com.example.lockscreencopy.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.example.lockscreencopy.model.BottomShortcut
import java.util.concurrent.TimeUnit

private const val MAX_FAVORITES_DISPLAY = 6
private const val MIN_FAVORITES_KEEP = 3
private const val GAP_RATIO = 0.4

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
        ?: return false
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

fun openUsageAccessSettings(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

fun loadWeeklyUsage(context: Context): Map<String, Long> {
    val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        ?: return emptyMap()
    val now = System.currentTimeMillis()
    val weekAgo = now - TimeUnit.DAYS.toMillis(7)
    val stats = runCatching {
        usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, weekAgo, now)
    }.getOrNull() ?: return emptyMap()
    val out = HashMap<String, Long>()
    for (s in stats) {
        val pkg = s.packageName ?: continue
        if (s.totalTimeInForeground <= 0L) continue
        out[pkg] = (out[pkg] ?: 0L) + s.totalTimeInForeground
    }
    return out
}

fun sortFavoritesByUsageWithGap(
    favorites: List<BottomShortcut>,
    usage: Map<String, Long>,
): List<BottomShortcut> {
    if (favorites.isEmpty()) return favorites

    val withUsage = favorites.map { sc ->
        val time = when (sc) {
            is BottomShortcut.App -> usage[sc.packageName] ?: 0L
            is BottomShortcut.System -> 0L
        }
        sc to time
    }.sortedByDescending { it.second }

    val sorted = withUsage.map { it.first }
    val times = withUsage.map { it.second }

    if (times.firstOrNull() == 0L) return favorites

    val cap = minOf(MAX_FAVORITES_DISPLAY, sorted.size)
    val minKeep = minOf(MIN_FAVORITES_KEEP, sorted.size)

    var cut = cap
    for (i in minKeep until cap) {
        val prev = times[i - 1]
        val cur = times[i]
        if (prev <= 0L || cur < prev * GAP_RATIO) {
            cut = i
            break
        }
    }
    return sorted.take(cut)
}
