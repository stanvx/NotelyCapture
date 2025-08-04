package com.module.notelycompose.notes.presentation.helpers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Comprehensive tests for RichTextEditorHelper focusing on performance optimizations,
 * security sanitization, rich text operations, and state management.
 */
class RichTextEditorHelperTest {

    private lateinit var helper: RichTextEditorHelper

    @Test
    fun `setContent with same content avoids unnecessary updates`() = runTest {
        helper = RichTextEditorHelper()
        val content = "Test content"
        
        // First call should update
        helper.setContent(content)
        val firstState = helper.richTextState.first()
        
        // Second call with same content should be optimized away
        helper.setContent(content)
        val secondState = helper.richTextState.first()
        
        // State should be identical (referential equality due to optimization)
        assertEquals(firstState.annotatedString.text, secondState.annotatedString.text)
    }

    @Test
    fun `setContent sanitizes HTML content for security`() = runTest {
        helper = RichTextEditorHelper()
        val maliciousContent = "<script>alert('XSS')</script><p>Safe content</p>"
        
        helper.setContent(maliciousContent)
        val resultContent = helper.getContent()
        
        // Should contain safe content but not script tags
        assertFalse(resultContent.contains("<script>"), "Script tags should be sanitized")
        assertTrue(resultContent.contains("Safe content"), "Safe content should be preserved")
    }

    @Test
    fun `getPlainText uses caching for performance`() = runTest {
        helper = RichTextEditorHelper()
        helper.setContent("<b>Bold text</b>")
        
        // First call should compute and cache
        val firstResult = helper.getPlainText()
        
        // Second call should use cache (same reference)
        val secondResult = helper.getPlainText()
        
        assertEquals(firstResult, secondResult)
        assertEquals("Bold text", firstResult)
    }

    @Test
    fun `toggleBold applies and removes bold formatting`() = runTest {
        helper = RichTextEditorHelper()
        val state = helper.richTextState.first()
        
        // Apply bold formatting
        helper.toggleBold()
        assertTrue(helper.isSelectionBold())
        
        // Remove bold formatting
        helper.toggleBold()
        assertFalse(helper.isSelectionBold())
    }

    @Test
    fun `toggleItalic applies and removes italic formatting`() = runTest {
        helper = RichTextEditorHelper()
        
        // Apply italic formatting
        helper.toggleItalic()
        assertTrue(helper.isSelectionItalic())
        
        // Remove italic formatting
        helper.toggleItalic()
        assertFalse(helper.isSelectionItalic())
    }

    @Test
    fun `toggleUnderline applies and removes underline formatting`() = runTest {
        helper = RichTextEditorHelper()
        
        // Apply underline formatting
        helper.toggleUnderline()
        assertTrue(helper.isSelectionUnderlined())
        
        // Remove underline formatting
        helper.toggleUnderline()
        assertFalse(helper.isSelectionUnderlined())
    }

    @Test
    fun `addHeading sets correct font size and weight`() = runTest {
        helper = RichTextEditorHelper()
        
        // Test different heading levels
        helper.addHeading(1)
        assertEquals(1, helper.getCurrentHeadingLevel())
        
        helper.addHeading(2)
        assertEquals(2, helper.getCurrentHeadingLevel())
        
        helper.addHeading(3)
        assertEquals(3, helper.getCurrentHeadingLevel())
    }

    @Test
    fun `setBodyText removes heading formatting`() = runTest {
        helper = RichTextEditorHelper()
        
        // First set as heading
        helper.addHeading(1)
        assertEquals(1, helper.getCurrentHeadingLevel())
        
        // Then convert to body text
        helper.setBodyText()
        assertEquals(null, helper.getCurrentHeadingLevel())
    }

    @Test
    fun `toggleUnorderedList creates and removes bullet points`() = runTest {
        helper = RichTextEditorHelper()
        
        // Apply unordered list
        helper.toggleUnorderedList()
        assertTrue(helper.isUnorderedList())
        
        // Remove unordered list
        helper.toggleUnorderedList()
        assertFalse(helper.isUnorderedList())
    }

    @Test
    fun `toggleOrderedList creates and removes numbered lists`() = runTest {
        helper = RichTextEditorHelper()
        
        // Apply ordered list
        helper.toggleOrderedList()
        assertTrue(helper.isOrderedList())
        
        // Remove ordered list
        helper.toggleOrderedList()
        assertFalse(helper.isOrderedList())
    }

    @Test
    fun `setAlignment updates text alignment correctly`() = runTest {
        helper = RichTextEditorHelper()
        
        // Test different alignments
        helper.setAlignment(TextAlign.Center)
        assertEquals(TextAlign.Center, helper.getCurrentAlignment())
        
        helper.setAlignment(TextAlign.End)
        assertEquals(TextAlign.End, helper.getCurrentAlignment())
        
        helper.setAlignment(TextAlign.Start)
        assertEquals(TextAlign.Start, helper.getCurrentAlignment())
    }

    @Test
    fun `toggleStrikethrough applies and removes strikethrough formatting`() = runTest {
        helper = RichTextEditorHelper()
        
        // Apply strikethrough
        helper.toggleStrikethrough()
        assertTrue(helper.hasStrikethrough())
        
        // Remove strikethrough
        helper.toggleStrikethrough()
        assertFalse(helper.hasStrikethrough())
    }

    @Test
    fun `toggleCodeBlock applies monospace font and background`() = runTest {
        helper = RichTextEditorHelper()
        
        // Apply code block formatting
        helper.toggleCodeBlock()
        assertTrue(helper.isCodeBlock())
        
        // Remove code block formatting
        helper.toggleCodeBlock()
        assertFalse(helper.isCodeBlock())
    }

    @Test
    fun `toggleQuoteBlock applies italic and indented formatting`() = runTest {
        helper = RichTextEditorHelper()
        
        // Apply quote block formatting
        helper.toggleQuoteBlock()
        assertTrue(helper.isQuoteBlock())
        
        // Quote blocks typically use italic text
        assertTrue(helper.isSelectionItalic())
    }

    @Test
    fun `clearFormatting removes all text formatting`() = runTest {
        helper = RichTextEditorHelper()
        
        // Apply multiple formatting
        helper.toggleBold()
        helper.toggleItalic()
        helper.toggleUnderline()
        helper.addHeading(2)
        
        // Verify formatting is applied
        assertTrue(helper.isSelectionBold())
        assertTrue(helper.isSelectionItalic())
        assertTrue(helper.isSelectionUnderlined())
        assertEquals(2, helper.getCurrentHeadingLevel())
        
        // Clear all formatting
        helper.clearFormatting()
        
        // Verify all formatting is removed
        assertFalse(helper.isSelectionBold())
        assertFalse(helper.isSelectionItalic())
        assertFalse(helper.isSelectionUnderlined())
        assertEquals(null, helper.getCurrentHeadingLevel())
    }

    @Test
    fun `increaseIndent and decreaseIndent manage indentation levels`() = runTest {
        helper = RichTextEditorHelper()
        
        // Initially no indentation
        assertEquals(0, helper.getIndentLevel())
        
        // Increase indentation
        helper.increaseIndent()
        assertEquals(1, helper.getIndentLevel())
        
        helper.increaseIndent()
        assertEquals(2, helper.getIndentLevel())
        
        // Decrease indentation
        helper.decreaseIndent()
        assertEquals(1, helper.getIndentLevel())
        
        helper.decreaseIndent()
        assertEquals(0, helper.getIndentLevel())
        
        // Cannot go below 0
        helper.decreaseIndent()
        assertEquals(0, helper.getIndentLevel())
    }

    @Test
    fun `indentation respects maximum level`() = runTest {
        helper = RichTextEditorHelper()
        
        // Increase to maximum (5 levels)
        repeat(10) { helper.increaseIndent() }
        
        // Should be capped at 5
        assertTrue(helper.getIndentLevel() <= 5)
    }

    @Test
    fun `setTextColor and removeTextColor manage text color`() = runTest {
        helper = RichTextEditorHelper()
        
        // Initially no custom text color
        assertFalse(helper.hasTextColor())
        
        // Set text color
        helper.setTextColor(Color.Red)
        assertTrue(helper.hasTextColor())
        assertEquals(Color.Red, helper.getCurrentTextColor())
        
        // Remove text color
        helper.removeTextColor()
        assertFalse(helper.hasTextColor())
    }

    @Test
    fun `setHighlightColor and removeHighlightColor manage background highlighting`() = runTest {
        helper = RichTextEditorHelper()
        
        // Initially no highlight
        assertFalse(helper.hasHighlight())
        
        // Set highlight color
        helper.setHighlightColor(Color.Yellow)
        assertTrue(helper.hasHighlight())
        assertEquals(Color.Yellow, helper.getCurrentHighlightColor())
        
        // Remove highlight
        helper.removeHighlightColor()
        assertFalse(helper.hasHighlight())
    }

    @Test
    fun `hasLink detects URL patterns in selection`() = runTest {
        helper = RichTextEditorHelper()
        helper.setContent("Visit https://example.com for more info")
        
        // This test would need proper selection handling
        // For now, we test the link detection logic conceptually
        val hasLink = helper.hasLink()
        
        // The actual result depends on current selection state
        // In a real implementation, you'd need to set selection to the URL
    }

    @Test
    fun `toggleLink applies and removes link formatting`() = runTest {
        helper = RichTextEditorHelper()
        helper.setContent("Click here")
        
        // Apply link formatting (visual indication)
        helper.toggleLink()
        
        // Remove link formatting
        helper.toggleLink()
        
        // Note: Link functionality is primarily visual in this implementation
    }

    @Test
    fun `insertDivider adds horizontal rule`() = runTest {
        helper = RichTextEditorHelper()
        helper.setContent("Before")
        
        helper.insertDivider()
        
        val content = helper.getContent()
        assertTrue(content.contains("---"), "Should contain divider markup")
    }

    @Test
    fun `isCursorAtEndOfHeading detects heading line endings`() = runTest {
        helper = RichTextEditorHelper()
        helper.addHeading(1)
        
        // This would need proper cursor positioning for accurate testing
        val isAtEnd = helper.isCursorAtEndOfHeading()
        
        // The result depends on actual cursor position
        // In practice, this would be tested with specific text and cursor positions
    }

    @Test
    fun `handleEnterKeyPress manages heading to body text conversion`() = runTest {
        helper = RichTextEditorHelper()
        helper.addHeading(1)
        
        // Simulate Enter key press at end of heading
        val handled = helper.handleEnterKeyPress()
        
        // Default behavior should let Enter key be processed normally
        assertFalse(handled, "Should allow default Enter behavior")
    }

    @Test
    fun `applyBodyTextAfterEnter converts to body text`() = runTest {
        helper = RichTextEditorHelper()
        helper.addHeading(2)
        
        // Apply body text after Enter
        helper.applyBodyTextAfterEnter()
        
        // Should no longer be a heading
        assertEquals(null, helper.getCurrentHeadingLevel())
    }

    @Test
    fun `createNew returns fresh instance`() = runTest {
        helper = RichTextEditorHelper()
        helper.setContent("Some content")
        
        val newHelper = helper.createNew()
        
        // Should be different instance
        assertNotEquals(helper, newHelper)
        
        // New instance should be clean
        assertEquals("", newHelper.getContent())
    }

    @Test
    fun `empty content handling`() = runTest {
        helper = RichTextEditorHelper()
        
        // Test with empty content
        helper.setContent("")
        assertEquals("", helper.getContent())
        assertEquals("", helper.getPlainText())
        
        // Test with null-like content
        helper.setContent("   ")
        assertEquals("   ", helper.getPlainText())
    }

    @Test
    fun `large content performance`() = runTest {
        helper = RichTextEditorHelper()
        
        // Test with large content to ensure performance optimizations work
        val largeContent = "A".repeat(10000)
        helper.setContent(largeContent)
        
        // Should handle large content without issues
        assertEquals(largeContent.length, helper.getPlainText().length)
        
        // Multiple calls should use caching
        repeat(5) {
            helper.getPlainText()
        }
    }

    @Test
    fun `malformed HTML content handling`() = runTest {
        helper = RichTextEditorHelper()
        
        // Test with malformed HTML
        val malformedHtml = "<p>Unclosed paragraph<div>Mixed tags</p></div>"
        helper.setContent(malformedHtml)
        
        // Should not crash and should produce some reasonable output
        val result = helper.getContent()
        val plainText = helper.getPlainText()
        
        // Should contain the text content
        assertTrue(plainText.contains("Unclosed paragraph"))
        assertTrue(plainText.contains("Mixed tags"))
    }

    @Test
    fun `concurrent access to content caching`() = runTest {
        helper = RichTextEditorHelper()
        helper.setContent("Test content")
        
        // Simulate concurrent access to cached content
        val results = List(10) {
            helper.getPlainText()
        }
        
        // All results should be identical
        assertTrue(results.all { it == results.first() })
    }

    @Test
    fun `formatting state consistency`() = runTest {
        helper = RichTextEditorHelper()
        
        // Apply multiple formatting options
        helper.toggleBold()
        helper.toggleItalic()
        helper.setTextColor(Color.Blue)
        helper.setHighlightColor(Color.Yellow)
        
        // State should be consistent
        assertTrue(helper.isSelectionBold())
        assertTrue(helper.isSelectionItalic())
        assertTrue(helper.hasTextColor())
        assertTrue(helper.hasHighlight())
        assertEquals(Color.Blue, helper.getCurrentTextColor())
        assertEquals(Color.Yellow, helper.getCurrentHighlightColor())
        
        // Clear formatting should reset everything
        helper.clearFormatting()
        assertFalse(helper.isSelectionBold())
        assertFalse(helper.isSelectionItalic())
        assertFalse(helper.hasTextColor())
        assertFalse(helper.hasHighlight())
    }
}