package com.module.notelycompose.platform.expect

import android.app.Activity
import android.view.WindowManager

actual class StatusBarManager(private val activity: Activity) {
    private var originalStatusBarColor: Int? = null

    actual fun setStatusBarColor(color: Int) {
        val window = activity.window
        originalStatusBarColor = window.statusBarColor
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = color
    }

    actual fun restoreDefaultStatusBarColor() {
        originalStatusBarColor?.let {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = it
        }
    }
}
