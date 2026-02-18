package marcinlowercase.a.ui.panel

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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

@Composable
fun VideoStatusPanel(
    modifier: Modifier = Modifier,
    gestureManager: MediaGestureManager,
    controlOption: MutableState<MediaControlOption>,
    pendingSeekSeconds: MutableState<Double>,
    interactionTrigger: MutableState<Int>, // Used to force recomposition when volume/brightness changes
    onSwapLayout: () -> Unit,
    isStatusAtTop: MutableState<Boolean>,
) {

    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState().value
val settings = viewModel.browserSettings.collectAsState().value

   
    var isTemporarilyVisible by remember { mutableStateOf(false) }

    LaunchedEffect(interactionTrigger.value) {
        if (interactionTrigger.value > 0) {
            isTemporarilyVisible = true
            delay(2000) // Keep visible for 2 seconds after interaction
            isTemporarilyVisible = false
        }
    }

    // 2. Calculate if the panel should be visible (Alpha = 1.0)
    val shouldShow by remember {
        derivedStateOf {
            uiState.isMediaControlPanelVisible || // Main panel is open
                    pendingSeekSeconds.value != 0.0 ||  // User is Seeking
                    isTemporarilyVisible                // User changed Vol/Bright
        }
    }

    val opacity by animateFloatAsState(
        targetValue = if (shouldShow) 1.0f else 0.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "video status opacity"
    )

    // Local tick for live position updates
    var liveTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.isMediaControlPanelVisible, viewModel.geckoManager.isActiveMediaSessionPaused) {
        if (!viewModel.geckoManager.isActiveMediaSessionPaused) {
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
            when (controlOption.value) {
                MediaControlOption.TIME -> {
                    val duration = viewModel.geckoManager.lastDuration.doubleValue
                    // If we are currently dragging (pendingSeekSeconds != 0), calculate target
                    if (pendingSeekSeconds.value != 0.0) {
                        val current = viewModel.geckoManager.lastPositionSnapshot.doubleValue
                        val targetTime = (current + pendingSeekSeconds.value).coerceIn(0.0, duration)

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
    var dragAccumulator by remember { mutableFloatStateOf(0f) }


    if (uiState.isOnFullscreenVideo && uiState.isMediaControlPanelDisplayed || shouldShow) Column (
        modifier = modifier
            .padding(settings.padding.dp)
            .widthIn(min = settings.heightForLayer(1).dp)
            .heightIn(min = settings.heightForLayer(1).dp)
            .clip(RoundedCornerShape(settings.cornerRadiusForLayer(1).dp))
            .alpha(opacity)
            .background(Color.Black)
            .clickable {
                if (uiState.isMediaControlPanelVisible) {
                    onSwapLayout()
                } else {
                    viewModel.updateUI { it.copy(isMediaControlPanelVisible = true) }
                }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = { dragAccumulator = 0f },
                    onDragCancel = { dragAccumulator = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount

                        // Sensitivity threshold (50 pixels)
                        if (dragAccumulator < -50f) {
                            // Dragged Up -> Stick to Top
                            isStatusAtTop.value = true
                            dragAccumulator = 0f
                        } else if (dragAccumulator > 50f) {
                            // Dragged Down -> Stick to Bottom
                            isStatusAtTop.value = false
                            dragAccumulator = 0f
                        }
                    }
                )
            }
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text = displayStatus,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .animateContentSize()
                .padding(horizontal = settings.cornerRadiusForLayer(1).dp)
        )
    }
}