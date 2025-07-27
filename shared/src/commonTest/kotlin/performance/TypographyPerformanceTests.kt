package performance

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import androidx.compose.ui.text.font.FontWeight
import com.module.notelycompose.notes.ui.theme.Material3ExpressiveTypography
import com.module.notelycompose.notes.ui.theme.noteTitle
import com.module.notelycompose.notes.ui.theme.noteContent
import com.module.notelycompose.notes.ui.theme.buttonText
import com.module.notelycompose.notes.ui.theme.appBarTitle

/**
 * Performance tests for Typography system optimization
 * 
 * Validates that the singleton pattern provides significant performance improvements
 * over the previous @Composable function approach.
 */
class TypographyPerformanceTests {
    
    @Test
    fun testTypographyAccessPerformance() {
        val startTime = System.currentTimeMillis()
        
        // Test that typography access is fast (should be instantaneous)
        repeat(1000) {
            val typography = Material3ExpressiveTypography
            val titleStyle = typography.noteTitle
            val contentStyle = typography.noteContent
            val buttonStyle = typography.buttonText
            val appBarStyle = typography.appBarTitle
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Should complete in under 50ms for 1000 iterations
        assertTrue(
            "Typography access too slow: ${duration}ms for 1000 iterations", 
            duration < 50
        )
    }
    
    @Test
    fun testTypographySingletonConsistency() {
        // Verify that repeated access returns the same object
        val typography1 = Material3ExpressiveTypography
        val typography2 = Material3ExpressiveTypography
        
        // Should be the same object reference (singleton)
        assertTrue(
            "Typography should be singleton",
            typography1 === typography2
        )
    }
    
    @Test
    fun testFontWeightMapping() {
        val typography = Material3ExpressiveTypography
        
        // Test core typography styles have correct font weights
        assertEquals(FontWeight.Medium, typography.titleMedium.fontWeight)
        assertEquals(FontWeight.SemiBold, typography.headlineMedium.fontWeight)
        assertEquals(FontWeight.Bold, typography.titleLarge.fontWeight)
        assertEquals(FontWeight.Normal, typography.bodyMedium.fontWeight)
        
        // Test semantic extensions have correct weights
        assertEquals(FontWeight.SemiBold, typography.noteTitle.fontWeight)
        assertEquals(FontWeight.Medium, typography.labelLarge.fontWeight)
    }
    
    @Test
    fun testSemanticTokenPerformance() {
        val startTime = System.currentTimeMillis()
        val typography = Material3ExpressiveTypography
        
        // Test that semantic token access is fast
        repeat(1000) {
            val noteTitle = typography.noteTitle
            val noteContent = typography.noteContent
            val buttonText = typography.buttonText
            val appBarTitle = typography.appBarTitle
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Should complete in under 50ms for 1000 iterations
        assertTrue(
            "Semantic token access too slow: ${duration}ms for 1000 iterations",
            duration < 50
        )
    }
    
    @Test
    fun testMemoryUsageOptimization() {
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        // Access typography multiple times to test for memory leaks
        repeat(100) {
            val typography = Material3ExpressiveTypography
            val styles = listOf(
                typography.noteTitle,
                typography.noteContent,
                typography.buttonText,
                typography.appBarTitle,
                typography.displayLarge,
                typography.headlineMedium,
                typography.titleLarge,
                typography.bodyMedium,
                typography.labelSmall
            )
            // Use the styles to prevent optimization
            styles.forEach { it.fontSize }
        }
        
        System.gc() // Force garbage collection
        Thread.sleep(100) // Give GC time to work
        
        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryIncrease = finalMemory - initialMemory
        
        // Memory usage should not significantly increase (less than 1MB)
        assertTrue(
            "Potential memory leak detected: ${memoryIncrease} bytes increase",
            memoryIncrease < 1024 * 1024
        )
    }
    
    @Test
    fun testSemanticTokenCorrectness() {
        val typography = Material3ExpressiveTypography
        
        // Verify semantic tokens map to correct base styles
        assertEquals(typography.headlineMedium.fontSize, typography.noteTitle.fontSize)
        assertEquals(typography.bodyMedium.fontSize, typography.noteContent.fontSize)
        assertEquals(typography.labelLarge.fontSize, typography.buttonText.fontSize)
        assertEquals(typography.titleLarge.fontSize, typography.appBarTitle.fontSize)
        
        // Verify semantic customizations are applied
        assertEquals(FontWeight.SemiBold, typography.noteTitle.fontWeight)
        assertEquals(FontWeight.Normal, typography.noteContent.fontWeight)
    }
}