package ru.momnotes.app

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

private data class TableCellAddress(val row: Int, val column: Int)

private data class DraftSnapshot(val title: String, val body: String)

private data class NoteListItem(val note: Note, val plainBody: String)

private class NoteDragState {
    var origin: Offset = Offset.Zero
    var pointer: Offset = Offset.Zero
    var offset: Offset = Offset.Zero
    val pointerState = mutableStateOf(Offset.Zero)
    val offsetState = mutableStateOf(Offset.Zero)
}

private fun detectInsertedRun(old: String, new: String): Pair<Int, String>? {
    if (new.length <= old.length) return null
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
    if (inserted.length !in 1..24 || inserted.contains('\n')) return null
    return prefix to inserted
}

@Composable
internal fun NotesRoot() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE) }
    var settings by remember { mutableStateOf(readAppSettings(context)) }
    var showStartupAnimation by remember { mutableStateOf(true) }
    var systemBarOverride by remember { mutableStateOf<Pair<Color, Boolean>?>(null) }
    val startupColor = remember(settings.iconBackground, settings.accent) {
        themeColorChoices.firstOrNull { it.first == settings.iconBackground }?.second
            ?.let(::parseHexColor)
            ?: accentColor(settings.accent)
    }
    var showWelcome by remember {
        val alreadyDone = prefs.getBoolean("welcome_done", false) ||
                prefs.all.any { (k, v) -> k.startsWith("welcome_done_") && v == true }
        mutableStateOf(!alreadyDone)
    }
    var showFeatureTour by remember { mutableStateOf(!isFeatureTourDone(context)) }
    val isDark = when (settings.theme) {
        "Светлая" -> false
        "Системная" -> isSystemInDarkTheme()
        else -> true
    }

    Box(Modifier.fillMaxSize()) {
        NotesTheme(settings) {
            val accent = accentColor(settings.accent)
            val defaultHeader = roleColor(
                settings.headerColor,
                mixColors(MaterialTheme.colorScheme.surface, accent, 0.14f)
            )
            val navBarBg = if (isDark) Color(0xFF0C100E) else Color(0xFFF3F4F0)
            val effectiveStatusColor = when {
                showStartupAnimation -> StartupAnimationBackground
                showFeatureTour -> MaterialTheme.colorScheme.background
                systemBarOverride != null -> systemBarOverride!!.first
                else -> defaultHeader
            }
            val lightStatusIcons = when {
                showStartupAnimation -> false
                showFeatureTour -> !isDark
                systemBarOverride != null -> !systemBarOverride!!.second
                else -> contrastColor(effectiveStatusColor) != Color.White
            }
            SideEffect {
                (context as? Activity)?.window?.let { window ->
                    window.statusBarColor = effectiveStatusColor.toArgb()
                    window.navigationBarColor = navBarBg.toArgb()
                    var flags = window.decorView.systemUiVisibility
                    flags = if (lightStatusIcons) flags or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    else flags and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    flags = if (!isDark) flags or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    else flags and android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                    window.decorView.systemUiVisibility = flags
                }
            }

            when {
                showStartupAnimation -> Box(Modifier.fillMaxSize().background(StartupAnimationBackground))
                showWelcome -> WelcomeSetupFlow(
                    settings = settings,
                    onSettingsChange = { changed ->
                        settings = changed
                        writeAppSettings(context, changed)
                    },
                    onFinish = {
                        prefs.edit().putBoolean("welcome_done", true).apply()
                        showWelcome = false
                    }
                )
                showFeatureTour -> FeatureTourScreen(
                    animateEffects = settings.animateNoteThemes,
                    onFinish = {
                        setFeatureTourDone(context, true)
                        showFeatureTour = false
                    }
                )
                else -> NotesApp(
                    settings = settings,
                    onSettingsChange = { changed ->
                        settings = changed
                        writeAppSettings(context, changed)
                    },
                    onStartTour = { showFeatureTour = true },
                    onStatusBarOverride = { systemBarOverride = it }
                )
            }
        }
        if (showStartupAnimation) {
            StartupAnimation(
                accent = startupColor,
                onFinished = { showStartupAnimation = false }
            )
        }
    }
}

@Composable
private fun NotesTheme(settings: AppSettings, content: @Composable () -> Unit) {
    val dark = when (settings.theme) {
        "Светлая" -> false
        "Системная" -> isSystemInDarkTheme()
        else -> true
    }
    val accent = accentColor(settings.accent)
    val colors = if (dark) {
        val background = mixColors(Color(0xFF0C100E), accent, 0.04f)
        val surface = mixColors(Color(0xFF141A17), accent, 0.055f)
        val surfaceVariant = mixColors(Color(0xFF1F2723), accent, 0.08f)
        darkColorScheme(
            primary = accent,
            onPrimary = contrastColor(accent),
            secondary = mixColors(accent, Color(0xFFB7D4C1), 0.28f),
            background = background,
            onBackground = Color(0xFFF2F5F2),
            surface = surface,
            onSurface = Color(0xFFE9EEEA),
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = Color(0xFFAAB7AF),
            outline = mixColors(Color(0xFF435048), accent, 0.12f),
            outlineVariant = Color(0xFF2B342F),
            error = Color(0xFFFF8A80)
        )
    } else {
        val background = mixColors(Color(0xFFF3F4F0), accent, 0.02f)
        val surfaceVariant = mixColors(Color(0xFFE8EAE5), accent, 0.04f)
        lightColorScheme(
            primary = accent,
            onPrimary = contrastColor(accent),
            secondary = mixColors(accent, Color(0xFF52675B), 0.25f),
            background = background,
            onBackground = Color(0xFF1B211D),
            surface = Color(0xFFFFFEFC),
            onSurface = Color(0xFF1B211D),
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = Color(0xFF56625A),
            outline = Color(0xFF879189),
            outlineVariant = Color(0xFFD5DBD5),
            error = Color(0xFFB3261E)
        )
    }
    MaterialTheme(colorScheme = colors, content = content)
}

@Composable
private fun BouncingHeaderTitle(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val bounceOffsets = remember(text) { List(text.length) { Animatable(0f) } }
    LaunchedEffect(text) {
        while (true) {
            delay(15_000L)
            for (i in text.indices) {
                if (text[i] == ' ') continue
                launch {
                    bounceOffsets[i].animateTo(
                        targetValue = -8f,
                        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                    )
                    bounceOffsets[i].animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
                delay(55L)
            }
        }
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        text.forEachIndexed { index, ch ->
            Text(
                text = ch.toString(),
                color = color,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.graphicsLayer {
                    translationY = bounceOffsets[index].value.dp.toPx()
                }
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    noteCount: Int,
    categoryCount: Int,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onAbout: () -> Unit,
    onStartTour: () -> Unit,
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    var section by remember { mutableStateOf("Внешний вид") }
    var confirmReset by remember { mutableStateOf(false) }
    val sections = listOf("Внешний вид", "Поведение", "ИИ-помощник", "Копии", "Ещё")
    val accent = accentColor(settings.accent)
    val headerColor = roleColor(settings.headerColor, mixColors(MaterialTheme.colorScheme.surface, accent, 0.14f))
    val headerContent = contrastColor(headerColor)

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(headerColor, mixColors(headerColor, Color.Black, 0.14f))
                    )
                )
                .padding(top = 8.dp, bottom = 14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = headerContent)
                }
                Column(Modifier.weight(1f)) {
                    Text("Настройки", color = headerContent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Персонализация, анимации и управление", color = headerContent.copy(alpha = 0.72f), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sections.forEach { item ->
                    val selected = section == item
                    val chipBg by animateColorAsState(
                        targetValue = if (selected) accent else headerContent.copy(alpha = 0.12f),
                        animationSpec = tween(220),
                        label = "settings-tab-bg"
                    )
                    val chipScale by animateFloatAsState(
                        targetValue = if (selected) 1.03f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "settings-tab-scale"
                    )
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = chipBg,
                        modifier = Modifier
                            .graphicsLayer { scaleX = chipScale; scaleY = chipScale }
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { section = item }
                    ) {
                        Text(
                            item,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            color = if (selected) contrastColor(accent) else headerContent.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = section,
            modifier = Modifier.fillMaxWidth().weight(1f),
            transitionSpec = {
                (fadeIn(tween(220)) + slideInVertically(tween(240, easing = FastOutSlowInEasing)) { it / 18 }) togetherWith
                        fadeOut(tween(140))
            },
            label = "settings-section-content"
        ) { currentSection ->
            val settingsListState = rememberLazyListState()
            LazyColumn(
                state = settingsListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (currentSection) {
                    "Внешний вид" -> {
                        item {
                            SettingsCard("✨  Анимации и фоны") {
                                ChoiceRow(
                                    label = "Фоны заметок",
                                    value = if (settings.animateNoteThemes) "Анимированные" else "Статичные",
                                    options = listOf("Анимированные", "Статичные")
                                ) { chosen ->
                                    onSettingsChange(settings.copy(animateNoteThemes = chosen == "Анимированные"))
                                }
                                Text(
                                    "В режиме «Анимированные» фоны заметок и плагинов оживают: сияние, волны озера, падающие лепестки, светлячки, звёзды и дождь.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                SettingSwitch(
                                    label = "Анимация «клава → заметки» (летящие буквы)",
                                    value = settings.keyboardToNoteAnimation
                                ) { onSettingsChange(settings.copy(keyboardToNoteAnimation = it)) }
                                Text(
                                    "Набранные буквы вылетают прямо с клавиш клавиатуры в точку вставки в заметке.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        item {
                            SettingsCard("🎨  Тема и шрифт") {
                                ChoiceRow("Оформление", settings.theme, listOf("Тёмная", "Светлая", "Системная")) { onSettingsChange(settings.copy(theme = it)) }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                ChoiceRow("Акцент интерфейса", settings.accent, listOf("Зелёный", "Синий", "Фиолетовый", "Бирюзовый", "Терракотовый", "Розовый")) { onSettingsChange(settings.copy(accent = it)) }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                ChoiceRow("Размер текста заметок", settings.noteTextSize, listOf("Маленький", "Средний", "Большой")) { onSettingsChange(settings.copy(noteTextSize = it)) }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                IconBackgroundSetting(settings.iconBackground) { colorName ->
                                    setLauncherIconColor(context, colorName)
                                    onSettingsChange(settings.copy(iconBackground = colorName))
                                }
                            }
                        }
                        item {
                            SettingsCard("🖌️  Своя палитра") {
                                Text("Перекрасьте любой элемент под своё настроение или оставьте «Авто».", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                ColorSetting("Верхняя панель", settings.headerColor, accent) { onSettingsChange(settings.copy(headerColor = it)) }
                                ColorSetting("Кнопки разделов", settings.sectionColor, accent) { onSettingsChange(settings.copy(sectionColor = it)) }
                                ColorSetting("Карточки заметок", settings.noteColor, MaterialTheme.colorScheme.surface) { onSettingsChange(settings.copy(noteColor = it)) }
                                ColorSetting("Таблицы в заметках", settings.tableColor, MaterialTheme.colorScheme.surfaceVariant) { onSettingsChange(settings.copy(tableColor = it)) }
                                ColorSetting("Кнопка «+»", settings.addButtonColor, accent) { onSettingsChange(settings.copy(addButtonColor = it)) }
                            }
                        }
                        item {
                            SettingsCard("🗂️  Список заметок") {
                                ChoiceRow("Плотность", settings.density, listOf("Компактно", "Просторно")) { onSettingsChange(settings.copy(density = it)) }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                ChoiceRow("Вид списка", settings.listStyle, listOf("Карточки", "Строки")) { onSettingsChange(settings.copy(listStyle = it)) }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                ChoiceRow("Скругление карточек", settings.corner, listOf("Маленькое", "Среднее", "Большое")) { onSettingsChange(settings.copy(corner = it)) }
                            }
                        }
                    }
                    "Поведение" -> item {
                        SettingsCard("⚡  Удобство работы") {
                            ChoiceRow("Сортировка", settings.sortBy, listOf("По дате изменения", "По дате создания", "По названию", "Пользовательский")) { onSettingsChange(settings.copy(sortBy = it)) }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                            ChoiceRow("Избранное", settings.favoriteBehavior, listOf("Поднимать наверх", "Только избранное", "Обычный порядок")) { onSettingsChange(settings.copy(favoriteBehavior = it)) }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                            SettingSwitch("Автосохранение при вводе", settings.autoSave) { onSettingsChange(settings.copy(autoSave = it)) }
                            SettingSwitch("Подтверждать удаление", settings.confirmDelete) { onSettingsChange(settings.copy(confirmDelete = it)) }
                            SettingSwitch("Показывать дату изменения", settings.showModifiedDate) { onSettingsChange(settings.copy(showModifiedDate = it)) }
                        }
                    }
                    "ИИ-помощник" -> item {
                        SettingsCard("✨  Передача в ИИ") {
                            Text("Доступные помощники (выбираются прямо внутри каждой заметки):", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Алиса", "Gemini", "DeepSeek").forEach { ai ->
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            AiAssistantIcon(ai, size = 18.dp)
                                            Text(ai, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                            ChoiceRow("Действие кнопки", settings.aiTransfer, listOf("Открыть приложение ИИ", "Копировать запрос")) { onSettingsChange(settings.copy(aiTransfer = it)) }
                        }
                    }
                    "Копии" -> item {
                        SettingsCard("💾  Локальные резервные копии") {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Сейчас на устройстве: $noteCount заметок · $categoryCount разделов",
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text("Резервная копия JSON сохраняет все заметки, фоны, разделы и таблицы.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onExport, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                                    Text("Экспорт", fontSize = 13.sp)
                                }
                                Button(
                                    onClick = onImport,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text("Импорт", fontSize = 13.sp)
                                }
                            }
                            Text("Импорт заменит текущие заметки и разделы.", color = Color(0xFFFFC56D), fontSize = 12.sp)
                        }
                    }
                    else -> {
                        item {
                            SettingsCard("🧭  Экскурс по приложению") {
                                Text(
                                    "Краткий обзор необычных возможностей: ИИ-помощник, будильник, плагины, перетаскивание заметок и живые фоны.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                                Button(
                                    onClick = onStartTour,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Запустить мини-туториал", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        item {
                            SettingsCard("⚙️  Система") {
                                ChoiceRow("Язык интерфейса", settings.language, listOf("Русский")) { onSettingsChange(settings.copy(language = it)) }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                SettingSwitch("Вибрация при действиях", settings.vibration) { onSettingsChange(settings.copy(vibration = it)) }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                ChoiceRow("Язык голосового ввода", settings.voiceLanguage, listOf("Русский", "Системный")) { onSettingsChange(settings.copy(voiceLanguage = it)) }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    TextButton(onClick = { confirmReset = true }) { Text("Сбросить настройки", color = MaterialTheme.colorScheme.error) }
                                    TextButton(onClick = onAbout) { Text("О приложении →") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (confirmReset) {
        CustomConfirmDialog(
            title = "Сбросить настройки?",
            message = "Настройки оформления и поведения вернутся к исходным. Заметки не удалятся.",
            confirmText = "Сбросить",
            isDanger = true,
            onDismiss = { confirmReset = false },
            onConfirm = {
                onReset()
                confirmReset = false
            }
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
internal fun ChoiceRow(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember(label) { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = true }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f).padding(end = 10.dp), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        Box {
            Surface(
                shape = RoundedCornerShape(11.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            ) {
                Text(
                    "$value  ▾",
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onSelect(option); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSetting(label: String, value: String, defaultColor: Color, onChange: (String) -> Unit) {
    var showPicker by remember(label) { mutableStateOf(false) }
    var customHex by remember(label, value) { mutableStateOf(value.ifBlank { colorHex(defaultColor) }) }
    val chosenColor = parseHexColor(value) ?: defaultColor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { customHex = value.ifBlank { colorHex(defaultColor) }; showPicker = true }
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ) {
            Text(
                if (value.isBlank()) "Авто" else "Свой",
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(chosenColor)
                .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f), CircleShape)
        )
    }
    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(label) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Готовые цвета", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        themeColorChoices.forEach { (_, hex) ->
                            val swatch = parseHexColor(hex) ?: Color.Gray
                            Box(
                                Modifier.size(34.dp).clip(CircleShape).background(swatch)
                                    .border(if (value.equals(hex, true)) 3.dp else 1.dp, if (value.equals(hex, true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape)
                                    .clickable { onChange(hex); showPicker = false }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = customHex,
                        onValueChange = { customHex = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Свой цвет, например #E8C52E") },
                        singleLine = true
                    )
                    TextButton(onClick = { onChange(""); showPicker = false }) { Text("Автоматически из темы") }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val normalized = parseHexColor(customHex)?.let(::colorHex)
                    if (normalized != null) { onChange(normalized); showPicker = false }
                }, enabled = parseHexColor(customHex) != null) { Text("Применить") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun IconBackgroundSetting(value: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Фон иконки приложения", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
            Text(value, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("Цвет значка на главном экране телефона.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            themeColorChoices.forEach { (name, hex) ->
                val swatch = parseHexColor(hex) ?: Color.Gray
                val isSelected = name == value
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.12f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "icon-swatch-scale"
                )
                Box(
                    Modifier.size(32.dp)
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .clip(CircleShape)
                        .background(swatch)
                        .border(
                            if (isSelected) 3.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                        .clickable { onChange(name) }
                )
            }
        }
    }
}

@Composable
internal fun SettingSwitch(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f).padding(end = 8.dp), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        Switch(checked = value, onCheckedChange = onChange)
    }
}

@Composable
private fun AboutScreen(
    onStartTour: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appName = remember {
        context.packageManager.getApplicationLabel(context.applicationInfo).toString()
    }
    val version = BuildConfig.VERSION_NAME
    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("О приложении", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ) {
            Column(
                Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(accent, mixColors(accent, Color.Black, 0.25f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(68.dp).graphicsLayer { scaleX = 1.42f; scaleY = 1.42f }
                    )
                }
                Text(appName, color = MaterialTheme.colorScheme.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accent.copy(alpha = 0.16f)
                ) {
                    Text(
                        "Версия $version · Большое обновление визуала",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        color = accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "Личные заметки с живыми фонами, кастомными окнами, ИИ-помощниками, будильниками и плагинами.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        SettingsCard("✨  Что нового в $version") {
            val features = listOf(
                "📝  Форматирование текста (жирный, курсив, цвет, размер), таблицы, фото, файлы и голосовой ввод.",
                "🎨  Анимированные и статичные фоны для заметок и плагинов, плюс кастомная палитра цветов.",
                "🔔  Обновлённый кастомный экран напоминаний прямо в цвете и фоне заметки + громкий будильник.",
                "⌨️  Плавная анимация «клава → заметки» с летящими буквами и эффектами приземления.",
                "🤖  Помощник ИИ (Алиса, Gemini, DeepSeek) — отправка текста и таблиц в один тап.",
                "🧩  Плагины прямо в верхней панели: «Журнал и Табель», полезные трекеры, бюджет, быт и пакеты без интернета.",
                "🔄  Тихая проверка обновлений через GitHub с загрузкой в приложении или через сайт.",
                "↕️  Перетаскивание заметок в пользовательском порядке и перенос между разделами.",
                "📤  Экспорт в PDF, HTML, Markdown, TXT, XML и полные резервные копии в JSON."
            )
            features.forEach { line ->
                Text(
                    line,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        SettingsCard("🧭  Мини-туториал") {
            Text(
                "Короткий экскурс по нестандартным фичам приложения без принуждения и лишних шагов.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Button(
                onClick = onStartTour,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Открыть экскурс по фичам", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        SettingsCard("🔒  Хранение и приватность") {
            Text(
                "Все ваши заметки, таблицы, фото, напоминания и данные плагинов хранятся локально на вашем устройстве — без обязательных аккаунтов, рекламы и подписок.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun NotesApp(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onStartTour: () -> Unit,
    onStatusBarOverride: (Pair<Color, Boolean>?) -> Unit
) {
    val context = LocalContext.current
    val notes = remember { mutableStateListOf<Note>().apply { addAll(readLocalNotesWithMigration(context)) } }
    val categoryList = remember { mutableStateListOf<String>().apply { addAll(readCategories(context)) } }
    var selectedCategory by remember { mutableStateOf<String?>(categoryList.firstOrNull()) }
    var activeDragNoteId by remember { mutableStateOf<String?>(null) }
    val noteDragState = remember { NoteDragState() }
    val activeDragPointerState = remember { mutableStateOf(Offset.Zero) }
    var noteMoveToConfirm by remember { mutableStateOf<Pair<String, String>?>(null) }
    val noteCardBounds = remember { mutableMapOf<String, Rect>() }
    val categoryDropBounds = remember { mutableMapOf<String, Rect>() }
    val manualOrderByCategory = remember {
        mutableStateMapOf<String, List<String>>().apply {
            categoryList.forEach { category ->
                val raw = context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE)
                    .getString("manual_order_$category", null)
                if (!raw.isNullOrBlank()) {
                    runCatching {
                        val array = JSONArray(raw)
                        put(category, (0 until array.length()).map { array.optString(it) }.filter(String::isNotBlank))
                    }
                }
            }
        }
    }
    var manualOrderRevision by remember { mutableStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    var appliedSearchText by remember { mutableStateOf("") }
    LaunchedEffect(searchText) {
        delay(120)
        appliedSearchText = searchText
    }
    var sortMode by remember { mutableStateOf(0) }
    var showSortMenu by remember { mutableStateOf(false) }
    var editorOpen by remember { mutableStateOf(false) }
    var noteBeingEdited by remember { mutableStateOf<Note?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var aboutOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    var updatesOpen by remember { mutableStateOf(false) }
    var pluginStoreOpen by remember { mutableStateOf(false) }
    var openPluginId by remember { mutableStateOf<String?>(null) }
    val plugins = remember { mutableStateListOf<PluginMeta>() }
    var updateState by remember { mutableStateOf(AppUpdateManager.readState(context)) }

    fun reloadPlugins() {
        plugins.clear()
        plugins.addAll(PluginManager.installed(context))
    }
    LaunchedEffect(Unit) {
        PluginManager.preinstallBundled(context)
        reloadPlugins()
    }
    LaunchedEffect(Unit) {
        if (AppUpdateManager.shouldAutoCheck(context)) {
            AppUpdateManager.check(context, silent = true).onSuccess { fresh ->
                updateState = fresh
            }
        }
    }
    LaunchedEffect(categoryList.toList()) {
        categoryDropBounds.keys.retainAll(categoryList.toSet())
    }
    val appBackgroundColor = MaterialTheme.colorScheme.background
    val pluginHeaderColor = roleColor(settings.headerColor, MaterialTheme.colorScheme.surface)
    LaunchedEffect(aboutOpen, settingsOpen, updatesOpen, pluginStoreOpen, openPluginId, editorOpen, appBackgroundColor, pluginHeaderColor) {
        if (!editorOpen) {
            when {
                aboutOpen -> onStatusBarOverride(appBackgroundColor to (contrastColor(appBackgroundColor) == Color.White))
                pluginStoreOpen || openPluginId != null -> onStatusBarOverride(pluginHeaderColor to (contrastColor(pluginHeaderColor) == Color.White))
                else -> onStatusBarOverride(null)
            }
        }
    }
    BackHandler(enabled = aboutOpen || settingsOpen || updatesOpen || pluginStoreOpen || openPluginId != null) {
        if (settingsOpen) settingsOpen = false
        else if (updatesOpen) updatesOpen = false
        else if (aboutOpen) aboutOpen = false
        else if (pluginStoreOpen) pluginStoreOpen = false
        else openPluginId = null
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            try {
                val backup = JSONObject()
                    .put("format", "mom_notes_backup")
                    .put("backupVersion", 1)
                    .put("categories", JSONArray(categoryList.toList()))
                    .put("notes", notesToJson(notes.toList()))
                context.contentResolver.openOutputStream(uri)?.use { it.write(backup.toString(2).toByteArray(Charsets.UTF_8)) }
                Toast.makeText(context, "Резервная копия сохранена", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(context, "Не удалось создать резервную копию", Toast.LENGTH_LONG).show()
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                val raw = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    ?: error("Файл не удалось прочитать")
                val backup = JSONObject(raw)
                require(backup.optString("format") == "mom_notes_backup")
                val importedNotes = backup.optJSONArray("notes")?.toNotes().orEmpty()
                val importedCategories = backup.optJSONArray("categories")?.toStringList().orEmpty()
                require(importedCategories.isNotEmpty())
                notes.clear(); notes.addAll(importedNotes)
                categoryList.clear(); categoryList.addAll(importedCategories)
                persistImported(context, notes.toList(), categoryList.toList(), "notes")
                selectedCategory = categoryList.firstOrNull()
                Toast.makeText(context, "Резервная копия импортирована", Toast.LENGTH_SHORT).show()
            } catch (error: Exception) {
                val detail = error.localizedMessage?.takeIf { it.isNotBlank() }?.take(180)
                val text = if (detail == null) "Не удалось импортировать файл резервной копии" else "Не удалось импортировать файл: $detail"
                Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun persist() {
        writeNotes(context, notes.toList(), "notes")
    }

    fun finishNoteDrag(note: Note) {
        val point = activeDragPointerState.value
        val originBounds = noteCardBounds[note.id]
        val dragOffset = noteDragState.offset
        val draggedBounds = originBounds?.let {
            Rect(it.left + dragOffset.x, it.top + dragOffset.y, it.right + dragOffset.x, it.bottom + dragOffset.y)
        }
        val destination = categoryDropBounds.entries.firstOrNull { (cat, tabBounds) ->
            cat != note.category && (tabBounds.contains(point) || (draggedBounds?.overlaps(tabBounds) == true))
        }?.key
        if (destination != null && destination != note.category) {
            noteMoveToConfirm = note.id to destination
        } else {
            val targetId = notes.asSequence()
                .filter { it.category == note.category && !it.isDeleted && it.id != note.id }
                .map { it.id }
                .firstOrNull { noteCardBounds[it]?.contains(point) == true }
            if (targetId != null) {
                val currentIds = notes.asSequence()
                    .filter { it.category == note.category && !it.isDeleted }
                    .map { it.id }
                    .toList()
                val savedOrder = manualOrderByCategory[note.category].orEmpty()
                val rank = savedOrder.withIndex().associate { it.value to it.index }
                val reorderedIds = currentIds.sortedBy { rank[it] ?: Int.MAX_VALUE }.toMutableList()
                val from = reorderedIds.indexOf(note.id)
                val to = reorderedIds.indexOf(targetId)
                if (from >= 0 && to >= 0) {
                    reorderedIds.removeAt(from)
                    reorderedIds.add(to, note.id)
                    manualOrderByCategory[note.category] = reorderedIds
                    context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE).edit()
                        .putString("manual_order_${note.category}", JSONArray(reorderedIds).toString())
                        .apply()
                    manualOrderRevision++
                }
            }
        }
        activeDragNoteId = null
        noteDragState.offset = Offset.Zero
        noteDragState.offsetState.value = Offset.Zero
    }

    val notesSnapshot by remember { derivedStateOf { notes.toList() } }
    var indexedNotes by remember { mutableStateOf(emptyList<NoteListItem>()) }
    LaunchedEffect(notesSnapshot) {
        val snapshot = notesSnapshot
        indexedNotes = withContext(Dispatchers.Default) {
            snapshot.map { note -> NoteListItem(note = note, plainBody = note.plainBody) }
        }
    }
    var visibleNotes by remember { mutableStateOf(emptyList<NoteListItem>()) }
    LaunchedEffect(indexedNotes, selectedCategory, appliedSearchText, settings.favoriteBehavior, settings.sortBy, sortMode, manualOrderRevision) {
        val locale = Locale.getDefault()
        val query = appliedSearchText.trim().lowercase(locale)
        val category = selectedCategory
        val favoriteBehavior = settings.favoriteBehavior
        val sortBy = settings.sortBy
        val activeSortMode = sortMode
        val customOrderRanks = manualOrderByCategory[category.orEmpty()].orEmpty().withIndex().associate { it.value to it.index }
        val filteredAndSorted = withContext(Dispatchers.Default) {
            val found = ArrayList<NoteListItem>(indexedNotes.size)
            indexedNotes.forEach { row ->
                val note = row.note
                if (note.isDeleted || note.category != category) return@forEach
                if (favoriteBehavior == "Только избранное" && !note.isFavorite) return@forEach
                if (activeSortMode == 2 && !note.isFavorite) return@forEach
                if (query.isNotEmpty() && !(note.title + "\n" + row.plainBody).lowercase(locale).contains(query)) return@forEach
                found.add(row)
            }
            val ordered = when (sortBy) {
                "Пользовательский" -> found.sortedBy { customOrderRanks[it.note.id] ?: Int.MAX_VALUE }
                "По дате создания" -> found.sortedByDescending { it.note.createdAt }
                "По названию" -> found.sortedBy { it.note.title.lowercase(locale) }
                else -> found.sortedByDescending { it.note.updatedAt }
            }
            when (activeSortMode) {
                1 -> ordered.asReversed()
                2 -> ordered
                else -> if (sortBy != "Пользовательский" && favoriteBehavior == "Поднимать наверх") {
                    ordered.sortedWith(compareByDescending<NoteListItem> { it.note.isFavorite }.thenByDescending { it.note.updatedAt })
                } else ordered
            }
        }
        visibleNotes = filteredAndSorted
    }

    val noteListState = rememberLazyListState()
    val addButtonScale by animateFloatAsState(
        targetValue = if (selectedCategory != null) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "add-button-scale"
    )
    val sectionMotion = remember { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(selectedCategory) {
        sectionMotion.snapTo(0.985f)
        sectionMotion.animateTo(1f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
        noteListState.scrollToItem(0)
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !aboutOpen && !settingsOpen && !updatesOpen && !editorOpen && !pluginStoreOpen && openPluginId == null,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(120))
        ) {
            val categoryItems = remember(categoryList.toList()) { categoryList.toList() }
            val activeNotesCount = notes.count { !it.isDeleted }
            val favoriteCount = notes.count { !it.isDeleted && it.isFavorite }
            val accent = accentColor(settings.accent)
            val sectionBase = roleColor(settings.sectionColor, accent)
            val headerColor = roleColor(settings.headerColor, mixColors(MaterialTheme.colorScheme.surface, accent, 0.14f))
            val headerBottom = mixColors(headerColor, Color.Black, 0.16f)
            val headerContentColor = contrastColor(headerColor)
            val headerShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, headerShape, clip = false)
                        .clip(headerShape)
                        .background(Brush.verticalGradient(listOf(headerColor, headerBottom)))
                ) {
                    Canvas(Modifier.matchParentSize()) {
                        drawCircle(
                            Brush.radialGradient(
                                listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(size.width * 0.90f, size.height * 0.15f),
                                radius = maxOf(size.width * 0.55f, 1f)
                            ),
                            radius = maxOf(size.width * 0.55f, 1f),
                            center = Offset(size.width * 0.90f, size.height * 0.15f)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                BouncingHeaderTitle(
                                    text = "Мои заметки",
                                    color = headerContentColor
                                )
                                Spacer(Modifier.height(3.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = headerContentColor.copy(alpha = 0.10f)
                                    ) {
                                        AnimatedContent(
                                            targetState = activeNotesCount,
                                            transitionSpec = {
                                                (fadeIn(tween(180)) + slideInVertically { -it / 2 }) togetherWith
                                                        (fadeOut(tween(120)) + slideOutVertically { it / 2 })
                                            },
                                            label = "active-count"
                                        ) { count ->
                                            Text(
                                                text = "Заметок: $count",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                color = headerContentColor.copy(alpha = 0.85f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    AnimatedVisibility(
                                        visible = favoriteCount > 0,
                                        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                                        exit = scaleOut() + fadeOut()
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFFC857).copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = "★ $favoriteCount",
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                                color = if (headerContentColor == Color.White) Color(0xFFFFDF8E) else Color(0xFF8A5D00),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                val enabledCount = plugins.count { it.enabled }
                                Surface(
                                    shape = RoundedCornerShape(13.dp),
                                    color = headerContentColor.copy(alpha = 0.14f),
                                    border = BorderStroke(1.dp, headerContentColor.copy(alpha = 0.20f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(13.dp))
                                        .clickable { pluginStoreOpen = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("🧩", fontSize = 13.sp)
                                        Text(
                                            if (enabledCount > 0) "$enabledCount" else "Плагины",
                                            color = headerContentColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = headerContentColor.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, headerContentColor.copy(alpha = 0.14f)),
                                    modifier = Modifier.size(35.dp).clip(CircleShape).clickable { aboutOpen = true }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Info, contentDescription = "О приложении", tint = headerContentColor, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Box(modifier = Modifier.size(37.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = headerContentColor.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, headerContentColor.copy(alpha = 0.14f)),
                                        modifier = Modifier
                                            .size(35.dp)
                                            .align(Alignment.Center)
                                            .clip(CircleShape)
                                            .clickable { updatesOpen = true }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            UpdateArrowIcon(color = headerContentColor, size = 18.dp)
                                        }
                                    }
                                    val badgeCount = updateState.visibleBadgeCount
                                    if (badgeCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE53935))
                                                .border(1.2.dp, headerColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                lineHeight = 9.sp
                                            )
                                        }
                                    }
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = headerContentColor.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, headerContentColor.copy(alpha = 0.14f)),
                                    modifier = Modifier.size(35.dp).clip(CircleShape).clickable { settingsOpen = true }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Settings, contentDescription = "Настройки", tint = headerContentColor, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            categoryItems.forEach { category ->
                                val selected = selectedCategory == category
                                val sectionNoteCount = notes.count { !it.isDeleted && it.category == category }
                                val chipBg by animateColorAsState(
                                    targetValue = if (selected) sectionBase else headerContentColor.copy(alpha = 0.12f),
                                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                                    label = "section-chip"
                                )
                                val chipScale by animateFloatAsState(
                                    targetValue = if (selected) 1.03f else 1f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                    label = "section-chip-scale"
                                )
                                val chipFg = if (selected) contrastColor(sectionBase) else headerContentColor.copy(alpha = 0.90f)
                                Surface(
                                    modifier = Modifier
                                        .height(38.dp)
                                        .graphicsLayer { scaleX = chipScale; scaleY = chipScale }
                                        .onGloballyPositioned { categoryDropBounds[category] = it.boundsInRoot() }
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            selectedCategory = category
                                            searchText = ""
                                            appliedSearchText = ""
                                            sortMode = 0
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = chipBg,
                                    border = if (selected) BorderStroke(1.dp, headerContentColor.copy(alpha = 0.30f)) else null
                                ) {
                                    Row(
                                        Modifier.padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            category,
                                            color = chipFg,
                                            fontSize = 13.sp,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                            maxLines = 1
                                        )
                                        if (sectionNoteCount > 0) {
                                            Surface(
                                                shape = CircleShape,
                                                color = chipFg.copy(alpha = if (selected) 0.22f else 0.14f)
                                            ) {
                                                Text(
                                                    "$sectionNoteCount",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                    color = chipFg,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = headerContentColor.copy(alpha = 0.14f),
                                modifier = Modifier.size(36.dp).clip(CircleShape).clickable { showCategoryDialog = true }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Add, contentDescription = "Новый раздел", tint = headerContentColor, modifier = Modifier.size(20.dp))
                                }
                            }
                            AnimatedVisibility(
                                visible = selectedCategory != null && categoryItems.size > 1,
                                enter = scaleIn() + fadeIn(),
                                exit = scaleOut() + fadeOut()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = headerContentColor.copy(alpha = 0.10f),
                                    modifier = Modifier.size(36.dp).clip(CircleShape).clickable { categoryToDelete = selectedCategory }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Clear, contentDescription = "Удалить раздел", tint = headerContentColor.copy(alpha = 0.80f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        val enabledPlugins = plugins.filter { it.enabled }
                        AnimatedVisibility(
                            visible = enabledPlugins.isNotEmpty(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(start = 18.dp, end = 18.dp, top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                enabledPlugins.forEach { plugin ->
                                    Surface(
                                        shape = RoundedCornerShape(13.dp),
                                        color = headerContentColor.copy(alpha = 0.13f),
                                        border = BorderStroke(1.dp, headerContentColor.copy(alpha = 0.18f)),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(13.dp))
                                            .clickable { openPluginId = plugin.id }
                                    ) {
                                        Row(
                                            Modifier.padding(horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(plugin.icon, fontSize = 14.sp)
                                            Text(plugin.name, color = headerContentColor, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Column(
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            scaleX = sectionMotion.value
                            scaleY = sectionMotion.value
                        }
                    ) {
                        if (selectedCategory == null) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Выберите раздел", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                Text("Все разделы находятся на верхней панели", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(20.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(sectionBase)
                                        )
                                        Spacer(Modifier.width(9.dp))
                                        AnimatedContent(
                                            targetState = selectedCategory.orEmpty(),
                                            transitionSpec = {
                                                (fadeIn(tween(200)) + slideInVertically { it / 3 }) togetherWith
                                                        (fadeOut(tween(120)) + slideOutVertically { -it / 3 })
                                            },
                                            label = "section-title"
                                        ) { titleText ->
                                            Text(
                                                titleText,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        AnimatedVisibility(
                                            visible = visibleNotes.isNotEmpty(),
                                            enter = scaleIn() + fadeIn(),
                                            exit = scaleOut() + fadeOut()
                                        ) {
                                            Row {
                                                Spacer(Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                                ) {
                                                    Text(
                                                        "${visibleNotes.size}",
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Box {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { showSortMenu = true }
                                        ) {
                                            val sortLabel = when {
                                                sortMode == 2 -> "Избранное ▾"
                                                sortMode == 1 -> "Обратный ▾"
                                                settings.sortBy == "Пользовательский" -> "Свой порядок ▾"
                                                else -> "Сортировка ▾"
                                            }
                                            Text(
                                                sortLabel,
                                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showSortMenu,
                                            onDismissRequest = { showSortMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Пользовательский порядок") },
                                                onClick = {
                                                    onSettingsChange(settings.copy(sortBy = "Пользовательский"))
                                                    sortMode = 0
                                                    showSortMenu = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("По настройке: ${settings.sortBy}") },
                                                onClick = { sortMode = 0; showSortMenu = false }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Обратный порядок") },
                                                onClick = { sortMode = 1; showSortMenu = false }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Только избранное") },
                                                onClick = { sortMode = 2; showSortMenu = false }
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = searchText,
                                    onValueChange = { searchText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    placeholder = { Text("Поиск по заметкам…", fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                                    trailingIcon = {
                                        AnimatedVisibility(
                                            visible = searchText.isNotEmpty(),
                                            enter = scaleIn() + fadeIn(),
                                            exit = scaleOut() + fadeOut()
                                        ) {
                                            IconButton(onClick = { searchText = ""; appliedSearchText = "" }) {
                                                Icon(Icons.Filled.Close, contentDescription = "Очистить", modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    )
                                )
                                Spacer(Modifier.height(10.dp))
                                Box(Modifier.fillMaxWidth().weight(1f)) {
                                    LazyColumn(
                                        state = noteListState,
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(top = 2.dp, bottom = 108.dp)
                                    ) {
                                        items(visibleNotes, key = { it.note.id }, contentType = { "note" }) { row ->
                                            val note = row.note
                                            val canDragNotes = settings.sortBy == "Пользовательский" && appliedSearchText.isBlank() && sortMode == 0
                                            Box(
                                                Modifier.fillMaxWidth().animateItem(
                                                    fadeInSpec = tween(220),
                                                    fadeOutSpec = tween(180),
                                                    placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                ).then(
                                                    if (canDragNotes) Modifier.onGloballyPositioned {
                                                        noteCardBounds[note.id] = it.boundsInRoot()
                                                    } else Modifier
                                                )
                                            ) {
                                                NoteCard(
                                                    note = note,
                                                    settings = settings,
                                                    previewText = row.plainBody,
                                                    dragEnabled = canDragNotes,
                                                    isDragging = activeDragNoteId == note.id,
                                                    dragOffset = Offset.Zero,
                                                    onDragStart = { localPoint ->
                                                        val point = noteCardBounds[note.id]?.topLeft?.plus(localPoint) ?: localPoint
                                                        noteDragState.origin = point
                                                        noteDragState.pointer = point
                                                        noteDragState.offset = Offset.Zero
                                                        noteDragState.pointerState.value = point
                                                        noteDragState.offsetState.value = Offset.Zero
                                                        activeDragPointerState.value = point
                                                        activeDragNoteId = note.id
                                                    },
                                                    onDragMove = { delta ->
                                                        noteDragState.offset += delta
                                                        noteDragState.pointer = noteDragState.origin + noteDragState.offset
                                                        noteDragState.offsetState.value = noteDragState.offset
                                                        noteDragState.pointerState.value = noteDragState.pointer
                                                        activeDragPointerState.value = noteDragState.pointer
                                                    },
                                                    onDragEnd = { finishNoteDrag(note) },
                                                    onDragCancel = {
                                                        activeDragNoteId = null
                                                        noteDragState.offset = Offset.Zero
                                                        noteDragState.offsetState.value = Offset.Zero
                                                    },
                                                    onEdit = {
                                                        noteBeingEdited = note
                                                        editorOpen = true
                                                    },
                                                    onCopy = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        val copiedText = buildString {
                                                            if (note.title.isNotBlank()) append(note.title).append("\n\n")
                                                            append(note.plainBody)
                                                        }
                                                        clipboard.setPrimaryClip(ClipData.newPlainText("Заметка", copiedText))
                                                        Toast.makeText(context, "Заметка скопирована", Toast.LENGTH_SHORT).show()
                                                    },
                                                    onDelete = {
                                                        if (settings.confirmDelete) noteToDelete = note else {
                                                            val index = notes.indexOfFirst { it.id == note.id }
                                                            if (index >= 0) {
                                                                cancelNoteReminders(context, notes[index])
                                                                notes[index] = notes[index].copy(isDeleted = true, updatedAt = System.currentTimeMillis(), reminderAt = 0L, reminders = emptyList())
                                                            }
                                                            persist()
                                                        }
                                                    },
                                                    onToggleFavorite = {
                                                        val index = notes.indexOfFirst { it.id == note.id }
                                                        if (index >= 0) {
                                                            notes[index] = notes[index].copy(isFavorite = !notes[index].isFavorite)
                                                            persist()
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = visibleNotes.isEmpty(),
                                        enter = fadeIn(tween(260)) + scaleIn(initialScale = 0.94f),
                                        exit = fadeOut(tween(150)),
                                        modifier = Modifier.align(Alignment.Center)
                                    ) {
                                        val floatTransition = rememberInfiniteTransition(label = "empty-float")
                                        val bobOffset by floatTransition.animateFloat(
                                            initialValue = -4f,
                                            targetValue = 4f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(2600, easing = FastOutSlowInEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "empty-bob"
                                        )
                                        Column(
                                            modifier = Modifier.padding(28.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .graphicsLayer { translationY = bobOffset.dp.toPx() }
                                                    .clip(RoundedCornerShape(26.dp))
                                                    .background(
                                                        Brush.radialGradient(
                                                            listOf(accent.copy(alpha = 0.24f), accent.copy(alpha = 0.06f))
                                                        )
                                                    )
                                                    .border(1.dp, accent.copy(alpha = 0.26f), RoundedCornerShape(26.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    if (searchText.isBlank()) Icons.Filled.Create else Icons.Filled.Search,
                                                    contentDescription = null,
                                                    tint = accent,
                                                    modifier = Modifier.size(34.dp)
                                                )
                                            }
                                            Spacer(Modifier.height(14.dp))
                                            Text(
                                                if (searchText.isBlank()) "Здесь пока пусто" else "Ничего не найдено",
                                                color = MaterialTheme.colorScheme.onBackground,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                if (searchText.isBlank()) "Запишите идею, список дел или важную мысль" else "Попробуйте изменить запрос",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 13.sp,
                                                textAlign = TextAlign.Center
                                            )
                                            if (searchText.isBlank()) {
                                                Spacer(Modifier.height(16.dp))
                                                Button(
                                                    onClick = { noteBeingEdited = null; editorOpen = true },
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = roleColor(settings.addButtonColor, accent),
                                                        contentColor = contrastColor(roleColor(settings.addButtonColor, accent))
                                                    )
                                                ) {
                                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Создать заметку", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (selectedCategory != null) {
                        val fabColor = roleColor(settings.addButtonColor, accent)
                        FloatingActionButton(
                            onClick = {
                                noteBeingEdited = null
                                editorOpen = true
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 18.dp, bottom = 24.dp)
                                .size(60.dp)
                                .graphicsLayer { scaleX = addButtonScale; scaleY = addButtonScale },
                            containerColor = fabColor,
                            contentColor = contrastColor(fabColor),
                            shape = RoundedCornerShape(20.dp),
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                        ) { Icon(Icons.Filled.Add, contentDescription = "Новая заметка", modifier = Modifier.size(28.dp)) }
                    }
                }
            }

            if (showCategoryDialog) {
                CustomNewSectionDialog(
                    existingCategories = categoryList.toList(),
                    onDismiss = { showCategoryDialog = false },
                    onCreate = { createdName ->
                        categoryList.add(createdName)
                        writeCategories(context, categoryList.toList())
                        selectedCategory = createdName
                        searchText = ""
                        appliedSearchText = ""
                        sortMode = 0
                        showCategoryDialog = false
                    }
                )
            }

            categoryToDelete?.let { target ->
                val count = notes.count { it.category == target && !it.isDeleted }
                CustomConfirmDialog(
                    title = "Удалить раздел?",
                    message = if (count == 0) "Раздел «$target» будет удалён." else "Раздел «$target» и все его заметки ($count) будут удалены с этого телефона.",
                    confirmText = "Удалить",
                    isDanger = true,
                    onDismiss = { categoryToDelete = null },
                    onConfirm = {
                        categoryList.remove(target)
                        categoryDropBounds.remove(target)
                        writeCategories(context, categoryList.toList())
                        val now = System.currentTimeMillis()
                        notes.indices.filter { notes[it].category == target && !notes[it].isDeleted }.forEach { index ->
                            cancelNoteReminders(context, notes[index])
                            notes[index] = notes[index].copy(isDeleted = true, updatedAt = now, reminderAt = 0L, reminders = emptyList())
                        }
                        persist()
                        selectedCategory = categoryList.firstOrNull()
                        categoryToDelete = null
                    }
                )
            }

            noteMoveToConfirm?.let { (noteId, destination) ->
                val target = notes.firstOrNull { it.id == noteId }
                if (target != null) {
                    Dialog(onDismissRequest = { noteMoveToConfirm = null }) {
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 10.dp
                        ) {
                            Column(
                                Modifier.fillMaxWidth().padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    Modifier.size(44.dp).clip(CircleShape)
                                        .background(roleColor(settings.sectionColor, accent)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("↗", color = contrastColor(roleColor(settings.sectionColor, accent)), fontSize = 24.sp)
                                }
                                Text("Перенести заметку?", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "«${target.title.ifBlank { "Без названия" }}» будет перемещена в раздел «$destination».",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { noteMoveToConfirm = null }) { Text("Отмена") }
                                    Spacer(Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            val index = notes.indexOfFirst { it.id == noteId }
                                            if (index >= 0) {
                                                notes[index] = notes[index].copy(category = destination)
                                                onSettingsChange(settings.copy(sortBy = "Пользовательский"))
                                                val existingDestinationOrder = manualOrderByCategory[destination].orEmpty()
                                                val destinationRanks = existingDestinationOrder.withIndex().associate { it.value to it.index }
                                                val destinationOrder = notes.asSequence()
                                                    .filter { it.category == destination && !it.isDeleted && it.id != noteId }
                                                    .map { it.id }
                                                    .sortedBy { destinationRanks[it] ?: Int.MAX_VALUE }
                                                    .toList() + noteId
                                                manualOrderByCategory[destination] = destinationOrder
                                                context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE).edit()
                                                    .putString("manual_order_$destination", JSONArray(destinationOrder).toString())
                                                    .apply()
                                                manualOrderRevision++
                                                selectedCategory = destination
                                                sortMode = 0
                                                persist()
                                            }
                                            noteMoveToConfirm = null
                                        },
                                        shape = RoundedCornerShape(14.dp)
                                    ) { Text("Перенести") }
                                }
                            }
                        }
                    }
                }
            }

            noteToDelete?.let { target ->
                CustomConfirmDialog(
                    title = "Удалить заметку?",
                    message = "«${target.title.ifBlank { "Без названия" }}» будет удалена с этого телефона.",
                    confirmText = "Удалить",
                    isDanger = true,
                    onDismiss = { noteToDelete = null },
                    onConfirm = {
                        val index = notes.indexOfFirst { it.id == target.id }
                        if (index >= 0) {
                            cancelNoteReminders(context, notes[index])
                            notes[index] = notes[index].copy(isDeleted = true, updatedAt = System.currentTimeMillis(), reminderAt = 0L, reminders = emptyList())
                        }
                        persist()
                        noteToDelete = null
                    }
                )
            }
        }
        NoteDragDimOverlay(visible = activeDragNoteId != null)
        activeDragNoteId?.let { draggingId ->
            val draggedNote = notes.firstOrNull { it.id == draggingId }
            val bounds = noteCardBounds[draggingId]
            if (draggedNote != null && bounds != null) {
                NoteDragOverlay(
                    note = draggedNote,
                    bounds = bounds,
                    dragState = noteDragState,
                    categoryBounds = categoryDropBounds.toMap(),
                    settings = settings
                )
            }
        }
        AnimatedVisibility(
            visible = editorOpen,
            enter = fadeIn(tween(220)) + slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 5 },
            exit = fadeOut(tween(180)) + slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { it / 6 }
        ) {
            EditorScreen(
                note = noteBeingEdited,
                category = selectedCategory.orEmpty(),
                allNotes = notes.toList(),
                settings = settings,
                onStatusBarStyle = { color, darkBg -> onStatusBarOverride(color to darkBg) },
                onAutoSave = { draft ->
                    val index = notes.indexOfFirst { it.id == draft.id }
                    if (index >= 0) notes[index] = draft else notes.add(draft)
                    updateReminder(context, draft)
                    persist()
                },
                onClose = {
                    onStatusBarOverride(null)
                    editorOpen = false
                },
                onSave = { edited ->
                    val index = notes.indexOfFirst { it.id == edited.id }
                    if (index >= 0) notes[index] = edited else notes.add(edited)
                    updateReminder(context, edited)
                    persist()
                    onStatusBarOverride(null)
                    editorOpen = false
                }
            )
        }
        AnimatedVisibility(
            visible = settingsOpen,
            enter = fadeIn(tween(220)) + slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 5 },
            exit = fadeOut(tween(180)) + slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { it / 6 }
        ) {
            SettingsScreen(
                settings = settings,
                onSettingsChange = onSettingsChange,
                noteCount = notes.count { !it.isDeleted },
                categoryCount = categoryList.size,
                onExport = { exportLauncher.launch("Заметки-резервная-копия.json") },
                onImport = { importLauncher.launch(arrayOf("application/json", "text/json", "*/*")) },
                onAbout = { settingsOpen = false; aboutOpen = true },
                onStartTour = {
                    settingsOpen = false
                    onStartTour()
                },
                onBack = { settingsOpen = false },
                onReset = { onSettingsChange(AppSettings()) }
            )
        }
        AnimatedVisibility(
            visible = updatesOpen,
            enter = fadeIn(tween(220)) + slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 5 },
            exit = fadeOut(tween(180)) + slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { it / 6 }
        ) {
            UpdatesScreen(
                settings = settings,
                state = updateState,
                onStateUpdated = { updateState = it },
                onBack = { updatesOpen = false }
            )
        }
        AnimatedVisibility(
            visible = aboutOpen,
            enter = fadeIn(tween(220)) + slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 5 },
            exit = fadeOut(tween(180)) + slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { it / 6 }
        ) {
            AboutScreen(
                onStartTour = {
                    aboutOpen = false
                    onStartTour()
                },
                onBack = { aboutOpen = false }
            )
        }
        AnimatedVisibility(
            visible = pluginStoreOpen,
            enter = fadeIn(tween(220)) + slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 5 },
            exit = fadeOut(tween(180)) + slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { it / 6 }
        ) {
            PluginStoreScreen(
                settings = settings,
                plugins = plugins.toList(),
                onPluginsChanged = { reloadPlugins() },
                onOpenPlugin = { meta ->
                    pluginStoreOpen = false
                    openPluginId = meta.id
                },
                onClose = { pluginStoreOpen = false }
            )
        }
        AnimatedVisibility(
            visible = openPluginId != null,
            enter = fadeIn(tween(220)) + slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 5 },
            exit = fadeOut(tween(180)) + slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { it / 6 }
        ) {
            openPluginId?.let { pluginId ->
                plugins.firstOrNull { it.id == pluginId }?.let { meta ->
                    PluginHostScreen(
                        meta = meta,
                        settings = settings,
                        onClose = { openPluginId = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteDragDimOverlay(visible: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 0.12f else 0f,
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "note-drag-dim"
    )
    if (alpha > 0f) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = alpha)))
    }
}

@Composable
private fun NoteDragOverlay(
    note: Note,
    bounds: Rect,
    dragState: NoteDragState,
    categoryBounds: Map<String, Rect>,
    settings: AppSettings
) {
    val hoveredCategory by remember(categoryBounds, bounds, note.category, dragState) {
        derivedStateOf {
            val dragOffset = dragState.offsetState.value
            val draggedBounds = Rect(
                bounds.left + dragOffset.x,
                bounds.top + dragOffset.y,
                bounds.right + dragOffset.x,
                bounds.bottom + dragOffset.y
            )
            categoryBounds.entries.firstOrNull { (category, tabBounds) ->
                category != note.category && draggedBounds.overlaps(tabBounds)
            }?.key
        }
    }
    val overAnotherSection = hoveredCategory != null && hoveredCategory != note.category
    val scale by animateFloatAsState(
        targetValue = if (overAnotherSection) 0.52f else 1f,
        animationSpec = tween(170, easing = FastOutSlowInEasing),
        label = "note-drag-scale"
    )
    val density = LocalDensity.current
    Box(
        Modifier
            .offset {
                IntOffset(
                    bounds.left.roundToInt() + dragState.offsetState.value.x.roundToInt(),
                    bounds.top.roundToInt() + dragState.offsetState.value.y.roundToInt()
                )
            }
            .width(with(density) { bounds.width.toDp() })
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .zIndex(5f)
    ) {
        NoteCard(
            note = note,
            settings = settings,
            previewText = note.plainBody,
            dragEnabled = false,
            isDragging = true,
            dragOffset = Offset.Zero,
            isDragPreview = true,
            onDragStart = {},
            onDragMove = {},
            onDragEnd = {},
            onDragCancel = {},
            onEdit = {},
            onCopy = {},
            onDelete = {},
            onToggleFavorite = {}
        )
    }
}

@Composable
private fun NoteCard(
    note: Note,
    settings: AppSettings,
    previewText: String,
    dragEnabled: Boolean,
    isDragging: Boolean,
    dragOffset: Offset,
    isDragPreview: Boolean = false,
    onDragStart: (Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val bodyPreview = remember(previewText) {
        previewText.lineSequence().take(3).joinToString("\n").take(240)
    }
    val formattedDate = remember(note.updatedAt) { formatDate(note.updatedAt) }
    val futureReminders = remember(note.reminders, note.reminderAt) {
        (note.reminders + listOfNotNull(note.reminderAt.takeIf { it > 0L }))
            .distinct()
            .filter { it > System.currentTimeMillis() }
            .sorted()
    }
    val normalizedAi = remember(note.aiAssistant) { normalizeAiAssistant(note.aiAssistant) }
    var actionsExpanded by remember(note.id) { mutableStateOf(false) }
    val noteTheme = remember(note.themeId) { NoteThemes.find(note.themeId) }
    val useThemeBackground = settings.noteColor.isBlank() && noteTheme != null

    val accent = accentColor(settings.accent)
    val baseCardColor = if (useThemeBackground) {
        noteTheme!!.surfaceTint
    } else {
        roleColor(settings.noteColor, MaterialTheme.colorScheme.surface)
    }
    val colorChroma = maxOf(baseCardColor.red, baseCardColor.green, baseCardColor.blue) -
            minOf(baseCardColor.red, baseCardColor.green, baseCardColor.blue)
    val colorSoftening = when {
        settings.noteColor.isNotBlank() && colorChroma > 0.22f -> 0.42f
        settings.noteColor.isNotBlank() -> 0.14f
        else -> 0f
    }
    val cardColor = mixColors(baseCardColor, MaterialTheme.colorScheme.surface, colorSoftening)
    val textColor = when {
        useThemeBackground -> noteTheme!!.textColor
        settings.noteColor.isBlank() -> MaterialTheme.colorScheme.onSurface
        else -> contrastColor(cardColor)
    }
    val focusColor = Color(1f - cardColor.red, 1f - cardColor.green, 1f - cardColor.blue)
    val defaultBorder = if (useThemeBackground) {
        noteTheme!!.accentColor.copy(alpha = 0.48f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    }
    val cardBorderColor by animateColorAsState(
        targetValue = if (isDragging) focusColor else defaultBorder,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "note-focus-border"
    )
    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 1.025f else 1f,
        animationSpec = tween(190, easing = FastOutSlowInEasing),
        label = "note-drag-scale"
    )
    val starScale by animateFloatAsState(
        targetValue = if (note.isFavorite) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "fav-star-scale"
    )
    val starTint by animateColorAsState(
        targetValue = if (note.isFavorite) Color(0xFFFFC857) else (if (useThemeBackground) noteTheme!!.mutedColor else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.42f),
        animationSpec = tween(220),
        label = "fav-star-tint"
    )
    val cardSubtextColor = when {
        useThemeBackground -> noteTheme!!.mutedColor
        settings.noteColor.isBlank() -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> textColor.copy(alpha = 0.78f)
    }
    val badgeBg = when {
        useThemeBackground && noteTheme!!.isDark -> Color.Black.copy(alpha = 0.28f)
        useThemeBackground -> Color.White.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)
    }
    val bodySize = when (settings.noteTextSize) { "Маленький" -> 12; "Большой" -> 17; else -> 14 }
    val corner = when (settings.corner) { "Маленькое" -> 14; "Большое" -> 26; else -> 20 }
    val gap = if (settings.density == "Компактно") 6.dp else 11.dp

    val dragGestureModifier = Modifier.pointerInput(note.id, dragEnabled) {
        if (dragEnabled) {
            detectDragGesturesAfterLongPress(
                onDragStart = onDragStart,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
                onDrag = { change, dragAmount ->
                    onDragMove(dragAmount)
                    change.consume()
                }
            )
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(dragGestureModifier)
            .graphicsLayer {
                scaleX = dragScale
                scaleY = dragScale
                translationX = if (isDragging) dragOffset.x else 0f
                translationY = if (isDragging) dragOffset.y else 0f
                shadowElevation = if (isDragging) 18.dp.toPx() else 4.dp.toPx()
                alpha = if (isDragging && !isDragPreview) 0f else 1f
            }
            .zIndex(if (isDragging) 2f else 0f)
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(corner.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 2.dp),
        border = BorderStroke(if (isDragging) 3.dp else 1.dp, cardBorderColor)
    ) {
        Box(Modifier.fillMaxWidth()) {
            if (useThemeBackground) {
                Box(Modifier.matchParentSize()) {
                    NoteThemeCanvas(
                        themeId = note.themeId,
                        isMiniPreview = true,
                        animated = settings.animateNoteThemes && !isDragPreview
                    )
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        noteTheme!!.surfaceTint.copy(alpha = 0.72f),
                                        noteTheme.surfaceTint.copy(alpha = 0.42f),
                                        noteTheme.surfaceTint.copy(alpha = 0.16f)
                                    )
                                )
                            )
                    )
                }
            } else {
                val stripColor = if (note.isFavorite) Color(0xFFFFC857) else accent.copy(alpha = 0.65f)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 1.dp, top = 12.dp, bottom = 12.dp)
                        .width(3.5.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .background(stripColor)
                )
            }

            if (settings.listStyle == "Строки") {
                Row(
                    Modifier.fillMaxWidth().padding(start = 15.dp, end = 6.dp, top = gap, bottom = gap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            note.title.ifBlank { "Без названия" },
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = (bodySize + 2).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = (bodyPreview.lineSequence().firstOrNull().orEmpty().ifBlank { note.category }).take(140),
                            color = cardSubtextColor,
                            fontSize = bodySize.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (settings.showModifiedDate) {
                            Text(formattedDate, color = cardSubtextColor.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "В избранное",
                            tint = starTint,
                            modifier = Modifier.size(22.dp).graphicsLayer { scaleX = starScale; scaleY = starScale }
                        )
                    }
                    NoteCardActions(
                        expanded = actionsExpanded,
                        iconTint = cardSubtextColor,
                        onExpand = { actionsExpanded = true },
                        onDismiss = { actionsExpanded = false },
                        onOpen = onEdit,
                        onCopy = onCopy,
                        onDelete = onDelete
                    )
                }
            } else {
                Column(Modifier.padding(start = 16.dp, end = 8.dp, top = gap + 4.dp, bottom = gap + 3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = note.title.ifBlank { "Без названия" },
                            modifier = Modifier.weight(1f),
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = (bodySize + 3).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(38.dp)) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "В избранное",
                                tint = starTint,
                                modifier = Modifier.size(22.dp).graphicsLayer { scaleX = starScale; scaleY = starScale }
                            )
                        }
                        NoteCardActions(
                            expanded = actionsExpanded,
                            iconTint = cardSubtextColor,
                            onExpand = { actionsExpanded = true },
                            onDismiss = { actionsExpanded = false },
                            onOpen = onEdit,
                            onCopy = onCopy,
                            onDelete = onDelete
                        )
                    }
                    if (bodyPreview.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = bodyPreview,
                            color = cardSubtextColor,
                            fontSize = bodySize.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = (bodySize + 6).sp
                        )
                    }
                    val attachCount = note.photoPaths.size + note.attachmentPaths.size
                    val hasMeta = futureReminders.isNotEmpty() || attachCount > 0 || note.tables.isNotEmpty() || normalizedAi != "Без ИИ"
                    if (hasMeta || settings.showModifiedDate) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (futureReminders.isNotEmpty()) {
                                    Surface(shape = RoundedCornerShape(9.dp), color = badgeBg) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            NoteVectorIcon("bell", textColor.copy(alpha = 0.9f), size = 12.dp)
                                            Text(
                                                formatReminderDate(futureReminders.first()),
                                                color = textColor.copy(alpha = 0.92f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                                if (attachCount > 0) {
                                    Surface(shape = RoundedCornerShape(9.dp), color = badgeBg) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            NoteVectorIcon("clip", textColor.copy(alpha = 0.9f), size = 12.dp)
                                            Text("$attachCount", color = textColor.copy(alpha = 0.92f), fontSize = 10.sp)
                                        }
                                    }
                                }
                                if (note.tables.isNotEmpty()) {
                                    Surface(shape = RoundedCornerShape(9.dp), color = badgeBg) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            NoteVectorIcon("table", textColor.copy(alpha = 0.9f), size = 12.dp)
                                            Text("${note.tables.size}", color = textColor.copy(alpha = 0.92f), fontSize = 10.sp)
                                        }
                                    }
                                }
                                if (normalizedAi != "Без ИИ") {
                                    Surface(shape = RoundedCornerShape(9.dp), color = badgeBg) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            AiAssistantIcon(normalizedAi, size = 13.dp)
                                            Text(
                                                normalizedAi,
                                                color = textColor.copy(alpha = 0.92f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                            if (settings.showModifiedDate) {
                                Text(
                                    formattedDate,
                                    modifier = Modifier.padding(start = 8.dp, end = 6.dp),
                                    color = cardSubtextColor.copy(alpha = 0.78f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCardActions(
    expanded: Boolean,
    iconTint: Color,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    Box {
        IconButton(onClick = onExpand, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Ещё", tint = iconTint, modifier = Modifier.size(20.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
            DropdownMenuItem(
                text = { Text("Открыть заметку") },
                onClick = { onDismiss(); onOpen() }
            )
            DropdownMenuItem(
                text = { Text("Скопировать текст") },
                onClick = { onDismiss(); onCopy() }
            )
            DropdownMenuItem(
                text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                onClick = { onDismiss(); onDelete() }
            )
        }
    }
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
private fun EditorScreen(
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val hostView = LocalView.current

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

    fun dismissEditorKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        richState.editText?.let { hideSoftKeyboard(context, it) }
        hideSoftKeyboard(context, hostView)
    }

    DisposableEffect(Unit) {
        onDispose {
            dismissEditorKeyboard()
        }
    }

    val keyboardAnimController = remember(note?.id) { KeyboardToNoteController() }
    var titleCoords by remember(note?.id) { mutableStateOf<LayoutCoordinates?>(null) }
    var pendingTitleInsert by remember(note?.id) { mutableStateOf<Pair<Int, String>?>(null) }
    val imeBottomPx = WindowInsets.ime.getBottom(LocalDensity.current)
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
    val history = remember(note?.id) { mutableStateListOf(DraftSnapshot(title, body)) }
    var historyIndex by remember(note?.id) { mutableStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showInsertMenu by remember { mutableStateOf(false) }
    var showShareMenu by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showAttachNoteDialog by remember { mutableStateOf(false) }
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

    val canSave = title.isNotBlank() || bodyHasContent || reminderTimes.isNotEmpty() ||
            photoPaths.isNotEmpty() || attachmentPaths.isNotEmpty() ||
            tableList.isNotEmpty() || aiInstruction.isNotBlank()

    LaunchedEffect(
        title, body, themeId, selectedAi, aiInstruction,
        reminderTimes.size, tableList.size, photoPaths.size, attachmentPaths.size,
        settings.autoSave
    ) {
        if (settings.autoSave && (canSave || lastSavedSnapshot != null)) {
            delay(500)
            commitAutoSave()
        }
    }

    fun saveDraft() {
        historyJob?.cancel()
        dismissEditorKeyboard()
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
        dismissEditorKeyboard()
        if (settings.autoSave) {
            historyJob?.cancel()
            if (canSave || lastSavedSnapshot != null) {
                saveDraft()
            } else {
                onClose()
            }
            return
        }
        if (!hasUnsavedChanges()) {
            onClose()
        } else {
            showExitDialog = true
        }
    }
    BackHandler { requestExit() }

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
                                    size = 20.dp
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
                                    size = 20.dp
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
                        tableTint = roleColor(settings.tableColor, MaterialTheme.colorScheme.surfaceVariant),
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
                    val hasFormatting = showFormatBar || richState.activeFlags.isNotEmpty()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showFormatBar = !showFormatBar }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            if (richState.activeFlags.isNotEmpty()) "Aa•" else "Aa",
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
        CustomReminderDialog(
            themeId = themeId,
            animateBackground = settings.animateNoteThemes,
            onDismiss = { showReminderPicker = false },
            onConfirm = { timestamp, type ->
                if (reminderTimes.none { kotlin.math.abs(it - timestamp) < 60_000L }) {
                    reminderTimes.add(timestamp)
                    reminderTimes.sort()
                    reminderTypes[timestamp] = type
                    requestReminderPermissions()
                    if (settings.autoSave && (canSave || lastSavedSnapshot != null)) commitAutoSave()
                }
                showReminderPicker = false
            }
        )
    }

    if (showExitDialog && !settings.autoSave) {
        CustomExitSaveDialog(
            themeId = themeId,
            animateBackground = settings.animateNoteThemes,
            canSave = canSave,
            onStay = { showExitDialog = false },
            onDiscard = {
                showExitDialog = false
                dismissEditorKeyboard()
                onClose()
            },
            onSave = {
                showExitDialog = false
                dismissEditorKeyboard()
                if (canSave) saveDraft() else onClose()
            }
        )
    }

    if (showInsertMenu) {
        CustomInsertMenuDialog(
            themeId = themeId,
            animateBackground = settings.animateNoteThemes,
            onDismiss = { showInsertMenu = false },
            onTable = { showInsertMenu = false; showTableSizeDialog = true },
            onLink = { showInsertMenu = false; showLinkDialog = true },
            onFile = { showInsertMenu = false; documentLauncher.launch(arrayOf("*/*")) },
            onAttachNote = { showInsertMenu = false; showAttachNoteDialog = true }
        )
    }

    if (showTableSizeDialog) {
        CustomTableSizeDialog(
            themeId = themeId,
            animateBackground = settings.animateNoteThemes,
            onDismiss = { showTableSizeDialog = false },
            onConfirm = { rows, cols ->
                tableList.add(NoteTable(cells = List(rows) { List(cols) { "" } }))
                if (settings.autoSave) commitAutoSave()
                showTableSizeDialog = false
            }
        )
    }

    if (showLinkDialog) {
        CustomLinkDialog(
            themeId = themeId,
            animateBackground = settings.animateNoteThemes,
            onDismiss = { showLinkDialog = false },
            onConfirm = { label, url ->
                insertIntoBody("[$label]($url)")
                showLinkDialog = false
            }
        )
    }

    if (showAttachNoteDialog) {
        val otherNotes = allNotes.filter { it.id != note?.id && !it.isDeleted }
        CustomAttachNoteDialog(
            themeId = themeId,
            animateBackground = settings.animateNoteThemes,
            otherNotes = otherNotes,
            onDismiss = { showAttachNoteDialog = false },
            onSelect = { saved ->
                val excerpt = saved.plainBody.take(500)
                insertIntoBody("Заметка «${saved.title.ifBlank { "Без названия" }}» (${saved.category}):\n$excerpt")
                showAttachNoteDialog = false
            }
        )
    }
}

@Composable
private fun NoteTableEditor(
    table: NoteTable,
    index: Int,
    tableTint: Color,
    onUpdateTable: (NoteTable) -> Unit,
    onDelete: () -> Unit
) {
    var editingCell by remember(table.id) { mutableStateOf<TableCellAddress?>(null) }
    var cellDraft by remember(table.id) { mutableStateOf("") }
    val headerCellBg = mixColors(tableTint, MaterialTheme.colorScheme.primary, 0.18f)
    val bodyCellBg = mixColors(tableTint, MaterialTheme.colorScheme.background, 0.18f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = mixColors(MaterialTheme.colorScheme.surface, tableTint, 0.30f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NoteVectorIcon("table", MaterialTheme.colorScheme.primary, size = 16.dp)
                Spacer(Modifier.width(6.dp))
                Text("Таблица $index", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                TextButton(
                    onClick = {
                        val cols = table.cells.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1
                        onUpdateTable(table.copy(cells = table.cells + listOf(List(cols) { "" })))
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) { Text("+ Строка", fontSize = 11.sp) }
                TextButton(
                    onClick = {
                        if ((table.cells.firstOrNull()?.size ?: 0) < 8) {
                            onUpdateTable(table.copy(cells = table.cells.map { it + "" }))
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) { Text("+ Столбец", fontSize = 11.sp) }
                TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 5.dp)) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(4.dp))
            Column(Modifier.horizontalScroll(rememberScrollState())) {
                table.cells.forEachIndexed { rowIndex, cells ->
                    val cellBg = if (rowIndex == 0) headerCellBg else bodyCellBg
                    val cellText = contrastColor(cellBg)
                    Row(verticalAlignment = Alignment.Top) {
                        cells.forEachIndexed { columnIndex, cell ->
                            Card(
                                modifier = Modifier
                                    .width(205.dp)
                                    .heightIn(min = if (rowIndex == 0) 54.dp else 68.dp)
                                    .padding(end = 6.dp, bottom = 6.dp)
                                    .clickable {
                                        editingCell = TableCellAddress(rowIndex, columnIndex)
                                        cellDraft = cell
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = cellBg),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = RichTextDocument.annotatedString(
                                        if (RichTextDocument.plainText(cell).isBlank()) "Нажмите для ввода" else cell
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    color = if (RichTextDocument.plainText(cell).isBlank()) cellText.copy(alpha = 0.5f) else cellText,
                                    fontWeight = if (rowIndex == 0) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    editingCell?.let { address ->
        AlertDialog(
            onDismissRequest = { editingCell = null },
            title = { Text("Ячейка ${address.row + 1}, ${address.column + 1}") },
            text = {
                RichTextField(
                    value = cellDraft,
                    onValueChange = { cellDraft = it },
                    label = "Содержимое ячейки",
                    editorHeight = 230.dp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val changed = table.cells.mapIndexed { rowIndex, cells ->
                        if (rowIndex == address.row) cells.mapIndexed { colIndex, cell -> if (colIndex == address.column) cellDraft else cell } else cells
                    }
                    onUpdateTable(table.copy(cells = changed))
                    editingCell = null
                }) { Text("Готово") }
            },
            dismissButton = { TextButton(onClick = { editingCell = null }) { Text("Отмена") } }
        )
    }
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
