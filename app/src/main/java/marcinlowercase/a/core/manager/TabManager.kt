package marcinlowercase.a.core.manager

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import marcinlowercase.a.core.constant.default_url
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.TabState
import kotlin.collections.forEach

class TabManager(context: Context) {
    private val prefs = context.getSharedPreferences("BrowserTabs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true } // Lenient JSON parser

    private val tabsKey = "tabs_list_json"
    private val activeTabIndexKey = "active_tab_index"

    private val settingsPrefs = context.getSharedPreferences("BrowserPrefs", Context.MODE_PRIVATE)


    fun getActiveTabIndex(): Int {
        return prefs.getInt(activeTabIndexKey, 0)
    }

    fun saveTabs(tabs: List<Tab> = emptyList(), activeTabIndex: Int) {
        // Convert the list of tabs into a single JSON string
        val jsonString = json.encodeToString(tabs)
        prefs.edit {
            putString(tabsKey, jsonString)
            putInt(activeTabIndexKey, activeTabIndex)
        }

    }

    // New function to freeze all tabs on exit
    fun freezeAllTabs() {
        Log.e("marcPip", "freezeAllTabs")

        val tabs = loadTabs(null) // Load the current state
        if (tabs.isNotEmpty()) {
            val activeIndex = prefs.getInt(activeTabIndexKey, 0)

            // Freeze all tabs
            tabs.forEach { it.state = TabState.FROZEN }

//            Log.e("RestoreSessionState", "${tabs[activeIndex]}")
            saveTabs(tabs, activeIndex)
        }
    }

    fun clearAllTabs() {
        prefs.edit {
            remove(tabsKey)
            remove(activeTabIndexKey)
            commit()
        }

    }

    fun loadTabs(intentUrl: String?): MutableList<Tab> {

        val jsonString = prefs.getString(tabsKey, null)

        return if (jsonString != null) {
            try {
                val loadedTabs = json.decodeFromString<MutableList<Tab>>(jsonString)
                if (intentUrl != null) {

                    val activeTabIndex = (getActiveTabIndex() + 1).coerceIn(0, loadedTabs.size)

                    loadedTabs.forEach { it.state = TabState.BACKGROUND }

                    loadedTabs.add( activeTabIndex,
                        Tab(
                            state = TabState.ACTIVE,
                            currentURL = intentUrl
                        )
                    )
                    prefs.edit {
                        putInt(activeTabIndexKey, activeTabIndex)
                    }

                }

                if (loadedTabs.isEmpty()) {
                    return createDefaultTabs(intentUrl)
                }
//                val activeIndex = prefs.getInt(activeTabIndexKey, 0)
//                loadedTabs.forEachIndexed { index, tab ->
////                    tab.state = if (index == activeIndex) TabState.ACTIVE else TabState.FROZEN
//                    tab.state = if (index == activeIndex) TabState.ACTIVE else TabState.FROZEN
//                }

                return loadedTabs

            } catch (_: Exception) {
                createDefaultTabs(intentUrl)
            }
        } else {
            // If no saved data, create a default tab list
            createDefaultTabs(intentUrl)
        }
    }

    private fun createDefaultTabs(intentUrl: String?): MutableList<Tab> {
        val customUrl = intentUrl
            ?: (settingsPrefs.getString("default_url", default_url) ?: default_url)

        return mutableListOf(
            Tab(
                state = TabState.ACTIVE,
                currentURL = customUrl,
            )
        )
    }
}