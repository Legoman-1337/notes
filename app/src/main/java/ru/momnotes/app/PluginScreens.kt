package ru.momnotes.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
private fun PluginShirtIcon(color: Color, modifier: Modifier = Modifier) {
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

/**
 * Экран плагина: прозрачный стилизованный WebView поверх живого/статичного фона NoteThemeCanvas.
 * Вверху доступна кнопка выбора фона (футболка), как и внутри заметок.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun PluginHostScreen(meta: PluginMeta, settings: AppSettings, onClose: () -> Unit) {
    val context = LocalContext.current
    var pluginThemeId by remember(meta.id) { mutableStateOf(PluginManager.pluginTheme(context, meta.id)) }
    var showThemePicker by remember { mutableStateOf(false) }
    val themeColors = pluginThemeColors(settings, pluginThemeId)
    val noteTheme = remember(pluginThemeId) { NoteThemes.find(pluginThemeId) }

    val headerColor = noteTheme?.surfaceTint ?: roleColor(settings.headerColor, MaterialTheme.colorScheme.surface)
    val headerContentColor = noteTheme?.textColor ?: contrastColor(headerColor)
    val headerAccentColor = noteTheme?.accentColor ?: themeColors.accent
    val topBtnBg = if (noteTheme == null) {
        headerContentColor.copy(alpha = 0.12f)
    } else if (noteTheme.isDark) {
        Color.Black.copy(alpha = 0.28f)
    } else {
        Color.White.copy(alpha = 0.72f)
    }

    BackHandler(enabled = showThemePicker) {
        showThemePicker = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NoteThemeCanvas(
            themeId = pluginThemeId,
            modifier = Modifier.matchParentSize(),
            animated = settings.animateNoteThemes
        )
        if (noteTheme != null) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                noteTheme.surfaceTint.copy(alpha = if (noteTheme.isDark) 0.18f else 0.18f),
                                noteTheme.surfaceTint.copy(alpha = if (noteTheme.isDark) 0.28f else 0.28f)
                            )
                        )
                    )
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = headerColor.copy(alpha = if (noteTheme != null) 0.72f else 0.96f),
                shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
                border = BorderStroke(1.dp, headerContentColor.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = topBtnBg,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onClose)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = headerContentColor, modifier = Modifier.size(19.dp))
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(headerAccentColor.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(meta.icon, fontSize = 19.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            meta.name,
                            color = headerContentColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Плагин · v${meta.version}",
                            color = headerContentColor.copy(alpha = 0.70f),
                            fontSize = 11.sp
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (showThemePicker) headerAccentColor.copy(alpha = 0.28f) else topBtnBg,
                        border = BorderStroke(1.dp, if (showThemePicker) headerAccentColor else headerContentColor.copy(alpha = 0.16f)),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { showThemePicker = !showThemePicker }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            PluginShirtIcon(color = if (showThemePicker) headerAccentColor else headerContentColor)
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            val ws = this.settings
                            ws.javaScriptEnabled = true
                            ws.domStorageEnabled = true
                            ws.allowFileAccess = true
                            webViewClient = WebViewClient()
                            tag = pluginThemeId
                            addJavascriptInterface(PluginBridge(ctx, meta.id, onClose), "MomHost")
                            loadDataWithBaseURL(
                                "file://" + meta.directory.absolutePath.trimEnd('/') + "/",
                                pluginShellHtml(meta, themeColors),
                                "text/html",
                                "utf-8",
                                null
                            )
                        }
                    },
                    update = { webView ->
                        if (webView.tag != pluginThemeId) {
                            webView.tag = pluginThemeId
                            webView.evaluateJavascript(pluginThemeCssUpdateScript(themeColors), null)
                        }
                    }
                )
            }

            AnimatedVisibility(
                visible = showThemePicker,
                enter = slideInVertically { it } + expandVertically() + fadeIn(),
                exit = slideOutVertically { it } + shrinkVertically() + fadeOut()
            ) {
                NoteThemePickerBar(
                    selectedId = pluginThemeId,
                    animated = settings.animateNoteThemes,
                    onSelect = { chosen ->
                        pluginThemeId = chosen
                        PluginManager.setPluginTheme(context, meta.id, chosen)
                    }
                )
            }
        }
    }
}

/** Цвета темы, передаваемые в CSS плагина. */
private data class PluginThemeColors(
    val bg: Color,
    val cardBg: String,
    val surface2: String,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    val border: String,
    val danger: Color
)

@Composable
private fun pluginThemeColors(settings: AppSettings, pluginThemeId: String): PluginThemeColors {
    val noteTheme = NoteThemes.find(pluginThemeId)
    val appAccent = roleColor(settings.sectionColor, accentColor(settings.accent))
    if (noteTheme != null) {
        val card = if (noteTheme.isDark) "rgba(14, 18, 22, 0.56)" else "rgba(255, 255, 255, 0.78)"
        val surf2 = if (noteTheme.isDark) "rgba(255, 255, 255, 0.11)" else "rgba(0, 0, 0, 0.06)"
        val border = if (noteTheme.isDark) "rgba(255, 255, 255, 0.16)" else "rgba(0, 0, 0, 0.10)"
        return PluginThemeColors(
            bg = noteTheme.surfaceTint,
            cardBg = card,
            surface2 = surf2,
            text = noteTheme.textColor,
            muted = noteTheme.mutedColor,
            accent = noteTheme.accentColor,
            onAccent = contrastColor(noteTheme.accentColor),
            border = border,
            danger = MaterialTheme.colorScheme.error
        )
    }
    val dark = contrastColor(MaterialTheme.colorScheme.background) == Color.White
    val surf = MaterialTheme.colorScheme.surface
    val card = if (dark) {
        "rgba(${(surf.red * 255).toInt()}, ${(surf.green * 255).toInt()}, ${(surf.blue * 255).toInt()}, 0.88)"
    } else {
        "rgba(255, 255, 255, 0.92)"
    }
    val s2 = MaterialTheme.colorScheme.surfaceVariant
    val surf2 = "rgba(${(s2.red * 255).toInt()}, ${(s2.green * 255).toInt()}, ${(s2.blue * 255).toInt()}, 0.85)"
    val border = if (dark) "rgba(255, 255, 255, 0.10)" else "rgba(0, 0, 0, 0.08)"
    return PluginThemeColors(
        bg = MaterialTheme.colorScheme.background,
        cardBg = card,
        surface2 = surf2,
        text = MaterialTheme.colorScheme.onBackground,
        muted = MaterialTheme.colorScheme.onSurfaceVariant,
        accent = appAccent,
        onAccent = contrastColor(appAccent),
        border = border,
        danger = MaterialTheme.colorScheme.error
    )
}

private fun colorToCssHex(color: Color): String =
    String.format(java.util.Locale.US, "#%06X", color.toArgb() and 0xFFFFFF)

private fun pluginThemeCssUpdateScript(colors: PluginThemeColors): String {
    return """
        (function(){
          var s = document.documentElement.style;
          s.setProperty('--bg', '${colorToCssHex(colors.bg)}');
          s.setProperty('--surface', '${colors.cardBg}');
          s.setProperty('--surface2', '${colors.surface2}');
          s.setProperty('--text', '${colorToCssHex(colors.text)}');
          s.setProperty('--muted', '${colorToCssHex(colors.muted)}');
          s.setProperty('--accent', '${colorToCssHex(colors.accent)}');
          s.setProperty('--on-accent', '${colorToCssHex(colors.onAccent)}');
          s.setProperty('--border', '${colors.border}');
          s.setProperty('--danger', '${colorToCssHex(colors.danger)}');
        })();
    """.trimIndent()
}

private fun pluginShellHtml(meta: PluginMeta, colors: PluginThemeColors): String {
    return """
<!doctype html>
<html lang="ru">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
:root {
  --bg: ${colorToCssHex(colors.bg)};
  --surface: ${colors.cardBg};
  --surface2: ${colors.surface2};
  --text: ${colorToCssHex(colors.text)};
  --muted: ${colorToCssHex(colors.muted)};
  --accent: ${colorToCssHex(colors.accent)};
  --on-accent: ${colorToCssHex(colors.onAccent)};
  --border: ${colors.border};
  --danger: ${colorToCssHex(colors.danger)};
}
* { box-sizing: border-box; -webkit-tap-highlight-color: transparent; }
html, body { margin: 0; padding: 0; background: transparent; min-height: 100%; }
body {
  background: transparent;
  color: var(--text);
  padding: 14px 14px 36px;
  font-family: -apple-system, 'Segoe UI', Roboto, sans-serif;
  font-size: 15px;
  line-height: 1.45;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}
h2 { font-size: 18px; font-weight: 700; margin: 4px 0 12px; letter-spacing: -0.2px; }
.card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 15px;
  margin-bottom: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.10);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  animation: cardIn 0.22s ease-out;
}
.hero {
  text-align: center;
  padding: 22px 16px;
}
.hero-icon {
  font-size: 44px;
  line-height: 1.1;
  margin-bottom: 8px;
}
.hero-num {
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -0.5px;
}
.muted { color: var(--muted); }
.row { display: flex; gap: 8px; align-items: center; }
.grow { flex: 1; min-width: 0; }
.btn {
  background: var(--accent);
  color: var(--on-accent);
  border: none;
  border-radius: 14px;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.12s ease, opacity 0.15s ease;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.12);
}
.btn:active { transform: scale(0.96); }
.btn:disabled { opacity: 0.45; }
.btn.secondary {
  background: var(--surface2);
  color: var(--text);
  border: 1px solid var(--border);
  font-weight: 500;
  box-shadow: none;
}
.btn.danger {
  background: transparent;
  color: var(--danger);
  font-weight: 600;
  box-shadow: none;
}
.btn.small { padding: 7px 12px; font-size: 13px; border-radius: 11px; }
input, select, textarea {
  min-width: 0;
  background: var(--surface2);
  color: var(--text);
  border: 1px solid var(--border);
  border-radius: 13px;
  padding: 10px 12px;
  font-size: 15px;
  font-family: inherit;
  outline: none;
  transition: border-color 0.15s ease;
}
input:focus, select:focus, textarea:focus {
  border-color: var(--accent);
}
.chip {
  background: var(--surface2);
  color: var(--text);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 7px 12px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}
.chip.active {
  background: var(--accent);
  color: var(--on-accent);
  border-color: var(--accent);
  font-weight: 600;
}
.prog-track {
  height: 10px;
  background: var(--surface2);
  border-radius: 99px;
  overflow: hidden;
  border: 1px solid var(--border);
}
.prog-fill {
  height: 100%;
  background: var(--accent);
  border-radius: 99px;
  transition: width 0.25s ease;
}
table { border-collapse: collapse; width: 100%; border-radius: 12px; overflow: hidden; }
th, td { border: 1px solid var(--border); padding: 6px 8px; font-size: 12px; text-align: center; }
th { color: var(--muted); font-weight: 600; background: var(--surface2); }
.atttable th, .atttable td { padding: 3px; font-size: 11px; }
.atttable .namecol {
  min-width: 96px; max-width: 130px; text-align: left; padding-left: 8px;
  position: sticky; left: 0; background: var(--surface); font-size: 12px; z-index: 1;
}
.atttable th.namecol { background: var(--surface2); }
.atttable .day { width: 30px; height: 32px; }
.day {
  width: 26px; height: 30px; border-radius: 9px; border: 1px solid var(--border);
  background: var(--surface2); color: var(--muted); font-size: 11px; padding: 0; cursor: pointer;
}
.day.on { background: var(--accent); color: var(--on-accent); border-color: var(--accent); font-weight: 700; }
.money { font-variant-numeric: tabular-nums; }
.error { color: var(--danger); white-space: pre-wrap; }
</style>
</head>
<body>
<div id="root"><p class="muted">Загрузка плагина…</p></div>
<script>
window.MomApp = {
  load: function () { return MomHost.load(); },
  save: function (json) { MomHost.save(String(json)); },
  toast: function (text) { MomHost.toast(String(text)); },
  close: function () { MomHost.close(); }
};
</script>
<script src="${meta.main}"></script>
<script>
(function () {
  var root = document.getElementById('root');
  try {
    var api = window.MomApp;
    if (typeof activate === 'function') { activate(api); }
    else if (window.plugin && typeof window.plugin.activate === 'function') { window.plugin.activate(api); }
    else { root.innerHTML = '<p class="muted">Плагин не определил activate(app)</p>'; }
  } catch (e) {
    root.innerHTML = '<p class="error">Ошибка плагина: ' + ((e && e.stack) || e) + '</p>';
  }
  window.addEventListener('pagehide', function () {
    try { if (typeof deactivate === 'function') deactivate(); } catch (_) {}
  });
})();
</script>
</body>
</html>
""".trimIndent()
}

/** Мост между plugin.js (JS в WebView) и приложением. Вызывается из фонового потока WebView. */
private class PluginBridge(
    private val context: Context,
    private val pluginId: String,
    private val onClose: () -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun load(): String {
        val file = PluginManager.dataFile(context, pluginId)
        return if (file.isFile) {
            try { file.readText(Charsets.UTF_8) } catch (_: Exception) { "{}" }
        } else "{}"
    }

    @JavascriptInterface
    fun save(json: String) {
        try {
            PluginManager.dataFile(context, pluginId).writeText(json, Charsets.UTF_8)
        } catch (_: Exception) {
        }
    }

    @JavascriptInterface
    fun toast(text: String) {
        mainHandler.post { Toast.makeText(context, text, Toast.LENGTH_SHORT).show() }
    }

    @JavascriptInterface
    fun close() {
        mainHandler.post { onClose() }
    }
}

private enum class PluginStoreSection(
    val title: String,
    val subtitle: String,
    val icon: String,
    val tint: Color
) {
    RECOMMENDED("Рекомендуемые", "Главные плагины из каталога", "⭐", Color(0xFF3E8E5A)),
    EXTRA("Полезное офлайн", "Инструменты для дома, дел и здоровья", "🧰", Color(0xFF5D6CE6)),
    PACKAGES("Пакеты", "Наборы плагинов одной кнопкой", "📦", Color(0xFFD97B38)),
    MINE("Мои плагины", "Установленные и активные сейчас", "🧩", Color(0xFF2889B8))
}

@Composable
private fun StoreHintCard(text: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PluginHubTile(
    section: PluginStoreSection,
    badgeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardSurface = mixColors(MaterialTheme.colorScheme.surface, section.tint, 0.14f)
    val borderColor = mixColors(MaterialTheme.colorScheme.outlineVariant, section.tint, 0.35f)
    val shape = RoundedCornerShape(24.dp)
    Surface(
        modifier = modifier
            .height(206.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = cardSurface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.55f))
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            section.tint.copy(alpha = 0.16f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(section.tint.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(section.icon, fontSize = 26.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = section.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = section.subtitle,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = section.tint.copy(alpha = 0.20f)
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendedTab(
    entries: List<RegistryEntry>?,
    loading: Boolean,
    errorText: String?,
    query: String,
    accent: Color,
    busyId: String?,
    onRefresh: () -> Unit,
    onInstall: (RegistryEntry) -> Unit
) {
    val context = LocalContext.current
    val normalized = query.trim().trimStart('@').lowercase()
    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        errorText != null -> Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Каталог недоступен", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(errorText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRefresh, shape = RoundedCornerShape(12.dp)) { Text("Повторить") }
        }
        else -> {
            val list = entries.orEmpty().filter { entry ->
                normalized.isBlank() ||
                        entry.id.lowercase().contains(normalized) ||
                        entry.name.lowercase().contains(normalized) ||
                        entry.description.lowercase().contains(normalized)
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(list, key = { it.id }) { entry ->
                    val installed = PluginManager.isInstalled(context, entry.id)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth().animateItem(
                            fadeInSpec = tween(200),
                            placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(44.dp).clip(RoundedCornerShape(13.dp))
                                        .background(accent.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) { Text(entry.icon, fontSize = 22.sp) }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(entry.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        "@${entry.id}" + if (entry.version.isNotBlank()) " · v${entry.version}" else "",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (installed) {
                                    Surface(shape = RoundedCornerShape(10.dp), color = accent.copy(alpha = 0.14f)) {
                                        Text("Установлено ✓", modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp), color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                } else {
                                    Button(
                                        onClick = { onInstall(entry) },
                                        enabled = busyId == null,
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        if (busyId == entry.id) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        } else {
                                            Text("Скачать", fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                            if (entry.description.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(entry.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (entry.author.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text("Автор: ${entry.author}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                if (list.isEmpty()) {
                    item {
                        Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ничего не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Попробуйте @id плагина", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtraPluginsTab(
    extras: List<PluginMeta>,
    query: String,
    accent: Color,
    busyId: String?,
    onInstall: (PluginMeta) -> Unit
) {
    val context = LocalContext.current
    val normalized = query.trim().trimStart('@').lowercase()
    val list = extras
        .filter {
            normalized.isBlank() || it.id.lowercase().contains(normalized) ||
                    it.name.lowercase().contains(normalized) || it.description.lowercase().contains(normalized)
        }
        .sortedWith(compareBy({ it.id != "fairy" }, { it.name.lowercase() }))
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            StoreHintCard("Только полезные инструменты для жизни, бюджета, здоровья и дома — работают без интернета и поддерживают живые фоны.")
        }
        items(list, key = { it.id }) { meta ->
            val installed = PluginManager.isInstalled(context, meta.id)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth().animateItem(fadeInSpec = tween(180))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(44.dp).clip(RoundedCornerShape(13.dp))
                                .background(if (installed) accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) { Text(meta.icon, fontSize = 22.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(meta.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("@${meta.id}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (installed) {
                            Surface(shape = RoundedCornerShape(10.dp), color = accent.copy(alpha = 0.14f)) {
                                Text("Установлено ✓", modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp), color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Button(
                                onClick = { onInstall(meta) },
                                enabled = busyId == null,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                if (busyId == meta.id) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Установить", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    if (meta.description.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(meta.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (list.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ничего не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Попробуйте @id плагина", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PackageListTab(
    packages: List<ExtraPackage>,
    extras: List<PluginMeta>,
    query: String,
    accent: Color,
    installingPkg: String?,
    onInstall: (ExtraPackage) -> Unit
) {
    val context = LocalContext.current
    val normalized = query.trim().trimStart('@').lowercase()
    val list = packages.filter {
        normalized.isBlank() || it.name.lowercase().contains(normalized) || it.description.lowercase().contains(normalized)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            StoreHintCard("Пакет устанавливает сразу набор совместимых плагинов одной кнопкой.")
        }
        items(list, key = { it.id }) { pkg ->
            val names = pkg.pluginIds.mapNotNull { id -> extras.firstOrNull { it.id == id }?.name }
            val installedCount = pkg.pluginIds.count { PluginManager.isInstalled(context, it) }
            val allInstalled = installedCount == pkg.pluginIds.size
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth().animateItem(fadeInSpec = tween(180))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                                .background(if (allInstalled) accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) { Text(pkg.icon, fontSize = 23.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(pkg.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${pkg.pluginIds.size} плагинов · установлено $installedCount",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (pkg.description.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(pkg.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        names.joinToString(" · "),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onInstall(pkg) },
                        enabled = !allInstalled && installingPkg == null,
                        shape = RoundedCornerShape(13.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        if (installingPkg == pkg.id) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (allInstalled) "Установлено ✓" else "Установить пакет", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
        if (list.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ничего не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun MyPluginsTab(
    plugins: List<PluginMeta>,
    query: String,
    accent: Color,
    onPluginsChanged: () -> Unit,
    onOpenPlugin: (PluginMeta) -> Unit
) {
    val context = LocalContext.current
    val normalized = query.trim().trimStart('@').lowercase()
    val filtered = plugins.filter {
        normalized.isBlank() || it.id.lowercase().contains(normalized) ||
                it.name.lowercase().contains(normalized) || it.description.lowercase().contains(normalized)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(filtered, key = { it.id }) { meta ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth().animateItem(fadeInSpec = tween(200), fadeOutSpec = tween(160))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(44.dp).clip(RoundedCornerShape(13.dp))
                                .background(if (meta.enabled) accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) { Text(meta.icon, fontSize = 22.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(meta.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("@${meta.id} · v${meta.version}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = meta.enabled, onCheckedChange = { on ->
                            PluginManager.setEnabled(context, meta.id, on)
                            onPluginsChanged()
                        })
                    }
                    if (meta.description.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(meta.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onOpenPlugin(meta) },
                            enabled = meta.enabled,
                            shape = RoundedCornerShape(13.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 7.dp)
                        ) { Text("Открыть", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                        TextButton(
                            onClick = {
                                PluginManager.uninstall(context, meta.id)
                                onPluginsChanged()
                                Toast.makeText(context, "Плагин удалён", Toast.LENGTH_SHORT).show()
                            }
                        ) { Text("Удалить", color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
                    }
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Пока нет установленных плагинов", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Откройте «Рекомендуемые» или «Полезное офлайн»", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun HubSearchResults(
    query: String,
    plugins: List<PluginMeta>,
    entries: List<RegistryEntry>,
    extras: List<PluginMeta>,
    accent: Color,
    busyId: String?,
    onInstallRegistry: (RegistryEntry) -> Unit,
    onInstallExtra: (PluginMeta) -> Unit,
    onOpenPlugin: (PluginMeta) -> Unit
) {
    val context = LocalContext.current
    val q = query.trim().trimStart('@').lowercase()
    val installedMatches = plugins.filter {
        it.id.lowercase().contains(q) || it.name.lowercase().contains(q) || it.description.lowercase().contains(q)
    }
    val installedIds = plugins.mapTo(mutableSetOf()) { it.id }
    val registryMatches = entries.filter {
        it.id !in installedIds && (it.id.lowercase().contains(q) || it.name.lowercase().contains(q) || it.description.lowercase().contains(q))
    }
    val extraMatches = extras.filter {
        it.id !in installedIds && (it.id.lowercase().contains(q) || it.name.lowercase().contains(q) || it.description.lowercase().contains(q))
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (installedMatches.isNotEmpty()) {
            item { Text("Мои плагины", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(installedMatches, key = { "my_${it.id}" }) { meta ->
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().animateItem()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                            Text(meta.icon, fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(meta.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("@${meta.id}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = { onOpenPlugin(meta) }, enabled = meta.enabled, shape = RoundedCornerShape(12.dp)) {
                            Text("Открыть", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
        if (registryMatches.isNotEmpty()) {
            item { Text("Рекомендуемые", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(registryMatches, key = { "reg_${it.id}" }) { entry ->
                val installed = PluginManager.isInstalled(context, entry.id)
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().animateItem()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                            Text(entry.icon, fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("@${entry.id}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (installed) {
                            Text("Установлено ✓", color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Button(onClick = { onInstallRegistry(entry) }, enabled = busyId == null, shape = RoundedCornerShape(12.dp)) {
                                Text(if (busyId == entry.id) "…" else "Скачать", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
        if (extraMatches.isNotEmpty()) {
            item { Text("Полезное офлайн", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(extraMatches, key = { "ext_${it.id}" }) { meta ->
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().animateItem()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                            Text(meta.icon, fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(meta.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("@${meta.id}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = { onInstallExtra(meta) }, enabled = busyId == null, shape = RoundedCornerShape(12.dp)) {
                            Text(if (busyId == meta.id) "…" else "Установить", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
        if (installedMatches.isEmpty() && registryMatches.isEmpty() && extraMatches.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ничего не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Попробуйте другой запрос", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * Магазин плагинов: главный экран с 4 крупными карточками (2×2) + быстрый поиск.
 */
@Composable
internal fun PluginStoreScreen(
    settings: AppSettings,
    plugins: List<PluginMeta>,
    onPluginsChanged: () -> Unit,
    onOpenPlugin: (PluginMeta) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var activeSection by remember { mutableStateOf<PluginStoreSection?>(null) }
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf<List<RegistryEntry>?>(null) }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var busyId by remember { mutableStateOf<String?>(null) }
    var urlDraft by remember { mutableStateOf(PluginManager.registryUrl(context)) }
    var showUrlEditor by remember { mutableStateOf(false) }
    var installingPkg by remember { mutableStateOf<String?>(null) }
    val extraList = remember(plugins) {
        ExtraCatalog.PLUGIN_IDS.mapNotNull { PluginManager.readBundledExtra(context, it) }
    }

    BackHandler(enabled = activeSection != null || query.isNotBlank()) {
        if (activeSection != null) {
            activeSection = null
            query = ""
        } else {
            query = ""
        }
    }

    fun refresh() {
        scope.launch {
            loading = true
            errorText = null
            try {
                val fetched = withContext(Dispatchers.IO) { PluginManager.fetchRegistry(urlDraft.trim()) }
                entries = fetched
            } catch (e: Exception) {
                entries = emptyList()
                errorText = e.message ?: "Не удалось загрузить каталог"
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    fun install(entry: RegistryEntry) {
        if (busyId != null) return
        scope.launch {
            busyId = entry.id
            try {
                withContext(Dispatchers.IO) {
                    val bytes = PluginManager.download(entry.downloadUrl)
                    PluginManager.install(context, entry.id, bytes)
                }
                onPluginsChanged()
                Toast.makeText(context, "Плагин «${entry.name}» установлен", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка установки: ${e.message}", Toast.LENGTH_LONG).show()
            }
            busyId = null
        }
    }

    fun installExtra(meta: PluginMeta) {
        if (busyId != null) return
        scope.launch {
            busyId = meta.id
            try {
                withContext(Dispatchers.IO) { PluginManager.installBundledExtra(context, meta.id) }
                onPluginsChanged()
                Toast.makeText(context, "Плагин «${meta.name}» установлен", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка установки: ${e.message}", Toast.LENGTH_LONG).show()
            }
            busyId = null
        }
    }

    fun installPackage(pkg: ExtraPackage) {
        if (busyId != null || installingPkg != null) return
        scope.launch {
            installingPkg = pkg.id
            try {
                val n = withContext(Dispatchers.IO) { PluginManager.installExtraPackage(context, pkg) }
                onPluginsChanged()
                Toast.makeText(
                    context,
                    if (n > 0) "Пакет «${pkg.name}»: установлено плагинов — $n" else "Все плагины пакета уже установлены",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка установки: ${e.message}", Toast.LENGTH_LONG).show()
            }
            installingPkg = null
        }
    }

    val accent = roleColor(settings.sectionColor, accentColor(settings.accent))
    val headerColor = roleColor(settings.headerColor, mixColors(MaterialTheme.colorScheme.surface, accent, 0.14f))
    val headerBottom = mixColors(headerColor, Color.Black, 0.16f)
    val headerContentColor = contrastColor(headerColor)
    val headerShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, headerShape, clip = false)
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 12.dp, top = 12.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = headerContentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(38.dp).clip(CircleShape).clickable {
                        if (activeSection != null) {
                            activeSection = null
                            query = ""
                        } else {
                            onClose()
                        }
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = headerContentColor, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = activeSection?.title ?: "Плагины",
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                        label = "store-header-title"
                    ) { title ->
                        Text(
                            text = title,
                            color = headerContentColor,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = activeSection?.subtitle ?: "Полезные дополнения и фоны для них",
                        color = headerContentColor.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = headerContentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(38.dp).clip(CircleShape).clickable { showUrlEditor = !showUrlEditor }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Create, contentDescription = "Адрес каталога", tint = headerContentColor, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("@id или название плагина") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, contentDescription = "Очистить") }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            AnimatedVisibility(
                visible = showUrlEditor,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(Modifier.padding(top = 8.dp)) {
                    Text("Адрес каталога (index.json на GitHub)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = urlDraft,
                            onValueChange = { urlDraft = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick = {
                                PluginManager.setRegistryUrl(context, urlDraft)
                                refresh()
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("ОК") }
                    }
                }
            }
        }

        AnimatedContent(
            targetState = activeSection,
            modifier = Modifier.fillMaxWidth().weight(1f),
            transitionSpec = {
                if (targetState != null) {
                    (fadeIn(tween(220)) + slideInHorizontally(tween(240, easing = FastOutSlowInEasing)) { it / 6 }) togetherWith
                            fadeOut(tween(140))
                } else {
                    fadeIn(tween(200)) togetherWith
                            (fadeOut(tween(150)) + slideOutHorizontally(tween(220, easing = FastOutSlowInEasing)) { it / 6 })
                }
            },
            label = "plugin-store-nav"
        ) { section ->
            when (section) {
                null -> {
                    if (query.isNotBlank()) {
                        HubSearchResults(
                            query = query,
                            plugins = plugins,
                            entries = entries.orEmpty(),
                            extras = extraList,
                            accent = accent,
                            busyId = busyId,
                            onInstallRegistry = { install(it) },
                            onInstallExtra = { installExtra(it) },
                            onOpenPlugin = onOpenPlugin
                        )
                    } else {
                        val recCount = entries?.size
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PluginHubTile(
                                    section = PluginStoreSection.RECOMMENDED,
                                    badgeText = when {
                                        loading -> "Загрузка…"
                                        recCount != null && recCount > 0 -> "$recCount в каталоге"
                                        else -> "Онлайн-каталог"
                                    },
                                    onClick = { activeSection = PluginStoreSection.RECOMMENDED },
                                    modifier = Modifier.weight(1f)
                                )
                                PluginHubTile(
                                    section = PluginStoreSection.EXTRA,
                                    badgeText = "${extraList.size} без сети",
                                    onClick = { activeSection = PluginStoreSection.EXTRA },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PluginHubTile(
                                    section = PluginStoreSection.PACKAGES,
                                    badgeText = "${ExtraCatalog.PACKAGES.size} наборов",
                                    onClick = { activeSection = PluginStoreSection.PACKAGES },
                                    modifier = Modifier.weight(1f)
                                )
                                PluginHubTile(
                                    section = PluginStoreSection.MINE,
                                    badgeText = "Установлено: ${plugins.size}",
                                    onClick = { activeSection = PluginStoreSection.MINE },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                PluginStoreSection.RECOMMENDED -> RecommendedTab(
                    entries = entries,
                    loading = loading,
                    errorText = errorText,
                    query = query,
                    accent = accent,
                    busyId = busyId,
                    onRefresh = { refresh() },
                    onInstall = { install(it) }
                )
                PluginStoreSection.EXTRA -> ExtraPluginsTab(
                    extras = extraList,
                    query = query,
                    accent = accent,
                    busyId = busyId,
                    onInstall = { installExtra(it) }
                )
                PluginStoreSection.PACKAGES -> PackageListTab(
                    packages = ExtraCatalog.PACKAGES,
                    extras = extraList,
                    query = query,
                    accent = accent,
                    installingPkg = installingPkg,
                    onInstall = { installPackage(it) }
                )
                PluginStoreSection.MINE -> MyPluginsTab(
                    plugins = plugins,
                    query = query,
                    accent = accent,
                    onPluginsChanged = onPluginsChanged,
                    onOpenPlugin = onOpenPlugin
                )
            }
        }
    }
}