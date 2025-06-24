package com.module.notelycompose.platform

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

actual class BrowserLauncher(private val context: Context) {
    actual fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
