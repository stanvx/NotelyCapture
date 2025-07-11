package audio.converter

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import audio.utils.IMPORTING_PREFIX
import audio.utils.generateWavFile
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class AndroidAudioConverter(
    private val context: Context
) : AudioConverter {
    // Target format
    private val targetSampleRate = 16000
    private val targetChannels = 1
    private val targetBitDepth = 16

    override suspend fun convertAudioToWav(path: String) = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(path)

            // Find audio track
            val audioTrackIndex = findAudioTrack(extractor)
            if (audioTrackIndex == -1) return@withContext null

            extractor.selectTrack(audioTrackIndex)
            val format = extractor.getTrackFormat(audioTrackIndex)

            // Create output WAV file
            val outputFile = context.generateWavFile(prefix = IMPORTING_PREFIX)

            // Process audio in chunks
            val success = processAudioInChunks(
                extractor = extractor,
                format = format,
                outputFile = outputFile
            )

            if (success) outputFile.absolutePath else null
        } catch (e: Exception) {
            Napier.e("Audio conversion failed: ${e.message}", e)
            null
        } finally {
            extractor.release()
        }
    }

    private fun processAudioInChunks(
        extractor: MediaExtractor,
        format: MediaFormat,
        outputFile: File
    ): Boolean {
        val mime = format.getString(MediaFormat.KEY_MIME) ?: return false
        val originalSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val originalChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(format, null, null, 0)
        codec.start()

        val info = MediaCodec.BufferInfo()
        var sawInputEOS = false
        var sawOutputEOS = false
        var totalBytesWritten = 0

        return try {
            // Write initial WAV header (will update size later)
            val header = createWavHeader(0)
            RandomAccessFile(outputFile, "rw").use { raf ->
                raf.write(header)

                // Process audio chunks
                while (!sawOutputEOS) {
                    if (!sawInputEOS) {
                        val inputBufferIndex = codec.dequeueInputBuffer(10000)
                        if (inputBufferIndex >= 0) {
                            val inputBuffer = codec.getInputBuffer(inputBufferIndex) ?: continue
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)

                            if (sampleSize < 0) {
                                sawInputEOS = true
                                codec.queueInputBuffer(
                                    inputBufferIndex,
                                    0,
                                    0,
                                    0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                )
                            } else {
                                codec.queueInputBuffer(
                                    inputBufferIndex,
                                    0,
                                    sampleSize,
                                    extractor.sampleTime,
                                    0
                                )
                                extractor.advance()
                            }
                        }
                    }

                    val outputBufferIndex = codec.dequeueOutputBuffer(info, 10000)
                    when {
                        outputBufferIndex >= 0 -> {
                            val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                            outputBuffer?.let {
                                val chunk = ByteArray(info.size)
                                it.position(info.offset)
                                it.limit(info.offset + info.size)
                                it.get(chunk, 0, info.size)

                                // Process and write the chunk
                                val processedChunk = processPcmChunk(
                                    chunk,
                                    originalSampleRate,
                                    originalChannels
                                )
                                raf.write(processedChunk)
                                totalBytesWritten += processedChunk.size
                            }
                            codec.releaseOutputBuffer(outputBufferIndex, false)

                            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                sawOutputEOS = true
                            }
                        }
                        outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {}
                        outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {}
                    }
                }

                // Update WAV header with actual file size
                raf.seek(0)
                raf.write(createWavHeader(totalBytesWritten))
            }
            true
        } catch (e: Exception) {
            Napier.e("Error processing audio chunks: ${e.message}", e)
            false
        } finally {
            codec.stop()
            codec.release()
        }
    }

    private fun findAudioTrack(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                return i
            }
        }
        return -1
    }

    private fun processPcmChunk(
        chunk: ByteArray,
        originalSampleRate: Int,
        originalChannels: Int
    ): ByteArray {
        // Convert byte array to short array (16-bit PCM)
        val shortSamples = ByteBuffer.wrap(chunk)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()
            .let { buffer ->
                val shorts = ShortArray(buffer.remaining())
                buffer.get(shorts)
                shorts
            }

        // Convert stereo to mono if needed
        val monoShorts = if (originalChannels == 2) {
            convertStereoToMono(shortSamples)
        } else {
            shortSamples
        }

        // Resample if needed
        val resampledShorts = if (originalSampleRate != targetSampleRate) {
            resampleAudio(monoShorts, originalSampleRate)
        } else {
            monoShorts
        }

        // Convert back to byte array
        return ByteBuffer.allocate(resampledShorts.size * 2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply {
                resampledShorts.forEach { putShort(it) }
            }
            .array()
    }

    private fun convertStereoToMono(stereoSamples: ShortArray): ShortArray {
        val monoSize = stereoSamples.size / 2
        val mono = ShortArray(monoSize)

        for (i in 0 until monoSize) {
            // Simple average of left and right channels
            val left = stereoSamples[i * 2].toInt()
            val right = stereoSamples[i * 2 + 1].toInt()
            mono[i] = ((left + right) / 2).toShort()
        }

        return mono
    }

    private fun resampleAudio(
        input: ShortArray,
        inputSampleRate: Int,
        outputSampleRate: Int = targetSampleRate
    ): ShortArray {
        if (inputSampleRate == outputSampleRate) return input

        val ratio = inputSampleRate.toDouble() / outputSampleRate.toDouble()
        val outputSize = (input.size / ratio).toInt()
        val output = ShortArray(outputSize)

        for (i in 0 until outputSize) {
            val inputIndex = (i * ratio).toInt()
            output[i] = if (inputIndex < input.size) input[inputIndex] else 0
        }

        return output
    }

    private fun createWavHeader(
        pcmDataLength: Int,
        sampleRate: Int = targetSampleRate,
        channels: Int = targetChannels
    ): ByteArray {
        val bitsPerSample = targetBitDepth
        val byteRate = sampleRate * channels * (bitsPerSample / 8)
        val blockAlign = channels * (bitsPerSample / 8)
        val totalDataLen = pcmDataLength + 36

        return ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt(totalDataLen)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16) // Subchunk1Size
            putShort(1.toShort()) // PCM
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign.toShort())
            putShort(bitsPerSample.toShort())
            put("data".toByteArray())
            putInt(pcmDataLength)
        }.array()
    }
}