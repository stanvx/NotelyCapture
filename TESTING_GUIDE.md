# Testing Guide for Notely Capture

## Overview

This guide provides comprehensive instructions for writing and maintaining tests in the Notely Capture project using the modern testing infrastructure established by the test refactoring strategy.

## Quick Start

### 1. Basic ViewModel Test Structure

```kotlin
class MyViewModelTest : KoinTestBase() {
    
    // Mock dependencies
    private val mockUseCase = mockk<TestMyUseCase>()
    
    // Test module configuration
    override val testModule = testModule {
        withMock(mockUseCase)
    }
    
    private lateinit var viewModel: MyViewModel
    
    override fun setupKoin() {
        super.setupKoin()
        viewModel = MyViewModel(mockUseCase)
    }
    
    @Test
    fun `when doing something, should update state correctly`() = runTest {
        // Given
        coEvery { mockUseCase(any()) } returns expectedResult
        
        // When & Then
        viewModel.uiState.test {
            val initialState = awaitItem()
            
            viewModel.doSomething()
            
            val updatedState = awaitItem()
            assertEquals(expectedValue, updatedState.someProperty)
        }
    }
}
```

### 2. Using Test Fixtures

```kotlin
@Test
fun `test with predefined data`() = runTest {
    // Use predefined test data
    val testNote = TestFixtures.standardNote
    val longContentNote = TestFixtures.longContentNote
    
    // Use parameterized test cases
    TestFixtures.inputValidationTestCases.forEach { testCase ->
        // Test logic here
    }
}
```

### 3. Custom Assertions

```kotlin
@Test
fun `test with custom matchers`() = runTest {
    // Use domain-specific assertions
    TestMatchers.assertNoteEquals(expected, actual)
    TestMatchers.assertStateLoading(uiState)
    TestMatchers.assertValidationSuccess(result)
}
```

## Testing Infrastructure Components

### Base Classes

#### ViewModelTestBase
- Provides coroutine testing setup
- Manages test dispatchers and scopes
- Use for simple tests without dependency injection

#### KoinTestBase
- Extends ViewModelTestBase with Koin DI support
- Manages Koin module lifecycle
- Use for tests that need dependency injection

### Test Data Management

#### TestDataBuilder
- Factory methods for creating test data
- Configurable properties with sensible defaults
- Use for creating custom test scenarios

```kotlin
val customNote = TestDataBuilder.createTestNote(
    title = "Custom Title",
    content = "Custom Content",
    isStarred = true
)
```

#### TestFixtures
- Predefined test data for common scenarios
- Parameterized test cases
- Use for standard testing scenarios

```kotlin
val note = TestFixtures.standardNote
val testCases = TestFixtures.inputValidationTestCases
```

### Mocking Strategy

#### Interface-Based Mocking
All use cases and services have corresponding test interfaces:

```kotlin
// Instead of extending final classes
class MyUseCase { /* final class */ }

// Use test interfaces
interface TestMyUseCase {
    suspend operator fun invoke(input: Input): Output
}

// Mock the interface
private val mockUseCase = mockk<TestMyUseCase>()
```

#### Dependency Injection

Use the test module system for clean DI setup:

```kotlin
// Basic module
override val testModule = TestModules.basicTestModule

// Custom module
override val testModule = testModule {
    withMock(myCustomMock)
    withFactory { MyService() }
}

// Specialized modules
override val testModule = TestModules.textEditorTestModule
```

## Common Testing Patterns

### 1. Flow Testing with Turbine

```kotlin
@Test
fun `test flow emissions`() = runTest {
    viewModel.uiState.test {
        // Wait for initial emission
        val initialState = awaitItem()
        
        // Trigger action
        viewModel.performAction()
        
        // Verify loading state
        val loadingState = awaitItem()
        assertTrue(loadingState.isLoading)
        
        // Verify final state
        val finalState = awaitItem()
        assertFalse(finalState.isLoading)
        assertEquals(expectedData, finalState.data)
    }
}
```

### 2. Error Handling Tests

```kotlin
@Test
fun `when operation fails, should handle error gracefully`() = runTest {
    // Given
    val expectedException = RuntimeException("Test error")
    coEvery { mockUseCase(any()) } throws expectedException
    
    // When & Then
    viewModel.uiState.test {
        awaitItem() // Initial state
        
        viewModel.performAction()
        
        awaitItem() // Loading state
        
        val errorState = awaitItem()
        TestMatchers.assertStateError(errorState, "Test error")
    }
}
```

### 3. Validation Testing

```kotlin
@Test
fun `validation scenarios`() = runTest {
    TestFixtures.inputValidationTestCases.forEach { testCase ->
        // Setup validation result
        val validationResult = TestDataBuilder.createTestValidationResult(
            isValid = testCase.expectedValid,
            errorMessage = if (!testCase.expectedValid) "Invalid input" else null
        )
        
        every { mockValidator.validate(testCase.input) } returns validationResult
        
        // Test validation behavior
        viewModel.validateInput(testCase.input)
        
        // Assert results
        if (testCase.expectedValid) {
            TestMatchers.assertValidationSuccess(validationResult)
        } else {
            TestMatchers.assertValidationFailure(validationResult)
        }
    }
}
```

### 4. Security Testing

```kotlin
@Test
fun `should sanitize malicious content`() = runTest {
    // Given
    val maliciousContent = TestDataBuilder.createHtmlContent(includeUnsafeContent = true)
    val sanitizedContent = TestDataBuilder.createHtmlContent(includeUnsafeContent = false)
    
    every { mockSecurityHelper.sanitizeHtml(maliciousContent) } returns sanitizedContent
    
    // When
    viewModel.updateContent(maliciousContent)
    
    // Then
    TestMatchers.assertHtmlSanitized(sanitizedContent, maliciousContent)
    verify { mockSecurityHelper.sanitizeHtml(maliciousContent) }
}
```

## Advanced Testing Scenarios

### 1. Platform-Specific Testing

```kotlin
// Common tests
// shared/src/commonTest/kotlin/
abstract class MyViewModelTestBase : KoinTestBase() {
    // Common test logic
}

// Android-specific tests
// shared/src/androidInstrumentedTest/kotlin/
class MyViewModelAndroidTest : MyViewModelTestBase() {
    @Test
    fun `android specific behavior`() {
        // Android-specific test logic
    }
}
```

### 2. Integration Testing

```kotlin
class IntegrationTest : KoinTestBase() {
    override val testModule = module {
        // Use real implementations for some components
        single<MyRepository> { RealMyRepository(get()) }
        
        // Mock only external dependencies
        single<NetworkService> { mockk<NetworkService>() }
    }
    
    @Test
    fun `integration test scenario`() = runTest {
        // Test with mixed real and mock dependencies
    }
}
```

### 3. Performance Testing

```kotlin
@Test
fun `should handle large data sets efficiently`() = runTest {
    // Given
    val largeDataSet = (1..10000).map { 
        TestDataBuilder.createTestNote(id = it.toLong()) 
    }
    
    coEvery { mockRepository.getAllNotes() } returns flowOf(largeDataSet)
    
    // When
    val startTime = System.currentTimeMillis()
    viewModel.loadAllNotes()
    val endTime = System.currentTimeMillis()
    
    // Then
    val processingTime = endTime - startTime
    assertTrue(processingTime < 1000, "Should process large data set in under 1 second")
}
```

## Best Practices

### 1. Test Organization

```kotlin
class MyViewModelTest : KoinTestBase() {
    
    // Group related tests with descriptive names
    
    // Happy path tests
    @Test
    fun `when loading note successfully, should update state with note data`() { }
    
    @Test
    fun `when saving valid note, should emit loading then success states`() { }
    
    // Error scenarios
    @Test
    fun `when loading non-existent note, should handle error gracefully`() { }
    
    @Test
    fun `when network fails, should show appropriate error message`() { }
    
    // Edge cases
    @Test
    fun `when note content is empty, should handle gracefully`() { }
    
    @Test
    fun `when note content is very long, should not crash`() { }
}
```

### 2. Mock Configuration

```kotlin
// Configure mocks in setUp when behavior is consistent across tests
override fun setupKoin() {
    super.setupKoin()
    
    // Default mock behaviors
    every { mockSecurityHelper.sanitizeHtml(any()) } returnsArgument 0
    every { mockValidator.validate(any()) } returns TestFixtures.validResult
    
    viewModel = MyViewModel(mockUseCase, mockSecurityHelper, mockValidator)
}

// Override in specific tests when needed
@Test
fun `specific test with different mock behavior`() = runTest {
    every { mockValidator.validate(any()) } returns TestFixtures.invalidResult
    // Test logic
}
```

### 3. Assertion Strategies

```kotlin
@Test
fun `comprehensive state verification`() = runTest {
    viewModel.uiState.test {
        val state = awaitItem()
        
        // Use specific assertions for clarity
        assertEquals("Expected Title", state.title)
        assertEquals("Expected Content", state.content)
        assertTrue(state.isStarred)
        assertFalse(state.isLoading)
        assertNull(state.error)
        
        // Or use custom matchers for domain logic
        TestMatchers.assertStateSuccess(state.toTestUiState())
    }
}
```

### 4. Test Data Management

```kotlin
class MyViewModelTest : KoinTestBase() {
    
    companion object {
        // Define test constants
        private const val TEST_NOTE_ID = 123L
        private const val EXPECTED_ERROR_MESSAGE = "Note not found"
        
        // Create shared test data
        private val testNote = TestDataBuilder.createTestNote(
            id = TEST_NOTE_ID,
            title = "Shared Test Note"
        )
    }
    
    @Test
    fun `test using shared data`() = runTest {
        coEvery { mockUseCase(TEST_NOTE_ID) } returns testNote
        // Test logic
    }
}
```

## Troubleshooting

### Common Issues and Solutions

#### 1. "Cannot extend final class" Error
**Problem**: Trying to extend a final use case class
**Solution**: Use the corresponding test interface

```kotlin
// ❌ Don't do this
class MockGetNoteUseCase : GetNoteUseCase() // Error: final class

// ✅ Do this instead
private val mockGetNoteUseCase = mockk<TestGetNoteUseCase>()
```

#### 2. "Cannot access protected method" Error
**Problem**: Trying to access protected ViewModel methods
**Solution**: Test through public interface only

```kotlin
// ❌ Don't do this
viewModel.protectedMethod() // Error: cannot access

// ✅ Do this instead
viewModel.publicMethod() // Test public behavior
// Verify internal state through observable properties
```

#### 3. Mock Configuration Issues
**Problem**: Mocks not behaving as expected
**Solution**: Use proper mockk configuration

```kotlin
// ✅ Proper mock setup
coEvery { mockUseCase(any()) } returns expectedResult
every { mockService.method() } returns value

// ✅ Verify interactions
coVerify { mockUseCase(expectedInput) }
verify(exactly = 1) { mockService.method() }
```

#### 4. Flow Testing Issues
**Problem**: Flow tests hanging or failing
**Solution**: Use Turbine properly with runTest

```kotlin
// ✅ Proper flow testing
@Test
fun `flow test`() = runTest {
    viewModel.flow.test {
        awaitItem() // Wait for emissions
        // Trigger actions
        awaitItem() // Wait for next emission
        // Don't forget to consume all emissions
    }
}
```

## Migration from Old Tests

### Step-by-Step Migration Process

1. **Update Dependencies**: Ensure build.gradle.kts has mockk and turbine
2. **Change Base Class**: Extend KoinTestBase instead of other base classes
3. **Replace Mocks**: Use interface-based mocks instead of extending final classes
4. **Update Assertions**: Use Turbine for Flow testing and custom matchers
5. **Configure DI**: Use test modules instead of manual dependency setup

### Example Migration

**Before:**
```kotlin
class OldViewModelTest {
    private lateinit var mockUseCase: MockGetNoteUseCase // Extends final class
    
    @Test
    fun test() {
        // Manual mock setup
        // Direct property access
        // Basic assertions
    }
}
```

**After:**
```kotlin
class NewViewModelTest : KoinTestBase() {
    private val mockUseCase = mockk<TestGetNoteUseCase>() // Interface-based
    
    override val testModule = testModule {
        withMock(mockUseCase)
    }
    
    @Test
    fun test() = runTest {
        // Mockk configuration
        coEvery { mockUseCase(any()) } returns result
        
        // Flow testing with Turbine
        viewModel.uiState.test {
            val state = awaitItem()
            TestMatchers.assertStateSuccess(state.toTestUiState())
        }
    }
}
```

## Conclusion

This testing infrastructure provides a robust, maintainable foundation for testing Kotlin Multiplatform applications. By following these patterns and guidelines, you can create comprehensive test suites that are easy to maintain and extend as the application grows.

For questions or issues with the testing infrastructure, refer to the TEST_REFACTORING_STRATEGY.md document or consult the example tests in the codebase.