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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lockscreencopy.data.lockWidgetApps
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.LockWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiRecommendSheet(
    installedApps: List<BottomShortcut.App>,
    systemShortcuts: List<BottomShortcut.System>,
    onDismiss: () -> Unit,
    onApply: (AiSelection) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var userPrompt by remember { mutableStateOf("") }
    var rec by remember { mutableStateOf<AiRecommendation?>(null) }

    var selectedTray = remember { mutableStateOf(setOf<String>()) }
    var selectedFloating = remember { mutableStateOf(setOf<String>()) }
    var selectedLeft = remember { mutableStateOf<String?>(null) }
    var selectedRight = remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color(0xFF1C1C1E)) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Text("AI 잠금화면 추천", color = Color.White)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = userPrompt, onValueChange = { userPrompt = it }, modifier = Modifier.fillMaxWidth(), label = { Text("요청") })
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    rec = generateAiRecommendation(userPrompt, installedApps, systemShortcuts)
                    selectedTray.value = emptySet(); selectedFloating.value = emptySet(); selectedLeft.value = null; selectedRight.value = null
                }, enabled = userPrompt.isNotBlank()) { Text("추천 받기") }
            }
            rec?.let { r ->
                item { Spacer(Modifier.height(16.dp)); Text("트레이 후보(최대 4칸)", color = Color.White) }
                items(r.trayCandidates) { w ->
                    val on = selectedTray.value.contains(w.id)
                    CandidateRow(w.name, on, alpha = if (on) 0.85f else 0.35f) {
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
                    CandidateRow(w.name, on, alpha = if (on) 0.85f else 0.35f) {
                        selectedFloating.value = if (on) selectedFloating.value - w.id else selectedFloating.value + w.id
                    }
                }
                item { Spacer(Modifier.height(16.dp)); Text("좌/우 바로가기 후보", color = Color.White) }
                items(r.shortcutCandidates) { s ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CandidateRow("좌: ${s.label}", selectedLeft.value == s.id, 0.55f) { selectedLeft.value = s.id }
                        CandidateRow("우: ${s.label}", selectedRight.value == s.id, 0.55f) { selectedRight.value = s.id }
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
private fun CandidateRow(text: String, selected: Boolean, alpha: Float, onClick: () -> Unit) {
    Text(
        text,
        color = Color.White,
        modifier = Modifier
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

private fun generateAiRecommendation(
    prompt: String,
    installedApps: List<BottomShortcut.App>,
    systemShortcuts: List<BottomShortcut.System>,
): AiRecommendation {
    // TODO: Gemini 2.5 Flash 연동 시 BuildConfig.GEMINI_API_KEY를 사용하세요.
    // 키 주입: ~/.gradle/gradle.properties 또는 프로젝트 local.properties의 GEMINI_API_KEY=...
    val normalized = prompt.lowercase()
    val apps = if (normalized.contains("운동") || normalized.contains("health")) {
        setOf("health", "weather")
    } else {
        setOf("weather", "calendar", "battery")
    }
    val widgets = lockWidgetApps.filter { apps.contains(it.id) }.flatMap { it.widgets }
    val tray = widgets.take(4)
    val floating = widgets.drop(1).take(4)
    val sc = buildList {
        addAll(systemShortcuts.filter { it.label.contains("손전등") || it.label.contains("위치") })
        addAll(installedApps.take(4))
    }
    return AiRecommendation(trayCandidates = tray, floatingCandidates = floating, shortcutCandidates = sc)
}
