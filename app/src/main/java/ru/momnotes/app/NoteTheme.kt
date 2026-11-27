package ru.momnotes.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal val availableAiAssistants = listOf("Без ИИ", "Алиса", "Gemini", "DeepSeek")

internal fun normalizeAiAssistant(name: String): String =
    if (name in availableAiAssistants) name else "Без ИИ"

@Composable
internal fun AiAssistantIcon(
    assistant: String,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    mutedTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val normalized = normalizeAiAssistant(assistant)
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = minOf(w, h) / 2f
        when (normalized) {
            "Алиса" -> {
                drawCircle(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF8E2DE2), Color(0xFF5D2CE0), Color(0xFFB845FF)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    ),
                    radius = r,
                    center = Offset(cx, cy)
                )
                val swirl = Path().apply {
                    moveTo(w * 0.50f, h * 0.22f)
                    cubicTo(w * 0.66f, h * 0.28f, w * 0.77f, h * 0.52f, w * 0.72f, h * 0.72f)
                    cubicTo(w * 0.62f, h * 0.77f, w * 0.38f, h * 0.77f, w * 0.28f, h * 0.72f)
                    cubicTo(w * 0.23f, h * 0.52f, w * 0.34f, h * 0.28f, w * 0.50f, h * 0.22f)
                    close()
                }
                drawPath(swirl, Color.White.copy(alpha = 0.94f))
                drawCircle(
                    color = Color(0xFF6A32E2),
                    radius = (r * 0.21f).coerceAtLeast(1f),
                    center = Offset(cx, cy + h * 0.04f)
                )
            }
            "Gemini" -> {
                val star = Path().apply {
                    moveTo(cx, h * 0.05f)
                    quadraticBezierTo(cx, cy, w * 0.95f, cy)
                    quadraticBezierTo(cx, cy, cx, h * 0.95f)
                    quadraticBezierTo(cx, cy, w * 0.05f, cy)
                    quadraticBezierTo(cx, cy, cx, h * 0.05f)
                    close()
                }
                drawPath(
                    path = star,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF2884FF), Color(0xFF7B61FF), Color(0xFFE0598D)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.65f),
                    radius = (r * 0.14f).coerceAtLeast(1f),
                    center = Offset(cx, cy)
                )
            }
            "DeepSeek" -> {
                drawCircle(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF4D8CFF), Color(0xFF1F54E6)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    ),
                    radius = r,
                    center = Offset(cx, cy)
                )
                val whale = Path().apply {
                    moveTo(w * 0.24f, h * 0.58f)
                    cubicTo(w * 0.22f, h * 0.36f, w * 0.48f, h * 0.27f, w * 0.68f, h * 0.42f)
                    cubicTo(w * 0.74f, h * 0.34f, w * 0.80f, h * 0.31f, w * 0.83f, h * 0.34f)
                    cubicTo(w * 0.80f, h * 0.43f, w * 0.76f, h * 0.50f, w * 0.74f, h * 0.54f)
                    cubicTo(w * 0.78f, h * 0.66f, w * 0.64f, h * 0.76f, w * 0.44f, h * 0.74f)
                    cubicTo(w * 0.32f, h * 0.72f, w * 0.25f, h * 0.66f, w * 0.24f, h * 0.58f)
                    close()
                }
                drawPath(whale, Color.White)
                drawCircle(
                    color = Color(0xFF1F54E6),
                    radius = (r * 0.08f).coerceAtLeast(0.8f),
                    center = Offset(w * 0.38f, h * 0.51f)
                )
            }
            else -> {
                val strokeW = (minOf(w, h) * 0.11f).coerceAtLeast(1.2f)
                drawCircle(
                    color = mutedTint.copy(alpha = 0.65f),
                    radius = (r * 0.76f).coerceAtLeast(1f),
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeW)
                )
                drawLine(
                    color = mutedTint.copy(alpha = 0.65f),
                    start = Offset(w * 0.27f, h * 0.73f),
                    end = Offset(w * 0.73f, h * 0.27f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
internal fun NoteVectorIcon(
    kind: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = (minOf(w, h) * 0.105f).coerceAtLeast(1.5f)
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (kind) {
            "bell" -> {
                val p = Path().apply {
                    moveTo(w * 0.24f, h * 0.68f)
                    lineTo(w * 0.28f, h * 0.42f)
                    cubicTo(w * 0.28f, h * 0.22f, w * 0.72f, h * 0.22f, w * 0.72f, h * 0.42f)
                    lineTo(w * 0.76f, h * 0.68f)
                    close()
                }
                drawPath(p, color, style = stroke)
                drawLine(color, Offset(w * 0.17f, h * 0.68f), Offset(w * 0.83f, h * 0.68f), sw, StrokeCap.Round)
                drawArc(
                    color = color,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.40f, h * 0.66f),
                    size = Size(w * 0.20f, h * 0.16f),
                    style = stroke
                )
                drawCircle(color, (sw * 0.6f).coerceAtLeast(1f), Offset(w * 0.50f, h * 0.18f))
            }
            "photo" -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.12f, h * 0.18f),
                    size = Size(w * 0.76f, h * 0.64f),
                    cornerRadius = CornerRadius(w * 0.14f),
                    style = stroke
                )
                drawCircle(color, (w * 0.075f).coerceAtLeast(1f), Offset(w * 0.34f, h * 0.38f))
                val hills = Path().apply {
                    moveTo(w * 0.18f, h * 0.74f)
                    lineTo(w * 0.40f, h * 0.52f)
                    lineTo(w * 0.54f, h * 0.65f)
                    lineTo(w * 0.68f, h * 0.46f)
                    lineTo(w * 0.84f, h * 0.74f)
                }
                drawPath(hills, color, style = stroke)
            }
            "clip" -> {
                val clip = Path().apply {
                    moveTo(w * 0.66f, h * 0.30f)
                    lineTo(w * 0.34f, h * 0.62f)
                    cubicTo(w * 0.24f, h * 0.72f, w * 0.38f, h * 0.86f, w * 0.48f, h * 0.76f)
                    lineTo(w * 0.80f, h * 0.44f)
                    cubicTo(w * 0.92f, h * 0.32f, w * 0.72f, h * 0.12f, w * 0.60f, h * 0.24f)
                    lineTo(w * 0.24f, h * 0.60f)
                    cubicTo(w * 0.14f, h * 0.70f, w * 0.24f, h * 0.88f, w * 0.40f, h * 0.86f)
                }
                drawPath(clip, color, style = stroke)
            }
            "table" -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.14f, h * 0.16f),
                    size = Size(w * 0.72f, h * 0.68f),
                    cornerRadius = CornerRadius(w * 0.12f),
                    style = stroke
                )
                drawLine(color, Offset(w * 0.14f, h * 0.40f), Offset(w * 0.86f, h * 0.40f), sw)
                drawLine(color, Offset(w * 0.14f, h * 0.62f), Offset(w * 0.86f, h * 0.62f), sw)
                drawLine(color, Offset(w * 0.48f, h * 0.16f), Offset(w * 0.48f, h * 0.84f), sw)
            }
            "mic" -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.36f, h * 0.14f),
                    size = Size(w * 0.28f, h * 0.46f),
                    cornerRadius = CornerRadius(w * 0.14f),
                    style = stroke
                )
                drawArc(
                    color = color,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.24f, h * 0.32f),
                    size = Size(w * 0.52f, h * 0.40f),
                    style = stroke
                )
                drawLine(color, Offset(w * 0.50f, h * 0.72f), Offset(w * 0.50f, h * 0.86f), sw, StrokeCap.Round)
                drawLine(color, Offset(w * 0.34f, h * 0.86f), Offset(w * 0.66f, h * 0.86f), sw, StrokeCap.Round)
            }
            "share" -> {
                drawLine(color, Offset(w * 0.50f, h * 0.62f), Offset(w * 0.50f, h * 0.16f), sw, StrokeCap.Round)
                val arrow = Path().apply {
                    moveTo(w * 0.32f, h * 0.32f)
                    lineTo(w * 0.50f, h * 0.14f)
                    lineTo(w * 0.68f, h * 0.32f)
                }
                drawPath(arrow, color, style = stroke)
                val tray = Path().apply {
                    moveTo(w * 0.22f, h * 0.54f)
                    lineTo(w * 0.22f, h * 0.82f)
                    lineTo(w * 0.78f, h * 0.82f)
                    lineTo(w * 0.78f, h * 0.54f)
                }
                drawPath(tray, color, style = stroke)
            }
            "undo" -> {
                // Чёткая единая линия без самопересечения и лишних точек
                val arrow = Path().apply {
                    moveTo(w * 0.38f, h * 0.20f)
                    lineTo(w * 0.20f, h * 0.37f)
                    lineTo(w * 0.38f, h * 0.54f)
                }
                drawPath(arrow, color, style = stroke)
                val body = Path().apply {
                    moveTo(w * 0.22f, h * 0.37f)
                    lineTo(w * 0.58f, h * 0.37f)
                    arcTo(
                        rect = Rect(w * 0.38f, h * 0.37f, w * 0.79f, h * 0.76f),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    lineTo(w * 0.36f, h * 0.76f)
                }
                drawPath(body, color, style = stroke)
            }
            "redo" -> {
                val arrow = Path().apply {
                    moveTo(w * 0.62f, h * 0.20f)
                    lineTo(w * 0.80f, h * 0.37f)
                    lineTo(w * 0.62f, h * 0.54f)
                }
                drawPath(arrow, color, style = stroke)
                val body = Path().apply {
                    moveTo(w * 0.78f, h * 0.37f)
                    lineTo(w * 0.42f, h * 0.37f)
                    arcTo(
                        rect = Rect(w * 0.21f, h * 0.37f, w * 0.62f, h * 0.76f),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = -180f,
                        forceMoveTo = false
                    )
                    lineTo(w * 0.64f, h * 0.76f)
                }
                drawPath(body, color, style = stroke)
            }
            "plus" -> {
                drawLine(color, Offset(w * 0.50f, h * 0.22f), Offset(w * 0.50f, h * 0.78f), sw, StrokeCap.Round)
                drawLine(color, Offset(w * 0.22f, h * 0.50f), Offset(w * 0.78f, h * 0.50f), sw, StrokeCap.Round)
            }
        }
    }
}

internal data class NoteThemeSpec(
    val id: String,
    val title: String,
    val isDark: Boolean,
    val textColor: Color,
    val mutedColor: Color,
    val accentColor: Color,
    val surfaceTint: Color
)

internal object NoteThemes {
    val all = listOf(
        NoteThemeSpec(
            id = "",
            title = "Обычная",
            isDark = true,
            textColor = Color.Unspecified,
            mutedColor = Color.Unspecified,
            accentColor = Color.Unspecified,
            surfaceTint = Color.Unspecified
        ),
        NoteThemeSpec(
            id = "dot_bamboo",
            title = "Блокнот",
            isDark = false,
            textColor = Color(0xFF23281E),
            mutedColor = Color(0xFF66705B),
            accentColor = Color(0xFF3E7A47),
            surfaceTint = Color(0xFFF3EFE0)
        ),
        NoteThemeSpec(
            id = "matcha_grid",
            title = "Матча",
            isDark = false,
            textColor = Color(0xFF1E2A20),
            mutedColor = Color(0xFF566B58),
            accentColor = Color(0xFF3D7D50),
            surfaceTint = Color(0xFFE5EDE0)
        ),
        NoteThemeSpec(
            id = "amber_evening",
            title = "Тёплая лампа",
            isDark = true,
            textColor = Color(0xFFF6E9D8),
            mutedColor = Color(0xFFCBB194),
            accentColor = Color(0xFFF4B266),
            surfaceTint = Color(0xFF241A13)
        ),
        NoteThemeSpec(
            id = "forest_lake",
            title = "Лесное озеро",
            isDark = true,
            textColor = Color(0xFFECF7F4),
            mutedColor = Color(0xFFA7CFC8),
            accentColor = Color(0xFF7BE0CE),
            surfaceTint = Color(0xFF102E2A)
        ),
        NoteThemeSpec(
            id = "nordic_aurora",
            title = "Сияние",
            isDark = true,
            textColor = Color(0xFFF1F8FA),
            mutedColor = Color(0xFFA9C8D9),
            accentColor = Color(0xFF64DFB9),
            surfaceTint = Color(0xFF0C1D36)
        ),
        NoteThemeSpec(
            id = "rainy_window",
            title = "Дождь",
            isDark = true,
            textColor = Color(0xFFEEF3F8),
            mutedColor = Color(0xFFA9B9CA),
            accentColor = Color(0xFF8ABCF0),
            surfaceTint = Color(0xFF182430)
        ),
        NoteThemeSpec(
            id = "teal_monstera",
            title = "Тропики",
            isDark = true,
            textColor = Color(0xFFFFFFFF),
            mutedColor = Color(0xFFD2F4F7),
            accentColor = Color(0xFFA7F7F0),
            surfaceTint = Color(0xFF0A6673)
        ),
        NoteThemeSpec(
            id = "paper_petals",
            title = "Сакура",
            isDark = false,
            textColor = Color(0xFF2D2021),
            mutedColor = Color(0xFF7E6163),
            accentColor = Color(0xFFC8545F),
            surfaceTint = Color(0xFFF5E7E3)
        ),
        NoteThemeSpec(
            id = "clean_mist",
            title = "Горы",
            isDark = false,
            textColor = Color(0xFF1A2530),
            mutedColor = Color(0xFF5D6F80),
            accentColor = Color(0xFF2E6CB5),
            surfaceTint = Color(0xFFE3ECF4)
        ),
        NoteThemeSpec(
            id = "deep_ocean",
            title = "Космос",
            isDark = true,
            textColor = Color(0xFFF5F8FF),
            mutedColor = Color(0xFFBBD2FF),
            accentColor = Color(0xFF8AB8FF),
            surfaceTint = Color(0xFF0C2A68)
        ),
        NoteThemeSpec(
            id = "dark_cube",
            title = "Кристаллы",
            isDark = true,
            textColor = Color(0xFFF3F5FA),
            mutedColor = Color(0xFFA1ACBD),
            accentColor = Color(0xFF7AA7FF),
            surfaceTint = Color(0xFF1A202B)
        ),
        NoteThemeSpec(
            id = "sage_garden",
            title = "Шалфей",
            isDark = true,
            textColor = Color(0xFFF2F8F3),
            mutedColor = Color(0xFFB4D5BF),
            accentColor = Color(0xFF8CE5B0),
            surfaceTint = Color(0xFF19422D)
        ),
        NoteThemeSpec(
            id = "warm_cream",
            title = "Прованс",
            isDark = false,
            textColor = Color(0xFF2D211B),
            mutedColor = Color(0xFF83685A),
            accentColor = Color(0xFFC26438),
            surfaceTint = Color(0xFFF3DFCE)
        )
    )

    fun find(id: String): NoteThemeSpec? =
        id.takeIf { it.isNotBlank() }?.let { key -> all.firstOrNull { it.id == key } }
}

private fun DrawScope.drawPalmTree(
    baseX: Float,
    baseY: Float,
    topX: Float,
    topY: Float,
    ctrlX: Float,
    ctrlY: Float,
    frondLen: Float,
    trunkColor: Color,
    leafDark: Color,
    leafLight: Color,
    scale: Float,
    swayDeg: Float = 0f
) {
    val trunk = Path().apply {
        moveTo(baseX - 9f * scale, baseY)
        quadraticBezierTo(ctrlX - 4f * scale, ctrlY, topX - 2.5f * scale, topY)
        lineTo(topX + 2.5f * scale, topY)
        quadraticBezierTo(ctrlX + 5f * scale, ctrlY, baseX + 10f * scale, baseY)
        close()
    }
    drawPath(trunk, trunkColor)

    for (i in 1..6) {
        val t = i / 7f
        val omt = 1f - t
        val px = omt * omt * baseX + 2f * omt * t * ctrlX + t * t * topX
        val py = omt * omt * baseY + 2f * omt * t * ctrlY + t * t * topY
        val hw = (9f - 6f * t) * scale
        drawLine(
            color = Color.Black.copy(alpha = 0.16f),
            start = Offset(px - hw, py),
            end = Offset(px + hw, py - 2f * scale),
            strokeWidth = (1.5f * scale).coerceAtLeast(0.7f)
        )
    }

    val cocoColor = Color(0xFF2A1C16).copy(alpha = 0.72f)
    drawCircle(cocoColor, (4.8f * scale).coerceAtLeast(1.2f), Offset(topX - 4f * scale, topY + 5f * scale))
    drawCircle(cocoColor, (4.4f * scale).coerceAtLeast(1.2f), Offset(topX + 4f * scale, topY + 5.5f * scale))
    drawCircle(cocoColor, (4.2f * scale).coerceAtLeast(1.1f), Offset(topX, topY + 8f * scale))

    val frondAngles = listOf(-155f, -122f, -86f, -52f, -18f, 15f, 152f)
    frondAngles.forEachIndexed { idx, baseDeg ->
        val deg = baseDeg + swayDeg * (if (idx % 2 == 0) 1f else 0.7f)
        val rad = deg * (PI / 180.0).toFloat()
        val endX = topX + cos(rad) * frondLen
        val endY = topY + sin(rad) * frondLen * 0.75f + frondLen * 0.22f
        val bendX = topX + cos(rad) * frondLen * 0.52f
        val bendY = topY + sin(rad) * frondLen * 0.35f - frondLen * 0.14f
        val color = if (idx % 2 == 0) leafLight else leafDark

        val blade = Path().apply {
            moveTo(topX, topY)
            quadraticBezierTo(bendX, bendY - 8f * scale, endX, endY)
            quadraticBezierTo(bendX, bendY + 9f * scale, topX, topY)
            close()
        }
        drawPath(blade, color)

        for (step in 2..7) {
            val t = step / 8f
            val omt = 1f - t
            val rx = omt * omt * topX + 2f * omt * t * bendX + t * t * endX
            val ry = omt * omt * topY + 2f * omt * t * bendY + t * t * endY
            val drop = (12f + (1f - t) * 14f) * scale
            val spread = (if (cos(rad) >= 0f) 1f else -1f) * 5f * scale
            val leaflet = Path().apply {
                moveTo(rx, ry)
                quadraticBezierTo(rx + spread, ry + drop * 0.45f, rx + spread * 0.4f, ry + drop)
                lineTo(rx - spread * 0.4f, ry + drop * 0.35f)
                close()
            }
            drawPath(leaflet, color.copy(alpha = 0.90f))
        }
    }
}

private fun DrawScope.drawSakuraBlossom(
    cx: Float,
    cy: Float,
    radius: Float,
    petalColor: Color,
    innerColor: Color
) {
    for (i in 0 until 5) {
        val angle = i * 72f - 18f
        rotate(degrees = angle, pivot = Offset(cx, cy)) {
            val petal = Path().apply {
                moveTo(cx, cy)
                cubicTo(
                    cx - radius * 0.58f, cy - radius * 0.35f,
                    cx - radius * 0.50f, cy - radius * 1.05f,
                    cx - radius * 0.08f, cy - radius * 0.96f
                )
                lineTo(cx, cy - radius * 0.80f)
                lineTo(cx + radius * 0.08f, cy - radius * 0.96f)
                cubicTo(
                    cx + radius * 0.50f, cy - radius * 1.05f,
                    cx + radius * 0.58f, cy - radius * 0.35f,
                    cx, cy
                )
                close()
            }
            drawPath(petal, petalColor)
        }
    }
    drawCircle(innerColor, (radius * 0.22f).coerceAtLeast(1f), Offset(cx, cy))
    for (i in 0 until 5) {
        val a = (i * 72f + 18f) * (PI / 180.0).toFloat()
        val sx = cx + cos(a) * radius * 0.34f
        val sy = cy + sin(a) * radius * 0.34f
        drawLine(innerColor, Offset(cx, cy), Offset(sx, sy), (radius * 0.06f).coerceAtLeast(0.6f))
        drawCircle(Color(0xFFFFD87A), (radius * 0.08f).coerceAtLeast(0.7f), Offset(sx, sy))
    }
}

private fun DrawScope.drawFourPointStar(cx: Float, cy: Float, r: Float, color: Color) {
    val p = Path().apply {
        moveTo(cx, cy - r)
        quadraticBezierTo(cx, cy, cx + r, cy)
        quadraticBezierTo(cx, cy, cx, cy + r)
        quadraticBezierTo(cx, cy, cx - r, cy)
        quadraticBezierTo(cx, cy, cx, cy - r)
        close()
    }
    drawPath(p, color)
}

@Composable
internal fun NoteThemeCanvas(
    themeId: String,
    modifier: Modifier = Modifier,
    isMiniPreview: Boolean = false,
    animated: Boolean = false
) {
    val defaultBg = MaterialTheme.colorScheme.background
    val defaultSurface = MaterialTheme.colorScheme.surface
    val accentCol = MaterialTheme.colorScheme.primary
    val phase = if (animated) {
        val transition = rememberInfiniteTransition(label = "theme-anim-$themeId")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "theme-phase"
        )
        value
    } else {
        0f
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width.coerceAtLeast(1f)
        val h = size.height.coerceAtLeast(1f)
        val s = (minOf(w, h) / 360f).coerceIn(0.22f, 1.5f)
        val twoPi = (2.0 * PI).toFloat()
        val wave = if (animated) sin(phase * twoPi) else 0f
        val wave2 = if (animated) cos(phase * twoPi) else 1f
        val fastWave = if (animated) sin(phase * twoPi * 2f) else 0f

        when (themeId) {
            "dot_bamboo" -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFF8F4E8), Color(0xFFF1EBDA))))
                val step = (24f * s).coerceAtLeast(8f)
                val dotR = (1.15f * s).coerceAtLeast(0.55f)
                val dotColor = Color(0xFF98A086).copy(alpha = if (isMiniPreview) 0.20f else 0.34f)
                if (!isMiniPreview || w > 120f) {
                    var gx = step
                    while (gx < w - step * 0.4f) {
                        var gy = step
                        while (gy < h - step * 0.4f) {
                            drawCircle(dotColor, dotR, Offset(gx, gy))
                            gy += step
                        }
                        gx += step
                    }
                }
                drawLine(
                    color = Color(0xFFD59B8C).copy(alpha = 0.28f),
                    start = Offset(w * 0.085f, 0f),
                    end = Offset(w * 0.085f, h),
                    strokeWidth = (1.3f * s).coerceAtLeast(0.6f)
                )
                val sunR = maxOf(w * (0.44f + 0.08f * wave), 1f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFF3BA).copy(alpha = 0.72f + 0.14f * fastWave), Color.Transparent),
                        center = Offset(w * 0.85f, h * 0.14f),
                        radius = sunR
                    ),
                    radius = sunR,
                    center = Offset(w * 0.85f, h * 0.14f)
                )
                if (animated) {
                    listOf(
                        0.20f to 0.24f, 0.48f to 0.16f, 0.72f to 0.34f,
                        0.34f to 0.56f, 0.62f to 0.68f, 0.80f to 0.82f
                    ).forEachIndexed { idx, (mx, my) ->
                        val t = (my - phase * 1f + idx * 0.17f + 2f) % 1f
                        val dx = sin((phase * 2f + idx * 0.35f) * twoPi) * 20f * s
                        val alpha = 0.58f * sin(t * PI.toFloat()).coerceAtLeast(0f)
                        drawCircle(Color(0xFFE7B650).copy(alpha = alpha), (3.4f * s).coerceAtLeast(1.1f), Offset(w * mx + dx, h * t))
                    }
                }
                fun drawBambooStalk(bx: Float, topY: Float, tilt: Float, alpha: Float) {
                    val swayX = wave * 13f * s
                    val sw = (5.5f * s).coerceAtLeast(1.8f)
                    val segCount = 5
                    val segH = (h - topY) / segCount
                    for (seg in 0 until segCount) {
                        val y1 = h - seg * segH
                        val y0 = h - (seg + 1) * segH
                        val x1 = bx + tilt * seg * 4f * s + swayX * (seg / segCount.toFloat())
                        val x0 = bx + tilt * (seg + 1) * 4f * s + swayX * ((seg + 1) / segCount.toFloat())
                        drawLine(Color(0xFF4B7D52).copy(alpha = alpha), Offset(x1, y1), Offset(x0, y0), sw, StrokeCap.Round)
                        drawLine(Color(0xFF2E5434).copy(alpha = alpha * 0.9f), Offset(x0 - sw * 0.7f, y0), Offset(x0 + sw * 0.7f, y0), (1.8f * s).coerceAtLeast(0.7f), StrokeCap.Round)
                        if (seg >= 2) {
                            listOf(-1f, 1f).forEach { dir ->
                                val lLen = (26f * s).coerceAtLeast(7f)
                                rotate(dir * 38f + wave * 10f, Offset(x0, y0)) {
                                    val leaf = Path().apply {
                                        moveTo(x0, y0)
                                        quadraticBezierTo(x0 + dir * lLen * 0.5f, y0 - lLen * 0.45f, x0 + dir * lLen, y0 - lLen * 0.1f)
                                        quadraticBezierTo(x0 + dir * lLen * 0.5f, y0 + lLen * 0.15f, x0, y0)
                                        close()
                                    }
                                    drawPath(leaf, Color(0xFF5D9462).copy(alpha = alpha * 0.85f))
                                }
                            }
                        }
                    }
                }
                drawBambooStalk(w * 0.89f, h * 0.46f, -1f, 0.42f)
                drawBambooStalk(w * 0.94f, h * 0.58f, -0.5f, 0.30f)
            }
            "matcha_grid" -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFE9F0E3), Color(0xFFDCE7D4))))
                val cell = (26f * s).coerceAtLeast(9f)
                val gridC = Color(0xFF6F8B6A).copy(alpha = 0.14f)
                var x = cell
                while (x < w) {
                    drawLine(gridC, Offset(x, 0f), Offset(x, h), 0.9f)
                    x += cell
                }
                var y = cell
                while (y < h) {
                    drawLine(gridC, Offset(0f, y), Offset(w, y), 0.9f)
                    y += cell
                }
                val cupX = w * 0.82f
                val cupY = h * 0.84f
                val cupW = 54f * s
                val cupH = 34f * s
                val cup = Path().apply {
                    moveTo(cupX - cupW * 0.5f, cupY)
                    cubicTo(cupX - cupW * 0.45f, cupY + cupH, cupX + cupW * 0.45f, cupY + cupH, cupX + cupW * 0.5f, cupY)
                    close()
                }
                drawPath(cup, Color(0xFF4F7550).copy(alpha = 0.30f))
                drawOval(
                    color = Color(0xFF729E6B).copy(alpha = 0.38f + 0.08f * fastWave),
                    topLeft = Offset(cupX - cupW * 0.48f, cupY - 5f * s),
                    size = Size(cupW * 0.96f, 10f * s)
                )
                for (i in 0..3) {
                    val sx = cupX + (i - 1.5f) * 12f * s
                    val prog = if (animated) ((phase * 2f + i * 0.25f) % 1f) else 0.3f
                    val drift = if (animated) sin((phase * 2f + i * 0.35f) * twoPi) * 11f * s else 0f
                    val lift = prog * 36f * s
                    val steamAlpha = if (animated) (0.48f * sin(prog * PI.toFloat())).coerceAtLeast(0.06f) else 0.25f
                    val steam = Path().apply {
                        moveTo(sx, cupY - 10f * s - lift * 0.4f)
                        quadraticBezierTo(sx - 10f * s + drift, cupY - 30f * s - lift * 0.7f, sx + drift * 0.8f, cupY - 54f * s - lift)
                    }
                    drawPath(steam, Color(0xFF4D7B4E).copy(alpha = steamAlpha), style = Stroke((2.6f * s).coerceAtLeast(0.9f), cap = StrokeCap.Round))
                }
                if (animated) {
                    listOf(0.22f to 0.22f, 0.65f to 0.34f, 0.38f to 0.68f).forEachIndexed { idx, (lx, ly) ->
                        val driftT = (ly + phase * 1f + idx * 0.3f) % 1f
                        val sway = sin((phase * 2f + idx) * twoPi) * 16f * s
                        rotate(25f + wave * 28f + idx * 30f, Offset(w * lx + sway, h * driftT)) {
                            val leaf = Path().apply {
                                val cx = w * lx + sway
                                val cy = h * driftT
                                moveTo(cx - 8f * s, cy)
                                cubicTo(cx - 2f * s, cy - 8f * s, cx + 8f * s, cy - 4f * s, cx + 7f * s, cy + 4f * s)
                                cubicTo(cx, cy + 7f * s, cx - 7f * s, cy + 3f * s, cx - 8f * s, cy)
                                close()
                            }
                            drawPath(leaf, Color(0xFF4B8449).copy(alpha = 0.28f * sin(driftT * PI.toFloat()).coerceAtLeast(0.05f)))
                        }
                    }
                }
            }
            "amber_evening" -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF281C14), Color(0xFF1E150F), Color(0xFF140D09))))
                val lampCenter = Offset(w * 0.80f, h * 0.18f)
                val glowRadius = maxOf(w * (0.64f + 0.12f * wave), 1f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFB86B).copy(alpha = 0.36f + 0.12f * fastWave), Color(0xFFE07A38).copy(alpha = 0.14f), Color.Transparent),
                        center = lampCenter,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = lampCenter
                )
                val shade = Path().apply {
                    moveTo(w * 0.74f, h * 0.12f)
                    lineTo(w * 0.86f, h * 0.12f)
                    lineTo(w * 0.90f, h * 0.21f)
                    lineTo(w * 0.70f, h * 0.21f)
                    close()
                }
                drawPath(shade, Color(0xFF3E291A).copy(alpha = 0.82f))
                drawLine(Color(0xFFF6C284).copy(alpha = 0.70f), Offset(w * 0.71f, h * 0.21f), Offset(w * 0.89f, h * 0.21f), (2.2f * s).coerceAtLeast(0.8f), StrokeCap.Round)

                val hill = Path().apply {
                    moveTo(0f, h * 0.84f)
                    cubicTo(w * 0.28f, h * 0.76f, w * 0.65f, h * 0.88f, w, h * 0.80f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(hill, Color(0xFF120B07).copy(alpha = 0.75f))

                val fireflies = listOf(
                    0.16f to 0.74f, 0.32f to 0.80f, 0.56f to 0.72f,
                    0.76f to 0.79f, 0.44f to 0.54f, 0.22f to 0.38f,
                    0.68f to 0.46f, 0.36f to 0.24f, 0.84f to 0.60f
                )
                fireflies.forEachIndexed { idx, (fx, fy) ->
                    val dx = if (animated) sin((phase + idx * 0.21f) * twoPi) * 26f * s else 0f
                    val dy = if (animated) cos((phase * 2f + idx * 0.17f) * twoPi) * 18f * s else 0f
                    val pulse = if (animated) 0.20f + 0.80f * ((sin((phase * 3f + idx * 0.4f) * twoPi) + 1f) * 0.5f) else 0.75f
                    val pt = Offset(w * fx + dx, h * fy + dy)
                    drawCircle(Color(0xFFFFC97A).copy(alpha = 0.38f * pulse), (18f * s).coerceAtLeast(3.5f), pt)
                    drawCircle(Color(0xFFFFE5AE).copy(alpha = 0.95f * pulse), (3.4f * s).coerceAtLeast(1.0f), pt)
                }
            }
            "forest_lake" -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF0C2320), Color(0xFF123630), Color(0xFF0A1D1B))))
                val horizon = h * 0.68f
                val moonPos = Offset(w * 0.22f, h * 0.20f)
                val moonGlowR = maxOf(w * (0.36f + 0.07f * wave), 1f)
                drawCircle(
                    Brush.radialGradient(listOf(Color(0xFFA7F3D0).copy(alpha = 0.30f + 0.08f * fastWave), Color.Transparent), center = moonPos, radius = moonGlowR),
                    radius = moonGlowR,
                    center = moonPos
                )
                drawCircle(Color(0xFFE6FFF6).copy(alpha = 0.92f), (18f * s).coerceAtLeast(4f), moonPos)

                listOf(0.45f to 0.14f, 0.64f to 0.10f, 0.78f to 0.22f, 0.54f to 0.28f, 0.88f to 0.14f, 0.38f to 0.36f).forEachIndexed { idx, (sx, sy) ->
                    val tw = if (animated) 0.18f + 0.82f * ((sin((phase * 3f + idx * 0.4f) * twoPi) + 1f) * 0.5f) else 0.65f
                    drawCircle(Color(0xFFE6FFF6).copy(alpha = tw), (2.2f * s).coerceAtLeast(0.7f), Offset(w * sx, h * sy))
                }

                val shore = Path().apply {
                    moveTo(0f, horizon)
                    quadraticBezierTo(w * 0.36f, horizon - h * 0.05f, w * 0.70f, horizon - h * 0.01f)
                    lineTo(w, horizon - h * 0.03f)
                    lineTo(w, horizon)
                    close()
                }
                drawPath(shore, Color(0xFF0A2420).copy(alpha = 0.85f))

                val spruces = listOf(0.08f to 42f, 0.15f to 56f, 0.23f to 36f, 0.72f to 48f, 0.81f to 62f, 0.90f to 44f)
                spruces.forEach { (px, heightDp) ->
                    val th = heightDp * s
                    val tw = th * 0.42f
                    val tree = Path().apply {
                        moveTo(w * px, horizon - th)
                        lineTo(w * px + tw, horizon)
                        lineTo(w * px - tw, horizon)
                        close()
                    }
                    drawPath(tree, Color(0xFF061B18).copy(alpha = 0.90f))
                }

                drawRect(
                    Brush.verticalGradient(listOf(Color(0xFF0D2B27), Color(0xFF071816))),
                    topLeft = Offset(0f, horizon),
                    size = Size(w, h - horizon)
                )
                for (i in 0..6) {
                    val ry = horizon + (i + 1) * h * 0.038f
                    val sway = if (animated) sin((phase * 2f + i * 0.22f) * twoPi) * 20f * s else 0f
                    val hw = w * (0.14f - i * 0.014f)
                    drawLine(
                        color = Color(0xFF99F6E4).copy(alpha = 0.38f - i * 0.03f),
                        start = Offset(w * 0.22f - hw + sway, ry),
                        end = Offset(w * 0.22f + hw + sway, ry),
                        strokeWidth = (2.6f * s).coerceAtLeast(0.9f),
                        cap = StrokeCap.Round
                    )
                }
                val mistX = wave * 28f * s
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xFFA7F3D0).copy(alpha = 0.18f + 0.06f * fastWave), Color.Transparent),
                        startY = horizon - h * 0.06f,
                        endY = horizon + h * 0.09f
                    ),
                    topLeft = Offset(mistX, horizon - h * 0.06f),
                    size = Size(w, h * 0.15f)
                )
            }
            "nordic_aurora" -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF071124), Color(0xFF0C1F3B), Color(0xFF071626))))
                val shift1 = wave * w * 0.14f
                val shift2 = wave2 * w * 0.12f
                val lift1 = fastWave * h * 0.05f
                val ribbon1 = Path().apply {
                    moveTo(-w * 0.15f, h * 0.28f + lift1)
                    cubicTo(w * 0.25f + shift1, h * 0.08f - lift1, w * 0.55f - shift1, h * 0.40f + lift1, w * 1.15f, h * 0.18f)
                    lineTo(w * 1.15f, h * 0.38f)
                    cubicTo(w * 0.55f - shift1, h * 0.58f + lift1, w * 0.25f + shift1, h * 0.24f - lift1, -w * 0.15f, h * 0.46f + lift1)
                    close()
                }
                drawPath(
                    ribbon1,
                    Brush.horizontalGradient(listOf(Color(0xFF2DD4BF).copy(alpha = 0.22f), Color(0xFF34D399).copy(alpha = 0.38f + 0.10f * fastWave), Color(0xFF818CF8).copy(alpha = 0.24f)))
                )
                val ribbon2 = Path().apply {
                    moveTo(-w * 0.15f, h * 0.16f - lift1)
                    cubicTo(w * 0.35f + shift2, h * 0.30f + lift1, w * 0.70f - shift2, h * 0.04f - lift1, w * 1.15f, h * 0.24f)
                    lineTo(w * 1.15f, h * 0.38f)
                    cubicTo(w * 0.70f - shift2, h * 0.18f - lift1, w * 0.35f + shift2, h * 0.42f + lift1, -w * 0.15f, h * 0.30f - lift1)
                    close()
                }
                drawPath(
                    ribbon2,
                    Brush.horizontalGradient(listOf(Color(0xFF38BDF8).copy(alpha = 0.20f), Color(0xFF6EE7B7).copy(alpha = 0.30f), Color(0xFFC084FC).copy(alpha = 0.22f)))
                )
                val stars = listOf(0.14f to 0.10f, 0.32f to 0.08f, 0.52f to 0.14f, 0.76f to 0.10f, 0.88f to 0.20f, 0.22f to 0.40f, 0.64f to 0.44f, 0.42f to 0.26f)
                stars.forEachIndexed { idx, (sx, sy) ->
                    val tw = if (animated) 0.18f + 0.82f * ((sin((phase * 3f + idx * 0.45f) * twoPi) + 1f) * 0.5f) else 0.75f
                    drawCircle(Color.White.copy(alpha = tw), (2.3f * s).coerceAtLeast(0.8f), Offset(w * sx, h * sy))
                }
                val ridge = Path().apply {
                    moveTo(0f, h * 0.84f)
                    lineTo(w * 0.30f, h * 0.74f)
                    lineTo(w * 0.60f, h * 0.82f)
                    lineTo(w * 0.88f, h * 0.72f)
                    lineTo(w, h * 0.78f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(ridge, Color(0xFF06101E).copy(alpha = 0.90f))
            }
            "rainy_window" -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF141E28), Color(0xFF1B2836), Color(0xFF101820))))
                val bokeh = listOf(
                    Triple(0.18f, 0.72f, Color(0xFFFFC078)),
                    Triple(0.32f, 0.78f, Color(0xFF93C5FD)),
                    Triple(0.68f, 0.66f, Color(0xFFFDE68A)),
                    Triple(0.82f, 0.75f, Color(0xFFFCA5A5)),
                    Triple(0.52f, 0.84f, Color(0xFFBAE6FD))
                )
                bokeh.forEachIndexed { idx, (bx, by, col) ->
                    val pulse = if (animated) 0.65f + 0.35f * sin((phase * 2f + idx * 0.2f) * twoPi) else 1f
                    drawCircle(
                        col.copy(alpha = 0.18f * pulse),
                        (28f * s).coerceAtLeast(6f),
                        Offset(w * bx, h * by)
                    )
                }
                val drops = listOf(
                    0.08f to 0.10f, 0.20f to 0.38f, 0.32f to 0.18f, 0.44f to 0.54f,
                    0.56f to 0.26f, 0.68f to 0.44f, 0.80f to 0.16f, 0.90f to 0.60f,
                    0.15f to 0.72f, 0.40f to 0.82f, 0.64f to 0.78f, 0.86f to 0.88f
                )
                drops.forEachIndexed { idx, (dx, dy) ->
                    val t = if (animated) (dy + phase * 3f + idx * 0.13f) % 1f else dy
                    val x = w * dx - t * 28f * s
                    val y = h * t
                    val len = (32f + (idx % 3) * 14f) * s
                    val alpha = if (animated) (0.52f * sin(t * PI.toFloat())).coerceAtLeast(0.06f) else 0.28f
                    drawLine(
                        Color(0xFFC8E1FA).copy(alpha = alpha),
                        Offset(x, y),
                        Offset(x - 7f * s, y + len),
                        (2.1f * s).coerceAtLeast(0.7f),
                        StrokeCap.Round
                    )
                }
            }
            "teal_monstera" -> {
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0A8596), Color(0xFF0B6F82), Color(0xFF074959))
                    )
                )
                val horizonY = h * 0.70f
                val glowRadius = maxOf(w * (0.46f + 0.08f * wave), 1f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFE6A8).copy(alpha = 0.30f + 0.08f * fastWave), Color(0xFF5EEAD4).copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(w * 0.72f, horizonY - h * 0.04f),
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = Offset(w * 0.72f, horizonY - h * 0.04f)
                )
                drawRect(
                    color = Color(0xFF053847).copy(alpha = 0.48f),
                    topLeft = Offset(0f, horizonY),
                    size = Size(w, h - horizonY)
                )
                val island = Path().apply {
                    moveTo(w * 0.35f, horizonY)
                    quadraticBezierTo(w * 0.52f, horizonY - h * 0.06f, w * 0.72f, horizonY)
                    close()
                }
                drawPath(island, Color(0xFF064855).copy(alpha = 0.65f))
                for (i in 0..4) {
                    val ry = horizonY + (i + 1) * h * 0.045f
                    val half = w * (0.14f - i * 0.02f)
                    val shimmerX = if (animated) sin((phase * 2f + i * 0.25f) * twoPi) * 16f * s else 0f
                    drawLine(
                        color = Color(0xFFA7F7F0).copy(alpha = 0.34f - i * 0.03f),
                        start = Offset(w * 0.72f - half + shimmerX, ry),
                        end = Offset(w * 0.72f + half + shimmerX, ry),
                        strokeWidth = (2.6f * s).coerceAtLeast(0.8f),
                        cap = StrokeCap.Round
                    )
                }
                val monstera = Path().apply {
                    moveTo(0f, h * 0.04f)
                    cubicTo(w * 0.22f, 0f, w * 0.30f, h * 0.18f, w * 0.16f, h * 0.26f)
                    lineTo(w * 0.07f, h * 0.20f)
                    lineTo(w * 0.12f, h * 0.29f)
                    cubicTo(w * 0.06f, h * 0.33f, 0f, h * 0.30f, 0f, h * 0.22f)
                    close()
                }
                drawPath(monstera, Color(0xFF14B8A6).copy(alpha = 0.28f))
                drawLine(
                    color = Color(0xFF99F6E4).copy(alpha = 0.22f),
                    start = Offset(0f, h * 0.08f),
                    end = Offset(w * 0.17f, h * 0.21f),
                    strokeWidth = (1.8f * s).coerceAtLeast(0.7f)
                )
                drawPalmTree(
                    baseX = w * 0.90f,
                    baseY = h,
                    topX = w * 0.73f + wave * 9f * s,
                    topY = h * 0.53f,
                    ctrlX = w * 0.87f,
                    ctrlY = h * 0.74f,
                    frondLen = minOf(w, h) * 0.28f,
                    trunkColor = Color(0xFF042D38).copy(alpha = 0.72f),
                    leafDark = Color(0xFF033742).copy(alpha = 0.68f),
                    leafLight = Color(0xFF0C5A66).copy(alpha = 0.65f),
                    scale = s,
                    swayDeg = wave * 8.5f
                )
                drawPalmTree(
                    baseX = w * 0.08f,
                    baseY = h,
                    topX = w * 0.17f + wave2 * 6f * s,
                    topY = h * 0.72f,
                    ctrlX = w * 0.10f,
                    ctrlY = h * 0.86f,
                    frondLen = minOf(w, h) * 0.17f,
                    trunkColor = Color(0xFF042D38).copy(alpha = 0.55f),
                    leafDark = Color(0xFF033742).copy(alpha = 0.50f),
                    leafLight = Color(0xFF0E6370).copy(alpha = 0.48f),
                    scale = s * 0.72f,
                    swayDeg = -wave * 6.5f
                )
                // Всплывающие прозрачные пузырьки
                val bubbles = listOf(0.83f to 0.18f to 14f, 0.25f to 0.44f to 8f, 0.56f to 0.28f to 10f)
                bubbles.forEachIndexed { idx, (posPair, radDp) ->
                    val bxBase = posPair.first
                    val byBase = posPair.second
                    val progress = if (animated) (byBase - phase * 1f + idx * 0.25f + 2f) % 1f else byBase
                    val bx = w * bxBase + (if (animated) sin((phase * 2f + idx) * twoPi) * 14f * s else 0f)
                    val by = h * progress
                    val br = (radDp * s).coerceAtLeast(2.5f)
                    val bAlpha = if (animated) sin(progress * PI.toFloat()).coerceAtLeast(0.10f) else 1f
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.24f * bAlpha), Color(0xFF99F6E4).copy(alpha = 0.08f * bAlpha)),
                            center = Offset(bx - br * 0.3f, by - br * 0.3f),
                            radius = br
                        ),
                        radius = br,
                        center = Offset(bx, by)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.42f * bAlpha),
                        radius = br,
                        center = Offset(bx, by),
                        style = Stroke(width = (1.3f * s).coerceAtLeast(0.6f))
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.65f * bAlpha),
                        radius = (br * 0.24f).coerceAtLeast(0.9f),
                        center = Offset(bx - br * 0.34f, by - br * 0.34f)
                    )
                }
            }
            "paper_petals" -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFFDF7F4), Color(0xFFF5E6E1))))
                val lineStep = (26f * s).coerceAtLeast(6f)
                var ly = lineStep
                while (ly < h) {
                    drawLine(
                        color = Color(0xFFCFA5A0).copy(alpha = 0.13f),
                        start = Offset(w * 0.07f, ly),
                        end = Offset(w * 0.93f, ly),
                        strokeWidth = 0.9f
                    )
                    ly += lineStep
                }
                val pad = if (isMiniPreview) w * 0.06f else w * 0.04f
                drawRoundRect(
                    color = Color(0xFFD99B97).copy(alpha = 0.55f),
                    topLeft = Offset(pad, pad),
                    size = Size(w - pad * 2f, h - pad * 2f),
                    cornerRadius = CornerRadius(14f * s),
                    style = Stroke(width = (1.8f * s).coerceAtLeast(0.8f))
                )
                val branch1 = Path().apply {
                    moveTo(w, h * 0.12f)
                    cubicTo(w * 0.84f, h * 0.16f, w * 0.72f, h * 0.13f, w * 0.58f, h * 0.22f)
                }
                drawPath(branch1, Color(0xFF5E3F3B).copy(alpha = 0.78f), style = Stroke(width = (4.5f * s).coerceAtLeast(1.4f), cap = StrokeCap.Round))
                val twig1 = Path().apply {
                    moveTo(w * 0.79f, h * 0.145f)
                    quadraticBezierTo(w * 0.74f, h * 0.08f, w * 0.67f, h * 0.07f)
                }
                drawPath(twig1, Color(0xFF5E3F3B).copy(alpha = 0.72f), style = Stroke(width = (2.5f * s).coerceAtLeast(0.9f), cap = StrokeCap.Round))
                val branch2 = Path().apply {
                    moveTo(0f, h * 0.86f)
                    cubicTo(w * 0.15f, h * 0.82f, w * 0.26f, h * 0.87f, w * 0.38f, h * 0.78f)
                }
                drawPath(branch2, Color(0xFF5E3F3B).copy(alpha = 0.72f), style = Stroke(width = (4f * s).coerceAtLeast(1.2f), cap = StrokeCap.Round))

                drawSakuraBlossom(w * 0.68f, h * 0.08f, 16f * s, Color(0xFFF6B9C0).copy(alpha = 0.90f), Color(0xFFD96473))
                drawSakuraBlossom(w * 0.84f, h * 0.16f, 22f * s, Color(0xFFFAD0D5).copy(alpha = 0.92f), Color(0xFFD96473))
                drawSakuraBlossom(w * 0.60f, h * 0.22f, 18f * s, Color(0xFFF3A7B1).copy(alpha = 0.88f), Color(0xFFC94B5C))
                drawSakuraBlossom(w * 0.92f, h * 0.11f, 14f * s, Color(0xFFF7C3C9).copy(alpha = 0.85f), Color(0xFFD96473))
                drawSakuraBlossom(w * 0.18f, h * 0.84f, 19f * s, Color(0xFFF7C3C9).copy(alpha = 0.88f), Color(0xFFD96473))
                drawSakuraBlossom(w * 0.36f, h * 0.79f, 15f * s, Color(0xFFF3A7B1).copy(alpha = 0.85f), Color(0xFFC94B5C))

                val drifting = listOf(
                    Triple(0.16f, 0.10f, 35f),
                    Triple(0.32f, 0.22f, -20f),
                    Triple(0.48f, 0.45f, 55f),
                    Triple(0.76f, 0.30f, -40f),
                    Triple(0.62f, 0.72f, 18f),
                    Triple(0.84f, 0.56f, -15f),
                    Triple(0.24f, 0.64f, 44f),
                    Triple(0.54f, 0.88f, -30f)
                )
                drifting.forEachIndexed { idx, (px, py, rot) ->
                    val driftT = if (animated) (py + phase * 1f + idx * 0.12f) % 1f else py
                    val swayX = if (animated) sin((phase * 2f + idx * 0.25f) * twoPi) * 24f * s else 0f
                    val drawX = w * px + swayX
                    val drawY = h * driftT
                    val pAlpha = if (animated) (0.76f * sin(driftT * PI.toFloat())).coerceAtLeast(0.08f) else 0.58f
                    rotate(rot + wave * 40f, Offset(drawX, drawY)) {
                        val p = Path().apply {
                            moveTo(drawX, drawY - 8f * s)
                            cubicTo(drawX + 7f * s, drawY - 2f * s, drawX + 6f * s, drawY + 7f * s, drawX, drawY + 8f * s)
                            cubicTo(drawX - 6f * s, drawY + 7f * s, drawX - 7f * s, drawY - 2f * s, drawX, drawY - 8f * s)
                        }
                        drawPath(p, Color(0xFFE67887).copy(alpha = pAlpha))
                    }
                }
            }
            "clean_mist" -> {
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color(0xFFD8E7F5), Color(0xFFEAF1F7), Color(0xFFF6F9FC))
                    )
                )
                val sunCenter = Offset(w * 0.76f, h * 0.24f)
                val sunHaloR = maxOf(w * (0.38f + 0.09f * wave), 1f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFF5D6).copy(alpha = 0.92f), Color(0xFFFFE7C2).copy(alpha = 0.40f), Color.Transparent),
                        center = sunCenter,
                        radius = sunHaloR
                    ),
                    radius = sunHaloR,
                    center = sunCenter
                )
                drawCircle(Color(0xFFFFF9E6), (22f * s).coerceAtLeast(4f), sunCenter)

                val m1 = Path().apply {
                    moveTo(0f, h * 0.76f)
                    lineTo(w * 0.22f, h * 0.58f)
                    lineTo(w * 0.44f, h * 0.69f)
                    lineTo(w * 0.72f, h * 0.52f)
                    lineTo(w, h * 0.66f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(m1, Color(0xFF9EB5CA).copy(alpha = 0.45f))

                val m2 = Path().apply {
                    moveTo(0f, h * 0.82f)
                    lineTo(w * 0.34f, h * 0.63f)
                    lineTo(w * 0.56f, h * 0.74f)
                    lineTo(w * 0.86f, h * 0.58f)
                    lineTo(w, h * 0.70f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(m2, Color(0xFF6B89A6).copy(alpha = 0.55f))
                val snow = Path().apply {
                    moveTo(w * 0.34f, h * 0.63f)
                    lineTo(w * 0.27f, h * 0.675f)
                    lineTo(w * 0.33f, h * 0.665f)
                    lineTo(w * 0.37f, h * 0.685f)
                    lineTo(w * 0.41f, h * 0.665f)
                    close()
                }
                drawPath(snow, Color.White.copy(alpha = 0.72f))

                val mistShift = wave * 24f * s
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.62f + 0.16f * fastWave), Color.Transparent),
                        startY = h * 0.66f,
                        endY = h * 0.86f
                    ),
                    topLeft = Offset(mistShift, h * 0.66f),
                    size = Size(w, h * 0.20f)
                )

                val m3 = Path().apply {
                    moveTo(0f, h * 0.88f)
                    quadraticBezierTo(w * 0.45f, h * 0.80f, w, h * 0.89f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(m3, Color(0xFF3E5972).copy(alpha = 0.70f))

                val pines = listOf(0.08f to 0.86f, 0.15f to 0.85f, 0.23f to 0.84f, 0.82f to 0.86f, 0.90f to 0.87f)
                pines.forEachIndexed { idx, (px, py) ->
                    val tw = (12f + (idx % 2) * 4f) * s
                    val th = (28f + (idx % 3) * 8f) * s
                    val tree = Path().apply {
                        moveTo(w * px, h * py - th)
                        lineTo(w * px + tw, h * py)
                        lineTo(w * px - tw, h * py)
                        close()
                    }
                    drawPath(tree, Color(0xFF2D465C).copy(alpha = 0.78f))
                }
                listOf(0.50f to 0.30f, 0.59f to 0.35f, 0.42f to 0.26f).forEachIndexed { idx, (bx, by) ->
                    val flap = if (animated) sin((phase * 5f + idx) * twoPi) * 4.5f * s else 0f
                    val gx = w * bx + wave * 20f * s
                    val gy = h * by + flap * 0.45f + wave2 * 4f * s
                    val bird = Path().apply {
                        moveTo(gx - 8f * s, gy + flap)
                        quadraticBezierTo(gx - 3f * s, gy - 5f * s, gx, gy)
                        quadraticBezierTo(gx + 3f * s, gy - 5f * s, gx + 8f * s, gy + flap)
                    }
                    drawPath(bird, Color(0xFF47637E).copy(alpha = 0.72f), style = Stroke((1.6f * s).coerceAtLeast(0.7f), cap = StrokeCap.Round))
                }
            }
            "deep_ocean" -> {
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color(0xFF071B4B), Color(0xFF0C2E78), Color(0xFF051336))
                    )
                )
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFF9333EA).copy(alpha = 0.30f + 0.08f * wave), Color(0xFF3B82F6).copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(w * 0.22f, h * 0.30f),
                        radius = maxOf(w * 0.62f, 1f)
                    ),
                    radius = maxOf(w * 0.62f, 1f),
                    center = Offset(w * 0.22f, h * 0.30f)
                )
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFF06B6D4).copy(alpha = 0.22f + 0.08f * wave2), Color.Transparent),
                        center = Offset(w * 0.78f, h * 0.76f),
                        radius = maxOf(w * 0.48f, 1f)
                    ),
                    radius = maxOf(w * 0.48f, 1f),
                    center = Offset(w * 0.78f, h * 0.76f)
                )
                val constPts = listOf(
                    Offset(w * 0.14f, h * 0.22f),
                    Offset(w * 0.24f, h * 0.16f),
                    Offset(w * 0.33f, h * 0.24f),
                    Offset(w * 0.28f, h * 0.35f)
                )
                for (i in 0 until constPts.lastIndex) {
                    drawLine(
                        color = Color(0xFF93C5FD).copy(alpha = 0.32f),
                        start = constPts[i],
                        end = constPts[i + 1],
                        strokeWidth = (1f * s).coerceAtLeast(0.5f)
                    )
                }
                constPts.forEach { pt ->
                    drawCircle(Color.White.copy(alpha = 0.82f), (2.2f * s).coerceAtLeast(0.8f), pt)
                }
                val px = w * 0.80f
                val py = h * 0.20f + wave * 9f * s
                val pr = (30f * s).coerceAtLeast(7f)
                rotate(-22f + wave * 4f, Offset(px, py)) {
                    drawArc(
                        color = Color(0xFFE2D4FF).copy(alpha = 0.48f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(px - pr * 1.95f, py - pr * 0.48f),
                        size = Size(pr * 3.9f, pr * 0.96f),
                        style = Stroke(width = (4.5f * s).coerceAtLeast(1.2f))
                    )
                }
                drawCircle(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFF6C69E), Color(0xFFD9876C), Color(0xFF5D3A6A)),
                        start = Offset(px - pr, py - pr),
                        end = Offset(px + pr, py + pr)
                    ),
                    radius = pr,
                    center = Offset(px, py)
                )
                rotate(-22f + wave * 4f, Offset(px, py)) {
                    drawArc(
                        color = Color(0xFFF5E6CA).copy(alpha = 0.82f),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(px - pr * 1.95f, py - pr * 0.48f),
                        size = Size(pr * 3.9f, pr * 0.96f),
                        style = Stroke(width = (4.5f * s).coerceAtLeast(1.2f))
                    )
                    drawArc(
                        color = Color(0xFF93C5FD).copy(alpha = 0.50f),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(px - pr * 1.65f, py - pr * 0.36f),
                        size = Size(pr * 3.3f, pr * 0.72f),
                        style = Stroke(width = (1.6f * s).coerceAtLeast(0.6f))
                    )
                }
                val meteorShift = if (animated) ((phase * 2f) % 1f) else 0.35f
                val mx = w * (0.76f - meteorShift * 0.60f)
                val my = h * (0.58f + meteorShift * 0.24f)
                val meteorAlpha = if (animated) sin(meteorShift * PI.toFloat()).coerceAtLeast(0.05f) else 0.85f
                drawLine(
                    brush = Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.92f * meteorAlpha), Color.Transparent),
                        start = Offset(mx, my),
                        end = Offset(mx + w * 0.18f, my - h * 0.07f)
                    ),
                    start = Offset(mx, my),
                    end = Offset(mx + w * 0.18f, my - h * 0.07f),
                    strokeWidth = (2.6f * s).coerceAtLeast(0.9f),
                    cap = StrokeCap.Round
                )
                drawFourPointStar(mx, my, (7.5f * s).coerceAtLeast(2.0f), Color.White.copy(alpha = meteorAlpha))
                drawFourPointStar(w * 0.18f, h * 0.62f, ((6.5f + 3.5f * fastWave) * s).coerceAtLeast(2f), Color(0xFFDBEAFE))
                drawFourPointStar(w * 0.84f, h * 0.82f, ((5.5f + 3.0f * wave2) * s).coerceAtLeast(1.8f), Color(0xFFBAE6FD))
                listOf(
                    0.50f to 0.14f, 0.10f to 0.46f, 0.68f to 0.48f,
                    0.35f to 0.86f, 0.88f to 0.56f, 0.58f to 0.90f, 0.44f to 0.38f
                ).forEachIndexed { idx, (sx, sy) ->
                    val tw = if (animated) 0.15f + 0.85f * ((sin((phase * 3f + idx * 0.5f) * twoPi) + 1f) * 0.5f) else 0.55f
                    drawCircle(Color.White.copy(alpha = tw), (2.2f * s).coerceAtLeast(0.7f), Offset(w * sx, h * sy))
                }
            }
            "dark_cube" -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF151922), Color(0xFF0E1117))))
                val gridColor = Color(0xFF60A5FA).copy(alpha = 0.07f + 0.03f * wave)
                val step = (44f * s).coerceAtLeast(10f)
                var gx = 0f
                while (gx < w + h) {
                    drawLine(gridColor, Offset(gx, 0f), Offset(gx - h * 0.58f, h), 1f)
                    drawLine(gridColor, Offset(gx - h * 0.58f, 0f), Offset(gx, h), 1f)
                    gx += step
                }
                fun drawIsoCube(cx: Float, cy: Float, r: Float, top: Color, left: Color, right: Color, edge: Color) {
                    val dx = r * 0.866f
                    val dy = r * 0.5f
                    val topFace = Path().apply {
                        moveTo(cx, cy - r)
                        lineTo(cx + dx, cy - dy)
                        lineTo(cx, cy)
                        lineTo(cx - dx, cy - dy)
                        close()
                    }
                    val leftFace = Path().apply {
                        moveTo(cx - dx, cy - dy)
                        lineTo(cx, cy)
                        lineTo(cx, cy + r)
                        lineTo(cx - dx, cy + dy)
                        close()
                    }
                    val rightFace = Path().apply {
                        moveTo(cx + dx, cy - dy)
                        lineTo(cx + dx, cy + dy)
                        lineTo(cx, cy + r)
                        lineTo(cx, cy)
                        close()
                    }
                    drawPath(topFace, top)
                    drawPath(leftFace, left)
                    drawPath(rightFace, right)
                    val sw = (1.5f * s).coerceAtLeast(0.7f)
                    drawPath(topFace, edge, style = Stroke(sw))
                    drawPath(leftFace, edge.copy(alpha = 0.50f), style = Stroke(sw))
                    drawPath(rightFace, edge.copy(alpha = 0.50f), style = Stroke(sw))
                }
                val floatY = wave * 14f * s
                val floatY2 = wave2 * 10f * s
                drawIsoCube(
                    w * 0.82f, h * 0.19f + floatY, 44f * s,
                    Color(0xFF2D394F), Color(0xFF1F2737), Color(0xFF171D29), Color(0xFF7FA8FF).copy(alpha = 0.62f + 0.25f * fastWave)
                )
                drawIsoCube(
                    w * 0.67f, h * 0.28f - floatY2 * 0.8f, 24f * s,
                    Color(0xFF334463), Color(0xFF242F45), Color(0xFF192131), Color(0xFF93C5FD).copy(alpha = 0.55f)
                )
                drawIsoCube(
                    w * 0.16f, h * 0.84f + floatY * 0.7f, 34f * s,
                    Color(0xFF2A3448), Color(0xFF1C2332), Color(0xFF141925), Color(0xFF7FA8FF).copy(alpha = 0.52f)
                )
                drawFourPointStar(w * 0.82f, h * 0.19f + floatY - 44f * s, ((6.5f + 3.5f * fastWave) * s).coerceAtLeast(1.8f), Color(0xFFBFDBFE).copy(alpha = 0.88f))
            }
            "sage_garden" -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF163A28), Color(0xFF1D4B34), Color(0xFF0F261A))))
                val beamR = maxOf(w * (0.66f + 0.10f * wave), 1f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFF86EFAC).copy(alpha = 0.22f + 0.08f * fastWave), Color.Transparent),
                        center = Offset(w * 0.15f, h * 0.12f),
                        radius = beamR
                    ),
                    radius = beamR,
                    center = Offset(w * 0.15f, h * 0.12f)
                )
                fun drawSageStem(sx: Float, sy: Float, ex: Float, ey: Float, cx: Float, cy: Float, flip: Float) {
                    val swayOffset = wave * 16f * s * flip
                    val stem = Path().apply {
                        moveTo(sx, sy)
                        quadraticBezierTo(cx, cy, ex + swayOffset, ey)
                    }
                    drawPath(stem, Color(0xFF8CE5B0).copy(alpha = 0.44f), style = Stroke((2.2f * s).coerceAtLeast(0.8f), cap = StrokeCap.Round))
                    for (i in 1..6) {
                        val t = i / 7f
                        val omt = 1f - t
                        val px = omt * omt * sx + 2f * omt * t * cx + t * t * (ex + swayOffset)
                        val py = omt * omt * sy + 2f * omt * t * cy + t * t * ey
                        val leafSize = (22f - i * 1.8f) * s
                        listOf(-42f, 42f).forEach { sideAngle ->
                            rotate(sideAngle * flip + 15f * flip + wave * 9f, Offset(px, py)) {
                                val leaf = Path().apply {
                                    moveTo(px, py)
                                    cubicTo(px - leafSize * 0.45f, py - leafSize * 0.4f, px - leafSize * 0.35f, py - leafSize * 1.1f, px, py - leafSize * 1.3f)
                                    cubicTo(px + leafSize * 0.35f, py - leafSize * 1.1f, px + leafSize * 0.45f, py - leafSize * 0.4f, px, py)
                                    close()
                                }
                                drawPath(leaf, Color(0xFF6EE7B7).copy(alpha = 0.28f))
                                drawLine(
                                    Color(0xFFA7F3D0).copy(alpha = 0.32f),
                                    Offset(px, py),
                                    Offset(px, py - leafSize * 1.15f),
                                    (1f * s).coerceAtLeast(0.5f)
                                )
                            }
                        }
                    }
                }
                drawSageStem(w * 0.92f, h * 0.98f, w * 0.66f, h * 0.56f, w * 0.88f, h * 0.72f, 1f)
                drawSageStem(w * 0.06f, h * 0.04f, w * 0.28f, h * 0.34f, w * 0.10f, h * 0.22f, -1f)
                if (animated) {
                    listOf(0.28f to 0.62f, 0.54f to 0.36f, 0.72f to 0.24f, 0.40f to 0.78f).forEachIndexed { idx, (mx, my) ->
                        val prog = (my - phase * 1f + idx * 0.22f + 2f) % 1f
                        val dx = sin((phase * 2f + idx * 0.4f) * twoPi) * 18f * s
                        val alpha = 0.60f * sin(prog * PI.toFloat()).coerceAtLeast(0f)
                        drawCircle(Color(0xFFD1FAE5).copy(alpha = alpha), (3.0f * s).coerceAtLeast(1.0f), Offset(w * mx + dx, h * prog))
                    }
                }
            }
            "warm_cream" -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFFDF2E9), Color(0xFFF7DFCB), Color(0xFFF2CFAF))))
                val sun = Offset(w * 0.76f, h * 0.74f)
                val sunR = maxOf(w * (0.40f + 0.09f * wave), 1f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFB27D).copy(alpha = 0.56f + 0.12f * fastWave), Color.Transparent),
                        center = sun,
                        radius = sunR
                    ),
                    radius = sunR,
                    center = sun
                )
                drawCircle(Color(0xFFFFD6A5).copy(alpha = 0.88f), (26f * s).coerceAtLeast(5f), sun)
                val hill1 = Path().apply {
                    moveTo(0f, h * 0.84f)
                    cubicTo(w * 0.30f, h * 0.74f, w * 0.62f, h * 0.88f, w, h * 0.78f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(hill1, Color(0xFFD49A7A).copy(alpha = 0.34f))
                val hill2 = Path().apply {
                    moveTo(0f, h * 0.90f)
                    cubicTo(w * 0.36f, h * 0.85f, w * 0.70f, h * 0.93f, w, h * 0.86f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(hill2, Color(0xFFB37459).copy(alpha = 0.38f))
                val stalks = listOf(0.08f to 0.74f, 0.14f to 0.70f, 0.20f to 0.76f)
                stalks.forEachIndexed { idx, (lx, topY) ->
                    val bx = w * lx
                    val sway = wave * 12f * s * (if (idx % 2 == 0) 1f else 0.8f)
                    drawLine(
                        Color(0xFF6B7D5C).copy(alpha = 0.55f),
                        Offset(bx - 4f * s, h),
                        Offset(bx + 3f * s + sway, h * topY),
                        (1.8f * s).coerceAtLeast(0.7f),
                        StrokeCap.Round
                    )
                    for (j in 0..5) {
                        val fy = h * topY + j * 8f * s
                        val fx = bx + 3f * s + sway * (1f - j / 6f) - j * 1f * s
                        drawCircle(Color(0xFF9067B8).copy(alpha = 0.58f), (3.4f * s).coerceAtLeast(1f), Offset(fx - 2.5f * s, fy))
                        drawCircle(Color(0xFFA77ED0).copy(alpha = 0.58f), (3.4f * s).coerceAtLeast(1f), Offset(fx + 2.5f * s, fy + 2f * s))
                    }
                }
                if (animated) {
                    listOf(0.38f to 0.22f, 0.64f to 0.38f, 0.82f to 0.28f, 0.46f to 0.56f).forEachIndexed { idx, (mx, my) ->
                        val prog = (my - phase * 1f + idx * 0.25f + 2f) % 1f
                        val dx = sin((phase * 2f + idx * 0.3f) * twoPi) * 16f * s
                        val alpha = 0.55f * sin(prog * PI.toFloat()).coerceAtLeast(0f)
                        drawCircle(Color(0xFFE79557).copy(alpha = alpha), (3.0f * s).coerceAtLeast(1.0f), Offset(w * mx + dx, h * prog))
                    }
                }
            }
            else -> {
                drawRect(if (isMiniPreview) defaultSurface else defaultBg)
                if (animated && !isMiniPreview) {
                    val glowR = maxOf(w * (0.55f + 0.08f * wave), 1f)
                    drawCircle(
                        Brush.radialGradient(
                            listOf(accentCol.copy(alpha = 0.10f + 0.04f * fastWave), Color.Transparent),
                            center = Offset(w * (0.78f + 0.06f * wave), h * 0.20f),
                            radius = glowR
                        ),
                        radius = glowR,
                        center = Offset(w * (0.78f + 0.06f * wave), h * 0.20f)
                    )
                    listOf(0.18f to 0.26f, 0.48f to 0.42f, 0.74f to 0.64f, 0.30f to 0.78f).forEachIndexed { idx, (mx, my) ->
                        val prog = (my - phase * 1f + idx * 0.22f + 2f) % 1f
                        val dx = sin((phase * 2f + idx * 0.4f) * twoPi) * 16f * s
                        val alpha = 0.28f * sin(prog * PI.toFloat()).coerceAtLeast(0f)
                        drawCircle(accentCol.copy(alpha = alpha), (2.8f * s).coerceAtLeast(1.0f), Offset(w * mx + dx, h * prog))
                    }
                }
            }
        }
    }
}

@Composable
internal fun NoteThemePickerBar(
    selectedId: String,
    animated: Boolean = false,
    onSelect: (String) -> Unit
) {
    val activeAccent = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 8.dp
    ) {
        Column(Modifier.padding(vertical = 10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NoteThemes.all.forEach { theme ->
                    val selected = theme.id == selectedId
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.05f else 0.96f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                        label = "theme-tile-scale"
                    )
                    val borderColor by animateColorAsState(
                        targetValue = if (selected) activeAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                        animationSpec = tween(220),
                        label = "theme-tile-border"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clickable { onSelect(theme.id) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 66.dp, height = 86.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = if (selected) 2.5.dp else 1.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(14.dp)
                                )
                        ) {
                            NoteThemeCanvas(
                                themeId = theme.id,
                                isMiniPreview = true,
                                animated = animated
                            )
                            if (theme.id.isBlank()) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    repeat(3) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth(0.75f)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f))
                                        )
                                        Spacer(Modifier.height(5.dp))
                                    }
                                }
                            }
                            if (selected) {
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(5.dp)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(activeAccent)
                                        .border(1.5.dp, Color.White, CircleShape)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            theme.title,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}