package com.module.notelycompose.testing

import io.mockk.mockk
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Test-specific Koin modules for dependency injection in tests.
 * 
 * These modules provide mock implementations of all dependencies needed
 * for testing ViewModels and other components in isolation.
 */
object TestModules {
    
    /**
     * Basic test module with all common mocked dependencies.
     * Use this as a base for most ViewModel tests.
     */
    val basicTestModule: Module = module {
        // Repository mocks
        single<TestNoteRepository> { mockk<TestNoteRepository>() }
        single<TestAudioRepository> { mockk<TestAudioRepository>() }
        single<TestPreferencesRepository> { mockk<TestPreferencesRepository>() }
        
        // Use case mocks
        factory<TestGetNoteUseCase> { mockk<TestGetNoteUseCase>() }
        factory<TestSaveNoteUseCase> { mockk<TestSaveNoteUseCase>() }
        factory<TestDeleteNoteUseCase> { mockk<TestDeleteNoteUseCase>() }
        factory<TestGetAllNotesUseCase> { mockk<TestGetAllNotesUseCase>() }
        factory<TestSearchNotesUseCase> { mockk<TestSearchNotesUseCase>() }
        factory<TestToggleNoteStarredUseCase> { mockk<TestToggleNoteStarredUseCase>() }
        
        // Audio use case mocks
        factory<TestStartRecordingUseCase> { mockk<TestStartRecordingUseCase>() }
        factory<TestStopRecordingUseCase> { mockk<TestStopRecordingUseCase>() }
        factory<TestTranscribeAudioUseCase> { mockk<TestTranscribeAudioUseCase>() }
        factory<TestPlayAudioUseCase> { mockk<TestPlayAudioUseCase>() }
        factory<TestStopAudioUseCase> { mockk<TestStopAudioUseCase>() }
        
        // Service mocks
        single<TestSecurityHelper> { mockk<TestSecurityHelper>() }
        single<TestInputValidator> { mockk<TestInputValidator>() }
        single<TestPermissionManager> { mockk<TestPermissionManager>() }
        single<TestFileManager> { mockk<TestFileManager>() }
        single<TestNotificationManager> { mockk<TestNotificationManager>() }
        single<TestAudioProcessor> { mockk<TestAudioProcessor>() }
        single<TestAudioPlayer> { mockk<TestAudioPlayer>() }
        single<TestUiStateManager> { mockk<TestUiStateManager>() }
    }
    
    /**
     * Text editor specific test module with additional mocks for text editing functionality.
     */
    val textEditorTestModule: Module = module {
        includes(basicTestModule)
        
        // Additional text editor specific mocks can go here
        // For example, if there are specific text processing services
    }
    
    /**
     * Audio recorder specific test module with additional mocks for recording functionality.
     */
    val audioRecorderTestModule: Module = module {
        includes(basicTestModule)
        
        // Additional audio recording specific mocks can go here
        // For example, if there are specific audio processing services
    }
    
    /**
     * Note list specific test module with additional mocks for list functionality.
     */
    val noteListTestModule: Module = module {
        includes(basicTestModule)
        
        // Additional note list specific mocks can go here
        // For example, if there are specific sorting or filtering services
    }
}

/**
 * Builder for creating custom test modules with specific configurations.
 * 
 * This allows tests to easily customize their dependency injection setup
 * while still using the common base module as a foundation.
 */
class TestModuleBuilder {
    private val customBindings = mutableListOf<Module>()
    
    /**
     * Add a custom module to the test configuration.
     */
    fun withModule(module: Module): TestModuleBuilder {
        customBindings.add(module)
        return this
    }
    
    /**
     * Add a single binding to the test configuration.
     */
    inline fun <reified T : Any> withMock(mock: T): TestModuleBuilder {
        val customModule = module {
            single<T> { mock }
        }
        customBindings.add(customModule)
        return this
    }
    
    /**
     * Add a factory binding to the test configuration.
     */
    inline fun <reified T : Any> withFactory(noinline factory: () -> T): TestModuleBuilder {
        val customModule = module {
            factory<T> { factory() }
        }
        customBindings.add(customModule)
        return this
    }
    
    /**
     * Build the final test module with all configurations.
     */
    fun build(baseModule: Module = TestModules.basicTestModule): Module {
        return module {
            includes(baseModule)
            customBindings.forEach { includes(it) }
        }
    }
}

/**
 * DSL function for creating custom test modules.
 * 
 * Usage:
 * ```
 * val testModule = testModule {
 *     withMock(myCustomMock)
 *     withFactory { MyCustomService() }
 * }
 * ```
 */
fun testModule(
    baseModule: Module = TestModules.basicTestModule,
    builder: TestModuleBuilder.() -> Unit
): Module {
    return TestModuleBuilder().apply(builder).build(baseModule)
}

/**
 * Pre-configured test modules for common testing scenarios.
 */
object CommonTestScenarios {
    
    /**
     * Module configured for testing offline scenarios.
     */
    val offlineTestModule: Module = testModule {
        // Configure mocks to simulate offline behavior
        // This would be implemented based on actual offline handling logic
    }
    
    /**
     * Module configured for testing error scenarios.
     */
    val errorTestModule: Module = testModule {
        // Configure mocks to simulate error conditions
        // This would be implemented based on actual error handling logic
    }
    
    /**
     * Module configured for testing loading scenarios.
     */
    val loadingTestModule: Module = testModule {
        // Configure mocks to simulate loading states
        // This would be implemented based on actual loading state logic
    }
    
    /**
     * Module configured for testing permission denied scenarios.
     */
    val noPermissionsTestModule: Module = testModule {
        // Configure permission manager to deny all permissions
    }
    
    /**
     * Module configured for testing storage full scenarios.
     */
    val storageFullTestModule: Module = testModule {
        // Configure file manager to simulate storage full conditions
    }
}