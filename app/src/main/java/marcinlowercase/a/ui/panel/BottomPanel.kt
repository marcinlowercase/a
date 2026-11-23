package marcinlowercase.a.ui.panel

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.util.Patterns
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.DownloadItem
import marcinlowercase.a.core.data_class.JsDialogState
import marcinlowercase.a.core.data_class.PanelVisibilityState
import marcinlowercase.a.core.data_class.Suggestion
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.AppState
import marcinlowercase.a.core.enum_class.BrowserOption
import marcinlowercase.a.core.enum_class.GestureNavAction
import marcinlowercase.a.core.enum_class.RevealState
import marcinlowercase.a.core.enum_class.SearchEngine
import marcinlowercase.a.core.enum_class.SuggestionSource
import marcinlowercase.a.core.enum_class.WindowMode
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
    floatingPanelStartPadding: Dp = 0.dp,
    floatingPanelEndPadding: Dp = 0.dp,
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
                if (!viewModel.uiState.value.isLandscape || viewModel.uiState.value.isOnFullscreenVideo) {
                    delay(50.milliseconds)
                    urlBarFocusRequester.requestFocus()
                    keyboardController?.show()
                }
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
        val outerStartPaddingPx = with(density) { settings.value.padding.dp.toPx() }
        val floatingStartPadPx = with(density) { floatingPanelStartPadding.toPx() }
        val floatingEndPadPx = with(density) { floatingPanelEndPadding.toPx() }
        val hasLandscapeOptions = remember(
            settings.value.optionsOrder,
            settings.value.hiddenOptions,
            settings.value.currentCornerRadius,
            uiState.value.windowMode,
            viewModel.isSortingButtons.value
        ) {
            settings.value.optionsOrder.split(",").mapNotNull {
                try {
                    BrowserOption.valueOf(it)
                } catch (_: Exception) {
                    null
                }
            }.any { opt ->
                if (uiState.value.windowMode != WindowMode.FULLSCREEN && opt == BrowserOption.FULLSCREEN) return@any false
                if (settings.value.cornerRadiusForLayer(0) == 0f && opt == BrowserOption.SHARP_MODE) return@any false
                viewModel.isSortingButtons.value || !viewModel.isOptionHidden(opt, settings.value)
            }
        }
        val col1InnerWidthPx = if (hasLandscapeOptions) {
            with(density) { (settings.value.heightForLayer(2).dp + settings.value.padding.dp * 2).toPx() }
        } else 0f

        val landscapeAppsHeightDp = settings.value.maxContainerSizeForLayer(
            3,
            uiState.value.windowMode,
            isLandscape = true,
            listHeightOverride = settings.value.getAppsListHeight(
                uiState.value.windowMode,
                isLandscape = true
            )
        ).dp + settings.value.padding.dp
        val landscapeAppsHeightPx = with(density) { landscapeAppsHeightDp.toPx() }

        val isFirstModeLoad = settings.value.isFirstLoadForMode(uiState.value.windowMode)
        val isLandscapeSettingsSubView = currentSettingView != SettingPanelView.MAIN || isFirstModeLoad
        SideEffect {
            viewModel.isLandscapeSettingsSubView.value = isLandscapeSettingsSubView
        }
        LaunchedEffect(viewModel.resetSettingsToMainTrigger.intValue) {
            if (viewModel.resetSettingsToMainTrigger.intValue > 0 && !isFirstModeLoad) {
                currentSettingView = SettingPanelView.MAIN
                viewModel.isLandscapeSettingsSubView.value = false
            }
        }
        val isLandscapeSettingsMainOpen =
            uiState.value.isSettingsPanelVisible && !isLandscapeSettingsSubView
        val isLandscapeCol2Open =
            isLandscapeSettingsMainOpen || uiState.value.isFindInPageVisible || isTabDataPanelVisible

        var isUrlBarOnRight by remember { mutableStateOf(settings.value.isLandscapeUrlBarOnRight) }
        LaunchedEffect(settings.value.isLandscapeUrlBarOnRight) {
            if (isUrlBarOnRight != settings.value.isLandscapeUrlBarOnRight) {
                isUrlBarOnRight = settings.value.isLandscapeUrlBarOnRight
            }
        }
        val drawerSign = if (isUrlBarOnRight) -1f else 1f
        val nearEdgePadPx = if (isUrlBarOnRight) floatingEndPadPx else floatingStartPadPx
        var bottomBarWidthPx by remember {
            mutableFloatStateOf(with(density) { viewModel.screenSizeDp.value.width.dp.toPx() })
        }
        val swapDistancePx = (bottomBarWidthPx.toInt() / 2).toFloat().coerceAtLeast(1f)

        var col2MeasuredWidthPx by remember {
            mutableFloatStateOf(
                if (isLandscapeLayout && isLandscapeCol2Open) {
                    with(density) { settings.value.heightForLayer(1).dp.toPx() }
                } else 0f
            )
        }

        val initialLeftStemState = when {
            uiState.value.isOptionsPanelVisible && col2MeasuredWidthPx > 0.5f ->
                if (col1InnerWidthPx > 0f) LandscapeLeftPanelState.Both else LandscapeLeftPanelState.Col2Only
            uiState.value.isOptionsPanelVisible ->
                if (col1InnerWidthPx > 0f) LandscapeLeftPanelState.OptionsOnly else LandscapeLeftPanelState.Hidden
            col2MeasuredWidthPx > 0.5f -> LandscapeLeftPanelState.Col2Only
            else -> LandscapeLeftPanelState.Hidden
        }
        var wasOpeningWithNearPad by remember {
            mutableStateOf(!uiState.value.isOptionsPanelVisible && !isLandscapeCol2Open)
        }
        var isCommitFittingDrawer by remember { mutableStateOf(false) }
        val fitDrawerWidthAnim = remember { Animatable(0f) }
        val fitBottomOffsetAnim = remember { Animatable(0f) }

        var isClosingDrawerShiftActive by remember { mutableStateOf(false) }
        var heldClosingOptions by remember { mutableStateOf(false) }
        val closingExtraClipAnim = remember { Animatable(0f) }

        val openingExtraPadPx =
            if (wasOpeningWithNearPad && !isCommitFittingDrawer) nearEdgePadPx else 0f

        val initialLandscapeOptionsAnchors = remember(
            drawerSign,
            col1InnerWidthPx,
            col2MeasuredWidthPx,
            outerStartPaddingPx,
            swapDistancePx,
            openingExtraPadPx
        ) {
            buildLandscapeOptionsAnchors(
                drawerSign = drawerSign,
                col1InnerWidthPx = col1InnerWidthPx,
                col2MeasuredWidthPx = col2MeasuredWidthPx,
                outerStartPaddingPx = outerStartPaddingPx,
                includeCol2OnlyAnchor = col2MeasuredWidthPx > 0.5f && !uiState.value.isOptionsPanelVisible,
                includeSwappedAnchor = !uiState.value.isOptionsPanelVisible && !isLandscapeCol2Open && col2MeasuredWidthPx <= 0.5f,
                swapDistancePx = swapDistancePx,
                openingExtraPadPx = openingExtraPadPx
            )
        }
        val landscapeOptionsDraggableState = remember {
            AnchoredDraggableState(
                initialValue = initialLeftStemState,
                anchors = initialLandscapeOptionsAnchors
            )
        }
        val landscapeOptionsInteractionSource = remember { MutableInteractionSource() }
        val isDraggingLandscapeOptions by landscapeOptionsInteractionSource.collectIsDraggedAsState()
        var prevLandscapeOptionsSettled by remember { mutableStateOf(initialLeftStemState) }
        var prevObservedOptionsVisible by remember { mutableStateOf(uiState.value.isOptionsPanelVisible) }
        val urlSettleOffsetAnim = remember { Animatable(0f) }
        var isCommitSettling by remember { mutableStateOf(false) }
        var activeFlingTarget by remember { mutableStateOf<LandscapeLeftPanelState?>(null) }

        val hasCol2Anchor = col2MeasuredWidthPx > 0.5f
        // omit intermediate col2 anchor once both panels are shown so swiping past half closes all panels
        val includeCol2OnlyAnchor = hasCol2Anchor && (
            !uiState.value.isOptionsPanelVisible ||
            landscapeOptionsDraggableState.currentValue == LandscapeLeftPanelState.Col2Only ||
            landscapeOptionsDraggableState.settledValue == LandscapeLeftPanelState.Col2Only
        )
        val includeSwappedAnchor = swapDistancePx > 1f && (
            landscapeOptionsDraggableState.settledValue == LandscapeLeftPanelState.Swapped ||
            landscapeOptionsDraggableState.currentValue == LandscapeLeftPanelState.Swapped ||
            landscapeOptionsDraggableState.targetValue == LandscapeLeftPanelState.Swapped ||
            (landscapeOptionsDraggableState.settledValue == LandscapeLeftPanelState.Hidden &&
                !uiState.value.isOptionsPanelVisible &&
                !isLandscapeCol2Open &&
                !hasCol2Anchor)
        )
        val landscapeOptionsAnchors = remember(
            drawerSign,
            col1InnerWidthPx,
            col2MeasuredWidthPx,
            outerStartPaddingPx,
            includeCol2OnlyAnchor,
            includeSwappedAnchor,
            swapDistancePx,
            openingExtraPadPx
        ) {
            buildLandscapeOptionsAnchors(
                drawerSign = drawerSign,
                col1InnerWidthPx = col1InnerWidthPx,
                col2MeasuredWidthPx = col2MeasuredWidthPx,
                outerStartPaddingPx = outerStartPaddingPx,
                includeCol2OnlyAnchor = includeCol2OnlyAnchor,
                includeSwappedAnchor = includeSwappedAnchor,
                swapDistancePx = swapDistancePx,
                openingExtraPadPx = openingExtraPadPx
            )
        }

        LaunchedEffect(
            landscapeOptionsDraggableState.settledValue,
            uiState.value.isOptionsPanelVisible,
            isLandscapeCol2Open,
            isCommitFittingDrawer,
            isDraggingLandscapeOptions,
            activeFlingTarget
        ) {
            if (!isDraggingLandscapeOptions && activeFlingTarget == null && !isCommitFittingDrawer) {
                if (landscapeOptionsDraggableState.settledValue == LandscapeLeftPanelState.Hidden &&
                    !uiState.value.isOptionsPanelVisible &&
                    !isLandscapeCol2Open
                ) {
                    wasOpeningWithNearPad = true
                } else if (isLandscapeCol2Open &&
                    landscapeOptionsDraggableState.settledValue == LandscapeLeftPanelState.Col2Only
                ) {
                    wasOpeningWithNearPad = false
                }
            }
        }

        LaunchedEffect(isDraggingLandscapeOptions, isLandscapeLayout) {
            if (!isLandscapeLayout) {
                isCommitFittingDrawer = false
                isClosingDrawerShiftActive = false
                heldClosingOptions = false
                closingExtraClipAnim.snapTo(0f)
                return@LaunchedEffect
            }
            if (isDraggingLandscapeOptions) {
                isCommitFittingDrawer = false
                val curSettled = landscapeOptionsDraggableState.settledValue
                val isOpenDrawer = curSettled == LandscapeLeftPanelState.OptionsOnly ||
                    curSettled == LandscapeLeftPanelState.Col2Only ||
                    curSettled == LandscapeLeftPanelState.Both
                if (isOpenDrawer && nearEdgePadPx > 0.5f) {
                    isClosingDrawerShiftActive = true
                    heldClosingOptions = viewModel.uiState.value.isOptionsPanelVisible
                    closingExtraClipAnim.snapTo(nearEdgePadPx)
                } else {
                    isClosingDrawerShiftActive = false
                    heldClosingOptions = false
                    closingExtraClipAnim.snapTo(0f)
                }
            } else if (isClosingDrawerShiftActive) {
                val outcome = snapshotFlow {
                    val flingTarget = activeFlingTarget
                    val offset = landscapeOptionsDraggableState.offset
                    val settled = landscapeOptionsDraggableState.settledValue
                    val settledPos = landscapeOptionsDraggableState.anchors.positionOf(settled)
                    val atSettled = !offset.isNaN() && !settledPos.isNaN() &&
                        kotlin.math.abs(offset - settledPos) < 0.5f
                    when {
                        flingTarget == LandscapeLeftPanelState.Hidden ||
                        (flingTarget == null && settled == LandscapeLeftPanelState.Hidden && atSettled) -> 1
                        flingTarget == LandscapeLeftPanelState.OptionsOnly ||
                        flingTarget == LandscapeLeftPanelState.Col2Only ||
                        flingTarget == LandscapeLeftPanelState.Both -> 2
                        flingTarget == null && !landscapeOptionsDraggableState.isAnimationRunning && atSettled -> 2
                        else -> 0
                    }
                }.first { it != 0 }

                if (outcome == 1) {
                    // slide remaining drawer past physical cutout edge alongside panel fling
                    closingExtraClipAnim.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(settings.value.animationSpeedForLayer(1))
                    )
                    snapshotFlow {
                        activeFlingTarget == null && !landscapeOptionsDraggableState.isAnimationRunning
                    }.first { it }
                } else {
                    snapshotFlow {
                        activeFlingTarget == null && !landscapeOptionsDraggableState.isAnimationRunning
                    }.first { it }
                    closingExtraClipAnim.snapTo(0f)
                }
                isClosingDrawerShiftActive = false
                heldClosingOptions = false
            } else if (wasOpeningWithNearPad && nearEdgePadPx > 0.5f) {
                val outcome = snapshotFlow {
                    val flingTarget = activeFlingTarget
                    val offset = landscapeOptionsDraggableState.offset
                    val settled = landscapeOptionsDraggableState.settledValue
                    val atZero = !offset.isNaN() && kotlin.math.abs(offset) <= 0.5f
                    when {
                        flingTarget == LandscapeLeftPanelState.OptionsOnly ||
                        flingTarget == LandscapeLeftPanelState.Both ||
                        settled == LandscapeLeftPanelState.OptionsOnly ||
                        settled == LandscapeLeftPanelState.Both -> 1
                        flingTarget == LandscapeLeftPanelState.Hidden ||
                        flingTarget == LandscapeLeftPanelState.Swapped -> 2
                        flingTarget == null && !landscapeOptionsDraggableState.isAnimationRunning && atZero -> 2
                        else -> 0
                    }
                }.first { it != 0 }

                if (outcome == 1) {
                    val curSigned =
                        (landscapeOptionsDraggableState.offset.takeIf { !it.isNaN() } ?: 0f) * drawerSign
                    val settledDrawerW = outerStartPaddingPx + col1InnerWidthPx
                    val fullDrawerW = settledDrawerW + nearEdgePadPx
                    val startDrawerW = curSigned.coerceIn(0f, fullDrawerW)

                    isCommitFittingDrawer = true
                    wasOpeningWithNearPad = false
                    fitDrawerWidthAnim.snapTo(startDrawerW)
                    fitBottomOffsetAnim.snapTo(startDrawerW)

                    if (!viewModel.uiState.value.isOptionsPanelVisible) {
                        viewModel.updateUI { it.copy(isOptionsPanelVisible = true) }
                    }
                    val targetAnchor =
                        if (hasCol2Anchor) LandscapeLeftPanelState.Both else LandscapeLeftPanelState.OptionsOnly
                    val nextAnchors = buildLandscapeOptionsAnchors(
                        drawerSign = drawerSign,
                        col1InnerWidthPx = col1InnerWidthPx,
                        col2MeasuredWidthPx = col2MeasuredWidthPx,
                        outerStartPaddingPx = outerStartPaddingPx,
                        includeCol2OnlyAnchor = false,
                        includeSwappedAnchor = false,
                        swapDistancePx = swapDistancePx,
                        openingExtraPadPx = 0f
                    )
                    prevLandscapeOptionsSettled = targetAnchor
                    landscapeOptionsDraggableState.updateAnchors(nextAnchors, targetAnchor)
                    landscapeOptionsDraggableState.snapTo(targetAnchor)

                    coroutineScope {
                        launch {
                            fitDrawerWidthAnim.animateTo(
                                targetValue = fullDrawerW,
                                animationSpec = tween(settings.value.animationSpeedForLayer(1))
                            )
                        }
                        launch {
                            fitBottomOffsetAnim.animateTo(
                                targetValue = settledDrawerW,
                                animationSpec = tween(settings.value.animationSpeedForLayer(1))
                            )
                        }
                    }
                    isCommitFittingDrawer = false
                }
            }
        }

        LaunchedEffect(isDraggingLandscapeOptions, isLandscapeLayout) {
            if (!isLandscapeLayout) {
                isCommitSettling = false
                urlSettleOffsetAnim.snapTo(0f)
                return@LaunchedEffect
            }
            if (isDraggingLandscapeOptions) {
                isCommitSettling = false
                snapshotFlow {
                    val offset = landscapeOptionsDraggableState.offset
                    val signed = if (offset.isNaN()) 0f else offset * drawerSign
                    signed < -0.5f
                }.collect { isSwappingDrag ->
                    val target = if (isSwappingDrag) {
                        val totalEdgePads = floatingStartPadPx + floatingEndPadPx
                        if (isUrlBarOnRight) -totalEdgePads else totalEdgePads
                    } else {
                        0f
                    }
                    urlSettleOffsetAnim.snapTo(target)
                }
            } else if (urlSettleOffsetAnim.value != 0f) {
                val outcome = snapshotFlow {
                    val flingTarget = activeFlingTarget
                    val offset = landscapeOptionsDraggableState.offset
                    val settled = landscapeOptionsDraggableState.settledValue
                    val atZero = !offset.isNaN() && kotlin.math.abs(offset) <= 0.5f
                    when {
                        flingTarget == LandscapeLeftPanelState.Swapped ||
                        settled == LandscapeLeftPanelState.Swapped -> 1
                        flingTarget == LandscapeLeftPanelState.Hidden -> 2
                        flingTarget == null && !landscapeOptionsDraggableState.isAnimationRunning && atZero -> 2
                        else -> 0
                    }
                }.first { it != 0 }

                if (outcome == 1) {
                    isCommitSettling = true
                    urlSettleOffsetAnim.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(settings.value.animationSpeedForLayer(1))
                    )
                } else {
                    // keep locked spacing while returning on cancel and snap after exiting screen edge
                    snapshotFlow {
                        activeFlingTarget == null && !landscapeOptionsDraggableState.isAnimationRunning
                    }.first { it }
                    isCommitSettling = false
                    urlSettleOffsetAnim.snapTo(0f)
                }
            }
        }
        val anchorUpdateTarget = when (landscapeOptionsDraggableState.targetValue) {
            LandscapeLeftPanelState.Swapped ->
                if (includeSwappedAnchor) LandscapeLeftPanelState.Swapped else LandscapeLeftPanelState.Hidden
            LandscapeLeftPanelState.Both,
            LandscapeLeftPanelState.OptionsOnly -> if (hasCol2Anchor) {
                if (col1InnerWidthPx > 0f) LandscapeLeftPanelState.Both
                else if (includeCol2OnlyAnchor) LandscapeLeftPanelState.Col2Only
                else LandscapeLeftPanelState.Hidden
            } else {
                if (col1InnerWidthPx > 0f) LandscapeLeftPanelState.OptionsOnly else LandscapeLeftPanelState.Hidden
            }
            LandscapeLeftPanelState.Col2Only -> if (hasCol2Anchor) {
                if (includeCol2OnlyAnchor) LandscapeLeftPanelState.Col2Only
                else if (uiState.value.isOptionsPanelVisible && col1InnerWidthPx > 0f) LandscapeLeftPanelState.Both
                else LandscapeLeftPanelState.Hidden
            } else {
                LandscapeLeftPanelState.Hidden
            }
            LandscapeLeftPanelState.Hidden ->
                if (hasCol2Anchor && !isDraggingLandscapeOptions && activeFlingTarget == null && !landscapeOptionsDraggableState.isAnimationRunning && isLandscapeCol2Open) {
                    if (uiState.value.isOptionsPanelVisible && col1InnerWidthPx > 0f) LandscapeLeftPanelState.Both
                    else if (includeCol2OnlyAnchor) LandscapeLeftPanelState.Col2Only
                    else LandscapeLeftPanelState.Hidden
                } else {
                    LandscapeLeftPanelState.Hidden
                }
        }
        SideEffect {
            if (landscapeOptionsDraggableState.anchors != landscapeOptionsAnchors &&
                !isDraggingLandscapeOptions &&
                activeFlingTarget == null
            ) {
                landscapeOptionsDraggableState.updateAnchors(landscapeOptionsAnchors, anchorUpdateTarget)
            }
        }

        val flingVelocityThresholdPx = with(density) { 400.dp.toPx() }
        val flingAnimDurationMs = settings.value.animationSpeedForLayer(1)
        val landscapeOptionsFling = remember(
            landscapeOptionsDraggableState,
            flingVelocityThresholdPx,
            flingAnimDurationMs
        ) {
            object : FlingBehavior {
                override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                    val currentOffset = landscapeOptionsDraggableState.offset
                    if (currentOffset.isNaN()) return 0f
                    val targetState = resolveLandscapeFlingTarget(
                        anchors = landscapeOptionsDraggableState.anchors,
                        offset = currentOffset,
                        velocity = initialVelocity,
                        velocityThresholdPx = flingVelocityThresholdPx
                    ) ?: return 0f
                    val targetOffset = landscapeOptionsDraggableState.anchors.positionOf(targetState)
                    if (targetOffset.isNaN()) return 0f

                    activeFlingTarget = targetState
                    try {
                        if (kotlin.math.abs(targetOffset - currentOffset) < 0.5f) {
                            scrollBy(targetOffset - currentOffset)
                        } else {
                            var lastValue = currentOffset
                            animate(
                                initialValue = currentOffset,
                                targetValue = targetOffset,
                                animationSpec = tween(flingAnimDurationMs)
                            ) { value, _ ->
                                val delta = value - lastValue
                                val consumed = scrollBy(delta)
                                lastValue += consumed
                            }
                        }
                    } finally {
                        activeFlingTarget = null
                    }
                    return 0f
                }
            }
        }

        val initialAppsState = if (uiState.value.isAppsPanelVisible) RevealState.Visible else RevealState.Hidden
        var landscapeAppsSettled by remember { mutableStateOf(initialAppsState) }
        var prevLandscapeAppsSettled by remember { mutableStateOf(initialAppsState) }
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

        LaunchedEffect(isLandscapeLayout) {
            if (!isLandscapeLayout) {
                col2MeasuredWidthPx = 0f
                prevObservedOptionsVisible = uiState.value.isOptionsPanelVisible
            }
        }
        if (!isLandscapeLayout) {
            SideEffect {
                prevLandscapeOptionsSettled = landscapeOptionsDraggableState.settledValue
            }
        }

        if (isLandscapeLayout) {
            LaunchedEffect(
                landscapeOptionsDraggableState.settledValue,
                activeFlingTarget,
                isDraggingLandscapeOptions
            ) {
                if (isDraggingLandscapeOptions || activeFlingTarget != null) return@LaunchedEffect
                val newSettled = landscapeOptionsDraggableState.settledValue
                if (newSettled == LandscapeLeftPanelState.Swapped) {
                    // finish post-release edge fit animation before resetting anchors to new side
                    if (urlSettleOffsetAnim.isRunning) {
                        snapshotFlow { !urlSettleOffsetAnim.isRunning || isDraggingLandscapeOptions }.first { it }
                    }
                    if (!isDraggingLandscapeOptions && urlSettleOffsetAnim.value != 0f) {
                        isCommitSettling = true
                        urlSettleOffsetAnim.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(settings.value.animationSpeedForLayer(1))
                        )
                    }
                    isCommitSettling = false
                    val nextRight = !isUrlBarOnRight
                    val nextDrawerSign = if (nextRight) -1f else 1f
                    isUrlBarOnRight = nextRight
                    viewModel.updateSettings { it.copy(isLandscapeUrlBarOnRight = nextRight) }
                    val nextNearPadPx = if (nextRight) floatingEndPadPx else floatingStartPadPx
                    val nextAnchors = buildLandscapeOptionsAnchors(
                        drawerSign = nextDrawerSign,
                        col1InnerWidthPx = col1InnerWidthPx,
                        col2MeasuredWidthPx = col2MeasuredWidthPx,
                        outerStartPaddingPx = outerStartPaddingPx,
                        includeCol2OnlyAnchor = false,
                        includeSwappedAnchor = swapDistancePx > 1f,
                        swapDistancePx = swapDistancePx,
                        openingExtraPadPx = if (wasOpeningWithNearPad) nextNearPadPx else 0f
                    )
                    prevLandscapeOptionsSettled = LandscapeLeftPanelState.Hidden
                    landscapeOptionsDraggableState.updateAnchors(nextAnchors, LandscapeLeftPanelState.Hidden)
                    landscapeOptionsDraggableState.snapTo(LandscapeLeftPanelState.Hidden)
                    return@LaunchedEffect
                }
                if (newSettled != prevLandscapeOptionsSettled) {
                    val curUi = viewModel.uiState.value
                    val curCol2Open =
                        (curUi.isSettingsPanelVisible && currentSettingView == SettingPanelView.MAIN && !isFirstModeLoad) ||
                            curUi.isFindInPageVisible ||
                            curUi.isTabDataPanelVisible
                    when (newSettled) {
                        LandscapeLeftPanelState.Both,
                        LandscapeLeftPanelState.OptionsOnly -> {
                            if (!curUi.isOptionsPanelVisible) {
                                viewModel.updateUI { it.copy(isOptionsPanelVisible = true) }
                            }
                        }
                        LandscapeLeftPanelState.Col2Only -> {
                            if (curUi.isOptionsPanelVisible) {
                                viewModel.updateUI { it.copy(isOptionsPanelVisible = false) }
                            }
                        }
                        LandscapeLeftPanelState.Hidden -> {
                            if (curUi.isOptionsPanelVisible || curCol2Open) {
                                viewModel.updateUI {
                                    it.copy(
                                        isOptionsPanelVisible = false,
                                        isSettingsPanelVisible = if (currentSettingView == SettingPanelView.MAIN) false else it.isSettingsPanelVisible,
                                        isFindInPageVisible = false,
                                        isTabDataPanelVisible = false
                                    )
                                }
                            }
                        }
                        LandscapeLeftPanelState.Swapped -> {}
                    }
                    prevLandscapeOptionsSettled = newSettled
                }
            }
            LaunchedEffect(
                uiState.value.isOptionsPanelVisible,
                isLandscapeCol2Open,
                hasCol2Anchor,
                col1InnerWidthPx
            ) {
                val curOffset = landscapeOptionsDraggableState.offset
                val isSwappingNow = landscapeOptionsDraggableState.targetValue == LandscapeLeftPanelState.Swapped ||
                    (!curOffset.isNaN() && (-curOffset * drawerSign) > 0.5f)
                if (isDraggingLandscapeOptions ||
                    activeFlingTarget != null ||
                    isCommitFittingDrawer ||
                    isClosingDrawerShiftActive ||
                    isSwappingNow ||
                    prevLandscapeOptionsSettled != landscapeOptionsDraggableState.settledValue
                ) {
                    prevObservedOptionsVisible = uiState.value.isOptionsPanelVisible
                    return@LaunchedEffect
                }
                if (uiState.value.isOptionsPanelVisible && wasOpeningWithNearPad) {
                    wasOpeningWithNearPad = false
                }
                val optionsChanged = prevObservedOptionsVisible != uiState.value.isOptionsPanelVisible
                prevObservedOptionsVisible = uiState.value.isOptionsPanelVisible

                val targetState: LandscapeLeftPanelState? = when {
                    uiState.value.isOptionsPanelVisible -> when {
                        isLandscapeCol2Open && hasCol2Anchor ->
                            if (col1InnerWidthPx > 0f) LandscapeLeftPanelState.Both else LandscapeLeftPanelState.Col2Only
                        !isLandscapeCol2Open && !hasCol2Anchor ->
                            if (col1InnerWidthPx > 0f) LandscapeLeftPanelState.OptionsOnly else LandscapeLeftPanelState.Hidden
                        hasCol2Anchor ->
                            if (col1InnerWidthPx > 0f) LandscapeLeftPanelState.Both else LandscapeLeftPanelState.Col2Only
                        else -> null
                    }
                    !isLandscapeCol2Open -> LandscapeLeftPanelState.Hidden
                    hasCol2Anchor -> LandscapeLeftPanelState.Col2Only
                    else -> null
                }

                if (targetState != null) {
                    val targetOffset = landscapeOptionsDraggableState.anchors.positionOf(targetState)
                    if (!targetOffset.isNaN()) {
                        val isOffsetAtTarget =
                            kotlin.math.abs(landscapeOptionsDraggableState.offset - targetOffset) < 1f

                        // let column two shrink naturally via animatedvisibility when options visibility did not change
                        val isWaitingForCol2ShrinkOnly = !optionsChanged && !isLandscapeCol2Open && hasCol2Anchor

                        if (!isWaitingForCol2ShrinkOnly && (
                                landscapeOptionsDraggableState.currentValue != targetState ||
                                landscapeOptionsDraggableState.targetValue != targetState ||
                                (!landscapeOptionsDraggableState.isAnimationRunning && !isOffsetAtTarget)
                            )
                        ) {
                            landscapeOptionsDraggableState.animateTo(
                                targetValue = targetState,
                                animationSpec = tween(settings.value.animationSpeedForLayer(1))
                            )
                        }
                    }
                }
            }

            LaunchedEffect(landscapeAppsDraggableState.settledValue) {
                val newSettled = landscapeAppsDraggableState.settledValue
                landscapeAppsSettled = newSettled
                if (newSettled != prevLandscapeAppsSettled) {
                    val shouldBeVisible = newSettled == RevealState.Visible
                    if (uiState.value.isAppsPanelVisible != shouldBeVisible) {
                        viewModel.updateUI { it.copy(isAppsPanelVisible = shouldBeVisible) }
                    }
                }
                prevLandscapeAppsSettled = newSettled
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
            val wasSpecialMode = viewModel.uiState.value.isPinningApp ||
                viewModel.uiState.value.isCreatingProfile ||
                viewModel.uiState.value.isRenamingProfile ||
                viewModel.uiState.value.isFindInPageVisible
            viewModel.updateUI {
                it.copy(
                    isFocusOnUrlTextField = false,
                    isFindInPageVisible = false,
                    isPinningApp = false,
                    isCreatingProfile = false,
                    isRenamingProfile = false
                )
            }
            if (wasSpecialMode) {
                val liveUrl = viewModel.activeTab?.currentURL ?: ""
                textFieldState.setTextAndPlaceCursorAtEnd(liveUrl.toDomain())
            }
            focusManager.clearFocus()
            keyboardController?.hide()
        }

        val submitUrlBar: (Boolean) -> Unit = { forceNewTab ->
            hasLandscapeUrlDraft = false
            val currentSubmitUi = viewModel.uiState.value
            if (currentSubmitUi.isFindInPageVisible && !forceNewTab) {
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
                    if (openInNewTab && !currentSubmitUi.isCreatingProfile && !currentSubmitUi.isPinningApp && !currentSubmitUi.isRenamingProfile) {
                        viewModel.createNewTab(viewModel.activeTabIndex.value + 1, "")
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        viewModel.updateUI { it.copy(isFocusOnUrlTextField = false) }
                    } else {
                        when {
                            currentSubmitUi.isCreatingProfile -> {
                                viewModel.createNewProfile()
                                viewModel.updateUI {
                                    it.copy(
                                        isCreatingProfile = false,
                                        isAppsPanelVisible = true
                                    )
                                }
                            }

                            currentSubmitUi.isPinningApp -> {
                                val customIconInput = (customIconUrlState.text).toString().trim()
                                val finalIconUrl =
                                    customIconInput.ifEmpty { viewModel.activeTab!!.currentFaviconUrl }
                                viewModel.pinApp(
                                    title = viewModel.activeTab!!.currentTitle,
                                    url = resetUrl,
                                    iconUrl = finalIconUrl,
                                )
                                viewModel.updateUI { it.copy(isPinningApp = false) }
                            }

                            currentSubmitUi.isRenamingProfile -> {
                                viewModel.updateUI {
                                    it.copy(
                                        isRenamingProfile = false,
                                        isAppsPanelVisible = true
                                    )
                                }
                            }

                            else -> activeSession.reload()
                        }

                        focusManager.clearFocus()
                        keyboardController?.hide()
                        val liveUrl = viewModel.activeTab?.currentURL ?: ""
                        textFieldState.setTextAndPlaceCursorAtEnd(liveUrl.toDomain())
                        viewModel.updateUI { it.copy(isFocusOnUrlTextField = false) }
                    }
                } else {
                    when {
                        currentSubmitUi.isRenamingProfile -> {
                            viewModel.renameProfile(input)
                            val liveUrl = viewModel.activeTab?.currentURL ?: ""
                            textFieldState.setTextAndPlaceCursorAtEnd(liveUrl.toDomain())
                            viewModel.updateUI {
                                it.copy(
                                    isRenamingProfile = false,
                                    isAppsPanelVisible = true
                                )
                            }
                        }

                        currentSubmitUi.isCreatingProfile -> {
                            viewModel.createNewProfile(input)
                            val liveUrl = viewModel.activeTab?.currentURL ?: ""
                            textFieldState.setTextAndPlaceCursorAtEnd(liveUrl.toDomain())
                            viewModel.updateUI {
                                it.copy(
                                    isCreatingProfile = false,
                                    isAppsPanelVisible = true
                                )
                            }
                        }

                        currentSubmitUi.isPinningApp -> {
                            val customIconInput =
                                (customIconUrlState.text).toString().trim()
                            val finalIconUrl =
                                customIconInput.ifEmpty { viewModel.activeTab!!.currentFaviconUrl }

                            viewModel.pinApp(
                                title = input,
                                url = resetUrl,
                                iconUrl = finalIconUrl,
                                onPinned = { finalUrl ->
                                    if (finalUrl != LocalAppServer.toVirtualUrl(resetUrl)) {
                                        onNewUrl(finalUrl)
                                    }
                                    textFieldState.setTextAndPlaceCursorAtEnd(finalUrl.toDomain())
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

        val currentIsLandscapeLayout by rememberUpdatedState(isLandscapeLayout)
        val currentDismissUrlBar by rememberUpdatedState(dismissUrlBar)
        val currentEditCurrentUrl by rememberUpdatedState(editCurrentUrl)
        val currentSubmitUrlBar by rememberUpdatedState(submitUrlBar)

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
                currentUi.isPinningApp -> when (focusAction) {
                    KeyboardShortcutManager.UrlFocusAction.SELECT_ALL -> viewModel.activeTab?.currentTitle ?: ""
                    KeyboardShortcutManager.UrlFocusAction.EMPTY -> ""
                    null -> textFieldState.text.toString()
                }
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
                        currentSubmitUrlBar(false)
                    } else {
                        if (currentUi.isFindInPageVisible) {
                            viewModel.findInPageTextFieldState.setTextAndPlaceCursorAtEnd(typedText)
                        } else if (currentUi.isPinningApp) {
                            // keep pinning mode active when keyboard hides so user can tap icon link or pick image
                            hasLandscapeUrlDraft = false
                            viewModel.keyboardShortcutManager.clearPendingNewTab()
                        } else if (currentUi.isCreatingProfile || currentUi.isRenamingProfile) {
                            hasLandscapeUrlDraft = false
                            viewModel.keyboardShortcutManager.clearPendingNewTab()
                            viewModel.updateUI {
                                it.copy(
                                    isCreatingProfile = false,
                                    isRenamingProfile = false
                                )
                            }
                            val liveUrl = viewModel.activeTab?.currentURL ?: ""
                            textFieldState.setTextAndPlaceCursorAtEnd(liveUrl.toDomain())
                        } else if (typedText.isNotBlank()) {
                            hasLandscapeUrlDraft = true
                        } else {
                            hasLandscapeUrlDraft = false
                            viewModel.keyboardShortcutManager.clearPendingNewTab()
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

        val currentOpenLandscapeUrlInput by rememberUpdatedState(openLandscapeUrlInput)

        LaunchedEffect(Unit) {
            viewModel.keyboardShortcutManager.shortcutEvents.collect { shortcut ->
                if (currentIsLandscapeLayout) {
                    when (shortcut) {
                        BrowserShortcut.FOCUS_OR_EDIT_URL -> currentOpenLandscapeUrlInput(KeyboardShortcutManager.UrlFocusAction.SELECT_ALL)
                        BrowserShortcut.NEW_TAB -> currentOpenLandscapeUrlInput(KeyboardShortcutManager.UrlFocusAction.EMPTY)
                        BrowserShortcut.OPEN_FIND_IN_PAGE -> currentOpenLandscapeUrlInput(null)
                        else -> {}
                    }
                } else {
                    when (shortcut) {
                        BrowserShortcut.DISMISS_URL_BAR -> currentDismissUrlBar()
                        BrowserShortcut.FOCUS_OR_EDIT_URL -> currentEditCurrentUrl()
                        BrowserShortcut.NEW_TAB -> {
                            textFieldState.setTextAndPlaceCursorAtEnd("")
                            urlBarFocusRequester.requestFocus()
                            keyboardController?.show()
                        }
                        BrowserShortcut.OPEN_FIND_IN_PAGE -> {
                            urlBarFocusRequester.requestFocus()
                            keyboardController?.show()
                        }
                        BrowserShortcut.OPEN_URL_IN_NEW_TAB -> currentSubmitUrlBar(true)
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

        val urlBarSection: @Composable (Modifier) -> Unit = { urlBarModifier ->
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
                            var wasIconFieldFocused by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                TextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusProperties { canFocus = !isLandscapeLayout }
                                        .heightIn(min = settings.value.heightForLayer(2).dp)
                                        .onFocusChanged { focusState ->
                                            val wasFocused = wasIconFieldFocused
                                            wasIconFieldFocused = focusState.isFocused
                                            viewModel.updateUI {
                                                it.copy(isFocusOnIconUrlTextField = focusState.isFocused)
                                            }
                                            // only run blur cleanup when focus was actually held and lost in portrait
                                            if (wasFocused && !focusState.isFocused && !isLandscapeLayout) {
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
                                isSquare = true,
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

                    Box(modifier = Modifier.fillMaxWidth().then(urlBarModifier)) {
                        var wasUrlFieldFocused by remember { mutableStateOf(false) }
                        TextField(
                            modifier = Modifier
                                .heightIn(min = settings.value.heightForLayer(1).dp)
                                .fillMaxWidth()
                                .focusProperties { canFocus = !isLandscapeLayout }
                                .focusRequester(urlBarFocusRequester)
                                .onFocusChanged { focusState ->
                                    val wasFocused = wasUrlFieldFocused
                                    wasUrlFieldFocused = focusState.isFocused
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
                                    } else if (wasFocused && !isLandscapeLayout) {
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

                        val currentNavigateWebView by rememberUpdatedState(navigateWebView)
                        val currentCanGoBack by rememberUpdatedState(viewModel.activeTab!!.canGoBack)
                        val currentCanGoForward by rememberUpdatedState(viewModel.activeTab!!.canGoForward)

                        val isInlineSpecialInput =
                            uiState.value.isFindInPageVisible ||
                                uiState.value.isPinningApp ||
                                uiState.value.isCreatingProfile ||
                                uiState.value.isRenamingProfile

                        if (uiState.value.isUrlOverlayBoxVisible && !uiState.value.isFocusOnUrlTextField && !isInlineSpecialInput) Box(
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
                                                if (currentIsLandscapeLayout) {
                                                    currentOpenLandscapeUrlInput(null)
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

                        if (isLandscapeLayout && isInlineSpecialInput) {
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
                val dragOffset = landscapeOptionsDraggableState.offset
                val signedDrawerOffset = if (dragOffset.isNaN()) 0f else dragOffset * drawerSign
                val swapRevealPx = (-signedDrawerOffset).coerceIn(0f, swapDistancePx)
                val swapProgress = (swapRevealPx / swapDistancePx).coerceIn(0f, 1f)
                val settledLeftState = landscapeOptionsDraggableState.settledValue
                val isSettledDrawerOpen =
                    settledLeftState == LandscapeLeftPanelState.OptionsOnly ||
                    settledLeftState == LandscapeLeftPanelState.Col2Only ||
                    settledLeftState == LandscapeLeftPanelState.Both
                val isClosingDragLive =
                    nearEdgePadPx > 0.5f && isDraggingLandscapeOptions && isSettledDrawerOpen
                val useOpeningDrawerShift =
                    (wasOpeningWithNearPad && signedDrawerOffset > 0.5f) || isCommitFittingDrawer
                val useClosingDrawerShift =
                    !useOpeningDrawerShift && (isClosingDragLive || isClosingDrawerShiftActive)
                val drawerShiftInt = if (useOpeningDrawerShift || useClosingDrawerShift) {
                    (if (isUrlBarOnRight) nearEdgePadPx else -nearEdgePadPx).roundToInt()
                } else {
                    0
                }
                val extraMaxWidthInt =
                    if (useOpeningDrawerShift || useClosingDrawerShift) nearEdgePadPx.roundToInt() else 0
                val closingExtraWidthPx = when {
                    isClosingDragLive -> nearEdgePadPx
                    isClosingDrawerShiftActive -> closingExtraClipAnim.value
                    else -> 0f
                }

                Layout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            if (size.width > 0) {
                                bottomBarWidthPx = size.width.toFloat()
                            }
                        }
                        .padding(vertical = settings.value.padding.dp)
                        .windowInsetsPadding(WindowInsets.ime)
                        .padding(bottom = floatingPanelBottomPadding),
                    content = {

                        val settledAnchorPos = landscapeOptionsDraggableState.anchors.positionOf(settledLeftState)
                        val isAtSettledAnchor = !dragOffset.isNaN() &&
                            !settledAnchorPos.isNaN() &&
                            kotlin.math.abs(dragOffset - settledAnchorPos) < 1.5f
                        val useLiveLayout = !isDraggingLandscapeOptions &&
                            activeFlingTarget == null &&
                            !isCommitFittingDrawer &&
                            !isClosingDrawerShiftActive &&
                            !landscapeOptionsDraggableState.isAnimationRunning &&
                            isAtSettledAnchor &&
                            (
                                (uiState.value.isOptionsPanelVisible &&
                                    (settledLeftState == LandscapeLeftPanelState.OptionsOnly ||
                                        settledLeftState == LandscapeLeftPanelState.Both)) ||
                                (!uiState.value.isOptionsPanelVisible &&
                                    (settledLeftState == LandscapeLeftPanelState.Col2Only ||
                                        (settledLeftState == LandscapeLeftPanelState.Hidden &&
                                            prevLandscapeOptionsSettled == LandscapeLeftPanelState.Hidden &&
                                            isLandscapeCol2Open)))
                            )
                        val hideExitingCol2AfterDrag = !isLandscapeCol2Open &&
                            settledLeftState == LandscapeLeftPanelState.Hidden &&
                            activeFlingTarget == null &&
                            !landscapeOptionsDraggableState.isAnimationRunning &&
                            !isClosingDrawerShiftActive
                        val currentOptionsThreshold = if (hasCol2Anchor) {
                            outerStartPaddingPx + col2MeasuredWidthPx
                        } else {
                            0f
                        }
                        val isUserMovingOptions = isDraggingLandscapeOptions ||
                            activeFlingTarget != null ||
                            landscapeOptionsDraggableState.isAnimationRunning ||
                            landscapeOptionsDraggableState.targetValue == LandscapeLeftPanelState.Both ||
                            landscapeOptionsDraggableState.targetValue == LandscapeLeftPanelState.OptionsOnly
                        val showOptionsInRow = uiState.value.isOptionsPanelVisible || heldClosingOptions || (
                            col1InnerWidthPx > 0f &&
                            !dragOffset.isNaN() &&
                            signedDrawerOffset > currentOptionsThreshold + 0.5f &&
                            isUserMovingOptions
                        )

                        val optionsColumn: @Composable () -> Unit = {
                            if (showOptionsInRow) {
                                Box(modifier = Modifier) {
                                    Box {
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
                        }

                        val col2Row: @Composable () -> Unit = {
                            Row(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .onSizeChanged { size ->
                                        col2MeasuredWidthPx = size.width.toFloat()
                                    }
                                    .layout { measurable, constraints ->
                                        val placeable = measurable.measure(constraints)
                                        if (hideExitingCol2AfterDrag) {
                                            layout(0, 0) {}
                                        } else {
                                            layout(placeable.width, placeable.height) {
                                                placeable.place(0, 0)
                                            }
                                        }
                                    },
                                verticalAlignment = Alignment.Bottom
                            ) {
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
                                    leftStemHeight = leftStemHeight,
                                    isMirrored = isUrlBarOnRight
                                )

                                SettingsPanel(
                                    onCloseAllTabs = onCloseAllTabs,
                                    confirmationPopup = confirmationPopup,
                                    onLoginClick = onSyncSignInClick,
                                    isLandscapeLayout = true,
                                    leftStemHeight = leftStemHeight,
                                    currentViewOverride = currentSettingView,
                                    onCurrentViewChange = {
                                        currentSettingView = it
                                        viewModel.isLandscapeSettingsSubView.value =
                                            it != SettingPanelView.MAIN || isFirstModeLoad
                                    },
                                    renderMainOnly = true,
                                    isMirrored = isUrlBarOnRight
                                )

                                TabDataPanel(
                                    isTabDataPanelVisible = isTabDataPanelVisible,
                                    onDismiss = { viewModel.updateUI { it.copy(isTabDataPanelVisible = false) } },
                                    onPermissionToggle = handlePermissionToggle,
                                    onClearSiteData = handleClearInspectedTabData,
                                    onCloseTab = handleCloseInspectedTab,
                                    isLandscapeLayout = true,
                                    leftStemHeight = leftStemHeight,
                                    isMirrored = isUrlBarOnRight
                                )
                            }
                        }

                        val drawerTargetWidthPx = when {
                            isCommitFittingDrawer -> fitDrawerWidthAnim.value
                            useClosingDrawerShift -> signedDrawerOffset.coerceAtLeast(0f) + closingExtraWidthPx
                            else -> signedDrawerOffset
                        }

                        // full-height vertical drawer pushed toward the active screen edge
                        LandscapeOptionsWrapper(
                            targetWidthPx = drawerTargetWidthPx,
                            outerStartPaddingPx = outerStartPaddingPx,
                            extraMaxWidthInt = extraMaxWidthInt,
                            useLiveLayout = useLiveLayout,
                            isRightEdge = isUrlBarOnRight
                        ) {
                            Row(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .padding(
                                        start = if (isUrlBarOnRight) 0.dp else settings.value.padding.dp,
                                        end = if (isUrlBarOnRight) settings.value.padding.dp else 0.dp
                                    )
                                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                if (isUrlBarOnRight) {
                                    col2Row()
                                    optionsColumn()
                                } else {
                                    optionsColumn()
                                    col2Row()
                                }
                            }
                        }

                        val isConfirmRightVisible = viewModel.confirmationState.value != null
                        val isContextRightVisible = viewModel.contextMenuData.value != null
                        val isPermissionRightVisible = uiState.value.isPermissionPanelVisible
                        val isMediaRightVisible = uiState.value.isUrlBarVisible &&
                            !uiState.value.isFocusOnTextField &&
                            !uiState.value.isFindInPageVisible &&
                            (viewModel.geckoManager.activeGeckoMediaSession != null ||
                                viewModel.geckoManager.activeSilentVideoSession != null ||
                                viewModel.geckoManager.currentFullscreenSession != null)
                        val isTabsRightVisible = viewModel.tabs.isNotEmpty() &&
                            uiState.value.isTabsPanelVisible &&
                            !uiState.value.isFindInPageVisible
                        val isTextEditRightVisible = isTextEditVisible
                        val anyRightPanelVisible = isConfirmRightVisible ||
                            isContextRightVisible ||
                            isPermissionRightVisible ||
                            isMediaRightVisible ||
                            isTabsRightVisible ||
                            isTextEditRightVisible

                        val isBottomBarShowing =
                            uiState.value.isBottomPanelVisible && uiState.value.appState == AppState.REGULAR
                        var heldConfirmRight by remember { mutableStateOf(isConfirmRightVisible) }
                        var heldContextRight by remember { mutableStateOf(isContextRightVisible) }
                        var heldPermissionRight by remember { mutableStateOf(isPermissionRightVisible) }
                        var heldMediaRight by remember { mutableStateOf(isMediaRightVisible) }
                        var heldTabsRight by remember { mutableStateOf(isTabsRightVisible) }
                        var heldTextEditRight by remember { mutableStateOf(isTextEditRightVisible) }
                        var effectiveAnyRightPanelVisible by remember { mutableStateOf(anyRightPanelVisible) }

                        // preserve last visible panel while sliding out to bottom and avoid double slide on full bar exit
                        if (isBottomBarShowing) {
                            effectiveAnyRightPanelVisible = anyRightPanelVisible
                            if (anyRightPanelVisible) {
                                heldConfirmRight = isConfirmRightVisible
                                heldContextRight = isContextRightVisible
                                heldPermissionRight = isPermissionRightVisible
                                heldMediaRight = isMediaRightVisible
                                heldTabsRight = isTabsRightVisible
                                heldTextEditRight = isTextEditRightVisible
                            }
                        }

                        val bottomTravelPx = with(density) {
                            (settings.value.padding.dp + floatingPanelBottomPadding).roundToPx()
                        }
                        val isSwappingToCommit = !isDraggingLandscapeOptions &&
                            landscapeOptionsDraggableState.targetValue == LandscapeLeftPanelState.Swapped
                        val targetTabsOnLeft =
                            if (isSwappingToCommit) !isUrlBarOnRight else isUrlBarOnRight
                        val tabsStartPadding by animateDpAsState(
                            targetValue = if (targetTabsOnLeft) settings.value.padding.dp else 0.dp,
                            animationSpec = tween(settings.value.animationSpeedForLayer(1)),
                            label = "LandscapeTabsStartPadding"
                        )
                        val tabsEndPadding by animateDpAsState(
                            targetValue = if (targetTabsOnLeft) 0.dp else settings.value.padding.dp,
                            animationSpec = tween(settings.value.animationSpeedForLayer(1)),
                            label = "LandscapeTabsEndPadding"
                        )

                        val totalEdgePads = floatingStartPadPx + floatingEndPadPx
                        val urlCommitFitFraction = if (isCommitSettling && totalEdgePads > 0.5f) {
                            (1f - kotlin.math.abs(urlSettleOffsetAnim.value) / totalEdgePads).coerceIn(0f, 1f)
                        } else if (isCommitSettling) {
                            1f
                        } else {
                            0f
                        }

                        // bottom horizontal bar with cyclic wrap-around swap between left and right halves
                        LandscapeCyclicBottomBar(
                            modifier = Modifier.fillMaxWidth(),
                            isUrlBarOnRight = isUrlBarOnRight,
                            swapProgress = swapProgress,
                            startPadPx = floatingStartPadPx,
                            endPadPx = floatingEndPadPx,
                            innerPadPx = outerStartPaddingPx,
                            urlExtraShiftPx = urlSettleOffsetAnim.value,
                            urlStationaryShiftPx = if (isCommitSettling) urlSettleOffsetAnim.value else 0f,
                            urlCommitFitFraction = urlCommitFitFraction,
                            urlHalfContent = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = settings.value.padding.dp)
                                        .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                        .anchoredDraggable(
                                            state = landscapeAppsDraggableState,
                                            orientation = Orientation.Vertical,
                                            flingBehavior = landscapeAppsFling,
                                            enabled = isDragEnabled
                                        )
                                        .pointerInput(isDragEnabled, drawerSign) {
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
                                                                if (dx * drawerSign > 0f) {
                                                                    viewModel.updateUI { it.copy(isOptionsPanelVisible = true) }
                                                                } else {
                                                                    viewModel.updateUI {
                                                                        it.copy(
                                                                            isOptionsPanelVisible = false,
                                                                            isSettingsPanelVisible = if (currentSettingView == SettingPanelView.MAIN) false else it.isSettingsPanelVisible,
                                                                            isFindInPageVisible = false,
                                                                            isTabDataPanelVisible = false
                                                                        )
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
                                        onCurrentViewChange = {
                                            currentSettingView = it
                                            viewModel.isLandscapeSettingsSubView.value =
                                                it != SettingPanelView.MAIN || isFirstModeLoad
                                        },
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
                                    urlBarSection(
                                        Modifier.anchoredDraggable(
                                            state = landscapeOptionsDraggableState,
                                            orientation = Orientation.Horizontal,
                                            flingBehavior = landscapeOptionsFling,
                                            enabled = isDragEnabled,
                                            interactionSource = landscapeOptionsInteractionSource
                                        )
                                    )

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
                                                    textFieldState.setTextAndPlaceCursorAtEnd("")
                                                    viewModel.updateUI {
                                                        it.copy(
                                                            isPinningApp = true,
                                                            isAppsPanelVisible = false
                                                        )
                                                    }
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
                            },
                            tabsHalfContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = tabsStartPadding, end = tabsEndPadding)
                                ) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = effectiveAnyRightPanelVisible,
                                        enter = slideInVertically(
                                            animationSpec = tween(settings.value.animationSpeedForLayer(1))
                                        ) { it + bottomTravelPx },
                                        exit = slideOutVertically(
                                            animationSpec = tween(settings.value.animationSpeedForLayer(1))
                                        ) { it + bottomTravelPx }
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                        ) {
                                            ConfirmationPanel(isVisibleOverride = heldConfirmRight)
                                            ContextMenuPanel(
                                                onDownload = onDownload,
                                                isVisibleOverride = heldContextRight
                                            )
                                            PermissionPanel(
                                                isUrlBarVisible = uiState.value.isUrlBarVisible,
                                                hasContentBelow = heldTabsRight || heldTextEditRight,
                                                isVisibleOverride = heldPermissionRight,
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
                                            MediaPanel(isVisibleOverride = heldMediaRight)
                                            TabsPanel(
                                                updateInspectingTab = updateInspectingTab,
                                                isLandscapeLayout = true,
                                                hasContentBelow = heldTextEditRight,
                                                isVisibleOverride = heldTabsRight,
                                            )
                                            TextEditPanel(
                                                isVisible = heldTextEditRight,
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
                            }
                        )
                    }
                ) { measurables, constraints ->
                    val drawerPlaceable = measurables[0].measure(
                        constraints.copy(minWidth = 0)
                    )
                    // keep bottom horizontal bar fixed at full container width while pushed aside
                    val bottomPlaceable = measurables[1].measure(
                        constraints.copy(minWidth = constraints.maxWidth, maxWidth = constraints.maxWidth)
                    )
                    val layoutHeight = maxOf(drawerPlaceable.height, bottomPlaceable.height)
                    val bottomShiftInt = when {
                        isCommitFittingDrawer -> fitBottomOffsetAnim.value.roundToInt()
                        useClosingDrawerShift -> signedDrawerOffset.roundToInt().coerceAtLeast(0)
                        else -> drawerPlaceable.width
                    }
                    layout(constraints.maxWidth, layoutHeight) {
                        if (isUrlBarOnRight) {
                            drawerPlaceable.placeRelative(
                                constraints.maxWidth - drawerPlaceable.width + drawerShiftInt,
                                layoutHeight - drawerPlaceable.height
                            )
                            bottomPlaceable.placeRelative(
                                -bottomShiftInt,
                                layoutHeight - bottomPlaceable.height
                            )
                        } else {
                            drawerPlaceable.placeRelative(
                                drawerShiftInt,
                                layoutHeight - drawerPlaceable.height
                            )
                            bottomPlaceable.placeRelative(
                                bottomShiftInt,
                                layoutHeight - bottomPlaceable.height
                            )
                        }
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
                    urlBarSection(Modifier)

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

private enum class LandscapeLeftPanelState {
    Hidden,
    OptionsOnly,
    Col2Only,
    Both,
    Swapped,
}

private fun resolveLandscapeFlingTarget(
    anchors: DraggableAnchors<LandscapeLeftPanelState>,
    offset: Float,
    velocity: Float,
    velocityThresholdPx: Float,
): LandscapeLeftPanelState? {
    val sorted = LandscapeLeftPanelState.entries.mapNotNull { state ->
        val pos = anchors.positionOf(state)
        if (pos.isNaN()) null else state to pos
    }.sortedBy { it.second }
    if (sorted.isEmpty()) return null
    if (offset <= sorted.first().second) return sorted.first().first
    if (offset >= sorted.last().second) return sorted.last().first

    for (i in 0 until sorted.lastIndex) {
        val (lowerState, lowerPos) = sorted[i]
        val (upperState, upperPos) = sorted[i + 1]
        if (offset in lowerPos..upperPos) {
            return when {
                velocity >= velocityThresholdPx -> upperState
                velocity <= -velocityThresholdPx -> lowerState
                offset >= (lowerPos + upperPos) * 0.5f -> upperState
                else -> lowerState
            }
        }
    }
    return sorted.minByOrNull { kotlin.math.abs(it.second - offset) }?.first
}

private fun buildLandscapeOptionsAnchors(
    drawerSign: Float,
    col1InnerWidthPx: Float,
    col2MeasuredWidthPx: Float,
    outerStartPaddingPx: Float,
    includeCol2OnlyAnchor: Boolean,
    includeSwappedAnchor: Boolean,
    swapDistancePx: Float,
    openingExtraPadPx: Float = 0f,
): DraggableAnchors<LandscapeLeftPanelState> = DraggableAnchors {
    LandscapeLeftPanelState.Hidden at 0f
    if (includeSwappedAnchor && swapDistancePx > 1f) {
        LandscapeLeftPanelState.Swapped at (-drawerSign * swapDistancePx)
    }
    if (col2MeasuredWidthPx > 0.5f) {
        val col2Pos = drawerSign * (outerStartPaddingPx + col2MeasuredWidthPx)
        if (includeCol2OnlyAnchor) {
            LandscapeLeftPanelState.Col2Only at col2Pos
        }
        if (col1InnerWidthPx > 0f) {
            LandscapeLeftPanelState.Both at (col2Pos + drawerSign * col1InnerWidthPx)
        }
    } else if (col1InnerWidthPx > 0f) {
        LandscapeLeftPanelState.OptionsOnly at
            (drawerSign * (outerStartPaddingPx + col1InnerWidthPx + openingExtraPadPx))
    }
}

@Composable
private fun LandscapeCyclicBottomBar(
    modifier: Modifier = Modifier,
    isUrlBarOnRight: Boolean,
    swapProgress: Float,
    startPadPx: Float,
    endPadPx: Float,
    innerPadPx: Float,
    urlExtraShiftPx: Float,
    urlStationaryShiftPx: Float,
    urlCommitFitFraction: Float,
    urlHalfContent: @Composable () -> Unit,
    tabsHalfContent: @Composable () -> Unit,
) {
    val urlGraphicsLayer = rememberGraphicsLayer()
    val isSwapping = swapProgress > 0.001f

    Layout(
        modifier = modifier.drawWithContent {
            if (isSwapping) {
                clipRect(
                    left = -startPadPx,
                    top = -10000f,
                    right = size.width + endPadPx,
                    bottom = size.height + 10000f
                ) {
                    this@drawWithContent.drawContent()
                    val halfW = (size.width.toInt() / 2).toFloat()
                    val shiftPx = swapProgress * halfW
                    val primaryExtraExit = if (isUrlBarOnRight) {
                        (endPadPx - innerPadPx).coerceAtLeast(0f) * urlCommitFitFraction
                    } else {
                        (startPadPx - innerPadPx).coerceAtLeast(0f) * urlCommitFitFraction
                    }
                    // keep primary, tabs, and wrapped url bar moving one-to-one with finger and fit edges after release
                    val urlPrimaryX = if (isUrlBarOnRight) {
                        (size.width - halfW) + shiftPx + primaryExtraExit
                    } else {
                        -shiftPx - primaryExtraExit
                    }
                    val urlWrappedX = if (isUrlBarOnRight) {
                        (-halfW + shiftPx) + urlExtraShiftPx
                    } else {
                        (size.width - shiftPx) + urlExtraShiftPx
                    }
                    val urlTopY = size.height - urlGraphicsLayer.size.height.toFloat()
                    translate(left = urlPrimaryX, top = urlTopY) {
                        drawLayer(urlGraphicsLayer)
                    }
                    translate(left = urlWrappedX, top = urlTopY) {
                        drawLayer(urlGraphicsLayer)
                    }
                }
            } else {
                drawContent()
            }
        },
        content = {
            Box(
                modifier = Modifier.drawWithContent {
                    if (isSwapping) {
                        urlGraphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                    } else {
                        drawContent()
                    }
                }
            ) {
                urlHalfContent()
            }
            Box {
                tabsHalfContent()
            }
        }
    ) { measurables, constraints ->
        val totalW = constraints.maxWidth
        val halfW = totalW / 2
        val otherHalfW = totalW - halfW
        val urlPlaceable = measurables[0].measure(
            constraints.copy(minWidth = halfW, maxWidth = halfW, minHeight = 0)
        )
        val tabsPlaceable = measurables[1].measure(
            constraints.copy(minWidth = otherHalfW, maxWidth = otherHalfW, minHeight = 0)
        )
        val layoutHeight = maxOf(urlPlaceable.height, tabsPlaceable.height)
        val shiftPx = (swapProgress * halfW).roundToInt()

        val urlExtraInt = if (isSwapping) 0 else urlStationaryShiftPx.roundToInt()
        val urlStationaryX = (if (isUrlBarOnRight) totalW - halfW else 0) + urlExtraInt
        val tabsX = if (isUrlBarOnRight) shiftPx else halfW - shiftPx

        layout(totalW, layoutHeight) {
            urlPlaceable.place(urlStationaryX, layoutHeight - urlPlaceable.height)
            tabsPlaceable.place(tabsX, layoutHeight - tabsPlaceable.height)
        }
    }
}

@Composable
private fun LandscapeOptionsWrapper(
    targetWidthPx: Float,
    outerStartPaddingPx: Float,
    extraMaxWidthInt: Int,
    useLiveLayout: Boolean,
    isRightEdge: Boolean,
    content: @Composable () -> Unit
) {
    val safeWidth = if (targetWidthPx.isNaN()) 0f else targetWidthPx
    val targetTotalWidth = safeWidth.roundToInt().coerceAtLeast(0)

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
        // match current measured width during column two expand/shrink to avoid shifting options panel
        val currentWidth = if (useLiveLayout) {
            if (placeable.width <= outerStartPaddingPx.roundToInt()) 0 else placeable.width
        } else {
            targetTotalWidth.coerceIn(0, placeable.width + extraMaxWidthInt)
        }

        if (currentWidth == 0) {
            layout(0, 0) {}
        } else {
            layout(currentWidth, placeable.height) {
                placeable.place(if (isRightEdge) 0 else currentWidth - placeable.width, 0)
            }
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