package marcinlowercase.a.core.manager

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.core.content.edit
import kotlinx.serialization.json.Json

class VisitedUrlManager(context: Context) {
    private val prefs = context.getSharedPreferences("BrowserHistory", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val historyKey = "visited_urls_map_json" // Renamed key

    // This now accepts both a URL and a title
    fun addUrl(url: String, title: String?) {
        // Don't save empty titles or invalid URLs
        if (title.isNullOrBlank() || !Patterns.WEB_URL.matcher(url).matches()) return

        val history = loadUrlMap().toMutableMap()
        history[url] = title

        // Serialize the whole map to a JSON string for storage
        val jsonString = json.encodeToString(history)
        prefs.edit {
            putString(historyKey, jsonString)
        }
    }

    // This now returns a Map<String, String>
    fun loadUrlMap(): Map<String, String> {
        val jsonString = prefs.getString(historyKey, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString<Map<String, String>>(jsonString)
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }
    }

    fun removeUrl(url: String) {
        val history = loadUrlMap().toMutableMap()
        if (history.containsKey(url)) {
            history.remove(url)
            val jsonString = json.encodeToString(history)
            prefs.edit {
                putString(historyKey, jsonString)
            }
        }
    }
}