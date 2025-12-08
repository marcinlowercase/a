package marcinlowercase.a.core.custom_class

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import marcinlowercase.a.core.function.isOnlineNow

class NetworkConnectivityObserver(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Expose the connectivity status as a Flow
    val networkStatus: Flow<Boolean> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                // This checks if the internet is actually valid (not just connected to a router without data)
                val isValid = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(isValid)
            }
        }

        // 1. Register the callback
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        // 2. Emit initial state immediately so the app doesn't wait for a change to know the status
        launch { send(context.isOnlineNow()) }

        // 3. Clean up when the Flow collector (the UI) stops listening
        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged() // Only emit if the value actually changes (true -> false)
}