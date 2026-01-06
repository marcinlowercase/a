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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.CustomPermissionRequest
import marcinlowercase.a.core.function.buttonSettingsForLayer
import kotlin.math.roundToInt

@Composable
fun PermissionPanel(
    isUrlBarVisible: Boolean,
    browserSettings: MutableState<BrowserSettings>,
    // The pending request, which also controls visibility. Null means hidden.
    request: CustomPermissionRequest?,
    // Event for when the user clicks "Allow" on our panel.
    onAllow: () -> Unit,
    // Event for when the user clicks "Deny" on our panel.
    onDeny: () -> Unit,
    isPermissionPanelVisible: Boolean = false,

    ) {
    var requestToShow by remember { mutableStateOf(request) }

    LaunchedEffect(request) {
        if (request != null) {
            // If there's a new request, update immediately.
            requestToShow = request
        }
    }

    AnimatedVisibility(
        visible = isPermissionPanelVisible,
        enter = expandVertically(animationSpec = tween(browserSettings.value.animationSpeed.roundToInt())) + fadeIn(
            tween(browserSettings.value.animationSpeed.roundToInt())
        ),
        exit = shrinkVertically(animationSpec = tween(browserSettings.value.animationSpeed.roundToInt())) + fadeOut(
            tween(browserSettings.value.animationSpeed.roundToInt())
        )
    ) {

        val currentRequest = requestToShow ?: return@AnimatedVisibility

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = browserSettings.value.padding.dp)
                .padding(
                    bottom = if (!isUrlBarVisible) browserSettings.value.padding.dp else 0.dp,
                    top = browserSettings.value.padding.dp,
                )


                .background(
                    color = Color.Black.copy(0.3f),
                    shape = RoundedCornerShape(
                        browserSettings.value.cornerRadiusForLayer(2).dp
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
            ) {
                // --- Deny Button ---
                IconButton(
                    onClick = onDeny,
                    modifier = Modifier.buttonSettingsForLayer(
                        layer = 2,
                        browserSettings.value,
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
                        browserSettings.value,
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