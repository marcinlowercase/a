package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.ui.component.CustomIconButton

@Composable
fun TextEditPanel(
    isVisible: Boolean,
    browserSettings: MutableState<BrowserSettings>,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
    isPinningApp: MutableState<Boolean>,
    onDismiss: () -> Unit,
    activeWebViewTitle: String,
    descriptionContent: MutableState<String>,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            animationSpec = tween(
                browserSettings.value.animationSpeedForLayer(2)
            )
        ) +
                fadeIn(tween(browserSettings.value.animationSpeedForLayer(2))),
        exit = shrinkVertically(
            animationSpec = tween(
                browserSettings.value.animationSpeedForLayer(2)
            )
        ) +
                fadeOut(tween(browserSettings.value.animationSpeedForLayer(2)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = browserSettings.value.padding.dp)
                .padding(bottom = browserSettings.value.padding.dp)
                .background(
                    color = Color.Cyan,
                    shape = RoundedCornerShape(
                        browserSettings.value.cornerRadiusForLayer(2).dp
                    )
                )
                .padding(browserSettings.value.padding.dp),
            horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
        ) {
            // Dismiss
            CustomIconButton(
                layer = 3,
                browserSettings = browserSettings,
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
                        layer = 3,
                        browserSettings = browserSettings,
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
                    layer = 3,
                    browserSettings = browserSettings,
                    modifier = Modifier.weight(1f),
                    onTap = onCopyClick,
                    descriptionContent = descriptionContent,
                    buttonDescription = "copy current url",
                    painterId = R.drawable.ic_content_copy,
                )


                // Edit Button (ensures keyboard is shown)
                CustomIconButton(
                    layer = 3,
                    browserSettings = browserSettings,
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