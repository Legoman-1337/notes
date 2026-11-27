package ru.momnotes.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

internal data class UpdateReleaseInfo(
    val tagName: String,
    val title: String,
    val changelog: String,
    val htmlUrl: String,
    val apkUrl: String?,
    val apkSizeBytes: Long,
    val publishedAt: String
)

internal data class UpdateCheckState(
    val latestRelease: UpdateReleaseInfo? = null,
    val newerCount: Int = 0,
    val lastCheckedAt: Long = 0L,
    val badgeHidden: Boolean = false,
    val hiddenForTag: String = ""
) {
    val visibleBadgeCount: Int
        get() = if (newerCount > 0 && !badgeHidden && latestRelease?.tagName != hiddenForTag) newerCount else 0
}

internal object AppUpdateManager {
    const val REPO_API_URL = "https://api.github.com/repos/Legoman-1337/notes/releases?per_page=15"
    const val REPO_WEB_URL = "https://github.com/Legoman-1337/notes/releases"
    const val SITE_DOWNLOAD_URL = "https://legoman-1337.github.io/notes/"

    private const val PREFS = "mom_notes_updates"
    private const val KEY_LAST_CHECK = "last_check_ms"
    private const val KEY_NEWER_COUNT = "newer_count"
    private const val KEY_TAG = "latest_tag"
    private const val KEY_TITLE = "latest_title"
    private const val KEY_BODY = "latest_body"
    private const val KEY_HTML = "latest_html"
    private const val KEY_APK = "latest_apk"
    private const val KEY_APK_SIZE = "latest_apk_size"
    private const val KEY_PUBLISHED = "latest_published"
    private const val KEY_BADGE_HIDDEN = "badge_hidden"
    private const val KEY_HIDDEN_FOR_TAG = "hidden_for_tag"

    // Тихая автопроверка не чаще раза в 6 часов, чтобы не грузить сеть
    private const val CHECK_INTERVAL_MS = 6L * 60L * 60L * 1000L

    fun readState(context: Context): UpdateCheckState {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val tag = prefs.getString(KEY_TAG, "").orEmpty()
        val release = if (tag.isNotBlank() && compareVersions(tag, BuildConfig.VERSION_NAME) > 0) {
            UpdateReleaseInfo(
                tagName = tag,
                title = prefs.getString(KEY_TITLE, tag).orEmpty().ifBlank { tag },
                changelog = prefs.getString(KEY_BODY, "").orEmpty(),
                htmlUrl = prefs.getString(KEY_HTML, REPO_WEB_URL).orEmpty().ifBlank { REPO_WEB_URL },
                apkUrl = prefs.getString(KEY_APK, null)?.takeIf { it.isNotBlank() },
                apkSizeBytes = prefs.getLong(KEY_APK_SIZE, 0L),
                publishedAt = prefs.getString(KEY_PUBLISHED, "").orEmpty()
            )
        } else {
            null
        }
        val count = if (release != null) prefs.getInt(KEY_NEWER_COUNT, 1).coerceAtLeast(1) else 0
        return UpdateCheckState(
            latestRelease = release,
            newerCount = count,
            lastCheckedAt = prefs.getLong(KEY_LAST_CHECK, 0L),
            badgeHidden = prefs.getBoolean(KEY_BADGE_HIDDEN, false),
            hiddenForTag = prefs.getString(KEY_HIDDEN_FOR_TAG, "").orEmpty()
        )
    }

    fun setBadgeHidden(context: Context, hidden: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BADGE_HIDDEN, hidden).apply()
    }

    fun dismissCurrentBadge(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val currentTag = prefs.getString(KEY_TAG, "").orEmpty()
        if (currentTag.isNotBlank()) {
            prefs.edit().putString(KEY_HIDDEN_FOR_TAG, currentTag).apply()
        }
    }

    fun shouldAutoCheck(context: Context): Boolean {
        val last = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_LAST_CHECK, 0L)
        return System.currentTimeMillis() - last >= CHECK_INTERVAL_MS
    }

    /**
     * Проверяет релизы на GitHub.
     * Если [silent] == true, любые сетевые/офлайн ошибки игнорируются без исключений.
     */
    suspend fun check(context: Context, silent: Boolean): Result<UpdateCheckState> = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(REPO_API_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "UdobnyeZametki/${BuildConfig.VERSION_NAME}")
            }
            try {
                val code = connection.responseCode
                if (code !in 200..299) {
                    val err = IllegalStateException("GitHub ответил кодом $code")
                    return@withContext if (silent) Result.success(readState(context)) else Result.failure(err)
                }
                val raw = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val array = JSONArray(raw)
                val releases = (0 until array.length()).mapNotNull { idx ->
                    val item = array.optJSONObject(idx) ?: return@mapNotNull null
                    if (item.optBoolean("draft", false) || item.optBoolean("prerelease", false)) return@mapNotNull null
                    val tag = item.optString("tag_name").trim()
                    if (tag.isBlank()) return@mapNotNull null
                    val assets = item.optJSONArray("assets")
                    var apkUrl: String? = null
                    var apkSize = 0L
                    if (assets != null) {
                        for (a in 0 until assets.length()) {
                            val asset = assets.optJSONObject(a) ?: continue
                            val name = asset.optString("name").lowercase()
                            val url = asset.optString("browser_download_url")
                            if (name.endsWith(".apk") && url.isNotBlank()) {
                                apkUrl = url
                                apkSize = asset.optLong("size", 0L)
                                break
                            }
                        }
                    }
                    UpdateReleaseInfo(
                        tagName = tag,
                        title = item.optString("name").trim().ifBlank { tag },
                        changelog = item.optString("body").trim(),
                        htmlUrl = item.optString("html_url").trim().ifBlank { REPO_WEB_URL },
                        apkUrl = apkUrl,
                        apkSizeBytes = apkSize,
                        publishedAt = item.optString("published_at").take(10)
                    )
                }

                val newer = releases
                    .filter { compareVersions(it.tagName, BuildConfig.VERSION_NAME) > 0 }
                    .sortedWith { a, b -> compareVersions(b.tagName, a.tagName) }

                val latest = newer.firstOrNull()
                val now = System.currentTimeMillis()
                val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                val edit = prefs.edit()
                    .putLong(KEY_LAST_CHECK, now)
                    .putInt(KEY_NEWER_COUNT, newer.size)
                if (latest != null) {
                    edit.putString(KEY_TAG, latest.tagName)
                        .putString(KEY_TITLE, latest.title)
                        .putString(KEY_BODY, latest.changelog)
                        .putString(KEY_HTML, latest.htmlUrl)
                        .putString(KEY_APK, latest.apkUrl.orEmpty())
                        .putLong(KEY_APK_SIZE, latest.apkSizeBytes)
                        .putString(KEY_PUBLISHED, latest.publishedAt)
                } else {
                    edit.putString(KEY_TAG, "")
                        .putInt(KEY_NEWER_COUNT, 0)
                }
                edit.apply()
                Result.success(readState(context))
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            if (silent) Result.success(readState(context)) else Result.failure(e)
        }
    }

    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val folder = File(context.cacheDir, "shared_notes").apply { mkdirs() }
        val target = File(folder, "zametki-update.apk")
        val connection = (URL(apkUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10000
            readTimeout = 20000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "UdobnyeZametki/${BuildConfig.VERSION_NAME}")
        }
        try {
            val code = connection.responseCode
            check(code in 200..299) { "Ошибка загрузки: HTTP $code" }
            val total = connection.contentLengthLong.takeIf { it > 0L } ?: -1L
            connection.inputStream.use { input ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var downloaded = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (total > 0L) {
                            val p = (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                            withContext(Dispatchers.Main) { onProgress(p) }
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) { onProgress(1f) }
            target
        } finally {
            connection.disconnect()
        }
    }

    fun installApk(context: Context, apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            Toast.makeText(
                context,
                "Разрешите установку обновлений для «Удобных заметок» и нажмите снова",
                Toast.LENGTH_LONG
            ).show()
            val permIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(permIntent) }
            return
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }

    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
            .onFailure { Toast.makeText(context, "Не удалось открыть браузер", Toast.LENGTH_SHORT).show() }
    }

    internal fun compareVersions(a: String, b: String): Int {
        val cleanA = a.trim().removePrefix("v").removePrefix("V").substringBefore('-')
        val cleanB = b.trim().removePrefix("v").removePrefix("V").substringBefore('-')
        val partsA = cleanA.split('.').map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        val partsB = cleanB.split('.').map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        val maxLen = maxOf(partsA.size, partsB.size)
        for (i in 0 until maxLen) {
            val va = partsA.getOrElse(i) { 0 }
            val vb = partsB.getOrElse(i) { 0 }
            if (va != vb) return va.compareTo(vb)
        }
        return 0
    }
}

@Composable
internal fun UpdateArrowIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 19.dp
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = (minOf(w, h) * 0.105f).coerceAtLeast(1.5f)
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawLine(color, Offset(w * 0.50f, h * 0.16f), Offset(w * 0.50f, h * 0.64f), sw, StrokeCap.Round)
        val arrow = Path().apply {
            moveTo(w * 0.30f, h * 0.46f)
            lineTo(w * 0.50f, h * 0.66f)
            lineTo(w * 0.70f, h * 0.46f)
        }
        drawPath(arrow, color, style = stroke)
        val tray = Path().apply {
            moveTo(w * 0.20f, h * 0.68f)
            lineTo(w * 0.20f, h * 0.84f)
            lineTo(w * 0.80f, h * 0.84f)
            lineTo(w * 0.80f, h * 0.68f)
        }
        drawPath(tray, color, style = stroke)
    }
}

@Composable
internal fun UpdatesScreen(
    settings: AppSettings,
    state: UpdateCheckState,
    onStateUpdated: (UpdateCheckState) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var checkError by remember { mutableStateOf<String?>(null) }
    var downloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    val accent = accentColor(settings.accent)
    val headerColor = roleColor(settings.headerColor, mixColors(MaterialTheme.colorScheme.surface, accent, 0.14f))
    val headerBottom = mixColors(headerColor, Color.Black, 0.16f)
    val headerContent = contrastColor(headerColor)
    val headerShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)

    val pulseTransition = rememberInfiniteTransition(label = "upd-pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "upd-pulse-scale"
    )

    fun runManualCheck() {
        if (checking) return
        scope.launch {
            checking = true
            checkError = null
            val res = AppUpdateManager.check(context, silent = false)
            res.onSuccess { updated ->
                onStateUpdated(updated)
            }.onFailure {
                checkError = "Нет интернета или GitHub сейчас недоступен."
            }
            checking = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = headerContent.copy(alpha = 0.12f),
                    modifier = Modifier.size(38.dp).clip(CircleShape).clickable(onClick = onBack)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = headerContent, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Обновления", color = headerContent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Установлена версия ${BuildConfig.VERSION_NAME}",
                        color = headerContent.copy(alpha = 0.74f),
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = headerContent.copy(alpha = 0.14f),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !checking) { runManualCheck() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (checking) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = headerContent)
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = "Проверить", tint = headerContent, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val latest = state.latestRelease
            val hasUpdate = latest != null

            // Карточка статуса
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (hasUpdate) accent.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .graphicsLayer {
                                if (hasUpdate) {
                                    scaleX = pulseScale
                                    scaleY = pulseScale
                                }
                            }
                            .clip(CircleShape)
                            .background(
                                if (hasUpdate) Color(0xFFD93838).copy(alpha = 0.16f)
                                else accent.copy(alpha = 0.16f)
                            )
                            .border(
                                1.5.dp,
                                if (hasUpdate) Color(0xFFD93838).copy(alpha = 0.55f) else accent.copy(alpha = 0.45f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasUpdate) {
                            UpdateArrowIcon(Color(0xFFE54848), size = 30.dp)
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = accent, modifier = Modifier.size(30.dp))
                        }
                    }

                    Text(
                        text = if (hasUpdate) "Доступна версия ${latest!!.tagName}" else "У вас актуальная версия",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (hasUpdate) {
                            "Найдено новых выпусков: ${state.newerCount}. Можно скачать APK прямо здесь или открыть сайт."
                        } else {
                            "Текущая версия ${BuildConfig.VERSION_NAME} (сборка ${BuildConfig.VERSION_CODE}). Новых обновлений пока нет."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )

                    AnimatedVisibility(visible = checkError != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                checkError.orEmpty(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Button(
                        onClick = { runManualCheck() },
                        enabled = !checking,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        if (checking) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Проверяю…", fontSize = 13.sp)
                        } else {
                            Text("Проверить обновления", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Карточка релиза и способов скачивания
            if (latest != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(latest.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            if (latest.publishedAt.isNotBlank()) {
                                Surface(shape = RoundedCornerShape(9.dp), color = accent.copy(alpha = 0.14f)) {
                                    Text(
                                        latest.publishedAt,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        color = accent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        if (latest.changelog.isNotBlank()) {
                            Text(
                                latest.changelog,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }

                        if (downloading) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                LinearProgressIndicator(
                                    progress = { downloadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape)
                                )
                                Text(
                                    "Загрузка APK: ${(downloadProgress * 100).toInt()}%",
                                    color = accent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        val apkUrl = latest.apkUrl
                        if (apkUrl != null) {
                            val sizeMb = if (latest.apkSizeBytes > 0L) {
                                String.format(java.util.Locale.US, " (%.1f МБ)", latest.apkSizeBytes / (1024f * 1024f))
                            } else ""
                            Button(
                                onClick = {
                                    if (downloading) return@Button
                                    scope.launch {
                                        downloading = true
                                        downloadProgress = 0f
                                        runCatching {
                                            val file = AppUpdateManager.downloadApk(context, apkUrl) { p ->
                                                downloadProgress = p
                                            }
                                            AppUpdateManager.installApk(context, file)
                                        }.onFailure { err ->
                                            Toast.makeText(
                                                context,
                                                "Не удалось скачать APK: ${err.message ?: "ошибка сети"}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        downloading = false
                                    }
                                },
                                enabled = !downloading,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(15.dp)
                            ) {
                                Text("Скачать и установить в приложении$sizeMb", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { AppUpdateManager.openUrl(context, AppUpdateManager.SITE_DOWNLOAD_URL) },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Скачать через сайт", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }

                        TextButton(
                            onClick = { AppUpdateManager.openUrl(context, latest.htmlUrl) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Открыть релиз на GitHub ↗", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // Кнопка сайта даже когда обновлений нет (удобно проверить новости/скачать)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Сайт приложения", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Свежие версии, список изменений и загрузка APK всегда доступны на нашем сайте.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Button(
                            onClick = { AppUpdateManager.openUrl(context, AppUpdateManager.SITE_DOWNLOAD_URL) },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Открыть legoman-1337.github.io/notes ↗", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Настройки значка обновлений
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Настройки значка", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Показывать красное число обновлений",
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = !state.badgeHidden,
                            onCheckedChange = { show ->
                                AppUpdateManager.setBadgeHidden(context, !show)
                                onStateUpdated(AppUpdateManager.readState(context))
                            }
                        )
                    }
                    if (state.visibleBadgeCount > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        TextButton(
                            onClick = {
                                AppUpdateManager.dismissCurrentBadge(context)
                                onStateUpdated(AppUpdateManager.readState(context))
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Скрыть число для версии ${state.latestRelease?.tagName.orEmpty()}")
                        }
                    }
                    if (state.lastCheckedAt > 0L) {
                        Text(
                            "Последняя проверка: ${formatDate(state.lastCheckedAt)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}