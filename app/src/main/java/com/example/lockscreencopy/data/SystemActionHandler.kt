package com.example.lockscreencopy.data

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.provider.Settings
import android.widget.Toast
import com.example.lockscreencopy.model.SystemAction

private var torchOn = false
private var torchCameraId: String? = null

fun handleSystemAction(context: Context, action: SystemAction) {
    val ok = runCatching {
        when (action) {
            SystemAction.SOUND -> startPanel(context, Settings.Panel.ACTION_VOLUME)
            SystemAction.FLASHLIGHT -> toggleFlashlight(context)
            SystemAction.AIRPLANE -> startSettings(context, Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            SystemAction.MOBILE_DATA -> startPanel(context, Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            SystemAction.POWER_SAVING -> startSettings(context, Settings.ACTION_BATTERY_SAVER_SETTINGS)
            SystemAction.DARK_MODE -> startSettings(context, Settings.ACTION_DISPLAY_SETTINGS)
            SystemAction.DO_NOT_DISTURB -> startSettings(context, Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            SystemAction.QR_SCAN -> launchQrScanner(context)
            SystemAction.LOCATION -> startSettings(context, Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        }
    }.isSuccess
    if (!ok) Toast.makeText(context, "이 동작은 지원되지 않습니다", Toast.LENGTH_SHORT).show()
}

private fun startPanel(context: Context, action: String) {
    val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun startSettings(context: Context, action: String) {
    val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun toggleFlashlight(context: Context) {
    val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val id = torchCameraId ?: cm.cameraIdList.firstOrNull { camId ->
        val chars = cm.getCameraCharacteristics(camId)
        chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    } ?: run {
        Toast.makeText(context, "손전등을 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
        return
    }
    torchCameraId = id
    torchOn = !torchOn
    cm.setTorchMode(id, torchOn)
    Toast.makeText(context, if (torchOn) "손전등 켜짐" else "손전등 꺼짐", Toast.LENGTH_SHORT).show()
}

private fun launchQrScanner(context: Context) {
    val intent = Intent("com.google.zxing.client.android.SCAN")
    val resolved = intent.resolveActivity(context.packageManager) != null
    if (resolved) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } else {
        val camera = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(camera)
    }
}
