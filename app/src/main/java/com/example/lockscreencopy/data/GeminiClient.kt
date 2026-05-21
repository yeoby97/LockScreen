package com.example.lockscreencopy.data

import com.example.lockscreencopy.model.BottomShortcut
import com.example.lockscreencopy.model.LockWidget
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

object GeminiClient {
    // TODO: 실제 키로 교체하세요. (현재는 임시 하드코딩 상수)
    private const val API_KEY: String = "YOUR_GEMINI_API_KEY"
    private const val MODEL = "gemini-2.5-flash"
    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

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
