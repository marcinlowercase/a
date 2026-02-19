package marcinlowercase.a.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import marcinlowercase.a.core.constant.default_url
import marcinlowercase.a.core.constant.pixel_9_corner_radius
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.BrowserUIState
import marcinlowercase.a.core.data_class.PanelVisibilityState
import marcinlowercase.a.core.enum_class.BrowserSettingField

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
        isSettingsPanelVisible = sharedPrefs.getBoolean("is_first_app_load", true),
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

}