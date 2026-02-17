package marcinlowercase.a.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import marcinlowercase.a.ui.composition.LocalBrowserSettings

@Composable
fun LoadingIndicator(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {

    val settings = LocalBrowserSettings.current.current

    // Animate the appearance and disappearance of the overlay.
    AnimatedVisibility(
        visible = isLoading,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .padding(settings.padding.dp)
                .clip(
                    RoundedCornerShape(
                        settings.cornerRadiusForLayer(1).dp
                    )
                )
                .size(settings.heightForLayer(1).dp)
                .background(
                    Color.Black.copy(alpha = 0.5f),
                )


        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(settings.padding.dp)
                    .size(settings.heightForLayer(1).dp),
                // Use a contrasting color that works well on the dark scrim.
                color = Color.White,
                strokeWidth = settings.padding.dp
            )
        }
    }
}