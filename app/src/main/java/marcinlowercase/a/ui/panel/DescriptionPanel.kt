package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import marcinlowercase.a.core.data_class.BrowserSettings

@Composable
fun DescriptionPanel(
    isVisible: Boolean,
    description: String,
    browserSettings: BrowserSettings,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            tween(
                browserSettings.animationSpeedForLayer(2)
            )
        ) + fadeIn(
            tween(
                browserSettings.animationSpeedForLayer(2)
            )
        ),
        exit = shrinkVertically(
            tween(
                browserSettings.animationSpeedForLayer(2)
            )
        ) + fadeOut(
            tween(
                browserSettings.animationSpeedForLayer(2)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .clickable(onClick = onDismiss)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = description,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = browserSettings.padding.dp)
                    .padding(top = browserSettings.padding.dp)
                    .fillMaxWidth()
            )
        }
    }
}