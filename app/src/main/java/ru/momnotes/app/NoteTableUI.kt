package ru.momnotes.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Locale

private data class TableCellAddress(val row: Int, val column: Int)

private fun parseNumericCell(raw: String): Double? {
    val plain = RichTextDocument.plainText(raw).trim()
    if (plain.isEmpty()) return null
    val cleaned = plain.replace(" ", "").replace("₽", "").replace("%", "").replace(',', '.')
    return cleaned.toDoubleOrNull()
}

private fun formatTableSum(sum: Double): String {
    val rounded = kotlin.math.round(sum * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else String.format(Locale.US, "%.2f", rounded).trimEnd('0').trimEnd('.')
}

@Composable
internal fun NoteTableEditor(
    table: NoteTable,
    index: Int,
    activeTheme: NoteThemeSpec?,
    customTableColorHex: String,
    onUpdateTable: (NoteTable) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var editingCell by remember(table.id) { mutableStateOf<TableCellAddress?>(null) }
    var cellDraft by remember(table.id) { mutableStateOf("") }
    var tableMenuExpanded by remember(table.id) { mutableStateOf(false) }
    var showColumnTotals by remember(table.id) { mutableStateOf(false) }

    val customTint = parseHexColor(customTableColorHex)
    val accentColor = activeTheme?.accentColor ?: MaterialTheme.colorScheme.primary
    val containerColor = when {
        customTint != null -> mixColors(MaterialTheme.colorScheme.surface, customTint, 0.34f)
        activeTheme == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        activeTheme.isDark -> mixColors(activeTheme.surfaceTint, Color.Black, 0.24f).copy(alpha = 0.86f)
        else -> Color.White.copy(alpha = 0.82f)
    }
    val borderColor = when {
        customTint != null -> customTint.copy(alpha = 0.55f)
        activeTheme != null -> activeTheme.accentColor.copy(alpha = 0.42f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    }
    val headerCellBg = when {
        customTint != null -> mixColors(customTint, accentColor, 0.22f)
        activeTheme != null -> activeTheme.accentColor.copy(alpha = if (activeTheme.isDark) 0.28f else 0.20f)
        else -> mixColors(MaterialTheme.colorScheme.surfaceVariant, accentColor, 0.20f)
    }
    val evenCellBg = when {
        customTint != null -> mixColors(customTint, MaterialTheme.colorScheme.background, 0.22f)
        activeTheme == null -> MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        activeTheme.isDark -> activeTheme.surfaceTint.copy(alpha = 0.65f)
        else -> Color.White.copy(alpha = 0.72f)
    }
    val oddCellBg = when {
        customTint != null -> mixColors(evenCellBg, accentColor, 0.06f)
        activeTheme == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        activeTheme.isDark -> mixColors(activeTheme.surfaceTint, Color.White, 0.06f).copy(alpha = 0.70f)
        else -> activeTheme.surfaceTint.copy(alpha = 0.58f)
    }
    val titleColor = when {
        customTint != null -> contrastColor(containerColor)
        activeTheme != null -> activeTheme.textColor
        else -> MaterialTheme.colorScheme.onSurface
    }
    val mutedColor = when {
        customTint != null -> titleColor.copy(alpha = 0.65f)
        activeTheme != null -> activeTheme.mutedColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val rowCount = table.cells.size
    val colCount = (table.cells.maxOfOrNull { it.size } ?: 1).coerceAtLeast(1)
    val columnSums = remember(table.cells, showColumnTotals) {
        if (!showColumnTotals || rowCount <= 1) emptyList()
        else (0 until colCount).map { col ->
            val values = table.cells.drop(1).mapNotNull { row -> parseNumericCell(row.getOrNull(col).orEmpty()) }
            if (values.isNotEmpty()) formatTableSum(values.sum()) else ""
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    NoteVectorIcon("table", accentColor, size = 15.dp)
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Таблица $index",
                        color = titleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "$rowCount × $colCount",
                        color = mutedColor,
                        fontSize = 11.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.14f),
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
                        onUpdateTable(table.copy(cells = table.cells + listOf(List(colCount) { "" })))
                    }
                ) {
                    Text(
                        "+ Строка",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.14f),
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
                        if (colCount < 8) {
                            onUpdateTable(table.copy(cells = table.cells.map { row ->
                                val padded = row + List((colCount - row.size).coerceAtLeast(0)) { "" }
                                padded + ""
                            }))
                        } else {
                            Toast.makeText(context, "Максимум 8 столбцов", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(
                        "+ Столбец",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box {
                    IconButton(onClick = { tableMenuExpanded = true }, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Действия с таблицей", tint = mutedColor, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = tableMenuExpanded, onDismissRequest = { tableMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(if (showColumnTotals) "Скрыть итоги Σ" else "Показать итоги Σ") },
                            onClick = { showColumnTotals = !showColumnTotals; tableMenuExpanded = false }
                        )
                        if (rowCount > 2) {
                            DropdownMenuItem(
                                text = { Text("Сортировать по 1-му столбцу") },
                                onClick = {
                                    val header = table.cells.first()
                                    val sortedBody = table.cells.drop(1).sortedBy {
                                        RichTextDocument.plainText(it.firstOrNull().orEmpty()).lowercase(Locale.getDefault())
                                    }
                                    onUpdateTable(table.copy(cells = listOf(header) + sortedBody))
                                    tableMenuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Скопировать таблицу") },
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Таблица $index", table.toMarkdownTable()))
                                Toast.makeText(context, "Таблица скопирована", Toast.LENGTH_SHORT).show()
                                tableMenuExpanded = false
                            }
                        )
                        if (rowCount > 1) {
                            DropdownMenuItem(
                                text = { Text("Удалить последнюю строку") },
                                onClick = {
                                    onUpdateTable(table.copy(cells = table.cells.dropLast(1)))
                                    tableMenuExpanded = false
                                }
                            )
                        }
                        if (colCount > 1) {
                            DropdownMenuItem(
                                text = { Text("Удалить последний столбец") },
                                onClick = {
                                    onUpdateTable(table.copy(cells = table.cells.map { it.dropLast(1) }))
                                    tableMenuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Удалить таблицу", color = MaterialTheme.colorScheme.error) },
                            onClick = { tableMenuExpanded = false; onDelete() }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Column(Modifier.horizontalScroll(rememberScrollState())) {
                table.cells.forEachIndexed { rowIndex, rawRow ->
                    val cells = List(colCount) { col -> rawRow.getOrNull(col).orEmpty() }
                    val cellBg = when {
                        rowIndex == 0 -> headerCellBg
                        rowIndex % 2 == 1 -> evenCellBg
                        else -> oddCellBg
                    }
                    val cellText = when {
                        customTint != null -> contrastColor(cellBg)
                        activeTheme != null -> activeTheme.textColor
                        else -> contrastColor(cellBg)
                    }
                    val cellBorder = if (rowIndex == 0) accentColor.copy(alpha = 0.55f) else borderColor.copy(alpha = 0.7f)
                    Row(verticalAlignment = Alignment.Top) {
                        cells.forEachIndexed { columnIndex, cell ->
                            val isBlankCell = RichTextDocument.plainText(cell).isBlank()
                            Card(
                                modifier = Modifier
                                    .width(172.dp)
                                    .heightIn(min = if (rowIndex == 0) 46.dp else 56.dp)
                                    .padding(end = 6.dp, bottom = 6.dp)
                                    .clickable {
                                        editingCell = TableCellAddress(rowIndex, columnIndex)
                                        cellDraft = cell
                                    },
                                shape = RoundedCornerShape(11.dp),
                                colors = CardDefaults.cardColors(containerColor = cellBg),
                                border = BorderStroke(if (rowIndex == 0) 1.2.dp else 0.8.dp, cellBorder)
                            ) {
                                Text(
                                    text = RichTextDocument.annotatedString(
                                        if (isBlankCell) {
                                            if (rowIndex == 0) "Столбец ${columnIndex + 1}" else "Пусто"
                                        } else cell
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp),
                                    color = if (isBlankCell) cellText.copy(alpha = 0.42f) else cellText,
                                    fontWeight = if (rowIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                if (showColumnTotals && columnSums.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        columnSums.forEachIndexed { columnIndex, sumText ->
                            Card(
                                modifier = Modifier
                                    .width(172.dp)
                                    .padding(end = 6.dp, top = 2.dp),
                                shape = RoundedCornerShape(11.dp),
                                colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.16f)),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = if (sumText.isNotEmpty()) "Σ $sumText" else if (columnIndex == 0) "Итого Σ" else "—",
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp),
                                    color = titleColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    editingCell?.let { address ->
        val dialogBg = activeTheme?.surfaceTint ?: MaterialTheme.colorScheme.surface
        val dialogText = activeTheme?.textColor ?: MaterialTheme.colorScheme.onSurface
        val dialogMuted = activeTheme?.mutedColor ?: MaterialTheme.colorScheme.onSurfaceVariant

        fun updateCurrentCellAndClose(nextCells: List<List<String>>) {
            onUpdateTable(table.copy(cells = nextCells))
            editingCell = null
        }

        fun committedCells(): List<List<String>> = table.cells.mapIndexed { r, row ->
            val uniform = List(colCount) { c -> row.getOrNull(c).orEmpty() }
            if (r == address.row) uniform.mapIndexed { c, cell -> if (c == address.column) cellDraft else cell } else uniform
        }

        Dialog(onDismissRequest = { editingCell = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = dialogBg,
                border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.42f)),
                shadowElevation = 14.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                if (address.row == 0) "Заголовок столбца ${address.column + 1}"
                                else "Ячейка ${address.row + 1}, ${address.column + 1}",
                                color = dialogText,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Поддерживает цвет, жирный, размер и шрифт", color = dialogMuted, fontSize = 11.sp)
                        }
                        IconButton(onClick = { editingCell = null }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Закрыть", tint = dialogMuted, modifier = Modifier.size(18.dp))
                        }
                    }

                    RichTextField(
                        value = cellDraft,
                        onValueChange = { cellDraft = it },
                        label = "",
                        editorHeight = 175.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = accentColor.copy(alpha = 0.14f),
                            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { cellDraft = "" }
                        ) {
                            Text("Очистить", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = accentColor.copy(alpha = 0.14f),
                            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
                                val updated = committedCells().toMutableList()
                                updated.add(address.row + 1, List(colCount) { "" })
                                updateCurrentCellAndClose(updated)
                            }
                        ) {
                            Text("+ Строка ниже", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (colCount < 8) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = accentColor.copy(alpha = 0.14f),
                                modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
                                    val updated = committedCells().map { row ->
                                        row.toMutableList().apply { add(address.column + 1, "") }
                                    }
                                    updateCurrentCellAndClose(updated)
                                }
                            ) {
                                Text("+ Столбец правее", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (rowCount > 1) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                                modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
                                    val updated = committedCells().filterIndexed { idx, _ -> idx != address.row }
                                    updateCurrentCellAndClose(updated)
                                }
                            ) {
                                Text("Удалить строку", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (colCount > 1) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                                modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
                                    val updated = committedCells().map { row ->
                                        row.filterIndexed { idx, _ -> idx != address.column }
                                    }
                                    updateCurrentCellAndClose(updated)
                                }
                            ) {
                                Text("Удалить столбец", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { editingCell = null }) {
                            Text("Отмена", color = dialogMuted)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { updateCurrentCellAndClose(committedCells()) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = contrastColor(accentColor)
                            )
                        ) {
                            Text("Готово", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}