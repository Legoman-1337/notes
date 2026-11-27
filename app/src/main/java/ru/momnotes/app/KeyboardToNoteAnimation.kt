package ru.momnotes.app

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Контроллер и слой анимации «клава → заметки» (KeyboardToNoteAnimation).
 *
 * Сам перелёт буквы остаётся быстрым (~460 мс), чтобы не мешать набору,
 * а визуальные эффекты (вспышка клавиши, шлейф, ореол, кольца и искорки приземления)
 * длятся дольше (~1.15 с) и выглядят мягко и выразительно.
 */
internal data class FlyingKeyParticle(
    val id: Long,
    val glyph: String,
    val start: Offset,
    val control: Offset,
    val end: Offset,
    val createdAtNanos: Long,
    val flightDurationNanos: Long,
    val totalLifeNanos: Long,
    val initialRotationDeg: Float,
    val accentColor: Color,
    val textColor: Color,
    val showKeycap: Boolean,
    val sparkAnglesRad: List<Float>
)

internal class KeyboardToNoteController {
    internal val particles = mutableStateListOf<FlyingKeyParticle>()
    private var nextId = 1L

    private var overlayOriginInWindow: Offset = Offset.Zero
    private var overlaySize: IntSize = IntSize.Zero
    var isSoftKeyboardVisible: Boolean = false

    fun updateOverlayCoordinates(coordinates: LayoutCoordinates) {
        if (!coordinates.isAttached) return
        overlayOriginInWindow = coordinates.positionInWindow()
        overlaySize = coordinates.size
    }

    fun emitText(
        inserted: String,
        targetInWindow: Offset,
        accentColor: Color,
        textColor: Color,
        forceRandomOrigin: Boolean = false
    ) {
        if (overlaySize.width <= 0 || overlaySize.height <= 0) return
        val visible = inserted.filter { !it.isWhitespace() }
        if (visible.isEmpty()) return

        val charsToEmit = when {
            visible.length <= 3 -> visible.toList()
            else -> listOf(visible.first(), visible.last())
        }

        val baseLocalTarget = Offset(
            x = (targetInWindow.x - overlayOriginInWindow.x).coerceIn(16f, overlaySize.width - 16f),
            y = (targetInWindow.y - overlayOriginInWindow.y).coerceIn(16f, overlaySize.height - 16f)
        )

        val now = System.nanoTime()
        charsToEmit.forEachIndexed { index, ch ->
            val target = Offset(
                x = (baseLocalTarget.x + index * 10f).coerceIn(16f, overlaySize.width - 16f),
                y = baseLocalTarget.y
            )
            val useRandom = forceRandomOrigin || !isSoftKeyboardVisible
            val start = resolveStartPosition(ch, useRandom)
            val midX = (start.x + target.x) * 0.5f
            val arcLift = (overlaySize.height * 0.16f).coerceIn(56f, 168f)
            val horizontalSway = (target.x - start.x) * 0.18f
            val control = Offset(
                x = (midX - horizontalSway).coerceIn(12f, overlaySize.width - 12f),
                y = (minOf(start.y, target.y) - arcLift).coerceAtLeast(16f)
            )

            if (particles.size >= 36) {
                particles.removeAt(0)
            }
            val baseAngle = Random.nextFloat() * 2f * PI.toFloat()
            val sparks = List(5) { s -> baseAngle + s * (2f * PI.toFloat() / 5f) + (Random.nextFloat() - 0.5f) * 0.35f }
            particles.add(
                FlyingKeyParticle(
                    id = nextId++,
                    glyph = ch.toString(),
                    start = start,
                    control = control,
                    end = target,
                    createdAtNanos = now + index * 38_000_000L,
                    flightDurationNanos = 460_000_000L,
                    totalLifeNanos = 1_150_000_000L,
                    initialRotationDeg = Random.nextFloat() * 22f - 11f,
                    accentColor = accentColor,
                    textColor = textColor,
                    showKeycap = !useRandom,
                    sparkAnglesRad = sparks
                )
            )
        }
    }

    private fun resolveStartPosition(ch: Char, useRandomOrigin: Boolean): Offset {
        val w = overlaySize.width.toFloat()
        val h = overlaySize.height.toFloat()

        if (useRandomOrigin) {
            val rx = w * (0.12f + Random.nextFloat() * 0.76f)
            val ry = h * (0.80f + Random.nextFloat() * 0.15f)
            return Offset(rx, ry)
        }

        val mapped = KeyboardKeyMap.locate(ch)
        return if (mapped != null) {
            val (colFrac, rowIndex) = mapped
            val jitterX = (Random.nextFloat() - 0.5f) * (w * 0.014f)
            val x = (w * colFrac + jitterX).coerceIn(22f, w - 22f)
            val rowLift = when (rowIndex) {
                0 -> 28f
                1 -> 20f
                2 -> 13f
                else -> 7f
            }
            Offset(x, (h - rowLift).coerceAtLeast(24f))
        } else {
            Offset(
                x = w * (0.16f + Random.nextFloat() * 0.68f),
                y = (h - 14f).coerceAtLeast(24f)
            )
        }
    }
}

private object KeyboardKeyMap {
    private data class KeySpot(val col: Float, val row: Int)

    private val map: Map<Char, KeySpot> = buildMap {
        // Русская раскладка ЙЦУКЕН
        val ruRow0 = listOf('й', 'ц', 'у', 'к', 'е', 'н', 'г', 'ш', 'щ', 'з', 'х', 'ъ')
        ruRow0.forEachIndexed { i, c -> put(c, KeySpot(0.045f + i * 0.0825f, 0)) }
        put('ё', KeySpot(0.375f, 0))

        val ruRow1 = listOf('ф', 'ы', 'в', 'а', 'п', 'р', 'о', 'л', 'д', 'ж', 'э')
        ruRow1.forEachIndexed { i, c -> put(c, KeySpot(0.055f + i * 0.089f, 1)) }

        val ruRow2 = listOf('я', 'ч', 'с', 'м', 'и', 'т', 'ь', 'б', 'ю')
        ruRow2.forEachIndexed { i, c -> put(c, KeySpot(0.16f + i * 0.085f, 2)) }

        // Английская раскладка QWERTY
        val enRow0 = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p')
        enRow0.forEachIndexed { i, c -> put(c, KeySpot(0.05f + i * 0.10f, 0)) }

        val enRow1 = listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l')
        enRow1.forEachIndexed { i, c -> put(c, KeySpot(0.10f + i * 0.10f, 1)) }

        val enRow2 = listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')
        enRow2.forEachIndexed { i, c -> put(c, KeySpot(0.20f + i * 0.10f, 2)) }

        // Цифры
        val digits = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
        digits.forEachIndexed { i, c -> put(c, KeySpot(0.05f + i * 0.10f, 0)) }

        // Пунктуация и символы
        put(',', KeySpot(0.20f, 3))
        put('.', KeySpot(0.80f, 3))
        put('!', KeySpot(0.16f, 2))
        put('?', KeySpot(0.84f, 2))
        put('-', KeySpot(0.88f, 1))
        put(':', KeySpot(0.75f, 2))
        put(';', KeySpot(0.68f, 2))
        put('(', KeySpot(0.32f, 1))
        put(')', KeySpot(0.41f, 1))
        put('"', KeySpot(0.25f, 1))
        put('/', KeySpot(0.82f, 2))
        put('+', KeySpot(0.78f, 0))
        put('=', KeySpot(0.86f, 0))
        put('%', KeySpot(0.45f, 0))
    }

    fun locate(ch: Char): Pair<Float, Int>? {
        val spot = map[ch.lowercaseChar()] ?: return null
        return spot.col to spot.row
    }
}

@Composable
internal fun KeyboardToNoteOverlay(
    controller: KeyboardToNoteController,
    modifier: Modifier = Modifier
) {
    var frameNanos by remember { mutableLongStateOf(0L) }
    val hasParticles = controller.particles.isNotEmpty()

    LaunchedEffect(controller) {
        snapshotFlow { controller.particles.isNotEmpty() }
            .collectLatest { active ->
                if (!active) return@collectLatest
                while (controller.particles.isNotEmpty()) {
                    withFrameNanos { nanos ->
                        frameNanos = nanos
                        controller.particles.removeAll { p ->
                            nanos - p.createdAtNanos >= p.totalLifeNanos
                        }
                    }
                }
            }
    }

    if (!hasParticles) return

    val glyphPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val now = if (frameNanos == 0L) System.nanoTime() else frameNanos
        val keycapW = 30.dp.toPx()
        val keycapH = 36.dp.toPx()
        val keycapCorner = 10.dp.toPx()
        val baseTextPx = 23.dp.toPx()

        controller.particles.forEach { p ->
            val elapsed = now - p.createdAtNanos
            if (elapsed < 0L || elapsed > p.totalLifeNanos) return@forEach

            val flightRaw = (elapsed.toFloat() / p.flightDurationNanos.toFloat()).coerceIn(0f, 1f)
            val lifeRaw = (elapsed.toFloat() / p.totalLifeNanos.toFloat()).coerceIn(0f, 1f)
            val t = FastOutSlowInEasing.transform(flightRaw)
            val pos = quadBezier(p.start, p.control, p.end, t)

            // 1. Вспышка и мягкое затухание клавиши на старте (~520 мс)
            val startEffectRaw = (elapsed.toFloat() / 520_000_000f).coerceIn(0f, 1f)
            if (startEffectRaw < 1f) {
                val capAlpha = (1f - startEffectRaw) * (1f - startEffectRaw * 0.4f)
                val popY = p.start.y - startEffectRaw * 24.dp.toPx()
                if (p.showKeycap) {
                    drawRoundRect(
                        color = p.accentColor.copy(alpha = 0.28f * capAlpha),
                        topLeft = Offset(p.start.x - keycapW * 0.5f, popY - keycapH * 0.5f),
                        size = Size(keycapW, keycapH),
                        cornerRadius = CornerRadius(keycapCorner, keycapCorner)
                    )
                    drawRoundRect(
                        color = p.accentColor.copy(alpha = 0.65f * capAlpha),
                        topLeft = Offset(p.start.x - keycapW * 0.5f, popY - keycapH * 0.5f),
                        size = Size(keycapW, keycapH),
                        cornerRadius = CornerRadius(keycapCorner, keycapCorner),
                        style = Stroke(width = 1.6.dp.toPx())
                    )
                } else {
                    drawCircle(
                        color = p.accentColor.copy(alpha = 0.34f * capAlpha),
                        radius = (10f + startEffectRaw * 44f).dp.toPx(),
                        center = p.start
                    )
                }
            }

            // 2. Удлинённый шлейф кометы (тянется за буквой и мягко растворяется после её приземления)
            val trailFade = (1f - ((lifeRaw - 0.25f) / 0.55f).coerceIn(0f, 1f))
            if (trailFade > 0.01f) {
                listOf(
                    0.06f to 0.34f,
                    0.13f to 0.24f,
                    0.21f to 0.16f,
                    0.30f to 0.09f
                ).forEachIndexed { idx, (lag, baseAlpha) ->
                    val lagElapsed = (elapsed.toFloat() - lag * p.flightDurationNanos).coerceAtLeast(0f)
                    val et = FastOutSlowInEasing.transform((lagElapsed / p.flightDurationNanos).coerceIn(0f, 1f))
                    if (elapsed > lag * p.flightDurationNanos && et < 0.995f) {
                        val echoPos = quadBezier(p.start, p.control, p.end, et)
                        drawCircle(
                            color = p.accentColor.copy(alpha = baseAlpha * trailFade),
                            radius = (5.5f - idx * 0.9f).dp.toPx(),
                            center = echoPos
                        )
                    }
                }
            }

            // 3. Летящий символ + светящееся гало (во время полёта и лёгкое эхо сразу после)
            val glyphFadeOut = if (flightRaw < 1f) {
                if (flightRaw < 0.10f) flightRaw / 0.10f else 1f
            } else {
                val postRaw = ((elapsed - p.flightDurationNanos).toFloat() / 280_000_000f).coerceIn(0f, 1f)
                (1f - postRaw) * (1f - postRaw)
            }

            if (glyphFadeOut > 0.01f) {
                val arcBoost = sin(flightRaw * PI.toFloat()) * 0.40f
                val settleScale = if (flightRaw < 1f) (1.16f + arcBoost - flightRaw * 0.18f) else (0.98f + (1f - glyphFadeOut) * 0.18f)

                drawCircle(
                    color = p.accentColor.copy(alpha = 0.24f * glyphFadeOut),
                    radius = 18.dp.toPx() * settleScale,
                    center = pos
                )

                drawIntoCanvas { canvas ->
                    val native = canvas.nativeCanvas
                    native.save()
                    val rot = p.initialRotationDeg * (1f - t)
                    native.translate(pos.x, pos.y)
                    native.rotate(rot)
                    native.scale(settleScale, settleScale)

                    glyphPaint.textSize = baseTextPx
                    glyphPaint.color = p.textColor.copy(alpha = glyphFadeOut).toArgb()
                    val yOffset = -(glyphPaint.descent() + glyphPaint.ascent()) * 0.5f
                    native.drawText(p.glyph, 0f, yOffset, glyphPaint)
                    native.restore()
                }
            }

            // 4. Длинные эффекты приземления в точке вставки (начинаются на 72% полёта и живут до конца totalLifeNanos)
            val landStartNanos = (p.flightDurationNanos * 0.72f).toLong()
            if (elapsed > landStartNanos) {
                val landRaw = ((elapsed - landStartNanos).toFloat() / (p.totalLifeNanos - landStartNanos).toFloat()).coerceIn(0f, 1f)
                val landEase = FastOutSlowInEasing.transform(landRaw)
                val fade = (1f - landRaw) * (1f - landRaw * 0.25f)

                // Мягкая вспышка в месте вставки
                if (landRaw < 0.65f) {
                    val glowFade = 1f - (landRaw / 0.65f)
                    drawCircle(
                        color = p.accentColor.copy(alpha = 0.26f * glowFade),
                        radius = (8f + landEase * 18f).dp.toPx(),
                        center = p.end
                    )
                }

                // Первое расширяющееся кольцо
                drawCircle(
                    color = p.accentColor.copy(alpha = 0.65f * fade),
                    radius = (4f + landEase * 24f).dp.toPx(),
                    center = p.end,
                    style = Stroke(width = (2.0f - landRaw * 1.1f).coerceAtLeast(0.7f).dp.toPx())
                )

                // Второе, более широкое и воздушное эхо-кольцо
                if (landRaw > 0.14f) {
                    val secondRaw = ((landRaw - 0.14f) / 0.86f).coerceIn(0f, 1f)
                    val secondEase = FastOutSlowInEasing.transform(secondRaw)
                    drawCircle(
                        color = p.accentColor.copy(alpha = 0.36f * (1f - secondRaw)),
                        radius = (5f + secondEase * 32f).dp.toPx(),
                        center = p.end,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }

                // Звёздочки-искорки, разлетающиеся от точки приземления
                p.sparkAnglesRad.forEachIndexed { idx, angle ->
                    val dist = (5f + landEase * (22f + (idx % 2) * 8f)).dp.toPx()
                    val sx = p.end.x + cos(angle) * dist
                    val sy = p.end.y + sin(angle) * dist
                    val sparkSize = (3.4f - landRaw * 2.1f).coerceAtLeast(0.8f).dp.toPx()
                    val sparkColor = if (idx % 2 == 0) p.accentColor else p.textColor
                    drawSparkle(
                        center = Offset(sx, sy),
                        radius = sparkSize,
                        color = sparkColor.copy(alpha = 0.72f * fade)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawSparkle(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticBezierTo(center.x, center.y, center.x + radius, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y + radius)
        quadraticBezierTo(center.x, center.y, center.x - radius, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color)
}

private fun quadBezier(p0: Offset, p1: Offset, p2: Offset, t: Float): Offset {
    val omt = 1f - t
    return Offset(
        x = omt * omt * p0.x + 2f * omt * t * p1.x + t * t * p2.x,
        y = omt * omt * p0.y + 2f * omt * t * p1.y + t * t * p2.y
    )
}