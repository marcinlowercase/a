package marcinlowercase.a.core.manager

import android.content.Context
import android.util.Patterns
import androidx.core.content.edit
import kotlinx.serialization.json.Json

class VisitedUrlManager(context: Context) {
    private val prefs = context.getSharedPreferences("BrowserHistory", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    // Dynamic key based on profile
    private fun getHistoryKey(profileId: String) = "visited_urls_map_json_$profileId"

    fun addUrl(profileId: String, url: String, title: String?) {
        if (title.isNullOrBlank() || !Patterns.WEB_URL.matcher(url).matches()) return

        val history = loadUrlMap(profileId).toMutableMap()
        history[url] = title

        prefs.edit { putString(getHistoryKey(profileId), json.encodeToString(history)) }
    }

    fun loadUrlMap(profileId: String): Map<String, String> {
        val jsonString = prefs.getString(getHistoryKey(profileId), null)
        return if (jsonString != null) {
            try { json.decodeFromString<Map<String, String>>(jsonString) } catch (_: Exception) { emptyMap() }
        } else emptyMap()
    }

    fun removeUrl(profileId: String, url: String) {
        val history = loadUrlMap(profileId).toMutableMap()
        if (history.containsKey(url)) {
            history.remove(url)
            prefs.edit { putString(getHistoryKey(profileId), json.encodeToString(history)) }
        }
    }
}