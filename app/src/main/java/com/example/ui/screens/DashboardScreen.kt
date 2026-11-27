package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.RoutineEntity
import com.example.ui.components.CategoryPill
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LifeTrackCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.SpringCheckCircle
import com.example.ui.components.StatMiniCard
import com.example.ui.components.categoryAccentColor
import com.example.ui.theme.LocalLifeTrackColors
import com.example.ui.viewmodel.LifeTrackUiState
import com.example.ui.viewmodel.StudyTimerMode
import com.example.util.DateUtils

@Composable
fun DashboardScreen(
    uiState: LifeTrackUiState,
    onToggleRoutine: (RoutineEntity) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleTimer: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = uiState.todayRoutines.count { it.isCompleted }
    val totalCount = uiState.todayRoutines.size
    val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
    val progressPct = (progress * 100).toInt()
    val (clockTime, clockAmPm) = DateUtils.formatLiveClock(uiState.nowMillis)
    val extraColors = LocalLifeTrackColors.current

    val activeGoalsCount = uiState.goals.count { !it.isCompleted }
    val avgGoalPct = if (uiState.goals.isEmpty()) 0 else {
        uiState.goals.map { (it.currentProgress * 100) / it.targetProgress.coerceAtLeast(1) }.average().toInt()
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
            // 1. Live Clock & Hero Status Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, extraColors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner),
                            contentDescription = "LifeTrack productivity header backdrop",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                                .alpha(if (extraColors.isDark) 0.42f else 0.14f)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${DateUtils.greetingForHour()}, ${uiState.settings.userName.substringBefore(" ")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = DateUtils.formatDisplayDate(uiState.todayKey),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onToggleDarkMode(!uiState.settings.isDarkMode) },
                                    modifier = Modifier
                                        .testTag("dashboard_theme_toggle")
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f))
                                ) {
                                    Icon(
                                        imageVector = if (uiState.settings.isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                        contentDescription = "Toggle dark or light theme",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = clockTime,
                                    style = MaterialTheme.typography.displayLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = clockAmPm,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Daily Progress Ring + Summary Card
            item {
                LifeTrackCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        CircularProgressRing(
                            progress = progress,
                            centerTitle = "$progressPct%",
                            centerSubtitle = "$completedCount/$totalCount Done",
                            size = 118.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            CategoryPill(text = "Daily Velocity", color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when {
                                    totalCount == 0 -> "Plan your day"
                                    progressPct == 100 -> "All routines complete!"
                                    progressPct >= 50 -> "Strong momentum"
                                    else -> "Steady progress ahead"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (totalCount == 0) {
                                    "Add activities to build your daily rhythm."
                                } else {
                                    "${totalCount - completedCount} task(s) left on today's schedule."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            FilledTonalButton(
                                onClick = { onNavigateToRoute("routine") },
                                modifier = Modifier.testTag("dashboard_open_routine_button")
                            ) {
                                Text("Open Planner")
                            }
                        }
                    }
                }
            }

            // 3. Active Study Timer Live Strip (always accessible)
            item {
                val timer = uiState.timer
                val displaySecs = if (timer.mode == StudyTimerMode.STOPWATCH) {
                    timer.elapsedSeconds
                } else {
                    timer.remainingSeconds
                }
                LifeTrackCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (timer.isRunning) MaterialTheme.colorScheme.secondary else extraColors.cardBorder,
                    onClick = { onNavigateToRoute("study") }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = "Study timer",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${timer.mode.label} · ${timer.subject}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (timer.isRunning) "Focusing live • ${DateUtils.formatDurationSeconds(displaySecs)}"
                                    else "Ready • ${DateUtils.formatDurationSeconds(displaySecs)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (timer.isRunning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = onToggleTimer,
                            modifier = Modifier
                                .testTag("dashboard_timer_toggle")
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f))
                        ) {
                            Icon(
                                imageVector = if (timer.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (timer.isRunning) "Pause timer" else "Start timer",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // 4. 2x2 KPI Statistics Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatMiniCard(
                            title = "Routines Done",
                            value = "$completedCount / $totalCount",
                            subtitle = "$progressPct%",
                            icon = Icons.Rounded.CheckCircle,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToRoute("routine") }
                        )
                        StatMiniCard(
                            title = "Top Habit Streak",
                            value = "${uiState.bestHabitStreak}d",
                            subtitle = "${uiState.habitsWithStats.size} habits",
                            icon = Icons.Rounded.LocalFireDepartment,
                            accentColor = extraColors.amber,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToRoute("habits") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val targetMins = uiState.settings.dailyStudyGoalMinutes.coerceAtLeast(1)
                        val studyPct = ((uiState.todayStudyMinutes * 100) / targetMins).coerceAtMost(999)
                        StatMiniCard(
                            title = "Study Today",
                            value = "${uiState.todayStudyMinutes}m",
                            subtitle = "$studyPct% of ${targetMins}m",
                            icon = Icons.Rounded.Schedule,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToRoute("study") }
                        )
                        StatMiniCard(
                            title = "Goals Progress",
                            value = "$avgGoalPct%",
                            subtitle = "$activeGoalsCount active",
                            icon = Icons.Rounded.Flag,
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToRoute("goals") }
                        )
                    }
                }
            }

            // 5. Quick Workspace Shortcuts
            item {
                val shortcuts = listOf(
                    Triple("Habits", Icons.Rounded.TrackChanges, "habits"),
                    Triple("Goals", Icons.Rounded.Flag, "goals"),
                    Triple("Analytics", Icons.Rounded.BarChart, "analytics"),
                    Triple("AI Coach", Icons.Rounded.AutoAwesome, "ai_coach")
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(shortcuts) { (label, icon, route) ->
                        QuickModuleChip(
                            label = label,
                            icon = icon,
                            onClick = { onNavigateToRoute(route) }
                        )
                    }
                }
            }

            // 6. Today's Routine Preview
            item {
                SectionHeader(
                    title = "Today's Schedule",
                    actionLabel = "Manage",
                    onActionClick = { onNavigateToRoute("routine") }
                )
            }

            if (uiState.todayRoutines.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.Add,
                        title = "No routines scheduled today",
                        message = "Tap Manage to add your morning, study, and deep work time blocks."
                    )
                }
            } else {
                items(uiState.todayRoutines, key = { it.id }) { routine ->
                    DashboardRoutineRow(
                        routine = routine,
                        onToggle = { onToggleRoutine(routine) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun QuickModuleChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, LocalLifeTrackColors.current.cardBorder),
        modifier = Modifier.testTag("shortcut_$label")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DashboardRoutineRow(
    routine: RoutineEntity,
    onToggle: () -> Unit
) {
    val accent = categoryAccentColor(routine.category)
    val bg = if (routine.isCompleted) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    LifeTrackCard(
        containerColor = bg,
        borderColor = if (routine.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else LocalLifeTrackColors.current.cardBorder,
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpringCheckCircle(
                checked = routine.isCompleted,
                onToggle = onToggle,
                accentColor = MaterialTheme.colorScheme.primary,
                contentDescription = "Toggle ${routine.title}",
                testTag = "dashboard_routine_check_${routine.id}"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (routine.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (routine.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = DateUtils.formatTimeRange(routine.startMinutes, routine.endMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            CategoryPill(text = routine.category, color = accent)
        }
    }
}
