package marcinlowercase.a.ui.panel

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.OptionItem
import marcinlowercase.a.core.enum_class.RevealState
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.composition.LocalBrowserSettings
import org.mozilla.geckoview.GeckoSession
import kotlin.math.roundToInt

@SuppressLint("SuspiciousIndentation")
@Composable
fun OptionsPanel(
    draggableState: AnchoredDraggableState<RevealState>,
    isPinningApp: MutableState<Boolean>,
    bottomPanelPagerState: PagerState,
    onCloseAllTabs: () -> Unit,
    activeSession: GeckoSession,
    isFindInPageVisible: MutableState<Boolean>,
    descriptionContent: MutableState<String>,
    reopenClosedTab: () -> Unit,
    isSettingsPanelVisible: MutableState<Boolean>,
    setIsOptionsPanelVisible: (Boolean) -> Job,
    toggleIsTabsPanelVisible: () -> Unit,
    tabsPanelLock: Boolean,
    isDownloadPanelVisible: MutableState<Boolean>,
    isCursorPadVisible: Boolean,
    isCursorMode: Boolean,
    setIsCursorMode: (Boolean) -> Unit,
    closedTabsCount: Int,
    addAppToPin: () -> Unit,
    updateCurrentRotation: ()-> Unit,
) {
    val settingsController = LocalBrowserSettings.current
    val settings = settingsController.current

    // This remains the same
    val allOptions =
        remember(
            settings,
            tabsPanelLock,
            isDownloadPanelVisible,
            isCursorPadVisible,
            isSettingsPanelVisible,
            activeSession,
            bottomPanelPagerState,
            isPinningApp.value,
            isFindInPageVisible.value,
            draggableState.currentValue

        ) {
            listOf(

                OptionItem(
                    R.drawable.ic_keep, // You'll need an icon for this
                    "pin", // Display the number of open tabs
                    isPinningApp.value
                ) {
                    addAppToPin()

                },

//                OptionItem(
//                    if (settings.isDesktopMode) R.drawable.ic_mobile else R.drawable.ic_desktop,
//                    "Desktop layout",
//                    settings.isDesktopMode
//                ) {
//                    updateBrowserSettings(settings.copy(isDesktopMode = !settings.isDesktopMode))
//                },
                OptionItem(
                    R.drawable.ic_tabs, // You'll need an icon for this
                    "tabs panel", // Display the number of open tabs
                    tabsPanelLock
                ) {
                    toggleIsTabsPanelVisible()
                    setIsOptionsPanelVisible(false)

                },
                OptionItem(
                    if (settings.isSharpMode) R.drawable.ic_rounded_corner else R.drawable.ic_sharp_corner,
                    "sharp mode",
                    settings.isSharpMode,
                ) {
                    settingsController.update(settings.copy(isSharpMode = !settings.isSharpMode))
                    setIsOptionsPanelVisible(false)

                },

                OptionItem(
                    R.drawable.ic_reopen_window, // You'll need an icon for this
                    "reopen closed tab",
                    enabled = closedTabsCount > 0, // Only enable if there are tabs to reopen
                ) {
                    reopenClosedTab()
                    setIsOptionsPanelVisible(false) // Close the panel after action
                },
//                OptionItem(
//                    R.drawable.ic_mouse_cursor, // You'll need a download icon
//                    "cursor pad",
//                    isCursorPadVisible,
//                ) {
//
//                    setIsCursorMode(!isCursorMode)
//                    setIsOptionsPanelVisible(false)
//                },

                OptionItem(
                    R.drawable.ic_screen_rotation_up,
                    "rotate",
                    false,
                ) {
                    updateCurrentRotation()
                },
                OptionItem(
                    R.drawable.ic_find_in_page, // You'll need an icon
                    "find in page",
                    isFindInPageVisible.value
                ) {
                    isFindInPageVisible.value = !isFindInPageVisible.value
                    setIsOptionsPanelVisible(false)
                },

                OptionItem(
                    R.drawable.ic_download, // You'll need a download icon
                    "download panel",
                    isDownloadPanelVisible.value
                ) {
                    isDownloadPanelVisible.value = !isDownloadPanelVisible.value
                    setIsOptionsPanelVisible(false)
                },

                OptionItem(
                    iconRes = R.drawable.ic_lightbulb, // Or a more specific icon like ic_manage_search
                    contentDescription = "suggestions",
                    enabled = settings.showSuggestions // The button is "active" when suggestions are on
                ) {
                    // When clicked, create a new settings object with the toggled value

                    settingsController.update(settings.copy(showSuggestions = !settings.showSuggestions))
                    setIsOptionsPanelVisible(false)

                },

                OptionItem(
                    iconRes = R.drawable.ic_close_all_tabs, // Ensure you have this drawable
                    contentDescription = "close all tabs",
                    enabled = false // Not a toggle, so never "active"
                ) {
                    onCloseAllTabs() // Call the function from BrowserScreen
                    setIsOptionsPanelVisible(false) // Hide the panel after initiating
                },

                OptionItem(
                    R.drawable.ic_settings, // You'll need a settings icon
                    "settings",
                    isSettingsPanelVisible.value,
                ) {
                    // When clicked, show the settings panel and hide this one.
                    isSettingsPanelVisible.value = !isSettingsPanelVisible.value

                    setIsOptionsPanelVisible(false)
                },


//                OptionItem(R.drawable.ic_bug, "debug", false) {
//                    activeWebView?.let { webView ->
//                        val outState = Bundle()
//                        val history = webView.saveState(outState)
//                        if (history != null) {
//                            for (key in outState.keySet()) {
//
//                            // --- DETAILED HISTORY LOGGING ---
//
//                            for (i in 0 until history.size) {
//                                val item = history.getItemAtIndex(i)
//                                val isCurrent =
//                                    if (i == history.currentIndex) " <-- CURRENT" else ""
//                                val favicon = item.favicon
//                                if (favicon != null) {
//                                } else {
//                                }
//                            }
//                            // --- END OF NEW LOGGING ---
//
//                        } else {
//                        }
//                    }
//                },
//                OptionItem(R.drawable.ic_fullscreen, "Button 4", false) { /* ... */ },
//                OptionItem(R.drawable.ic_fullscreen, "Button 5", false) { /* ... */ },
//                OptionItem(R.drawable.ic_fullscreen, "Button 6", false) { /* ... */ },
//                OptionItem(R.drawable.ic_fullscreen, "Button 7", false) { /* ... */ },
//                OptionItem(R.drawable.ic_fullscreen, "Button 8", false) { /* ... */ }
            )
        }

// --- NEW: Group the options into pages of 4 ---
    val optionPages = remember(allOptions) {
        allOptions.chunked(4)
    }

    // --- Pager State ---
    // The pagerState remembers the current page and handles scroll animations.
    val pagerState = rememberPagerState(pageCount = { optionPages.size })

        Box(
            modifier = Modifier
                .padding(horizontal = settings.padding.dp)
                .padding(bottom = settings.padding.dp)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        settings.cornerRadiusForLayer(2).dp
                    )
                )

        ) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { pageIndex ->
                // This composable block is called for each page.

                // A Row holds the 4 buttons for the current page.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(
                                settings.cornerRadiusForLayer(2).dp
                            )
                        ),

                    horizontalArrangement = Arrangement.spacedBy(settings.padding.dp)
                ) {
                    // Get the options for the current page
                    val pageOptions = optionPages[pageIndex]

                    // Create an IconButton for each option on the page
                    pageOptions.forEach { option ->
                        CustomIconButton(
//                            currentRotation = currentRotation,
                            layer = 2,
                            modifier = Modifier.weight(1f),
                            onTap = option.onClick,
                            descriptionContent = descriptionContent,
                            buttonDescription = option.contentDescription,
                            painterId = option.iconRes,
                            isWhite = option.enabled

                        )

                    }

//                    // If a page has fewer than 4 items, we add spacers to keep the layout consistent.
//                    repeat(4 - pageOptions.size) {
//                        Spacer(modifier = Modifier.weight(1f))
//                    }
                }
            }

        }
//    }
}


@Composable
fun OptionsPanelWrapper(
    maxHeight: Float,
    dragOffset: Float,
    content: @Composable () -> Unit
) {
    val safeOffset = if (dragOffset.isNaN()) 0f else dragOffset
    val animatedHeight = -safeOffset.coerceIn(-maxHeight, 0f)

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