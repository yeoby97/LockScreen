# LockScreen

Jetpack Compose 기반 Android 잠금화면 프로토타입입니다.  
잠금화면에서 위젯·알림·앱 바로가기를 사용자 상황에 맞게 구성하고, Gemini를 활용한 위젯 및 바로가기 추천 기능을 실험한 팀 프로젝트입니다.

---

## 해결하려 한 문제

- 기본 잠금화면에서는 필요한 정보와 기능에 빠르게 접근하기 어렵다.
- 사용자의 자유로운 요청("운동할 때 쓸 화면으로 구성해줘")에 맞춰 잠금화면 레이아웃을 자동으로 추천하는 방법을 실험했다.

---

## 주요 기능

### 잠금화면 UI
- Jetpack Compose로 구현한 전체화면 잠금화면 (`LockScreenActivity`)
- 시스템 내비게이션 바 숨김, 화면 켜기, 키가드 해제 처리

### 위젯 배치 및 편집
- **트레이 슬롯**: 상단 고정 영역에 SMALL(1칸) / WIDE(2칸) mock 위젯 배치 (총 4칸)
- **자유 배치(Floating)**: 실제 설치된 앱 위젯을 드래그·크기조절하여 화면 자유 배치
- **위젯 공간(Space)**: 여러 위젯을 버블 하나로 묶어 보관. 탭하면 확장되어 내부 위젯을 자유 배치
- 실제 `AppWidgetHost` / `AppWidgetManager` 연동, 바인딩 및 설정 흐름 처리

### 앱 바로가기
- 잠금화면 하단 좌·우측에 바로가기 버튼 배치 (시스템 기능 / 설치 앱)
- 앱 사용 통계(`UsageStatsManager`)를 기반으로 자주 쓰는 앱을 즐겨찾기로 표시

### 알림 표시
- `NotificationListenerService`로 실시간 알림 수신
- 채팅 앱 알림을 갤럭시 스타일 스택 카드로 표시 (앱 → 채팅방 → 메시지 계층 구조)
- 알림 카드 확장 / 축소 애니메이션

### Gemini 기반 추천 (AI 기능)
- **위젯 추천**: 사용자 자연어 요청을 받아 Gemini가 트레이 위젯·자유 위젯·바로가기를 한 번에 선별
- **넛지 분석**: 채팅 알림 메시지에서 "일정 추가" / "지도 열기" 같은 액션 가능 항목을 AI가 탐지, 잠금화면에 넛지로 표시
- 엔진 우선순위: 온디바이스 Gemini Nano(ML Kit GenAI Prompt API) → 클라우드 Gemini API 자동 폴백

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| 언어 | Kotlin |
| UI | Jetpack Compose, Material3 |
| AI (클라우드) | Gemini API (`gemini-3-flash-preview`, `gemini-3.1-flash-lite`) via OkHttp |
| AI (온디바이스) | Gemini Nano — ML Kit GenAI Prompt API |
| Android API | AppWidgetHost/Manager, NotificationListenerService, UsageStatsManager |
| 빌드 | Gradle (Kotlin DSL), `BuildConfig` 필드로 API 키 주입 |
| 최소 SDK | API 36 (Android 16) |

---

## 프로젝트 구조

```
app/src/main/java/com/example/lockscreencopy/
├── MainActivity.kt              # 앱 진입점 → LockScreenActivity 실행
├── LockScreenActivity.kt        # AppWidgetHost 관리, 위젯 바인딩/설정 흐름
├── model/
│   └── Models.kt                # 데이터 모델 (Widget, Shortcut, Notification, Chat 등)
├── data/
│   ├── GeminiClient.kt          # 클라우드 Gemini API 호출 (위젯 추천)
│   ├── NudgeAnalyzer.kt         # Nano/Cloud 기반 알림 넛지 분석
│   ├── NanoPromptClient.kt      # 온디바이스 Gemini Nano 래퍼
│   ├── LlmCatalog.kt            # Gemini 프롬프트용 위젯·앱 카탈로그 빌더
│   ├── NotificationRepository.kt # 알림 데이터 저장소
│   ├── LockNotificationListenerService.kt # 실시간 알림 수신
│   ├── UsageStatsLoader.kt      # 앱 사용 통계 로딩
│   ├── SystemActionHandler.kt   # 플래시라이트·사운드 등 시스템 액션
│   └── SampleData / DummyLlmData # 개발용 더미 데이터
└── ui/
    ├── LockScreen.kt            # 잠금화면 루트 Composable
    ├── picker/
    │   ├── LlmLayoutSheet.kt    # Gemini 추천 요청 바텀시트
    │   ├── RealWidgetPickerSheet.kt # 실제 앱 위젯 선택
    │   └── ShortcutPickerDialog.kt  # 바로가기 선택
    ├── space/
    │   └── WidgetSpaceExpanded.kt   # 위젯 공간 확장 화면
    ├── widget/
    │   ├── FloatingWidgetItem.kt    # 자유 배치 위젯 (드래그·리사이즈)
    │   ├── HostedWidgetItem.kt      # 실제 AppWidget 렌더링
    │   └── ClockHeader.kt           # 시계·날짜 헤더
    ├── notification/
    │   └── ChatNotificationStack.kt # 채팅 알림 스택 카드
    └── theme/                   # 색상·타이포그래피·글래스 UI 테마
```

---

## Gemini 기능 상세

### 위젯 추천 흐름

```
사용자 자연어 입력
    ↓
buildLlmCatalog(): 기기에 설치된 전체 앱 위젯 + 바로가기 목록 수집
    ↓
GeminiClient.recommendWidgets(): Gemini에게 후보 목록과 함께 프롬프트 전송
    ↓
Gemini 응답 (JSON): { tray: [...], floating: [...], left: "...", right: "..." }
    ↓
LlmCatalog.resolveRecommendation(): 추천 ID를 실제 위젯 객체로 환원
    ↓
잠금화면에 ghost 미리보기 → 사용자 확인 후 배치
```

- 프롬프트는 한국어로 작성되며, 위젯 이름·크기·패키지 정보를 포함한다.
- Gemini는 트레이(4칸 제한)와 자유 배치 영역, 좌·우 바로가기를 한 번에 선별한다.

### 넛지 분석 흐름

```
알림 수신 (NotificationListenerService)
    ↓
NudgeAnalyzer.isCandidate(): 5자 미만 초단문 조기 필터
    ↓
① Gemini Nano (온디바이스, 지원 기기만)
② 클라우드 Gemini API (gemini-3.1-flash-lite) — Nano 미지원/실패 시 폴백
    ↓
판단: { important, label, actions, mapQuery, startIso }
    ↓
알림 카드에 넛지 배지 + 액션 버튼 표시 ("일정 추가" / "지도 열기")
```

### 현재 한계

- Gemini는 추천 결과를 생성하지만, 그 결과를 화면에 적용할지는 사용자가 결정한다. **완전 자율형 AI Agent가 아니다.**
- 추천 품질은 기기에 설치된 앱 위젯 목록의 풍부함에 따라 크게 달라진다.
- Gemini Nano는 AICore를 지원하는 기기(갤럭시 폴드7 등)에서만 동작한다.
- 클라우드 Gemini API는 키 설정 없이는 동작하지 않는다.

---

## 실행 방법

### 요구 환경

- Android Studio Meerkat 이상
- Android SDK 36 (Android 16) 이상이 설치된 실제 기기 또는 에뮬레이터
- (권장) 갤럭시 폴드7 등 Gemini Nano 지원 기기 — 온디바이스 넛지 분석

### 빌드

```bash
git clone https://github.com/yeoby97/lockscreen.git
cd lockscreen
```

### Gemini API 키 설정

프로젝트 루트의 `local.properties` 파일에 다음을 추가합니다.

```properties
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

> **주의**: `local.properties` 파일은 `.gitignore`에 포함되어 있습니다.  
> 실제 API 키를 Git에 커밋하거나 공개 저장소에 올리지 마십시오.

Android Studio에서 프로젝트를 열고 Run(`▶`)을 누르면 빌드와 설치가 진행됩니다.  
API 키 없이도 앱 실행 및 UI 탐색은 가능하지만, Gemini 위젯 추천과 클라우드 넛지 분석은 동작하지 않습니다.

### 추가 권한

앱 실행 후 다음 권한을 수동으로 허용해야 전체 기능이 동작합니다.

- **알림 접근**: 설정 → 알림 접근 → LockScreenCopy 허용
- **앱 사용 정보**: 설정 → 앱 사용 정보 접근 → LockScreenCopy 허용

---

## 스크린샷

> 아래 스크린샷이 필요합니다. 이미지를 캡처 후 `docs/` 폴더에 추가하고 링크를 업데이트하세요.
>
> - 메인 잠금화면 (시계 + 위젯 + 바로가기)
> - 위젯 편집 화면 (자유 배치 + 위젯 공간)
> - Gemini 추천 요청 바텀시트 및 결과
> - 넛지가 표시된 알림 카드

---

## 팀 프로젝트 및 기여

이 프로젝트는 팀이 함께 개발한 프로젝트입니다.

## My Contributions

> 아래 항목은 프로젝트 참여자가 실제 담당 범위에 맞게 작성합니다.

- 담당 기능:
- 주요 구현:
- 문제 해결 경험:
- AI 도구 활용:

---

## 현재 한계

- **프로토타입 단계**: 실제 사용자 테스트나 성능 검증은 수행되지 않았습니다.
- **최소 SDK 36**: Android 16 이상 기기에서만 실행됩니다.
- **Gemini Nano 기기 제한**: AICore 미지원 기기에서는 클라우드 API로 자동 폴백합니다.
- **잠금화면 시스템 권한**: `showWhenLocked` / `DISABLE_KEYGUARD` 사용으로 실제 잠금화면 교체는 아닙니다. Android 시스템 잠금화면 위에 오버레이 형태로 동작합니다.
- **위젯 권한**: 트레이 영역의 mock 위젯은 실제 AppWidget이 아닌 Compose 기반 더미 위젯입니다.
- **지속성 없음**: 위젯 배치는 앱 재시작 시 초기화됩니다.
