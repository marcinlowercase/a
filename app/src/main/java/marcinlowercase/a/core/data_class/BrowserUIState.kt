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
package marcinlowercase.a.core.data_class

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
    val isFocusOnIconUrlTextField: Boolean = false,
    val isPinningApp: Boolean = false,
    val isCloningBrowser: Boolean = false,
    val isCreatingProfile: Boolean = false,
    val isRenamingProfile: Boolean = false,


    // Web Load
    val initialLoadDone: Boolean = false,
    val isLoading: Boolean = false,
    val isFirstLoadPWA: Boolean = true,

    // Screen Size / Orientation
    val isLandscape: Boolean = false,
    val isLandscapeByButton: Boolean = false,
    val isSettingCornerRadius: Boolean = true,

    val optionsPanelHeightPx: Float = 0f,
    val appsPanelHeightPx: Float = 0f,
    val totalRevealHeightPx: Float = 0f,
    val isFullscreenPreview: Boolean = false,



    // State Restoration (used when focusing URL bar)
    val savedPanelState: PanelVisibilityState? = null,

    )