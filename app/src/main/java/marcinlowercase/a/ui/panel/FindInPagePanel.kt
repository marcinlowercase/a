package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
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
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ) + fadeIn(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ),
        exit = shrinkVertically(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ) + fadeOut(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        )
    ) {
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