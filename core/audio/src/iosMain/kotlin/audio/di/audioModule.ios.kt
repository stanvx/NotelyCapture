package audio.di

import audio.FileManager
import audio.IOSFileManager
import audio.launcher.AudioPickerLauncher
import audio.launcher.IOSAudioPickerLauncher
import audio.recorder.AudioRecorder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val audioModule: Module = module {
    single { AudioRecorder() }
    single<AudioPickerLauncher> { IOSAudioPickerLauncher() }
    single<FileManager> { IOSFileManager(get()) }
}