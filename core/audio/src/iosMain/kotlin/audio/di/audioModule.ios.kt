package audio.di

import audio.FileManager
import audio.IOSFileManager
import audio.converter.AudioConverter
import audio.converter.IOSAudioConverter
import audio.launcher.IOSAudioPickerLauncher
import audio.recorder.AudioRecorder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val audioModule: Module = module {
    single { AudioRecorder() }
    single { IOSAudioPickerLauncher() }
    single <AudioConverter>{ IOSAudioConverter() }
    single<FileManager> { IOSFileManager(get(), get()) }
}