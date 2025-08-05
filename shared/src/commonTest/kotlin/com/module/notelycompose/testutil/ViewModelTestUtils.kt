package com.module.notelycompose.testutil

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Utility functions for ViewModel testing in Kotlin Multiplatform projects.
 * These utilities address common testing challenges and provide consistent patterns.
 */

/**
 * Extension function to safely test ViewModel lifecycle without accessing protected methods.
 * This replaces the need to call onCleared() directly.
 */
fun ViewModel.testLifecycle(action: () -> Unit) {
    try {
        action()
    } finally {
        // Use reflection or custom method to trigger cleanup
        if (this is TestableViewModel) {
            this.clearViewModel()
        }
    }
}

/**
 * Interface that ViewModels can implement to support proper testing lifecycle.
 */
interface TestableViewModel {
    fun clearViewModel()
}

/**
 * Test scope factory for consistent ViewModel testing.
 */
object ViewModelTestScope {
    fun create(): TestScope {
        val dispatcher = StandardTestDispatcher()
        return TestScope(dispatcher)
    }
}

/**
 * Assertion helpers for StateFlow testing.
 */
suspend fun <T> StateFlow<T>.awaitValue(
    predicate: (T) -> Boolean,
    timeoutMs: Long = 1000L
): T {
    var currentValue = value
    val startTime = System.currentTimeMillis()
    
    while (!predicate(currentValue) && (System.currentTimeMillis() - startTime) < timeoutMs) {
        delay(10)
        currentValue = value
    }
    
    assertTrue(predicate(currentValue), "StateFlow value did not match predicate within timeout")
    return currentValue
}

/**
 * Collects the first emission from a StateFlow that matches the predicate.
 */
suspend fun <T> StateFlow<T>.firstWhere(predicate: (T) -> Boolean): T {
    return awaitValue(predicate)
}

/**
 * Asserts that a StateFlow emits a specific value within timeout.
 */
suspend fun <T> StateFlow<T>.assertEmits(
    expected: T,
    timeoutMs: Long = 1000L,
    message: String = "StateFlow did not emit expected value"
) {
    val actual = awaitValue({ it == expected }, timeoutMs)
    assertEquals(expected, actual, message)
}

/**
 * Base class for test ViewModels that implements TestableViewModel.
 */
abstract class BaseTestViewModel : ViewModel(), TestableViewModel {
    protected var isCleared = false
        private set
    
    override fun clearViewModel() {
        if (!isCleared) {
            onCleared()
            isCleared = true
        }
    }
    
    protected fun assertNotCleared() {
        if (isCleared) {
            throw IllegalStateException("ViewModel has been cleared")
        }
    }
}

/**
 * Test runner that sets up proper coroutine context for ViewModel tests.
 */
class ViewModelTestRunner(private val testScope: TestScope) {
    
    fun runTest(block: suspend TestScope.() -> Unit) {
        testScope.runTest {
            try {
                block()
            } finally {
                // Ensure all coroutines complete
                advanceUntilIdle()
            }
        }
    }
}

/**
 * Creates a test runner with proper dispatcher setup.
 */
fun createViewModelTestRunner(): ViewModelTestRunner {
    val testScope = ViewModelTestScope.create()
    Dispatchers.setMain(testScope.testScheduler)
    return ViewModelTestRunner(testScope)
}

/**
 * Cleans up test dispatchers after testing.
 */
fun cleanupViewModelTest() {
    Dispatchers.resetMain()
}