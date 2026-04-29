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
package marcinlowercase.a.core.data_class

import kotlinx.serialization.Serializable
import marcinlowercase.a.ui.panel.isColorDark
import kotlin.math.roundToInt

@Serializable
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
    val optionsOrder: String,
    val settingsOrder: String,
    val hiddenOptions: String,
    val isEnabledConfirmation: Boolean,
    val isEnabledBackgroundPlayback: Boolean,

    val isEnabledMediaControl: Boolean,
    val isEnabledOutSync: Boolean,
    val memoryUsage: Int,
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
    fun onHighlight(): Int {
        return if (isColorDark(highlightColor)) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
    }
    fun offHighlight(): Int {
        return if (!isColorDark(highlightColor)) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
    }
}
