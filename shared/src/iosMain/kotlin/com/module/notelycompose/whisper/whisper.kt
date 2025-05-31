package com.module.notelycompose.whisper

import kotlinx.cinterop.*
import platform.darwin.*
import whisper.*
import platform.CoreFoundation.*
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice
import kotlin.math.max
import kotlin.math.min
import cnames.structs.whisper_context
import platform.posix.memcpy
import platform.posix.uname

var  globalWhisperCallback:WhisperCallback? = null
sealed class WhisperError : Exception() {
    object CouldNotInitializeContext : WhisperError()
    object TranscriptionFailed : WhisperError()
    object ModelLoadFailed : WhisperError()
}

interface WhisperCallback{
    fun onProgress(progress: Int)
    fun onNewSegment(l1:Long, l2:Long, text:String)
    fun onComplete()
}


@OptIn(ExperimentalForeignApi::class)
 class WhisperContext  constructor(
    private val context: CValuesRef<whisper_context>
) {


      var stopTranscription = false



     fun release() {
        whisper_free(context)
    }

    private fun extractTextById(ctx: CPointer<whisper_context>?, nNew: Int, whisperCallback: WhisperCallback) {
        for (i in 0 until nNew) {
            val segment_id = whisper_full_n_segments(ctx) - nNew + i
            val text = whisper_full_get_segment_text(ctx, segment_id)
            whisperCallback.onNewSegment(0,0,text?.toKString()?:"")
        }

    }

    fun fullTranscribe(samples: FloatArray, tLanguage: String, callback: WhisperCallback) {
        globalWhisperCallback = callback
        stopTranscription = false
        println("full_transcribe_param")

        val params = whisper_full_default_params(whisper_sampling_strategy.WHISPER_SAMPLING_GREEDY)

        memScoped {


            val modifiedParams = params.useContents {
                cValue<whisper_full_params> {
                    memcpy(this.ptr, params.ptr, sizeOf<whisper_full_params>().toULong())

                    print_realtime = true
                    print_progress = false
                    print_timestamps = false
                    print_special = false
                    translate = false
                    language = tLanguage.cstr.ptr
                    n_threads = max(1, min(8, cpuCount() - 2)).convert()
                    offset_ms = 0
                    no_context = true
                    single_segment = false

                    // Set callbacks with user_data support
                    progress_callback = staticCFunction { ctx: CPointer<whisper_context>?,
                                                          _: CPointer<cnames.structs.whisper_state>?,
                                                          progress: Int,
                                                          user_data: COpaquePointer? ->
                        globalWhisperCallback?.onProgress(progress)

                    }

                    new_segment_callback = staticCFunction { ctx: CPointer<whisper_context>?,
                                                             _: CPointer<cnames.structs.whisper_state>?,
                                                             n_new: Int,
                                                             user_data: COpaquePointer? ->
                        for (i in 0 until n_new) {
                            val segment_id = whisper_full_n_segments(ctx) - n_new + i
                            val text = whisper_full_get_segment_text(ctx, segment_id)
                            globalWhisperCallback?.onNewSegment(0, 0, text?.toKString() ?: "")
                        }

                    }

                    abort_callback = staticCFunction { _: COpaquePointer? ->
                        false
                    }
                }
            }

            println("full_transcribe")

            if (whisper_full(
                    context,
                    modifiedParams,
                    samples.toCValues().ptr,
                    samples.size
                ) != 0
            ) {
                throw WhisperError.TranscriptionFailed
            }else{
                callback.onComplete()
            }
        }
        }

     fun getTranscription(): String = buildString {
        for (i in 0 until whisper_full_n_segments(context).convert<Int>()) {
            append(whisper_full_get_segment_text(context, i.convert())?.toKString() ?: "")
        }
    }


     companion object {
         fun createContext(path: String): WhisperContext = memScoped {
             // 1. Get default params as CValue
             val params = whisper_context_default_params()

             // 2. Create a modified copy using useContents
             val modifiedParams = params.useContents {
                 cValue<whisper_context_params> {
                     // Copy all fields from original
                     memcpy(this.ptr, params.ptr, sizeOf<whisper_context_params>().toULong())
                     // Apply your modifications
                   //  if(isRunningOnSimulator())
                    use_gpu = false
                    // else
                      //   flash_attn = true
                 }
             }

             // 3. Pass the modified CValue
             whisper_init_from_file_with_params(path, modifiedParams)?.let {
                 WhisperContext(it)
             } ?: throw WhisperError.CouldNotInitializeContext
         }

         private fun isRunningOnSimulator(): Boolean {
                 memScoped {
                     val utsname = alloc<platform.posix.utsname>()
                     if (uname(utsname.ptr) == 0) {
                         val machine = utsname.machine.toKString()
                         return machine == "x86_64" || machine == "i386"
                     }
                 }
                 return false

         }
     }



    private fun cpuCount(): Int = NSProcessInfo.processInfo.processorCount.toInt()



}