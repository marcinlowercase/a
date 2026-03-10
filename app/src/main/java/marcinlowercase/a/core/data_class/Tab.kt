package marcinlowercase.a.core.data_class

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import marcinlowercase.a.core.constant.DefaultSettingValues
import marcinlowercase.a.core.enum_class.TabState

@Serializable
data class Tab(
    val id: Long = System.currentTimeMillis(),
    var state: TabState = TabState.BACKGROUND,
    var currentURL: String = DefaultSettingValues.URL,
    var currentTitle: String = "",
    var currentFaviconUrl: String = currentURL,
    var savedState: String? = null,
    var profileId: String,

    val faviconCache: Map<String, String> = emptyMap(),


    @Transient var canGoBack: Boolean = false,
    @Transient var canGoForward: Boolean = false,
    @Transient var errorState: ErrorState? = null

) {
    companion object {
        fun createEmpty(profileId: String,id: Long = System.currentTimeMillis()): Tab {
            return Tab(profileId = profileId, id = id)
        }
    }
}
