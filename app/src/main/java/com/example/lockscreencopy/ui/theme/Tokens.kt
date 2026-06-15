package com.example.lockscreencopy.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * One UI 스타일 공용 디자인 토큰.
 *
 * 잠금화면 위에 얹히는 모든 표면(BottomSheet · Dialog · Widget card · Dock)이 같은
 * 색/모서리/획 언어를 쓰도록 한곳에 모았다. 하드코딩된 색·radius 를 이 토큰으로 교체하면
 * 전체 톤이 자동으로 통일된다.
 */
object LockTokens {

    // ── Glass surfaces : 배경(벽지) 위에 얹는 반투명 표면 ──────────────
    val GlassWhiteStrong = Color.White.copy(alpha = 0.18f) // 강조 카드/버튼
    val GlassWhite = Color.White.copy(alpha = 0.12f)       // 기본 카드
    val GlassWhiteSoft = Color.White.copy(alpha = 0.08f)   // 은은한 표면
    val DockBg = Color.Black.copy(alpha = 0.32f)           // 하단 dock 배경

    // ── Borders / hairlines ─────────────────────────────────────────
    val BorderSoft = Color.White.copy(alpha = 0.16f)
    val Border = Color.White.copy(alpha = 0.28f)
    val BorderStrong = Color.White.copy(alpha = 0.45f)

    // ── Text (벽지 위) ──────────────────────────────────────────────
    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.68f)
    val TextTertiary = Color.White.copy(alpha = 0.40f)

    // ── Accent : One UI 블루 계열 ───────────────────────────────────
    val Accent = Color(0xFF3E91FF)
    val AccentTrack = Color(0xFF2E6FD0)

    // ── Sheet / Dialog : 불투명 dark 표면 (One UI 다크 시트) ─────────
    val SheetBg = Color(0xFF1C1C1E)
    val SheetSurface = Color(0xFF2C2C2E)
    val SheetSurfaceHigh = Color(0xFF3A3A3C)
    val OnSheetSecondary = Color(0xFF9A9AA0)

    // ── Semantic ────────────────────────────────────────────────────
    val Danger = Color(0xFFFF453A)

    // ── Widget card (벽지와 섞이는 프로스티드 카드) ─────────────────
    val WidgetCardBg = Color.White.copy(alpha = 0.13f)
    val WidgetCardBorder = Color.White.copy(alpha = 0.22f)

    // ── Shapes : 8 / 12 / 16 / 20 / 28 dp 스케일 ────────────────────
    val ShapeSM = RoundedCornerShape(12.dp)
    val ShapeMD = RoundedCornerShape(16.dp)
    val ShapeLG = RoundedCornerShape(20.dp)
    val ShapeXL = RoundedCornerShape(28.dp)
    val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val DialogShape = RoundedCornerShape(28.dp)
}
