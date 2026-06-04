package com.example.lockscreencopy.data

import android.util.Log
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.flow.collect

/**
 * 온디바이스 Gemini Nano(ML Kit GenAI Prompt API) 래퍼.
 *
 * 갤럭시 폴드7 등 AICore 지원 기기에서 자유 프롬프트로 추론한다.
 * 미지원 기기에서는 [checkAvailable]가 false를 돌려주어, 호출 측([NudgeAnalyzer])이
 * 클라우드 Gemini API로 폴백하도록 한다.
 *
 * Nano는 클라우드 모델처럼 responseSchema(구조화 출력)를 지원하지 않으므로,
 * 프롬프트에서 "JSON만 출력"하도록 지시하고 결과를 직접 파싱한다([NudgeAnalyzer.extractJson]).
 */
object NanoPromptClient {

    private const val TAG = "NudgeAnalyzer"

    private val model: GenerativeModel by lazy { Generation.getClient() }

    /**
     * Nano가 즉시 사용 가능한지 확인한다.
     *
     * - AVAILABLE         → true (바로 추론 가능)
     * - DOWNLOADABLE/ING  → 백그라운드 다운로드를 시도하고, 다운로드가 끝나 AVAILABLE이
     *                       되면 true. (다운로드가 오래 걸리면 이번 호출은 false로 보고 폴백)
     * - 그 외(UNAVAILABLE)→ false (미지원 기기)
     */
    suspend fun checkAvailable(): Boolean = runCatching {
        when (model.checkStatus()) {
            FeatureStatus.AVAILABLE -> true
            FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> {
                Log.i(TAG, "Nano 모델 다운로드 시도")
                runCatching { model.download().collect {} }
                    .onFailure { Log.w(TAG, "Nano 다운로드 실패", it) }
                model.checkStatus() == FeatureStatus.AVAILABLE
            }
            else -> false
        }
    }.getOrElse {
        Log.w(TAG, "Nano 상태 확인 실패 — 미지원으로 처리", it)
        false
    }

    /** 자유 프롬프트로 추론하고 텍스트 결과를 돌려준다. 실패 시 예외를 던진다. */
    suspend fun generate(prompt: String): String {
        // generateContentRequest(text, init): 텍스트와 빌더 람다 둘 다 필수(람다는 비워도 됨).
        val request = generateContentRequest(TextPart(prompt)) {}
        val result = model.generateContent(request)
        return result.candidates.firstOrNull()?.text.orEmpty()
    }
}
