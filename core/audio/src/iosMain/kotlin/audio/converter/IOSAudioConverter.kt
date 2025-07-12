package audio.converter

import audio.utils.generateNewAudioFile
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.*
import platform.AVFAudio.*
import platform.Foundation.*
import platform.CoreAudioTypes.*
import platform.CoreMedia.*
import platform.darwin.*
import io.github.aakira.napier.Napier
import kotlin.coroutines.*
import audio.utils.IMPORTING_PREFIX

@OptIn(ExperimentalForeignApi::class)
class IOSAudioConverter : AudioConverter {

    override suspend fun convertAudioToWav(path: String): String? = withContext(Dispatchers.IO) {
        try {
            val inputUrl = NSURL.fileURLWithPath(path)
            val asset = AVURLAsset.URLAssetWithURL(inputUrl, null)

            val outputUrl = generateNewAudioFile(IMPORTING_PREFIX) ?: return@withContext null
            val outputPath = outputUrl.path ?: return@withContext null

            val reader = AVAssetReader(asset, null)
            val writer = AVAssetWriter(outputUrl, AVFileTypeWAVE, null)

            val audioTrack =
                asset.tracksWithMediaType(AVMediaTypeAudio).firstOrNull() as? AVAssetTrack
            if (audioTrack == null) {
                Napier.e { "No audio track found in asset" }
                return@withContext null
            }

            // Reader output settings: full PCM
            val readerOutputSettings = mapOf<Any?, Any?>(
                AVFormatIDKey to kAudioFormatLinearPCM,
                AVSampleRateKey to 16000.0,
                AVNumberOfChannelsKey to 1,
                AVLinearPCMBitDepthKey to 16,
                AVLinearPCMIsBigEndianKey to false,
                AVLinearPCMIsFloatKey to false,
                AVLinearPCMIsNonInterleavedKey to false
            )

            val readerOutput = AVAssetReaderTrackOutput(audioTrack, readerOutputSettings)
            if (!reader.canAddOutput(readerOutput)) {
                Napier.e { "Reader cannot add output" }
                return@withContext null
            }
            reader.addOutput(readerOutput)

            // Writer input settings: match recorder format exactly
            val writerOutputSettings = mapOf<Any?, Any?>(
                AVFormatIDKey to kAudioFormatLinearPCM,
                AVSampleRateKey to 16000.0,
                AVNumberOfChannelsKey to 1,
                AVLinearPCMBitDepthKey to 16,
                AVLinearPCMIsBigEndianKey to false,
                AVLinearPCMIsFloatKey to false,
                AVLinearPCMIsNonInterleavedKey to false
            )

            val writerInput = AVAssetWriterInput(AVMediaTypeAudio, writerOutputSettings)
            writerInput.expectsMediaDataInRealTime = false
            if (!writer.canAddInput(writerInput)) {
                Napier.e { "Writer cannot add input. Check if output settings are supported." }
                return@withContext null
            }
            writer.addInput(writerInput)

            if (!reader.startReading()) {
                Napier.e { "Reader failed to start: ${reader.error?.localizedDescription}" }
                return@withContext null
            }

            if (!writer.startWriting()) {
                Napier.e { "Writer failed to start: ${writer.error?.localizedDescription}" }
                return@withContext null
            }

            writer.startSessionAtSourceTime(CMTimeMake(value = 0, timescale = 1))

            return@withContext suspendCancellableCoroutine { cont ->
                val queue = dispatch_queue_create("audio.writer.queue", null)
                writerInput.requestMediaDataWhenReadyOnQueue(queue) {
                    try {
                        while (writerInput.isReadyForMoreMediaData()) {
                            val sampleBuffer = readerOutput.copyNextSampleBuffer()
                            if (sampleBuffer != null) {
                                val success = writerInput.appendSampleBuffer(sampleBuffer)
                                if (!success) {
                                    val errorMsg = writer.error?.localizedDescription
                                        ?: "Unknown error appending sampleBuffer"
                                    Napier.e { "Failed to append sample buffer: $errorMsg" }
                                    cont.resumeWithException(Throwable(errorMsg))
                                    return@requestMediaDataWhenReadyOnQueue
                                }
                            } else {
                                writerInput.markAsFinished()
                                writer.finishWritingWithCompletionHandler {
                                    val status = writer.status
                                    if (status == AVAssetWriterStatusCompleted) {
                                        Napier.d { "Audio conversion successful: $outputPath" }
                                        cont.resume(outputPath)
                                    } else {
                                        val errorMsg = writer.error?.localizedDescription
                                            ?: "Unknown error during finishWriting"
                                        Napier.e { "Writer failed to finish: $errorMsg" }
                                        cont.resumeWithException(Throwable(errorMsg))
                                    }
                                }
                                break
                            }
                        }
                    } catch (e: Throwable) {
                        Napier.e("Exception in media writing loop: $e")
                        cont.resumeWithException(e)
                    }
                }

                cont.invokeOnCancellation {
                    Napier.e { "Conversion cancelled" }
                    reader.cancelReading()
                    writer.cancelWriting()
                }
            }
        } catch (e: Throwable) {
            Napier.e("Unexpected error during audio conversion: $e")
            return@withContext null
        }
    }
}
