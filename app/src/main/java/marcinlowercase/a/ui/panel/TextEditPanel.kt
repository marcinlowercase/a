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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.function.buttonSettingsForLayer

@Composable
fun TextEditPanel(
    isVisible: Boolean,
    browserSettings: BrowserSettings,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
    isPinningApp: MutableState<Boolean>,
    onDismiss: () -> Unit,
    activeWebViewTitle: String,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            animationSpec = tween(
                browserSettings.animationSpeedForLayer(2)
            )
        ) +
                fadeIn(tween(browserSettings.animationSpeedForLayer(2))),
        exit = shrinkVertically(
            animationSpec = tween(
                browserSettings.animationSpeedForLayer(2)
            )
        ) +
                fadeOut(tween(browserSettings.animationSpeedForLayer(2)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = browserSettings.padding.dp)
                .padding(bottom = browserSettings.padding.dp)
                .background(
                    color = Color.Black,
                    shape = RoundedCornerShape(
                        browserSettings.cornerRadiusForLayer(3).dp
                    )
                )
                .padding(browserSettings.padding.dp),
            horizontalArrangement = Arrangement.spacedBy(browserSettings.padding.dp)
        ) {
            // Dismiss
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .buttonSettingsForLayer(
                        layer = 3,
                        browserSettings,
                        false
                    )
                    .weight(1f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Copy URL",
                    tint = Color.White
                )
            }
            if (isPinningApp.value) {
                if (activeWebViewTitle.isNotBlank()) {
                    // Edit Button (ensures keyboard is shown)
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .buttonSettingsForLayer(
                                layer = 3,
                                browserSettings,
                            )
                            .weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit URL",
                            tint = Color.Black
                        )
                    }
                }
            } else {
                // Copy Button
                IconButton(
                    onClick = onCopyClick,
                    modifier = Modifier
                        .buttonSettingsForLayer(
                            layer = 3,
                            browserSettings,
                        )
                        .weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_content_copy),
                        contentDescription = "Copy URL",
                        tint = Color.Black
                    )
                }

                // Edit Button (ensures keyboard is shown)
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .buttonSettingsForLayer(
                            layer = 3,
                            browserSettings,
                        )
                        .weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Edit URL",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}