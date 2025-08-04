package com.module.notelycompose.core

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

object DateTimeUtil {
    fun now(): LocalDateTime {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    fun toEpochMilli(localDateTime: LocalDateTime): Long {
        return localDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

}