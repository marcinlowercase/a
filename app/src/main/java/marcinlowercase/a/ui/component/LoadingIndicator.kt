package marcinlowercase.a.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {

    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()


    // Animate the appearance and disappearance of the overlay.
    AnimatedVisibility(
        visible = uiState.value.isLoading,
        modifier = modifier,
        enter = expandVertically(tween(settings.value.animationSpeedForLayer(1))),
        exit = shrinkVertically(tween(settings.value.animationSpeedForLayer(1))) +
                fadeOut(tween(settings.value.animationSpeedForLayer(1)))
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .padding(top = settings.value.padding.dp)
                .padding(horizontal = settings.value.cornerRadiusForLayer(1).dp)
                .fillMaxWidth()
            ,
            color = Color.White,
            trackColor = Color.White.copy(0.3f),
        )
    }
}