package marcinlowercase.a.ui.panel

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.zIndex
import marcinlowercase.a.core.enum_class.MediaControlMode
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import marcinlowercase.a.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import marcinlowercase.a.core.enum_class.MediaControlOption
import marcinlowercase.a.core.function.formatTime
import marcinlowercase.a.core.manager.MediaGestureManager
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel


sealed class VideoStatusAction(val seq: Long = System.nanoTime()) {
    class Play(seq: Long = System.nanoTime()) : VideoStatusAction(seq)
    class Pause(seq: Long = System.nanoTime()) : VideoStatusAction(seq)
    class SeekStep(
        val deltaSeconds: Int,
        val originSeconds: Double,
        seq: Long = System.nanoTime()
    ) : VideoStatusAction(seq)
    class Speed2x(seq: Long = System.nanoTime()) : VideoStatusAction(seq)

    companion object {
        fun accumulateSeek(
            current: VideoStatusAction?,
            delta: Int,
            currentPositionSeconds: Double
        ): SeekStep {
            val prev = current as? SeekStep
            val sameDirection = prev != null &&
                ((prev.deltaSeconds > 0 && delta > 0) || (prev.deltaSeconds < 0 && delta < 0))
            val total = if (sameDirection) prev.deltaSeconds + delta else delta
            val origin = if (sameDirection) prev.originSeconds else currentPositionSeconds.coerceAtLeast(0.0)
            return SeekStep(total, origin)
        }
    }
}
@Composable
fun VideoStatusPanel(
    modifier: Modifier = Modifier,
    gestureManager: MediaGestureManager,
    controlOption: MutableState<MediaControlOption>,
    pendingSeekSeconds: MutableState<Double>,
    interactionTrigger: MutableState<Int>,
    statusAction: MutableState<VideoStatusAction?> = remember { mutableStateOf(null) },
    temporaryEscapeSeconds: MutableState<Int> = remember { mutableIntStateOf(0) },
    onSwapLayout: () -> Unit,
    onMoveHorizontal: (moveToLeft: Boolean) -> Unit = {},
    onExitFullscreen: () -> Unit = {},
    isStatusAtTop: MutableState<Boolean>,
) {

    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

   
    var isTemporarilyVisible by remember { mutableStateOf(false) }

    LaunchedEffect(interactionTrigger.value) {
        if (interactionTrigger.value > 0) {
            isTemporarilyVisible = true
            delay(2000) // keep visible for 2 seconds after interaction
            isTemporarilyVisible = false
        }
    }

    LaunchedEffect(uiState.value.isMediaControlPanelVisible) {
        if (!uiState.value.isMediaControlPanelVisible) {
            isTemporarilyVisible = false
        }
    }

    var showTimeAfterIdle by remember { mutableStateOf(false) }

    // switch display back to video timeline after 3 seconds without changing MediaControlPanel mode
    LaunchedEffect(interactionTrigger.value, controlOption.value) {
        if (controlOption.value != MediaControlOption.TIME) {
            showTimeAfterIdle = false
            delay(3000)
            showTimeAfterIdle = true
        } else {
            showTimeAfterIdle = false
        }
    }

    // show play/pause or seek step action for 1 second then revert to timeline; keep 2x until release
    LaunchedEffect(statusAction.value) {
        val current = statusAction.value
        if (current != null && current !is VideoStatusAction.Speed2x) {
            delay(1000)
            if (statusAction.value === current) {
                statusAction.value = null
            }
        }
    }

    LaunchedEffect(pendingSeekSeconds.value) {
        if (pendingSeekSeconds.value != 0.0) {
            statusAction.value = null
        }
    }

    LaunchedEffect(uiState.value.isOnFullscreenVideo) {
        if (!uiState.value.isOnFullscreenVideo) {
            showTimeAfterIdle = false
            statusAction.value = null
        }
    }

    val shouldShow by remember {
        derivedStateOf {
            uiState.value.isMediaControlPanelVisible ||
                pendingSeekSeconds.value != 0.0 ||
                isTemporarilyVisible ||
                statusAction.value != null ||
                temporaryEscapeSeconds.value > 0
        }
    }

    val opacity = if (shouldShow) 0.7f else 0.0f

    // Local tick for live position updates
    var liveTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(shouldShow, viewModel.geckoManager.isActiveMediaSessionPaused) {
        if (shouldShow && !viewModel.geckoManager.isActiveMediaSessionPaused) {
            while (true) {
                viewModel.geckoManager.tickLivePosition()
                liveTick++
                delay(500)
            }
        }
    }

    val displayStatus by remember(controlOption.value, interactionTrigger, pendingSeekSeconds) {
        derivedStateOf {
            val trigger = interactionTrigger.value
            val tick = liveTick
            val effectiveOption = if (pendingSeekSeconds.value != 0.0 || showTimeAfterIdle) {
                MediaControlOption.TIME
            } else {
                controlOption.value
            }
            when (effectiveOption) {
                MediaControlOption.TIME -> {
                    val duration = viewModel.geckoManager.lastDuration.doubleValue
                    // If we are currently dragging (pendingSeekSeconds != 0), calculate target
                    if (pendingSeekSeconds.value != 0.0) {
                        val current = viewModel.geckoManager.lastPositionSnapshot.doubleValue
                        val targetTime = (current + pendingSeekSeconds.value).coerceAtLeast(0.0)

                        val sign = if (pendingSeekSeconds.value > 0) "+" else ""
                        // Format: "1:20 / 5:00 (+15s)"
                        "${formatTime(targetTime)} ($sign${pendingSeekSeconds.value.toInt()}s)"
                    } else {
                        // Standard display
                        val current = formatTime(viewModel.geckoManager.lastPositionSnapshot.doubleValue)
                        val total = formatTime(duration)
                        "$current / $total"
                    }
                }
                MediaControlOption.VOLUME -> {
                    val (current, max) = gestureManager.getVolumeSteps()
                    "$current / $max"
                }
                MediaControlOption.BRIGHTNESS -> {
                    val (current, _) = gestureManager.getBrightnessSteps()
                    "$current%"
                }
            }
        }
    }
    var dragAccumulatorX by remember { mutableFloatStateOf(0f) }
    var dragAccumulatorY by remember { mutableFloatStateOf(0f) }
    val mediaControlMode = settings.value.currentMediaControlMode()
    val isInteractive = shouldShow || mediaControlMode == MediaControlMode.ONE_HAND

    if (settings.value.isEnabledMediaControl && uiState.value.isOnFullscreenVideo && (uiState.value.isMediaControlPanelDisplayed || shouldShow)) Column(
        modifier = modifier
            .zIndex(2f)
            .padding(settings.value.padding.dp)
            .widthIn(min = settings.value.heightForLayer(1).dp)
            .heightIn(min = settings.value.heightForLayer(1).dp)
            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
            .alpha(opacity)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .animateContentSize()
            .then(
                if (isInteractive) {
                    Modifier
                        .clickable {
                            if (temporaryEscapeSeconds.value > 0) {
                                // restore gesture overlay immediately on tap during countdown
                                temporaryEscapeSeconds.value = 0
                            } else if (mediaControlMode == MediaControlMode.GESTURE) {
                                onExitFullscreen()
                            } else if (uiState.value.isMediaControlPanelVisible) {
                                onSwapLayout()
                            } else {
                                viewModel.updateUI { it.copy(isMediaControlPanelVisible = true) }
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    dragAccumulatorX = 0f
                                    dragAccumulatorY = 0f
                                },
                                onDragEnd = {
                                    dragAccumulatorX = 0f
                                    dragAccumulatorY = 0f
                                },
                                onDragCancel = {
                                    dragAccumulatorX = 0f
                                    dragAccumulatorY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragAccumulatorX += dragAmount.x
                                    dragAccumulatorY += dragAmount.y

                                    // in gesture mode keep status panel locked to top left/right
                                    if (mediaControlMode != MediaControlMode.GESTURE) {
                                        if (dragAccumulatorY < -50f) {
                                            isStatusAtTop.value = true
                                            dragAccumulatorY = 0f
                                        } else if (dragAccumulatorY > 50f) {
                                            isStatusAtTop.value = false
                                            dragAccumulatorY = 0f
                                        }
                                    }

                                    if (dragAccumulatorX < -50f) {
                                        onMoveHorizontal(true)
                                        dragAccumulatorX = 0f
                                    } else if (dragAccumulatorX > 50f) {
                                        onMoveHorizontal(false)
                                        dragAccumulatorX = 0f
                                    }
                                }
                            )
                        }
                } else {
                    Modifier
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        if (temporaryEscapeSeconds.value > 0) {
            Text(
                text = temporaryEscapeSeconds.value.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = settings.value.padding.dp)
                    .padding(horizontal = settings.value.cornerRadiusForLayer(1).dp)
            )
        } else {
            when (val activeAction = statusAction.value) {
                is VideoStatusAction.Play -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play_arrow),
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                is VideoStatusAction.Pause -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pause),
                        contentDescription = "Pause",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                is VideoStatusAction.SeekStep -> {
                    val duration = viewModel.geckoManager.lastDuration.doubleValue
                    val rawTarget = (activeAction.originSeconds + activeAction.deltaSeconds).coerceAtLeast(0.0)
                    val targetTime = if (duration > 0.0) rawTarget.coerceAtMost(duration) else rawTarget
                    val sign = if (activeAction.deltaSeconds > 0) "+" else ""
                    Text(
                        text = "${formatTime(targetTime)} ($sign${activeAction.deltaSeconds}s)",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = settings.value.padding.dp)
                            .padding(horizontal = settings.value.cornerRadiusForLayer(1).dp)
                    )
                }
                is VideoStatusAction.Speed2x -> {
                    @Suppress("UNUSED_VARIABLE")
                    val tick = liveTick
                    val current = formatTime(viewModel.geckoManager.lastPositionSnapshot.doubleValue)
                    Text(
                        text = "$current / 2x",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = settings.value.padding.dp)
                            .padding(horizontal = settings.value.cornerRadiusForLayer(1).dp)
                    )
                }
                null -> {
                    Text(
                        text = displayStatus,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = settings.value.padding.dp)
                            .padding(horizontal = settings.value.cornerRadiusForLayer(1).dp)
                    )
                }
            }
        }
    }
}