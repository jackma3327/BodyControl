package com.bodycontrol.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 备份/恢复：把练习记录、提醒、自定义清单及其媒体文件打包成一个 zip。
 * zip 结构：backup.json + media/<fileName>。导入为合并（不清空现有数据）。
 */
object BackupManager {
    private const val ENTRY_JSON = "backup.json"
    private const val MEDIA_PREFIX = "media/"
    private const val VERSION = 1

    fun export(context: Context, out: OutputStream): Boolean = runCatching {
        ZipOutputStream(BufferedOutputStream(out)).use { zip ->
            zip.putNextEntry(ZipEntry(ENTRY_JSON))
            zip.write(buildJson().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            CustomRepository.items.value.forEach { item ->
                val f = item.file(context)
                if (f.exists()) {
                    zip.putNextEntry(ZipEntry(MEDIA_PREFIX + item.fileName))
                    f.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        }
    }.isSuccess

    /** 返回是否成功导入。成功后需由调用方重新注册提醒闹钟。 */
    fun import(context: Context, input: InputStream): Boolean = runCatching {
        val mediaDir = File(context.filesDir, CustomItem.DIR).apply { mkdirs() }
        var jsonText: String? = null

        ZipInputStream(BufferedInputStream(input)).use { zip ->
            var entry: ZipEntry? = zip.nextEntry
            while (entry != null) {
                val name = entry.name
                when {
                    name == ENTRY_JSON -> jsonText = zip.readBytes().toString(Charsets.UTF_8)
                    name.startsWith(MEDIA_PREFIX) && !entry.isDirectory -> {
                        val safe = File(name.removePrefix(MEDIA_PREFIX)).name // 防路径穿越
                        if (safe.isNotBlank()) {
                            File(mediaDir, safe).outputStream().use { zip.copyTo(it) }
                        }
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        val text = jsonText
        if (text != null) {
            applyJson(context, text, mediaDir)
            true
        } else {
            false
        }
    }.getOrDefault(false)

    private fun buildJson(): String {
        val records = JSONArray()
        PracticeRepository.records.value.forEach { r ->
            records.put(
                JSONObject()
                    .put("trackId", r.trackId)
                    .put("title", r.title)
                    .put("timestamp", r.timestamp)
                    .put("category", r.category)
            )
        }
        val reminders = JSONArray()
        PracticeRepository.reminders.value.forEach { rm ->
            reminders.put(
                JSONObject()
                    .put("id", rm.id)
                    .put("hour", rm.hour)
                    .put("minute", rm.minute)
                    .put("enabled", rm.enabled)
                    .put("label", rm.label)
            )
        }
        val custom = JSONArray()
        CustomRepository.items.value.forEach { c ->
            custom.put(
                JSONObject()
                    .put("id", c.id)
                    .put("title", c.title)
                    .put("category", c.category)
                    .put("kind", c.kind.name)
                    .put("fileName", c.fileName)
            )
        }
        return JSONObject()
            .put("version", VERSION)
            .put("records", records)
            .put("reminders", reminders)
            .put("custom", custom)
            .toString()
    }

    private fun applyJson(context: Context, text: String, mediaDir: File) {
        val root = JSONObject(text)

        val records = root.optJSONArray("records")?.let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                PracticeRecord(
                    trackId = o.getString("trackId"),
                    title = o.getString("title"),
                    timestamp = o.getLong("timestamp"),
                    category = o.optString("category", ""),
                )
            }
        }.orEmpty()

        val reminders = root.optJSONArray("reminders")?.let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Reminder(
                    id = o.getInt("id"),
                    hour = o.getInt("hour"),
                    minute = o.getInt("minute"),
                    enabled = o.optBoolean("enabled", true),
                    label = o.optString("label", ""),
                )
            }
        }.orEmpty()

        val custom = root.optJSONArray("custom")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                val fileName = o.getString("fileName")
                // 仅恢复媒体文件确实存在的条目，避免出现无法播放的空条目
                if (!File(mediaDir, fileName).exists()) return@mapNotNull null
                CustomItem(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    category = o.optString("category", ""),
                    kind = runCatching { MediaKind.valueOf(o.optString("kind", MediaKind.AUDIO.name)) }
                        .getOrDefault(MediaKind.AUDIO),
                    fileName = fileName,
                )
            }
        }.orEmpty()

        PracticeRepository.restore(context, records, reminders)
        CustomRepository.restore(context, custom)
    }
}
