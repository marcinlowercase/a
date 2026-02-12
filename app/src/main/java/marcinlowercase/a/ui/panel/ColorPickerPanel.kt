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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.JsColorState
import marcinlowercase.a.ui.component.CustomIconButton
import kotlin.String


// marcinlowercase.a.ui.panel.ColorPickerPanel.kt
import android.graphics.Color as AndroidColor // Alias to avoid confusion with Compose Color

@Composable
fun ColorPickerPanel(
    colorState: MutableState<JsColorState?>, // Use MutableState for consistency with your other panels
    browserSettings: MutableState<BrowserSettings>,
    descriptionContent: MutableState<String>,
    onDismiss: () -> Unit
) {
    val state = colorState.value ?: return
    val prompt = state.prompt
    val result = state.result

    // 1. Parse the string defaultValue to an Int
    val initialColorInt = remember(prompt.defaultValue) {
        try {
            // prompt.defaultValue is "#RRGGBB"
            android.graphics.Color.parseColor(prompt.defaultValue)
        } catch (e: Exception) {
            android.graphics.Color.BLACK
        }
    }

    var selectedColor by remember { mutableIntStateOf(initialColorInt) }

    val presetColors = listOf(
        0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0xFFF44336.toInt(), 0xFFE91E63.toInt(),
        0xFF9C27B0.toInt(), 0xFF673AB7.toInt(), 0xFF3F51B5.toInt(), 0xFF2196F3.toInt(),
        0xFF03A9F4.toInt(), 0xFF00BCD4.toInt(), 0xFF009688.toInt(), 0xFF4CAF50.toInt(),
        0xFF8BC34A.toInt(), 0xFFCDDC39.toInt(), 0xFFFFEB3B.toInt(), 0xFFFFC107.toInt()
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = browserSettings.value.padding.dp)
            .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(1).dp))
            .background(Color.Black)
            .padding(browserSettings.value.padding.dp)
    ) {
        Column(
            modifier = Modifier.clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(2).dp)),
            verticalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
        ) {
            // Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(browserSettings.value.heightForLayer(2).dp)
                    .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(2).dp))
                    .background(Color(selectedColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("#%06X", (0xFFFFFF and selectedColor)),
                    color = if (isColorDark(selectedColor)) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            // Grid using .size and index to avoid import issues
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                modifier = Modifier.heightIn(max = browserSettings.value.maxContainerSizeForLayer(2).dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presetColors.size) { index ->
                    val colorInt = presetColors[index]
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .clickable { selectedColor = colorInt }
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
                    painterId = R.drawable.ic_tab_close
                )
                CustomIconButton(
                    layer = 3,
                    browserSettings = browserSettings,
                    modifier = Modifier.weight(1f),
                    onTap = {
                        // Convert the Int (e.g., -16777216) to a Hex String (e.g., "#000000")
                        // We mask with 0xFFFFFF to remove the Alpha channel, as HTML colors are usually 6 chars.
                        val hexColor = String.format("#%06X", 0xFFFFFF and selectedColor)

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