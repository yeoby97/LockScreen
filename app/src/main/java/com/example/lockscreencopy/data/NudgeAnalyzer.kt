package com.example.lockscreencopy.data

import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import kotlinx.coroutines.tasks.await

/**
 * 온디바이스 AI(ML Kit Entity Extraction, 한국어 모델)로 메시지에서
 * 날짜·시간·주소 엔티티를 추출해 넛지 액션을 예측한다.
 *
 * - 완전 오프라인: 모델은 기기에 다운로드되며 추론 시 네트워크가 필요 없다.
 * - 날짜/시간은 ML Kit의 [Entity.TYPE_DATE_TIME] 으로 자연어 표현까지 인식한다.
 * - 장소는 ML Kit [Entity.TYPE_ADDRESS] 와 키워드 정규식([PLACE_PATTERN])을 함께 사용한다.
 *   (한국식 상호·지명은 ADDRESS 로 잘 잡히지 않아 키워드 매칭을 병행한다.)
 * - 모델 미탑재/추론 실패 시 정규식 기반 [detectNudge] 로 자동 폴백한다.
 */
object NudgeAnalyzer {

    private val extractor: EntityExtractor by lazy {
        EntityExtraction.getClient(
            EntityExtractorOptions.Builder(EntityExtractorOptions.KOREAN).build(),
        )
    }

    @Volatile
    private var modelReady = false

    /** 한국어 엔티티 추출 모델을 미리 내려받아 첫 추론 지연을 줄인다. */
    suspend fun warmUp() {
        ensureModel()
    }

    private suspend fun ensureModel(): Boolean {
        if (modelReady) return true
        return runCatching {
            extractor.downloadModelIfNeeded().await()
            modelReady = true
            true
        }.getOrDefault(false)
    }

    /**
     * 온디바이스 AI로 넛지를 분석한다. 모델이 준비되지 않았거나 추론이 실패하면
     * 정규식 기반 결과로 폴백하므로 항상 안전하게 결과를 반환한다.
     */
    suspend fun analyze(title: String, body: String): NudgeResult {
        val text = "$title $body"
        if (!ensureModel()) return detectNudge(title, body)

        val annotations = runCatching {
            extractor.annotate(EntityExtractionParams.Builder(text).build()).await()
        }.getOrElse { return detectNudge(title, body) }

        val types = annotations.flatMap { it.entities }.map { it.type }.toSet()
        val hasDateTime = Entity.TYPE_DATE_TIME in types
        // 시간 신호는 별도로 구분하지 않고 날짜/시간 엔티티에 포함시킨다.
        val hasDate = hasDateTime
        val hasTime = hasDateTime
        val hasPlace = Entity.TYPE_ADDRESS in types || PLACE_PATTERN.containsMatchIn(text)

        val aiResult = resolveNudge(hasDate, hasTime, hasPlace)
        // AI가 아무 신호도 못 찾으면 정규식이 잡아낸 신호라도 살린다.
        return if (aiResult.hasNudge) aiResult else detectNudge(title, body)
    }
}
