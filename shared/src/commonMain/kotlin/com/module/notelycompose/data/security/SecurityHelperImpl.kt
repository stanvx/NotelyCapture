package com.module.notelycompose.data.security

import com.module.notelycompose.domain.security.SecurityHelper
import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory

/**
 * Production implementation of SecurityHelper using OWASP HTML Sanitizer.
 * This implementation provides robust security for HTML content sanitization.
 */
class SecurityHelperImpl : SecurityHelper {
    
    private val htmlPolicy: PolicyFactory = HtmlPolicyBuilder()
        .allowElements(
            "p", "br", "strong", "b", "em", "i", "u", "h1", "h2", "h3", "h4", "h5", "h6",
            "ul", "ol", "li", "blockquote", "pre", "code", "span", "div"
        )
        .allowAttributes("style", "class")
        .onElements("span", "div", "p")
        .allowStyling()
        .toFactory()
    
    override fun sanitizeHtml(input: String): String {
        if (input.isBlank()) return input
        
        return try {
            htmlPolicy.sanitize(input)
        } catch (e: Exception) {
            // If sanitization fails, return plain text
            input.replace(Regex("<[^>]*>"), "")
        }
    }
    
    override fun validateInput(input: String): Boolean {
        if (input.isBlank()) return true
        
        // Check for common XSS patterns
        val dangerousPatterns = listOf(
            "javascript:",
            "vbscript:",
            "onload=",
            "onerror=",
            "onclick=",
            "onmouseover=",
            "<script",
            "</script>",
            "eval(",
            "expression("
        )
        
        val lowerInput = input.lowercase()
        return dangerousPatterns.none { pattern ->
            lowerInput.contains(pattern.lowercase())
        }
    }
}