package ru.momnotes.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal const val APP_VERSION_DISPLAY = "3.5.5"

@Composable
internal fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    noteCount: Int,
    categoryCount: Int,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onAbout: () -> Unit,
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    var section by remember { mutableStateOf("Внешний вид") }
    var confirmReset by remember { mutableStateOf(false) }
    val sections = listOf("Внешний вид", "Поведение", "ИИ-помощник", "Копии", "Ещё")
    val bgTheme = remember(settings.appBackgroundTheme) { NoteThemes.find(settings.appBackgroundTheme) }
    val themeLocked = bgTheme != null
    val accent = bgTheme?.accentColor ?: accentColor(settings.accent)
    val headerColor = if (bgTheme != null) {
        mixColors(bgTheme.surfaceTint, accent, 0.14f)
    } else {
        roleColor(settings.headerColor, mixColors(MaterialTheme.colorScheme.surface, accent, 0.14f))
    }
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
                            SettingsCard("🖼️  Фон главного экрана") {
                                Text(
                                    if (bgTheme != null) {
                                        "Сейчас применён фон «${bgTheme.title}». Обычные темы и цвета отключены."
                                    } else {
                                        "Выберите любой живой или спокойный фон на главный экран заметок вместо обычных цветных тем."
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                                NoteThemePickerBar(
                                    selectedId = settings.appBackgroundTheme,
                                    animated = settings.animateNoteThemes,
                                    onSelect = { chosen ->
                                        onSettingsChange(settings.copy(appBackgroundTheme = chosen))
                                    }
                                )
                                AnimatedVisibility(visible = themeLocked) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = accent.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                "Активен: ${bgTheme?.title.orEmpty()}",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                color = accent,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        TextButton(onClick = {
                                            onSettingsChange(settings.copy(appBackgroundTheme = ""))
                                        }) {
                                            Text("Вернуть темы и цвета")
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            SettingsCard("✨  Анимации") {
                                SettingSwitch(
                                    label = "Анимация «клава → заметки» (летящие буквы)",
                                    value = settings.keyboardToNoteAnimation
                                ) { onSettingsChange(settings.copy(keyboardToNoteAnimation = it)) }
                                Text(
                                    "Набранные буквы вылетают прямо с клавиш клавиатуры в точку вставки в заметке.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                SettingSwitch(
                                    label = "Анимированные фоны заметок и главного экрана",
                                    value = settings.animateNoteThemes
                                ) { onSettingsChange(settings.copy(animateNoteThemes = it)) }
                                Text(
                                    "Живое сияние, волны озера, падающие лепестки, светлячки и капли дождя на фонах.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        item {
                            SettingsCard("🎨  Тема и шрифт") {
                                if (themeLocked) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "Оформление и акцент отключены, так как применён фон «${bgTheme?.title.orEmpty()}».",
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                ChoiceRow(
                                    label = "Оформление",
                                    value = if (themeLocked) "Фон" else settings.theme,
                                    options = listOf("Тёмная", "Светлая", "Системная"),
                                    enabled = !themeLocked
                                ) { onSettingsChange(settings.copy(theme = it)) }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                ChoiceRow(
                                    label = "Акцент интерфейса",
                                    value = if (themeLocked) "Из фона" else settings.accent,
                                    options = listOf("Зелёный", "Синий", "Фиолетовый", "Бирюзовый", "Терракотовый", "Розовый"),
                                    enabled = !themeLocked
                                ) { onSettingsChange(settings.copy(accent = it)) }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                ChoiceRow("Размер текста заметок", settings.noteTextSize, listOf("Маленький", "Средний", "Большой")) {
                                    onSettingsChange(settings.copy(noteTextSize = it))
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                IconBackgroundSetting(settings.iconBackground) { colorName ->
                                    setLauncherIconColor(context, colorName)
                                    onSettingsChange(settings.copy(iconBackground = colorName))
                                }
                            }
                        }
                        item {
                            SettingsCard("🖌️  Своя палитра") {
                                Text(
                                    if (themeLocked) {
                                        "Отключено: применён фон главного экрана «${bgTheme?.title.orEmpty()}». Выберите «Обычная» выше, чтобы настроить цвета вручную."
                                    } else {
                                        "Перекрасьте любой элемент под своё настроение или оставьте «Авто»."
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                                ColorSetting("Верхняя панель", settings.headerColor, accent, enabled = !themeLocked) {
                                    onSettingsChange(settings.copy(headerColor = it))
                                }
                                ColorSetting("Кнопки разделов", settings.sectionColor, accent, enabled = !themeLocked) {
                                    onSettingsChange(settings.copy(sectionColor = it))
                                }
                                ColorSetting("Карточки заметок", settings.noteColor, MaterialTheme.colorScheme.surface, enabled = !themeLocked) {
                                    onSettingsChange(settings.copy(noteColor = it))
                                }
                                ColorSetting("Таблицы в заметках", settings.tableColor, MaterialTheme.colorScheme.surfaceVariant, enabled = !themeLocked) {
                                    onSettingsChange(settings.copy(tableColor = it))
                                }
                                ColorSetting("Кнопка «+»", settings.addButtonColor, accent, enabled = !themeLocked) {
                                    onSettingsChange(settings.copy(addButtonColor = it))
                                }
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
                    else -> item {
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
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Сбросить настройки?") },
            text = { Text("Настройки оформления и поведения вернутся к исходным. Заметки не удалятся.") },
            confirmButton = { TextButton(onClick = { onReset(); confirmReset = false }) { Text("Сбросить", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Отмена") } }
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
internal fun ChoiceRow(
    label: String,
    value: String,
    options: List<String>,
    enabled: Boolean = true,
    onSelect: (String) -> Unit
) {
    var expanded by remember(label) { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { expanded = true }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f).padding(end = 10.dp), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        Box {
            Surface(
                shape = RoundedCornerShape(11.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 0.14f else 0.08f)
            ) {
                Text(
                    "$value  ▾",
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            DropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
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
private fun ColorSetting(
    label: String,
    value: String,
    defaultColor: Color,
    enabled: Boolean = true,
    onChange: (String) -> Unit
) {
    var showPicker by remember(label) { mutableStateOf(false) }
    var customHex by remember(label, value) { mutableStateOf(value.ifBlank { colorHex(defaultColor) }) }
    val chosenColor = parseHexColor(value) ?: defaultColor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { customHex = value.ifBlank { colorHex(defaultColor) }; showPicker = true }
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ) {
            Text(
                if (!enabled) "Фон" else if (value.isBlank()) "Авто" else "Свой",
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
    if (showPicker && enabled) {
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
internal fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val appName = remember {
        context.packageManager.getApplicationLabel(context.applicationInfo).toString()
    }
    val version35Description = remember { context.getString(R.string.about_version_description) }
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
                        "Версия $APP_VERSION_DISPLAY",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        color = accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        SettingsCard("🆕  Что нового в версии 3.5.5") {
            Text(
                "• Исправлен набор с форматированием на телефоне: цвет, размер, шрифт, жирный, курсив, подчёркивание и зачёркивание теперь держатся на всех вводимых буквах до ручного отключения.\n" +
                        "• На панели текста добавлен выбор шрифта (Sans, Serif, Mono, Cursive) и индикация активного цвета, размера и шрифта.\n" +
                        "• В настройках появился выбор живых и спокойных фонов для главного экрана заметок (при включении фона настройки обычных тем и цветов отключаются).\n" +
                        "• Обновлены таблицы: оформление под тему заметки, вставка и удаление строк и столбцов, сортировка, автоподсчёт сумм Σ и быстрое копирование.\n" +
                        "• Оптимизирована лента заметок и добавлено кэширование миниатюр фото.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }

        SettingsCard("✨  Версия 3.5 — о проекте") {
            Text(
                version35Description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        SettingsCard("🔒  Хранение и приватность") {
            Text(
                "Все ваши заметки, таблицы, фото, напоминания и данные плагинов хранятся локально на вашем устройстве — без аккаунтов и лишних подписок.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}