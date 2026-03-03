package marcinlowercase.a.ui.viewmodel

import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
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
import androidx.compose.ui.unit.IntSize
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import marcinlowercase.a.core.constant.default_url
import marcinlowercase.a.core.constant.generic_location_permission
import marcinlowercase.a.core.constant.pixel_9_corner_radius
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
import marcinlowercase.a.core.data_class.PanelVisibilityState
import marcinlowercase.a.core.data_class.PollData
import marcinlowercase.a.core.data_class.SiteSettings
import marcinlowercase.a.core.data_class.Suggestion
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.BrowserSettingField
import marcinlowercase.a.core.enum_class.DownloadStatus
import marcinlowercase.a.core.enum_class.GestureNavAction
import marcinlowercase.a.core.enum_class.SearchEngine
import marcinlowercase.a.core.enum_class.SuggestionSource
import marcinlowercase.a.core.enum_class.TabState
import marcinlowercase.a.core.manager.AppManager
import marcinlowercase.a.core.manager.BrowserDownloadManager
import marcinlowercase.a.core.manager.SiteSettingsManager
import marcinlowercase.a.core.manager.TabManager
import marcinlowercase.a.core.manager.VisitedUrlManager
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Collections
import java.util.regex.Pattern

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

    //endregion

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
                    Log.e("Intent", "Bad intent URI: $url", e)
                    return
                }
            } else {
                // Standard schemes (mailto, tel, market)
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // --- THE FIX: Try to launch immediately ---
            // Don't check resolveActivity() first. It returns null on Android 11+
            // unless you declare <queries> in Manifest. Just try to start it.
            try {
                activity.startActivity(intent)
                return // Success! We are done.
            } catch (e: ActivityNotFoundException) {
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
                        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pack"))
                        marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            activity.startActivity(marketIntent)
                        } catch (e: Exception) {
                            Log.e("Intent", "Play Store not found", e)
                        }
                    }
                }
            } else {
                Toast.makeText(activity, "No app found to open this link", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("Intent", "Failed to handle intent", e)
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
//                    Log.e("Intent", "Bad intent URI", e)
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
//            Log.e("Intent", "No app found for $url")
//            // Optional: Show a Toast "No app found to open this link"
//        } catch (e: Exception) {
//            Log.e("Intent", "Failed to launch external app", e)
//        }
//    }
    //endregion
    //region Browser Settings
    private val sharedPrefs = application.getSharedPreferences("BrowserPrefs", Context.MODE_PRIVATE)
    private val _browserSettings = MutableStateFlow(loadSettingsFromPrefs())
    val browserSettings = _browserSettings.asStateFlow()
    private fun loadSettingsFromPrefs(): BrowserSettings {
        return BrowserSettings(
            isFirstAppLoad = sharedPrefs.getBoolean("is_first_app_load", true),
            padding = sharedPrefs.getFloat("padding", 8f),
            deviceCornerRadius = sharedPrefs.getFloat(
                "device_corner_radius",
                pixel_9_corner_radius
            ),
            defaultUrl = sharedPrefs.getString("default_url", default_url) ?: default_url,
            animationSpeed = sharedPrefs.getFloat("animation_speed", 300f),
            singleLineHeight = sharedPrefs.getFloat("single_line_height", 100f),
            isSharpMode = sharedPrefs.getBoolean("is_sharp_mode", false),
            cursorContainerSize = sharedPrefs.getFloat("cursor_container_size", 50f),
            cursorPointerSize = sharedPrefs.getFloat("cursor_pointer_size", 5f),
            cursorTrackingSpeed = sharedPrefs.getFloat("cursor_tracking_speed", 1.75f),
            showSuggestions = sharedPrefs.getBoolean("show_suggestions", false),
            closedTabHistorySize = sharedPrefs.getFloat("closed_tab_history_size", 2f),
            backSquareOffsetX = sharedPrefs.getFloat("back_square_offset_x", -1f),
            backSquareOffsetY = sharedPrefs.getFloat("back_square_offset_y", -1f),
            backSquareIdleOpacity = sharedPrefs.getFloat("back_square_idle_opacity", 0.2f),
            maxListHeight = sharedPrefs.getFloat("max_list_height", 2.5f),
            searchEngine = sharedPrefs.getInt("search_engine", 0),
            isFullscreenMode = sharedPrefs.getBoolean("is_fullscreen_mode", false),
            highlightColor = sharedPrefs.getInt("highlight_color", 0xFFBA160C.toInt()),
            isAdBlockEnabled = sharedPrefs.getBoolean("is_ad_block_enabled", true),
            isGuideModeEnabled = sharedPrefs.getBoolean("is_guide_mode_enabled", true)

        )
    }

    fun updateSettings(mutation: (BrowserSettings) -> BrowserSettings) {
        _browserSettings.update(mutation)
        // Persist the resulting value after the update
        saveSettingsToPrefs(_browserSettings.value)
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

                    // Optional: Reload the current tab so the user sees the ads disappear/reappear instantly
                    activeTab?.let { tab ->
                        geckoManager.getSession(tab).reload()
                    }

                    current.copy(isAdBlockEnabled = isEnabled)
                }
                BrowserSettingField.GUIDE_MODE -> current.copy(isGuideModeEnabled = value as Boolean)


                BrowserSettingField.INFO -> current
            }
        }
    }

    fun resetSettings() {
        updateSettings {
            it.copy(
                padding = 8f,
                deviceCornerRadius = pixel_9_corner_radius,
                defaultUrl = default_url,
                animationSpeed = 300f,
                singleLineHeight = 100f,
                isSharpMode = false,
                cursorContainerSize = 50f,
                cursorPointerSize = 5f,
                cursorTrackingSpeed = 1.75f,
                backSquareIdleOpacity = 0.2f,
                highlightColor = 0xFFBA160C.toInt(),
            )
        }
    }

    private fun saveSettingsToPrefs(settings: BrowserSettings) {
        sharedPrefs.edit().apply {
            putBoolean("is_first_app_load", settings.isFirstAppLoad)
            putFloat("padding", settings.padding)
            putFloat("device_corner_radius", settings.deviceCornerRadius)
            putString("default_url", settings.defaultUrl)
            putFloat("animation_speed", settings.animationSpeed)
            putFloat("single_line_height", settings.singleLineHeight)
//            putInt("desktop_mode_width", settings.desktopModeWidth)
            putBoolean("is_sharp_mode", settings.isSharpMode)
            putFloat("cursor_container_size", settings.cursorContainerSize)
            putFloat("cursor_pointer_size", settings.cursorPointerSize)
            putFloat("cursor_tracking_speed", settings.cursorTrackingSpeed)
            putBoolean("show_suggestions", settings.showSuggestions)
            putFloat("closed_tab_history_size", settings.closedTabHistorySize)
            putFloat("back_square_offset_x", settings.backSquareOffsetX)
            putFloat("back_square_offset_y", settings.backSquareOffsetY)
            putFloat("back_square_idle_opacity", settings.backSquareIdleOpacity)
            putFloat("max_list_height", settings.maxListHeight)
            putInt("search_engine", settings.searchEngine)
            putBoolean("is_fullscreen_mode", settings.isFullscreenMode)
            putInt("highlight_color", settings.highlightColor)
            putBoolean("is_ad_block_enabled", settings.isAdBlockEnabled)
            putBoolean("is_guide_mode_enabled", settings.isGuideModeEnabled)


            apply()
        }
    }

    //endregion

    //region UI State
    private val _uiState = MutableStateFlow(
        BrowserUIState(
            isSettingsPanelVisible = sharedPrefs.getBoolean("is_first_app_load", true)
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

    /**
     * Helper to save current panel state before entering "Search Mode" (Focusing URL bar)
     */
    fun saveCurrentPanelState() {
        val current = _uiState.value
        val snapshot = PanelVisibilityState(
            options = false, // Assuming options panel isn't part of this main state yet
            tabs = current.isTabsPanelVisible,
            downloads = current.isDownloadPanelVisible,
            tabData = current.isTabDataPanelVisible,
            nav = current.isNavPanelVisible
        )
        updateUI { it.copy(savedPanelState = snapshot) }
    }

    /**
     * Helper to restore panel state (e.g. when clicking out of URL bar without searching)
     */
    fun restorePanelState() {
        val saved = _uiState.value.savedPanelState
        if (saved != null) {
            updateUI {
                it.copy(
                    isTabsPanelVisible = saved.tabs,
                    isDownloadPanelVisible = saved.downloads,
                    isTabDataPanelVisible = saved.tabData,
                    isNavPanelVisible = saved.nav,
                    savedPanelState = null // Clear after restore
                )
            }
        }
    }
    //endregion

    //region Tab logic

    val tabs = mutableStateListOf<Tab>()
    val recentlyClosedTabs = mutableStateListOf<Tab>()

    private val _activeTabIndex = MutableStateFlow(0)
    val activeTabIndex = _activeTabIndex.asStateFlow()

    val activeTab: Tab?
        get() = tabs.getOrNull(_activeTabIndex.value)
    private var isInitialized = false
    val initializeTabs = { initialUrl: String? ->
        if (!isInitialized) {
            // 1. Load tabs using the intent URL (if provided)
            val loadedTabs = tabManager.loadTabs(initialUrl)
            tabs.clear()
            tabs.addAll(loadedTabs)

            // 2. Set the active index
            _activeTabIndex.value = tabManager.getActiveTabIndex().coerceAtLeast(0)

            isInitialized = true
        }
    }


    val currentInspectingTab: Tab?
        get() = _uiState.value.inspectingTabId?.let { id ->
            tabs.find { it.id == id }
        }


    val selectTab = { newIndex: Int ->
        Log.d("TabFlow", "change to tab index $newIndex")
        val currentIndex = _activeTabIndex.value

        if (newIndex != currentIndex && newIndex in tabs.indices) {
            // 1. Reset loading state if switching away
            if (_uiState.value.isLoading) {
                updateUI { it.copy(isLoading = false) }
            }

            val oldTab = tabs[currentIndex]
            val newTab = tabs[newIndex]

            geckoManager.getSession(oldTab).setActive(false)

            geckoManager.getSession(newTab).setActive(true)


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
                    tabManager.clearAllTabs()

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
            tabManager.clearAllTabs()

            // Trigger UI callback to finish activity
            onExitApp()
        }
    }
    val createNewTab = { insertAtIndex: Int, url: String ->
        // 1. Deactivate current active tab
        val currentIndex = _activeTabIndex.value
        if (currentIndex in tabs.indices) {
            tabs[currentIndex] = tabs[currentIndex].copy(state = TabState.BACKGROUND)
        }

        // 2. Create the new Tab object
        val newTab = Tab(
            currentURL = url.ifBlank { _browserSettings.value.defaultUrl },
            state = TabState.ACTIVE
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
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index != -1) {
            val oldTab = tabs[index]
            val newTab = transform(oldTab)

            if (oldTab != newTab) {
                tabs[index] = newTab
                saveTabs()

                Log.e("TabFlow", "Memory Updated: $tabId | URL: ${newTab.currentURL}")
            }
        }
    }
    private var saveJob: Job? = null

    val saveTabs = {
        saveJob?.cancel()
        saveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500) // Wait for rapid events (like redirects) to finish
            tabManager.saveTabs(tabs.toList(), _activeTabIndex.value)
            Log.d("TabFlow", "Disk Save Complete")
        }
    }

    //endregion

    //region App/Pin Logic
    val apps = mutableStateListOf<App>().apply {
        addAll(appManager.loadApps())
    }
    val inspectingAppId = mutableLongStateOf(0L)

    fun pinApp(title: String, url: String, iconUrl: String) {
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
            Collections.swap(apps, fromIndex, toIndex)
            saveApps()
        }
    }

    private fun saveApps() {
        appManager.saveApps(apps)
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
                            Log.e("Download", "Error querying download", e)
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
            Log.e("Download", "Failed to enqueue", e)
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
        putAll(siteSettingsManager.loadSettings())
    }
    val togglePermission = { domain: String?, permission: String, isGranted: Boolean ->
        if (domain != null) {
            val currentSettings = siteSettings[domain] ?: SiteSettings(domain = domain)

            // Toggle the boolean
            val updatedDecisions = currentSettings.permissionDecisions.toMutableMap().apply {
                this[permission] = !(this[permission] ?: false)
            }

            val newSettings = currentSettings.copy(permissionDecisions = updatedDecisions)

            // Update memory map and save to disk
            siteSettings[domain] = newSettings
            siteSettingsManager.saveSettings(siteSettings)
        }
    }

    val visitedUrlMap = mutableStateMapOf<String, String>().apply {
        putAll(visitedUrlManager.loadUrlMap())
    }

    val addHistory = { url: String, title: String ->
        visitedUrlManager.addUrl(url, title)
        if (title.isNotBlank()) {
            visitedUrlMap[url] = title
        }
    }

    val clearDomainData = { domain: String ->
        siteSettings.remove(domain)
        siteSettingsManager.saveSettings(siteSettings)
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
        siteSettingsManager.saveSettings(siteSettings)
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
            siteSettingsManager.saveSettings(siteSettings)
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
                Log.e("Suggestions", "Network fetch failed", e)
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
            visitedUrlManager.removeUrl(suggestionToRemove.url)
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

    //region functions

    //endregion
    init {
        geckoManager.setAdBlockEnabled(_browserSettings.value.isAdBlockEnabled)
        startDownloadPolling()
    }
}