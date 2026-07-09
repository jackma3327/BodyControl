package com.bodycontrol.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.core.app.NotificationManagerCompat
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bodycontrol.data.CustomRepository
import com.bodycontrol.data.PracticeRecord
import com.bodycontrol.data.PracticeRepository
import com.bodycontrol.data.PracticeTypes
import com.bodycontrol.data.Reminder
import com.bodycontrol.notify.ReminderScheduler
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

private val heroColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))

@Composable
fun MineScreen(
    records: List<PracticeRecord>,
    reminders: List<Reminder>,
    bottomInset: Dp,
) {
    val context = LocalContext.current
    val customItems by CustomRepository.items.collectAsStateWithLifecycle()
    var showLogDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp + bottomInset),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { MineHeader(records) }

        item {
            Section("本周练习") {
                WeekStrip(records)
            }
        }

        item {
            Section("练习提醒") {
                NotificationHint()
                ReminderList(
                    reminders = reminders,
                    onToggle = { r ->
                        val updated = r.copy(enabled = !r.enabled)
                        PracticeRepository.upsertReminder(context, updated)
                        ReminderScheduler.schedule(context, updated)
                    },
                    onDelete = { r ->
                        ReminderScheduler.cancel(context, r)
                        PracticeRepository.removeReminder(context, r.id)
                    },
                    onAdd = { showReminderDialog = true },
                )
            }
        }

        item {
            Section("最近记录") {
                LogPracticeButton(onClick = { showLogDialog = true })
                RecentRecords(records)
            }
        }
    }

    if (showLogDialog) {
        LogPracticeDialog(
            customTitles = customItems.map { it.title },
            onDismiss = { showLogDialog = false },
            onPick = { title, category ->
                PracticeRepository.addManualPractice(context, title, category)
                showLogDialog = false
            },
        )
    }

    if (showReminderDialog) {
        AddReminderDialog(
            onDismiss = { showReminderDialog = false },
            onPickTime = { label ->
                showReminderDialog = false
                val now = Calendar.getInstance()
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val reminder = Reminder(
                            id = PracticeRepository.nextReminderId(context),
                            hour = hour,
                            minute = minute,
                            enabled = true,
                            label = label,
                        )
                        PracticeRepository.upsertReminder(context, reminder)
                        ReminderScheduler.schedule(context, reminder)
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true,
                ).show()
            },
        )
    }
}

/* ---------- 手动打卡 ---------- */

@Composable
private fun LogPracticeButton(onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(heroColors))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
        Text(
            "记录一次练习",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LogPracticeDialog(
    customTitles: List<String>,
    onDismiss: () -> Unit,
    onPick: (title: String, category: String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录练习") },
        text = {
            Column {
                Text(
                    "选择运动种类",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PracticeTypes.forEach { type ->
                        PickChip(type) { onPick(type, type) }
                    }
                }
                if (customTitles.isNotEmpty()) {
                    Text(
                        "我的自定义",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        customTitles.forEach { title ->
                            PickChip(title) { onPick(title, "自定义") }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun PickChip(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun AddReminderDialog(
    onDismiss: () -> Unit,
    onPickTime: (label: String) -> Unit,
) {
    var label by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建提醒") },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    singleLine = true,
                    label = { Text("提醒内容（可选）") },
                    placeholder = { Text("如：晚间拉伸") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "下一步选择每天提醒的时间",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onPickTime(label.trim()) }) {
                Icon(Icons.Filled.EditCalendar, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  选择时间")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

/* ---------- 顶部统计 ---------- */

@Composable
private fun MineHeader(records: List<PracticeRecord>) {
    val total = records.size
    val streak = streakDays(records)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.linearGradient(heroColors))
            .statusBarsPadding()
            .padding(20.dp),
    ) {
        Column {
            Text("我的", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(
                "坚持练习，感受身体的变化",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 2.dp),
            )
            Row(
                Modifier.padding(top = 18.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatBubble("累计练习", "$total", "次", Modifier.weight(1f))
                StatBubble("连续天数", "$streak", "天", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatBubble(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(vertical = 14.dp, horizontal = 16.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.9f))
        Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(
                unit,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(start = 3.dp, bottom = 4.dp),
            )
        }
    }
}

/* ---------- 区块容器 ---------- */

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 10.dp, start = 2.dp),
        )
        content()
    }
}

/* ---------- 本周活动条 ---------- */

@Composable
private fun WeekStrip(records: List<PracticeRecord>) {
    val today = LocalDate.now()
    val practiced = records.map { it.date }.toSet()
    val weekdayNames = listOf("一", "二", "三", "四", "五", "六", "日")
    // 最近 7 天，从 6 天前到今天
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }

    Row(
        Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        days.forEach { day ->
            val done = day in practiced
            val isToday = day == today
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    weekdayNames[day.dayOfWeek.value - 1],
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    Modifier
                        .padding(top = 8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (done) Brush.linearGradient(heroColors)
                            else Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (done) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Text(
                            "${day.dayOfMonth}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/* ---------- 提醒列表 ---------- */

@Composable
private fun NotificationHint() {
    val context = LocalContext.current
    if (NotificationManagerCompat.from(context).areNotificationsEnabled()) return
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .clickable {
                runCatching {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    )
                }
            }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.NotificationsOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(22.dp),
        )
        Text(
            "通知未开启，提醒将无法送达 · 点此开启",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}

@Composable
private fun ReminderList(
    reminders: List<Reminder>,
    onToggle: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit,
    onAdd: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        reminders.forEach { reminder ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(heroColors)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(
                        reminder.timeText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        if (reminder.enabled) "每天提醒" else "已关闭",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = reminder.enabled, onCheckedChange = { onToggle(reminder) })
                Box(
                    Modifier
                        .padding(start = 6.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onDelete(reminder) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // 添加按钮
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onAdd)
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(
                "添加提醒",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

/* ---------- 最近记录 ---------- */

@Composable
private fun RecentRecords(records: List<PracticeRecord>) {
    if (records.isEmpty()) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "还没有练习记录，去练习一次吧～",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    val formatter = DateTimeFormatter.ofPattern("MM月dd日 HH:mm")
    val recent = records.sortedByDescending { it.timestamp }.take(12)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        recent.forEach { record ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(heroColors)),
                )
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(
                        record.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    if (record.category.isNotBlank()) {
                        Box(
                            Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                record.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Text(
                    java.time.Instant.ofEpochMilli(record.timestamp)
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(formatter),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/* ---------- 连续天数计算 ---------- */

private fun streakDays(records: List<PracticeRecord>): Int {
    val dates = records.map { it.date }.toSet()
    if (dates.isEmpty()) return 0
    val today = LocalDate.now()
    // 从今天（或昨天）起向前连续计数
    var day = when {
        today in dates -> today
        today.minusDays(1) in dates -> today.minusDays(1)
        else -> return 0
    }
    var count = 0
    while (day in dates) {
        count++
        day = day.minus(1, ChronoUnit.DAYS)
    }
    return count
}
