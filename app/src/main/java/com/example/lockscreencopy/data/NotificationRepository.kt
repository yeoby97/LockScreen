package com.example.lockscreencopy.data

import android.content.Context
import com.example.lockscreencopy.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object NotificationRepository {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val appNameCache = HashMap<String, String>()

    fun resolveAppName(context: Context, pkg: String): String =
        appNameCache.getOrPut(pkg) {
            runCatching {
                context.applicationContext.packageManager
                    .getApplicationLabel(context.packageManager.getApplicationInfo(pkg, 0))
                    .toString()
            }.getOrDefault(pkg)
        }

    fun add(item: NotificationItem) =
        _notifications.update { current -> listOf(item) + current.filterNot { it.id == item.id } }

    /** 기존 항목의 넛지 결과만 제자리에서 갱신한다(순서 유지). 온디바이스 AI 재분석 결과 반영용. */
    fun updateNudge(id: String, result: NudgeResult) =
        _notifications.update { current ->
            current.map { item ->
                if (item.id != id) item
                else item.copy(
                    hasNudge = result.hasNudge,
                    nudgeLabel = result.nudgeLabel,
                    nudgeActions = result.actions,
                    mapQuery = result.mapQuery,
                )
            }
        }

    fun remove(id: String) =
        _notifications.update { current -> current.filterNot { it.id == id } }

    fun reset(items: List<NotificationItem>) {
        _notifications.value = items
    }
}
