package com.module.notelycompose.notes.presentation.mapper

import com.module.notelycompose.notes.domain.model.NoteDomainModel
import com.module.notelycompose.notes.presentation.list.model.NotePresentationModel
import com.module.notelycompose.notes.ui.list.model.NoteUiModel
import com.module.notelycompose.platform.PlatformAudioPlayer
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant

private const val TIME_STRING = "at"
private const val PAD_START_LENGTH = 2
private const val PAD_CHARACTER = '0'

class NotePresentationMapper(
    private val audioPlayer: PlatformAudioPlayer
) {
    suspend fun mapToPresentationModel(domainModel: NoteDomainModel): NotePresentationModel {
        val audioDuration = if (domainModel.recordingPath.isNotEmpty()) {
            getAudioDuration(domainModel.recordingPath)
        } else {
            0
        }
        
        return NotePresentationModel(
            id = domainModel.id,
            title = domainModel.title,
            content = domainModel.content,
            isStarred = domainModel.starred,
            isVoice = domainModel.recordingPath.isNotEmpty(),
            createdAt = domainModel.createdAt.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toString(),
            recordingPath = domainModel.recordingPath,
            words = countWords(domainModel.content),
            audioDurationMs = audioDuration
        )
    }

    private fun completeTime(createdAt: LocalDateTime): String {
        // Use unified date format: Sun 3 Aug 8:15pm
        val shortDayName = when (createdAt.dayOfWeek) {
            kotlinx.datetime.DayOfWeek.MONDAY -> "Mon"
            kotlinx.datetime.DayOfWeek.TUESDAY -> "Tue" 
            kotlinx.datetime.DayOfWeek.WEDNESDAY -> "Wed"
            kotlinx.datetime.DayOfWeek.THURSDAY -> "Thu"
            kotlinx.datetime.DayOfWeek.FRIDAY -> "Fri"
            kotlinx.datetime.DayOfWeek.SATURDAY -> "Sat"
            kotlinx.datetime.DayOfWeek.SUNDAY -> "Sun"
            else -> "Sun"
        }
        
        val shortMonthName = when (createdAt.monthNumber) {
            1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
            5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
            9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
            else -> "Jan"
        }
        
        // 12-hour time format
        val hour = if (createdAt.hour == 0) 12 else if (createdAt.hour > 12) createdAt.hour - 12 else createdAt.hour
        val amPm = if (createdAt.hour < 12) "am" else "pm"
        val minute = createdAt.minute.toString().padStart(PAD_START_LENGTH, PAD_CHARACTER)
        val time = "$hour:$minute$amPm"
        
        return "$shortDayName ${createdAt.dayOfMonth} $shortMonthName $time"
    }

    private fun formatTimeWithLeadingZeros(localDateTime: LocalDateTime): String {
        val formattedHour = localDateTime.hour.toString().padStart(PAD_START_LENGTH, PAD_CHARACTER)
        val formattedMinute = localDateTime.minute.toString().padStart(PAD_START_LENGTH, PAD_CHARACTER)
        return "$formattedHour:$formattedMinute"
    }

    private fun countWords(str: String): Int {
        if (str.isBlank()) {
            return 0
        }
        return str.trim().split("\\s+".toRegex()).size
    }

    private suspend fun getAudioDuration(recordingPath: String): Int {
        return if (recordingPath.isNotEmpty()) {
            try {
                audioPlayer.prepare(recordingPath)
            } catch (e: Exception) {
                println("Failed to get audio duration for $recordingPath: ${e.message}")
                0
            }
        } else {
            0
        }
    }

    fun mapToUiModel(presentationModel: NotePresentationModel): NoteUiModel {
        return NoteUiModel(
            id = presentationModel.id,
            title = presentationModel.title,
            content = presentationModel.content,
            isStarred = presentationModel.isStarred,
            isVoice = presentationModel.isVoice,
            createdAt = presentationModel.createdAt,
            recordingPath = presentationModel.recordingPath,
            words = presentationModel.words,
            audioDurationMs = presentationModel.audioDurationMs
        )
    }
}
