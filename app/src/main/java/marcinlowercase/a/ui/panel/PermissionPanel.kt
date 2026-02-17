package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import marcinlowercase.a.core.data_class.CustomPermissionRequest
import marcinlowercase.a.core.function.buttonSettingsForLayer
import marcinlowercase.a.ui.composition.LocalBrowserSettings
import kotlin.math.roundToInt

@Composable
fun PermissionPanel(
    isUrlBarVisible: Boolean,
    // The pending request, which also controls visibility. Null means hidden.
    request: CustomPermissionRequest?,
    // Event for when the user clicks "Allow" on our panel.
    onAllow: () -> Unit,
    // Event for when the user clicks "Deny" on our panel.
    onDeny: () -> Unit,
    isPermissionPanelVisible: Boolean = false,

    ) {
    val settingsController = LocalBrowserSettings.current
    val settings = settingsController.current
    var requestToShow by remember { mutableStateOf(request) }

    LaunchedEffect(request) {
        if (request != null) {
            // If there's a new request, update immediately.
            requestToShow = request
        }
    }

    AnimatedVisibility(
        visible = isPermissionPanelVisible,
        enter = expandVertically(animationSpec = tween(settings.animationSpeed.roundToInt())) + fadeIn(
            tween(settings.animationSpeed.roundToInt())
        ),
        exit = shrinkVertically(animationSpec = tween(settings.animationSpeed.roundToInt())) + fadeOut(
            tween(settings.animationSpeed.roundToInt())
        )
    ) {

        val currentRequest = requestToShow ?: return@AnimatedVisibility

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = settings.padding.dp)
                .padding(
                    bottom = if (!isUrlBarVisible) settings.padding.dp else 0.dp,
                    top = settings.padding.dp,
                )


                .background(
                    color = Color.Black.copy(0.3f),
                    shape = RoundedCornerShape(
                        settings.cornerRadiusForLayer(2).dp
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(settings.padding.dp)
            ) {
                // --- Deny Button ---
                IconButton(
                    onClick = onDeny,
                    modifier = Modifier.buttonSettingsForLayer(
                        layer = 2,
                        settings,
                        white = false // false = transparent background, white border
                    )
                        .weight(1f),

                    ) {
                    Icon(
                        painter = painterResource(id = currentRequest.iconResDeny), // You can make this icon generic too
                        contentDescription = "Deny Permission",
                        tint = Color.White
                    )
                }

                // --- Allow Button ---
                IconButton(
                    onClick = onAllow,
                    modifier = Modifier
                        .buttonSettingsForLayer(
                        layer = 2,
                        settings,
                        white = true // true = white background
                    )
                        .weight(1f),
                ) {
                    Icon(
                        painter = painterResource(id = currentRequest.iconResAllow), // You can make this icon generic too
                        contentDescription = "Allow Permission",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}