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

import marcinlowercase.a.R
import android.content.Context
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import marcinlowercase.a.core.data_class.Profile
import java.util.UUID

class ProfileManager(private val context: Context) {
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
        val activeId = prefs.getString(activeProfileKey, null)
        if (activeId == null) {
            val profiles = loadProfiles()
            val newId = profiles.first().id
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
}