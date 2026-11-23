package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel

@Composable
fun FindInPagePanel(
    isVisible: Boolean,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onClose: () -> Unit,
    isLandscapeLayout: Boolean = false,
    leftStemHeight: Dp = 305.dp,
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    AnimatedVisibility(
        visible = isVisible,
        enter = if (isLandscapeLayout) {
            expandHorizontally(tween(settings.value.animationSpeedForLayer(1))) +
                    fadeIn(tween(settings.value.animationSpeedForLayer(1)))
        } else {
            expandVertically(tween(settings.value.animationSpeedForLayer(1))) +
                    fadeIn(tween(settings.value.animationSpeedForLayer(1)))
        },
        exit = if (isLandscapeLayout) {
            shrinkHorizontally(tween(settings.value.animationSpeedForLayer(1))) +
                    fadeOut(tween(settings.value.animationSpeedForLayer(1)))
        } else {
            shrinkVertically(tween(settings.value.animationSpeedForLayer(1))) +
                    fadeOut(tween(settings.value.animationSpeedForLayer(1)))
        }
    ) {
        if (isLandscapeLayout) {
            Column(
                modifier = Modifier
                    .height(leftStemHeight)
                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding( vertical = settings.value.padding.dp)
                    .padding( end = settings.value.padding.dp)
                    .padding(start = if (uiState.value.isOptionsPanelVisible) 0.dp else settings.value.padding.dp)
                    .width(settings.value.heightForLayer(2).dp),
                verticalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomIconButton(
                    isLandscape = true,
                    layer = 2,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    onTap = onFindPrevious,
                    buttonDescription = stringResource(R.string.desc_find_previous),
                    painterId = R.drawable.ic_arrow_upward,
                    isWhite = viewModel.findInPageResult.value.second > 0,
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${viewModel.findInPageResult.value.first}/${viewModel.findInPageResult.value.second}",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                CustomIconButton(
                    isLandscape = true,
                    layer = 2,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    onTap = onFindNext,
                    buttonDescription = stringResource(R.string.desc_find_next),
                    painterId = R.drawable.ic_arrow_downward,
                    isWhite = viewModel.findInPageResult.value.second > 0,
                )

                CustomIconButton(
                    isLandscape = true,
                    layer = 2,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    onTap = onClose,
                    buttonDescription = stringResource(R.string.desc_cancel),
                    painterId = R.drawable.ic_arrow_back,
                    isWhite = false,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(1).dp
                        )
                    )
                    .fillMaxWidth()
                    .padding(horizontal = settings.value.padding.dp)
                    .padding(top = settings.value.padding.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
            ) {
                CustomIconButton(
                    layer = 2,
                    modifier = Modifier.weight(1f),
                    onTap = onClose,
                    buttonDescription = stringResource(R.string.desc_cancel),
                    painterId = R.drawable.ic_arrow_back,
                    isWhite = false,
                )
                CustomIconButton(
                    layer = 2,
                    modifier = Modifier.weight(1f),
                    onTap = onFindNext,
                    buttonDescription = stringResource(R.string.desc_find_next),
                    painterId = R.drawable.ic_arrow_downward,
                    isWhite = viewModel.findInPageResult.value.second > 0,
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${viewModel.findInPageResult.value.first}/${viewModel.findInPageResult.value.second}",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                CustomIconButton(
                    layer = 2,
                    modifier = Modifier.weight(1f),
                    onTap = onFindPrevious,
                    buttonDescription = stringResource(R.string.desc_find_previous),
                    painterId = R.drawable.ic_arrow_upward,
                    isWhite = viewModel.findInPageResult.value.second > 0,
                )
            }
        }
    }
}