package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.RoutineEntity
import com.example.ui.components.CategoryPill
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LifeTrackCard
import com.example.ui.components.SpringCheckCircle
import com.example.ui.components.categoryAccentColor
import com.example.ui.theme.LocalLifeTrackColors
import com.example.ui.viewmodel.LifeTrackUiState
import com.example.util.DateUtils

private val ROUTINE_CATEGORIES = listOf("All", "Morning", "Deep Work", "Study", "Health", "Evening")
private val EDITABLE_CATEGORIES = listOf("Morning", "Deep Work", "Study", "Health", "Evening")
private val PRIORITIES = listOf("High", "Medium", "Low")

@Composable
fun RoutineScreen(
    uiState: LifeTrackUiState,
    onSelectDate: (String) -> Unit,
    onToggleRoutine: (RoutineEntity) -> Unit,
    onSaveRoutine: (Long, String, String, Int, Int, String, String, String, Boolean) -> Unit,
    onDeleteRoutine: (RoutineEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var editingRoutine by remember { mutableStateOf<RoutineEntity?>(null) }
    var showEditorDialog by remember { mutableStateOf(false) }

    val weekKeys = remember(uiState.selectedRoutineDateKey) {
        DateUtils.weekKeysAround(uiState.selectedRoutineDateKey)
    }
    val filteredRoutines = remember(uiState.selectedDateRoutines, selectedCategory) {
        if (selectedCategory == "All") uiState.selectedDateRoutines
        else uiState.selectedDateRoutines.filter { it.category == selectedCategory }
    }
    val completedCount = uiState.selectedDateRoutines.count { it.isCompleted }
    val totalCount = uiState.selectedDateRoutines.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRoutine = null
                    showEditorDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_routine_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add routine activity")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Week Strip Header
                item {
                    LifeTrackCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = DateUtils.formatDisplayDate(uiState.selectedRoutineDateKey),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$completedCount of $totalCount activities completed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            TextButton(onClick = { onSelectDate(uiState.todayKey) }) {
                                Text("Today")
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            weekKeys.forEach { key ->
                                val isSelected = key == uiState.selectedRoutineDateKey
                                val isToday = key == uiState.todayKey
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                                else -> MaterialTheme.colorScheme.surfaceContainerLow
                                            }
                                        )
                                        .clickable { onSelectDate(key) }
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = DateUtils.formatDayShort(key),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${DateUtils.formatDayNumber(key)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Category Filter Chips
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ROUTINE_CATEGORIES) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                if (filteredRoutines.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.EventNote,
                            title = "No activities in this view",
                            message = "Tap the + button to schedule a time-blocked routine activity."
                        )
                    }
                } else {
                    items(filteredRoutines, key = { it.id }) { routine ->
                        RoutinePlannerCard(
                            routine = routine,
                            onToggle = { onToggleRoutine(routine) },
                            onEdit = {
                                editingRoutine = routine
                                showEditorDialog = true
                            },
                            onDelete = { onDeleteRoutine(routine) }
                        )
                    }
                }
            }
        }
    }

    if (showEditorDialog) {
        RoutineEditorDialog(
            initial = editingRoutine,
            defaultDateKey = uiState.selectedRoutineDateKey,
            onDismiss = { showEditorDialog = false },
            onSave = { title, category, startMin, endMin, priority, notes ->
                onSaveRoutine(
                    editingRoutine?.id ?: 0L,
                    title,
                    category,
                    startMin,
                    endMin,
                    editingRoutine?.dateKey ?: uiState.selectedRoutineDateKey,
                    priority,
                    notes,
                    editingRoutine?.isCompleted ?: false
                )
                showEditorDialog = false
            }
        )
    }
}

@Composable
private fun RoutinePlannerCard(
    routine: RoutineEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = categoryAccentColor(routine.category)
    val extra = LocalLifeTrackColors.current
    val priorityColor = when (routine.priority) {
        "High" -> extra.amber
        "Medium" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    LifeTrackCard(
        containerColor = if (routine.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface,
        borderColor = if (routine.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else extra.cardBorder,
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            SpringCheckCircle(
                checked = routine.isCompleted,
                onToggle = onToggle,
                accentColor = MaterialTheme.colorScheme.primary,
                contentDescription = "Complete ${routine.title}",
                testTag = "routine_checkbox_${routine.id}"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = DateUtils.formatTimeRange(routine.startMinutes, routine.endMinutes),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(priorityColor)
                    )
                    Text(
                        text = routine.priority,
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (routine.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (routine.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (routine.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = routine.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                CategoryPill(text = routine.category, color = accent)
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_routine_${routine.id}")) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit ${routine.title}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_routine_${routine.id}")) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete ${routine.title}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineEditorDialog(
    initial: RoutineEntity?,
    defaultDateKey: String,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "Deep Work") }
    var priority by remember { mutableStateOf(initial?.priority ?: "High") }
    var startMinutes by remember { mutableIntStateOf(initial?.startMinutes ?: (9 * 60)) }
    var durationMinutes by remember {
        mutableIntStateOf(
            if (initial != null) (initial.endMinutes - initial.startMinutes).coerceAtLeast(15) else 60
        )
    }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) "New Routine Block" else "Edit Routine Block",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Date: ${DateUtils.formatShortDate(initial?.dateKey ?: defaultDateKey)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("routine_title_input")
                )
                Text("Category", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(EDITABLE_CATEGORIES) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Text("Time Slot", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { startMinutes = (startMinutes - 30).coerceAtLeast(5 * 60) }) {
                        Text("-30m")
                    }
                    Text(
                        text = DateUtils.formatTimeRange(startMinutes, startMinutes + durationMinutes),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { startMinutes = (startMinutes + 30).coerceAtMost(22 * 60) }) {
                        Text("+30m")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(30, 45, 60, 90).forEach { dur ->
                        FilterChip(
                            selected = durationMinutes == dur,
                            onClick = { durationMinutes = dur },
                            label = { Text("${dur}m") }
                        )
                    }
                }
                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PRIORITIES.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(title, category, startMinutes, startMinutes + durationMinutes, priority, notes)
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_routine_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
