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
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.Profile
import java.util.UUID

class ProfileManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("BrowserProfiles", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val profilesKey = "profiles_list_json"
    private val activeProfileKey = "active_profile_id"

    fun loadProfiles(): List<Profile> {
        val jsonString = prefs.getString(profilesKey, null)
        if (jsonString != null) {
            try {
                val profiles = json.decodeFromString<List<Profile>>(jsonString).toMutableList()

                // ONE-TIME MIGRATION: If "profile_1" exists, make it globally unique!
                val legacyIndex = profiles.indexOfFirst { it.id == "profile_1" }
                if (legacyIndex != -1) {
                    val newUniqueId = "profile_${UUID.randomUUID().toString().replace("-", "")}"
                    profiles[legacyIndex] = profiles[legacyIndex].copy(id = newUniqueId)

                    // If they are currently using it, update the active pointer too
                    if (prefs.getString(activeProfileKey, null) == "profile_1") {
                        saveActiveProfileId(newUniqueId)
                    }

                    migrateLegacyData("profile_1", newUniqueId)
                    saveProfiles(profiles)
                    return profiles
                }

                return profiles
            } catch (_: Exception) {
                return createDefaultProfile()
            }
        } else {
            return createDefaultProfile()
        }
    }

    fun saveProfiles(profiles: List<Profile>) {
        prefs.edit { putString(profilesKey, json.encodeToString(profiles)) }
    }

    fun getActiveProfileId(): String {
        val activeId = prefs.getString(activeProfileKey, null)
        // If it's missing or somehow still pointing to profile_1, force a reload to trigger migration/creation
        if (activeId == "profile_1" || activeId == null) {
            val profiles = loadProfiles()
            val newId = profiles.firstOrNull()?.id ?: createDefaultProfile().first().id
            saveActiveProfileId(newId)
            return newId
        }
        return activeId
    }

    fun saveActiveProfileId(id: String) {
        prefs.edit { putString(activeProfileKey, id) }
    }

    private fun createDefaultProfile(): List<Profile> {
        val uniqueId = "profile_${UUID.randomUUID().toString().replace("-", "")}"
        val defaultProfile = Profile(
            id = uniqueId,
            name = "${context.getString(R.string.placeholder_profile)} 1"
        )
        saveProfiles(listOf(defaultProfile))
        saveActiveProfileId(uniqueId)
        return listOf(defaultProfile)
    }

    private fun migrateLegacyData(oldId: String, newId: String) {
        // 1. Migrate Browser Settings
        val oldSettings = context.getSharedPreferences("BrowserPrefs_$oldId", Context.MODE_PRIVATE)
        val newSettings = context.getSharedPreferences("BrowserPrefs_$newId", Context.MODE_PRIVATE)
        newSettings.edit().apply {
            oldSettings.all.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Int -> putInt(key, value)
                    is Float -> putFloat(key, value)
                    is Long -> putLong(key, value)
                }
            }
            apply()
        }
        oldSettings.edit().clear().apply()

        // 2. Migrate Tabs
        val tabsPrefs = context.getSharedPreferences("BrowserTabs", Context.MODE_PRIVATE)
        tabsPrefs.edit().apply {
            tabsPrefs.getString("tabs_list_json_$oldId", null)?.let { putString("tabs_list_json_$newId", it) }
            if (tabsPrefs.contains("active_tab_index_$oldId")) {
                putInt("active_tab_index_$newId", tabsPrefs.getInt("active_tab_index_$oldId", 0))
            }
            remove("tabs_list_json_$oldId")
            remove("active_tab_index_$oldId")
            apply()
        }

        // 3. Migrate Apps
        val appsPrefs = context.getSharedPreferences("BrowserApps", Context.MODE_PRIVATE)
        appsPrefs.edit().apply {
            appsPrefs.getString("installed_apps_list_$oldId", null)?.let { putString("installed_apps_list_$newId", it) }
            remove("installed_apps_list_$oldId")
            apply()
        }

        // 4. Migrate Site Settings
        val sitePrefs = context.getSharedPreferences("BrowserSiteSettings", Context.MODE_PRIVATE)
        sitePrefs.edit().apply {
            sitePrefs.getString("site_settings_map_json_$oldId", null)?.let { putString("site_settings_map_json_$newId", it) }
            remove("site_settings_map_json_$oldId")
            apply()
        }

        // 5. Migrate History
        val historyPrefs = context.getSharedPreferences("BrowserHistory", Context.MODE_PRIVATE)
        historyPrefs.edit().apply {
            historyPrefs.getString("visited_urls_map_json_$oldId", null)?.let { putString("visited_urls_map_json_$newId", it) }
            remove("visited_urls_map_json_$oldId")
            apply()
        }
    }
}

///*
// * Copyright (C) 2026 marcinlowercase
// *
// * This program is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation; version 2 of the License.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
// */
//package marcinlowercase.a.core.manager
//
//import marcinlowercase.a.R
//import android.content.Context
//import androidx.core.content.edit
//import kotlinx.serialization.json.Json
//import marcinlowercase.a.core.data_class.Profile
//import java.util.UUID
//
//class ProfileManager(private val context: Context) {
//    private val prefs = context.getSharedPreferences("BrowserProfiles", Context.MODE_PRIVATE)
//    private val json = Json { ignoreUnknownKeys = true }
//    private val profilesKey = "profiles_list_json"
//    private val activeProfileKey = "active_profile_id"
//
//    fun loadProfiles(): List<Profile> {
//        val jsonString = prefs.getString(profilesKey, null)
//        return if (jsonString != null) {
//            try { json.decodeFromString(jsonString) } catch (_: Exception) { createDefaultProfile() }
//        } else {
//            createDefaultProfile()
//        }
//    }
//
//    fun saveProfiles(profiles: List<Profile>) {
//        prefs.edit { putString(profilesKey, json.encodeToString(profiles)) }
//    }
//
//    fun getActiveProfileId(): String {
//        val activeId = prefs.getString(activeProfileKey, null)
//        if (activeId == null) {
//            val profiles = loadProfiles()
//            val newId = profiles.first().id
//            saveActiveProfileId(newId)
//            return newId
//        }
//        return activeId
//    }
//
//    fun saveActiveProfileId(id: String) {
//        prefs.edit { putString(activeProfileKey, id) }
//    }
//
//    private fun createDefaultProfile(): List<Profile> {
//        val uniqueId = "profile_${UUID.randomUUID().toString().replace("-", "")}"
//        val defaultProfile = Profile(
//            id = uniqueId,
//            name = "${context.getString(R.string.placeholder_profile)} 1"
//        )
//        saveProfiles(listOf(defaultProfile))
//        saveActiveProfileId(uniqueId)
//        return listOf(defaultProfile)
//    }
//}