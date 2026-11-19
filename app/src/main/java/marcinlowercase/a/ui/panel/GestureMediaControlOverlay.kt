package marcinlowercase.a.ui.panel

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.core.enum_class.MediaControlMode
import marcinlowercase.a.core.enum_class.MediaControlOption
import marcinlowercase.a.core.manager.MediaGestureManager
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import kotlin.math.abs
import kotlin.math.hypot
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

    val coroutineScope = rememberCoroutineScope()
    var isHolding2x by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            if (isHolding2x) {
                isHolding2x = false
                viewModel.geckoManager.sendVideoCommand("stop_2x")
                if (statusAction.value is VideoStatusAction.Speed2x) {
                    statusAction.value = null
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var pendingSingleTapJob: Job? = null
                var lastTapTimeMs = 0L
                var lastTapPosition = Offset.Zero

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)

                    // leave bottom gesture pill area untouched for system home/pip swipe
                    if (down.position.y >= size.height - 24.dp.toPx()) {
                        return@awaitEachGesture
                    }

                    val now = System.currentTimeMillis()
                    val doubleTapSlopPx = 100.dp.toPx()
                    val isPotentialDoubleTap =
                        pendingSingleTapJob?.isActive == true &&
                                (now - lastTapTimeMs) <= viewConfiguration.doubleTapTimeoutMillis &&
                                hypot(
                                    (down.position.x - lastTapPosition.x).toDouble(),
                                    (down.position.y - lastTapPosition.y).toDouble()
                                ) <= doubleTapSlopPx

                    if (isPotentialDoubleTap) {
                        pendingSingleTapJob?.cancel()
                    }

                    var dragMode = 0 // 0 = none, 1 = horizontal seek, 2 = vertical vol/bright
                    var horizontalDragAccumulator = 0f
                    var verticalDragAccumulator = 0f
                    val isLeftSide = down.position.x < size.width * 0.5f

                    val holdJob = coroutineScope.launch {
                        delay(350)
                        isHolding2x = true
                        viewModel.geckoManager.sendVideoCommand("start_2x")
                        statusAction.value = VideoStatusAction.Speed2x()
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }

                    var endedByCancel = false

                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val activeChange = event.changes.firstOrNull { it.id == down.id }
                                ?: event.changes.firstOrNull()

                            if (activeChange == null || !activeChange.pressed) {
                                break
                            }

                            if (isHolding2x) {
                                activeChange.consume()
                                continue
                            }

                            val deltaFromStart = activeChange.position - down.position
                            if (dragMode == 0) {
                                val dist = hypot(deltaFromStart.x.toDouble(), deltaFromStart.y.toDouble())
                                if (dist > viewConfiguration.touchSlop) {
                                    holdJob.cancel()
                                    pendingSingleTapJob?.cancel()
                                    statusAction.value = null
                                    if (abs(deltaFromStart.x) > abs(deltaFromStart.y)) {
                                        dragMode = 1
                                        controlOption.value = MediaControlOption.TIME
                                        horizontalDragAccumulator = 0f
                                    } else {
                                        dragMode = 2
                                        controlOption.value = if (isLeftSide) {
                                            MediaControlOption.VOLUME
                                        } else {
                                            MediaControlOption.BRIGHTNESS
                                        }
                                        verticalDragAccumulator = 0f
                                    }
                                    activeChange.consume()
                                }
                            } else {
                                val delta = activeChange.positionChange()
                                activeChange.consume()

                                if (dragMode == 1) {
                                    horizontalDragAccumulator += delta.x
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
                                } else {
                                    verticalDragAccumulator += -delta.y
                                    val sensitivity = if (isLeftSide) 40f else 15f
                                    if (abs(verticalDragAccumulator) > sensitivity) {
                                        val steps = (verticalDragAccumulator / sensitivity).toInt()
                                        if (steps != 0) {
                                            if (isLeftSide) {
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
                            }
                        }
                    } catch (_: Exception) {
                        endedByCancel = true
                    } finally {
                        holdJob.cancel()
                        if (isHolding2x) {
                            isHolding2x = false
                            viewModel.geckoManager.sendVideoCommand("stop_2x")
                            if (statusAction.value is VideoStatusAction.Speed2x) {
                                statusAction.value = null
                            }
                        }
                    }

                    if (endedByCancel) {
                        pendingSeekSeconds.value = 0.0
                        return@awaitEachGesture
                    }

                    if (dragMode == 1) {
                        if (pendingSeekSeconds.value != 0.0) {
                            viewModel.geckoManager.sendVideoCommand("seek_relative", pendingSeekSeconds.value)
                            pendingSeekSeconds.value = 0.0
                        }
                    } else if (dragMode == 0 && !isHolding2x) {
                        if (isPotentialDoubleTap) {
                            lastTapTimeMs = 0L
                            val width = size.width.toFloat()
                            val leftZoneEnd = width * 0.35f
                            val rightZoneStart = width * 0.65f

                            when {
                                down.position.x < leftZoneEnd -> {
                                    val stepSec = settings.value.mediaSeekStep.roundToInt().coerceIn(1, 60)
                                    controlOption.value = MediaControlOption.TIME
                                    viewModel.geckoManager.mediaSeekStepSeconds = stepSec.toDouble()
                                    viewModel.geckoManager.sendVideoCommand("prev_5")
                                    statusAction.value = VideoStatusAction.SeekStep(-stepSec)
                                    interactionTrigger.value++
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                down.position.x > rightZoneStart -> {
                                    val stepSec = settings.value.mediaSeekStep.roundToInt().coerceIn(1, 60)
                                    controlOption.value = MediaControlOption.TIME
                                    viewModel.geckoManager.mediaSeekStepSeconds = stepSec.toDouble()
                                    viewModel.geckoManager.sendVideoCommand("next_5")
                                    statusAction.value = VideoStatusAction.SeekStep(stepSec)
                                    interactionTrigger.value++
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                else -> {
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
                        } else {
                            lastTapTimeMs = System.currentTimeMillis()
                            lastTapPosition = down.position
                            pendingSingleTapJob = coroutineScope.launch {
                                delay(viewConfiguration.doubleTapTimeoutMillis)
                                lastTapTimeMs = 0L
                                viewModel.updateUI {
                                    it.copy(isMediaControlPanelVisible = !it.isMediaControlPanelVisible)
                                }
                            }
                        }
                    }
                }
            }
    )
}