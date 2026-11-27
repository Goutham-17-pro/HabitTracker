package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.HabitLogEntity
import com.example.data.repository.LifeTrackRepository
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LifeTrack", appName)
    }

    @Test
    fun `streak calculation handles consecutive days`() {
        val today = "2026-11-13"
        val logs = listOf(
            HabitLogEntity(habitId = 1L, dateKey = "2026-11-13"),
            HabitLogEntity(habitId = 1L, dateKey = "2026-11-12"),
            HabitLogEntity(habitId = 1L, dateKey = "2026-11-11"),
            HabitLogEntity(habitId = 1L, dateKey = "2026-11-09")
        )
        assertEquals(3, LifeTrackRepository.calculateCurrentStreak(1L, logs, today))
    }

    @Test
    fun `week keys returns 7 consecutive days and month grid returns 42 cells`() {
        val week = DateUtils.weekKeysAround("2026-11-13")
        assertEquals(7, week.size)
        assertEquals("2026-11-09", week.first())
        assertEquals("2026-11-15", week.last())

        val monthCells = DateUtils.monthCalendarCells(2026, 10) // Nov 2026
        assertEquals(42, monthCells.size)
    }
}
