# Test Refactoring Strategy for TextEditorViewModelTest.kt

## Executive Summary

This document outlines a comprehensive strategy to refactor the failing `TextEditorViewModelTest.kt` file and establish modern, maintainable testing patterns for the Notely Capture Kotlin Multiplatform project.

## Current Architecture Analysis

### Project Structure
- **Architecture**: Clean Architecture with MVVM presentation layer
- **DI Framework**: Koin 4.1.0 with compose-viewmodel integration
- **Platform**: Kotlin Multiplatform (KMP 2.2.0) with commonTest, androidInstrumentedTest, iosTest
- **Testing Dependencies**: kotlin("test"), kotlinx-coroutines-test, koin-test

### Identified Problems

1. **Final Class Extension**: Attempting to extend final use case classes
2. **Protected Method Access**: Trying to access protected ViewModel methods
3. **Mock Implementation Issues**: Overriding non-existent methods in SecurityHelper
4. **Platform-Specific Mock Issues**: Incorrect mocking of platform-specific classes
5. **Missing Test Dependencies**: No modern mocking framework (mockk) included

## Refactoring Strategy

### Phase 1: Dependencies and Infrastructure

#### 1.1 Add Missing Test Dependencies

Add to `shared/build.gradle.kts`:

```kotlin
val commonTest by getting {
    dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        implementation(libs.koin.test)
        implementation(libs.datastore.preferences)
        
        // Add modern testing dependencies
        implementation("io.mockk:mockk:1.13.8")
        implementation("app.cash.turbine:turbine:1.0.0") // For Flow testing
    }
}

// For Android-specific tests
val androidInstrumentedTest by getting {
    dependencies {
        implementation("io.mockk:mockk-android:1.13.8")
        implementation("androidx.test.ext:junit:1.1.5")
        implementation("androidx.test.espresso:espresso-core:3.5.1")
    }
}
```

#### 1.2 Interface-Based Architecture

Create test-friendly interfaces for all use cases and major dependencies:

```kotlin
// Domain layer interfaces
interface GetNoteUseCase {
    suspend operator fun invoke(noteId: Long): Note?
}

interface SaveNoteUseCase {
    suspend operator fun invoke(note: Note): Result<Unit>
}

interface SecurityHelper {
    fun sanitizeHtml(html: String): String
    fun validateInput(input: String): ValidationResult
}

// Repository interfaces (should already exist)
interface NoteRepository {
    suspend fun getNoteById(id: Long): Note?
    suspend fun saveNote(note: Note): Result<Unit>
    fun getAllNotes(): Flow<List<Note>>
}
```

### Phase 2: Test Architecture Design

#### 2.1 Test Base Classes

Create base test classes for consistent setup:

```kotlin
// Base test class for ViewModels
abstract class ViewModelTestBase {
    protected lateinit var testScope: TestScope
    protected lateinit var testDispatcher: UnconfinedTestDispatcher
    
    @BeforeTest
    fun setupBase() {
        testDispatcher = UnconfinedTestDispatcher()
        testScope = TestScope(testDispatcher)
        Dispatchers.setMain(testDispatcher)
    }
    
    @AfterTest
    fun tearDownBase() {
        Dispatchers.resetMain()
    }
}

// Base test class with Koin DI
abstract class KoinTestBase : ViewModelTestBase() {
    
    @BeforeTest
    fun setupKoin() {
        startKoin {
            modules(testModule)
        }
    }
    
    @AfterTest
    fun tearDownKoin() {
        stopKoin()
    }
    
    abstract val testModule: Module
}
```

#### 2.2 Test-Specific Koin Configuration

```kotlin
// Test module with mocked dependencies
val testModule = module {
    
    // Mock repositories
    single<NoteRepository> { mockk<NoteRepository>() }
    single<PreferencesRepository> { mockk<PreferencesRepository>() }
    
    // Mock use cases with interfaces
    factory<GetNoteUseCase> { mockk<GetNoteUseCase>() }
    factory<SaveNoteUseCase> { mockk<SaveNoteUseCase>() }
    factory<SecurityHelper> { mockk<SecurityHelper>() }
    
    // Real ViewModels (what we're testing)
    viewModel { TextEditorViewModel(get(), get(), get(), get()) }
}
```

### Phase 3: Modern Testing Patterns

#### 3.1 Test Structure Template

```kotlin
class TextEditorViewModelTest : KoinTestBase() {
    
    override val testModule = module {
        single<NoteRepository> { mockNoteRepository }
        factory<GetNoteUseCase> { mockGetNoteUseCase }
        factory<SaveNoteUseCase> { mockSaveNoteUseCase }
        factory<SecurityHelper> { mockSecurityHelper }
        viewModel { TextEditorViewModel(get(), get(), get(), get()) }
    }
    
    // Mocks as properties for easy access
    private val mockNoteRepository = mockk<NoteRepository>()
    private val mockGetNoteUseCase = mockk<GetNoteUseCase>()
    private val mockSaveNoteUseCase = mockk<SaveNoteUseCase>()
    private val mockSecurityHelper = mockk<SecurityHelper>()
    
    private lateinit var viewModel: TextEditorViewModel
    
    @BeforeTest
    fun setUp() {
        super.setupBase()
        super.setupKoin()
        viewModel = get()
    }
    
    @Test
    fun `when loading note, should update state correctly`() = runTest {
        // Given
        val noteId = 1L
        val expectedNote = createTestNote(id = noteId, title = "Test Note")
        coEvery { mockGetNoteUseCase(noteId) } returns expectedNote
        
        // When
        viewModel.loadNote(noteId)
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(expectedNote.title, state.title)
            assertEquals(expectedNote.content, state.content)
            assertFalse(state.isLoading)
        }
        
        coVerify { mockGetNoteUseCase(noteId) }
    }
}
```

#### 3.2 Flow Testing with Turbine

```kotlin
@Test
fun `when saving note, should emit loading then success states`() = runTest {
    // Given
    val note = createTestNote(title = "New Title", content = "New Content")
    coEvery { mockSaveNoteUseCase(any()) } returns Result.success(Unit)
    
    // When & Then
    viewModel.uiState.test {
        // Initial state
        val initialState = awaitItem()
        assertFalse(initialState.isLoading)
        
        // Trigger save
        viewModel.saveNote()
        
        // Loading state
        val loadingState = awaitItem()
        assertTrue(loadingState.isLoading)
        
        // Success state
        val successState = awaitItem()
        assertFalse(successState.isLoading)
        assertNull(successState.error)
    }
}
```

### Phase 4: Test Data Management

#### 4.1 Test Data Builders

```kotlin
object TestDataBuilder {
    fun createTestNote(
        id: Long = 1L,
        title: String = "Test Note",
        content: String = "Test Content",
        dateCreated: Long = System.currentTimeMillis(),
        dateModified: Long = System.currentTimeMillis(),
        isStarred: Boolean = false
    ) = Note(
        id = id,
        title = title,
        content = content,
        dateCreated = dateCreated,
        dateModified = dateModified,
        isStarred = isStarred
    )
    
    fun createTestValidationResult(
        isValid: Boolean = true,
        errorMessage: String? = null
    ) = ValidationResult(isValid, errorMessage)
}
```

#### 4.2 Custom Test Matchers

```kotlin
object TestMatchers {
    fun assertNoteEquals(expected: Note, actual: Note) {
        assertEquals(expected.id, actual.id)
        assertEquals(expected.title, actual.title)
        assertEquals(expected.content, actual.content)
        assertEquals(expected.isStarred, actual.isStarred)
    }
    
    fun assertStateLoading(state: TextEditorUiState) {
        assertTrue(state.isLoading)
        assertNull(state.error)
    }
    
    fun assertStateError(state: TextEditorUiState, expectedError: String) {
        assertFalse(state.isLoading)
        assertEquals(expectedError, state.error)
    }
}
```

### Phase 5: Platform-Specific Testing

#### 5.1 Common Test Strategy

```kotlin
// shared/src/commonTest/kotlin/
abstract class TextEditorViewModelTestBase : KoinTestBase() {
    // Common test logic that works across all platforms
    
    @Test
    fun `common business logic tests`() {
        // Tests that don't depend on platform-specific implementations
    }
}
```

#### 5.2 Android-Specific Tests

```kotlin
// shared/src/androidInstrumentedTest/kotlin/
class TextEditorViewModelAndroidTest : TextEditorViewModelTestBase() {
    
    @Test
    fun `android specific security helper behavior`() {
        // Test Android-specific implementations
        val mockContext = mockk<Context>()
        // Android-specific test logic
    }
}
```

### Phase 6: Advanced Testing Patterns

#### 6.1 Parameterized Tests

```kotlin
class TextEditorValidationTest {
    
    @Test
    fun `validate input with various scenarios`() = parameterizedTest(
        TestCase("Valid input", "Hello World", true),
        TestCase("Empty input", "", false),
        TestCase("HTML injection", "<script>alert('xss')</script>", false),
        TestCase("Long content", "a".repeat(10000), true)
    ) { testCase ->
        // Given
        every { mockSecurityHelper.validateInput(testCase.input) } returns 
            ValidationResult(testCase.expectedValid, if (!testCase.expectedValid) "Invalid" else null)
        
        // When
        val result = viewModel.validateInput(testCase.input)
        
        // Then
        assertEquals(testCase.expectedValid, result.isValid)
    }
    
    data class TestCase(val name: String, val input: String, val expectedValid: Boolean)
}
```

#### 6.2 Test Fixtures and Factories

```kotlin
object TextEditorTestFixtures {
    val standardNote = createTestNote(
        id = 1L,
        title = "Standard Note",
        content = "This is a standard test note with regular content."
    )
    
    val emptyNote = createTestNote(
        title = "",
        content = ""
    )
    
    val richTextNote = createTestNote(
        title = "Rich Text Note",
        content = "<h1>Heading</h1><p><strong>Bold text</strong> and <em>italic text</em></p>"
    )
    
    val longContentNote = createTestNote(
        title = "Long Content Note",
        content = generateLongContent(5000)
    )
    
    private fun generateLongContent(length: Int): String {
        return buildString {
            repeat(length / 50) {
                append("This is a long content note for testing purposes. ")
            }
        }
    }
}
```

## Implementation Plan

### Step 1: Infrastructure Setup (High Priority)
1. Add mockk and turbine dependencies to build.gradle.kts
2. Create base test classes (ViewModelTestBase, KoinTestBase)
3. Set up test-specific Koin module configuration

### Step 2: Interface Creation (High Priority)
1. Create interfaces for all use cases currently being extended as final classes
2. Update existing implementations to implement these interfaces
3. Update Koin modules to use interface bindings

### Step 3: Test Refactoring (High Priority)
1. Refactor TextEditorViewModelTest to use new architecture
2. Replace manual mocks with mockk-based mocks
3. Implement proper Flow testing with Turbine
4. Add comprehensive test scenarios

### Step 4: Test Data & Utilities (Medium Priority)
1. Create TestDataBuilder and test fixtures
2. Implement custom matchers and assertions
3. Add parameterized test support

### Step 5: Advanced Patterns (Medium Priority)
1. Implement platform-specific test strategies
2. Add integration test patterns
3. Create comprehensive test documentation

## Benefits of This Approach

### 1. Maintainability
- **Interface-based mocking**: Easy to mock and test
- **Consistent patterns**: Standardized test structure across the project
- **Separation of concerns**: Clear separation between test setup, execution, and assertions

### 2. Scalability
- **Reusable components**: Base classes and utilities can be used across all tests
- **Platform support**: Proper support for KMP testing across Android, iOS, and common
- **Easy extension**: New tests can be added following established patterns

### 3. Modern Best Practices
- **Mockk integration**: Modern, idiomatic Kotlin mocking
- **Flow testing**: Proper reactive testing with Turbine
- **Coroutine testing**: Proper async testing with TestScope and TestDispatchers

### 4. Quality Assurance
- **Type safety**: Interface-based approach ensures compile-time safety
- **Test isolation**: Each test runs in isolation with fresh mocks
- **Comprehensive coverage**: Covers success, error, and edge cases

## Migration Strategy

### Phase 1: Quick Fixes (1-2 days)
- Add dependencies and create basic interfaces
- Get existing tests passing with minimal changes

### Phase 2: Architecture Improvement (3-5 days)
- Implement comprehensive interface-based architecture
- Refactor all existing tests to use new patterns

### Phase 3: Enhanced Testing (1-2 weeks)
- Add comprehensive test coverage
- Implement advanced testing patterns and utilities

## Conclusion

This refactoring strategy addresses all identified issues while establishing a robust, maintainable testing architecture that follows modern Kotlin Multiplatform best practices. The approach ensures long-term scalability and maintainability while providing immediate solutions to current test failures.