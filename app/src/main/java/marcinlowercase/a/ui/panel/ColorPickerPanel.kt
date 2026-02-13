package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.JsColorState
import marcinlowercase.a.ui.component.CustomIconButton
import kotlin.String


// marcinlowercase.a.ui.panel.ColorPickerPanel.kt
import android.graphics.Color as AndroidColor // Alias to avoid confusion with Compose Color

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
const val sliderHeight = 16

@Composable
fun ColorPickerPanel(
    colorState: MutableState<JsColorState?>,
    browserSettings: MutableState<BrowserSettings>,
    descriptionContent: MutableState<String>,
    onDismiss: () -> Unit,
    isFocusOnTextField: MutableState<Boolean>
) {
    val state = colorState.value ?: return
    val prompt = state.prompt
    val result = state.result

    // --- 1. Core HSV States ---
    val initialHsv = remember(prompt.defaultValue) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(AndroidColor.parseColor(prompt.defaultValue), hsv)
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
            .padding(horizontal = browserSettings.value.padding.dp)
            .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(1).dp))
            .background(Color(selectedColorInt))
            .padding(browserSettings.value.padding.dp)
            .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(2).dp))

            .background(Color.Black)


    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp),
            modifier = Modifier.padding(browserSettings.value.padding.dp)
        ) {
            // --- 1. BIG PREVIEW BOX / EDITABLE TEXT FIELD ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(browserSettings.value.heightForLayer(3).dp)
                    .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(2).dp))
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
                                val parsedColor = AndroidColor.parseColor(if (filtered.startsWith("#")) filtered else "#$filtered")
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
                        .onFocusChanged({ focusState ->
                            isFocusOnTextField.value = focusState.hasFocus
                            if (focusState.hasFocus) {
                                hexText = ""
                            }
                        })
                )
            }


            AnimatedVisibility(
                visible = !isFocusOnTextField.value,

                enter = expandVertically(tween(browserSettings.value.animationSpeedForLayer(1))),
                exit = shrinkVertically(tween(browserSettings.value.animationSpeedForLayer(1)))

            ) {

                Column() {
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

            Row(horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)) {
                CustomIconButton(
                    layer = 3,
                    browserSettings = browserSettings,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        result.complete(prompt.dismiss())
                        onDismiss()
                    },
                    buttonDescription = "cancel",
                    descriptionContent = descriptionContent,
                    painterId = R.drawable.ic_close,
                    isWhite = false
                )
                CustomIconButton(
                    layer = 3,
                    browserSettings = browserSettings,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        // Return lowercase hex to Gecko
                        val finalHex = String.format("#%06x", 0xFFFFFF and selectedColorInt)
                        result.complete(prompt.confirm(finalHex))
                        onDismiss()
                    },
                    buttonDescription = "confirm",
                    descriptionContent = descriptionContent,
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