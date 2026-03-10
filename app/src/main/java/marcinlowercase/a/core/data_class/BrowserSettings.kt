package marcinlowercase.a.core.data_class

import kotlin.math.roundToInt

data class BrowserSettings(
    val isFirstAppLoad: Boolean,
    val padding: Float,
    val deviceCornerRadius: Float,
    val defaultUrl: String,
    val animationSpeed: Float,
    val singleLineHeight: Float,
    val isAdBlockEnabled: Boolean = true,
    val isDesktopMode: Boolean,
    val isSharpMode: Boolean,
    val cursorContainerSize: Float,
    val cursorPointerSize: Float,
    val cursorTrackingSpeed: Float,
    val showSuggestions: Boolean,
    val closedTabHistorySize: Float,
    val backSquareOffsetX: Float = -1f,
    val backSquareOffsetY: Float = 0f,
    val backSquareIdleOpacity: Float = 0.2f,
    val maxListHeight: Float =  2f,
    val searchEngine: Int = 0,
    val isFullscreenMode: Boolean,
    val highlightColor: Int = 0xFFBA160C.toInt(),
    val isGuideModeEnabled: Boolean = true,
) {
    fun cornerRadiusForLayer(layer: Int): Float {
        if (layer == 0) return deviceCornerRadius
        return (cornerRadiusForLayer(layer - 1) - padding).coerceAtLeast(0f)
    }

    fun heightForLayer(layer: Int): Float {

        return if ((cornerRadiusForLayer(layer) * 2f) > minHeightForLayer(layer)) {
            cornerRadiusForLayer(layer) * 2f
        } else {
            minHeightForLayer(layer)
        }
    }
    fun minHeightForLayer(layer: Int) : Float {
        return singleLineHeight - (padding * layer* 2)
    }


    fun maxContainerSizeForLayer(layer: Int): Float {
        return (heightForLayer(layer) * maxListHeight) + (padding * (maxListHeight.toInt()  + 1))
    }

    fun animationSpeedForLayer(layer: Int): Int {
        val adjusted = animationSpeed - 50f * layer
        return adjusted.coerceAtLeast(0f).roundToInt()
    }
}
