package com.example.lockscreencopy.data

import android.util.Log
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 온디바이스 Gemini Nano(ML Kit GenAI Prompt API) 래퍼.
 *
 * 갤럭시 폴드7 등 AICore 지원 기기에서 자유 프롬프트로 추론한다.
 * 미지원 기기에서는 [checkAvailable]가 false를 돌려주어, 호출 측([NudgeAnalyzer])이
 * 클라우드 Gemini API로 폴백하도록 한다.
 *
 * 모델 다운로드 흐름:
 *  - AVAILABLE   → 바로 온디바이스 추론.
 *  - DOWNLOADABLE/DOWNLOADING → 기기는 Nano를 지원하지만 모델이 아직 없음. 다운로드를
 *    "백그라운드에서 1회만" 시작하고, 진행되는 동안은 false를 돌려 클라우드로 폴백한다.
 *    다운로드가 끝나면 다음 호출의 checkStatus()가 AVAILABLE이 되어 자동으로 Nano로 전환된다.
 *  - UNAVAILABLE → 미지원 기기. 항상 클라우드.
 *
 * Nano는 클라우드 모델처럼 responseSchema(구조화 출력)를 지원하지 않으므로,
 * 프롬프트에서 "JSON만 출력"하도록 지시하고 결과를 직접 파싱한다([NudgeAnalyzer.extractJson]).
 */
object NanoPromptClient {

    private const val TAG = "NudgeAnalyzer"

    private val model: GenerativeModel by lazy { Generation.getClient() }

    /** 모델 다운로드 백그라운드 작업용 스코프(분석 흐름을 막지 않기 위해 분리). */
    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 다운로드를 이미 시작했는지. 매 분석마다 중복 트리거되는 것을 막는다. */
    @Volatile
    private var downloadStarted = false

    /**
     * Nano가 "지금 당장" 사용 가능한지 확인한다.
     * DOWNLOADABLE이면 백그라운드 다운로드만 걸어두고 false를 돌려준다(이번 건은 클라우드).
     */
    suspend fun checkAvailable(): Boolean = runCatching {
        val status = model.checkStatus()
        Log.i(TAG, "Nano checkStatus=$status (AVAILABLE=${FeatureStatus.AVAILABLE}, DOWNLOADABLE=${FeatureStatus.DOWNLOADABLE}, UNAVAILABLE=${FeatureStatus.UNAVAILABLE})")
        when (status) {
            FeatureStatus.AVAILABLE -> true
            FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> {
                ensureDownloadStarted()
                false // 다운로드 진행 중 — 이번엔 클라우드, 완료되면 다음부터 Nano
            }
            else -> false
        }
    }.getOrElse {
        Log.w(TAG, "Nano 상태 확인 실패 — 미지원으로 처리", it)
        false
    }

    /** Nano 모델 다운로드를 백그라운드에서 최초 1회만 시작한다. */
    private fun ensureDownloadStarted() {
        if (downloadStarted) {
            Log.i(TAG, "Nano 모델 다운로드 이미 진행 중 — 완료되면 온디바이스로 전환")
            return
        }
        downloadStarted = true
        Log.i(TAG, "Nano 모델 다운로드 시작(백그라운드). Wi-Fi/충전 상태에서 더 빨리 받습니다.")
        downloadScope.launch {
            runCatching {
                model.download().collect { Log.i(TAG, "Nano 다운로드 진행: $it") }
            }.onSuccess {
                Log.i(TAG, "Nano 다운로드 완료. 현재 상태=${model.checkStatus()}")
            }.onFailure {
                downloadStarted = false // 실패 시 다음 기회에 재시도 허용
                Log.w(TAG, "Nano 다운로드 실패 — 계속 클라우드 사용", it)
            }
        }
    }

    /** 자유 프롬프트로 추론하고 텍스트 결과를 돌려준다. 실패 시 예외를 던진다. */
    suspend fun generate(prompt: String): String {
        // generateContentRequest(text, init): 텍스트와 빌더 람다 둘 다 필수(람다는 비워도 됨).
        val request = generateContentRequest(TextPart(prompt)) {}
        val result = model.generateContent(request)
        return result.candidates.firstOrNull()?.text.orEmpty()
    }
}
