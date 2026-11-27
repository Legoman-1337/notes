package ru.momnotes.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class DialogPalette(
    val bg: Color,
    val cardBg: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val border: Color,
    val isDark: Boolean
)

@Composable
private fun rememberNoteDialogPalette(themeId: String): DialogPalette {
    val noteTheme = remember(themeId) { NoteThemes.find(themeId) }
    val defaultSurface = MaterialTheme.colorScheme.surface
    val defaultVariant = MaterialTheme.colorScheme.surfaceVariant
    val defaultText = MaterialTheme.colorScheme.onSurface
    val defaultMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val defaultAccent = MaterialTheme.colorScheme.primary
    val defaultDark = contrastColor(defaultSurface) == Color.White

    return if (noteTheme != null) {
        val card = if (noteTheme.isDark) Color.Black.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.72f)
        val border = noteTheme.accentColor.copy(alpha = 0.38f)
        DialogPalette(
            bg = noteTheme.surfaceTint,
            cardBg = card,
            text = noteTheme.textColor,
            muted = noteTheme.mutedColor,
            accent = noteTheme.accentColor,
            border = border,
            isDark = noteTheme.isDark
        )
    } else {
        DialogPalette(
            bg = defaultSurface,
            cardBg = defaultVariant.copy(alpha = 0.55f),
            text = defaultText,
            muted = defaultMuted,
            accent = defaultAccent,
            border = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
            isDark = defaultDark
        )
    }
}

@Composable
private fun NoteThemedDialogCard(
    themeId: String,
    animateBackground: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.(DialogPalette) -> Unit
) {
    val palette = rememberNoteDialogPalette(themeId)
    val noteTheme = remember(themeId) { NoteThemes.find(themeId) }
    val shape = RoundedCornerShape(28.dp)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            shape = shape,
            color = palette.bg,
            border = BorderStroke(1.2.dp, palette.border),
            shadowElevation = 18.dp
        ) {
            Box(Modifier.fillMaxWidth()) {
                if (noteTheme != null) {
                    Box(Modifier.matchParentSize()) {
                        NoteThemeCanvas(
                            themeId = themeId,
                            isMiniPreview = true,
                            animated = animateBackground
                        )
                        Box(
                            Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            noteTheme.surfaceTint.copy(alpha = if (noteTheme.isDark) 0.42f else 0.46f),
                                            noteTheme.surfaceTint.copy(alpha = if (noteTheme.isDark) 0.58f else 0.56f)
                                        )
                                    )
                                )
                        )
                    }
                } else {
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        mixColors(palette.bg, palette.accent, 0.08f),
                                        palette.bg
                                    )
                                )
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    content(palette)
                }
            }
        }
    }
}

private fun formatRelativeTime(targetMillis: Long): String {
    val diffMs = targetMillis - System.currentTimeMillis()
    if (diffMs <= 0L) return "время уже прошло"
    val totalMin = (diffMs / 60_000L).toInt().coerceAtLeast(1)
    val days = totalMin / (24 * 60)
    val hours = (totalMin % (24 * 60)) / 60
    val mins = totalMin % 60
    return when {
        days > 0 -> "через $days дн. $hours ч"
        hours > 0 && mins > 0 -> "через $hours ч $mins мин"
        hours > 0 -> "через $hours ч"
        else -> "через $mins мин"
    }
}

@Composable
internal fun CustomReminderDialog(
    themeId: String,
    animateBackground: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long, String) -> Unit
) {
    var selectedMillis by remember {
        val cal = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 15)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        mutableLongStateOf(cal.timeInMillis)
    }
    var reminderType by remember { mutableStateOf("Уведомление и будильник") }

    val cal = remember(selectedMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedMillis }
    }
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val monthName = remember(selectedMillis) {
        SimpleDateFormat("MMM", Locale("ru")).format(Date(selectedMillis)).replace(".", "")
    }
    val year = cal.get(Calendar.YEAR)
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    val isFuture = selectedMillis > System.currentTimeMillis()

    fun shiftField(field: Int, amount: Int) {
        val updated = Calendar.getInstance().apply {
            timeInMillis = selectedMillis
            add(field, amount)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        selectedMillis = updated.timeInMillis
    }

    fun applyQuickMinutes(minutes: Int) {
        val updated = Calendar.getInstance().apply {
            add(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        selectedMillis = updated.timeInMillis
    }

    fun applyDayHour(addDays: Int, targetHour: Int, targetMinute: Int = 0) {
        val updated = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, addDays)
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        selectedMillis = updated.timeInMillis
    }

    NoteThemedDialogCard(
        themeId = themeId,
        animateBackground = animateBackground,
        onDismiss = onDismiss
    ) { p ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(p.accent.copy(alpha = 0.22f))
                    .border(1.dp, p.accent.copy(alpha = 0.42f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                NoteVectorIcon("bell", p.accent, size = 23.dp)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "Напоминание",
                    color = p.text,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${formatReminderDate(selectedMillis)} · ${formatRelativeTime(selectedMillis)}",
                    color = if (isFuture) p.accent else MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = CircleShape,
                color = p.cardBg,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDismiss)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Close, contentDescription = "Закрыть", tint = p.muted, modifier = Modifier.size(18.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            listOf(
                "+ 10 мин" to { applyQuickMinutes(10) },
                "+ 30 мин" to { applyQuickMinutes(30) },
                "+ 1 час" to { applyQuickMinutes(60) },
                "Сегодня 18:00" to { applyDayHour(0, 18, 0) },
                "Завтра 09:00" to { applyDayHour(1, 9, 0) },
                "Завтра 20:00" to { applyDayHour(1, 20, 0) }
            ).forEach { (label, action) ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = p.cardBg,
                    border = BorderStroke(1.dp, p.border.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { action() }
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        color = p.text,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = p.cardBg,
            border = BorderStroke(1.dp, p.border.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeStepperUnit(
                        valueText = hour.toString().padStart(2, '0'),
                        label = "Часы",
                        palette = p,
                        onMinus = { shiftField(Calendar.HOUR_OF_DAY, -1) },
                        onPlus = { shiftField(Calendar.HOUR_OF_DAY, 1) }
                    )
                    Text(
                        ":",
                        color = p.accent,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                    TimeStepperUnit(
                        valueText = minute.toString().padStart(2, '0'),
                        label = "Минуты",
                        palette = p,
                        onMinus = { shiftField(Calendar.MINUTE, -5) },
                        onPlus = { shiftField(Calendar.MINUTE, 5) },
                        onFineMinus = { shiftField(Calendar.MINUTE, -1) },
                        onFinePlus = { shiftField(Calendar.MINUTE, 1) }
                    )
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(p.muted.copy(alpha = 0.16f))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DateChipStepper(
                        text = "$day",
                        sub = "День",
                        palette = p,
                        onMinus = { shiftField(Calendar.DAY_OF_MONTH, -1) },
                        onPlus = { shiftField(Calendar.DAY_OF_MONTH, 1) },
                        modifier = Modifier.weight(1f)
                    )
                    DateChipStepper(
                        text = monthName,
                        sub = "Месяц",
                        palette = p,
                        onMinus = { shiftField(Calendar.MONTH, -1) },
                        onPlus = { shiftField(Calendar.MONTH, 1) },
                        modifier = Modifier.weight(1.1f)
                    )
                    DateChipStepper(
                        text = "$year",
                        sub = "Год",
                        palette = p,
                        onMinus = { shiftField(Calendar.YEAR, -1) },
                        onPlus = { shiftField(Calendar.YEAR, 1) },
                        modifier = Modifier.weight(1.15f)
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Как напомнить",
                color = p.muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                listOf(
                    Triple("Уведомление", "🔔", "Тихо"),
                    Triple("Будильник", "⏰", "Будильник"),
                    Triple("Уведомление и будильник", "✨", "Оба")
                ).forEach { (typeValue, icon, shortLabel) ->
                    val selected = reminderType == typeValue
                    val chipBg by animateColorAsState(
                        targetValue = if (selected) p.accent else p.cardBg,
                        animationSpec = tween(200),
                        label = "rem-type-bg"
                    )
                    val chipScale by animateFloatAsState(
                        targetValue = if (selected) 1.03f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "rem-type-scale"
                    )
                    val fg = if (selected) contrastColor(p.accent) else p.text
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer { scaleX = chipScale; scaleY = chipScale }
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { reminderType = typeValue },
                        shape = RoundedCornerShape(14.dp),
                        color = chipBg,
                        border = BorderStroke(1.dp, if (selected) p.accent else p.border.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(icon, fontSize = 15.sp)
                            Text(
                                shortLabel,
                                color = fg,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = !isFuture) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Выберите дату и время в будущем",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .weight(0.9f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .clickable(onClick = onDismiss),
                shape = RoundedCornerShape(15.dp),
                color = p.cardBg,
                border = BorderStroke(1.dp, p.border.copy(alpha = 0.45f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Отмена", color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
            Button(
                onClick = { if (isFuture) onConfirm(selectedMillis, reminderType) },
                enabled = isFuture,
                modifier = Modifier
                    .weight(1.4f)
                    .height(46.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = p.accent,
                    contentColor = contrastColor(p.accent)
                )
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Поставить", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TimeStepperUnit(
    valueText: String,
    label: String,
    palette: DialogPalette,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onFineMinus: (() -> Unit)? = null,
    onFinePlus: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StepperPillButton(text = if (onFineMinus != null) "−5" else "−", palette = palette, onClick = onMinus)
            Text(
                valueText,
                color = palette.text,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.width(46.dp),
                textAlign = TextAlign.Center
            )
            StepperPillButton(text = if (onFinePlus != null) "+5" else "+", palette = palette, onClick = onPlus)
        }
        if (onFineMinus != null && onFinePlus != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "−1",
                    color = palette.accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onFineMinus() }
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
                Text(label, color = palette.muted, fontSize = 11.sp)
                Text(
                    "+1",
                    color = palette.accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onFinePlus() }
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        } else {
            Text(label, color = palette.muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun StepperPillButton(
    text: String,
    palette: DialogPalette,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(11.dp),
        color = palette.accent.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, palette.accent.copy(alpha = 0.32f)),
        modifier = Modifier
            .size(width = 34.dp, height = 34.dp)
            .clip(RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = palette.accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DateChipStepper(
    text: String,
    sub: String,
    palette: DialogPalette,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = palette.bg.copy(alpha = if (palette.isDark) 0.42f else 0.72f),
        border = BorderStroke(1.dp, palette.border.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(sub, color = palette.muted, fontSize = 10.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "‹",
                    color = palette.accent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onMinus)
                        .padding(horizontal = 7.dp, vertical = 1.dp)
                )
                Text(
                    text,
                    color = palette.text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    "›",
                    color = palette.accent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onPlus)
                        .padding(horizontal = 7.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
internal fun CustomNewSectionDialog(
    existingCategories: List<String>,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val trimmed = name.trim()
    val isDuplicate = trimmed.isNotEmpty() && existingCategories.any { it.equals(trimmed, ignoreCase = true) }
    val canCreate = trimmed.isNotEmpty() && !isDuplicate
    val suggestions = remember(existingCategories) {
        listOf("Идеи", "Покупки", "Работа", "Семья", "Учёба", "Здоровье", "Финансы", "Планы")
            .filter { s -> existingCategories.none { it.equals(s, ignoreCase = true) } }
    }

    NoteThemedDialogCard(
        themeId = "",
        animateBackground = false,
        onDismiss = onDismiss
    ) { p ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(p.accent.copy(alpha = 0.18f))
                    .border(1.dp, p.accent.copy(alpha = 0.40f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = p.accent, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("Новый раздел", color = p.text, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("Появится вверху на панели вкладок", color = p.muted, fontSize = 12.sp)
            }
            Surface(
                shape = CircleShape,
                color = p.cardBg,
                modifier = Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onDismiss)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Close, contentDescription = "Закрыть", tint = p.muted, modifier = Modifier.size(18.dp))
                }
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Например: Покупки или Идеи", color = p.muted.copy(alpha = 0.65f)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            trailingIcon = {
                if (name.isNotEmpty()) {
                    IconButton(onClick = { name = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Очистить", modifier = Modifier.size(18.dp))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = p.text,
                unfocusedTextColor = p.text,
                focusedContainerColor = p.cardBg,
                unfocusedContainerColor = p.cardBg,
                focusedBorderColor = p.accent,
                unfocusedBorderColor = p.border.copy(alpha = 0.55f)
            )
        )

        if (suggestions.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Быстрые варианты", color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    suggestions.forEach { preset ->
                        val active = trimmed.equals(preset, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) p.accent.copy(alpha = 0.22f) else p.cardBg,
                            border = BorderStroke(1.dp, if (active) p.accent else p.border.copy(alpha = 0.5f)),
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { name = preset }
                        ) {
                            Text(
                                preset,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                color = if (active) p.accent else p.text,
                                fontSize = 12.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = isDuplicate) {
            Text(
                "Раздел с таким названием уже существует",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(0.9f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .clickable(onClick = onDismiss),
                shape = RoundedCornerShape(15.dp),
                color = p.cardBg,
                border = BorderStroke(1.dp, p.border.copy(alpha = 0.45f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Отмена", color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
            Button(
                onClick = { if (canCreate) onCreate(trimmed) },
                enabled = canCreate,
                modifier = Modifier.weight(1.4f).height(46.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = p.accent,
                    contentColor = contrastColor(p.accent)
                )
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Создать", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun CustomConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    isDanger: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    NoteThemedDialogCard(
        themeId = "",
        animateBackground = false,
        onDismiss = onDismiss
    ) { p ->
        val actionColor = if (isDanger) MaterialTheme.colorScheme.error else p.accent
        Text(title, color = p.text, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(message, color = p.muted, fontSize = 14.sp, lineHeight = 20.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onDismiss),
                shape = RoundedCornerShape(14.dp),
                color = p.cardBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Отмена", color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = actionColor,
                    contentColor = contrastColor(actionColor)
                )
            ) {
                Text(confirmText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun CustomExitSaveDialog(
    themeId: String,
    animateBackground: Boolean,
    canSave: Boolean,
    onStay: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit
) {
    NoteThemedDialogCard(
        themeId = themeId,
        animateBackground = animateBackground,
        onDismiss = onStay
    ) { p ->
        Text("Сохранить заметку?", color = p.text, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(
            "В заметке есть несохранённые изменения. Можно сохранить их или выйти без сохранения.",
            color = p.muted,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (canSave) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = p.accent,
                        contentColor = contrastColor(p.accent)
                    )
                ) {
                    Text("Сохранить и выйти", fontWeight = FontWeight.Bold)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onStay),
                    shape = RoundedCornerShape(14.dp),
                    color = p.cardBg,
                    border = BorderStroke(1.dp, p.border.copy(alpha = 0.45f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Остаться", color = p.text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onDiscard),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Не сохранять", color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
internal fun CustomInsertMenuDialog(
    themeId: String,
    animateBackground: Boolean,
    onDismiss: () -> Unit,
    onTable: () -> Unit,
    onLink: () -> Unit,
    onFile: () -> Unit,
    onAttachNote: () -> Unit
) {
    NoteThemedDialogCard(
        themeId = themeId,
        animateBackground = animateBackground,
        onDismiss = onDismiss
    ) { p ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Добавить в заметку", color = p.text, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Surface(
                shape = CircleShape,
                color = p.cardBg,
                modifier = Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onDismiss)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Close, contentDescription = "Закрыть", tint = p.muted, modifier = Modifier.size(18.dp))
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            @Composable
            fun InsertRow(title: String, subtitle: String, iconKind: String?, onClick: () -> Unit) {
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
                    shape = RoundedCornerShape(16.dp),
                    color = p.cardBg,
                    border = BorderStroke(1.dp, p.border.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(p.accent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (iconKind != null) {
                                NoteVectorIcon(iconKind, p.accent, size = 19.dp)
                            } else {
                                Icon(Icons.Filled.Create, contentDescription = null, tint = p.accent, modifier = Modifier.size(19.dp))
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(title, color = p.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(subtitle, color = p.muted, fontSize = 11.sp)
                        }
                    }
                }
            }

            InsertRow("Таблицу", "Строки и столбцы прямо внутри заметки", "table", onTable)
            InsertRow("Гиперссылку", "Кликабельная ссылка на сайт", "share", onLink)
            InsertRow("Файл или документ", "Прикрепить PDF, документ или архив", "clip", onFile)
            InsertRow("Другую заметку", "Вставить текст из сохранённой заметки", null, onAttachNote)
        }
    }
}

@Composable
internal fun CustomTableSizeDialog(
    themeId: String,
    animateBackground: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (rows: Int, cols: Int) -> Unit
) {
    var rows by remember { mutableIntStateOf(3) }
    var cols by remember { mutableIntStateOf(3) }

    NoteThemedDialogCard(
        themeId = themeId,
        animateBackground = animateBackground,
        onDismiss = onDismiss
    ) { p ->
        Text("Новая таблица", color = p.text, fontSize = 19.sp, fontWeight = FontWeight.Bold)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = p.cardBg,
            border = BorderStroke(1.dp, p.border.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(rows.coerceAtMost(6)) { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(cols) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (r == 0) p.accent.copy(alpha = 0.35f)
                                        else p.text.copy(alpha = 0.14f)
                                    )
                            )
                        }
                    }
                }
                if (rows > 6) {
                    Text("+ ещё ${rows - 6} стр.", color = p.muted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DateChipStepper(
                text = "$rows",
                sub = "Строк (1–12)",
                palette = p,
                onMinus = { rows = (rows - 1).coerceAtLeast(1) },
                onPlus = { rows = (rows + 1).coerceAtMost(12) },
                modifier = Modifier.weight(1f)
            )
            DateChipStepper(
                text = "$cols",
                sub = "Столбцов (1–8)",
                palette = p,
                onMinus = { cols = (cols - 1).coerceAtLeast(1) },
                onPlus = { cols = (cols + 1).coerceAtMost(8) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(0.9f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onDismiss),
                shape = RoundedCornerShape(14.dp),
                color = p.cardBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Отмена", color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
            Button(
                onClick = { onConfirm(rows, cols) },
                modifier = Modifier.weight(1.4f).height(44.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = p.accent,
                    contentColor = contrastColor(p.accent)
                )
            ) {
                Text("Создать $rows×$cols", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun CustomLinkDialog(
    themeId: String,
    animateBackground: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (label: String, url: String) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    val valid = url.startsWith("https://") || url.startsWith("http://")

    NoteThemedDialogCard(
        themeId = themeId,
        animateBackground = animateBackground,
        onDismiss = onDismiss
    ) { p ->
        Text("Вставить ссылку", color = p.text, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Текст ссылки", color = p.muted) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = p.text,
                unfocusedTextColor = p.text,
                focusedContainerColor = p.cardBg,
                unfocusedContainerColor = p.cardBg,
                focusedBorderColor = p.accent,
                unfocusedBorderColor = p.border.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Адрес (https://…)", color = p.muted) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = p.text,
                unfocusedTextColor = p.text,
                focusedContainerColor = p.cardBg,
                unfocusedContainerColor = p.cardBg,
                focusedBorderColor = p.accent,
                unfocusedBorderColor = p.border.copy(alpha = 0.5f)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.weight(0.9f).height(44.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onDismiss),
                shape = RoundedCornerShape(14.dp),
                color = p.cardBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Отмена", color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
            Button(
                onClick = { onConfirm(label.ifBlank { url }, url) },
                enabled = valid,
                modifier = Modifier.weight(1.3f).height(44.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = p.accent,
                    contentColor = contrastColor(p.accent)
                )
            ) {
                Text("Вставить", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun CustomAttachNoteDialog(
    themeId: String,
    animateBackground: Boolean,
    otherNotes: List<Note>,
    onDismiss: () -> Unit,
    onSelect: (Note) -> Unit
) {
    NoteThemedDialogCard(
        themeId = themeId,
        animateBackground = animateBackground,
        onDismiss = onDismiss
    ) { p ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Вложить заметку", color = p.text, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Surface(
                shape = CircleShape,
                color = p.cardBg,
                modifier = Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onDismiss)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Close, contentDescription = "Закрыть", tint = p.muted, modifier = Modifier.size(18.dp))
                }
            }
        }
        if (otherNotes.isEmpty()) {
            Text("Пока нет других сохранённых заметок.", color = p.muted, fontSize = 14.sp)
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                otherNotes.take(12).forEach { saved ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onSelect(saved) },
                        shape = RoundedCornerShape(14.dp),
                        color = p.cardBg,
                        border = BorderStroke(1.dp, p.border.copy(alpha = 0.45f))
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                saved.title.ifBlank { "Без названия" },
                                color = p.text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${saved.category} · ${RichTextDocument.plainText(saved.body).take(60)}".trimEnd(),
                                color = p.muted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
            Text("Закрыть", color = p.accent, fontWeight = FontWeight.SemiBold)
        }
    }
}