package ru.momnotes.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Base64OutputStream
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.Executors

private val defaultCategories = listOf("Важное")
private val retiredDefaultCategories = setOf(
    "Работа", "Бассейн", "Подготовка к занятию", "Подготовка к индивидуальному занятию"
)

internal fun normalizeSectionName(name: String): String =
    if (name.trim() in retiredDefaultCategories) "Важное" else name.trim()

internal data class AppSettings(
    val theme: String = "Тёмная",
    val accent: String = "Зелёный",
    val headerColor: String = "",
    val sectionColor: String = "",
    val noteColor: String = "",
    val addButtonColor: String = "",
    val tableColor: String = "",
    val iconBackground: String = "Зелёный",
    val noteTextSize: String = "Средний",
    val density: String = "Просторно",
    val corner: String = "Среднее",
    val sortBy: String = "По дате изменения",
    val listStyle: String = "Карточки",
    val autoSave: Boolean = false,
    val confirmDelete: Boolean = true,
    val showModifiedDate: Boolean = true,
    val favoriteBehavior: String = "Поднимать наверх",
    val aiTransfer: String = "Открыть приложение ИИ",
    val language: String = "Русский",
    val vibration: Boolean = true,
    val voiceLanguage: String = "Русский",
    val animateNoteThemes: Boolean = true,
    val keyboardToNoteAnimation: Boolean = true
)

internal data class NoteTable(
    val id: String = UUID.randomUUID().toString(),
    val cells: List<List<String>>
)

internal data class Note(
    val id: String,
    val category: String,
    val title: String,
    val body: String,
    val updatedAt: Long,
    val createdAt: Long = updatedAt,
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val photoPaths: List<String> = emptyList(),
    val attachmentPaths: List<String> = emptyList(),
    val photoIds: List<String> = emptyList(),
    val attachmentIds: List<String> = emptyList(),
    val tables: List<NoteTable> = emptyList(),
    val aiAssistant: String = "Без ИИ",
    val aiInstruction: String = "",
    val reminderAt: Long = 0L,
    val reminders: List<Long> = emptyList(),
    val reminderTypes: Map<Long, String> = emptyMap(),
    val themeId: String = ""
) {
    val plainBody: String by lazy(LazyThreadSafetyMode.NONE) { RichTextDocument.plainText(body) }
}

private val launcherIconAliases = listOf(
    "Красный" to "Red", "Оранжевый" to "Orange", "Жёлтый" to "Yellow",
    "Зелёный" to "Green", "Бирюзовый" to "Teal", "Голубой" to "Sky",
    "Синий" to "Blue", "Фиолетовый" to "Purple", "Розовый" to "Pink",
    "Терракотовый" to "Terra", "Серый" to "Gray", "Чёрный" to "Black"
)

internal val themeColorChoices = listOf(
    "Красный" to "#D64545", "Оранжевый" to "#E67828", "Жёлтый" to "#E8C52E",
    "Зелёный" to "#31824B", "Бирюзовый" to "#168A83", "Голубой" to "#258BC4",
    "Синий" to "#3676C8", "Фиолетовый" to "#8055B7", "Розовый" to "#C14F7A",
    "Терракотовый" to "#C26343", "Серый" to "#69747E", "Чёрный" to "#25282C"
)

internal fun setLauncherIconColor(context: Context, colorName: String) {
    val selected = launcherIconAliases.firstOrNull { it.first == colorName }?.second ?: "Green"
    val manager = context.packageManager
    val classPackage = MainActivity::class.java.name.substringBeforeLast('.')
    launcherIconAliases.forEach { (_, suffix) ->
        try {
            val component = ComponentName(context, "$classPackage.MainActivityIcon$suffix")
            val targetState = if (suffix == selected) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            if (manager.getComponentEnabledSetting(component) != targetState) {
                manager.setComponentEnabledSetting(
                    component,
                    targetState,
                    PackageManager.DONT_KILL_APP
                )
            }
        } catch (_: Exception) {
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(21, 59, 41)
        window.navigationBarColor = android.graphics.Color.rgb(16, 22, 18)
        setLauncherIconColor(applicationContext, readAppSettings(applicationContext).iconBackground)
        setContent { NotesRoot() }
    }
}

internal fun accentColor(name: String): Color = when (name) {
    "Синий" -> Color(0xFF3B7DD8)
    "Фиолетовый" -> Color(0xFF8358C2)
    "Бирюзовый" -> Color(0xFF16918A)
    "Терракотовый" -> Color(0xFFC96846)
    "Розовый" -> Color(0xFFC95480)
    else -> Color(0xFF2B7048)
}

internal fun mixColors(base: Color, tint: Color, amount: Float): Color {
    val fraction = amount.coerceIn(0f, 1f)
    return Color(
        red = base.red * (1f - fraction) + tint.red * fraction,
        green = base.green * (1f - fraction) + tint.green * fraction,
        blue = base.blue * (1f - fraction) + tint.blue * fraction,
        alpha = base.alpha * (1f - fraction) + tint.alpha * fraction
    )
}

internal fun parseHexColor(value: String): Color? {
    return try {
        val digits = value.trim().removePrefix("#")
        val expanded = when {
            digits.matches(Regex("[0-9a-fA-F]{3}")) -> digits.map { "$it$it" }.joinToString("")
            digits.matches(Regex("[0-9a-fA-F]{6}")) -> digits
            else -> null
        }
        expanded?.let { Color(android.graphics.Color.parseColor("#$it")) }
    } catch (_: Exception) {
        null
    }
}

internal fun colorHex(color: Color): String = String.format(Locale.US, "#%06X", color.toArgb() and 0xFFFFFF)

internal fun roleColor(value: String, fallback: Color): Color =
    parseHexColor(value)?.let { mixColors(it, fallback, 0.10f) } ?: fallback

internal fun contrastColor(color: Color): Color {
    fun linear(channel: Float): Float =
        if (channel <= 0.04045f) channel / 12.92f
        else Math.pow(((channel + 0.055f) / 1.055f).toDouble(), 2.4).toFloat()
    val luminance = 0.2126f * linear(color.red) + 0.7152f * linear(color.green) + 0.0722f * linear(color.blue)
    return if (luminance > 0.42f) Color(0xFF161A17) else Color.White
}

internal fun readAppSettings(context: Context): AppSettings {
    val prefs = context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE)
    val savedAccent = prefs.getString("pref_accent", "Зелёный") ?: "Зелёный"
    val cleanAccent = if (savedAccent == "Gemini") "Зелёный" else savedAccent
    return AppSettings(
        theme = prefs.getString("pref_theme", "Тёмная") ?: "Тёмная",
        accent = cleanAccent,
        headerColor = prefs.getString("pref_header_color", "") ?: "",
        sectionColor = prefs.getString("pref_section_color", "") ?: "",
        noteColor = prefs.getString("pref_note_color", "") ?: "",
        addButtonColor = prefs.getString("pref_add_button_color", "") ?: "",
        tableColor = prefs.getString("pref_table_color", "") ?: "",
        iconBackground = prefs.getString("pref_icon_background", "Зелёный") ?: "Зелёный",
        noteTextSize = prefs.getString("pref_text_size", "Средний") ?: "Средний",
        density = prefs.getString("pref_density", "Просторно") ?: "Просторно",
        corner = prefs.getString("pref_corner", "Среднее") ?: "Среднее",
        sortBy = prefs.getString("pref_sort", "По дате изменения") ?: "По дате изменения",
        listStyle = prefs.getString("pref_list_style", "Карточки") ?: "Карточки",
        autoSave = prefs.getBoolean("pref_auto_save", false),
        confirmDelete = prefs.getBoolean("pref_confirm_delete", true),
        showModifiedDate = prefs.getBoolean("pref_show_date", true),
        favoriteBehavior = prefs.getString("pref_favorite", "Поднимать наверх") ?: "Поднимать наверх",
        aiTransfer = prefs.getString("pref_ai_transfer", "Открыть приложение ИИ") ?: "Открыть приложение ИИ",
        language = prefs.getString("pref_language", "Русский") ?: "Русский",
        vibration = prefs.getBoolean("pref_vibration", true),
        voiceLanguage = prefs.getString("pref_voice_language", "Русский") ?: "Русский",
        animateNoteThemes = prefs.getBoolean("pref_animate_note_themes", true),
        keyboardToNoteAnimation = prefs.getBoolean("pref_keyboard_to_note_anim", true)
    )
}

internal fun writeAppSettings(context: Context, s: AppSettings) {
    context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE).edit()
        .putString("pref_theme", s.theme).putString("pref_accent", s.accent)
        .putString("pref_header_color", s.headerColor).putString("pref_section_color", s.sectionColor)
        .putString("pref_note_color", s.noteColor).putString("pref_add_button_color", s.addButtonColor)
        .putString("pref_table_color", s.tableColor).putString("pref_icon_background", s.iconBackground)
        .putString("pref_text_size", s.noteTextSize).putString("pref_density", s.density)
        .putString("pref_corner", s.corner).putString("pref_sort", s.sortBy)
        .putString("pref_list_style", s.listStyle).putBoolean("pref_auto_save", s.autoSave)
        .putBoolean("pref_confirm_delete", s.confirmDelete).putBoolean("pref_show_date", s.showModifiedDate)
        .putString("pref_favorite", s.favoriteBehavior)
        .putString("pref_ai_transfer", s.aiTransfer).putString("pref_language", s.language)
        .putBoolean("pref_vibration", s.vibration).putString("pref_voice_language", s.voiceLanguage)
        .putBoolean("pref_animate_note_themes", s.animateNoteThemes)
        .putBoolean("pref_keyboard_to_note_anim", s.keyboardToNoteAnimation).apply()
}

internal fun cancelNoteReminders(context: Context, note: Note) {
    (note.reminders + listOfNotNull(note.reminderAt.takeIf { it > 0L })).distinct().forEach { time ->
        NoteReminderScheduler.cancel(context, note.id, time)
    }
}

internal fun updateReminder(context: Context, note: Note) {
    cancelNoteReminders(context, note)
    if (!note.isDeleted) {
        (note.reminders + listOfNotNull(note.reminderAt.takeIf { it > 0L })).distinct().forEach { time ->
            NoteReminderScheduler.schedule(
                context,
                note.id,
                note.title.ifBlank { "Напоминание из заметок" },
                time,
                note.reminderTypes[time] ?: "Уведомление и будильник"
            )
        }
    }
}

internal fun decodeThumbnail(path: String): Bitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    while (bounds.outWidth / sample > 320 || bounds.outHeight / sample > 320) sample *= 2
    BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
} catch (_: Exception) {
    null
}

internal fun decodeFullPreview(path: String): Bitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    while (bounds.outWidth / sample > 1600 || bounds.outHeight / sample > 1600) sample *= 2
    BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
} catch (_: Exception) {
    null
}

internal fun copyUriToPrivateFile(context: Context, uri: Uri, folderName: String): File {
    val folder = File(context.filesDir, folderName)
    check(folder.exists() || folder.mkdirs()) { "Не удалось создать папку для вложения" }
    val mime = context.contentResolver.getType(uri)
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)?.let { ".$it" } ?: ".bin"
    val destination = File(folder, "${UUID.randomUUID()}$extension")
    val input = context.contentResolver.openInputStream(uri)
        ?: throw java.io.IOException("Хранилище не предоставило доступ к выбранному файлу")
    input.use { source -> FileOutputStream(destination).use { output -> source.copyTo(output) } }
    return destination
}

internal fun formatDate(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy, HH:mm", Locale("ru")).format(Date(timestamp))

private fun escapeHtml(value: String): String = value
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&#39;")

private fun appendDataUri(writer: BufferedWriter, file: File, mimeType: String) {
    writer.write("data:$mimeType;base64,")
    val bridge = object : OutputStream() {
        override fun write(value: Int) { writer.write(value) }
        override fun write(buffer: ByteArray, offset: Int, length: Int) {
            writer.write(String(buffer, offset, length, Charsets.US_ASCII))
        }
        override fun close() { flush() }
        override fun flush() { writer.flush() }
    }
    Base64OutputStream(bridge, Base64.NO_WRAP).use { encoded ->
        file.inputStream().buffered().use { input -> input.copyTo(encoded) }
    }
}

internal fun shareNoteAsTextFile(context: Context, note: Note) {
    try {
        val folder = File(context.cacheDir, "shared_notes").apply { mkdirs() }
        val safeTitle = note.title.ifBlank { "Заметка" }.replace(Regex("[\\/:*?\"<>|]"), "_").take(80)
        val file = File(folder, "$safeTitle-${System.currentTimeMillis()}.txt")
        val payload = buildString {
            if (note.title.isNotBlank()) append(note.title.trim()).append("\n\n")
            if (note.body.isNotBlank()) append(RichTextDocument.plainText(note.body).trim()).append('\n')
            note.tables.forEachIndexed { index, table ->
                append("\nТаблица ${index + 1}:\n")
                table.cells.forEach { row -> append(row.joinToString("\t") { RichTextDocument.plainText(it) }).append('\n') }
            }
            note.photoPaths.filter { File(it).isFile }.forEach { append("\nФото: ").append(File(it).name) }
            note.attachmentPaths.filter { File(it).isFile }.forEach { append("\nВложение: ").append(File(it).name) }
        }.trim()
        file.writeText(payload, Charsets.UTF_8)
        shareNoteFile(context, file, note.title, "text/plain", "Поделиться TXT заметки")
    } catch (_: Exception) {
        Toast.makeText(context, "Не удалось подготовить TXT-файл заметки.", Toast.LENGTH_LONG).show()
    }
}

internal fun shareNoteAsXml(context: Context, note: Note) {
    try {
        val folder = File(context.cacheDir, "shared_notes").apply { mkdirs() }
        val safeTitle = note.title.ifBlank { "Заметка" }.replace(Regex("[\\/:*?\"<>|]"), "_").take(80)
        val file = File(folder, "$safeTitle-${System.currentTimeMillis()}.xml")
        fun xml(value: String): String = value.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
        val payload = buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<note>\n")
            append("  <title>").append(xml(note.title)).append("</title>\n")
            append("  <body>").append(xml(RichTextDocument.plainText(note.body))).append("</body>\n")
            append("  <tables>\n")
            note.tables.forEach { table ->
                append("    <table>\n")
                table.cells.forEach { row ->
                    append("      <row>")
                    row.forEach { cell -> append("<cell>").append(xml(RichTextDocument.plainText(cell))).append("</cell>") }
                    append("</row>\n")
                }
                append("    </table>\n")
            }
            append("  </tables>\n  <photos>\n")
            note.photoPaths.filter { File(it).isFile }.forEach { append("    <file name=\"").append(xml(File(it).name)).append("\"/>\n") }
            append("  </photos>\n  <attachments>\n")
            note.attachmentPaths.filter { File(it).isFile }.forEach { append("    <file name=\"").append(xml(File(it).name)).append("\"/>\n") }
            append("  </attachments>\n</note>")
        }
        file.writeText(payload, Charsets.UTF_8)
        shareNoteFile(context, file, note.title, "application/xml", "Поделиться XML заметки")
    } catch (_: Exception) {
        Toast.makeText(context, "Не удалось подготовить XML-файл заметки.", Toast.LENGTH_LONG).show()
    }
}

internal fun shareNoteAsMarkdown(context: Context, note: Note) {
    try {
        val folder = File(context.cacheDir, "shared_notes").apply { mkdirs() }
        val safeTitle = note.title.ifBlank { "Заметка" }.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(80)
        val file = File(folder, "$safeTitle-${System.currentTimeMillis()}.md")
        val payload = buildString {
            if (note.title.isNotBlank()) append("# ${note.title.trim()}\n\n")
            if (note.body.isNotBlank()) append(RichTextDocument.plainText(note.body).trim()).append("\n\n")
            note.tables.forEachIndexed { index, table -> append("## Таблица ${index + 1}\n\n").append(table.toMarkdownTable()).append("\n\n") }
            note.photoPaths.filter { File(it).isFile }.forEach { append("Фото: ${File(it).name}\n") }
            note.attachmentPaths.filter { File(it).isFile }.forEach { append("Вложение: ${File(it).name}\n") }
        }.trim()
        file.writeText(payload, Charsets.UTF_8)
        shareNoteFile(context, file, note.title, "text/markdown", "Поделиться заметкой Markdown")
    } catch (_: Exception) {
        Toast.makeText(context, "Не удалось подготовить Markdown-файл заметки.", Toast.LENGTH_LONG).show()
    }
}

internal fun shareNoteAsHtml(context: Context, scope: CoroutineScope, note: Note) {
    scope.launch {
        try {
            val htmlFile = withContext(Dispatchers.IO) { createShareableNoteHtml(context, note) }
            shareNoteHtmlFile(context, htmlFile, note.title)
        } catch (_: Exception) {
            Toast.makeText(context, "Не удалось подготовить HTML-файл заметки.", Toast.LENGTH_LONG).show()
        }
    }
}

internal fun shareNoteAsPdf(context: Context, scope: CoroutineScope, note: Note) {
    scope.launch {
        try {
            val pdfFile = withContext(Dispatchers.IO) { createShareableNotePdf(context, note) }
            shareNotePdfFile(context, pdfFile, note.title)
        } catch (_: Exception) {
            Toast.makeText(context, "Не удалось подготовить PDF заметки.", Toast.LENGTH_LONG).show()
        }
    }
}

private fun createShareableNotePdf(context: Context, note: Note): File {
    val folder = File(context.cacheDir, "shared_notes").apply { mkdirs() }
    val output = File(folder, "Заметка-${System.currentTimeMillis()}.pdf")
    val document = android.graphics.pdf.PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 44f
    val bodyPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(34, 42, 36)
        textSize = 12f
    }
    val titlePaint = android.graphics.Paint(bodyPaint).apply { textSize = 20f; typeface = android.graphics.Typeface.DEFAULT_BOLD }
    val headingPaint = android.graphics.Paint(bodyPaint).apply { textSize = 14f; typeface = android.graphics.Typeface.DEFAULT_BOLD }
    val linePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(180, 190, 181); strokeWidth = 0.7f }
    val imagePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
    var pageNumber = 0
    var page: android.graphics.pdf.PdfDocument.Page? = null
    var canvas: android.graphics.Canvas? = null
    var y = margin
    fun newPage() {
        page?.let(document::finishPage)
        pageNumber++
        page = document.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        canvas = page!!.canvas
        y = margin
    }
    fun ensureRoom(height: Float) {
        if (page == null || y + height > pageHeight - margin) newPage()
    }
    fun drawWrapped(text: String, paint: android.graphics.Paint, indent: Float = 0f, gap: Float = 6f) {
        val maxWidth = pageWidth - margin * 2 - indent
        text.split('\n').forEach { paragraph ->
            if (paragraph.isEmpty()) { y += paint.textSize * 0.55f; return@forEach }
            val words = paragraph.split(Regex("\\s+"))
            var line = StringBuilder()
            words.forEach { word ->
                val candidate = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                    ensureRoom(paint.textSize * 1.5f)
                    canvas!!.drawText(line.toString(), margin + indent, y + paint.textSize, paint)
                    y += paint.textSize * 1.5f
                    line = StringBuilder(word)
                } else line = StringBuilder(candidate)
            }
            ensureRoom(paint.textSize * 1.5f)
            canvas!!.drawText(line.toString(), margin + indent, y + paint.textSize, paint)
            y += paint.textSize * 1.5f
            y += gap
        }
    }
    fun drawTable(table: NoteTable) {
        val columns = table.cells.maxOfOrNull { it.size } ?: return
        if (columns == 0) return
        val width = pageWidth - margin * 2
        val colWidth = width / columns
        table.cells.forEachIndexed { rowIndex, row ->
            val prepared = List(columns) { col -> row.getOrNull(col).orEmpty() }
            val wrapped = prepared.map { cell ->
                val words = RichTextDocument.plainText(cell).replace('\n', ' ').split(Regex("\\s+"))
                val maxText = colWidth - 14f
                val result = mutableListOf<String>()
                var line = StringBuilder()
                words.forEach { word ->
                    val candidate = if (line.isEmpty()) word else "$line $word"
                    if (bodyPaint.measureText(candidate) > maxText && line.isNotEmpty()) {
                        result += line.toString(); line = StringBuilder(word)
                    } else line = StringBuilder(candidate)
                }
                if (line.isNotEmpty()) result += line.toString()
                result.ifEmpty { listOf("") }
            }
            val rowHeight = maxOf(32f, (wrapped.maxOfOrNull { it.size } ?: 1) * 16f + 16f)
            ensureRoom(rowHeight + 2f)
            val top = y
            val fill = if (rowIndex == 0) android.graphics.Color.rgb(231, 239, 232) else android.graphics.Color.WHITE
            val bg = android.graphics.Paint().apply { color = fill; style = android.graphics.Paint.Style.FILL }
            canvas!!.drawRect(margin, top, pageWidth - margin, top + rowHeight, bg)
            for (col in 0..columns) canvas!!.drawLine(margin + col * colWidth, top, margin + col * colWidth, top + rowHeight, linePaint)
            canvas!!.drawLine(margin, top, pageWidth - margin, top, linePaint)
            canvas!!.drawLine(margin, top + rowHeight, pageWidth - margin, top + rowHeight, linePaint)
            wrapped.forEachIndexed { col, cellLines ->
                val paint = if (rowIndex == 0) headingPaint else bodyPaint
                cellLines.forEachIndexed { lineIndex, text ->
                    canvas!!.drawText(text, margin + col * colWidth + 7f, top + 15f + lineIndex * 16f, paint)
                }
            }
            y += rowHeight
        }
        y += 16f
    }
    try {
        if (note.title.isNotBlank()) drawWrapped(note.title, titlePaint, gap = 12f)
        if (RichTextDocument.plainText(note.body).isNotBlank()) drawWrapped(RichTextDocument.plainText(note.body), bodyPaint)
        note.tables.forEachIndexed { index, table ->
            drawWrapped("Таблица ${index + 1}", headingPaint, gap = 5f)
            drawTable(table)
        }
        note.photoPaths.forEach { path ->
            val bitmap = runCatching { BitmapFactory.decodeFile(path) }.getOrNull() ?: return@forEach
            val ratio = minOf((pageWidth - margin * 2) / bitmap.width.toFloat(), 240f / bitmap.height.toFloat(), 1f)
            val h = bitmap.height * ratio
            ensureRoom(h + 18f)
            val dest = android.graphics.RectF(margin, y, margin + bitmap.width * ratio, y + h)
            canvas!!.drawBitmap(bitmap, null, dest, imagePaint)
            y += h + 18f
            bitmap.recycle()
        }
        if (note.attachmentPaths.isNotEmpty()) {
            drawWrapped("Вложения", headingPaint)
            note.attachmentPaths.filter { File(it).isFile }.forEach { drawWrapped("• ${File(it).name}", bodyPaint, 8f) }
        }
        if (page == null) newPage()
        page?.let(document::finishPage)
        FileOutputStream(output).use { document.writeTo(it) }
    } finally {
        document.close()
    }
    return output
}

private fun shareNotePdfFile(context: Context, file: File, title: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { "Заметка" })
        clipData = ClipData.newUri(context.contentResolver, title.ifBlank { "Заметка" }, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(send, "Поделиться PDF заметки"))
}

private fun createShareableNoteHtml(context: Context, note: Note): File {
    val folder = File(context.cacheDir, "shared_notes").apply { mkdirs() }
    val output = File(folder, "Заметка-${System.currentTimeMillis()}.html")
    val mimeMap = MimeTypeMap.getSingleton()
    BufferedWriter(OutputStreamWriter(FileOutputStream(output), Charsets.UTF_8)).use { writer ->
        writer.write("""<!doctype html><html lang="ru"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>${escapeHtml(note.title.ifBlank { "Заметка" })}</title><style>body{font:16px/1.6 system-ui,sans-serif;max-width:900px;margin:32px auto;padding:0 18px;color:#202720}h1{line-height:1.25}p{white-space:pre-wrap}table{border-collapse:collapse;width:100%;margin:18px 0}th,td{border:1px solid #aeb8af;padding:9px;text-align:left;vertical-align:top;white-space:pre-wrap}th{background:#e9f0ea}img{display:block;max-width:100%;height:auto;margin:14px 0;border-radius:8px}.attachment{display:block;margin:8px 0;overflow-wrap:anywhere}</style></head><body>""")
        if (note.title.isNotBlank()) writer.write("<h1>${escapeHtml(note.title)}</h1>")
        if (RichTextDocument.plainText(note.body).isNotBlank()) writer.write("<p>${escapeHtml(RichTextDocument.plainText(note.body))}</p>")
        note.tables.forEachIndexed { tableIndex, table ->
            if (table.cells.isNotEmpty()) {
                writer.write("<h2>Таблица ${tableIndex + 1}</h2><table><tbody>")
                table.cells.forEachIndexed { rowIndex, row ->
                    writer.write("<tr>")
                    row.forEach { cell ->
                        val tag = if (rowIndex == 0) "th" else "td"
                        writer.write("<$tag>${escapeHtml(RichTextDocument.plainText(cell))}</$tag>")
                    }
                    writer.write("</tr>")
                }
                writer.write("</tbody></table>")
            }
        }
        note.photoPaths.forEach { path ->
            val file = File(path)
            if (file.isFile) {
                val extension = file.extension.lowercase(Locale.ROOT)
                val mime = mimeMap.getMimeTypeFromExtension(extension) ?: "image/jpeg"
                writer.write("<img alt=\"Фото заметки\" src=\"")
                appendDataUri(writer, file, mime)
                writer.write("\">")
            }
        }
        note.attachmentPaths.forEach { path ->
            val file = File(path)
            if (file.isFile) {
                val extension = file.extension.lowercase(Locale.ROOT)
                val mime = mimeMap.getMimeTypeFromExtension(extension) ?: "application/octet-stream"
                writer.write("<a class=\"attachment\" download=\"${escapeHtml(file.name)}\" href=\"")
                appendDataUri(writer, file, mime)
                writer.write("\">${escapeHtml(file.name)}</a>")
            }
        }
        writer.write("</body></html>")
    }
    return output
}

private fun shareNoteHtmlFile(context: Context, file: File, title: String) =
    shareNoteFile(context, file, title, "text/html", "Поделиться заметкой HTML")

private fun shareNoteFile(context: Context, file: File, title: String, mimeType: String, chooserTitle: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { "Заметка" })
        clipData = ClipData.newUri(context.contentResolver, title.ifBlank { "Заметка" }, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(send, chooserTitle))
}

internal fun readNotes(context: Context, storageKey: String = "notes"): List<Note> {
    val raw = context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE)
        .getString(storageKey, "[]") ?: "[]"
    return try {
        val array = JSONArray(raw)
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            Note(
                id = item.optString("id"),
                category = normalizeSectionName(item.optString("category")),
                title = item.optString("title"),
                body = item.optString("body"),
                updatedAt = item.optLong("updatedAt"),
                createdAt = item.optLong("createdAt", item.optLong("updatedAt")),
                isFavorite = item.optBoolean("isFavorite", false),
                isDeleted = item.optBoolean("isDeleted", false),
                photoPaths = item.optJSONArray("photoPaths").toStringList(),
                attachmentPaths = item.optJSONArray("attachmentPaths").toStringList(),
                photoIds = item.optJSONArray("photoIds").toStringList(),
                attachmentIds = item.optJSONArray("attachmentIds").toStringList(),
                tables = item.optJSONArray("tables").toNoteTables(),
                aiAssistant = normalizeAiAssistant(item.optString("aiAssistant", "Без ИИ")),
                aiInstruction = item.optString("aiInstruction", ""),
                reminderAt = item.optLong("reminderAt", 0L),
                reminders = item.optJSONArray("reminders").toLongList(),
                reminderTypes = item.optJSONObject("reminderTypes").toReminderTypes(),
                themeId = item.optString("themeId", "")
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

internal fun readLocalNotesWithMigration(context: Context): List<Note> {
    val current = readNotes(context, "notes")
    if (current.isNotEmpty()) return current
    val prefs = context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE)
    val oldKey = prefs.all.keys
        .filter { it.startsWith("notes_user_") }
        .firstOrNull { notesJsonNonEmpty(prefs.getString(it, null)) }
    if (oldKey != null) {
        val migrated = readNotes(context, oldKey)
        if (migrated.isNotEmpty()) {
            writeNotes(context, migrated, "notes")
            return migrated
        }
    }
    return emptyList()
}

private fun notesJsonNonEmpty(raw: String?): Boolean {
    if (raw.isNullOrBlank()) return false
    return try {
        JSONArray(raw).length() > 0
    } catch (_: Exception) {
        false
    }
}

internal fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { optString(it).takeIf(String::isNotBlank) }
}

private fun JSONArray?.toLongList(): List<Long> {
    if (this == null) return emptyList()
    return (0 until length()).map { optLong(it) }.filter { it > 0L }.distinct().sorted()
}

private fun JSONObject?.toReminderTypes(): Map<Long, String> {
    if (this == null) return emptyMap()
    return keys().asSequence().mapNotNull { key ->
        val time = key.toLongOrNull() ?: return@mapNotNull null
        val type = optString(key, "Уведомление и будильник")
        if (time > 0L && type in setOf("Уведомление", "Будильник", "Уведомление и будильник")) time to type else null
    }.toMap()
}

internal fun notesToJson(notes: List<Note>): JSONArray {
    return JSONArray().apply {
        notes.forEach { note ->
            val photos = JSONArray().apply { note.photoPaths.forEach(::put) }
            val files = JSONArray().apply { note.attachmentPaths.forEach(::put) }
            val tables = JSONArray().apply {
                note.tables.forEach { table ->
                    val rows = JSONArray().apply {
                        table.cells.forEach { cells -> put(JSONArray().apply { cells.forEach(::put) }) }
                    }
                    put(JSONObject().put("id", table.id).put("cells", rows))
                }
            }
            put(JSONObject()
                .put("id", note.id)
                .put("category", note.category)
                .put("title", note.title)
                .put("body", note.body)
                .put("updatedAt", note.updatedAt)
                .put("createdAt", note.createdAt)
                .put("isFavorite", note.isFavorite)
                .put("isDeleted", note.isDeleted)
                .put("photoPaths", photos)
                .put("attachmentPaths", files)
                .put("photoIds", JSONArray(note.photoIds))
                .put("attachmentIds", JSONArray(note.attachmentIds))
                .put("tables", tables)
                .put("aiAssistant", normalizeAiAssistant(note.aiAssistant))
                .put("aiInstruction", note.aiInstruction)
                .put("reminderAt", note.reminderAt)
                .put("reminders", JSONArray(note.reminders))
                .put("reminderTypes", JSONObject().apply { note.reminderTypes.forEach { (time, type) -> put(time.toString(), type) } })
                .put("themeId", note.themeId))
        }
    }
}

private val noteStorageExecutor = Executors.newSingleThreadExecutor()

internal fun writeNotes(context: Context, notes: List<Note>, storageKey: String = "notes") {
    val snapshot = notes.toList()
    noteStorageExecutor.execute {
        val serialized = notesToJson(snapshot).toString()
        context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE).edit()
            .putString(storageKey, serialized).apply()
    }
}

internal fun persistImported(context: Context, notes: List<Note>, categories: List<String>, notesKey: String = "notes") {
    writeNotes(context, notes, notesKey)
    writeCategories(context, categories)
}

internal fun JSONArray?.toNotes(): List<Note> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        val item = optJSONObject(index) ?: return@mapNotNull null
        val updatedAt = item.optLong("updatedAt")
        Note(
            id = item.optString("id"),
            category = normalizeSectionName(item.optString("category")),
            title = item.optString("title"),
            body = item.optString("body"),
            updatedAt = updatedAt,
            createdAt = item.optLong("createdAt", updatedAt),
            isFavorite = item.optBoolean("isFavorite", false),
            isDeleted = item.optBoolean("isDeleted", false),
            photoPaths = item.optJSONArray("photoPaths").toStringList(),
            attachmentPaths = item.optJSONArray("attachmentPaths").toStringList(),
            photoIds = item.optJSONArray("photoIds").toStringList(),
            attachmentIds = item.optJSONArray("attachmentIds").toStringList(),
            tables = item.optJSONArray("tables").toNoteTables(),
            aiAssistant = normalizeAiAssistant(item.optString("aiAssistant", "Без ИИ")),
            aiInstruction = item.optString("aiInstruction", ""),
            reminderAt = item.optLong("reminderAt", 0L),
            reminders = item.optJSONArray("reminders").toLongList(),
            reminderTypes = item.optJSONObject("reminderTypes").toReminderTypes(),
            themeId = item.optString("themeId", "")
        )
    }
}

private fun JSONArray?.toNoteTables(): List<NoteTable> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        val table = optJSONObject(index) ?: return@mapNotNull null
        val rows = table.optJSONArray("cells") ?: return@mapNotNull null
        val cells = (0 until rows.length()).map { rowIndex ->
            val row = rows.optJSONArray(rowIndex) ?: JSONArray()
            (0 until row.length()).map { colIndex -> row.optString(colIndex) }
        }
        NoteTable(id = table.optString("id", UUID.randomUUID().toString()), cells = cells)
    }
}

internal fun readCategories(context: Context): List<String> {
    val raw = context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE).getString("categories", null)
        ?: return defaultCategories
    val values = try {
        val array = JSONArray(raw)
        (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }
    } catch (_: Exception) {
        emptyList()
    }
    return (defaultCategories + values.map(::normalizeSectionName))
        .distinctBy { it.lowercase(Locale.ROOT) }
}

internal fun writeCategories(context: Context, categories: List<String>) {
    val array = JSONArray().apply { categories.forEach(::put) }
    context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE).edit().putString("categories", array.toString()).apply()
}

private fun escapeMarkdownTableCell(value: String): String =
    value
        .replace("\\", "\\\\")
        .replace("|", "\\|")
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .replace("\n", "<br>")

private fun NoteTable.toMarkdownTable(): String {
    val columnCount = cells.maxOfOrNull { it.size } ?: return ""
    if (columnCount == 0) return ""

    val rows = cells.map { row ->
        List(columnCount) { column -> escapeMarkdownTableCell(row.getOrNull(column).orEmpty()) }
    }
    val header = rows.firstOrNull() ?: return ""
    val markdownRows = buildList {
        add(header)
        add(List(columnCount) { "---" })
        addAll(rows.drop(1))
    }
    return markdownRows.joinToString("\n") { row -> "| " + row.joinToString(" | ") + " |" }
}

internal fun shareNoteWithAssistant(
    context: Context,
    title: String,
    body: String,
    tables: List<NoteTable>,
    assistant: String,
    instruction: String,
    transferMode: String
) {
    val chosen = normalizeAiAssistant(assistant)
    if (chosen == "Без ИИ") return
    val tableText = tables.mapIndexed { index, table ->
        "Таблица ${index + 1}:\n${table.toMarkdownTable()}"
    }.filter { it.substringAfter('\n').isNotBlank() }.joinToString("\n\n")
    val prompt = buildString {
        append("Этот текст предоставлен из приложения Удобные заметки напрямую тебе.\n\n")
        if (instruction.isNotBlank()) append("Правки для ИИ: ${instruction.trim()}\n\n")
        if (title.isNotBlank()) append("Заголовок заметки: $title\n")
        if (body.isNotBlank()) append("Текст заметки: $body\n")
        if (tableText.isNotBlank()) {
            if (isNotEmpty()) append('\n')
            append("Таблицы заметки:\n")
            append(tableText)
        }
    }
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Запрос для $chosen", prompt))
    if (transferMode == "Копировать запрос") {
        Toast.makeText(context, "Запрос для $chosen скопирован", Toast.LENGTH_SHORT).show()
        return
    }
    val aiPackage = when (chosen) {
        "Алиса" -> "com.yandex.aliceapp"
        "Gemini" -> "com.google.android.apps.bard"
        "DeepSeek" -> "com.deepseek.chat"
        else -> null
    }
    if (chosen == "Алиса" || chosen == "Gemini") {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, prompt)
            putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { "Заметка" })
            if (aiPackage != null) setPackage(aiPackage)
        }
        try {
            if (chosen == "Gemini") {
                Toast.makeText(context, "Запрос скопирован — если не подставится, вставьте", Toast.LENGTH_SHORT).show()
            }
            context.startActivity(sendIntent)
            return
        } catch (_: android.content.ActivityNotFoundException) {
        }
    }
    val launchIntent = aiPackage?.let { context.packageManager.getLaunchIntentForPackage(it) }
    if (launchIntent != null) {
        Toast.makeText(context, "Запрос скопирован — вставьте в $chosen", Toast.LENGTH_SHORT).show()
        context.startActivity(launchIntent)
    } else {
        Toast.makeText(context, "$chosen не установлен — запрос скопирован в буфер", Toast.LENGTH_SHORT).show()
        val fallback = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, prompt)
            putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { "Заметка" })
        }
        context.startActivity(Intent.createChooser(fallback, "Отправить запрос в $chosen"))
    }
}