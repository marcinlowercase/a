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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import marcinlowercase.a.R
import marcinlowercase.a.ui.component.CustomIconButton
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



            val allowString = stringResource(R.string.word_allow)
            val denyString = stringResource(R.string.word_deny)

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
            ) {
                // --- Deny Button ---

                CustomIconButton(
                    layer = 2,
                    onTap = {
                        viewModel.denyCurrentPermissionRequest()
                    },
                    buttonDescription = "$denyString ${currentRequest.title}",
                    painterId = currentRequest.iconResDeny,
                    isWhite = false,
                    modifier = Modifier.weight(1f)
                )
                CustomIconButton(
                    layer = 2,
                    onTap = onAllow,
                    buttonDescription = "$allowString ${currentRequest.title}",
                    painterId = currentRequest.iconResAllow,
                    modifier = Modifier.weight(1f)

                )
            }
        }
    }
}