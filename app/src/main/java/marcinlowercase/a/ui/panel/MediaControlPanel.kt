package marcinlowercase.a.ui.panel

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import marcinlowercase.a.R
import marcinlowercase.a.core.enum_class.MediaControlMode
import marcinlowercase.a.core.enum_class.MediaControlOption
import marcinlowercase.a.core.function.buttonPointerInput
import marcinlowercase.a.core.manager.MediaGestureManager
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import kotlin.math.abs
import androidx.compose.runtime.mutableStateOf
import kotlin.math.roundToInt

@Composable
fun MediaControlPanel(
    hapticFeedback: HapticFeedback,
    modifier: Modifier,
    onExitFullscreen: () -> Unit,
    gestureManager: MediaGestureManager,
    controlOption: MutableState<MediaControlOption>,
    pendingSeekSeconds: MutableState<Double>,
    interactionTrigger: MutableState<Int>,
    statusAction: MutableState<VideoStatusAction?> = remember { mutableStateOf(null) },
    isControlsOnRight: Boolean = false,
    webViewPadding: PaddingValues = PaddingValues(0.dp),
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()
    val layoutDirection = LocalLayoutDirection.current

    val opacity by animateFloatAsState(
        targetValue = if (uiState.value.isMediaControlPanelVisible) 0.7f else 0.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "media control panel opacity"
    )

    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(uiState.value.isMediaControlPanelVisible, viewModel.geckoManager.isActiveMediaSessionPaused) {
        if (!viewModel.geckoManager.isActiveMediaSessionPaused) {
            while (true) {
                viewModel.geckoManager.tickLivePosition()
                delay(500)
            }
        }
    }

    if (settings.value.currentMediaControlMode() == MediaControlMode.ONE_HAND && uiState.value.isOnFullscreenVideo && uiState.value.isMediaControlPanelDisplayed) {
        val pad = settings.value.padding.dp
        val topPad = webViewPadding.calculateTopPadding()
        val bottomPad = webViewPadding.calculateBottomPadding()
        val startEdgePad = webViewPadding.calculateStartPadding(layoutDirection)
        val endEdgePad = webViewPadding.calculateEndPadding(layoutDirection)

        val onTopTap: () -> Unit = {
            if (uiState.value.isMediaControlPanelVisible) {
                onExitFullscreen()
            } else {
                viewModel.updateUI { it.copy(isMediaControlPanelVisible = true) }
            }
        }
        val onTopLongPress: () -> Boolean = {
            viewModel.updateUI { it.copy(isMediaControlPanelDisplayed = false) }
            true
        }
        val onBottomTap: () -> Unit = {
            if (uiState.value.isMediaControlPanelVisible) {
                val nextIndex = (controlOption.value.ordinal + 1) % MediaControlOption.entries.size
                controlOption.value = MediaControlOption.entries[nextIndex]
                interactionTrigger.value++
            } else {
                viewModel.updateUI { it.copy(isMediaControlPanelVisible = true) }
            }
        }

        Box(
            modifier = modifier.padding(top = topPad, bottom = bottomPad)
        ) {
            // visual panel keeps exact cutout and edge padding
            Column(
                modifier = Modifier
                    .padding(
                        start = if (!isControlsOnRight) startEdgePad + pad else pad,
                        end = if (isControlsOnRight) endEdgePad + pad else pad,
                        top = pad,
                        bottom = pad
                    )
                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                    .alpha(opacity)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .width(settings.value.heightForLayer(1).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomIconButton(
                    isLandscape = true,
                    layer = 2,
                    modifier = Modifier
                        .weight(1f)
                        .padding(pad),
                    onTap = onTopTap,
                    onLongPress = onTopLongPress,
                    buttonDescription = "plus/minus",
                    painterId = R.drawable.ic_fullscreen_exit,
                    isWhite = true
                )

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .padding(horizontal = pad)
                        .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
                        .fillMaxHeight()
                        .width(settings.value.heightForLayer(2).dp)
                        .background(Color(settings.value.highlightColor))
                )

                CustomIconButton(
                    isLandscape = true,
                    layer = 2,
                    modifier = Modifier
                        .weight(1f)
                        .padding(pad),
                    onTap = onBottomTap,
                    buttonDescription = "control option",
                    painterId = controlOption.value.iconRes,
                    isWhite = true
                )
            }

            // expanded touch surface reaches the active screen edge
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = pad, bottom = pad)
                    .clickable(
                        enabled = false,
                        onClick = {}
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .buttonPointerInput(
                            onTap = onTopTap,
                            onLongPress = onTopLongPress,
                            hapticFeedback = hapticFeedback,
                            descriptionContent = viewModel.descriptionContent,
                            buttonDescription = "plus/minus",
                            useLongPress = true
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (controlOption.value == MediaControlOption.TIME && pendingSeekSeconds.value != 0.0) {
                                        viewModel.geckoManager.sendVideoCommand("seek_relative", pendingSeekSeconds.value)
                                        pendingSeekSeconds.value = 0.0
                                    }
                                    dragAccumulator = 0f
                                },
                                onDragCancel = {
                                    pendingSeekSeconds.value = 0.0
                                    dragAccumulator = 0f
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    val effectiveDrag = if (controlOption.value == MediaControlOption.TIME) {
                                        dragAmount
                                    } else {
                                        -dragAmount
                                    }

                                    dragAccumulator += effectiveDrag

                                    if (controlOption.value == MediaControlOption.TIME) {
                                        val sensitivity = 7f
                                        if (abs(dragAccumulator) > sensitivity) {
                                            val secondsToAdd = (dragAccumulator / sensitivity).toInt()
                                            if (secondsToAdd != 0) {
                                                pendingSeekSeconds.value += secondsToAdd
                                                dragAccumulator -= (secondsToAdd * sensitivity)
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        }
                                    } else {
                                        val sensitivity = if (controlOption.value == MediaControlOption.VOLUME) 40f else 15f
                                        if (abs(dragAccumulator) > sensitivity) {
                                            val steps = (dragAccumulator / sensitivity).toInt()
                                            if (steps != 0) {
                                                statusAction.value = null
                                                if (controlOption.value == MediaControlOption.VOLUME) {
                                                    gestureManager.setVolume(steps)
                                                } else {
                                                    gestureManager.setBrightness(steps * 2)
                                                }
                                                interactionTrigger.value++
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                dragAccumulator -= (steps * sensitivity)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    if (uiState.value.isMediaControlPanelVisible) {
                                        if (viewModel.geckoManager.isActiveMediaSessionPaused) {
                                            viewModel.geckoManager.sendVideoCommand("play")
                                            statusAction.value = VideoStatusAction.Play()
                                        } else {
                                            viewModel.geckoManager.sendVideoCommand("pause")
                                            statusAction.value = VideoStatusAction.Pause()
                                        }
                                        interactionTrigger.value++
                                    } else {
                                        viewModel.updateUI { it.copy(isMediaControlPanelVisible = true) }
                                    }
                                },
                                onDoubleTap = { offset ->
                                    val segmentHeight = size.height / 2
                                    val isTopHalf = offset.y < segmentHeight

                                    when (controlOption.value) {
                                        MediaControlOption.TIME -> {
                                            val stepSec = settings.value.mediaSeekStep.roundToInt().coerceIn(1, 60)
                                            viewModel.geckoManager.mediaSeekStepSeconds = stepSec.toDouble()
                                            if (isTopHalf) {
                                                viewModel.geckoManager.sendVideoCommand("prev_5")
                                                statusAction.value = VideoStatusAction.SeekStep(-stepSec)
                                            } else {
                                                viewModel.geckoManager.sendVideoCommand("next_5")
                                                statusAction.value = VideoStatusAction.SeekStep(stepSec)
                                            }
                                        }
                                        MediaControlOption.VOLUME -> {
                                            statusAction.value = null
                                            if (isTopHalf) {
                                                gestureManager.setVolume(1)
                                            } else {
                                                gestureManager.setVolume(-1)
                                            }
                                        }
                                        MediaControlOption.BRIGHTNESS -> {
                                            statusAction.value = null
                                            val stepAmount = 5
                                            if (isTopHalf) {
                                                gestureManager.setBrightness(stepAmount)
                                            } else {
                                                gestureManager.setBrightness(-stepAmount)
                                            }
                                        }
                                    }
                                    interactionTrigger.value++
                                },
                                onLongPress = { offset ->
                                    val segmentHeight = size.height / 2
                                    val isTopHalf = offset.y < segmentHeight
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    statusAction.value = null

                                    when (controlOption.value) {
                                        MediaControlOption.TIME -> {
                                            val target = if (viewModel.geckoManager.lastDuration.doubleValue > 0.0) {
                                                viewModel.geckoManager.lastDuration.doubleValue / 10.0
                                            } else {
                                                60.0
                                            }
                                            if (isTopHalf) {
                                                viewModel.geckoManager.sendVideoCommand("seek_relative", -target)
                                            } else {
                                                viewModel.geckoManager.sendVideoCommand("seek_relative", target)
                                            }
                                        }
                                        MediaControlOption.VOLUME -> {
                                            val maxSteps = 100
                                            if (isTopHalf) {
                                                gestureManager.setVolume(maxSteps)
                                            } else {
                                                gestureManager.setVolume(-maxSteps)
                                            }
                                        }
                                        MediaControlOption.BRIGHTNESS -> {
                                            val maxSteps = 100
                                            if (isTopHalf) {
                                                gestureManager.setBrightness(maxSteps)
                                            } else {
                                                gestureManager.setBrightness(-maxSteps)
                                            }
                                        }
                                    }
                                    interactionTrigger.value++
                                }
                            )
                        }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .buttonPointerInput(
                            onTap = onBottomTap,
                            hapticFeedback = hapticFeedback,
                            descriptionContent = viewModel.descriptionContent,
                            buttonDescription = "control option",
                            useLongPress = false
                        )
                )
            }
        }
    }
}