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
import java.io.ByteArrayOutputStream
import java.io.File
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

            // Decode audio to PCM
            val rawPcmData = decodeToPcm(extractor, format) ?: return@withContext null

            // Resample if needed (simplified approach - for production consider proper resampling)
            val processedPcmData = processPcmData(
                rawPcmData,
                originalSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                originalChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            )

            val outputFile = context.generateWavFile(prefix = IMPORTING_PREFIX)
            writeWavFile(
                file = outputFile,
                rawPcm = processedPcmData,
                sampleRate = targetSampleRate,
                channels = targetChannels
            )
            outputFile.absolutePath
        } catch (e: Exception) {
            Napier.e("Audio conversion failed: ${e.message}", e)
            null
        } finally {
            extractor.release()
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

    private fun decodeToPcm(extractor: MediaExtractor, format: MediaFormat): ByteArray? {
        val mime = format.getString(MediaFormat.KEY_MIME) ?: return null
        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(format, null, null, 0)
        codec.start()

        val info = MediaCodec.BufferInfo()
        val pcmData = ByteArrayOutputStream()

        try {
            var sawInputEOS = false
            var sawOutputEOS = false

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
                            // Create a temporary array to hold the data
                            val chunk = ByteArray(info.size)
                            it.position(info.offset)
                            it.limit(info.offset + info.size)
                            it.get(chunk, 0, info.size)
                            pcmData.write(chunk)
                        }
                        codec.releaseOutputBuffer(outputBufferIndex, false)

                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            sawOutputEOS = true
                        }
                    }
                    outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                        // Not needed for decoder
                    }
                    outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        // Handle format change if needed
                    }
                }
            }

            return pcmData.toByteArray()
        } finally {
            codec.stop()
            codec.release()
        }
    }
    private fun processPcmData(
        rawPcm: ByteArray,
        originalSampleRate: Int,
        originalChannels: Int
    ): ByteArray {
        // Convert byte array to short array (16-bit PCM)
        val shortSamples = ByteBuffer.wrap(rawPcm)
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
            resampleAudio(monoShorts, originalSampleRate, targetSampleRate)
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
        outputSampleRate: Int
    ): ShortArray {
        val ratio = inputSampleRate.toDouble() / outputSampleRate.toDouble()
        val outputSize = (input.size / ratio).toInt()
        val output = ShortArray(outputSize)

        for (i in 0 until outputSize) {
            val inputIndex = (i * ratio).toInt()
            output[i] = if (inputIndex < input.size) input[inputIndex] else 0
        }

        return output
    }

    private fun writeWavFile(
        file: File,
        rawPcm: ByteArray,
        sampleRate: Int,
        channels: Int
    ) {
        val wavHeader = createWavHeader(
            pcmDataLength = rawPcm.size,
            sampleRate = sampleRate,
            channels = channels
        )
        file.outputStream().use {
            it.write(wavHeader)
            it.write(rawPcm)
        }
    }

    private fun createWavHeader(
        pcmDataLength: Int,
        sampleRate: Int,
        channels: Int
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