package studio.oo1.browser.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import studio.oo1.browser.core.data_class.BrowserSettings
import studio.oo1.browser.core.function.buttonPointerInput
import studio.oo1.browser.core.function.buttonSettingsForLayer

@Composable
fun CustomIconButton(
    layer: Int,
    browserSettings: MutableState<BrowserSettings>,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit),
    descriptionContent: MutableState<String>,
    buttonDescription: String,
    painterId: Int,
    isWhite: Boolean = true,


    ) {
    val  hapticFeedback = LocalHapticFeedback.current
    Box(
        modifier = modifier

            .height(
                browserSettings.value.heightForLayer(layer).dp
            )
            .buttonSettingsForLayer(
                layer,
                browserSettings.value,
                isWhite
            )

            .buttonPointerInput(
                onTap = onTap,
                hapticFeedback = hapticFeedback,
                descriptionContent = descriptionContent,
                buttonDescription = buttonDescription

            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = painterId),
            contentDescription = buttonDescription,
            tint = if (isWhite) Color.Black else Color.White
        )
    }
}