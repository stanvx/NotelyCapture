package com.module.notelycompose.testing

import kotlinx.coroutines.flow.Flow

/**
 * Test interfaces for dependency injection and mocking.
 * 
 * These interfaces provide testable abstractions for use cases and services
 * that are commonly used in ViewModels and other components. By using interfaces,
 * we can easily create mock implementations for testing without trying to extend
 * final classes.
 */

/**
 * Interface for note-related use cases.
 */
interface TestGetNoteUseCase {
    suspend operator fun invoke(noteId: Long): TestNote?
}

interface TestSaveNoteUseCase {
    suspend operator fun invoke(note: TestNote): Result<Unit>
}

interface TestDeleteNoteUseCase {
    suspend operator fun invoke(noteId: Long): Result<Unit>
}

interface TestGetAllNotesUseCase {
    operator fun invoke(): Flow<List<TestNote>>
}

interface TestSearchNotesUseCase {
    operator fun invoke(query: String): Flow<List<TestNote>>
}

interface TestToggleNoteStarredUseCase {
    suspend operator fun invoke(noteId: Long): Result<Unit>
}

/**
 * Interface for audio-related use cases.
 */
interface TestStartRecordingUseCase {
    suspend operator fun invoke(): Result<String> // Returns audio file path
}

interface TestStopRecordingUseCase {
    suspend operator fun invoke(): Result<Unit>
}

interface TestTranscribeAudioUseCase {
    suspend operator fun invoke(audioPath: String): Result<String>
}

interface TestPlayAudioUseCase {
    suspend operator fun invoke(audioPath: String, speed: Float = 1.0f): Result<Unit>
}

interface TestStopAudioUseCase {
    suspend operator fun invoke(): Result<Unit>
}

/**
 * Interface for validation and security services.
 */
interface TestSecurityHelper {
    fun sanitizeHtml(html: String): String
    fun validateInput(input: String): TestValidationResult
    fun validateNote(note: TestNote): TestValidationResult
}

interface TestInputValidator {
    fun validateTitle(title: String): TestValidationResult
    fun validateContent(content: String): TestValidationResult
    fun validateAudioPath(path: String): TestValidationResult
    fun validatePlaybackSpeed(speed: Float): TestValidationResult
}

/**
 * Interface for repository abstractions.
 */
interface TestNoteRepository {
    suspend fun getNoteById(id: Long): TestNote?
    suspend fun saveNote(note: TestNote): Result<Unit>
    suspend fun deleteNote(id: Long): Result<Unit>
    fun getAllNotes(): Flow<List<TestNote>>
    fun searchNotes(query: String): Flow<List<TestNote>>
    suspend fun toggleStarred(id: Long): Result<Unit>
}

interface TestAudioRepository {
    suspend fun saveAudioFile(path: String, noteId: Long): Result<String>
    suspend fun deleteAudioFile(path: String): Result<Unit>
    suspend fun getAudioDuration(path: String): Result<Long>
}

interface TestPreferencesRepository {
    suspend fun getDefaultLanguage(): String
    suspend fun setDefaultLanguage(language: String): Result<Unit>
    suspend fun getPlaybackSpeed(): Float
    suspend fun setPlaybackSpeed(speed: Float): Result<Unit>
    suspend fun getTheme(): String
    suspend fun setTheme(theme: String): Result<Unit>
}

/**
 * Interface for platform-specific services.
 */
interface TestPermissionManager {
    suspend fun requestAudioPermission(): Boolean
    suspend fun hasAudioPermission(): Boolean
    suspend fun requestStoragePermission(): Boolean
    suspend fun hasStoragePermission(): Boolean
}

interface TestFileManager {
    suspend fun createTempAudioFile(): String
    suspend fun deleteFile(path: String): Result<Unit>
    suspend fun getFileSize(path: String): Result<Long>
    suspend fun copyFile(source: String, destination: String): Result<Unit>
}

interface TestNotificationManager {
    fun showRecordingNotification()
    fun hideRecordingNotification()
    fun showTranscriptionCompleteNotification(noteTitle: String)
}

/**
 * Interface for audio processing services.
 */
interface TestAudioProcessor {
    suspend fun startRecording(outputPath: String): Result<Unit>
    suspend fun stopRecording(): Result<Unit>
    fun isRecording(): Boolean
    suspend fun transcribeAudio(audioPath: String, language: String): Result<String>
}

interface TestAudioPlayer {
    suspend fun play(audioPath: String, speed: Float): Result<Unit>
    suspend fun pause(): Result<Unit>
    suspend fun stop(): Result<Unit>
    suspend fun seekTo(positionMs: Long): Result<Unit>
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun isPlaying(): Boolean
}

/**
 * Interface for UI state management.
 */
interface TestUiStateManager {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun clearError()
    fun showSuccess(message: String)
}

/**
 * Test implementation classes that can be used as base for mocking.
 */
abstract class TestUseCaseBase<TInput, TOutput> {
    abstract suspend operator fun invoke(input: TInput): TOutput
}

abstract class TestFlowUseCaseBase<TInput, TOutput> {
    abstract operator fun invoke(input: TInput): Flow<TOutput>
}

/**
 * Result wrapper for test operations.
 */
sealed class TestResult<out T> {
    data class Success<T>(val data: T) : TestResult<T>()
    data class Error(val exception: Throwable) : TestResult<Nothing>()
    data class Loading(val message: String = "Loading...") : TestResult<Nothing>()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }
}

/**
 * Extension functions for easier result handling in tests.
 */
fun <T> TestResult<T>.onSuccess(action: (T) -> Unit): TestResult<T> {
    if (this is TestResult.Success) action(data)
    return this
}

fun <T> TestResult<T>.onError(action: (Throwable) -> Unit): TestResult<T> {
    if (this is TestResult.Error) action(exception)
    return this
}

fun <T> TestResult<T>.onLoading(action: (String) -> Unit): TestResult<T> {
    if (this is TestResult.Loading) action(message)
    return this
}