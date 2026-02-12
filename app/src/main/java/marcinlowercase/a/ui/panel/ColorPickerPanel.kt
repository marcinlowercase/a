package marcinlowercase.a.ui.panel

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

@Composable
fun ColorPickerPanel(
    colorState: MutableState<JsColorState?>,
    browserSettings: MutableState<BrowserSettings>,
    descriptionContent: MutableState<String>,
    onDismiss: () -> Unit
) {
    val state = colorState.value ?: return
    val prompt = state.prompt
    val result = state.result

    // Convert initial Hex to HSV
    val initialHsv = remember(prompt.defaultValue) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(AndroidColor.parseColor(prompt.defaultValue), hsv)
        hsv
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    // Derive the final color
    val selectedColorInt = remember(hue, saturation, value) {
        AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = browserSettings.value.padding.dp)
            .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(1).dp))
            .background(Color.Black)
//            .padding(browserSettings.value.padding.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp),
            modifier = Modifier
                .padding(browserSettings.value.padding.dp)
                .clip(RoundedCornerShape(
                    browserSettings.value.cornerRadiusForLayer(2).dp
                ))
//                .background(Color.Magenta)
        ) {
            // 1. BIG PREVIEW BOX
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(browserSettings.value.heightForLayer(2).dp)
                    .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(2).dp))
                    .background(Color(selectedColorInt)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("#%06X", (0xFFFFFF and selectedColorInt)),
                    color = if (isColorDark(selectedColorInt)) Color.White else Color.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // 2. HUE SLIDER (The Rainbow)
            val rainbowBrush = remember {
                Brush.horizontalGradient(
                    listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                )
            }
            Box (
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(CircleShape).background(rainbowBrush))
                Slider(
                    value = hue,
                    onValueChange = { hue = it },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.Transparent, inactiveTrackColor = Color.Transparent)
                )
            }

            // 3. SATURATION SLIDER
            Column {
//                Text("saturation", color = Color.Gray, fontSize = 12.sp)
                Slider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color(selectedColorInt).copy(alpha = saturation))
                )
            }

            // 4. BRIGHTNESS SLIDER
            Column {
//                Text("brightness", color = Color.Gray, fontSize = 12.sp)
                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White.copy(alpha = value))
                )
            }

            // 5. ACTION BUTTONS
            Row(horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)) {
                CustomIconButton(
                    layer = 2,
                    browserSettings = browserSettings,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        result.complete(prompt.dismiss())
                        onDismiss()
                    },
                    buttonDescription = "cancel",
                    descriptionContent = descriptionContent,
                    painterId = R.drawable.ic_tab_close
                )
                CustomIconButton(
                    layer = 2,
                    browserSettings = browserSettings,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        val hexColor = String.format("#%06x", 0xFFFFFF and selectedColorInt)
                        result.complete(prompt.confirm(hexColor))
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