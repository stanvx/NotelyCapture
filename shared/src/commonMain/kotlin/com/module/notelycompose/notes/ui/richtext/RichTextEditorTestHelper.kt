package com.module.notelycompose.notes.ui.richtext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.module.notelycompose.notes.presentation.helpers.RichTextEditorHelper

/**
 * Test helper for verifying rich text editor functionality during development.
 * This helps ensure all formatting operations work correctly and are properly connected.
 */
class RichTextEditorTestHelper {
    
    companion object {
        /**
         * Performs a comprehensive test of all rich text formatting features.
         * Call this during development to verify everything is working.
         * 
         * @param richTextEditorHelper The helper instance to test
         * @return TestResult with success status and any issues found
         */
        fun performComprehensiveTest(richTextEditorHelper: RichTextEditorHelper): TestResult {
            val issues = mutableListOf<String>()
            
            try {
                // Test 1: Basic content setting and retrieval
                richTextEditorHelper.setContent("Test content")
                val content = richTextEditorHelper.getContent()
                if (content.isEmpty()) {
                    issues.add("Content setting/getting failed")
                }
                
                // Test 2: Plain text extraction
                val plainText = richTextEditorHelper.getPlainText()
                if (plainText != "Test content") {
                    issues.add("Plain text extraction failed. Expected 'Test content', got '$plainText'")
                }
                
                // Test 3: Basic formatting operations
                richTextEditorHelper.toggleBold()
                if (!richTextEditorHelper.isSelectionBold()) {
                    issues.add("Bold formatting not applied or not detected")
                }
                
                richTextEditorHelper.toggleItalic()
                if (!richTextEditorHelper.isSelectionItalic()) {
                    issues.add("Italic formatting not applied or not detected")
                }
                
                richTextEditorHelper.toggleUnderline()
                if (!richTextEditorHelper.isSelectionUnderlined()) {
                    issues.add("Underline formatting not applied or not detected")
                }
                
                // Test 4: List operations
                richTextEditorHelper.toggleUnorderedList()
                if (!richTextEditorHelper.isUnorderedList()) {
                    issues.add("Unordered list not applied or not detected")
                }
                
                richTextEditorHelper.toggleOrderedList()
                if (!richTextEditorHelper.isOrderedList()) {
                    issues.add("Ordered list not applied or not detected")
                }
                
                // Test 5: Heading operations
                richTextEditorHelper.addHeading(1)
                val headingLevel = richTextEditorHelper.getCurrentHeadingLevel()
                if (headingLevel != 1) {
                    issues.add("Heading level 1 not applied. Current level: $headingLevel")
                }
                
                richTextEditorHelper.addHeading(2)
                val headingLevel2 = richTextEditorHelper.getCurrentHeadingLevel()
                if (headingLevel2 != 2) {
                    issues.add("Heading level 2 not applied. Current level: $headingLevel2")
                }
                
                // Test 6: Body text operation
                richTextEditorHelper.setBodyText()
                val bodyHeadingLevel = richTextEditorHelper.getCurrentHeadingLevel()
                if (bodyHeadingLevel != null) {
                    issues.add("Body text not applied. Still has heading level: $bodyHeadingLevel")
                }
                
                // Test 7: Text alignment
                richTextEditorHelper.setAlignment(TextAlign.Center)
                val alignment = richTextEditorHelper.getCurrentAlignment()
                if (alignment != TextAlign.Center) {
                    issues.add("Center alignment not applied. Current: $alignment")
                }
                
                richTextEditorHelper.setAlignment(TextAlign.End)
                val rightAlignment = richTextEditorHelper.getCurrentAlignment()
                if (rightAlignment != TextAlign.End) {
                    issues.add("Right alignment not applied. Current: $rightAlignment")
                }
                
                // Reset to start alignment
                richTextEditorHelper.setAlignment(TextAlign.Start)
                
                // Test 8: Advanced formatting
                richTextEditorHelper.toggleStrikethrough()
                if (!richTextEditorHelper.hasStrikethrough()) {
                    issues.add("Strikethrough formatting not applied or not detected")
                }
                
                richTextEditorHelper.toggleCodeBlock()
                if (!richTextEditorHelper.isCodeBlock()) {
                    issues.add("Code block formatting not applied or not detected")
                }
                
                richTextEditorHelper.toggleQuoteBlock()
                if (!richTextEditorHelper.isQuoteBlock()) {
                    issues.add("Quote block formatting not applied or not detected")
                }
                
                // Test 9: Color operations
                val testColor = Color.Red
                richTextEditorHelper.setTextColor(testColor)
                if (!richTextEditorHelper.hasTextColor()) {
                    issues.add("Text color not applied or not detected")
                }
                
                val highlightColor = Color.Yellow
                richTextEditorHelper.setHighlightColor(highlightColor)
                if (!richTextEditorHelper.hasHighlight()) {
                    issues.add("Highlight color not applied or not detected")
                }
                
                // Test 10: Indentation
                val initialIndent = richTextEditorHelper.getIndentLevel()
                richTextEditorHelper.increaseIndent()
                val increasedIndent = richTextEditorHelper.getIndentLevel()
                if (increasedIndent <= initialIndent) {
                    issues.add("Indent increase not working. Initial: $initialIndent, After: $increasedIndent")
                }
                
                richTextEditorHelper.decreaseIndent()
                val decreasedIndent = richTextEditorHelper.getIndentLevel()
                if (decreasedIndent != initialIndent) {
                    issues.add("Indent decrease not working. Expected: $initialIndent, Got: $decreasedIndent")
                }
                
                // Test 11: Clear formatting
                richTextEditorHelper.clearFormatting()
                if (richTextEditorHelper.isSelectionBold() || 
                    richTextEditorHelper.isSelectionItalic() || 
                    richTextEditorHelper.isSelectionUnderlined() ||
                    richTextEditorHelper.hasTextColor() ||
                    richTextEditorHelper.hasHighlight()) {
                    issues.add("Clear formatting did not remove all formatting")
                }
                
                // Test 12: HTML sanitization (security test)
                val maliciousContent = "<script>alert('xss')</script><p>Safe content</p>"
                richTextEditorHelper.setContent(maliciousContent)
                val sanitizedContent = richTextEditorHelper.getContent()
                if (sanitizedContent.contains("<script>")) {
                    issues.add("SECURITY ISSUE: HTML sanitization failed - script tags not removed")
                }
                
            } catch (e: Exception) {
                issues.add("Exception during testing: ${e.message}")
            }
            
            return TestResult(
                success = issues.isEmpty(),
                issues = issues,
                totalTests = 12
            )
        }
        
        /**
         * Quick test to verify basic functionality is working.
         */
        fun performQuickTest(richTextEditorHelper: RichTextEditorHelper): Boolean {
            return try {
                richTextEditorHelper.setContent("Quick test")
                val content = richTextEditorHelper.getPlainText()
                richTextEditorHelper.toggleBold()
                val isBold = richTextEditorHelper.isSelectionBold()
                content.isNotEmpty() && isBold
            } catch (e: Exception) {
                false
            }
        }
        
        /**
         * Test toolbar integration by verifying all toolbar functions work.
         */
        fun testToolbarIntegration(toolbarViewModel: RichTextToolbarViewModel): TestResult {
            val issues = mutableListOf<String>()
            
            try {
                // Test toolbar visibility
                toolbarViewModel.showToolbar()
                // Note: We can't easily test StateFlow values in this context
                // This would need to be done in a Compose test environment
                
                // Test formatting operations through toolbar
                toolbarViewModel.toggleBold()
                toolbarViewModel.toggleItalic()
                toolbarViewModel.toggleUnderline()
                toolbarViewModel.setAlignment(TextAlign.Center)
                toolbarViewModel.addHeading(1)
                toolbarViewModel.setBodyText()
                toolbarViewModel.clearFormatting()
                
                // If we get here without exceptions, basic integration is working
                
            } catch (e: Exception) {
                issues.add("Toolbar integration exception: ${e.message}")
            }
            
            return TestResult(
                success = issues.isEmpty(),
                issues = issues,
                totalTests = 1
            )
        }
    }
}

/**
 * Result of rich text editor testing.
 */
data class TestResult(
    val success: Boolean,
    val issues: List<String>,
    val totalTests: Int
) {
    val passedTests: Int get() = totalTests - issues.size
    
    fun getReport(): String {
        return buildString {
            appendLine("Rich Text Editor Test Report")
            appendLine("=============================")
            appendLine("Total Tests: $totalTests")
            appendLine("Passed: $passedTests")
            appendLine("Failed: ${issues.size}")
            appendLine("Success: $success")
            appendLine()
            
            if (issues.isNotEmpty()) {
                appendLine("Issues Found:")
                issues.forEachIndexed { index, issue ->
                    appendLine("${index + 1}. $issue")
                }
            } else {
                appendLine("All tests passed! ✓")
            }
        }
    }
}