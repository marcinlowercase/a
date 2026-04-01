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

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.util.Patterns
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.DownloadItem
import marcinlowercase.a.core.data_class.JsDialogState
import marcinlowercase.a.core.data_class.PanelVisibilityState
import marcinlowercase.a.core.data_class.Suggestion
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.GestureNavAction
import marcinlowercase.a.core.enum_class.RevealState
import marcinlowercase.a.core.enum_class.SearchEngine
import marcinlowercase.a.core.enum_class.SuggestionSource
import marcinlowercase.a.core.function.toDomain
import marcinlowercase.a.core.function.webViewLoad
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.component.LoadingIndicator
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("FrequentlyChangingValue")
@Composable
fun BottomPanel(
    geckoViewRef: MutableState<GeckoView?>,
    floatingPanelBottomPadding: Dp,
    draggableState: AnchoredDraggableState<RevealState>,
    flingBehavior: FlingBehavior,
    onDownload: (String) -> Unit,

    textFieldState: TextFieldState,
    onCloseAllTabs: () -> Unit,
    onSuggestionClick: (Suggestion) -> Unit, // Changed from (String)
    onRemoveSuggestion: (Suggestion) -> Unit,


//    onAddToHomeScreen: () -> Unit,
    confirmationPopup: (message: Int, url: String, onConfirm: () -> Unit, onCancel: () -> Unit) -> Unit,
    urlBarFocusRequester: FocusRequester,

    updateInspectingTab: (Tab) -> Unit,
    isTabDataPanelVisible: Boolean,
    handleCloseInspectedTab: () -> Unit,
    handleClearInspectedTabData: () -> Unit,
    handlePermissionToggle: (domain: String?, permission: String, isGranted: Boolean) -> Unit,

    onDownloadRowClicked: (DownloadItem) -> Unit,
    onOpenFolderClicked: () -> Unit,

    activeSession: GeckoSession,

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

    val context = LocalContext.current

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

        val customIconUrlState = rememberTextFieldState("")
        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

        // --- NEW: Local Image Picker ---
        val isPickingImage = remember { mutableStateOf(false) }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: android.net.Uri? ->
            uri?.let {
                customIconUrlState.setTextAndPlaceCursorAtEnd(it.toString())
            }

            // CRITICAL FIX: Delay resetting the flag!
            // When returning from the picker, Android's onResume lifecycle often fires spurious
            // onFocusChanged(false) events. By waiting 500ms before resetting this flag,
            // we perfectly bridge over those false alarms and keep the pinning panel open!
            coroutineScope.launch {
                delay(500)
                isPickingImage.value = false
                delay(50) // Tiny layout buffer
                urlBarFocusRequester.requestFocus()
                keyboardController?.show()
            }

        }
        // Automatically clear the custom URL text box when the user finishes pinning or cancels
        LaunchedEffect(uiState.value.isPinningApp, uiState.value.isCloningBrowser) {
            if (!uiState.value.isPinningApp && !uiState.value.isCloningBrowser) {
                customIconUrlState.setTextAndPlaceCursorAtEnd("")
            }
        }

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
//                    .pointerInput(Unit) {
//                        awaitEachGesture {
//                            // Wait for the first touch-down event
//                            awaitFirstDown(requireUnconsumed = false)
//
//                            // Update the trigger. This causes the LaunchedEffect above to RESTART.
//                            interactionTrigger = System.currentTimeMillis()
//
//                            // We do NOT consume the event.
//                            // The click will pass through to buttons/pagers below normally.
//                        }
//                    }
//                    .graphicsLayer {
//                        scaleX = ghostPressScale.value
//                        scaleY = ghostPressScale.value
//                    }
                    .anchoredDraggable(
                        state = draggableState,
                        orientation = Orientation.Vertical,
                        flingBehavior = flingBehavior,
//                                            enabled = !isFocusOnTextField && contextMenuData == null && !isPromptPanelVisible  && (!isPermissionPanelVisible || (isPermissionPanelVisible &&  isUrlBarVisible) )
                        enabled = uiState.value.isUrlBarVisible && (!uiState.value.isFocusOnTextField && viewModel.contextMenuData.value == null && !uiState.value.isPromptPanelVisible && (!uiState.value.isPermissionPanelVisible))
                    )

            ) {

                LoadingIndicator()

                DescriptionPanel()


                NavigationPanel(
                    activeSession = activeSession
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
                                // 0 means no special flags (case-insensitive, forward direction)
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
                    onCloseAllTabs = onCloseAllTabs,
                    confirmationPopup = confirmationPopup,
                    changeBrowserIcon = {
                        viewModel.updateUI { it.copy(isCloningBrowser = true) }
                        urlBarFocusRequester.requestFocus()
                    }
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


                AnimatedVisibility(visible = viewModel.suggestions.isNotEmpty() && textFieldState.text.isNotEmpty() && uiState.value.isFocusOnUrlTextField) {
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
                                    .heightIn(min = settings.value.heightForLayer(3).dp)
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
                    visible = uiState.value.isUrlBarVisible && !viewModel.isStandaloneMode.value,
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
                    Box(modifier = Modifier) {
                        Column(
                            modifier = Modifier.onSizeChanged { size ->
                                setTextFieldHeightPx(size.height)
                            }
                        ) {
                            AnimatedVisibility(
                                visible = uiState.value.isPinningApp || uiState.value.isCloningBrowser,
                                enter = expandVertically(
                                    tween(
                                        settings.value.animationSpeedForLayer(
                                            1
                                        )
                                    )
                                ) + fadeIn(tween(settings.value.animationSpeedForLayer(1))),
                                exit = shrinkVertically(
                                    tween(
                                        settings.value.animationSpeedForLayer(
                                            1
                                        )
                                    )
                                ) + fadeOut(tween(settings.value.animationSpeedForLayer(1)))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = settings.value.padding.dp,
                                            end = settings.value.padding.dp,
                                            top = settings.value.padding.dp,
                                            bottom = 0.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
                                ) {
                                    TextField(
                                        modifier = Modifier
                                            .weight(1f) // Take up remaining space
                                            .heightIn(min = settings.value.heightForLayer(2).dp)
                                            .onFocusChanged { focusState ->
                                                viewModel.updateUI {
                                                    it.copy(isFocusOnIconUrlTextField = focusState.isFocused)
                                                }
                                                if (!focusState.isFocused) {
                                                    val input =
                                                        (customIconUrlState.text).toString().trim()
                                                    if (input.isNotEmpty()) {
                                                        // CRITICAL FIX: Allow content:// URIs from the local file picker!
                                                        val isValid =
                                                            Patterns.WEB_URL.matcher(input)
                                                                .matches() ||
                                                                    input.startsWith(
                                                                        "data:image",
                                                                        ignoreCase = true
                                                                    ) ||
                                                                    input.startsWith(
                                                                        "content://",
                                                                        ignoreCase = true
                                                                    ) ||
                                                                    input.startsWith(
                                                                        "<svg",
                                                                        ignoreCase = true
                                                                    ) // <-- NEW: Allow raw SVG text!

                                                        if (!isValid) {
                                                            customIconUrlState.setTextAndPlaceCursorAtEnd(
                                                                ""
                                                            )
                                                        }
                                                    }
                                                    val resetUrl = viewModel.activeTab!!.currentURL
                                                    coroutineScope.launch {
                                                        delay(150)
                                                        val currentState = viewModel.uiState.value
                                                        // Protect UI from tearing down if picker is open
                                                        if (!currentState.isFocusOnIconUrlTextField && !currentState.isFocusOnUrlTextField && !isPickingImage.value) {
                                                            if (currentState.isPinningApp) viewModel.updateUI {
                                                                it.copy(
                                                                    isPinningApp = false
                                                                )
                                                            }
                                                            if (currentState.isCloningBrowser) viewModel.updateUI {
                                                                it.copy(
                                                                    isCloningBrowser = false
                                                                )
                                                            }
                                                            if (currentState.isCreatingProfile) viewModel.updateUI {
                                                                it.copy(
                                                                    isCreatingProfile = false
                                                                )
                                                            }
                                                            if (currentState.isRenamingProfile) viewModel.updateUI {
                                                                it.copy(
                                                                    isRenamingProfile = false
                                                                )
                                                            }

                                                            currentState.savedPanelState?.let { savedState ->
                                                                viewModel.updateUI {
                                                                    it.copy(
                                                                        isOptionsPanelVisible = savedState.options,
                                                                        isDownloadPanelVisible = savedState.downloads,
                                                                        isTabDataPanelVisible = false,
                                                                        isNavPanelVisible = savedState.nav,
                                                                        savedPanelState = null,
                                                                    )
                                                                }
                                                                if (currentState.isTabsPanelLock) viewModel.updateUI {
                                                                    it.copy(isTabsPanelVisible = savedState.tabs)
                                                                }
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
                                                }
                                            },
                                        state = customIconUrlState,
                                        placeholder = {
                                            Text(stringResource(R.string.placeholder_icon_link), color = Color.Gray)
                                        },
                                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
                                        lineLimits = TextFieldLineLimits.SingleLine,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                        shape = RoundedCornerShape(
                                            settings.value.cornerRadiusForLayer(
                                                2
                                            ).dp
                                        ),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            cursorColor = Color.Black,
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent,
                                            errorIndicatorColor = Color.Transparent
                                        )
                                    )

                                    // --- NEW: Local Image Picker Button ---


                                    CustomIconButton(
                                        isLandscape = true,
                                        layer = 2,
                                        modifier = Modifier,
                                        onTap = {
                                            isPickingImage.value = true // Prevent UI from closing
                                            // Launch the native Android File Picker looking only for images
                                            imagePickerLauncher.launch("image/*")
                                        },
                                        buttonDescription = "pick local image",
                                        painterId = R.drawable.ic_image,
                                        isWhite = true
                                    )
                                }
                            }
                            val defaultIconUrl = stringResource(R.string.bold_icon_url)
                            TextField(
                                modifier = Modifier
                                    .heightIn(
                                        min = settings.value.heightForLayer(1).dp
                                    )
                                    .onSizeChanged { size ->
                                        setTextFieldHeightPx(size.height)
                                    }
                                    .fillMaxWidth()
                                    .focusRequester(urlBarFocusRequester)

                                    .onFocusChanged { focusState ->
                                        val resetUrl = viewModel.activeTab!!.currentURL
                                        viewModel.updateUI { it.copy(isFocusOnUrlTextField = focusState.isFocused) }

                                        if (focusState.isFocused) {

                                            if (!(uiState.value.isPinningApp || uiState.value.isCloningBrowser)) {
                                                viewModel.updateUI {
                                                    it.copy(
                                                        savedPanelState = PanelVisibilityState(
                                                            options = draggableState.currentValue == RevealState.Visible,
                                                            tabs = uiState.value.isTabsPanelVisible,
                                                            downloads = uiState.value.isDownloadPanelVisible,
                                                            tabData = isTabDataPanelVisible,
                                                            nav = uiState.value.isNavPanelVisible
                                                        )
                                                    )
                                                }

                                                viewModel.updateUI {
                                                    it.copy(
                                                        isOptionsPanelVisible = false,
                                                        isTabsPanelVisible = false,
                                                        isDownloadPanelVisible = false,
                                                        isTabDataPanelVisible = false,
                                                        isNavPanelVisible = false,
                                                        isSettingsPanelVisible = false,
                                                        isUrlOverlayBoxVisible = false,
                                                        isAppsPanelVisible = false,
                                                    )
                                                }
                                                textFieldState.setTextAndPlaceCursorAtEnd("")
                                            }

                                        } else {
                                            // THE FIX: Wait a tiny fraction of a second before tearing down the UI.
                                            // This gives Compose enough time to shift focus to the Icon TextField!
                                            coroutineScope.launch {
                                                delay(150)
                                                val currentState = viewModel.uiState.value
                                                // Protect the UI from tearing down if the picker is open
                                                if (!currentState.isFocusOnIconUrlTextField && !currentState.isFocusOnUrlTextField && !isPickingImage.value) {
                                                    if (currentState.isPinningApp) viewModel.updateUI {
                                                        it.copy(
                                                            isPinningApp = false
                                                        )
                                                    }
                                                    if (currentState.isCloningBrowser) viewModel.updateUI {
                                                        it.copy(
                                                            isCloningBrowser = false
                                                        )
                                                    }
                                                    if (currentState.isCreatingProfile) viewModel.updateUI {
                                                        it.copy(
                                                            isCreatingProfile = false
                                                        )
                                                    }
                                                    if (currentState.isRenamingProfile) viewModel.updateUI {
                                                        it.copy(
                                                            isRenamingProfile = false
                                                        )
                                                    }

                                                    currentState.savedPanelState?.let { savedState ->
                                                        viewModel.updateUI {
                                                            it.copy(
                                                                isOptionsPanelVisible = savedState.options,
                                                                isDownloadPanelVisible = savedState.downloads,
                                                                isTabDataPanelVisible = false,
                                                                isNavPanelVisible = savedState.nav,
                                                                savedPanelState = null,
                                                            )
                                                        }
                                                        if (currentState.isTabsPanelLock) viewModel.updateUI {
                                                            it.copy(isTabsPanelVisible = savedState.tabs)
                                                        }
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
                                        }
                                    }
                                    .clip(
                                        RoundedCornerShape(
                                            settings.value.cornerRadiusForLayer(1).dp
                                        )
                                    ),
                                contentPadding = PaddingValues(horizontal = settings.value.cornerRadiusForLayer(1).dp),
                                placeholder = {
                                    when {
                                        uiState.value.isCreatingProfile -> Text(stringResource(R.string.placeholder_profile_label))
                                        uiState.value.isRenamingProfile -> Text(stringResource(R.string.placeholder_profile_label))
                                        uiState.value.isPinningApp -> Text(stringResource(R.string.placeholder_pin_label))
                                        uiState.value.isCloningBrowser -> Text(stringResource(R.string.placeholder_browser_label))
                                        else -> Text(stringResource(R.string.placeholder_url))
                                    }
                                },
                                state = textFieldState,
                                textStyle = LocalTextStyle.current.copy(
                                    //                            fontFamily = FontFamily.Monospace,
                                    textAlign = if (uiState.value.isFocusOnUrlTextField || uiState.value.isFocusOnIconUrlTextField) TextAlign.Start else TextAlign.Center
                                ),
                                //                        state = rememberTextFieldState("Hello"),
                                lineLimits = TextFieldLineLimits.SingleLine,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                onKeyboardAction = {
                                    val input = (textFieldState.text as String).trim()
                                    val resetUrl = viewModel.activeTab!!.currentURL


                                    if (input.isEmpty()) {

                                        when {
                                            uiState.value.isCreatingProfile -> {
                                                viewModel.createNewProfile()
                                                viewModel.updateUI { it.copy(isAppsPanelVisible = true) }
                                            }

                                            uiState.value.isPinningApp -> {
                                                viewModel.pinApp(
                                                    context = context,
                                                    title = viewModel.activeTab!!.currentTitle,
                                                    url = resetUrl,
                                                    iconUrl = viewModel.activeTab!!.currentFaviconUrl,
                                                )
                                                viewModel.updateUI { it.copy(isPinningApp = false) }
                                            }
                                            uiState.value.isCloningBrowser -> {
                                                viewModel.generateAndInstallWebApk(
                                                    context = context,
                                                    title = "13rowser",
                                                    url = resetUrl,
                                                    iconUrl = defaultIconUrl,
                                                    isFullBrowser = true,
                                                )
                                                viewModel.updateUI { it.copy(isCloningBrowser = false) }
                                            }

                                            else -> activeSession.reload()
                                        }

                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        textFieldState.setTextAndPlaceCursorAtEnd(resetUrl.toDomain())

                                        viewModel.updateUI { it.copy(isFocusOnUrlTextField = false) }
                                        return@TextField
                                    }




                                    when {

                                        uiState.value.isRenamingProfile -> {
                                            viewModel.renameProfile(input)
                                            viewModel.updateUI { it.copy(isAppsPanelVisible = true) }
                                        }

                                        uiState.value.isCreatingProfile -> {
                                            viewModel.createNewProfile(input)
                                            viewModel.updateUI { it.copy(isAppsPanelVisible = true) }
                                        }

                                        uiState.value.isPinningApp -> {

                                            val customIconInput =
                                                (customIconUrlState.text).toString().trim()
                                            val finalIconUrl =
                                                customIconInput.ifEmpty { viewModel.activeTab!!.currentFaviconUrl }

                                            viewModel.pinApp(
                                                context = context,
                                                title = input,
                                                url = resetUrl,
                                                iconUrl = finalIconUrl,
                                            )
                                            viewModel.updateUI { it.copy(isPinningApp = false) }
                                        }
                                        uiState.value.isCloningBrowser -> {
                                            val customIconInput =
                                                (customIconUrlState.text).toString().trim()
                                            val finalIconUrl =
                                                customIconInput.ifEmpty { defaultIconUrl }

                                            viewModel.generateAndInstallWebApk(
                                                context = context,
                                                title = input,
                                                url = settings.value.defaultUrl,
                                                iconUrl = finalIconUrl,
                                                isFullBrowser = true,
                                            )
                                            viewModel.updateUI { it.copy(isCloningBrowser = false) }

                                        }


                                        else -> {
                                            // search
                                            val isUrl = try {
                                                input.startsWith("about:", ignoreCase = true) ||
                                                        input.startsWith(
                                                            "javascript:",
                                                            ignoreCase = true
                                                        ) ||
                                                        input.startsWith(
                                                            "file:",
                                                            ignoreCase = true
                                                        ) ||
                                                        input.startsWith(
                                                            "data:",
                                                            ignoreCase = true
                                                        ) ||
                                                        Patterns.WEB_URL.matcher(input).matches() ||
                                                        (input.contains(".") && !input.contains(" "))
                                                        && !input.endsWith(".")
                                                        && !input.startsWith(".")
                                            } catch (_: Exception) {
                                                false
                                            }
                                            val finalUrl = if (isUrl) {
                                                if (input.startsWith(
                                                        "http://",
                                                        ignoreCase = true
                                                    ) ||
                                                    input.startsWith(
                                                        "https://",
                                                        ignoreCase = true
                                                    ) ||
                                                    input.startsWith("about:", ignoreCase = true) ||
                                                    input.startsWith(
                                                        "javascript:",
                                                        ignoreCase = true
                                                    ) ||
                                                    input.startsWith("file:", ignoreCase = true) ||
                                                    input.startsWith("data:", ignoreCase = true)
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
                        }

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
//                                                    if (abs(horizontalDragAccumulator) > abs(
//                                                            verticalDragAccumulator
//                                                        )
//                                                    ) {
//                                                        viewModel.updateUI {
//                                                            it.copy(
//                                                                isUrlOverlayBoxVisible = false
//                                                            )
//                                                        }
//
//                                                    }


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

                // SETTING OPTIONS
                OptionsPanelWrapper(
                    dragOffset = draggableState.offset
                ) {
                    Column {
                        OptionsPanel(
                            onCloseAllTabs = onCloseAllTabs,
                            confirmationPopup = confirmationPopup,
                            changeBrowserIcon = {
                                viewModel.updateUI { it.copy(isCloningBrowser = true) }
                                urlBarFocusRequester.requestFocus()
                            }
                        )

                        AppsPanel(
                            onAppClick = { app ->
                                webViewLoad(activeSession, app.url, settings.value)
                                viewModel.updateUI { it.copy(isSettingsPanelVisible = false) }
                                viewModel.updateUI { it.copy(isUrlBarVisible = false) }
                            },
                            addAppToPin = {
                                viewModel.updateUI { it.copy(isPinningApp = true) }
                                urlBarFocusRequester.requestFocus()
                            },
                            createNewProfile = {
                                viewModel.updateUI { it.copy(isCreatingProfile = true) }
                                urlBarFocusRequester.requestFocus()
                            },
                            renameProfile = {
                                viewModel.updateUI { it.copy(isRenamingProfile = true) }
                                urlBarFocusRequester.requestFocus()
                            },
                            deleteProfile = {
                                viewModel.updateUI {
                                    it.copy(
                                        isOptionsPanelVisible = false,
                                        isAppsPanelVisible = false
                                    )
                                }
                                confirmationPopup(
                                    R.string.confirm_delete_profile,
                                    "",
                                    {
                                        viewModel.deleteProfile()
                                    },
                                    {}
                                )

                            },
                            draggableState = draggableState

                        )

                    }
                }


                val browserName = stringResource(R.string.app_name)
                val profileText = stringResource(R.string.placeholder_profile)
                TextEditPanel(
//                    currentRotation =  currentRotation,
                    isVisible = uiState.value.isPinningApp || uiState.value.isCloningBrowser || (uiState.value.isFocusOnUrlTextField && textFieldState.text.isBlank()),
                    onCopyClick = {
                        val clipData =
                            ClipData.newPlainText("url", viewModel.activeTab!!.currentURL)

                        clipboard.nativeClipboard.setPrimaryClip(clipData)

                    },
                    onEditClick = {
                        textFieldState.setTextAndPlaceCursorAtEnd(
                            when {
                                uiState.value.isCreatingProfile -> "$profileText "
                                uiState.value.isRenamingProfile -> viewModel.profiles.find { it.id == viewModel.activeProfileId.value }?.name
                                    ?: ""

                                uiState.value.isPinningApp -> viewModel.activeTab!!.currentTitle
                                uiState.value.isCloningBrowser -> browserName
                                else -> viewModel.activeTab!!.errorState?.failingUrl
                                    ?: viewModel.activeTab!!.currentURL
                            }
                        )
                        urlBarFocusRequester.requestFocus()
                        keyboardController?.show()
                    },
                    onDismiss = {
                        viewModel.updateUI { it.copy(isFocusOnUrlTextField = false) }
                        focusManager.clearFocus()
                    },
                    onAddToHomeScreen = {
                        val input = (textFieldState.text).toString().trim()
                        // 2. If the user cleared the text, fallback to the website's title
                        val finalTitle = input.ifEmpty { viewModel.activeTab!!.currentTitle }
                        val customIconInput = (customIconUrlState.text).toString().trim()
                        val finalIconUrl =
                            customIconInput.ifEmpty { viewModel.activeTab!!.currentFaviconUrl }
                        viewModel.generateAndInstallWebApk(
                            context = context,
                            title = finalTitle,
                            url = viewModel.activeTab!!.currentURL,
                            iconUrl = finalIconUrl,
                            isFullBrowser = false
                        )
                        viewModel.updateUI { it.copy(isPinningApp = false) }
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                    activeWebViewTitle = viewModel.activeTab!!.currentTitle,
                )
            }
        }

    }
}