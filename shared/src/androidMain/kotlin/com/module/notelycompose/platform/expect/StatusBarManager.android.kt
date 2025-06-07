package com.module.notelycompose.platform.expect

import android.app.Activity
import androidx.core.view.WindowCompat

actual class StatusBarManager(private val activity: Activity) {
    private var originalStatusBarColor: Int? = null

    actual fun setStatusBarColor(color: Long) {
        // Store original values
        if (originalStatusBarColor == null) {
            originalStatusBarColor = activity.window.statusBarColor
        }
        activity.window.statusBarColor = color.toInt()
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = isLightColor(color)
        }
    }

    actual fun restoreDefaultStatusBarColor() {
        originalStatusBarColor?.let { color ->
            activity.window.statusBarColor = color
            WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
                isAppearanceLightStatusBars = isLightColor(color.toLong())
            }
        }
    }

    private fun isLightColor(color: Long): Boolean {
        val red = (color shr 16) and 0xFF
        val green = (color shr 8) and 0xFF
        val blue = color and 0xFF
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
        return luminance > 0.5
    }
}