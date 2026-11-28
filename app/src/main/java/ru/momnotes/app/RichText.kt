package ru.momnotes.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONArray
import org.json.JSONObject

internal data class RichRun(
    val start: Int,
    val end: Int,
    val color: String? = null,
    val size: Float? = null,
    val font: String? = null,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: String? = null,
    val strike: Boolean = false
)

internal class RichEditorState {
    internal var editText: RichEditText? = null
    internal var lastCallbackValue: String = ""
    var activeFlags by mutableStateOf(emptySet<String>())
        internal set
    var activeColor by mutableStateOf<String?>(null)
        internal set
    var activeSize by mutableStateOf<Float?>(null)
        internal set
    var activeFont by mutableStateOf<String?>(null)
        internal set
    var onFormatted: ((String) -> Unit)? = null

    fun apply(attribute: String, style: String = "toggle") {
        val editor = editText ?: return
        val updated = editor.applyFormat(attribute, style)
        lastCallbackValue = updated
        syncFlags()
        onFormatted?.invoke(updated)
    }

    fun clearFormatting() {
        val editor = editText ?: return
        val updated = editor.clearFormatting()
        lastCallbackValue = updated
        syncFlags()
        onFormatted?.invoke(updated)
    }

    internal fun syncFlags() {
        val editor = editText
        if (editor == null) {
            activeFlags = emptySet()
            activeColor = null
            activeSize = null
            activeFont = null
        } else {
            activeFlags = editor.activeStyleFlags()
            activeColor = editor.activeAttributeValue("color")
            activeSize = editor.activeAttributeValue("size")?.toFloatOrNull()
            activeFont = editor.activeAttributeValue("font")
        }
    }
}

internal object RichTextDocument {
    private fun parse(value: String): Pair<String, List<RichRun>>? {
        if (!value.startsWith("{\"text\":")) return null
        return try {
            val json = JSONObject(value)
            val text = json.optString("text", "")
            val sourceRuns = json.optJSONArray("runs") ?: JSONArray()
            val runs = (0 until sourceRuns.length()).mapNotNull { index ->
                val item = sourceRuns.optJSONObject(index) ?: return@mapNotNull null
                val start = item.optInt("start", -1)
                val end = item.optInt("end", -1)
                if (start < 0 || end <= start || end > text.length) return@mapNotNull null
                RichRun(
                    start = start,
                    end = end,
                    color = item.optString("color").takeIf { it.matches(Regex("#[0-9A-Fa-f]{6}")) },
                    size = item.optDouble("size", Double.NaN).takeIf { it.isFinite() }?.toFloat(),
                    font = item.optString("font").takeIf(String::isNotBlank),
                    bold = item.optBoolean("bold"),
                    italic = item.optBoolean("italic"),
                    underline = item.optString("underline").takeIf { it in setOf("single", "double", "wavy") },
                    strike = item.optBoolean("strike")
                )
            }
            text to runs
        } catch (_: Exception) {
            null
        }
    }

    fun plainText(value: String): String = parse(value)?.first ?: value

    fun canonical(value: String): String {
        val (text, runs) = decode(value)
        return if (runs.isEmpty()) text else encode(text, runs)
    }

    fun appendText(value: String, addition: String): String {
        val (text, runs) = decode(value)
        return if (runs.isEmpty()) text + addition else encode(text + addition, runs)
    }

    private fun mergeAdjacentRuns(runs: List<RichRun>, textLen: Int): List<RichRun> {
        val valid = runs.filter { it.start >= 0 && it.end <= textLen && it.end > it.start }
            .sortedWith(compareBy<RichRun> { it.start }.thenBy { it.end }.thenBy { it.color ?: it.font ?: it.underline ?: "" })
        if (valid.size <= 1) return valid
        val merged = ArrayList<RichRun>(valid.size)
        for (run in valid) {
            val idx = merged.indexOfLast {
                it.end >= run.start &&
                        it.color == run.color &&
                        it.size == run.size &&
                        it.font == run.font &&
                        it.bold == run.bold &&
                        it.italic == run.italic &&
                        it.underline == run.underline &&
                        it.strike == run.strike
            }
            if (idx >= 0) {
                val prev = merged[idx]
                merged[idx] = prev.copy(end = maxOf(prev.end, run.end))
            } else {
                merged.add(run)
            }
        }
        return merged
    }

    fun encode(text: String, runs: List<RichRun> = emptyList()): String = JSONObject().apply {
        put("text", text)
        val compact = mergeAdjacentRuns(runs, text.length)
        put("runs", JSONArray().apply {
            compact.forEach { run ->
                put(JSONObject().apply {
                    put("start", run.start)
                    put("end", run.end)
                    run.color?.let { put("color", it) }
                    run.size?.let { put("size", it.toDouble()) }
                    run.font?.let { put("font", it) }
                    if (run.bold) put("bold", true)
                    if (run.italic) put("italic", true)
                    run.underline?.let { put("underline", it) }
                    if (run.strike) put("strike", true)
                })
            }
        })
    }.toString()

    fun annotatedString(value: String): AnnotatedString {
        val (text, runs) = parse(value) ?: (value to emptyList())
        return buildAnnotatedString {
            append(text)
            runs.forEach { run ->
                val decorations = buildList {
                    if (run.underline == "single") add(TextDecoration.Underline)
                    if (run.strike) add(TextDecoration.LineThrough)
                }
                addStyle(
                    SpanStyle(
                        color = run.color?.let(::colorFromHex) ?: Color.Unspecified,
                        fontSize = run.size?.sp ?: androidx.compose.ui.unit.TextUnit.Unspecified,
                        fontFamily = run.font?.let(::fontFamily),
                        fontWeight = if (run.bold) FontWeight.Bold else null,
                        fontStyle = if (run.italic) FontStyle.Italic else null,
                        textDecoration = if (decorations.isEmpty()) null else TextDecoration.combine(decorations)
                    ),
                    run.start.coerceIn(0, text.length),
                    run.end.coerceIn(0, text.length)
                )
            }
        }
    }

    internal fun decode(value: String): Pair<String, List<RichRun>> = parse(value) ?: (value to emptyList())

    private fun colorFromHex(value: String): Color = try {
        Color(android.graphics.Color.parseColor(value))
    } catch (_: Exception) {
        Color.Unspecified
    }

    private fun fontFamily(value: String): FontFamily = when {
        value.contains("serif") && !value.startsWith("sans") -> FontFamily.Serif
        value.contains("mono") -> FontFamily.Monospace
        value.contains("cursive") -> FontFamily.Cursive
        else -> FontFamily.SansSerif
    }
}

private class RichColorSpan(val value: String) : CharacterStyle(), UpdateAppearance {
    override fun updateDrawState(tp: TextPaint) { runCatching { tp.color = android.graphics.Color.parseColor(value) } }
}
private class RichSizeSpan(val value: Float, private val scaledDensity: Float) : CharacterStyle(), UpdateAppearance {
    override fun updateDrawState(tp: TextPaint) { tp.textSize = value * scaledDensity }
}
private class RichFontSpan(val value: String) : CharacterStyle(), UpdateAppearance {
    override fun updateDrawState(tp: TextPaint) {
        val current = tp.typeface?.style ?: Typeface.NORMAL
        tp.typeface = Typeface.create(value, current)
    }
}
private class RichBoldSpan : CharacterStyle(), UpdateAppearance {
    override fun updateDrawState(tp: TextPaint) {
        tp.isFakeBoldText = true
    }
}
private class RichItalicSpan : CharacterStyle(), UpdateAppearance {
    override fun updateDrawState(tp: TextPaint) {
        tp.textSkewX = -0.25f
    }
}
private class RichUnderlineSpan(val value: String) : CharacterStyle(), UpdateAppearance {
    override fun updateDrawState(tp: TextPaint) { tp.isUnderlineText = value == "single" }
}
private class RichStrikeSpan : CharacterStyle(), UpdateAppearance {
    override fun updateDrawState(tp: TextPaint) { tp.isStrikeThruText = true }
}

private data class SavedReplacedSpan(
    val relStart: Int,
    val relEnd: Int,
    val span: CharacterStyle
)

internal class RichEditText(context: Context) : AppCompatEditText(context) {
    private var suppressCallback = false
    // Активные параметры ввода: сохраняются, пока пользователь сам их не выключит или не сбросит
    private val pendingFormats = linkedMapOf<String, String>()
    private var lastSelectionStart = 0
    private var lastSelectionEnd = 0

    // Отслеживание точного диффа при IME-композиции (Gboard / Яндекс / Samsung),
    // чтобы замена/коммит слова не сбрасывали спаны уже набранных букв.
    private var changedStart = 0
    private var oldReplacedText = ""
    private var savedReplacedSpans: List<SavedReplacedSpan> = emptyList()
    private var newReplacedText = ""

    private var pendingTypedText: String? = null
    private var pendingTypedEnd: Int = 0
    var onRichTextChanged: ((String) -> Unit)? = null
    var onSelectionChanged: (() -> Unit)? = null
    var onCharacterTyped: ((text: String, targetInWindow: Offset) -> Unit)? = null

    init {
        gravity = Gravity.TOP or Gravity.START
        isFocusable = true
        isFocusableInTouchMode = true
        isCursorVisible = true
        showSoftInputOnFocus = true
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        setLineSpacing(0f, 1.28f)
        background = null
        setOnClickListener {
            requestFocus()
            post {
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                    ?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (suppressCallback) return
                changedStart = start
                if (s != null && count > 0) {
                    val safeEnd = (start + count).coerceAtMost(s.length)
                    oldReplacedText = s.subSequence(start, safeEnd).toString()
                    savedReplacedSpans = if (s is Spanned) {
                        s.getSpans(start, safeEnd, CharacterStyle::class.java).mapNotNull { sp ->
                            if (!isOurRichSpan(sp)) return@mapNotNull null
                            val spStart = maxOf(s.getSpanStart(sp), start) - start
                            val spEnd = minOf(s.getSpanEnd(sp), safeEnd) - start
                            if (spEnd > spStart) SavedReplacedSpan(spStart, spEnd, sp) else null
                        }
                    } else emptyList()
                } else {
                    oldReplacedText = ""
                    savedReplacedSpans = emptyList()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (suppressCallback) {
                    pendingTypedText = null
                    return
                }
                if (s != null && count > 0) {
                    val safeEnd = (start + count).coerceAtMost(s.length)
                    newReplacedText = s.subSequence(start, safeEnd).toString()
                    if (count in 1..12) {
                        pendingTypedText = newReplacedText
                        pendingTypedEnd = safeEnd
                    } else {
                        pendingTypedText = null
                    }
                } else {
                    newReplacedText = ""
                    pendingTypedText = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (suppressCallback) {
                    pendingTypedText = null
                    savedReplacedSpans = emptyList()
                    return
                }
                applyDiffFormatting(s)
                savedReplacedSpans = emptyList()
                onRichTextChanged?.invoke(currentRichText())

                val typed = pendingTypedText
                val endPos = pendingTypedEnd
                pendingTypedText = null
                if (typed != null && !typed.contains('\n') && typed.any { !it.isWhitespace() }) {
                    val callback = onCharacterTyped
                    if (callback != null) {
                        post {
                            val textLayout = this@RichEditText.layout ?: return@post
                            val textLen = this@RichEditText.length()
                            if (textLen <= 0) return@post
                            val charOffset = (endPos - 1).coerceIn(0, textLen - 1)
                            val endOffset = endPos.coerceIn(0, textLen)
                            val line = textLayout.getLineForOffset(charOffset)
                            val startX = textLayout.getPrimaryHorizontal(charOffset)
                            val charCenterX = if (endOffset > charOffset && textLayout.getLineForOffset(endOffset) == line) {
                                (startX + textLayout.getPrimaryHorizontal(endOffset)) * 0.5f
                            } else {
                                startX + textSize * 0.25f
                            }
                            val baseline = textLayout.getLineBaseline(line).toFloat()
                            val ascent = textLayout.getLineAscent(line).toFloat()
                            val loc = IntArray(2)
                            getLocationInWindow(loc)
                            val targetX = loc[0] + compoundPaddingLeft - scrollX + charCenterX
                            val targetY = loc[1] + compoundPaddingTop - scrollY + baseline + ascent * 0.45f
                            callback(typed, Offset(targetX, targetY))
                        }
                    }
                }
            }
        })
    }

    private fun isOurRichSpan(span: Any): Boolean = span is RichColorSpan ||
            span is RichSizeSpan ||
            span is RichFontSpan ||
            span is RichBoldSpan ||
            span is RichItalicSpan ||
            span is RichUnderlineSpan ||
            span is RichStrikeSpan

    fun configurePadding(horizontalDp: Int, topDp: Int, bottomDp: Int) {
        val d = resources.displayMetrics.density
        setPadding((horizontalDp * d).toInt(), (topDp * d).toInt(), (horizontalDp * d).toInt(), (bottomDp * d).toInt())
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (selStart < 0 || selEnd < 0) return
        if (selStart != lastSelectionStart || selEnd != lastSelectionEnd) {
            lastSelectionStart = selStart
            lastSelectionEnd = selEnd
            onSelectionChanged?.invoke()
        }
    }

    private fun spanClassFor(attribute: String): Class<out CharacterStyle> = when (attribute) {
        "color" -> RichColorSpan::class.java
        "size" -> RichSizeSpan::class.java
        "font" -> RichFontSpan::class.java
        "bold" -> RichBoldSpan::class.java
        "italic" -> RichItalicSpan::class.java
        "underline" -> RichUnderlineSpan::class.java
        else -> RichStrikeSpan::class.java
    }

    private fun applyDiffFormatting(editable: Editable?) {
        if (editable == null) return
        val start = changedStart.coerceIn(0, editable.length)
        val oldLen = oldReplacedText.length
        val newLen = newReplacedText.length
        val newEnd = (start + newLen).coerceIn(start, editable.length)

        var prefix = 0
        val minLen = minOf(oldLen, newLen)
        while (prefix < minLen && oldReplacedText[prefix] == newReplacedText[prefix]) {
            prefix++
        }
        if (prefix == 0 && oldLen == newLen && oldLen > 0 && oldReplacedText.equals(newReplacedText, ignoreCase = true)) {
            prefix = oldLen
        }
        var suffix = 0
        while (suffix < minLen - prefix &&
            oldReplacedText[oldLen - 1 - suffix] == newReplacedText[newLen - 1 - suffix]
        ) {
            suffix++
        }

        if (savedReplacedSpans.isNotEmpty() && (prefix > 0 || suffix > 0)) {
            val delta = newLen - oldLen
            for (saved in savedReplacedSpans) {
                val kind = saved.span.javaClass
                if (prefix > 0 && saved.relStart < prefix) {
                    val pStart = (start + saved.relStart).coerceIn(0, editable.length)
                    val pEnd = (start + minOf(saved.relEnd, prefix)).coerceIn(pStart, editable.length)
                    if (pEnd > pStart && !hasExactSpan(editable, pStart, pEnd, kind)) {
                        cloneSpan(saved.span).forEach {
                            editable.setSpan(it, pStart, pEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
                if (suffix > 0 && saved.relEnd > oldLen - suffix) {
                    val origTailStart = maxOf(saved.relStart, oldLen - suffix)
                    val sStart = (start + origTailStart + delta).coerceIn(0, editable.length)
                    val sEnd = (start + saved.relEnd + delta).coerceIn(sStart, editable.length)
                    if (sEnd > sStart && !hasExactSpan(editable, sStart, sEnd, kind)) {
                        cloneSpan(saved.span).forEach {
                            editable.setSpan(it, sStart, sEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
            }
        }

        val insertStart = (start + prefix).coerceIn(0, editable.length)
        val insertEnd = (newEnd - suffix).coerceIn(insertStart, editable.length)
        if (insertEnd > insertStart && pendingFormats.isNotEmpty()) {
            applyPendingFormatsRange(editable, insertStart, insertEnd)
        }
    }

    private fun hasExactSpan(editable: Spannable, start: Int, end: Int, kind: Class<out CharacterStyle>): Boolean =
        editable.getSpans(start, end, kind).any { editable.getSpanStart(it) <= start && editable.getSpanEnd(it) >= end }

    private fun applyPendingFormatsRange(editable: Editable, safeStart: Int, end: Int) {
        if (end <= safeStart || pendingFormats.isEmpty()) return
        pendingFormats.toMap().forEach { (attribute, value) ->
            val kind = spanClassFor(attribute)
            val existing = editable.getSpans(safeStart, end, kind).toList()
            existing.forEach { old ->
                val oldStart = editable.getSpanStart(old)
                val oldEnd = editable.getSpanEnd(old)
                editable.removeSpan(old)
                if (oldStart < safeStart) cloneSpan(old).forEach { editable.setSpan(it, oldStart, minOf(safeStart, oldEnd), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
                if (oldEnd > end) cloneSpan(old).forEach { editable.setSpan(it, maxOf(end, oldStart), oldEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
            }
            if (value != "off") {
                createFormatSpan(attribute, value)?.let {
                    editable.setSpan(it, safeStart, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }

    private fun createFormatSpan(attribute: String, value: String): CharacterStyle? = when (attribute) {
        "color" -> RichColorSpan(value)
        "size" -> value.toFloatOrNull()?.let { RichSizeSpan(it, resources.displayMetrics.scaledDensity) }
        "font" -> RichFontSpan(value)
        "bold" -> RichBoldSpan()
        "italic" -> RichItalicSpan()
        "underline" -> value.takeUnless { it == "none" || it == "off" }?.let(::RichUnderlineSpan)
        "strike" -> RichStrikeSpan()
        else -> null
    }

    private fun hasSpanAtRange(editable: Spannable, start: Int, end: Int, kind: Class<out CharacterStyle>): Boolean =
        editable.getSpans(start, end, kind).any { editable.getSpanStart(it) < end && editable.getSpanEnd(it) > start }

    private fun extractSpanValue(span: CharacterStyle): String? = when (span) {
        is RichColorSpan -> span.value
        is RichSizeSpan -> span.value.toInt().toString()
        is RichFontSpan -> span.value
        is RichUnderlineSpan -> span.value
        else -> null
    }

    fun activeAttributeValue(attribute: String): String? {
        val pending = pendingFormats[attribute]
        if (pending == "off") return null
        if (pending != null) return pending

        val editable = text as? Spannable ?: return null
        val s = selectionStart.coerceIn(0, editable.length)
        val e = selectionEnd.coerceIn(0, editable.length)
        val kind = spanClassFor(attribute)
        val span = if (s == e) {
            if (s > 0) editable.getSpans(s - 1, s, kind).lastOrNull {
                editable.getSpanStart(it) < s && editable.getSpanEnd(it) >= s
            } else null
        } else {
            val start = minOf(s, e)
            val end = maxOf(s, e)
            editable.getSpans(start, end, kind).lastOrNull {
                editable.getSpanStart(it) < end && editable.getSpanEnd(it) > start
            }
        }
        return span?.let(::extractSpanValue)
    }

    fun activeStyleFlags(): Set<String> {
        val editable = text as? Spannable ?: return emptySet()
        val s = selectionStart.coerceIn(0, editable.length)
        val e = selectionEnd.coerceIn(0, editable.length)
        val out = mutableSetOf<String>()
        val toggleAttrs = listOf("bold", "italic", "underline", "strike")
        if (s == e) {
            toggleAttrs.forEach { attr ->
                val pending = pendingFormats[attr]
                when {
                    pending == "off" -> Unit
                    pending != null -> out += attr
                    s > 0 && hasSpanAtRange(editable, s - 1, s, spanClassFor(attr)) -> out += attr
                }
            }
            return out
        }
        val start = minOf(s, e)
        val end = maxOf(s, e)
        toggleAttrs.forEach { attr ->
            if (hasSpanAtRange(editable, start, end, spanClassFor(attr))) out += attr
        }
        return out
    }

    fun clearFormatting(): String {
        val editable = text as? Spannable ?: return currentRichText()
        val start = minOf(selectionStart.coerceAtLeast(0), selectionEnd.coerceAtLeast(0))
        val end = maxOf(selectionStart.coerceAtLeast(0), selectionEnd.coerceAtLeast(0))
        pendingFormats.clear()
        listOf("bold", "italic", "underline", "strike", "color", "size", "font").forEach {
            pendingFormats[it] = "off"
        }
        if (start != end) {
            editable.getSpans(start, end, CharacterStyle::class.java).forEach { span ->
                if (!isOurRichSpan(span)) return@forEach
                val oldStart = editable.getSpanStart(span)
                val oldEnd = editable.getSpanEnd(span)
                if (oldStart < end && oldEnd > start) {
                    editable.removeSpan(span)
                    if (oldStart < start) cloneSpan(span).forEach { editable.setSpan(it, oldStart, minOf(start, oldEnd), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
                    if (oldEnd > end) cloneSpan(span).forEach { editable.setSpan(it, maxOf(end, oldStart), oldEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
                }
            }
        }
        invalidate()
        return currentRichText()
    }

    fun currentRichText(): String {
        val editable = text as? Spannable ?: return RichTextDocument.encode(text.toString())
        val runs = mutableListOf<RichRun>()
        fun range(span: CharacterStyle, make: (Int, Int) -> RichRun) {
            val start = editable.getSpanStart(span)
            val end = editable.getSpanEnd(span)
            if (start >= 0 && end > start) runs += make(start, end)
        }
        editable.getSpans(0, editable.length, RichColorSpan::class.java).forEach { span -> range(span) { a, b -> RichRun(a, b, color = span.value) } }
        editable.getSpans(0, editable.length, RichSizeSpan::class.java).forEach { span -> range(span) { a, b -> RichRun(a, b, size = span.value) } }
        editable.getSpans(0, editable.length, RichFontSpan::class.java).forEach { span -> range(span) { a, b -> RichRun(a, b, font = span.value) } }
        editable.getSpans(0, editable.length, RichBoldSpan::class.java).forEach { span -> range(span) { a, b -> RichRun(a, b, bold = true) } }
        editable.getSpans(0, editable.length, RichItalicSpan::class.java).forEach { span -> range(span) { a, b -> RichRun(a, b, italic = true) } }
        editable.getSpans(0, editable.length, RichUnderlineSpan::class.java).forEach { span -> range(span) { a, b -> RichRun(a, b, underline = span.value) } }
        editable.getSpans(0, editable.length, RichStrikeSpan::class.java).forEach { span -> range(span) { a, b -> RichRun(a, b, strike = true) } }
        return if (runs.isEmpty()) editable.toString() else RichTextDocument.encode(editable.toString(), runs)
    }

    fun loadRichText(encoded: String, clearPending: Boolean = false) {
        val (plain, runs) = RichTextDocument.decode(encoded)
        suppressCallback = true
        if (clearPending) pendingFormats.clear()
        setText(plain)
        val editable = text as? SpannableStringBuilder
        if (editable != null) runs.forEach { run ->
            val spans = buildList<CharacterStyle> {
                run.color?.let { add(RichColorSpan(it)) }
                run.size?.let { add(RichSizeSpan(it, resources.displayMetrics.scaledDensity)) }
                run.font?.let { add(RichFontSpan(it)) }
                if (run.bold) add(RichBoldSpan())
                if (run.italic) add(RichItalicSpan())
                run.underline?.let { add(RichUnderlineSpan(it)) }
                if (run.strike) add(RichStrikeSpan())
            }
            spans.forEach { editable.setSpan(it, run.start, run.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
        }
        setSelection(editable?.length ?: plain.length)
        suppressCallback = false
        invalidate()
    }

    fun applyFormat(attribute: String, value: String = "toggle"): String {
        val editable = text as? Spannable ?: return currentRichText()
        val selectionStart = selectionStart.coerceIn(0, editable.length)
        val selectionEnd = selectionEnd.coerceIn(0, editable.length)
        val isToggle = attribute in setOf("bold", "italic", "strike", "underline") || value == "toggle"
        if (selectionStart == selectionEnd) {
            if (isToggle) {
                val activeAtCursor = attribute in activeStyleFlags()
                pendingFormats[attribute] = if (activeAtCursor) "off" else value
            } else {
                if (value == "none" || value == "off") pendingFormats[attribute] = "off"
                else pendingFormats[attribute] = value
            }
            invalidate()
            return currentRichText()
        }

        val start = minOf(selectionStart, selectionEnd)
        val end = maxOf(selectionStart, selectionEnd)
        val kind = spanClassFor(attribute)
        val existing = editable.getSpans(start, end, kind).toList()
        val turnOff = (isToggle && existing.any { editable.getSpanStart(it) < end && editable.getSpanEnd(it) > start }) ||
                value == "none" || value == "off"
        existing.forEach { old ->
            val oldStart = editable.getSpanStart(old)
            val oldEnd = editable.getSpanEnd(old)
            editable.removeSpan(old)
            if (oldStart < start) cloneSpan(old).forEach { editable.setSpan(it, oldStart, minOf(start, oldEnd), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
            if (oldEnd > end) cloneSpan(old).forEach { editable.setSpan(it, maxOf(end, oldStart), oldEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
        }
        if (!turnOff) {
            createFormatSpan(attribute, value)?.let {
                editable.setSpan(it, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            pendingFormats[attribute] = value
        } else {
            pendingFormats[attribute] = "off"
        }
        setSelection(selectionStart, selectionEnd)
        invalidate()
        return currentRichText()
    }

    private fun cloneSpan(span: CharacterStyle): List<CharacterStyle> = when (span) {
        is RichColorSpan -> listOf(RichColorSpan(span.value))
        is RichSizeSpan -> listOf(RichSizeSpan(span.value, resources.displayMetrics.scaledDensity))
        is RichFontSpan -> listOf(RichFontSpan(span.value))
        is RichBoldSpan -> listOf(RichBoldSpan())
        is RichItalicSpan -> listOf(RichItalicSpan())
        is RichUnderlineSpan -> listOf(RichUnderlineSpan(span.value))
        is RichStrikeSpan -> listOf(RichStrikeSpan())
        else -> emptyList()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val layout = layout ?: return
        val editable = text as? Spannable ?: return
        val custom = editable.getSpans(0, editable.length, RichUnderlineSpan::class.java).filter { it.value == "double" || it.value == "wavy" }
        if (custom.isEmpty()) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = currentTextColor
            style = Paint.Style.STROKE
            strokeWidth = resources.displayMetrics.density
        }
        val density = resources.displayMetrics.density
        canvas.save()
        canvas.translate((compoundPaddingLeft - scrollX).toFloat(), (compoundPaddingTop - scrollY).toFloat())
        custom.forEach { span ->
            val start = editable.getSpanStart(span).coerceAtLeast(0)
            val end = editable.getSpanEnd(span).coerceAtMost(editable.length)
            if (end <= start) return@forEach
            val firstLine = layout.getLineForOffset(start)
            val lastLine = layout.getLineForOffset((end - 1).coerceAtLeast(start))
            for (line in firstLine..lastLine) {
                val from = maxOf(start, layout.getLineStart(line))
                val to = minOf(end, layout.getLineEnd(line))
                if (to <= from) continue
                val x1 = layout.getPrimaryHorizontal(from)
                val x2 = layout.getPrimaryHorizontal(to)
                val y = layout.getLineBaseline(line) + 3.5f * density
                if (span.value == "double") {
                    canvas.drawLine(x1, y + 2.4f * density, x2, y + 2.4f * density, paint)
                } else {
                    val path = android.graphics.Path().apply {
                        moveTo(x1, y)
                        var x = x1
                        var up = true
                        while (x < x2) {
                            val next = (x + 3.5f * density).coerceAtMost(x2)
                            lineTo(next, y + if (up) -1.3f * density else 1.3f * density)
                            x = next
                            up = !up
                        }
                    }
                    canvas.drawPath(path, paint)
                }
            }
        }
        canvas.restore()
    }
}

@Composable
internal fun RichFormatBar(
    state: RichEditorState,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    var chooser by remember { mutableStateOf<String?>(null) }
    var customSize by remember { mutableStateOf("16") }
    val colors = listOf(
        "#202124", "#FFFFFF", "#E53935", "#C62828", "#F57C00", "#FFB300", "#FDD835",
        "#43A047", "#00897B", "#00ACC1", "#1E88E5", "#3949AB", "#5E35B1", "#8E24AA", "#D81B60"
    )
    val sizeOptions = listOf("12", "14", "16", "18", "22", "26")
    val fontOptions = listOf(
        "sans-serif" to "Обычный (Sans)",
        "serif" to "С засечками (Serif)",
        "monospace" to "Моноширинный",
        "cursive" to "Рукописный"
    )
    val active = state.activeFlags
    val currentColorHex = state.activeColor
    val currentSize = state.activeSize
    val currentFont = state.activeFont
    val parsedColor = remember(currentColorHex) {
        currentColorHex?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
    }
    val sizeActive = currentSize != null
    val fontActive = currentFont != null && currentFont != "sans-serif"
    val colorActive = parsedColor != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        @Composable
        fun FormatChip(flag: String, content: @Composable (Color) -> Unit) {
            val isOn = flag in active
            val chipBg by animateColorAsState(
                targetValue = if (isOn) accentColor.copy(alpha = 0.22f) else textColor.copy(alpha = 0.08f),
                animationSpec = tween(180),
                label = "fmt-chip-bg"
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = chipBg,
                border = if (isOn) BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)) else null,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        if (flag == "underline") state.apply("underline", "single") else state.apply(flag)
                    }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    content(if (isOn) accentColor else textColor)
                }
            }
        }

        FormatChip("bold") { c -> Text("Ж", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = c) }
        FormatChip("italic") { c -> Text("К", fontStyle = FontStyle.Italic, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = c) }
        FormatChip("underline") { c -> Text("Ч", textDecoration = TextDecoration.Underline, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = c) }
        FormatChip("strike") { c -> Text("S", textDecoration = TextDecoration.LineThrough, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = c) }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (sizeActive) accentColor.copy(alpha = 0.22f) else textColor.copy(alpha = 0.08f),
            border = if (sizeActive) BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)) else null,
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
                customSize = (currentSize?.toInt() ?: 16).toString()
                chooser = "size"
            }
        ) {
            Text(
                if (currentSize != null) "${currentSize.toInt()} sp ▾" else "Размер ▾",
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                fontSize = 13.sp,
                fontWeight = if (sizeActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (sizeActive) accentColor else textColor
            )
        }

        val fontLabel = when (currentFont) {
            "serif" -> "Serif ▾"
            "monospace" -> "Mono ▾"
            "cursive" -> "Cursive ▾"
            else -> "Шрифт ▾"
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (fontActive) accentColor.copy(alpha = 0.22f) else textColor.copy(alpha = 0.08f),
            border = if (fontActive) BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)) else null,
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { chooser = "font" }
        ) {
            Text(
                fontLabel,
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                fontSize = 13.sp,
                fontWeight = if (fontActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (fontActive) accentColor else textColor
            )
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (colorActive) accentColor.copy(alpha = 0.22f) else textColor.copy(alpha = 0.08f),
            border = if (colorActive) BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)) else null,
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { chooser = "color" }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(parsedColor ?: accentColor)
                        .border(1.dp, textColor.copy(alpha = 0.35f), CircleShape)
                )
                Text(
                    "Цвет ▾",
                    fontSize = 13.sp,
                    fontWeight = if (colorActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (colorActive) accentColor else textColor
                )
            }
        }
        TextButton(onClick = { state.clearFormatting() }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
            Text("Сброс", fontSize = 12.sp, color = textColor.copy(alpha = 0.72f))
        }
    }

    chooser?.let { mode ->
        AlertDialog(
            onDismissRequest = { chooser = null },
            title = {
                Text(
                    when (mode) {
                        "size" -> "Размер текста"
                        "font" -> "Шрифт текста"
                        else -> "Цвет текста"
                    }
                )
            },
            text = {
                when (mode) {
                    "color" -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            colors.forEach { hex ->
                                val selected = currentColorHex.equals(hex, ignoreCase = true)
                                Surface(
                                    modifier = Modifier.padding(2.dp),
                                    shape = RoundedCornerShape(50),
                                    color = Color(android.graphics.Color.parseColor(hex)),
                                    border = BorderStroke(
                                        if (selected) 3.dp else 1.dp,
                                        if (selected) accentColor else MaterialTheme.colorScheme.outline
                                    ),
                                    onClick = { state.apply("color", hex); chooser = null }
                                ) {
                                    Text(
                                        "A",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        color = if (hex == "#FFFFFF") Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (colorActive) {
                            TextButton(onClick = { state.apply("color", "off"); chooser = null }) {
                                Text("Вернуть обычный цвет")
                            }
                        }
                    }
                    "font" -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        fontOptions.forEach { (fontKey, title) ->
                            val family = when (fontKey) {
                                "serif" -> FontFamily.Serif
                                "monospace" -> FontFamily.Monospace
                                "cursive" -> FontFamily.Cursive
                                else -> FontFamily.SansSerif
                            }
                            val selected = currentFont == fontKey
                            TextButton(
                                onClick = {
                                    state.apply("font", if (fontKey == "sans-serif") "off" else fontKey)
                                    chooser = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (selected) "✓ $title" else title,
                                    fontFamily = family,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    else -> Column {
                        sizeOptions.forEach { size ->
                            val selected = currentSize?.toInt()?.toString() == size
                            TextButton(
                                onClick = { state.apply("size", size); chooser = null },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (selected) "✓ $size sp" else "$size sp",
                                    fontSize = size.toInt().sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customSize,
                            onValueChange = { customSize = it.filter(Char::isDigit).take(2) },
                            label = { Text("Другой размер, 8–48 sp") },
                            singleLine = true
                        )
                        if (sizeActive) {
                            TextButton(onClick = { state.apply("size", "off"); chooser = null }) {
                                Text("Вернуть обычный размер")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    if (mode == "size") {
                        TextButton(onClick = {
                            val size = customSize.toIntOrNull()?.coerceIn(8, 48)
                            if (size != null) state.apply("size", size.toString())
                            chooser = null
                        }) { Text("Применить") }
                    }
                    TextButton(onClick = { chooser = null }) { Text("Закрыть") }
                }
            }
        )
    }
}

@Composable
internal fun RichTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    editorHeight: Dp = 190.dp,
    seamless: Boolean = false,
    state: RichEditorState? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    hintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onCharacterTyped: ((String, Offset) -> Unit)? = null
) {
    val latestChange by rememberUpdatedState(onValueChange)
    val latestCharTyped by rememberUpdatedState(onCharacterTyped)
    val internalState = state ?: remember { RichEditorState() }
    internalState.onFormatted = latestChange
    val editorTextColor = textColor.toArgb()
    val editorHintColor = hintColor.toArgb()

    val androidEditor: @Composable (Modifier) -> Unit = { mod ->
        AndroidView(
            modifier = mod,
            factory = { context ->
                RichEditText(context).apply {
                    if (seamless) {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        configurePadding(horizontalDp = 0, topDp = 6, bottomDp = 18)
                    } else {
                        configurePadding(horizontalDp = 12, topDp = 11, bottomDp = 11)
                    }
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.5f)
                    setTextColor(editorTextColor)
                    setHintTextColor(editorHintColor)
                    hint = "Начните писать заметку…"
                    internalState.lastCallbackValue = value
                    loadRichText(value, clearPending = true)
                    onRichTextChanged = { updated ->
                        internalState.lastCallbackValue = updated
                        internalState.syncFlags()
                        latestChange(updated)
                    }
                    onSelectionChanged = { internalState.syncFlags() }
                    this.onCharacterTyped = { typed, target -> latestCharTyped?.invoke(typed, target) }
                    internalState.editText = this
                    internalState.syncFlags()
                }
            },
            update = { editor ->
                if (editor.currentTextColor != editorTextColor) {
                    editor.setTextColor(editorTextColor)
                }
                if (editor.currentHintTextColor != editorHintColor) {
                    editor.setHintTextColor(editorHintColor)
                }
                val canonicalIncoming = RichTextDocument.canonical(value)
                val canonicalLast = RichTextDocument.canonical(internalState.lastCallbackValue)
                if (canonicalIncoming != canonicalLast && canonicalIncoming != RichTextDocument.canonical(editor.currentRichText())) {
                    internalState.lastCallbackValue = value
                    val selectionStart = editor.selectionStart.coerceAtLeast(0)
                    val selectionEnd = editor.selectionEnd.coerceAtLeast(0)
                    editor.loadRichText(value, clearPending = false)
                    editor.setSelection(
                        selectionStart.coerceAtMost(editor.length()),
                        selectionEnd.coerceAtMost(editor.length())
                    )
                }
                editor.onRichTextChanged = { updated ->
                    internalState.lastCallbackValue = updated
                    internalState.syncFlags()
                    latestChange(updated)
                }
                editor.onSelectionChanged = { internalState.syncFlags() }
                editor.onCharacterTyped = { typed, target -> latestCharTyped?.invoke(typed, target) }
                internalState.editText = editor
            }
        )
    }

    if (seamless) {
        androidEditor(Modifier.fillMaxWidth().heightIn(min = editorHeight))
    } else {
        Column {
            if (label.isNotBlank()) {
                Text(
                    label,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    RichFormatBar(state = internalState)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                    androidEditor(Modifier.fillMaxWidth().height(editorHeight))
                }
            }
        }
    }
}