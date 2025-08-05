package com.module.notelycompose.openai.domain

import kotlinx.coroutines.flow.Flow

/**
 * Interface for monitoring network connectivity across platforms.
 */
interface NetworkConnectivityManager {
    
    /**
     * Checks if the device currently has an active network connection.
     * @return true if network is available, false otherwise
     */
    suspend fun isNetworkAvailable(): Boolean
    
    /**
     * Checks if the device has a metered network connection (e.g., mobile data).
     * @return true if connection is metered, false otherwise
     */
    suspend fun isMeteredConnection(): Boolean
    
    /**
     * Gets the current network connection type.
     * @return NetworkType enum value representing the connection type
     */
    suspend fun getNetworkType(): NetworkType
    
    /**
     * Observes network connectivity changes as a Flow.
     * @return Flow emitting true when network becomes available, false when unavailable
     */
    fun observeNetworkStatus(): Flow<Boolean>
    
    /**
     * Performs a connectivity test by attempting to reach a specific host.
     * @param host The host to test connectivity against (default: OpenAI API)
     * @return true if host is reachable, false otherwise
     */
    suspend fun testConnectivity(host: String = "api.openai.com"): Boolean
}

/**
 * Enumeration of network connection types.
 */
enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    BLUETOOTH,
    VPN,
    UNKNOWN,
    NONE
}