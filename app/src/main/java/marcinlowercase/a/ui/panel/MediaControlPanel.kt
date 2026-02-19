package marcinlowercase.a.ui.panel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import marcinlowercase.a.R
import marcinlowercase.a.core.enum_class.MediaControlOption
import marcinlowercase.a.core.manager.MediaGestureManager
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import kotlin.math.abs

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MediaControlPanel(
    hapticFeedback: HapticFeedback,
    modifier: Modifier,
    descriptionContent:  MutableState<String>,
    onExitFullscreen: () -> Unit,
    gestureManager: MediaGestureManager,
    controlOption: MutableState<MediaControlOption>,
    pendingSeekSeconds: MutableState<Double>,
    interactionTrigger: MutableState<Int>
) {

    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()
    LaunchedEffect(uiState.value.isMediaControlPanelVisible) {
        Log.d("MediaDisplay", "isMediaControlPanelVisible ${uiState.value.isMediaControlPanelVisible}")
    }
    val opacity by animateFloatAsState(
        targetValue = if (uiState.value.isMediaControlPanelVisible) 1.0f else 0.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "media control panel opacity"
    )
//    var isPlus by remember { mutableStateOf(true) }



    var dragAccumulator by remember { mutableFloatStateOf(0f) }


    LaunchedEffect(uiState.value.isMediaControlPanelVisible, viewModel.geckoManager.isActiveMediaSessionPaused) {
        if (  !viewModel.geckoManager.isActiveMediaSessionPaused) {
            while (true) {
                // Update the state inside GeckoManager directly
                viewModel.geckoManager.tickLivePosition()

                delay(500) // Update UI every 500ms
            }
        }
    }


    Log.w("MediaDisplay", "uiState.value.isOnFullscreenVideo${uiState.value.isOnFullscreenVideo}")
    Log.w("MediaDisplay", "uiState.value.isMediaControlPanelDisplayed${uiState.value.isMediaControlPanelDisplayed}")
    if (uiState.value.isOnFullscreenVideo && uiState.value.isMediaControlPanelDisplayed) {
        Column (
            modifier = modifier
                .padding(settings.value.padding.dp)
                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                .alpha(opacity)
                .clickable(
                    enabled = true,
                    onClick = {
                        if (!uiState.value.isMediaControlPanelVisible) viewModel.updateUI { it.copy(isMediaControlPanelVisible = true) }
                    }
                )
                .background(Color.Black)
                .width(settings.value.heightForLayer(1).dp)
                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
            ,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            CustomIconButton(
                isLandscape = true,
                layer = 2,
                modifier = Modifier
                    .weight(1f)
                    .padding(settings.value.padding.dp),
                onTap = {
                    if (uiState.value.isMediaControlPanelVisible) {
                        onExitFullscreen()
                    } else {
                        viewModel.updateUI { it.copy(isMediaControlPanelVisible = true)}
                    }
                },
                onLongPress = {
                    viewModel.updateUI { it.copy(isMediaControlPanelDisplayed = false)}
                        true
                },
                descriptionContent = descriptionContent,
                buttonDescription = "plus/minus",
                painterId = R.drawable.ic_fullscreen_exit,
                isWhite = true
            )

            // Action Bar
            BoxWithConstraints(
                modifier = Modifier
                    .weight(2f)
                    .padding(horizontal = settings.value.padding.dp)
//                    .padding(vertical = settings.value.padding.dp)
                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
                    .fillMaxHeight()
                    .width(settings.value.heightForLayer(2).dp)
                    .background(Color.Red)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                // === ON RELEASE ===
                                // Only for Time: Apply the final seek now
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
                                    dragAmount // Natural scroll for Time (Down is Next)
                                } else {
                                    -dragAmount // Inverted scroll for Vol/Bright (Up is Increase)
                                }

                                dragAccumulator += effectiveDrag

                                if (controlOption.value == MediaControlOption.TIME) {
                                    // === TIME (PREVIEW MODE) ===
                                    // Sensitivity: 7 pixels = 1 second
                                    val sensitivity = 7f

                                    if (abs(dragAccumulator) > sensitivity) {
                                        // Put - to reverse the way
                                        val secondsToAdd = (dragAccumulator / sensitivity).toInt()

                                        if (secondsToAdd != 0) {
                                            pendingSeekSeconds.value += secondsToAdd
                                            // Consume pixels so we don't re-add them next frame
                                            dragAccumulator -= (secondsToAdd * sensitivity)

                                            // Haptic only on value change
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                } else {
                                    // === VOLUME & BRIGHTNESS (LIVE MODE) ===
                                    val sensitivity = if (controlOption.value == MediaControlOption.VOLUME) 40f else 15f

                                    if (abs(dragAccumulator) > sensitivity) {
                                        val steps = (dragAccumulator / sensitivity).toInt()
                                        if (steps != 0) {
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
                        detectTapGestures  (
                            onTap = {
                                if (uiState.value.isMediaControlPanelVisible) {
                                    Log.i("MediaDisplay", "${viewModel.geckoManager.isActiveMediaSessionPaused}")
                                    if (viewModel.geckoManager.isActiveMediaSessionPaused) {
                                        viewModel.geckoManager.sendVideoCommand("play")
                                    } else {
                                        viewModel.geckoManager.sendVideoCommand("pause")
                                    }
                                } else {
                                    viewModel.updateUI { it.copy(isMediaControlPanelVisible = true)}
                                }
                                Log.e("marcMGesture", "tap")

                            },
                            onDoubleTap = { offset ->

                                val segmentHeight = size.height / 2
                                // Top half = Decrease / Prev
                                // Bottom half = Increase / Next
                                val isTopHalf = offset.y < segmentHeight

                                when (controlOption.value) {
                                    MediaControlOption.TIME -> {
                                        if (isTopHalf) {
                                            viewModel.geckoManager.sendVideoCommand("prev_5")
                                        } else {
                                            Log.w("marcMGesture", "next")
                                            viewModel.geckoManager.sendVideoCommand("next_5")
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
                                interactionTrigger.value++
                            },
                            onLongPress = {offset ->
                                val segmentHeight = size.height / 2
                                // Top half = Decrease / Prev
                                // Bottom half = Increase / Next
                                val isTopHalf = offset.y < segmentHeight
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                                when (controlOption.value) {
                                    MediaControlOption.TIME -> {
                                        val target  =  if (viewModel.geckoManager.lastDuration.doubleValue > 0.0) viewModel.geckoManager.lastDuration.doubleValue / 10.0 else 60.0
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
                                            gestureManager.setBrightness(+maxSteps)

                                        } else {
                                            gestureManager.setBrightness(-maxSteps)
                                        }
                                    }
                                }
                                interactionTrigger.value++
                            }
                        )
                    }


            ) {
               // nothing inside this
            }

            CustomIconButton(
                isLandscape = true,
//                        currentRotation = 0f,
                layer = 2,
                modifier = Modifier
                    .weight(1f)
                    .padding( settings.value.padding.dp),
                onTap = {
                    if (uiState.value.isMediaControlPanelVisible) {
                        val nextIndex = (controlOption.value.ordinal + 1) % MediaControlOption.entries.size
                        controlOption.value = MediaControlOption.entries[nextIndex]
                        interactionTrigger.value++
                    } else {
                        viewModel.updateUI { it.copy(isMediaControlPanelVisible = true)}
                    }
                },
                descriptionContent = descriptionContent,
                buttonDescription = "control option",
                painterId = controlOption.value.iconRes,
                isWhite = true
            )
        }
    }
}