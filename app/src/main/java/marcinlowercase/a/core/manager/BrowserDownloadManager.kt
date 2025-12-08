package marcinlowercase.a.core.manager

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import marcinlowercase.a.core.data_class.DownloadItem

class BrowserDownloadManager(context: Context) {
    private val prefs = context.getSharedPreferences("BrowserDownloads", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val downloadsKey = "downloads_list_json"

    fun saveDownloads(downloads: List<DownloadItem>) {
        val jsonString = json.encodeToString(downloads)
        prefs.edit { putString(downloadsKey, jsonString) }
    }

    fun loadDownloads(): MutableList<DownloadItem> {
        val jsonString = prefs.getString(downloadsKey, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
    }
}
