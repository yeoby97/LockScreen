package com.example.lockscreencopy.data

import com.example.lockscreencopy.model.AiTextSlot
import com.example.lockscreencopy.model.AiWidgetTemplateType
import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.LockWidget
import com.example.lockscreencopy.model.ResolvedInfoItem
import com.example.lockscreencopy.model.WidgetSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class LlmRecommendation(
    val tray: List<String>,
    val floating: List<String>,
    val left: String?,
    val right: String?,
)

/**
 * AI 스케치 장면 설계 결과.
 * 정보가 그림의 자연스러운 요소(나뭇잎/행성/불꽃/땀방울 등)에 "녹아들도록"
 * 한 LLM이 함께 만들어낸 이미지 생성 프롬프트 + 각 정보의 위치 슬롯.
 */
data class SketchScene(
    val imagePrompt: String,
    val slots: List<AiTextSlot>,
)

object GeminiClient {
    // TODO: 실제 키로 교체하세요. (현재는 임시 하드코딩 상수)
    private const val API_KEY: String = "YOUR_GEMINI_API_KEY"
    private const val MODEL = "gemini-2.5-flash"
    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private const val IMAGE_MODEL = "gemini-2.5-flash-image"
    private const val IMAGE_ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$IMAGE_MODEL:generateContent"

    // HTTP/2 협상 실패로 인한 "Connection reset"을 피하기 위해 HTTP/1.1만 사용
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .writeTimeout(40, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .protocols(listOf(Protocol.HTTP_1_1))
        .build()

    private const val MAX_ATTEMPTS = 3

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    class LlmException(message: String, cause: Throwable? = null) : Exception(message, cause)

    suspend fun recommendApps(userQuery: String, entries: List<LlmAppEntry>): List<String> =
        withContext(Dispatchers.IO) {
            val listText = buildString {
                entries.forEach { e ->
                    val kindLabel = when (e.kind) {
                        LlmAppEntry.Kind.WIDGET_APP -> "트레이용 mock 위젯 앱"
                        LlmAppEntry.Kind.REAL_WIDGET_APP -> "자유배치용 실제 앱"
                        LlmAppEntry.Kind.SYSTEM_SHORTCUT -> "기본 기능"
                        LlmAppEntry.Kind.INSTALLED_APP -> "설치된 앱 바로가기"
                    }
                    append("- id=").append(e.id)
                        .append(" | 분류=").append(kindLabel)
                        .append(" | 이름=").append(e.name)
                        .append('\n')
                }
            }
            val prompt = """
                당신은 잠금화면 위젯 추천 도우미입니다.
                사용자 요구: "${userQuery.trim()}"

                아래 목록에서 사용자 요구를 잘 도와줄 항목들을 골라 주세요.
                반드시 JSON으로만 응답하세요. 다른 설명 없이 JSON만.

                응답 스키마:
                {"selected": ["<id>", "<id>", ...]}

                항목 목록:
                $listText
            """.trimIndent()
            val raw = callGemini(prompt)
            parseSelectedIds(raw)
        }

    suspend fun recommendWidgets(
        userQuery: String,
        trayCandidates: List<LockWidget>,
        floatingCandidates: List<RealWidgetEntry>,
        shortcutCandidates: List<BottomShortcut>,
    ): LlmRecommendation = withContext(Dispatchers.IO) {
        val trayText = trayCandidates.joinToString("\n") { w ->
            "- id=${w.id} | 앱=${w.appId} | 이름=${w.name} | 크기=${if (w.size == WidgetSize.WIDE) "WIDE(2칸)" else "SMALL(1칸)"}"
        }
        val floatingText = floatingCandidates.joinToString("\n") { e ->
            "- id=${e.component} | 앱=${e.appLabel} (${e.packageName}) | 이름=${e.label} | 크기=${e.sizeText}"
        }
        val shortcutText = shortcutCandidates.joinToString("\n") { sc ->
            val kind = if (sc is BottomShortcut.System) "기본기능" else "앱"
            "- id=${sc.id} | 분류=$kind | 이름=${sc.label}"
        }
        val prompt = """
            당신은 잠금화면 위젯 배치 도우미입니다.
            사용자 요구: "${userQuery.trim()}"

            세 영역에 배치할 위젯/바로가기를 추천해 주세요.
            - 트레이 영역: 작은 위젯 슬롯(권한 없는 영역이므로 mock 위젯 사용).
              SMALL=1칸, WIDE=2칸. 총 합계 4칸을 넘지 마세요.
            - 자유 배치 영역: 실제 설치된 앱의 위젯들. id 는 반드시 component
              문자열을 그대로 사용. 0~6개 추천.
            - 좌측/우측 바로가기: 하단의 단일 바로가기. 각각 1개 또는 null.

            반드시 JSON으로만 응답하세요. 다른 설명 없이 JSON만.

            응답 스키마:
            {
              "tray": ["<mock lockwidget id>", ...],
              "floating": ["<real widget component>", ...],
              "left": "<shortcut id 또는 null>",
              "right": "<shortcut id 또는 null>"
            }

            [트레이 후보 (mock)]
            $trayText

            [자유 배치 후보 (실제 설치 앱 위젯)]
            $floatingText

            [바로가기 후보]
            $shortcutText
        """.trimIndent()
        val raw = callGemini(prompt)
        parseRecommendation(raw)
    }

    /**
     * 사용자의 자연어 정보 입력을 (레이블, 샘플값) 쌍 목록으로 파싱.
     * API 키가 없거나 LLM 호출이 실패하면 콤마 split + 더미값으로 fallback.
     */
    suspend fun parseInfoItems(userQuery: String): List<Pair<String, String>> =
        withContext(Dispatchers.IO) {
            if (API_KEY.isBlank() || API_KEY == "YOUR_GEMINI_API_KEY") {
                return@withContext commaFallback(userQuery)
            }
            runCatching {
                val prompt = """
                    사용자가 잠금화면 위젯에 표시하고 싶은 정보를 입력했습니다: "${userQuery.trim()}"

                    이 입력을 위젯에 표시할 정보 항목 목록으로 변환해주세요.
                    각 항목은 2~6자의 짧은 레이블과 그 정보의 대표 샘플 값으로 구성됩니다.
                    최대 4개 항목만. 반드시 JSON으로만 응답하세요.

                    응답 스키마:
                    {"items": [{"label": "날씨", "value": "맑음"}, {"label": "온도", "value": "23°C"}]}
                """.trimIndent()
                val raw = callGemini(prompt)
                parseInfoItemsJson(raw).takeIf { it.isNotEmpty() } ?: commaFallback(userQuery)
            }.getOrElse { commaFallback(userQuery) }
        }

    private fun parseInfoItemsJson(text: String): List<Pair<String, String>> {
        val obj = runCatching { JSONObject(sanitizeJson(text)) }.getOrNull() ?: return emptyList()
        val arr = obj.optJSONArray("items") ?: return emptyList()
        return List(arr.length()) { i ->
            val item = arr.optJSONObject(i) ?: return@List null
            val label = item.optString("label", "").trim()
            val value = item.optString("value", "--").trim()
            if (label.isBlank()) null else label to value
        }.filterNotNull().take(4)
    }

    private fun commaFallback(userQuery: String): List<Pair<String, String>> =
        userQuery.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(4)
            .map { label -> label to fallbackValueFor(label) }

    private fun fallbackValueFor(label: String): String {
        val lower = label.lowercase()
        return when {
            lower.contains("날씨") || lower.contains("weather") -> "맑음"
            lower.contains("온도") || lower.contains("기온") || lower.contains("temp") -> "23°C"
            lower.contains("습도") || lower.contains("humidity") -> "45%"
            lower.contains("시간") || lower.contains("time") -> {
                val cal = java.util.Calendar.getInstance()
                "%d:%02d".format(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
            }
            lower.contains("날짜") || lower.contains("date") -> {
                val cal = java.util.Calendar.getInstance()
                "${cal.get(java.util.Calendar.MONTH) + 1}월 ${cal.get(java.util.Calendar.DAY_OF_MONTH)}일"
            }
            lower.contains("걸음") || lower.contains("step") -> "4,350"
            lower.contains("배터리") || lower.contains("battery") -> "72%"
            lower.contains("미세먼지") || lower.contains("dust") -> "좋음 15"
            lower.contains("강수") || lower.contains("rain") -> "10%"
            lower.contains("자외선") || lower.contains("uv") -> "보통 5"
            else -> "--"
        }
    }

    /**
     * 사용자 입력(imageShape + infoQuery)에서 적합한 위젯 템플릿을 선택한다.
     * - "공부/메모/할 일/일정" 키워드 → LABEL_BOARD
     * - "캐릭터/귀여운/동물/우주/스티커" 등 감성형 → STICKER
     * - 그 외 날씨/운동/기본 정보형 → GLASS_INFO
     */
    fun selectTemplate(imageShape: String, infoQuery: String): AiWidgetTemplateType {
        val combined = (imageShape + " " + infoQuery).lowercase()
        val labelBoardKeywords = listOf("공부", "할 일", "할일", "메모", "일정", "노트", "todo", "체크", "리스트")
        val stickerKeywords = listOf(
            "목장", "양", "고양이", "강아지", "우주", "행성", "캐릭터", "귀여운", "귀엽",
            "스티커", "동물", "판타지", "드래곤", "공룡", "토끼", "곰", "펭귄", "별", "구름",
        )
        return when {
            labelBoardKeywords.any { combined.contains(it) } -> AiWidgetTemplateType.LABEL_BOARD
            stickerKeywords.any { combined.contains(it) } -> AiWidgetTemplateType.STICKER
            else -> AiWidgetTemplateType.GLASS_INFO
        }
    }

    /**
     * Full Bleed 방식의 분위기/풍경 이미지 프롬프트를 생성한다.
     * 위젯 전체를 채울 아름다운 배경 장면을 요청한다.
     * 텍스트는 Compose 글래스 패널이 렌더링하므로 이미지에 글자가 들어가면 안 된다.
     */
    fun buildDecorationPrompt(imageShape: String, templateType: AiWidgetTemplateType): String {
        val subject = imageShape.trim().ifBlank { "a dreamy landscape" }
        val base = when (templateType) {
            AiWidgetTemplateType.STICKER ->
                "A beautiful dreamy scene featuring $subject as the central subject. " +
                "Vibrant rich colors, soft bokeh background, cinematic lighting, " +
                "painterly digital illustration style, fills the entire frame edge to edge."

            AiWidgetTemplateType.GLASS_INFO ->
                "A stunning atmospheric landscape inspired by $subject. " +
                "Beautiful sky, depth and atmosphere, rich color palette, " +
                "cinematic wide composition, fills the entire frame edge to edge, " +
                "painterly illustration style."

            AiWidgetTemplateType.LABEL_BOARD ->
                "A cozy warm illustrated scene featuring $subject. " +
                "Soft warm lighting, gentle pastel colors, storybook illustration style, " +
                "fills the entire frame edge to edge."
        }
        return "$base ABSOLUTELY NO TEXT, NO NUMBERS, NO SYMBOLS, NO LETTERS anywhere in the image."
    }

    /**
     * infoItems를 AiTextSlot으로 변환하고, Full Bleed 배경용 풍경 이미지 프롬프트를 반환한다.
     */
    suspend fun designSketchScene(
        infoItems: List<ResolvedInfoItem>,
        imageShape: String,
        aspectRatio: Float,
    ): SketchScene = withContext(Dispatchers.IO) {
        val slots = infoItems.mapIndexed { index, item ->
            AiTextSlot(
                label = item.label,
                value = item.value,
                role = if (index == 0) "main" else "sub",
                xRatio = 0f, yRatio = 0f, widthRatio = 0f, heightRatio = 0f,
                fontScale = 1f,
                anchorObject = "compose_ui",
                source = item.source,
            )
        }
        val imagePrompt = buildString {
            append("A beautiful atmospheric scene of ")
            append(imageShape.trim().ifBlank { "a dreamy landscape" })
            append(". Painterly digital illustration, rich vibrant colors, ")
            append("cinematic lighting, fills entire frame, high detail. ")
            append("No text, no numbers, no symbols.")
        }
        SketchScene(imagePrompt, slots)
    }

    private fun fallbackScene(
        infoItems: List<ResolvedInfoItem>,
        imageShape: String,
        aspectRatio: Float,
        aspectLabel: String,
    ): SketchScene {
        val slots = infoItems.mapIndexed { index, item ->
            AiTextSlot(
                label = item.label, value = item.value, role = if (index == 0) "main" else "sub",
                xRatio = 0f, yRatio = 0f, widthRatio = 0f, heightRatio = 0f,
                fontScale = 1f, anchorObject = "compose_ui", source = item.source,
            )
        }
        val imagePrompt = "A cute ${imageShape.trim().ifBlank { "subject" }} illustration, solid white background, no text."
        return SketchScene(imagePrompt, slots)
    }

    /**
     * Gemini 이미지 생성 모델로 위젯 이미지를 생성한다.
     * 장면 설계 단계에서 만든 imagePrompt를 그대로 사용하되,
     * 투명 배경 + 글자 없음을 강제하는 안전 접미사를 항상 덧붙인다.
     */
    suspend fun generateWidgetImage(imagePrompt: String): ByteArray = withContext(Dispatchers.IO) {
        if (API_KEY.isBlank() || API_KEY == "YOUR_GEMINI_API_KEY") {
            throw LlmException("Gemini API 키가 설정되지 않았습니다. GeminiClient.API_KEY를 채워주세요.")
        }
        val prompt = imagePrompt.trim() + IMAGE_SAFETY_SUFFIX
        val bodyJson = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(
                    JSONObject().put("text", prompt)
                ))
            ))
            put("generationConfig", JSONObject().apply {
                put("responseModalities", JSONArray().put("TEXT").put("IMAGE"))
            })
        }.toString()
        val request = Request.Builder()
            .url("$IMAGE_ENDPOINT?key=$API_KEY")
            .header("Content-Type", "application/json; charset=utf-8")
            .post(bodyJson.toRequestBody(jsonMedia))
            .build()
        callImageGenWithRetry(request)
    }

    private const val IMAGE_SAFETY_SUFFIX =
        " CRITICAL: Fill the entire image frame with the scene — no empty margins, no white borders. " +
            "ABSOLUTELY NO TEXT, NO NUMBERS, NO SYMBOLS, NO WORDS anywhere in the image. " +
            "High quality, vibrant colors, painterly illustration style."

    private fun callImageGenWithRetry(request: Request): ByteArray {
        var lastErr: Throwable? = null
        for (attempt in 1..MAX_ATTEMPTS) {
            try {
                client.newCall(request).execute().use { resp ->
                    val raw = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        if (resp.code in 500..599 && attempt < MAX_ATTEMPTS) {
                            lastErr = LlmException("서버 오류 ${resp.code}"); return@use
                        }
                        throw LlmException("이미지 생성 호출 실패 (${resp.code}): ${raw.take(300)}")
                    }
                    return parseImageGenBytes(raw)
                }
            } catch (e: LlmException) {
                throw e
            } catch (e: IOException) {
                lastErr = e
                if (attempt >= MAX_ATTEMPTS) throw LlmException("네트워크 오류 (${e.message})", e)
                Thread.sleep(300L * attempt)
            }
        }
        throw LlmException("이미지 생성 호출 실패", lastErr)
    }

    private fun parseImageGenBytes(raw: String): ByteArray {
        val root = JSONObject(raw)
        val candidates = root.optJSONArray("candidates")
            ?: throw LlmException("이미지 생성 응답에 candidates 없음")
        if (candidates.length() == 0) throw LlmException("이미지 생성 응답이 비어있습니다")
        val parts = candidates.getJSONObject(0)
            .optJSONObject("content")
            ?.optJSONArray("parts")
            ?: throw LlmException("이미지 응답에 content.parts 없음")

        val textBuf = StringBuilder()
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            val inlineData = part.optJSONObject("inlineData")
            if (inlineData != null) {
                val encoded = inlineData.optString("data", "")
                if (encoded.isNotBlank()) {
                    return android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
                }
            }
            val text = part.optString("text", "")
            if (text.isNotBlank()) textBuf.append(text)
        }
        val hint = if (textBuf.isNotBlank()) " 모델 응답: ${textBuf.take(300)}" else ""
        throw LlmException("이미지 데이터(inlineData)가 없습니다.$hint")
    }

    private fun aspectRatioLabel(ratio: Float): String = when {
        ratio >= 1.5f -> "16:9"
        ratio >= 1.1f -> "4:3"
        ratio >= 0.65f -> "1:1"
        ratio >= 0.45f -> "3:4"
        else -> "9:16"
    }

    private fun callGemini(prompt: String): String {
        if (API_KEY.isBlank() || API_KEY == "YOUR_GEMINI_API_KEY") {
            throw LlmException("Gemini API 키가 설정되지 않았습니다. GeminiClient.API_KEY를 채워주세요.")
        }
        val body = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(
                    JSONObject().put("text", prompt)
                ))
            ))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("responseMimeType", "application/json")
            })
        }
        val request = Request.Builder()
            .url("$ENDPOINT?key=$API_KEY")
            .header("Content-Type", "application/json; charset=utf-8")
            .header("Accept", "application/json")
            .post(body.toString().toRequestBody(jsonMedia))
            .build()

        var lastError: Throwable? = null
        for (attempt in 1..MAX_ATTEMPTS) {
            try {
                client.newCall(request).execute().use { resp ->
                    val raw = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        if (resp.code in 500..599 && attempt < MAX_ATTEMPTS) {
                            lastError = LlmException("서버 오류 ${resp.code}")
                            return@use
                        }
                        throw LlmException("Gemini 호출 실패 (${resp.code}): ${raw.take(300)}")
                    }
                    return extractText(raw)
                }
            } catch (e: IOException) {
                lastError = e
                if (attempt >= MAX_ATTEMPTS) {
                    throw LlmException(
                        "네트워크 오류로 Gemini에 연결할 수 없습니다 (${e.javaClass.simpleName}: ${e.message}). " +
                            "Wi-Fi/데이터 연결을 확인하세요.",
                        e,
                    )
                }
                Thread.sleep((300L * attempt))
            }
        }
        throw LlmException("Gemini 호출 실패", lastError)
    }

    private fun extractText(rawJson: String): String {
        val root = JSONObject(rawJson)
        val candidates = root.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""
        val content = candidates.getJSONObject(0).optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: return ""
        val sb = StringBuilder()
        for (i in 0 until parts.length()) {
            sb.append(parts.getJSONObject(i).optString("text", ""))
        }
        return sb.toString()
    }

    private fun parseSelectedIds(text: String): List<String> {
        val payload = sanitizeJson(text)
        val obj = runCatching { JSONObject(payload) }.getOrNull()
            ?: throw LlmException("LLM 응답 파싱 실패: $text")
        val arr = obj.optJSONArray("selected") ?: return emptyList()
        return List(arr.length()) { arr.optString(it).orEmpty() }.filter { it.isNotBlank() }
    }

    private fun parseRecommendation(text: String): LlmRecommendation {
        val payload = sanitizeJson(text)
        val obj = runCatching { JSONObject(payload) }.getOrNull()
            ?: throw LlmException("LLM 응답 파싱 실패: $text")
        val tray = obj.optJSONArray("tray").toStringList()
        val floating = obj.optJSONArray("floating").toStringList()
        val left = obj.optString("left").takeIf { it.isNotBlank() && it != "null" }
        val right = obj.optString("right").takeIf { it.isNotBlank() && it != "null" }
        return LlmRecommendation(tray, floating, left, right)
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return List(length()) { optString(it).orEmpty() }.filter { it.isNotBlank() }
    }

    // 모델이 코드펜스(```json ... ```)를 붙이는 경우를 대비한 가벼운 정리
    private fun sanitizeJson(text: String): String {
        val trimmed = text.trim()
        if (!trimmed.startsWith("```")) return trimmed
        val firstNewline = trimmed.indexOf('\n')
        val withoutHead = if (firstNewline >= 0) trimmed.substring(firstNewline + 1) else trimmed.removePrefix("```")
        return withoutHead.removeSuffix("```").trim()
    }
}
