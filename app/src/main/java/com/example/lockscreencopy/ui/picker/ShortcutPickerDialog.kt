package com.example.lockscreencopy.ui.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ShortcutChoice { RealWidget, FavoriteApp, Text }

private data class MenuItem(
    val icon: ImageVector,
    val iconTint: Color,
    val label: String,
    val choice: ShortcutChoice,
)

/** Dialog 창이 아닌 일반 컴포저블 카드 — 호출부에서 위치/애니메이션을 제어한다. */
@Composable
fun ShortcutPickerDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSelect: (ShortcutChoice) -> Unit,
) {
    val menuItems = listOf(
        MenuItem(Icons.Filled.GridView, Color(0xFF6C7BFF), "앱 위젯",    ShortcutChoice.RealWidget),
        MenuItem(Icons.Filled.Star,     Color(0xFFFFD60A), "즐겨찾는 앱", ShortcutChoice.FavoriteApp),
        MenuItem(Icons.Filled.Edit,     Color(0xFFFF9F0A), "글 넣기",    ShortcutChoice.Text),
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1C1C1E),
        shadowElevation = 6.dp,
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 헤더: LockStar + 닫기
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "LockStar",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "닫기",
                    tint = Color(0xFF8E8E93),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onDismiss() },
                )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.10f), thickness = 0.5.dp)

            menuItems.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(item.choice) }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.iconTint,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = item.label,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (index < menuItems.lastIndex) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.06f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 56.dp),
                    )
                }
            }

            Spacer(Modifier.padding(bottom = 8.dp))
        }
    }
}
