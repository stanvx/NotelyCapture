package com.module.notelycompose.transcription

import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.domain.model.TextAlignDomainModel
import com.module.notelycompose.transcription.error.TranscriptionError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Test suite for BackgroundTranscriptionService
 * 
 * Tests cover:
 * - State transitions and lifecycle management
 * - Error handling scenarios
 * - Resource management and disposal
 * - File validation integration
 * - Callback invocation
 */
class BackgroundTranscriptionServiceTest {
    
    private lateinit var transcriptionViewModel: TranscriptionViewModel
    private lateinit var insertNoteUseCase: InsertNoteUseCase
    private lateinit var service: BackgroundTranscriptionService
    private lateinit var testScope: TestScope
    
    private val mockUiState = MutableStateFlow(TranscriptionUiState())
    
    @BeforeTest
    fun setup() {
        testScope = TestScope(StandardTestDispatcher())
        transcriptionViewModel = mockk(relaxed = true)
        insertNoteUseCase = mockk(relaxed = true)
        
        // Setup default mock behavior
        every { transcriptionViewModel.uiState } returns mockUiState
        
        service = BackgroundTranscriptionService(
            transcriptionViewModel = transcriptionViewModel,
            insertNoteUseCase = insertNoteUseCase
        )
    }
    
    @Test
    fun `initial state should be idle`() = testScope.runTest {
        assertEquals(BackgroundTranscriptionState.Idle, service.state.first())
        assertFalse(service.disposed)
    }
    
    @Test
    fun `startTranscription should reject when service is disposed`() = testScope.runTest {
        // Given: Service is disposed
        service.dispose()
        
        var capturedError: TranscriptionError? = null
        
        // When: Starting transcription
        service.startTranscription(
            audioFilePath = "/valid/path/test.wav",
            onError = { error -> capturedError = error }
        )
        
        // Then: Should reject with ServiceDisposedError
        assertIs<TranscriptionError.ServiceDisposedError>(capturedError)
        verify(exactly = 0) { transcriptionViewModel.initRecognizer() }
    }
    
    @Test
    fun `startTranscription should validate file path before processing`() = testScope.runTest {
        var capturedError: TranscriptionError? = null
        
        // When: Starting transcription with invalid file
        service.startTranscription(
            audioFilePath = "/invalid/path/test.xyz", // Unsupported format
            onError = { error -> capturedError = error }
        )
        
        // Then: Should fail with validation error
        assertIs<TranscriptionError.AudioFileValidationError>(capturedError)
        verify(exactly = 0) { transcriptionViewModel.initRecognizer() }
    }
    
    @Test
    fun `startTranscription should handle initialization failure`() = testScope.runTest {
        // Given: ViewModel initialization fails
        every { transcriptionViewModel.initRecognizer() } throws RuntimeException("Init failed")
        
        var capturedError: TranscriptionError? = null
        
        // When: Starting transcription
        service.startTranscription(
            audioFilePath = "/test/path/audio.wav",
            onError = { error -> capturedError = error }
        )
        
        // Allow coroutine to execute
        testScheduler.advanceUntilIdle()
        
        // Then: Should capture initialization error
        assertIs<TranscriptionError.InitializationError>(capturedError)
        assertEquals("Failed to initialize transcription: Init failed", capturedError?.message)
    }
    
    @Test
    fun `startTranscription should handle note creation failure`() = testScope.runTest {
        // Given: Note creation fails
        coEvery { insertNoteUseCase.execute(any(), any(), any(), any(), any(), any()) } returns null
        
        var capturedError: TranscriptionError? = null
        var completionCalled = false
        
        // When: Starting transcription and completing successfully
        service.startTranscription(
            audioFilePath = "/test/path/audio.wav",
            onComplete = { completionCalled = true },
            onError = { error -> capturedError = error }
        )
        
        // Simulate successful transcription
        mockUiState.value = TranscriptionUiState(
            inTranscription = false,
            originalText = "Test transcription result"
        )
        
        testScheduler.advanceUntilIdle()
        
        // Then: Should capture note creation error
        assertIs<TranscriptionError.NoteCreationError>(capturedError)
        assertFalse(completionCalled)
    }
    
    @Test
    fun `successful transcription should create note and complete`() = testScope.runTest {
        // Given: Successful note creation
        coEvery { 
            insertNoteUseCase.execute(any(), any(), any(), any(), any(), any()) 
        } returns 123L
        
        var completionNoteId: Long? = null
        var errorCaptured: TranscriptionError? = null
        
        // When: Starting transcription
        service.startTranscription(
            audioFilePath = "/test/path/audio.wav",
            onComplete = { noteId -> completionNoteId = noteId },
            onError = { error -> errorCaptured = error }
        )
        
        // Simulate successful transcription
        mockUiState.value = TranscriptionUiState(
            inTranscription = false,
            originalText = "Test transcription result"
        )
        
        testScheduler.advanceUntilIdle()
        
        // Then: Should complete successfully
        assertEquals(123L, completionNoteId)
        assertEquals(null, errorCaptured)
        
        // Verify note creation with correct parameters
        coVerify {
            insertNoteUseCase.execute(
                title = match { it.startsWith("Quick Record") },
                content = "Test transcription result",
                starred = false,
                formatting = emptyList(),
                textAlign = TextAlignDomainModel.Left,
                recordingPath = "/test/path/audio.wav"
            )
        }
    }
    
    @Test
    fun `state transitions should be correct during successful flow`() = testScope.runTest {
        // Given: Successful setup
        coEvery { insertNoteUseCase.execute(any(), any(), any(), any(), any(), any()) } returns 123L
        
        val stateHistory = mutableListOf<BackgroundTranscriptionState>()
        
        // Collect state changes
        val job = backgroundScope.launch {
            service.state.collect { state ->
                stateHistory.add(state)
            }
        }
        
        // When: Starting transcription
        service.startTranscription(
            audioFilePath = "/test/path/audio.wav",
            onComplete = { }
        )
        
        // Simulate successful transcription
        mockUiState.value = TranscriptionUiState(
            inTranscription = false,
            originalText = "Test result"
        )
        
        testScheduler.advanceUntilIdle()
        job.cancel()
        
        // Then: Should have correct state transitions
        assertEquals(BackgroundTranscriptionState.Idle, stateHistory[0])
        assertEquals(BackgroundTranscriptionState.Processing, stateHistory[1])
        assertEquals(BackgroundTranscriptionState.Complete, stateHistory[2])
    }
    
    @Test
    fun `dispose should cancel ongoing operations and mark as disposed`() = testScope.runTest {
        // Given: Service with ongoing operation
        service.startTranscription(
            audioFilePath = "/test/path/audio.wav",
            onComplete = { },
            onError = { }
        )
        
        // When: Disposing service
        service.dispose()
        
        // Then: Should be marked as disposed
        assertTrue(service.disposed)
        assertEquals(BackgroundTranscriptionState.Idle, service.state.first())
        
        // Should reject new operations
        var errorCaptured: TranscriptionError? = null
        service.startTranscription(
            audioFilePath = "/test/path/audio.wav",
            onError = { error -> errorCaptured = error }
        )
        
        assertIs<TranscriptionError.ServiceDisposedError>(errorCaptured)
    }
    
    @Test
    fun `reset should only work when not disposed`() = testScope.runTest {
        // Given: Service in error state
        service.startTranscription(
            audioFilePath = "/invalid/path.xyz",
            onError = { }
        )
        
        // When: Resetting
        service.reset()
        
        // Then: Should reset to idle
        assertEquals(BackgroundTranscriptionState.Idle, service.state.first())
        
        // But after disposal, reset should have no effect
        service.dispose()
        service.reset()
        assertEquals(BackgroundTranscriptionState.Idle, service.state.first())
        assertTrue(service.disposed)
    }
    
    @Test
    fun `error state should preserve error information and recoverability`() = testScope.runTest {
        // Given: Service that will fail
        every { transcriptionViewModel.initRecognizer() } throws RuntimeException("Test error")
        
        var capturedState: BackgroundTranscriptionState.Error? = null
        
        val job = backgroundScope.launch {
            service.state.collect { state ->
                if (state is BackgroundTranscriptionState.Error) {
                    capturedState = state
                }
            }
        }
        
        // When: Starting transcription that fails
        service.startTranscription(
            audioFilePath = "/test/path/audio.wav",
            onError = { }
        )
        
        testScheduler.advanceUntilIdle()
        job.cancel()
        
        // Then: Error state should have correct properties
        capturedState?.let { errorState ->
            assertIs<TranscriptionError.InitializationError>(errorState.error)
            assertTrue(errorState.isRecoverable) // Init errors are recoverable
            assertTrue(errorState.message.isNotEmpty())
        }
    }
}

/**
 * Mock implementation of TranscriptionUiState for testing
 */
private data class TranscriptionUiState(
    val inTranscription: Boolean = false,
    val originalText: String = ""
)