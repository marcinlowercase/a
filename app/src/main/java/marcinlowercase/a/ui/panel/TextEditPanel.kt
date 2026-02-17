package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.composition.LocalBrowserSettings

@Composable
fun TextEditPanel(
//    currentRotation: Float,
    isVisible: Boolean,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
    isPinningApp: MutableState<Boolean>,
    onDismiss: () -> Unit,
    activeWebViewTitle: String,
    descriptionContent: MutableState<String>,
) {
    val settingsController = LocalBrowserSettings.current
    val settings = settingsController.current
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            animationSpec = tween(
                settings.animationSpeedForLayer(2)
            )
        ) +
                fadeIn(tween(settings.animationSpeedForLayer(2))),
        exit = shrinkVertically(
            animationSpec = tween(
                settings.animationSpeedForLayer(2)
            )
        ) +
                fadeOut(tween(settings.animationSpeedForLayer(2)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = settings.padding.dp)
                .padding(bottom = settings.padding.dp)
                .clip(RoundedCornerShape(
                    settings.cornerRadiusForLayer(2).dp
                ))
                .padding(settings.padding.dp),
            horizontalArrangement = Arrangement.spacedBy(settings.padding.dp)
        ) {
            // Dismiss
            CustomIconButton(
//                currentRotation = currentRotation,
                layer = 3,
                modifier = Modifier.weight(1f),
                onTap = onDismiss,
                descriptionContent = descriptionContent,
                buttonDescription = "cancel",
                painterId = R.drawable.ic_arrow_back,
                isWhite = false,
            )

            if (isPinningApp.value) {
                if (activeWebViewTitle.isNotBlank()) {
                    // Edit Button (ensures keyboard is shown)
                    CustomIconButton(
//                        currentRotation = currentRotation,
                        layer = 3,
                        modifier = Modifier.weight(1f),
                        onTap = onEditClick,
                        descriptionContent = descriptionContent,
                        buttonDescription = "edit pin name",
                        painterId = R.drawable.ic_edit,

                    )
                }
            } else {
                // Copy Button
                CustomIconButton(
//                    currentRotation = currentRotation,
                    layer = 3,
                    modifier = Modifier.weight(1f),
                    onTap = onCopyClick,
                    descriptionContent = descriptionContent,
                    buttonDescription = "copy current url",
                    painterId = R.drawable.ic_content_copy,
                )


                // Edit Button (ensures keyboard is shown)
                CustomIconButton(
//                    currentRotation = currentRotation,
                    layer = 3,
                    modifier = Modifier.weight(1f),
                    onTap = onEditClick,
                    descriptionContent = descriptionContent,
                    buttonDescription = "edit current url",
                    painterId = R.drawable.ic_edit,
                )
            }
        }
    }
}