package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AiMessageEntity
import com.example.data.local.GoalEntity
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.LifeTrackDatabase
import com.example.data.local.RoutineEntity
import com.example.data.local.StudySessionEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserProfileSettings
import com.example.data.repository.AiRepository
import com.example.data.repository.LifeTrackRepository
import com.example.data.repository.PlaceholderAiRepository
import com.example.data.repository.ProductivitySnapshot
import com.example.util.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class StudyTimerMode(val label: String) {
    POMODORO("Pomodoro"),
    DEEP_WORK("Deep Work"),
    STOPWATCH("Stopwatch")
}

data class StudyTimerState(
    val mode: StudyTimerMode = StudyTimerMode.POMODORO,
    val subject: String = "System Architecture",
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val elapsedSeconds: Int = 0,
    val isRunning: Boolean = false,
    val lastCompletedMessage: String? = null
)

data class HabitWithStats(
    val habit: HabitEntity,
    val currentStreak: Int,
    val completedThisWeek: Int,
    val weekCompletionMap: Map<String, Boolean>
)

data class DailyProductivityPoint(
    val dateKey: String,
    val dayLabel: String,
    val routineTotal: Int,
    val routineCompleted: Int,
    val completionPercent: Int,
    val studyMinutes: Int
)

data class LifeTrackUiState(
    val isLoading: Boolean = true,
    val nowMillis: Long = System.currentTimeMillis(),
    val todayKey: String = DateUtils.todayKey(),
    val settings: UserProfileSettings = UserProfileSettings(),
    val selectedRoutineDateKey: String = DateUtils.todayKey(),
    val selectedCalendarDateKey: String = DateUtils.todayKey(),
    val allRoutines: List<RoutineEntity> = emptyList(),
    val todayRoutines: List<RoutineEntity> = emptyList(),
    val selectedDateRoutines: List<RoutineEntity> = emptyList(),
    val habitsWithStats: List<HabitWithStats> = emptyList(),
    val allHabitLogs: List<HabitLogEntity> = emptyList(),
    val studySessions: List<StudySessionEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val aiMessages: List<AiMessageEntity> = emptyList(),
    val isAiGenerating: Boolean = false,
    val isLiveAiConnected: Boolean = false,
    val timer: StudyTimerState = StudyTimerState(),
    val weeklyTrend: List<DailyProductivityPoint> = emptyList(),
    val todayStudyMinutes: Int = 0,
    val weeklyStudyMinutes: Int = 0,
    val bestHabitStreak: Int = 0,
    val categoryDistribution: Map<String, Int> = emptyMap(),
    val snackbarMessage: String? = null
)

private data class CoreDbBundle(
    val routines: List<RoutineEntity>,
    val habits: List<HabitEntity>,
    val habitLogs: List<HabitLogEntity>,
    val studySessions: List<StudySessionEntity>,
    val goals: List<GoalEntity>,
    val aiMessages: List<AiMessageEntity>
)

private data class LocalControlBundle(
    val nowMillis: Long,
    val selectedRoutineDate: String,
    val selectedCalendarDate: String,
    val timer: StudyTimerState,
    val isAiGenerating: Boolean,
    val snackbarMessage: String?
)

class LifeTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = UserPreferencesRepository(application)
    private val dao = LifeTrackDatabase.getInstance(application).dao()
    private val repository = LifeTrackRepository(dao, prefsRepo)
    private val aiRepository: AiRepository = PlaceholderAiRepository()

    private val nowMillisFlow = MutableStateFlow(System.currentTimeMillis())
    private val selectedRoutineDateFlow = MutableStateFlow(DateUtils.todayKey())
    private val selectedCalendarDateFlow = MutableStateFlow(DateUtils.todayKey())
    private val timerStateFlow = MutableStateFlow(StudyTimerState())
    private val isAiGeneratingFlow = MutableStateFlow(false)
    private val snackbarMessageFlow = MutableStateFlow<String?>(null)

    private var timerJob: Job? = null

    private val coreDbFlow = combine(
        repository.allRoutines,
        repository.habits,
        repository.allHabitLogs,
        repository.studySessions,
        repository.goals,
        repository.aiMessages
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        CoreDbBundle(
            routines = values[0] as List<RoutineEntity>,
            habits = values[1] as List<HabitEntity>,
            habitLogs = values[2] as List<HabitLogEntity>,
            studySessions = values[3] as List<StudySessionEntity>,
            goals = values[4] as List<GoalEntity>,
            aiMessages = values[5] as List<AiMessageEntity>
        )
    }

    private val localControlFlow = combine(
        nowMillisFlow,
        selectedRoutineDateFlow,
        selectedCalendarDateFlow,
        timerStateFlow,
        isAiGeneratingFlow,
        snackbarMessageFlow
    ) { values ->
        LocalControlBundle(
            nowMillis = values[0] as Long,
            selectedRoutineDate = values[1] as String,
            selectedCalendarDate = values[2] as String,
            timer = values[3] as StudyTimerState,
            isAiGenerating = values[4] as Boolean,
            snackbarMessage = values[5] as String?
        )
    }

    val uiState: StateFlow<LifeTrackUiState> = combine(
        coreDbFlow,
        prefsRepo.settingsFlow,
        localControlFlow
    ) { db, settings, local ->
        val today = DateUtils.todayKey()
        val weekKeys = DateUtils.weekKeysAround(today)
        val last7Keys = DateUtils.lastNDaysKeys(7, today)

        val todayRoutines = db.routines.filter { it.dateKey == today }.sortedBy { it.startMinutes }
        val selectedDateRoutines = db.routines
            .filter { it.dateKey == local.selectedRoutineDate }
            .sortedBy { it.startMinutes }

        val habitsWithStats = db.habits.map { habit ->
            val logSet = db.habitLogs.asSequence()
                .filter { it.habitId == habit.id }
                .map { it.dateKey }
                .toSet()
            val weekMap = weekKeys.associateWith { key -> logSet.contains(key) }
            val completedThisWeek = weekMap.values.count { it }
            val streak = LifeTrackRepository.calculateCurrentStreak(habit.id, db.habitLogs, today)
            HabitWithStats(
                habit = habit,
                currentStreak = streak,
                completedThisWeek = completedThisWeek,
                weekCompletionMap = weekMap
            )
        }

        val weeklyTrend = last7Keys.map { key ->
            val dayRoutines = db.routines.filter { it.dateKey == key }
            val completed = dayRoutines.count { it.isCompleted }
            val total = dayRoutines.size
            val pct = if (total == 0) 0 else (completed * 100) / total
            val studyMins = db.studySessions.filter { it.dateKey == key }.sumOf { it.durationMinutes }
            DailyProductivityPoint(
                dateKey = key,
                dayLabel = DateUtils.formatDayShort(key),
                routineTotal = total,
                routineCompleted = completed,
                completionPercent = pct,
                studyMinutes = studyMins
            )
        }

        val todayStudyMinutes = db.studySessions
            .filter { it.dateKey == today }
            .sumOf { it.durationMinutes }

        val last7Set = last7Keys.toSet()
        val weeklyStudyMinutes = db.studySessions
            .filter { it.dateKey in last7Set }
            .sumOf { it.durationMinutes }

        val bestStreak = habitsWithStats.maxOfOrNull { it.currentStreak } ?: 0

        val categoryDist = db.routines.groupingBy { it.category }.eachCount()

        LifeTrackUiState(
            isLoading = false,
            nowMillis = local.nowMillis,
            todayKey = today,
            settings = settings,
            selectedRoutineDateKey = local.selectedRoutineDate,
            selectedCalendarDateKey = local.selectedCalendarDate,
            allRoutines = db.routines,
            todayRoutines = todayRoutines,
            selectedDateRoutines = selectedDateRoutines,
            habitsWithStats = habitsWithStats,
            allHabitLogs = db.habitLogs,
            studySessions = db.studySessions,
            goals = db.goals,
            aiMessages = db.aiMessages,
            isAiGenerating = local.isAiGenerating,
            isLiveAiConnected = aiRepository.isLiveModelConnected,
            timer = local.timer,
            weeklyTrend = weeklyTrend,
            todayStudyMinutes = todayStudyMinutes,
            weeklyStudyMinutes = weeklyStudyMinutes,
            bestHabitStreak = bestStreak,
            categoryDistribution = categoryDist,
            snackbarMessage = local.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LifeTrackUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeededIfNeeded()
        }
        viewModelScope.launch {
            while (isActive) {
                nowMillisFlow.value = System.currentTimeMillis()
                delay(1000L)
            }
        }
    }

    fun selectRoutineDate(dateKey: String) {
        selectedRoutineDateFlow.value = dateKey
    }

    fun selectCalendarDate(dateKey: String) {
        selectedCalendarDateFlow.value = dateKey
    }

    fun clearSnackbar() {
        snackbarMessageFlow.value = null
    }

    // Routine Actions
    fun saveRoutine(
        existingId: Long = 0L,
        title: String,
        category: String,
        startMinutes: Int,
        endMinutes: Int,
        dateKey: String,
        priority: String,
        notes: String,
        isCompleted: Boolean = false
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val normalizedEnd = endMinutes.coerceAtLeast(startMinutes + 15).coerceAtMost(23 * 60 + 59)
        viewModelScope.launch {
            repository.saveRoutine(
                RoutineEntity(
                    id = existingId,
                    title = cleanTitle,
                    category = category,
                    startMinutes = startMinutes,
                    endMinutes = normalizedEnd,
                    dateKey = dateKey,
                    isCompleted = isCompleted,
                    priority = priority,
                    notes = notes.trim()
                )
            )
            snackbarMessageFlow.value = if (existingId == 0L) "Routine block added" else "Routine updated"
        }
    }

    fun toggleRoutineCompletion(routine: RoutineEntity) {
        viewModelScope.launch {
            repository.toggleRoutineCompletion(routine)
        }
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
            snackbarMessageFlow.value = "Routine removed"
        }
    }

    // Habit Actions
    fun saveHabit(
        existingId: Long = 0L,
        title: String,
        category: String,
        accentKey: String,
        targetPerWeek: Int
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        viewModelScope.launch {
            repository.saveHabit(
                HabitEntity(
                    id = existingId,
                    title = cleanTitle,
                    category = category,
                    accentKey = accentKey,
                    targetPerWeek = targetPerWeek.coerceIn(1, 7)
                )
            )
            snackbarMessageFlow.value = if (existingId == 0L) "Habit created" else "Habit updated"
        }
    }

    fun toggleHabitDate(habitId: Long, dateKey: String) {
        viewModelScope.launch {
            repository.toggleHabitForDate(habitId, dateKey)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            snackbarMessageFlow.value = "Habit deleted"
        }
    }

    // Study Timer Actions
    fun selectTimerMode(mode: StudyTimerMode) {
        if (timerStateFlow.value.isRunning) return
        val settings = uiState.value.settings
        val durationSec = when (mode) {
            StudyTimerMode.POMODORO -> settings.pomodoroMinutes * 60
            StudyTimerMode.DEEP_WORK -> settings.deepWorkMinutes * 60
            StudyTimerMode.STOPWATCH -> 0
        }
        timerStateFlow.update {
            it.copy(
                mode = mode,
                totalSeconds = durationSec,
                remainingSeconds = durationSec,
                elapsedSeconds = 0,
                lastCompletedMessage = null
            )
        }
    }

    fun setTimerSubject(subject: String) {
        timerStateFlow.update { it.copy(subject = subject) }
    }

    fun toggleTimerRunning() {
        val current = timerStateFlow.value
        if (current.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        if (timerStateFlow.value.isRunning) return
        timerStateFlow.update { it.copy(isRunning = true, lastCompletedMessage = null) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && timerStateFlow.value.isRunning) {
                delay(1000L)
                val state = timerStateFlow.value
                if (!state.isRunning) break
                if (state.mode == StudyTimerMode.STOPWATCH) {
                    timerStateFlow.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                } else {
                    val nextRem = (state.remainingSeconds - 1).coerceAtLeast(0)
                    val nextElapsed = state.elapsedSeconds + 1
                    if (nextRem == 0) {
                        val loggedMinutes = (state.totalSeconds / 60).coerceAtLeast(1)
                        val subject = state.subject
                        val modeLabel = state.mode.label
                        timerStateFlow.update {
                            it.copy(
                                isRunning = false,
                                remainingSeconds = state.totalSeconds,
                                elapsedSeconds = 0,
                                lastCompletedMessage = "Logged ${loggedMinutes}m of $subject"
                            )
                        }
                        repository.logStudySession(subject, loggedMinutes, modeLabel, "Completed timer session")
                        snackbarMessageFlow.value = "Focus session logged: ${loggedMinutes}m of $subject"
                        break
                    } else {
                        timerStateFlow.update {
                            it.copy(remainingSeconds = nextRem, elapsedSeconds = nextElapsed)
                        }
                    }
                }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        timerStateFlow.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        timerJob?.cancel()
        val mode = timerStateFlow.value.mode
        val settings = uiState.value.settings
        val durationSec = when (mode) {
            StudyTimerMode.POMODORO -> settings.pomodoroMinutes * 60
            StudyTimerMode.DEEP_WORK -> settings.deepWorkMinutes * 60
            StudyTimerMode.STOPWATCH -> 0
        }
        timerStateFlow.update {
            it.copy(
                isRunning = false,
                totalSeconds = durationSec,
                remainingSeconds = durationSec,
                elapsedSeconds = 0
            )
        }
    }

    fun completeAndLogTimerNow() {
        timerJob?.cancel()
        val state = timerStateFlow.value
        val mins = (state.elapsedSeconds / 60).coerceAtLeast(if (state.elapsedSeconds >= 10) 1 else 0)
        if (mins <= 0) {
            snackbarMessageFlow.value = "Run the timer for at least 10 seconds to log a session"
            resetTimer()
            return
        }
        viewModelScope.launch {
            repository.logStudySession(state.subject, mins, state.mode.label, "Saved from focus timer")
            snackbarMessageFlow.value = "Saved ${mins}m study session for ${state.subject}"
        }
        resetTimer()
    }

    fun logManualStudySession(subject: String, durationMinutes: Int, mode: String, notes: String) {
        viewModelScope.launch {
            repository.logStudySession(subject, durationMinutes, mode, notes)
            snackbarMessageFlow.value = "Study session logged (${durationMinutes}m)"
        }
    }

    fun deleteStudySession(session: StudySessionEntity) {
        viewModelScope.launch {
            repository.deleteStudySession(session)
            snackbarMessageFlow.value = "Study session deleted"
        }
    }

    // Goal Actions
    fun saveGoal(
        existingId: Long = 0L,
        title: String,
        category: String,
        currentProgress: Int,
        targetProgress: Int,
        unit: String,
        deadlineDateKey: String,
        priority: String,
        notes: String
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        viewModelScope.launch {
            repository.saveGoal(
                GoalEntity(
                    id = existingId,
                    title = cleanTitle,
                    category = category,
                    currentProgress = currentProgress,
                    targetProgress = targetProgress.coerceAtLeast(1),
                    unit = unit.trim().ifEmpty { "%" },
                    deadlineDateKey = deadlineDateKey,
                    priority = priority,
                    notes = notes.trim()
                )
            )
            snackbarMessageFlow.value = if (existingId == 0L) "Goal added" else "Goal updated"
        }
    }

    fun incrementGoal(goal: GoalEntity, delta: Int) {
        viewModelScope.launch {
            repository.incrementGoal(goal, delta)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            snackbarMessageFlow.value = "Goal removed"
        }
    }

    // AI Assistant Actions
    fun askAiCoach(prompt: String) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isEmpty() || isAiGeneratingFlow.value) return
        val current = uiState.value
        val snapshot = ProductivitySnapshot(
            userName = current.settings.userName,
            completedRoutinesToday = current.todayRoutines.count { it.isCompleted },
            totalRoutinesToday = current.todayRoutines.size,
            pendingRoutines = current.todayRoutines.filter { !it.isCompleted },
            bestHabitStreak = current.bestHabitStreak,
            todayStudyMinutes = current.todayStudyMinutes,
            dailyStudyTargetMinutes = current.settings.dailyStudyGoalMinutes,
            weeklyStudyMinutes = current.weeklyStudyMinutes,
            activeGoals = current.goals.filter { !it.isCompleted },
            recentSessions = current.studySessions.take(5)
        )

        viewModelScope.launch {
            isAiGeneratingFlow.value = true
            try {
                repository.insertAiMessage(isUser = true, text = cleanPrompt)
                val reply = aiRepository.generateProductivityAdvice(cleanPrompt, snapshot)
                repository.insertAiMessage(isUser = false, text = reply)
            } finally {
                isAiGeneratingFlow.value = false
            }
        }
    }

    fun clearAiConversation() {
        viewModelScope.launch {
            repository.clearAiMessages()
        }
    }

    // Settings Actions
    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            prefsRepo.setDarkMode(isDark)
        }
    }

    fun updateProfile(name: String, role: String, email: String) {
        viewModelScope.launch {
            prefsRepo.updateProfile(name, role, email)
            snackbarMessageFlow.value = "Profile updated"
        }
    }

    fun updateStudyTargets(dailyGoalMinutes: Int, pomodoroMinutes: Int, deepWorkMinutes: Int) {
        viewModelScope.launch {
            prefsRepo.updateStudyTargets(dailyGoalMinutes, pomodoroMinutes, deepWorkMinutes)
            if (!timerStateFlow.value.isRunning) {
                selectTimerMode(timerStateFlow.value.mode)
            }
            snackbarMessageFlow.value = "Timer & study targets saved"
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setNotificationsEnabled(enabled)
        }
    }

    fun resetStarterWorkspace() {
        viewModelScope.launch {
            repository.resetToStarterData()
            snackbarMessageFlow.value = "Starter workspace restored"
        }
    }

    fun clearAllWorkspaceData() {
        viewModelScope.launch {
            repository.clearAllUserData()
            snackbarMessageFlow.value = "All workspace data cleared"
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
