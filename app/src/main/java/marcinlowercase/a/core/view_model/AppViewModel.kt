package marcinlowercase.a.core.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import marcinlowercase.a.core.custom_class.NetworkConnectivityObserver
import marcinlowercase.a.core.function.isOnlineNow

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val networkObserver = NetworkConnectivityObserver(application)

    // stateIn converts the Flow to StateFlow
    val isOnline: StateFlow<Boolean> = networkObserver.networkStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Stop updates 5s after UI disappears (saves battery)
            initialValue = application.isOnlineNow() // Initial value while loading
        )
}