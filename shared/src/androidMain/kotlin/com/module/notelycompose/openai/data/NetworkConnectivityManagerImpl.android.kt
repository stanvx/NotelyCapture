package com.module.notelycompose.openai.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.module.notelycompose.openai.domain.NetworkConnectivityManager
import com.module.notelycompose.openai.domain.NetworkType
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Android implementation of NetworkConnectivityManager using ConnectivityManager.
 */
class NetworkConnectivityManagerImpl(
    private val context: Context
) : NetworkConnectivityManager {

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override suspend fun isNetworkAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val network = connectivityManager.activeNetwork ?: return@withContext false
                    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@withContext false
                    
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                } else {
                    @Suppress("DEPRECATION")
                    val networkInfo = connectivityManager.activeNetworkInfo
                    networkInfo?.isConnected == true
                }
            } catch (e: Exception) {
                Napier.e("Error checking network availability", e)
                false
            }
        }
    }

    override suspend fun isMeteredConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connectivityManager.isActiveNetworkMetered
                } else {
                    @Suppress("DEPRECATION")
                    val networkInfo = connectivityManager.activeNetworkInfo
                    networkInfo?.type == ConnectivityManager.TYPE_MOBILE
                }
            } catch (e: Exception) {
                Napier.e("Error checking if connection is metered", e)
                false
            }
        }
    }

    override suspend fun getNetworkType(): NetworkType {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val network = connectivityManager.activeNetwork ?: return@withContext NetworkType.NONE
                    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@withContext NetworkType.NONE
                    
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> NetworkType.BLUETOOTH
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkType.VPN
                        else -> NetworkType.UNKNOWN
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val networkInfo = connectivityManager.activeNetworkInfo
                    when (networkInfo?.type) {
                        ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
                        ConnectivityManager.TYPE_MOBILE -> NetworkType.CELLULAR
                        ConnectivityManager.TYPE_ETHERNET -> NetworkType.ETHERNET
                        ConnectivityManager.TYPE_BLUETOOTH -> NetworkType.BLUETOOTH
                        ConnectivityManager.TYPE_VPN -> NetworkType.VPN
                        else -> if (networkInfo?.isConnected == true) NetworkType.UNKNOWN else NetworkType.NONE
                    }
                }
            } catch (e: Exception) {
                Napier.e("Error getting network type", e)
                NetworkType.UNKNOWN
            }
        }
    }

    override fun observeNetworkStatus(): Flow<Boolean> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(isConnected)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
            } else {
                val networkRequest = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            }

            // Send initial state
            trySend(isNetworkAvailable())
        } catch (e: Exception) {
            Napier.e("Error registering network callback", e)
            trySend(false)
        }

        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } catch (e: Exception) {
                Napier.e("Error unregistering network callback", e)
            }
        }
    }

    override suspend fun testConnectivity(host: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, 443), 5000)
                    true
                }
            } catch (e: Exception) {
                Napier.d("Connectivity test failed for host: $host", e)
                false
            }
        }
    }
}