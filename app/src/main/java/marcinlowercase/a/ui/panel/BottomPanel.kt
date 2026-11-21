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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt
import marcinlowercase.a.core.function.rememberAnchoredDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import marcinlowercase.a.core.data_class.LandscapeInputSession
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.nativeClipboardManager
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
import marcinlowercase.a.core.enum_class.AppState
import marcinlowercase.a.core.enum_class.GestureNavAction
import marcinlowercase.a.core.enum_class.RevealState
import marcinlowercase.a.core.enum_class.SearchEngine
import marcinlowercase.a.core.enum_class.SuggestionSource
import marcinlowercase.a.core.function.toDomain
import marcinlowercase.a.core.function.webViewLoad
import marcinlowercase.a.core.server.LocalAppServer
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.component.LoadingIndicator
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.text.TextRange
import marcinlowercase.a.core.enum_class.BrowserShortcut
import marcinlowercase.a.core.function.rememberIsStudioKeyboard
import marcinlowercase.a.core.manager.KeyboardShortcutManager

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

    //string
    val loginFailedText = stringResource(R.string.ui_login_failed)


    val speed = settings.value.animationSpeedForLayer(0)

// Detects if the sheet was already open on screen (meaning this is a mode switch!)
    var wasPanelVisible by remember { mutableStateOf(uiState.value.isBottomPanelVisible) }
    val isModeSwitch = uiState.value.isBottomPanelVisible && wasPanelVisible
    SideEffect {
        wasPanelVisible = uiState.value.isBottomPanelVisible
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = uiState.value.appState == AppState.REGULAR && uiState.value.isBottomPanelVisible,
        enter = if (isModeSwitch) {
            // Returning from Build Mode: slide in from left to right
            slideInHorizontally(tween(speed)) { -it }
        } else {
            // Normal reveal from BackSquare: slide up vertically
            slideInVertically(tween(speed)) { it }
        },
        exit = if (uiState.value.isBottomPanelVisible) {
            // Entering Build Mode: slide out to the LEFT
            slideOutHorizontally(tween(speed)) { -it }
        } else {
            // Normal dismiss on webview touch: slide down vertically
            slideOutVertically(tween(speed)) { it }
        }
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
                delay(500.milliseconds)
                isPickingImage.value = false
                delay(50.milliseconds) // Tiny layout buffer
                urlBarFocusRequester.requestFocus()
                keyboardController?.show()
            }
        }


        val isDragEnabled = uiState.value.isUrlBarVisible &&
                !uiState.value.isFocusOnTextField &&
                viewModel.contextMenuData.value == null &&
                !uiState.value.isPromptPanelVisible &&
                !uiState.value.isPermissionPanelVisible &&
                !uiState.value.isNavPanelVisible

        val isLandscapeLayout = uiState.value.isLandscape && !uiState.value.isOnFullscreenVideo
        var currentSettingView by remember { mutableStateOf(SettingPanelView.MAIN) }
        var hasLandscapeUrlDraft by remember { mutableStateOf(false) }

        LaunchedEffect(viewModel.activeTab?.id, viewModel.activeProfileId.value) {
            hasLandscapeUrlDraft = false
        }

        val density = LocalDensity.current
        val landscapeOptionsWidthDp = settings.value.heightForLayer(2).dp + settings.value.padding.dp * 3
        val landscapeOptionsWidthPx = with(density) { landscapeOptionsWidthDp.toPx() }
        val landscapeAppsHeightDp = settings.value.maxContainerSizeForLayer(
            3,
            uiState.value.windowMode,
            isLandscape = true
        ).dp + settings.value.padding.dp
        val landscapeAppsHeightPx = with(density) { landscapeAppsHeightDp.toPx() }

        var landscapeOptionsSettled by remember { mutableStateOf(RevealState.Hidden) }
        val landscapeOptionsAnchors = remember(landscapeOptionsWidthPx, landscapeOptionsSettled) {
            DraggableAnchors {
                RevealState.Hidden at 0f
                RevealState.Visible at landscapeOptionsWidthPx
            }
        }
        val landscapeOptionsDraggableState = rememberAnchoredDraggableState(
            initialValue = landscapeOptionsSettled,
            anchors = landscapeOptionsAnchors
        )
        val landscapeOptionsFling = AnchoredDraggableDefaults.flingBehavior(
            state = landscapeOptionsDraggableState,
            positionalThreshold = { distance -> distance * 0.5f },
        )

        var landscapeAppsSettled by remember { mutableStateOf(RevealState.Hidden) }
        val landscapeAppsAnchors = remember(landscapeAppsHeightPx, landscapeAppsSettled) {
            DraggableAnchors {
                RevealState.Hidden at 0f
                RevealState.Visible at -landscapeAppsHeightPx
            }
        }
        val landscapeAppsDraggableState = rememberAnchoredDraggableState(
            initialValue = landscapeAppsSettled,
            anchors = landscapeAppsAnchors
        )
        val landscapeAppsFling = AnchoredDraggableDefaults.flingBehavior(
            state = landscapeAppsDraggableState,
            positionalThreshold = { distance -> distance * 0.5f },
        )

        if (isLandscapeLayout) {
            LaunchedEffect(landscapeOptionsDraggableState.settledValue) {
                landscapeOptionsSettled = landscapeOptionsDraggableState.settledValue
                val shouldBeVisible = landscapeOptionsDraggableState.settledValue == RevealState.Visible
                if (uiState.value.isOptionsPanelVisible != shouldBeVisible) {
                    viewModel.updateUI { it.copy(isOptionsPanelVisible = shouldBeVisible) }
                }
            }
            LaunchedEffect(
                uiState.value.isOptionsPanelVisible,
                landscapeOptionsWidthPx,
                landscapeOptionsDraggableState.settledValue,
                landscapeOptionsDraggableState.isAnimationRunning
            ) {
                val target = if (uiState.value.isOptionsPanelVisible) RevealState.Visible else RevealState.Hidden
                val targetOffset = landscapeOptionsDraggableState.anchors.positionOf(target)
                val isOffsetAtTarget = !targetOffset.isNaN() &&
                    kotlin.math.abs(landscapeOptionsDraggableState.offset - targetOffset) < 1f
                if (landscapeOptionsDraggableState.currentValue != target ||
                    landscapeOptionsDraggableState.targetValue != target ||
                    (!landscapeOptionsDraggableState.isAnimationRunning && !isOffsetAtTarget)
                ) {
                    landscapeOptionsDraggableState.animateTo(
                        targetValue = target,
                        animationSpec = tween(settings.value.animationSpeedForLayer(1))
                    )
                }
            }

            LaunchedEffect(landscapeAppsDraggableState.settledValue) {
                landscapeAppsSettled = landscapeAppsDraggableState.settledValue
                val shouldBeVisible = landscapeAppsDraggableState.settledValue == RevealState.Visible
                if (uiState.value.isAppsPanelVisible != shouldBeVisible) {
                    viewModel.updateUI { it.copy(isAppsPanelVisible = shouldBeVisible) }
                }
            }
            LaunchedEffect(
                uiState.value.isAppsPanelVisible,
                landscapeAppsHeightPx,
                landscapeAppsDraggableState.settledValue,
                landscapeAppsDraggableState.isAnimationRunning
            ) {
                val target = if (uiState.value.isAppsPanelVisible) RevealState.Visible else RevealState.Hidden
                val targetOffset = landscapeAppsDraggableState.anchors.positionOf(target)
                val isOffsetAtTarget = !targetOffset.isNaN() &&
                    kotlin.math.abs(landscapeAppsDraggableState.offset - targetOffset) < 1f
                if (landscapeAppsDraggableState.currentValue != target ||
                    landscapeAppsDraggableState.targetValue != target ||
                    (!landscapeAppsDraggableState.isAnimationRunning && !isOffsetAtTarget)
                ) {
                    landscapeAppsDraggableState.animateTo(
                        targetValue = target,
                        animationSpec = tween(settings.value.animationSpeedForLayer(1))
                    )
                }
            }
        }

        // automatically clear the custom url text box when pinning ends
        LaunchedEffect(uiState.value.isPinningApp) {
            if (!uiState.value.isPinningApp) {
                customIconUrlState.setTextAndPlaceCursorAtEnd("")
            }
        }

        // shared find-in-page finder lifecycle
        LaunchedEffect(uiState.value.isFindInPageVisible) {
            if (uiState.value.isFindInPageVisible) {
                textFieldState.setTextAndPlaceCursorAtEnd(
                    viewModel.findInPageTextFieldState.text.toString()
                )
            } else {
                viewModel.findInPageTextFieldState.setTextAndPlaceCursorAtEnd("")
                viewModel.findInPageResult.value = 0 to 0
                activeSession.finder.clear()
                if (!uiState.value.isFocusOnUrlTextField) {
                    val liveUrl = viewModel.activeTab?.currentURL ?: ""
                    textFieldState.setTextAndPlaceCursorAtEnd(liveUrl.toDomain())
                }
            }
        }

        LaunchedEffect(uiState.value.isFindInPageVisible, activeSession) {
            if (uiState.value.isFindInPageVisible) {
                androidx.compose.runtime.snapshotFlow {
                    textFieldState.text.toString()
                }.collect { newText ->
                    viewModel.findInPageTextFieldState.setTextAndPlaceCursorAtEnd(newText)
                    if (newText.isEmpty()) {
                        activeSession.finder.clear()
                        viewModel.findInPageResult.value = 0 to 0
                    } else {
                        activeSession.finder.find(newText, 0).then { result ->
                            if (result != null) {
                                val currentMatch =
                                    if (result.total > 0) result.current + 1 else 0
                                viewModel.findInPageResult.value = currentMatch to result.total
                            }
                            GeckoResult.fromValue(result)
                        }
                    }
                }
            }
        }

        val driveConsentLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            coroutineScope.launch {
                val token = viewModel.driveSyncManager.handleDriveAuthResult(result.data)
                viewModel.onDriveAuthResult(token)
            }
        }

        LaunchedEffect(viewModel.pendingDriveAuthResolution.value) {
            viewModel.pendingDriveAuthResolution.value?.let { request ->
                driveConsentLauncher.launch(request)
                viewModel.pendingDriveAuthResolution.value = null
            }
        }

        val profileText = stringResource(R.string.placeholder_profile)
        val isStudioKeyboard = rememberIsStudioKeyboard()

        val copyCurrentUrl: () -> Unit = {
            val activeUrl = viewModel.activeTab!!.currentURL
            val cleanUrl = LocalAppServer.formatForDisplay(activeUrl)
            val clipData = ClipData.newPlainText("url", cleanUrl)
            clipboard.nativeClipboardManager.setPrimaryClip(clipData)
        }

        val editCurrentUrl: () -> Unit = {
            viewModel.keyboardShortcutManager.clearPendingNewTab()
            val activeUrl = viewModel.activeTab!!.currentURL
            val cleanUrl = LocalAppServer.formatForDisplay(activeUrl)
            textFieldState.setTextAndPlaceCursorAtEnd(
                when {
                    uiState.value.isCreatingProfile -> "$profileText "
                    uiState.value.isRenamingProfile -> viewModel.profiles.find { it.id == viewModel.activeProfileId.value }?.name ?: ""
                    uiState.value.isPinningApp -> viewModel.activeTab!!.currentTitle
                    cleanUrl.startsWith(LocalAppServer.VIRTUAL_SCHEME) -> cleanUrl
                    else -> viewModel.activeTab!!.errorState?.failingUrl ?: activeUrl
                }
            )
            urlBarFocusRequester.requestFocus()
            keyboardController?.show()
        }

        val dismissUrlBar: () -> Unit = {
            viewModel.keyboardShortcutManager.clearPendingNewTab()
            viewModel.keyboardShortcutManager.clearPendingUrlFocusAction()
            viewModel.updateUI {
                it.copy(
                    isFocusOnUrlTextField = false,
                    isFindInPageVisible = false
                )
            }
            focusManager.clearFocus()
            keyboardController?.hide()
        }

        val submitUrlBar: (Boolean) -> Unit = { forceNewTab ->
            hasLandscapeUrlDraft = false
            if (uiState.value.isFindInPageVisible && !forceNewTab) {
                val query = textFieldState.text.toString()
                if (query.isNotEmpty()) {
                    activeSession.finder.find(query, GeckoSession.FINDER_FIND_FORWARD)
                        .then { result ->
                            if (result != null) {
                                val currentMatch =
                                    if (result.total > 0) result.current else 0
                                viewModel.findInPageResult.value =
                                    currentMatch to result.total
                            }
                            GeckoResult.fromValue(result)
                        }
                }
                keyboardController?.hide()
            } else {
                val openInNewTab = forceNewTab || viewModel.keyboardShortcutManager.consumePendingNewTab()
                val input = (textFieldState.text as String).trim()
                val resetUrl = viewModel.activeTab!!.currentURL

                if (input.isEmpty()) {
                    if (openInNewTab && !uiState.value.isCreatingProfile && !uiState.value.isPinningApp && !uiState.value.isRenamingProfile) {
                        viewModel.createNewTab(viewModel.activeTabIndex.value + 1, "")
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        viewModel.updateUI { it.copy(isFocusOnUrlTextField = false) }
                    } else {
                        when {
                            uiState.value.isCreatingProfile -> {
                                viewModel.createNewProfile()
                                viewModel.updateUI { it.copy(isAppsPanelVisible = true) }
                            }

                            uiState.value.isPinningApp -> {
                                viewModel.pinApp(
                                    title = viewModel.activeTab!!.currentTitle,
                                    url = resetUrl,
                                    iconUrl = viewModel.activeTab!!.currentFaviconUrl,
                                )
                                viewModel.updateUI { it.copy(isPinningApp = false) }
                            }

                            uiState.value.isRenamingProfile -> {
                            }

                            else -> activeSession.reload()
                        }

                        focusManager.clearFocus()
                        keyboardController?.hide()
                        val liveUrl = viewModel.activeTab?.currentURL ?: ""
                        textFieldState.setTextAndPlaceCursorAtEnd(
                            LocalAppServer.formatForDisplay(liveUrl)
                        )
                        viewModel.updateUI { it.copy(isFocusOnUrlTextField = false) }
                    }
                } else {
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
                                title = input,
                                url = resetUrl,
                                iconUrl = finalIconUrl,
                                onPinned = { finalUrl ->
                                    onNewUrl(finalUrl)
                                    textFieldState.setTextAndPlaceCursorAtEnd(
                                        LocalAppServer.formatForDisplay(finalUrl)
                                    )
                                }
                            )
                            viewModel.updateUI { it.copy(isPinningApp = false) }
                        }

                        else -> {
                            val isRawHtml = input.startsWith("<!DOCTYPE", ignoreCase = true) ||
                                input.startsWith("<html", ignoreCase = true) ||
                                (input.startsWith("<") && input.contains("</html>", ignoreCase = true))

                            if (isRawHtml) {
                                val extractedTitle = Regex("<title>(.*?)</title>", RegexOption.IGNORE_CASE)
                                    .find(input)?.groupValues?.get(1)?.trim() ?: "Local App"

                                (context.applicationContext as? marcinlowercase.a.CustomApplication)?.localAppServer?.currentDraftHtml = input

                                val virtualDraftUrl = "local://draft/index.html"
                                if (openInNewTab) {
                                    viewModel.createNewTab(viewModel.activeTabIndex.value + 1, virtualDraftUrl)
                                } else {
                                    viewModel.activeTab?.let { tab ->
                                        viewModel.updateTabById(tab.id) {
                                            it.copy(currentURL = virtualDraftUrl, currentTitle = extractedTitle)
                                        }
                                    }
                                    onNewUrl(virtualDraftUrl)
                                    textFieldState.setTextAndPlaceCursorAtEnd(
                                        LocalAppServer.formatForDisplay(virtualDraftUrl)
                                    )
                                }
                            } else if (input.startsWith("local:", ignoreCase = true)) {
                                val virtualUrl = if (input.startsWith(LocalAppServer.VIRTUAL_SCHEME, ignoreCase = true)) {
                                    input
                                } else {
                                    "local://${input.removePrefix("local:").removePrefix("/")}"
                                }

                                if (openInNewTab) {
                                    viewModel.createNewTab(viewModel.activeTabIndex.value + 1, virtualUrl)
                                } else {
                                    viewModel.activeTab?.let { tab ->
                                        viewModel.updateTabById(tab.id) { it.copy(currentURL = virtualUrl) }
                                    }
                                    onNewUrl(virtualUrl)
                                    textFieldState.setTextAndPlaceCursorAtEnd(
                                        LocalAppServer.formatForDisplay(virtualUrl)
                                    )
                                }
                            } else {
                                val isUrl = try {
                                    input.startsWith("about:", ignoreCase = true) ||
                                        input.startsWith("javascript:", ignoreCase = true) ||
                                        input.startsWith("file:", ignoreCase = true) ||
                                        input.startsWith("data:", ignoreCase = true) ||
                                        Patterns.WEB_URL.matcher(input).matches() ||
                                        (input.contains(".") && !input.contains(" ")) &&
                                        !input.endsWith(".") &&
                                        !input.startsWith(".")
                                } catch (_: Exception) {
                                    false
                                }

                                val finalUrl = if (isUrl) {
                                    if (input.startsWith("http://", ignoreCase = true) ||
                                        input.startsWith("https://", ignoreCase = true) ||
                                        input.startsWith("about:", ignoreCase = true) ||
                                        input.startsWith("javascript:", ignoreCase = true) ||
                                        input.startsWith("file:", ignoreCase = true) ||
                                        input.startsWith("data:", ignoreCase = true)
                                    ) {
                                        input
                                    } else {
                                        "https://$input"
                                    }
                                } else {
                                    val encodedQuery = URLEncoder.encode(
                                        input,
                                        StandardCharsets.UTF_8.toString()
                                    )
                                    SearchEngine.entries[settings.value.searchEngine].getSearchUrl(
                                        encodedQuery
                                    )
                                }

                                if (openInNewTab) {
                                    viewModel.createNewTab(viewModel.activeTabIndex.value + 1, finalUrl)
                                } else {
                                    onNewUrl(finalUrl)
                                }
                            }
                        }
                    }

                    focusManager.clearFocus()
                    keyboardController?.hide()
                    viewModel.updateUI { it.copy(isFocusOnUrlTextField = false) }
                }
            }
        }

        val placeholderUrlText = stringResource(R.string.placeholder_url)
        val placeholderPinText = stringResource(R.string.placeholder_pin_label)
        val placeholderProfileText = stringResource(R.string.placeholder_profile_label)
        val placeholderFindText = stringResource(R.string.ui_find_in_page)
        val placeholderIconLinkText = stringResource(R.string.placeholder_icon_link)

        val openLandscapeUrlInput: (KeyboardShortcutManager.UrlFocusAction?) -> Unit = { focusAction ->
            val currentUi = viewModel.uiState.value
            val activeUrl = viewModel.activeTab?.currentURL ?: ""
            val cleanUrl = LocalAppServer.formatForDisplay(activeUrl)
            val fullUrl = if (cleanUrl.startsWith(LocalAppServer.VIRTUAL_SCHEME)) {
                cleanUrl
            } else {
                viewModel.activeTab?.errorState?.failingUrl ?: activeUrl
            }

            val initialText = when {
                currentUi.isFindInPageVisible -> viewModel.findInPageTextFieldState.text.toString()
                currentUi.isCreatingProfile -> ""
                currentUi.isRenamingProfile -> viewModel.profiles.find { it.id == viewModel.activeProfileId.value }?.name ?: ""
                currentUi.isPinningApp -> if (focusAction == KeyboardShortcutManager.UrlFocusAction.SELECT_ALL) viewModel.activeTab?.currentTitle ?: "" else ""
                focusAction == KeyboardShortcutManager.UrlFocusAction.SELECT_ALL -> fullUrl
                focusAction == KeyboardShortcutManager.UrlFocusAction.EMPTY -> ""
                hasLandscapeUrlDraft -> textFieldState.text.toString()
                else -> ""
            }

            val placeholder = when {
                currentUi.isFindInPageVisible -> placeholderFindText
                currentUi.isCreatingProfile || currentUi.isRenamingProfile -> placeholderProfileText
                currentUi.isPinningApp -> placeholderPinText
                else -> placeholderUrlText
            }

            val kbType = when {
                currentUi.isFindInPageVisible || currentUi.isCreatingProfile || currentUi.isRenamingProfile || currentUi.isPinningApp -> KeyboardType.Text
                else -> KeyboardType.Uri
            }

            val actionType = when {
                currentUi.isFindInPageVisible -> ImeAction.Search
                currentUi.isCreatingProfile || currentUi.isRenamingProfile || currentUi.isPinningApp -> ImeAction.Done
                else -> ImeAction.Go
            }

            viewModel.landscapeInputSession.value = LandscapeInputSession(
                initialText = initialText,
                selectAllOnOpen = focusAction == KeyboardShortcutManager.UrlFocusAction.SELECT_ALL,
                placeholder = placeholder,
                keyboardType = kbType,
                imeAction = actionType,
                isUrlBar = !currentUi.isFindInPageVisible && !currentUi.isPinningApp && !currentUi.isCreatingProfile && !currentUi.isRenamingProfile,
                onTextChanged = if (currentUi.isFindInPageVisible) {
                    { live -> textFieldState.setTextAndPlaceCursorAtEnd(live) }
                } else null,
                onCommit = { typedText, submitAction ->
                    textFieldState.setTextAndPlaceCursorAtEnd(typedText)
                    if (submitAction) {
                        hasLandscapeUrlDraft = false
                        submitUrlBar(false)
                    } else {
                        if (currentUi.isFindInPageVisible) {
                            viewModel.findInPageTextFieldState.setTextAndPlaceCursorAtEnd(typedText)
                        } else if (typedText.isNotBlank()) {
                            hasLandscapeUrlDraft = true
                        } else {
                            hasLandscapeUrlDraft = false
                            viewModel.keyboardShortcutManager.clearPendingNewTab()
                            if (currentUi.isPinningApp) viewModel.updateUI { it.copy(isPinningApp = false) }
                            if (currentUi.isCreatingProfile) viewModel.updateUI { it.copy(isCreatingProfile = false) }
                            if (currentUi.isRenamingProfile) viewModel.updateUI { it.copy(isRenamingProfile = false) }
                            val liveUrl = viewModel.activeTab?.currentURL ?: ""
                            textFieldState.setTextAndPlaceCursorAtEnd(liveUrl.toDomain())
                        }
                    }
                },
                onCancel = {
                    hasLandscapeUrlDraft = false
                    viewModel.keyboardShortcutManager.clearPendingNewTab()
                    if (currentUi.isFindInPageVisible) {
                        viewModel.updateUI { it.copy(isFindInPageVisible = false) }
                    } else {
                        if (currentUi.isPinningApp) viewModel.updateUI { it.copy(isPinningApp = false) }
                        if (currentUi.isCreatingProfile) viewModel.updateUI { it.copy(isCreatingProfile = false) }
                        if (currentUi.isRenamingProfile) viewModel.updateUI { it.copy(isRenamingProfile = false) }
                        val liveUrl = viewModel.activeTab?.currentURL ?: ""
                        textFieldState.setTextAndPlaceCursorAtEnd(liveUrl.toDomain())
                    }
                }
            )
        }

        val openLandscapeIconInput: () -> Unit = {
            viewModel.landscapeInputSession.value = LandscapeInputSession(
                initialText = customIconUrlState.text.toString(),
                placeholder = placeholderIconLinkText,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done,
                onCommit = { typedText, _ ->
                    val input = typedText.trim()
                    val isValid = input.isEmpty() ||
                        Patterns.WEB_URL.matcher(input).matches() ||
                        input.startsWith("data:image", ignoreCase = true) ||
                        input.startsWith("content://", ignoreCase = true) ||
                        input.startsWith("<svg", ignoreCase = true)
                    customIconUrlState.setTextAndPlaceCursorAtEnd(if (isValid) input else "")
                }
            )
        }

        LaunchedEffect(Unit) {
            viewModel.keyboardShortcutManager.shortcutEvents.collect { shortcut ->
                if (isLandscapeLayout) {
                    when (shortcut) {
                        BrowserShortcut.FOCUS_OR_EDIT_URL -> openLandscapeUrlInput(KeyboardShortcutManager.UrlFocusAction.SELECT_ALL)
                        BrowserShortcut.NEW_TAB -> openLandscapeUrlInput(KeyboardShortcutManager.UrlFocusAction.EMPTY)
                        BrowserShortcut.OPEN_FIND_IN_PAGE -> openLandscapeUrlInput(null)
                        else -> {}
                    }
                } else {
                    when (shortcut) {
                        BrowserShortcut.DISMISS_URL_BAR -> dismissUrlBar()
                        BrowserShortcut.FOCUS_OR_EDIT_URL -> editCurrentUrl()
                        BrowserShortcut.NEW_TAB -> {
                            textFieldState.setTextAndPlaceCursorAtEnd("")
                            urlBarFocusRequester.requestFocus()
                            keyboardController?.show()
                        }
                        BrowserShortcut.OPEN_FIND_IN_PAGE -> {
                            urlBarFocusRequester.requestFocus()
                            keyboardController?.show()
                        }
                        BrowserShortcut.OPEN_URL_IN_NEW_TAB -> submitUrlBar(true)
                        else -> {}
                    }
                }
            }
        }

        val suggestionsSection: @Composable () -> Unit = {
            AnimatedVisibility(visible = viewModel.suggestions.isNotEmpty() && textFieldState.text.isNotEmpty() && uiState.value.isFocusOnUrlTextField && (!uiState.value.isPinningApp && !uiState.value.isRenamingProfile && !uiState.value.isCreatingProfile && !uiState.value.isFindInPageVisible)) {
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = settings.value.padding.dp)
                        .padding(top = settings.value.padding.dp)
                        .clip(
                            RoundedCornerShape(
                                settings.value.cornerRadiusForLayer(2).dp
                            )
                        )
                        .heightIn(
                            max = settings.value.maxContainerSizeForLayer(
                                3,
                                uiState.value.windowMode,
                                isLandscapeLayout
                            ).dp
                        ),
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = "Search suggestion",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(settings.value.padding.dp))
                            Text(
                                text = suggestion.text,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (suggestion.source == SuggestionSource.HISTORY) {
                                IconButton(
                                    onClick = { onRemoveSuggestion(suggestion) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = "Remove from history",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        val urlBarSection: @Composable () -> Unit = {
            AnimatedVisibility(
                modifier = Modifier,
                visible = uiState.value.isUrlBarVisible && !viewModel.isStandaloneMode.value,
                enter =
                    if (uiState.value.isIndependentPanelVisible())
                        expandVertically(tween(settings.value.animationSpeedForLayer(1))) +
                            fadeIn(tween(settings.value.animationSpeedForLayer(1)))
                    else
                        fadeIn(tween(settings.value.animationSpeedForLayer(1))),
                exit = if (uiState.value.isIndependentPanelVisible())
                    shrinkVertically(tween(settings.value.animationSpeedForLayer(1))) +
                        fadeOut(tween(settings.value.animationSpeedForLayer(1)))
                else fadeOut(tween(settings.value.animationSpeedForLayer(1)))
            ) {
                val pendingUrlFocusAction by viewModel.keyboardShortcutManager.pendingUrlFocusAction.collectAsState()
                LaunchedEffect(pendingUrlFocusAction, isLandscapeLayout) {
                    if (pendingUrlFocusAction != null) {
                        if (isLandscapeLayout) {
                            val action = viewModel.keyboardShortcutManager.consumePendingUrlFocusAction()
                            openLandscapeUrlInput(action)
                        } else {
                            withFrameNanos {}
                            geckoViewRef.value?.clearFocus()
                            urlBarFocusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    }
                }

                Box(modifier = Modifier) {
                    Column(
                        modifier = Modifier.onSizeChanged { size ->
                            setTextFieldHeightPx(size.height)
                        }
                    ) {
                        AnimatedVisibility(
                            visible = uiState.value.isPinningApp,
                            enter = expandVertically(tween(settings.value.animationSpeedForLayer(1))) +
                                fadeIn(tween(settings.value.animationSpeedForLayer(1))),
                            exit = shrinkVertically(tween(settings.value.animationSpeedForLayer(1))) +
                                fadeOut(tween(settings.value.animationSpeedForLayer(1)))
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
                                Box(modifier = Modifier.weight(1f)) {
                                TextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusProperties { canFocus = !isLandscapeLayout }
                                        .heightIn(min = settings.value.heightForLayer(2).dp)
                                        .onFocusChanged { focusState ->
                                            viewModel.updateUI {
                                                it.copy(isFocusOnIconUrlTextField = focusState.isFocused)
                                            }
                                            if (!focusState.isFocused) {
                                                val input = (customIconUrlState.text).toString().trim()
                                                if (input.isNotEmpty()) {
                                                    val isValid =
                                                        Patterns.WEB_URL.matcher(input).matches() ||
                                                            input.startsWith("data:image", ignoreCase = true) ||
                                                            input.startsWith("content://", ignoreCase = true) ||
                                                            input.startsWith("<svg", ignoreCase = true)

                                                    if (!isValid) {
                                                        customIconUrlState.setTextAndPlaceCursorAtEnd("")
                                                    }
                                                }
                                                val resetUrl = viewModel.activeTab!!.currentURL
                                                coroutineScope.launch {
                                                    delay(150.milliseconds)
                                                    val currentState = viewModel.uiState.value
                                                    if (!currentState.isFocusOnIconUrlTextField && !currentState.isFocusOnUrlTextField && !isPickingImage.value) {
                                                        if (currentState.isPinningApp) viewModel.updateUI { it.copy(isPinningApp = false) }
                                                        if (currentState.isCreatingProfile) viewModel.updateUI { it.copy(isCreatingProfile = false) }
                                                        if (currentState.isRenamingProfile) viewModel.updateUI { it.copy(isRenamingProfile = false) }

                                                        currentState.savedPanelState?.let { savedState ->
                                                            viewModel.updateUI {
                                                                it.copy(
                                                                    isOptionsPanelVisible = savedState.options,
                                                                    isAppsPanelVisible = savedState.apps,
                                                                    isDownloadPanelVisible = savedState.downloads,
                                                                    isTabDataPanelVisible = false,
                                                                    isNavPanelVisible = savedState.nav,
                                                                    savedPanelState = null,
                                                                )
                                                            }
                                                            if (settings.value.isTabsPanelLock) viewModel.updateUI {
                                                                it.copy(isTabsPanelVisible = savedState.tabs)
                                                            }
                                                        }

                                                        textFieldState.setTextAndPlaceCursorAtEnd(resetUrl.toDomain())
                                                        viewModel.updateUI { it.copy(isUrlOverlayBoxVisible = true) }
                                                    }
                                                }
                                            }
                                        },
                                    state = customIconUrlState,
                                    contentPadding = PaddingValues(
                                        horizontal = settings.value.cornerRadiusForLayer(2).dp + settings.value.padding.dp
                                    ),
                                    placeholder = {
                                        Text(
                                            stringResource(R.string.placeholder_icon_link),
                                            color = Color.Gray
                                        )
                                    },
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
                                    lineLimits = TextFieldLineLimits.SingleLine,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    shape = RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.onSurface,
                                        cursorColor = MaterialTheme.colorScheme.surfaceContainer,
                                        focusedTextColor = MaterialTheme.colorScheme.surfaceContainer,
                                        unfocusedTextColor = MaterialTheme.colorScheme.surfaceContainer,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        errorIndicatorColor = Color.Transparent,
                                        selectionColors = TextSelectionColors(
                                            handleColor = MaterialTheme.colorScheme.surfaceContainer,
                                            backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.25f)
                                        ),
                                    )
                                )
                                if (isLandscapeLayout) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                openLandscapeIconInput()
                                            }
                                    )
                                }
                                }

                                CustomIconButton(
                                    isLandscape = true,
                                    layer = 2,
                                    modifier = Modifier,
                                    onTap = {
                                        isPickingImage.value = true
                                        imagePickerLauncher.launch("image/*")
                                    },
                                    buttonDescription = "pick local image",
                                    painterId = R.drawable.ic_image,
                                    isWhite = true
                                )
                            }
                        }
                        TextField(
                            modifier = Modifier
                                .heightIn(min = settings.value.heightForLayer(1).dp)
                                .onSizeChanged { size ->
                                    setTextFieldHeightPx(size.height)
                                }
                                .fillMaxWidth()
                                .focusProperties { canFocus = !isLandscapeLayout }
                                .focusRequester(urlBarFocusRequester)
                                .onFocusChanged { focusState ->
                                    viewModel.updateUI { it.copy(isFocusOnUrlTextField = focusState.isFocused) }

                                    if (focusState.isFocused) {
                                        if (uiState.value.isFindInPageVisible) {
                                            viewModel.keyboardShortcutManager.consumePendingUrlFocusAction()
                                            val keepOptionsInLandscape = isLandscapeLayout
                                            viewModel.updateUI {
                                                it.copy(
                                                    isOptionsPanelVisible = if (keepOptionsInLandscape) it.isOptionsPanelVisible else false,
                                                    isTabsPanelVisible = false,
                                                    isDownloadPanelVisible = false,
                                                    isTabDataPanelVisible = false,
                                                    isNavPanelVisible = false,
                                                    isSettingsPanelVisible = false,
                                                    isUrlOverlayBoxVisible = false,
                                                    isAppsPanelVisible = false
                                                )
                                            }
                                        } else if (!(uiState.value.isPinningApp)) {
                                            viewModel.updateUI { state ->
                                                val isAnyExclusivePanelOpen =
                                                    state.isFindInPageVisible || state.isDownloadPanelVisible || state.isSettingsPanelVisible || state.isSyncPanelVisible || state.isTabDataPanelVisible

                                                val realOptionsVisible =
                                                    state.isOptionsPanelVisible && !isAnyExclusivePanelOpen
                                                val realAppsVisible =
                                                    state.isAppsPanelVisible && !isAnyExclusivePanelOpen

                                                state.copy(
                                                    savedPanelState = PanelVisibilityState(
                                                        options = realOptionsVisible,
                                                        apps = realAppsVisible,
                                                        tabs = state.isTabsPanelVisible,
                                                        downloads = state.isDownloadPanelVisible,
                                                        tabData = state.isTabDataPanelVisible,
                                                        nav = state.isNavPanelVisible
                                                    ),
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
                                            when (viewModel.keyboardShortcutManager.consumePendingUrlFocusAction()) {
                                                KeyboardShortcutManager.UrlFocusAction.SELECT_ALL -> {
                                                    val activeUrl = viewModel.activeTab?.currentURL ?: ""
                                                    val cleanUrl = LocalAppServer.formatForDisplay(activeUrl)
                                                    val fullUrl = if (cleanUrl.startsWith(LocalAppServer.VIRTUAL_SCHEME)) {
                                                        cleanUrl
                                                    } else {
                                                        viewModel.activeTab?.errorState?.failingUrl ?: activeUrl
                                                    }
                                                    textFieldState.edit {
                                                        replace(0, length, fullUrl)
                                                        selection = TextRange(0, length)
                                                    }
                                                }
                                                KeyboardShortcutManager.UrlFocusAction.EMPTY,
                                                null -> {
                                                    textFieldState.setTextAndPlaceCursorAtEnd("")
                                                }
                                            }
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            delay(150.milliseconds)
                                            val currentState = viewModel.uiState.value
                                            if (!currentState.isFocusOnIconUrlTextField && !currentState.isFocusOnUrlTextField && !isPickingImage.value) {
                                                viewModel.keyboardShortcutManager.clearPendingNewTab()

                                                if (currentState.isPinningApp) viewModel.updateUI { it.copy(isPinningApp = false) }
                                                if (currentState.isCreatingProfile) viewModel.updateUI { it.copy(isCreatingProfile = false) }
                                                if (currentState.isRenamingProfile) viewModel.updateUI { it.copy(isRenamingProfile = false) }

                                                currentState.savedPanelState?.let { savedState ->
                                                    viewModel.updateUI {
                                                        it.copy(
                                                            isOptionsPanelVisible = savedState.options,
                                                            isAppsPanelVisible = savedState.apps,
                                                            isDownloadPanelVisible = savedState.downloads,
                                                            isTabDataPanelVisible = false,
                                                            isNavPanelVisible = savedState.nav,
                                                            savedPanelState = null,
                                                        )
                                                    }
                                                    if (settings.value.isTabsPanelLock) viewModel.updateUI {
                                                        it.copy(isTabsPanelVisible = savedState.tabs)
                                                    }
                                                }
                                                if (!currentState.isFindInPageVisible) {
                                                    val liveUrl = viewModel.activeTab?.currentURL ?: ""
                                                    textFieldState.setTextAndPlaceCursorAtEnd(liveUrl.toDomain())
                                                }
                                                viewModel.updateUI { it.copy(isUrlOverlayBoxVisible = true) }
                                            }
                                        }
                                    }
                                }
                                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp)),
                            contentPadding = PaddingValues(
                                horizontal = settings.value.cornerRadiusForLayer(1).dp + settings.value.padding.dp
                            ),
                            placeholder = {
                                when {
                                    uiState.value.isFindInPageVisible -> Text(
                                        stringResource(R.string.ui_find_in_page),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    uiState.value.isCreatingProfile -> Text(
                                        stringResource(R.string.placeholder_profile_label),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    uiState.value.isRenamingProfile -> Text(
                                        stringResource(R.string.placeholder_profile_label),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    uiState.value.isPinningApp -> Text(
                                        stringResource(R.string.placeholder_pin_label),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    else -> Text(
                                        stringResource(R.string.placeholder_url),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            state = textFieldState,
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = if (uiState.value.isFocusOnUrlTextField || uiState.value.isFocusOnIconUrlTextField || uiState.value.isFindInPageVisible) TextAlign.Start else TextAlign.Center
                            ),
                            lineLimits = TextFieldLineLimits.SingleLine,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = when {
                                    uiState.value.isRenamingProfile
                                            || uiState.value.isCreatingProfile
                                            || uiState.value.isPinningApp
                                            || uiState.value.isFindInPageVisible
                                        -> androidx.compose.ui.text.input.KeyboardType.Text

                                    else -> androidx.compose.ui.text.input.KeyboardType.Uri
                                },
                                imeAction = when {
                                    uiState.value.isRenamingProfile
                                            || uiState.value.isCreatingProfile
                                            || uiState.value.isPinningApp
                                        -> ImeAction.Done

                                    uiState.value.isFindInPageVisible
                                        -> ImeAction.Search

                                    else -> ImeAction.Go
                                }
                            ),
                            onKeyboardAction = {
                                submitUrlBar(false)
                            },
                            shape = RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                cursorColor = MaterialTheme.colorScheme.onSurface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent,
                                selectionColors = TextSelectionColors(
                                    handleColor = MaterialTheme.colorScheme.onSurface,
                                    backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                ),
                            ),
                        )
                    }

                    val currentNavigateWebView by rememberUpdatedState(navigateWebView)
                    val currentCanGoBack by rememberUpdatedState(viewModel.activeTab!!.canGoBack)
                    val currentCanGoForward by rememberUpdatedState(viewModel.activeTab!!.canGoForward)

                    if (uiState.value.isUrlOverlayBoxVisible && !uiState.value.isFocusOnUrlTextField && !uiState.value.isFindInPageVisible) Box(
                        modifier = Modifier
                            .background(
                                Color.Transparent,
                                shape = RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp)
                            )
                            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                            .matchParentSize()
                            .pointerInput(Unit) {
                                val gestureScope = CoroutineScope(currentCoroutineContext())
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)

                                    var isLongPressActive = false
                                    val longPressJob = gestureScope.launch {
                                        delay(viewConfiguration.longPressTimeoutMillis.milliseconds)
                                        isLongPressActive = true
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        focusManager.clearFocus(true)
                                        viewModel.updateUI {
                                            it.copy(isNavPanelVisible = true)
                                        }
                                        setActiveNavAction(GestureNavAction.NONE)
                                    }

                                    var horizontalDragAccumulator = 0f
                                    var verticalDragAccumulator = 0f
                                    var previousAction = GestureNavAction.NONE
                                    val horizontalDragThreshold = 40.dp.toPx()
                                    val verticalCancelThreshold = -28.dp.toPx()
                                    var movedBeforeLongPress = false

                                    while (true) {
                                        val pass = if (isLongPressActive) PointerEventPass.Initial else PointerEventPass.Main
                                        val event = awaitPointerEvent(pass)
                                        val change = event.changes.firstOrNull { it.id == down.id }
                                            ?: event.changes.firstOrNull()
                                        if (change == null || !change.pressed) {
                                            break
                                        }

                                        if (isLongPressActive) {
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

                                                horizontalDragAccumulator < -horizontalDragThreshold ->
                                                    if (currentCanGoBack) GestureNavAction.BACK else GestureNavAction.NONE

                                                horizontalDragAccumulator > horizontalDragThreshold ->
                                                    if (currentCanGoForward) GestureNavAction.FORWARD else GestureNavAction.NONE

                                                else -> GestureNavAction.NONE
                                            }

                                            if (newAction != previousAction) {
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                previousAction = newAction
                                            }
                                            setActiveNavAction(newAction)
                                        } else {
                                            val totalDist = kotlin.math.hypot(
                                                (change.position.x - down.position.x).toDouble(),
                                                (change.position.y - down.position.y).toDouble()
                                            )
                                            if (totalDist > viewConfiguration.touchSlop) {
                                                movedBeforeLongPress = true
                                                longPressJob.cancel()
                                                break
                                            }
                                        }
                                    }

                                    if (isLongPressActive) {
                                        currentNavigateWebView()
                                    } else {
                                        if (longPressJob.isActive) {
                                            longPressJob.cancel()
                                        }
                                        if (!movedBeforeLongPress) {
                                            if (isLandscapeLayout) {
                                                openLandscapeUrlInput(null)
                                            } else {
                                                if (urlBarFocusRequester.requestFocus()) {
                                                    keyboardController?.show()
                                                } else {
                                                    urlBarFocusRequester.requestFocus()
                                                }
                                                viewModel.updateUI {
                                                    it.copy(isUrlOverlayBoxVisible = false)
                                                }
                                            }
                                        }
                                    }

                                    viewModel.updateUI { it.copy(isNavPanelVisible = false) }
                                    setActiveNavAction(GestureNavAction.NONE)
                                }
                            }
                    ) {}

                    if (isLandscapeLayout && (uiState.value.isFindInPageVisible || uiState.value.isPinningApp || uiState.value.isCreatingProfile || uiState.value.isRenamingProfile)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    openLandscapeUrlInput(null)
                                }
                        )
                    }
                }
            }
        }

        val onSyncSignInClick: () -> Unit = {
            coroutineScope.launch {
                val email =
                    viewModel.driveSyncManager.signInWithGoogle(context as android.app.Activity)
                if (email != null) {
                    viewModel.onSignInSuccess(email)
                } else {
                    viewModel.showCustomNotification(loginFailedText)
                }
            }
        }

        val isTextEditVisible = !isStudioKeyboard && !uiState.value.isFindInPageVisible && (
            uiState.value.isPinningApp ||
            (uiState.value.isFocusOnUrlTextField && textFieldState.text.isBlank())
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLandscapeLayout) {
                val imeBottomDp = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
                val leftStemHeight = (viewModel.screenSizeDp.value.height.dp - imeBottomDp - (settings.value.padding * 2).dp)
                    .coerceAtLeast(120.dp)

                Layout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(settings.value.padding.dp)
                        .windowInsetsPadding(WindowInsets.ime)
                        .padding(bottom = floatingPanelBottomPadding),
                    content = {
                        // full-height left vertical stem that translates the bottom bar right
                        Row(
                            modifier = Modifier
                                .wrapContentSize()
                                .anchoredDraggable(
                                    state = landscapeOptionsDraggableState,
                                    orientation = Orientation.Horizontal,
                                    flingBehavior = landscapeOptionsFling,
                                    enabled = isDragEnabled
                                ),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            LandscapeOptionsWrapper(
                                dragOffset = landscapeOptionsDraggableState.offset,
                                maxWidthPx = landscapeOptionsWidthPx
                            ) {
                                Box(modifier = Modifier.padding(end = settings.value.padding.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainer)
                                    ) {
                                        OptionsPanel(
                                            onCloseAllTabs = onCloseAllTabs,
                                            confirmationPopup = confirmationPopup,
                                            onLoginClick = onSyncSignInClick,
                                            isLandscapeLayout = true,
                                            leftStemHeight = leftStemHeight
                                        )
                                    }
                                }
                            }

                            FindInPagePanel(
                                isVisible = uiState.value.isFindInPageVisible,
                                onFindNext = {
                                    activeSession.finder.find(
                                        viewModel.findInPageTextFieldState.text.toString(),
                                        GeckoSession.FINDER_FIND_FORWARD
                                    ).then { result ->
                                        if (result != null) {
                                            val currentMatch = if (result.total > 0) result.current else 0
                                            viewModel.findInPageResult.value = currentMatch to result.total
                                        }
                                        GeckoResult.fromValue(result)
                                    }
                                },
                                onFindPrevious = {
                                    activeSession.finder.find(
                                        viewModel.findInPageTextFieldState.text.toString(),
                                        GeckoSession.FINDER_FIND_BACKWARDS
                                    ).then { result ->
                                        if (result != null) {
                                            val currentMatch = if (result.total > 0) result.current else 0
                                            viewModel.findInPageResult.value = currentMatch to result.total
                                        }
                                        GeckoResult.fromValue(result)
                                    }
                                },
                                onClose = {
                                    viewModel.updateUI { it.copy(isFindInPageVisible = false) }
                                    viewModel.findInPageTextFieldState.setTextAndPlaceCursorAtEnd("")
                                    activeSession.finder.clear()
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                },
                                isLandscapeLayout = true,
                                leftStemHeight = leftStemHeight
                            )

                            SettingsPanel(
                                onCloseAllTabs = onCloseAllTabs,
                                confirmationPopup = confirmationPopup,
                                onLoginClick = onSyncSignInClick,
                                isLandscapeLayout = true,
                                leftStemHeight = leftStemHeight,
                                currentViewOverride = currentSettingView,
                                onCurrentViewChange = { currentSettingView = it },
                                renderMainOnly = true
                            )

                            TabDataPanel(
                                isTabDataPanelVisible = isTabDataPanelVisible,
                                onDismiss = { viewModel.updateUI { it.copy(isTabDataPanelVisible = false) } },
                                onPermissionToggle = handlePermissionToggle,
                                onClearSiteData = handleClearInspectedTabData,
                                onCloseTab = handleCloseInspectedTab,
                                isLandscapeLayout = true,
                                leftStemHeight = leftStemHeight
                            )
                        }

                        // bottom horizontal bar keeps exact full width and translates right when pushed
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                        // bottom-left half: sub-panels above url bar, url bar, and apps panel below url bar
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .anchoredDraggable(
                                    state = landscapeAppsDraggableState,
                                    orientation = Orientation.Vertical,
                                    flingBehavior = landscapeAppsFling,
                                    enabled = isDragEnabled
                                )
                                .anchoredDraggable(
                                    state = landscapeOptionsDraggableState,
                                    orientation = Orientation.Horizontal,
                                    flingBehavior = landscapeOptionsFling,
                                    enabled = isDragEnabled
                                )
                                .pointerInput(isDragEnabled) {
                                    if (!isDragEnabled) return@pointerInput
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Final)
                                            if (event.type == PointerEventType.Scroll) {
                                                val change = event.changes.firstOrNull() ?: continue
                                                if (change.isConsumed) continue
                                                val dx = change.scrollDelta.x
                                                val dy = change.scrollDelta.y
                                                val now = System.currentTimeMillis()
                                                if (kotlin.math.abs(dy) >= kotlin.math.abs(dx) && dy != 0f) {
                                                    val dir = if (dy > 0f) 1 else -1
                                                    val isNewRoll = (dir != viewModel.lastMouseWheelDirection) ||
                                                        (now - viewModel.lastMouseWheelScrollMs >= 150L)
                                                    viewModel.lastMouseWheelScrollMs = now
                                                    viewModel.lastMouseWheelDirection = dir
                                                    if (isNewRoll) {
                                                        viewModel.updateUI {
                                                            it.copy(isAppsPanelVisible = dy > 0f)
                                                        }
                                                    }
                                                } else if (dx != 0f) {
                                                    val dir = if (dx > 0f) 1 else -1
                                                    val isNewRoll = (dir != viewModel.lastMouseWheelDirection) ||
                                                        (now - viewModel.lastMouseWheelScrollMs >= 150L)
                                                    viewModel.lastMouseWheelScrollMs = now
                                                    viewModel.lastMouseWheelDirection = dir
                                                    if (isNewRoll) {
                                                        viewModel.updateUI {
                                                            it.copy(isOptionsPanelVisible = dx > 0f)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                        ) {
                            LoadingIndicator()
                            DescriptionPanel()
                            NavigationPanel(activeSession = activeSession)
                            DownloadPanel(
                                confirmationPopup = confirmationPopup,
                                isDownloadPanelVisible = uiState.value.isDownloadPanelVisible,
                                onDownloadRowClicked = onDownloadRowClicked,
                                onOpenFolderClicked = onOpenFolderClicked,
                            )
                            SettingsPanel(
                                onCloseAllTabs = onCloseAllTabs,
                                confirmationPopup = confirmationPopup,
                                onLoginClick = onSyncSignInClick,
                                isLandscapeLayout = true,
                                currentViewOverride = currentSettingView,
                                onCurrentViewChange = { currentSettingView = it },
                                renderSubViewsOnly = true
                            )
                            SyncPanel(
                                isVisible = uiState.value.isSyncPanelVisible,
                                onDismiss = { viewModel.updateUI { it.copy(isSyncPanelVisible = false) } },
                                onPush = {
                                    confirmationPopup(R.string.confirm_push, "", { viewModel.triggerManualPush() }, {})
                                },
                                onPull = {
                                    confirmationPopup(R.string.confirm_pull, "", { viewModel.triggerManualPull() }, {})
                                },
                                onMerge = {
                                    confirmationPopup(R.string.confirm_merge, "", { viewModel.triggerSmartMerge() }, {})
                                },
                                onLogout = {
                                    confirmationPopup(R.string.confirm_logout, viewModel.getLoggedInEmail(), { viewModel.logout() }, {})
                                },
                                onDeleteAccount = {
                                    confirmationPopup(R.string.confirm_wipe_cloud_data, viewModel.getLoggedInEmail(), {
                                        viewModel.triggerDeleteAccount()
                                        viewModel.logout()
                                    }, {})
                                }
                            )
                            suggestionsSection()
                            urlBarSection()

                            LandscapeAppsWrapper(
                                dragOffset = landscapeAppsDraggableState.offset,
                                maxHeightPx = landscapeAppsHeightPx
                            ) {
                                Box(modifier = Modifier.padding(bottom = settings.value.padding.dp)) {
                                    AppsPanel(
                                        onAppClick = { app ->
                                            viewModel.activeTab?.let { tab ->
                                                viewModel.updateTabById(tab.id) {
                                                    it.copy(currentURL = app.url)
                                                }
                                            }
                                            textFieldState.setTextAndPlaceCursorAtEnd(app.url.toDomain())
                                            webViewLoad(activeSession, app.url, context)
                                            viewModel.updateUI { it.copy(isSettingsPanelVisible = false) }
                                            viewModel.updateUI { it.copy(isUrlBarVisible = false) }
                                        },
                                        addAppToPin = {
                                            viewModel.updateUI { it.copy(isPinningApp = true) }
                                            openLandscapeUrlInput(KeyboardShortcutManager.UrlFocusAction.EMPTY)
                                        },
                                        createNewProfile = {
                                            viewModel.updateUI { it.copy(isCreatingProfile = true) }
                                            openLandscapeUrlInput(KeyboardShortcutManager.UrlFocusAction.EMPTY)
                                        },
                                        renameProfile = {
                                            viewModel.updateUI { it.copy(isRenamingProfile = true) }
                                            openLandscapeUrlInput(KeyboardShortcutManager.UrlFocusAction.EMPTY)
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
                                                { viewModel.deleteProfile() },
                                                {}
                                            )
                                        },
                                        draggableState = landscapeAppsDraggableState
                                    )
                                }
                            }
                        }

                        // bottom-right half: tabs, media, permission, context menu, confirmation, text edit
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            ConfirmationPanel()
                            ContextMenuPanel(onDownload = onDownload)
                            PermissionPanel(
                                isUrlBarVisible = uiState.value.isUrlBarVisible,
                                hasContentBelow = uiState.value.isTabsPanelVisible || isTextEditVisible,
                                onAllow = {
                                    viewModel.pendingPermissionRequest.value?.let { request ->
                                        if (!request.isSystemRequest) {
                                            val grantedMap = request.permissionsToRequest.associateWith { true }
                                            if (request.isPersistent) {
                                                val domain = request.origin.toDomain()
                                                viewModel.savePermissionDecision(domain, grantedMap)
                                            }
                                            request.onResult(grantedMap, viewModel.pendingPermissionRequest)
                                            viewModel.pendingPermissionRequest.value = null
                                        } else if (request.permissionsToRequest.contains(Manifest.permission.CAMERA) ||
                                            request.permissionsToRequest.contains(Manifest.permission.RECORD_AUDIO)
                                        ) {
                                            viewModel.allowMediaPermissionRequest(request.permissionsToRequest.associateWith { true })
                                        } else {
                                            permissionLauncher.launch(request.permissionsToRequest.toTypedArray())
                                        }
                                    }
                                },
                            )
                            MediaPanel()
                            TabsPanel(
                                updateInspectingTab = updateInspectingTab,
                                isLandscapeLayout = true,
                                hasContentBelow = isTextEditVisible,
                            )
                            TextEditPanel(
                                isVisible = isTextEditVisible,
                                onCopyClick = copyCurrentUrl,
                                onEditClick = editCurrentUrl,
                                onDismiss = dismissUrlBar,
                                onAddToHomeScreen = {},
                                activeWebViewTitle = viewModel.activeTab!!.currentTitle,
                                isLandscapeLayout = true
                            )
                        }
                    }
                    }
                ) { measurables, constraints ->
                    val leftPlaceable = measurables[0].measure(
                        constraints.copy(minWidth = 0)
                    )
                    // keep bottom horizontal bar fixed at full container width while sliding right
                    val bottomPlaceable = measurables[1].measure(
                        constraints.copy(minWidth = constraints.maxWidth, maxWidth = constraints.maxWidth)
                    )
                    val layoutHeight = maxOf(leftPlaceable.height, bottomPlaceable.height)
                    layout(constraints.maxWidth, layoutHeight) {
                        leftPlaceable.placeRelative(0, layoutHeight - leftPlaceable.height)
                        bottomPlaceable.placeRelative(leftPlaceable.width, layoutHeight - bottomPlaceable.height)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(settings.value.padding.dp)
                        .windowInsetsPadding(WindowInsets.ime)
                        .padding(bottom = floatingPanelBottomPadding)
                        .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .anchoredDraggable(
                            state = draggableState,
                            orientation = Orientation.Vertical,
                            flingBehavior = flingBehavior,
                            enabled = isDragEnabled
                        )
                        .pointerInput(isDragEnabled) {
                            if (!isDragEnabled) return@pointerInput
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Final)
                                    if (event.type == PointerEventType.Scroll) {
                                        val change = event.changes.firstOrNull() ?: continue
                                        if (change.isConsumed) continue
                                        val dy = change.scrollDelta.y
                                        if (dy != 0f && kotlin.math.abs(dy) >= kotlin.math.abs(change.scrollDelta.x)) {
                                            val now = System.currentTimeMillis()
                                            val dir = if (dy > 0f) 1 else -1
                                            val isNewRoll = (dir != viewModel.lastMouseWheelDirection) ||
                                                (now - viewModel.lastMouseWheelScrollMs >= 150L)
                                            viewModel.lastMouseWheelScrollMs = now
                                            viewModel.lastMouseWheelDirection = dir
                                            if (isNewRoll) {
                                                val current = viewModel.uiState.value
                                                if (dy > 0f) {
                                                    // natural scroll down reveals options then apps panel
                                                    when {
                                                        !current.isOptionsPanelVisible -> {
                                                            viewModel.updateUI {
                                                                it.copy(
                                                                    isOptionsPanelVisible = true,
                                                                    isAppsPanelVisible = false
                                                                )
                                                            }
                                                        }
                                                        !current.isAppsPanelVisible -> {
                                                            viewModel.updateUI {
                                                                it.copy(
                                                                    isOptionsPanelVisible = true,
                                                                    isAppsPanelVisible = true
                                                                )
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    // natural scroll up collapses apps then options panel
                                                    when {
                                                        current.isAppsPanelVisible -> {
                                                            viewModel.updateUI {
                                                                it.copy(
                                                                    isOptionsPanelVisible = true,
                                                                    isAppsPanelVisible = false
                                                                )
                                                            }
                                                        }
                                                        current.isOptionsPanelVisible -> {
                                                            viewModel.updateUI {
                                                                it.copy(
                                                                    isOptionsPanelVisible = false,
                                                                    isAppsPanelVisible = false
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
                        }
                ) {
                    LoadingIndicator()
                    DescriptionPanel()
                    NavigationPanel(activeSession = activeSession)
                    MediaPanel()
                    DownloadPanel(
                        confirmationPopup = confirmationPopup,
                        isDownloadPanelVisible = uiState.value.isDownloadPanelVisible,
                        onDownloadRowClicked = onDownloadRowClicked,
                        onOpenFolderClicked = onOpenFolderClicked,
                    )
                    ContextMenuPanel(onDownload = onDownload)
                    FindInPagePanel(
                        isVisible = uiState.value.isFindInPageVisible,
                        onFindNext = {
                            activeSession.finder.find(
                                viewModel.findInPageTextFieldState.text.toString(),
                                GeckoSession.FINDER_FIND_FORWARD
                            ).then { result ->
                                if (result != null) {
                                    val currentMatch = if (result.total > 0) result.current else 0
                                    viewModel.findInPageResult.value = currentMatch to result.total
                                }
                                GeckoResult.fromValue(result)
                            }
                        },
                        onFindPrevious = {
                            activeSession.finder.find(
                                viewModel.findInPageTextFieldState.text.toString(),
                                GeckoSession.FINDER_FIND_BACKWARDS
                            ).then { result ->
                                if (result != null) {
                                    val currentMatch = if (result.total > 0) result.current else 0
                                    viewModel.findInPageResult.value = currentMatch to result.total
                                }
                                GeckoResult.fromValue(result)
                            }
                        },
                        onClose = {
                            viewModel.updateUI { it.copy(isFindInPageVisible = false) }
                            viewModel.findInPageTextFieldState.setTextAndPlaceCursorAtEnd("")
                            activeSession.finder.clear()
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        },
                    )
                    PromptPanel(
                        geckoViewRef = geckoViewRef,
                        isUrlBarVisible = uiState.value.isUrlBarVisible,
                        onDismiss = onDismiss,
                        state = state,
                    )
                    SyncPanel(
                        isVisible = uiState.value.isSyncPanelVisible,
                        onDismiss = { viewModel.updateUI { it.copy(isSyncPanelVisible = false) } },
                        onPush = {
                            confirmationPopup(R.string.confirm_push, "", { viewModel.triggerManualPush() }, {})
                        },
                        onPull = {
                            confirmationPopup(R.string.confirm_pull, "", { viewModel.triggerManualPull() }, {})
                        },
                        onMerge = {
                            confirmationPopup(R.string.confirm_merge, "", { viewModel.triggerSmartMerge() }, {})
                        },
                        onLogout = {
                            confirmationPopup(R.string.confirm_logout, viewModel.getLoggedInEmail(), { viewModel.logout() }, {})
                        },
                        onDeleteAccount = {
                            confirmationPopup(R.string.confirm_wipe_cloud_data, viewModel.getLoggedInEmail(), {
                                viewModel.triggerDeleteAccount()
                                viewModel.logout()
                            }, {})
                        }
                    )
                    SettingsPanel(
                        onCloseAllTabs = onCloseAllTabs,
                        confirmationPopup = confirmationPopup,
                        onLoginClick = onSyncSignInClick
                    )
                    TabDataPanel(
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
                            viewModel.pendingPermissionRequest.value?.let { request ->
                                if (!request.isSystemRequest) {
                                    val grantedMap = request.permissionsToRequest.associateWith { true }
                                    if (request.isPersistent) {
                                        val domain = request.origin.toDomain()
                                        viewModel.savePermissionDecision(domain, grantedMap)
                                    }
                                    request.onResult(grantedMap, viewModel.pendingPermissionRequest)
                                    viewModel.pendingPermissionRequest.value = null
                                } else if (request.permissionsToRequest.contains(Manifest.permission.CAMERA) ||
                                    request.permissionsToRequest.contains(Manifest.permission.RECORD_AUDIO)
                                ) {
                                    viewModel.allowMediaPermissionRequest(request.permissionsToRequest.associateWith { true })
                                } else {
                                    permissionLauncher.launch(request.permissionsToRequest.toTypedArray())
                                }
                            }
                        },
                    )

                    suggestionsSection()
                    ConfirmationPanel()
                    urlBarSection()

                    // setting options
                    OptionsPanelWrapper(
                        dragOffset = draggableState.offset
                    ) {
                        Column {
                            OptionsPanel(
                                onCloseAllTabs = onCloseAllTabs,
                                confirmationPopup = confirmationPopup,
                                onLoginClick = onSyncSignInClick
                            )

                            AppsPanel(
                                onAppClick = { app ->
                                    viewModel.activeTab?.let { tab ->
                                        viewModel.updateTabById(tab.id) {
                                            it.copy(currentURL = app.url)
                                        }
                                    }
                                    textFieldState.setTextAndPlaceCursorAtEnd(app.url.toDomain())
                                    webViewLoad(activeSession, app.url, context)
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
                                        { viewModel.deleteProfile() },
                                        {}
                                    )
                                },
                                draggableState = draggableState
                            )
                        }
                    }

                    TextEditPanel(
                        isVisible = isTextEditVisible,
                        onCopyClick = copyCurrentUrl,
                        onEditClick = editCurrentUrl,
                        onDismiss = dismissUrlBar,
                        onAddToHomeScreen = {},
                        activeWebViewTitle = viewModel.activeTab!!.currentTitle,
                    )
                }
            }
        }
    }
}

@Composable
private fun LandscapeOptionsWrapper(
    dragOffset: Float,
    maxWidthPx: Float,
    content: @Composable () -> Unit
) {
    val safeOffset = if (dragOffset.isNaN()) 0f else dragOffset
    val animatedWidth = safeOffset.coerceIn(0f, maxWidthPx)

    Layout(
        content = content,
        modifier = Modifier.clipToBounds()
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(
            constraints.copy(
                minWidth = 0,
                maxWidth = androidx.compose.ui.unit.Constraints.Infinity
            )
        )
        val currentWidth = animatedWidth.roundToInt()

        layout(currentWidth, placeable.height) {
            placeable.place(currentWidth - placeable.width, 0)
        }
    }
}

@Composable
private fun LandscapeAppsWrapper(
    dragOffset: Float,
    maxHeightPx: Float,
    content: @Composable () -> Unit
) {
    val safeOffset = if (dragOffset.isNaN()) 0f else dragOffset
    val animatedHeight = (-safeOffset).coerceIn(0f, maxHeightPx)

    Layout(
        content = content,
        modifier = Modifier.clipToBounds()
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(
            constraints.copy(
                minHeight = 0,
                maxHeight = androidx.compose.ui.unit.Constraints.Infinity
            )
        )
        val currentHeight = animatedHeight.roundToInt()

        layout(placeable.width, currentHeight) {
            placeable.place(0, 0)
        }
    }
}