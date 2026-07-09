package com.bodycontrol.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

enum class MediaKind { VIDEO, AUDIO }

/** 用户上传的自定义运动媒体（视频或音频），文件复制保存在 App 私有目录。 */
data class CustomItem(
    val id: String,
    val title: String,
    val category: String,
    val kind: MediaKind,
    val fileName: String,
) {
    fun file(context: Context): File = File(File(context.filesDir, DIR), fileName)

    companion object {
        const val DIR = "custom_media"
    }
}

/** 自定义媒体仓库：元数据存 SharedPreferences，文件存私有目录。 */
object CustomRepository {
    private const val PREFS = "body_control_custom"
    private const val KEY_ITEMS = "custom_items"

    private var prefs: SharedPreferences? = null

    private val _items = MutableStateFlow<List<CustomItem>>(emptyList())
    val items: StateFlow<List<CustomItem>> = _items.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        _items.value = load(p)
    }

    /**
     * 将选中的媒体复制进 App 私有目录并保存记录。会进行阻塞式 IO，请在后台线程调用。
     * 返回新建的条目，失败返回 null。
     */
    fun addItem(
        context: Context,
        title: String,
        category: String,
        kind: MediaKind,
        source: Uri,
    ): CustomItem? {
        init(context)
        val app = context.applicationContext
        val dir = File(app.filesDir, CustomItem.DIR).apply { mkdirs() }
        val id = "c_${System.currentTimeMillis()}"
        val ext = extensionFor(app, source, kind)
        val fileName = "$id.$ext"
        val dest = File(dir, fileName)

        val ok = runCatching {
            app.contentResolver.openInputStream(source)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
        }.isSuccess
        if (!ok || !dest.exists() || dest.length() == 0L) {
            dest.delete()
            return null
        }

        val item = CustomItem(id, title.ifBlank { "未命名" }, category, kind, fileName)
        val updated = _items.value + item
        _items.value = updated
        prefs?.let { save(it, updated) }
        return item
    }

    fun removeItem(context: Context, id: String) {
        init(context)
        _items.value.firstOrNull { it.id == id }?.let { runCatching { it.file(context).delete() } }
        val updated = _items.value.filterNot { it.id == id }
        _items.value = updated
        prefs?.let { save(it, updated) }
    }

    private fun extensionFor(context: Context, uri: Uri, kind: MediaKind): String {
        val fromMime = context.contentResolver.getType(uri)?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
        val fromName = uri.lastPathSegment?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() && it.length <= 5 }
        return (fromName ?: fromMime ?: if (kind == MediaKind.VIDEO) "mp4" else "m4a").lowercase()
    }

    private fun load(p: SharedPreferences): List<CustomItem> {
        val raw = p.getString(KEY_ITEMS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                CustomItem(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    category = o.optString("category", ""),
                    kind = MediaKind.valueOf(o.optString("kind", MediaKind.AUDIO.name)),
                    fileName = o.getString("fileName"),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun save(p: SharedPreferences, list: List<CustomItem>) {
        val arr = JSONArray()
        list.forEach { it ->
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("category", it.category)
                    .put("kind", it.kind.name)
                    .put("fileName", it.fileName)
            )
        }
        p.edit().putString(KEY_ITEMS, arr.toString()).apply()
    }
}
