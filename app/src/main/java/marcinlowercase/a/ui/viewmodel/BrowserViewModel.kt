package marcinlowercase.a.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import marcinlowercase.a.CustomApplication
import marcinlowercase.a.core.constant.default_url
import marcinlowercase.a.core.constant.pixel_9_corner_radius
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.BrowserUIState
import marcinlowercase.a.core.data_class.PanelVisibilityState
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.BrowserSettingField
import marcinlowercase.a.core.enum_class.TabState
import marcinlowercase.a.core.manager.TabManager

val LocalBrowserViewModel = staticCompositionLocalOf<BrowserViewModel> {
    error("No BrowserViewModel provided! Check your root Composable.")
}
class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    //region Browser Settings
    private val sharedPrefs = application.getSharedPreferences("BrowserPrefs", Context.MODE_PRIVATE)
    private val _browserSettings = MutableStateFlow(loadSettingsFromPrefs())
    val browserSettings = _browserSettings.asStateFlow()
    private fun loadSettingsFromPrefs(): BrowserSettings {
        return BrowserSettings(
            isFirstAppLoad = sharedPrefs.getBoolean("is_first_app_load", true),
            padding = sharedPrefs.getFloat("padding", 8f),
            deviceCornerRadius = sharedPrefs.getFloat("device_corner_radius", pixel_9_corner_radius),
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
            highlightColor = sharedPrefs.getInt("highlight_color", 0xFFFFFF00.toInt())
        )
    }
    fun updateSettings(newSettings: BrowserSettings) {
        _browserSettings.value = newSettings
        saveSettingsToPrefs(newSettings)
    }

    fun updateField(field: BrowserSettingField, value: Any) {
        val current = _browserSettings.value
        val next = when (field) {
            BrowserSettingField.CORNER_RADIUS ->
                current.copy(deviceCornerRadius = value as Float)

            BrowserSettingField.PADDING ->
                current.copy(padding = value as Float)

            BrowserSettingField.ANIMATION_SPEED ->
                current.copy(animationSpeed = value as Float)

            BrowserSettingField.CURSOR_CONTAINER_SIZE ->
                current.copy(cursorContainerSize = value as Float)

            BrowserSettingField.CURSOR_TRACKING_SPEED ->
                current.copy(cursorTrackingSpeed = value as Float)

            BrowserSettingField.BACK_SQUARE_OPACITY ->
                current.copy(backSquareIdleOpacity = value as Float)

            BrowserSettingField.DEFAULT_URL ->
                current.copy(defaultUrl = value as String)

            BrowserSettingField.CLOSED_TAB_HISTORY_SIZE ->
                current.copy(closedTabHistorySize = value as Float)

            BrowserSettingField.MAX_LIST_HEIGHT ->
                current.copy(maxListHeight = value as Float)

            BrowserSettingField.SINGLE_LINE_HEIGHT ->
                current.copy(singleLineHeight = value as Float)

            BrowserSettingField.SEARCH_ENGINE -> {
                // Converts Float from slider to Int for the data class
                val index = when(value) {
                    is Float -> value.toInt()
                    is Int -> value
                    else -> current.searchEngine
                }
                current.copy(searchEngine = index)
            }

            BrowserSettingField.HIGHLIGHT_COLOR -> {
                // Handles both Int and Long (hex) color values
                val color = when(value) {
                    is Int -> value
                    is Long -> value.toInt()
                    else -> current.highlightColor
                }
                current.copy(highlightColor = color)
            }

            BrowserSettingField.INFO -> current // "Info" is read-only, return current state
        }

        // Calls your existing updateSettings function to trigger StateFlow update and Save
        updateSettings(next)
    }

    fun resetSettings() {
        // Create default settings object (copied from your Activity logic)
        val defaults = _browserSettings.value.copy(
            padding = 8f,
            deviceCornerRadius = pixel_9_corner_radius,
            defaultUrl = default_url,
            animationSpeed = 300f,
            singleLineHeight = 100f,
            isSharpMode = false,
            cursorContainerSize = 50f,
            cursorPointerSize = 5f,
            cursorTrackingSpeed = 1.75f,
            backSquareIdleOpacity = 0.2f
        )
        updateSettings(defaults)
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

            apply()
        }
    }

    //endregion

    //region UI State
    private val _uiState = MutableStateFlow(BrowserUIState(
        isSettingsPanelVisible = sharedPrefs.getBoolean("is_first_app_load", true)
    ))
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
    val tabManager = TabManager(application)
    val geckoManager = (application as CustomApplication).geckoManager

    val tabs = mutableStateListOf<Tab>()
    val recentlyClosedTabs = mutableStateListOf<Tab>()

    private val _activeTabIndex = MutableStateFlow(0)
    val activeTabIndex = _activeTabIndex.asStateFlow()

    val activeTab: Tab?
        get() = tabs.getOrNull(_activeTabIndex.value)
    init {
        // Load initial tabs
        val loadedTabs = tabManager.loadTabs(null)
        tabs.addAll(loadedTabs)
        _activeTabIndex.value = tabManager.getActiveTabIndex().coerceAtLeast(0)
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
    val duplicateInspectedTab =  {
        val originalTab = currentInspectingTab

        if(originalTab != null){
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

            if (indexToClose != -1){
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
                        val nextTabIndex =
                            if (indexToClose >= tabs.size) tabs.lastIndex else indexToClose
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
            val nextTabIndex = if (indexToRemove >= tabs.size) tabs.lastIndex else indexToRemove

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

            // Only update and save if the transformation actually changed the data
            if (oldTab != newTab) {
                tabs[index] = newTab
                saveTabs()
            }
        }
    }

    fun saveTabs() {
        tabManager.saveTabs(tabs, _activeTabIndex.value)
    }

    //endregion

}