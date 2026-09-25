package marcinlowercase.a.core.data_class

import marcinlowercase.a.core.enum_class.AppState
import marcinlowercase.a.core.enum_class.WindowMode

data class BrowserUIState(

    val appState: AppState = AppState.REGULAR,

    val isBuildPreview: Boolean = false,

    // Bottom Bar & General Overlay
    val isUrlBarVisible: Boolean = true,
    val isUrlOverlayBoxVisible: Boolean = true,
    val isBottomPanelVisible: Boolean = true,
    val isOtherPanelVisible: Boolean = false,

    // Specific Panels
    val isAppsPanelVisible: Boolean = false,
    val isSettingsPanelVisible: Boolean = false,
    val isDownloadPanelVisible: Boolean = false,
    val isFindInPageVisible: Boolean = false,
    val isNavPanelVisible: Boolean = false,
    val isPermissionPanelVisible: Boolean = false,
    val isPromptPanelVisible: Boolean = false,
    val isOptionsPanelVisible: Boolean = false,
    val isSyncPanelVisible: Boolean = false,


    // Tabs
    val isTabsPanelVisible: Boolean = false,
//    val isTabsPanelLock: Boolean = false,
    val isTabDataPanelVisible: Boolean = false,
    val inspectingTabId: Long? = null,

    // Media
    val isMediaControlPanelVisible: Boolean = false,
    val isMediaControlPanelDisplayed: Boolean = false, // Track if actually shown (fullscreen logic)
    val isOnFullscreenVideo: Boolean = false, // Track if actually shown (fullscreen logic)

    // Cursor / Input
    val isCursorMode: Boolean = false, // Controls isCursorPadVisible
    val isCursorPadVisible: Boolean = false,
    val isLongPressDrag: Boolean = false,


    // Text Field Focus State
    val isFocusOnTextField: Boolean = false,
    val isFocusOnUrlTextField: Boolean = false,
    val isFocusOnSettingTextField: Boolean = false,
    val isFocusOnFindTextField: Boolean = false,
    val isFocusOnProfileTextField: Boolean = false,
    val isFocusOnIconUrlTextField: Boolean = false,
    val isFocusOnBuildTextField: Boolean = false,
    val isPinningApp: Boolean = false,
    val isCreatingProfile: Boolean = false,
    val isRenamingProfile: Boolean = false,

    // Web Load
    val initialLoadDone: Boolean = false,
    val isLoading: Boolean = false,
    val isFirstLoadPWA: Boolean = true,

    // Screen Size / Orientation / Window Mode
    val windowMode: WindowMode = WindowMode.FULLSCREEN,
    val isLandscape: Boolean = false,
    val isLandscapeByButton: Boolean = false,
    val isSettingCornerRadius: Boolean = true,

    val optionsPanelHeightPx: Float = 0f,
    val appsPanelHeightPx: Float = 0f,
    val totalRevealHeightPx: Float = 0f,
    val isFullscreenPreview: Boolean = false,



    // State Restoration (used when focusing URL bar)
    val savedPanelState: PanelVisibilityState? = null,

    ) {
    fun isIndependentPanelVisible(): Boolean {
        return isFindInPageVisible || isPermissionPanelVisible || isPromptPanelVisible
    }

}