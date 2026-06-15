package com.example.lockscreencopy.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.lockscreencopy.model.InfoSource
import com.example.lockscreencopy.model.ResolvedInfoItem
import java.util.Calendar

/**
 * 사용자 정보 항목 레이블별로 실제 시스템 값을 가져올 수 있으면 REAL,
 * 아직 연동이 없거나 권한이 없는 항목은 기존 샘플 값을 SAMPLE로 유지한다.
 *
 * 현재 REAL 구현:
 *   - 배터리: BatteryManager (권한 불필요)
 *   - 시간:   시스템 Calendar
 *   - 날짜:   시스템 Calendar
 *
 * 나머지 (날씨/온도/걸음수/강수 등)는 API/권한 연동 전까지 SAMPLE 유지.
 */
object DataSourceResolver {

    fun resolve(
        context: Context,
        items: List<Pair<String, String>>,
    ): List<ResolvedInfoItem> = items.map { (label, sampleValue) ->
        val lower = label.lowercase()
        val real = tryRealValue(context, lower)
        if (real != null) {
            ResolvedInfoItem(label, real, InfoSource.REAL)
        } else {
            ResolvedInfoItem(label, sampleValue, InfoSource.SAMPLE)
        }
    }

    private fun tryRealValue(context: Context, lower: String): String? = when {
        lower.contains("배터리") || lower.contains("battery") -> readBattery(context)
        lower.contains("시간") || lower.contains("time") -> readTime()
        lower.contains("날짜") || lower.contains("date") -> readDate()
        else -> null
    }

    private fun readBattery(context: Context): String? = runCatching {
        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        ) ?: return null
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return null
        "${level * 100 / scale}%"
    }.getOrNull()

    private fun readTime(): String {
        val cal = Calendar.getInstance()
        return "%d:%02d".format(
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
        )
    }

    private fun readDate(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.MONTH) + 1}월 ${cal.get(Calendar.DAY_OF_MONTH)}일"
    }
}
