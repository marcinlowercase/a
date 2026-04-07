/*
 * Copyright (C) 2026 marcinlowercase
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.function.buttonSettingsForLayer
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel

@Composable
fun ConfirmationPanel() {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()
    AnimatedVisibility(
        visible = viewModel.confirmationState.value != null,
        enter = expandVertically(tween(settings.value.animationSpeedForLayer(1))),
        exit = shrinkVertically(tween(settings.value.animationSpeedForLayer(1))) +
                fadeOut(tween(settings.value.animationSpeedForLayer(1)))
    ) {

        val state = viewModel.confirmationDisplayState.value ?: return@AnimatedVisibility
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = settings.value.padding.dp)
                .padding(
                    top = settings.value.padding.dp,
                    bottom = if (uiState.value.isUrlBarVisible) 0.dp else settings.value.padding.dp
                )
                .clip(RoundedCornerShape(
                    settings.value.cornerRadiusForLayer(2).dp
                ))

        ) {

            Column(
                modifier = Modifier
                    .padding(top = settings.value.padding.dp)
                    .padding(horizontal = settings.value.padding.dp)
                    .heightIn(min = settings.value.heightForLayer(3).dp)
                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp))
                    .background(Color(settings.value.backgroundForHighlightText()))
                    .padding(vertical = settings.value.padding.dp)
                    .padding(horizontal = settings.value.padding.dp)
                    .padding(horizontal = settings.value.cornerRadiusForLayer(3).dp),
                verticalArrangement = Arrangement.Center

            ) {
                Text(
                    text = stringResource(state.message),
                    color = Color(settings.value.highlightColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Start
                )

                if (state.url.isNotBlank()) Text(
                    text = state.url,
//                    color = Color.Yellow,
                    color = Color(settings.value.highlightColor),
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
                    .padding(settings.value.padding.dp),
                horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
            ) {
                // Cancel Button
                IconButton(
                    modifier = Modifier
                        .buttonSettingsForLayer(
                            3,
                            settings.value,
                            false
                        )
                        .weight(1f),
                    shape = RoundedCornerShape(
                        settings.value.cornerRadiusForLayer(3).dp
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
                            settings.value,
                        )
                        .weight(1f),
                    shape = RoundedCornerShape(
                        settings.value.cornerRadiusForLayer(2).dp
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