package com.module.notelycompose.testing

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Base test class that combines ViewModel testing setup with Koin dependency injection.
 * 
 * This class provides:
 * - Coroutine testing setup from ViewModelTestBase
 * - Koin module setup and teardown
 * - Access to Koin's get() function for dependency retrieval
 * 
 * Subclasses must provide a testModule that defines all dependencies needed for testing.
 */
abstract class KoinTestBase : ViewModelTestBase(), KoinTest {
    
    /**
     * Abstract property that subclasses must implement to provide their test module.
     * This module should contain all mocked dependencies needed for the test.
     */
    abstract val testModule: Module
    
    @BeforeTest
    fun setupKoin() {
        super.setupBase()
        startKoin {
            modules(testModule)
        }
    }
    
    @AfterTest
    fun tearDownKoin() {
        stopKoin()
        super.tearDownBase()
    }
}