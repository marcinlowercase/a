package marcinlowercase.a.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import marcinlowercase.a.core.function.buttonPointerInput
import marcinlowercase.a.core.function.buttonSettingsForLayer
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel

@Composable
fun CustomTextButton(
    modifier: Modifier = Modifier,
    otherColor: Color = Color.Transparent,
    containerColor: Color? = null,
    contentColor: Color? = null,
    isLandscape: Boolean = false,
    layer: Int,
    onTap: (() -> Unit),
    onLongPress: () -> Boolean = { false },
    text: String,
    buttonDescription: String = text,
    isWhite: Boolean = true,
    useLongPress: Boolean = true,
    isSquare: Boolean = false,
    fontSize: TextUnit = 14.sp,
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current
    val speed = settings.value.animationSpeedForLayer(layer)

    val baseBgColor = if (otherColor != Color.Transparent) {
        otherColor
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val targetBackgroundColor = when {
        containerColor != null -> containerColor
        isWhite || otherColor != Color.Transparent -> baseBgColor
        else -> baseBgColor.copy(alpha = 0f)
    }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(speed),
        label = "TextButtonBackground"
    )

    val targetContentColor = when {
        contentColor != null -> contentColor
        otherColor != Color.Transparent && settings.value.isMaterialYou() -> MaterialTheme.colorScheme.onPrimary
        isWhite -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val animatedContentColor by animateColorAsState(
        targetValue = targetContentColor,
        animationSpec = tween(speed),
        label = "TextButtonContent"
    )

    val sizeModifier = if (isSquare) {
        Modifier
            .width(settings.value.heightForLayer(layer).dp)
            .height(settings.value.heightForLayer(layer).dp)
    } else if (isLandscape) {
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
                animatedBackgroundColor
            )
            .buttonPointerInput(
                onTap = onTap,
                hapticFeedback = hapticFeedback,
                descriptionContent = viewModel.descriptionContent,
                buttonDescription = buttonDescription,
                onLongPress = onLongPress,
                useLongPress = useLongPress,
            )
            .background(otherColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = animatedContentColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = fontSize,
            maxLines = 1
        )
    }
}