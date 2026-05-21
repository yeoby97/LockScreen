package com.example.lockscreencopy.ui.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.data.GeminiClient
import com.example.lockscreencopy.data.LlmCatalog
import com.example.lockscreencopy.data.SelectedFirstStep
import com.example.lockscreencopy.data.buildLlmCatalog
import com.example.lockscreencopy.data.LlmRecommendation
import com.example.lockscreencopy.data.dummyLlmCases
import com.example.lockscreencopy.data.toSelectedFirstStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LlmSuggestionResult(
    val userQuery: String,
    val selected: SelectedFirstStep,
    val recommendation: LlmRecommendation,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmLayoutSheet(
    onDismiss: () -> Unit,
    onResult: (LlmSuggestionResult) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var stepMessage by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var catalog by remember { mutableStateOf<LlmCatalog?>(null) }
    var showDummyPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        catalog = withContext(Dispatchers.IO) { buildLlmCatalog(context) }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!loading) onDismiss() },
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                "AI 위젯 배치",
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            Text(
                "원하는 상황을 자유롭게 적어주세요. (예: \"운동할 때 필요한 거 배치해줘\")",
                color = Color(0xFFB0B0B5), fontSize = 13.sp,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                enabled = !loading,
                placeholder = { Text("상황을 입력하세요", color = Color(0xFF8E8E93)) },
            )

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showDummyPicker = !showDummyPicker },
                enabled = !loading && catalog != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (showDummyPicker) "더미 케이스 닫기" else "🧪 더미 데이터로 테스트",
                    color = Color.White,
                )
            }

            if (showDummyPicker) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "API 호출 없이 미리 정의된 테스트 케이스를 사용합니다.",
                    color = Color(0xFFB0B0B5), fontSize = 11.sp,
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(dummyLlmCases) { case ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFF2C2C2E),
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .clickable(enabled = !loading) {
                                    val cat = catalog ?: return@clickable
                                    val selected = case.toSelectedFirstStep(cat)
                                    onResult(
                                        LlmSuggestionResult(
                                            userQuery = case.userQuery,
                                            selected = selected,
                                            recommendation = case.recommendation,
                                        ),
                                    )
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Column {
                                Text(
                                    case.title,
                                    color = Color.White, fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    case.description,
                                    color = Color(0xFFB0B0B5), fontSize = 11.sp,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "💬 \"${case.userQuery}\"",
                                    color = Color(0xFF8E8E93), fontSize = 11.sp,
                                )
                            }
                        }
                    }
                }
            }

            if (loading) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("  $stepMessage", color = Color.White, fontSize = 13.sp)
                }
            }

            error?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(msg, color = Color(0xFFFF6B6B), fontSize = 12.sp)
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss, enabled = !loading) {
                    Text("취소", color = Color.White)
                }
                Spacer(Modifier.height(0.dp))
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    Button(
                        enabled = !loading && input.isNotBlank() && catalog != null,
                        onClick = {
                            val cat = catalog ?: return@Button
                            error = null
                            loading = true
                            stepMessage = "추천 앱을 고르는 중..."
                            scope.launch {
                                try {
                                    val ids = GeminiClient.recommendApps(input, cat.firstStepEntries())
                                    val selected = cat.resolveSelectedApps(ids)
                                    if (selected.widgetApps.isEmpty() &&
                                        selected.systemShortcuts.isEmpty() &&
                                        selected.installedApps.isEmpty()
                                    ) {
                                        error = "AI가 추천한 앱이 없어요. 입력을 더 구체적으로 적어주세요."
                                        loading = false
                                        return@launch
                                    }
                                    stepMessage = "위젯을 고르는 중..."
                                    val rec = GeminiClient.recommendWidgets(
                                        userQuery = input,
                                        trayCandidates = selected.trayCandidates(),
                                        floatingCandidates = selected.floatingCandidates(),
                                        shortcutCandidates = selected.shortcutCandidates(),
                                    )
                                    loading = false
                                    onResult(LlmSuggestionResult(input, selected, rec))
                                } catch (t: Throwable) {
                                    loading = false
                                    error = t.message ?: t.toString()
                                }
                            }
                        },
                    ) { Text("AI에게 부탁") }
                }
            }
        }
    }
}
