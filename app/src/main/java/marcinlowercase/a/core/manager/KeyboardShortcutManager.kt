package marcinlowercase.a.core.manager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.KeyEvent
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

    fun consumePendingUrlFocusAction(): UrlFocusAction? {
        val action = _pendingUrlFocusAction.value ?: return null
        _pendingUrlFocusAction.value = null
        return action
    }

    fun clearPendingUrlFocusAction() {
        _pendingUrlFocusAction.value = null
    }

    fun handleKeyEvent(event: KeyEvent, viewModel: BrowserViewModel): Boolean {
        val uiState = viewModel.uiState.value
        val isCtrl = event.isCtrlPressed
        val isShift = event.isShiftPressed
        val isAlt = event.isAltPressed
        val isMeta = event.isMetaPressed

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

            // ctrl + t creates a new tab in regular mode
            event.keyCode == KeyEvent.KEYCODE_T &&
                    isCtrl && !isShift && !isAlt && !isMeta &&
                    !viewModel.isStandaloneMode.value &&
                    uiState.appState == AppState.REGULAR -> {
                BrowserShortcut.NEW_TAB
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
                _shortcutEvents.tryEmit(BrowserShortcut.DISMISS_URL_BAR)
            }

            BrowserShortcut.FOCUS_OR_EDIT_URL -> {
                if (uiState.isFocusOnUrlTextField) {
                    _shortcutEvents.tryEmit(BrowserShortcut.FOCUS_OR_EDIT_URL)
                } else if (!uiState.isPinningApp && !uiState.isCreatingProfile && !uiState.isRenamingProfile) {
                    _pendingUrlFocusAction.value = UrlFocusAction.SELECT_ALL
                    viewModel.updateUI {
                        it.copy(
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
                    _shortcutEvents.tryEmit(BrowserShortcut.DISMISS_URL_BAR)
                }
            }

            BrowserShortcut.NEW_TAB -> {
                val newIndex = viewModel.activeTabIndex.value + 1
                viewModel.createNewTab(newIndex, "")

                if (uiState.isFocusOnUrlTextField) {
                    _shortcutEvents.tryEmit(BrowserShortcut.NEW_TAB)
                } else {
                    _pendingUrlFocusAction.value = UrlFocusAction.EMPTY
                    viewModel.updateUI {
                        it.copy(
                            isPinningApp = false,
                            isCreatingProfile = false,
                            isRenamingProfile = false,
                            isUrlBarVisible = true,
                            isBottomPanelVisible = true,
                            isUrlOverlayBoxVisible = false
                        )
                    }
                }
            }
        }
    }
}