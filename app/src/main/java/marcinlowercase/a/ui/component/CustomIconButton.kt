package marcinlowercase.a.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.function.buttonPointerInput
import marcinlowercase.a.core.function.buttonSettingsForLayer
import marcinlowercase.a.ui.composition.LocalBrowserSettings

@Composable
fun CustomIconButton(
    modifier: Modifier = Modifier,
    isLandscape : Boolean = false,
    layer: Int,
    onTap: (() -> Unit),
    onLongPress: () -> Boolean = {
        false
    },
    descriptionContent: MutableState<String>,
    buttonDescription: String,
    painterId: Int,
    isWhite: Boolean = true,

    useLongPress: Boolean = true,

    ) {
    val settingsController = LocalBrowserSettings.current
    val settings = settingsController.current

    val  hapticFeedback = LocalHapticFeedback.current
    val sizeModifier = if (isLandscape) {
        Modifier.width(settings.heightForLayer(layer).dp)
    } else {
        Modifier.height(settings.heightForLayer(layer).dp)
    }
    Box(
        modifier = modifier

            .then(sizeModifier)

            .buttonSettingsForLayer(
                layer,
                settings,
                isWhite
            )

            .buttonPointerInput(
                onTap = onTap,
                hapticFeedback = hapticFeedback,
                descriptionContent = descriptionContent,
                buttonDescription = buttonDescription,
                onLongPress = onLongPress,
                useLongPress = useLongPress,

            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier
//                .rotate(currentRotation)
            ,
            painter = painterResource(id = painterId),
            contentDescription = buttonDescription,
            tint = if (isWhite) Color.Black else Color.White
        )
    }
}