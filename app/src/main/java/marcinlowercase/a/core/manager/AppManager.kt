package marcinlowercase.a.core.manager

import android.content.Context
import kotlinx.serialization.json.Json
import marcinlowercase.a.core.data_class.App
import androidx.core.content.edit


class AppManager(context: Context) {
    private val prefs = context.getSharedPreferences("BrowserApps", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val appsKey = "installed_apps_list"

    fun saveApps(apps: List<App>) {
        val jsonString = json.encodeToString(apps)
        prefs.edit { putString(appsKey, jsonString) }
    }

    fun loadApps(): List<App> {
        val jsonString = prefs.getString(appsKey, null) ?: return emptyList()
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}