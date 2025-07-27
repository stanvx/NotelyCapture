package com.module.notelycompose.audio.domain

import kotlin.math.*
import kotlin.random.Random

/**
 * Extracts waveform amplitude data from audio files for visualization.
 * 
 * Note: This is a simplified implementation that generates representative
 * waveform data. A full implementation would require parsing actual audio
 * file data using platform-specific audio processing libraries.
 */
class AudioWaveformExtractor {
    
    /**
     * Extracts amplitude data for waveform visualization.
     * 
     * @param filePath Path to the audio file
     * @param sampleCount Number of amplitude samples to extract
     * @return List of normalized amplitude values (0.0 to 1.0)
     */
    suspend fun extractAmplitudes(filePath: String, sampleCount: Int = 100): List<Float> {
        if (filePath.isEmpty()) {
            return emptyList()
        }
        
        // For now, generate representative waveform data based on file characteristics
        // In a production app, this would parse the actual audio file
        return generateRepresentativeWaveform(filePath, sampleCount)
    }
    
    /**
     * Generates a representative waveform pattern that mimics real audio data.
     * Uses the file path as a seed for consistent results per file.
     */
    private fun generateRepresentativeWaveform(filePath: String, sampleCount: Int): List<Float> {
        val seed = filePath.hashCode().toLong()
        val random = Random(seed)
        
        return (0 until sampleCount).map { i ->
            val normalizedPosition = i.toFloat() / sampleCount
            
            // Create a realistic audio waveform pattern
            val primary = sin(normalizedPosition * PI * 12).toFloat()
            val secondary = sin(normalizedPosition * PI * 24).toFloat() * 0.3f
            val noise = (random.nextFloat() - 0.5f) * 0.4f
            
            // Combine waves and add dynamic envelope
            val envelope = 1.0f - abs(normalizedPosition - 0.5f) * 1.2f
            val amplitude = (primary + secondary + noise) * envelope
            
            // Normalize to 0.1 - 1.0 range for better visualization
            (abs(amplitude) * 0.9f + 0.1f).coerceIn(0.1f, 1.0f)
        }
    }
    
    /**
     * Extracts amplitude data specifically optimized for a given duration.
     * Adjusts density based on audio length for optimal visualization.
     */
    suspend fun extractAmplitudesForDuration(
        filePath: String, 
        durationMs: Int, 
        targetSampleCount: Int = 80
    ): List<Float> {
        if (filePath.isEmpty() || durationMs <= 0) {
            return emptyList()
        }
        
        // Adjust sample count based on duration for better representation
        val adjustedSampleCount = when {
            durationMs < 10000 -> minOf(targetSampleCount / 2, 40) // Short clips - fewer samples
            durationMs > 300000 -> targetSampleCount * 2 // Long recordings - more samples
            else -> targetSampleCount
        }
        
        return extractAmplitudes(filePath, adjustedSampleCount)
    }
}
