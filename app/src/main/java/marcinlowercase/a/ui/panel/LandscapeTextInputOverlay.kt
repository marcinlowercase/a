package marcinlowercase.a.ui.panel

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import marcinlowercase.a.core.enum_class.BrowserShortcut
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel

@Composable
fun LandscapeTextInputOverlay(
    webViewPaddingValue: PaddingValues,
    onClearWebViewFocus: () -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    val session = viewModel.landscapeInputSession.value

    AnimatedVisibility(
        visible = session != null,
        enter = fadeIn(tween(settings.value.animationSpeedForLayer(1))),
        exit = fadeOut(tween(settings.value.animationSpeedForLayer(1)))
    ) {
        val activeSession = session ?: return@AnimatedVisibility
        val layoutDirection = LocalLayoutDirection.current
        val density = LocalDensity.current
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }

        val textFieldState = remember(activeSession.sessionId) {
            TextFieldState(
                initialText = activeSession.initialText,
                initialSelection = if (activeSession.selectAllOnOpen) {
                    TextRange(0, activeSession.initialText.length)
                } else {
                    TextRange(activeSession.initialText.length)
                }
            )
        }

        var hasFinished by remember(activeSession.sessionId) { mutableStateOf(false) }
        var hasImeBeenVisible by remember(activeSession.sessionId) { mutableStateOf(false) }
        val isImeVisible = WindowInsets.ime.getBottom(density) > 0

        val finishAndClose: (submit: Boolean, forceNewTab: Boolean) -> Unit = { submit, forceNewTab ->
            if (!hasFinished) {
                hasFinished = true
                if (forceNewTab) {
                    viewModel.keyboardShortcutManager.markPendingNewTab()
                }
                val typedText = textFieldState.text.toString()
                focusManager.clearFocus()
                keyboardController?.hide()
                viewModel.landscapeInputSession.value = null
                activeSession.onCommit(typedText, submit)
            }
        }

        val cancelAndClose: () -> Unit = {
            if (!hasFinished) {
                hasFinished = true
                focusManager.clearFocus()
                keyboardController?.hide()
                viewModel.landscapeInputSession.value = null
                activeSession.onCancel()
            }
        }

        BackHandler(enabled = !hasFinished) {
            finishAndClose(false, false)
        }

        LaunchedEffect(activeSession.sessionId) {
            withFrameNanos {}
            onClearWebViewFocus()
            focusRequester.requestFocus()
            keyboardController?.show()
        }

        LaunchedEffect(activeSession.sessionId, textFieldState) {
            snapshotFlow { textFieldState.text.toString() }.collect { current ->
                if (!hasFinished) {
                    activeSession.onTextChanged?.invoke(current)
                }
            }
        }

        LaunchedEffect(isImeVisible, activeSession.sessionId) {
            if (isImeVisible) {
                hasImeBeenVisible = true
            } else if (hasImeBeenVisible && !hasFinished) {
                // keyboard was hidden by user: paste typed text back into target field
                finishAndClose(false, false)
            }
        }

        LaunchedEffect(activeSession.sessionId) {
            viewModel.keyboardShortcutManager.shortcutEvents.collect { shortcut ->
                when (shortcut) {
                    BrowserShortcut.DISMISS_URL_BAR -> cancelAndClose()
                    BrowserShortcut.OPEN_URL_IN_NEW_TAB -> {
                        if (activeSession.isUrlBar) {
                            finishAndClose(true, true)
                        }
                    }
                    else -> {}
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
                .windowInsetsPadding(WindowInsets.ime)
                .padding(
                    start = webViewPaddingValue.calculateLeftPadding(layoutDirection),
                    top = webViewPaddingValue.calculateTopPadding(),
                    end = webViewPaddingValue.calculateRightPadding(layoutDirection),
                    bottom = 0.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            TextField(
                state = textFieldState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .focusRequester(focusRequester),
                lineLimits = TextFieldLineLimits.SingleLine,
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Start
                ),
                placeholder = {
                    if (activeSession.placeholder.isNotEmpty()) {
                        Text(
                            text = activeSession.placeholder,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            fontSize = 20.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = activeSession.keyboardType,
                    imeAction = activeSession.imeAction
                ),
                onKeyboardAction = {
                    finishAndClose(true, false)
                },
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
                    )
                )
            )
        }
    }
}