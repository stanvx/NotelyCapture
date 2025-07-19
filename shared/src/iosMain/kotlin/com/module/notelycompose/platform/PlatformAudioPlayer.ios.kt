package com.module.notelycompose.platform

import com.module.notelycompose.core.debugPrintln
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.cinterop.*
import platform.AVFAudio.AVAudioSession

private const val MILLISECONDS_MULTIPLIER = 1000
private const val SECONDS_DIVISOR = 1000.0
private const val DEFAULT_POSITION = 0.0
private const val ERROR_DURATION = 0

actual class PlatformAudioPlayer actual constructor() {
    private var audioPlayer: AVAudioPlayer? = null
    private var recordingSession: AVAudioSession = AVAudioSession.sharedInstance()

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun prepare(filePath: String): Int {
        audioPlayer?.stop()

        return try {
            val fileManager = NSFileManager.defaultManager
            if (!fileManager.fileExistsAtPath(filePath)) {
                debugPrintln{"Error: File does not exist at path: $filePath"}
                return ERROR_DURATION
            }
            val url = NSURL.fileURLWithPath(filePath)
            memScoped {
                val errorPtr: ObjCObjectVar<NSError?> = alloc()
                val player = AVAudioPlayer(contentsOfURL = url, error = errorPtr.ptr)

                if (player == null) {
                    val error = errorPtr.value
                    debugPrintln{"Error creating audio player: ${error?.localizedDescription ?: "Unknown error"}"}
                    return ERROR_DURATION
                }

                audioPlayer = player
                audioPlayer?.volume = 1f
                (player.duration * MILLISECONDS_MULTIPLIER).toInt()
            }

        } catch (e: Exception) {
            debugPrintln{"Exception preparing audio player: ${e.message ?: "Unknown error"}"}
            e.printStackTrace()
            0
        }
    }

    actual fun play() {
        audioPlayer?.play()
    }

    actual fun pause() {
        audioPlayer?.pause()
    }

    actual fun stop() {
        audioPlayer?.stop()
        audioPlayer?.setCurrentTime(DEFAULT_POSITION)
    }

    actual fun release() {
        audioPlayer?.stop()
        audioPlayer = null
    }

    actual fun seekTo(position: Int) {
        // Convert position from milliseconds to seconds
        val positionInSeconds = position / SECONDS_DIVISOR
        audioPlayer?.setCurrentTime(positionInSeconds)
    }

    actual fun getCurrentPosition(): Int {
        // Return position in milliseconds
        return ((audioPlayer?.currentTime ?: DEFAULT_POSITION) * SECONDS_DIVISOR).toInt()
    }

    actual fun isPlaying(): Boolean {
        return audioPlayer?.isPlaying() ?: false
    }

    actual fun setPlaybackSpeed(speed: Float) {
        try {
            audioPlayer?.let { player ->
                if (speed < 0.5f || speed > 2.0f) {
                    debugPrintln{"Warning: iOS playback speed $speed is outside recommended range (0.5-2.0)"}
                }
                player.rate = speed
                debugPrintln{"Successfully set iOS playback speed to $speed"}
            } ?: run {
                debugPrintln{"Warning: Cannot set playback speed - audio player is null"}
            }
        } catch (e: Exception) {
            debugPrintln{"Error setting iOS playback speed to $speed: ${e.message}"}
            // Gracefully handle the error - don't crash the app
            // iOS will continue playing at current speed
        }
    }
}
