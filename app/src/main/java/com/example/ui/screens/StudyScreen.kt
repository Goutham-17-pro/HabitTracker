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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.example.data.local.StudySessionEntity
import com.example.ui.components.CategoryPill
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LifeTrackCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatMiniCard
import com.example.ui.viewmodel.LifeTrackUiState
import com.example.ui.viewmodel.StudyTimerMode
import com.example.util.DateUtils

private val DEFAULT_SUBJECTS = listOf(
    "System Architecture",
    "Algorithms & Graphs",
    "Jetpack Compose UI",
    "Product Strategy",
    "Language Study"
)

@Composable
fun StudyScreen(
    uiState: LifeTrackUiState,
    onSelectMode: (StudyTimerMode) -> Unit,
    onSetSubject: (String) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onCompleteAndLogNow: () -> Unit,
    onLogManualSession: (String, Int, String, String) -> Unit,
    onDeleteSession: (StudySessionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showManualDialog by remember { mutableStateOf(false) }
    var customSubjectInput by remember { mutableStateOf("") }
    var showSubjectInput by remember { mutableStateOf(false) }

    val timer = uiState.timer
    val displaySeconds = if (timer.mode == StudyTimerMode.STOPWATCH) {
        timer.elapsedSeconds
    } else {
        timer.remainingSeconds
    }
    val progress = when {
        timer.mode == StudyTimerMode.STOPWATCH -> ((timer.elapsedSeconds % 3600) / 3600f)
        timer.totalSeconds > 0 -> timer.elapsedSeconds.toFloat() / timer.totalSeconds.toFloat()
        else -> 0f
    }

    val dailyGoal = uiState.settings.dailyStudyGoalMinutes.coerceAtLeast(1)
    val dailyGoalRatio = (uiState.todayStudyMinutes.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)
    val weeklyHours = uiState.weeklyStudyMinutes / 60
    val weeklyRemainderMins = uiState.weeklyStudyMinutes % 60

    val subjectTotals = remember(uiState.studySessions) {
        uiState.studySessions
            .groupBy { it.subject }
            .mapValues { (_, list) -> list.sumOf { it.durationMinutes } }
            .entries
            .sortedByDescending { it.value }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Focus Timer Card
            item {
                LifeTrackCard {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        StudyTimerMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = timer.mode == mode,
                                onClick = { onSelectMode(mode) },
                                enabled = !timer.isRunning,
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = StudyTimerMode.entries.size
                                )
                            ) {
                                Text(mode.label, maxLines = 1)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(DEFAULT_SUBJECTS) { subject ->
                            FilterChip(
                                selected = timer.subject == subject,
                                onClick = { onSetSubject(subject) },
                                label = { Text(subject) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = timer.subject !in DEFAULT_SUBJECTS,
                                onClick = { showSubjectInput = true },
                                label = { Text("+ Custom") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressRing(
                            progress = progress,
                            centerTitle = DateUtils.formatDurationSeconds(displaySeconds),
                            centerSubtitle = if (timer.isRunning) "Focusing · ${timer.subject}" else timer.subject,
                            size = 204.dp,
                            strokeWidth = 14.dp,
                            gradientColors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onResetTimer,
                            modifier = Modifier.weight(1f).testTag("study_reset_button")
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Reset timer", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset")
                        }
                        Button(
                            onClick = onToggleTimer,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (timer.isRunning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1.4f).testTag("study_start_pause_button")
                        ) {
                            Icon(
                                imageVector = if (timer.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (timer.isRunning) "Pause" else "Start",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (timer.isRunning) "Pause" else "Start Focus")
                        }
                        FilledTonalButton(
                            onClick = onCompleteAndLogNow,
                            enabled = timer.elapsedSeconds >= 10,
                            modifier = Modifier.weight(1f).testTag("study_log_now_button")
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = "Save session", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save")
                        }
                    }
                }
            }

            // 2. Learning-Hour Statistics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMiniCard(
                        title = "Today's Focus",
                        value = "${uiState.todayStudyMinutes}m",
                        subtitle = "Goal ${dailyGoal}m",
                        icon = Icons.Rounded.MenuBook,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        title = "Weekly Learning",
                        value = "${weeklyHours}h ${weeklyRemainderMins}m",
                        subtitle = "${uiState.studySessions.size} logs",
                        icon = Icons.Rounded.PlayArrow,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                LifeTrackCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Study Target",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${uiState.todayStudyMinutes} / $dailyGoal min (${(dailyGoalRatio * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { dailyGoalRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }

            // 3. Subject Breakdown
            if (subjectTotals.isNotEmpty()) {
                item {
                    LifeTrackCard {
                        Text(
                            text = "Hours by Subject",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val maxSubjectMins = subjectTotals.first().value.coerceAtLeast(1)
                        subjectTotals.take(5).forEach { (subject, mins) ->
                            val ratio = mins.toFloat() / maxSubjectMins.toFloat()
                            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = subject,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${mins / 60}h ${mins % 60}m",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                            }
                        }
                    }
                }
            }

            // 4. Recent Study Logs
            item {
                SectionHeader(
                    title = "Logged Sessions",
                    actionLabel = "+ Manual Log",
                    onActionClick = { showManualDialog = true }
                )
            }

            if (uiState.studySessions.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.Add,
                        title = "No study sessions logged",
                        message = "Start the focus timer above or add a manual session log."
                    )
                }
            } else {
                items(uiState.studySessions, key = { it.id }) { session ->
                    LifeTrackCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = session.subject,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    CategoryPill(
                                        text = session.mode,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${DateUtils.formatShortDate(session.dateKey)} · ${session.durationMinutes} min" +
                                        if (session.notes.isNotBlank()) " · ${session.notes}" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteSession(session) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Delete session",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    if (showSubjectInput) {
        AlertDialog(
            onDismissRequest = { showSubjectInput = false },
            title = { Text("Custom Subject") },
            text = {
                OutlinedTextField(
                    value = customSubjectInput,
                    onValueChange = { customSubjectInput = it },
                    label = { Text("Subject or topic") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customSubjectInput.isNotBlank()) {
                            onSetSubject(customSubjectInput.trim())
                        }
                        showSubjectInput = false
                    }
                ) { Text("Set Subject") }
            },
            dismissButton = {
                TextButton(onClick = { showSubjectInput = false }) { Text("Cancel") }
            }
        )
    }

    if (showManualDialog) {
        ManualStudyLogDialog(
            defaultSubject = timer.subject,
            onDismiss = { showManualDialog = false },
            onSave = { subject, mins, mode, notes ->
                onLogManualSession(subject, mins, mode, notes)
                showManualDialog = false
            }
        )
    }
}

@Composable
private fun ManualStudyLogDialog(
    defaultSubject: String,
    onDismiss: () -> Unit,
    onSave: (String, Int, String, String) -> Unit
) {
    var subject by remember { mutableStateOf(defaultSubject) }
    var minutes by remember { mutableIntStateOf(45) }
    var mode by remember { mutableStateOf("Deep Work") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Study Session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Duration: $minutes min", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(25, 45, 60, 90).forEach { m ->
                        FilterChip(
                            selected = minutes == m,
                            onClick = { minutes = m },
                            label = { Text("${m}m") }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Pomodoro", "Deep Work", "Review").forEach { m ->
                        FilterChip(
                            selected = mode == m,
                            onClick = { mode = m },
                            label = { Text(m) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Session notes (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(subject, minutes, mode, notes) },
                enabled = subject.isNotBlank()
            ) { Text("Log Session") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
