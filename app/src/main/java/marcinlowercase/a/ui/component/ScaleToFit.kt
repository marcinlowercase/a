package marcinlowercase.a.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp

@Composable
fun ScaleToFit(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        // The Material 3 DatePicker is designed to be roughly 360dp wide.
        val minWidthPx = 360.dp.roundToPx()

        // Safely determine available width (fallback if parent passed Infinity)
        val availableWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else minWidthPx

        // Force the DatePicker to lay itself out at least 360dp wide so it doesn't squish
        val targetWidth = maxOf(availableWidth, minWidthPx)

        // 1. Measure the child with FIXED, FINITE constraints to prevent the crash
        val childConstraints = constraints.copy(
            minWidth = targetWidth,
            maxWidth = targetWidth
        )
        val placeable = measurables.first().measure(childConstraints)

        // 2. Calculate scale factor if the required width is larger than what the screen allows
        val scale = if (targetWidth > availableWidth && availableWidth > 0) {
            availableWidth.toFloat() / targetWidth.toFloat()
        } else {
            1f
        }

        // 3. Calculate final bounding box size
        val scaledWidth = (placeable.width * scale).toInt()
        val scaledHeight = (placeable.height * scale).toInt()

        // 4. Place and scale
        layout(scaledWidth, scaledHeight) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}