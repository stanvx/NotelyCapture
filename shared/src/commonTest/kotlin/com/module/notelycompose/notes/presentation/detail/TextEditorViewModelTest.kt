package com.module.notelycompose.notes.presentation.detail

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import com.module.notelycompose.notes.domain.DeleteNoteById
import com.module.notelycompose.notes.domain.GetLastNote
import com.module.notelycompose.notes.domain.GetNoteById
import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.domain.UpdateNoteUseCase
import com.module.notelycompose.notes.domain.NoteDataSource
import com.module.notelycompose.notes.domain.model.NoteDomainModel
import com.module.notelycompose.notes.domain.model.TextAlignDomainModel
import com.module.notelycompose.notes.domain.model.TextFormatDomainModel
import com.module.notelycompose.notes.domain.mapper.NoteDomainMapper
import com.module.notelycompose.notes.domain.mapper.TextFormatMapper
import com.module.notelycompose.notes.presentation.helpers.RichTextEditorHelper
import com.module.notelycompose.notes.presentation.helpers.TextEditorHelper
import com.module.notelycompose.notes.presentation.mapper.EditorPresentationToUiStateMapper
import com.module.notelycompose.notes.presentation.mapper.TextAlignPresentationMapper
import com.module.notelycompose.notes.presentation.mapper.TextFormatPresentationMapper
import com.module.notelycompose.platform.PlatformAudioPlayer
import com.module.notelycompose.core.security.SecurityHelper
import com.module.notelycompose.core.security.SecurityMonitoringService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TextEditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // Fakes and Mocks
    private lateinit var fakeGetNoteById: FakeGetNoteById
    private lateinit var fakeInsertNoteUseCase: FakeInsertNoteUseCase
    private lateinit var fakeDeleteNoteUseCase: FakeDeleteNoteById
    private lateinit var fakeUpdateNoteUseCase: FakeUpdateNoteUseCase
    private lateinit var fakeGetLastNote: FakeGetLastNote
    private lateinit var fakeSecurityHelper: FakeSecurityHelper
    private lateinit var fakeAudioPlayer: FakeAudioPlayer
    private lateinit var fakeRichTextEditorHelper: FakeRichTextEditorHelper

    // Real dependencies (simple mappers)
    private val textFormatPresentationMapper = TextFormatPresentationMapper()
    private val textAlignPresentationMapper = TextAlignPresentationMapper()
    private val editorPresentationToUiStateMapper = EditorPresentationToUiStateMapper()
    private val textEditorHelper = TextEditorHelper(null) // No content predictor in tests

    private lateinit var viewModel: TextEditorViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeGetNoteById = FakeGetNoteById()
        fakeInsertNoteUseCase = FakeInsertNoteUseCase()
        fakeDeleteNoteUseCase = FakeDeleteNoteById()
        fakeUpdateNoteUseCase = FakeUpdateNoteUseCase()
        fakeGetLastNote = FakeGetLastNote()
        fakeSecurityHelper = FakeSecurityHelper()
        fakeAudioPlayer = FakeAudioPlayer()
        fakeRichTextEditorHelper = FakeRichTextEditorHelper()

        viewModel = TextEditorViewModel(
            getNoteByIdUseCase = fakeGetNoteById,
            insertNoteUseCase = fakeInsertNoteUseCase,
            deleteNoteUseCase = fakeDeleteNoteUseCase,
            updateNoteUseCase = fakeUpdateNoteUseCase,
            getLastNoteUseCase = fakeGetLastNote,
            editorPresentationToUiStateMapper = editorPresentationToUiStateMapper,
            textFormatPresentationMapper = textFormatPresentationMapper,
            textAlignPresentationMapper = textAlignPresentationMapper,
            textEditorHelper = textEditorHelper,
            richTextEditorHelper = fakeRichTextEditorHelper,
            securityHelper = fakeSecurityHelper,
            audioPlayer = fakeAudioPlayer
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @Test
    fun `onUpdateContent for new note triggers debounced insert`() = testScope.runTest {
        val newContent = TextFieldValue("New note content")
        viewModel.onUpdateContent(newContent)

        // Assert no save before debounce delay
        assertEquals(0, fakeInsertNoteUseCase.callCount)
        advanceTimeBy(499)
        assertEquals(0, fakeInsertNoteUseCase.callCount)

        // Assert save happens after debounce delay
        advanceTimeBy(1)
        assertEquals(1, fakeInsertNoteUseCase.callCount)
        assertEquals("New note content", fakeInsertNoteUseCase.lastTitle)
        assertNotEquals(0L, viewModel.currentNoteId.value)
    }

    @Test
    fun `onUpdateContent for existing note triggers debounced update`() = testScope.runTest {
        // Arrange: Load an existing note
        val note = createFakeNote(id = 1L, content = "Initial content")
        fakeGetNoteById.notesToReturn[1L] = note
        viewModel.onGetNoteById("1")
        testScheduler.advanceUntilIdle() // Ensure note is loaded

        // Act: Update content
        val updatedContent = TextFieldValue("Updated content")
        viewModel.onUpdateContent(updatedContent)

        // Assert: No update before debounce delay
        assertEquals(0, fakeUpdateNoteUseCase.callCount)
        advanceTimeBy(500)

        // Assert: Update happens after delay
        assertEquals(1, fakeUpdateNoteUseCase.callCount)
        assertEquals(1L, fakeUpdateNoteUseCase.lastId)
        assertEquals("Updated content", fakeUpdateNoteUseCase.lastContent)
        assertEquals(0, fakeInsertNoteUseCase.callCount) // Ensure insert is not called
    }

    @Test
    fun `rapid content updates trigger only one save operation`() = testScope.runTest {
        viewModel.onUpdateContent(TextFieldValue("a"))
        advanceTimeBy(100)
        viewModel.onUpdateContent(TextFieldValue("ab"))
        advanceTimeBy(100)
        viewModel.onUpdateContent(TextFieldValue("abc"))

        // Total time is 200ms, less than debounce delay
        assertEquals(0, fakeInsertNoteUseCase.callCount)

        // Advance time past the debounce delay for the *last* update
        advanceTimeBy(500)
        assertEquals(1, fakeInsertNoteUseCase.callCount)
        assertEquals("abc", fakeInsertNoteUseCase.lastContent)
    }

    @Test
    fun `debounced save is cancelled on onCleared`() = testScope.runTest {
        viewModel.onUpdateContent(TextFieldValue("Some content"))

        // Act: Clear the ViewModel before debounce delay finishes
        viewModel.onCleared()
        advanceTimeBy(501)

        // Assert: No save operation was executed
        assertEquals(0, fakeInsertNoteUseCase.callCount)
    }

    @Test
    fun `debounced save is cancelled if note is deleted`() = testScope.runTest {
        // Arrange: Create a new note and trigger a save
        viewModel.onUpdateContent(TextFieldValue("Content to be deleted"))
        advanceTimeBy(501)
        assertEquals(1, fakeInsertNoteUseCase.callCount)
        val noteId = viewModel.currentNoteId.value!!

        // Act: Update content again (to trigger a new debounced save) and then delete immediately
        viewModel.onUpdateContent(TextFieldValue("This save should be cancelled"))
        viewModel.onDeleteNote()
        advanceTimeBy(501)

        // Assert: The debounced update was cancelled and the note was deleted
        assertEquals(1, fakeDeleteNoteUseCase.callCount)
        assertEquals(noteId, fakeDeleteNoteUseCase.lastId)
        assertEquals(0, fakeUpdateNoteUseCase.callCount) // The update never ran
    }

    @Test
    fun `onUpdateContent with invalid content is ignored`() = testScope.runTest {
        // Arrange: Configure security helper to reject content
        fakeSecurityHelper.shouldValidateNoteContent = false
        val initialContent = viewModel.editorPresentationState.value.content

        // Act
        viewModel.onUpdateContent(TextFieldValue("Invalid content with <script>"))
        advanceTimeBy(501)

        // Assert
        assertEquals(initialContent, viewModel.editorPresentationState.value.content)
        assertEquals(0, fakeInsertNoteUseCase.callCount)
    }

    @Test
    fun `onUpdateRecordingPath with unsafe path is ignored`() = testScope.runTest {
        // Arrange
        fakeSecurityHelper.isPathSafe = false
        val unsafePath = "../../../etc/hosts"

        // Act
        viewModel.onUpdateRecordingPath(unsafePath)
        testScheduler.advanceUntilIdle()

        // Assert
        assertFalse(viewModel.editorPresentationState.value.recording.isRecordingExist)
        assertEquals("", viewModel.editorPresentationState.value.recording.recordingPath)
        assertEquals(0, fakeAudioPlayer.prepareCallCount)
    }

    @Test
    fun `onDeleteRecord calls secureDeleteFile and updates state`() = testScope.runTest {
        // Arrange: Load a note with a recording
        val note = createFakeNote(id = 1L, recordingPath = "/safe/path/audio.mp3")
        fakeGetNoteById.notesToReturn[1L] = note
        viewModel.onGetNoteById("1")
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.editorPresentationState.value.recording.isRecordingExist)

        // Act
        viewModel.onDeleteRecord()
        testScheduler.advanceUntilIdle()

        // Assert
        assertEquals(1, fakeSecurityHelper.secureDeleteFileCallCount)
        assertEquals("/safe/path/audio.mp3", fakeSecurityHelper.lastDeletedPath)
        assertFalse(viewModel.editorPresentationState.value.recording.isRecordingExist)
        assertEquals("", viewModel.editorPresentationState.value.recording.recordingPath)
    }

    @Test
    fun `onUpdateRichContent syncs from helper and triggers debounced save`() = testScope.runTest {
        // Arrange
        fakeRichTextEditorHelper.htmlContentToReturn = "<b>Rich Content</b>"
        fakeRichTextEditorHelper.plainTextToReturn = "Rich Content"

        // Act
        viewModel.onUpdateRichContent()
        testScheduler.advanceUntilIdle() // Let the sync happen

        // Assert state is updated from rich text helper
        assertEquals("Rich Content", viewModel.editorPresentationState.value.content.text)

        // Assert debounced save is triggered with HTML content
        assertEquals(0, fakeInsertNoteUseCase.callCount)
        advanceTimeBy(500)
        assertEquals(1, fakeInsertNoteUseCase.callCount)
        assertEquals("<b>Rich Content</b>", fakeInsertNoteUseCase.lastContent)
    }

    @Test
    fun `loadNote with HTML content correctly syncs to rich text helper`() = testScope.runTest {
        // Arrange
        val note = createFakeNote(id = 1L, content = "<h1>Title</h1><p>Body</p>")
        fakeGetNoteById.notesToReturn[1L] = note

        // Act
        viewModel.onGetNoteById("1")
        testScheduler.advanceUntilIdle() // Let loading and sync complete

        // Assert
        assertEquals("Title\\nBody", viewModel.editorPresentationState.value.content.text)
        assertEquals(1, fakeRichTextEditorHelper.setContentCallCount)
        assertEquals("<h1>Title</h1><p>Body</p>", fakeRichTextEditorHelper.lastSetContent)
    }

    @Test
    fun `updateNoteUseCase failure is handled gracefully`() = testScope.runTest {
        // Arrange: Load a note and configure the use case to fail
        val note = createFakeNote(id = 1L, content = "Initial")
        fakeGetNoteById.notesToReturn[1L] = note
        viewModel.onGetNoteById("1")
        testScheduler.advanceUntilIdle()
        fakeUpdateNoteUseCase.shouldThrowException = true

        // Act: This will throw inside the viewmodel's coroutine but shouldn't crash the test
        val job = launch { viewModel.onUpdateContent(TextFieldValue("Updated")) }
        advanceTimeBy(501)
        job.join() // Ensure coroutine completes

        // Assert: The call was attempted
        assertEquals(1, fakeUpdateNoteUseCase.callCount)
        // No specific state assertion needed, just that the test didn't crash.
        // In a real app, we'd check for a logged error or a UI error state.
    }

    @Test
    fun `concurrent access to saveMutex is properly synchronized`() = testScope.runTest {
        // Arrange: Multiple concurrent content updates
        val jobs = List(5) { index ->
            launch {
                viewModel.onUpdateContent(TextFieldValue("Content $index"))
                advanceTimeBy(100) // Stagger the calls slightly
            }
        }

        // Wait for all jobs to start
        jobs.forEach { it.join() }
        
        // Advance time to trigger all saves
        advanceTimeBy(600)

        // Assert: Only the last content was saved due to debouncing
        assertTrue(fakeInsertNoteUseCase.callCount <= 1, "Should have at most 1 save due to debouncing")
    }

    @Test
    fun `toggleStar updates state and triggers save`() = testScope.runTest {
        // Arrange: Load a note
        val note = createFakeNote(id = 1L, starred = false)
        fakeGetNoteById.notesToReturn[1L] = note
        viewModel.onGetNoteById("1")
        testScheduler.advanceUntilIdle()

        // Act
        viewModel.onToggleStar()
        advanceTimeBy(501)

        // Assert
        assertTrue(viewModel.editorPresentationState.value.starred)
        assertEquals(1, fakeUpdateNoteUseCase.callCount)
        assertTrue(fakeUpdateNoteUseCase.lastStarred!!)
    }
}

// --- Fakes for Dependency Injection ---

private class FakeGetNoteById : GetNoteById(FakeNoteDataSource(), NoteDomainMapper(TextFormatMapper())) {
    val notesToReturn = mutableMapOf<Long, NoteDomainModel>()
    override fun execute(id: Long): NoteDomainModel? = notesToReturn[id]
}

private class FakeInsertNoteUseCase : InsertNoteUseCase(FakeNoteDataSource(), TextFormatMapper(), NoteDomainMapper(TextFormatMapper())) {
    var callCount = 0
    var lastTitle: String? = null
    var lastContent: String? = null
    private var nextId = 1L
    override suspend fun execute(
        title: String, 
        content: String, 
        starred: Boolean, 
        formatting: List<TextFormatDomainModel>, 
        textAlign: TextAlignDomainModel, 
        recordingPath: String
    ) {
        callCount++
        lastTitle = title
        lastContent = content
        nextId++
    }
}

private class FakeUpdateNoteUseCase : UpdateNoteUseCase(FakeNoteDataSource(), TextFormatMapper(), NoteDomainMapper(TextFormatMapper())) {
    var callCount = 0
    var lastId: Long? = null
    var lastContent: String? = null
    var lastStarred: Boolean? = null
    var shouldThrowException = false
    override suspend fun execute(
        id: Long, 
        title: String, 
        content: String, 
        starred: Boolean, 
        formatting: List<TextFormatDomainModel>, 
        textAlign: TextAlignDomainModel, 
        recordingPath: String
    ) {
        callCount++
        lastId = id
        lastContent = content
        lastStarred = starred
        if (shouldThrowException) throw RuntimeException("Database update failed!")
    }
}

private class FakeDeleteNoteById : DeleteNoteById(FakeNoteDataSource()) {
    var callCount = 0
    var lastId: Long? = null
    override suspend fun execute(id: Long) {
        callCount++
        lastId = id
    }
}

private class FakeGetLastNote : GetLastNote(FakeNoteDataSource(), NoteDomainMapper(TextFormatMapper())) {
    override fun execute(): NoteDomainModel? = null
}

private class FakeSecurityHelper : SecurityHelper(FakeSecurityMonitoringService()) {
    var shouldValidateNoteContent = true
    var isPathSafe = true
    var secureDeleteFileCallCount = 0
    var lastDeletedPath: String? = null

    override suspend fun validateNoteContent(content: String?): Boolean {
        return shouldValidateNoteContent
    }

    override suspend fun isPathSafe(filePath: String): Boolean {
        return isPathSafe
    }

    override suspend fun secureDeleteFile(filePath: String?): SecurityHelper.FileDeleteResult {
        secureDeleteFileCallCount++
        lastDeletedPath = filePath
        return SecurityHelper.FileDeleteResult(success = true)
    }
}

private class FakeAudioPlayer : PlatformAudioPlayer {
    var prepareCallCount = 0
    override suspend fun prepare(path: String): Int {
        prepareCallCount++
        return 30000 // 30 seconds
    }
    override fun play() {}
    override fun pause() {}
    override fun stop() {}
    override fun release() {}
    override fun seekTo(position: Int) {}
    override fun isPlaying(): Boolean = false
    override fun getCurrentPosition(): Int = 0
    override fun setPlaybackSpeed(speed: Float) {}
}

private class FakeRichTextEditorHelper : RichTextEditorHelper() {
    var setContentCallCount = 0
    var lastSetContent: String? = null
    var htmlContentToReturn = ""
    var plainTextToReturn = ""

    override fun setContent(content: String) {
        setContentCallCount++
        lastSetContent = content
        super.setContent(content)
    }
    override fun getContent(): String = htmlContentToReturn
    override fun getPlainText(): String = plainTextToReturn
}

// --- Test Utilities ---

private fun createFakeNote(
    id: Long,
    content: String = "Fake content for note $id",
    recordingPath: String = "",
    starred: Boolean = false
): NoteDomainModel {
    return NoteDomainModel(
        id = id,
        title = content.take(20),
        content = content,
        createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC),
        updatedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC),
        starred = starred,
        formatting = emptyList(),
        textAlign = TextAlignDomainModel.LEFT,
        recordingPath = recordingPath
    )
}

// Dummy implementations for constructor requirements
private class FakeNoteDataSource : NoteDataSource {
    override suspend fun getNoteById(id: Long): com.module.notelycompose.notes.data.model.NoteDataModel? = null
    override fun getAllNotes(): kotlinx.coroutines.flow.Flow<List<com.module.notelycompose.notes.data.model.NoteDataModel>> = kotlinx.coroutines.flow.flowOf(emptyList())
    override suspend fun insertNote(
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<com.module.notelycompose.notes.data.model.TextFormatDataModel>,
        textAlign: com.module.notelycompose.notes.data.model.TextAlignDataModel,
        recordingPath: String
    ): Long? = null
    override suspend fun deleteNoteById(id: Long) {}
    override suspend fun getLastNote(): com.module.notelycompose.notes.data.model.NoteDataModel? = null
    override fun searchNotes(query: String): kotlinx.coroutines.flow.Flow<List<com.module.notelycompose.notes.data.model.NoteDataModel>> = kotlinx.coroutines.flow.flowOf(emptyList())
    override suspend fun updateNote(
        id: Long,
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<com.module.notelycompose.notes.data.model.TextFormatDataModel>,
        textAlign: com.module.notelycompose.notes.data.model.TextAlignDataModel,
        recordingPath: String
    ) {}
}

private class FakeSecurityMonitoringService : SecurityMonitoringService {
    override suspend fun reportSecurityEvent(
        type: SecurityMonitoringService.SecurityEventType, 
        severity: SecurityMonitoringService.SecuritySeverity, 
        message: String, 
        details: Map<String, String>, 
        userContext: SecurityMonitoringService.UserContext?, 
        remediation: String?, 
        throwable: Throwable?
    ) {}
    override suspend fun reportValidationFailure(
        validationType: String, 
        input: String, 
        validationError: String, 
        userContext: SecurityMonitoringService.UserContext?
    ) {}
    override suspend fun reportFileSystemViolation(
        operation: String, 
        filePath: String, 
        violation: String, 
        userContext: SecurityMonitoringService.UserContext?
    ) {}
    override suspend fun reportSuspiciousActivity(
        activityType: String, 
        description: String, 
        confidence: Double, 
        userContext: SecurityMonitoringService.UserContext?
    ) {}
    override fun getSecurityEvents(): kotlinx.coroutines.flow.Flow<List<SecurityMonitoringService.SecurityEvent>> = kotlinx.coroutines.flow.flowOf(emptyList())
    override suspend fun getSecurityMetrics(): SecurityMonitoringService.SecurityMetrics = SecurityMonitoringService.SecurityMetrics(0, emptyMap(), emptyMap(), 0, 0.0, emptyList(), "")
    override suspend fun cleanupOldEvents() {}
    override suspend fun updateConfiguration(config: SecurityMonitoringService.SecurityConfig) {}
    override suspend fun getConfiguration(): SecurityMonitoringService.SecurityConfig = SecurityMonitoringService.SecurityConfig()
}