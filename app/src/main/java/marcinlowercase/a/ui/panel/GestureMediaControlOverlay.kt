package marcinlowercase.a.ui.panel

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import marcinlowercase.a.core.enum_class.MediaControlMode
import marcinlowercase.a.core.enum_class.MediaControlOption
import marcinlowercase.a.core.manager.MediaGestureManager
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun GestureMediaControlOverlay(
    modifier: Modifier = Modifier,
    hapticFeedback: HapticFeedback,
    gestureManager: MediaGestureManager,
    controlOption: MutableState<MediaControlOption>,
    pendingSeekSeconds: MutableState<Double>,
    interactionTrigger: MutableState<Int>,
    statusAction: MutableState<VideoStatusAction?> = remember { mutableStateOf(null) },
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    if (!uiState.value.isOnFullscreenVideo || settings.value.currentMediaControlMode() != MediaControlMode.GESTURE) return

    var horizontalDragAccumulator by remember { mutableFloatStateOf(0f) }
    var verticalDragAccumulator by remember { mutableFloatStateOf(0f) }
    var isVerticalLeftSide by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // toggle VideoStatusPanel visibility on single tap
                        viewModel.updateUI {
                            it.copy(isMediaControlPanelVisible = !it.isMediaControlPanelVisible)
                        }
                    },
                    onDoubleTap = { offset ->
                        val width = size.width.toFloat()
                        val leftZoneEnd = width * 0.35f
                        val rightZoneStart = width * 0.65f

                        when {
                            offset.x < leftZoneEnd -> {
                                val stepSec = settings.value.mediaSeekStep.roundToInt().coerceIn(1, 60)
                                controlOption.value = MediaControlOption.TIME
                                viewModel.geckoManager.mediaSeekStepSeconds = stepSec.toDouble()
                                viewModel.geckoManager.sendVideoCommand("prev_5")
                                statusAction.value = VideoStatusAction.SeekStep(-stepSec)
                                interactionTrigger.value++
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            offset.x > rightZoneStart -> {
                                val stepSec = settings.value.mediaSeekStep.roundToInt().coerceIn(1, 60)
                                controlOption.value = MediaControlOption.TIME
                                viewModel.geckoManager.mediaSeekStepSeconds = stepSec.toDouble()
                                viewModel.geckoManager.sendVideoCommand("next_5")
                                statusAction.value = VideoStatusAction.SeekStep(stepSec)
                                interactionTrigger.value++
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            else -> {
                                // center 30% toggles play or pause
                                if (viewModel.geckoManager.isActiveMediaSessionPaused) {
                                    viewModel.geckoManager.sendVideoCommand("play")
                                    statusAction.value = VideoStatusAction.Play()
                                } else {
                                    viewModel.geckoManager.sendVideoCommand("pause")
                                    statusAction.value = VideoStatusAction.Pause()
                                }
                                interactionTrigger.value++
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        statusAction.value = null
                        controlOption.value = MediaControlOption.TIME
                        horizontalDragAccumulator = 0f
                    },
                    onDragEnd = {
                        if (pendingSeekSeconds.value != 0.0) {
                            viewModel.geckoManager.sendVideoCommand("seek_relative", pendingSeekSeconds.value)
                            pendingSeekSeconds.value = 0.0
                        }
                        horizontalDragAccumulator = 0f
                    },
                    onDragCancel = {
                        pendingSeekSeconds.value = 0.0
                        horizontalDragAccumulator = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        horizontalDragAccumulator += dragAmount
                        val sensitivity = 7f
                        if (abs(horizontalDragAccumulator) > sensitivity) {
                            val secondsToAdd = (horizontalDragAccumulator / sensitivity).toInt()
                            if (secondsToAdd != 0) {
                                pendingSeekSeconds.value += secondsToAdd
                                horizontalDragAccumulator -= (secondsToAdd * sensitivity)
                                interactionTrigger.value++
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        statusAction.value = null
                        isVerticalLeftSide = offset.x < size.width * 0.5f
                        controlOption.value = if (isVerticalLeftSide) {
                            MediaControlOption.VOLUME
                        } else {
                            MediaControlOption.BRIGHTNESS
                        }
                        verticalDragAccumulator = 0f
                    },
                    onDragEnd = { verticalDragAccumulator = 0f },
                    onDragCancel = { verticalDragAccumulator = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        verticalDragAccumulator += -dragAmount
                        val sensitivity = if (isVerticalLeftSide) 40f else 15f
                        if (abs(verticalDragAccumulator) > sensitivity) {
                            val steps = (verticalDragAccumulator / sensitivity).toInt()
                            if (steps != 0) {
                                if (isVerticalLeftSide) {
                                    gestureManager.setVolume(steps)
                                } else {
                                    gestureManager.setBrightness(steps * 2)
                                }
                                interactionTrigger.value++
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                verticalDragAccumulator -= (steps * sensitivity)
                            }
                        }
                    }
                )
            }
    )
}