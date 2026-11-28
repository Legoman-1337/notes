package ru.momnotes.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.math.roundToInt

private data class NoteListItem(val note: Note, val plainBody: String)

private class NoteDragState {
    var origin: Offset = Offset.Zero
    var pointer: Offset = Offset.Zero
    var offset: Offset = Offset.Zero
    val pointerState = mutableStateOf(Offset.Zero)
    val offsetState = mutableStateOf(Offset.Zero)
}

@Composable
internal fun NotesRoot() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mom_notes", Context.MODE_PRIVATE) }
    var settings by remember { mutableStateOf(readAppSettings(context)) }
    var showStartupAnimation by remember { mutableStateOf(true) }
    var systemBarOverride by remember { mutableStateOf<Pair<Color, Boolean>?>(null) }
    val startupColor = remember(settings.iconBackground, settings.accent, settings.appBackgroundTheme) {
        NoteThemes.find(settings.appBackgroundTheme)?.accentColor
            ?: themeColorChoices.firstOrNull { it.first == settings.iconBackground }?.second?.let(::parseHexColor)
            ?: accentColor(settings.accent)
    }
    var showWelcome by remember {
        val alreadyDone = prefs.getBoolean("welcome_done", false) ||
                prefs.all.any { (k, v) -> k.startsWith("welcome_done_") && v == true }
        mutableStateOf(!alreadyDone)
    }
    val bgTheme = remember(settings.appBackgroundTheme) { NoteThemes.find(settings.appBackgroundTheme) }
    val isDark = bgTheme?.isDark ?: when (settings.theme) {
        "Светлая" -> false
        "Системная" -> isSystemInDarkTheme()
        else -> true
    }

    Box(Modifier.fillMaxSize()) {
        NotesTheme(settings) {
            val accent = bgTheme?.accentColor ?: accentColor(settings.accent)
            val defaultHeader = if (bgTheme != null) {
                mixColors(bgTheme.surfaceTint, accent, 0.14f)
            } else {
                roleColor(
                    settings.headerColor,
                    mixColors(MaterialTheme.colorScheme.surface, accent, 0.14f)
                )
            }
            val navBarBg = bgTheme?.surfaceTint ?: if (isDark) Color(0xFF0C100E) else Color(0xFFF3F4F0)
            val effectiveStatusColor = when {
                showStartupAnimation -> StartupAnimationBackground
                systemBarOverride != null -> systemBarOverride!!.first
                else -> defaultHeader
            }
            val lightStatusIcons = when {
                showStartupAnimation -> false
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
                else -> NotesApp(
                    settings = settings,
                    onSettingsChange = { changed ->
                        settings = changed
                        writeAppSettings(context, changed)
                    },
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
    val bgTheme = remember(settings.appBackgroundTheme) { NoteThemes.find(settings.appBackgroundTheme) }
    val dark = bgTheme?.isDark ?: when (settings.theme) {
        "Светлая" -> false
        "Системная" -> isSystemInDarkTheme()
        else -> true
    }
    val accent = bgTheme?.accentColor ?: accentColor(settings.accent)
    val colors = if (bgTheme != null) {
        if (bgTheme.isDark) {
            val surface = mixColors(bgTheme.surfaceTint, Color.White, 0.08f)
            val surfaceVariant = mixColors(bgTheme.surfaceTint, Color.White, 0.14f)
            darkColorScheme(
                primary = accent,
                onPrimary = contrastColor(accent),
                secondary = mixColors(accent, Color.White, 0.25f),
                background = bgTheme.surfaceTint,
                onBackground = bgTheme.textColor,
                surface = surface,
                onSurface = bgTheme.textColor,
                surfaceVariant = surfaceVariant,
                onSurfaceVariant = bgTheme.mutedColor,
                outline = accent.copy(alpha = 0.45f),
                outlineVariant = bgTheme.mutedColor.copy(alpha = 0.28f),
                error = Color(0xFFFF8A80)
            )
        } else {
            val surface = mixColors(bgTheme.surfaceTint, Color.White, 0.58f)
            val surfaceVariant = mixColors(bgTheme.surfaceTint, Color.Black, 0.06f)
            lightColorScheme(
                primary = accent,
                onPrimary = contrastColor(accent),
                secondary = mixColors(accent, Color.Black, 0.18f),
                background = bgTheme.surfaceTint,
                onBackground = bgTheme.textColor,
                surface = surface,
                onSurface = bgTheme.textColor,
                surfaceVariant = surfaceVariant,
                onSurfaceVariant = bgTheme.mutedColor,
                outline = accent.copy(alpha = 0.45f),
                outlineVariant = bgTheme.mutedColor.copy(alpha = 0.28f),
                error = Color(0xFFB3261E)
            )
        }
    } else if (dark) {
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
private fun NotesApp(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
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
    var newCategoryName by remember { mutableStateOf("") }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var aboutOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    var pluginStoreOpen by remember { mutableStateOf(false) }
    var openPluginId by remember { mutableStateOf<String?>(null) }
    val plugins = remember { mutableStateListOf<PluginMeta>() }

    fun reloadPlugins() {
        plugins.clear()
        plugins.addAll(PluginManager.installed(context))
    }
    LaunchedEffect(Unit) {
        PluginManager.preinstallBundled(context)
        reloadPlugins()
    }
    LaunchedEffect(categoryList.toList()) {
        categoryDropBounds.keys.retainAll(categoryList.toSet())
    }
    val appBackgroundColor = MaterialTheme.colorScheme.background
    val pluginHeaderColor = if (settings.appBackgroundTheme.isNotBlank()) {
        MaterialTheme.colorScheme.surface
    } else {
        roleColor(settings.headerColor, MaterialTheme.colorScheme.surface)
    }
    LaunchedEffect(aboutOpen, settingsOpen, pluginStoreOpen, openPluginId, editorOpen, appBackgroundColor, pluginHeaderColor) {
        if (!editorOpen) {
            when {
                aboutOpen -> onStatusBarOverride(appBackgroundColor to (contrastColor(appBackgroundColor) == Color.White))
                pluginStoreOpen || openPluginId != null -> onStatusBarOverride(pluginHeaderColor to (contrastColor(pluginHeaderColor) == Color.White))
                else -> onStatusBarOverride(null)
            }
        }
    }
    BackHandler(enabled = aboutOpen || settingsOpen || pluginStoreOpen || openPluginId != null) {
        if (settingsOpen) settingsOpen = false
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
    val activeNotesCount by remember { derivedStateOf { notes.count { !it.isDeleted } } }
    val favoriteCount by remember { derivedStateOf { notes.count { !it.isDeleted && it.isFavorite } } }
    val categoryNoteCounts by remember {
        derivedStateOf {
            val map = HashMap<String, Int>()
            notes.forEach { n ->
                if (!n.isDeleted) map[n.category] = (map[n.category] ?: 0) + 1
            }
            map
        }
    }

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

    val appBgTheme = remember(settings.appBackgroundTheme) { NoteThemes.find(settings.appBackgroundTheme) }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !aboutOpen && !settingsOpen && !editorOpen && !pluginStoreOpen && openPluginId == null,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(120))
        ) {
            val categoryItems = remember(categoryList.toList()) { categoryList.toList() }
            val enabledPlugins = remember(plugins.toList()) { plugins.filter { it.enabled } }
            val enabledCount = enabledPlugins.size

            val accent = appBgTheme?.accentColor ?: accentColor(settings.accent)
            val sectionBase = if (appBgTheme != null) accent else roleColor(settings.sectionColor, accent)
            val headerColor = if (appBgTheme != null) {
                mixColors(appBgTheme.surfaceTint, accent, 0.14f)
            } else {
                roleColor(settings.headerColor, mixColors(MaterialTheme.colorScheme.surface, accent, 0.14f))
            }
            val headerBottom = mixColors(headerColor, Color.Black, 0.16f)
            val headerContentColor = if (appBgTheme != null) appBgTheme.textColor else contrastColor(headerColor)
            val headerShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (appBgTheme != null) {
                    NoteThemeCanvas(
                        themeId = appBgTheme.id,
                        modifier = Modifier.matchParentSize(),
                        animated = settings.animateNoteThemes
                    )
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(
                                appBgTheme.surfaceTint.copy(alpha = if (appBgTheme.isDark) 0.18f else 0.14f)
                            )
                    )
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, headerShape, clip = false)
                            .clip(headerShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        headerColor.copy(alpha = if (appBgTheme != null) 0.84f else 1f),
                                        headerBottom.copy(alpha = if (appBgTheme != null) 0.80f else 1f)
                                    )
                                )
                            )
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
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "Мои заметки",
                                        color = headerContentColor,
                                        fontSize = 25.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = headerContentColor.copy(alpha = 0.14f),
                                        border = BorderStroke(1.dp, headerContentColor.copy(alpha = 0.20f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { pluginStoreOpen = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text("🧩", fontSize = 14.sp)
                                            Text(
                                                if (enabledCount > 0) "Плагины · $enabledCount" else "Плагины",
                                                color = headerContentColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = headerContentColor.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, headerContentColor.copy(alpha = 0.14f)),
                                        modifier = Modifier.size(38.dp).clip(CircleShape).clickable { aboutOpen = true }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.Info, contentDescription = "О приложении", tint = headerContentColor, modifier = Modifier.size(19.dp))
                                        }
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = headerContentColor.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, headerContentColor.copy(alpha = 0.14f)),
                                        modifier = Modifier.size(38.dp).clip(CircleShape).clickable { settingsOpen = true }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.Settings, contentDescription = "Настройки", tint = headerContentColor, modifier = Modifier.size(19.dp))
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
                                    val sectionNoteCount = categoryNoteCounts[category] ?: 0
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
                                    modifier = Modifier.size(36.dp).clip(CircleShape).clickable { newCategoryName = ""; showCategoryDialog = true }
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
                                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f)
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
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = if (appBgTheme != null) 0.80f else 0.90f),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
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
                                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (appBgTheme != null) 0.88f else 1f),
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (appBgTheme != null) 0.74f else 0.70f),
                                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                        )
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Box(Modifier.fillMaxWidth().weight(1f)) {
                                        val canDragNotes = settings.sortBy == "Пользовательский" && appliedSearchText.isBlank() && sortMode == 0
                                        LazyColumn(
                                            state = noteListState,
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            contentPadding = PaddingValues(top = 2.dp, bottom = 108.dp)
                                        ) {
                                            items(visibleNotes, key = { it.note.id }, contentType = { "note" }) { row ->
                                                val note = row.note
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
                                                        appBgTheme = appBgTheme,
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
                                            val fabCreateColor = if (appBgTheme != null) accent else roleColor(settings.addButtonColor, accent)
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
                                                                listOf(accent.copy(alpha = 0.24f), accent.copy(alpha = 0.08f))
                                                            )
                                                        )
                                                        .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(26.dp)),
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
                                                            containerColor = fabCreateColor,
                                                            contentColor = contrastColor(fabCreateColor)
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
                            val fabColor = if (appBgTheme != null) accent else roleColor(settings.addButtonColor, accentColor(settings.accent))
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
            }

            if (showCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showCategoryDialog = false },
                    title = { Text("Новый раздел") },
                    text = {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Название раздела") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val name = newCategoryName.trim()
                            if (name.isNotEmpty() && categoryList.none { it.equals(name, ignoreCase = true) }) {
                                categoryList.add(name)
                                writeCategories(context, categoryList.toList())
                                selectedCategory = name
                                searchText = ""
                                appliedSearchText = ""
                                sortMode = 0
                                showCategoryDialog = false
                            }
                        }, enabled = newCategoryName.isNotBlank()) { Text("Добавить") }
                    },
                    dismissButton = { TextButton(onClick = { showCategoryDialog = false }) { Text("Отмена") } }
                )
            }

            categoryToDelete?.let { target ->
                val count = categoryNoteCounts[target] ?: 0
                AlertDialog(
                    onDismissRequest = { categoryToDelete = null },
                    title = { Text("Удалить раздел?") },
                    text = { Text(if (count == 0) "Раздел «$target» будет удалён." else "Раздел «$target» и все его заметки ($count) будут удалены с этого телефона.") },
                    confirmButton = {
                        TextButton(onClick = {
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
                        }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = { TextButton(onClick = { categoryToDelete = null }) { Text("Отмена") } }
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
                                    Modifier.size(44.dp).clip(CircleShape).background(sectionBase),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("↗", color = contrastColor(sectionBase), fontSize = 24.sp)
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
                AlertDialog(
                    onDismissRequest = { noteToDelete = null },
                    title = { Text("Удалить заметку?") },
                    text = { Text("«${target.title.ifBlank { "Без названия" }}» будет удалена с этого телефона.") },
                    confirmButton = {
                        TextButton(onClick = {
                            val index = notes.indexOfFirst { it.id == target.id }
                            if (index >= 0) {
                                cancelNoteReminders(context, notes[index])
                                notes[index] = notes[index].copy(isDeleted = true, updatedAt = System.currentTimeMillis(), reminderAt = 0L, reminders = emptyList())
                            }
                            persist()
                            noteToDelete = null
                        }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = { TextButton(onClick = { noteToDelete = null }) { Text("Отмена") } }
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
                    settings = settings,
                    appBgTheme = appBgTheme
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
                noteCount = activeNotesCount,
                categoryCount = categoryList.size,
                onExport = { exportLauncher.launch("Заметки-резервная-копия.json") },
                onImport = { importLauncher.launch(arrayOf("application/json", "text/json", "*/*")) },
                onAbout = { settingsOpen = false; aboutOpen = true },
                onBack = { settingsOpen = false },
                onReset = { onSettingsChange(AppSettings()) }
            )
        }
        AnimatedVisibility(
            visible = aboutOpen,
            enter = fadeIn(tween(220)) + slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 5 },
            exit = fadeOut(tween(180)) + slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { it / 6 }
        ) {
            AboutScreen(onBack = { aboutOpen = false })
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
    settings: AppSettings,
    appBgTheme: NoteThemeSpec?
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
            appBgTheme = appBgTheme,
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
    appBgTheme: NoteThemeSpec?,
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
        if (note.reminders.isEmpty() && note.reminderAt <= 0L) emptyList()
        else {
            val now = System.currentTimeMillis()
            (note.reminders + listOfNotNull(note.reminderAt.takeIf { it > 0L }))
                .distinct()
                .filter { it > now }
                .sorted()
        }
    }
    val normalizedAi = remember(note.aiAssistant) { normalizeAiAssistant(note.aiAssistant) }
    var actionsExpanded by remember(note.id) { mutableStateOf(false) }
    val noteTheme = remember(note.themeId) { NoteThemes.find(note.themeId) }
    val useThemeBackground = (appBgTheme != null || settings.noteColor.isBlank()) && noteTheme != null

    val accent = appBgTheme?.accentColor ?: accentColor(settings.accent)
    val cardColor = when {
        useThemeBackground -> noteTheme!!.surfaceTint
        appBgTheme != null -> if (appBgTheme.isDark) {
            mixColors(appBgTheme.surfaceTint, Color.Black, 0.22f).copy(alpha = 0.84f)
        } else {
            Color.White.copy(alpha = 0.84f)
        }
        settings.noteColor.isBlank() -> MaterialTheme.colorScheme.surface
        else -> {
            val baseCardColor = roleColor(settings.noteColor, MaterialTheme.colorScheme.surface)
            val colorChroma = maxOf(baseCardColor.red, baseCardColor.green, baseCardColor.blue) -
                    minOf(baseCardColor.red, baseCardColor.green, baseCardColor.blue)
            val colorSoftening = if (colorChroma > 0.22f) 0.42f else 0.14f
            mixColors(baseCardColor, MaterialTheme.colorScheme.surface, colorSoftening)
        }
    }
    val textColor = when {
        useThemeBackground -> noteTheme!!.textColor
        appBgTheme != null -> appBgTheme.textColor
        settings.noteColor.isBlank() -> MaterialTheme.colorScheme.onSurface
        else -> contrastColor(cardColor)
    }
    val focusColor = Color(1f - cardColor.red, 1f - cardColor.green, 1f - cardColor.blue)
    val defaultBorder = when {
        useThemeBackground -> noteTheme!!.accentColor.copy(alpha = 0.48f)
        appBgTheme != null -> appBgTheme.accentColor.copy(alpha = 0.30f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
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
        targetValue = if (note.isFavorite) Color(0xFFFFC857) else (
                when {
                    useThemeBackground -> noteTheme!!.mutedColor
                    appBgTheme != null -> appBgTheme.mutedColor
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                ).copy(alpha = 0.42f),
        animationSpec = tween(220),
        label = "fav-star-tint"
    )
    val cardSubtextColor = when {
        useThemeBackground -> noteTheme!!.mutedColor
        appBgTheme != null -> appBgTheme.mutedColor
        settings.noteColor.isBlank() -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> textColor.copy(alpha = 0.78f)
    }
    val badgeBg = when {
        useThemeBackground && noteTheme!!.isDark -> Color.Black.copy(alpha = 0.28f)
        useThemeBackground -> Color.White.copy(alpha = 0.65f)
        appBgTheme != null && appBgTheme.isDark -> Color.Black.copy(alpha = 0.28f)
        appBgTheme != null -> appBgTheme.accentColor.copy(alpha = 0.14f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)
    }
    val bodySize = when (settings.noteTextSize) { "Маленький" -> 12; "Большой" -> 17; else -> 14 }
    val corner = when (settings.corner) { "Маленькое" -> 14; "Большое" -> 26; else -> 20 }
    val gap = if (settings.density == "Компактно") 6.dp else 11.dp

    val dragGestureModifier = if (dragEnabled) {
        Modifier.pointerInput(note.id, true) {
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
    } else Modifier

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
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else if (appBgTheme != null) 0.dp else 2.dp),
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
                            if (hasMeta) {
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
                            } else {
                                Spacer(Modifier.weight(1f))
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