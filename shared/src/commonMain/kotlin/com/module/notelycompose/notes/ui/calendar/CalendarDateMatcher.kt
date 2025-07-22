package com.module.notelycompose.notes.ui.calendar

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Utility class to handle calendar date matching with comprehensive debugging and validation.
 * This class helps identify and resolve date parsing issues between note creation and calendar display.
 */
object CalendarDateMatcher {
    
    /**
     * Matches a note's createdAt string against a target date with detailed logging.
     * Returns true if the note was created on the target date, false otherwise.
     */
    fun matchesDate(
        noteCreatedAt: String, 
        targetDate: LocalDate, 
        noteId: Long,
        enableDebugLogging: Boolean = true
    ): Boolean {
        val noteDate = noteCreatedAt.parseToLocalDate()
        
        if (enableDebugLogging) {
            when {
                noteDate == null -> {
                    println("[Calendar Debug] Note $noteId: Failed to parse date '$noteCreatedAt'")
                }
                noteDate == targetDate -> {
                    println("[Calendar Debug] Note $noteId: MATCH - note date '$noteDate' == target '$targetDate'")
                }
                else -> {
                    println("[Calendar Debug] Note $noteId: No match - note date '$noteDate' != target '$targetDate'")
                }
            }
        }
        
        return noteDate == targetDate
    }
    
    /**
     * Validates that the calendar date parsing logic works correctly.
     * Tests various date formats that might be encountered.
     */
    fun validateDateParsing(): Map<String, Boolean> {
        val testCases = listOf(
            "21 July at 14:30",
            "1 January at 09:15", 
            "31 December at 23:59",
            "2025-07-21T14:30:00",
            "2025-07-21",
            "Invalid Date Format"
        )
        
        val results = mutableMapOf<String, Boolean>()
        
        testCases.forEach { testDate ->
            val parsed = testDate.parseToLocalDate()
            results[testDate] = parsed != null
            println("[Date Parsing Test] '$testDate' -> ${if (parsed != null) "SUCCESS: $parsed" else "FAILED"}")
        }
        
        return results
    }
    
    /**
     * Checks for potential timezone-related issues between note storage and calendar display.
     */
    fun checkTimezoneConsistency(): String {
        val currentSystemTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val utcTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        
        return buildString {
            appendLine("[Timezone Check]")
            appendLine("System timezone: ${TimeZone.currentSystemDefault()}")
            appendLine("Current system time: $currentSystemTime")
            appendLine("Current UTC time: $utcTime")
            appendLine("Date difference: ${currentSystemTime.date != utcTime.date}")
        }
    }
    
    /**
     * Comprehensive debugging function that analyzes a collection of notes
     * and their date matching behavior against a specific date.
     */
    fun debugNotesForDate(
        notes: List<Pair<Long, String>>, // (noteId, createdAt)
        targetDate: LocalDate
    ): String {
        return buildString {
            appendLine("[Calendar Debug] Analyzing ${notes.size} notes for date $targetDate")
            appendLine(checkTimezoneConsistency())
            appendLine()
            
            var matchCount = 0
            var parseFailures = 0
            
            notes.forEach { (noteId, createdAt) ->
                val noteDate = createdAt.parseToLocalDate()
                when {
                    noteDate == null -> {
                        parseFailures++
                        appendLine("❌ Note $noteId: Parse failed for '$createdAt'")
                    }
                    noteDate == targetDate -> {
                        matchCount++
                        appendLine("✅ Note $noteId: MATCH '$createdAt' -> $noteDate")
                    }
                    else -> {
                        appendLine("ℹ️  Note $noteId: Different date '$createdAt' -> $noteDate")
                    }
                }
            }
            
            appendLine()
            appendLine("Summary:")
            appendLine("- Total notes: ${notes.size}")
            appendLine("- Matching notes: $matchCount")
            appendLine("- Parse failures: $parseFailures")
            appendLine("- Success rate: ${((notes.size - parseFailures).toFloat() / notes.size * 100).toInt()}%")
        }
    }
}
