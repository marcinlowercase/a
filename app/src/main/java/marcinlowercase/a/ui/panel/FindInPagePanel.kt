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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun FindInPagePanel(
//    currentRotation: Float,
    isVisible: Boolean,

    onSearchTextChanged: (String) -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onClose: () -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ) + fadeIn(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ),
        exit = shrinkVertically(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ) + fadeOut(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        )
    ) {

        Column(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        settings.value.cornerRadiusForLayer(1).dp
                    )
                )
                .padding(horizontal = settings.value.padding.dp)
                .padding(top = settings.value.padding.dp)

        ) {
            TextField(
                value = viewModel.findInPageText.value,
                onValueChange = onSearchTextChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(2).dp
                        )
                    )
                    .heightIn(
                        min = settings.value.heightForLayer(2).dp
                    )
                    .onFocusChanged { focusState ->
                        viewModel.updateUI { it.copy(isFocusOnFindTextField = focusState.isFocused) }
                    },
                shape = RoundedCornerShape(
                    settings.value.cornerRadiusForLayer(2).dp
                ),
                placeholder = { Text(stringResource(R.string.ui_find_in_page)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,

                    // 3. This is the key to removing the underline
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = settings.value.padding.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
            ) {

                CustomIconButton(
                    layer = 2,
                    modifier = Modifier.weight(1f),
                    onTap = onClose,

                    buttonDescription = stringResource(R.string.desc_cancel),
                    painterId = R.drawable.ic_arrow_back,
                    isWhite = false,
                )
                CustomIconButton(
                    layer = 2,
                    modifier = Modifier.weight(1f),
                    onTap = onFindNext,

                    buttonDescription = stringResource(R.string.desc_find_next),
                    painterId = R.drawable.ic_arrow_downward,
                    isWhite = viewModel.findInPageResult.value.second > 0,
                )



                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${viewModel.findInPageResult.value.first}/${viewModel.findInPageResult.value.second}",
                        color = Color.White,

                        )
                }
                CustomIconButton(
                    layer = 2,
                    modifier = Modifier.weight(1f),
                    onTap = onFindPrevious,

                    buttonDescription = stringResource(R.string.desc_find_previous),
                    painterId = R.drawable.ic_arrow_upward,
                    isWhite = viewModel.findInPageResult.value.second > 0,
                )
            }
        }
    }
}