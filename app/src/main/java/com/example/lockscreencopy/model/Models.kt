package com.example.lockscreencopy.model

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class WidgetSize { SMALL, WIDE }

enum class AddTarget { SLOT, FLOATING }

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
