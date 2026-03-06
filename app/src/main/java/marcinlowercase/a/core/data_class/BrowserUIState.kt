package marcinlowercase.a.core.data_class

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class BrowserUIState(
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

    // Tabs
    val isTabsPanelVisible: Boolean = false,
    val isTabsPanelLock: Boolean = false,
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
    val isPinningApp: Boolean = false,


    // Web Load
    val initialLoadDone: Boolean = false,
    val isLoading: Boolean = false,

    // Screen Size / Orientation
    val isLandscape: Boolean = false,
    val isLandscapeByButton: Boolean = false,
    val isSettingCornerRadius: Boolean = true,

    val optionsPanelHeightPx: Float = 0f,
    val appsPanelHeightPx: Float = 0f,
    val totalRevealHeightPx: Float = 0f,



    // State Restoration (used when focusing URL bar)
    val savedPanelState: PanelVisibilityState? = null,

    )