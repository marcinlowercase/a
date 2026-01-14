package marcinlowercase.a.core.manager

import android.content.Context
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.serialization.json.Json
import marcinlowercase.a.core.data_class.SiteSettings

class SiteSettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("BrowserSiteSettings", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val settingsKey = "site_settings_map_json"

    // We store all settings as a single Map<Domain, SiteSettings> serialized to JSON
    fun saveSettings(settings: Map<String, SiteSettings>) {
        val jsonString = json.encodeToString(settings)
        prefs.edit { putString(settingsKey, jsonString) }
    }

    fun loadSettings(): MutableMap<String, SiteSettings> {
        val jsonString = prefs.getString(settingsKey, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString(jsonString)
            } catch (_: Exception) {
                mutableMapOf()
            }
        } else {
            mutableMapOf()
        }
    }

    // Helper to extract a domain from a URL (e.g., "https://www.google.com/search" -> "google.com")
    fun getDomain(url: String?): String? {
        return url?.toUri()?.host?.removePrefix("www.")
    }
}