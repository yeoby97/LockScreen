package com.example.lockscreencopy.data

import android.util.Log
import com.example.lockscreencopy.BuildConfig
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class NudgeResult(
    val hasNudge: Boolean,
    val nudgeLabel: String,
    val actions: List<String>,
    /** 지도 검색용 정제된 장소명. 없으면 빈 문자열. */
    val mapQuery: String = "",
) {
    companion object {
        val NONE = NudgeResult(hasNudge = false, nudgeLabel = "", actions = emptyList())
    }
}

/**
 * 메시지 행동(넛지)을 의미 단위로 분석한다.
 *
 * 구현 메모(데모): 진짜 온디바이스 LLM(Gemini Nano)은 Z플립5 등 AICore 미지원
 * 기기에서 동작하지 않으므로, 데모 단계에서는 클라우드 Gemini API
 * (`gemini-2.5-flash-lite`, 경량·저지연 모델)를 호출한다. UI에서는 "온디바이스 AI"로
 * 표현하되, 추후 지원 기기에서 진짜 온디바이스로 교체할 수 있다.
 *
 * 모든 넛지 판단은 전적으로 이 AI가 담당한다(정규식/키워드 패턴 미사용).
 * 따라서 화면에 뜨는 넛지는 곧 'AI가 찾은 것'이며, 키가 없거나 호출이 실패하면
 * 넛지를 표시하지 않는다([NudgeResult.NONE]).
 *
 * 목적: 단톡방/오픈톡방처럼 메시지가 폭주하는 상황에서, 잡담 사이에 섞인
 * "실제 행동으로 이어질 수 있는(액션 가능)" 중요한 메시지만 넛지로 띄운다.
 */
object NudgeAnalyzer {

    private const val TAG = "NudgeAnalyzer"
    private const val MODEL = "gemini-2.5-flash-lite"
    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val JSON = "application/json".toMediaType()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(8, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    private val apiKey: String get() = BuildConfig.GEMINI_API_KEY

    /** API 키 설정 여부. (키워드/길이와 무관하게 키 상태만 확인) */
    fun hasApiKey(): Boolean = apiKey.isNotBlank()

    private val systemPrompt = """
        당신은 잠금화면 알림 도우미입니다. 단체 채팅방처럼 잡담이 많은 대화 속에서
        사용자가 실제 행동으로 옮길 만한 '중요한' 메시지만 골라내는 역할입니다.

        주어진 알림(앱 이름, 제목, 내용)을 분석해 아래 JSON 스키마로만 답하세요.

        판단 기준:
        - important: 약속/일정/만남/예약/장소 방문/해야 할 일 등 구체적인 '행동'으로
          이어질 수 있는 정보가 있으면 true. 단순 잡담·인사·감정표현·리액션·이모지면 false.
        - label: "일정 추가"(날짜나 시간이 있을 때) 또는 "지도 열기"(갈 만한 장소가 있을 때).
          둘 다 해당하면 "일정 추가". 해당 없으면 "".
        - actions: 제안할 액션 목록. ["일정 추가"], ["지도 열기"],
          ["일정 추가","지도 열기"] 중 하나. 없으면 [].
        - mapQuery: 지도 앱에서 그대로 검색할 '장소명'만 깔끔하게 추출.
          발신자 이름, 조사("~한테","~에서"), 잡설은 절대 포함하지 말고
          실제 상호명이나 지명만 넣으세요. 장소가 없으면 "".
          (예: "최희준한테 맛집 추천받았어" → 특정 장소가 없으므로 "")
          (예: "토요일에 강남역 한우집 갈까?" → "강남역 한우집")

        중요: 날짜·시간이 없어도 "어디 가자/가실래요/가시죠"처럼 특정 장소로의
        방문을 제안하면 그 자체로 행동(지도 열기)이므로 important=true 로 본다.
          (예: "김밥천국 갑시다" → important=true, label="지도 열기",
               actions=["지도 열기"], mapQuery="김밥천국")
    """.trimIndent()

    /**
     * 비용/배터리를 아끼기 위한 값싼 1차 필터. 명백히 분석할 가치가 없는
     * 초단문/빈 메시지는 LLM 호출 없이 걸러낸다. (키워드에 의존하지 않는다.)
     * 키 유무는 여기서 보지 않는다 — 키가 없을 때도 [analyze]가 로그를 남기도록 하기 위함.
     */
    fun isCandidate(title: String, body: String): Boolean {
        val text = "$title $body".trim()
        return text.length >= 5
    }

    /**
     * 메시지를 의미 분석해 넛지 결과를 반환한다. 키가 없거나 호출이 실패하면
     * 넛지를 표시하지 않는다([NudgeResult.NONE]) — 정규식 폴백은 사용하지 않는다.
     */
    suspend fun analyze(title: String, body: String): NudgeResult {
        Log.i(TAG, "분석 시작: title='$title' body='$body' (apiKey 길이=${apiKey.length})")
        if (apiKey.isBlank()) {
            Log.w(TAG, "GEMINI_API_KEY 미설정 — local.properties에 GEMINI_API_KEY를 넣고 다시 빌드하세요. (넛지 없음)")
            return NudgeResult.NONE
        }
        return withContext(Dispatchers.IO) {
            runCatching { requestGemini(title, body) }
                .onSuccess { Log.i(TAG, "분석 결과: $it") }
                .getOrElse {
                    Log.w(TAG, "Gemini 분석 실패, 넛지 없음으로 처리", it)
                    NudgeResult.NONE
                }
        }
    }

    private fun requestGemini(title: String, body: String): NudgeResult {
        val userText = "앱: (알림)\n제목: $title\n내용: $body"
        val payload = JSONObject().apply {
            put("systemInstruction", JSONObject().put("parts", JSONArray().put(textPart(systemPrompt))))
            put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(textPart(userText)))))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0)
                put("responseMimeType", "application/json")
                put("responseSchema", responseSchema())
            })
        }

        val request = Request.Builder()
            .url(ENDPOINT)
            .header("x-goog-api-key", apiKey)
            .post(payload.toString().toRequestBody(JSON))
            .build()

        Log.i(TAG, "Gemini 호출 → $MODEL")
        client.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string().orEmpty()
            Log.i(TAG, "Gemini 응답 HTTP ${response.code}: $bodyStr")
            if (!response.isSuccessful) error("HTTP ${response.code}: $bodyStr")
            return parseResult(bodyStr)
        }
    }

    private fun textPart(text: String) = JSONObject().put("text", text)

    private fun responseSchema(): JSONObject = JSONObject().apply {
        put("type", "OBJECT")
        put("properties", JSONObject().apply {
            put("important", JSONObject().put("type", "BOOLEAN"))
            put("label", JSONObject().put("type", "STRING"))
            put("actions", JSONObject().apply {
                put("type", "ARRAY")
                put("items", JSONObject().put("type", "STRING"))
            })
            put("mapQuery", JSONObject().put("type", "STRING"))
        })
        put("required", JSONArray().put("important").put("label").put("actions").put("mapQuery"))
    }

    private fun parseResult(responseBody: String): NudgeResult {
        val text = JSONObject(responseBody)
            .getJSONArray("candidates").getJSONObject(0)
            .getJSONObject("content").getJSONArray("parts").getJSONObject(0)
            .getString("text")

        Log.i(TAG, "모델 판단 JSON: $text")
        val obj = JSONObject(text)
        val important = obj.optBoolean("important", false)
        if (!important) return NudgeResult.NONE

        val label = obj.optString("label", "")
        val actions = obj.optJSONArray("actions")?.let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        }.orEmpty()
        val mapQuery = obj.optString("mapQuery", "")

        // 액션이 비면 넛지로 보지 않는다.
        if (actions.isEmpty() && label.isBlank()) return NudgeResult.NONE
        return NudgeResult(
            hasNudge = true,
            nudgeLabel = label.ifBlank { actions.first() },
            actions = actions.ifEmpty { listOf(label) },
            mapQuery = mapQuery,
        )
    }
}
