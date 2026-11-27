package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.LifeTrackCard
import com.example.ui.components.ProductivityBarChart
import com.example.ui.components.StatMiniCard
import com.example.ui.components.StudyTrendChart
import com.example.ui.theme.LocalLifeTrackColors
import com.example.ui.viewmodel.LifeTrackUiState

@Composable
fun AnalyticsScreen(
    uiState: LifeTrackUiState,
    modifier: Modifier = Modifier
) {
    val activeDays = uiState.weeklyTrend.filter { it.routineTotal > 0 }
    val avgCompletion = if (activeDays.isEmpty()) 0 else {
        activeDays.map { it.completionPercent }.average().toInt()
    }
    val completedGoals = uiState.goals.count { it.isCompleted }
    val totalGoals = uiState.goals.size
    val totalHabitsTarget = uiState.habitsWithStats.sumOf { it.habit.targetPerWeek }.coerceAtLeast(1)
    val totalHabitsDone = uiState.habitsWithStats.sumOf { it.completedThisWeek }
    val habitConsistencyPct = ((totalHabitsDone * 100) / totalHabitsTarget).coerceIn(0, 100)
    val extraColors = LocalLifeTrackColors.current

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
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatMiniCard(
                            title = "7-Day Routine Avg",
                            value = "$avgCompletion%",
                            subtitle = "${activeDays.sumOf { it.routineCompleted }} done",
                            icon = Icons.Rounded.CheckCircle,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatMiniCard(
                            title = "Weekly Study",
                            value = "${uiState.weeklyStudyMinutes / 60}h ${uiState.weeklyStudyMinutes % 60}m",
                            subtitle = "${uiState.studySessions.size} sessions",
                            icon = Icons.Rounded.Schedule,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatMiniCard(
                            title = "Habit Consistency",
                            value = "$habitConsistencyPct%",
                            subtitle = "${uiState.bestHabitStreak}d streak",
                            icon = Icons.Rounded.LocalFireDepartment,
                            accentColor = extraColors.amber,
                            modifier = Modifier.weight(1f)
                        )
                        StatMiniCard(
                            title = "Goals Completed",
                            value = "$completedGoals / $totalGoals",
                            subtitle = "${totalGoals - completedGoals} active",
                            icon = Icons.Rounded.Flag,
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                LifeTrackCard {
                    Text(
                        text = "Routine Completion (Last 7 Days)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap any day bar to inspect exact completion statistics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ProductivityBarChart(points = uiState.weeklyTrend)
                }
            }

            item {
                LifeTrackCard {
                    Text(
                        text = "Study Hours & Focus Trend",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Dashed line marks your ${uiState.settings.dailyStudyGoalMinutes}m daily study goal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    StudyTrendChart(
                        points = uiState.weeklyTrend,
                        dailyTargetMinutes = uiState.settings.dailyStudyGoalMinutes
                    )
                }
            }

            item {
                LifeTrackCard {
                    Text(
                        text = "Focus & Routine Category Split",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    CategoryDonutChart(distribution = uiState.categoryDistribution)
                }
            }

            if (uiState.habitsWithStats.isNotEmpty()) {
                item {
                    LifeTrackCard {
                        Text(
                            text = "Habit Adherence Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        uiState.habitsWithStats.forEach { stat ->
                            val ratio = (stat.completedThisWeek.toFloat() / stat.habit.targetPerWeek.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stat.habit.title, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "${stat.completedThisWeek}/${stat.habit.targetPerWeek} · ${stat.currentStreak}d",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
