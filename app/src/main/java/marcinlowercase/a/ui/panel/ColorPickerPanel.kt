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
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import marcinlowercase.a.R
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import android.graphics.Color as AndroidColor

const val sliderHeight = 16

@Composable
fun ColorPickerPanel() {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val isFocusOnSettingTextField = uiState.value.isFocusOnSettingTextField
    val settings = viewModel.browserSettings.collectAsState()


    val state = viewModel.colorDisplayState.value ?: return
    val prompt = state.prompt
    val result = state.result

    // --- 1. Core HSV States ---
    val initialHsv = remember(prompt.defaultValue) {
        val hsv = FloatArray(3)
        // prompt.defaultValue is a String, so we use the extension function
        AndroidColor.colorToHSV(prompt.defaultValue?.toColorInt()?: 0xFFFF0000.toInt(), hsv)
        hsv
    }
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    val selectedColorInt = remember(hue, saturation, value) {
        AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
    }

    // --- 2. Hex Text State ---
    // We maintain a separate string state for the TextField so typing isn't interrupted
    var hexText by remember { mutableStateOf(String.format("#%06X", 0xFFFFFF and selectedColorInt)) }

    // Sync Text with Sliders (whenever sliders move, update the text)
    LaunchedEffect(selectedColorInt) {
        val currentHex = String.format("#%06X", 0xFFFFFF and selectedColorInt)
        // Only update text if it's significantly different (prevents cursor jumping)
        if (hexText.uppercase() != currentHex.uppercase() && hexText.length >= 7) {
            hexText = currentHex
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = settings.value.padding.dp)
            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
            .background(Color(selectedColorInt))
            .padding(settings.value.padding.dp)
            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))

            .background(Color.Black)


    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
            modifier = Modifier.padding(settings.value.padding.dp)
        ) {
            // --- 1. BIG PREVIEW BOX / EDITABLE TEXT FIELD ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(settings.value.heightForLayer(3).dp)
                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
                    .background(Color(selectedColorInt)),
                contentAlignment = Alignment.Center
            ) {
                val textColor = if (isColorDark(selectedColorInt)) Color.White else Color.Black

                BasicTextField(
                    value = hexText,
                    onValueChange = { newText ->
                        // Only allow valid hex characters and limit length
                        val filtered = newText.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it == '#' }
                        if (filtered.length <= 7) {
                            hexText = filtered

                            // Try to parse and update sliders if it's a complete hex code
                            try {
                                val parsedColor = (if (filtered.startsWith("#")) filtered else "#$filtered").toColorInt()


                                val hsv = FloatArray(3)
                                AndroidColor.colorToHSV(parsedColor, hsv)
                                hue = hsv[0]
                                saturation = hsv[1]
                                value = hsv[2]
                            } catch (_: Exception) {
                                // Ignore invalid/incomplete hex while typing
                            }
                        }
                    },
                    textStyle = TextStyle(
                        color = textColor,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(textColor),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        autoCorrectEnabled = false
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged{ focusState ->
                            viewModel.updateUI { it.copy(isFocusOnSettingTextField = focusState.hasFocus) }
                            if (focusState.hasFocus) {
                                hexText = ""
                            }
                        }
                )
            }


            AnimatedVisibility(
                visible = !isFocusOnSettingTextField,

                enter = expandVertically(tween(settings.value.animationSpeedForLayer(1))),
                exit = shrinkVertically(tween(settings.value.animationSpeedForLayer(1)))

            ) {

                Column{
                    val rainbowBrush = remember {
                        Brush.horizontalGradient(
                            listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                        )
                    }
                    Box(contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(sliderHeight.dp)
                            .clip(CircleShape)
                            .background(rainbowBrush))
                        Slider(
                            value = hue,
                            onValueChange = { hue = it },
                            valueRange = 0f..360f,
                            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.Transparent, inactiveTrackColor = Color.Transparent)
                        )
                    }

                    Slider(
                        value = saturation,
                        onValueChange = { saturation = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(selectedColorInt).copy(alpha = 1f)
                        )
                    )

                    Slider(
                        value = value,
                        onValueChange = { value = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White.copy(alpha = value)
                        )
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)) {
                CustomIconButton(
                    layer = 3,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        result.complete(prompt.dismiss())
                        viewModel.colorState.value = null
                    },
                    buttonDescription = "cancel",
                    painterId = R.drawable.ic_close,
                    isWhite = false
                )
                CustomIconButton(
                    layer = 3,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        // Return lowercase hex to Gecko
                        val finalHex = String.format("#%06x", 0xFFFFFF and selectedColorInt)
                        result.complete(prompt.confirm(finalHex))
                        viewModel.colorState.value = null
                    },
                    buttonDescription = "confirm",
                    painterId = R.drawable.ic_check,
                    isWhite = true
                )
            }
        }
    }
}

// Helper to determine text contrast
fun isColorDark(color: Int): Boolean {
    val darkness = 1 - (0.299 * AndroidColor.red(color) + 0.587 * AndroidColor.green(color) + 0.114 * AndroidColor.blue(color)) / 255
    return darkness >= 0.5
}