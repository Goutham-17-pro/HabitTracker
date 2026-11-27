package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "Morning", "Deep Work", "Study", "Health", "Evening"
    val startMinutes: Int, // Minutes from midnight (e.g., 420 = 7:00 AM)
    val endMinutes: Int,
    val dateKey: String, // yyyy-MM-dd
    val isCompleted: Boolean = false,
    val priority: String = "Medium", // "High", "Medium", "Low"
    val notes: String = ""
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "Wellness", "Learning", "Focus", "Fitness"
    val accentKey: String = "emerald", // "emerald", "cyan", "violet", "amber"
    val targetPerWeek: Int = 7,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId", "dateKey"], unique = true)]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val dateKey: String // yyyy-MM-dd
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val durationMinutes: Int,
    val dateKey: String, // yyyy-MM-dd
    val mode: String = "Pomodoro", // "Pomodoro", "Deep Work", "Stopwatch"
    val notes: String = "",
    val completedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val currentProgress: Int = 0,
    val targetProgress: Int = 100,
    val unit: String = "%",
    val deadlineDateKey: String,
    val priority: String = "High",
    val isCompleted: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isUser: Boolean,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
