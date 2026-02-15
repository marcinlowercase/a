package marcinlowercase.a.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import marcinlowercase.a.CustomApplication
import marcinlowercase.a.core.constant.default_url
import marcinlowercase.a.core.constant.pixel_9_corner_radius
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.ActivePanel
import marcinlowercase.a.core.enum_class.TabState
import marcinlowercase.a.core.manager.AppManager
import marcinlowercase.a.core.manager.BrowserDownloadManager
import marcinlowercase.a.core.manager.GeckoManager
import marcinlowercase.a.core.manager.SiteSettingsManager
import marcinlowercase.a.core.manager.TabManager
import marcinlowercase.a.core.manager.VisitedUrlManager
import marcinlowercase.a.ui.state.BrowserUiState

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    // --- 1. MANAGERS (The Model) ---
    // We initialize them here so MainActivity doesn't have to.

    val tabManager = TabManager(application)
    val geckoManager: GeckoManager = (application as CustomApplication).geckoManager
    val siteSettingsManager = SiteSettingsManager(application)
    val visitedUrlManager = VisitedUrlManager(application)
    val downloadManager = BrowserDownloadManager(application)
    val appManager = AppManager(application)

    private val sharedPrefs = application.getSharedPreferences("BrowserPrefs", Context.MODE_PRIVATE)

    // --- 2. STATES (The Source of Truth) ---

    // A. The Big UI State (Replaces 20+ booleans)
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState = _uiState.asStateFlow()

    // B. Browser Settings
    private val _browserSettings = MutableStateFlow(loadSettingsFromPrefs())
    val browserSettings = _browserSettings.asStateFlow()

    // C. Tabs
    private val _tabs = MutableStateFlow<List<Tab>>(emptyList())
    val tabs = _tabs.asStateFlow()

    private val _activeTabIndex = MutableStateFlow(0)
    val activeTabIndex = _activeTabIndex.asStateFlow()

    // Derived: Current Active Tab Object
    val activeTab: StateFlow<Tab?> = combine(_tabs, _activeTabIndex) { tabList, index ->
        if (tabList.isNotEmpty() && index in tabList.indices) tabList[index] else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- 3. INIT ---
    init {
        // Load initial data
        loadTabs(null)
    }

    // --- 4. BASIC HELPERS ---

    private fun loadTabs(intentUrl: String?) {
        val loaded = tabManager.loadTabs(intentUrl)
        _tabs.value = loaded
        _activeTabIndex.value = tabManager.getActiveTabIndex().coerceIn(0, loaded.lastIndex)

        // Update UI state with the current URL
        if (loaded.isNotEmpty()) {
            val idx = _activeTabIndex.value
            _uiState.update { it.copy(urlBarText = loaded[idx].currentURL) }
        }
    }

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

    // --- TAB LOGIC ---

    fun createNewTab(url: String = _browserSettings.value.defaultUrl, insertAtIndex: Int? = null) {
        val currentList = _tabs.value.toMutableList()
        val currentIndex = _activeTabIndex.value

        // 1. Deactivate current tab (set state to BACKGROUND)
        if (currentIndex in currentList.indices) {
            currentList[currentIndex] = currentList[currentIndex].copy(state = TabState.BACKGROUND)
        }

        // 2. Create the new tab
        val newTab = Tab(
            currentURL = url,
            state = TabState.ACTIVE,
            // ID is generated automatically in Tab data class, or you can pass System.currentTimeMillis()
        )

        // 3. Determine where to insert
        val targetIndex = insertAtIndex ?: (currentIndex + 1)
        val safeIndex = targetIndex.coerceIn(0, currentList.size)
        currentList.add(safeIndex, newTab)

        // 4. Initialize Gecko Session (Critical!)
        // We do this here so the session exists before the View tries to render it
        geckoManager.getSession(newTab)

        // 5. Update State
        _tabs.value = currentList
        _activeTabIndex.value = safeIndex

        // Update URL bar text to match new tab
        _uiState.update { it.copy(urlBarText = url) }

        // 6. Save to Disk
        saveTabsToDisk()
    }

    fun closeTab(tabId: Long) {
        val currentList = _tabs.value.toMutableList()
        val tabToDelete = currentList.find { it.id == tabId } ?: return
        val indexToDelete = currentList.indexOf(tabToDelete)

        // 1. Tell Gecko to kill the session
        geckoManager.closeSession(tabToDelete)

        // 2. Remove from list
        currentList.removeAt(indexToDelete)

        // 3. Handle Empty State (Close App?)
        if (currentList.isEmpty()) {
            _tabs.value = emptyList()
            tabManager.clearAllTabs()
            // We need a way to tell the Activity to finish.
            // For now, we leave the list empty. The UI will see empty list and can exit.
            return
        }

        // 4. Calculate new Active Index
        var newIndex = _activeTabIndex.value
        if (indexToDelete == newIndex) {
            // We closed the active tab. Go to previous (or stay at 0)
            newIndex = (indexToDelete - 1).coerceAtLeast(0)
            currentList[newIndex] = currentList[newIndex].copy(state = TabState.ACTIVE)
        } else if (indexToDelete < newIndex) {
            // We closed a tab "to the left", so our index shifts down by 1
            newIndex -= 1
        }

        // 5. Update State
        _tabs.value = currentList
        _activeTabIndex.value = newIndex

        // Update URL bar text
        val newActiveUrl = currentList[newIndex].currentURL
        _uiState.update { it.copy(urlBarText = newActiveUrl) }

        saveTabsToDisk()
    }

    fun setActiveTab(index: Int) {
        if (index == _activeTabIndex.value) return
        val currentList = _tabs.value.toMutableList()

        // Deactivate old
        val oldIndex = _activeTabIndex.value
        if (oldIndex in currentList.indices) {
            currentList[oldIndex] = currentList[oldIndex].copy(state = TabState.BACKGROUND)
        }

        // Activate new
        if (index in currentList.indices) {
            currentList[index] = currentList[index].copy(state = TabState.ACTIVE)

            // Update URL bar
            val newUrl = currentList[index].currentURL
            _uiState.update { it.copy(urlBarText = newUrl) }
        }

        _tabs.value = currentList
        _activeTabIndex.value = index
        saveTabsToDisk()
    }

    private fun saveTabsToDisk() {
        tabManager.saveTabs(_tabs.value, _activeTabIndex.value)
    }


    // --- PANEL TRAFFIC CONTROL ---

    fun setUrlBarVisible(visible: Boolean) {
        _uiState.update {
            it.copy(
                isUrlBarVisible = visible,
                // If URL bar hides, usually the bottom panel hides too
                isBottomPanelVisible = visible,
                // If URL bar hides, clear any active panel overlay
                activePanel = if (!visible) null else it.activePanel
            )
        }
    }

    fun togglePanel(panel: ActivePanel) {
        _uiState.update { currentState ->
            if (currentState.activePanel == panel) {
                // If clicking the same panel, close it
                currentState.copy(activePanel = null)
            } else {
                // Open new panel, ensure bottom bar is visible
                currentState.copy(
                    activePanel = panel,
                    isBottomPanelVisible = true
                )
            }
        }
    }

    fun closeAllPanels() {
        _uiState.update { it.copy(activePanel = null) }
    }

    // Called when user types in URL bar
    fun onUrlTextChange(text: String) {
        _uiState.update { it.copy(urlBarText = text) }
    }

    // --- SETTINGS LOGIC ---

    fun updateSettings(newSettings: BrowserSettings) {
        _browserSettings.value = newSettings
        saveSettingsToPrefs(newSettings)
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
            putBoolean("is_first_app_load", browserSettings.value.isFirstAppLoad)
            putFloat("padding", browserSettings.value.padding)
            putFloat("device_corner_radius", browserSettings.value.deviceCornerRadius)
            putString("default_url", browserSettings.value.defaultUrl)
            putFloat("animation_speed", browserSettings.value.animationSpeed)
            putFloat("single_line_height", browserSettings.value.singleLineHeight)
//            putInt("desktop_mode_width", browserSettings.value.desktopModeWidth)
            putBoolean("is_sharp_mode", browserSettings.value.isSharpMode)
//            putFloat("top_sharp_edge", browserSettings.value.topSharpEdge)
//            putFloat("bottom_sharp_edge", browserSettings.value.bottomSharpEdge)
            putFloat("cursor_container_size", browserSettings.value.cursorContainerSize)
            putFloat("cursor_pointer_size", browserSettings.value.cursorPointerSize)
            putFloat("cursor_tracking_speed", browserSettings.value.cursorTrackingSpeed)
            putBoolean("show_suggestions", browserSettings.value.showSuggestions)
            putFloat("closed_tab_history_size", browserSettings.value.closedTabHistorySize)
            putFloat("back_square_offset_x", browserSettings.value.backSquareOffsetX)
            putFloat("back_square_offset_y", browserSettings.value.backSquareOffsetY)
            putFloat("back_square_idle_opacity", browserSettings.value.backSquareIdleOpacity)
            putFloat("max_list_height", browserSettings.value.maxListHeight)
            putInt("search_engine", browserSettings.value.searchEngine)
            putBoolean("is_fullscreen_mode", browserSettings.value.isFullscreenMode)
            putInt("highlight_color", browserSettings.value.highlightColor)

            apply()
        }
    }

    // --- PROMPT HANDLING ---

    fun onJsAlert(message: String) {
        _uiState.update { it.copy(jsDialogState = marcinlowercase.a.core.data_class.JsAlert(message)) }
    }

    fun onJsConfirm(message: String, callback: (Boolean) -> Unit) {
        _uiState.update { it.copy(jsDialogState = marcinlowercase.a.core.data_class.JsConfirm(message, callback)) }
    }

    fun dismissPrompt() {
        _uiState.update {
            it.copy(
                jsDialogState = null,
                choiceState = null,
                colorState = null,
                dateTimeState = null,
                contextMenuData = null
            )
        }
    }

}