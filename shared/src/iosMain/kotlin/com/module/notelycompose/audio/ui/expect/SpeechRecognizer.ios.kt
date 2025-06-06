package com.module.notelycompose.audio.ui.expect

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.setActive
import platform.Foundation.NSError
import platform.Foundation.NSLocale
import platform.Foundation.NSURL
import platform.Foundation.localeIdentifier
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognitionTaskHintDictation
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus
import platform.Speech.SFSpeechURLRecognitionRequest
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.close
import platform.posix.exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

const val IOS_VERSION = 16
const val ZERO = 0

