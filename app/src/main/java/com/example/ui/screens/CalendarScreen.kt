package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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

enum class CalendarViewMode(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

@Composable
fun CalendarScreen(
    uiState: LifeTrackUiState,
    onSelectDate: (String) -> Unit,
    onToggleRoutine: (RoutineEntity) -> Unit,
    onOpenRoutinePlannerForDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTHLY) }
    val (initYear, initMonth) = remember { DateUtils.currentYearMonth() }
    var currentYear by remember { mutableIntStateOf(initYear) }
    var currentMonth by remember { mutableIntStateOf(initMonth) }

    val selectedDate = uiState.selectedCalendarDateKey
    val routinesOnDate = remember(uiState.allRoutines, selectedDate) {
        uiState.allRoutines.filter { it.dateKey == selectedDate }.sortedBy { it.startMinutes }
    }
    val studyOnDate = remember(uiState.studySessions, selectedDate) {
        uiState.studySessions.filter { it.dateKey == selectedDate }
    }
    val goalsDueOnDate = remember(uiState.goals, selectedDate) {
        uiState.goals.filter { it.deadlineDateKey == selectedDate }
    }

    val routineDatesSet = remember(uiState.allRoutines) {
        uiState.allRoutines.map { it.dateKey }.toSet()
    }
    val studyDatesSet = remember(uiState.studySessions) {
        uiState.studySessions.map { it.dateKey }.toSet()
    }
    val goalDatesSet = remember(uiState.goals) {
        uiState.goals.map { it.deadlineDateKey }.toSet()
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    CalendarViewMode.entries.forEachIndexed { idx, mode ->
                        SegmentedButton(
                            selected = viewMode == mode,
                            onClick = { viewMode = mode },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = idx,
                                count = CalendarViewMode.entries.size
                            )
                        ) {
                            Text(mode.label)
                        }
                    }
                }
            }

            when (viewMode) {
                CalendarViewMode.MONTHLY -> {
                    item {
                        val cells = remember(currentYear, currentMonth) {
                            DateUtils.monthCalendarCells(currentYear, currentMonth)
                        }
                        LifeTrackCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (currentMonth == 0) {
                                            currentMonth = 11
                                            currentYear -= 1
                                        } else {
                                            currentMonth -= 1
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                        contentDescription = "Previous month"
                                    )
                                }
                                Text(
                                    text = DateUtils.formatMonthYear(currentYear, currentMonth),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = {
                                        if (currentMonth == 11) {
                                            currentMonth = 0
                                            currentYear += 1
                                        } else {
                                            currentMonth += 1
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        contentDescription = "Next month"
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                                    Text(
                                        text = d,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            cells.chunked(7).forEach { weekRow ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    weekRow.forEach { (dateKey, inMonth) ->
                                        val isSelected = dateKey == selectedDate
                                        val isToday = dateKey == uiState.todayKey
                                        val hasRoutine = dateKey in routineDatesSet
                                        val hasStudy = dateKey in studyDatesSet
                                        val hasGoal = dateKey in goalDatesSet

                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    when {
                                                        isSelected -> MaterialTheme.colorScheme.primary
                                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                                        else -> MaterialTheme.colorScheme.surface
                                                    }
                                                )
                                                .clickable { onSelectDate(dateKey) }
                                                .padding(vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "${DateUtils.formatDayNumber(dateKey)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                                    !inMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                if (hasRoutine) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                                else MaterialTheme.colorScheme.primary
                                                            )
                                                    )
                                                }
                                                if (hasStudy) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                                else MaterialTheme.colorScheme.secondary
                                                            )
                                                    )
                                                }
                                                if (hasGoal) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                                else MaterialTheme.colorScheme.tertiary
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                CalendarViewMode.WEEKLY -> {
                    val weekKeys = DateUtils.weekKeysAround(selectedDate)
                    items(weekKeys) { dayKey ->
                        val dayRoutines = uiState.allRoutines.filter { it.dateKey == dayKey }.sortedBy { it.startMinutes }
                        val dayStudy = uiState.studySessions.filter { it.dateKey == dayKey }.sumOf { it.durationMinutes }
                        val isSelected = dayKey == selectedDate
                        LifeTrackCard(
                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else LocalLifeTrackColors.current.cardBorder,
                            onClick = { onSelectDate(dayKey) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = DateUtils.formatDisplayDate(dayKey),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (dayKey == uiState.todayKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${dayRoutines.count { it.isCompleted }}/${dayRoutines.size} tasks · ${dayStudy}m study",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            if (dayRoutines.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                dayRoutines.take(3).forEach { r ->
                                    Text(
                                        text = "• ${DateUtils.formatMinutesOfDay(r.startMinutes)}  ${r.title}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                CalendarViewMode.DAILY -> {
                    item {
                        LifeTrackCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { onSelectDate(DateUtils.offsetDateKey(-1, selectedDate)) }) {
                                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous day")
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = DateUtils.formatDisplayDate(selectedDate),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${routinesOnDate.size} activities · ${studyOnDate.sumOf { it.durationMinutes }}m focus",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { onSelectDate(DateUtils.offsetDateKey(1, selectedDate)) }) {
                                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next day")
                                }
                            }
                        }
                    }
                }
            }

            // Selected Day Details (shown below Monthly & Daily views)
            if (viewMode != CalendarViewMode.WEEKLY) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Agenda · ${DateUtils.formatShortDate(selectedDate)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        FilledTonalButton(onClick = { onOpenRoutinePlannerForDate(selectedDate) }) {
                            Text("Edit in Planner")
                        }
                    }
                }

                if (routinesOnDate.isEmpty() && studyOnDate.isEmpty() && goalsDueOnDate.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.CalendarMonth,
                            title = "Clear schedule on ${DateUtils.formatShortDate(selectedDate)}",
                            message = "Tap Edit in Planner to schedule routine blocks for this date."
                        )
                    }
                } else {
                    items(routinesOnDate, key = { "cal_r_${it.id}" }) { routine ->
                        LifeTrackCard(onClick = { onToggleRoutine(routine) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SpringCheckCircle(
                                    checked = routine.isCompleted,
                                    onToggle = { onToggleRoutine(routine) },
                                    contentDescription = "Toggle ${routine.title}"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = routine.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = DateUtils.formatTimeRange(routine.startMinutes, routine.endMinutes),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                CategoryPill(text = routine.category, color = categoryAccentColor(routine.category))
                            }
                        }
                    }

                    items(studyOnDate, key = { "cal_s_${it.id}" }) { session ->
                        LifeTrackCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.MenuBook, contentDescription = "Study", tint = MaterialTheme.colorScheme.secondary)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(session.subject, style = MaterialTheme.typography.titleSmall)
                                    Text("${session.durationMinutes}m · ${session.mode}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    items(goalsDueOnDate, key = { "cal_g_${it.id}" }) { goal ->
                        LifeTrackCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.Flag, contentDescription = "Goal deadline", tint = MaterialTheme.colorScheme.tertiary)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Deadline: ${goal.title}", style = MaterialTheme.typography.titleSmall)
                                    Text("${goal.currentProgress}/${goal.targetProgress} ${goal.unit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
