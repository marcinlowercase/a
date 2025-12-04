package marcinlowercase.a.core.data_class

import kotlinx.serialization.Serializable

@Serializable

data class App(
    val label: String,
    val iconUrl: String,
    val url: String,
)
