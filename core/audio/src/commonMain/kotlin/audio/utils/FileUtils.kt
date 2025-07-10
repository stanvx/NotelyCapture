package audio.utils


internal const val RECORDING_PREFIX = "recording_"
internal const val IMPORTING_PREFIX = "importing_"
internal const val RECORDING_EXTENSION = ".wav"

expect fun deleteFile(filePath: String): Boolean
expect fun fileExists(filePath: String): Boolean
