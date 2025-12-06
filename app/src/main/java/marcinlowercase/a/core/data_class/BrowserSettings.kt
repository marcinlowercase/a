package marcinlowercase.a.core.data_class

import kotlin.math.roundToInt

data class BrowserSettings(
    val isFirstAppLoad: Boolean,
    val padding: Float,
    val deviceCornerRadius: Float,
    val defaultUrl: String,
    val animationSpeed: Float,
    val singleLineHeight: Float,
//    val isDesktopMode: Boolean,
//    val desktopModeWidth: Int,
    val isSharpMode: Boolean,
//    val topSharpEdge: Float,
//    val bottomSharpEdge: Float,
    val cursorContainerSize: Float,
    val cursorPointerSize: Float,
    val cursorTrackingSpeed: Float,
    val showSuggestions: Boolean,
    val closedTabHistorySize: Float,

    val backSquareOffsetX: Float = 0f,
    val backSquareOffsetY: Float = 0f,
    val backSquareIdleOpacity: Float = 0.2f,
    ) {
    fun cornerRadiusForLayer(layer: Int): Float {
        if (layer == 0) return deviceCornerRadius
        return (cornerRadiusForLayer(layer - 1) - padding).coerceAtLeast(0f)
    }

    fun heightForLayer(layer: Int): Float {
        return if (deviceCornerRadius > singleLineHeight) {
            cornerRadiusForLayer(layer) * 2f
        } else {
            cornerRadiusForLayer(layer, maxRadius = 50f) * 2f
        }
    }

    private fun cornerRadiusForLayer(layer: Int, maxRadius: Float): Float {
        if (layer == 0) return maxRadius
        return (cornerRadiusForLayer(layer - 1, maxRadius) - padding).coerceAtLeast(0f)
    }

    fun animationSpeedForLayer(layer: Int): Int {
        val adjusted = animationSpeed - 50f * layer
        return adjusted.coerceAtLeast(0f).roundToInt()
    }
}
