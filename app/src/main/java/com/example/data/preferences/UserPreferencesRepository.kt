package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lifetrack_prefs")

data class UserProfileSettings(
    val userName: String = "Alex Mercer",
    val userRole: String = "Product Engineer & Learner",
    val userEmail: String = "alex.mercer@lifetrack.app",
    val isDarkMode: Boolean = true,
    val dailyStudyGoalMinutes: Int = 180,
    val pomodoroMinutes: Int = 25,
    val deepWorkMinutes: Int = 50,
    val notificationsEnabled: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_ROLE = stringPreferencesKey("user_role")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val DAILY_STUDY_GOAL_MINUTES = intPreferencesKey("daily_study_goal_minutes")
        val POMODORO_MINUTES = intPreferencesKey("pomodoro_minutes")
        val DEEP_WORK_MINUTES = intPreferencesKey("deep_work_minutes")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val HAS_SEEDED = booleanPreferencesKey("has_seeded_initial_data")
    }

    val settingsFlow: Flow<UserProfileSettings> = context.dataStore.data.map { prefs ->
        UserProfileSettings(
            userName = prefs[Keys.USER_NAME] ?: "Alex Mercer",
            userRole = prefs[Keys.USER_ROLE] ?: "Product Engineer & Learner",
            userEmail = prefs[Keys.USER_EMAIL] ?: "alex.mercer@lifetrack.app",
            isDarkMode = prefs[Keys.IS_DARK_MODE] ?: true,
            dailyStudyGoalMinutes = prefs[Keys.DAILY_STUDY_GOAL_MINUTES] ?: 180,
            pomodoroMinutes = prefs[Keys.POMODORO_MINUTES] ?: 25,
            deepWorkMinutes = prefs[Keys.DEEP_WORK_MINUTES] ?: 50,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
        )
    }

    suspend fun hasSeeded(): Boolean {
        return context.dataStore.data.first()[Keys.HAS_SEEDED] ?: false
    }

    suspend fun markSeeded(seeded: Boolean = true) {
        context.dataStore.edit { it[Keys.HAS_SEEDED] = seeded }
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_MODE] = isDark }
    }

    suspend fun updateProfile(name: String, role: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = name.trim().ifEmpty { "Alex Mercer" }
            prefs[Keys.USER_ROLE] = role.trim().ifEmpty { "Lifelong Learner" }
            prefs[Keys.USER_EMAIL] = email.trim().ifEmpty { "alex@lifetrack.app" }
        }
    }

    suspend fun updateStudyTargets(dailyGoalMinutes: Int, pomodoroMinutes: Int, deepWorkMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DAILY_STUDY_GOAL_MINUTES] = dailyGoalMinutes.coerceIn(15, 720)
            prefs[Keys.POMODORO_MINUTES] = pomodoroMinutes.coerceIn(5, 90)
            prefs[Keys.DEEP_WORK_MINUTES] = deepWorkMinutes.coerceIn(15, 180)
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
}
