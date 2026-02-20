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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun TextEditPanel(
//    currentRotation: Float,
    isVisible: Boolean,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
    onDismiss: () -> Unit,
    activeWebViewTitle: String,
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            animationSpec = tween(
                settings.value.animationSpeedForLayer(2)
            )
        ) +
                fadeIn(tween(settings.value.animationSpeedForLayer(2))),
        exit = shrinkVertically(
            animationSpec = tween(
                settings.value.animationSpeedForLayer(2)
            )
        ) +
                fadeOut(tween(settings.value.animationSpeedForLayer(2)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = settings.value.padding.dp)
                .padding(bottom = settings.value.padding.dp)
                .clip(RoundedCornerShape(
                    settings.value.cornerRadiusForLayer(2).dp
                ))
                .padding(settings.value.padding.dp),
            horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
        ) {
            // Dismiss
            CustomIconButton(
//                currentRotation = currentRotation,
                layer = 3,
                modifier = Modifier.weight(1f),
                onTap = onDismiss,
                
                buttonDescription = "cancel",
                painterId = R.drawable.ic_arrow_back,
                isWhite = false,
            )

            if (uiState.value.isPinningApp) {
                if (activeWebViewTitle.isNotBlank()) {
                    // Edit Button (ensures keyboard is shown)
                    CustomIconButton(
//                        currentRotation = currentRotation,
                        layer = 3,
                        modifier = Modifier.weight(1f),
                        onTap = onEditClick,
                        
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
                    
                    buttonDescription = "copy current url",
                    painterId = R.drawable.ic_content_copy,
                )


                // Edit Button (ensures keyboard is shown)
                CustomIconButton(
//                    currentRotation = currentRotation,
                    layer = 3,
                    modifier = Modifier.weight(1f),
                    onTap = onEditClick,
                    
                    buttonDescription = "edit current url",
                    painterId = R.drawable.ic_edit,
                )
            }
        }
    }
}