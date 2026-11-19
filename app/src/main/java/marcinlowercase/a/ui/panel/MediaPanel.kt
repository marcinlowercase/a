package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import marcinlowercase.a.core.enum_class.MediaControlMode

@Composable
fun MediaPanel() {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    val mediaMode = settings.value.currentMediaControlMode()

    val hasActiveVideo = viewModel.geckoManager.activeGeckoMediaSession != null ||
            viewModel.geckoManager.activeSilentVideoSession != null ||
            viewModel.geckoManager.currentFullscreenSession != null

    val isVisible = uiState.value.isUrlBarVisible &&
            !uiState.value.isFocusOnTextField &&
            hasActiveVideo

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(tween(settings.value.animationSpeedForLayer(1))) +
                fadeIn(tween(settings.value.animationSpeedForLayer(1))),
        exit = shrinkVertically(tween(settings.value.animationSpeedForLayer(1))) +
                fadeOut(tween(settings.value.animationSpeedForLayer(1)))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = settings.value.padding.dp)
                .padding(top = settings.value.padding.dp)
                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
                .fillMaxWidth()
                .heightIn(min = settings.value.heightForLayer(2).dp)
                .padding(horizontal = settings.value.padding.dp)
            ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
        ) {
            Text(
                text = viewModel.geckoManager.lastTitle.value.ifBlank { "video" },
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = settings.value.padding.dp)
                    .padding(start = settings.value.cornerRadiusForLayer(3).dp)
                    .horizontalScroll(rememberScrollState())
            )

            // toggle one hand media control mode
            CustomIconButton(
                layer = 3,
                isSquare = true,
                onTap = {
                    val nextMode = if (mediaMode == MediaControlMode.ONE_HAND) {
                        MediaControlMode.NONE
                    } else {
                        MediaControlMode.ONE_HAND
                    }
                    viewModel.updateSettings {
                        it.copy(
                            mediaControlMode = nextMode.ordinal,
                            isEnabledMediaControl = nextMode != MediaControlMode.NONE
                        )
                    }
                },
                buttonDescription = stringResource(R.string.desc_one_hand_media_control),
                painterId = R.drawable.ic_fullscreen,
                isWhite = mediaMode == MediaControlMode.ONE_HAND
            )

            // toggle gesture media control mode
            CustomIconButton(
                layer = 3,
                isSquare = true,
                onTap = {
                    val nextMode = if (mediaMode == MediaControlMode.GESTURE) {
                        MediaControlMode.NONE
                    } else {
                        MediaControlMode.GESTURE
                    }
                    viewModel.updateSettings {
                        it.copy(
                            mediaControlMode = nextMode.ordinal,
                            isEnabledMediaControl = nextMode != MediaControlMode.NONE
                        )
                    }
                },
                buttonDescription = stringResource(R.string.desc_gesture_media_control),
                painterId = R.drawable.ic_media_gesture,
                isWhite = mediaMode == MediaControlMode.GESTURE
            )
        }
    }
}