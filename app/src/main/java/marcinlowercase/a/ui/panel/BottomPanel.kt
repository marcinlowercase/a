package marcinlowercase.a.ui.panel

import android.content.ClipData
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.R
import marcinlowercase.a.core.custom_class.CustomWebView
import marcinlowercase.a.core.data_class.App
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.ConfirmationDialogState
import marcinlowercase.a.core.data_class.ContextMenuData
import marcinlowercase.a.core.data_class.CustomPermissionRequest
import marcinlowercase.a.core.data_class.DownloadItem
import marcinlowercase.a.core.data_class.JsDialogState
import marcinlowercase.a.core.data_class.PanelVisibilityState
import marcinlowercase.a.core.data_class.SiteSettings
import marcinlowercase.a.core.data_class.Suggestion
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.BottomPanelMode
import marcinlowercase.a.core.enum_class.GestureNavAction
import marcinlowercase.a.core.enum_class.SuggestionSource
import marcinlowercase.a.core.function.toDomain
import marcinlowercase.a.core.manager.WebViewManager
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.abs

@Composable
fun BottomPanel(
    setIsBottomPanelVisible : (Boolean) -> Unit,
    setIsUrlBarVisible : (Boolean) -> Unit,
    isAppsPanelVisible: MutableState<Boolean>,
    resetBottomPanelTrigger: MutableState<Boolean>,
    apps: MutableList<App>,
    isBottomPanelLock: MutableState<Boolean>,
    bottomPanelPagerState: PagerState,
    onOpenInNewTab: (String) -> Unit,
    onDownloadImage: (String) -> Unit,
    contextMenuData: ContextMenuData?,
    displayContextMenuData: ContextMenuData?,
    onDismissContextMenu: () -> Unit,
    isFocusOnTextField: Boolean,
    textFieldState: TextFieldState,
    onCloseAllTabs: () -> Unit,
    suggestions: List<Suggestion>, // Changed from List<String>
    onSuggestionClick: (Suggestion) -> Unit, // Changed from (String)
    onRemoveSuggestion: (Suggestion) -> Unit,
    webViewManager: WebViewManager,
    isFindInPageVisible: MutableState<Boolean>,
    findInPageText: MutableState<String>,
    findInPageResult: MutableState<Pair<Int, Int>>,

    onAddToHomeScreen: () -> Unit,
    descriptionContent: MutableState<String>,
    recentlyClosedTabs: SnapshotStateList<Tab>,
    reopenClosedTab: () -> Unit,
    confirmationPopup: (message: String, onConfirm: () -> Unit, onCancel: () -> Unit) -> Unit,
    resetBrowserSettings: () -> Int,
    backgroundColor: MutableState<Color>,
    setIsSettingsPanelVisible: (Boolean) -> Unit,
    isSettingsPanelVisible: Boolean,
//    setIsSettingsPanelVisible: (Boolean) -> Unit,
    urlBarFocusRequester: FocusRequester,
    isCursorPadVisible: Boolean,
    isCursorMode: Boolean,
    setIsCursorMode: (Boolean) -> Unit,
    setIsTabsPanelVisible: (Boolean) -> Unit,
    setIsDownloadPanelVisible: (Boolean) -> Unit,
    setIsTabDataPanelVisible: (Boolean) -> Unit,
    savedPanelState: PanelVisibilityState?,
    setSavedPanelState: (PanelVisibilityState?) -> Unit,
    confirmationDisplayState: ConfirmationDialogState?, // Add this

    confirmationState: ConfirmationDialogState?,
    onPermissionDeny: () -> Unit,
    tabsPanelLock: Boolean,
    updateInspectingTab: (Tab) -> Unit,
    isTabDataPanelVisible: Boolean,
    inspectingTab: Tab?,
    handleHistoryNavigation: (tab: Tab, index: Int, webViewManager: WebViewManager) -> Unit,
    handleCloseInspectedTab: () -> Unit,
    handleClearInspectedTabData: () -> Unit,
    handlePermissionToggle: (domain: String?, permission: String, isGranted: Boolean) -> Unit,
    siteSettings: Map<String, SiteSettings>,
    onTabDataPanelDismiss: () -> Unit,
    onTabLongPressed: (Tab) -> Unit,

    onDownloadRowClicked: (DownloadItem) -> Unit,
    onDeleteClicked: (DownloadItem) -> Unit,
    onOpenFolderClicked: () -> Unit,
    onClearAllClicked: () -> Unit,
    isDownloadPanelVisible: Boolean,
    downloads: List<DownloadItem>,

    activeWebView: CustomWebView?,
    isUrlOverlayBoxVisible: Boolean,
    setIsUrlOverlayBoxVisible: (Boolean) -> Unit,
    onNewTabClicked: (Int) -> Unit,

    isTabsPanelVisible: Boolean,
    onTabSelected: (Int) -> Unit,
    navigateWebView: () -> Unit,
    hapticFeedback: HapticFeedback,
    setActiveNavAction: (GestureNavAction) -> Unit,
    setIsNavPanelVisible: (Boolean) -> Unit,

    isNavPanelVisible: Boolean,
    activeNavAction: GestureNavAction,
    isPromptPanelVisible: Boolean = false,
    state: JsDialogState?,
    promptComponentDisplayState: JsDialogState?,
    onDismiss: () -> Unit,
    isPermissionPanelVisible: Boolean = false,
    permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
    pendingPermissionRequest: CustomPermissionRequest?,
    modifier: Modifier,
    activeTabIndex: MutableState<Int>,
    tabs: List<Tab>,
    isUrlBarVisible: Boolean,
    isBottomPanelVisible: Boolean,
    isOptionsPanelVisible: Boolean,
    browserSettings: BrowserSettings,
    updateBrowserSettings: (BrowserSettings) -> Int,
//    url: String,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    setIsOptionsPanelVisible: (Boolean) -> Unit = {},
    toggleIsTabsPanelVisible: () -> Unit = {},
    onNewUrl: (String) -> Unit = {},
    setTextFieldHeightPx: (Int) -> Unit = {},
    setIsFocusOnTextField: (Boolean) -> Unit = {},
) {

    val isPinningApp= remember { mutableStateOf(false) }
    AnimatedVisibility(
        modifier = modifier,
        visible = isBottomPanelVisible,
        enter = slideInVertically(
            animationSpec = tween(
                browserSettings.animationSpeedForLayer(0)
            ),
            initialOffsetY = { it }
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                browserSettings.animationSpeedForLayer(1)
            ),
            targetOffsetY = { it }
        )
    ) {
        val clipboard = LocalClipboard.current

        Column(
            modifier = Modifier
                .padding(browserSettings.padding.dp)


                .clip(
                    RoundedCornerShape(
                        browserSettings.cornerRadiusForLayer(1).dp
                    )
                )

                .background(
                    Color.Black,
                )
//                .border(
//                    color = Color.White,
//                    width = 1.dp,
//                    shape = RoundedCornerShape(
//                        cornerRadiusForLayer(
//                            1,
//                            browserSettings.deviceCornerRadius,
//                            browserSettings.padding
//                        ).dp
//                    )
//                )

        ) {

            AppsPanel(
                apps = apps,
                visibility = isAppsPanelVisible,
                browserSettings = browserSettings,
                onAppClick = { app ->
                    onOpenInNewTab(app.url)
                    setIsBottomPanelVisible(false)
                    setIsUrlBarVisible(false)

                }
            )
            DescriptionPanel(
                isVisible = descriptionContent.value.isNotEmpty(),
                description = descriptionContent.value,
                browserSettings = browserSettings,
                onDismiss = {
                    descriptionContent.value = ""
                }

            )


            NavigationPanel(
                isNavPanelVisible = isNavPanelVisible,
                activeWebView = activeWebView,
                browserSettings = browserSettings,
                activeAction = activeNavAction,

                )


            DownloadPanel(
                confirmationPopup = confirmationPopup,
                setIsDownloadPanelVisible = setIsDownloadPanelVisible,
                isDownloadPanelVisible = isDownloadPanelVisible,
                downloads = downloads,
                browserSettings = browserSettings,
                onDownloadRowClicked = onDownloadRowClicked,
                onDeleteClicked = onDeleteClicked,
                onOpenFolderClicked = onOpenFolderClicked,
                onClearAllClicked = onClearAllClicked
            )

            ContextMenuPanel(
                descriptionContent = descriptionContent,

                isVisible = contextMenuData != null,
                data = displayContextMenuData,

                browserSettings = browserSettings,
                onDismiss = onDismissContextMenu,
                onOpenInNewTab = { url ->
                    onOpenInNewTab(url)
                    onDismissContextMenu()
                },
                onDownloadImage = onDownloadImage,
                hapticFeedback = hapticFeedback,
            )

            FindInPagePanel(
                isVisible = isFindInPageVisible.value,
                searchText = findInPageText.value,
                searchResult = findInPageResult.value,
                onSearchTextChanged = { newText ->
                    findInPageText.value = newText
                    activeWebView?.findAllAsync(newText)
                },
                onFindNext = { activeWebView?.findNext(true) },
                onFindPrevious = { activeWebView?.findNext(false) },
                onClose = {
                    isFindInPageVisible.value = false
                    findInPageText.value = ""
                    activeWebView?.clearMatches()
                },
                browserSettings = browserSettings,
            )
            PromptPanel(
                isUrlBarVisible = isUrlBarVisible,
                activeWebView = activeWebView,
                browserSettings = browserSettings,
                //                modifier = modifier,
                isPromptPanelVisible = isPromptPanelVisible,
                onDismiss = onDismiss,
                state = state,
                promptComponentDisplayState = promptComponentDisplayState,

                )
            SettingsPanel(
                descriptionContent = descriptionContent,
                hapticFeedback = hapticFeedback,
                isSettingsPanelVisible = isSettingsPanelVisible,
                browserSettings = browserSettings,
                updateBrowserSettings = updateBrowserSettings,
                backgroundColor = backgroundColor,
                resetBrowserSettings = resetBrowserSettings,
                confirmationPopup = confirmationPopup,
                setIsSettingsPanelVisible = setIsSettingsPanelVisible

            )
            TabDataPanel(
                onAddToHomeScreen = onAddToHomeScreen,
                isTabDataPanelVisible = isTabDataPanelVisible,
                inspectingTab = inspectingTab,
                onDismiss = onTabDataPanelDismiss,
                browserSettings = browserSettings,
                siteSettings = siteSettings,
                onPermissionToggle = handlePermissionToggle,
                onClearSiteData = handleClearInspectedTabData,
                onCloseTab = handleCloseInspectedTab,
                onHistoryItemClicked = handleHistoryNavigation,
                webViewManager = webViewManager,
            )
            TabsPanel(

                isTabsPanelVisible = isTabsPanelVisible,
                tabs = tabs,
                activeTabIndex = activeTabIndex.value,
                browserSettings = browserSettings,
                onTabSelected = onTabSelected,
                onNewTabClicked = onNewTabClicked,
                hapticFeedback = hapticFeedback,
                onTabLongPressed = onTabLongPressed,
                updateInspectingTab = updateInspectingTab,
            )
            PermissionPanel(
                isUrlBarVisible = isUrlBarVisible,
                isPermissionPanelVisible = isPermissionPanelVisible,
                browserSettings = browserSettings,
                request = pendingPermissionRequest,
                onAllow = {
                    // When user clicks allow, launch the system dialog with the permissions
                    // stored in our request object.

                    pendingPermissionRequest?.let {
                        permissionLauncher.launch(it.permissionsToRequest.toTypedArray())
                    }
                },
                onDeny = {
                    // When user clicks deny, immediately invoke the stored onResult callback
                    // with an empty map (signifying denial) and clear the request.
                    onPermissionDeny()
                }
            )


            AnimatedVisibility(visible = suggestions.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = browserSettings.padding.dp)
                        .padding(top = browserSettings.padding.dp)
                        .clip(
                            RoundedCornerShape(
                                browserSettings.cornerRadiusForLayer(2).dp
                            )
                        )
//                        .border(
//                            1.dp,
//                            Color.White,
//                            RoundedCornerShape(
//                                cornerRadiusForLayer(
//                                    2,
//                                    browserSettings.deviceCornerRadius,
//                                    browserSettings.padding
//                                ).dp
//                            )
//                        )
                        .heightIn(max = 250.dp), // Prevent the list from being too tall
                    reverseLayout = true,
                ) {

                    items(suggestions) { suggestion ->
                        val iconRes = when (suggestion.source) {
                            SuggestionSource.HISTORY -> R.drawable.ic_history
                            SuggestionSource.GOOGLE -> R.drawable.ic_search
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionClick(suggestion) }
                                .padding(browserSettings.padding.dp * 2),
                            verticalAlignment = Alignment.CenterVertically // Align icon and text vertically
                        ) {
                            // 2. Add the search Icon
                            Icon(
                                painter = painterResource(id = iconRes), // Make sure you have this drawable
                                contentDescription = "Search suggestion",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp) // Give the icon a suitable size
                            )

                            // 3. Add a Spacer for visual separation
                            Spacer(modifier = Modifier.width(browserSettings.padding.dp))

                            // 4. Add the Text, which now takes the remaining space
                            Text(
                                text = suggestion.text,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f) // Ensures text fills available space
                            )

                            if (suggestion.source == SuggestionSource.HISTORY) {
                                IconButton(
                                    onClick = { onRemoveSuggestion(suggestion) },
                                    modifier = Modifier.size(24.dp) // A small, compact size
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close), // The 'X' icon
                                        contentDescription = "Remove from history",
                                        tint = Color.Gray // A subtle color
                                    )
                                }
                            }
                        }
                    }
                }
            }


            ConfirmationPanel(
                isUrlBarVisible = isUrlBarVisible,
                browserSettings = browserSettings,
                state = confirmationDisplayState, // Use the display state here
                isConfirmationPanelVisible = confirmationState != null // Visibility is controlled by the primary state
            )


            // URL BAR
            AnimatedVisibility(
                modifier = modifier
                    .pointerInput(Unit) {
                        // The long press on the UrlBar will activate the gesture

                    },
                visible = isUrlBarVisible,
                enter = fadeIn(
                    tween(
                        browserSettings.animationSpeedForLayer(1)
                    )
                ),
                exit = fadeOut(
                    tween(
                        browserSettings.animationSpeedForLayer(1)
                    )
                )
            ) {

                HorizontalPager(
                    state = bottomPanelPagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                       ,
                    contentPadding = PaddingValues(0.dp),
                    pageSpacing = browserSettings.padding.dp // Optional spacing
                ) { pageIndex ->

                    when (pageIndex) {
                        // --- LEFT BOX (Page 0) ---
                        BottomPanelMode.APPS.ordinal -> {
                            Box(
                                modifier = Modifier
                                    .height(
                                        browserSettings.heightForLayer(1).dp
                                    )
                                    .fillMaxWidth()
                                    .padding(browserSettings.padding.dp)
                                    .clip(RoundedCornerShape(browserSettings.cornerRadiusForLayer(1).dp))
                                    .clickable{
                                        resetBottomPanelTrigger.value = !resetBottomPanelTrigger.value
                                    }

                                ,
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                                    contentDescription = "toggle app",
                                    tint = Color.White
                                )
                            }
                        }

                        BottomPanelMode.SEARCH.ordinal -> {
                            Box (modifier = Modifier){

                                TextField(
                                    modifier = Modifier
                                        .height(
                                            browserSettings.heightForLayer(1).dp
                                        )
                                        .padding(browserSettings.padding.dp)
                                        .onSizeChanged { size ->
                                            setTextFieldHeightPx(size.height)
                                        }
                                        .fillMaxWidth()
                                        .focusRequester(urlBarFocusRequester)
                                        //                            .padding(horizontal = browserSettings.padding.dp, vertical = browserSettings.padding.dp / 2)
                                        .onFocusChanged {
                                            val resetUrl = activeWebView?.url ?: ""
                                            setIsFocusOnTextField(it.isFocused)
                                            if (it.isFocused) {
                                                setSavedPanelState(
                                                    PanelVisibilityState(
                                                        options = isOptionsPanelVisible,
                                                        tabs = isTabsPanelVisible,
                                                        downloads = isDownloadPanelVisible,
                                                        tabData = isTabDataPanelVisible,
                                                        nav = isNavPanelVisible
                                                    )
                                                )
                                                setIsOptionsPanelVisible(false)
                                                setIsTabsPanelVisible(false)
                                                Log.i("SETT", "SET")
                                                setIsDownloadPanelVisible(false)
                                                setIsTabDataPanelVisible(false)
                                                setIsNavPanelVisible(false)
                                                setIsSettingsPanelVisible(false)
                                                setIsUrlOverlayBoxVisible(false)

//                                    textFieldState.edit { selectAll() }
                                                textFieldState.setTextAndPlaceCursorAtEnd("")
                                            } else {
                                                if(isPinningApp.value) isPinningApp.value = false
                                                setIsUrlOverlayBoxVisible(true)
                                                savedPanelState?.let { savedState ->
                                                    setIsOptionsPanelVisible(savedState.options)
                                                    setIsTabsPanelVisible(savedState.tabs)
                                                    setIsDownloadPanelVisible(savedState.downloads)
                                                    setIsTabDataPanelVisible(savedState.tabData)
                                                    setIsNavPanelVisible(savedState.nav)
                                                    setSavedPanelState(null) // Clear the saved state
                                                }
                                                textFieldState.setTextAndPlaceCursorAtEnd(resetUrl.toDomain())


                                                setIsUrlOverlayBoxVisible(true)
                                            }
                                        }
//                                        .pointerInput(Unit) {
//                                            detectHorizontalDragGestures { _, dragAmount ->
//                                                if (dragAmount > 0) {
//                                                    val resetUrl =
//                                                        activeWebView?.url ?: ""
//                                                    textFieldState.setTextAndPlaceCursorAtEnd(
//                                                        resetUrl.toDomain()
//                                                    )
//                                                }
//                                            }
//                                        }

                                        .clip(
                                            RoundedCornerShape(
                                                browserSettings.cornerRadiusForLayer(2).dp
                                            )
                                        ),
                                    placeholder = { if (!isPinningApp.value) Text("search / url") else Text("pin label") },
                                    state = textFieldState,
                                    textStyle = LocalTextStyle.current.copy(
//                            fontFamily = FontFamily.Monospace,
                                        textAlign = if (isFocusOnTextField) TextAlign.Start else TextAlign.Center
                                    ),
//                        state = rememberTextFieldState("Hello"),
                                    lineLimits = TextFieldLineLimits.SingleLine,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                    onKeyboardAction = {
                                        val input = (textFieldState.text as String).trim()
                                        val resetUrl = activeWebView?.url ?: ""


                                        if (input.isEmpty()) {

                                            if (isPinningApp.value) {
                                                apps.add(
                                                    App(
                                                        label = tabs[activeTabIndex.value].currentTitle,
                                                        iconUrl = tabs[activeTabIndex.value].currentFaviconUrl,
                                                        url = resetUrl
                                                    )
                                                )
                                                isPinningApp.value = false
                                            }
                                            else {
                                                activeWebView?.reload()
                                            }


                                            textFieldState.setTextAndPlaceCursorAtEnd(resetUrl.toDomain())
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            setIsFocusOnTextField(false)

                                            return@TextField
                                        }


                                        val isUrl = try {
                                            Patterns.WEB_URL.matcher(input).matches() ||
                                                    (input.contains(".") && !input.contains(" "))
                                                    && !input.endsWith(".")
                                                    && !input.startsWith(".")
                                        } catch (_: Exception) {
                                            false
                                        }

                                        if (isPinningApp.value) {
                                            apps.add(App(
                                                label = input,
                                                iconUrl = tabs[activeTabIndex.value].currentFaviconUrl,
                                                url = resetUrl,
                                            ))
                                        } else { // search
                                            val finalUrl = if (isUrl) {
                                                if (input.startsWith("http://") || input.startsWith("https://")) {
                                                    input
                                                } else {
                                                    "https://$input"
                                                }
                                            } else {
                                                val encodedQuery =
                                                    URLEncoder.encode(
                                                        input,
                                                        StandardCharsets.UTF_8.toString()
                                                    )
                                                "https://www.google.com/search?q=$encodedQuery"
                                            }

                                            onNewUrl(finalUrl)
                                        }


                                        focusManager.clearFocus()
                                        keyboardController?.hide()

                                        setIsFocusOnTextField(false)
                                    },
                                    shape = RoundedCornerShape(
                                        browserSettings.cornerRadiusForLayer(2).dp
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Black,
                                        unfocusedContainerColor = Color.Black,
                                        cursorColor = Color.White,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,

                                        // 3. This is the key to removing the underline
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        errorIndicatorColor = Color.Transparent
                                    ),
                                )

                                if (isUrlOverlayBoxVisible) Box(
                                    modifier = Modifier
                                        .background(
                                            Color.Transparent, shape = RoundedCornerShape(
                                                browserSettings.cornerRadiusForLayer(1).dp
                                            )
                                        )
//                                        .background(Color.Cyan)
                                        .clip(
                                            RoundedCornerShape(
                                                browserSettings.cornerRadiusForLayer(1).dp
                                            )
                                        )
                                        .matchParentSize()
                                        .pointerInput(
                                            Unit,
                                            activeWebView?.canGoBack(),
                                            activeWebView?.canGoForward()
                                        ) {
                                            // 1. CAPTURE the CoroutineScope provided by pointerInput
                                            val coroutineScope =
                                                CoroutineScope(currentCoroutineContext())
                                            awaitEachGesture {
                                                val down = awaitFirstDown(requireUnconsumed = false)

                                                // 2. USE the captured scope to launch the long press job
                                                val longPressJob = coroutineScope.launch {
                                                    delay(viewConfiguration.longPressTimeoutMillis)

                                                    // LONG PRESS CONFIRMED
                                                    hapticFeedback.performHapticFeedback(
                                                        HapticFeedbackType.LongPress
                                                    )
                                                    focusManager.clearFocus(true)
                                                    setIsNavPanelVisible(true)
                                                    setActiveNavAction(GestureNavAction.NONE)

                                                }

                                                val drag =
                                                    awaitTouchSlopOrCancellation(down.id) { change, _ ->
                                                        if (longPressJob.isActive) {
                                                            longPressJob.cancel()
                                                        }
                                                        change.consume()
                                                    }

                                                if (longPressJob.isCompleted && !longPressJob.isCancelled) {
                                                    if (drag != null) {
                                                        var horizontalDragAccumulator = 0f
                                                        var verticalDragAccumulator = 0f
                                                        var previousAction =
                                                            GestureNavAction.REFRESH
                                                        val horizontalDragThreshold =
                                                            40.dp.toPx()

                                                        val verticalCancelThreshold =
                                                            -40.dp.toPx()


                                                        drag(drag.id) { change ->
                                                            change.consume()
                                                            horizontalDragAccumulator += change.position.x - change.previousPosition.x
                                                            verticalDragAccumulator += change.position.y - change.previousPosition.y


                                                            val newAction = when {
                                                                verticalDragAccumulator < verticalCancelThreshold -> {
                                                                    when {
                                                                        horizontalDragAccumulator < -horizontalDragThreshold -> GestureNavAction.CLOSE_TAB
                                                                        horizontalDragAccumulator > horizontalDragThreshold -> GestureNavAction.NEW_TAB
                                                                        else -> GestureNavAction.REFRESH
                                                                    }
                                                                }

                                                                horizontalDragAccumulator < -horizontalDragThreshold -> if (activeWebView?.canGoBack()
                                                                        ?: false
                                                                ) GestureNavAction.BACK else GestureNavAction.NONE

                                                                horizontalDragAccumulator > horizontalDragThreshold -> if (activeWebView?.canGoForward()
                                                                        ?: false
                                                                ) GestureNavAction.FORWARD else GestureNavAction.NONE

                                                                else -> GestureNavAction.NONE
                                                            }

                                                            if (newAction != previousAction) {
                                                                hapticFeedback.performHapticFeedback(
                                                                    HapticFeedbackType.TextHandleMove
                                                                )
                                                                previousAction = newAction
                                                            }
                                                            //                                                    activeNavAction = newAction
                                                            setActiveNavAction(newAction)
                                                        }

                                                        navigateWebView()
                                                    }
                                                } else {
                                                    if (drag != null) {

                                                        var horizontalDragAccumulator = 0f
                                                        var verticalDragAccumulator = 0f
//                                                        val horizontalDragThreshold =
//                                                            40.dp.toPx()

                                                        val verticalCancelThreshold =
                                                            -40.dp.toPx()


                                                        drag(drag.id) { change ->
                                                            change.consume()
                                                            horizontalDragAccumulator += change.position.x - change.previousPosition.x
                                                            verticalDragAccumulator += change.position.y - change.previousPosition.y


//                                                            coroutineScope.launch {
//                                                                bottomPanelPagerState.scrollBy(change.position.x)
//                                                            }

                                                            Log.i(
                                                                "DragTest",
                                                                "horizontalDragAccumulator : $horizontalDragAccumulator"
                                                            )
                                                            Log.i(
                                                                "DragTest",
                                                                "verticalDragAccumulator : $verticalDragAccumulator"
                                                            )
                                                            when {
//                                                                horizontalDragAccumulator < -horizontalDragThreshold -> { // left
//                                                                    Log.e("BotHDrag", "left")
//
//                                                                }
//
//                                                                horizontalDragAccumulator > horizontalDragThreshold -> { // right
//                                                                    Log.e("BotHDrag", "right")
//
//                                                                }
                                                                abs(horizontalDragAccumulator) > abs(
                                                                    verticalDragAccumulator
                                                                ) -> {
                                                                    setIsUrlOverlayBoxVisible(false)
                                                                }

                                                                verticalDragAccumulator < verticalCancelThreshold -> { // up
                                                                    Log.e("BotVDrag", "up")
                                                                    setIsOptionsPanelVisible(true)
                                                                }

                                                                verticalDragAccumulator > verticalCancelThreshold -> { // down
                                                                    Log.e("BotVDrag", "down")

                                                                    setIsOptionsPanelVisible(false)
                                                                }

//                                                                abs(horizontalDragAccumulator) > 0 -> {
//                                                                    setIsUrlOverlayBoxVisible(false)
//                                                                }

                                                                else -> {// nothing
                                                                }
                                                            }


//                                                if (newAction != previousAction) {
//                                                    hapticFeedback.performHapticFeedback(
//                                                        HapticFeedbackType.TextHandleMove
//                                                    )
//                                                    previousAction = newAction
//                                                }
//                                                //                                                    activeNavAction = newAction
//                                                setActiveNavAction(newAction)
                                                        }


                                                    } else {
                                                        // Gesture is fully over
                                                        if (longPressJob.isActive) {
                                                            longPressJob.cancel()
                                                            // This was a tap
                                                            urlBarFocusRequester.requestFocus()
                                                            setIsUrlOverlayBoxVisible(false)
                                                        }
                                                    }
                                                }

                                                //                                        // Gesture is fully over
                                                //                                        if (longPressJob.isActive) {
                                                //                                            longPressJob.cancel()
                                                //                                            // This was a tap
                                                //                                            focusRequester.requestFocus()
                                                //                                        }

                                                // Reset the UI state
                                                //                                        isNavPanelVisible = false
                                                setIsNavPanelVisible(false)
                                                //                                        activeNavAction = GestureNavAction.NONE
                                                setActiveNavAction(GestureNavAction.NONE)
                                            }
                                        }
                                ) {
                                }
                            }
                        }

                        BottomPanelMode.LOCK.ordinal -> {
                            Box(
                                modifier = Modifier
                                    .height(
                                        browserSettings.heightForLayer(1).dp
                                    )
                                    .fillMaxWidth()
                                    .padding(browserSettings.padding.dp)
                                    .clip(RoundedCornerShape(browserSettings.cornerRadiusForLayer(1).dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = if (isBottomPanelLock.value)R.drawable.ic_lock else R.drawable.ic_lock_open_right),
                                    contentDescription = "toggle bottom panel lock",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }


            // SETTING OPTIONS
            OptionsPanel(
                bottomPanelPagerState = bottomPanelPagerState,
                onCloseAllTabs = onCloseAllTabs,
                activeWebView = activeWebView,
                isFindInPageVisible = isFindInPageVisible,
                descriptionContent = descriptionContent,
                hapticFeedback = hapticFeedback,
                reopenClosedTab = reopenClosedTab,
                isSettingsPanelVisible = isSettingsPanelVisible,
                isOptionsPanelVisible = isOptionsPanelVisible,
                setIsOptionsPanelVisible = setIsOptionsPanelVisible,
                updateBrowserSettings = updateBrowserSettings,
                browserSettings = browserSettings,
                toggleIsTabsPanelVisible = toggleIsTabsPanelVisible,
                tabs = tabs,
                tabsPanelLock = tabsPanelLock,
                isDownloadPanelVisible = isDownloadPanelVisible,
                setIsDownloadPanelVisible = setIsDownloadPanelVisible,
                isCursorPadVisible = isCursorPadVisible,
                isCursorMode = isCursorMode,
                setIsCursorMode = setIsCursorMode,
                setIsSettingsPanelVisible = setIsSettingsPanelVisible,
                closedTabsCount = recentlyClosedTabs.size,
                addAppToPin = {
                    isPinningApp.value = true
                    urlBarFocusRequester.requestFocus()
                },
            )

            UrlEditPanel(
                isPinningApp = isPinningApp,
                isVisible = isPinningApp.value || (isFocusOnTextField && textFieldState.text.isBlank()),
                browserSettings = browserSettings,
                onCopyClick = {
                    val clipData = ClipData.newPlainText("url", activeWebView?.url ?: "")

                    clipboard.nativeClipboard.setPrimaryClip(clipData)

                },
                onEditClick = {
                    textFieldState.setTextAndPlaceCursorAtEnd(
                        if (isPinningApp.value) {
                            activeWebView?.title ?: ""
                        } else activeWebView?.url ?: ""
                    )
                    urlBarFocusRequester.requestFocus()
                    keyboardController?.show()
                },
                onDismiss = {
                    setIsFocusOnTextField(false)
                    focusManager.clearFocus()
                },
                activeWebViewTitle = activeWebView?.title ?: "",
            )
        }

    }
}