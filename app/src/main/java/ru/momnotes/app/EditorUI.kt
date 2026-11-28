package ru.momnotes.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.util.UUID

private data class DraftSnapshot(val title: String, val body: String)

private fun detectInsertedRun(old: String, new: String): Pair<Int, String>? {
    val diff = new.length - old.length
    if (diff !in 1..12) return null
    var prefix = 0
    val minLen = minOf(old.length, new.length)
    while (prefix < minLen && old[prefix] == new[prefix]) prefix++
    var suffix = 0
    while (suffix < minLen - prefix &&
        old[old.length - 1 - suffix] == new[new.length - 1 - suffix]
    ) {
        suffix++
    }
    val insertedEnd = new.length - suffix
    if (insertedEnd <= prefix) return null
    val inserted = new.substring(prefix, insertedEnd)
    return if (inserted.contains('\n')) null else prefix to inserted
}

@Composable
private fun ThemeShirtIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.30f, h * 0.14f)
            lineTo(w * 0.12f, h * 0.28f)
            lineTo(w * 0.22f, h * 0.44f)
            lineTo(w * 0.30f, h * 0.38f)
            lineTo(w * 0.30f, h * 0.86f)
            lineTo(w * 0.70f, h * 0.86f)
            lineTo(w * 0.70f, h * 0.38f)
            lineTo(w * 0.78f, h * 0.44f)
            lineTo(w * 0.88f, h * 0.28f)
            lineTo(w * 0.70f, h * 0.14f)
            cubicTo(w * 0.62f, h * 0.24f, w * 0.38f, h * 0.24f, w * 0.30f, h * 0.14f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
internal fun EditorScreen(
    note: Note?,
    category: String,
    allNotes: List<Note>,
    settings: AppSettings,
    onStatusBarStyle: (Color, Boolean) -> Unit,
    onAutoSave: (Note) -> Unit,
    onClose: () -> Unit,
    onSave: (Note) -> Unit
) {
    val context = LocalContext.current
    val mediaScope = rememberCoroutineScope()
    var title by remember(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var body by remember(note?.id) { mutableStateOf(note?.body.orEmpty()) }
    var themeId by remember(note?.id) { mutableStateOf(note?.themeId.orEmpty()) }
    val reminderTimes = remember(note?.id) {
        mutableStateListOf<Long>().apply {
            addAll((note?.reminders.orEmpty() + listOfNotNull(note?.reminderAt?.takeIf { it > 0L })).distinct().sorted())
        }
    }
    val reminderTypes = remember(note?.id) {
        mutableStateMapOf<Long, String>().apply {
            putAll(note?.reminderTypes.orEmpty())
            (note?.reminders.orEmpty() + listOfNotNull(note?.reminderAt?.takeIf { it > 0L })).forEach {
                if (get(it).isNullOrBlank()) put(it, "Уведомление и будильник")
            }
        }
    }
    val plainTextBody = remember(body) { RichTextDocument.plainText(body) }
    val bodyHasContent = remember(plainTextBody) { plainTextBody.isNotBlank() }
    val wordCount = remember(plainTextBody) {
        plainTextBody.trim().split(Regex("\\s+")).count { it.isNotBlank() }
    }
    var selectedAi by remember(note?.id) { mutableStateOf(normalizeAiAssistant(note?.aiAssistant ?: "Без ИИ")) }
    var aiInstruction by remember(note?.id) { mutableStateOf(note?.aiInstruction.orEmpty()) }
    val richState = remember(note?.id) { RichEditorState() }
    var showThemePicker by remember { mutableStateOf(false) }
    var showFormatBar by remember { mutableStateOf(false) }
    var previewPhotoPath by remember { mutableStateOf<String?>(null) }

    val keyboardAnimController = remember(note?.id) { KeyboardToNoteController() }
    var titleCoords by remember(note?.id) { mutableStateOf<LayoutCoordinates?>(null) }
    var pendingTitleInsert by remember(note?.id) { mutableStateOf<Pair<Int, String>?>(null) }
    val imeBottomPx = WindowInsets.ime.getBottom(LocalDensity.current)
    val hostView = LocalView.current
    SideEffect {
        val visibleRect = android.graphics.Rect()
        hostView.getWindowVisibleDisplayFrame(visibleRect)
        val rootHeight = hostView.rootView.height
        val viewImeHeight = (rootHeight - visibleRect.bottom).coerceAtLeast(0)
        keyboardAnimController.isSoftKeyboardVisible =
            imeBottomPx > 80 || (rootHeight > 0 && viewImeHeight > rootHeight * 0.15f)
    }

    val activeTheme = remember(themeId) { NoteThemes.find(themeId) }
    val defaultBg = MaterialTheme.colorScheme.background
    val editorTextColor = activeTheme?.textColor ?: MaterialTheme.colorScheme.onBackground
    val editorMutedColor = activeTheme?.mutedColor ?: MaterialTheme.colorScheme.onSurfaceVariant
    val editorAccentColor = activeTheme?.accentColor ?: MaterialTheme.colorScheme.primary
    val subtleSurfaceColor = when {
        activeTheme == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
        activeTheme.isDark -> Color.Black.copy(alpha = 0.26f)
        else -> Color.White.copy(alpha = 0.68f)
    }
    val subtleCardBorder = when {
        activeTheme == null -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        activeTheme.isDark -> Color.White.copy(alpha = 0.14f)
        else -> Color.Black.copy(alpha = 0.08f)
    }

    LaunchedEffect(activeTheme, defaultBg) {
        val barColor = activeTheme?.surfaceTint ?: defaultBg
        val darkBar = activeTheme?.isDark ?: (contrastColor(defaultBg) == Color.White)
        onStatusBarStyle(barColor, darkBar)
    }

    val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        NoteReminderScheduler.rescheduleSaved(context)
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !NoteReminderScheduler.canScheduleExactly(context)) {
            runCatching { exactAlarmSettingsLauncher.launch(NoteReminderScheduler.exactAlarmSettingsIntent(context)) }
        }
    }
    fun requestReminderPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !NoteReminderScheduler.canScheduleExactly(context)) {
            runCatching { exactAlarmSettingsLauncher.launch(NoteReminderScheduler.exactAlarmSettingsIntent(context)) }
        }
    }
    val photoPaths = remember(note?.id) { mutableStateListOf<String>().apply { addAll(note?.photoPaths.orEmpty()) } }
    val attachmentPaths = remember(note?.id) { mutableStateListOf<String>().apply { addAll(note?.attachmentPaths.orEmpty()) } }
    val photoIds = remember(note?.id) {
        mutableStateListOf<String>().apply {
            val existing = note?.photoIds.orEmpty()
            val count = note?.photoPaths?.size ?: 0
            for (i in 0 until count) add(existing.getOrNull(i).orEmpty())
        }
    }
    val attachmentIds = remember(note?.id) {
        mutableStateListOf<String>().apply {
            val existing = note?.attachmentIds.orEmpty()
            val count = note?.attachmentPaths?.size ?: 0
            for (i in 0 until count) add(existing.getOrNull(i).orEmpty())
        }
    }
    val tableList = remember(note?.id) { mutableStateListOf<NoteTable>().apply { addAll(note?.tables.orEmpty()) } }
    var showTableSizeDialog by remember { mutableStateOf(false) }
    var tableRowCount by remember { mutableStateOf(3) }
    var tableColumnCount by remember { mutableStateOf(3) }
    val history = remember(note?.id) { mutableStateListOf(DraftSnapshot(title, body)) }
    var historyIndex by remember(note?.id) { mutableStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showInsertMenu by remember { mutableStateOf(false) }
    var showShareMenu by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showAttachNoteDialog by remember { mutableStateOf(false) }
    var linkLabel by remember { mutableStateOf("") }
    var linkAddress by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var showReminderPicker by remember { mutableStateOf(false) }

    var historyJob by remember(note?.id) { mutableStateOf<Job?>(null) }
    fun record(nextTitle: String = title, nextBody: String = body) {
        historyJob?.cancel()
        historyJob = mediaScope.launch {
            delay(450)
            val snapshot = DraftSnapshot(nextTitle, nextBody)
            if (history.getOrNull(historyIndex) == snapshot) return@launch
            while (history.lastIndex > historyIndex) history.removeAt(history.lastIndex)
            history.add(snapshot)
            if (history.size > 50) history.removeAt(0) else historyIndex++
            if (historyIndex >= history.size) historyIndex = history.lastIndex
        }
    }

    val draftId = remember(note?.id) { note?.id ?: UUID.randomUUID().toString() }
    var lastSavedSnapshot by remember(note?.id) { mutableStateOf(note) }

    fun currentDraft(): Note {
        val now = System.currentTimeMillis()
        return Note(
            id = draftId,
            category = category,
            title = title.trim(),
            body = body,
            reminderAt = reminderTimes.firstOrNull() ?: 0L,
            reminders = reminderTimes.toList(),
            reminderTypes = reminderTypes.toMap(),
            updatedAt = now,
            createdAt = lastSavedSnapshot?.createdAt ?: note?.createdAt ?: now,
            isFavorite = lastSavedSnapshot?.isFavorite ?: note?.isFavorite ?: false,
            isDeleted = lastSavedSnapshot?.isDeleted ?: note?.isDeleted ?: false,
            photoPaths = photoPaths.toList(),
            attachmentPaths = attachmentPaths.toList(),
            photoIds = photoIds.toList(),
            attachmentIds = attachmentIds.toList(),
            tables = tableList.toList(),
            aiAssistant = selectedAi,
            aiInstruction = aiInstruction.trim(),
            themeId = themeId
        )
    }

    fun commitAutoSave() {
        val saved = currentDraft()
        lastSavedSnapshot = saved
        onAutoSave(saved)
    }

    fun setTitle(value: String) {
        title = value
        record(nextTitle = value)
    }

    fun setBody(value: String) {
        body = value
        record(nextBody = value)
    }

    fun insertIntoBody(value: String) {
        val plain = RichTextDocument.plainText(body)
        val prefix = if (plain.isBlank()) "" else if (plain.endsWith("\n")) "" else "\n"
        setBody(RichTextDocument.appendText(body, prefix + value))
    }

    LaunchedEffect(title, body, settings.autoSave) {
        if (settings.autoSave && (title.isNotBlank() || bodyHasContent || reminderTimes.isNotEmpty() || tableList.isNotEmpty())) {
            delay(700)
            commitAutoSave()
        }
    }

    fun saveDraft() {
        historyJob?.cancel()
        val saved = currentDraft()
        lastSavedSnapshot = saved
        onSave(saved)
    }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val recognized = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!recognized.isNullOrBlank()) insertIntoBody(recognized.trim())
        }
    }
    fun launchSpeech() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (settings.voiceLanguage == "Системный") Locale.getDefault().toLanguageTag() else "ru-RU")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Продиктуйте текст заметки")
        }
        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            message = "На телефоне не найдено приложение для распознавания речи. Можно использовать микрофон клавиатуры."
        }
    }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) mediaScope.launch {
            val result = withContext(Dispatchers.IO) { runCatching { copyUriToPrivateFile(context, uri, "note_photos") } }
            val copied = result.getOrNull()
            if (copied != null) {
                photoPaths.add(copied.absolutePath)
                photoIds.add("")
                if (settings.autoSave) commitAutoSave()
            } else {
                val detail = result.exceptionOrNull()?.localizedMessage?.takeIf { it.isNotBlank() }?.take(140)
                message = if (detail == null) "Не удалось добавить фотографию." else "Не удалось прочитать фотографию: $detail"
            }
        }
    }
    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) mediaScope.launch {
            val result = withContext(Dispatchers.IO) { runCatching { copyUriToPrivateFile(context, uri, "note_files") } }
            val copied = result.getOrNull()
            if (copied != null) {
                attachmentPaths.add(copied.absolutePath)
                attachmentIds.add("")
                if (settings.autoSave) commitAutoSave()
            } else {
                val detail = result.exceptionOrNull()?.localizedMessage?.takeIf { it.isNotBlank() }?.take(140)
                message = if (detail == null) "Не удалось добавить вложение." else "Не удалось прочитать файл: $detail"
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        val baseline = lastSavedSnapshot
        if (title.trim() != baseline?.title.orEmpty() || RichTextDocument.canonical(body) != RichTextDocument.canonical(baseline?.body.orEmpty())) return true
        if (themeId != baseline?.themeId.orEmpty()) return true
        val baseReminders = (baseline?.reminders.orEmpty() + listOfNotNull(baseline?.reminderAt?.takeIf { it > 0L })).distinct().sorted()
        if (reminderTimes.toList() != baseReminders || reminderTypes.toMap() != baseline?.reminderTypes.orEmpty()) return true
        val baseAi = normalizeAiAssistant(baseline?.aiAssistant ?: "Без ИИ")
        if (selectedAi != baseAi || aiInstruction.trim() != baseline?.aiInstruction.orEmpty()) return true
        if (photoPaths.toList() != baseline?.photoPaths.orEmpty() || attachmentPaths.toList() != baseline?.attachmentPaths.orEmpty()) return true
        if (tableList.toList() != baseline?.tables.orEmpty()) return true
        return false
    }

    fun requestExit() {
        if (showThemePicker) {
            showThemePicker = false
            return
        }
        if (!hasUnsavedChanges()) onClose() else showExitDialog = true
    }
    BackHandler { requestExit() }

    val canSave = title.isNotBlank() || bodyHasContent || reminderTimes.isNotEmpty() || photoPaths.isNotEmpty() || attachmentPaths.isNotEmpty() || tableList.isNotEmpty() || aiInstruction.isNotBlank()
    val topActionBg = when {
        activeTheme == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        activeTheme.isDark -> Color.Black.copy(alpha = 0.28f)
        else -> Color.White.copy(alpha = 0.72f)
    }
    val topActionBorder = when {
        activeTheme == null -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        activeTheme.isDark -> Color.White.copy(alpha = 0.16f)
        else -> Color.Black.copy(alpha = 0.08f)
    }
    val saveBtnColor by animateColorAsState(
        targetValue = if (canSave) editorAccentColor else topActionBg,
        animationSpec = tween(220),
        label = "editor-save-bg"
    )
    val saveBtnScale by animateFloatAsState(
        targetValue = if (canSave) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "editor-save-scale"
    )

    val customTableHex = if (settings.appBackgroundTheme.isBlank()) settings.tableColor else ""

    Box(
        Modifier
            .fillMaxSize()
            .imePadding()
            .onGloballyPositioned { coords ->
                keyboardAnimController.updateOverlayCoordinates(coords)
            }
    ) {
        NoteThemeCanvas(
            themeId = themeId,
            modifier = Modifier.matchParentSize(),
            animated = settings.animateNoteThemes
        )
        if (activeTheme != null) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                activeTheme.surfaceTint.copy(alpha = if (activeTheme.isDark) 0.16f else 0.18f),
                                activeTheme.surfaceTint.copy(alpha = if (activeTheme.isDark) 0.24f else 0.26f),
                                activeTheme.surfaceTint.copy(alpha = if (activeTheme.isDark) 0.14f else 0.14f)
                            )
                        )
                    )
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = topActionBg,
                    border = BorderStroke(1.dp, topActionBorder),
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable { requestExit() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = editorTextColor, modifier = Modifier.size(20.dp))
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = topActionBg,
                        border = BorderStroke(1.dp, topActionBorder)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    historyJob?.cancel()
                                    val current = DraftSnapshot(title, body)
                                    if (history.getOrNull(historyIndex) != current) {
                                        while (history.lastIndex > historyIndex) history.removeAt(history.lastIndex)
                                        history.add(current)
                                        historyIndex = history.lastIndex
                                    }
                                    if (historyIndex > 0) {
                                        historyIndex--
                                        val snapshot = history[historyIndex]
                                        title = snapshot.title
                                        body = snapshot.body
                                    }
                                },
                                enabled = historyIndex > 0,
                                modifier = Modifier.size(38.dp)
                            ) {
                                NoteVectorIcon(
                                    "undo",
                                    color = if (historyIndex > 0) editorTextColor else editorMutedColor.copy(alpha = 0.35f),
                                    size = 18.dp
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (historyIndex < history.lastIndex) {
                                        historyIndex++
                                        val snapshot = history[historyIndex]
                                        title = snapshot.title
                                        body = snapshot.body
                                    }
                                },
                                enabled = historyIndex < history.lastIndex,
                                modifier = Modifier.size(38.dp)
                            ) {
                                NoteVectorIcon(
                                    "redo",
                                    color = if (historyIndex < history.lastIndex) editorTextColor else editorMutedColor.copy(alpha = 0.35f),
                                    size = 18.dp
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (showThemePicker) editorAccentColor.copy(alpha = 0.25f) else topActionBg,
                        border = BorderStroke(1.dp, if (showThemePicker) editorAccentColor else topActionBorder),
                        modifier = Modifier.size(40.dp).clip(CircleShape).clickable { showThemePicker = !showThemePicker }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            ThemeShirtIcon(color = if (showThemePicker) editorAccentColor else editorTextColor)
                        }
                    }

                    Box {
                        val shareEnabled = title.isNotBlank() || bodyHasContent || tableList.isNotEmpty() || photoPaths.isNotEmpty() || attachmentPaths.isNotEmpty()
                        Surface(
                            shape = CircleShape,
                            color = topActionBg,
                            border = BorderStroke(1.dp, topActionBorder),
                            modifier = Modifier.size(40.dp).clip(CircleShape).clickable(enabled = shareEnabled) { showShareMenu = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                NoteVectorIcon(
                                    "share",
                                    color = if (shareEnabled) editorTextColor else editorMutedColor.copy(alpha = 0.35f),
                                    size = 18.dp
                                )
                            }
                        }
                        DropdownMenu(expanded = showShareMenu, onDismissRequest = { showShareMenu = false }) {
                            DropdownMenuItem(text = { Text("PDF") }, onClick = { showShareMenu = false; shareNoteAsPdf(context, mediaScope, currentDraft()) })
                            DropdownMenuItem(text = { Text("Markdown (.md)") }, onClick = { showShareMenu = false; shareNoteAsMarkdown(context, currentDraft()) })
                            DropdownMenuItem(text = { Text("Текст (.txt)") }, onClick = { showShareMenu = false; shareNoteAsTextFile(context, currentDraft()) })
                            DropdownMenuItem(text = { Text("HTML") }, onClick = { showShareMenu = false; shareNoteAsHtml(context, mediaScope, currentDraft()) })
                            DropdownMenuItem(text = { Text("XML") }, onClick = { showShareMenu = false; shareNoteAsXml(context, currentDraft()) })
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = saveBtnColor,
                        border = BorderStroke(1.dp, if (canSave) editorAccentColor else topActionBorder),
                        modifier = Modifier
                            .height(40.dp)
                            .graphicsLayer { scaleX = saveBtnScale; scaleY = saveBtnScale }
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = canSave) { saveDraft() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Сохранить",
                                tint = if (canSave) contrastColor(editorAccentColor) else editorMutedColor.copy(alpha = 0.35f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "Готово",
                                color = if (canSave) contrastColor(editorAccentColor) else editorMutedColor.copy(alpha = 0.35f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = editorAccentColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, editorAccentColor.copy(alpha = 0.25f))
                    ) {
                        Text(
                            category,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = editorAccentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "${formatDate(note?.updatedAt ?: System.currentTimeMillis())} · $wordCount сл. · ${plainTextBody.length} симв.",
                        color = editorMutedColor.copy(alpha = 0.88f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                BasicTextField(
                    value = title,
                    onValueChange = { next ->
                        if (settings.keyboardToNoteAnimation) {
                            detectInsertedRun(title, next)?.let { pendingTitleInsert = it }
                        }
                        setTitle(next)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { titleCoords = it },
                    textStyle = TextStyle(
                        color = editorTextColor,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    ),
                    singleLine = false,
                    maxLines = 3,
                    cursorBrush = SolidColor(editorAccentColor),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    onTextLayout = { result ->
                        val pending = pendingTitleInsert
                        if (pending != null && settings.keyboardToNoteAnimation) {
                            pendingTitleInsert = null
                            val (insIdx, added) = pending
                            val coords = titleCoords
                            if (coords != null && coords.isAttached && result.layoutInput.text.isNotEmpty()) {
                                val targetIdx = (insIdx + added.length - 1)
                                    .coerceIn(0, result.layoutInput.text.length - 1)
                                val box = result.getBoundingBox(targetIdx)
                                val posInWindow = coords.localToWindow(Offset(box.center.x, box.center.y))
                                keyboardAnimController.emitText(
                                    inserted = added,
                                    targetInWindow = posInWindow,
                                    accentColor = editorAccentColor,
                                    textColor = editorTextColor
                                )
                            }
                        }
                    },
                    decorationBox = { innerTextField ->
                        Box {
                            if (title.isEmpty()) {
                                Text(
                                    "Заголовок",
                                    color = editorMutedColor.copy(alpha = 0.50f),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                RichTextField(
                    value = body,
                    onValueChange = { setBody(it) },
                    label = "",
                    editorHeight = 270.dp,
                    seamless = true,
                    state = richState,
                    textColor = editorTextColor,
                    hintColor = editorMutedColor.copy(alpha = 0.52f),
                    onCharacterTyped = if (settings.keyboardToNoteAnimation) {
                        { inserted, targetInWindow ->
                            keyboardAnimController.emitText(
                                inserted = inserted,
                                targetInWindow = targetInWindow,
                                accentColor = editorAccentColor,
                                textColor = editorTextColor
                            )
                        }
                    } else null
                )

                tableList.toList().forEachIndexed { tableIndex, table ->
                    NoteTableEditor(
                        table = table,
                        index = tableIndex + 1,
                        activeTheme = activeTheme,
                        customTableColorHex = customTableHex,
                        onUpdateTable = { updated ->
                            val index = tableList.indexOfFirst { it.id == table.id }
                            if (index >= 0) {
                                tableList[index] = updated
                                if (settings.autoSave) commitAutoSave()
                            }
                        },
                        onDelete = { tableList.removeAll { it.id == table.id } }
                    )
                }

                AnimatedVisibility(
                    visible = photoPaths.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        photoPaths.toList().forEachIndexed { index, path ->
                            PhotoAttachment(
                                path = path,
                                onOpen = { previewPhotoPath = path },
                                onRemove = {
                                    if (index in photoPaths.indices) {
                                        photoPaths.removeAt(index)
                                        if (index < photoIds.size) photoIds.removeAt(index)
                                        if (settings.autoSave) commitAutoSave()
                                    }
                                }
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = attachmentPaths.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        attachmentPaths.toList().forEachIndexed { index, path ->
                            val file = File(path)
                            val ext = file.extension.uppercase(Locale.ROOT).ifBlank { "FILE" }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = subtleSurfaceColor,
                                border = BorderStroke(1.dp, subtleCardBorder),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = editorAccentColor.copy(alpha = 0.18f)) {
                                        Text(
                                            ext.take(5),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                            color = editorAccentColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(file.name, modifier = Modifier.weight(1f), color = editorTextColor, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    TextButton(onClick = {
                                        if (index in attachmentPaths.indices) {
                                            attachmentPaths.removeAt(index)
                                            if (index < attachmentIds.size) attachmentIds.removeAt(index)
                                            if (settings.autoSave) commitAutoSave()
                                        }
                                    }) { Text("Убрать", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = message != null) {
                    Text(message.orEmpty(), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                HorizontalDivider(color = editorMutedColor.copy(alpha = 0.22f))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = subtleSurfaceColor,
                    border = BorderStroke(1.dp, subtleCardBorder),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NoteVectorIcon("bell", editorAccentColor, size = 18.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Напоминания", modifier = Modifier.weight(1f), color = editorTextColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            TextButton(onClick = { showReminderPicker = true }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                                Text("+ Добавить", color = editorAccentColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        reminderTimes.forEach { time ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(formatReminderDate(time), color = editorTextColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(reminderTypes[time] ?: "Уведомление и будильник", color = editorMutedColor, fontSize = 11.sp)
                                }
                                TextButton(onClick = {
                                    NoteReminderScheduler.cancel(context, draftId, time)
                                    reminderTimes.remove(time)
                                    reminderTypes.remove(time)
                                    if (settings.autoSave) commitAutoSave()
                                }) { Text("Убрать", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = subtleSurfaceColor,
                    border = BorderStroke(1.dp, subtleCardBorder),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        var aiMenu by remember { mutableStateOf(false) }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AiAssistantIcon(selectedAi, size = 20.dp, mutedTint = editorMutedColor)
                                Text("Помощник ИИ", color = editorTextColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(11.dp),
                                    color = editorAccentColor.copy(alpha = 0.16f),
                                    modifier = Modifier.clip(RoundedCornerShape(11.dp)).clickable { aiMenu = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        AiAssistantIcon(selectedAi, size = 15.dp, mutedTint = editorAccentColor)
                                        Text(
                                            "$selectedAi ▾",
                                            color = editorAccentColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                DropdownMenu(expanded = aiMenu, onDismissRequest = { aiMenu = false }) {
                                    availableAiAssistants.forEach { option ->
                                        DropdownMenuItem(
                                            leadingIcon = { AiAssistantIcon(option, size = 20.dp) },
                                            text = {
                                                Text(
                                                    option,
                                                    fontWeight = if (option == selectedAi) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = { selectedAi = option; aiMenu = false }
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            availableAiAssistants.forEach { option ->
                                val isSelected = selectedAi == option
                                val chipColor by animateColorAsState(
                                    targetValue = if (isSelected) editorAccentColor.copy(alpha = 0.22f) else editorTextColor.copy(alpha = 0.06f),
                                    animationSpec = tween(200),
                                    label = "ai-chip-bg"
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = chipColor,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) editorAccentColor.copy(alpha = 0.7f) else subtleCardBorder
                                    ),
                                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { selectedAi = option }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        AiAssistantIcon(option, size = 16.dp, mutedTint = editorMutedColor)
                                        Text(
                                            option,
                                            color = if (isSelected) editorTextColor else editorMutedColor,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = selectedAi != "Без ИИ",
                            enter = expandVertically(tween(240)) + fadeIn(tween(220)),
                            exit = shrinkVertically(tween(180)) + fadeOut(tween(140))
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Исправь ошибки", "Сделай короче", "Составь список", "Улучши стиль").forEach { preset ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = editorTextColor.copy(alpha = 0.07f),
                                            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
                                                aiInstruction = if (aiInstruction.isBlank()) preset else "${aiInstruction.trim()}. $preset"
                                            }
                                        ) {
                                            Text(
                                                preset,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                                color = editorMutedColor,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = aiInstruction,
                                    onValueChange = { aiInstruction = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Инструкция для $selectedAi", color = editorMutedColor) },
                                    placeholder = { Text("Например: исправь ошибки и сохрани стиль", color = editorMutedColor.copy(alpha = 0.6f)) },
                                    minLines = 2,
                                    maxLines = 4,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = editorTextColor,
                                        unfocusedTextColor = editorTextColor,
                                        focusedBorderColor = editorAccentColor,
                                        unfocusedBorderColor = editorMutedColor.copy(alpha = 0.35f)
                                    )
                                )
                                Button(
                                    onClick = {
                                        shareNoteWithAssistant(
                                            context = context,
                                            title = title.trim(),
                                            body = plainTextBody,
                                            tables = tableList.toList(),
                                            assistant = selectedAi,
                                            instruction = aiInstruction,
                                            transferMode = settings.aiTransfer
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = editorAccentColor,
                                        contentColor = contrastColor(editorAccentColor)
                                    )
                                ) {
                                    AiAssistantIcon(selectedAi, size = 18.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        if (settings.aiTransfer == "Копировать запрос") "Скопировать запрос для $selectedAi"
                                        else "Отправить в $selectedAi",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            AnimatedVisibility(
                visible = showThemePicker,
                enter = slideInVertically { it } + expandVertically() + fadeIn(),
                exit = slideOutVertically { it } + shrinkVertically() + fadeOut()
            ) {
                NoteThemePickerBar(
                    selectedId = themeId,
                    animated = settings.animateNoteThemes,
                    onSelect = { chosen ->
                        themeId = chosen
                        if (settings.autoSave) commitAutoSave()
                    }
                )
            }

            AnimatedVisibility(
                visible = showFormatBar,
                enter = slideInVertically { it } + expandVertically() + fadeIn(),
                exit = slideOutVertically { it } + shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = if (activeTheme?.isDark == false) Color.White.copy(alpha = 0.94f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    tonalElevation = 6.dp
                ) {
                    RichFormatBar(
                        state = richState,
                        textColor = editorTextColor,
                        accentColor = editorAccentColor
                    )
                }
            }

            val bottomShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            Surface(
                shape = bottomShape,
                color = if (activeTheme == null) MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                else if (activeTheme.isDark) Color.Black.copy(alpha = 0.42f)
                else Color.White.copy(alpha = 0.90f),
                border = BorderStroke(1.dp, editorMutedColor.copy(alpha = 0.16f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val hasFormatting = showFormatBar || richState.activeFlags.isNotEmpty() ||
                            richState.activeColor != null || richState.activeSize != null ||
                            (richState.activeFont != null && richState.activeFont != "sans-serif")
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showFormatBar = !showFormatBar }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            if (hasFormatting && !showFormatBar) "Aa•" else "Aa",
                            color = if (hasFormatting) editorAccentColor else editorTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Текст", color = if (hasFormatting) editorAccentColor else editorMutedColor, fontSize = 10.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        NoteVectorIcon("photo", editorTextColor, size = 18.dp)
                        Spacer(Modifier.height(2.dp))
                        Text("Фото", color = editorMutedColor, fontSize = 10.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showInsertMenu = true }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        NoteVectorIcon("plus", editorTextColor, size = 18.dp)
                        Spacer(Modifier.height(2.dp))
                        Text("Вставить", color = editorMutedColor, fontSize = 10.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { launchSpeech() }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        NoteVectorIcon("mic", editorTextColor, size = 18.dp)
                        Spacer(Modifier.height(2.dp))
                        Text("Голос", color = editorMutedColor, fontSize = 10.sp)
                    }
                }
            }
        }

        if (settings.keyboardToNoteAnimation) {
            KeyboardToNoteOverlay(
                controller = keyboardAnimController,
                modifier = Modifier.matchParentSize().zIndex(10f)
            )
        }
    }

    previewPhotoPath?.let { path ->
        val fullBitmapState = remember(path) { mutableStateOf<Bitmap?>(null) }
        LaunchedEffect(path) {
            fullBitmapState.value = withContext(Dispatchers.IO) { decodeFullPreview(path) }
        }
        Dialog(
            onDismissRequest = { previewPhotoPath = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .clickable { previewPhotoPath = null }
            ) {
                fullBitmapState.value?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Просмотр фото",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.16f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(18.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { previewPhotoPath = null }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Close, contentDescription = "Закрыть", tint = Color.White)
                    }
                }
            }
        }
    }

    if (showReminderPicker) {
        ReminderPickerDialog(
            onDismiss = { showReminderPicker = false },
            onConfirm = { timestamp, type ->
                if (reminderTimes.none { kotlin.math.abs(it - timestamp) < 60_000L }) {
                    reminderTimes.add(timestamp)
                    reminderTimes.sort()
                    reminderTypes[timestamp] = type
                    requestReminderPermissions()
                    if (settings.autoSave && (title.isNotBlank() || bodyHasContent || reminderTimes.isNotEmpty() || tableList.isNotEmpty())) commitAutoSave()
                }
                showReminderPicker = false
            }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Сохранить заметку?") },
            text = { Text("Можно сохранить изменения или выйти без сохранения.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        if (canSave) saveDraft() else onClose()
                    },
                    enabled = canSave
                ) { Text("Сохранить") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { showExitDialog = false }) { Text("Остаться") }
                    TextButton(onClick = { showExitDialog = false; onClose() }) { Text("Выйти без сохранения") }
                }
            }
        )
    }

    if (showInsertMenu) {
        AlertDialog(
            onDismissRequest = { showInsertMenu = false },
            title = { Text("Добавить в заметку") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        showInsertMenu = false
                        tableRowCount = 3
                        tableColumnCount = 3
                        showTableSizeDialog = true
                    }) {
                        NoteVectorIcon("table", MaterialTheme.colorScheme.primary, size = 18.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Таблицу")
                    }
                    TextButton(onClick = { showInsertMenu = false; showLinkDialog = true }) {
                        NoteVectorIcon("share", MaterialTheme.colorScheme.primary, size = 18.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Гиперссылку")
                    }
                    TextButton(onClick = {
                        showInsertMenu = false
                        documentLauncher.launch(arrayOf("*/*"))
                    }) {
                        NoteVectorIcon("clip", MaterialTheme.colorScheme.primary, size = 18.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Импортировать файл")
                    }
                    TextButton(onClick = { showInsertMenu = false; showAttachNoteDialog = true }) {
                        Icon(Icons.Filled.Create, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Вложить заметку")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showInsertMenu = false }) { Text("Закрыть") } }
        )
    }

    if (showTableSizeDialog) {
        AlertDialog(
            onDismissRequest = { showTableSizeDialog = false },
            title = { Text("Размер таблицы") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Строк: $tableRowCount", color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { tableRowCount = (tableRowCount - 1).coerceAtLeast(1) }) { Text("−") }
                        Text("$tableRowCount", modifier = Modifier.padding(horizontal = 18.dp))
                        TextButton(onClick = { tableRowCount = (tableRowCount + 1).coerceAtMost(12) }) { Text("+") }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Столбцов: $tableColumnCount", color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { tableColumnCount = (tableColumnCount - 1).coerceAtLeast(1) }) { Text("−") }
                        Text("$tableColumnCount", modifier = Modifier.padding(horizontal = 18.dp))
                        TextButton(onClick = { tableColumnCount = (tableColumnCount + 1).coerceAtMost(8) }) { Text("+") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    tableList.add(NoteTable(cells = List(tableRowCount) { List(tableColumnCount) { "" } }))
                    if (settings.autoSave) commitAutoSave()
                    showTableSizeDialog = false
                }) { Text("Создать таблицу") }
            },
            dismissButton = { TextButton(onClick = { showTableSizeDialog = false }) { Text("Отмена") } }
        )
    }

    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = { Text("Вставить ссылку") },
            text = {
                Column {
                    OutlinedTextField(value = linkLabel, onValueChange = { linkLabel = it }, label = { Text("Текст ссылки") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = linkAddress, onValueChange = { linkAddress = it }, label = { Text("Адрес сайта") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val label = linkLabel.ifBlank { linkAddress }
                    insertIntoBody("[$label]($linkAddress)")
                    linkLabel = ""
                    linkAddress = ""
                    showLinkDialog = false
                }, enabled = linkAddress.startsWith("https://") || linkAddress.startsWith("http://")) { Text("Вставить") }
            },
            dismissButton = { TextButton(onClick = { showLinkDialog = false }) { Text("Отмена") } }
        )
    }

    if (showAttachNoteDialog) {
        val otherNotes = allNotes.filter { it.id != note?.id && !it.isDeleted }
        AlertDialog(
            onDismissRequest = { showAttachNoteDialog = false },
            title = { Text("Выберите заметку") },
            text = {
                if (otherNotes.isEmpty()) Text("Пока нет других сохранённых заметок.")
                else Column {
                    otherNotes.take(8).forEach { saved ->
                        TextButton(onClick = {
                            val excerpt = saved.plainBody.take(500)
                            insertIntoBody("Заметка «${saved.title.ifBlank { "Без названия" }}» (${saved.category}):\n$excerpt")
                            showAttachNoteDialog = false
                        }) {
                            Text("${saved.title.ifBlank { "Без названия" }} · ${saved.category}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAttachNoteDialog = false }) { Text("Закрыть") } }
        )
    }
}

@Composable
private fun ReminderPickerDialog(onDismiss: () -> Unit, onConfirm: (Long, String) -> Unit) {
    val initial = remember { Calendar.getInstance().apply { add(Calendar.MINUTE, 10); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) } }
    var day by remember { mutableStateOf(initial.get(Calendar.DAY_OF_MONTH).toString()) }
    var month by remember { mutableStateOf((initial.get(Calendar.MONTH) + 1).toString()) }
    var year by remember { mutableStateOf(initial.get(Calendar.YEAR).toString()) }
    var hour by remember { mutableStateOf(initial.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')) }
    var minute by remember { mutableStateOf(initial.get(Calendar.MINUTE).toString().padStart(2, '0')) }
    var reminderType by remember { mutableStateOf("Уведомление и будильник") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новое напоминание", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Выбери дату и время, напоминание сохранится в этой заметке.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(day, { day = it.filter(Char::isDigit).take(2) }, Modifier.weight(0.7f), label = { Text("День") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(month, { month = it.filter(Char::isDigit).take(2) }, Modifier.weight(0.7f), label = { Text("Месяц") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(year, { year = it.filter(Char::isDigit).take(4) }, Modifier.weight(1.2f), label = { Text("Год") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(hour, { hour = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f), label = { Text("Часы") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Text(":", fontSize = 20.sp)
                    OutlinedTextField(minute, { minute = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f), label = { Text("Минуты") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                ChoiceRow(
                    label = "Как напомнить",
                    value = reminderType,
                    options = listOf("Уведомление", "Будильник", "Уведомление и будильник"),
                    onSelect = { reminderType = it }
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val d = day.toIntOrNull(); val m = month.toIntOrNull(); val y = year.toIntOrNull()
                val h = hour.toIntOrNull(); val min = minute.toIntOrNull()
                if (d == null || m == null || y == null || h == null || min == null || m !in 1..12 || h !in 0..23 || min !in 0..59) {
                    error = "Проверь дату и время."
                } else {
                    val selected = Calendar.getInstance().apply { clear(); set(y, m - 1, d, h, min, 0) }
                    if (selected.get(Calendar.YEAR) != y || selected.get(Calendar.MONTH) != m - 1 || selected.get(Calendar.DAY_OF_MONTH) != d) error = "Такой даты нет в календаре."
                    else if (selected.timeInMillis <= System.currentTimeMillis()) error = "Выбери время в будущем."
                    else onConfirm(selected.timeInMillis, reminderType)
                }
            }) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun PhotoAttachment(path: String, onOpen: () -> Unit, onRemove: () -> Unit) {
    val bitmapState = remember(path) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(path) {
        bitmapState.value = withContext(Dispatchers.IO) { decodeThumbnail(path) }
    }
    val decodedBitmap: Bitmap? = bitmapState.value

    Box(modifier = Modifier.size(92.dp)) {
        if (decodedBitmap != null) {
            Image(
                bitmap = decodedBitmap.asImageBitmap(),
                contentDescription = "Фото во вложении",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpen)
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxSize().clickable(onClick = onOpen),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF26342B)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Фото", color = Color.White, fontSize = 12.sp)
                }
            }
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(Color(0xCC101612), CircleShape)
        ) {
            Text("×", color = Color.White, fontSize = 15.sp)
        }
    }
}