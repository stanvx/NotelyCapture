package com.module.notelycompose.platform.presentation

import com.module.notelycompose.platform.Platform
import com.module.notelycompose.platform.PlatformUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for PlatformViewModel state management and platform operations
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlatformViewModelTest {

    private lateinit var viewModel: PlatformViewModel
    private lateinit var mockPlatform: MockPlatform
    private lateinit var mockPlatformUtils: MockPlatformUtils
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockPlatform = MockPlatform()
        mockPlatformUtils = MockPlatformUtils()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): PlatformViewModel {
        return PlatformViewModel(
            platformInfo = mockPlatform,
            platformUtils = mockPlatformUtils
        )
    }

    @Test
    fun `initial state loads platform information correctly`() = runTest {
        mockPlatform.apply {
            appVersion = "1.2.3"
            name = "Android"
            isAndroid = true
            isTablet = false
            isLandscape = true
        }

        viewModel = createViewModel()
        val state = viewModel.state.first()

        assertEquals("1.2.3", state.appVersion)
        assertEquals("Android", state.platformName)
        assertTrue(state.isAndroid)
        assertFalse(state.isTablet)
        assertTrue(state.isLandscape)
        assertFalse(state.isExporting)
        assertNull(state.exportSuccess)
        assertNull(state.exportMessage)
    }

    @Test
    fun `shareText calls platform utils when text is not blank`() = runTest {
        viewModel = createViewModel()
        
        viewModel.shareText("Hello World")
        
        assertEquals("Hello World", mockPlatformUtils.sharedText)
    }

    @Test
    fun `shareText does not call platform utils when text is blank`() = runTest {
        viewModel = createViewModel()
        
        viewModel.shareText("")
        viewModel.shareText("   ")
        
        assertNull(mockPlatformUtils.sharedText)
    }

    @Test
    fun `shareRecording calls export when path is not blank`() = runTest {
        viewModel = createViewModel()
        
        viewModel.shareRecording("/path/to/recording.wav")
        
        assertEquals("/path/to/recording.wav", mockPlatformUtils.exportedPath)
        assertTrue(mockPlatformUtils.exportFileName.contains("recording_"))
        assertTrue(mockPlatformUtils.exportFileName.endsWith(".wav"))
    }

    @Test
    fun `shareRecording does not call export when path is blank`() = runTest {
        viewModel = createViewModel()
        
        viewModel.shareRecording("")
        viewModel.shareRecording("   ")
        
        assertNull(mockPlatformUtils.exportedPath)
    }

    @Test
    fun `onExportAudio sets exporting state and generates correct filename`() = runTest {
        viewModel = createViewModel()
        
        viewModel.onExportAudio("/path/to/test.wav")
        
        val state = viewModel.state.first()
        assertEquals("/path/to/test.wav", mockPlatformUtils.exportedPath)
        assertTrue(mockPlatformUtils.exportFileName.startsWith("recording_"))
        assertTrue(mockPlatformUtils.exportFileName.endsWith(".wav"))
        assertFalse(state.isExporting) // Should be false after callback in test
    }

    @Test
    fun `export success updates state correctly`() = runTest {
        mockPlatformUtils.exportResult = true
        mockPlatformUtils.exportMessage = "Success!"
        
        viewModel = createViewModel()
        viewModel.onExportAudio("/path/to/test.wav")
        
        val state = viewModel.state.first()
        assertFalse(state.isExporting)
        assertEquals(true, state.exportSuccess)
        assertEquals("Success!", state.exportMessage)
    }

    @Test
    fun `export failure updates state correctly`() = runTest {
        mockPlatformUtils.exportResult = false
        mockPlatformUtils.exportMessage = "Export failed"
        
        viewModel = createViewModel()
        viewModel.onExportAudio("/path/to/test.wav")
        
        val state = viewModel.state.first()
        assertFalse(state.isExporting)
        assertEquals(false, state.exportSuccess)
        assertEquals("Export failed", state.exportMessage)
    }

    @Test
    fun `export failure with null message provides default message`() = runTest {
        mockPlatformUtils.exportResult = false
        mockPlatformUtils.exportMessage = null
        
        viewModel = createViewModel()
        viewModel.onExportAudio("/path/to/test.wav")
        
        val state = viewModel.state.first()
        assertEquals(false, state.exportSuccess)
        assertEquals("Failed to export audio", state.exportMessage)
    }

    @Test
    fun `export success with null message provides default message`() = runTest {
        mockPlatformUtils.exportResult = true
        mockPlatformUtils.exportMessage = null
        
        viewModel = createViewModel()
        viewModel.onExportAudio("/path/to/test.wav")
        
        val state = viewModel.state.first()
        assertEquals(true, state.exportSuccess)
        assertEquals("Audio exported successfully", state.exportMessage)
    }

    @Test
    fun `clearExportStatus resets export state`() = runTest {
        mockPlatformUtils.exportResult = true
        mockPlatformUtils.exportMessage = "Export completed"
        
        viewModel = createViewModel()
        viewModel.onExportAudio("/path/to/test.wav")
        
        // Verify export state is set
        var state = viewModel.state.first()
        assertEquals(true, state.exportSuccess)
        assertEquals("Export completed", state.exportMessage)
        
        // Clear export status
        viewModel.clearExportStatus()
        
        // Verify export state is cleared
        state = viewModel.state.first()
        assertNull(state.exportSuccess)
        assertNull(state.exportMessage)
    }

    @Test
    fun `onExportAudio does not call platform utils when path is blank`() = runTest {
        viewModel = createViewModel()
        
        viewModel.onExportAudio("")
        viewModel.onExportAudio("   ")
        
        assertNull(mockPlatformUtils.exportedPath)
    }

    @Test
    fun `tablet state is correctly loaded from platform`() = runTest {
        mockPlatform.isTablet = true
        
        viewModel = createViewModel()
        val state = viewModel.state.first()
        
        assertTrue(state.isTablet)
    }

    @Test
    fun `landscape state is correctly loaded from platform`() = runTest {
        mockPlatform.isLandscape = false
        
        viewModel = createViewModel()
        val state = viewModel.state.first()
        
        assertFalse(state.isLandscape)
    }
}

// Mock implementations
private class MockPlatform : Platform {
    override var appVersion: String = "1.0.0"
    override var name: String = "Test Platform"
    override var isAndroid: Boolean = true
    override var isTablet: Boolean = false
    override var isLandscape: Boolean = false
}

private class MockPlatformUtils : PlatformUtils {
    var sharedText: String? = null
    var exportedPath: String? = null
    var exportFileName: String = ""
    var exportResult: Boolean = true
    var exportMessage: String? = null

    override fun shareText(text: String) {
        sharedText = text
    }

    override fun exportRecordingWithFilePicker(
        sourcePath: String,
        fileName: String,
        callback: (Boolean, String?) -> Unit
    ) {
        exportedPath = sourcePath
        exportFileName = fileName
        callback(exportResult, exportMessage)
    }
}