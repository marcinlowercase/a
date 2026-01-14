package marcinlowercase.a.core.data_class

import kotlinx.serialization.Serializable
import marcinlowercase.a.core.constant.default_url
import marcinlowercase.a.core.enum_class.TabState

@Serializable
data class Tab(
    val id: Long = System.currentTimeMillis(),
    var state: TabState = TabState.BACKGROUND,
    var currentURL: String = default_url,
    var currentTitle: String = "",
    var currentFaviconUrl: String = currentURL,
    var savedState: String? = null
) {
    companion object {
        fun createEmpty(): Tab {
            return Tab()
        }
    }
}
