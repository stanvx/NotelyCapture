package com.module.notelycompose.notes.utils

import kotlinx.datetime.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * Shared utilities for date and time formatting across the application.
 * Provides consistent formatting for durations, relative times, and dates.
 */
object DateTimeFormatUtils {

    /**
     * Format duration from milliseconds to human-readable format.
     * Examples: "2:34", "15s", "1:05:30"
     */
    fun formatDuration(durationMs: Long): String {
        val duration = durationMs.milliseconds
        val hours = duration.inWholeHours
        val minutes = duration.inWholeMinutes % 60
        val seconds = duration.inWholeSeconds % 60
        
        return when {
            hours > 0 -> "${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
            minutes > 0 -> "${minutes}:${seconds.toString().padStart(2, '0')}"
            else -> "${seconds}s"
        }
    }

    /**
     * Format relative time for display with Apple-style formatting.
     * Examples: "2h ago", "Yesterday", "3d ago", "1w ago"
     */
    fun formatRelativeTime(dateTimeString: String): String {
        return try {
            val noteDateTime = Instant.parse(dateTimeString)
            val now = Clock.System.now()
            val diff = now - noteDateTime
            
            when {
                diff.inWholeDays >= 7 -> {
                    val weeks = diff.inWholeDays / 7
                    if (weeks == 1L) "1 week ago" else "${weeks}w ago"
                }
                diff.inWholeDays >= 2 -> "${diff.inWholeDays}d ago"
                diff.inWholeDays == 1L -> "Yesterday"
                diff.inWholeHours >= 1 -> "${diff.inWholeHours}h ago"
                diff.inWholeMinutes >= 1 -> "${diff.inWholeMinutes}m ago"
                else -> "Now"
            }
        } catch (e: Exception) {
            // Fallback for simple time extraction
            try {
                when {
                    dateTimeString.contains("T") -> {
                        val timePart = dateTimeString.substringAfter("T").substringBefore(".")
                        val hourMinute = timePart.substringBeforeLast(":")
                        "at $hourMinute"
                    }
                    dateTimeString.contains(":") -> {
                        val time = dateTimeString.substringBeforeLast(":")
                        "at $time"
                    }
                    else -> "Today"
                }
            } catch (e: Exception) {
                "Recently"
            }
        }
    }

    /**
     * Format date with custom unified format.
     * Format: <short-day> <day-number> <short-month> <12h-time>
     * Examples: "Sun 3 Aug 8:15pm", "Mon 15 Jan 2:30pm"
     */
    fun formatUnifiedDate(dateTimeString: String): String {
        return try {
            val noteDateTime = Instant.parse(dateTimeString)
            val timeZone = TimeZone.currentSystemDefault()
            val localDateTime = noteDateTime.toLocalDateTime(timeZone)
            
            // Short day names
            val shortDayName = when (localDateTime.dayOfWeek.isoDayNumber) {
                1 -> "Mon"
                2 -> "Tue" 
                3 -> "Wed"
                4 -> "Thu"
                5 -> "Fri"
                6 -> "Sat"
                7 -> "Sun"
                else -> "Sun"
            }
            
            // Short month names
            val shortMonthName = when (localDateTime.monthNumber) {
                1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
                5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
                9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
                else -> "Jan"
            }
            
            // 12-hour time format
            val hour = if (localDateTime.hour == 0) 12 else if (localDateTime.hour > 12) localDateTime.hour - 12 else localDateTime.hour
            val amPm = if (localDateTime.hour < 12) "am" else "pm"
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val time = "$hour:$minute$amPm"
            
            "$shortDayName ${localDateTime.dayOfMonth} $shortMonthName $time"
        } catch (e: Exception) {
            // Fallback format - this should rarely be hit now
            "Sun 1 Jan 12:00pm"
        }
    }

    /**
     * Format date with Apple-style calendar display.
     * Examples: "2:30 PM" (today), "Yesterday", "Monday", "Jan 15"
     */
    fun formatAppleStyleDate(dateTimeString: String): String {
        return try {
            val noteDateTime = Instant.parse(dateTimeString)
            val now = Clock.System.now()
            val diff = now - noteDateTime
            val timeZone = TimeZone.currentSystemDefault()
            val localDateTime = noteDateTime.toLocalDateTime(timeZone)
            
            when {
                diff.inWholeDays == 0L -> {
                    // Today - show time only
                    val hour = if (localDateTime.hour == 0) 12 else if (localDateTime.hour > 12) localDateTime.hour - 12 else localDateTime.hour
                    val amPm = if (localDateTime.hour < 12) "AM" else "PM"
                    val minute = localDateTime.minute.toString().padStart(2, '0')
                    "$hour:$minute $amPm"
                }
                diff.inWholeDays == 1L -> "Yesterday"
                diff.inWholeDays < 7 -> {
                    // This week - show day name
                    val dayName = when (localDateTime.dayOfWeek.isoDayNumber) {
                        1 -> "Monday"
                        2 -> "Tuesday" 
                        3 -> "Wednesday"
                        4 -> "Thursday"
                        5 -> "Friday"
                        6 -> "Saturday"
                        7 -> "Sunday"
                        else -> "Today"
                    }
                    dayName
                }
                else -> {
                    // Older - show date
                    val monthName = when (localDateTime.monthNumber) {
                        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
                        5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
                        9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
                        else -> "Jan"
                    }
                    "$monthName ${localDateTime.dayOfMonth}"
                }
            }
        } catch (e: Exception) {
            // Fallback
            try {
                when {
                    dateTimeString.contains("T") -> {
                        val timePart = dateTimeString.substringAfter("T").substringBefore(".")
                        val hourMinute = timePart.substringBeforeLast(":")
                        hourMinute
                    }
                    else -> "Today"
                }
            } catch (e: Exception) {
                "Today"
            }
        }
    }
}