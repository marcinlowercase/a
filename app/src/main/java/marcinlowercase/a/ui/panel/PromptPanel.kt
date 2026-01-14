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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.custom_class.CustomWebView
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.JsAlert
import marcinlowercase.a.core.data_class.JsConfirm
import marcinlowercase.a.core.data_class.JsDialogState
import marcinlowercase.a.core.data_class.JsPrompt
import marcinlowercase.a.core.function.buttonSettingsForLayer

@Composable
fun PromptPanel(
    isUrlBarVisible: Boolean,
    activeWebView: CustomWebView?,
    browserSettings: MutableState<BrowserSettings>,
    isPromptPanelVisible: Boolean,
    state: JsDialogState?,
    promptComponentDisplayState: JsDialogState?,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
//        modifier = modifier,
        visible = isPromptPanelVisible,
        enter = fadeIn(tween(browserSettings.value.animationSpeedForLayer(1))),
        exit = shrinkVertically(
            tween(
                browserSettings.value.animationSpeedForLayer(1)
            )
        ) + fadeOut(
            tween(
                browserSettings.value.animationSpeedForLayer(1)
            )
        )
    ) {
        var textInput by remember(state) {
            mutableStateOf(if (state is JsPrompt) state.defaultValue else "")
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = browserSettings.value.padding.dp)
                .padding(
                    top = browserSettings.value.padding.dp,
                    bottom = if (isUrlBarVisible) 0.dp else browserSettings.value.padding.dp
                )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = browserSettings.value.padding.dp)
                    .padding(horizontal = browserSettings.value.padding.dp * 3)
                    .clip(
                        RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(2).dp
                        )
                    )
                    .background(
                        Color.Black
                    )
//                    .border(
//                        1.dp,
//                        Color.White,
//                        shape = RoundedCornerShape(
//                            cornerRadiusForLayer(
//                                2,
//                                browserSettings.value.deviceCornerRadius,
//                                browserSettings.value.padding
//                            ).dp
//                        )
//                    )
                ,

                verticalAlignment = Alignment.CenterVertically // Keeps text aligned nicely
            ) {
                // "from" Text - Fixed Size
                Row(
                    modifier = Modifier
                        .padding(browserSettings.value.padding.dp)
                ) {
                    Text(
                        text = "from ", // Added a space for better readability
                        color = Color.White.copy(alpha = 0.7f), // Subtly de-emphasize
                        maxLines = 1, // Ensure it doesn't wrap
                    )

                    // URL Text - Scrollable and takes up remaining space
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = activeWebView?.url
                                ?: "the current page", // Safely handle null URL
                            color = Color.White,
                            maxLines = 1, // Crucial for horizontal scrolling
                            overflow = TextOverflow.Ellipsis, // Good practice, though scrolling will hide it
                            modifier = Modifier
//                                .weight(1f) // Takes all available remaining space
                                .horizontalScroll(rememberScrollState()) // THIS MAKES IT SCROLLABLE
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.Black,
                        shape = RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(2).dp
                        )
                    )
//                    .border(
//                        width = 1.dp,
//                        color = Color.White,
//                        shape = RoundedCornerShape(
//                            cornerRadiusForLayer(
//                                2,
//                                browserSettings.value.deviceCornerRadius,
//                                browserSettings.value.padding
//                            ).dp
//                        )
//                    )
            )
            {

                val textModifier = Modifier
                    .padding(browserSettings.value.padding.dp)


                Column(
                    modifier = Modifier
                        .padding(browserSettings.value.padding.dp)
                        .background(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(
                                browserSettings.value.cornerRadiusForLayer(3).dp
                            )
                        )
                ) {
                    when (promptComponentDisplayState) {
                        is JsAlert -> Text(
                            text = promptComponentDisplayState.message,
                            color = Color.White,
                            modifier = textModifier
                        )

                        is JsConfirm -> Text(
                            text = promptComponentDisplayState.message,
                            color = Color.White,
                            modifier = textModifier
                        )

                        is JsPrompt -> {
                            Text(
                                text = promptComponentDisplayState.message,
                                color = Color.White,
                                modifier = textModifier
                            )
                            Spacer(Modifier.height(browserSettings.value.padding.dp))
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                maxLines = 6, //hardcode
                                modifier = Modifier
                                    .height(IntrinsicSize.Min)
                                    .fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        activeWebView?.requestFocus()
                                        promptComponentDisplayState.onResult(textInput)
                                        onDismiss()
                                    }
                                ),
                                shape = RoundedCornerShape(
                                    browserSettings.value.cornerRadiusForLayer(3).dp
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Black.copy(0.95f), // Background when focused
                                    unfocusedContainerColor = Color.Black.copy(0.8f), // Background when unfocused
                                    cursorColor = Color.White,
                                    disabledContainerColor = Color.White, // Background when disabled
                                    errorContainerColor = Color.Red, // Background when in error state.
                                    focusedIndicatorColor = Color.White.copy(0.95f),      // Outline color when focused
                                    unfocusedIndicatorColor = Color.White.copy(0.8f),    // Outline color when unfocused
                                    disabledIndicatorColor = Color.White, // Outline color when disabled
                                    errorIndicatorColor = Color.Red,          // Outline color on error
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White.copy(0.8f),
                                )
                            )
                        }

                        null -> {

                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(browserSettings.value.padding.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        browserSettings.value.padding.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Dismiss/Cancel Button (only for confirm/prompt)
                    if (promptComponentDisplayState is JsConfirm || promptComponentDisplayState is JsPrompt) {
                        Button(
                            modifier = Modifier
                                .buttonSettingsForLayer(
                                    3,
                                    browserSettings.value,
                                    false
                                )
                                .weight(1f),
//                                .border(
//                                    1.dp, Color.White, shape = RoundedCornerShape(
//                                        cornerRadiusForLayer(
//                                            3,
//                                            browserSettings.value.deviceCornerRadius,
//                                            browserSettings.value.padding,
//                                        ).dp
//                                    )
//                                )
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black
                            ),
                            shape = RoundedCornerShape(
                                browserSettings.value.cornerRadiusForLayer(
                                    3
                                ).dp
                            ),

                            onClick = {
                                activeWebView?.requestFocus()
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
                                browserSettings.value
                            )
                            .weight(1f)
                            .background(
                                Color.White, shape = RoundedCornerShape(
                                    browserSettings.value.cornerRadiusForLayer(3).dp
                                )
                            ),
                        shape = RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(2).dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        onClick = {
                            activeWebView?.requestFocus()
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