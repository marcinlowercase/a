package marcinlowercase.a.core.function

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.core.data_class.BrowserSettings

import java.net.URL

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
        .background(if (white) Color.White else Color.Transparent)
)

fun Modifier.buttonPointerInput(
    onTap: (() -> Unit),
    hapticFeedback: HapticFeedback,
    descriptionContent: MutableState<String>,
    buttonDescription: String,
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
                    hapticFeedback.performHapticFeedback(
                        HapticFeedbackType.LongPress
                    )
                    descriptionContent.value =
                        buttonDescription


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


                descriptionContent.value = ""
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