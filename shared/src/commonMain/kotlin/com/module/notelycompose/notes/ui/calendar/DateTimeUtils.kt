package com.module.notelycompose.notes.ui.calendar

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Date and time utility functions for the Calendar module.
 * Provides parsing, formatting, and manipulation utilities.
 */

// Extension function to safely convert date string to LocalDate
fun String.parseToLocalDate(): LocalDate? {
    return try {
        // Handle various date formats that might be in the createdAt field
        when {
            // ISO format: 2025-07-21T19:46:00.000Z or 2025-07-21T19:46:00
            this.contains("T") -> {
                val datePart = this.substringBefore("T")
                LocalDate.parse(datePart)
            }
            // Simple date format: 2025-07-21
            this.length >= 10 && this.contains("-") -> {
                LocalDate.parse(this.substring(0, 10))
            }
            // Other formats - try to extract date
            else -> {
                // Try to extract YYYY-MM-DD pattern
                val datePattern = Regex("""(\d{4}-\d{2}-\d{2})""")
                val matchResult = datePattern.find(this)
                matchResult?.value?.let { LocalDate.parse(it) }
            }
        }
    } catch (e: Exception) {
        println("Failed to parse date: $this, error: ${e.message}")
        null
    }
}

// Extension function to safely convert date string to time string
fun String.parseToTimeString(): String {
    return try {
        // Extract time portion from the date string
        // This is a simple implementation - adjust based on your actual format
        val time = this.substring(11, 16) // Assuming format includes time like "2025-01-01T14:30:00"
        val hour = time.substring(0, 2).toInt()
        val minute = time.substring(3, 5)
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        "$displayHour:$minute $amPm"
    } catch (e: Exception) {
        this // Return original string if parsing fails
    }
}

// Extension function to format LocalDate to display string
fun LocalDate.formatToDisplayString(): String {
    val dayOfWeek = when (dayOfWeek) {
        kotlinx.datetime.DayOfWeek.MONDAY -> "Monday"
        kotlinx.datetime.DayOfWeek.TUESDAY -> "Tuesday"
        kotlinx.datetime.DayOfWeek.WEDNESDAY -> "Wednesday"
        kotlinx.datetime.DayOfWeek.THURSDAY -> "Thursday"
        kotlinx.datetime.DayOfWeek.FRIDAY -> "Friday"
        kotlinx.datetime.DayOfWeek.SATURDAY -> "Saturday"
        kotlinx.datetime.DayOfWeek.SUNDAY -> "Sunday"
        else -> ""
    }
    
    val monthName = when (month) {
        Month.JANUARY -> "January"
        Month.FEBRUARY -> "February"
        Month.MARCH -> "March"
        Month.APRIL -> "April"
        Month.MAY -> "May"
        Month.JUNE -> "June"
        Month.JULY -> "July"
        Month.AUGUST -> "August"
        Month.SEPTEMBER -> "September"
        Month.OCTOBER -> "October"
        Month.NOVEMBER -> "November"
        Month.DECEMBER -> "December"
        else -> ""
    }
    
    return "$dayOfWeek, $monthName $dayOfMonth, $year"
}

// Custom YearMonth implementation for kotlinx-datetime compatibility
data class YearMonthKt(val year: Int, val month: Month) {
    fun atDay(day: Int): LocalDate = LocalDate(year, month, day)
    
    fun lengthOfMonth(): Int {
        return when (month) {
            Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY, 
            Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
            Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
            Month.FEBRUARY -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }
    }
    
    fun minusMonths(months: Int): YearMonthKt {
        var newYear = year
        var newMonth = month.ordinal // 0-based
        
        newMonth -= months
        while (newMonth < 0) {
            newMonth += 12
            newYear -= 1
        }
        
        return YearMonthKt(newYear, Month.entries[newMonth])
    }
    
    fun plusMonths(months: Int): YearMonthKt {
        var newYear = year
        var newMonth = month.ordinal // 0-based
        
        newMonth += months
        while (newMonth >= 12) {
            newMonth -= 12
            newYear += 1
        }
        
        return YearMonthKt(newYear, Month.entries[newMonth])
    }
    
    fun formatToMonthYearString(): String {
        val monthName = when (month) {
            Month.JANUARY -> "January"
            Month.FEBRUARY -> "February"
            Month.MARCH -> "March"
            Month.APRIL -> "April"
            Month.MAY -> "May"
            Month.JUNE -> "June"
            Month.JULY -> "July"
            Month.AUGUST -> "August"
            Month.SEPTEMBER -> "September"
            Month.OCTOBER -> "October"
            Month.NOVEMBER -> "November"
            Month.DECEMBER -> "December"
            else -> ""
        }
        return "$monthName $year"
    }
    
    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }
}