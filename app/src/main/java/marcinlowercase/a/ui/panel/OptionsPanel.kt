package marcinlowercase.a.ui.panel

import android.os.Bundle
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.custom_class.CustomWebView
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.OptionItem
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.function.buttonPointerInput
import kotlin.collections.chunked
import kotlin.collections.forEach

@Composable
fun OptionsPanel(
    isPinningApp: MutableState<Boolean>,
    bottomPanelPagerState: PagerState,
    onCloseAllTabs: () -> Unit,
    activeWebView: CustomWebView?,
    isFindInPageVisible: MutableState<Boolean>,
    descriptionContent: MutableState<String>,
    hapticFeedback: HapticFeedback,
    reopenClosedTab: () -> Unit,
    setIsSettingsPanelVisible: (Boolean) -> Unit,
    isSettingsPanelVisible: Boolean,
    setIsDownloadPanelVisible: (Boolean) -> Unit,

    isOptionsPanelVisible: Boolean = false,
    setIsOptionsPanelVisible: (Boolean) -> Unit = {},
    toggleIsTabsPanelVisible: () -> Unit,
    updateBrowserSettings: (BrowserSettings) -> Int,
    browserSettings: BrowserSettings,
    tabs: List<Tab>,
    tabsPanelLock: Boolean,
    isDownloadPanelVisible: Boolean,
    isCursorPadVisible: Boolean,
    isCursorMode: Boolean,
    setIsCursorMode: (Boolean) -> Unit,
    closedTabsCount: Int,
    addAppToPin: () -> Unit,
) {


    // This remains the same
    val allOptions =
        remember(
            browserSettings,
            tabsPanelLock,
            isDownloadPanelVisible,
            isCursorPadVisible,
            isSettingsPanelVisible,
            activeWebView,
            browserSettings.showSuggestions,
            bottomPanelPagerState,
            isPinningApp.value,
            isFindInPageVisible.value

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
//                    if (browserSettings.isDesktopMode) R.drawable.ic_mobile else R.drawable.ic_desktop,
//                    "Desktop layout",
//                    browserSettings.isDesktopMode
//                ) {
//                    updateBrowserSettings(browserSettings.copy(isDesktopMode = !browserSettings.isDesktopMode))
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
                    if (browserSettings.isSharpMode) R.drawable.ic_rounded_corner else R.drawable.ic_sharp_corner,
                    "sharp mode",
                    browserSettings.isSharpMode,
                ) {
                    updateBrowserSettings(browserSettings.copy(isSharpMode = !browserSettings.isSharpMode))
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
                OptionItem(
                    R.drawable.ic_mouse_cursor, // You'll need a download icon
                    "cursor pad",
                    isCursorPadVisible,
                ) {
                    Log.e("isCursorMode", "isCursorMode: $isCursorMode")

                    setIsCursorMode(!isCursorMode)
                    setIsOptionsPanelVisible(false)
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
                    isDownloadPanelVisible
                ) {
                    setIsDownloadPanelVisible(!isDownloadPanelVisible)
                    setIsOptionsPanelVisible(false)
                },

                OptionItem(
                    iconRes = R.drawable.ic_lightbulb, // Or a more specific icon like ic_manage_search
                    contentDescription = "suggestions",
                    enabled = browserSettings.showSuggestions // The button is "active" when suggestions are on
                ) {
                    // When clicked, create a new settings object with the toggled value
                    updateBrowserSettings(
                        browserSettings.copy(showSuggestions = !browserSettings.showSuggestions)
                    )
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
                    isSettingsPanelVisible,
                ) {
                    // When clicked, show the settings panel and hide this one.
                    setIsSettingsPanelVisible(!isSettingsPanelVisible)
                    setIsOptionsPanelVisible(false)
                },


                OptionItem(R.drawable.ic_bug, "debug", false) {
                    Log.e("BROWSER SETTINGS", browserSettings.toString())
                    Log.e("Tabs List", tabs.toString())
                    activeWebView?.let { webView ->
                        val outState = Bundle()
                        val history = webView.saveState(outState)
                        if (history != null) {
                            Log.d("WebViewSaveState", "State saved. Bundle content: $outState")
                            for (key in outState.keySet()) {
                                Log.d(
                                    "WebViewSaveState",
                                    "-> Key: $key, Value: ${outState.keySet()}"
                                )
                            }

                            // --- DETAILED HISTORY LOGGING ---
                            Log.d("WebViewSaveState", "--- History Details ---")
                            Log.d("WebViewSaveState", "History Size: ${history.size}")
                            Log.d("WebViewSaveState", "Current Index: ${history.currentIndex}")

                            for (i in 0 until history.size) {
                                val item = history.getItemAtIndex(i)
                                val isCurrent =
                                    if (i == history.currentIndex) " <-- CURRENT" else ""
                                Log.d(
                                    "WebViewSaveState",
                                    "[$i] Title: '${item.title}', URL: '${item.url}', Original URL: '${item.originalUrl}'$isCurrent"
                                )
                                val favicon = item.favicon
                                if (favicon != null) {
                                    Log.d(
                                        "WebViewSaveState",
                                        "  -> Favicon: Yes, ${favicon.width}x${favicon.height}px"
                                    )
                                } else {
                                    Log.d("WebViewSaveState", "  -> Favicon: No")
                                }
                            }
                            Log.d("WebViewSaveState", "--- End History Details ---")
                            // --- END OF NEW LOGGING ---

                        } else {
                            Log.e("WebViewSaveState", "Failed to save WebView state.")
                        }
                    }
                },
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

    AnimatedVisibility(
        visible = isOptionsPanelVisible,
        enter = expandVertically(
            tween(
                browserSettings.animationSpeedForLayer( 1)
            )
        ) + fadeIn(
            tween(
                browserSettings.animationSpeedForLayer(1)
            )
        ),
        exit = shrinkVertically(
            tween(
                browserSettings.animationSpeedForLayer( 1)
            )
        ) + fadeOut(
            tween(
                browserSettings.animationSpeedForLayer(1)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = browserSettings.padding.dp)
                .padding(bottom = browserSettings.padding.dp)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        browserSettings.cornerRadiusForLayer(2).dp
                    )
                )
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            // dragAmount is the change in the Y-axis.
                            // A negative value means the finger has moved UP.
                            if (dragAmount < 0) {
                                setIsOptionsPanelVisible(true)
                            }
                            // A positive value means the finger has moved DOWN.
                            else if (dragAmount > 0) {
                                setIsOptionsPanelVisible(false)
                            }
                        })
                }

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
                                browserSettings.cornerRadiusForLayer(2).dp
                            )
                        ),

                    horizontalArrangement = Arrangement.spacedBy(browserSettings.padding.dp)
                ) {
                    // Get the options for the current page
                    val pageOptions = optionPages[pageIndex]

                    // Create an IconButton for each option on the page
                    pageOptions.forEach { option ->
                        Box(
                            // Use weight to make the buttons share space equally
                            modifier = Modifier
                                .weight(1f)
                                .height(
                                    browserSettings.heightForLayer(2).dp
                                )
                                .background(
                                    if (option.enabled) Color.White else Color.Black,
                                    shape = RoundedCornerShape(
                                        browserSettings.cornerRadiusForLayer(2).dp
                                    )
                                )
                                .buttonPointerInput(
                                    onTap = option.onClick,
                                    hapticFeedback = hapticFeedback,
                                    descriptionContent = descriptionContent,
                                    buttonDescription = option.contentDescription
                                )
                            ,
                            contentAlignment = Alignment.Center


                        ) {
                            Icon(
                                painter = painterResource(id = option.iconRes),
                                contentDescription = option.contentDescription,
                                tint = if (option.enabled) Color.Black else Color.White
                            )
                        }
                    }

//                    // If a page has fewer than 4 items, we add spacers to keep the layout consistent.
//                    repeat(4 - pageOptions.size) {
//                        Spacer(modifier = Modifier.weight(1f))
//                    }
                }
            }

        }
    }
}