package com.example.lockscreencopy.ui.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lockscreencopy.BuildConfig
import com.example.lockscreencopy.data.lockWidgetApps
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.LockWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiRecommendSheet(
    installedApps: List<BottomShortcut.App>,
    systemShortcuts: List<BottomShortcut.System>,
    onDismiss: () -> Unit,
    onApply: (AiSelection) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var userPrompt by remember { mutableStateOf("") }
    var rec by remember { mutableStateOf<AiRecommendation?>(null) }
    var loading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    var selectedTray = remember { mutableStateOf(setOf<String>()) }
    var selectedFloating = remember { mutableStateOf(setOf<String>()) }
    var selectedLeft = remember { mutableStateOf<String?>(null) }
    var selectedRight = remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color(0xFF1C1C1E)) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Text("AI 잠금화면 추천", color = Color.White)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = userPrompt,
                    onValueChange = { userPrompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("요청", color = Color(0xFFB0B0B0)) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF2C2C2E),
                        unfocusedContainerColor = Color(0xFF2C2C2E),
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    scope.launch {
                        loading = true
                        status = null
                        val result = withContext(Dispatchers.IO) {
                            fetchRecommendation(userPrompt, installedApps, systemShortcuts)
                        }
                        rec = result
                        selectedTray.value = emptySet(); selectedFloating.value = emptySet(); selectedLeft.value = null; selectedRight.value = null
                        status = if (BuildConfig.GEMINI_API_KEY.isBlank()) "API 키가 없어 로컬 추천으로 동작 중" else "AI 추천 완료"
                        loading = false
                    }
                }, enabled = userPrompt.isNotBlank() && !loading) { Text(if (loading) "추천 생성 중..." else "추천 받기") }
                status?.let { Text(it, color = Color(0xFFB0B0B0), modifier = Modifier.padding(top = 8.dp)) }
            }
            rec?.let { r ->
                item { Spacer(Modifier.height(16.dp)); Text("트레이 후보(최대 4칸)", color = Color.White) }
                items(r.trayCandidates) { w ->
                    val on = selectedTray.value.contains(w.id)
                    CandidateRow(w.name, alpha = if (on) 0.85f else 0.35f) {
                        val span = if (w.size.name == "WIDE") 2 else 1
                        val current = selectedTray.value.mapNotNull { id -> r.trayCandidates.find { it.id == id } }
                        val used = current.sumOf { if (it.size.name == "WIDE") 2 else 1 }
                        if (!on && used + span > 4) return@CandidateRow
                        selectedTray.value = if (on) selectedTray.value - w.id else selectedTray.value + w.id
                    }
                }
                item { Spacer(Modifier.height(16.dp)); Text("자유 배치 후보", color = Color.White) }
                items(r.floatingCandidates) { w ->
                    val on = selectedFloating.value.contains(w.id)
                    CandidateRow(w.name, alpha = if (on) 0.85f else 0.35f) {
                        selectedFloating.value = if (on) selectedFloating.value - w.id else selectedFloating.value + w.id
                    }
                }
                item { Spacer(Modifier.height(16.dp)); Text("좌/우 바로가기 후보", color = Color.White) }
                items(r.shortcutCandidates) { s ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CandidateRow("좌: ${s.label}", alpha = if (selectedLeft.value == s.id) 0.85f else 0.35f, modifier = Modifier.weight(1f)) { selectedLeft.value = s.id }
                        CandidateRow("우: ${s.label}", alpha = if (selectedRight.value == s.id) 0.85f else 0.35f, modifier = Modifier.weight(1f)) { selectedRight.value = s.id }
                    }
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        onApply(AiSelection(selectedTray.value.toList(), selectedFloating.value.toList(), selectedLeft.value, selectedRight.value, r))
                    }) { Text("적용") }
                }
            }
        }
    }
}

@Composable
private fun CandidateRow(text: String, alpha: Float, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        text,
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color.White.copy(alpha = alpha), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp),
    )
}

data class AiRecommendation(
    val trayCandidates: List<LockWidget>,
    val floatingCandidates: List<LockWidget>,
    val shortcutCandidates: List<BottomShortcut>,
)

data class AiSelection(
    val trayWidgetIds: List<String>,
    val floatingWidgetIds: List<String>,
    val leftShortcutId: String?,
    val rightShortcutId: String?,
    val recommendation: AiRecommendation,
)

private fun fetchRecommendation(prompt: String, installedApps: List<BottomShortcut.App>, systemShortcuts: List<BottomShortcut.System>): AiRecommendation {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isBlank()) return fallbackRecommendation(prompt, installedApps, systemShortcuts)

    return runCatching {
        // 1단계: 앱/기능 선택 (시스템 토글 + 설치 앱 + 위젯앱)
        val appCatalog = lockWidgetApps.joinToString("\n") { "APP:${it.id}:${it.name}" }
        val systemCatalog = systemShortcuts.joinToString("\n") { "SYSTEM:${it.id}:${it.label}" }
        val installedCatalog = installedApps.joinToString("\n") { "INSTALLED:${it.id}:${it.label}" }

        val step1Prompt = """
            사용자 요청: $prompt
            아래 후보에서 관련 appId를 고르세요. 최대 6개. JSON만 반환.
            위젯 앱 목록:
            $appCatalog
            시스템 기능 목록:
            $systemCatalog
            설치 앱 목록:
            $installedCatalog
            반환 스키마:
            {"appIds":["weather","health"],"shortcutIds":["sys_flashlight","app_com.xxx"]}
        """.trimIndent()
        val step1 = callGeminiJson(apiKey, step1Prompt)
        val appIds = step1.optJSONArray("appIds").toStrList().toSet()
        val pickedWidgetApps = lockWidgetApps.filter { it.id in appIds }

        // 2단계: 영역별 위젯/바로가기 선택 (중복 허용)
        val widgetCatalog = pickedWidgetApps.flatMap { app -> app.widgets.map { "${app.id}:${it.id}:${it.name}:${it.size}" } }
            .joinToString("\n")
        val shortcutCatalog = (systemShortcuts + installedApps).joinToString("\n") { "${it.id}:${it.label}" }

        val step2Prompt = """
            사용자 요청: $prompt
            선택된 앱: ${pickedWidgetApps.joinToString { it.name }}
            트레이/자유배치/좌우 바로가기 후보를 고르세요. 중복 허용. JSON만 반환.
            트레이는 최대 4칸이며 WIDE는 2칸, SMALL은 1칸.
            위젯 목록:
            $widgetCatalog
            바로가기 목록:
            $shortcutCatalog
            반환 스키마:
            {
              "trayWidgetIds":["h1","w_temp","h1"],
              "floatingWidgetIds":["w_uv","w_uv","w_rain"],
              "shortcutCandidateIds":["sys_flashlight","app_com.foo","app_com.foo"]
            }
        """.trimIndent()
        val step2 = callGeminiJson(apiKey, step2Prompt)

        val trayIds = step2.optJSONArray("trayWidgetIds").toStrList()
        val floatingIds = step2.optJSONArray("floatingWidgetIds").toStrList()
        val shortcutIds = step2.optJSONArray("shortcutCandidateIds").toStrList()
        buildRecommendationFromIds(trayIds, floatingIds, shortcutIds, installedApps, systemShortcuts, pickedWidgetApps)
    }.getOrElse { fallbackRecommendation(prompt, installedApps, systemShortcuts) }
}

private fun callGeminiJson(apiKey: String, prompt: String): JSONObject {
    val body = JSONObject().put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))))
    val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
    val conn = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        setRequestProperty("Content-Type", "application/json")
        doOutput = true
        outputStream.use { it.write(body.toString().toByteArray()) }
    }
    val response = conn.inputStream.bufferedReader().use { it.readText() }
    val text = JSONObject(response).getJSONArray("candidates").getJSONObject(0)
        .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
    val jsonText = text.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
    return JSONObject(jsonText)
}

private fun JSONArray?.toStrList(): List<String> = if (this == null) emptyList() else (0 until length()).mapNotNull { optString(it) }

private fun buildRecommendationFromIds(
    trayIds: List<String>,
    floatingIds: List<String>,
    shortcutIds: List<String>,
    installedApps: List<BottomShortcut.App>,
    systemShortcuts: List<BottomShortcut.System>,
    pickedWidgetApps: List<com.example.lockscreencopy.model.WidgetApp>,
): AiRecommendation {
    val baseApps = if (pickedWidgetApps.isNotEmpty()) pickedWidgetApps else lockWidgetApps
    val allWidgets = baseApps.flatMap { it.widgets }
    val tray = trayIds.mapNotNull { id -> allWidgets.find { it.id == id } }
    val floating = floatingIds.mapNotNull { id -> allWidgets.find { it.id == id } }
    val shortcutMap = (systemShortcuts + installedApps).associateBy { it.id }
    val sc = shortcutIds.mapNotNull { shortcutMap[it] }
    return AiRecommendation(
        if (tray.isNotEmpty()) tray else allWidgets.take(6),
        if (floating.isNotEmpty()) floating else allWidgets.drop(1).take(6),
        if (sc.isNotEmpty()) sc else (systemShortcuts + installedApps).take(10),
    )
}

private fun fallbackRecommendation(prompt: String, installedApps: List<BottomShortcut.App>, systemShortcuts: List<BottomShortcut.System>): AiRecommendation {
    val normalized = prompt.lowercase()
    val apps = when {
        normalized.contains("운동") || normalized.contains("health") -> setOf("health", "weather")
        normalized.contains("음악") || normalized.contains("music") -> setOf("ytmusic", "weather")
        normalized.contains("일정") || normalized.contains("공부") -> setOf("calendar", "reminder")
        else -> setOf("weather", "calendar", "battery")
    }
    val widgets = lockWidgetApps.filter { apps.contains(it.id) }.flatMap { it.widgets }
    return AiRecommendation(
        trayCandidates = widgets.take(4),
        floatingCandidates = widgets.drop(1).take(4),
        shortcutCandidates = (systemShortcuts + installedApps).take(8),
    )
}
