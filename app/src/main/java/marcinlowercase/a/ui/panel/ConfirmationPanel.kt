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
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.ConfirmationDialogState
import marcinlowercase.a.core.function.buttonSettingsForLayer

@Composable
fun ConfirmationPanel(
    isConfirmationPanelVisible: Boolean,
    isUrlBarVisible: Boolean,
    browserSettings: MutableState<BrowserSettings>,
    state: ConfirmationDialogState?,
) {
    AnimatedVisibility(
        visible = isConfirmationPanelVisible,
        enter = expandVertically(tween(browserSettings.value.animationSpeedForLayer(1))),
        exit = shrinkVertically(tween(browserSettings.value.animationSpeedForLayer(1))) +
                fadeOut(tween(browserSettings.value.animationSpeedForLayer(1)))
    ) {

        if (state == null) return@AnimatedVisibility
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = browserSettings.value.padding.dp)
                .padding(
                    top = browserSettings.value.padding.dp,
                    bottom = if (isUrlBarVisible) 0.dp else browserSettings.value.padding.dp
                )
                .background(
                    Color.Black,
                    shape = RoundedCornerShape(
                        browserSettings.value.cornerRadiusForLayer(2).dp
                    )
                )
        ) {

            Column (
                modifier = Modifier
//                    .heightIn(min = browserSettings.value.heightForLayer(3).dp)
                    .padding(browserSettings.value.padding.dp)
                    .padding(browserSettings.value.padding.dp)
                    .padding(horizontal = browserSettings.value.cornerRadiusForLayer(3).dp)
//                    .background(Color.Cyan),


            ) {
                Text(
                    text = state.message,
//                    color = Color.Yellow,
                    color = Color(browserSettings.value.highlightColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Start
                )

                if (state.url.isNotBlank()) Text(
                    text = state.url,
//                    color = Color.Yellow,
                    color = Color(browserSettings.value.highlightColor),
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
                    .padding(browserSettings.value.padding.dp),
                horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
            ) {
                // Cancel Button
                IconButton(
                    modifier = Modifier
                        .buttonSettingsForLayer(
                            3,
                            browserSettings.value,
                            false
                        )
                        .weight(1f)
                    ,
                    shape = RoundedCornerShape(
                        browserSettings.value.cornerRadiusForLayer(3).dp
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
                            browserSettings.value,
                        )
                        .weight(1f),
                    shape = RoundedCornerShape(
                        browserSettings.value.cornerRadiusForLayer(2).dp
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