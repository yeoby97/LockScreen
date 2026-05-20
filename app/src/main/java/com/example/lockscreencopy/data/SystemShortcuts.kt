package com.example.lockscreencopy.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.graphics.Color
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.SystemAction

val systemShortcuts: List<BottomShortcut.System> = listOf(
    BottomShortcut.System("sys_sound", "소리", SystemAction.SOUND, Icons.Filled.VolumeUp),
    BottomShortcut.System("sys_flashlight", "손전등", SystemAction.FLASHLIGHT, Icons.Filled.FlashlightOn, Color(0xFFFFD54F)),
    BottomShortcut.System("sys_airplane", "비행기 탑승 모드", SystemAction.AIRPLANE, Icons.Filled.AirplanemodeActive),
    BottomShortcut.System("sys_mobile_data", "모바일 데이터", SystemAction.MOBILE_DATA, Icons.Filled.NetworkCell),
    BottomShortcut.System("sys_power_saving", "절전 모드", SystemAction.POWER_SAVING, Icons.Filled.BatterySaver, Color(0xFF81C784)),
    BottomShortcut.System("sys_dark_mode", "다크 모드", SystemAction.DARK_MODE, Icons.Filled.DarkMode),
    BottomShortcut.System("sys_dnd", "방해 금지", SystemAction.DO_NOT_DISTURB, Icons.Filled.DoNotDisturbOn),
    BottomShortcut.System("sys_qr", "QR 코드 스캔", SystemAction.QR_SCAN, Icons.Filled.QrCodeScanner),
    BottomShortcut.System("sys_location", "위치", SystemAction.LOCATION, Icons.Filled.LocationOn),
)
