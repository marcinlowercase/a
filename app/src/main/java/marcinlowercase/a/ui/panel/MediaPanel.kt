package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
fun MediaPanel() {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    val mediaProfileId = viewModel.geckoManager.activeMediaProfileId ?: viewModel.currentProfileId
    val currentMediaProfileId by rememberUpdatedState(mediaProfileId)
    val mediaMode = viewModel.getMediaControlModeForProfile(mediaProfileId)

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
            val isPlaying = !viewModel.geckoManager.isActiveMediaSessionPaused

            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = settings.value.heightForLayer(3).dp)
                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp))
                    .clickable { viewModel.jumpToActiveMediaTab() }
                    .padding(horizontal = settings.value.padding.dp)
                    .padding(start = settings.value.cornerRadiusForLayer(3).dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = viewModel.geckoManager.lastTitle.value.ifBlank { "video" },
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = if (isPlaying) TextOverflow.Clip else TextOverflow.Ellipsis,
                    modifier = Modifier.then(
                        if (isPlaying) {
                            Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                        } else {
                            Modifier
                        }
                    )
                )
            }

            // toggle one hand media control mode for the playing video's profile
            CustomIconButton(
                layer = 3,
                isSquare = true,
                onTap = {
                    viewModel.toggleMediaControlModeForProfile(
                        currentMediaProfileId,
                        MediaControlMode.ONE_HAND
                    )
                },
                buttonDescription = stringResource(R.string.desc_one_hand_media_control),
                painterId = R.drawable.ic_one_hand_media_control,
                isWhite = mediaMode == MediaControlMode.ONE_HAND
            )

            // toggle fullscreen media control mode for the playing video's profile
            CustomIconButton(
                layer = 3,
                isSquare = true,
                onTap = {
                    viewModel.toggleMediaControlModeForProfile(
                        currentMediaProfileId,
                        MediaControlMode.GESTURE
                    )
                },
                buttonDescription = stringResource(R.string.desc_gesture_media_control),
                painterId = R.drawable.ic_fullscreen_media_control,
                isWhite = mediaMode == MediaControlMode.GESTURE
            )
        }
    }
}