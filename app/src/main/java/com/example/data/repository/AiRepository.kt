package com.example.data.repository

import com.example.data.local.GoalEntity
import com.example.data.local.RoutineEntity
import com.example.data.local.StudySessionEntity
import kotlinx.coroutines.delay

data class ProductivitySnapshot(
    val userName: String,
    val completedRoutinesToday: Int,
    val totalRoutinesToday: Int,
    val pendingRoutines: List<RoutineEntity>,
    val bestHabitStreak: Int,
    val todayStudyMinutes: Int,
    val dailyStudyTargetMinutes: Int,
    val weeklyStudyMinutes: Int,
    val activeGoals: List<GoalEntity>,
    val recentSessions: List<StudySessionEntity>
)

interface AiRepository {
    val isLiveModelConnected: Boolean
    suspend fun generateProductivityAdvice(
        userPrompt: String,
        snapshot: ProductivitySnapshot
    ): String
}

/**
 * Placeholder implementation used when Firebase AI Logic is not enabled.
 * Routes all AI coach requests through [AiRepository] and returns clearly labeled
 * sample placeholder responses derived from the user's actual local data.
 */
class PlaceholderAiRepository : AiRepository {
    override val isLiveModelConnected: Boolean = false

    override suspend fun generateProductivityAdvice(
        userPrompt: String,
        snapshot: ProductivitySnapshot
    ): String {
        delay(550L)
        val lower = userPrompt.lowercase()
        val routinePct = if (snapshot.totalRoutinesToday == 0) 0 else {
            (snapshot.completedRoutinesToday * 100) / snapshot.totalRoutinesToday
        }
        val remainingMinutes = (snapshot.dailyStudyTargetMinutes - snapshot.todayStudyMinutes).coerceAtLeast(0)
        val nextTask = snapshot.pendingRoutines.firstOrNull()?.title ?: "your next priority block"
        val topGoal = snapshot.activeGoals.firstOrNull()?.title ?: "your active goal"

        val body = when {
            "schedule" in lower || "optimize" in lower || "plan" in lower -> buildString {
                appendLine("Suggested Focus Blocks for ${snapshot.userName}:")
                appendLine("1. Next Priority: Tackle \"$nextTask\" first while cognitive energy is highest.")
                if (remainingMinutes > 0) {
                    appendLine("2. Study Target: Add a ${remainingMinutes.coerceAtMost(50)}-minute Deep Work sprint to stay on pace for your ${snapshot.dailyStudyTargetMinutes}m daily goal.")
                } else {
                    appendLine("2. Study Target: You already hit today's study target (${snapshot.todayStudyMinutes}m) — use remaining time for light review.")
                }
                appendLine("3. Goal Alignment: Dedicate 25m toward \"$topGoal\" before your evening wind-down.")
            }

            "progress" in lower || "analy" in lower || "summary" in lower || "weekly" in lower -> buildString {
                appendLine("Current Progress Snapshot:")
                appendLine("• Today's Routine: ${snapshot.completedRoutinesToday}/${snapshot.totalRoutinesToday} tasks completed ($routinePct%).")
                appendLine("• Study Momentum: ${snapshot.todayStudyMinutes}m today · ${snapshot.weeklyStudyMinutes / 60}h ${snapshot.weeklyStudyMinutes % 60}m over the last 7 days.")
                appendLine("• Habit Consistency: Top active streak is ${snapshot.bestHabitStreak} day(s).")
                appendLine("• Recommendation: Protect one uninterrupted morning block tomorrow to push routine completion above 85%.")
            }

            "study" in lower || "learn" in lower || "focus" in lower -> buildString {
                appendLine("Study Plan Recommendation:")
                val topSubject = snapshot.recentSessions.firstOrNull()?.subject ?: "Core Subject"
                appendLine("• Primary Block (50m): Deep dive on $topSubject with Pomodoro breaks.")
                appendLine("• Remaining Daily Target: ${remainingMinutes}m left to reach ${snapshot.dailyStudyTargetMinutes}m today.")
                appendLine("• Retention Tip: Close each session with a 3-bullet active recall summary in your session notes.")
            }

            "habit" in lower || "streak" in lower -> buildString {
                appendLine("Habit Streak Strategy:")
                appendLine("• Your longest active streak is ${snapshot.bestHabitStreak} day(s) — pair your hardest habit right after an established morning anchor.")
                appendLine("• Keep a 2-minute minimum version on busy days so you never break the chain twice in a row.")
            }

            else -> buildString {
                appendLine("Here is a tailored takeaway for \"${userPrompt.trim()}\":")
                appendLine("• You have ${snapshot.pendingRoutines.size} pending routine item(s) today ($routinePct% complete) and ${snapshot.todayStudyMinutes}m of logged focus.")
                appendLine("• Start with \"$nextTask\", then run one focused timer sprint toward \"$topGoal\".")
            }
        }

        return "[Sample Placeholder Response]\n\n$body".trim()
    }
}
