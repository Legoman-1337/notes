package ru.momnotes.app

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal fun isFeatureTourDone(context: Context): Boolean =
    context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE)
        .getBoolean("feature_tour_done", false)

internal fun setFeatureTourDone(context: Context, done: Boolean) {
    context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE).edit()
        .putBoolean("feature_tour_done", done)
        .apply()
}

private data class TourSlide(
    val title: String,
    val subtitle: String,
    val badge: String,
    val note: String
)

private val tourSlides = listOf(
    TourSlide(
        badge = "ИИ прямо в заметке",
        title = "Помощник ИИ под рукой",
        subtitle = "В любой заметке есть выбор: Алиса, Gemini или DeepSeek. Можно одной кнопкой отправить туда текст заметки и даже таблицы — при желании с вашей подсказкой.",
        note = "Подключать или настраивать ничего не нужно — пользуйтесь только когда захочется."
    ),
    TourSlide(
        badge = "Не просто текст",
        title = "Напоминания и будильник",
        subtitle = "К любой заметке можно прикрепить одно или несколько напоминаний. Вы сами выбираете формат: тихое уведомление или настоящий громкий будильник поверх экрана.",
        note = "Напоминания автоматически восстановятся даже после перезагрузки телефона."
    ),
    TourSlide(
        badge = "Мини-приложения внутри",
        title = "Плагины на все случаи",
        subtitle = "Вверху живут ваши плагины: учёт и табель, бюджет и копилка, трекеры здоровья, домашние списки и многое другое. Готовые наборы ставятся без интернета.",
        note = "Включайте только те плагины, что нужны — остальные не занимают место на панели."
    ),
    TourSlide(
        badge = "Удобная организация",
        title = "Перемещение и свой порядок",
        subtitle = "Включите «Пользовательский» порядок в меню «Сортировка ▾» или в настройках. После этого зажмите любую карточку заметки и перетащите её на новое место — или прямо на вкладку другого раздела вверху!",
        note = "Также вверху легко создавать новые разделы под свои задачи."
    ),
    TourSlide(
        badge = "Уют и эффекты",
        title = "Живые фоны, цвета и анимации",
        subtitle = "Выбирайте для каждой заметки и плагина анимированный или статичный фон (сияние, озеро, сакура, дождь…), настраивайте цвета интерфейса и летящие с клавиш буквы.",
        note = "Этот экскурс всегда можно открыть заново в Настройках или в «О приложении»."
    )
)

@Composable
internal fun FeatureTourScreen(
    animateEffects: Boolean = true,
    onFinish: () -> Unit
) {
    var page by remember { mutableIntStateOf(0) }
    val total = tourSlides.size
    val accent = MaterialTheme.colorScheme.primary

    BackHandler {
        if (page > 0) page-- else onFinish()
    }

    val pulseTransition = rememberInfiniteTransition(label = "tour-pulse")
    val ambientFloat by pulseTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tour-float"
    )
    val glowAlpha by pulseTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tour-glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        mixColors(MaterialTheme.colorScheme.background, accent, 0.12f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accent.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
                ) {
                    Text(
                        "Экскурс · ${page + 1} из $total",
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                        color = accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = onFinish) {
                    Text(
                        "Пропустить",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            AnimatedContent(
                targetState = page,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                transitionSpec = {
                    val forward = targetState >= initialState
                    (fadeIn(tween(260)) + slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) {
                        if (forward) it / 5 else -it / 5
                    }) togetherWith (fadeOut(tween(180)) + slideOutHorizontally(tween(240)) {
                        if (forward) -it / 7 else it / 7
                    })
                },
                label = "feature-tour-slide"
            ) { index ->
                val slide = tourSlides[index]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                    ) {
                        Text(
                            slide.badge,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        slide.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                    Spacer(Modifier.height(18.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, accent.copy(alpha = glowAlpha * 0.65f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        when (index) {
                            0 -> AiShowcaseVisual(accent = accent, floatOffset = ambientFloat)
                            1 -> AlarmShowcaseVisual(accent = accent, glowAlpha = glowAlpha)
                            2 -> PluginsShowcaseVisual(accent = accent, floatOffset = ambientFloat)
                            3 -> DragOrderShowcaseVisual(accent = accent, floatOffset = ambientFloat)
                            else -> ThemesEffectsShowcaseVisual(
                                accent = accent,
                                animateEffects = animateEffects,
                                floatOffset = ambientFloat
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Text(
                        slide.subtitle,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.90f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = accent.copy(alpha = 0.10f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            slide.note,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tourSlides.indices.forEach { idx ->
                    val selected = idx == page
                    val dotWidth by animateDpAsState(
                        targetValue = if (selected) 26.dp else 8.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "tour-dot-w"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(if (selected) accent else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { page = idx }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (page > 0) {
                    TextButton(onClick = { page-- }) {
                        Text("← Назад", fontSize = 15.sp)
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
                Button(
                    onClick = { if (page < total - 1) page++ else onFinish() },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = contrastColor(accent)
                    )
                ) {
                    Text(
                        if (page < total - 1) "Дальше →" else "Понятно, поехали!",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AiShowcaseVisual(accent: Color, floatOffset: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Алиса", "Gemini", "DeepSeek").forEachIndexed { idx, ai ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            translationY = if (idx == 1) floatOffset * 0.45f else 0f
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = if (idx == 1) accent.copy(alpha = 0.18f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    border = if (idx == 1) BorderStroke(1.dp, accent.copy(alpha = 0.5f)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AiAssistantIcon(ai, size = 18.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(ai, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                }
            }
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Пример подсказки для ИИ:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "«Сделай список покупок из заметки и аккуратную таблицу»",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Исправь ошибки", "Короче", "В список").forEach { chip ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accent.copy(alpha = 0.12f)
                ) {
                    Text(
                        chip,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmShowcaseVisual(accent: Color, glowAlpha: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.14f + glowAlpha * 0.14f))
                .border(1.5.dp, accent.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            NoteVectorIcon("bell", accent, size = 32.dp)
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Завтра, 08:30", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Уведомление и будильник", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(10.dp), color = accent.copy(alpha = 0.18f)) {
                    Text(
                        "Активно",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PluginsShowcaseVisual(accent: Color, floatOffset: Float) {
    val featured = listOf(
        Triple("📋", "Журнал и Табель", "Посещаемость, группы, оплаты"),
        Triple("🧚", "Фея", "Тёплые фразы, факты, советы"),
        Triple("🐷", "Бюджет и дела", "Траты, копилка, меню недели, дом"),
        Triple("💧", "Здоровье и учёт", "Вода, сон, лекарства, таймеры")
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        featured.forEachIndexed { idx, (icon, name, desc) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationX = if (idx % 2 == 0) floatOffset * 0.25f else -floatOffset * 0.25f
                    },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(icon, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun DragOrderShowcaseVisual(accent: Color, floatOffset: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accent,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Важное",
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = contrastColor(accent),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.16f),
                border = BorderStroke(1.5.dp, accent),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Идеи ↗",
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = floatOffset * 0.7f
                    rotationZ = floatOffset * 0.22f
                },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
            border = BorderStroke(1.5.dp, accent)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Список дел на неделю", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Зажмите карточку и тащите вверх или вниз", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("⠿", fontSize = 20.sp, color = accent)
            }
        }
        Text(
            "Режим: Сортировка ▾ → Пользовательский порядок",
            modifier = Modifier.fillMaxWidth(),
            color = accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ThemesEffectsShowcaseVisual(
    accent: Color,
    animateEffects: Boolean,
    floatOffset: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("nordic_aurora" to "Сияние", "paper_petals" to "Сакура", "deep_ocean" to "Космос").forEach { (id, label) ->
                val spec = NoteThemes.find(id)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                ) {
                    NoteThemeCanvas(themeId = id, isMiniPreview = true, animated = animateEffects)
                    Text(
                        label,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp),
                        color = spec?.textColor ?: Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Свои цвета, живые фоны и летящие буквы", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "✨",
                    fontSize = 16.sp,
                    modifier = Modifier.graphicsLayer { translationY = floatOffset * 0.4f }
                )
            }
        }
    }
}