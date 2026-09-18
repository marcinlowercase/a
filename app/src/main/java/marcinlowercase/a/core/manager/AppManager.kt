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