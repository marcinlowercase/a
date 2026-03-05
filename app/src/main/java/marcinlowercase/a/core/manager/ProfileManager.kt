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
        val defaultProfile = Profile(id = "profile_1")
        saveProfiles(listOf(defaultProfile))
        return listOf(defaultProfile)
    }
}