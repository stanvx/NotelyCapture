package audio.converter

import android.content.Context
import audio.utils.IMPORTED_PREFIX
import audio.utils.generateWavFile
import com.mrljdx.kmp.libs.ffmpegkit.FFmpegKit
import com.mrljdx.kmp.libs.ffmpegkit.isCancel
import com.mrljdx.kmp.libs.ffmpegkit.isSuccess
import com.mrljdx.kmp.libs.logger.ContextObtainer
import com.mrljdx.kmp.libs.logger.contextObtainer
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidAudioConverter(
    private val context: Context
): AudioConverter {

    override suspend fun convertAudioToWav(
        path: String,
    ): String? = withContext(Dispatchers.Default) {
        initKmpLogger()
        val outputPath = context.generateWavFile(prefix = IMPORTED_PREFIX).absolutePath
            ?: return@withContext null
        val ffmpegCommand = listOf(
            "-y", // Overwrite output file if it exists
            "-i", path, // Input file
            "-acodec", "pcm_s16le", // 16-bit Linear PCM
            "-ac", "1", // Mono (1 channel)
            "-ar", "16000", // 16 kHz sample rate
            outputPath // Output file
        ).joinToString(" ")

        return@withContext try {
            val session = FFmpegKit.execute(ffmpegCommand)
            val code = session.returnCode
            when {
                code?.isSuccess == true -> {
                    Napier.d("Audio converted successfully")
                    outputPath
                }

                code?.isCancel == true -> {
                    Napier.e("Audio conversion canceled")
                    null
                }

                else -> {
                    Napier.e("Audio conversion failed with code: $code")
                    null
                }
            }
        }catch (e: Exception){
            Napier.e("Audio conversion failed: ${e.message}")
            null
        }
    }

    private fun initKmpLogger() {
        contextObtainer = object : ContextObtainer {
            override fun obtainAppContext(): Context {
                return context
            }
        }
    }
}
