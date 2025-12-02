package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.ConfirmationDialogState
import marcinlowercase.a.core.function.buttonSettingsForLayer

@Composable
fun ConfirmationPanel(
    isConfirmationPanelVisible: Boolean,
    isUrlBarVisible: Boolean,
    browserSettings: BrowserSettings,
    state: ConfirmationDialogState?,
) {
    AnimatedVisibility(
        visible = isConfirmationPanelVisible,
        enter = expandVertically(tween(browserSettings.animationSpeedForLayer(1))),
        exit = shrinkVertically(tween(browserSettings.animationSpeedForLayer(1))) +
                fadeOut(tween(browserSettings.animationSpeedForLayer(1)))
    ) {

        if (state == null) return@AnimatedVisibility
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = browserSettings.padding.dp)
                .padding(
                    top = browserSettings.padding.dp,
                    bottom = if (isUrlBarVisible) 0.dp else browserSettings.padding.dp
                )
                .background(
                    Color.Black,
                    shape = RoundedCornerShape(
                       browserSettings.cornerRadiusForLayer(2).dp
                    )
                )
//                .border(
//                    width = 1.dp,
//                    color = Color.White,
//                    shape = RoundedCornerShape(
//                        cornerRadiusForLayer(
//                            2,
//                            browserSettings.deviceCornerRadius,
//                            browserSettings.padding
//                        ).dp
//                    )
//                )
        ) {

            Text(
                text = state.message,
                color = Color.Yellow,
                modifier = Modifier
                    .padding(browserSettings.padding.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(browserSettings.padding.dp),
                horizontalArrangement = Arrangement.spacedBy(browserSettings.padding.dp)
            ) {
                // Cancel Button
                IconButton(
                    modifier = Modifier
                        .buttonSettingsForLayer(
                        3,
                       browserSettings,
                        false
                    )
                        .weight(1f)
                    ,
                    shape = RoundedCornerShape(
                        browserSettings.cornerRadiusForLayer(3).dp
                    ),
                    onClick = state.onCancel
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        tint = Color.White,
                        contentDescription = "Cancel",
                    )
                }

                // Confirm Button
                IconButton(
                    modifier = Modifier
                        .buttonSettingsForLayer(
                        3,
                        browserSettings,
                    )
                        .weight(1f),
                    shape = RoundedCornerShape(
                        browserSettings.cornerRadiusForLayer(2).dp
                    ),
                    onClick = state.onConfirm
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        tint = Color.Black,
                        contentDescription = "Confirm",
                    )
                }
            }
        }
    }
}