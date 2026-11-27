package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.example.data.local.GoalEntity
import com.example.ui.components.CategoryPill
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LifeTrackCard
import com.example.ui.components.categoryAccentColor
import com.example.ui.theme.LocalLifeTrackColors
import com.example.ui.viewmodel.LifeTrackUiState
import com.example.util.DateUtils

private val GOAL_FILTERS = listOf("All", "Active", "Completed")
private val GOAL_CATEGORIES = listOf("Learning", "Study", "Career", "Wellness")

@Composable
fun GoalsScreen(
    uiState: LifeTrackUiState,
    onSaveGoal: (Long, String, String, Int, Int, String, String, String, String) -> Unit,
    onIncrementGoal: (GoalEntity, Int) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var filter by remember { mutableStateOf("All") }
    var editingGoal by remember { mutableStateOf<GoalEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    val filteredGoals = remember(uiState.goals, filter) {
        when (filter) {
            "Active" -> uiState.goals.filter { !it.isCompleted }
            "Completed" -> uiState.goals.filter { it.isCompleted }
            else -> uiState.goals
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingGoal = null
                    showEditor = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_goal_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add new goal")
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
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(GOAL_FILTERS) { tab ->
                            FilterChip(
                                selected = filter == tab,
                                onClick = { filter = tab },
                                label = { Text(tab) }
                            )
                        }
                    }
                }

                if (filteredGoals.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.Flag,
                            title = "No goals in $filter",
                            message = "Tap + to define a measurable goal with a deadline and track your milestones."
                        )
                    }
                } else {
                    items(filteredGoals, key = { it.id }) { goal ->
                        GoalProgressCard(
                            goal = goal,
                            todayKey = uiState.todayKey,
                            onIncrement = { delta -> onIncrementGoal(goal, delta) },
                            onEdit = {
                                editingGoal = goal
                                showEditor = true
                            },
                            onDelete = { onDeleteGoal(goal) }
                        )
                    }
                }
            }
        }
    }

    if (showEditor) {
        GoalEditorDialog(
            initial = editingGoal,
            todayKey = uiState.todayKey,
            onDismiss = { showEditor = false },
            onSave = { title, category, current, target, unit, deadlineKey, priority, notes ->
                onSaveGoal(
                    editingGoal?.id ?: 0L,
                    title,
                    category,
                    current,
                    target,
                    unit,
                    deadlineKey,
                    priority,
                    notes
                )
                showEditor = false
            }
        )
    }
}

@Composable
private fun GoalProgressCard(
    goal: GoalEntity,
    todayKey: String,
    onIncrement: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = categoryAccentColor(goal.category)
    val extraColors = LocalLifeTrackColors.current
    val ratio = (goal.currentProgress.toFloat() / goal.targetProgress.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val pct = (ratio * 100).toInt()
    val daysRemaining = DateUtils.daysUntil(goal.deadlineDateKey, todayKey)
    val deadlineText = when {
        goal.isCompleted -> "Completed"
        daysRemaining < 0 -> "${-daysRemaining}d overdue"
        daysRemaining == 0 -> "Due today"
        else -> "${daysRemaining}d left"
    }
    val deadlineColor = when {
        goal.isCompleted -> MaterialTheme.colorScheme.primary
        daysRemaining < 0 -> MaterialTheme.colorScheme.error
        daysRemaining <= 5 -> extraColors.amber
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    LifeTrackCard(
        borderColor = if (goal.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else extraColors.cardBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryPill(text = goal.category, color = accent)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(deadlineColor.copy(alpha = 0.14f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = deadlineText,
                            style = MaterialTheme.typography.labelSmall,
                            color = deadlineColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (goal.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = goal.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit goal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete goal",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${goal.currentProgress} / ${goal.targetProgress} ${goal.unit}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$pct% · Due ${DateUtils.formatShortDate(goal.deadlineDateKey)}",
                style = MaterialTheme.typography.labelMedium,
                color = accent
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            color = accent,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val step = if (goal.targetProgress >= 50) 5 else 1
            FilledTonalIconButton(
                onClick = { onIncrement(-step) },
                enabled = goal.currentProgress > 0,
                modifier = Modifier.testTag("goal_dec_${goal.id}")
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "Decrease progress")
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalIconButton(
                onClick = { onIncrement(step) },
                enabled = goal.currentProgress < goal.targetProgress,
                modifier = Modifier.testTag("goal_inc_${goal.id}")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Increase progress")
            }
        }
    }
}

@Composable
private fun GoalEditorDialog(
    initial: GoalEntity?,
    todayKey: String,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Int, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "Learning") }
    var targetText by remember { mutableStateOf((initial?.targetProgress ?: 20).toString()) }
    var currentText by remember { mutableStateOf((initial?.currentProgress ?: 0).toString()) }
    var unit by remember { mutableStateOf(initial?.unit ?: "tasks") }
    var daysOut by remember {
        mutableIntStateOf(
            if (initial != null) DateUtils.daysUntil(initial.deadlineDateKey, todayKey).coerceAtLeast(1) else 14
        )
    }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Goal" else "Edit Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("goal_title_input")
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GOAL_CATEGORIES) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentText,
                        onValueChange = { currentText = it.filter { c -> c.isDigit() } },
                        label = { Text("Current") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it.filter { c -> c.isDigit() } },
                        label = { Text("Target") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = "Deadline: ${DateUtils.formatShortDate(DateUtils.offsetDateKey(daysOut, todayKey))} (+${daysOut}d)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(7, 14, 30, 60).forEach { d ->
                        FilterChip(
                            selected = daysOut == d,
                            onClick = { daysOut = d },
                            label = { Text("+${d}d") }
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
                    val target = targetText.toIntOrNull()?.coerceAtLeast(1) ?: 10
                    val current = currentText.toIntOrNull()?.coerceIn(0, target) ?: 0
                    val deadlineKey = DateUtils.offsetDateKey(daysOut, todayKey)
                    onSave(title, category, current, target, unit, deadlineKey, "High", notes)
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_goal_button")
            ) { Text("Save Goal") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
