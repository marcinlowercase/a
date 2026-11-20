package marcinlowercase.a.core.manager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import marcinlowercase.a.core.enum_class.AppState
import marcinlowercase.a.core.enum_class.BrowserShortcut
import marcinlowercase.a.core.server.LocalAppServer
import marcinlowercase.a.ui.viewmodel.BrowserViewModel

class KeyboardShortcutManager(private val context: Context) {

    private val _shortcutEvents = MutableSharedFlow<BrowserShortcut>(extraBufferCapacity = 8)
    val shortcutEvents = _shortcutEvents.asSharedFlow()

    enum class UrlFocusAction {
        SELECT_ALL,
        EMPTY
    }

    private val _pendingUrlFocusAction = MutableStateFlow<UrlFocusAction?>(null)
    val pendingUrlFocusAction = _pendingUrlFocusAction.asStateFlow()

    private val _isPendingNewTab = MutableStateFlow(false)
    val isPendingNewTab = _isPendingNewTab.asStateFlow()

    fun consumePendingUrlFocusAction(): UrlFocusAction? {
        val action = _pendingUrlFocusAction.value ?: return null
        _pendingUrlFocusAction.value = null
        return action
    }

    private var shiftDownPendingTap = false
    private var shiftDownTimeMs = 0L
    private var lastCleanShiftTapMs = 0L

    enum class MouseTouchResult {
        PASS_THROUGH,
        CONSUME,
        CANCEL_AND_CONSUME
    }

    private var isLeftMouseDown = false
    private var isRightChordPressed = false
    private var cancelActiveLeftPointer = false

    fun cancelShiftTap() {
        shiftDownPendingTap = false
        lastCleanShiftTapMs = 0L
    }

    private fun canShowBottomPanelWithMouse(viewModel: BrowserViewModel): Boolean {
        val uiState = viewModel.uiState.value
        return !viewModel.isStandaloneMode.value &&
                uiState.appState == AppState.REGULAR &&
                !uiState.isBottomPanelVisible
    }

    private fun triggerMouseShowBottomPanel(viewModel: BrowserViewModel) {
        viewModel.contextMenuData.value = null
        executeShortcut(BrowserShortcut.SHOW_BOTTOM_PANEL, viewModel)
        cancelActiveLeftPointer = true
    }

    fun handleTouchEvent(ev: MotionEvent, viewModel: BrowserViewModel): MouseTouchResult {
        val isMouse = ev.isFromSource(InputDevice.SOURCE_MOUSE) ||
                (ev.pointerCount > 0 && ev.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE)
        if (!isMouse) {
            isLeftMouseDown = false
            isRightChordPressed = false
            cancelActiveLeftPointer = false
            return MouseTouchResult.PASS_THROUGH
        }

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isLeftMouseDown = ev.buttonState == MotionEvent.BUTTON_PRIMARY
                isRightChordPressed = false
                cancelActiveLeftPointer = false
                return MouseTouchResult.PASS_THROUGH
            }

            MotionEvent.ACTION_MOVE -> {
                if (cancelActiveLeftPointer) {
                    return MouseTouchResult.CONSUME
                }
                if (isLeftMouseDown && canShowBottomPanelWithMouse(viewModel)) {
                    val isRightDown = (ev.buttonState and MotionEvent.BUTTON_SECONDARY) != 0
                    val isLeftStillDown = (ev.buttonState and MotionEvent.BUTTON_PRIMARY) != 0
                    if (isRightDown && isLeftStillDown) {
                        isRightChordPressed = true
                    } else if (isRightChordPressed && !isRightDown && isLeftStillDown) {
                        // right button clicked and released while left button stays held
                        isRightChordPressed = false
                        triggerMouseShowBottomPanel(viewModel)
                        return MouseTouchResult.CANCEL_AND_CONSUME
                    }
                }
                return MouseTouchResult.PASS_THROUGH
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                isLeftMouseDown = false
                isRightChordPressed = false
                if (cancelActiveLeftPointer) {
                    cancelActiveLeftPointer = false
                    return MouseTouchResult.CONSUME
                }
                return MouseTouchResult.PASS_THROUGH
            }

            else -> return MouseTouchResult.PASS_THROUGH
        }
    }

    fun handleGenericMotionEvent(ev: MotionEvent, viewModel: BrowserViewModel): MouseTouchResult {
        val isMouse = ev.isFromSource(InputDevice.SOURCE_MOUSE) ||
                (ev.pointerCount > 0 && ev.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE)
        if (!isMouse || !isLeftMouseDown || cancelActiveLeftPointer || !canShowBottomPanelWithMouse(viewModel)) {
            return MouseTouchResult.PASS_THROUGH
        }

        when (ev.actionMasked) {
            MotionEvent.ACTION_BUTTON_PRESS -> {
                if (ev.actionButton == MotionEvent.BUTTON_SECONDARY &&
                    (ev.buttonState and MotionEvent.BUTTON_PRIMARY) != 0
                ) {
                    isRightChordPressed = true
                    return MouseTouchResult.CONSUME
                }
            }

            MotionEvent.ACTION_BUTTON_RELEASE -> {
                if (ev.actionButton == MotionEvent.BUTTON_SECONDARY &&
                    isRightChordPressed &&
                    (ev.buttonState and MotionEvent.BUTTON_PRIMARY) != 0
                ) {
                    isRightChordPressed = false
                    triggerMouseShowBottomPanel(viewModel)
                    return MouseTouchResult.CANCEL_AND_CONSUME
                }
            }
        }
        return MouseTouchResult.PASS_THROUGH
    }

    fun clearPendingUrlFocusAction() {
        _pendingUrlFocusAction.value = null
    }

    fun consumePendingNewTab(): Boolean {
        if (!_isPendingNewTab.value) return false
        _isPendingNewTab.value = false
        return true
    }

    fun clearPendingNewTab() {
        _isPendingNewTab.value = false
    }

    fun handleKeyEvent(event: KeyEvent, viewModel: BrowserViewModel): Boolean {
        val uiState = viewModel.uiState.value
        val isCtrl = event.isCtrlPressed
        val isShift = event.isShiftPressed
        val isAlt = event.isAltPressed
        val isMeta = event.isMetaPressed

        val isShiftKey = event.keyCode == KeyEvent.KEYCODE_SHIFT_LEFT ||
                event.keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
        if (isShiftKey) {
            if (!isCtrl && !isAlt && !isMeta) {
                when (event.action) {
                    KeyEvent.ACTION_DOWN -> {
                        if (event.repeatCount == 0) {
                            shiftDownPendingTap = true
                            shiftDownTimeMs = event.eventTime
                        } else {
                            cancelShiftTap()
                        }
                    }

                    KeyEvent.ACTION_UP -> {
                        val isQuickTap = shiftDownPendingTap &&
                                (event.eventTime - shiftDownTimeMs) <= 300L
                        shiftDownPendingTap = false
                        if (isQuickTap) {
                            val now = event.eventTime
                            if (lastCleanShiftTapMs != 0L && (now - lastCleanShiftTapMs) <= 400L) {
                                lastCleanShiftTapMs = 0L
                                if (!viewModel.isStandaloneMode.value &&
                                    uiState.appState == AppState.REGULAR &&
                                    !uiState.isBottomPanelVisible
                                ) {
                                    executeShortcut(BrowserShortcut.SHOW_BOTTOM_PANEL, viewModel)
                                }
                            } else {
                                lastCleanShiftTapMs = now
                            }
                        } else {
                            lastCleanShiftTapMs = 0L
                        }
                    }
                }
            } else {
                cancelShiftTap()
            }
            return false
        }

        // any non-shift key cancels an in-progress shift tap or double-shift sequence
        cancelShiftTap()

        val shortcut = when {
            // esc only applies while url bar or icon field is focused
            event.keyCode == KeyEvent.KEYCODE_ESCAPE &&
                    !isCtrl && !isShift && !isAlt && !isMeta &&
                    (uiState.isFocusOnUrlTextField || uiState.isFocusOnIconUrlTextField) -> {
                BrowserShortcut.DISMISS_URL_BAR
            }

            // ctrl + shift + c works anywhere in the browser
            event.keyCode == KeyEvent.KEYCODE_C &&
                    isCtrl && isShift && !isAlt && !isMeta -> {
                BrowserShortcut.COPY_URL
            }

            // ctrl + l works anywhere in regular mode
            event.keyCode == KeyEvent.KEYCODE_L &&
                    isCtrl && !isShift && !isAlt && !isMeta &&
                    !viewModel.isStandaloneMode.value &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.FOCUS_OR_EDIT_URL
            }

            // ctrl + r refreshes active tab
            event.keyCode == KeyEvent.KEYCODE_R &&
                    isCtrl && !isShift && !isAlt && !isMeta &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.REFRESH_TAB
            }

            // ctrl + shift + t reopens last closed tab in regular mode
            event.keyCode == KeyEvent.KEYCODE_T &&
                    isCtrl && isShift && !isAlt && !isMeta &&
                    !viewModel.isStandaloneMode.value &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.REOPEN_CLOSED_TAB
            }

            // ctrl + t focuses url bar to open a new tab on submit
            event.keyCode == KeyEvent.KEYCODE_T &&
                    isCtrl && !isShift && !isAlt && !isMeta &&
                    !viewModel.isStandaloneMode.value &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.NEW_TAB
            }

            // ctrl + w closes active tab in regular mode
            event.keyCode == KeyEvent.KEYCODE_W &&
                    isCtrl && !isShift && !isAlt && !isMeta &&
                    !viewModel.isStandaloneMode.value &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.CLOSE_TAB
            }

            // alt + h goes back on active tab without changing focus
            event.keyCode == KeyEvent.KEYCODE_H &&
                    !isCtrl && !isShift && isAlt && !isMeta &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.GO_BACK
            }

            // alt + l goes forward on active tab without changing focus
            event.keyCode == KeyEvent.KEYCODE_L &&
                    !isCtrl && !isShift && isAlt && !isMeta &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.GO_FORWARD
            }

            // alt + j switches to next tab on same profile without changing focus
            event.keyCode == KeyEvent.KEYCODE_J &&
                    !isCtrl && !isShift && isAlt && !isMeta &&
                    !viewModel.isStandaloneMode.value &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.NEXT_TAB
            }

            // alt + k switches to previous tab on same profile without changing focus
            event.keyCode == KeyEvent.KEYCODE_K &&
                    !isCtrl && !isShift && isAlt && !isMeta &&
                    !viewModel.isStandaloneMode.value &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.PREVIOUS_TAB
            }

            // ctrl + alt + j switches to next profile without changing focus
            event.keyCode == KeyEvent.KEYCODE_J &&
                    isCtrl && !isShift && isAlt && !isMeta &&
                    !viewModel.isStandaloneMode.value &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.NEXT_PROFILE
            }

            // ctrl + alt + k switches to previous profile without changing focus
            event.keyCode == KeyEvent.KEYCODE_K &&
                    isCtrl && !isShift && isAlt && !isMeta &&
                    !viewModel.isStandaloneMode.value &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.PREVIOUS_PROFILE
            }

            // ctrl + j opens download panel and defocuses url bar if focused
            event.keyCode == KeyEvent.KEYCODE_J &&
                    isCtrl && !isShift && !isAlt && !isMeta &&
                    !viewModel.isStandaloneMode.value &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.OPEN_DOWNLOADS
            }

            // ctrl + f opens find in page panel and focuses its text field
            event.keyCode == KeyEvent.KEYCODE_F &&
                    isCtrl && !isShift && !isAlt && !isMeta &&
                    !viewModel.isStandaloneMode.value &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.OPEN_FIND_IN_PAGE
            }

            // ctrl + enter opens url bar input in a new tab
            (event.keyCode == KeyEvent.KEYCODE_ENTER || event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) &&
                    isCtrl && !isShift && !isAlt && !isMeta &&
                    !viewModel.isStandaloneMode.value &&
                    uiState.appState == AppState.REGULAR &&
                    uiState.isFocusOnUrlTextField -> {
                BrowserShortcut.OPEN_URL_IN_NEW_TAB
            }

            else -> null
        } ?: return false

        // trigger action once on key down, consume repeats and key up
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            executeShortcut(shortcut, viewModel)
        }
        return true
    }

    private fun executeShortcut(shortcut: BrowserShortcut, viewModel: BrowserViewModel) {
        val uiState = viewModel.uiState.value
        when (shortcut) {
            BrowserShortcut.COPY_URL -> {
                val activeUrl = viewModel.activeTab?.currentURL ?: return
                val cleanUrl = LocalAppServer.formatForDisplay(activeUrl)
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.setPrimaryClip(ClipData.newPlainText("url", cleanUrl))
            }

            BrowserShortcut.DISMISS_URL_BAR -> {
                _pendingUrlFocusAction.value = null
                _isPendingNewTab.value = false
                _shortcutEvents.tryEmit(BrowserShortcut.DISMISS_URL_BAR)
            }

            BrowserShortcut.FOCUS_OR_EDIT_URL -> {
                _isPendingNewTab.value = false
                if (uiState.isFindInPageVisible) {
                    viewModel.updateUI { it.copy(isFindInPageVisible = false) }
                }
                if (uiState.isFocusOnUrlTextField) {
                    _shortcutEvents.tryEmit(BrowserShortcut.FOCUS_OR_EDIT_URL)
                } else if (!uiState.isPinningApp && !uiState.isCreatingProfile && !uiState.isRenamingProfile) {
                    _pendingUrlFocusAction.value = UrlFocusAction.SELECT_ALL
                    viewModel.updateUI {
                        it.copy(
                            isFindInPageVisible = false,
                            isUrlBarVisible = true,
                            isBottomPanelVisible = true,
                            isUrlOverlayBoxVisible = false
                        )
                    }
                }
            }

            BrowserShortcut.REFRESH_TAB -> {
                viewModel.activeTab?.let { viewModel.geckoManager.getSession(it).reload() }
                if (uiState.isFocusOnUrlTextField || uiState.isFocusOnIconUrlTextField) {
                    _pendingUrlFocusAction.value = null
                    _isPendingNewTab.value = false
                    _shortcutEvents.tryEmit(BrowserShortcut.DISMISS_URL_BAR)
                }
            }

            BrowserShortcut.NEW_TAB -> {
                _isPendingNewTab.value = true
                if (uiState.isFocusOnUrlTextField) {
                    if (uiState.isPinningApp || uiState.isCreatingProfile || uiState.isRenamingProfile || uiState.isFindInPageVisible) {
                        viewModel.updateUI {
                            it.copy(
                                isPinningApp = false,
                                isCreatingProfile = false,
                                isRenamingProfile = false,
                                isFindInPageVisible = false
                            )
                        }
                    }
                    _shortcutEvents.tryEmit(BrowserShortcut.NEW_TAB)
                } else {
                    _pendingUrlFocusAction.value = UrlFocusAction.EMPTY
                    viewModel.updateUI {
                        it.copy(
                            isPinningApp = false,
                            isCreatingProfile = false,
                            isRenamingProfile = false,
                            isFindInPageVisible = false,
                            isUrlBarVisible = true,
                            isBottomPanelVisible = true,
                            isUrlOverlayBoxVisible = false
                        )
                    }
                }
            }

            BrowserShortcut.OPEN_URL_IN_NEW_TAB -> {
                _shortcutEvents.tryEmit(BrowserShortcut.OPEN_URL_IN_NEW_TAB)
            }

            BrowserShortcut.CLOSE_TAB -> {
                _pendingUrlFocusAction.value = null
                _isPendingNewTab.value = false
                if (uiState.isFocusOnUrlTextField || uiState.isFocusOnIconUrlTextField) {
                    _shortcutEvents.tryEmit(BrowserShortcut.DISMISS_URL_BAR)
                }
                _shortcutEvents.tryEmit(BrowserShortcut.CLOSE_TAB)
            }

            BrowserShortcut.REOPEN_CLOSED_TAB -> {
                if (viewModel.recentlyClosedTabs.isNotEmpty()) {
                    _pendingUrlFocusAction.value = null
                    _isPendingNewTab.value = false
                    viewModel.reopenClosedTab()
                    if (uiState.isFocusOnUrlTextField || uiState.isFocusOnIconUrlTextField) {
                        _shortcutEvents.tryEmit(BrowserShortcut.DISMISS_URL_BAR)
                    }
                }
            }

            BrowserShortcut.GO_BACK -> {
                viewModel.activeTab?.let { tab ->
                    if (tab.canGoBack) {
                        viewModel.geckoManager.getSession(tab).goBack(true)
                    }
                }
            }

            BrowserShortcut.GO_FORWARD -> {
                viewModel.activeTab?.let { tab ->
                    if (tab.canGoForward) {
                        viewModel.geckoManager.getSession(tab).goForward(true)
                    }
                }
            }

            BrowserShortcut.NEXT_TAB -> {
                val count = viewModel.tabs.size
                if (count > 1) {
                    val nextIndex = (viewModel.activeTabIndex.value + 1) % count
                    viewModel.selectTab(nextIndex)
                }
            }

            BrowserShortcut.PREVIOUS_TAB -> {
                val count = viewModel.tabs.size
                if (count > 1) {
                    val prevIndex = (viewModel.activeTabIndex.value - 1 + count) % count
                    viewModel.selectTab(prevIndex)
                }
            }

            BrowserShortcut.NEXT_PROFILE -> {
                val count = viewModel.profiles.size
                if (count > 1) {
                    val currentIndex = viewModel.profiles
                        .indexOfFirst { it.id == viewModel.activeProfileId.value }
                        .coerceAtLeast(0)
                    val nextIndex = (currentIndex + 1) % count
                    viewModel.switchProfile(viewModel.profiles[nextIndex].id)
                }
            }

            BrowserShortcut.PREVIOUS_PROFILE -> {
                val count = viewModel.profiles.size
                if (count > 1) {
                    val currentIndex = viewModel.profiles
                        .indexOfFirst { it.id == viewModel.activeProfileId.value }
                        .coerceAtLeast(0)
                    val prevIndex = (currentIndex - 1 + count) % count
                    viewModel.switchProfile(viewModel.profiles[prevIndex].id)
                }
            }

            BrowserShortcut.OPEN_DOWNLOADS -> {
                val wasTextFocused = uiState.isFocusOnUrlTextField || uiState.isFocusOnIconUrlTextField
                _pendingUrlFocusAction.value = null
                _isPendingNewTab.value = false
                viewModel.updateUI {
                    it.copy(
                        isPinningApp = false,
                        isCreatingProfile = false,
                        isRenamingProfile = false,
                        isFocusOnUrlTextField = false,
                        isFocusOnIconUrlTextField = false,
                        isUrlBarVisible = true,
                        isBottomPanelVisible = true,
                        isUrlOverlayBoxVisible = true,
                        isDownloadPanelVisible = true,
                        isOptionsPanelVisible = false,
                        isAppsPanelVisible = false,
                        isSettingsPanelVisible = false,
                        isSyncPanelVisible = false,
                        isFindInPageVisible = false,
                        isTabDataPanelVisible = false,
                        isNavPanelVisible = false,
                        savedPanelState = null
                    )
                }
                if (wasTextFocused) {
                    _shortcutEvents.tryEmit(BrowserShortcut.DISMISS_URL_BAR)
                }
            }

            BrowserShortcut.OPEN_FIND_IN_PAGE -> {
                _isPendingNewTab.value = false
                if (!uiState.isFocusOnUrlTextField) {
                    _pendingUrlFocusAction.value = UrlFocusAction.EMPTY
                }
                viewModel.updateUI {
                    it.copy(
                        isPinningApp = false,
                        isCreatingProfile = false,
                        isRenamingProfile = false,
                        isFocusOnIconUrlTextField = false,
                        isUrlBarVisible = true,
                        isBottomPanelVisible = true,
                        isUrlOverlayBoxVisible = false,
                        isFindInPageVisible = true,
                        isOptionsPanelVisible = false,
                        isAppsPanelVisible = false,
                        isSettingsPanelVisible = false,
                        isSyncPanelVisible = false,
                        isDownloadPanelVisible = false,
                        isTabDataPanelVisible = false,
                        isNavPanelVisible = false,
                        savedPanelState = null
                    )
                }
                if (uiState.isFocusOnUrlTextField) {
                    _shortcutEvents.tryEmit(BrowserShortcut.OPEN_FIND_IN_PAGE)
                }
            }

            BrowserShortcut.SHOW_BOTTOM_PANEL -> {
                viewModel.updateUI {
                    it.copy(
                        isUrlBarVisible = true,
                        isBottomPanelVisible = true
                    )
                }
            }
        }
    }
}