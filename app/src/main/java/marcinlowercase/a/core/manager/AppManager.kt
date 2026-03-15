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
import marcinlowercase.a.core.data_class.App

class AppManager(context: Context) {
    private val prefs = context.getSharedPreferences("BrowserApps", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    // Private helper to generate a unique key for each profile
    private fun getAppsKey(profileId: String) = "installed_apps_list_$profileId"

    fun saveApps(profileId: String, apps: List<App>) {
        val jsonString = json.encodeToString(apps)
        // Uses the dynamic key (e.g., "installed_apps_list_profile_1")
        prefs.edit { putString(getAppsKey(profileId), jsonString) }
    }


    fun loadApps(profileId: String): List<App> {
        // Reads from the dynamic key
        val jsonString = prefs.getString(getAppsKey(profileId), null) ?: return emptyList()
        return try {
            json.decodeFromString(jsonString)
        } catch (_: Exception) {
            emptyList()
        }
    }
}