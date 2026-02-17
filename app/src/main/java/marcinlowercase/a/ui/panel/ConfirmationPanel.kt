package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.ConfirmationDialogState
import marcinlowercase.a.core.function.buttonSettingsForLayer
import marcinlowercase.a.ui.composition.LocalBrowserSettings

@Composable
fun ConfirmationPanel(
    isConfirmationPanelVisible: Boolean,
    isUrlBarVisible: Boolean,
    state: ConfirmationDialogState?,
) {
    val settingsController = LocalBrowserSettings.current
    val settings = settingsController.current
    AnimatedVisibility(
        visible = isConfirmationPanelVisible,
        enter = expandVertically(tween(settings.animationSpeedForLayer(1))),
        exit = shrinkVertically(tween(settings.animationSpeedForLayer(1))) +
                fadeOut(tween(settings.animationSpeedForLayer(1)))
    ) {

        if (state == null) return@AnimatedVisibility
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = settings.padding.dp)
                .padding(
                    top = settings.padding.dp,
                    bottom = if (isUrlBarVisible) 0.dp else settings.padding.dp
                )
                .background(
                    Color.Black,
                    shape = RoundedCornerShape(
                        settings.cornerRadiusForLayer(2).dp
                    )
                )
        ) {

            Column (
                modifier = Modifier
//                    .heightIn(min = settings.heightForLayer(3).dp)
                    .padding(settings.padding.dp)
                    .padding(settings.padding.dp)
                    .padding(horizontal = settings.cornerRadiusForLayer(3).dp)
//                    .background(Color.Cyan),


            ) {
                Text(
                    text = state.message,
//                    color = Color.Yellow,
                    color = Color(settings.highlightColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Start
                )

                if (state.url.isNotBlank()) Text(
                    text = state.url,
//                    color = Color.Yellow,
                    color = Color(settings.highlightColor),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .horizontalScroll(rememberScrollState())
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(settings.padding.dp),
                horizontalArrangement = Arrangement.spacedBy(settings.padding.dp)
            ) {
                // Cancel Button
                IconButton(
                    modifier = Modifier
                        .buttonSettingsForLayer(
                            3,
                            settings,
                            false
                        )
                        .weight(1f)
                    ,
                    shape = RoundedCornerShape(
                        settings.cornerRadiusForLayer(3).dp
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
                            settings,
                        )
                        .weight(1f),
                    shape = RoundedCornerShape(
                        settings.cornerRadiusForLayer(2).dp
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