package audio.di

import audio.AndroidFileManager
import audio.FileManager
import audio.recorder.AudioRecorder
import audio.utils.LauncherHolder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val audioModule: Module = module {
    single { LauncherHolder() }
    single<FileManager> { AndroidFileManager(get(), get()) }
    single { AudioRecorder(get(), get()) }
}