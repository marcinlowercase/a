package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import marcinlowercase.a.core.enum_class.MediaControlMode
import marcinlowercase.a.core.enum_class.MediaControlOption
import marcinlowercase.a.ui.component.OptionsRoller
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import java.util.Locale

@Composable
fun VideoSeekBar(
    modifier: Modifier = Modifier,
    controlOption: MutableState<MediaControlOption>,
    pendingSeekSeconds: MutableState<Double>,
    interactionTrigger: MutableState<Int>,
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    if (settings.value.currentMediaControlMode() != MediaControlMode.GESTURE ||
        !uiState.value.isOnFullscreenVideo ||
        !uiState.value.isMediaControlPanelDisplayed
    ) {
        return
    }

    val speedOptions = remember { (10..20).map { it / 10f } }
    var selectedSpeed by remember { mutableFloatStateOf(1.0f) }

    LaunchedEffect(uiState.value.isOnFullscreenVideo) {
        if (!uiState.value.isOnFullscreenVideo) {
            selectedSpeed = 1.0f
        }
    }

    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    var liveTick by remember { mutableIntStateOf(0) }

    val isVisible = uiState.value.isMediaControlPanelVisible || isDragging

    LaunchedEffect(isVisible, isDragging, viewModel.geckoManager.isActiveMediaSessionPaused) {
        if (isVisible && !isDragging && !viewModel.geckoManager.isActiveMediaSessionPaused) {
            while (true) {
                viewModel.geckoManager.tickLivePosition()
                liveTick++
                delay(500)
            }
        }
    }

    val duration = viewModel.geckoManager.lastDuration.doubleValue
    @Suppress("UNUSED_VARIABLE")
    val tick = liveTick
    val currentPos = viewModel.geckoManager.lastPositionSnapshot.doubleValue.coerceAtLeast(0.0)
    val liveFraction = if (duration > 0.0) (currentPos / duration).toFloat().coerceIn(0f, 1f) else 0f

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(settings.value.animationSpeedForLayer(1))),
        exit = fadeOut(tween(settings.value.animationSpeedForLayer(1))),
        modifier = modifier.zIndex(2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(settings.value.padding.dp)
                .heightIn(min = settings.value.heightForLayer(1).dp)
                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                .alpha(0.7f)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(settings.value.padding.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
        ) {
            OptionsRoller(
                items = speedOptions,
                selectedItem = selectedSpeed,
                onItemSelected = { newSpeed ->
                    selectedSpeed = newSpeed
                    viewModel.geckoManager.sendVideoCommand("set_speed", newSpeed.toDouble())
                },
                itemLabel = { speed ->
                    String.format(Locale.US, "%.1fx", speed)
                },
                layer = 2
            )

            Slider(
                value = if (isDragging) dragFraction else liveFraction,
                enabled = duration > 0.0,
                onValueChange = { newFraction ->
                    if (!isDragging) {
                        viewModel.geckoManager.tickLivePosition()
                        isDragging = true
                    }
                    dragFraction = newFraction.coerceIn(0f, 1f)
                    if (duration > 0.0) {
                        controlOption.value = MediaControlOption.TIME
                        val targetSeconds = dragFraction * duration
                        val snapshot = viewModel.geckoManager.lastPositionSnapshot.doubleValue.coerceAtLeast(0.0)
                        val delta = targetSeconds - snapshot
                        pendingSeekSeconds.value = if (delta == 0.0) 0.0001 else delta
                        interactionTrigger.value++
                    }
                },
                onValueChangeFinished = {
                    if (duration > 0.0) {
                        val targetSeconds = (dragFraction * duration).coerceIn(0.0, duration)
                        viewModel.geckoManager.sendVideoCommand("seek_to", targetSeconds)
                    }
                    pendingSeekSeconds.value = 0.0
                    isDragging = false
                },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onSurface,
                    activeTrackColor = Color(settings.value.highlightColor),
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = settings.value.cornerRadiusForLayer(2).dp)
            )
        }
    }
}