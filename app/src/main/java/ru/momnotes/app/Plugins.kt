package ru.momnotes.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

data class PluginMeta(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val main: String,
    val icon: String,
    val enabled: Boolean,
    val directory: File
)

data class RegistryEntry(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val icon: String,
    val downloadUrl: String
)

/** Пакет «Дополнительных» плагинов: ставит набор совместимых плагинов одной кнопкой. */
data class ExtraPackage(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val pluginIds: List<String>
)

/**
 * Управление плагинами заметок.
 *
 * Каждый плагин — папка вида <filesDir>/plugins/<id>/ c файлами:
 *   manifest.json — паспорт плагина (id, name, version, author, description, main, icon)
 *   plugin.js     — код плагина: activate(app) встраивает интерфейс, deactivate() срабатывает при закрытии
 *
 * Данные плагина хранятся отдельно, в <filesDir>/plugin_data/<id>.json — чтобы обновление
 * или удаление плагина не трогало данные пользователя.
 */
object PluginManager {

    const val DEFAULT_REGISTRY_URL =
        "https://raw.githubusercontent.com/Legoman-1337/notes-plugins-registry/main/index.json"

    private const val PREFS = "mom_notes_plugins"
    private const val KEY_ENABLED_PREFIX = "enabled_"
    private const val KEY_THEME_PREFIX = "theme_"
    private const val KEY_REGISTRY_URL = "registry_url"
    private const val KEY_PREINSTALLED = "preinstalled_v1"

    fun pluginsDir(context: Context): File = File(context.filesDir, "plugins").apply { mkdirs() }

    private fun dataDir(context: Context): File = File(context.filesDir, "plugin_data").apply { mkdirs() }

    fun dataFile(context: Context, pluginId: String): File = File(dataDir(context), "$pluginId.json")

    fun registryUrl(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_REGISTRY_URL, DEFAULT_REGISTRY_URL) ?: DEFAULT_REGISTRY_URL

    fun setRegistryUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_REGISTRY_URL, url.trim()).apply()
    }

    fun pluginTheme(context: Context, pluginId: String): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME_PREFIX + pluginId, "").orEmpty()

    fun setPluginTheme(context: Context, pluginId: String, themeId: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME_PREFIX + pluginId, themeId).apply()
    }

    /** Читает manifest.json из папки плагина. */
    fun readManifest(dir: File): PluginMeta? {
        val manifest = File(dir, "manifest.json")
        if (!manifest.isFile) return null
        return try {
            val json = JSONObject(manifest.readText(Charsets.UTF_8))
            PluginMeta(
                id = json.optString("id").trim(),
                name = json.optString("name", json.optString("id")).trim(),
                version = json.optString("version", "1.0.0"),
                author = json.optString("author"),
                description = json.optString("description"),
                main = json.optString("main", "plugin.js").ifBlank { "plugin.js" },
                icon = json.optString("icon", "🧩").ifBlank { "🧩" },
                enabled = false,
                directory = dir
            )
        } catch (_: Exception) {
            null
        }
    }

    /** Список установленных плагинов с состоянием «включён». */
    fun installed(context: Context): List<PluginMeta> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return pluginsDir(context).listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { dir ->
                readManifest(dir)?.let { meta ->
                    meta.copy(enabled = prefs.getBoolean(KEY_ENABLED_PREFIX + meta.id, false))
                }
            }
            ?.sortedBy { it.name.lowercase() }
            .orEmpty()
    }

    fun isEnabled(context: Context, pluginId: String): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED_PREFIX + pluginId, false)

    fun setEnabled(context: Context, pluginId: String, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED_PREFIX + pluginId, enabled).apply()
    }

    fun isInstalled(context: Context, pluginId: String): Boolean =
        File(pluginsDir(context), pluginId).let { it.isDirectory && readManifest(it) != null }

    /** Установка плагина из архива (.zip) или одиночного .js файла. */
    fun install(context: Context, pluginId: String, bytes: ByteArray): PluginMeta {
        val target = File(pluginsDir(context), sanitizeId(pluginId))
        if (!target.exists()) target.mkdirs()
        if (looksLikeZip(bytes)) {
            unzip(bytes, target)
        } else {
            File(target, "plugin.js").writeBytes(bytes)
        }
        val meta = readManifest(target)
            ?: run {
                target.deleteRecursively()
                throw IOException("В плагине нет manifest.json")
            }
        setEnabled(context, meta.id, true)
        return meta.copy(enabled = true)
    }

    fun uninstall(context: Context, pluginId: String) {
        setEnabled(context, pluginId, false)
        File(pluginsDir(context), sanitizeId(pluginId)).deleteRecursively()
    }

    /**
     * Плагины, вшитые в приложение (assets/plugins/<id>/…), ставятся только
     * в сборке для мамы (momEdition=mom). Публичная сборка стартует без плагинов —
     * их ставят сами через «+ Плагины».
     */
    fun preinstallBundled(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_PREINSTALLED, false)) return
        prefs.edit().putBoolean(KEY_PREINSTALLED, true).apply()
        if (BuildConfig.MOM_EDITION != "mom") return
        val assets = try {
            context.assets.list("plugins")?.toList().orEmpty()
        } catch (_: Exception) {
            emptyList<String>()
        }
        assets.forEach { pluginId ->
            val target = File(pluginsDir(context), pluginId)
            if (!target.isDirectory) {
                target.mkdirs()
                copyAssetDir(context, "plugins/$pluginId", target)
            }
            if (readManifest(target) != null) {
                setEnabled(context, pluginId, true)
            }
        }
    }

    private fun copyAssetDir(context: Context, assetPath: String, target: File) {
        val children = try {
            context.assets.list(assetPath)?.toList().orEmpty()
        } catch (_: Exception) {
            emptyList<String>()
        }
        if (children.isEmpty()) {
            context.assets.open(assetPath).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        } else {
            target.mkdirs()
            children.forEach { child -> copyAssetDir(context, "$assetPath/$child", File(target, child)) }
        }
    }

    /** Читает manifest дополнительного вшитого плагина из assets приложения. */
    fun readBundledExtra(context: Context, pluginId: String): PluginMeta? {
        val json = runCatching {
            context.assets.open("plugins_extra/$pluginId/manifest.json")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrNull() ?: return null
        return runCatching {
            val m = JSONObject(json)
            val installed = isInstalled(context, pluginId)
            PluginMeta(
                id = m.optString("id").ifBlank { pluginId },
                name = m.optString("name", pluginId).ifBlank { pluginId },
                version = m.optString("version", "1.0.0"),
                author = m.optString("author", "Legoman"),
                description = m.optString("description"),
                main = m.optString("main", "plugin.js").ifBlank { "plugin.js" },
                icon = m.optString("icon", "🧩").ifBlank { "🧩" },
                enabled = installed && isEnabled(context, pluginId),
                directory = File(pluginsDir(context), pluginId)
            )
        }.getOrNull()
    }

    /** Ставит дополнительный вшитый плагин (копирование из assets, без интернета). */
    fun installBundledExtra(context: Context, pluginId: String): PluginMeta {
        val target = File(pluginsDir(context), sanitizeId(pluginId))
        if (!target.exists()) target.mkdirs()
        listOf("manifest.json", "plugin.js").forEach { child ->
            context.assets.open("plugins_extra/$pluginId/$child").use { input ->
                File(target, child).outputStream().use { output -> input.copyTo(output) }
            }
        }
        val meta = readManifest(target) ?: run {
            target.deleteRecursively()
            throw IOException("В плагине нет manifest.json")
        }
        setEnabled(context, meta.id, true)
        return meta.copy(enabled = true)
    }

    /** Ставит все недостающие плагины пакета; возвращает, сколько установлено. */
    fun installExtraPackage(context: Context, pkg: ExtraPackage): Int {
        var count = 0
        pkg.pluginIds.forEach { id ->
            if (!isInstalled(context, id)) {
                installBundledExtra(context, id)
                count++
            }
        }
        return count
    }

    /** Загружает index.json из реестра (GitHub). */
    fun fetchRegistry(url: String): List<RegistryEntry> {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (connection.responseCode !in 200..299) {
                throw IOException("Реестр недоступен: HTTP ${connection.responseCode}")
            }
            val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val array = JSONArray(body)
            return (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val id = item.optString("id").trim()
                if (id.isEmpty()) return@mapNotNull null
                RegistryEntry(
                    id = id,
                    name = item.optString("name", id).ifBlank { id },
                    version = item.optString("version", ""),
                    author = item.optString("author", ""),
                    description = item.optString("description", ""),
                    icon = item.optString("icon", "🧩").ifBlank { "🧩" },
                    downloadUrl = item.optString("download_url", "")
                )
            }
        } finally {
            connection.disconnect()
        }
    }

    /** Скачивает файл плагина по URL. */
    fun download(url: String): ByteArray {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 15000
        try {
            if (connection.responseCode !in 200..299) {
                throw IOException("Не удалось скачать: HTTP ${connection.responseCode}")
            }
            return connection.inputStream.use { it.readBytes() }
        } finally {
            connection.disconnect()
        }
    }

    private fun looksLikeZip(bytes: ByteArray): Boolean =
        bytes.size >= 4 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()

    private fun unzip(bytes: ByteArray, target: File) {
        val canonicalTarget = target.canonicalPath
        ZipInputStream(bytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            var extracted = false
            while (entry != null) {
                val out = File(target, entry.name)
                if (!out.canonicalPath.startsWith(canonicalTarget + File.separator) && out.canonicalPath != canonicalTarget) {
                    throw IOException("Опасный путь в архиве: ${entry.name}")
                }
                if (entry.isDirectory) {
                    out.mkdirs()
                } else {
                    out.parentFile?.mkdirs()
                    out.outputStream().use { output -> zip.copyTo(output) }
                    extracted = true
                }
                entry = zip.nextEntry
            }
            if (!extracted) throw IOException("Архив пуст")
        }
    }

    private fun sanitizeId(id: String): String =
        id.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "plugin" }
}