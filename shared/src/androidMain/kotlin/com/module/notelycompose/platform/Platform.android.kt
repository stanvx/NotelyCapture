package com.module.notelycompose.platform

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.module.notelycompose.BuildConfig

class AndroidPlatform(
    private val version: String,
    private val context: Context?
) : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val isAndroid: Boolean get() = true
    override val appVersion: String get() = version
    override val isTablet: Boolean get() = context?.let { isTablet(it) } ?: false
    override val isLandscape: Boolean get() = isDeviceLandscape()

    private fun isTablet(context: Context): Boolean {
        val configuration = context.resources.configuration
        val screenLayout = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val isLargeScreen = screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE
        return isLargeScreen
    }

    private fun isDeviceLandscape(): Boolean {
        val configuration = context?.resources?.configuration
        return configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}

actual fun getPlatform(): Platform = AndroidPlatform(
    version = "",
    context = null
)

actual fun isDebugMode(): Boolean = BuildConfig.DEBUG
