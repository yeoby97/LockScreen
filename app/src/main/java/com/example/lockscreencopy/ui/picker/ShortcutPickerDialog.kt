package com.example.lockscreencopy.ui.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class ShortcutChoice { RealWidget, FavoriteApp, Text }

private data class MenuItem(
    val icon: ImageVector,
    val iconTint: Color,
    val label: String,
    val choice: ShortcutChoice,
)

@Composable
fun ShortcutPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (ShortcutChoice) -> Unit,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val menuItems = listOf(
        MenuItem(Icons.Filled.GridView, Color(0xFF6C7BFF), "앱 위젯", ShortcutChoice.RealWidget),
        MenuItem(Icons.Filled.Star,     Color(0xFFFFD60A), "즐겨찾는 앱", ShortcutChoice.FavoriteApp),
        MenuItem(Icons.Filled.Edit,     Color(0xFFFF9F0A), "글 넣기", ShortcutChoice.Text),
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // 바깥 어두운 스크림 — 클릭 시 닫기
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onDismiss() },
        ) {
            // 실제 카드 — LockStar 바 위에 표시 (바 시각 위치 ≈ 화면 하단 30%)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = screenHeight * 0.30f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { /* 카드 내부 클릭이 스크림으로 전달되지 않도록 소비 */ },
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1C1C1E),
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 20.dp, bottom = 14.dp),
                ) {
                    Text(
                        text = "추가할 항목 선택",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp),
                    )

                    menuItems.forEachIndexed { index, item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item.choice) },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.08f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = item.iconTint,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = item.label,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                        if (index < menuItems.lastIndex) Spacer(Modifier.height(8.dp))
                    }

                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDismiss() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("취소", color = Color(0xFF8E8E93), fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
