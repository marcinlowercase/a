package marcinlowercase.a.ui.panel

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.enum_class.GestureNavAction
import marcinlowercase.a.core.enum_class.MediaControlOption
import marcinlowercase.a.core.function.formatTime
import marcinlowercase.a.core.manager.GeckoManager
import marcinlowercase.a.core.manager.MediaGestureManager
import marcinlowercase.a.ui.component.CustomIconButton
import kotlin.math.abs

@Composable
fun MediaControlPanel(
//    activeMediaCurrentPosition: MutableState<Double>,
//    activeMediaDuration: MutableState<Double>,
    hapticFeedback: HapticFeedback,
    modifier: Modifier,
    isMediaControlPanelVisible: MutableState<Boolean>,
    isOnFullscreenVideo: MutableState<Boolean>,
    browserSettings: MutableState<BrowserSettings>,
    descriptionContent:  MutableState<String>,
    onExitFullscreen: () -> Unit,
    geckoManager: GeckoManager,
    gestureManager: MediaGestureManager,
) {

    val opacity by animateFloatAsState(
        targetValue = if (isMediaControlPanelVisible.value) 1.0f else 0.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "media control panel opacity"
    )
//    var isPlus by remember { mutableStateOf(true) }
    var controlOption by remember { mutableStateOf(MediaControlOption.TIME) }

    var livePosition by remember { mutableStateOf(0.0) }

    var interactionTrigger by remember { mutableStateOf(0) }

    var dragAccumulator by remember { mutableStateOf(0f) }
    var pendingSeekSeconds by remember { mutableStateOf(0.0) }


    LaunchedEffect(isMediaControlPanelVisible.value, geckoManager.isActiveMediaSessionPaused) {
        if (  !geckoManager.isActiveMediaSessionPaused) {
            while (true) {
                // Update the state inside GeckoManager directly
                geckoManager.tickLivePosition()

                delay(500) // Update UI every 500ms
            }
        }
    }

    LaunchedEffect(isOnFullscreenVideo.value) {
        if (isOnFullscreenVideo.value) {
            gestureManager.ensureFullscreenBrightness()
        }
    }

    LaunchedEffect(geckoManager.lastDuration.value) {
        Log.i("marcMedia", "duration: ${geckoManager.lastDuration.value}")
    }

    val displayStatus by remember(controlOption, livePosition, interactionTrigger, pendingSeekSeconds) {
        derivedStateOf {
            when (controlOption) {
                MediaControlOption.TIME -> {
                    val duration = geckoManager.lastDuration.value
                    // If we are currently dragging (pendingSeekSeconds != 0), calculate target
                    if (pendingSeekSeconds != 0.0) {
                        val current = geckoManager.lastPositionSnapshot.value
                        val targetTime = (current + pendingSeekSeconds).coerceIn(0.0, duration)

                        val sign = if (pendingSeekSeconds > 0) "+" else ""
                        // Format: "1:20 / 5:00 (+15s)"
                        "${formatTime(targetTime)} / ${formatTime(duration)} ($sign${pendingSeekSeconds.toInt()}s)"
                    } else {
                        // Standard display
                        val current = formatTime(geckoManager.lastPositionSnapshot.value)
                        val total = formatTime(duration)
                        "$current / $total"
                    }
                }
                MediaControlOption.VOLUME -> {
                    val (current, max) = gestureManager.getVolumeSteps()
                    "Vol: $current / $max"
                }
                MediaControlOption.BRIGHTNESS -> {
                    val (current, _) = gestureManager.getBrightnessSteps()
                    "Bright: $current%"
                }
            }
        }
    }

    if (isOnFullscreenVideo.value) {
        Column (
            modifier = modifier
                .padding(browserSettings.value.padding.dp)
                .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(1).dp))
                .alpha(opacity)
                .clickable(
                    enabled = true,
                    onClick = {
                        if (!isMediaControlPanelVisible.value) isMediaControlPanelVisible.value =
                            true
                    }
                )
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Text(
                text = displayStatus,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .padding(browserSettings.value.padding.dp)
            )
            Row(
                modifier = Modifier
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()

                        .width(browserSettings.value.heightForLayer(1).dp)
                        .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(1).dp)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomIconButton(
                        isLandscape = true,
                        currentRotation = 0f,
                        layer = 2,
                        browserSettings = browserSettings,
                        modifier = Modifier
                            .weight(1f)
                            .padding(browserSettings.value.padding.dp),
                        onTap = {
                            if (isMediaControlPanelVisible.value) {
                                onExitFullscreen()
                            } else {
                                isMediaControlPanelVisible.value = true
                            }
                        },
                        descriptionContent = descriptionContent,
                        buttonDescription = "plus/minus",
                        painterId = R.drawable.ic_fullscreen_exit,
                        isWhite = true
                    )

                    CustomIconButton(
                        isLandscape = true,
                        currentRotation = 0f,
                        layer = 2,
                        browserSettings = browserSettings,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = browserSettings.value.padding.dp)
                            .padding(bottom = browserSettings.value.padding.dp),
                        onTap = {
                            if (isMediaControlPanelVisible.value) {
                                val nextIndex = (controlOption.ordinal + 1) % MediaControlOption.entries.size
                                controlOption = MediaControlOption.entries[nextIndex]
                                interactionTrigger++
                            } else {
                                isMediaControlPanelVisible.value = true
                            }
                        },
                        descriptionContent = descriptionContent,
                        buttonDescription = "control option",
                        painterId = controlOption.iconRes,
                        isWhite = true
                    )
                }


                // Action Bar
                BoxWithConstraints(
                    modifier = Modifier
                        .padding(end = browserSettings.value.padding.dp)
                        .padding(vertical = browserSettings.value.padding.dp)
                        .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(2).dp))
                        .fillMaxHeight()
                        .width(browserSettings.value.heightForLayer(2).dp)
                        .background(Color.Red)
//                        .pointerInput(Unit) {
//                            detectVerticalDragGestures(
//                                onDragEnd = { dragAccumulator = 0f },
//                                onDragCancel = { dragAccumulator = 0f },
//                                onVerticalDrag = { change, dragAmount ->
//                                    change.consume()
//
//                                    // Invert dragAmount because swiping UP gives negative Y,
//                                    // but we want UP to be Positive (Increase)
//                                    dragAccumulator += (-dragAmount)
//
//                                    // Define Sensitivity (Pixels required to trigger 1 step)
//                                    val sensitivity = when(controlOption) {
//                                        MediaControlOption.VOLUME -> 40f     // Heavier pull for volume
//                                        MediaControlOption.BRIGHTNESS -> 15f // Lighter pull for brightness (0-100)
//                                        MediaControlOption.TIME -> 30f       // Medium pull for time
//                                    }
//
//                                    // Check if we crossed the threshold (positive or negative)
//                                    if (abs(dragAccumulator) > sensitivity) {
//                                        // How many steps did we move?
//                                        val steps = (dragAccumulator / sensitivity).toInt()
//
//                                        if (steps != 0) {
//                                            when (controlOption) {
//                                                MediaControlOption.TIME -> {
//                                                    // 5 seconds per step
//                                                    val timeDelta = (steps * 5).toDouble()
//                                                    geckoManager.sendVideoCommand("seek_relative", timeDelta)
//                                                }
//                                                MediaControlOption.VOLUME -> {
//                                                    // 1 volume level per step
//                                                    gestureManager.setVolume(steps)
//                                                }
//                                                MediaControlOption.BRIGHTNESS -> {
//                                                    // 2% brightness per step
//                                                    gestureManager.setBrightness(steps * 2)
//                                                }
//                                            }
//
//                                            // Trigger UI update and Haptic
//                                            interactionTrigger++
//                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//
//                                            // Reset accumulator by removing the consumed steps
//                                            // (keeping the remainder ensures smooth continuous dragging)
//                                            dragAccumulator -= (steps * sensitivity)
//                                        }
//                                    }
//                                }
//                            )
//                        }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    // === ON RELEASE ===
                                    // Only for Time: Apply the final seek now
                                    if (controlOption == MediaControlOption.TIME && pendingSeekSeconds != 0.0) {
                                        geckoManager.sendVideoCommand("seek_relative", pendingSeekSeconds)
                                        pendingSeekSeconds = 0.0
                                    }
                                    dragAccumulator = 0f
                                },
                                onDragCancel = {
                                    pendingSeekSeconds = 0.0
                                    dragAccumulator = 0f
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    // Invert: Swipe UP (+)
                                    dragAccumulator += (-dragAmount)

                                    if (controlOption == MediaControlOption.TIME) {
                                        // === TIME (PREVIEW MODE) ===
                                        // Sensitivity: 10 pixels = 1 second
                                        val sensitivity = 10f

                                        if (abs(dragAccumulator) > sensitivity) {
                                            val secondsToAdd = (dragAccumulator / sensitivity).toInt()

                                            if (secondsToAdd != 0) {
                                                pendingSeekSeconds += secondsToAdd
                                                // Consume pixels so we don't re-add them next frame
                                                dragAccumulator -= (secondsToAdd * sensitivity)

                                                // Haptic only on value change
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        }
                                    } else {
                                        // === VOLUME & BRIGHTNESS (LIVE MODE) ===
                                        val sensitivity = if (controlOption == MediaControlOption.VOLUME) 40f else 15f

                                        if (abs(dragAccumulator) > sensitivity) {
                                            val steps = (dragAccumulator / sensitivity).toInt()
                                            if (steps != 0) {
                                                if (controlOption == MediaControlOption.VOLUME) {
                                                    gestureManager.setVolume(steps)
                                                } else {
                                                    gestureManager.setBrightness(steps * 2)
                                                }

                                                interactionTrigger++
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                dragAccumulator -= (steps * sensitivity)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTapGestures  (
                                onDoubleTap = { offset ->

                                    val segmentHeight = size.height / 2
                                    // Top half = Decrease / Prev
                                    // Bottom half = Increase / Next
                                    val isTopHalf = offset.y < segmentHeight

                                    when (controlOption) {
                                        MediaControlOption.TIME -> {
                                            if (isTopHalf) {
                                                geckoManager.sendVideoCommand("prev_5")
                                            } else {
                                                Log.w("marcMGesture", "next")
                                                geckoManager.sendVideoCommand("next_5")
                                            }
                                        }
                                        MediaControlOption.VOLUME -> {
                                            if (isTopHalf) {
                                                Log.i("mrVol", "decrease step")
                                                gestureManager.setVolume(-1)
                                            } else {
                                                Log.i("mrVol", "increase step")
                                                gestureManager.setVolume(1)
                                            }
                                        }
                                        MediaControlOption.BRIGHTNESS -> {
                                            val stepAmount = 5

                                            if (isTopHalf) {
                                                Log.i("mrBright", "decrease")
                                                gestureManager.setBrightness(-stepAmount)
                                            } else {
                                                Log.i("mrBright", "increase")
                                                gestureManager.setBrightness(stepAmount)
                                            }
                                        }
                                    }
                                    interactionTrigger++
                                },
                                onTap = {
                                    if (isMediaControlPanelVisible.value) {
                                        if (geckoManager.isActiveMediaSessionPaused) {
                                            geckoManager.sendVideoCommand("play")
                                        } else {
                                            geckoManager.sendVideoCommand("pause")
                                        }
                                    } else {
                                        isMediaControlPanelVisible.value = true
                                    }
                                    Log.e("marcMGesture", "tap")

                                },
                            )
                        }

                ) {

                }

            }
        }
    }
}