package com.example.lockscreencopy.data

private val DATE_PATTERNS = listOf(
    Regex("""(\d{1,2})월\s*(\d{1,2})일"""),
    Regex("""(월|화|수|목|금|토|일)요일"""),
    Regex("""다음\s*(주|달)"""),
    Regex("""이번\s*(주|달|토|일)"""),
)

private val TIME_PATTERNS = listOf(
    Regex("""(\d{1,2})시"""),
    Regex("""오전|오후"""),
    Regex("""아침|점심|저녁|밤"""),
)

internal val PLACE_PATTERN = Regex("""역|스타벅스|카페|식당|맛집|공원|센터|병원|학교|마트""")

/** 장소 키워드 주변 문맥(최대 12자)을 추출해 지도 검색어로 반환 (AI 미사용 시 폴백) */
internal fun extractPlaceQuery(text: String): String? {
    val match = PLACE_PATTERN.find(text) ?: return null
    val start = (match.range.first - 8).coerceAtLeast(0)
    val end = (match.range.last + 1).coerceAtMost(text.length)
    return text.substring(start, end).trim()
}

data class NudgeResult(
    val hasNudge: Boolean,
    val nudgeLabel: String,
    val actions: List<String>,
    /** 지도 검색용 정제된 장소명. 없으면 빈 문자열. */
    val mapQuery: String = "",
)

/**
 * 날짜/시간/장소 신호 조합을 넛지 액션으로 변환하는 공통 결정 로직.
 * 정규식 폴백 [detectNudge] 와 AI 분석 [NudgeAnalyzer] 가 함께 사용한다.
 */
internal fun resolveNudge(
    hasDate: Boolean,
    hasTime: Boolean,
    hasPlace: Boolean,
    mapQuery: String = "",
): NudgeResult =
    when {
        hasDate && (hasTime || hasPlace) -> NudgeResult(
            hasNudge = true,
            nudgeLabel = "일정 추가",
            actions = buildList {
                add("일정 추가")
                if (hasPlace) add("지도 열기")
            },
            mapQuery = mapQuery,
        )
        hasPlace -> NudgeResult(
            hasNudge = true,
            nudgeLabel = "지도 열기",
            actions = listOf("지도 열기"),
            mapQuery = mapQuery,
        )
        else -> NudgeResult(hasNudge = false, nudgeLabel = "", actions = emptyList())
    }

/**
 * 정규식 기반 즉시 탐지. 알림 도착 즉시 호출되어 첫 화면을 그리며,
 * AI(클라우드) 분석이 실패하거나 키가 없을 때의 폴백으로도 쓰인다.
 */
fun detectNudge(title: String, body: String): NudgeResult {
    val text = "$title $body"
    val hasDate = DATE_PATTERNS.any { it.containsMatchIn(text) }
    val hasTime = TIME_PATTERNS.any { it.containsMatchIn(text) }
    val hasPlace = PLACE_PATTERN.containsMatchIn(text)
    val mapQuery = if (hasPlace) extractPlaceQuery(text).orEmpty() else ""
    return resolveNudge(hasDate, hasTime, hasPlace, mapQuery)
}
