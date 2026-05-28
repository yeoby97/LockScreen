package com.example.lockscreencopy.model

import android.appwidget.AppWidgetProviderInfo
import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class WidgetSize { SMALL, WIDE }

enum class AddTarget { SLOT, FLOATING }

enum class FavoriteAppsLayout { BOTTOM_LEFT, LEFT_VERTICAL, BOTTOM_RIGHT }

data class LockWidget(
    val id: String,
    val appId: String,
    val name: String,
    val size: WidgetSize,
    val icon: ImageVector? = null,
    val iconTint: Color = Color.White,
    val mainValue: String = "",
    val subValue: String = "",
)

data class PlacedWidget(
    val uid: String,
    val widget: LockWidget,
)

data class FloatingWidget(
    val uid: String,
    val widget: LockWidget,
    val offset: Offset,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
)

data class WidgetApp(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val iconBg: Color,
    val widgets: List<LockWidget>,
)

data class AppItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val tint: Color,
)

enum class SystemAction {
    SOUND, FLASHLIGHT, AIRPLANE, MOBILE_DATA, POWER_SAVING,
    DARK_MODE, DO_NOT_DISTURB, QR_SCAN, LOCATION,
}

sealed class BottomShortcut {
    abstract val id: String
    abstract val label: String

    data class System(
        override val id: String,
        override val label: String,
        val action: SystemAction,
        val icon: ImageVector,
        val tint: Color = Color.White,
    ) : BottomShortcut()

    data class App(
        override val id: String,
        override val label: String,
        val packageName: String,
        val drawable: Drawable? = null,
    ) : BottomShortcut()
}

data class HostedAppWidget(
    val uid: String,
    val appWidgetId: Int,
    val providerInfo: AppWidgetProviderInfo,
    val widthPx: Int,
    val heightPx: Int,
    val offset: Offset,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
)

data class NotificationItem(
    val id: String,
    val appName: String,
    val title: String,
    val body: String,
    val timeLabel: String,
    val hasNudge: Boolean = false,
    val nudgeLabel: String = "",
    val nudgeActions: List<String> = emptyList(),
)
