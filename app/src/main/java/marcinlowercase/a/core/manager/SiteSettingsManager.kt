package marcinlowercase.a.core.manager

import android.content.Context
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.serialization.json.Json
import marcinlowercase.a.core.data_class.SiteSettings

class SiteSettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("BrowserSiteSettings", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private fun getSettingsKey(profileId: String) = "site_settings_map_json_$profileId"

    fun saveSettings(profileId: String, settings: Map<String, SiteSettings>) {
        val jsonString = json.encodeToString(settings)
        prefs.edit { putString(getSettingsKey(profileId), jsonString) }
    }

    fun loadSettings(profileId: String): MutableMap<String, SiteSettings> {
        val jsonString = prefs.getString(getSettingsKey(profileId), null)
        return if (jsonString != null) {
            try { json.decodeFromString(jsonString) } catch (_: Exception) { mutableMapOf() }
        } else mutableMapOf()
    }

    fun getDomain(url: String?): String? {
        return url?.toUri()?.host?.removePrefix("www.")
    }
}