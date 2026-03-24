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
package marcinlowercase.a.ui.viewmodel

import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marcinlowercase.a.CustomApplication
import marcinlowercase.a.MainActivity
import marcinlowercase.a.core.constant.DefaultSettingValues
import marcinlowercase.a.core.constant.generic_location_permission
import marcinlowercase.a.core.data_class.App
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.BrowserUIState
import marcinlowercase.a.core.data_class.ConfirmationDialogState
import marcinlowercase.a.core.data_class.ContextMenuData
import marcinlowercase.a.core.data_class.CustomPermissionRequest
import marcinlowercase.a.core.data_class.DownloadItem
import marcinlowercase.a.core.data_class.DownloadParams
import marcinlowercase.a.core.data_class.JsChoiceState
import marcinlowercase.a.core.data_class.JsColorState
import marcinlowercase.a.core.data_class.JsDateTimeState
import marcinlowercase.a.core.data_class.JsDialogState
import marcinlowercase.a.core.data_class.PollData
import marcinlowercase.a.core.data_class.Profile
import marcinlowercase.a.core.data_class.SiteSettings
import marcinlowercase.a.core.data_class.Suggestion
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.BrowserOption
import marcinlowercase.a.core.enum_class.BrowserSettingField
import marcinlowercase.a.core.enum_class.DownloadStatus
import marcinlowercase.a.core.enum_class.GestureNavAction
import marcinlowercase.a.core.enum_class.SearchEngine
import marcinlowercase.a.core.enum_class.SuggestionSource
import marcinlowercase.a.core.enum_class.TabState
import marcinlowercase.a.core.manager.AppManager
import marcinlowercase.a.core.manager.BrowserDownloadManager
import marcinlowercase.a.core.manager.ProfileManager
import marcinlowercase.a.core.manager.SiteSettingsManager
import marcinlowercase.a.core.manager.TabManager
import marcinlowercase.a.core.manager.VisitedUrlManager
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Collections
import java.util.regex.Pattern
import androidx.core.graphics.scale
import marcinlowercase.a.R
import androidx.core.graphics.createBitmap

val LocalBrowserViewModel = staticCompositionLocalOf<BrowserViewModel> {
    error("No BrowserViewModel provided! Check your root Composable.")
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {
    //region Manager
    val tabManager = TabManager(application)
    val geckoManager = (application as CustomApplication).geckoManager
    val appManager = AppManager(application)
    val downloadTracker = BrowserDownloadManager(application)
    val siteSettingsManager = SiteSettingsManager(application)
    val visitedUrlManager = VisitedUrlManager(application)

    val profileManager = ProfileManager(application)

    //endregion


    //region PWA Logic

    val isStandaloneMode = mutableStateOf(false)
    val pwaTab = mutableStateOf<Tab?>(null)
    val pwaProfileId = mutableStateOf("")


    fun initPwa(url: String, targetProfileId: String) {
        // 1. Set the isolated PWA Profile ID
        pwaProfileId.value = targetProfileId
        setupPrefsListener(targetProfileId)

        // 2. Load settings for this profile into RAM (Padding, Corner Radius, AdBlock, etc.)
        _browserSettings.value = loadSettingsFromPrefs(targetProfileId)
        geckoManager.setAdBlockEnabled(_browserSettings.value.isAdBlockEnabled)

        // 3. Load the necessary memory lists (Site Permissions)
        siteSettings.clear()
        siteSettings.putAll(siteSettingsManager.loadSettings(targetProfileId))

        // 4. Launch the PWA Tab
        val currentPwa = pwaTab.value

        // Prevent reloading if we already spun up this PWA
        if (currentPwa != null && (currentPwa.currentURL.startsWith(url) || url.startsWith(currentPwa.currentURL))) {
            return
        }

        val newTab = Tab(
            profileId = currentProfileId,
            currentURL = url,
            state = TabState.ACTIVE,
        )

        geckoManager.getSession(newTab) // Boot the engine
        pwaTab.value = newTab // Set it as the active standalone app
    }
    //endregion
    // region Profile Logic
    val profiles = mutableStateListOf<Profile>().apply {
        addAll(profileManager.loadProfiles())
    }

    val activeProfileId = mutableStateOf(profileManager.getActiveProfileId())

    val currentProfileId: String
        get() = if (isStandaloneMode.value && pwaProfileId.value.isNotEmpty()) pwaProfileId.value else activeProfileId.value

    // --- ON-THE-FLY SYNC ---
    private var globalPrefs: android.content.SharedPreferences? = null
    private var currentProfilePrefs: android.content.SharedPreferences? = null
    private var syncJob: Job? = null // The debounce tracker

    private val prefsListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        // DEBOUNCE: Cancel the previous job if the user is still actively dragging a slider!
        syncJob?.cancel()
        syncJob = viewModelScope.launch(Dispatchers.IO) {
            // Wait 300ms for the user to finish dragging/typing before we do heavy disk reads
            delay(300)

            // Do the heavy lifting on a background thread so the UI never drops a frame
            val newSettings = loadSettingsFromPrefs(currentProfileId)
            val newSiteSettings = siteSettingsManager.loadSettings(currentProfileId)

            // Push the fresh data to the UI on the Main thread
            withContext(Dispatchers.Main) {
                _browserSettings.value = newSettings
                geckoManager.setAdBlockEnabled(newSettings.isAdBlockEnabled)

                siteSettings.clear()
                siteSettings.putAll(newSiteSettings)
            }
        }
    }

    private fun setupPrefsListener(profileId: String) {
        val context = getApplication<Application>()

        // Listen to Global Prefs (Corner Radius, Padding, etc)
        globalPrefs = context.getSharedPreferences("BrowserPrefs", Context.MODE_PRIVATE)
        globalPrefs?.registerOnSharedPreferenceChangeListener(prefsListener)

        // Listen to Profile Prefs (AdBlock, Theme, etc)
        currentProfilePrefs?.unregisterOnSharedPreferenceChangeListener(prefsListener)
        currentProfilePrefs = context.getSharedPreferences("BrowserPrefs_$profileId", Context.MODE_PRIVATE)
        currentProfilePrefs?.registerOnSharedPreferenceChangeListener(prefsListener)
    }


    fun switchProfile(newProfileId: String) {
        if (activeProfileId.value == newProfileId) return

        if (inspectingAppId.longValue != 0L) inspectingAppId.longValue = 0L

        // 1. Identify the Active Tab of the profile we are LEAVING
        val currentActiveTabId = activeTab?.id

        // 2. Prepare tabs for saving: Update states and Manage RAM
        val memoryUsage = _browserSettings.value.memoryUsage // 0: Low, 1: Standard, 2: High

        val updatedTabsForSave = tabs.map { tab ->
            val liveState = geckoManager.getSessionStateString(tab.id)
            val updatedTab = if (liveState != null) tab.copy(savedState = liveState) else tab

            when (memoryUsage) {
                0 -> { // LOW MEMORY: Close everything
                    geckoManager.closeSession(tab)
                }
                1 -> { // STANDARD: Keep only the active tab paused, close the rest
                    if (tab.id == currentActiveTabId) {
                        geckoManager.pauseSessionIfExists(tab.id)
                    } else {
                        geckoManager.closeSession(tab)
                    }
                }
                2 -> { // HIGH MEMORY: Keep all tabs paused in RAM
                    geckoManager.pauseSessionIfExists(tab.id)
                }
            }
            updatedTab
        }

        // 3. Freeze current tabs to disk before switching
        tabManager.saveTabs(activeProfileId.value, updatedTabsForSave, _activeTabIndex.value)

        // 4. Update Profile ID
        activeProfileId.value = newProfileId
        profileManager.saveActiveProfileId(newProfileId)
        setupPrefsListener(newProfileId)

        // 5. Reload settings
        _browserSettings.value = loadSettingsFromPrefs(newProfileId)
        geckoManager.setAdBlockEnabled(_browserSettings.value.isAdBlockEnabled)

        // 6. Reload auxiliary data (Apps, History, Settings)
        apps.clear()
        apps.addAll(appManager.loadApps(newProfileId))

        visitedUrlMap.clear()
        visitedUrlMap.putAll(visitedUrlManager.loadUrlMap(newProfileId))

        siteSettings.clear()
        siteSettings.putAll(siteSettingsManager.loadSettings(newProfileId))

        // 7. Reload Tabs for the NEW profile
        val loadedTabs = tabManager.loadTabs(newProfileId, null)
        tabs.clear()
        tabs.addAll(loadedTabs)

        // 8. Set new Active Index
        _activeTabIndex.value = tabManager.getActiveTabIndex(newProfileId).coerceAtLeast(0)

        // 9. Force UI to update session
        sessionRefreshTrigger.intValue++
    }




    fun createNewProfile(name: String = "new profile") {
        // Default to: (Active Profile Index) + 1
        val targetIndex =
            (profiles.indexOfFirst { it.id == activeProfileId.value }.coerceAtLeast(0) + 1)

        val newProfile = Profile(
            id = "profile_${System.currentTimeMillis()}",
            name = name
        )

        // Ensure the index is within valid bounds (0 to current size)
        val safeIndex = targetIndex.coerceIn(0, profiles.size)

        // Insert at the specific index
        profiles.add(safeIndex, newProfile)

        profileManager.saveProfiles(profiles)

        val currentSettings = _browserSettings.value
        saveSettingsToPrefs(newProfile.id, currentSettings)


        // Switch to the newly created profile immediately
        switchProfile(newProfile.id)
    }

    fun renameProfile(newName: String) {
        val trimmedName = newName.trim()
        if (trimmedName.isEmpty()) return // Optional: prevent empty names

        val currentId = activeProfileId.value
        val currentIndex = profiles.indexOfFirst { it.id == currentId }

        if (currentIndex != -1) {
            // 1. Update the profile in memory (triggers UI recomposition)
            profiles[currentIndex] = profiles[currentIndex].copy(name = trimmedName)

            // 2. Save the updated list to disk
            profileManager.saveProfiles(profiles)
        }
    }

    fun moveInspectedTabToNextProfile() {
        val tabToMove = currentInspectingTab ?: return
        val currentProfileIdStr = activeProfileId.value

        // 1. Determine Target Profile
        val targetProfileId: String
        if (profiles.size <= 1) {
            // Case: Only 1 profile exists. Create a new one.
            val newProfile = Profile(
                id = "profile_${System.currentTimeMillis()}",
                name = "profile ${profiles.size + 1}"
            )
            profiles.add(newProfile)
            profileManager.saveProfiles(profiles)
            targetProfileId = newProfile.id
        } else {
            // Case: Multiple profiles. Find the next one in the list (circular).
            val currentIndex = profiles.indexOfFirst { it.id == currentProfileIdStr }
            val nextIndex = (currentIndex + 1) % profiles.size
            targetProfileId = profiles[nextIndex].id
        }

        // 2. Add Tab to Target Profile (Disk Operation)
        val targetTabs = tabManager.loadTabs(targetProfileId, null).toMutableList()

        // Use .copy() to preserve savedState, history, etc., and ensure a fresh ID
        val newTabForTarget = tabToMove.copy(
            profileId = targetProfileId,
            id = System.currentTimeMillis(),
            state = TabState.ACTIVE
        )

        // Deactivate whatever was previously active in the target profile
        for (i in targetTabs.indices) {
            if (targetTabs[i].state == TabState.ACTIVE) {
                targetTabs[i] = targetTabs[i].copy(state = TabState.BACKGROUND)
            }
        }

        targetTabs.add(newTabForTarget)
        val newTargetActiveIndex = targetTabs.lastIndex

        // Save Target to Disk
        tabManager.saveTabs(targetProfileId, targetTabs, newTargetActiveIndex)

        // 3. Remove from Current Profile (Memory Operation)
        val indexToRemove = tabs.indexOf(tabToMove)
        if (indexToRemove != -1) {
            val wasActive = (indexToRemove == _activeTabIndex.value)

            // Close the Gecko Session to free RAM immediately
            geckoManager.closeSession(tabToMove)

            // Remove from list
            tabs.removeAt(indexToRemove)

            // Safely adjust Active Index for the current profile to prevent OutOfBounds crashes
            if (tabs.isEmpty()) {
                val defaultTab = Tab(
                    profileId = currentProfileIdStr,
                    currentURL = DefaultSettingValues.URL,
                    state = TabState.ACTIVE
                )
                tabs.add(defaultTab)
                _activeTabIndex.value = 0
            } else {
                if (wasActive) {
                    // Moving the active tab: focus the one to its left
                    val nextActiveIndex = (indexToRemove - 1).coerceAtLeast(0)
                    _activeTabIndex.value = nextActiveIndex
                    tabs[nextActiveIndex] = tabs[nextActiveIndex].copy(state = TabState.ACTIVE)
                } else if (indexToRemove < _activeTabIndex.value) {
                    // Moving a background tab to the left of the active tab: shift the index down
                    _activeTabIndex.value -= 1
                }
            }
        }

        // 4. Perform Switch
        // This wipes memory and reloads the target profile from disk
        switchProfile(targetProfileId)

        // 5. Cleanup UI
        // CRITICAL: Point the UI to the newly loaded tab's actual ID (which is now the activeTab)
        updateUI { it.copy(inspectingTabId = activeTab?.id, isTabDataPanelVisible = false) }
    }

    fun deleteProfile() {
        // Prevent deleting if it's the only profile left
        if (profiles.size <= 1) return

        val idToDelete = activeProfileId.value
        val currentIndex = profiles.indexOfFirst { it.id == idToDelete }

        if (currentIndex == -1) return // Safety check if not found

        // Determine which profile to switch to BEFORE removing it.
        // If deleting the very first one (index 0), fallback to index 1.
        // Otherwise, move to the previous one (index - 1).
        val targetIndex = if (currentIndex > 0) currentIndex - 1 else 1
        val profileIdToSwitchTo = profiles[targetIndex].id

        // 1. Wipe the container data from GeckoView
        geckoManager.wipeProfileData(idToDelete)

        // 2. Remove from list and save
        profiles.removeAt(currentIndex)
        profileManager.saveProfiles(profiles)

        // 3. Switch to the newly determined profile
        switchProfile(profileIdToSwitchTo)
    }

    // endregion
    //region Gecko
    fun handleExternalIntent(activity: Activity, url: String) {
        try {
            val intent: Intent
            val isIntentScheme = url.startsWith("intent://")

            if (isIntentScheme) {
                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    // Security: Do not allow the target app to access your browser's components
                    intent.addCategory(Intent.CATEGORY_BROWSABLE)
                    intent.component = null
                    intent.selector = null
                } catch (e: Exception) {
                    //Log.e("Intent", "Bad intent URI: $url", e)
                    return
                }
            } else {
                // Standard schemes (mailto, tel, market)
                intent = Intent(Intent.ACTION_VIEW, url.toUri())
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // --- THE FIX: Try to launch immediately ---
            // Don't check resolveActivity() first. It returns null on Android 11+
            // unless you declare <queries> in Manifest. Just try to start it.
            try {
                activity.startActivity(intent)
                return // Success! We are done.
            } catch (_: ActivityNotFoundException) {
                // App not installed. Now we handle the fallback.
            }

            // --- FALLBACK LOGIC (Only runs if startActivity failed) ---
            if (isIntentScheme) {
                val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                if (!fallbackUrl.isNullOrEmpty()) {
                    // 1. Load Fallback URL (Mobile Website)
                    activeTab?.let { tab ->
                        val session = geckoManager.getSession(tab)
                        session.loadUri(fallbackUrl)
                    }
                } else {
                    // 2. Open Play Store
                    val pack = intent.`package`
                    if (!pack.isNullOrEmpty()) {
                        val marketIntent =
                            Intent(Intent.ACTION_VIEW, "market://details?id=$pack".toUri())
                        marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            activity.startActivity(marketIntent)
                        } catch (e: Exception) {
                            //Log.e("Intent", "Play Store not found", e)
                        }
                    }
                }
            } else {
                Toast.makeText(activity, "No app found to open this link", Toast.LENGTH_SHORT)
                    .show()
            }

        } catch (e: Exception) {
            //Log.e("Intent", "Failed to handle intent", e)
        }
    }

    //    fun handleExternalIntent(activity: Activity, url: String) {
//        try {
//            val intent: Intent
//
//            // 1. Special handling for Android "intent://" scheme
//            if (url.startsWith("intent://")) {
//                try {
//                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
//                } catch (e: Exception) {
//                    //Log.e("Intent", "Bad intent URI", e)
//                    return
//                }
//
//                // Check if an app exists to handle this
//                val packageManager = activity.packageManager
//                val info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
//
//                if (info == null) {
//                    // App not installed. Check if there is a fallback URL (e.g. Play Store link)
//                    val fallbackUrl = intent.getStringExtra("browser_fallback_url")
//                    if (!fallbackUrl.isNullOrEmpty()) {
//                        // Load the fallback URL in our browser
//                        activeTab?.let { tab ->
//                            val session = geckoManager.getSession(tab)
//                            session.loadUri(fallbackUrl)
//                        }
//                    } else {
//                        // No fallback? Try to open the Play Store for the package
//                        val pack = intent.`package`
//                        if (!pack.isNullOrEmpty()) {
//                            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pack"))
//                            try {
//                                activity.startActivity(marketIntent)
//                            } catch (e: Exception) {
//                                // If no Play Store, try web play store
//                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pack"))
//                                activity.startActivity(webIntent)
//                            }
//                        }
//                    }
//                    return
//                }
//            } else {
//                // 2. Standard schemes (mailto:, tel:, market://, etc.)
//                intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//            }
//
//            // 3. Launch the external app
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            activity.startActivity(intent)
//
//        } catch (e: ActivityNotFoundException) {
//            // Optional: Show a Toast "No app found to open this link"
//        } catch (e: Exception) {
//        }
//    }
    //endregion
    //region Browser Settings
    private val _browserSettings = MutableStateFlow(loadSettingsFromPrefs(activeProfileId.value))
    val browserSettings = _browserSettings.asStateFlow()

    private fun loadSettingsFromPrefs(profileId: String): BrowserSettings {
        val context = getApplication<Application>()
        // 1. Global Preferences (Always shared)
        val globalPrefs = context.getSharedPreferences("BrowserPrefs", Context.MODE_PRIVATE)
        // 2. Profile-specific Preferences
        val profilePrefs =
            context.getSharedPreferences("BrowserPrefs_$profileId", Context.MODE_PRIVATE)

        // Fallback for migration (if the profile doesn't exist yet, pull from global)
        // We now check for "default_url" instead of "is_first_app_load" since it moved to global
        val prefsToUse = if (profilePrefs.contains("default_url")) profilePrefs else globalPrefs
        val d = DefaultSettingValues

        val rawOptionsOrder =
            prefsToUse.getString("options_order", d.OPTIONS_ORDER) ?: d.OPTIONS_ORDER
        val rawSettingsOrder =
            prefsToUse.getString("settings_order", d.SETTINGS_ORDER) ?: d.SETTINGS_ORDER

        val allSavedTokens = (rawOptionsOrder.split(",") + rawSettingsOrder.split(","))
            .filter { it.isNotBlank() }
            .toSet()

        val missingTokens = BrowserOption.entries
            .map { it.name }
            .filter { it !in allSavedTokens }

        val finalSettingsOrder = if (missingTokens.isNotEmpty()) {
            val updated = rawSettingsOrder.split(",").filter { it.isNotBlank() }.toMutableList()
            updated.addAll(missingTokens)
            updated.joinToString(",")
        } else {
            rawSettingsOrder
        }
        return BrowserSettings(
            // --- GLOBAL SETTINGS (Shared across all profiles) ---
            isFirstAppLoad = globalPrefs.getBoolean("is_first_app_load", true),
            padding = globalPrefs.getFloat("padding", d.PADDING),
            deviceCornerRadius = globalPrefs.getFloat("device_corner_radius", d.CORNER_RADIUS),
            singleLineHeight = globalPrefs.getFloat("single_line_height", d.SINGLE_LINE_HEIGHT),
            maxListHeight = globalPrefs.getFloat("max_list_height", d.MAX_LIST_HEIGHT),
            memoryUsage = globalPrefs.getInt("memory_usage", d.MEMORY_USAGE),


            // --- PROFILE-SPECIFIC SETTINGS ---
            defaultUrl = prefsToUse.getString("default_url", d.URL) ?: d.URL,
            animationSpeed = prefsToUse.getFloat("animation_speed", d.ANIMATION_SPEED),
            isSharpMode = prefsToUse.getBoolean("is_sharp_mode", d.IS_SHARP_MODE),
            cursorContainerSize = prefsToUse.getFloat(
                "cursor_container_size",
                d.CURSOR_CONTAINER_SIZE
            ),
            cursorPointerSize = prefsToUse.getFloat("cursor_pointer_size", d.CURSOR_POINTER_SIZE),
            cursorTrackingSpeed = prefsToUse.getFloat(
                "cursor_tracking_speed",
                d.CURSOR_TRACKING_SPEED
            ),
            showSuggestions = prefsToUse.getBoolean("show_suggestions", d.SHOW_SUGGESTIONS),
            closedTabHistorySize = prefsToUse.getFloat(
                "closed_tab_history_size",
                d.CLOSED_TAB_HISTORY_SIZE
            ),
            backSquareIdleOpacity = prefsToUse.getFloat(
                "back_square_idle_opacity",
                d.BACK_SQUARE_IDLE_OPACITY
            ),
            searchEngine = prefsToUse.getInt("search_engine", d.SEARCH_ENGINE),
            isFullscreenMode = prefsToUse.getBoolean("is_fullscreen_mode", d.IS_FULLSCREEN_MODE),
            highlightColor = prefsToUse.getInt("highlight_color", d.HIGHLIGHT_COLOR),
            isAdBlockEnabled = prefsToUse.getBoolean("is_ad_block_enabled", d.IS_AD_BLOCK_ENABLED),
            isDesktopMode = prefsToUse.getBoolean("is_desktop_mode", d.IS_DESKTOP_MODE),
            optionsOrder = rawOptionsOrder,
            settingsOrder = finalSettingsOrder,
            hiddenOptions = prefsToUse.getString("hidden_options", d.HIDDEN_OPTIONS)
                ?: d.HIDDEN_OPTIONS,
            isEnabledMediaControl = prefsToUse.getBoolean(
                "is_enabled_media_control",
                d.IS_ENABLED_MEDIA_CONTROL
            ),
            isEnabledOutSync = prefsToUse.getBoolean("is_enabled_out_sync", d.IS_ENABLED_OUT_SYNC),


            // Settings that don't have constants yet (or are dynamic)
            backSquareOffsetX = prefsToUse.getFloat("back_square_offset_x", -1f),
            backSquareOffsetY = prefsToUse.getFloat("back_square_offset_y", -1f),
            isGuideModeEnabled = prefsToUse.getBoolean("is_guide_mode_enabled", true),
        )
    }

    fun updateSettings(mutation: (BrowserSettings) -> BrowserSettings) {
        _browserSettings.update(mutation)
        // Persist the resulting value after the update for the CURRENT active profile
        saveSettingsToPrefs(currentProfileId, _browserSettings.value)
    }

    fun updateField(field: BrowserSettingField, value: Any) {
        updateSettings { current ->
            when (field) {
                BrowserSettingField.CORNER_RADIUS -> current.copy(deviceCornerRadius = value as Float)
                BrowserSettingField.PADDING -> current.copy(padding = value as Float)
                BrowserSettingField.ANIMATION_SPEED -> current.copy(animationSpeed = value as Float)
                BrowserSettingField.CURSOR_CONTAINER_SIZE -> current.copy(cursorContainerSize = value as Float)
                BrowserSettingField.CURSOR_TRACKING_SPEED -> current.copy(cursorTrackingSpeed = value as Float)
                BrowserSettingField.BACK_SQUARE_OPACITY -> current.copy(backSquareIdleOpacity = value as Float)
                BrowserSettingField.DEFAULT_URL -> current.copy(defaultUrl = value as String)
                BrowserSettingField.CLOSED_TAB_HISTORY_SIZE -> current.copy(closedTabHistorySize = value as Float)
                BrowserSettingField.MAX_LIST_HEIGHT -> current.copy(maxListHeight = value as Float)
                BrowserSettingField.SINGLE_LINE_HEIGHT -> current.copy(singleLineHeight = value as Float)
                BrowserSettingField.SEARCH_ENGINE -> {
                    val index = when (value) {
                        is Float -> value.toInt()
                        is Int -> value
                        else -> current.searchEngine
                    }
                    current.copy(searchEngine = index)
                }

                BrowserSettingField.HIGHLIGHT_COLOR -> {
                    val color = when (value) {
                        is Int -> value
                        is Long -> value.toInt()
                        else -> current.highlightColor
                    }
                    current.copy(highlightColor = color)
                }

                BrowserSettingField.AD_BLOCK_ENABLED -> {
                    val isEnabled = value as Boolean
                    geckoManager.setAdBlockEnabled(isEnabled)

                    activeTab?.let { tab ->
                        geckoManager.getSession(tab).reload()
                    }

                    current.copy(isAdBlockEnabled = isEnabled)
                }

                BrowserSettingField.GUIDE_MODE -> current.copy(isGuideModeEnabled = value as Boolean)
                BrowserSettingField.MEMORY_USAGE -> {
                    val index = when (value) {
                        is Float -> value.toInt()
                        is Int -> value
                        else -> current.memoryUsage
                    }
                    current.copy(memoryUsage = index)
                }

                BrowserSettingField.INFO -> current
                BrowserSettingField.OPTIONS_ORDER -> current.copy(optionsOrder = value as String)
                BrowserSettingField.SETTINGS_ORDER -> current.copy(settingsOrder = value as String)
                BrowserSettingField.HIDDEN_OPTIONS -> current.copy(hiddenOptions = value as String)

            }
        }
    }

    fun resetSettings() {
        updateSettings {
            val d = DefaultSettingValues
            it.copy(
                padding = d.PADDING,
                deviceCornerRadius = d.CORNER_RADIUS,
                singleLineHeight = d.SINGLE_LINE_HEIGHT,
                maxListHeight = d.MAX_LIST_HEIGHT,
                defaultUrl = d.URL,
                showSuggestions = d.SHOW_SUGGESTIONS,
                animationSpeed = d.ANIMATION_SPEED,
                isSharpMode = d.IS_SHARP_MODE,
                isDesktopMode = d.IS_DESKTOP_MODE,
                cursorContainerSize = d.CURSOR_CONTAINER_SIZE,
                cursorPointerSize = d.CURSOR_POINTER_SIZE,
                cursorTrackingSpeed = d.CURSOR_TRACKING_SPEED,
                backSquareIdleOpacity = d.BACK_SQUARE_IDLE_OPACITY,
                highlightColor = d.HIGHLIGHT_COLOR,
                isAdBlockEnabled = d.IS_AD_BLOCK_ENABLED,
                isFullscreenMode = d.IS_FULLSCREEN_MODE,
                closedTabHistorySize = d.CLOSED_TAB_HISTORY_SIZE,
                searchEngine = d.SEARCH_ENGINE,
                optionsOrder = d.OPTIONS_ORDER,
                settingsOrder = d.SETTINGS_ORDER,
                hiddenOptions = d.HIDDEN_OPTIONS,
                isEnabledMediaControl = d.IS_ENABLED_MEDIA_CONTROL,
                isEnabledOutSync = d.IS_ENABLED_OUT_SYNC,
                memoryUsage = d.MEMORY_USAGE
            )
        }
    }
    fun refreshSettings() {
        // Force the ViewModel to re-read the disk.
        // This ensures the PWA instantly adopts any changes made in the Main Browser!
        _browserSettings.value = loadSettingsFromPrefs(currentProfileId)
        geckoManager.setAdBlockEnabled(_browserSettings.value.isAdBlockEnabled)

        val updatedSiteSettings = siteSettingsManager.loadSettings(currentProfileId)
        siteSettings.clear()
        siteSettings.putAll(updatedSiteSettings)
    }
    private fun saveSettingsToPrefs(profileId: String, settings: BrowserSettings) {
        val context = getApplication<Application>()
        val globalPrefs = context.getSharedPreferences("BrowserPrefs", Context.MODE_PRIVATE)
        val profilePrefs =
            context.getSharedPreferences("BrowserPrefs_$profileId", Context.MODE_PRIVATE)

        // --- Save GLOBAL Settings ---
        globalPrefs.edit().apply {
            putBoolean("is_first_app_load", settings.isFirstAppLoad)
            putFloat("padding", settings.padding)
            putFloat("device_corner_radius", settings.deviceCornerRadius)
            putFloat("single_line_height", settings.singleLineHeight)
            putFloat("max_list_height", settings.maxListHeight)
            putInt("memory_usage", settings.memoryUsage)

            apply()
        }

        // --- Save PROFILE-SPECIFIC Settings ---
        profilePrefs.edit().apply {
            putString("default_url", settings.defaultUrl)
            putFloat("animation_speed", settings.animationSpeed)
            putBoolean("is_sharp_mode", settings.isSharpMode)
            putFloat("cursor_container_size", settings.cursorContainerSize)
            putFloat("cursor_pointer_size", settings.cursorPointerSize)
            putFloat("cursor_tracking_speed", settings.cursorTrackingSpeed)
            putBoolean("show_suggestions", settings.showSuggestions)
            putFloat("closed_tab_history_size", settings.closedTabHistorySize)
            putFloat("back_square_offset_x", settings.backSquareOffsetX)
            putFloat("back_square_offset_y", settings.backSquareOffsetY)
            putFloat("back_square_idle_opacity", settings.backSquareIdleOpacity)
            putInt("search_engine", settings.searchEngine)
            putBoolean("is_fullscreen_mode", settings.isFullscreenMode)
            putInt("highlight_color", settings.highlightColor)
            putBoolean("is_ad_block_enabled", settings.isAdBlockEnabled)
            putBoolean("is_guide_mode_enabled", settings.isGuideModeEnabled)
            putBoolean("is_desktop_mode", settings.isDesktopMode)
            putBoolean("is_enabled_media_control", settings.isEnabledMediaControl)
            putBoolean("is_enabled_out_sync", settings.isEnabledOutSync)
            putString("options_order", settings.optionsOrder)
            putString("settings_order", settings.settingsOrder)
            putString("hidden_options", settings.hiddenOptions)
            apply()
        }
    }    //endregion
    //region UI State
    private val _uiState = MutableStateFlow(
        BrowserUIState(
            isSettingsPanelVisible = _browserSettings.value.isFirstAppLoad
        )
    )
    val uiState = _uiState.asStateFlow()

    /**
     * Generic update function.
     * Usage in Compose: viewModel.updateUI { it.copy(isUrlBarVisible = false) }
     */
    fun updateUI(mutation: (BrowserUIState) -> BrowserUIState) {
        _uiState.update(mutation)
    }

//    /**
//     * Helper to save current panel state before entering "Search Mode" (Focusing URL bar)
//     */
//    fun saveCurrentPanelState() {
//        val current = _uiState.value
//        val snapshot = PanelVisibilityState(
//            options = false, // Assuming options panel isn't part of this main state yet
//            tabs = current.isTabsPanelVisible,
//            downloads = current.isDownloadPanelVisible,
//            tabData = current.isTabDataPanelVisible,
//            nav = current.isNavPanelVisible
//        )
//        updateUI { it.copy(savedPanelState = snapshot) }
//    }
//
//    /**
//     * Helper to restore panel state (e.g. when clicking out of URL bar without searching)
//     */
//    fun restorePanelState() {
//        val saved = _uiState.value.savedPanelState
//        if (saved != null) {
//            updateUI {
//                it.copy(
//                    isTabsPanelVisible = saved.tabs,
//                    isDownloadPanelVisible = saved.downloads,
//                    isTabDataPanelVisible = saved.tabData,
//                    isNavPanelVisible = saved.nav,
//                    savedPanelState = null // Clear after restore
//                )
//            }
//        }
//    }
    //endregion
    //region Tab logic

    val tabs = mutableStateListOf<Tab>()
    val recentlyClosedTabs = mutableStateListOf<Tab>()

    private val _activeTabIndex = MutableStateFlow(0)
    val activeTabIndex = _activeTabIndex.asStateFlow()

    //    val activeTab: Tab?
//        get() = tabs.getOrNull(_activeTabIndex.value)
    val activeTab: Tab?
        get() = if (isStandaloneMode.value) pwaTab.value else tabs.getOrNull(_activeTabIndex.value)
    private var isInitialized = false
    val initializeTabs = { initialUrl: String? ->
        if (!isInitialized) {
            // 1. Load tabs using the intent URL (if provided)
            val loadedTabs = tabManager.loadTabs(activeProfileId.value, initialUrl)
            tabs.clear()
            tabs.addAll(loadedTabs)

            // 2. Set the active index
            _activeTabIndex.value =
                tabManager.getActiveTabIndex(activeProfileId.value).coerceAtLeast(0)

            isInitialized = true
        }
    }


    val currentInspectingTab: Tab?
        get() = _uiState.value.inspectingTabId?.let { id ->
            tabs.find { it.id == id }
        }


    val selectTab = { newIndex: Int ->
        val currentIndex = _activeTabIndex.value

        if (newIndex != currentIndex && newIndex in tabs.indices) {
            // 1. Reset loading state if switching away
            if (_uiState.value.isLoading) {
                updateUI { it.copy(isLoading = false) }
            }

            val oldTab = tabs[currentIndex]
            val newTab = tabs[newIndex]

            geckoManager.pauseSessionIfExists(oldTab.id)

//            geckoManager.getSession(newTab).setActive(true)


            // 2. Deactivate current tab
            if (currentIndex in tabs.indices) {
                tabs[currentIndex] = tabs[currentIndex].copy(state = TabState.BACKGROUND)
            }

            // 3. Activate new tab
            tabs[newIndex] = tabs[newIndex].copy(state = TabState.ACTIVE)

            // 4. Update the active index
            _activeTabIndex.value = newIndex

            // 5. Sync the inspection ID to the newly selected tab
            updateUI { it.copy(inspectingTabId = tabs[newIndex].id) }

            // 6. Persist changes
            saveTabs()
        }
    }

    val handleNewSession = { engineId: Long, uri: String ->
        // 1. Deactivate current active tab
        val currentIndex = _activeTabIndex.value
        if (currentIndex in tabs.indices) {
            tabs[currentIndex] = tabs[currentIndex].copy(state = TabState.BACKGROUND)
        }

        // 2. Create the new Tab object using the ID provided by the engine
       val newTab = Tab(
            profileId = currentProfileId,
            id = engineId,
            currentURL = uri,
            state = TabState.ACTIVE
        )

        // 3. Insert right after the current active tab
        val insertIndex = (currentIndex + 1).coerceIn(0, tabs.size)
        tabs.add(insertIndex, newTab)

        // 4. Switch the active index
        _activeTabIndex.value = insertIndex

        // 5. Sync inspection state
        updateUI { it.copy(inspectingTabId = newTab.id) }

        // 6. Persist to disk
        saveTabs()
    }

    val reopenClosedTab = {
        // Check if there are any viewModel.tabs to reopen.
        if (recentlyClosedTabs.isNotEmpty()) {
            // Get the last closed tab and remove it from the stack.
            val tabToReopen = recentlyClosedTabs.removeAt(recentlyClosedTabs.lastIndex)

            // Deactivate the current tab.
            val currentIndex = _activeTabIndex.value
            if (currentIndex in tabs.indices) {
                tabs[currentIndex] = tabs[currentIndex].copy(state = TabState.BACKGROUND)
            }

            // Add the reopened tab back to the list, usually at the end or a specific index.
            // Let's add it at the end for simplicity.
            tabs.add(tabToReopen)

            // Make the reopened tab the new active tab.
            val newIndex = tabs.lastIndex
            _activeTabIndex.value = newIndex
            tabs[newIndex] = tabs[newIndex].copy(state = TabState.ACTIVE)

            // Trigger a save.
            saveTabs()
        }
    }
    val duplicateInspectedTab = {
        val originalTab = currentInspectingTab

        if (originalTab != null) {
            val liveState = geckoManager.getSessionStateString(originalTab.id)
                ?: originalTab.savedState


            val clonedTab = originalTab.copy(
                id = System.currentTimeMillis(), // New unique ID
                savedState = liveState,
                state = TabState.BACKGROUND
            )

            // 3. Find where the original is in the list
            val originalIndex = tabs.indexOf(originalTab)
            val insertIndex = (originalIndex + 1).coerceIn(0, tabs.size)

            // 4. Deactivate the current active tab
            val currentIndex = _activeTabIndex.value
            if (currentIndex in tabs.indices) {
                tabs[currentIndex] = tabs[currentIndex].copy(state = TabState.BACKGROUND)
            }

            // 5. Insert the clone and jump to it
            tabs.add(insertIndex, clonedTab)
            _activeTabIndex.value = insertIndex
            tabs[insertIndex] = tabs[insertIndex].copy(state = TabState.ACTIVE)

            updateUI { it.copy(isTabDataPanelVisible = false) }
            saveTabs()
        }
    }
    val closeTabById = { idToClose: Long ->
        val tabToClose = tabs.find { it.id == idToClose }
        if (tabToClose != null) {
            val indexToClose = tabs.indexOf(tabToClose)
            val wasActive = (indexToClose == _activeTabIndex.value)

            // 1. Close the Gecko Session
            geckoManager.closeSession(tabToClose)

            // 2. Manage Recently Closed History
            recentlyClosedTabs.add(tabToClose)
            val limit = _browserSettings.value.closedTabHistorySize.toInt()
            while (recentlyClosedTabs.size > limit) {
                recentlyClosedTabs.removeAt(0)
            }

            // 3. Remove from main list
            tabs.removeAt(indexToClose)

            // 4. Handle index and state updates
            if (wasActive) {
                // Opener pattern: move to the tab to the left
                val nextIndex = (indexToClose - 1).coerceAtLeast(0)
                if (tabs.isNotEmpty()) {
                    _activeTabIndex.value = nextIndex
                    // Update the new active tab's state
                    tabs[nextIndex] = tabs[nextIndex].copy(state = TabState.ACTIVE)
                }
            } else if (indexToClose < _activeTabIndex.value) {
                // If closing a tab before the active one, shift index down
                _activeTabIndex.value -= 1
            }

            // 5. Persist
            saveTabs()
        }
    }
    val closeInspectedTab = { onExitApp: () -> Unit ->
        val tabToClose = currentInspectingTab
        if (tabToClose != null) {
            val indexToClose = tabs.indexOf(tabToClose)

            if (indexToClose != -1) {
                if (tabs.size > 1) {
                    // 1. Handle Recently Closed History
                    recentlyClosedTabs.add(tabToClose)
                    val limit = _browserSettings.value.closedTabHistorySize.toInt()
                    while (recentlyClosedTabs.size > limit) {
                        recentlyClosedTabs.removeAt(0)
                    }

                    // 2. Close Gecko Session
                    geckoManager.closeSession(tabToClose)

                    // 3. Remove from list
                    tabs.removeAt(indexToClose)

                    // 4. Determine next active tab and update Index/UIState
                    val wasActive = (indexToClose == _activeTabIndex.value)

                    if (wasActive) {
//                        val nextTabIndex =
//                            if (indexToClose >= tabs.size) tabs.lastIndex else indexToClose

                        val nextTabIndex = (indexToClose - 1).coerceAtLeast(0)

                        _activeTabIndex.value = nextTabIndex
                        tabs[nextTabIndex] = tabs[nextTabIndex].copy(state = TabState.ACTIVE)

                        // Update what we are currently inspecting to match the new active tab
                        updateUI { it.copy(inspectingTabId = tabs[nextTabIndex].id) }
                    } else {
                        if (indexToClose < _activeTabIndex.value) {
                            _activeTabIndex.value -= 1
                        }
                        // If we closed a background tab, update inspection ID to the current active tab
                        updateUI { it.copy(inspectingTabId = activeTab?.id) }
                    }

                    saveTabs()
                } else {
                    // LAST TAB CASE
                    tabs.clear()
                    tabManager.clearAllTabs(activeProfileId.value)

                    // We trigger the UI callback to finish the Activity
                    onExitApp()
                }
            }
        }
    }
    val closeActiveTab = { onExitApp: () -> Unit ->
        val indexToRemove = _activeTabIndex.value

        if (tabs.size > 1 && indexToRemove in tabs.indices) {
            val tabToRemove = tabs[indexToRemove]

            // 2. Handle Recently Closed History
            recentlyClosedTabs.add(tabToRemove)
            val limit = _browserSettings.value.closedTabHistorySize.toInt()
            while (recentlyClosedTabs.size > limit) {
                recentlyClosedTabs.removeAt(0)
            }

            // 3. Close Gecko Session
            geckoManager.closeSession(tabToRemove)

            // 4. Remove from the list
            tabs.removeAt(indexToRemove)

            // 5. Determine the next active tab index
//            val nextTabIndex = if (indexToRemove >= tabs.size) tabs.lastIndex else indexToRemove
            val nextTabIndex = (indexToRemove - 1).coerceAtLeast(0)

            // 6. Update state: Set new index and make that tab ACTIVE
            _activeTabIndex.value = nextTabIndex
            tabs[nextTabIndex] = tabs[nextTabIndex].copy(state = TabState.ACTIVE)

            // 7. Sync the inspection ID to the new active tab
            updateUI { it.copy(inspectingTabId = tabs[nextTabIndex].id) }

            saveTabs()
        } else {
            // CASE: Last tab being closed
            tabs.clear()
            tabManager.clearAllTabs(activeProfileId.value)

            // Trigger UI callback to finish activity
            onExitApp()
        }
    }

    val optimizeMemory = {
        val currentActiveTab = activeTab
        if (currentActiveTab != null) {
            // 1. Identify tabs in the CURRENT profile to be closed
            val tabsToClose = tabs.filter { it.id != currentActiveTab.id }

            // 2. Add them to recently closed history
            tabsToClose.forEach { tab ->
                recentlyClosedTabs.add(tab)
            }

            val limit = _browserSettings.value.closedTabHistorySize.toInt()
            while (recentlyClosedTabs.size > limit) {
                recentlyClosedTabs.removeAt(0)
            }

            // 3. Clear the UI list and only keep the active tab
            tabs.clear()
            tabs.add(currentActiveTab)
            _activeTabIndex.value = 0

            // 4. Aggressively clear the Gecko Engine RAM (kills background profile sessions too!)
            geckoManager.optimizeMemoryExcept(currentActiveTab.id)

            // 5. Persist the cleaned state to disk
            saveTabs()

            // Force a UI refresh
            sessionRefreshTrigger.intValue++
        }
    }

    fun createNewTab(insertAtIndex: Int, url: String) {

        // 1. Deactivate current active tab
        val currentIndex = _activeTabIndex.value
        if (currentIndex in tabs.indices) {
            tabs[currentIndex] = tabs[currentIndex].copy(state = TabState.BACKGROUND)
        }

        // 2. Create the new Tab object
        val initialUrl = url.ifBlank { _browserSettings.value.defaultUrl }

        Log.d("marcBlank", "load blank from createNewTab, $initialUrl")
       val newTab = Tab(
            profileId = currentProfileId,
            currentURL = initialUrl.ifBlank { "about:blank" },
            state = TabState.ACTIVE,
        )

        // 3. Insert into the list at the desired index
        val targetIndex = insertAtIndex.coerceIn(0, tabs.size)
        tabs.add(targetIndex, newTab)

        // 4. Ensure Gecko creates a session for this new tab
        geckoManager.getSession(newTab)

        // 5. Update the inspection state to follow the new tab

        updateUI { it.copy(inspectingTabId = newTab.id) }

        // 6. Update the active index
        _activeTabIndex.value = targetIndex

        // 7. Persist changes to disk
        saveTabs()

    }

    val updateTabById = { tabId: Long, transform: (Tab) -> Tab ->
        // If we are in a PWA, apply the updates directly to the isolated PWA Tab!
        if (isStandaloneMode.value && pwaTab.value?.id == tabId) {
            pwaTab.value = transform(pwaTab.value!!)
        } else {
            // Normal Browser Tab update
            val index = tabs.indexOfFirst { it.id == tabId }
            if (index != -1) {
                val oldTab = tabs[index]
                val newTab = transform(oldTab)

                if (oldTab != newTab) {
                    tabs[index] = newTab
                    saveTabs()
                }
            }
        }
    }
    private var saveJob: Job? = null
    val saveTabs = {
        saveJob?.cancel()
        saveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500) // Wait for rapid events (like redirects) to finish
            // Clean, regular save. PWA tabs don't exist in this list anymore!
            tabManager.saveTabs(activeProfileId.value, tabs.toList(), _activeTabIndex.value)
        }
    }

    //endregion
    //region Pin Logic
    val apps = mutableStateListOf<App>().apply {
        addAll(appManager.loadApps(activeProfileId.value))
    }
    val inspectingAppId = mutableLongStateOf(0L)

    fun pinApp(context: Context, title: String, url: String, iconUrl: String) {
        val newApp = App(
            id = System.currentTimeMillis(),
            label = title,
            url = url,
            iconUrl = iconUrl
        )
        apps.add(newApp)
        saveApps()
    }

    fun removeApp(appId: Long) {
        val index = apps.indexOfFirst { it.id == appId }
        if (index != -1) {
            apps.removeAt(index)
            saveApps()
        }
    }

    fun swapApps(fromIndex: Int, toIndex: Int) {
        if (fromIndex in apps.indices && toIndex in apps.indices) {
            val temp = apps[fromIndex]
            apps[fromIndex] = apps[toIndex]
            apps[toIndex] = temp

            saveApps()
        }
    }

    private fun saveApps() {
        appManager.saveApps(activeProfileId.value, apps)
    }


    fun generateAndInstallWebApk(context: Context, title: String, url: String, iconUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "preparing web app installer...", Toast.LENGTH_SHORT).show()
                }

                // Step 1: Generate or Download the APK
                val apkFile = File(context.cacheDir, "webapk_${System.currentTimeMillis()}.apk")

                // Run the generation process
                val isGenerated =
                    mockGenerateWebApkLocallyOrRemotely(context, title, url, iconUrl, apkFile)

                // NOW check if it generated successfully
                withContext(Dispatchers.Main) {
                    if (isGenerated && apkFile.exists()) {
                        installApkWithIntent(context, apkFile)
                    } else {
                        Toast.makeText(
                            context,
                            "failed to generate web app installer . ",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("WebAPK", "Error generating WebAPK", e)
            }
        }
    }

    private suspend fun mockGenerateWebApkLocallyOrRemotely(
        context: Context, title: String, url: String, iconUrl: String, outFile: File
    ): Boolean {

        Log.i("WebAPK", "iconURl: ${iconUrl}")
        return withContext(Dispatchers.IO) {
            try {
                val unsignedApk =
                    File(context.cacheDir, "unsigned_${System.currentTimeMillis()}.apk")

                // 1. Try to load the Favicon using Coil

                 // 1. Try to load the Favicon using your GLOBAL Custom ImageLoader
                var iconBitmap: Bitmap? = null
                try {
                    if (iconUrl.isNotBlank()) {
                        val loader = coil.Coil.imageLoader(context)

                        // --- NEW: Check if the string is raw SVG text ---
                        // If it is, we wrap it in a ByteBuffer so Coil natively parses it as a file stream!
                        val dataPayload: Any = if (iconUrl.trim().startsWith("<svg", ignoreCase = true)) {
                            java.nio.ByteBuffer.wrap(iconUrl.toByteArray(Charsets.UTF_8))
                        } else {
                            iconUrl
                        }

                        val request = ImageRequest.Builder(context)
                            .addHeader(
                                "User-Agent",
                                "Mozilla/5.0 (Android 14; Mobile; rv:130.0) Gecko/130.0 Firefox/130.0"
                            )
                            .data(dataPayload)
                            .size(192)
                            .allowHardware(false) //
                            .build()

                        val result = loader.execute(request)

                        if (result is SuccessResult) {
                            val drawable = result.drawable
                            if (drawable is BitmapDrawable) {
                                iconBitmap = drawable.bitmap
                            } else {
                                // Because it's an SVG, Coil might return a PictureDrawable!
                                // We must draw it onto a native Bitmap Canvas.
                                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 192
                                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 192
                                val bitmap =
                                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                val canvas = android.graphics.Canvas(bitmap)
                                drawable.setBounds(0, 0, canvas.width, canvas.height)
                                drawable.draw(canvas)
                                iconBitmap = bitmap
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WebAPK", "Failed to load Favicon", e)
                }

                Log.d("WebAPK", "favicon ${iconBitmap.toString()}")

                // 2. FALLBACK: If Coil fails (e.g. because it's an .ico file),
                // dynamically generate a beautiful Monogram letter icon!
                if (iconBitmap == null) {
                    val size = 192
                    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

                    // Draw a sleek dark grey background
                    paint.color = android.graphics.Color.parseColor("#252526")
                    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

                    // Draw the first letter of the App Title
                    paint.color = android.graphics.Color.WHITE
                    paint.textSize = 100f
                    paint.textAlign = android.graphics.Paint.Align.CENTER

                    val letter = if (title.isNotBlank()) title.take(1).uppercase() else "W"
                    val textY = (size / 2f) - ((paint.descent() + paint.ascent()) / 2f)
                    canvas.drawText(letter, size / 2f, textY, paint)

                    iconBitmap = bitmap
                }

                // --- THE DYNAMIC PACKAGE NAME GENERATOR ---
                // The template package is exactly 50 characters long
                val targetPackage = "com.webapk.application.template.placeholder.namexx"

                // 1. Detect the Host Flavor and set the base prefix
                val prefix = when (context.packageName) {
                    "marcinlowercase.a" -> "marcinlowercase.pwa_"
                    "studio.oo1.browser" -> "studio.oo1.web_app_"
                    else -> "com.webapp.pwa_"
                }

                // 2. Clean the App Title (lowercase, spaces to _, alphanumeric only)
                // We no longer need to force "app_" because our prefix already ends with an underscore
                // and the segment starts with "pwa_" or "web_app_", which are valid letters!
                val cleanName = title.lowercase().replace(Regex("\\s+"), "_").filter { it.isLetterOrDigit() || it == '_' }

                // 3. Add the System Clock suffix (Must start with a letter, so we use '.t')
                val timeSuffix = ".t" + System.currentTimeMillis().toString()

                // 4. Calculate how much room we have left for the app name to ensure we don't exceed 50 chars
                val maxNameLen = 50 - prefix.length - timeSuffix.length
                val safeName = cleanName.take(maxOf(0, maxNameLen))

                val desiredPackage = "$prefix$safeName$timeSuffix"

                // 5. Pad the end with 'x' to guarantee it is exactly 50 bytes long!
                val replacementPackage = desiredPackage.padEnd(50, 'x')

                java.util.zip.ZipInputStream(context.assets.open("template.apk")).use { zis ->
                    java.util.zip.ZipOutputStream(java.io.FileOutputStream(unsignedApk))
                        .use { zos ->
                            var entry = zis.nextEntry
                            while (entry != null) {
                                val name = entry.name

                                Log.w("WebAPK", "name : $name")
                                if (name.startsWith("META-INF/")) {
                                    entry = zis.nextEntry
                                    continue
                                }


                                var bytesToWrite: ByteArray? = null

                                // --- ICON FIX 2: Replace ALL remaining PNG/WebP files ---
                                if (name.contains("ic_launcher")) {
                                    if (name.endsWith(".xml")) {
                                        // 1. SILENTLY DROP ANY XML ICONS.
                                        entry = zis.nextEntry
                                        continue
                                    } else if (name.endsWith(".png") || name.endsWith(".webp") || name.endsWith(
                                            ".jpg"
                                        )
                                    ) {
                                        // 2. OVERWRITE EVERY SINGLE RASTER ICON WITH OUR FAVICON
                                        val stream = java.io.ByteArrayOutputStream()

                                        // PREVENT STRETCHING: Draw the loaded image onto a perfect 192x192 square Canvas
                                        val original = iconBitmap!!
                                        val targetSize = 192
                                        val scaled =
                                            if (original.width == targetSize && original.height == targetSize) {
                                                original
                                            } else {
                                                val square = Bitmap.createBitmap(
                                                    targetSize,
                                                    targetSize,
                                                    Bitmap.Config.ARGB_8888
                                                )
                                                val canvas = android.graphics.Canvas(square)

                                                // Calculate the scale to maintain aspect ratio
                                                val scale = minOf(
                                                    targetSize.toFloat() / original.width,
                                                    targetSize.toFloat() / original.height
                                                )
                                                val drawW = (original.width * scale).toInt()
                                                val drawH = (original.height * scale).toInt()

                                                // Center the scaled image
                                                val left = (targetSize - drawW) / 2
                                                val top = (targetSize - drawH) / 2

                                                val srcRect = android.graphics.Rect(
                                                    0,
                                                    0,
                                                    original.width,
                                                    original.height
                                                )
                                                val destRect = android.graphics.Rect(
                                                    left,
                                                    top,
                                                    left + drawW,
                                                    top + drawH
                                                )

                                                val paint =
                                                    android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
                                                canvas.drawBitmap(
                                                    original,
                                                    srcRect,
                                                    destRect,
                                                    paint
                                                )
                                                square
                                            }

                                        // Match the compression to the file extension Android Studio secretly generated
                                        if (name.endsWith(".webp")) {
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                                scaled.compress(
                                                    Bitmap.CompressFormat.WEBP_LOSSLESS,
                                                    100,
                                                    stream
                                                )
                                            } else {
                                                @Suppress("DEPRECATION")
                                                scaled.compress(
                                                    Bitmap.CompressFormat.WEBP,
                                                    100,
                                                    stream
                                                )
                                            }
                                        } else {
                                            scaled.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                        }

                                        bytesToWrite = stream.toByteArray()
                                    }
                                }

                                // If the file wasn't an icon, process normally
                                if (bytesToWrite == null) {
                                    when (name) {
                                                                                "assets/config.json" -> {
                                            val hostPackage = context.packageName
                                            val pId = activeProfileId.value

                                            // --- NEW: Safely escape everything ---
                                            // Because raw SVGs contain quotes, slashes, and line breaks, we MUST safely
                                            // escape them so the JSON doesn't become corrupted inside the APK!
                                            val escapedTitle = title.replace("\\", "\\\\").replace("\"", "\\\"")
                                            val escapedIconUrl = iconUrl.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")

                                            val configJson =
                                                """{"url": "$url", "host": "$hostPackage", "profileId": "$pId", "name": "$escapedTitle", "iconUrl": "$escapedIconUrl"}"""
                                            bytesToWrite = configJson.toByteArray(Charsets.UTF_8)
                                        }


                                        "resources.arsc" -> {
                                            val bytes = zis.readBytes()
                                            val targetTitle = "___PWA_APP_NAME___"

                                            var cleanTitle = ""
                                            var utf8Size = 0
                                            for (char in title) {
                                                val charSize =
                                                    char.toString().toByteArray(Charsets.UTF_8).size
                                                if (utf8Size + charSize > 18) break
                                                cleanTitle += char
                                                utf8Size += charSize
                                            }

                                            val replace8Bytes = ByteArray(18) { 0 }
                                            val title8Bytes = cleanTitle.toByteArray(Charsets.UTF_8)
                                            System.arraycopy(
                                                title8Bytes,
                                                0,
                                                replace8Bytes,
                                                0,
                                                title8Bytes.size
                                            )

                                            val replace16Bytes = ByteArray(36) { 0 }
                                            val title16Bytes =
                                                cleanTitle.toByteArray(Charsets.UTF_16LE)
                                            System.arraycopy(
                                                title16Bytes,
                                                0,
                                                replace16Bytes,
                                                0,
                                                title16Bytes.size
                                            )

                                            var patched = replaceBytes(
                                                bytes,
                                                targetTitle.toByteArray(Charsets.UTF_16LE),
                                                replace16Bytes
                                            )
                                            patched = replaceBytes(
                                                patched,
                                                targetTitle.toByteArray(Charsets.UTF_8),
                                                replace8Bytes
                                            )

                                            patched = replaceBytes(
                                                patched,
                                                targetPackage.toByteArray(Charsets.UTF_16LE),
                                                replacementPackage.toByteArray(Charsets.UTF_16LE)
                                            )
                                            patched = replaceBytes(
                                                patched,
                                                targetPackage.toByteArray(Charsets.UTF_8),
                                                replacementPackage.toByteArray(Charsets.UTF_8)
                                            )

                                            bytesToWrite = patched
                                        }

                                        "AndroidManifest.xml" -> {
                                            val bytes = zis.readBytes()
                                            var patched = replaceBytes(
                                                bytes,
                                                targetPackage.toByteArray(Charsets.UTF_16LE),
                                                replacementPackage.toByteArray(Charsets.UTF_16LE)
                                            )
                                            patched = replaceBytes(
                                                patched,
                                                targetPackage.toByteArray(Charsets.UTF_8),
                                                replacementPackage.toByteArray(Charsets.UTF_8)
                                            )
                                            bytesToWrite = patched
                                        }
                                    }
                                }

                                if (bytesToWrite == null) {
                                    bytesToWrite = zis.readBytes()
                                }

                                val newEntry = java.util.zip.ZipEntry(name)
                                if (entry.method == java.util.zip.ZipEntry.STORED) {
                                    newEntry.method = java.util.zip.ZipEntry.STORED
                                    newEntry.size = bytesToWrite.size.toLong()
                                    val crc = java.util.zip.CRC32()
                                    crc.update(bytesToWrite)
                                    newEntry.crc = crc.value
                                } else {
                                    newEntry.method = java.util.zip.ZipEntry.DEFLATED
                                }

                                zos.putNextEntry(newEntry)
                                zos.write(bytesToWrite)
                                zos.closeEntry()

                                entry = zis.nextEntry
                            }
                        }
                }

                val signerConfig = com.android.apksig.ApkSigner.SignerConfig.Builder(
                    "PWA_KEY",
                    getPrivateKeyFromAssets(context),
                    getCertificatesFromAssets(context)
                ).build()

                val apkSigner = com.android.apksig.ApkSigner.Builder(listOf(signerConfig))
                    .setInputApk(unsignedApk)
                    .setOutputApk(outFile)
                    .setV1SigningEnabled(true)
                    .setV2SigningEnabled(true)
                    .build()

                apkSigner.sign()
                unsignedApk.delete()

                return@withContext true
            } catch (e: Exception) {
                Log.e("WebAPK", "Failed to build local WebAPK", e)
                return@withContext false
            }
        }
    }

    // Helper: Binary Search and Replace Array
    private fun replaceBytes(
        source: ByteArray,
        target: ByteArray,
        replacement: ByteArray
    ): ByteArray {
        if (target.size != replacement.size) throw IllegalArgumentException("Target and replacement must be identical length to preserve binary offsets")
        if (target.isEmpty()) return source

        val result = source.clone()
        var i = 0

        while (i <= result.size - target.size) {
            var match = true
            for (j in target.indices) {
                if (result[i + j] != target[j]) {
                    match = false
                    break
                }
            }
            if (match) {
                // Replace the bytes
                System.arraycopy(replacement, 0, result, i, replacement.size)
                // Skip ahead by the target size so we don't accidentally re-scan modified bytes
                i += target.size
            } else {
                i++
            }
        }
        return result
    }

    // Helpers for Keystore
    // Helpers for Keystore
    // Helpers for Cryptography (No Keystore Needed!)
    private fun getPrivateKeyFromAssets(context: Context): java.security.PrivateKey {
        // Read the raw unencrypted PKCS#8 private key
        val keyBytes = context.assets.open("key.pk8").readBytes()
        val keySpec = java.security.spec.PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = java.security.KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }

    private fun getCertificatesFromAssets(context: Context): List<java.security.cert.X509Certificate> {
        // Read the raw X.509 certificate
        val certFactory = java.security.cert.CertificateFactory.getInstance("X.509")
        val cert = context.assets.open("cert.der").use {
            certFactory.generateCertificate(it) as java.security.cert.X509Certificate
        }
        return listOf(cert)
    }

    private fun installApkWithIntent(context: Context, apkFile: File) {
        try {
            // Using FileProvider. Ensure your provider authorities match exactly what's in your AndroidManifest.xml
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("WebAPK", "Failed to start install intent", e)
            Toast.makeText(context, "Failed to start APK installer.", Toast.LENGTH_SHORT).show()
        }
    }

    //endregion
    //region Download Logic
    val downloads =
        mutableStateListOf<DownloadItem>().apply { addAll(downloadTracker.loadDownloads()) }

    var pendingDownload: DownloadParams? = null
    private val lastPollData = mutableMapOf<Long, PollData>()


    private fun startDownloadPolling() {
        viewModelScope.launch(Dispatchers.IO) {
            val downloadManager =
                getApplication<Application>().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            while (isActive) { // Runs as long as ViewModel is alive
                val activeDownloads = downloads.filter {
                    it.status == DownloadStatus.RUNNING || it.status == DownloadStatus.PENDING
                }

                if (activeDownloads.isEmpty()) {
                    if (lastPollData.isNotEmpty()) lastPollData.clear()
                } else {
                    val currentTimeMs = System.currentTimeMillis()
                    var changed = false

                    activeDownloads.forEach { item ->
                        try {
                            val query = DownloadManager.Query().setFilterById(item.id)
                            downloadManager.query(query)?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    val statusIndex =
                                        cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                                    val downloadedBytesIndex =
                                        cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                                    val totalBytesIndex =
                                        cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                                    if (statusIndex != -1 && downloadedBytesIndex != -1 && totalBytesIndex != -1) {
                                        val downloadedBytes = cursor.getLong(downloadedBytesIndex)
                                        val totalBytes = cursor.getLong(totalBytesIndex)
                                        val statusInt = cursor.getInt(statusIndex)

                                        var speedBps = item.downloadSpeedBps
                                        var etrMs = item.timeRemainingMs

                                        // Calculate Speed
                                        val lastData = lastPollData[item.id]
                                        if (lastData != null) {
                                            if (downloadedBytes > lastData.bytesDownloaded) {
                                                val timeDeltaMs =
                                                    currentTimeMs - lastData.timestampMs
                                                val bytesDelta =
                                                    downloadedBytes - lastData.bytesDownloaded
                                                if (timeDeltaMs > 0) {
                                                    speedBps = (bytesDelta * 1000f) / timeDeltaMs
                                                    if (totalBytes > 0L) {
                                                        val bytesRemaining =
                                                            totalBytes - downloadedBytes
                                                        etrMs =
                                                            ((bytesRemaining / speedBps) * 1000).toLong()
                                                    }
                                                    lastPollData[item.id] = PollData(
                                                        currentTimeMs,
                                                        downloadedBytes,
                                                        speedBps
                                                    )
                                                }
                                            } else if ((currentTimeMs - lastData.timestampMs) > 2000) {
                                                speedBps = 0f
                                                etrMs = 0L
                                                lastPollData[item.id] =
                                                    lastData.copy(lastSpeedBps = 0f)
                                            }
                                        } else {
                                            lastPollData[item.id] =
                                                PollData(currentTimeMs, downloadedBytes)
                                        }

                                        val status = when (statusInt) {
                                            DownloadManager.STATUS_RUNNING -> DownloadStatus.RUNNING
                                            DownloadManager.STATUS_PAUSED -> DownloadStatus.PAUSED
                                            DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.SUCCESSFUL
                                            DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                                            else -> item.status
                                        }

                                        val progress =
                                            if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt() else 0
                                        val itemIndex = downloads.indexOfFirst { it.id == item.id }

                                        if (itemIndex != -1) {
                                            // Only update if something actually changed to avoid recomposition spam
                                            val oldItem = downloads[itemIndex]
                                            if (oldItem.progress != progress || oldItem.status != status || oldItem.downloadSpeedBps != speedBps) {
                                                downloads[itemIndex] = oldItem.copy(
                                                    status = status,
                                                    progress = progress,
                                                    downloadedBytes = downloadedBytes,
                                                    totalBytes = totalBytes
                                                ).apply {
                                                    this.downloadSpeedBps = speedBps
                                                    this.timeRemainingMs = etrMs
                                                }
                                                changed = true
                                            }
                                        }

                                        if (status != DownloadStatus.RUNNING && status != DownloadStatus.PENDING) {
                                            lastPollData.remove(item.id)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            //Log.e("Download", "Error querying download", e)
                        }
                    }
                    if (changed) {
                        downloadTracker.saveDownloads(downloads)
                    }
                }
                delay(500L)
            }
        }
    }

    fun performDownloadEnqueue(params: DownloadParams) {
        updateUI {
            it.copy(
                isUrlBarVisible = true, isDownloadPanelVisible = true,
                isTabsPanelVisible = false,
                isTabsPanelLock = false,
                isSettingsPanelVisible = false,
                isAppsPanelVisible = false,
                isFindInPageVisible = false,
                isNavPanelVisible = false,
                savedPanelState = null
            )
        }

        val context = getApplication<Application>()
        val initialFilename =
            getBestGuessFilename(params.url, params.contentDisposition, params.mimeType)
        val finalFilename = generateUniqueFilename(initialFilename, downloads)

        val request = DownloadManager.Request(params.url.toUri()).apply {
            setTitle(finalFilename)
            setDescription("Downloading file...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, finalFilename)
            addRequestHeader("User-Agent", params.userAgent)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        try {
            val downloadId = downloadManager.enqueue(request)
            val newDownload = DownloadItem(
                id = downloadId,
                url = params.url,
                filename = finalFilename,
                mimeType = params.mimeType ?: "application/octet-stream",
                status = DownloadStatus.PENDING
            )
            downloads.add(0, newDownload)
            downloadTracker.saveDownloads(downloads)
        } catch (e: Exception) {
            //Log.e("Download", "Failed to enqueue", e)
            // Ideally, emit a UI Event here to show a Toast
        }
    }

    fun deleteDownload(item: DownloadItem) {
        if (item.isBlobDownload) {
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, item.filename)
            if (file.exists()) {
                if (file.delete()) {
                    MediaScannerConnection.scanFile(
                        getApplication(),
                        arrayOf(file.absolutePath),
                        null,
                        null
                    )
                }
            }
        } else {
            val downloadManager =
                getApplication<Application>().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.remove(item.id)
        }
        downloads.remove(item)
        downloadTracker.saveDownloads(downloads)
    }

    fun clearDownloadList() {
        downloads.clear()
        downloadTracker.saveDownloads(downloads)
    }

    // Helper functions (moved from UI)
    private fun getBestGuessFilename(
        url: String,
        contentDisposition: String?,
        mimeType: String?
    ): String {
        if (contentDisposition != null) {
            val pattern =
                Pattern.compile("filename\\*?=['\"]?([^'\"\\s]+)['\"]?", Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(contentDisposition)
            if (matcher.find()) {
                val filename = matcher.group(1)
                if (filename != null) {
                    try {
                        return URLDecoder.decode(filename, "UTF-8")
                    } catch (_: Exception) {
                    }
                }
            }
        }
        try {
            val path = url.toUri().path
            if (path != null) {
                val lastSegment = path.substringAfterLast('/')
                if (lastSegment.isNotBlank()) return lastSegment
            }
        } catch (_: Exception) {
        }
        return URLUtil.guessFileName(url, contentDisposition, mimeType)
    }

    private fun generateUniqueFilename(
        initialName: String,
        existingDownloads: List<DownloadItem>
    ): String {
        val existingFilenames = existingDownloads.map { it.filename }.toSet()
        if (!existingFilenames.contains(initialName)) return initialName

        val baseName = initialName.substringBeforeLast('.')
        val extension = initialName.substringAfterLast('.', "")
        val finalExtension = if (extension.isNotEmpty()) ".$extension" else ""
        var counter = 1
        while (true) {
            val newName = "$baseName ($counter)$finalExtension"
            if (!existingFilenames.contains(newName)) return newName
            counter++
        }
    }
    //endregion
    //region Site Settings logic
    val siteSettings = mutableStateMapOf<String, SiteSettings>().apply {
        putAll(siteSettingsManager.loadSettings(activeProfileId.value))
    }
//    val togglePermission = { domain: String?, permission: String, isGranted: Boolean ->
//        if (domain != null) {
//            val currentSettings = siteSettings[domain] ?: SiteSettings(domain = domain)
//
//            // Toggle the boolean
//            val updatedDecisions = currentSettings.permissionDecisions.toMutableMap().apply {
//                this[permission] = !(this[permission] ?: false)
//            }
//
//            val newSettings = currentSettings.copy(permissionDecisions = updatedDecisions)
//
//            // Update memory map and save to disk
//            siteSettings[domain] = newSettings
//            siteSettingsManager.saveSettings(activeProfileId.value, siteSettings)
//        }
//    }

    val visitedUrlMap = mutableStateMapOf<String, String>().apply {
        putAll(visitedUrlManager.loadUrlMap(activeProfileId.value))
    }

    val addHistory = { url: String, title: String ->
        visitedUrlManager.addUrl(currentProfileId, url, title)
        if (title.isNotBlank()) {
            visitedUrlMap[url] = title
        }
    }

    val clearDomainData = { domain: String ->
        siteSettings.remove(domain)
        siteSettingsManager.saveSettings(currentProfileId, siteSettings)
    }

    //endregion
    //region Permission Logic
    val pendingPermissionRequest = mutableStateOf<CustomPermissionRequest?>(null)
    val pendingMediaPermissionRequest = mutableStateOf<CustomPermissionRequest?>(null)

    val savePermissionDecision = { domain: String, permissions: Map<String, Boolean> ->
        val currentSettings = siteSettings.getOrPut(domain) { SiteSettings(domain = domain) }
        val updatedDecisions = currentSettings.permissionDecisions.toMutableMap()

        // 1. Add all results from the system dialog
        updatedDecisions.putAll(permissions)

        // 2. Location Consolidation Logic
        if (updatedDecisions.containsKey(Manifest.permission.ACCESS_FINE_LOCATION) ||
            updatedDecisions.containsKey(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            val isGranted = updatedDecisions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    updatedDecisions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            updatedDecisions.remove(Manifest.permission.ACCESS_FINE_LOCATION)
            updatedDecisions.remove(Manifest.permission.ACCESS_COARSE_LOCATION)
            updatedDecisions[generic_location_permission] = isGranted
        }

       // 3. Update memory map and persist to disk
        val newSettings = currentSettings.copy(permissionDecisions = updatedDecisions)
        siteSettings[domain] = newSettings
        siteSettingsManager.saveSettings(currentProfileId, siteSettings)
    }

    val toggleSitePermission = { domain: String?, permission: String ->
        if (domain != null) {
            val currentSettings = siteSettings[domain] ?: SiteSettings(domain = domain)

            // Create a new map with the flipped boolean
            val updatedDecisions = currentSettings.permissionDecisions.toMutableMap().apply {
                this[permission] = !(this[permission] ?: false)
            }

            // Update state map and persist to disk
            val newSettings = currentSettings.copy(permissionDecisions = updatedDecisions)
            siteSettings[domain] = newSettings
            siteSettingsManager.saveSettings(currentProfileId, siteSettings)
        }
    }

    val denyCurrentPermissionRequest = {
        val request = pendingPermissionRequest.value
        if (request != null) {
            // 1. Identify domain
            val domain = siteSettingsManager.getDomain(request.origin)

            // 2. Map all requested permissions to FALSE
            if (domain != null) {
                val deniedPermissions = request.permissionsToRequest.associateWith { false }
                savePermissionDecision(domain, deniedPermissions)
            }

            // 3. Notify GeckoView that the request is finished (with empty result)
            request.onResult.invoke(emptyMap(), pendingPermissionRequest)
        }
    }

    val allowMediaPermissionRequest = { permissions: Map<String, Boolean> ->
        val request = pendingPermissionRequest.value
        if (request != null) {
            // 1. Identify domain and save the "Allow" decision
            siteSettingsManager.getDomain(request.origin)?.let { domain ->
                savePermissionDecision(domain, permissions)
            }

            // 2. Invoke the callback to tell GeckoView the result
            request.onResult.invoke(permissions, pendingPermissionRequest)

        }
    }
    //endregion
    //region Suggestions Logic
    val suggestions = mutableStateListOf<Suggestion>()
    fun fetchSuggestions(query: String, isPinning: Boolean) {
        val settings = _browserSettings.value
        val cleanQuery = query.trim()

        // 1. Guard clauses
        if (!settings.showSuggestions || cleanQuery.isBlank() || isPinning) {
            suggestions.clear()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val finalSuggestions = mutableListOf<Suggestion>()
            val addedHistoryUrls = mutableSetOf<String>()

            // A. Process Local History
            val historyMatches = visitedUrlMap.entries
                .filter { (url, title) ->
                    url.contains(cleanQuery, ignoreCase = true) || title.contains(
                        cleanQuery,
                        ignoreCase = true
                    )
                }
                .map { (url, title) ->
                    val rank = when {
                        url.startsWith(cleanQuery, ignoreCase = true) -> 1
                        title.startsWith(cleanQuery, ignoreCase = true) -> 2
                        url.contains(cleanQuery, ignoreCase = true) -> 3
                        else -> 4
                    }
                    Triple(
                        Suggestion(text = title, source = SuggestionSource.HISTORY, url = url),
                        rank,
                        url
                    )
                }
                .sortedBy { it.second }
                .map { it.first }

            finalSuggestions.addAll(historyMatches)
            addedHistoryUrls.addAll(historyMatches.map { it.url })

            // B. Fetch Search Engine Suggestions
            try {
                val searchEngine = SearchEngine.entries[settings.searchEngine]
                val encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8")
                val url = searchEngine.getSuggestionUrl(encodedQuery)

                val result = java.net.URL(url).readText(Charsets.UTF_8)
                val jsonArray = org.json.JSONArray(result)
                val suggestionsArray = jsonArray.getJSONArray(1)

                for (i in 0 until suggestionsArray.length()) {
                    val suggestionText = suggestionsArray.getString(i)
                    if (!addedHistoryUrls.contains(suggestionText)) {
                        finalSuggestions.add(
                            Suggestion(
                                text = suggestionText,
                                source = SuggestionSource.GOOGLE,
                                url = searchEngine.getSearchUrl(
                                    URLEncoder.encode(
                                        suggestionText,
                                        "UTF-8"
                                    )
                                )
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                //Log.e("Suggestions", "Network fetch failed", e)
            }

            // C. Update UI on Main Thread
            withContext(Dispatchers.Main) {
                // Ensure the query hasn't changed while we were fetching (simple verification)
                suggestions.clear()
                suggestions.addAll(finalSuggestions.take(10))
            }
        }

    }

  val removeSuggestionFromHistory = { suggestionToRemove: Suggestion ->
        if (suggestionToRemove.source == SuggestionSource.HISTORY) {
            visitedUrlManager.removeUrl(currentProfileId, suggestionToRemove.url)
            visitedUrlMap.remove(suggestionToRemove.url)
            suggestions.remove(suggestionToRemove)
        }
    }

    //endregion
    //region Single Purpose State
    val descriptionContent = mutableStateOf("")
    val resetBottomPanelTrigger = mutableStateOf(false)
    val isApplyImePaddingToWebView = mutableStateOf(true)
    val sessionRefreshTrigger = mutableIntStateOf(0)
    val isBackSquareInitialized = mutableStateOf(_browserSettings.value.backSquareOffsetX != -1f)
    val activeNavAction = mutableStateOf(GestureNavAction.REFRESH)
    val overlayHeightPx = mutableFloatStateOf(0f)
    val cursorPointerPosition = mutableStateOf(Offset.Zero)
    val screenSize = mutableStateOf(IntSize.Zero)
    val screenSizeDp = mutableStateOf(IntSize.Zero)

    val backgroundColor = mutableStateOf(Color.Black)


    //endregion
    //region JS/Complex State
    val jsDialogState = mutableStateOf<JsDialogState?>(null)
    val jsDialogDisplayState = mutableStateOf<JsDialogState?>(null)

    val choiceState = mutableStateOf<JsChoiceState?>(null)
    val choiceDisplayState = mutableStateOf<JsChoiceState?>(null)

    val colorState = mutableStateOf<JsColorState?>(null)
    val colorDisplayState = mutableStateOf<JsColorState?>(null)

    val dateTimeState = mutableStateOf<JsDateTimeState?>(null)
    val dateTimeDisplayState = mutableStateOf<JsDateTimeState?>(null)


    val contextMenuData = mutableStateOf<ContextMenuData?>(null)
    val contextMenuDisplayData = mutableStateOf<ContextMenuData?>(null)

    val findInPageText = mutableStateOf("")
    val findInPageResult = mutableStateOf(0 to 0)

    //endregion
    //region Confirmation logic
    val confirmationState = mutableStateOf<ConfirmationDialogState?>(null)
    val confirmationDisplayState = mutableStateOf<ConfirmationDialogState?>(null)

    //endregion
    //region Sorting logic
    val isSortingButtons = mutableStateOf(false)
    val inspectingOption = mutableStateOf<BrowserOption?>(null)
    val topVisibleOptionsPanelItem = mutableStateOf<BrowserOption?>(null)

    // --- SORTING LOGIC ---
    fun canMoveOptionLeft(option: BrowserOption, currentSettings: BrowserSettings): Boolean {
        val opts = currentSettings.optionsOrder.split(",").filter { it.isNotBlank() }
        val sets = currentSettings.settingsOrder.split(",").filter { it.isNotBlank() }

        if (opts.contains(option.name)) return opts.indexOf(option.name) > 0
        if (sets.contains(option.name)) {
            val idx = sets.indexOf(option.name)
            if (idx > 0) return true
            // Can jump from Settings to Options ONLY if it's a Toggle!
            return option.type == marcinlowercase.a.core.enum_class.OptionType.TOGGLE_ACTION
        }
        return false
    }

    fun canMoveOptionRight(option: BrowserOption, currentSettings: BrowserSettings): Boolean {
        val opts = currentSettings.optionsOrder.split(",").filter { it.isNotBlank() }
        val sets = currentSettings.settingsOrder.split(",").filter { it.isNotBlank() }

        if (sets.contains(option.name)) return sets.indexOf(option.name) < sets.lastIndex
        if (opts.contains(option.name)) {
            val idx = opts.indexOf(option.name)
            if (idx < opts.lastIndex) return true
            // Can jump from Options to Settings ONLY if it's a Toggle!
            return option.type == marcinlowercase.a.core.enum_class.OptionType.TOGGLE_ACTION
        }
        return false
    }

    fun isOptionHidden(option: BrowserOption, currentSettings: BrowserSettings): Boolean {
        return currentSettings.hiddenOptions.split(",").contains(option.name)
    }

    fun moveOptionLeft() {
        val option = inspectingOption.value ?: return
        if (!canMoveOptionLeft(option, _browserSettings.value)) return

        val opts = _browserSettings.value.optionsOrder.split(",").filter { it.isNotBlank() }
            .toMutableList()
        val sets = _browserSettings.value.settingsOrder.split(",").filter { it.isNotBlank() }
            .toMutableList()

        if (opts.contains(option.name)) {
            val idx = opts.indexOf(option.name)
            Collections.swap(opts, idx, idx - 1)
        } else if (sets.contains(option.name)) {
            val idx = sets.indexOf(option.name)
            if (idx > 0) {
                Collections.swap(sets, idx, idx - 1)
            } else {
                sets.removeAt(0)
                opts.add(option.name)
            }
        }
        updateField(BrowserSettingField.OPTIONS_ORDER, opts.joinToString(","))
        updateField(BrowserSettingField.SETTINGS_ORDER, sets.joinToString(","))
    }

    fun moveOptionRight() {
        val option = inspectingOption.value ?: return
        if (!canMoveOptionRight(option, _browserSettings.value)) return

        val opts = _browserSettings.value.optionsOrder.split(",").filter { it.isNotBlank() }
            .toMutableList()
        val sets = _browserSettings.value.settingsOrder.split(",").filter { it.isNotBlank() }
            .toMutableList()

        if (sets.contains(option.name)) {
            val idx = sets.indexOf(option.name)
            Collections.swap(sets, idx, idx + 1)
        } else if (opts.contains(option.name)) {
            val idx = opts.indexOf(option.name)
            if (idx < opts.lastIndex) {
                Collections.swap(opts, idx, idx + 1)
            } else {
                opts.removeAt(opts.lastIndex)
                sets.add(0, option.name)
            }
        }
        updateField(BrowserSettingField.OPTIONS_ORDER, opts.joinToString(","))
        updateField(BrowserSettingField.SETTINGS_ORDER, sets.joinToString(","))
    }

    fun toggleOptionVisibility() {
        val option = inspectingOption.value ?: return
        if (option == BrowserOption.SETTINGS ||
            option == BrowserOption.SORT_BUTTONS
        ) {
            return
        }
        val hidden = _browserSettings.value.hiddenOptions.split(",").filter { it.isNotBlank() }
            .toMutableSet()

        if (hidden.contains(option.name)) hidden.remove(option.name)
        else hidden.add(option.name)

        updateField(BrowserSettingField.HIDDEN_OPTIONS, hidden.joinToString(","))
    }

    //endregion
    init {
        geckoManager.setAdBlockEnabled(_browserSettings.value.isAdBlockEnabled)
        startDownloadPolling()
        setupPrefsListener(currentProfileId)
    }
}