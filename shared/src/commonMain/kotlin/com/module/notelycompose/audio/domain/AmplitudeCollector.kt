package com.module.notelycompose.audio.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.random.Random

/**
 * Collects and manages audio amplitude data during recording.
 * 
 * Provides normalized amplitude values and maintains a rolling history
 * for waveform visualization during audio recording.
 */
class AmplitudeCollector {
    
    private val _currentAmplitude = MutableStateFlow(0f)
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()
    
    private val _amplitudeHistory = MutableStateFlow<List<Float>>(emptyList())
    val amplitudeHistory: StateFlow<List<Float>> = _amplitudeHistory.asStateFlow()
    
    private val maxHistorySize = 100
    private var maxAmplitudeRecorded = 1f // For normalization
    
    /**
     * Updates the current amplitude value.
     * 
     * @param rawAmplitude The raw amplitude value from the audio recorder
     */
    fun updateAmplitude(rawAmplitude: Float) {
        val normalizedAmplitude = normalizeAmplitude(rawAmplitude)
        _currentAmplitude.value = normalizedAmplitude
        
        // Update history with size limit
        val currentHistory = _amplitudeHistory.value.toMutableList()
        currentHistory.add(normalizedAmplitude)
        
        if (currentHistory.size > maxHistorySize) {
            currentHistory.removeFirst()
        }
        
        _amplitudeHistory.value = currentHistory
    }
    
    /**
     * Normalizes amplitude to a 0-1 range for consistent visualization.
     * 
     * @param rawAmplitude The raw amplitude value
     * @return Normalized amplitude between 0 and 1
     */
    private fun normalizeAmplitude(rawAmplitude: Float): Float {
        val absAmplitude = abs(rawAmplitude)
        
        // Update max amplitude for better normalization
        if (absAmplitude > maxAmplitudeRecorded) {
            maxAmplitudeRecorded = absAmplitude
        }
        
        // Normalize to 0-1 range with some smoothing
        val normalized = if (maxAmplitudeRecorded > 0) {
            absAmplitude / maxAmplitudeRecorded
        } else {
            0f
        }
        
        // Apply some smoothing and ensure minimum visibility
        return (normalized * 0.9f + 0.1f).coerceIn(0f, 1f)
    }
    
    /**
     * Converts decibel values to normalized amplitude.
     * 
     * @param decibels Decibel value (typically negative)
     * @return Normalized amplitude between 0 and 1
     */
    fun fromDecibels(decibels: Float): Float {
        // Convert dB to linear scale (typical range: -60dB to 0dB)
        val clampedDb = decibels.coerceIn(-60f, 0f)
        val linear = 10f.pow(clampedDb / 20f)
        return linear.coerceIn(0f, 1f)
    }
    
    /**
     * Converts linear amplitude to normalized value.
     * 
     * @param linear Linear amplitude value
     * @param maxValue Maximum possible value for normalization
     * @return Normalized amplitude between 0 and 1
     */
    fun fromLinear(linear: Float, maxValue: Float = 32767f): Float {
        val normalized = abs(linear) / maxValue
        return normalized.coerceIn(0f, 1f)
    }
    
    /**
     * Gets a smoothed amplitude value using exponential moving average.
     * 
     * @param newAmplitude New amplitude value
     * @param alpha Smoothing factor (0-1, higher = less smoothing)
     * @return Smoothed amplitude value
     */
    fun getSmoothedAmplitude(newAmplitude: Float, alpha: Float = 0.3f): Float {
        val currentValue = _currentAmplitude.value
        return alpha * newAmplitude + (1 - alpha) * currentValue
    }
    
    /**
     * Resets the amplitude collector state.
     */
    fun reset() {
        _currentAmplitude.value = 0f
        _amplitudeHistory.value = emptyList()
        maxAmplitudeRecorded = 1f
    }
    
    /**
     * Gets the current amplitude history as a list.
     * 
     * @return List of normalized amplitude values
     */
    fun getAmplitudeHistory(): List<Float> {
        return _amplitudeHistory.value
    }
    
    /**
     * Gets the current amplitude value.
     * 
     * @return Current normalized amplitude
     */
    fun getCurrentAmplitude(): Float {
        return _currentAmplitude.value
    }
    
    /**
     * Generates demo amplitude data for testing/preview purposes.
     * 
     * @param length Number of amplitude values to generate
     * @return List of demo amplitude values
     */
    fun generateDemoAmplitudes(length: Int = 50): List<Float> {
        return (0 until length).map { i ->
            val normalizedPosition = i.toFloat() / length
            val sine = sin(normalizedPosition * PI * 8).toFloat()
            val noise = Random.nextFloat() * 0.4f - 0.2f
            (abs(sine) + noise).coerceIn(0.1f, 1f)
        }
    }
}