package marcinlowercase.a.core.function

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import android.graphics.Bitmap

import java.net.URL
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun Modifier.buttonSettingsForLayer(
    layer: Int,
    browserSettings: BrowserSettings,
    enabled: Boolean = true
): Modifier = this.then(
    Modifier
        .clip(
            RoundedCornerShape(
                browserSettings.cornerRadiusForLayer(layer).dp
            )
        )
        .heightIn(min = browserSettings.heightForLayer(layer).dp)
        .background(if (enabled) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
)

@Composable
fun Modifier.buttonSettingsForLayer(
    layer: Int,
    browserSettings: BrowserSettings,
    backgroundColor: Color
): Modifier = this.then(
    Modifier
        .clip(
            RoundedCornerShape(
                browserSettings.cornerRadiusForLayer(layer).dp
            )
        )
        .heightIn(min = browserSettings.heightForLayer(layer).dp)
        .background(backgroundColor)
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
        .pointerInput(buttonDescription) {
            // 1. CAPTURE the CoroutineScope provided by pointerInput
            val coroutineScope = CoroutineScope(
                currentCoroutineContext()
            )
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)

                // 2. USE the captured scope to launch the long press job
                val longPressJob = coroutineScope.launch {
                    delay(viewConfiguration.longPressTimeoutMillis.milliseconds)

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

fun String.toDomain(): String {
    val virtual = marcinlowercase.a.core.server.LocalAppServer.toVirtualUrl(this)
    if (virtual.startsWith(marcinlowercase.a.core.server.LocalAppServer.VIRTUAL_SCHEME, ignoreCase = true)) {
        val formatted = marcinlowercase.a.core.server.LocalAppServer.formatForDisplay(virtual)
        return if (formatted.isNotBlank() && formatted != "local:") formatted else "local://draft"
    }

    return try {
        java.net.URL(this).host?.let {
            if (it.startsWith("www.", ignoreCase = true)) it.substring(4) else it
        } ?: this
    } catch (_: Exception) {
        this
    }
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

fun isBubbleMode(activity: Activity?): Boolean {
    if (activity == null || !activity.isInMultiWindowMode) return false

    // 1. Check for Freeform/Floating Window Mode (Works on HyperOS, MIUI, One UI)
    try {
        val config = activity.resources.configuration
        val getWindowConfig = config.javaClass.getMethod("getWindowConfiguration")
        val windowConfig = getWindowConfig.invoke(config)
        val getWindowingMode = windowConfig?.javaClass?.getMethod("getWindowingMode")
        val mode = getWindowingMode?.invoke(windowConfig) as? Int

        Log.i("mrcHello", "mode $mode")
        Log.i("mrcHello", "")
        // 5 = WINDOWING_MODE_FREEFORM (Floating Window / Pop-up view)
        if (mode == 5) return true
    } catch (_: Exception) {
        // Fallback if reflection fails
    }

    // 2. Fallback for Stock Android / Pixel Bubbles
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val wm = activity.windowManager
        val windowBounds = wm.currentWindowMetrics.bounds
        val displayBounds = wm.maximumWindowMetrics.bounds

        return windowBounds.width() < displayBounds.width() &&
                windowBounds.height() < displayBounds.height()
    }

    return false
}

fun extractEdgeColorsFromBitmap(bitmap: Bitmap): Pair<Color, Color> {
    val height = bitmap.height
    if (bitmap.width <= 0 || height <= 0) return Color.Black to Color.Black
    val topColor = sampleDominantRowColor(bitmap, listOf(1, 3))
    val bottomColor = sampleDominantRowColor(bitmap, listOf(height - 2, height - 4))
    return topColor to bottomColor
}

private fun sampleDominantRowColor(bitmap: Bitmap, ys: List<Int>): Color {
    val width = bitmap.width
    val height = bitmap.height
    val sampleCount = 24
    val startX = (width * 0.04f).toInt().coerceIn(0, width - 1)
    val endX = (width * 0.96f).toInt().coerceIn(startX, width - 1)
    val step = ((endX - startX).toFloat() / (sampleCount - 1)).coerceAtLeast(1f)

    val bucketCounts = HashMap<Int, Int>()
    val bucketSumR = HashMap<Int, Long>()
    val bucketSumG = HashMap<Int, Long>()
    val bucketSumB = HashMap<Int, Long>()

    for (rawY in ys) {
        val y = rawY.coerceIn(0, height - 1)
        var fx = startX.toFloat()
        while (fx <= endX + 0.5f) {
            val x = fx.toInt().coerceIn(0, width - 1)
            val pixel = bitmap.getPixel(x, y)
            if (android.graphics.Color.alpha(pixel) >= 128) {
                val r = android.graphics.Color.red(pixel)
                val g = android.graphics.Color.green(pixel)
                val b = android.graphics.Color.blue(pixel)
                // quantize into 5-bit buckets to ignore small icons or text
                val key = ((r shr 3) shl 10) or ((g shr 3) shl 5) or (b shr 3)
                bucketCounts[key] = (bucketCounts[key] ?: 0) + 1
                bucketSumR[key] = (bucketSumR[key] ?: 0L) + r
                bucketSumG[key] = (bucketSumG[key] ?: 0L) + g
                bucketSumB[key] = (bucketSumB[key] ?: 0L) + b
            }
            fx += step
        }
    }

    val winningKey = bucketCounts.maxByOrNull { it.value }?.key ?: return Color.Black
    val count = bucketCounts[winningKey] ?: 1
    val avgR = (bucketSumR[winningKey]!! / count).toInt().coerceIn(0, 255)
    val avgG = (bucketSumG[winningKey]!! / count).toInt().coerceIn(0, 255)
    val avgB = (bucketSumB[winningKey]!! / count).toInt().coerceIn(0, 255)
    return Color(avgR, avgG, avgB)
}