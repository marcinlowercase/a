package marcinlowercase.a.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.function.buttonPointerInput
import marcinlowercase.a.core.function.buttonSettingsForLayer
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel

@Composable
fun CustomIconButton(
    modifier: Modifier = Modifier,
    isLandscape : Boolean = false,
    layer: Int,
    onTap: (() -> Unit),
    onLongPress: () -> Boolean = {
        false
    },
    textIcon: String? = null,
    buttonDescription: String,
    painterId: Int,
    isWhite: Boolean = true,

    useLongPress: Boolean = true,

    ) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()


    val  hapticFeedback = LocalHapticFeedback.current
    val sizeModifier = if (isLandscape) {
        Modifier.width(settings.value.heightForLayer(layer).dp)
    } else {
        Modifier.height(settings.value.heightForLayer(layer).dp)
    }
    Box(
        modifier = modifier

            .then(sizeModifier)

            .buttonSettingsForLayer(
                layer,
                settings.value,
                isWhite
            )

            .buttonPointerInput(
                onTap = onTap,
                hapticFeedback = hapticFeedback,
                descriptionContent = viewModel.descriptionContent,
                buttonDescription = buttonDescription,
                onLongPress = onLongPress,
                useLongPress = useLongPress,

            ),
        contentAlignment = Alignment.Center
    ) {
        if (textIcon != null) {
            // Show the Space Number
            Text(
                text = textIcon,
                fontFamily = FontFamily.Monospace,
                color = if (isWhite) Color.Black else Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                textDecoration = TextDecoration.Underline
                // Adjust font size/style as needed to match icons
            )
        } else Icon(
            modifier = Modifier
//                .rotate(currentRotation)
            ,
            painter = painterResource(id = painterId),
            contentDescription = buttonDescription,
            tint = if (isWhite) Color.Black else Color.White
        )
    }
}