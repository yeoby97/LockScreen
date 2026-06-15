package com.example.lockscreencopy.ui.space

import android.appwidget.AppWidgetHost
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreencopy.model.WidgetSpace

/**
 * 펼쳐진 위젯 공간 — 화면을 덮는 유리 패널 안에서 멤버 위젯을 원래 종횡비/크기에 가깝게
 * 세로로 나열해 정보를 모두 관찰할 수 있게 한다. (홈스크린 폴더 확장과 유사)
 *
 * float(편집) 모드에서는 이름 편집·위젯 추가·개별 위젯 빼기·공간 삭제가 모두 가능하다.
 *
 * @param onRename        공간 이름 변경.
 * @param onRemoveMember  멤버 위젯(uid)을 공간에서 빼 잠금화면으로 되돌림.
 * @param onAddWidgets    이 공간에 위젯을 더 담기 위해 스케치 모드로 진입.
 */
@Composable
fun WidgetSpaceExpanded(
    space: WidgetSpace,
    members: List<SpaceMember>,
    appWidgetHost: AppWidgetHost?,
    isFloating: Boolean,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
    onRemoveMember: (String) -> Unit,
    onDragMember: (String, Offset) -> Unit,
    onResizeMember: (String, Float, Float, Float, Float) -> Unit,
    onAddWidgets: () -> Unit,
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
                Box(modifier = Modifier.weight(1f)) {
                    if (isFloating) {
                        EditableTitle(name = space.name, onRename = onRename)
                    } else {
                        Text(
                            text = space.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Text(
                    text = "위젯 ${members.size}개",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                )
                if (isFloating) {
                    Spacer(Modifier.width(10.dp))
                    HeaderIconButton(
                        background = Color(0xFFFF453A),
                        icon = Icons.Filled.Delete,
                        desc = "공간 삭제",
                        onClick = onDelete,
                    )
                }
                Spacer(Modifier.width(8.dp))
                HeaderIconButton(
                    background = Color.White.copy(alpha = 0.18f),
                    icon = Icons.Filled.Close,
                    desc = "닫기",
                    onClick = onClose,
                )
            }

            Spacer(Modifier.size(14.dp))

            if (members.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "이 공간에 위젯이 없습니다.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                    )
                }
            } else {
                // 캔버스가 패널 폭을 그대로 채운다(같은 비율) → 사각형은 하나뿐, 전 영역 자유 배치.
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(SpaceCanvas.ASPECT),
                ) {
                    val scale = maxWidth.value / SpaceCanvas.WIDTH_DP
                    SpaceCanvasView(
                        members = members,
                        layouts = space.layouts,
                        appWidgetHost = appWidgetHost,
                        displayScale = scale,
                        interactive = isFloating,
                        compactContent = false,
                        showFrame = false,
                        onDragMember = onDragMember,
                        onResizeMember = onResizeMember,
                        onRemoveMember = onRemoveMember,
                    )
                }
            }

            if (isFloating) {
                Spacer(Modifier.size(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .clickable { onAddWidgets() }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("위젯 추가", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    background: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = desc, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}

/** float 모드에서 공간 이름을 직접 편집. 연필 아이콘 탭 시 입력 필드로 전환. */
@Composable
private fun EditableTitle(name: String, onRename: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    var draft by remember(name) { mutableStateOf(name) }

    if (editing) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = draft,
                onValueChange = { draft = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.20f))
                    .clickable {
                        val trimmed = draft.trim()
                        if (trimmed.isNotEmpty()) onRename(trimmed)
                        editing = false
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text("확인", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { draft = name; editing = true },
        ) {
            Text(
                text = name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Filled.Edit,
                contentDescription = "이름 편집",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
