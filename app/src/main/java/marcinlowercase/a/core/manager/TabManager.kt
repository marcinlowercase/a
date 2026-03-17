/*
 * Copyright (C) 2026 marcinlowercase
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package marcinlowercase.a.core.manager

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import marcinlowercase.a.core.constant.DefaultSettingValues
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.TabState

// 1. Make context a private property so we can access it inside functions
class TabManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("BrowserTabs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    // Global prefs for fallback
    private val globalPrefs = context.getSharedPreferences("BrowserPrefs", Context.MODE_PRIVATE)

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
        // 2. Fetch the specific SharedPreferences for the active profile
        val profilePrefs = context.getSharedPreferences("BrowserPrefs_$profileId", Context.MODE_PRIVATE)

        // 3. Check profile prefs first, fallback to global prefs (for older migrations), then the hardcoded default
        val customUrl = intentUrl
            ?: profilePrefs.getString("default_url", null)
            ?: globalPrefs.getString("default_url", DefaultSettingValues.URL)
            ?: DefaultSettingValues.URL
        val actualUrl = if (customUrl.isBlank()) "about:blank" else customUrl
        Log.d("marcBlank", "load blank from create default, $actualUrl")

        // CRITICAL: We pass the profileId into the new Tab so GeckoView can isolate cookies!
        return mutableListOf(Tab(state = TabState.ACTIVE, currentURL = actualUrl, profileId = profileId))
    }
}