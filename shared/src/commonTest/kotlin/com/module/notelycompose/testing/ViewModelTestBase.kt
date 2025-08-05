package com.module.notelycompose.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Base test class for ViewModels that provides common setup for coroutine testing.
 * 
 * This class handles:
 * - Test dispatcher setup for coroutines
 * - Main dispatcher replacement for testing
 * - Test scope creation for proper coroutine testing
 */
@OptIn(ExperimentalCoroutinesApi::class)
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