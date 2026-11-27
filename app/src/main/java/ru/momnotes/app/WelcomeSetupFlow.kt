package ru.momnotes.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
internal fun WelcomeSetupFlow(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onFinish: () -> Unit
) {
    var page by remember { mutableIntStateOf(0) }
    var showTitle by remember { mutableStateOf(false) }
    BackHandler(enabled = true) {
        if (page > 1) page--
    }

    LaunchedEffect(Unit) {
        delay(280)
        showTitle = true
        delay(1150)
        page = 1
    }

    val accent = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        mixColors(MaterialTheme.colorScheme.background, accent, 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    (fadeIn(tween(420)) + slideInVertically(tween(480)) { it / 10 }) togetherWith
                            (fadeOut(tween(220)) + slideOutVertically(tween(280)) { -it / 12 })
                },
                label = "welcome-pages"
            ) { current ->
                if (current == 0) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(134.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(accent.copy(alpha = 0.24f), Color.Transparent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CheckmarkMark(Modifier.size(90.dp))
                        }
                        AnimatedVisibility(
                            visible = showTitle,
                            enter = fadeIn(tween(420)) + slideInVertically(tween(460)) { it / 4 }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Отлично!",
                                    modifier = Modifier.padding(top = 18.dp),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Настроим заметки под вас за 10 секунд",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            (1..3).forEach { step ->
                                Box(
                                    modifier = Modifier
                                        .height(6.dp)
                                        .width(if (step == current) 28.dp else 10.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (step <= current) accent
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Шаг $current из 3",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                when (current) {
                                    1 -> "Стиль и порядок"
                                    2 -> "ИИ-помощник"
                                    else -> "Всё готово!"
                                },
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                when (current) {
                                    1 -> "Всё это можно поменять позже в настройках."
                                    2 -> "Выберите, как заметка будет передаваться в Алису, Gemini или DeepSeek."
                                    else -> "Можно начинать вести заметки, таблицы и подключать плагины."
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(
                                Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                when (current) {
                                    1 -> {
                                        ChoiceRow("Оформление", settings.theme, listOf("Тёмная", "Светлая", "Системная")) {
                                            onSettingsChange(settings.copy(theme = it))
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                        ChoiceRow("Сортировка заметок", settings.sortBy, listOf("По дате изменения", "По дате создания", "По названию")) {
                                            onSettingsChange(settings.copy(sortBy = it))
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                        ChoiceRow("Избранное", settings.favoriteBehavior, listOf("Поднимать наверх", "Только избранное", "Обычный порядок")) {
                                            onSettingsChange(settings.copy(favoriteBehavior = it))
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                        SettingSwitch("Автосохранение при вводе", settings.autoSave) {
                                            onSettingsChange(settings.copy(autoSave = it))
                                        }
                                        SettingSwitch("Подтверждать удаление", settings.confirmDelete) {
                                            onSettingsChange(settings.copy(confirmDelete = it))
                                        }
                                    }
                                    2 -> {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("Алиса", "Gemini", "DeepSeek").forEach { ai ->
                                                Surface(
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                                ) {
                                                    Row(
                                                        Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        AiAssistantIcon(ai, size = 18.dp)
                                                        Text(ai, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                                    }
                                                }
                                            }
                                        }
                                        ChoiceRow("Действие кнопки ИИ", settings.aiTransfer, listOf("Открыть приложение ИИ", "Копировать запрос")) {
                                            onSettingsChange(settings.copy(aiTransfer = it))
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = accent.copy(alpha = 0.12f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                "Внутри любой заметки запрос автоматически копируется в буфер обмена — удобно для любого помощника.",
                                                modifier = Modifier.padding(12.dp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    else -> {
                                        Column(
                                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("✨", fontSize = 36.sp)
                                            Text(
                                                "Добро пожаловать в «Удобные заметки»",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                "Все заметки и плагины работают прямо на телефоне, без регистрации и интернета.",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 13.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (page > 0) {
                Spacer(Modifier.height(22.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onFinish) {
                        Text("Пропустить", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { if (page < 3) page++ else onFinish() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            if (page < 3) "Далее →" else "Начать",
                            modifier = Modifier.padding(horizontal = 12.dp),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckmarkMark(modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(720, easing = FastOutSlowInEasing),
        label = "success-mark-scale"
    )
    val markColor = MaterialTheme.colorScheme.primary
    Canvas(modifier.scale(scale)) {
        val stroke = Stroke(width = size.minDimension * 0.08f)
        drawCircle(color = markColor, style = stroke)
        drawLine(
            color = markColor,
            start = Offset(size.width * 0.27f, size.height * 0.52f),
            end = Offset(size.width * 0.44f, size.height * 0.68f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
        drawLine(
            color = markColor,
            start = Offset(size.width * 0.44f, size.height * 0.68f),
            end = Offset(size.width * 0.74f, size.height * 0.34f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}