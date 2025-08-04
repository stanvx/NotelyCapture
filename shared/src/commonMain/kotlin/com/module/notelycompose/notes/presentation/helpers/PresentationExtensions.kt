package com.module.notelycompose.notes.presentation.helpers

import kotlinx.datetime.LocalDateTime

const val DEFAULT_CONTENT = ""
const val NEW_LINE = "\n"
const val ELLIPSIS = "..."
const val DEFAULT_MAX_LENGTH = 20
const val DATE_STR = "at"
const val MINUTE_PADDING_LENGTH = 2
const val PADDING_CHAR = '0'

fun String.returnFirstLine(): String {
    return this.split(NEW_LINE).firstOrNull().orEmpty()
}

fun String.truncateWithEllipsis(maxLength: Int = DEFAULT_MAX_LENGTH): String {
    return if (this.length > maxLength) {
        this.take(maxLength) + ELLIPSIS
    } else {
        this
    }
}

fun String.getFirstNonEmptyLineAfterFirst(): String {
    val lines = this.split(NEW_LINE)
    if (lines.size > 1) {
        for (i in 1 until lines.size) {
            if (lines[i].isNotBlank()) {
                return lines[i]
            }
        }
    }
    return ""
}

fun LocalDateTime.formattedDate(): String {
    // Use unified date format: Sun 3 Aug 8:15pm
    val shortDayName = when (this.dayOfWeek) {
        kotlinx.datetime.DayOfWeek.MONDAY -> "Mon"
        kotlinx.datetime.DayOfWeek.TUESDAY -> "Tue" 
        kotlinx.datetime.DayOfWeek.WEDNESDAY -> "Wed"
        kotlinx.datetime.DayOfWeek.THURSDAY -> "Thu"
        kotlinx.datetime.DayOfWeek.FRIDAY -> "Fri"
        kotlinx.datetime.DayOfWeek.SATURDAY -> "Sat"
        kotlinx.datetime.DayOfWeek.SUNDAY -> "Sun"
        else -> "Sun"
    }
    
    val shortMonthName = when (this.monthNumber) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
        9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
        else -> "Jan"
    }
    
    // 12-hour time format
    val hour = if (this.hour == 0) 12 else if (this.hour > 12) this.hour - 12 else this.hour
    val amPm = if (this.hour < 12) "am" else "pm"
    val minute = this.minute.toString().padStart(MINUTE_PADDING_LENGTH, PADDING_CHAR)
    val time = "$hour:$minute$amPm"
    
    return "$shortDayName ${this.dayOfMonth} $shortMonthName $time"
}