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
import marcinlowercase.a.core.data_class.Profile

class ProfileManager(context: Context) {
    private val prefs = context.getSharedPreferences("BrowserProfiles", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val profilesKey = "profiles_list_json"
    private val activeProfileKey = "active_profile_id"

    fun loadProfiles(): List<Profile> {
        val jsonString = prefs.getString(profilesKey, null)
        return if (jsonString != null) {
            try { json.decodeFromString(jsonString) } catch (_: Exception) { createDefaultProfile() }
        } else {
            createDefaultProfile()
        }
    }

    fun saveProfiles(profiles: List<Profile>) {
        prefs.edit { putString(profilesKey, json.encodeToString(profiles)) }
    }

    fun getActiveProfileId(): String {
        return prefs.getString(activeProfileKey, "profile_1") ?: "profile_1"
    }

    fun saveActiveProfileId(id: String) {
        prefs.edit { putString(activeProfileKey, id) }
    }

    private fun createDefaultProfile(): List<Profile> {
        val defaultProfile = Profile(id = "profile_1", "profile 1")
        saveProfiles(listOf(defaultProfile))
        return listOf(defaultProfile)
    }
}