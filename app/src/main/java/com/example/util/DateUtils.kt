package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US)
    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    private val dayOfWeekShortFormat = SimpleDateFormat("EEE", Locale.US)
    private val liveClockTimeFormat = SimpleDateFormat("hh:mm:ss", Locale.US)
    private val liveClockAmPmFormat = SimpleDateFormat("a", Locale.US)

    fun todayKey(): String = keyFormat.format(Date())

    fun offsetDateKey(daysOffset: Int, fromDateKey: String = todayKey()): String {
        val cal = parseDateKey(fromDateKey)
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        return keyFormat.format(cal.time)
    }

    fun parseDateKey(dateKey: String): Calendar {
        val cal = Calendar.getInstance()
        val parsed = try {
            keyFormat.parse(dateKey)
        } catch (_: Exception) {
            null
        }
        cal.time = parsed ?: Date()
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    fun formatDisplayDate(dateKey: String): String {
        val cal = parseDateKey(dateKey)
        return displayDateFormat.format(cal.time)
    }

    fun formatShortDate(dateKey: String): String {
        val cal = parseDateKey(dateKey)
        return shortDateFormat.format(cal.time)
    }

    fun formatDayShort(dateKey: String): String {
        val cal = parseDateKey(dateKey)
        return dayOfWeekShortFormat.format(cal.time)
    }

    fun formatDayNumber(dateKey: String): Int {
        return parseDateKey(dateKey).get(Calendar.DAY_OF_MONTH)
    }

    fun formatMonthYear(year: Int, monthZeroBased: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthZeroBased)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return monthYearFormat.format(cal.time)
    }

    fun currentYearMonth(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
    }

    fun weekKeysAround(anchorDateKey: String): List<String> {
        val cal = parseDateKey(anchorDateKey)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val diffToMonday = when (dayOfWeek) {
            Calendar.SUNDAY -> -6
            else -> Calendar.MONDAY - dayOfWeek
        }
        cal.add(Calendar.DAY_OF_YEAR, diffToMonday)
        return List(7) { idx ->
            val key = keyFormat.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            key
        }
    }

    fun lastNDaysKeys(days: Int, endDateKey: String = todayKey()): List<String> {
        return (-(days - 1)..0).map { offset -> offsetDateKey(offset, endDateKey) }
    }

    /**
     * Returns 42 cells (6 rows x 7 cols, Mon..Sun) as Pair(dateKey, isCurrentMonth).
     */
    fun monthCalendarCells(year: Int, monthZeroBased: Int): List<Pair<String, Boolean>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthZeroBased)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 12)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val leadingEmpty = when (firstDayOfWeek) {
            Calendar.SUNDAY -> 6
            else -> firstDayOfWeek - Calendar.MONDAY
        }
        cal.add(Calendar.DAY_OF_YEAR, -leadingEmpty)
        return List(42) {
            val cellMonth = cal.get(Calendar.MONTH)
            val key = keyFormat.format(cal.time)
            val inMonth = cellMonth == monthZeroBased
            cal.add(Calendar.DAY_OF_YEAR, 1)
            key to inMonth
        }
    }

    fun daysUntil(targetDateKey: String, fromDateKey: String = todayKey()): Int {
        val target = parseDateKey(targetDateKey).timeInMillis
        val from = parseDateKey(fromDateKey).timeInMillis
        val diff = target - from
        return (diff / (1000L * 60L * 60L * 24L)).toInt()
    }

    fun formatMinutesOfDay(minutes: Int): String {
        val clamped = minutes.coerceIn(0, 23 * 60 + 59)
        val h24 = clamped / 60
        val m = clamped % 60
        val amPm = if (h24 < 12) "AM" else "PM"
        val h12 = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        return String.format(Locale.US, "%d:%02d %s", h12, m, amPm)
    }

    fun formatTimeRange(startMinutes: Int, endMinutes: Int): String {
        return "${formatMinutesOfDay(startMinutes)} – ${formatMinutesOfDay(endMinutes)}"
    }

    fun formatLiveClock(nowMillis: Long): Pair<String, String> {
        val date = Date(nowMillis)
        return liveClockTimeFormat.format(date) to liveClockAmPmFormat.format(date)
    }

    fun greetingForHour(hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): String {
        return when (hourOfDay) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Late Focus"
        }
    }

    fun formatDurationSeconds(totalSeconds: Int): String {
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", mins, secs)
    }
}
