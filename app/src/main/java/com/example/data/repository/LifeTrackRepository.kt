package com.example.data.repository

import com.example.data.local.AiMessageEntity
import com.example.data.local.GoalEntity
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.LifeTrackDao
import com.example.data.local.RoutineEntity
import com.example.data.local.StudySessionEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow

class LifeTrackRepository(
    private val dao: LifeTrackDao,
    private val prefs: UserPreferencesRepository
) {
    val allRoutines: Flow<List<RoutineEntity>> = dao.observeAllRoutines()
    val habits: Flow<List<HabitEntity>> = dao.observeHabits()
    val allHabitLogs: Flow<List<HabitLogEntity>> = dao.observeAllHabitLogs()
    val studySessions: Flow<List<StudySessionEntity>> = dao.observeStudySessions()
    val goals: Flow<List<GoalEntity>> = dao.observeGoals()
    val aiMessages: Flow<List<AiMessageEntity>> = dao.observeAiMessages()

    fun routinesForDate(dateKey: String): Flow<List<RoutineEntity>> =
        dao.observeRoutinesForDate(dateKey)

    suspend fun saveRoutine(routine: RoutineEntity) {
        dao.insertRoutine(routine)
    }

    suspend fun toggleRoutineCompletion(routine: RoutineEntity) {
        dao.updateRoutine(routine.copy(isCompleted = !routine.isCompleted))
    }

    suspend fun deleteRoutine(routine: RoutineEntity) {
        dao.deleteRoutine(routine)
    }

    suspend fun saveHabit(habit: HabitEntity) {
        dao.insertHabit(habit)
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        dao.deleteLogsForHabit(habit.id)
        dao.deleteHabit(habit)
    }

    suspend fun toggleHabitForDate(habitId: Long, dateKey: String) {
        val count = dao.countHabitLog(habitId, dateKey)
        if (count > 0) {
            dao.deleteHabitLog(habitId, dateKey)
        } else {
            dao.insertHabitLog(HabitLogEntity(habitId = habitId, dateKey = dateKey))
        }
    }

    suspend fun logStudySession(subject: String, durationMinutes: Int, mode: String, notes: String = "") {
        if (durationMinutes <= 0) return
        dao.insertStudySession(
            StudySessionEntity(
                subject = subject.trim().ifEmpty { "General Study" },
                durationMinutes = durationMinutes,
                dateKey = DateUtils.todayKey(),
                mode = mode,
                notes = notes.trim(),
                completedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteStudySession(session: StudySessionEntity) {
        dao.deleteStudySession(session)
    }

    suspend fun saveGoal(goal: GoalEntity) {
        val clampedCurrent = goal.currentProgress.coerceIn(0, goal.targetProgress.coerceAtLeast(1))
        val completed = clampedCurrent >= goal.targetProgress
        dao.insertGoal(goal.copy(currentProgress = clampedCurrent, isCompleted = completed))
    }

    suspend fun incrementGoal(goal: GoalEntity, delta: Int) {
        val next = (goal.currentProgress + delta).coerceIn(0, goal.targetProgress.coerceAtLeast(1))
        dao.updateGoal(goal.copy(currentProgress = next, isCompleted = next >= goal.targetProgress))
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        dao.deleteGoal(goal)
    }

    suspend fun insertAiMessage(isUser: Boolean, text: String) {
        dao.insertAiMessage(AiMessageEntity(isUser = isUser, text = text))
    }

    suspend fun clearAiMessages() {
        dao.clearAiMessages()
    }

    suspend fun ensureSeededIfNeeded() {
        if (!prefs.hasSeeded()) {
            seedStarterWorkspace()
            prefs.markSeeded(true)
        }
    }

    suspend fun resetToStarterData() {
        clearAllUserData()
        seedStarterWorkspace()
        prefs.markSeeded(true)
    }

    suspend fun clearAllUserData() {
        dao.clearRoutines()
        dao.clearHabitLogs()
        dao.clearHabits()
        dao.clearStudySessions()
        dao.clearGoals()
        dao.clearAiMessages()
    }

    private suspend fun seedStarterWorkspace() {
        val today = DateUtils.todayKey()
        val yesterday = DateUtils.offsetDateKey(-1)
        val twoDaysAgo = DateUtils.offsetDateKey(-2)
        val threeDaysAgo = DateUtils.offsetDateKey(-3)
        val fourDaysAgo = DateUtils.offsetDateKey(-4)
        val fiveDaysAgo = DateUtils.offsetDateKey(-5)
        val tomorrow = DateUtils.offsetDateKey(1)

        val starterRoutines = listOf(
            RoutineEntity(title = "Morning Hydration & Mobility", category = "Morning", startMinutes = 7 * 60, endMinutes = 7 * 60 + 30, dateKey = today, isCompleted = true, priority = "High", notes = "15m stretching + electrolytes"),
            RoutineEntity(title = "Daily Plan & Top 3 Priorities", category = "Morning", startMinutes = 8 * 60, endMinutes = 8 * 60 + 20, dateKey = today, isCompleted = true, priority = "High", notes = "Time-block deep work"),
            RoutineEntity(title = "System Architecture Deep Work", category = "Deep Work", startMinutes = 9 * 60, endMinutes = 11 * 60, dateKey = today, isCompleted = true, priority = "High", notes = "Zero notifications block"),
            RoutineEntity(title = "Kotlin & Compose Study Block", category = "Study", startMinutes = 13 * 60 + 30, endMinutes = 15 * 60, dateKey = today, isCompleted = false, priority = "High", notes = "Finish Canvas charts module"),
            RoutineEntity(title = "Strength Training & Cardio", category = "Health", startMinutes = 17 * 60 + 30, endMinutes = 18 * 60 + 30, dateKey = today, isCompleted = false, priority = "Medium", notes = "Zone 2 + upper body"),
            RoutineEntity(title = "Evening Wind-Down & Reading", category = "Evening", startMinutes = 21 * 60, endMinutes = 21 * 60 + 45, dateKey = today, isCompleted = false, priority = "Low", notes = "No screens after 21:30"),
            // Past days for real multi-day analytics & calendar
            RoutineEntity(title = "Morning Focus Sprint", category = "Morning", startMinutes = 7 * 60 + 30, endMinutes = 8 * 60 + 15, dateKey = yesterday, isCompleted = true, priority = "High"),
            RoutineEntity(title = "Algorithm Practice Set", category = "Study", startMinutes = 10 * 60, endMinutes = 11 * 60 + 30, dateKey = yesterday, isCompleted = true, priority = "High"),
            RoutineEntity(title = "API Design Review", category = "Deep Work", startMinutes = 14 * 60, endMinutes = 16 * 60, dateKey = yesterday, isCompleted = true, priority = "Medium"),
            RoutineEntity(title = "5K Evening Run", category = "Health", startMinutes = 18 * 60, endMinutes = 18 * 60 + 45, dateKey = yesterday, isCompleted = false, priority = "Medium"),
            RoutineEntity(title = "Deep Focus Session", category = "Deep Work", startMinutes = 9 * 60, endMinutes = 11 * 60, dateKey = twoDaysAgo, isCompleted = true, priority = "High"),
            RoutineEntity(title = "UI Polish & Testing", category = "Study", startMinutes = 14 * 60, endMinutes = 15 * 60 + 30, dateKey = twoDaysAgo, isCompleted = true, priority = "Medium"),
            RoutineEntity(title = "Weekly Sprint Kickoff", category = "Deep Work", startMinutes = 9 * 60, endMinutes = 10 * 60 + 30, dateKey = tomorrow, isCompleted = false, priority = "High"),
            RoutineEntity(title = "System Design Mock Review", category = "Study", startMinutes = 15 * 60, endMinutes = 16 * 60 + 30, dateKey = tomorrow, isCompleted = false, priority = "Medium")
        )
        dao.insertRoutines(starterRoutines)

        val starterHabits = listOf(
            HabitEntity(title = "Deep Work 90m", category = "Focus", accentKey = "violet", targetPerWeek = 6),
            HabitEntity(title = "Read 20 Pages", category = "Learning", accentKey = "cyan", targetPerWeek = 7),
            HabitEntity(title = "Morning Workout", category = "Fitness", accentKey = "emerald", targetPerWeek = 5),
            HabitEntity(title = "Zero Sugar & 2.5L Water", category = "Wellness", accentKey = "amber", targetPerWeek = 7)
        )
        val habitIds = dao.insertHabits(starterHabits)
        if (habitIds.size >= 4) {
            val h1 = habitIds[0]
            val h2 = habitIds[1]
            val h3 = habitIds[2]
            val h4 = habitIds[3]
            val logs = listOf(
                HabitLogEntity(habitId = h1, dateKey = today),
                HabitLogEntity(habitId = h1, dateKey = yesterday),
                HabitLogEntity(habitId = h1, dateKey = twoDaysAgo),
                HabitLogEntity(habitId = h1, dateKey = threeDaysAgo),
                HabitLogEntity(habitId = h1, dateKey = fourDaysAgo),
                HabitLogEntity(habitId = h2, dateKey = yesterday),
                HabitLogEntity(habitId = h2, dateKey = twoDaysAgo),
                HabitLogEntity(habitId = h2, dateKey = threeDaysAgo),
                HabitLogEntity(habitId = h3, dateKey = today),
                HabitLogEntity(habitId = h3, dateKey = twoDaysAgo),
                HabitLogEntity(habitId = h3, dateKey = fourDaysAgo),
                HabitLogEntity(habitId = h3, dateKey = fiveDaysAgo),
                HabitLogEntity(habitId = h4, dateKey = today),
                HabitLogEntity(habitId = h4, dateKey = yesterday),
                HabitLogEntity(habitId = h4, dateKey = threeDaysAgo)
            )
            dao.insertHabitLogs(logs)
        }

        val now = System.currentTimeMillis()
        val starterStudy = listOf(
            StudySessionEntity(subject = "System Architecture", durationMinutes = 50, dateKey = today, mode = "Deep Work", notes = "Event-driven patterns", completedAtMillis = now - 3_600_000L),
            StudySessionEntity(subject = "Algorithms & Graphs", durationMinutes = 25, dateKey = today, mode = "Pomodoro", notes = "Shortest path drills", completedAtMillis = now - 7_200_000L),
            StudySessionEntity(subject = "Jetpack Compose UI", durationMinutes = 75, dateKey = yesterday, mode = "Deep Work", notes = "Custom Canvas rings", completedAtMillis = now - 86_400_000L),
            StudySessionEntity(subject = "System Architecture", durationMinutes = 60, dateKey = twoDaysAgo, mode = "Deep Work", notes = "Caching strategies", completedAtMillis = now - 172_800_000L),
            StudySessionEntity(subject = "Product Strategy", durationMinutes = 45, dateKey = threeDaysAgo, mode = "Pomodoro", notes = "Metrics review", completedAtMillis = now - 259_200_000L),
            StudySessionEntity(subject = "Algorithms & Graphs", durationMinutes = 90, dateKey = fourDaysAgo, mode = "Deep Work", notes = "DP practice", completedAtMillis = now - 345_600_000L),
            StudySessionEntity(subject = "Jetpack Compose UI", durationMinutes = 50, dateKey = fiveDaysAgo, mode = "Deep Work", notes = "Animation specs", completedAtMillis = now - 432_000_000L)
        )
        dao.insertStudySessions(starterStudy)

        val starterGoals = listOf(
            GoalEntity(
                title = "Complete Advanced Android Architecture Track",
                category = "Learning",
                currentProgress = 14,
                targetProgress = 20,
                unit = "modules",
                deadlineDateKey = DateUtils.offsetDateKey(18),
                priority = "High",
                notes = "Focus on offline-first sync & adaptive layouts"
            ),
            GoalEntity(
                title = "Log 60 Hours of Deep Study",
                category = "Study",
                currentProgress = 42,
                targetProgress = 60,
                unit = "hours",
                deadlineDateKey = DateUtils.offsetDateKey(25),
                priority = "High",
                notes = "Maintain 2h+ average on weekdays"
            ),
            GoalEntity(
                title = "30-Day Morning Routine Consistency",
                category = "Wellness",
                currentProgress = 19,
                targetProgress = 30,
                unit = "days",
                deadlineDateKey = DateUtils.offsetDateKey(11),
                priority = "Medium",
                notes = "No snooze + hydration before coffee"
            ),
            GoalEntity(
                title = "Ship Personal Portfolio v2",
                category = "Career",
                currentProgress = 100,
                targetProgress = 100,
                unit = "%",
                deadlineDateKey = DateUtils.offsetDateKey(-2),
                priority = "Medium",
                isCompleted = true,
                notes = "Deployed with dark showcase"
            )
        )
        dao.insertGoals(starterGoals)

        dao.insertAiMessage(
            AiMessageEntity(
                isUser = false,
                text = "[Sample Placeholder Response]\nWelcome to LifeTrack AI Coach! Ask me to optimize your daily schedule, review your habit streaks, or suggest a study plan based on your tracked progress.",
                timestampMillis = now - 60_000L
            )
        )
    }

    companion object {
        fun calculateCurrentStreak(habitId: Long, logs: List<HabitLogEntity>, todayKey: String = DateUtils.todayKey()): Int {
            val dateSet = logs.asSequence().filter { it.habitId == habitId }.map { it.dateKey }.toSet()
            if (dateSet.isEmpty()) return 0
            var streak = 0
            var cursorOffset = if (dateSet.contains(todayKey)) 0 else -1
            while (true) {
                val key = DateUtils.offsetDateKey(cursorOffset, todayKey)
                if (dateSet.contains(key)) {
                    streak++
                    cursorOffset--
                } else {
                    break
                }
            }
            return streak
        }
    }
}
