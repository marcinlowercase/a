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
package marcinlowercase.a.ui.panel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.delay
import marcinlowercase.a.R
import marcinlowercase.a.core.constant.privacy_policy_url
import marcinlowercase.a.core.data_class.OptionItem
import marcinlowercase.a.core.enum_class.BrowserOption
import marcinlowercase.a.core.enum_class.BrowserSettingField
import marcinlowercase.a.core.enum_class.SearchEngine
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith


enum class SettingPanelView {
    MAIN, CORNER_RADIUS, PADDING, ANIMATION_SPEED, CURSOR_CONTAINER_SIZE,
    CURSOR_TRACKING_SPEED, BACK_SQUARE_OPACITY, DEFAULT_URL, INFO,
    CLOSED_TAB_HISTORY_SIZE, MAX_LIST_HEIGHT, SEARCH_ENGINE,
    SINGLE_LINE_HEIGHT, HIGHLIGHT_COLOR,MEMORY_USAGE
}

// --- MASTER REGISTRY OF ALL BUTTONS ---
@Composable
fun rememberBrowserOptionsRegistry(
    onCloseAllTabs: () -> Unit,
    onNavigateToSetting: (SettingPanelView) -> Unit,
    confirmationPopup: (Int, String, () -> Unit, () -> Unit) -> Unit,
    changeBrowserIcon: () -> Unit,
    onLoginClick: () -> Unit,
): Map<BrowserOption, OptionItem> {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    return remember(
        uiState.value,
        settings.value,
        viewModel.recentlyClosedTabs.size,
        viewModel.isSortingButtons.value
    ) {
        mapOf(
            // --- Options Panel Exclusives ---
            BrowserOption.TABS_PANEL to OptionItem(
                id = BrowserOption.TABS_PANEL,
                iconRes = R.drawable.ic_tabs,
                contentDescription = R.string.desc_tabs_panel,
                enabled = uiState.value.isTabsPanelLock
            ) {
                viewModel.updateUI {
                    it.copy(
                        isTabsPanelVisible = !it.isTabsPanelVisible,
                        isTabsPanelLock = !it.isTabsPanelLock
                    )
                }
                if (!uiState.value.isAppsPanelVisible) viewModel.updateUI {
                    it.copy(
                        isOptionsPanelVisible = false,
                        isAppsPanelVisible = false
                    )
                }
            },
            BrowserOption.FIND_IN_PAGE to OptionItem(
                id = BrowserOption.FIND_IN_PAGE,
                iconRes = R.drawable.ic_find_in_page,
                contentDescription = R.string.desc_find_in_page,
                enabled = uiState.value.isFindInPageVisible
            ) {
                viewModel.updateUI {
                    it.copy(
                        isFindInPageVisible = !it.isFindInPageVisible,
                        isOptionsPanelVisible = false,
                        isAppsPanelVisible = false
                    )
                }
            },
            BrowserOption.DOWNLOAD_PANEL to OptionItem(
                id = BrowserOption.DOWNLOAD_PANEL,
                iconRes = R.drawable.ic_download,
                contentDescription = R.string.desc_download_panel,
                enabled = uiState.value.isDownloadPanelVisible
            ) {
                viewModel.updateUI {
                    it.copy(
                        isDownloadPanelVisible = !it.isDownloadPanelVisible,
                        isOptionsPanelVisible = false,
                        isAppsPanelVisible = false
                    )
                }
            },
            BrowserOption.SETTINGS to OptionItem(
                id = BrowserOption.SETTINGS,
                iconRes = R.drawable.ic_settings,
                contentDescription = R.string.desc_settings,
                enabled = uiState.value.isSettingsPanelVisible
            ) {
                viewModel.updateUI {
                    it.copy(
                        isSettingsPanelVisible = !it.isSettingsPanelVisible,
                        isOptionsPanelVisible = false,
                        isAppsPanelVisible = false
                    )
                }
            },

            // --- Settings Panel Exclusives ---
            BrowserOption.SEARCH_ENGINE to OptionItem(
                id = BrowserOption.SEARCH_ENGINE,
                iconRes = R.drawable.ic_search,
                contentDescription = R.string.desc_search_engine
            ) { onNavigateToSetting(SettingPanelView.SEARCH_ENGINE) },
            BrowserOption.DEFAULT_URL to OptionItem(
                id = BrowserOption.DEFAULT_URL,
                iconRes = R.drawable.ic_link,
                contentDescription = R.string.desc_default_url
            ) { onNavigateToSetting(SettingPanelView.DEFAULT_URL) },
            BrowserOption.HISTORY_SIZE to OptionItem(
                id = BrowserOption.HISTORY_SIZE,
                iconRes = R.drawable.ic_manage_history,
                contentDescription = R.string.desc_history_size
            ) { onNavigateToSetting(SettingPanelView.CLOSED_TAB_HISTORY_SIZE) },
            BrowserOption.CORNER_RADIUS to OptionItem(
                id = BrowserOption.CORNER_RADIUS,
                iconRes = R.drawable.ic_adjust_corner_radius,
                contentDescription = R.string.desc_corner_radius
            ) { onNavigateToSetting(SettingPanelView.CORNER_RADIUS) },
            BrowserOption.PADDING to OptionItem(
                id = BrowserOption.PADDING,
                iconRes = R.drawable.ic_padding,
                contentDescription = R.string.desc_padding
            ) { onNavigateToSetting(SettingPanelView.PADDING) },
            BrowserOption.SINGLE_LINE_HEIGHT to OptionItem(
                id = BrowserOption.SINGLE_LINE_HEIGHT,
                iconRes = R.drawable.ic_expand,
                contentDescription = R.string.desc_min_height
            ) { onNavigateToSetting(SettingPanelView.SINGLE_LINE_HEIGHT) },
            BrowserOption.MAX_LIST_HEIGHT to OptionItem(
                id = BrowserOption.MAX_LIST_HEIGHT,
                iconRes = R.drawable.ic_max_list_height,
                contentDescription = R.string.desc_max_list_height
            ) { onNavigateToSetting(SettingPanelView.MAX_LIST_HEIGHT) },
            BrowserOption.HIGHLIGHT_COLOR to OptionItem(
                id = BrowserOption.HIGHLIGHT_COLOR,
                iconRes = R.drawable.ic_colors,
                contentDescription = R.string.desc_highlight_color
            ) { onNavigateToSetting(SettingPanelView.HIGHLIGHT_COLOR) },
            BrowserOption.ANIMATION_SPEED to OptionItem(
                id = BrowserOption.ANIMATION_SPEED,
                iconRes = R.drawable.ic_animation,
                contentDescription = R.string.desc_animation_speed
            ) { onNavigateToSetting(SettingPanelView.ANIMATION_SPEED) },
            BrowserOption.CURSOR_SIZE to OptionItem(
                id = BrowserOption.CURSOR_SIZE,
                iconRes = R.drawable.ic_cursor_size,
                contentDescription = R.string.desc_cursor_size
            ) { onNavigateToSetting(SettingPanelView.CURSOR_CONTAINER_SIZE) },
            BrowserOption.CURSOR_SPEED to OptionItem(
                id = BrowserOption.CURSOR_SPEED,
                iconRes = R.drawable.ic_cursor_speed,
                contentDescription = R.string.desc_cursor_speed
            ) { onNavigateToSetting(SettingPanelView.CURSOR_TRACKING_SPEED) },
            BrowserOption.BACK_SQUARE_OPACITY to OptionItem(
                id = BrowserOption.BACK_SQUARE_OPACITY,
                iconRes = R.drawable.ic_opacity,
                contentDescription = R.string.desc_back_square_opacity
            ) { onNavigateToSetting(SettingPanelView.BACK_SQUARE_OPACITY) },
            BrowserOption.PRIVACY_POLICY to OptionItem(
                id = BrowserOption.PRIVACY_POLICY,
                iconRes = R.drawable.ic_developer_guide,
                contentDescription = R.string.desc_privacy_policy
            ) {
                viewModel.createNewTab(viewModel.activeTabIndex.value + 1, privacy_policy_url)
                viewModel.updateUI {
                    it.copy(
                        isOptionsPanelVisible = false,
                        isAppsPanelVisible = false
                    )
                }
            },
            BrowserOption.CHANGE_ICON to OptionItem(
                id = BrowserOption.CHANGE_ICON,
                iconRes = R.drawable.ic_empty_logo,
                contentDescription = R.string.desc_change_icon
            ) {
                changeBrowserIcon()
            },
            BrowserOption.CONFIRMATION to OptionItem(
                id = BrowserOption.CONFIRMATION,
                iconRes = R.drawable.ic_warning,
                enabled = settings.value.isEnabledConfirmation,
                contentDescription = R.string.desc_disable_confirmation
            ) {
                if (settings.value.isEnabledConfirmation) {
                    confirmationPopup(
                        R.string.confirm_disable_confirmation,
                        "",
                        { viewModel.updateSettings { it.copy(isEnabledConfirmation = false) } },
                        {}
                    )
                } else {
                    viewModel.updateSettings { it.copy(isEnabledConfirmation = true) }
                }
            },
            BrowserOption.BACKGROUND_PLAYBACK to OptionItem(
                id = BrowserOption.BACKGROUND_PLAYBACK,
                iconRes = R.drawable.ic_background_playback,
                contentDescription = R.string.desc_background_playback,
                enabled = settings.value.isEnabledBackgroundPlayback
            ) {
                viewModel.updateSettings { it.copy(isEnabledBackgroundPlayback = !it.isEnabledBackgroundPlayback) }
            },
            BrowserOption.MATERIAL_YOU to OptionItem(
                id = BrowserOption.MATERIAL_YOU,
                iconRes = R.drawable.ic_material_design,
                contentDescription = R.string.desc_material_you,
                enabled = settings.value.isEnabledMaterialYou
            ) {
                viewModel.updateSettings { it.copy(isEnabledMaterialYou = !it.isEnabledMaterialYou) }
            },

            BrowserOption.SYNC to OptionItem(
                id = BrowserOption.SYNC,
                iconRes = R.drawable.ic_sync,
                contentDescription = R.string.desc_sync, // Make sure to add to strings.xml
                enabled = uiState.value.isSyncPanelVisible// Disable click if not logged in
            ) {
                if (viewModel.isLoggedIn()) {
                    viewModel.updateUI {
                        it.copy(isSyncPanelVisible = !it.isSyncPanelVisible, isOptionsPanelVisible = false, isSettingsPanelVisible = false)
                    }
//                    viewModel.updateSettings { it.copy(isSync = !it.isSync) }
                } else {
                    onLoginClick()
                }
            },

            BrowserOption.RESET_SETTINGS to OptionItem(
                id = BrowserOption.RESET_SETTINGS,
                iconRes = R.drawable.ic_reset_settings,
                contentDescription = R.string.desc_reset_settings
            ) {
                confirmationPopup(R.string.confirm_reset_setting, "", { viewModel.resetSettings() }, {})
            },
            BrowserOption.SORT_BUTTONS to OptionItem(
                id = BrowserOption.SORT_BUTTONS,
                iconRes = R.drawable.ic_swap_calls,
                contentDescription = R.string.desc_sort_buttons,
                enabled = viewModel.isSortingButtons.value
            ) {
                viewModel.isSortingButtons.value = !viewModel.isSortingButtons.value
                if (viewModel.isSortingButtons.value) {
                    viewModel.inspectingOption.value = BrowserOption.SETTINGS

                    viewModel.updateUI { it.copy(isAppsPanelVisible = true) }
                } else {
                    viewModel.inspectingOption.value = null
                }
            },

            // --- Toggles (Movable) ---
            BrowserOption.SHARP_MODE to OptionItem(
                id = BrowserOption.SHARP_MODE,
                iconRes = if (settings.value.isSharpMode) R.drawable.ic_rounded_corner else R.drawable.ic_sharp_corner,
                contentDescription = R.string.desc_sharp_mode,
                enabled = settings.value.isSharpMode
            ) {
                viewModel.updateSettings { it.copy(isSharpMode = !it.isSharpMode) }; viewModel.updateUI {
                it.copy(
                    isOptionsPanelVisible = false,
                    isAppsPanelVisible = false
                )
            }
            },
            BrowserOption.REOPEN_TAB to OptionItem(
                id = BrowserOption.REOPEN_TAB,
                iconRes = R.drawable.ic_reopen_window,
                contentDescription = R.string.desc_reopen_tab,
                enabled = viewModel.recentlyClosedTabs.isNotEmpty()
            ) {
                viewModel.reopenClosedTab(); viewModel.updateUI {
                it.copy(
                    isOptionsPanelVisible = false,
                    isAppsPanelVisible = false
                )
            }
            },
            BrowserOption.FULLSCREEN to OptionItem(
                id = BrowserOption.FULLSCREEN,
                iconRes = R.drawable.ic_fullscreen,
                contentDescription = R.string.desc_fullscreen_mode,
                enabled = settings.value.isFullscreenMode
            ) { viewModel.updateSettings { it.copy(isFullscreenMode = !it.isFullscreenMode) } },
            BrowserOption.DESKTOP_MODE to OptionItem(
                id = BrowserOption.DESKTOP_MODE,
                iconRes = if (settings.value.isDesktopMode) R.drawable.ic_computer else R.drawable.ic_mobile_3,
                contentDescription = R.string.desc_desktop_mode,
                enabled = settings.value.isDesktopMode
            ) {
                viewModel.updateSettings { it.copy(isDesktopMode = !it.isDesktopMode) }; viewModel.updateUI {
                it.copy(
                    isOptionsPanelVisible = false,
                    isAppsPanelVisible = false
                )
            }
            },
            BrowserOption.ROTATE to OptionItem(
                id = BrowserOption.ROTATE,
                iconRes = R.drawable.ic_screen_rotation_up,
                contentDescription = R.string.desc_rotate
            ) { viewModel.updateUI { it.copy(isLandscapeByButton = true) } },
            BrowserOption.CLOSE_ALL_TABS to OptionItem(
                id = BrowserOption.CLOSE_ALL_TABS,
                iconRes = R.drawable.ic_close_all_tabs,
                contentDescription = R.string.desc_close_all_tabs
            ) {
                onCloseAllTabs()
                viewModel.updateUI {
                it.copy(
                    isOptionsPanelVisible = false,
                    isAppsPanelVisible = false
                )
            }
            },
            BrowserOption.ADBLOCK to OptionItem(
                id = BrowserOption.ADBLOCK,
                iconRes = if (settings.value.isAdBlockEnabled) R.drawable.ic_ublock else R.drawable.ic_remove_moderator,
                contentDescription = R.string.desc_adblock,
                enabled = settings.value.isAdBlockEnabled
            ) {
                viewModel.updateField(
                    BrowserSettingField.AD_BLOCK_ENABLED,
                    !settings.value.isAdBlockEnabled
                ); viewModel.updateUI {
                it.copy(
                    isOptionsPanelVisible = false,
                    isAppsPanelVisible = false
                )
            }
            },
            BrowserOption.SUGGESTIONS to OptionItem(
                id = BrowserOption.SUGGESTIONS,
                iconRes = R.drawable.ic_lightbulb,
                contentDescription = R.string.desc_suggestions,
                enabled = settings.value.showSuggestions
            ) {
                viewModel.updateSettings { it.copy(showSuggestions = !it.showSuggestions) }; viewModel.updateUI {
                it.copy(
                    isOptionsPanelVisible = false,
                    isAppsPanelVisible = false
                )
            }
            },
            BrowserOption.MEDIA_CONTROL to OptionItem(
                id = BrowserOption.MEDIA_CONTROL,
                iconRes = R.drawable.ic_video_settings,
                contentDescription = R.string.desc_media_control,
                enabled = settings.value.isEnabledMediaControl
            ) {
                viewModel.updateSettings { it.copy(isEnabledMediaControl = !it.isEnabledMediaControl) }; viewModel.updateUI {
                it.copy(
                    isOptionsPanelVisible = false,
                    isAppsPanelVisible = false
                )
            }
            },
            BrowserOption.OUT_SYNC to OptionItem(
                id = BrowserOption.OUT_SYNC,
                iconRes = R.drawable.ic_room_preferences,
                contentDescription = R.string.desc_out_sync,
                enabled = settings.value.isEnabledOutSync
            ) {
                viewModel.updateSettings { it.copy(isEnabledOutSync = !it.isEnabledOutSync) }; viewModel.updateUI {
                it.copy(
                    isOptionsPanelVisible = false,
                    isAppsPanelVisible = false
                )
            }
            },
            BrowserOption.OPTIMIZE_MEMORY to OptionItem(
                id = BrowserOption.OPTIMIZE_MEMORY,
                iconRes = if (viewModel.tabs.size > 1 || viewModel.geckoManager.sessionPoolSize > 1 ) R.drawable.ic_rocket_launch else R.drawable.ic_rocket,
                contentDescription = R.string.desc_optimize_memory,
                enabled = viewModel.tabs.size > 1 || viewModel.geckoManager.sessionPoolSize > 1
            ) {
               if (viewModel.tabs.size > 1 || viewModel.geckoManager.sessionPoolSize > 1) confirmationPopup(
                    R.string.confirm_optimize_memory,
                    "",
                    {
                        viewModel.optimizeMemory()
                        viewModel.updateUI {
                            it.copy(
                                isOptionsPanelVisible = false,
                                isAppsPanelVisible = false
                            )
                        }
                    },
                    {}
                )
            },
            BrowserOption.MEMORY_USAGE to OptionItem(
                id = BrowserOption.MEMORY_USAGE,
                iconRes = R.drawable.ic_memory, // Or ic_memory if you have one!
                contentDescription = R.string.desc_memory_usage
            ) { onNavigateToSetting(SettingPanelView.MEMORY_USAGE) },

            )
    }
}

// --- OPTIONS PANEL ---
@SuppressLint("SuspiciousIndentation")
@Composable
fun OptionsPanel(
    onCloseAllTabs: () -> Unit,
    confirmationPopup: (Int, String, () -> Unit, () -> Unit) -> Unit,
    changeBrowserIcon: () -> Unit,
    onLoginClick: () -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()

    // 1. Compute IDs first
    val currentOrderIds = settings.value.optionsOrder.split(",")
        .mapNotNull {
            try {
                BrowserOption.valueOf(it)
            } catch (_: Exception) {
                null
            }
        }

    val visibleIds = currentOrderIds.filter { opt ->
        viewModel.isSortingButtons.value || !viewModel.isOptionHidden(opt, settings.value)
    }

    // 2. Setup Pager State
    val optionPagesIds = visibleIds.chunked(4)
    val realPageCount = optionPagesIds.size
    val initialInfinitePage =
        if (realPageCount > 0) (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % realPageCount) else 0

    val pagerState =
        rememberPagerState(initialPage = initialInfinitePage, pageCount = { Int.MAX_VALUE })

    val actualPageIndexForSync =
        if (realPageCount > 0) pagerState.currentPage % realPageCount else 0
    val topOption = optionPagesIds.getOrNull(actualPageIndexForSync)?.firstOrNull()

    LaunchedEffect(topOption) {
        viewModel.topVisibleOptionsPanelItem.value = topOption
    }

    val registry = rememberBrowserOptionsRegistry(
        onCloseAllTabs = onCloseAllTabs,
        onNavigateToSetting = {},
        confirmationPopup = confirmationPopup,
        changeBrowserIcon = changeBrowserIcon,
        onLoginClick = onLoginClick,

    )

    // 4. Map IDs to UI Items
    val displayOptions = visibleIds.mapNotNull { registry[it] }
    val optionPages = displayOptions.chunked(4)
    val inspectingOpt = viewModel.inspectingOption.value
    LaunchedEffect(inspectingOpt, displayOptions) {
        if (inspectingOpt != null && displayOptions.isNotEmpty() && realPageCount > 0) {
            val flatIndex = displayOptions.indexOfFirst { it.id == inspectingOpt }
            if (flatIndex != -1) {
                val targetActualPage = flatIndex / 4 // 4 items per page
                val currentActualPage = pagerState.currentPage % realPageCount

                if (targetActualPage != currentActualPage) {
                    var diff = targetActualPage - currentActualPage
                    // Calculate the shortest scroll path for an infinite pager
                    if (diff > realPageCount / 2) diff -= realPageCount
                    else if (diff < -(realPageCount / 2)) diff += realPageCount

                    pagerState.animateScrollToPage(pagerState.currentPage + diff)
                }
            }
        }
    }
    if (displayOptions.isNotEmpty()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            HorizontalPager(
                state = pagerState, modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = realPageCount > 1
            ) { pageIndex ->
                val actualPageIndex = if (realPageCount > 0) pageIndex % realPageCount else 0
                val pageOptions = optionPages[actualPageIndex]

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(settings.value.padding.dp)
//                        .padding(horizontal = settings.value.padding.dp)
//                        .padding(bottom = settings.value.padding.dp)
                        .height(settings.value.heightForLayer(2).dp) // EXACT height of 1 row
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp)
                        ),
                    horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
                    userScrollEnabled = false // Prevent accidental vertical scrolling
                ) {
                    items(
                        items = pageOptions,
                        key = { it.id }
                    ) { option ->
                        val isHidden = viewModel.isOptionHidden(option.id, settings.value)
                        val isInspecting = viewModel.inspectingOption.value == option.id

                        Box(
                            modifier = Modifier
                                .animateItem() // <-- THE MAGIC ANIMATION MODIFIER
                                .fillMaxSize()
                                .alpha(if (isHidden) 0.3f else 1f)
                                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
                                .border(
                                    width = if (isInspecting) 2.dp else 0.dp,
                                    color = if (isInspecting) Color(settings.value.highlightColor) else Color.Transparent,
                                    shape = RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp)
                                )
                        ) {
                            CustomIconButton(
                                layer = 2,
                                modifier = Modifier.fillMaxSize(),
                                onTap = {
                                    if (viewModel.isSortingButtons.value) {
                                        viewModel.inspectingOption.value = option.id
                                        viewModel.updateUI { it.copy(isAppsPanelVisible = true) }
                                    } else {
                                        if (!pagerState.isScrollInProgress) option.onClick()
                                    }
                                },
                                textIcon = option.textIcon,
                                buttonDescription = stringResource(option.contentDescription),
                                painterId = option.iconRes,
                                isWhite = option.enabled
                            )
                        }
                    }

                    // Fill empty slots so the grid doesn't collapse
                    val remaining = 4 - pageOptions.size
                    if (remaining > 0) {
                        items(
                            count = remaining,
                            key = { "spacer_${actualPageIndex}_$it" }
                        ) {
                            Spacer(modifier = Modifier
                                .animateItem()
                                .fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}

// --- SETTINGS PANEL ---
@Composable
fun SettingsPanel(
    confirmationPopup: (Int, String, () -> Unit, () -> Unit) -> Unit,
    onCloseAllTabs: () -> Unit,
    targetSetting: SettingPanelView = SettingPanelView.MAIN,
    changeBrowserIcon: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    ) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    var currentView by remember { mutableStateOf(targetSetting) }
    val onBackClick = {
        if (!settings.value.isFirstAppLoad) currentView =
            SettingPanelView.MAIN else viewModel.updateUI { it.copy(isSettingsPanelVisible = false) }
    }

    LaunchedEffect(currentView) {
        if (currentView == SettingPanelView.CORNER_RADIUS) {
            if (settings.value.isSharpMode) viewModel.updateSettings { it.copy(isSharpMode = false) }
            viewModel.backgroundColor.value = Color.Red
            viewModel.updateUI { it.copy(isSettingCornerRadius = true,
                isFullscreenPreview = true) }
        } else {
            viewModel.backgroundColor.value = Color.Black
            Log.e("mrcFF", "here")
            if (uiState.value.isSettingCornerRadius) viewModel.updateUI {
                it.copy(
                    isFullscreenPreview = false
                )
            }
            viewModel.updateUI { it.copy(isSettingCornerRadius = false) }
        }
    }

    LaunchedEffect(uiState.value.isSettingsPanelVisible) {
        if (!uiState.value.isSettingsPanelVisible) {
            delay(settings.value.animationSpeed.toLong())
            currentView = SettingPanelView.MAIN
        }
    }

    // 1. Compute IDs first
    val currentOrderIds = settings.value.settingsOrder.split(",")
        .mapNotNull {
            try {
                BrowserOption.valueOf(it)
            } catch (_: Exception) {
                null
            }
        }

    val visibleIds = currentOrderIds.filter { opt ->
        viewModel.isSortingButtons.value || !viewModel.isOptionHidden(opt, settings.value)
    }

    // 2. Setup Pager State
    val optionPagesIds = visibleIds.chunked(4)
    val realPageCount = optionPagesIds.size
    val initialInfinitePage =
        if (realPageCount > 0) (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % realPageCount) else 0

    val pagerState =
        rememberPagerState(initialPage = initialInfinitePage, pageCount = { Int.MAX_VALUE })

    // 3. Build Registry
    val registry = rememberBrowserOptionsRegistry(
        onCloseAllTabs = onCloseAllTabs,
        onNavigateToSetting = { currentView = it },
        confirmationPopup = confirmationPopup,
        changeBrowserIcon = changeBrowserIcon,
        onLoginClick = onLoginClick
    )
    // 4. Map IDs to UI items
    val displayOptions = visibleIds.mapNotNull { registry[it] }
    val optionPages = displayOptions.chunked(4)
    val inspectingOpt = viewModel.inspectingOption.value
    LaunchedEffect(inspectingOpt, displayOptions) {
        if (inspectingOpt != null && displayOptions.isNotEmpty() && realPageCount > 0) {
            val flatIndex = displayOptions.indexOfFirst { it.id == inspectingOpt }
            if (flatIndex != -1) {
                val targetActualPage = flatIndex / 4 // 4 items per page
                val currentActualPage = pagerState.currentPage % realPageCount

                if (targetActualPage != currentActualPage) {
                    var diff = targetActualPage - currentActualPage
                    // Calculate the shortest scroll path for an infinite pager
                    if (diff > realPageCount / 2) diff -= realPageCount
                    else if (diff < -(realPageCount / 2)) diff += realPageCount

                    pagerState.animateScrollToPage(pagerState.currentPage + diff)
                }
            }
        }
    }
    AnimatedVisibility(
        visible = uiState.value.isSettingsPanelVisible || settings.value.isFirstAppLoad,
        enter = expandVertically(tween(settings.value.animationSpeedForLayer(1))),
        exit = shrinkVertically(
            tween(
                if (settings.value.isFirstAppLoad) settings.value.animationSpeedForLayer(0) * 6
                else settings.value.animationSpeedForLayer(1)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = settings.value.padding.dp)
                .padding(top = settings.value.padding.dp)
                .fillMaxWidth()
                .animateContentSize(tween(settings.value.animationSpeedForLayer(1)))
        ) {

            // --- NEW: AnimatedContent wrapper ---
            AnimatedContent(
                targetState = currentView,
                transitionSpec = {
                    val speed = settings.value.animationSpeedForLayer(1)
                    if (targetState != SettingPanelView.MAIN && initialState == SettingPanelView.MAIN) {
                        // Sliding IN to a setting (Content moves Left)
                        (slideInHorizontally(tween(speed)) { it / 4 } + fadeIn(tween(speed))) togetherWith
                                (slideOutHorizontally(tween(speed)) { -it / 4 } + fadeOut(tween(speed)))
                    } else {
                        // Sliding BACK to MAIN (Content moves Right)
                        (slideInHorizontally(tween(speed)) { -it / 4 } + fadeIn(tween(speed))) togetherWith
                                (slideOutHorizontally(tween(speed)) { it / 4 } + fadeOut(tween(speed)))
                    }.using(SizeTransform(clip = false)) // Let the parent's animateContentSize handle the height!
                },
                label = "SettingsViewTransition"
            ) { targetView ->
                when (targetView) {
                    SettingPanelView.MAIN -> {
                        if (displayOptions.isNotEmpty()) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth(),
                                userScrollEnabled = realPageCount > 1
                            ) { pageIndex ->
                                val actualPageIndex = if (realPageCount > 0) pageIndex % realPageCount else 0
                                val pageOptions = optionPages[actualPageIndex]

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(4),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(settings.value.heightForLayer(2).dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp)
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
                                    userScrollEnabled = false
                                ) {
                                    items(items = pageOptions, key = { it.id }) { option ->
                                        val isHidden = viewModel.isOptionHidden(option.id, settings.value)
                                        val isInspecting = viewModel.inspectingOption.value == option.id

                                        Box(
                                            modifier = Modifier
                                                .animateItem()
                                                .fillMaxSize()
                                                .alpha(if (isHidden) 0.3f else 1f)
                                                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
                                                .border(
                                                    width = if (isInspecting) 2.dp else 0.dp,
                                                    color = if (isInspecting) Color(settings.value.highlightColor) else Color.Transparent,
                                                    shape = RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp)
                                                )
                                        ) {
                                            CustomIconButton(
                                                layer = 2,
                                                modifier = Modifier.fillMaxSize(),
                                                onTap = {
                                                    if (viewModel.isSortingButtons.value) {
                                                        viewModel.inspectingOption.value = option.id
                                                        viewModel.updateUI { it.copy(isAppsPanelVisible = true) }
                                                    } else {
                                                        if (!pagerState.isScrollInProgress) option.onClick()
                                                    }
                                                },
                                                textIcon = option.textIcon,
                                                buttonDescription = stringResource(option.contentDescription),
                                                painterId = option.iconRes,
                                                isWhite = option.enabled,
                                            )
                                        }
                                    }

                                    val remaining = 4 - pageOptions.size
                                    if (remaining > 0) {
                                        items(count = remaining, key = { "spacer_${actualPageIndex}_$it" }) {
                                            Spacer(modifier = Modifier.animateItem().fillMaxSize())
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- ALL OTHER SETTINGS VIEWS ---
                    SettingPanelView.CORNER_RADIUS -> SliderSetting(
                        onBackClick = onBackClick,
                        valueRange = 0f..55f,
                        steps = 5499,
                        textFieldValueFun = { src -> src.take(2) + "." + src.substring(2, 4) },
                        iconID = R.drawable.ic_adjust_corner_radius,
                        field = BrowserSettingField.CORNER_RADIUS
                    )

                    SettingPanelView.ANIMATION_SPEED -> SliderSetting(
                        onBackClick = onBackClick,
                        valueRange = 0f..1000f,
                        steps = 999,
                        textFieldValueFun = { it },
                        afterDecimal = false,
                        iconID = R.drawable.ic_animation,
                        field = BrowserSettingField.ANIMATION_SPEED
                    )

                    SettingPanelView.SINGLE_LINE_HEIGHT -> SliderSetting(
                        onBackClick = onBackClick,
                        valueRange = 90f..110f,
                        steps = 19,
                        textFieldValueFun = { it },
                        afterDecimal = false,
                        iconID = R.drawable.ic_expand,
                        field = BrowserSettingField.SINGLE_LINE_HEIGHT
                    )

                    SettingPanelView.PADDING -> SliderSetting(
                        onBackClick = onBackClick,
                        valueRange = 2f..8f,
                        steps = 5,
                        textFieldValueFun = { it },
                        afterDecimal = false,
                        iconID = R.drawable.ic_padding,
                        digitCount = 2,
                        field = BrowserSettingField.PADDING
                    )

                    SettingPanelView.CURSOR_CONTAINER_SIZE -> SliderSetting(
                        onBackClick = onBackClick,
                        valueRange = 20f..70f,
                        steps = 49,
                        textFieldValueFun = { it },
                        afterDecimal = false,
                        iconID = R.drawable.ic_cursor_size,
                        digitCount = 2,
                        field = BrowserSettingField.CURSOR_CONTAINER_SIZE
                    )

                    SettingPanelView.CURSOR_TRACKING_SPEED -> SliderSetting(
                        onBackClick = onBackClick,
                        valueRange = 0.5f..2f,
                        steps = 29,
                        textFieldValueFun = { src -> src[1] + "." + src.substring(2, 4) },
                        afterDecimal = true,
                        iconID = R.drawable.ic_cursor_speed,
                        digitCount = 4,
                        field = BrowserSettingField.CURSOR_TRACKING_SPEED
                    )

                    SettingPanelView.DEFAULT_URL -> TextSetting(
                        onBackClick = onBackClick,
                        iconID = R.drawable.ic_link,
                        currentSettingOriginalValue = settings.value.defaultUrl,
                        field = BrowserSettingField.DEFAULT_URL
                    )

                    SettingPanelView.INFO -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(settings.value.heightForLayer(2).dp),
                        contentAlignment = Alignment.Center
                    ) { Text("make by marcinlowercase", color = Color.White) }

                    SettingPanelView.CLOSED_TAB_HISTORY_SIZE -> SliderSetting(
                        onBackClick = onBackClick,
                        valueRange = 0f..30f,
                        steps = 29,
                        textFieldValueFun = { it },
                        afterDecimal = false,
                        iconID = R.drawable.ic_history,
                        digitCount = 2,
                        field = BrowserSettingField.CLOSED_TAB_HISTORY_SIZE
                    )

                    SettingPanelView.BACK_SQUARE_OPACITY -> SliderSetting(
                        onBackClick = onBackClick,
                        valueRange = 0f..1f,
                        steps = 19,
                        textFieldValueFun = { src -> src[1] + "." + src.substring(2, 4) },
                        iconID = R.drawable.ic_opacity,
                        digitCount = 4,
                        afterDecimal = true,
                        field = BrowserSettingField.BACK_SQUARE_OPACITY
                    )

                    SettingPanelView.MAX_LIST_HEIGHT -> SliderSetting(
                        onBackClick = { currentView = SettingPanelView.MAIN },
                        valueRange = 0f..5f,
                        steps = 49,
                        textFieldValueFun = { src -> src.take(2) + "." + src.substring(2, 4) },
                        iconID = R.drawable.ic_max_list_height,
                        digitCount = 4,
                        afterDecimal = true,
                        field = BrowserSettingField.MAX_LIST_HEIGHT
                    )

                    SettingPanelView.MEMORY_USAGE -> {
                        val memoryLow = stringResource(R.string.memory_low)
                        val memoryStandard = stringResource(R.string.memory_standard)
                        val memoryHigh = stringResource(R.string.memory_high)
                        SliderSetting(
                            textEnabled = false,
                            onBackClick = { currentView = SettingPanelView.MAIN },
                            valueRange = 0f..2f,
                            steps = 1,
                            textFieldValueFun = { src ->
                                when (src[1].digitToInt()) {
                                    0 -> memoryLow
                                    1 -> memoryStandard
                                    2 -> memoryHigh
                                    else -> memoryStandard
                                }
                            },
                            storeValueFun = { src -> src[1].digitToInt().toFloat() },
                            iconID = R.drawable.ic_memory,
                            digitCount = 4,
                            afterDecimal = true,
                            field = BrowserSettingField.MEMORY_USAGE
                        )
                    }

                    SettingPanelView.SEARCH_ENGINE -> SliderSetting(
                        textEnabled = false,
                        onBackClick = { currentView = SettingPanelView.MAIN },
                        valueRange = 0f..SearchEngine.entries.lastIndex.toFloat(),
                        steps = SearchEngine.entries.lastIndex - 1,
                        textFieldValueFun = { src -> SearchEngine.entries[src[1].digitToInt()].title },
                        storeValueFun = { src -> src[1].digitToInt().toFloat() },
                        iconID = R.drawable.ic_search,
                        digitCount = 4,
                        afterDecimal = true,
                        field = BrowserSettingField.SEARCH_ENGINE
                    )

                    SettingPanelView.HIGHLIGHT_COLOR -> {
                        val initialHsv = remember(settings.value.highlightColor) {
                            val hsv = FloatArray(3)
                            AndroidColor.colorToHSV(settings.value.highlightColor, hsv)
                            hsv
                        }
                        var hue by remember { mutableFloatStateOf(initialHsv[0]) }
                        var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
                        var value by remember { mutableFloatStateOf(initialHsv[2]) }

                        val selectedColorInt = remember(hue, saturation, value) {
                            AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
                        }
                        var hexText by remember {
                            mutableStateOf(String.format("#%06X", 0xFFFFFF and selectedColorInt))
                        }

                        // Only auto-update the text when NOT typing (allows sliders to update text instantly)
                        LaunchedEffect(selectedColorInt) {
                            if (!uiState.value.isFocusOnSettingTextField) {
                                hexText = String.format("#%06X", 0xFFFFFF and selectedColorInt)
                            }
                            viewModel.updateSettings { it.copy(highlightColor = selectedColorInt) }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(settings.value.padding.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(settings.value.heightForLayer(3).dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
                            ) {
                                IconButton(
                                    onClick = onBackClick,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp))
                                        .fillMaxHeight()
                                        .background(Color.White)
                                        .defaultMinSize(minWidth = settings.value.heightForLayer(3).dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (settings.value.isFirstAppLoad) R.drawable.ic_check else R.drawable.ic_arrow_back),
                                        contentDescription = "Back to Settings",
                                        tint = Color.Black
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp))
                                        .background(Color(selectedColorInt)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val textColor =
                                        if (isColorDark(selectedColorInt)) Color.White else Color.Black
                                    val keyboardController = LocalSoftwareKeyboardController.current
                                    val focusManager = LocalFocusManager.current
                                    BasicTextField(
                                        value = hexText,
                                        onValueChange = { newText ->
                                            val filtered =
                                                newText.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it == '#' }
                                            if (filtered.length <= 7) {
                                                hexText = filtered
                                                try {
                                                    val parsedColor =
                                                        (if (filtered.startsWith("#")) filtered else "#$filtered").toColorInt()
                                                    val hsv = FloatArray(3)
                                                    AndroidColor.colorToHSV(parsedColor, hsv)
                                                    hue = hsv[0]
                                                    saturation = hsv[1]
                                                    value = hsv[2]
                                                } catch (_: Exception) {
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged { focusState ->
                                                viewModel.updateUI {
                                                    it.copy(isFocusOnSettingTextField = focusState.hasFocus)
                                                }
                                                hexText = if (focusState.hasFocus) {
                                                    "" // Clear text so user can easily type
                                                } else {
                                                    // The moment focus is lost, force text to match the real color
                                                    String.format(
                                                        "#%06X",
                                                        0xFFFFFF and selectedColorInt
                                                    )
                                                }
                                            },
                                        cursorBrush = SolidColor(Color.Transparent),
                                        textStyle = TextStyle(
                                            color = textColor,
                                            fontFamily = FontFamily.Monospace,
                                            textAlign = TextAlign.Center
                                        ),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = {
                                            // Clearing focus will trigger onFocusChanged(false), auto-formatting the text for us!
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        }),
                                        singleLine = true
                                    )
                                }
                                IconButton(
                                    onClick = { },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp))
                                        .fillMaxHeight()
                                        .defaultMinSize(minWidth = settings.value.heightForLayer(3).dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_colors),
                                        contentDescription = "Highlight Color",
                                        tint = Color.White
                                    )
                                }
                            }
                            AnimatedVisibility(
                                visible = !uiState.value.isFocusOnSettingTextField,
                                enter = expandVertically(tween(settings.value.animationSpeedForLayer(1))),
                                exit = shrinkVertically(tween(settings.value.animationSpeedForLayer(1)))
                            ) {
                                Column {
                                    val rainbowBrush = remember {
                                        Brush.horizontalGradient(
                                            listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                                        )
                                    }
                                    Box(contentAlignment = Alignment.Center) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(sliderHeight.dp)
                                                .clip(CircleShape)
                                                .background(rainbowBrush)
                                        )
                                        Slider(
                                            value = hue,
                                            onValueChange = { hue = it },
                                            valueRange = 0f..360f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color.White,
                                                activeTrackColor = Color.Transparent,
                                                inactiveTrackColor = Color.Transparent
                                            )
                                        )
                                    }

                                    Slider(
                                        value = saturation,
                                        onValueChange = { saturation = it },
                                        valueRange = 0f..1f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color.White,
                                            activeTrackColor = Color(selectedColorInt).copy(alpha = 1f)
                                        )
                                    )

                                    Slider(
                                        value = value,
                                        onValueChange = { value = it },
                                        valueRange = 0f..1f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color.White,
                                            activeTrackColor = Color.White.copy(alpha = value)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun OptionsPanelWrapper(
    dragOffset: Float,
    content: @Composable () -> Unit
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val safeOffset = if (dragOffset.isNaN()) 0f else dragOffset
    val animatedHeight = -safeOffset.coerceIn(-uiState.value.totalRevealHeightPx, 0f)

    Layout(
        content = content,
        modifier = Modifier.clipToBounds()
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(constraints)
        val currentHeight = animatedHeight.roundToInt()

        layout(placeable.width, currentHeight) {
            placeable.place(0, 0)
        }
    }
}