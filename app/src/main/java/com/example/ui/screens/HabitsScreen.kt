package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.HabitEntity
import com.example.ui.components.CategoryPill
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LifeTrackCard
import com.example.ui.components.categoryAccentColor
import com.example.ui.theme.LocalLifeTrackColors
import com.example.ui.viewmodel.HabitWithStats
import com.example.ui.viewmodel.LifeTrackUiState
import com.example.util.DateUtils

private val HABIT_CATEGORIES = listOf("Focus", "Learning", "Fitness", "Wellness")
private val ACCENT_KEYS = listOf("emerald", "cyan", "violet", "amber")

@Composable
fun HabitsScreen(
    uiState: LifeTrackUiState,
    onToggleHabitDate: (Long, String) -> Unit,
    onSaveHabit: (Long, String, String, String, Int) -> Unit,
    onDeleteHabit: (HabitEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val weekKeys = remember(uiState.todayKey) { DateUtils.weekKeysAround(uiState.todayKey) }
    val totalWeeklyChecks = uiState.habitsWithStats.sumOf { it.completedThisWeek }
    val totalWeeklyTarget = uiState.habitsWithStats.sumOf { it.habit.targetPerWeek }.coerceAtLeast(1)
    val extraColors = LocalLifeTrackColors.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_habit_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add new habit")
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
                // Weekly Overview Banner
                item {
                    LifeTrackCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Weekly Habit Matrix",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$totalWeeklyChecks / $totalWeeklyTarget weekly targets hit",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(extraColors.amberSoft)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = "Best streak",
                                    tint = extraColors.amber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.bestHabitStreak}d Best",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = extraColors.amber
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            weekKeys.forEach { key ->
                                val isToday = key == uiState.todayKey
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = DateUtils.formatDayShort(key).take(2),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${DateUtils.formatDayNumber(key)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.habitsWithStats.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.TrackChanges,
                            title = "No habits tracked yet",
                            message = "Tap + to create a daily habit and build unstoppable streaks."
                        )
                    }
                } else {
                    items(uiState.habitsWithStats, key = { it.habit.id }) { stats ->
                        HabitTrackerCard(
                            stats = stats,
                            weekKeys = weekKeys,
                            todayKey = uiState.todayKey,
                            onToggleDate = { key -> onToggleHabitDate(stats.habit.id, key) },
                            onDelete = { onDeleteHabit(stats.habit) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        HabitCreateDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { title, category, accentKey, target ->
                onSaveHabit(0L, title, category, accentKey, target)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun HabitTrackerCard(
    stats: HabitWithStats,
    weekKeys: List<String>,
    todayKey: String,
    onToggleDate: (String) -> Unit,
    onDelete: () -> Unit
) {
    val accent = categoryAccentColor(stats.habit.accentKey)
    val extraColors = LocalLifeTrackColors.current
    val doneToday = stats.weekCompletionMap[todayKey] == true

    LifeTrackCard(
        borderColor = if (doneToday) accent.copy(alpha = 0.45f) else extraColors.cardBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stats.habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryPill(text = stats.habit.category, color = accent)
                    Text(
                        text = "${stats.completedThisWeek}/${stats.habit.targetPerWeek} this wk",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${stats.currentStreak}d",
                        style = MaterialTheme.typography.labelMedium,
                        color = accent
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete ${stats.habit.title}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekKeys.forEach { key ->
                val checked = stats.weekCompletionMap[key] == true
                val isToday = key == todayKey
                val bg by animateColorAsState(
                    targetValue = if (checked) accent else MaterialTheme.colorScheme.surfaceContainerLow,
                    label = "habit_day_bg"
                )
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("habit_${stats.habit.id}_$key")
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleDate(key) },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(bg)
                            .border(
                                width = if (isToday && !checked) 1.5.dp else 1.dp,
                                color = when {
                                    checked -> accent
                                    isToday -> accent.copy(alpha = 0.7f)
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (checked) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Completed on $key",
                                tint = Color.Black.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = DateUtils.formatDayShort(key).take(1),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isToday) accent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitCreateDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Focus") }
    var accentKey by remember { mutableStateOf("emerald") }
    var targetPerWeek by remember { mutableIntStateOf(6) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Habit", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("habit_title_input")
                )
                Text("Category", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(HABIT_CATEGORIES) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Text("Accent Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ACCENT_KEYS.forEach { key ->
                        val color = categoryAccentColor(key)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (accentKey == key) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { accentKey = key }
                        )
                    }
                }
                Text(
                    text = "Weekly Target: $targetPerWeek days/week",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = targetPerWeek.toFloat(),
                    onValueChange = { targetPerWeek = it.toInt().coerceIn(1, 7) },
                    valueRange = 1f..7f,
                    steps = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, category, accentKey, targetPerWeek) },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_habit_button")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
