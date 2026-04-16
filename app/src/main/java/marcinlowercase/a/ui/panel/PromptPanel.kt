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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.JsAlert
import marcinlowercase.a.core.data_class.JsConfirm
import marcinlowercase.a.core.data_class.JsDialogState
import marcinlowercase.a.core.data_class.JsPrompt
import marcinlowercase.a.core.function.buttonSettingsForLayer
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import org.mozilla.geckoview.GeckoView
import kotlin.math.ceil

@Composable
fun PromptPanel(
    geckoViewRef: MutableState<GeckoView?>,
    isUrlBarVisible: Boolean,
    state: JsDialogState?,
    onDismiss: () -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()
    AnimatedVisibility(
//        modifier = modifier,
        visible = uiState.value.isPromptPanelVisible,
        enter = fadeIn(tween(settings.value.animationSpeedForLayer(1))),
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
        val displayState = viewModel.jsDialogDisplayState.value ?: return@AnimatedVisibility
        var textInput by remember(state) {
            mutableStateOf(if (state is JsPrompt) state.defaultValue else "")
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = settings.value.padding.dp)
                .padding(
                    top = settings.value.padding.dp,
                    bottom = if (isUrlBarVisible) 0.dp else settings.value.padding.dp
                )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = settings.value.padding.dp)
                    .padding(horizontal = settings.value.padding.dp)
                    .padding(horizontal = settings.value.cornerRadiusForLayer(2).dp)
                    .clip(
                        RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(2).dp
                        )
                    )
                    .background(
                        Color.Black
                    ),

                verticalAlignment = Alignment.CenterVertically // Keeps text aligned nicely
            ) {
                // "from" Text - Fixed Size
                Text(
                    text = "${stringResource(R.string.word_from)} ", // Added a space for better readability
                    color = Color.White.copy(alpha = 0.7f), // Subtly de-emphasize
                    maxLines = 1, // Ensure it doesn't wrap
                )

                // URL Text - Scrollable and takes up remaining space
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = viewModel.activeTab!!.currentURL, // Safely handle null URL
                        color = Color.White,
                        maxLines = 1, // Crucial for horizontal scrolling
                        overflow = TextOverflow.Ellipsis, // Good practice, though scrolling will hide it
                        modifier = Modifier
//                                .weight(1f) // Takes all available remaining space
                            .horizontalScroll(rememberScrollState()) // THIS MAKES IT SCROLLABLE
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.Black,
                        shape = RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(2).dp
                        )
                    )
            )
            {

                val textModifier = Modifier
                    .padding(vertical = settings.value.padding.dp)
                    .padding(horizontal = settings.value.padding.dp)
                    .padding(horizontal = settings.value.cornerRadiusForLayer(3).dp)


                Column(
                    modifier = Modifier
                        .padding(settings.value.padding.dp)
                        .clip(
                            RoundedCornerShape(
                                settings.value.cornerRadiusForLayer(3).dp
                            )
                        )
                        .heightIn(
                            settings.value.heightForLayer(3).dp
                        )
                        .background(
                            color = Color.White,
                        ),
                    verticalArrangement = Arrangement.Center
                ) {
                    when (displayState) {
                        is JsAlert -> Text(
                            text = displayState.message,
                            color = Color.Black,
                            modifier = textModifier
                        )

                        is JsConfirm -> Text(
                            text = displayState.message,
                            color = Color.Black,
                            modifier = textModifier
                        )

                        is JsPrompt -> {
                            Text(
                                text = displayState.message,
                                color = Color.Black,
                                modifier = textModifier
                            )
                            BasicTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                maxLines = ceil(settings.value.maxListHeight).toInt(),
                                modifier = Modifier
                                    // 1. External padding (margins)
                                    .padding(horizontal = settings.value.padding.dp)
                                    .padding(bottom = settings.value.padding.dp)
                                    // 2. Sizing
                                    .fillMaxWidth()
                                    .heightIn(min = settings.value.heightForLayer(4).dp)
                                    // 3. Container background and shape (replaces TextFieldDefaults container colors)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(
                                            settings.value.cornerRadiusForLayer(
                                                4
                                            ).dp
                                        )
                                    ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        geckoViewRef.value?.requestFocus()
                                        displayState.onResult(textInput)
                                        onDismiss()
                                    }
                                ),
                                // Text and cursor styling
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.Black
                                ),
                                cursorBrush = SolidColor(Color.Black),

                                // 4. The decorationBox allows us to define the vertical centering
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            // Internal padding inside the text field
                                            .padding(horizontal = 16.dp, vertical = 8.dp),

                                        // THIS is the magic property that centers the text vertically!
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(settings.value.padding.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        settings.value.padding.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Dismiss/Cancel Button (only for confirm/prompt)
                    if (displayState is JsConfirm || displayState is JsPrompt) {
                        Button(
                            modifier = Modifier
                                .buttonSettingsForLayer(
                                    3,
                                    settings.value,
                                    false
                                )
                                .weight(1f),
//                                .border(
//                                    1.dp, Color.White, shape = RoundedCornerShape(
//                                        cornerRadiusForLayer(
//                                            3,
//                                            settings.value.deviceCornerRadius,
//                                            settings.value.padding,
//                                        ).dp
//                                    )
//                                )
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black
                            ),
                            shape = RoundedCornerShape(
                                settings.value.cornerRadiusForLayer(
                                    3
                                ).dp
                            ),

                            onClick = {
                                geckoViewRef.value?.requestFocus()
                                when (state) {
                                    is JsConfirm -> state.onResult(false)
                                    is JsPrompt -> state.onResult(null)
                                    else -> {}
                                }
                                onDismiss()
                            },

                            ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                tint = Color.White,
                                contentDescription = "Dismiss",
                            )
                        }
                    }


                    // Confirm Button
                    Button(
                        modifier = Modifier
                            .buttonSettingsForLayer(
                                3,
                                settings.value
                            )
                            .weight(1f)
                            .background(
                                Color.White, shape = RoundedCornerShape(
                                    settings.value.cornerRadiusForLayer(3).dp
                                )
                            ),
                        shape = RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(2).dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        onClick = {
                            geckoViewRef.value?.requestFocus()
                            when (state) {
                                is JsAlert -> { /* Just dismiss */
                                }

                                is JsConfirm -> state.onResult(true)
                                is JsPrompt -> state.onResult(textInput)
                                null -> {

                                }
                            }
                            onDismiss()
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "Confirm",
                            tint = Color.Black
                        )
                    }
                }


            }


        }

    }
}