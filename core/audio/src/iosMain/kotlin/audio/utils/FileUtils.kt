package audio.utils

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlin.random.Random
import io.github.aakira.napier.Napier
import kotlinx.cinterop.free

private const val RECORDING_PREFIX = "recording_"
private const val RECORDING_EXTENSION = ".wav"

internal fun generateNewAudioFile(): NSURL? {
    val randomNumber = Random.nextInt(100000, 999999)
    val fileName = "$RECORDING_PREFIX${randomNumber}$RECORDING_EXTENSION"
    val documentsDirectory = NSFileManager.defaultManager.URLsForDirectory(
        NSDocumentDirectory,
        NSUserDomainMask
    ).first() as NSURL

    return documentsDirectory.URLByAppendingPathComponent(fileName)
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun NSURL.savePickedAudioToAppStorage(): NSURL?{
    val file = generateNewAudioFile() ?: return null
    val fileManager = NSFileManager.defaultManager

    val errorPtr = nativeHeap.alloc<ObjCObjectVar<NSError?>>()
    errorPtr.value = null

    val success = fileManager.copyItemAtURL(this, file, errorPtr.ptr)

    if(!success){
        Napier.e { "File copy failed: ${errorPtr.value?.localizedDescription}" }
        nativeHeap.free(errorPtr)
        return null
    }
    nativeHeap.free(errorPtr)
    return file
}