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
package marcinlowercase.a.core.function

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.core.data_class.BrowserSettings

import java.net.URL
import java.util.Locale

@Composable
fun Modifier.buttonSettingsForLayer(
    layer: Int,
    browserSettings: BrowserSettings,
    white: Boolean = true
): Modifier = this.then(
    Modifier
        .clip(
            RoundedCornerShape(
                browserSettings.cornerRadiusForLayer(layer).dp
            )
        )
        .height(browserSettings.heightForLayer(layer).dp)
        .background(if (white) MaterialTheme.colorScheme.onSurface else Color.Transparent)
)

fun Modifier.buttonPointerInput(
    onTap: (() -> Unit),
    onLongPress: () -> Boolean = {
        false
    },
    hapticFeedback: HapticFeedback,
    descriptionContent: MutableState<String>,
    buttonDescription: String,
    useLongPress: Boolean = true,
    ): Modifier = this.then(
    Modifier
        .pointerInput(Unit) {
            // 1. CAPTURE the CoroutineScope provided by pointerInput
            val coroutineScope = CoroutineScope(
                currentCoroutineContext()
            )
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)

                // 2. USE the captured scope to launch the long press job
                val longPressJob = coroutineScope.launch {
                    delay(viewConfiguration.longPressTimeoutMillis)

                    // LONG PRESS CONFIRMED
                    if (useLongPress) {
                        hapticFeedback.performHapticFeedback(
                            HapticFeedbackType.LongPress
                        )
                        if (!onLongPress()) descriptionContent.value = buttonDescription

                    }


                }
                val drag =
                    awaitTouchSlopOrCancellation(down.id) { change, _ ->
                        if (longPressJob.isActive) {
                            longPressJob.cancel()
                        }
                        change.consume()
                    }



                if (!(longPressJob.isCompleted && !longPressJob.isCancelled)) {
                    if (drag == null) {
                        if (longPressJob.isActive) {
                            longPressJob.cancel()
                            // This was a tap
                            onTap()

                        }
                    }
                }


                if (descriptionContent.value == buttonDescription) descriptionContent.value = ""
            }
        }
)

fun formatTimeRemaining(millis: Long): String {
    if (millis <= 0) return ""
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) {
        "${minutes}m ${seconds}s left"
    } else {
        "${seconds}s left"
    }
}

fun formatTime(seconds: Double): String {
    if (seconds <= 0 || seconds.isNaN()) return "--:--"
    val totalSeconds = seconds.toLong()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60

    return if (hours > 0) {
        String.format(Locale.US,"%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format(Locale.US,"%02d:%02d", minutes, secs)
    }
}

fun formatSpeed(bytesPerSecond: Float): String {
    if (bytesPerSecond < 1024) return "%.0f B/s".format(bytesPerSecond)
    val kbps = bytesPerSecond / 1024
    if (kbps < 1024) return "%.1f KB/s".format(kbps)
    val mbps = kbps / 1024
    return "%.1f MB/s".format(mbps)
}

fun getFaviconUrlFromGoogleServer(pageUrl: String): String {
    val host = try {
        pageUrl.toUri().host ?: ""
    } catch (e: Exception) {
        e.toString()
    }
    // Using Google's favicon service is a reliable way to get icons.
    return "https://www.google.com/s2/favicons?sz=64&domain_url=$host"
}

fun String.toDomain(): String = try {
    URL(this).host?.let {
        if (it.startsWith("www.", ignoreCase = true)) it.substring(4) else it
    } ?: this
} catch (_: Exception) {
    this
}

fun formatArgbToCss(argb: String): String {
    // Remove the '#' if the user accidentally included it
    val cleanHex = argb.removePrefix("#")

    return if (cleanHex.length == 8) {
        val alpha = cleanHex.substring(0, 2)
        val rgb = cleanHex.substring(2, 8)
        "#$rgb$alpha"
    } else {
        // Fallback for 6-digit hex or invalid strings
        "#$cleanHex"
    }
}

