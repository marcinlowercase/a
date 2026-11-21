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
import android.content.ComponentName
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.platform.LocalContext
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
    if (activity == null || !activity.isInMultiWindowMode || activity.isInPictureInPictureMode) return false

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

fun parseHexColorOrNull(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        null
    }
}

private data class EdgeSample(
    val color: Color,
    val usedJsFallback: Boolean,
    val hasPixelSample: Boolean
)

fun extractEdgeColorsFromBitmap(
    bitmap: Bitmap,
    jsTopBg: Color? = null,
    jsBottomBg: Color? = null
): Pair<Color, Color> {
    val height = bitmap.height
    if (bitmap.width <= 0 || height <= 0) {
        return (jsTopBg ?: Color.Black) to (jsBottomBg ?: Color.Black)
    }
    val topSample = sampleEdgeColor(bitmap, listOf(1, 3), jsTopBg)
    val bottomSample = sampleEdgeColor(bitmap, listOf(height - 2, height - 4), jsBottomBg)

    // if one side fell back to js background, reuse the sampled pixel color from the other side
    val topColor = if (topSample.usedJsFallback && !bottomSample.usedJsFallback && bottomSample.hasPixelSample) {
        bottomSample.color
    } else {
        topSample.color
    }
    val bottomColor = if (bottomSample.usedJsFallback && !topSample.usedJsFallback && topSample.hasPixelSample) {
        topSample.color
    } else {
        bottomSample.color
    }
    return topColor to bottomColor
}

private fun sampleEdgeColor(bitmap: Bitmap, ys: List<Int>, jsFallbackColor: Color?): EdgeSample {
    val width = bitmap.width
    val height = bitmap.height
    val sampleCount = 32
    val leftX = 2.coerceIn(0, width - 1)
    val rightX = (width - 3).coerceIn(leftX, width - 1)
    val step = ((rightX - leftX).toFloat() / (sampleCount - 1)).coerceAtLeast(1f)

    val bucketCounts = HashMap<Int, Int>()
    val bucketSumR = HashMap<Int, Long>()
    val bucketSumG = HashMap<Int, Long>()
    val bucketSumB = HashMap<Int, Long>()
    var totalOpaque = 0

    val leftKeys = mutableListOf<Int>()
    val rightKeys = mutableListOf<Int>()

    fun recordPixel(x: Int, y: Int, edgeList: MutableList<Int>? = null) {
        val pixel = bitmap.getPixel(x, y)
        if (android.graphics.Color.alpha(pixel) < 128) return
        val r = android.graphics.Color.red(pixel)
        val g = android.graphics.Color.green(pixel)
        val b = android.graphics.Color.blue(pixel)
        // quantize into 5-bit buckets to ignore small icons or text
        val key = ((r shr 3) shl 10) or ((g shr 3) shl 5) or (b shr 3)
        bucketCounts[key] = (bucketCounts[key] ?: 0) + 1
        bucketSumR[key] = (bucketSumR[key] ?: 0L) + r
        bucketSumG[key] = (bucketSumG[key] ?: 0L) + g
        bucketSumB[key] = (bucketSumB[key] ?: 0L) + b
        totalOpaque++
        edgeList?.add(key)
    }

    for (rawY in ys) {
        val y = rawY.coerceIn(0, height - 1)
        recordPixel(leftX, y, leftKeys)
        recordPixel(rightX, y, rightKeys)

        var fx = leftX + step
        while (fx < rightX - 0.5f) {
            recordPixel(fx.toInt().coerceIn(0, width - 1), y)
            fx += step
        }
    }

    if (totalOpaque == 0) {
        return EdgeSample(
            color = jsFallbackColor ?: Color.Black,
            usedJsFallback = jsFallbackColor != null,
            hasPixelSample = false
        )
    }

    val winningEntry = bucketCounts.maxByOrNull { it.value } ?: return EdgeSample(
        color = jsFallbackColor ?: Color.Black,
        usedJsFallback = jsFallbackColor != null,
        hasPixelSample = false
    )
    val winningKey = winningEntry.key
    val winningCount = winningEntry.value
    val winningRatio = winningCount.toFloat() / totalOpaque

    fun colorForKey(key: Int): Color {
        val count = bucketCounts[key] ?: 1
        val avgR = (bucketSumR[key]!! / count).toInt().coerceIn(0, 255)
        val avgG = (bucketSumG[key]!! / count).toInt().coerceIn(0, 255)
        val avgB = (bucketSumB[key]!! / count).toInt().coerceIn(0, 255)
        return Color(avgR, avgG, avgB)
    }

    val leftMatchesWinner = leftKeys.any { it == winningKey }
    val rightMatchesWinner = rightKeys.any { it == winningKey }

    // use pixel dominant color only when it truly spans edge to edge
// use pixel dominant color only when it truly spans edge to edge
    if (winningRatio >= 0.51f && leftMatchesWinner && rightMatchesWinner) {        return EdgeSample(
            color = colorForKey(winningKey),
            usedJsFallback = false,
            hasPixelSample = true
        )
    }

    // mark js background fallback so the other side's pixel sample can be reused
    if (jsFallbackColor != null) {
        return EdgeSample(
            color = jsFallbackColor,
            usedJsFallback = true,
            hasPixelSample = false
        )
    }

    // fallback to matching outer edge pixel color for gradient or image backgrounds
    val sharedEdgeKey = leftKeys.firstOrNull { it in rightKeys }
    if (sharedEdgeKey != null) {
        return EdgeSample(
            color = colorForKey(sharedEdgeKey),
            usedJsFallback = false,
            hasPixelSample = true
        )
    }

    return EdgeSample(
        color = colorForKey(winningKey),
        usedJsFallback = false,
        hasPixelSample = true
    )
}


@Composable
fun rememberIsStudioKeyboard(): Boolean {
    val context = LocalContext.current
    fun check(): Boolean {
        val ime = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ) ?: return false
        val pkg = ComponentName.unflattenFromString(ime)?.packageName ?: ime.substringBefore('/')
        return pkg == "studio.oo1.keyboard"
    }

    var isStudioKeyboard by remember { mutableStateOf(check()) }

    DisposableEffect(context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                isStudioKeyboard = check()
            }
        }
        val uri = Settings.Secure.getUriFor(Settings.Secure.DEFAULT_INPUT_METHOD)
        context.contentResolver.registerContentObserver(uri, false, observer)
        onDispose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }

    return isStudioKeyboard
}

fun Modifier.mouseHorizontalPagerScroll(
    pagerState: PagerState,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this
    val scope = rememberCoroutineScope()
    var lastScrollTimeMs by remember { mutableLongStateOf(0L) }

    this.pointerInput(pagerState) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.type == PointerEventType.Scroll) {
                    val change = event.changes.firstOrNull() ?: continue
                    val dx = change.scrollDelta.x
                    if (dx != 0f) {
                        change.consume()
                        val now = System.currentTimeMillis()
                        if (now - lastScrollTimeMs >= 120L) {
                            lastScrollTimeMs = now
                            val direction = if (dx > 0f) 1 else -1
                            val target = (pagerState.targetPage + direction)
                                .coerceIn(0, (pagerState.pageCount - 1).coerceAtLeast(0))
                            if (target != pagerState.targetPage) {
                                scope.launch {
                                    pagerState.animateScrollToPage(target)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}