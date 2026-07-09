package com.bodycontrol.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodycontrol.data.CustomItem
import com.bodycontrol.data.CustomRepository
import com.bodycontrol.data.MediaKind
import com.bodycontrol.data.PracticeRepository
import com.bodycontrol.data.PracticeTypes
import com.bodycontrol.player.PlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val customColors = listOf(Color(0xFF22D3EE), Color(0xFF0EA5E9))

@Composable
fun CustomScreen(
    mediaItems: List<CustomItem>,
    bottomInset: Dp,
    onPlayVideo: (CustomItem) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 选中文件后待填写标题/种类
    var pending by remember { mutableStateOf<Pair<Uri, MediaKind>?>(null) }

    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) pending = uri to MediaKind.VIDEO
    }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) pending = uri to MediaKind.AUDIO
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp + bottomInset),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { CustomHeader() }

        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                UploadButton(
                    icon = Icons.Filled.Videocam,
                    label = "上传视频",
                    modifier = Modifier.weight(1f),
                    onClick = { videoPicker.launch("video/*") },
                )
                UploadButton(
                    icon = Icons.Filled.Audiotrack,
                    label = "上传音频",
                    modifier = Modifier.weight(1f),
                    onClick = { audioPicker.launch("audio/*") },
                )
            }
        }

        if (mediaItems.isEmpty()) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "还没有自定义内容\n上传你自己的运动视频或音频吧",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        items(mediaItems, key = { it.id }) { item ->
            CustomRow(
                item = item,
                onPlay = {
                    if (item.kind == MediaKind.VIDEO) {
                        onPlayVideo(item)
                    } else {
                        PlayerController.playFile(
                            context,
                            item.id,
                            item.title,
                            item.file(context).absolutePath,
                            item.category.ifBlank { "自定义" },
                        )
                    }
                },
                onAddToPractice = {
                    PracticeRepository.addManualPractice(
                        context,
                        item.title,
                        item.category.ifBlank { "自定义" },
                    )
                    Toast.makeText(context, "已添加到今日练习", Toast.LENGTH_SHORT).show()
                },
                onDelete = { CustomRepository.removeItem(context, item.id) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }

    pending?.let { (uri, kind) ->
        AddCustomDialog(
            kind = kind,
            onDismiss = { pending = null },
            onSave = { title, category ->
                pending = null
                scope.launch(Dispatchers.IO) {
                    CustomRepository.addItem(context, title, category, kind, uri)
                }
            },
        )
    }
}

@Composable
private fun CustomHeader() {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.linearGradient(customColors))
            .statusBarsPadding()
            .padding(20.dp),
    ) {
        Column {
            Text("自定义", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(
                "上传保存你的运动视频与音频，随时跟练打卡",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun UploadButton(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = customColors.last())
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(customColors))
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun CustomRow(
    item: CustomItem,
    onPlay: () -> Unit,
    onAddToPractice: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onPlay)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(customColors)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (item.kind == MediaKind.VIDEO) Icons.Filled.Movie else Icons.Filled.MusicNote,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
        }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                "${if (item.kind == MediaKind.VIDEO) "视频" else "音频"} · ${item.category.ifBlank { "自定义" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        IconCircle(Icons.Filled.LibraryAdd, "添加到今日练习", onAddToPractice)
        IconCircle(Icons.Filled.PlayArrow, "播放", onPlay)
        IconCircle(Icons.Filled.DeleteOutline, "删除", onDelete)
    }
}

@Composable
private fun IconCircle(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        Modifier
            .padding(start = 4.dp)
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = desc, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCustomDialog(
    kind: MediaKind,
    onDismiss: () -> Unit,
    onSave: (title: String, category: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PracticeTypes.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (kind == MediaKind.VIDEO) "保存视频" else "保存音频") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "运动种类",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PracticeTypes.forEach { type ->
                        CategoryChip(type, type == category) { category = type }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, category) }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

@Composable
private fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) Brush.linearGradient(customColors)
                else Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
