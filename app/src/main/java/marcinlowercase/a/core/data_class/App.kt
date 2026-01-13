package studio.oo1.browser.core.data_class

import kotlinx.serialization.Serializable

@Serializable

data class App(
    val id: Long,
    val label: String,
    val iconUrl: String,
    val url: String,
)
