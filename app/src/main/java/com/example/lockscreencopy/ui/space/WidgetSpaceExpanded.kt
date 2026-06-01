package com.example.lockscreencopy.ui.space

import android.appwidget.AppWidgetHost
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.WidgetSpace

/**
 * 펼쳐진 위젯 공간 — 화면을 덮는 유리 패널 안에서 멤버 위젯을 원래 종횡비/크기에 가깝게
 * 세로로 나열해 정보를 모두 관찰할 수 있게 한다. (홈스크린 폴더 확장과 유사)
 *
 * float(편집) 모드에서는 헤더에 공간 삭제 버튼이 노출된다.
 */
@Composable
fun WidgetSpaceExpanded(
    space: WidgetSpace,
    members: List<SpaceMember>,
    appWidgetHost: AppWidgetHost?,
    isFloating: Boolean,
    onClose: () -> Unit,
    onDelete: () -> Unit,
) {
    BackHandler { onClose() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            // 패널 바깥을 탭하면 닫힘
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClose() },
        contentAlignment = Alignment.Center,
    ) {
        // 유리 패널 — 내부 클릭은 닫기로 전파되지 않도록 자체 클릭 소비
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 640.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.20f),
                            Color.White.copy(alpha = 0.06f),
                        ),
                    ),
                )
                .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { /* 패널 내부 탭 소비 */ }
                .padding(16.dp),
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = space.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "위젯 ${members.size}개",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                )
                if (isFloating) {
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF453A))
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "공간 삭제",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "닫기",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(Modifier.size(14.dp))

            if (members.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "이 공간에 위젯이 없습니다.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    members.forEach { member ->
                        // 종횡비를 유지하되 너무 길어지지 않도록 높이 제한
                        val aspect = (member.aspectW / member.aspectH).coerceIn(0.5f, 3f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(aspect)
                                .heightIn(max = 220.dp)
                                .clip(RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            SpaceMemberView(
                                member = member,
                                appWidgetHost = appWidgetHost,
                                compact = false,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                    Spacer(Modifier.size(4.dp))
                }
            }
        }
    }
}
