package marcinlowercase.a.core.manager

import android.content.Context
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import marcinlowercase.a.core.constant.DefaultSettingValues
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.TabState

class TabManager(context: Context) {
    private val prefs = context.getSharedPreferences("BrowserTabs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val settingsPrefs = context.getSharedPreferences("BrowserPrefs", Context.MODE_PRIVATE)

    private fun getTabsKey(profileId: String) = "tabs_list_json_$profileId"
    private fun getActiveTabIndexKey(profileId: String) = "active_tab_index_$profileId"

    fun getActiveTabIndex(profileId: String): Int = prefs.getInt(getActiveTabIndexKey(profileId), 0)

    fun saveTabs(profileId: String, tabs: List<Tab> = emptyList(), activeTabIndex: Int) {
        prefs.edit {
            putString(getTabsKey(profileId), json.encodeToString(tabs))
            putInt(getActiveTabIndexKey(profileId), activeTabIndex)
        }
    }

    fun freezeAllTabs(profileId: String) {
        val tabs = loadTabs(profileId, null)
        if (tabs.isNotEmpty()) {
            val activeIndex = getActiveTabIndex(profileId)
            tabs.forEach { it.state = TabState.FROZEN }
            saveTabs(profileId, tabs, activeIndex)
        }
    }

    fun clearAllTabs(profileId: String) {
        prefs.edit {
            remove(getTabsKey(profileId))
            remove(getActiveTabIndexKey(profileId))
            commit()
        }
    }

    fun loadTabs(profileId: String, intentUrl: String?): MutableList<Tab> {
        val jsonString = prefs.getString(getTabsKey(profileId), null)
        return if (jsonString != null) {
            try {
                val loadedTabs = json.decodeFromString<MutableList<Tab>>(jsonString)
                if (intentUrl != null) {
                    val activeTabIndex = (getActiveTabIndex(profileId) + 1).coerceIn(0, loadedTabs.size)
                    loadedTabs.forEach { it.state = TabState.BACKGROUND }
                    loadedTabs.add(activeTabIndex, Tab(state = TabState.ACTIVE, currentURL = intentUrl, profileId = profileId))
                    prefs.edit { putInt(getActiveTabIndexKey(profileId), activeTabIndex) }
                }
                if (loadedTabs.isEmpty()) return createDefaultTabs(profileId, intentUrl)
                return loadedTabs
            } catch (_: Exception) {
                createDefaultTabs(profileId, intentUrl)
            }
        } else {
            createDefaultTabs(profileId, intentUrl)
        }
    }

    private fun createDefaultTabs(profileId: String, intentUrl: String?): MutableList<Tab> {
        val customUrl = intentUrl ?: settingsPrefs.getString("default_url", DefaultSettingValues.URL) ?: DefaultSettingValues.URL
        // CRITICAL: We pass the profileId into the new Tab so GeckoView can isolate cookies!
        return mutableListOf(Tab(state = TabState.ACTIVE, currentURL = customUrl, profileId = profileId))
    }
}