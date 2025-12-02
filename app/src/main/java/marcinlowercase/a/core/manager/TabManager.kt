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


    fun saveTabs(tabs: List<Tab>, activeTabIndex: Int) {
        // Convert the list of tabs into a single JSON string
        val jsonString = json.encodeToString(tabs)
        prefs.edit {
            putString(tabsKey, jsonString)
            putInt(activeTabIndexKey, activeTabIndex)
        }

        Log.d("TabManager", "Tabs saved.")
    }

    // New function to freeze all tabs on exit
    fun freezeAllTabs() {
        val tabs = loadTabs(default_url) // Load the current state
        if (tabs.isNotEmpty()) {
            val activeIndex = prefs.getInt(activeTabIndexKey, 0)

            // Freeze all tabs
            tabs.forEach { it.state = TabState.FROZEN }

            // Mark the last known active tab as ACTIVE so we can find it on next launch
            if (activeIndex in tabs.indices) {
                tabs[activeIndex].state = TabState.ACTIVE
            }

            saveTabs(tabs, activeIndex)
            Log.d("TabManager", "All tabs have been frozen.")
        }
    }

    fun clearAllTabs() {
        prefs.edit {
            remove(tabsKey)
            remove(activeTabIndexKey)
            commit()
        }

    }

    fun loadTabs(defaultUrl: String): MutableList<Tab> {
        val jsonString = prefs.getString(tabsKey, null)

        Log.i("TabManager", "Loading tabs with url: $defaultUrl")
        Log.i("TabManager", "Loading tabs with json: $jsonString")
        return if (jsonString != null) {
            try {
                val loadedTabs = json.decodeFromString<MutableList<Tab>>(jsonString)


                if (loadedTabs.isEmpty()) {
                    Log.w("TabManager", "Loaded tab list was empty. Creating default.")
                    return createDefaultTabs(defaultUrl)
                }
                val activeIndex = prefs.getInt(activeTabIndexKey, 0)
                loadedTabs.forEachIndexed { index, tab ->
                    tab.state = if (index == activeIndex) TabState.ACTIVE else TabState.FROZEN
                }

                return loadedTabs

            } catch (e: Exception) {
                Log.e("TabManager", "Failed to decode tabs, creating default.", e)
                createDefaultTabs(defaultUrl)
            }
        } else {
            // If no saved data, create a default tab list
            createDefaultTabs(defaultUrl)
        }
    }

    private fun createDefaultTabs(defaultUrl: String): MutableList<Tab> {

        Log.i("TabManager", "Creating default tabs with url: $defaultUrl")
        return mutableListOf(
            Tab(
                state = TabState.ACTIVE,
            )
        )
    }
}