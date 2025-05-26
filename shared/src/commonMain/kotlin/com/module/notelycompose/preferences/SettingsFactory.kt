package com.module.notelycompose.preferences

import com.russhwolf.settings.Settings

expect class SettingsFactory {
    fun createSettings(): Settings
}
