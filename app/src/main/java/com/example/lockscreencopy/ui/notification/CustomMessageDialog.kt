package com.example.lockscreencopy.ui.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.lockscreencopy.ui.theme.LockTokens

/**
 * floating 모드에서 호출하는 "알림 메시지 발행" 입력 다이얼로그.
 *
 * 입력한 메시지는 [com.example.lockscreencopy.data.CustomMessagePublisher]를 통해
 * 앱 알림으로 떠서, 실시간 알림과 동일한 온디바이스 넛지 판단을 거친다.
 */
@Composable
fun CustomMessageDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = LockTokens.DialogShape, color = LockTokens.SheetBg) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = "알림 메시지 발행",
                    color = LockTokens.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "입력한 메시지가 앱 알림으로 떠서, 현재 온디바이스 넛지 판단을 그대로 거칩니다.",
                    color = LockTokens.TextSecondary,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "예: 토요일 저녁 7시 홍대 곱창집에서 보자",
                            color = LockTokens.TextTertiary,
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(color = LockTokens.TextPrimary),
                    shape = LockTokens.ShapeSM,
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LockTokens.Accent,
                        unfocusedBorderColor = LockTokens.Border,
                        cursorColor = LockTokens.Accent,
                    ),
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소", color = LockTokens.TextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { if (text.isNotBlank()) onSend(text) },
                        enabled = text.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LockTokens.Accent,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("발송")
                    }
                }
            }
        }
    }
}
