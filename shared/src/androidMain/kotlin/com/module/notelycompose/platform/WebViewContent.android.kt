package com.module.notelycompose.platform

import android.annotation.SuppressLint
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Validates if a URL is safe to load in the WebView.
 * Only allows trusted domains and HTTPS URLs for security.
 */
private fun isAllowedUrl(url: String): Boolean {
    return when {
        // Allow HTTPS URLs from trusted domains only
        url.startsWith("https://tosinonikute.com/") -> true
        url.startsWith("https://github.com/tosinonikute/") -> true
        // Allow localhost for development (consider removing in production)
        url.startsWith("https://localhost") || url.startsWith("https://127.0.0.1") -> true
        // Block all other URLs
        else -> false
    }
}

@Composable
actual fun WebViewContent(url: String) {
    Surface(
        modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            factory = { context ->
                WebView(context).apply {
                    // SECURITY: Configure secure WebView settings
                    settings.apply {
                        // CRITICAL: Disable JavaScript by default to prevent XSS attacks
                        javaScriptEnabled = false
                        
                        // CRITICAL: Disable file access to prevent local file disclosure
                        allowFileAccess = false
                        allowFileAccessFromFileURLs = false
                        allowUniversalAccessFromFileURLs = false
                        
                        // SECURITY: Block mixed content (HTTP content on HTTPS pages)
                        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        
                        // SECURITY: Disable plugins and additional attack vectors
                        javaScriptCanOpenWindowsAutomatically = false
                        setSupportMultipleWindows(false)
                        
                        // SECURITY: Disable geolocation access
                        setGeolocationEnabled(false)
                        
                        // Display settings (keep existing functionality)
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                    }
                    
                    // SECURITY: Custom WebViewClient with security controls
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val requestUrl = request?.url?.toString()
                            return if (requestUrl != null && isAllowedUrl(requestUrl)) {
                                false // Allow loading of safe URLs
                            } else {
                                // SECURITY: Block unsafe URLs - could open in external browser instead
                                true // Block loading
                            }
                        }
                        
                        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                            // SECURITY: Always reject SSL errors - never call handler?.proceed()
                            handler?.cancel()
                        }
                        
                        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                            // SECURITY: Log security-relevant errors for monitoring
                            // In production, consider adding proper logging/monitoring
                            super.onReceivedError(view, errorCode, description, failingUrl)
                        }
                    }
                    
                    // Set display properties
                    isVerticalScrollBarEnabled = true
                    overScrollMode = android.view.View.OVER_SCROLL_ALWAYS
                    
                    // SECURITY: Validate URL before loading
                    if (isAllowedUrl(url)) {
                        loadUrl(url)
                    } else {
                        // SECURITY: Load error page for blocked URLs
                        loadData(
                            "<html><body><h1>Blocked</h1><p>URL not allowed for security reasons.</p></body></html>",
                            "text/html",
                            "UTF-8"
                        )
                    }
                }
            },
            update = { webView ->
                // SECURITY: Validate URL before updating
                if (isAllowedUrl(url)) {
                    webView.loadUrl(url)
                } else {
                    webView.loadData(
                        "<html><body><h1>Blocked</h1><p>URL not allowed for security reasons.</p></body></html>",
                        "text/html",
                        "UTF-8"
                    )
                }
            }
        )
    }
}