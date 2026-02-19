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
import marcinlowercase.a.core.function.buttonSettingsForLayer
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import androidx.compose.runtime.collectAsState
import kotlin.math.roundToInt

@Composable
fun PermissionPanel(
    isUrlBarVisible: Boolean,
    // The pending request, which also controls visibility. Null means hidden.
    // Event for when the user clicks "Allow" on our panel.
    onAllow: () -> Unit,
    // Event for when the user clicks "Deny" on our panel.

    ) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val request = viewModel.pendingPermissionRequest.value
val settings = viewModel.browserSettings.collectAsState()
    var requestToShow by remember { mutableStateOf(request) }

    LaunchedEffect(request) {
        if (request != null) {
            // If there's a new request, update immediately.
            requestToShow = request
        }
    }

    AnimatedVisibility(
        visible = uiState.value.isPermissionPanelVisible,
        enter = expandVertically(animationSpec = tween(settings.value.animationSpeed.roundToInt())) + fadeIn(
            tween(settings.value.animationSpeed.roundToInt())
        ),
        exit = shrinkVertically(animationSpec = tween(settings.value.animationSpeed.roundToInt())) + fadeOut(
            tween(settings.value.animationSpeed.roundToInt())
        )
    ) {

        val currentRequest = requestToShow ?: return@AnimatedVisibility

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = settings.value.padding.dp)
                .padding(
                    bottom = if (!isUrlBarVisible) settings.value.padding.dp else 0.dp,
                    top = settings.value.padding.dp,
                )


                .background(
                    color = Color.Black.copy(0.3f),
                    shape = RoundedCornerShape(
                        settings.value.cornerRadiusForLayer(2).dp
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
            ) {
                // --- Deny Button ---
                IconButton(
                    onClick = {
                        viewModel.denyCurrentPermissionRequest()
                    },
                    modifier = Modifier
                        .buttonSettingsForLayer(
                            layer = 2,
                            settings.value,
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
                            settings.value,
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