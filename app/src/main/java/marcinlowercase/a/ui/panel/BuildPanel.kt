package marcinlowercase.a.ui.panel

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.ui.draw.drawWithContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import marcinlowercase.a.R
import marcinlowercase.a.core.enum_class.AppState
import marcinlowercase.a.core.function.AudioRecorderHelper
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import org.mozilla.geckoview.GeckoSession
import kotlin.math.ceil

@Composable
fun BuildPanel(
    modifier: Modifier = Modifier,
    floatingPanelBottomPadding: Dp,
    confirmationPopup: (message: Int, url: String, onConfirm: () -> Unit, onCancel: () -> Unit) -> Unit
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    val uiState = viewModel.uiState.collectAsState()
    val textState = rememberTextFieldState("")
    val layer2Height = settings.value.heightForLayer(2).dp
    val verticalCenterPad = maxOf(settings.value.padding.dp, (layer2Height - 24.dp) / 2)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioRecorder = remember { AudioRecorderHelper(context) }
    var isRecording by remember { mutableStateOf(false) }
    var isTranscribing by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val isImeVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp
    var isTextFieldFocused by remember { mutableStateOf(false) }
    var wasKeyboardOpenBeforePreview by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    // Automatically switch GeckoView to the OutSync preview template upon entering Build Mode
    LaunchedEffect(uiState.value.appState) {
        if (uiState.value.appState == AppState.BUILD) {
            viewModel.activeTab?.let { tab ->
                val session = viewModel.geckoManager.getSession(tab)
                session.load(
                    GeckoSession.Loader().uri("resource://android/assets/preview/template.html")
                )
            }
        }
    }

    val textFieldScrollState = rememberScrollState()

    DisposableEffect(Unit) {
        onDispose {
            audioRecorder.stopRecording()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isRecording = audioRecorder.startRecording()
        }
    }


    val speed = settings.value.animationSpeedForLayer(0)

// Detects if the sheet was already open on screen
    var wasPanelVisible by remember { mutableStateOf(uiState.value.isBottomPanelVisible) }
    val isModeSwitch = uiState.value.isBottomPanelVisible && wasPanelVisible
    SideEffect {
        wasPanelVisible = uiState.value.isBottomPanelVisible
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = uiState.value.appState == AppState.BUILD && uiState.value.isBottomPanelVisible,
        enter = if (isModeSwitch) {
            // Entering Build Mode: slide in from the RIGHT
            slideInHorizontally(tween(speed)) { it }
        } else {
            // Normal reveal from BackSquare: slide up vertically
            slideInVertically(tween(speed)) { it }
        },
        exit = if (uiState.value.isBottomPanelVisible) {
            // Exiting Build Mode: slide out to the RIGHT
            slideOutHorizontally(tween(speed)) { it }
        } else {
            // Normal dismiss on webview touch: slide down vertically
            slideOutVertically(tween(speed)) { it }
        }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(settings.value.padding.dp)
                    .windowInsetsPadding(WindowInsets.ime)
                    .padding(bottom = floatingPanelBottomPadding)
                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(settings.value.padding.dp * 2),
                verticalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
            ) {

                DescriptionPanel()
                ConfirmationPanel()

                // ROW 1: Multiline Text Input
                TextField(
                    state = textState,
                    scrollState = textFieldScrollState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { isTextFieldFocused = it.isFocused }
                        .drawWithContent {
                            drawContent()
                            if (textFieldScrollState.maxValue > 0) {
                                val scrollbarWidth = 4.dp.toPx()
                                val rightMargin = 8.dp.toPx()
                                val verticalMargin = verticalCenterPad.toPx()

                                val trackHeight = size.height - (verticalMargin * 2)
                                val totalHeight = trackHeight + textFieldScrollState.maxValue
                                val thumbHeight = (trackHeight / totalHeight * trackHeight).coerceAtLeast(20.dp.toPx())
                                val scrollProgress = textFieldScrollState.value.toFloat() / textFieldScrollState.maxValue
                                val thumbOffset = verticalMargin + scrollProgress * (trackHeight - thumbHeight)

                                drawRoundRect(
                                    color = Color.Gray.copy(alpha = 0.5f), // or MaterialTheme.colorScheme.surfaceContainer
                                    topLeft = Offset(size.width - scrollbarWidth - rightMargin, thumbOffset),
                                    size = Size(scrollbarWidth, thumbHeight),
                                    cornerRadius = CornerRadius(scrollbarWidth / 2, scrollbarWidth / 2)
                                )
                            }
                        },
                    lineLimits = TextFieldLineLimits.MultiLine(
                        minHeightInLines = 1,
                        maxHeightInLines = 7
//                        maxHeightInLines = ceil(settings.value.maxListHeight).toInt()
//                            .coerceAtLeast(1)
                    ),
                    placeholder = {
                        Text(
                            text = "Describe your app...",
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            textAlign = TextAlign.Start
                        )
                    },
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(textAlign = TextAlign.Start),
                    shape = RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp),
                    contentPadding = PaddingValues(
                        horizontal = settings.value.cornerRadiusForLayer(1).dp + settings.value.padding.dp,
                        vertical = verticalCenterPad
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedTextColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // ROW 2: Action Buttons (Exit, Mic / Spinner, Send)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
                ) {

                    // Exit Build Mode Button
                    CustomIconButton(
                        layer = 3,
                        onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            confirmationPopup(
                                R.string.confirm_exit_build_mode,
                                "",
                                {
                                    viewModel.updateUI {
                                        it.copy(
                                            appState = AppState.REGULAR,
                                            isBuildPreview = false
                                        )
                                    }
                                },
                                {}
                            )
                        },
                        buttonDescription = stringResource(R.string.desc_exit_build_mode),
                        painterId = R.drawable.ic_close,
                        modifier = Modifier.weight(1f),
                        isWhite = !isColorDark((if (settings.value.isMaterialYou()) MaterialTheme.colorScheme.error else Color.Red).toArgb()),
                        otherColor = if (settings.value.isMaterialYou()) MaterialTheme.colorScheme.error else Color.Red
                    )

                    CustomIconButton(
                        layer = 3,
                        onTap = {
                            if (uiState.value.isBuildPreview) {
                                // Turning from Preview BACK to Chat:
                                if (wasKeyboardOpenBeforePreview) {
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                }
                            } else {
                                // Turning from Chat TO Preview:
                                // Save state: true only if focused AND keyboard is up
                                wasKeyboardOpenBeforePreview = isTextFieldFocused && isImeVisible

                                // Always hide keyboard when entering preview
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }

                            viewModel.updateUI { it.copy(isBuildPreview = !it.isBuildPreview) }
                        },
                        buttonDescription = if (uiState.value.isBuildPreview) stringResource(R.string.word_chat) else stringResource(
                            R.string.word_preview
                        ),
                        painterId = if (uiState.value.isBuildPreview) R.drawable.ic_forum else R.drawable.ic_deployed_code,
                        isWhite = true,
                        modifier = Modifier.weight(1f)
                    )

//                    // Center: Loading Spinner or Mic Button
//                    if (isTranscribing) {
//                        Box(
//                            modifier = Modifier
//                                .weight(2f)
//                                .height(settings.value.heightForLayer(2).dp)
//                                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
//                                .background(MaterialTheme.colorScheme.surfaceContainer),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            CircularProgressIndicator(
//                                modifier = Modifier.size(20.dp),
//                                color = MaterialTheme.colorScheme.onSurface,
//                                strokeWidth = 2.dp
//                            )
//                        }
//                    } else {
//                        CustomIconButton(
//                            layer = 2,
//                            isSquare = false,
//                            onTap = {
//                                val hasPermission = ContextCompat.checkSelfPermission(
//                                    context, Manifest.permission.RECORD_AUDIO
//                                ) == PackageManager.PERMISSION_GRANTED
//
//                                if (!hasPermission) {
//                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
//                                } else {
//                                    if (isRecording) {
//                                        val recordedFile = audioRecorder.stopRecording()
//                                        isRecording = false
//
//                                        if (recordedFile != null && recordedFile.exists()) {
//                                            isTranscribing = true
//                                            coroutineScope.launch {
//                                                val result = viewModel.geminiManager.sendVoiceInit(recordedFile)
//                                                isTranscribing = false
//
//                                                result.onSuccess { responseJson ->
//                                                    // 1. Put clean transcript in text box
//                                                    val transcript = responseJson.optString("transcription")
//                                                    if (transcript.isNotBlank()) {
//                                                        textState.setTextAndPlaceCursorAtEnd(transcript)
//                                                    }
//
//                                                    // 2. Stream blueprint into GeckoView template
//                                                    val blueprintObj = responseJson.optJSONObject("blueprint")
//                                                    if (blueprintObj != null) {
//                                                        viewModel.activeTab?.let { tab ->
//                                                            val session = viewModel.geckoManager.getSession(tab)
//                                                            val jsCall = "javascript:void(window.renderPreview($blueprintObj));"
//                                                            session.load(GeckoSession.Loader().uri(jsCall))
//                                                        }
//                                                    }
//
//                                                    viewModel.showCustomNotification("App drafted! Check preview above.")
//                                                }.onFailure { error ->
//                                                    viewModel.showCustomNotification("Voice build failed: ${error.message}")
//                                                }
//                                            }
//                                        }
//                                    } else {
//                                        isRecording = audioRecorder.startRecording()
//                                    }
//                                }
//                            },
//                            buttonDescription = if (isRecording) "Stop Recording" else "Voice Input",
//                            painterId = R.drawable.ic_mic,
//                            isWhite = true,
//                            modifier = Modifier.weight(2f),
//                            otherColor = if (isRecording) MaterialTheme.colorScheme.error else Color.Transparent
//                        )
//                    }

                    // Send Button / Spinner
                    if (viewModel.isChatThinking.value) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(settings.value.heightForLayer(3).dp)
                                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                strokeWidth = 2.dp
                            )
                        }
                    } else {
                        CustomIconButton(
                            layer = 3,
                            onTap = {
                                val query = textState.text.toString().trim()
                                if (query.isNotBlank()) {
                                    viewModel.sendBuildChatMessage(query)
                                    textState.clearText()
                                }
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            },
                            buttonDescription = stringResource(R.string.word_send),
                            painterId = R.drawable.ic_send,
                            isWhite = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}