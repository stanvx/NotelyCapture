package com.module.notelycompose.security

import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory
import java.util.regex.Pattern

/**
 * Secure HTML sanitizer using OWASP Java HTML Sanitizer library.
 * 
 * Provides XSS protection for user-generated rich text content by allowing
 * only safe HTML elements and attributes while removing potentially malicious content.
 * 
 * This implementation follows OWASP security guidelines and is configured
 * specifically for rich text editing use cases in the note-taking application.
 */
object HtmlSanitizer {
    
    /**
     * Pattern for safe CSS style attributes.
     * Allows common text formatting styles while blocking dangerous properties.
     */
    private val SAFE_STYLE_PATTERN = Pattern.compile(
        "^\\s*(color|background-color|font-family|font-size|font-weight|font-style|text-align|text-decoration|line-height|margin|padding)\\s*:\\s*[^;{}]*\\s*(;\\s*(color|background-color|font-family|font-size|font-weight|font-style|text-align|text-decoration|line-height|margin|padding)\\s*:\\s*[^;{}]*\\s*)*$"
    )
    
    /**
     * Pattern for safe CSS class names.
     * Allows common alignment and formatting classes.
     */
    private val SAFE_CLASS_PATTERN = Pattern.compile(
        "^(text-left|text-center|text-right|text-justify|bold|italic|underline|heading-[1-6])$"
    )
    
    /**
     * Policy factory configured for rich text content.
     * Allows common formatting elements while blocking script execution and dangerous attributes.
     */
    private val policy: PolicyFactory = HtmlPolicyBuilder()
        .allowElements("b", "i", "u", "em", "strong", "mark", "small", "del", "ins", "sub", "sup")
        .allowElements("h1", "h2", "h3", "h4", "h5", "h6")
        .allowElements("p", "br", "div", "span")
        .allowElements("ul", "ol", "li")
        .allowElements("blockquote", "code", "pre")
        .allowAttributes("style")
            .matching(SAFE_STYLE_PATTERN)
            .onElements("span", "div", "p", "h1", "h2", "h3", "h4", "h5", "h6")
        .allowAttributes("class")
            .matching(SAFE_CLASS_PATTERN)
            .onElements("p", "div", "h1", "h2", "h3", "h4", "h5", "h6")
        
        .toFactory()
    
    /**
     * Sanitizes HTML content to prevent XSS attacks while preserving safe formatting.
     * 
     * @param content The HTML content to sanitize
     * @return Sanitized HTML content safe for display
     * 
     * @throws IllegalArgumentException if content is null
     */
    fun sanitize(content: String?): String {
        if (content == null) {
            throw IllegalArgumentException("Content cannot be null")
        }
        
        // Handle empty or whitespace-only content
        if (content.isBlank()) {
            return content
        }
        
        return try {
            policy.sanitize(content)
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Sanitizes HTML content and provides detailed information about what was removed.
     * Useful for debugging and user feedback.
     * 
     * @param content The HTML content to sanitize
     * @return SanitizationResult containing sanitized content and removal details
     */
    fun sanitizeWithDetails(content: String?): SanitizationResult {
        if (content == null) {
            return SanitizationResult("", emptyList(), "Content was null")
        }
        
        if (content.isBlank()) {
            return SanitizationResult(content, emptyList(), null)
        }
        
        return try {
            val sanitized = policy.sanitize(content)
            val removedElements = detectRemovedElements(content, sanitized)
            
            SanitizationResult(
                sanitizedContent = sanitized,
                removedElements = removedElements,
                error = null
            )
        } catch (e: Exception) {
            SanitizationResult(
                sanitizedContent = "",
                removedElements = emptyList(),
                error = "Sanitization failed: ${e.message}"
            )
        }
    }
    
    /**
     * Detects what elements were removed during sanitization.
     */
    private fun detectRemovedElements(original: String, sanitized: String): List<String> {
        val removed = mutableListOf<String>()
        
        val dangerousElements = listOf("script", "iframe", "object", "embed", "form", "input")
        
        for (element in dangerousElements) {
            if (original.contains("<$element", ignoreCase = true) && 
                !sanitized.contains("<$element", ignoreCase = true)) {
                removed.add(element)
            }
        }
        
        return removed
    }
    
    /**
     * Validates if the given HTML content is safe (would not be modified by sanitization).
     */
    fun isSafe(content: String?): Boolean {
        if (content == null) return false
        return content == sanitize(content)
    }
}

/**
 * Result of HTML sanitization with detailed information.
 */
data class SanitizationResult(
    val sanitizedContent: String,
    val removedElements: List<String>,
    val error: String?
) {
    val wasModified: Boolean
        get() = removedElements.isNotEmpty() || error != null
}