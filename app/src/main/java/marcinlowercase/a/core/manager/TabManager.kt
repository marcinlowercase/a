package marcinlowercase.a.core.manager

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import marcinlowercase.a.core.constant.DefaultSettingValues
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.TabState
import java.io.File
import java.util.concurrent.ConcurrentHashMap

@Serializable
private data class LegacyTabStateEntry(
    val id: Long,
    val savedState: String? = null
)

class TabManager(private val context: Context) {
    companion object {
        private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val pendingStateWrites = ConcurrentHashMap<Long, String>()
    }

    private val prefs = context.getSharedPreferences("BrowserTabs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val globalPrefs = context.getSharedPreferences("BrowserPrefs", Context.MODE_PRIVATE)
    private val tabStatesDir: File by lazy {
        File(context.filesDir, "tab_states").apply {
            if (!exists()) mkdirs()
        }
    }

    private fun getTabsKey(profileId: String) = "tabs_list_json_$profileId"
    private fun getActiveTabIndexKey(profileId: String) = "active_tab_index_$profileId"
    private fun getStateFile(tabId: Long): File = File(tabStatesDir, "$tabId.state")

    fun getActiveTabIndex(profileId: String): Int = prefs.getInt(getActiveTabIndexKey(profileId), 0)

    fun saveTabStateAsync(tabId: Long, stateString: String?) {
        if (stateString.isNullOrBlank()) {
            deleteTabState(tabId)
            return
        }
        pendingStateWrites[tabId] = stateString
        ioScope.launch {
            try {
                getStateFile(tabId).writeText(stateString)
            } catch (_: Exception) {
            } finally {
                pendingStateWrites.remove(tabId, stateString)
            }
        }
    }

    fun saveTabStateSync(tabId: Long, stateString: String?) {
        if (stateString.isNullOrBlank()) {
            pendingStateWrites.remove(tabId)
            try {
                getStateFile(tabId).delete()
            } catch (_: Exception) {
            }
            return
        }
        pendingStateWrites[tabId] = stateString
        try {
            getStateFile(tabId).writeText(stateString)
        } catch (_: Exception) {
        } finally {
            pendingStateWrites.remove(tabId, stateString)
        }
    }

    fun loadTabState(tabId: Long): String? {
        pendingStateWrites[tabId]?.let { return it }
        return try {
            val file = getStateFile(tabId)
            if (file.exists()) file.readText() else null
        } catch (_: Exception) {
            null
        }
    }

    fun deleteTabState(tabId: Long) {
        pendingStateWrites.remove(tabId)
        ioScope.launch {
            try {
                getStateFile(tabId).delete()
            } catch (_: Exception) {
            }
        }
    }

    fun pruneOrphanTabStates(validTabIds: Set<Long>) {
        ioScope.launch {
            try {
                tabStatesDir.listFiles()?.forEach { file ->
                    val name = file.name
                    if (name.endsWith(".state")) {
                        val id = name.removeSuffix(".state").toLongOrNull()
                        if (id != null && !validTabIds.contains(id) && !pendingStateWrites.containsKey(id)) {
                            file.delete()
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

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
            saveTabs(profileId, tabs, activeIndex)
        }
    }

    fun clearAllTabs(profileId: String) {
        val jsonString = prefs.getString(getTabsKey(profileId), null)
        if (jsonString != null) {
            try {
                val existingTabs = json.decodeFromString<List<Tab>>(jsonString)
                existingTabs.forEach { deleteTabState(it.id) }
            } catch (_: Exception) {
            }
        }
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
                if (jsonString.contains("\"savedState\"")) {
                    // migrate legacy inline savedState strings into per-tab state files
                    try {
                        val legacyEntries = json.decodeFromString<List<LegacyTabStateEntry>>(jsonString)
                        legacyEntries.forEach { entry ->
                            if (!entry.savedState.isNullOrBlank()) {
                                saveTabStateAsync(entry.id, entry.savedState)
                            }
                        }
                    } catch (_: Exception) {
                    }
                }

                val loadedTabs = json.decodeFromString<MutableList<Tab>>(jsonString)
                if (jsonString.contains("\"savedState\"") && loadedTabs.isNotEmpty()) {
                    saveTabs(profileId, loadedTabs, getActiveTabIndex(profileId))
                }
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
        val profilePrefs = context.getSharedPreferences("BrowserPrefs_$profileId", Context.MODE_PRIVATE)

        val customUrl = intentUrl
            ?: profilePrefs.getString("default_url", null)
            ?: globalPrefs.getString("default_url", DefaultSettingValues.URL)
            ?: DefaultSettingValues.URL
        val actualUrl = customUrl.ifBlank { "about:blank" }
        Log.d("marcBlank", "load blank from create default, $actualUrl")

        return mutableListOf(Tab(state = TabState.ACTIVE, currentURL = actualUrl, profileId = profileId))
    }
}