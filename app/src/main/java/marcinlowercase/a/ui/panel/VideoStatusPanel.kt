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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.enum_class.MediaControlOption
import marcinlowercase.a.core.function.formatTime
import marcinlowercase.a.core.manager.GeckoManager
import marcinlowercase.a.core.manager.MediaGestureManager

@Composable
fun VideoStatusPanel(
    modifier: Modifier = Modifier,
    isMediaControlPanelVisible: MutableState<Boolean>,
    browserSettings: MutableState<BrowserSettings>,
    geckoManager: GeckoManager,
    gestureManager: MediaGestureManager,
    controlOption: MutableState<MediaControlOption>,
    pendingSeekSeconds: MutableState<Double>,
    interactionTrigger: MutableState<Int>, // Used to force recomposition when volume/brightness changes
    onSwapLayout: () -> Unit,
    isStatusAtTop: MutableState<Boolean>,
    isOnFullscreenVideo: MutableState<Boolean>,
) {
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
            isMediaControlPanelVisible.value || // Main panel is open
                    pendingSeekSeconds.value != 0.0 ||  // User is Seeking
                    isTemporarilyVisible                // User changed Vol/Bright
        }
    }

    // 3. Update Animation based on 'shouldShow' instead of just 'isMediaControlPanelVisible'
    val opacity by animateFloatAsState(
        targetValue = if (shouldShow) 1.0f else 0.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "video status opacity"
    )

    // Local tick for live position updates
    var liveTick by remember { mutableStateOf(0) }

    LaunchedEffect(isMediaControlPanelVisible.value, geckoManager.isActiveMediaSessionPaused) {
        if (!geckoManager.isActiveMediaSessionPaused) {
            while (true) {
                geckoManager.tickLivePosition()
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
                    val duration = geckoManager.lastDuration.value
                    // If we are currently dragging (pendingSeekSeconds != 0), calculate target
                    if (pendingSeekSeconds.value != 0.0) {
                        val current = geckoManager.lastPositionSnapshot.value
                        val targetTime = (current + pendingSeekSeconds.value).coerceIn(0.0, duration)

                        val sign = if (pendingSeekSeconds.value > 0) "+" else ""
                        // Format: "1:20 / 5:00 (+15s)"
                        "${formatTime(targetTime)} ($sign${pendingSeekSeconds.value.toInt()}s)"
                    } else {
                        // Standard display
                        val current = formatTime(geckoManager.lastPositionSnapshot.value)
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


    if (isOnFullscreenVideo.value || shouldShow) Column (
        modifier = modifier
            .padding(browserSettings.value.padding.dp)
            .widthIn(min = browserSettings.value.heightForLayer(1).dp)
            .heightIn(min = browserSettings.value.heightForLayer(1).dp)
            .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(1).dp))
            .alpha(opacity)
            .background(Color.Black)
            .clickable {
                if (isMediaControlPanelVisible.value) {
                    onSwapLayout()
                } else {
                    isMediaControlPanelVisible.value = true
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
                .padding(horizontal = browserSettings.value.cornerRadiusForLayer(1).dp)
        )
    }
}