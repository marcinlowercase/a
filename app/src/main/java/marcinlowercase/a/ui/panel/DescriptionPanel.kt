package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun DescriptionPanel(
    isVisible: Boolean,
    description: String,
    onDismiss: () -> Unit
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            tween(
                settings.value.animationSpeedForLayer(2)
            )
        ) + fadeIn(
            tween(
                settings.value.animationSpeedForLayer(2)
            )
        ),
        exit = shrinkVertically(
            tween(
                settings.value.animationSpeedForLayer(2)
            )
        ) + fadeOut(
            tween(
                settings.value.animationSpeedForLayer(2)
            )
        )
    ) {
        Box(
            modifier = Modifier

                .fillMaxWidth()
                .padding(horizontal = settings.value.padding.dp * 4)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = description,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier
                    .padding(horizontal = settings.value.padding.dp)
                    .padding(top = settings.value.padding.dp)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            )
        }
    }
}