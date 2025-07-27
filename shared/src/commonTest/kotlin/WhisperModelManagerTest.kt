import com.module.notelycompose.transcription.domain.WhisperLoadResult
import com.module.notelycompose.transcription.domain.WhisperModelLoader
import com.module.notelycompose.transcription.domain.WhisperModelManager
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WhisperModelManagerTest {
    
    private class TestWhisperModelLoader : WhisperModelLoader {
        var loadCallCount = 0
        var releaseCallCount = 0
        var shouldThrowOnLoad = false
        
        override suspend fun loadModel() {
            loadCallCount++
            if (shouldThrowOnLoad) {
                throw RuntimeException("Test error")
            }
        }
        
        override suspend fun releaseModel() {
            releaseCallCount++
        }
    }
    
    @Test
    fun testSingletonBehavior() = runTest {
        val loader = TestWhisperModelLoader()
        val manager = WhisperModelManager(loader)
        
        // First call should load the model
        val result1 = manager.ensureModelLoaded()
        assertTrue(result1 is WhisperLoadResult.Success)
        assertEquals(1, loader.loadCallCount)
        assertTrue(manager.isLoaded())
        
        // Second call should not load again
        val result2 = manager.ensureModelLoaded()
        assertTrue(result2 is WhisperLoadResult.Success)
        assertEquals(1, loader.loadCallCount) // Should still be 1
        assertTrue(manager.isLoaded())
    }
    
    @Test
    fun testTranscriptionSessionTracking() = runTest {
        val loader = TestWhisperModelLoader()
        val manager = WhisperModelManager(loader)
        
        // Load model
        manager.ensureModelLoaded()
        
        // Start transcription session
        manager.startTranscriptionSession()
        val stats1 = manager.getStats()
        assertEquals(1, stats1.activeTranscriptions)
        
        // Start another session
        manager.startTranscriptionSession()
        val stats2 = manager.getStats()
        assertEquals(2, stats2.activeTranscriptions)
        
        // End one session
        manager.endTranscriptionSession()
        val stats3 = manager.getStats()
        assertEquals(1, stats3.activeTranscriptions)
        
        // End remaining session
        manager.endTranscriptionSession()
        val stats4 = manager.getStats()
        assertEquals(0, stats4.activeTranscriptions)
    }
    
    @Test
    fun testMemoryPressureHandling() = runTest {
        val loader = TestWhisperModelLoader()
        val manager = WhisperModelManager(loader)
        
        // Load model
        manager.ensureModelLoaded()
        assertTrue(manager.isLoaded())
        
        // Memory pressure with no active transcriptions should release model
        manager.handleMemoryPressure()
        assertFalse(manager.isLoaded())
        assertEquals(1, loader.releaseCallCount)
        
        // Load again and start transcription
        manager.ensureModelLoaded()
        manager.startTranscriptionSession()
        assertTrue(manager.isLoaded())
        
        // Memory pressure with active transcription should NOT release model
        manager.handleMemoryPressure()
        assertTrue(manager.isLoaded())
        assertEquals(1, loader.releaseCallCount) // Should still be 1
    }
    
    @Test
    fun testErrorHandling() = runTest {
        val loader = TestWhisperModelLoader()
        loader.shouldThrowOnLoad = true
        val manager = WhisperModelManager(loader)
        
        // Loading should fail and return error result
        val result = manager.ensureModelLoaded()
        assertTrue(result is WhisperLoadResult.Failure.LoadError)
        assertFalse(manager.isLoaded())
        assertEquals(1, loader.loadCallCount)
    }
    
    @Test
    fun testForceRelease() = runTest {
        val loader = TestWhisperModelLoader()
        val manager = WhisperModelManager(loader)
        
        // Load model and start transcription
        manager.ensureModelLoaded()
        manager.startTranscriptionSession()
        assertTrue(manager.isLoaded())
        
        // Force release should work even with active transcriptions
        manager.forceRelease()
        assertFalse(manager.isLoaded())
        assertEquals(1, loader.releaseCallCount)
        
        // Should also reset active transcription count
        val stats = manager.getStats()
        assertEquals(0, stats.activeTranscriptions)
    }
}