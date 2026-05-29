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

private val PLACE_PATTERN = Regex("""역|스타벅스|카페|식당|맛집|공원|센터|병원|학교|마트""")

fun detectNudge(title: String, body: String): Pair<Boolean, String> {
    val text = "$title $body"
    val hasDate = DATE_PATTERNS.any { it.containsMatchIn(text) }
    val hasTime = TIME_PATTERNS.any { it.containsMatchIn(text) }
    val hasPlace = PLACE_PATTERN.containsMatchIn(text)
    return when {
        hasDate && hasTime -> true to "일정 추가"
        hasDate && hasPlace -> true to "일정 추가"
        else -> false to ""
    }
}
