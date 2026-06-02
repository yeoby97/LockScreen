package com.example.lockscreencopy.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * One UI 다크 글래스 선택 다이얼로그.
 *
 * 흰 카드 대신 어두운 시트 톤([LockTokens.SheetBg]) + 반투명 글래스 옵션 행으로 그려서
 * 잠금화면 위에서 자연스럽게 얹히도록 한다. 여러 picker 가 같은 표면을 공유한다.
 *
 * @param options (라벨, onClick) 쌍 목록. 위에서부터 순서대로 표시.
 */
@Composable
fun GlassChoiceDialog(
    title: String,
    options: List<Pair<String, () -> Unit>>,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = LockTokens.DialogShape,
            color = LockTokens.SheetBg,
            shadowElevation = 16.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LockTokens.TextPrimary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
                )
                options.forEachIndexed { index, (label, onClick) ->
                    if (index > 0) Spacer(Modifier.height(8.dp))
                    GlassOptionRow(label = label, onClick = onClick)
                }
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .clip(LockTokens.ShapeMD)
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Text("취소", color = LockTokens.TextSecondary, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun GlassOptionRow(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LockTokens.ShapeMD)
            .background(LockTokens.GlassWhiteSoft)
            .border(1.dp, LockTokens.BorderSoft, LockTokens.ShapeMD)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = LockTokens.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
