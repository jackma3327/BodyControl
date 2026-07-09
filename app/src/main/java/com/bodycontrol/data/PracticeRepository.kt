package com.bodycontrol.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** 一次练习打卡记录。 */
data class PracticeRecord(
    val trackId: String,
    val title: String,
    val timestamp: Long,
    /** 运动种类，例如 瑜伽 / 拉伸 / 自定义，可为空。 */
    val category: String = "",
) {
    val date: LocalDate
        get() = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
}

/** 内置的运动种类，供手动打卡选择。 */
val PracticeTypes: List<String> = listOf(
    "瑜伽", "气功", "呼吸法", "拉伸", "有氧", "力量", "冥想", "跑步", "散步", "其他",
)

/** 一条每日练习提醒。 */
data class Reminder(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true,
    val label: String = "",
) {
    val timeText: String get() = "%02d:%02d".format(hour, minute)
}

/**
 * 本地持久化仓库：练习记录 + 提醒设置，基于 SharedPreferences（JSON 编码）。
 * 通过 StateFlow 暴露，Compose 可直接观察。
 */
object PracticeRepository {
    private const val PREFS = "body_control_prefs"
    private const val KEY_RECORDS = "practice_records"
    private const val KEY_REMINDERS = "reminders"
    private const val KEY_NEXT_ID = "reminder_next_id"

    private var prefs: SharedPreferences? = null

    private val _records = MutableStateFlow<List<PracticeRecord>>(emptyList())
    val records: StateFlow<List<PracticeRecord>> = _records.asStateFlow()

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        _records.value = loadRecords(p)
        _reminders.value = loadReminders(p)
    }

    /* ---------- 练习记录 ---------- */

    /** 记录一次练习。同一条目同一天只记录一次，避免重复播放刷记录。 */
    fun logPractice(context: Context, trackId: String, title: String, category: String = "") {
        init(context)
        val today = LocalDate.now()
        if (_records.value.any { it.trackId == trackId && it.date == today }) return
        val updated = _records.value + PracticeRecord(trackId, title, System.currentTimeMillis(), category)
        _records.value = updated
        prefs?.let { saveRecords(it, updated) }
    }

    /** 手动打卡：始终新增一条记录（允许同类多次），用于「记录练习」与「添加到今日练习」。 */
    fun addManualPractice(context: Context, title: String, category: String) {
        init(context)
        val trackId = "manual_${System.currentTimeMillis()}"
        val updated = _records.value + PracticeRecord(trackId, title, System.currentTimeMillis(), category)
        _records.value = updated
        prefs?.let { saveRecords(it, updated) }
    }

    /* ---------- 提醒 ---------- */

    fun upsertReminder(context: Context, reminder: Reminder) {
        init(context)
        val list = _reminders.value.filterNot { it.id == reminder.id } + reminder
        val sorted = list.sortedWith(compareBy({ it.hour }, { it.minute }))
        _reminders.value = sorted
        prefs?.let { saveReminders(it, sorted) }
    }

    fun removeReminder(context: Context, id: Int) {
        init(context)
        val list = _reminders.value.filterNot { it.id == id }
        _reminders.value = list
        prefs?.let { saveReminders(it, list) }
    }

    fun nextReminderId(context: Context): Int {
        init(context)
        val p = prefs ?: return 1
        val id = p.getInt(KEY_NEXT_ID, 1)
        p.edit().putInt(KEY_NEXT_ID, id + 1).apply()
        return id
    }

    /* ---------- 持久化 ---------- */

    private fun loadRecords(p: SharedPreferences): List<PracticeRecord> {
        val raw = p.getString(KEY_RECORDS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                PracticeRecord(
                    trackId = o.getString("trackId"),
                    title = o.getString("title"),
                    timestamp = o.getLong("timestamp"),
                    category = o.optString("category", ""),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun saveRecords(p: SharedPreferences, list: List<PracticeRecord>) {
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(
                JSONObject()
                    .put("trackId", r.trackId)
                    .put("title", r.title)
                    .put("timestamp", r.timestamp)
                    .put("category", r.category)
            )
        }
        p.edit().putString(KEY_RECORDS, arr.toString()).apply()
    }

    private fun loadReminders(p: SharedPreferences): List<Reminder> {
        val raw = p.getString(KEY_REMINDERS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Reminder(
                    id = o.getInt("id"),
                    hour = o.getInt("hour"),
                    minute = o.getInt("minute"),
                    enabled = o.optBoolean("enabled", true),
                    label = o.optString("label", ""),
                )
            }.sortedWith(compareBy({ it.hour }, { it.minute }))
        }.getOrDefault(emptyList())
    }

    private fun saveReminders(p: SharedPreferences, list: List<Reminder>) {
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(
                JSONObject()
                    .put("id", r.id)
                    .put("hour", r.hour)
                    .put("minute", r.minute)
                    .put("enabled", r.enabled)
                    .put("label", r.label)
            )
        }
        p.edit().putString(KEY_REMINDERS, arr.toString()).apply()
    }
}
