package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeTrackDao {
    // Routines
    @Query("SELECT * FROM routines ORDER BY dateKey ASC, startMinutes ASC")
    fun observeAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE dateKey = :dateKey ORDER BY startMinutes ASC")
    fun observeRoutinesForDate(dateKey: String): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<RoutineEntity>)

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routines")
    suspend fun clearRoutines()

    // Habits & Logs
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit_logs")
    fun observeAllHabitLogs(): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>): List<Long>

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabitLog(log: HabitLogEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabitLogs(logs: List<HabitLogEntity>)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateKey = :dateKey")
    suspend fun deleteHabitLog(habitId: Long, dateKey: String)

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND dateKey = :dateKey")
    suspend fun countHabitLog(habitId: Long, dateKey: String): Int

    @Query("DELETE FROM habits")
    suspend fun clearHabits()

    @Query("DELETE FROM habit_logs")
    suspend fun clearHabitLogs()

    // Study Sessions
    @Query("SELECT * FROM study_sessions ORDER BY completedAtMillis DESC")
    fun observeStudySessions(): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySessions(sessions: List<StudySessionEntity>)

    @Delete
    suspend fun deleteStudySession(session: StudySessionEntity)

    @Query("DELETE FROM study_sessions")
    suspend fun clearStudySessions()

    // Goals
    @Query("SELECT * FROM goals ORDER BY isCompleted ASC, deadlineDateKey ASC")
    fun observeGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM goals")
    suspend fun clearGoals()

    // AI Messages
    @Query("SELECT * FROM ai_messages ORDER BY timestampMillis ASC")
    fun observeAiMessages(): Flow<List<AiMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiMessage(message: AiMessageEntity): Long

    @Query("DELETE FROM ai_messages")
    suspend fun clearAiMessages()
}
