package marcinlowercase.a.ui.panel

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
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
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.App
import marcinlowercase.a.core.data_class.DownloadItem
import marcinlowercase.a.core.data_class.JsDialogState
import marcinlowercase.a.core.data_class.PanelVisibilityState
import marcinlowercase.a.core.data_class.Suggestion
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.BottomPanelMode
import marcinlowercase.a.core.enum_class.DragDirection
import marcinlowercase.a.core.enum_class.GestureNavAction
import marcinlowercase.a.core.enum_class.RevealState
import marcinlowercase.a.core.enum_class.SearchEngine
import marcinlowercase.a.core.enum_class.SuggestionSource
import marcinlowercase.a.core.function.buttonSettingsForLayer
import marcinlowercase.a.core.function.consumeChangePointerInput
import marcinlowercase.a.core.function.toDomain
import marcinlowercase.a.core.function.webViewLoad
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.abs

@SuppressLint("FrequentlyChangingValue")
@Composable
fun BottomPanel(
    setIsOptionsPanelVisible: (Boolean) -> Job,
    geckoViewRef: MutableState<GeckoView?>,
    floatingPanelBottomPadding: Dp,
    optionsPanelHeightPx: Float,
    draggableState: AnchoredDraggableState<RevealState>,
    flingBehavior: FlingBehavior,

    bottomPanelPagerState: PagerState,
    onDownload: (String) -> Unit,

    textFieldState: TextFieldState,
    onCloseAllTabs: () -> Unit,
    onSuggestionClick: (Suggestion) -> Unit, // Changed from (String)
    onRemoveSuggestion: (Suggestion) -> Unit,


//    onAddToHomeScreen: () -> Unit,
    confirmationPopup: (message: String, url: String, onConfirm: () -> Unit, onCancel: () -> Unit) -> Unit,
    urlBarFocusRequester: FocusRequester,

    updateInspectingTab: (Tab) -> Unit,
    isTabDataPanelVisible: Boolean,
    handleCloseInspectedTab: () -> Unit,
    handleClearInspectedTabData: () -> Unit,
    handlePermissionToggle: (domain: String?, permission: String, isGranted: Boolean) -> Unit,
    onTabLongPressed: (Tab) -> Unit,

    onDownloadRowClicked: (DownloadItem) -> Unit,
    onOpenFolderClicked: () -> Unit,

    activeSession: GeckoSession,

    isTabsPanelVisible: Boolean,
    navigateWebView: () -> Unit,
    hapticFeedback: HapticFeedback,
    setActiveNavAction: (GestureNavAction) -> Unit,
    state: JsDialogState?,
    onDismiss: () -> Unit,
    permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
    modifier: Modifier,
//    url: String,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    onNewUrl: (String) -> Unit = {},
    setTextFieldHeightPx: (Int) -> Unit = {},
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    AnimatedVisibility(
        modifier = modifier,
        visible = uiState.value.isBottomPanelVisible,
        enter = slideInVertically(
            animationSpec = tween(
                settings.value.animationSpeedForLayer(0)
            ),
            initialOffsetY = { it }
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                settings.value.animationSpeedForLayer(0)
            ),
            targetOffsetY = { it }
        )
    ) {
        val clipboard = LocalClipboard.current

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(settings.value.padding.dp)
                    .windowInsetsPadding(WindowInsets.ime)
                    //                .padding(webViewPaddingValue)
                    .padding(bottom = floatingPanelBottomPadding)


                    .clip(
                        RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(1).dp
                        )
                    )

                    .background(
                        Color.Black,
                    )
                    .anchoredDraggable(
                        state = draggableState,
                        orientation = Orientation.Vertical,
                        flingBehavior = flingBehavior,
//                                            enabled = !isFocusOnTextField && contextMenuData == null && !isPromptPanelVisible  && (!isPermissionPanelVisible || (isPermissionPanelVisible &&  isUrlBarVisible) )
                        enabled = uiState.value.isUrlBarVisible && (!uiState.value.isFocusOnTextField && viewModel.contextMenuData.value == null && !uiState.value.isPromptPanelVisible && (!uiState.value.isPermissionPanelVisible))
                    )

            ) {


                DescriptionPanel()

                AppsPanel(
                    onAppClick = { app ->
                        webViewLoad(activeSession, app.url, settings.value)
                        viewModel.updateUI { it.copy(isSettingsPanelVisible = false) }
                        viewModel.updateUI { it.copy(isUrlBarVisible = false) }
                    },
                )
                NavigationPanel(
                    isNavPanelVisible = uiState.value.isNavPanelVisible,
                )

                DownloadPanel(
                    confirmationPopup = confirmationPopup,
                    isDownloadPanelVisible = uiState.value.isDownloadPanelVisible,
                    onDownloadRowClicked = onDownloadRowClicked,
                    onOpenFolderClicked = onOpenFolderClicked,
                )

                ContextMenuPanel(
                    onDownload = onDownload,
                )

                FindInPagePanel(
                    isVisible = uiState.value.isFindInPageVisible,
                    onSearchTextChanged = { newText ->
                        viewModel.findInPageText.value = newText
                        activeSession.let { session ->
                            if (newText.isEmpty()) {
                                session.finder.clear()
                                // Update UI to show 0/0 results
                                viewModel.findInPageResult.value = 0 to 0
                            } else {
                                // 0 means no special flags (case insensitive, forward direction)
                                session.finder.find(newText, 0).then { result ->
                                    // result is of type GeckoSession.FinderResult?
                                    if (result != null) {
                                        // Update your Compose state directly here
                                        // result.current is 1-based index (WebView was 0-based, Gecko is 1-based usually, check logic)
                                        // Actually Gecko's 'current' is 0-based index of the match, or -1 if none.

                                        val currentMatch =
                                            if (result.total > 0) result.current + 1 else 0
                                        viewModel.findInPageResult.value =
                                            currentMatch to result.total
                                    }
                                    GeckoResult.fromValue(result)
                                }
                            }
                        }
                    },
                    onFindNext = {
                        activeSession.finder.find(
                            viewModel.findInPageText.value,
                            GeckoSession.FINDER_FIND_FORWARD
                        )
                            .then { result ->
                                // Update UI with new result.current
                                if (result != null) {
                                    // Update your Compose state directly here
                                    // result.current is 1-based index (WebView was 0-based, Gecko is 1-based usually, check logic)
                                    // Actually Gecko's 'current' is 0-based index of the match, or -1 if none.

                                    val currentMatch = if (result.total > 0) result.current else 0
                                    viewModel.findInPageResult.value = currentMatch to result.total
                                }

                                GeckoResult.fromValue(result)


                            }
                    },
                    onFindPrevious = {
                        activeSession.finder.find(
                            viewModel.findInPageText.value,
                            GeckoSession.FINDER_FIND_BACKWARDS
                        )
                            .then { result ->
                                // Update UI
                                if (result != null) {
                                    // Update your Compose state directly here
                                    // result.current is 1-based index (WebView was 0-based, Gecko is 1-based usually, check logic)
                                    // Actually Gecko's 'current' is 0-based index of the match, or -1 if none.

                                    val currentMatch = if (result.total > 0) result.current else 0
                                    viewModel.findInPageResult.value = currentMatch to result.total
                                }
                                GeckoResult.fromValue(result)
                            }
                    },
                    onClose = {
                        viewModel.updateUI { it.copy(isFindInPageVisible = false) }
                        viewModel.findInPageText.value = ""
                        activeSession.finder.clear()
                    },
                )
                PromptPanel(
                    geckoViewRef = geckoViewRef,
                    isUrlBarVisible = uiState.value.isUrlBarVisible,
                    onDismiss = onDismiss,
                    state = state,
                )
                SettingsPanel(
                    confirmationPopup = confirmationPopup,
                )
                TabDataPanel(
                    //                onAddToHomeScreen = onAddToHomeScreen,
                    isTabDataPanelVisible = isTabDataPanelVisible,
                    onDismiss = { viewModel.updateUI { it.copy(isTabDataPanelVisible = false) } },
                    onPermissionToggle = handlePermissionToggle,
                    onClearSiteData = handleClearInspectedTabData,
                    onCloseTab = handleCloseInspectedTab,
                )
                TabsPanel(
                    isTabsPanelVisible = isTabsPanelVisible,
                    onTabLongPressed = onTabLongPressed,
                    updateInspectingTab = updateInspectingTab,
                )
                PermissionPanel(
                    isUrlBarVisible = uiState.value.isUrlBarVisible,
                    onAllow = {
                        // When user clicks allow, launch the system dialog with the permissions
                        // stored in our request object.

                        viewModel.pendingPermissionRequest.value?.let {
                            if (it.permissionsToRequest.contains(Manifest.permission.CAMERA) || it.permissionsToRequest.contains(
                                    Manifest.permission.RECORD_AUDIO
                                )
                            ) {
                                viewModel.allowMediaPermissionRequest(it.permissionsToRequest.associateWith { true })
                            } else {
                                permissionLauncher.launch(it.permissionsToRequest.toTypedArray())

                            }

                        }
                    },

                    )


                AnimatedVisibility(visible = viewModel.suggestions.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = settings.value.padding.dp)
                            .padding(top = settings.value.padding.dp)
                            .clip(
                                RoundedCornerShape(
                                    settings.value.cornerRadiusForLayer(2).dp
                                )
                            )
                            .heightIn(max = settings.value.maxContainerSizeForLayer(3).dp), // Prevent the list from being too tall
                        reverseLayout = true,
                    ) {

                        items(viewModel.suggestions) { suggestion ->
                            val iconRes = when (suggestion.source) {
                                SuggestionSource.HISTORY -> R.drawable.ic_history
                                SuggestionSource.GOOGLE -> R.drawable.ic_search
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = settings.value.padding.dp)
                                    .padding(
                                        top = settings.value.padding.dp,
                                        bottom = if (viewModel.suggestions.indexOf(suggestion) == 0) settings.value.padding.dp else 0.dp
                                    )
                                    .height(settings.value.heightForLayer(3).dp)
                                    .clip(
                                        RoundedCornerShape(
                                            settings.value.cornerRadiusForLayer(3).dp
                                        )
                                    )
                                    .clickable { onSuggestionClick(suggestion) }

                                    .padding(settings.value.padding.dp),
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
                                Spacer(modifier = Modifier.width(settings.value.padding.dp))

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
                ConfirmationPanel()
                // URL BAR
                AnimatedVisibility(
                    modifier = modifier
                        .pointerInput(Unit) {
                            // The long press on the UrlBar will activate the gesture

                        },
                    visible = uiState.value.isUrlBarVisible,
                    enter = fadeIn(
                        tween(
                            settings.value.animationSpeedForLayer(1)
                        )
                    ),
                    exit = fadeOut(
                        tween(
                            settings.value.animationSpeedForLayer(1)
                        )
                    )
                ) {
                    HorizontalPager(
                        state = bottomPanelPagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        contentPadding = PaddingValues(0.dp),
                        pageSpacing = settings.value.padding.dp // Optional spacing
                    ) { pageIndex ->
                        LaunchedEffect(pageIndex) {
                            if (pageIndex != BottomPanelMode.SEARCH.ordinal) {
                                setIsOptionsPanelVisible(false)
                            }
                        }
                        when (pageIndex) {
                            // --- LEFT BOX (Page 0) ---
                            BottomPanelMode.APPS.ordinal -> {
                                Column(
                                    modifier = Modifier
                                        .height(
                                            settings.value.heightForLayer(1).dp
                                        )
                                        .fillMaxWidth()
                                        .padding(settings.value.padding.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                settings.value.cornerRadiusForLayer(
                                                    1
                                                ).dp
                                            )
                                        )
                                        .consumeChangePointerInput(dragDirection = DragDirection.Vertical)
                                ) {

                                    AnimatedVisibility(
                                        visible = viewModel.inspectingAppId.longValue > 0L,
                                        enter = fadeIn(
                                            tween(
                                                settings.value.animationSpeedForLayer(
                                                    0
                                                )
                                            )
                                        ),
                                        exit = fadeOut(
                                            tween(
                                                settings.value.animationSpeedForLayer(
                                                    0
                                                )
                                            )
                                        ),
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(
                                                    RoundedCornerShape(
                                                        settings.value.cornerRadiusForLayer(
                                                            1
                                                        ).dp
                                                    )
                                                )
                                        ) {

                                            val currentIndex =
                                                viewModel.apps.indexOfFirst { it.id == viewModel.inspectingAppId.longValue }
                                            Box(
                                                modifier = Modifier
                                                    .buttonSettingsForLayer(
                                                        2,
                                                        settings.value,
                                                        currentIndex > 0
                                                    )
                                                    .weight(1f)
                                                    .clickable {
                                                        if (currentIndex > 0) {
                                                            viewModel.swapApps(
                                                                currentIndex,
                                                                currentIndex - 1
                                                            )
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_arrow_downward),
                                                    contentDescription = "edit pin",
                                                    tint = Color.Black
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(settings.value.padding.dp))

                                            Box(
                                                modifier = Modifier
                                                    .buttonSettingsForLayer(
                                                        2,
                                                        settings.value,
                                                        false
                                                    )
                                                    .weight(1f)
                                                    .clickable {
                                                        viewModel.removeApp(viewModel.inspectingAppId.longValue)
                                                        viewModel.inspectingAppId.longValue = 0L
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_delete_forever),
                                                    contentDescription = "delete pin",
                                                    tint = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(settings.value.padding.dp))
                                            Box(
                                                modifier = Modifier
                                                    .buttonSettingsForLayer(
                                                        2,
                                                        settings.value,
                                                        currentIndex < viewModel.apps.lastIndex && currentIndex >= 0
                                                    )
                                                    .weight(1f)
                                                    .clickable {

                                                        if (currentIndex < viewModel.apps.lastIndex) {
                                                            viewModel.swapApps(
                                                                currentIndex,
                                                                currentIndex + 1
                                                            )
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_arrow_upward),
                                                    contentDescription = "edit pin",
                                                    tint = Color.Black
                                                )
                                            }
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = viewModel.inspectingAppId.longValue == 0L,
                                        enter = fadeIn(
                                            tween(
                                                settings.value.animationSpeedForLayer(
                                                    0
                                                )
                                            )
                                        ),
                                        exit = fadeOut(
                                            tween(
                                                settings.value.animationSpeedForLayer(
                                                    0
                                                )
                                            )
                                        ),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .height(
                                                    settings.value.heightForLayer(1).dp
                                                )
                                                .fillMaxWidth()
                                                .clip(
                                                    RoundedCornerShape(
                                                        settings.value.cornerRadiusForLayer(
                                                            1
                                                        ).dp
                                                    )
                                                )
                                                .clickable {
                                                    viewModel.resetBottomPanelTrigger.value =
                                                        !viewModel.resetBottomPanelTrigger.value
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_arrow_forward),
                                                contentDescription = "toggle app",
                                                tint = Color.White
                                            )
                                        }
                                    }

                                }

                            }

                            BottomPanelMode.SEARCH.ordinal -> {
                                Box(modifier = Modifier) {

                                    TextField(
                                        modifier = Modifier
                                            .height(
                                                settings.value.heightForLayer(1).dp
                                            )
                                            .padding(settings.value.padding.dp)
                                            .onSizeChanged { size ->
                                                setTextFieldHeightPx(size.height)
                                            }
                                            .fillMaxWidth()
                                            .focusRequester(urlBarFocusRequester)
                                            //                            .padding(horizontal = settings.value.padding.dp, vertical = settings.value.padding.dp / 2)
                                            .onFocusChanged { focusState ->
                                                val resetUrl = viewModel.activeTab!!.currentURL
                                                viewModel.updateUI { it.copy(isFocusOnUrlTextField = focusState.isFocused) }

                                                if (focusState.isFocused) {

                                                    //                                                geckoViewRef.value?.clearFocus()
                                                    //                                                CoroutineScope(Dispatchers.Main).launch {
                                                    //                                                    delay(300) // 50ms is usually enough to beat the race condition
                                                    //                                                    keyboardController?.show()
                                                    //                                                }

                                                    viewModel.updateUI {
                                                        it.copy(
                                                            savedPanelState = PanelVisibilityState(
                                                                options = draggableState.currentValue == RevealState.Visible,
                                                                tabs = isTabsPanelVisible,
                                                                downloads = uiState.value.isDownloadPanelVisible,
                                                                tabData = isTabDataPanelVisible,
                                                                nav = uiState.value.isNavPanelVisible
                                                            )
                                                        )
                                                    }

                                                    setIsOptionsPanelVisible(false)
                                                    viewModel.updateUI { it.copy(isTabsPanelVisible = false) }
                                                    viewModel.updateUI {
                                                        it.copy(
                                                            isDownloadPanelVisible = false
                                                        )
                                                    }

                                                    viewModel.updateUI {
                                                        it.copy(
                                                            isTabDataPanelVisible = false
                                                        )
                                                    }
                                                    viewModel.updateUI { it.copy(isNavPanelVisible = false) }
                                                    viewModel.updateUI {
                                                        it.copy(
                                                            isSettingsPanelVisible = false
                                                        )
                                                    }

                                                    viewModel.updateUI {
                                                        it.copy(
                                                            isUrlOverlayBoxVisible = false
                                                        )
                                                    }

                                                    //                                    textFieldState.edit { selectAll() }
                                                    textFieldState.setTextAndPlaceCursorAtEnd("")


                                                } else {
                                                    if (uiState.value.isPinningApp) viewModel.updateUI {
                                                        it.copy(
                                                            isPinningApp = false
                                                        )
                                                    }
                                                    viewModel.updateUI {
                                                        it.copy(
                                                            isUrlOverlayBoxVisible = true
                                                        )
                                                    }

                                                    uiState.value.savedPanelState?.let { savedState ->
                                                        if (bottomPanelPagerState.currentPage == BottomPanelMode.SEARCH.ordinal) {
                                                            setIsOptionsPanelVisible(savedState.options)
                                                            viewModel.updateUI {
                                                                it.copy(
                                                                    isTabsPanelVisible = savedState.tabs
                                                                )
                                                            }
                                                            viewModel.updateUI {
                                                                it.copy(
                                                                    isDownloadPanelVisible = savedState.downloads
                                                                )
                                                            }
                                                            viewModel.updateUI {
                                                                it.copy(
                                                                    isTabDataPanelVisible = false
                                                                )
                                                            }
                                                            viewModel.updateUI {
                                                                it.copy(
                                                                    isNavPanelVisible = savedState.nav
                                                                )
                                                            }
                                                        }

                                                        viewModel.updateUI { it.copy(savedPanelState = null) }
                                                    }
                                                    textFieldState.setTextAndPlaceCursorAtEnd(
                                                        resetUrl.toDomain()
                                                    )


                                                    viewModel.updateUI {
                                                        it.copy(
                                                            isUrlOverlayBoxVisible = true
                                                        )
                                                    }
                                                }
                                            }
                                            //

                                            .clip(
                                                RoundedCornerShape(
                                                    settings.value.cornerRadiusForLayer(2).dp
                                                )
                                            ),
                                        placeholder = {
                                            if (!uiState.value.isPinningApp) Text("search / url") else Text(
                                                "pin label"
                                            )
                                        },
                                        state = textFieldState,
                                        textStyle = LocalTextStyle.current.copy(
                                            //                            fontFamily = FontFamily.Monospace,
                                            textAlign = if (uiState.value.isFocusOnUrlTextField) TextAlign.Start else TextAlign.Center
                                        ),
                                        //                        state = rememberTextFieldState("Hello"),
                                        lineLimits = TextFieldLineLimits.SingleLine,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                        onKeyboardAction = {
                                            val input = (textFieldState.text as String).trim()
                                            val resetUrl = viewModel.activeTab!!.currentURL


                                            if (input.isEmpty()) {

                                                if (uiState.value.isPinningApp) {
                                                    viewModel.pinApp(
                                                        title = viewModel.activeTab!!.currentTitle,
                                                        url = resetUrl,
                                                        iconUrl = viewModel.activeTab!!.currentFaviconUrl,
                                                    )
                                                    viewModel.updateUI { it.copy(isPinningApp = false) }
                                                } else {
                                                    activeSession.reload()
                                                }
                                                focusManager.clearFocus()
                                                keyboardController?.hide()

                                                textFieldState.setTextAndPlaceCursorAtEnd(resetUrl.toDomain())

                                                viewModel.updateUI { it.copy(isFocusOnUrlTextField = false) }

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

                                            if (uiState.value.isPinningApp) {
                                                viewModel.apps.add(
                                                    App(
                                                        id = System.currentTimeMillis(),
                                                        label = input,
                                                        iconUrl = viewModel.activeTab!!.currentFaviconUrl,
                                                        url = resetUrl,
                                                    )
                                                )
                                            } else { // search
                                                val finalUrl = if (isUrl) {
                                                    if (input.startsWith("http://") || input.startsWith(
                                                            "https://"
                                                        )
                                                    ) {
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
                                                    //                                                "https://www.google.com/search?q=$encodedQuery"
                                                    //                                                "https://duckduckgo.com/?q=$encodedQuery"
                                                    //                                                "https://www.bing.com/search?q=$encodedQuery"
                                                    SearchEngine.entries[settings.value.searchEngine].getSearchUrl(
                                                        encodedQuery
                                                    )
                                                }

                                                onNewUrl(finalUrl)
                                            }


                                            focusManager.clearFocus()
                                            keyboardController?.hide()

                                            viewModel.updateUI { it.copy(isFocusOnUrlTextField = false) }
                                        },
                                        shape = RoundedCornerShape(
                                            settings.value.cornerRadiusForLayer(2).dp
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

                                    if (uiState.value.isUrlOverlayBoxVisible && !uiState.value.isFocusOnUrlTextField) Box(
                                        modifier = Modifier
                                            .background(
                                                Color.Transparent, shape = RoundedCornerShape(
                                                    settings.value.cornerRadiusForLayer(1).dp
                                                )
                                            )
                                            .clip(
                                                RoundedCornerShape(
                                                    settings.value.cornerRadiusForLayer(1).dp
                                                )
                                            )

                                            .matchParentSize()
                                            .pointerInput(
                                                Unit,
                                                viewModel.activeTab!!.canGoBack,
                                                viewModel.activeTab!!.canGoForward,

                                                ) {
                                                // 1. CAPTURE the CoroutineScope provided by pointerInput
                                                val coroutineScope =
                                                    CoroutineScope(currentCoroutineContext())
                                                awaitEachGesture {
                                                    val down =
                                                        awaitFirstDown(requireUnconsumed = false)

                                                    // 2. USE the captured scope to launch the long press job
                                                    val longPressJob = coroutineScope.launch {
                                                        delay(viewConfiguration.longPressTimeoutMillis)

                                                        // LONG PRESS CONFIRMED
                                                        hapticFeedback.performHapticFeedback(
                                                            HapticFeedbackType.LongPress
                                                        )
                                                        focusManager.clearFocus(true)
                                                        viewModel.updateUI {
                                                            it.copy(
                                                                isNavPanelVisible = true
                                                            )
                                                        }
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

                                                                    horizontalDragAccumulator < -horizontalDragThreshold -> if (viewModel.activeTab!!.canGoBack
                                                                    ) GestureNavAction.BACK else GestureNavAction.NONE

                                                                    horizontalDragAccumulator > horizontalDragThreshold -> if (viewModel.activeTab!!.canGoForward
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

                                                            drag(drag.id) { change ->
                                                                horizontalDragAccumulator += change.position.x - change.previousPosition.x
                                                                verticalDragAccumulator += change.position.y - change.previousPosition.y

//                                                                if (isFocusOnUrlTextField ) change.consume()
                                                                if (abs(horizontalDragAccumulator) > abs(
                                                                        verticalDragAccumulator
                                                                    )
                                                                ) {
                                                                    viewModel.updateUI {
                                                                        it.copy(
                                                                            isUrlOverlayBoxVisible = false
                                                                        )
                                                                    }

                                                                }


                                                            }


                                                        } else {
                                                            // Gesture is fully over
                                                            if (longPressJob.isActive) {
                                                                longPressJob.cancel()
                                                                // This was a tap

                                                                if (urlBarFocusRequester.requestFocus()) {
                                                                    keyboardController?.show()
                                                                } else {
                                                                    urlBarFocusRequester.requestFocus()
                                                                }
                                                                viewModel.updateUI {
                                                                    it.copy(
                                                                        isUrlOverlayBoxVisible = false
                                                                    )
                                                                }
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
                                                    viewModel.updateUI { it.copy(isNavPanelVisible = false) }
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
                                            settings.value.heightForLayer(1).dp
                                        )
                                        .fillMaxWidth()
                                        .padding(settings.value.padding.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                settings.value.cornerRadiusForLayer(
                                                    1
                                                ).dp
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (settings.value.isFullscreenMode) R.drawable.ic_fullscreen else R.drawable.ic_fullscreen_exit),
                                        contentDescription = "toggle bottom panel lock",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                // SETTING OPTIONS
                OptionsPanelWrapper(
                    maxHeight = optionsPanelHeightPx,
                    dragOffset = draggableState.offset
                ) {
                    OptionsPanel(
                        onCloseAllTabs = onCloseAllTabs,
                        setIsOptionsPanelVisible = setIsOptionsPanelVisible,
                        addAppToPin = {
                            viewModel.updateUI { it.copy(isPinningApp = true) }
                            urlBarFocusRequester.requestFocus()
                        },
                    )
                }
                TextEditPanel(
//                    currentRotation =  currentRotation,
                    isVisible = uiState.value.isPinningApp || (uiState.value.isFocusOnUrlTextField && textFieldState.text.isBlank()),
                    onCopyClick = {
                        val clipData =
                            ClipData.newPlainText("url", viewModel.activeTab!!.currentURL)

                        clipboard.nativeClipboard.setPrimaryClip(clipData)

                    },
                    onEditClick = {
                        textFieldState.setTextAndPlaceCursorAtEnd(
                            if (uiState.value.isPinningApp) {
                                viewModel.activeTab!!.currentTitle
                            } else viewModel.activeTab!!.currentURL
                        )
                        urlBarFocusRequester.requestFocus()
                        keyboardController?.show()
                    },
                    onDismiss = {
                        viewModel.updateUI { it.copy(isFocusOnUrlTextField = false) }
                        focusManager.clearFocus()
                    },
                    activeWebViewTitle = viewModel.activeTab!!.currentTitle,
                )
            }
        }

    }
}