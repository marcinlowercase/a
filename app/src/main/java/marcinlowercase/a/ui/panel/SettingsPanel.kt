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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.delay
import marcinlowercase.a.R
import marcinlowercase.a.core.constant.privacy_policy_url
import marcinlowercase.a.core.data_class.OptionItem
import marcinlowercase.a.core.enum_class.BrowserOption
import marcinlowercase.a.core.enum_class.BrowserSettingField
import marcinlowercase.a.core.enum_class.SearchEngine
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor
//
//enum class SettingPanelView {
//    MAIN, CORNER_RADIUS, PADDING, ANIMATION_SPEED, CURSOR_CONTAINER_SIZE,
//    CURSOR_TRACKING_SPEED, BACK_SQUARE_OPACITY, DEFAULT_URL, INFO,
//    CLOSED_TAB_HISTORY_SIZE, MAX_LIST_HEIGHT, SEARCH_ENGINE,
//    SINGLE_LINE_HEIGHT, HIGHLIGHT_COLOR,
//}

@Composable
fun SliderSetting(
    textEnabled: Boolean = true,
    field: BrowserSettingField,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onBackClick: () -> Unit,
    textFieldValueFun: (String) -> String,
    storeValueFun: (String) -> Float = {digits ->
        textFieldValueFun(digits).toFloatOrNull() ?: 0f
    },
    afterDecimal: Boolean = true,
    iconID: Int,
    digitCount: Int = 4,
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()

    val currentSettingOriginalValue = remember(settings, field) {
        when (field) {
            BrowserSettingField.CORNER_RADIUS -> settings.value.deviceCornerRadius
            BrowserSettingField.PADDING -> settings.value.padding
            BrowserSettingField.ANIMATION_SPEED -> settings.value.animationSpeed
            BrowserSettingField.SINGLE_LINE_HEIGHT -> settings.value.singleLineHeight
            BrowserSettingField.CURSOR_CONTAINER_SIZE -> settings.value.cursorContainerSize
            BrowserSettingField.CURSOR_TRACKING_SPEED -> settings.value.cursorTrackingSpeed
            BrowserSettingField.CLOSED_TAB_HISTORY_SIZE -> settings.value.closedTabHistorySize
            BrowserSettingField.BACK_SQUARE_OPACITY -> settings.value.backSquareIdleOpacity
            BrowserSettingField.MAX_LIST_HEIGHT -> settings.value.maxListHeight
            BrowserSettingField.SEARCH_ENGINE -> settings.value.searchEngine.toFloat()
            BrowserSettingField.MEMORY_USAGE -> settings.value.memoryUsage.toFloat()
            else -> 0f
        }
    }


    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var digits by remember {
        mutableStateOf(
            ((currentSettingOriginalValue * if (afterDecimal) 100f else 1f).roundToInt())
                .toString()
                .padStart(digitCount, '0')

        )
    }


    // 1. The raw digits are the single source of truth.
    // Initialize it from the global browserSettings.value.


    val sliderValue = (digits.toIntOrNull() ?: 0) / if (afterDecimal) 100f else 1f


    val commitTextFieldValue = {
        val parsedValue = storeValueFun(digits)
        val coercedValue = parsedValue.coerceIn(valueRange)

        // Update the global settings with the coerced value.
        viewModel.updateField(field, coercedValue)

        // CRUCIAL: Update the 'digits' state based on the coerced value.
        // This forces the TextField to display the corrected number (e.g., "60.00").
        digits = ((coercedValue * if (afterDecimal) 100 else 1).roundToInt())
            .toString()
            .padStart(digitCount, '0')
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(
                    settings.value.cornerRadiusForLayer(2).dp
                )
            )
            .padding(settings.value.padding.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    settings.value.heightForLayer( 3).dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Back button to return to the main settings view

            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(3).dp
                        )
                    )
                    .fillMaxHeight()
                    .background(Color.White)
                    .defaultMinSize(
                        minWidth = settings.value.heightForLayer(3).dp
                    )


            ) {
                Icon(
                    painter = painterResource(id = if(settings.value.isFirstAppLoad) R.drawable.ic_check else R.drawable.ic_arrow_back),
                    contentDescription = "Back to Settings",
                    tint = Color.Black
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center // 2. Center the content of the Box
            ) {
                BasicTextField(
                    enabled = textEnabled,
                    value = textFieldValueFun(digits),
                    onValueChange = {},
                    modifier = Modifier

                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyUp) {
                                // --- THIS IS THE CORRECTED LOGIC ---

                                // 1. Get the UniCode character as an Int.
                                val unicodeChar = event.nativeKeyEvent.unicodeChar

                                // 2. Check if it's a valid character (not 0) and then convert it to a Char.
                                if (unicodeChar != 0) {
                                    val typedChar = unicodeChar.toChar()

                                    // 3. Now, safely call digitToIntOrNull() on the Char.
                                    val digit = typedChar.digitToIntOrNull()

                                    if (digit != null) {
                                        // Append new digit and keep the last 4 characters.
                                        digits = (digits + digit.toString()).takeLast(digitCount)
                                        return@onKeyEvent true // Event handled
                                    }
                                }
                                // --- END OF CORRECTION ---

                                // Check for the Backspace key
                                if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DEL) {
                                    digits = ("0$digits").take(digitCount)
                                    return@onKeyEvent true // Event handled
                                }
                            }
                            false // Event not handled
                        }
                        .onFocusChanged {focusState ->
                            viewModel.updateUI { it.copy(isFocusOnSettingTextField = focusState.hasFocus) }

                            commitTextFieldValue()
                        },
                    cursorBrush = SolidColor(Color.Transparent),

                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, // Show the number pad
                        imeAction = ImeAction.Done // Show a "Done" button
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            commitTextFieldValue()
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true
                )
            }
            IconButton(
                onClick = { },
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(3).dp
                        )
                    )
                    .fillMaxHeight()
                    .defaultMinSize(
                        minWidth = settings.value.heightForLayer(3).dp
                    )


            ) {
                Icon(
                    painter = painterResource(id = iconID),
                    contentDescription = "Back to Settings",
                    tint = Color.White
                )
            }
        }

        Slider(
            value = sliderValue,
            onValueChange = { newSliderValue ->

                val finalValue = newSliderValue.coerceIn(valueRange)

                // 2. Update the digits string based on this final, clean value.
                digits = ((finalValue * if (afterDecimal) 100 else 1).roundToInt())
                    .toString()
                    .padStart(digitCount, '0')

                // 3. Immediately pass the NEW, CORRECT finalValue to your update function.
                viewModel.updateField(field, finalValue)
            },
            valueRange = valueRange,
            steps = steps,

            modifier = Modifier
                .fillMaxWidth()
                .padding(top = settings.value.padding.dp)
                .heightIn(
                    min = settings.value.heightForLayer(3).dp
                )
                .padding(settings.value.padding.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.Black,
                inactiveTickColor = Color.White,
                activeTickColor = Color.Black,


                )
        )

    }
}
@Composable
fun TextSetting(
    onBackClick: () -> Unit,
    iconID: Int,
    currentSettingOriginalValue: String,
    field: BrowserSettingField,
) {

    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // State to hold the text being edited.
    var textValue by remember { mutableStateOf(currentSettingOriginalValue) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(
                    settings.value.cornerRadiusForLayer(2).dp
                )
            )
            .padding(settings.value.padding.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- TOP ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    settings.value.heightForLayer(3).dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button (same as before)
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(3).dp
                        )
                    )
                    .fillMaxHeight()
                    .background(Color.White)
                    .defaultMinSize(
                        minWidth = settings.value.heightForLayer(3).dp
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back to Settings",
                    tint = Color.Black
                )
            }

            // --- SPACER ---
            Spacer(modifier = Modifier.weight(1f))

            // Right Icon (same as before)
            IconButton(
                onClick = { /* No action */ },
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(3).dp
                        )
                    )
                    .fillMaxHeight()
                    .defaultMinSize(
                        minWidth = settings.value.heightForLayer(3).dp
                    )
            ) {
                Icon(
                    painter = painterResource(id = iconID),
                    contentDescription = "Setting Icon",
                    tint = Color.White
                )
            }
        }

        // --- OutlinedTextField (replaces Slider) ---
        TextField(
            value = textValue,
            onValueChange = { textValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = settings.value.padding.dp)
                .heightIn(
                    min = settings.value.heightForLayer(3).dp
                )
                .onFocusChanged{ focusState ->
                    viewModel.updateUI { it.copy(isFocusOnSettingTextField = focusState.hasFocus) }
                },
            shape = RoundedCornerShape(
                settings.value.cornerRadiusForLayer(3).dp
            ),
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri, // Good for URLs
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // APPLY URL BAR LOGIC FOR DEFAULT_URL
                    val finalValue = if (field == BrowserSettingField.DEFAULT_URL) {
                        val input = textValue.trim()

                        if (input.isEmpty()) {
                            "about:blank" // Let it be empty if user clears it
                        } else {
                            val isUrl = try {
                                android.util.Patterns.WEB_URL.matcher(input).matches() ||
                                        (input.contains(".") && !input.contains(" "))
                                        && !input.endsWith(".")
                                        && !input.startsWith(".")
                            } catch (_: Exception) {
                                false
                            }

                            if (isUrl) {
                                if (input.startsWith("http://") || input.startsWith("https://")) {
                                    input
                                } else {
                                    "https://$input"
                                }
                            } else {
                                val encodedQuery = java.net.URLEncoder.encode(
                                    input,
                                    java.nio.charset.StandardCharsets.UTF_8.toString()
                                )
                                SearchEngine.entries[settings.value.searchEngine].getSearchUrl(encodedQuery)
                            }
                        }
                    } else {
                        textValue
                    }

                    // Update the TextField visually so the user sees the generated web search URL/scheme
                    textValue = finalValue

                    // Update global settings
                    viewModel.updateField(field, finalValue)

                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            singleLine = true
        )
    }
}