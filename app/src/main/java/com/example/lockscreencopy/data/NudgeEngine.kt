package com.example.lockscreencopy.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 넛지(행동 예측) 분석에 사용하는 AI 엔진 종류.
 *
 * 우선순위는 [NANO] → [CLOUD]. 즉 기기가 온디바이스 Gemini Nano를 지원하면
 * Nano로 추론하고, 불가능하면 클라우드 Gemini API로 폴백한다.
 */
enum class NudgeEngine {
    /** 아직 어떤 엔진을 쓸지 확인되지 않음(초기/확인 중). */
    UNKNOWN,

    /** 온디바이스 Gemini Nano (ML Kit GenAI Prompt API, 자유 프롬프트). */
    NANO,

    /** 클라우드 Gemini API. */
    CLOUD,

    /** 사용 가능한 엔진 없음(Nano 미지원 + API 키 미설정). */
    NONE,
}

/**
 * 현재 넛지 분석에 사용 중인 엔진을 UI에 노출하기 위한 전역 상태.
 *
 * 화면에 "온디바이스 Nano" / "클라우드 API" 중 무엇이 동작 중인지 작게 표시하는 데 쓴다.
 */
object NudgeEngineStatus {
    private val _engine = MutableStateFlow(NudgeEngine.UNKNOWN)
    val engine: StateFlow<NudgeEngine> = _engine.asStateFlow()

    fun report(engine: NudgeEngine) {
        _engine.value = engine
    }
}
