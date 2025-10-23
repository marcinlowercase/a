package marcinlowercase.a

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import marcinlowercase.a.ui.theme.Themeinlowercase
import java.io.File
import java.io.FileOutputStream
import java.net.URISyntaxException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern
import kotlin.coroutines.coroutineContext
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.system.exitProcess


//region Global Variables
var databaseCurrentIndexHolder = -1
var realtimePreviousIndexHolder = 0
var pixel_9_corner_radius = 54.85f

const val default_url = "https://oo3.deno.dev/i"
//const val default_url = "http://192.168.1.195:11111/i"


//region JS Code


const val JS_HOVER_SIMULATOR = """
    (function() {
        console.log("JS_HOVER_SIMULATOR")
        // Keep track of the last element we hovered over to correctly fire mouseout.
        window.lastHoveredElement = null;

        // The main function that Kotlin will call.
        window.simulateHover = function(x, y) {
            try {
                // Find the element at the given viewport coordinates.
                var currentElement = document.elementFromPoint(x, y);

                // If the element under the cursor has changed...
                if (currentElement !== window.lastHoveredElement) {
                    // ...dispatch a 'mouseout' event on the old element (if it exists).
                    if (window.lastHoveredElement) {
                        var mouseOutEvent = new MouseEvent('mouseout', { bubbles: true, cancelable: true, view: window });
                        window.lastHoveredElement.dispatchEvent(mouseOutEvent);
                    }
                    // ...and dispatch a 'mouseover' event on the new element (if it exists).
                    if (currentElement) {
                        var mouseOverEvent = new MouseEvent('mouseover', { bubbles: true, cancelable: true, view: window });
                        currentElement.dispatchEvent(mouseOverEvent);
                    }
                }

                // Always dispatch a 'mousemove' event on the current element for continuous effects.
                if (currentElement) {
                    var mouseMoveEvent = new MouseEvent('mousemove', { clientX: x, clientY: y, bubbles: true, cancelable: true, view: window });
                    currentElement.dispatchEvent(mouseMoveEvent);
                }

                // Update the last hovered element for the next call.
                window.lastHoveredElement = currentElement;
            } catch (e) {
                // Fails silently if the page context is weird.
            }
        };
    })();
"""

//endregion

//endregion

//region Global Functions


fun webViewLoad(view: CustomWebView?, url: String, browserSettings: BrowserSettings) {
    val headerinlowercase = mutableMapOf<String, String>()

    // i want it send the string of "device_corner_radius" with the value of browserSettings.deviceCornerRadius
    headerinlowercase["device_corner_radius"] = browserSettings.deviceCornerRadius.toString()

    view?.loadUrl(url, headerinlowercase)
}


fun createNotificationChannel(context: Context) {
    val name = "downloads"
    val descriptionText = "shows download progress and completion"
    val importance = NotificationManager.IMPORTANCE_LOW
    val channel = NotificationChannel("download_channel", name, importance).apply {
        description = descriptionText
    }
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}


@SuppressLint("ModifierFactoryExtensionFunction")
fun buttonModifierForLayer(
    layer: Int,
    deviceCornerRadius: Float = 0f,
    padding: Float = 0f,
    singleLineHeight: Float,
    white: Boolean = true
): Modifier {
    return Modifier
        .clip(
            RoundedCornerShape(
                cornerRadiusForLayer(
                    layer,
                    deviceCornerRadius,
                    padding
                ).dp
            )
        )
        .height(
            heightForLayer(layer, deviceCornerRadius, padding, singleLineHeight).dp
        )
        .background(if (white) Color.White else Color.Transparent)
        .border(
            width = 1.dp,
            color = if (white) Color.Transparent else Color.White,
            shape = RoundedCornerShape(
                cornerRadiusForLayer(
                    layer,
                    deviceCornerRadius,
                    padding
                ).dp
            )
        )
}

fun formatSpeed(bytesPerSecond: Float): String {
    if (bytesPerSecond < 1024) return "%.0f B/s".format(bytesPerSecond)
    val kbps = bytesPerSecond / 1024
    if (kbps < 1024) return "%.1f KB/s".format(kbps)
    val mbps = kbps / 1024
    return "%.1f MB/s".format(mbps)
}

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

fun cornerRadiusForLayer(layer: Int, deviceCornerRadius: Float, padding: Float): Float {

    if (layer == 0) {
        return deviceCornerRadius
    }
    return (cornerRadiusForLayer(
        layer - 1,
        deviceCornerRadius,
        padding
    ) - padding).coerceAtLeast(0f)
}

fun heightForLayer(
    layer: Int,
    deviceCornerRadius: Float,
    padding: Float,
    singleLineHeight: Float
): Float {
    return if (deviceCornerRadius > singleLineHeight)
        cornerRadiusForLayer(layer, deviceCornerRadius, padding) * 2
    else
        cornerRadiusForLayer(layer, 50f, padding) * 2


}

fun animationSpeedForLayer(layer: Int, animationSpeed: Float = 0f): Int {
    return (if ((animationSpeed - 50) <= 0) 0f else animationSpeed - 50 * layer).roundToInt()
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

//endregion

//region JavaScript Interface
class FaviconJavascriptInterface(
    private val onFaviconUrlFound: (String) -> Unit
) {
    @Suppress("unused")
    @JavascriptInterface
    fun passFaviconUrl(absoluteIconUrl: String?) {
        if (absoluteIconUrl != null) {
            Log.d("Favicon", "Received absolute icon URL from JS: $absoluteIconUrl")
            onFaviconUrlFound(absoluteIconUrl)
        } else {
            // Here you can decide on a fallback. Maybe do nothing and let Coil show a placeholder.
            // Or you can try the /favicon.ico, but it's less reliable.
            Log.d("Favicon", "No icon URL found in page HTML.")
        }
    }
}

class BlobDownloaderInterface(
    // It now takes a callback function in its constructor
    private val onBlobDataReceived: (base64Data: String, filename: String, mimeType: String) -> Unit
) {
    @Suppress("unused")
    @JavascriptInterface
    fun downloadBase64File(base64Data: String, filename: String, mimeType: String) {
        // Instead of saving the file, it just calls the callback with the data.
        onBlobDataReceived(base64Data, filename, mimeType)
    }
}
//endregion

//region Data Class

data class PanelVisibilityState(
    val options: Boolean,
    val tabs: Boolean,
    val downloads: Boolean,
    val tabData: Boolean,
    val nav: Boolean
)

private enum class TabDataPanelView {
    MAIN,
    HISTORY,
    PERMISSIONS
}

private enum class SettingPanelView {
    MAIN,
    CORNER_RADIUS,
    PADDING,
    ANIMATION_SPEED,
    CURSOR_CONTAINER_SIZE,
    CURSOR_TRACKING_SPEED,
    DEFAULT_URL,
    INFO,

}

data class ConfirmationDialogState(
    val message: String,
    val onConfirm: () -> Unit,
    val onCancel: () -> Unit
)

@Serializable
data class SiteSettings(
    val domain: String,
    // Map of <PermissionConstant, isGranted> e.g., <"android.permission.CAMERA", true>
    val permissionDecisions: MutableMap<String, Boolean> = mutableMapOf()
)

class SiteSettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("BrowserSiteSettings", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val settingsKey = "site_settings_map_json"

    // We store all settings as a single Map<Domain, SiteSettings> serialized to JSON
    fun saveSettings(settings: Map<String, SiteSettings>) {
        val jsonString = json.encodeToString(settings)
        prefs.edit { putString(settingsKey, jsonString) }
    }

    fun loadSettings(): MutableMap<String, SiteSettings> {
        val jsonString = prefs.getString(settingsKey, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                Log.e("SiteSettingsManager", "Failed to decode site settings", e)
                mutableMapOf()
            }
        } else {
            mutableMapOf()
        }
    }

    // Helper to extract a domain from a URL (e.g., "https://www.google.com/search" -> "google.com")
    fun getDomain(url: String?): String? {
        return url?.toUri()?.host?.removePrefix("www.")
    }
}

private data class PollData(
    val timestampMs: Long,
    val bytesDownloaded: Long,
    val lastSpeedBps: Float = 0f
)


@Serializable
enum class DownloadStatus {
    PENDING,
    RUNNING,
    PAUSED,
    SUCCESSFUL,
    FAILED,
    CANCELLED
}

@Serializable
data class DownloadItem(
    val id: Long, // This ID comes from Android's DownloadManager
    val url: String,
    val filename: String,
    val mimeType: String,
    var status: DownloadStatus = DownloadStatus.PENDING,
    var progress: Int = 0, // Progress from 0 to 100
    var totalBytes: Long = 0,
    var downloadedBytes: Long = 0,
    val isBlobDownload: Boolean = false,
    @kotlinx.serialization.Transient var downloadSpeedBps: Float = 0f, // Bytes per second
    @kotlinx.serialization.Transient var timeRemainingMs: Long = 0L    // Milliseconds
)


class DownloadTracker(context: Context) {
    private val prefs = context.getSharedPreferences("BrowserDownloads", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val downloadsKey = "downloads_list_json"

    fun saveDownloads(downloads: List<DownloadItem>) {
        val jsonString = json.encodeToString(downloads)
        prefs.edit { putString(downloadsKey, jsonString) }
        Log.d("DownloadTracker", "${downloads.size} downloads saved.")
    }

    fun loadDownloads(): MutableList<DownloadItem> {
        val jsonString = prefs.getString(downloadsKey, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                Log.e("DownloadTracker", "Failed to decode downloads", e)
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
    }
}


// A sealed interface to represent any type of JS Dialog
sealed interface JsDialogState

// Represents the "OK" button dialog from window.alert()
data class JsAlert(val message: String) : JsDialogState

// Represents the "OK" / "Cancel" dialog from window.confirm()
data class JsConfirm(val message: String, val onResult: (Boolean) -> Unit) : JsDialogState

// Represents the text input dialog from window.prompt()
data class JsPrompt(
    val message: String,
    val defaultValue: String,
    val onResult: (String?) -> Unit
) : JsDialogState

data class OptionItem(
    val iconRes: Int, // The drawable resource ID for the icon
    val contentDescription: String,
    val enabled: Boolean = false,
    val onClick: () -> Unit,
)

data class ColorScheme(
    val backgroundColor: Color,
    val foregroundColor: Color
)

data class BrowserSettings(
    val paddingDp: Float,
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
)

enum class GestureNavAction {
    NONE, // The overlay is hidden
    BACK,
    REFRESH,
    FORWARD,
    NEW_TAB,
    CLOSE_TAB,
}

data class CustomPermissionRequest(
    val origin: String,
    val title: String,
    val rationale: String,
    val iconResAllow: Int,
    val iconResDeny: Int,
    val permissionsToRequest: List<String>,
    val onResult: (Map<String, Boolean>) -> Unit
)

// The enum for the state of a tab
@Serializable // Marks this class as serializable
enum class TabState {
    ACTIVE,      // The tab currently visible to the user
    BACKGROUND,  // A tab that is loaded but not visible
    FROZEN       // A tab that needs to be reloaded when opened
}

// The data class for a single tab
// Serializable version of WebHistoryItem
@Serializable
data class SerializableHistoryItem(
    val url: String,
    val title: String,
    val faviconUrl: String? = null,
)

// Serializable version of WebBackForwardList
@Serializable
data class SerializableBackForwardList(
    val items: List<SerializableHistoryItem>,
    val currentIndex: Int
)

@Serializable
data class Tab(
    val id: Long = System.currentTimeMillis(),
    var state: TabState = TabState.BACKGROUND,
    var historyState: SerializableBackForwardList? = null
) {
    // A convenient property to get the current URL from our saved state
    val currentUrl: String?
        get() = historyState?.items?.getOrNull(historyState!!.currentIndex)?.url

    companion object {
        fun createEmpty(): Tab {
            return Tab(id = 0)
        }
    }
}

class TabManager(context: Context) {
    private val prefs = context.getSharedPreferences("BrowserTabs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true } // Lenient JSON parser

    private val tabsKey = "tabs_list_json"
    private val activeTabIndexKey = "active_tab_index"


    fun saveTabs(tabs: List<Tab>, activeTabIndex: Int) {
        // Convert the list of tabs into a single JSON string
        val jsonString = json.encodeToString(tabs)
        prefs.edit {
            putString(tabsKey, jsonString)
            putInt(activeTabIndexKey, activeTabIndex)
        }

        Log.d("TabManager", "Tabs saved.")
    }

    // New function to freeze all tabs on exit
    fun freezeAllTabs() {
        val tabs = loadTabs(default_url) // Load the current state
        if (tabs.isNotEmpty()) {
            val activeIndex = prefs.getInt(activeTabIndexKey, 0)

            // Freeze all tabs
            tabs.forEach { it.state = TabState.FROZEN }

            // Mark the last known active tab as ACTIVE so we can find it on next launch
            if (activeIndex in tabs.indices) {
                tabs[activeIndex].state = TabState.ACTIVE
            }

            saveTabs(tabs, activeIndex)
            Log.d("TabManager", "All tabs have been frozen.")
        }
    }

    fun clearAllTabs() {
        prefs.edit {
            remove(tabsKey)
            commit()
        }

    }

    fun loadTabs(defaultUrl: String): MutableList<Tab> {
        val jsonString = prefs.getString(tabsKey, null)

        Log.i("TabManager", "Loading tabs with url: $defaultUrl")
        Log.i("TabManager", "Loading tabs with json: $jsonString")
        return if (jsonString != null) {
            try {
                val loadedTabs = json.decodeFromString<MutableList<Tab>>(jsonString)


                if (loadedTabs.isEmpty()) {
                    Log.w("TabManager", "Loaded tab list was empty. Creating default.")
                    return createDefaultTabs(defaultUrl)
                }
                val activeIndex = prefs.getInt(activeTabIndexKey, 0)
                loadedTabs.forEachIndexed { index, tab ->
                    tab.state = if (index == activeIndex) TabState.ACTIVE else TabState.FROZEN
                }

                return loadedTabs

            } catch (e: Exception) {
                Log.e("TabManager", "Failed to decode tabs, creating default.", e)
                createDefaultTabs(defaultUrl)
            }
        } else {
            // If no saved data, create a default tab list
            createDefaultTabs(defaultUrl)
        }
    }

    private fun createDefaultTabs(defaultUrl: String): MutableList<Tab> {

        Log.i("TabManager", "Creating default tabs with url: $defaultUrl")
        return mutableListOf(
            Tab(
                state = TabState.ACTIVE,
                // Create a default history state for the first launch
                historyState = SerializableBackForwardList(
                    items = listOf(SerializableHistoryItem(url = defaultUrl, title = "oo")),
                    currentIndex = 0
                )
            )
        )
    }
}


class CustomWebView(context: Context) : WebView(context) {

    var onWebViewTouch: (() -> Unit)? = null

    /**
     * This method is called for every touch event on the WebView.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // 2. We only care about the beginning of a touch gesture.
        if (event?.action == MotionEvent.ACTION_DOWN) {
            // 3. If the user starts touching the screen, invoke our callback.
            onWebViewTouch?.invoke()
        }
        // 4. IMPORTANT: We must call super to let the WebView handle scrolling,
        // clicking, and other gestures normally.
        return super.onTouchEvent(event)
    }
//
//
//    override fun startActionMode(
//        callback: ActionMode.Callback,
//        type: Int
//    ): ActionMode? {
//        // Create a custom callback that does just enough to keep the mode alive
//        // for text highlighting, but never shows a menu.
//        val customCallback = object : ActionMode.Callback {
//            /**
//             * MUST return true. This tells the system to create the ActionMode,
//             * which is what enables the text highlighting.
//             */
//            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
//                callback.onCreateActionMode(mode, menu)
//                return true
//            }
//
//            /**
//
//             * This is the key. By returning false, we tell the system "Don't
//             * prepare or show the menu UI". The mode stays active in the background,
//             * but the user never sees the floating toolbar.
//             */
//            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
//                // Let the default callback prepare the menu.
//                callback.onPrepareActionMode(mode, menu)
//
//                // --- OUR CUSTOM LOGIC STARTS HERE ---
//
//                var translateItem: MenuItem? = null
//                var itemIndex = -1
//
//                // 1. Find the "Translate" item
//                for (i in 0 until menu.size()) {
//                    val item = menu[i]
//                    if (item.title.toString().equals("Translate", ignoreCase = true)) {
//                        translateItem = item
//                        itemIndex = i
//                        break // Stop searching once we've found it
//                    }
//                }
//
//                // 2. If we found it, move it to the front
//                if (translateItem != null) {
//                    // a. Store all of its original properties
//                    val originalTitle: CharSequence? = translateItem.title
//                    val originalIcon: Drawable? = translateItem.icon
//                    val originalIntent: Intent? = translateItem.intent
//                    val originalGroupId: Int = translateItem.groupId
//                    val originalItemId: Int = translateItem.itemId
//
//                    // b. Remove the item from its original position
//                    menu.removeItem(originalItemId)
//
//                    // c. Re-add the item at the very beginning of the menu
//                    val newTranslateItem = menu.add(
//                        originalGroupId,
//                        originalItemId,
//                        Menu.FIRST, // This is the key to forcing it to the front
//                        originalTitle
//                    )
//
//                    // d. Restore its original intent and icon
//                    newTranslateItem.intent = originalIntent
//                    newTranslateItem.icon = originalIcon
//                }
//
//                // --- OUR CUSTOM LOGIC ENDS HERE ---
//
//                // **CRUCIAL**: Return true to allow the system to draw the
//                // now-modified menu. Returning false would hide it.
//                return true
//            }
//
//            // These methods won't be called since there are no menu items,
//            // but we must implement them.
//            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
//                return callback.onActionItemClicked(mode, item)
//            }
//
//            override fun onDestroyActionMode(mode: ActionMode) {
//                // No action needed here. Our JavaScript handles hiding the
//                // custom Compose menu when the user clicks away.
//                callback.onDestroyActionMode(mode)
//
//            }
//        }
//
//        // We start the action mode, but we pass OUR custom callback, not the original one.
//        return super.startActionMode(callback, type)
//    }
}

class WebViewManager(private val context: Context) {

    private val webViewPool = mutableMapOf<Long, CustomWebView>()
    private var activeWebView: CustomWebView? = null
    val activity = context as? Activity

    //region JS Code
    private val jsFaviconDiscovery = """
    (function() {
        let icon = document.querySelector("link[rel='apple-touch-icon']") ||
                   document.querySelector("link[rel='icon']") ||
                   document.querySelector("link[rel='shortcut icon']");
        
        // The 'href' property of an HTMLAnchorElement or HTMLLinkElement is
        // automatically resolved to an absolute URL by the browser engine.
        // We don't need to resolve it in Kotlin anymore.
        WebAppFavicon.passFaviconUrl(icon ? icon.href : null);
    })();
""".trimIndent()
    //endregion


    // This method gets the WebView for a given tab, creating one if it doesn't exist.
    fun getWebView(tab: Tab): CustomWebView {
        val webView = webViewPool.getOrPut(tab.id) {
            createAndConfigureWebView()
        }
        activeWebView = webView
        return webView
    }

//    // Pre-loads a WebView for a background tab if it's not already loaded.
//    fun preloadWebView(tab: Tab) {
//        if (!webViewPool.containsKey(tab.id)) {
//            val backgroundWebView = getWebView(tab)
//            backgroundWebView.loadUrl(tab.currentUrl ?: "")
//        }
//    }

    // Call this when closing a tab to release resources.
    fun destroyWebView(tab: Tab) {
        webViewPool.remove(tab.id)?.apply {
            (parent as? ViewGroup)?.removeView(this)
            destroy()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createAndConfigureWebView(): CustomWebView {
        return CustomWebView(context).apply {
            // Force WebView to be transparent so Compose can control the background
            setBackgroundColor(android.graphics.Color.TRANSPARENT)

            // Apply all your production-grade settings
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                javaScriptCanOpenWindowsAutomatically = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Use the modern, non-deprecated API on Android 13+
                    isAlgorithmicDarkeningAllowed = false
                } else {
                    // Use the deprecated API for older versions, suppressing the warning
                    @Suppress("DEPRECATION")
                    forceDark = WebSettings.FORCE_DARK_OFF
                }

                mediaPlaybackRequiresUserGesture = false

                // CRITICAL: Zoom must be supported for overview mode to work reliably.
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false // Hide the on-screen +/- buttons

                clearCache(true)
            }

            // Enable remote debugging for debug builds
            if (0 != (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true)
            }

            // Ensure hardware acceleration
            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

            // Add your JS interface
//            addJavascriptInterface(WebAppInterface(), "Android")
        }
    }

    // We can also move the client setup here.
    // Note: These now take lambdas to communicate back to the Composable.
    fun setWebViewClients(
        webView: CustomWebView,
        tab: Tab,
        siteSettingsManager: SiteSettingsManager,
        siteSettings: Map<String, SiteSettings>,
        onFaviconChanged: (Long, String) -> Unit,
        onJsAlert: (String) -> Unit,
        onJsConfirm: (String, (Boolean) -> Unit) -> Unit,
        onJsPrompt: (String, String, (String?) -> Unit) -> Unit,
        onPermissionRequest: (CustomPermissionRequest?) -> Unit,
        setCustomViewCallback: (WebChromeClient.CustomViewCallback?) -> Unit,
        setOriginalOrientation: (Int) -> Unit,
        resetCustomView: () -> Unit,
        onDownloadRequested: (url: String, userAgent: String, contentDisposition: String, mimeType: String, contentLength: Long) -> Unit,
        onBlobDownloadRequested: (base64: String, filename: String, mimeType: String) -> Unit,
        onPageStartedFun: (WebView, String?, Bitmap?) -> Unit,
        onPageFinishedFun: (WebView, String?) -> Unit,
        onDoUpdateVisitedHistoryFun: (WebView, String?, Boolean) -> Unit,
        onTitleReceived: (webView: WebView, url: String, title: String) -> Unit,
    ) {

        webView.addJavascriptInterface(
            BlobDownloaderInterface { base64Data, filename, mimeType ->
                // This forwards the data to the callback we received from BrowserScreen
                onBlobDownloadRequested(base64Data, filename, mimeType)
            },
            "BlobDownloader"
        )

        webView.addJavascriptInterface(
            FaviconJavascriptInterface { faviconUrl ->
                // This is called from the JS bridge
                onFaviconChanged(tab.id, faviconUrl)
            },
            "WebAppFavicon" // This name must match the JS
        )

        // The WebChromeClient handles UI-related browser events.
        webView.webChromeClient = object : WebChromeClient() {

            private var fullscreenView: View? = null

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                // When the title is received, pass it back up along with the URL
                // so we can find the correct history item to update.
                if (view?.url != null && !title.isNullOrBlank()) {
                    onTitleReceived(view, view.url!!, title)
                }
            }

            // Handles window.alert()
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: android.webkit.JsResult?
            ): Boolean {

                // jsDialogState = JsAlert(message ?: "")

                onJsAlert(message ?: "")

                // We consume the result here and will handle it in our Compose Dialog
                result?.confirm()
                return true // Return true to indicate we've handled it.
            }

            // Handles window.confirm()
            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: android.webkit.JsResult?
            ): Boolean {
//                jsDialogState = JsConfirm(message ?: "") { confirmed ->
//                    if (confirmed) result?.confirm() else result?.cancel()
//                }

                onJsConfirm(message ?: "") { confirmed ->
                    if (confirmed) result?.confirm() else result?.cancel()
                }

                return true
            }

            // Handles window.prompt()
            override fun onJsPrompt(
                view: WebView?,
                url: String?,
                message: String?,
                defaultValue: String?,
                result: android.webkit.JsPromptResult?
            ): Boolean {
//                jsDialogState = JsPrompt(message ?: "", defaultValue ?: "") { inputText ->
//                    if (inputText != null) {
//                        result?.confirm(inputText)
//                    } else {
//                        result?.cancel()
//                    }
//                }

                onJsPrompt(message ?: "", defaultValue ?: "") { inputText ->
                    if (inputText != null) {
                        result?.confirm(inputText)
                    } else {
                        result?.cancel()
                    }
                }

                return true
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                if (origin == null || callback == null) return

                val domain = siteSettingsManager.getDomain(origin)
                val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
                val decision = siteSettings[domain]?.permissionDecisions?.get(locationPermission)

                if (decision != null) {
                    Log.d("PermissionCheck", "Found saved location decision for $domain: $decision")
                    // Invoke the callback immediately and skip the UI prompt
                    callback.invoke(origin, decision, false)
                    return // Stop further processing
                }


                // Create a new generic permission request for this specific geolocation prompt.
                val newRequest = CustomPermissionRequest(
                    origin = origin,
                    title = "Location Access Required",
                    rationale = "This website wants to use your device's location.",
                    iconResAllow = R.drawable.ic_location_on,
                    iconResDeny = R.drawable.ic_location_off,
                    permissionsToRequest = listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    // This is the key: the onResult callback for this specific request
                    // knows how to talk back to the WebView's Geolocation callback.
                    onResult = { permissions ->
                        val isGranted =
                            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                        callback.invoke(origin, isGranted, false)
                    }
                )
                onPermissionRequest(newRequest)

            }


            override fun onPermissionRequest(request: PermissionRequest) {
                Log.d(
                    "WebViewPermission",
                    "onPermissionRequest called for: ${request.resources.joinToString(", ")} from origin: ${request.origin}"
                )

                val requestedAndroidPermissions = mutableListOf<String>()
                var title = "Permission Required" // Default title
                var rationale =
                    "'${request.origin}' wants to use your device features." // Default rationale
                var allowIcon = R.drawable.ic_bug // Default allow icon
                var denyIcon = R.drawable.ic_bug   // Default deny icon

                val requestsCamera =
                    request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
                val requestsMicrophone =
                    request.resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)

                if (requestsCamera) {
                    requestedAndroidPermissions.add(Manifest.permission.CAMERA)
                    title = "Camera Access"
                    rationale = "Allow camera access for video recording."
                    allowIcon = R.drawable.ic_camera_on
                    denyIcon = R.drawable.ic_camera_off
                } else if (requestsMicrophone) {
                    requestedAndroidPermissions.add(Manifest.permission.RECORD_AUDIO)
                    title = "Microphone Access"
                    rationale = "Allow microphone access for audio recording."
                    allowIcon = R.drawable.ic_mic_on
                    denyIcon = R.drawable.ic_mic_off
                }

                // Add other permission mappings if needed
                if (request.resources.contains(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID)) {
                    // Handle protected media if needed
                    Log.d(
                        "WebViewPermission",
                        "Protected media ID requested - typically not mapped to runtime permissions"
                    )
                    // If no other Android permissions were added, you might want to deny or handle appropriately.
                    if (requestedAndroidPermissions.isEmpty()) {
                        Log.d(
                            "WebViewPermission",
                            "Protected media ID requested with no other mappable Android permissions; denying request."
                        )
                        request.deny()
                        return
                    }
                }

                if (requestedAndroidPermissions.isEmpty()) {
                    Log.d(
                        "WebViewPermission",
                        "No mappable Android permissions for the requested WebView resources; denying request."
                    )
                    request.deny()
                    return
                }

                // Check if we already have these permissions
                val context = webView.context
                val hasAllPermissions = requestedAndroidPermissions.all { permission ->
                    ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                }

                if (hasAllPermissions) {
                    // If we already have permissions, grant them immediately
                    Log.d(
                        "WebViewPermission",
                        "Permissions already granted, granting to WebView"
                    )
                    request.grant(request.resources)
                    return
                }


                val domain = siteSettingsManager.getDomain(request.origin.toString())
                if (domain != null && requestedAndroidPermissions.isNotEmpty()) {
                    val firstPermission =
                        requestedAndroidPermissions.first() // Assuming one permission per request for simplicity
                    val decision = siteSettings[domain]?.permissionDecisions?.get(firstPermission)

                    if (decision == true) {
                        Log.d(
                            "PermissionCheck",
                            "Found saved ALLOW decision for $domain, granting."
                        )
                        request.grant(request.resources)
                        return // Stop further processing
                    } else if (decision == false) {
                        Log.d("PermissionCheck", "Found saved DENY decision for $domain, denying.")
                        request.deny()
                        return // Stop further processing
                    }
                }

                // Create the custom request
                val newRequest = CustomPermissionRequest(
                    origin = request.origin.toString(),
                    title = title,
                    rationale = rationale,
                    iconResAllow = allowIcon,
                    iconResDeny = denyIcon,
                    permissionsToRequest = requestedAndroidPermissions,
                    onResult = { permissionsResult ->
                        activity?.runOnUiThread {
                            // Check which permissions were actually granted
                            val grantedPermissions = permissionsResult.filter { it.value }.keys

                            // Build a list of WebView resources to grant based on granted Android permissions
                            val resourcesToGrant = mutableListOf<String>()

                            if (grantedPermissions.contains(Manifest.permission.CAMERA) &&
                                request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
                            ) {
                                resourcesToGrant.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
                            }

                            if (grantedPermissions.contains(Manifest.permission.RECORD_AUDIO) &&
                                request.resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
                            ) {
                                resourcesToGrant.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
                            }

                            if (resourcesToGrant.isNotEmpty()) {
                                Log.d(
                                    "WebViewPermission",
                                    "Granting resources: ${resourcesToGrant.joinToString()}"
                                )
                                request.grant(resourcesToGrant.toTypedArray())
                            } else {
                                Log.d(
                                    "WebViewPermission",
                                    "No permissions granted; denying all resources."
                                )
                                request.deny()
                            }
                        }
                    }
                )

                onPermissionRequest(newRequest)
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (fullscreenView != null) {
                    callback?.onCustomViewHidden()
                    return
                }


//                originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                setOriginalOrientation(
                    activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                )

//                customViewCallback = callback
                setCustomViewCallback(callback)
                fullscreenView = view

                // B. Get the root view of the Activity and add our fullscreen view to it.
                val decorView = activity?.window?.decorView as? ViewGroup
                decorView?.addView(
                    fullscreenView,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )

                // C. Now, control the window
                val insetsController = activity?.let {
                    WindowCompat.getInsetsController(
                        it.window,
                        it.window.decorView
                    )
                }
                insetsController?.hide(WindowInsetsCompat.Type.systemBars())
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                // Tell the WebView to resume, as it might have paused.
                webView.onResume()
            }

            override fun onHideCustomView() {
                val decorView = activity?.window?.decorView as? ViewGroup
                decorView?.removeView(fullscreenView)
                fullscreenView = null

                val insetsController = activity?.let {
                    WindowCompat.getInsetsController(
                        it.window,
                        it.window.decorView
                    )
                }
                insetsController?.show(WindowInsetsCompat.Type.systemBars())

                resetCustomView()
//                activity?.requestedOrientation = originalOrientation
//
//                customViewCallback?.onCustomViewHidden()
//                customViewCallback = null

                webView.onResume()
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)

//                if (browserSettings.isDesktopMode && newProgress < 25) {
//                    view?.evaluateJavascript(
//                        JS_POINTER_FINE_OVERRIDE.trimIndent().replace("\n", ""), null
//                    )
//                }

                // Inject our JavaScript helper as the page is loading.

                val js =
                    "document.documentElement.style.setProperty('--vh', window.innerHeight + 'px');"
                view?.evaluateJavascript(js, null)


//                // Discontinue Desktop Mode
//                if (browserSettings.isDesktopMode) {
//                    // --- THIS IS THE FINAL, AGGRESSIVE SCRIPT ---
//                    val jsEnforceViewport = """
//            (function() {
//                function enforceDesktopViewport() {
//                    var meta = document.querySelector('meta[name=viewport]');
//                    if (!meta) {
//                        meta = document.createElement('meta');
//                        meta.setAttribute('name', 'viewport');
//                        document.getElementsByTagName('head')[0].appendChild(meta);
//                    }
//                    if (meta.getAttribute('content') !== 'width=${browserSettings.desktopModeWidth}') {
//                        meta.setAttribute('content', 'width=${browserSettings.desktopModeWidth}');
//                    }
//                }
//                enforceDesktopViewport();
//                var observer = new MutationObserver(function(mutations) {
//                    enforceDesktopViewport();
//                });
//                var head = document.getElementsByTagName('head')[0];
//                if (head) {
//                    observer.observe(head, { childList: true, subtree: true });
//                }
//            })();
//        """.trimIndent().replace("\n", "") // 2. Remove all newlines to create a single line.
//
//                    // 3. Evaluate the clean, single-line script.
//                    view?.evaluateJavascript(jsEnforceViewport, null)
//
//                }

            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    Log.d(
                        "WebViewConsole",
                        "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}"
                    )
                }
                return true
            }


        }

        webView.webViewClient = object : WebViewClient() {


            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (view != null) onPageStartedFun(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, currentUrlString: String?) {
                super.onPageFinished(view, currentUrlString)



                if (view != null) {
                    Log.e("Favicon", "onPageFinished")
                    view.evaluateJavascript(jsFaviconDiscovery, null)

                    onPageFinishedFun(view, currentUrlString)
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url ?: return false
                val urlString = url.toString()

//                if (url.scheme == "http" || url.scheme == "https") {
//                    if (browserSettings.isDesktopMode) {
//                        val extraHeaders = mutableMapOf<String, String>()
//                        extraHeaders["simulated_cursor"] = "true"
//
//                        // Manually load the URL with the extra header
//                        view?.loadUrl(urlString, extraHeaders)
//
//                        // Return true to tell the WebView we've handled the loading.
//                        return true
//                    } else {
//                        // Not in desktop mode, so no header needed. Let the WebView handle it normally.
//                        return false
//                    }
//                }


                if (url.scheme == "http" || url.scheme == "https") {
                    activeWebView?.loadUrl(url.toString())
                    return true
                }


                if (url.scheme == "intent") {
                    try {
                        val intent = Intent.parseUri(urlString, Intent.URI_INTENT_SCHEME)
                        view?.context?.startActivity(intent)
                    } catch (e: Exception) {
                        Log.w(
                            "shouldOverrideUrlLoading",
                            "Could not handle intent, trying fallback",
                            e
                        )
                        val packageName = try {
                            Intent.parseUri(urlString, Intent.URI_INTENT_SCHEME).`package`
                        } catch (parseEx: URISyntaxException) {
                            Log.e(
                                "shouldOverrideUrlLoading",
                                "Could not get package name from intent",
                                parseEx
                            )
                            null
                        }

                        if (packageName != null) {
                            try {
                                val marketIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    "market://details?id=$packageName".toUri()
                                )
                                view?.context?.startActivity(marketIntent)
                                view?.goBack()
                            } catch (marketError: Exception) {
                                Log.e(
                                    "shouldOverrideUrlLoading",
                                    "Could not open Play Store for package: $packageName",
                                    marketError
                                )
                            }
                        }
                    }
                    return true // We've handled the intent
                }

                // Handle other simple schemes like market://, mailto:// etc.
                try {
                    val intent = Intent(Intent.ACTION_VIEW, url)
                    // DO NOT add FLAG_ACTIVITY_NEW_TASK
                    view?.context?.startActivity(intent)

                    // Immediately go back to the previous page
                    view?.goBack()
                } catch (e: Exception) {
                    Log.w("WebView", "No app found to handle URL: $urlString", e)
                }
                return true
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {

                if (view != null) onDoUpdateVisitedHistoryFun(view, url, isReload)


                super.doUpdateVisitedHistory(view, url, isReload)


            }
        }
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            onDownloadRequested(url, userAgent, contentDisposition, mimeType, contentLength)
        }
    }
}

//endregion


//region Composable


class MainActivity : ComponentActivity() {

    private val tabManager by lazy { TabManager(this) }
    val newUrlFromIntent = MutableStateFlow<String?>(null)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        createNotificationChannel(this) // Call it here
        setContent {
            Themeinlowercase {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BrowserScreen(newUrlFlow = newUrlFromIntent)
                }
            }
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle intents that arrive while the app is already running
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Hide the system bars.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        // Configure the behavior for revealing them temporarily.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.dataString?.let { urlFromIntent ->
                Log.d("MainActivity", "Handling VIEW intent for URL: $urlFromIntent")
                // Instead of creating the tab here, we just emit the URL.
                // The Composable will react to this emission.
                newUrlFromIntent.update { urlFromIntent }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop called. Freezing all tabs.")
        // When the app goes to the background, freeze everything.
        tabManager.freezeAllTabs()
    }
}


@Composable
fun BrowserScreen(newUrlFlow: StateFlow<String?>, modifier: Modifier = Modifier) {


    //region Variables
    val context = LocalContext.current

    val webViewManager = remember { WebViewManager(context) }

    val sharedPrefs =
        remember { context.getSharedPreferences("BrowserPrefs", Context.MODE_PRIVATE) }
    var browserSettings by remember {
        mutableStateOf(
            BrowserSettings(
                paddingDp = sharedPrefs.getFloat("padding_dp", 8f),
                deviceCornerRadius = sharedPrefs.getFloat(
                    "corner_radius_dp",
                    pixel_9_corner_radius
                ),
                defaultUrl = sharedPrefs.getString("default_url", default_url)
                    ?: default_url,
                animationSpeed = sharedPrefs.getFloat("animation_speed", 300f),
                singleLineHeight = sharedPrefs.getFloat("single_line_height", 50f),
//                isDesktopMode = sharedPrefs.getBoolean("is_desktop_mode", false),
//                desktopModeWidth = sharedPrefs.getInt("desktop_mode_width", 820),
                isSharpMode = sharedPrefs.getBoolean("is_sharp_mode", false),
//                topSharpEdge = sharedPrefs.getFloat("top_sharp_edge", 65.90476f),
//                bottomSharpEdge = sharedPrefs.getFloat("bottom_sharp_edge", 65.90476f),
//                topSharpEdge = sharedPrefs.getFloat("top_sharp_edge", pixel_9_corner_radius),
//                bottomSharpEdge = sharedPrefs.getFloat("bottom_sharp_edge", pixel_9_corner_radius),
                cursorContainerSize = sharedPrefs.getFloat(
                    "cursor_container_size",
                    50f
                ),
                cursorPointerSize = sharedPrefs.getFloat("cursor_pointer_size", 5f),
                cursorTrackingSpeed = sharedPrefs.getFloat("cursor_tracking_speed", 1.75f),
            )
        )
    }

    var savedPanelState by remember { mutableStateOf<PanelVisibilityState?>(null) }


    val tabManager = remember { TabManager(context) }
    val tabs = remember {
        mutableStateListOf<Tab>().apply {
            addAll(tabManager.loadTabs(browserSettings.defaultUrl))
        }
    }
    val activeTabIndex = remember {
        mutableIntStateOf(tabs.indexOfFirst { it.state == TabState.ACTIVE }.coerceAtLeast(0))
    }

    val activeTab = tabs.getOrNull(activeTabIndex.intValue)

    val activeWebView = activeTab?.let { tab ->
        webViewManager.getWebView(tab).apply {
            // If the tab was frozen, its WebView was just created and needs to load its URL
            if (tab.state == TabState.FROZEN || this.url == null) {
                Log.d("BrowserScreen", "Loading URL for previously frozen tab: ${tab.currentUrl}")
//                this.loadUrl(tab.currentUrl ?: browserSettings.defaultUrl)
                webViewLoad(this, tab.currentUrl ?: browserSettings.defaultUrl, browserSettings)
            }
        }
    }

    var initialLoadDone by rememberSaveable { mutableStateOf(false) }

    var saveTrigger by remember { mutableIntStateOf(0) }


    val siteSettingsManager = remember { SiteSettingsManager(context) }
    val siteSettings = remember {
        mutableStateMapOf<String, SiteSettings>().apply {
            putAll(siteSettingsManager.loadSettings())
        }
    }


    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(tabs[activeTabIndex.intValue].currentUrl ?: "", TextRange(0)))
    }



    var isLoading by remember { mutableStateOf(false) }
    var isFocusOnTextField by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Density is needed to convert Px to Dp


    var isUrlBarVisible by rememberSaveable { mutableStateOf(true) }
    var isUrlOverlayBoxVisible by rememberSaveable { mutableStateOf(true) }
    var isPermissionPanelVisible by rememberSaveable { mutableStateOf(false) }
    var isBottomPanelVisible by rememberSaveable { mutableStateOf(true) }
    var isPromptPanelVisible by rememberSaveable { mutableStateOf(false) }
    var isTabsPanelVisible by remember { mutableStateOf(false) }
    var tabsPanelLock by remember { mutableStateOf(false) }


    var isNavPanelVisible by remember { mutableStateOf(false) }
    val isLongPressDrag = remember { mutableStateOf(false) }
    var activeNavAction by remember { mutableStateOf(GestureNavAction.REFRESH) }

    var isTabDataPanelVisible by remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current

    var isNavigateInProgress by rememberSaveable { mutableStateOf(false) }
    var isNavigateInProgressWithTabDataPanel by rememberSaveable { mutableStateOf(false) }


    var isOptionsPanelVisible by rememberSaveable { mutableStateOf(false) }
    var isSettingsPanelVisible by remember { mutableStateOf(false) }
    val offsetY = remember { Animatable(0f) }
    var overlayHeightPx by remember { mutableFloatStateOf(0f) }



    val animatedCornerRadius by animateDpAsState(
        targetValue = if (!browserSettings.isSharpMode) browserSettings.deviceCornerRadius.dp else 0.dp,
        label = "Corner Radius Animation",
    )

    // 1. Get the raw cutout padding values.
    val cutoutPaddingValues = WindowInsets.displayCutout.asPaddingValues()
    val cutoutTop = cutoutPaddingValues.calculateTopPadding()
    val cutoutBottom = cutoutPaddingValues.calculateBottomPadding()

    Log.i("cutoutTop", "$cutoutTop")

    val webViewTopPadding by animateDpAsState(
        targetValue = if (browserSettings.isSharpMode) (
                if (cutoutTop >= browserSettings.deviceCornerRadius.dp) cutoutTop else browserSettings.deviceCornerRadius.dp
                ) else cutoutTop,
        label = "WebView Top Padding Animation"
    )
    val webViewBottomPadding by animateDpAsState(
        targetValue = if (browserSettings.isSharpMode) (
                if (cutoutBottom >= browserSettings.deviceCornerRadius.dp) cutoutBottom else browserSettings.deviceCornerRadius.dp

                ) else cutoutBottom,
        label = "WebView Top Padding Animation"
    )


    var pendingPermissionRequest by remember {
        mutableStateOf<CustomPermissionRequest?>(null)
    }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }


    // We only need the CustomViewCallback as state now.
    var originalOrientation by remember { mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) }

    val activity = context as? Activity // Get the activity reference

    val isDarkTheme = isSystemInDarkTheme()
    val view = LocalView.current // Get the underlying view

    val colorScheme = ColorScheme(
        backgroundColor = if (isDarkTheme) Color.Black else Color.White,
        foregroundColor = if (isDarkTheme) Color.White else Color.Black
    )

    val canGoBack by remember {
        derivedStateOf {
            ((tabs[activeTabIndex.intValue].historyState?.currentIndex
                ?: 0) > 0) && !isNavigateInProgress
        }
    }
    val canGoForward by remember {
        derivedStateOf {
            val history = tabs[activeTabIndex.intValue].historyState
            if (history == null) false else (history.currentIndex < history.items.lastIndex) && !isNavigateInProgress
        }
    }

    databaseCurrentIndexHolder = tabs[activeTabIndex.intValue].historyState?.currentIndex ?: 0

    val savePermissionDecision = { domain: String, permissions: Map<String, Boolean> ->
        val currentSettings = siteSettings.getOrPut(domain) { SiteSettings(domain = domain) }
        currentSettings.permissionDecisions.putAll(permissions)
        siteSettings[domain] = currentSettings // Trigger state update
        siteSettingsManager.saveSettings(siteSettings)
        Log.d("PermissionSave", "Saved decisions for $domain: $permissions")
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            // When the system dialog returns a result, trigger the onResult
            // callback that we stored in our pendingPermissionRequest.

            pendingPermissionRequest?.let { request ->
                siteSettingsManager.getDomain(request.origin)?.let { domain ->
                    savePermissionDecision(domain, permissions)

                }
            }

            pendingPermissionRequest?.onResult?.invoke(permissions)

            // Clear the request to hide the panel.
            pendingPermissionRequest = null
        }
    )

    var squareAlignment by remember { mutableStateOf(Alignment.BottomEnd) }
    val squareAlpha = remember { Animatable(0f) }

    //  hold the currently active dialog
    var jsDialogState by remember { mutableStateOf<JsDialogState?>(null) }
    var promptComponentDisplayState by remember { mutableStateOf<JsDialogState?>(null) }


    val downloadTracker = remember { DownloadTracker(context) }
    val downloads =
        remember { mutableStateListOf<DownloadItem>().apply { addAll(downloadTracker.loadDownloads()) } }
    var isDownloadPanelVisible by remember { mutableStateOf(false) }


    var inspectingTabId by remember { mutableStateOf<Long?>(null) }

    val currentInspectingTab by remember {
        derivedStateOf {
            // This will re-run whenever inspectingTabId or the tabs list changes.
            inspectingTabId?.let { id ->
                tabs.find { it.id == id }
            }
        }
    }
    var confirmationState by remember { mutableStateOf<ConfirmationDialogState?>(null) }
    var confirmationDisplayState by remember { mutableStateOf<ConfirmationDialogState?>(null) } // Add this line


    val coroutineScope = rememberCoroutineScope()


    var isCursorPadVisible by remember { mutableStateOf(false) } // 1. Add new state for expansion
    var isCursorMode by remember { mutableStateOf(false) }
    val cursorPointerPosition = remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    var screenSizeDp by remember { mutableStateOf(IntSize.Zero) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val imeInsets = WindowInsets.ime.asPaddingValues()
    // 2. Convert the bottom padding (keyboard height) to Dp.
    val keyboardHeight = imeInsets.calculateBottomPadding()
    // 3. Check if the keyboard is currently visible.
    val isKeyboardVisible = keyboardHeight > 0.dp

    val cursorPadHeight by animateDpAsState(
        targetValue = if (isKeyboardVisible) ((screenSizeDp.height.dp - webViewTopPadding) / 8
                ) else (screenSizeDp.height.dp - webViewTopPadding) / 2,
        label = "Cursor Pad Height Animation"
    )

    val urlBarFocusRequester = remember { FocusRequester() } // <-- CREATE IT HERE


    //endregion
    // FUNCTIONS


    //region Functions


    fun confirmationPopup(message: String, onConfirm: () -> Unit, onCancel: () -> Unit = {}) {
        confirmationState = ConfirmationDialogState(
            message = message,
            onConfirm = {
                onConfirm()
                confirmationState = null // Automatically dismiss after action
            },
            onCancel = {
                onCancel()
                confirmationState = null // Automatically dismiss after action
            }
        )
        confirmationDisplayState = confirmationState
    }


    val handleHistoryNavigation = { tabToNavigate: Tab, historyIndex: Int ->
        val tabIndexInMainList = tabs.indexOf(tabToNavigate)

        // --- Use a positive case check ---
        if (tabIndexInMainList != -1 && !isNavigateInProgressWithTabDataPanel && !isNavigateInProgress) {

            isUrlBarVisible = false

            isNavigateInProgressWithTabDataPanel = true
            isNavigateInProgress = true
            val currentHistory = tabToNavigate.historyState

            if (currentHistory != null && historyIndex in currentHistory.items.indices) {
                // --- All checks passed, proceed with the logic ---

                // 1. Update the history state of the target tab
                val updatedHistory = currentHistory.copy(currentIndex = historyIndex)
                val updatedTab = tabToNavigate.copy(historyState = updatedHistory)
                tabs[tabIndexInMainList] = updatedTab
                saveTrigger++

                // 2. Make the target tab the active tab
                if (activeTabIndex.intValue != tabIndexInMainList) {
                    tabs[activeTabIndex.intValue].state = TabState.BACKGROUND
                    activeTabIndex.intValue = tabIndexInMainList
                    updatedTab.state = TabState.ACTIVE
                }

                // 3. Get the WebView instance and load the URL
                val webView = webViewManager.getWebView(updatedTab)
                val urlToLoad = updatedHistory.items[historyIndex].url
                webViewLoad(webView, urlToLoad, browserSettings)
//                webView.loadUrl(urlToLoad)

                // 4. Update the text field and close the panel
                textFieldValue = TextFieldValue(urlToLoad, TextRange(urlToLoad.length))

            }
            // If the inner 'if' fails (bad history), nothing happens, which is correct.
        }
        // If the outer 'if' fails (tab not found), nothing happens, which is correct.


    }


    val handlePermissionToggle = { domain: String?, permission: String, isGranted: Boolean ->

        Log.i("PermissionToggle", "$domain $permission $isGranted")

        if (domain != null) {
            // 1. Get the current settings for the domain, or create a new one if it doesn't exist.
            val currentSettings = siteSettings[domain] ?: SiteSettings(domain = domain)

            // 2. Create a new, updated map of permissions.
            val updatedPermissions = currentSettings.permissionDecisions.toMutableMap().apply {
                this[permission] = isGranted
            }

            // 3. Create a new SiteSettings object using copy() with the updated map.
            val newSettings = currentSettings.copy(permissionDecisions = updatedPermissions)

            // 4. Update the state map. This is the crucial step for triggering recomposition.
            siteSettings[domain] = newSettings

            // 5. Save the entire map to persistent storage.
            siteSettingsManager.saveSettings(siteSettings)




            confirmationPopup(
                message = "refresh?",
                onConfirm = {
                    activeWebView?.reload()
                    isUrlBarVisible = false
                },
                onCancel = {
                    // Do nothing, the popup will just dismiss.
                }
            )

        }

    }

    val handleCloseInspectedTab = {
        val tabToClose = currentInspectingTab
        if (tabToClose != null && tabs.indexOf(tabToClose) > -1) {
            confirmationPopup(
                message = "close tab?",
                onConfirm = {
                    val indexToClose = tabs.indexOf(tabToClose)
                    Log.i("CloseTab", "$indexToClose")

                    if (tabs.size > 1) {
                        val tabToRemoveIndex = indexToClose

                        val tabToRemove = tabToClose
                        webViewManager.destroyWebView(tabToRemove)
                        tabs.removeAt(tabToRemoveIndex)

                        // Determine the next active tab
                        if (tabToRemoveIndex == activeTabIndex.intValue) {
                            val nextTabIndex = if (tabToRemoveIndex >= tabs.size) {
                                tabs.lastIndex
                            } else {
                                tabToRemoveIndex
                            }


                            tabs[nextTabIndex].state = TabState.ACTIVE
                            inspectingTabId = tabs[nextTabIndex].id
                            activeTabIndex.intValue = nextTabIndex

//                    val urlToLoad = tabs[nextTabIndex].currentUrl ?: browserSettings.defaultUrl
//                    textFieldValue = TextFieldValue(urlToLoad, TextRange(urlToLoad.length))
//
//                    activeWebView?.loadUrl(urlToLoad)
                        } else
                            if (tabToRemoveIndex < activeTabIndex.intValue) {
                                activeTabIndex.intValue = activeTabIndex.intValue - 1
                            }


//                val urlToLoad = tabs[nextTabIndex].currentUrl ?: browserSettings.defaultUrl
//                activeWebView?.loadUrl(urlToLoad)
//                textFieldValue = TextFieldValue(urlToLoad, TextRange(urlToLoad.length))
                        saveTrigger++
                    } else {

                        // 1. Remove the last tab from the list.
                        tabs.clear()

                        // 2. Save the now-empty tab list.
                        tabManager.clearAllTabs()


                        // 3. Finish the activity to close the app.
                        activity?.finishAndRemoveTask()

                        exitProcess(0)

                    }
                }
            )
        }
    }

    val handleClearInspectedTabData = {
        confirmationPopup(
            message = "clear tab data?",
            onConfirm = {
                val inspectingTab = currentInspectingTab
                if (inspectingTab != null) {
                    val domain = siteSettingsManager.getDomain(inspectingTab.currentUrl)
                    if (domain != null) {
                        // 1. Remove the settings for this domain from the state map
                        siteSettings.remove(domain)
                        // 2. Save the updated map to persistent storage
                        siteSettingsManager.saveSettings(siteSettings)
                        Log.d("ClearData", "Cleared all saved permissions for domain: $domain")
                    }

                    if (inspectingTab.state != TabState.FROZEN) {
                        val webView = webViewManager.getWebView(inspectingTab)
                        CookieManager.getInstance().removeAllCookies(null)
                        CookieManager.getInstance().flush()
                        WebStorage.getInstance().deleteAllData()
                        webView.clearCache(true)
                        webView.reload()
                    }

                    isTabDataPanelVisible = false
                }
            }
        )
    }

    fun getBestGuessFilename(url: String, contentDisposition: String?, mimeType: String?): String {
        // 1. Try to parse the Content-Disposition header first.
        if (contentDisposition != null) {
            val pattern =
                Pattern.compile("filename\\*?=['\"]?([^'\"\\s]+)['\"]?", Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(contentDisposition)
            if (matcher.find()) {
                val filename = matcher.group(1)
                if (filename != null) {
                    try {
                        // Decode the filename in case it's URL-encoded
                        return URLDecoder.decode(filename, "UTF-8")
                    } catch (e: Exception) {
                        Log.e("DownloadManager", "Failed to decode filename", e)
                    }
                }
            }
        }

        // 2. If that fails, try to get the filename from the URL path.
        try {
            val path = url.toUri().path
            if (path != null) {
                val lastSegment = path.substringAfterLast('/')
                if (lastSegment.isNotBlank()) {
                    return lastSegment
                }
            }
        } catch (e: Exception) {
            Log.e("DownloadManager", "Failed to parse URL for filename", e)
        }


        // 3. As a last resort, use the original, less reliable method.
        return URLUtil.guessFileName(url, contentDisposition, mimeType)
    }

    /**
     * Generates a unique filename by checking against a list of existing downloads.
     * If "file.txt" exists, it will return "file (1).txt", then "file (2).txt", etc.
     */
    fun generateUniqueFilename(initialName: String, existingDownloads: List<DownloadItem>): String {
        val existingFilenames = existingDownloads.map { it.filename }.toSet()
        Log.i("DownloadManager", "initialName: $initialName")

        if (!existingFilenames.contains(initialName)) {
            return initialName // The original name is already unique
        }

        val baseName = initialName.substringBeforeLast('.')
        val extension = initialName.substringAfterLast('.', "")
        val finalExtension = if (extension.isNotEmpty()) ".$extension" else ""

        var counter = 1
        while (true) {
            val newName = "$baseName ($counter)$finalExtension"
            if (!existingFilenames.contains(newName)) {
                return newName
            }
            counter++
        }
    }


    // This function will be our single, safe way to update settings.
    val updateBrowserSettings = { newSettings: BrowserSettings ->
        browserSettings = newSettings
        Log.e("updateBrowserSettings", browserSettings.toString())
    }

    val resetBrowserSettings = {
        updateBrowserSettings(
            browserSettings.copy(
                paddingDp = 8f,
                deviceCornerRadius = pixel_9_corner_radius,
                defaultUrl = default_url,
                animationSpeed = 300f,
                singleLineHeight = 50f,
                isSharpMode = false,
                cursorContainerSize = 50f,
                cursorPointerSize = 5f,
                cursorTrackingSpeed = 1.75f
            )
        )
    }

    fun createNewTab(insertAtIndex: Int, url: String = browserSettings.defaultUrl) {
        if (activeTabIndex.intValue in tabs.indices) {
            tabs[activeTabIndex.intValue].state = TabState.BACKGROUND
        }

        val newTab = Tab(
            state = TabState.ACTIVE,
            historyState = SerializableBackForwardList(
                items = listOf(SerializableHistoryItem(url = url, title = "New Tab")),
                currentIndex = 0
            )
        )

        tabs.add(insertAtIndex, newTab)

        val newWebView = webViewManager.getWebView(newTab)
//        newWebView.loadUrl(url)
        webViewLoad(newWebView, url, browserSettings)

        inspectingTabId = newTab.id

        activeTabIndex.intValue = insertAtIndex


        textFieldValue = TextFieldValue(url, TextRange(url.length))
        saveTrigger++


    }


    fun navigateWebView() {
        when (activeNavAction) {
            GestureNavAction.BACK -> if (canGoBack) {
                tabs[activeTabIndex.intValue].historyState?.let { history ->
                    val newIndex =
                        history.currentIndex - 1
                    Log.i("Web View Navigation", "Back")

                    history.items.getOrNull(newIndex)
                        ?.let { itemToLoad ->
                            isNavigateInProgress = true
//                            activeWebView?.loadUrl(itemToLoad.url)
                            webViewLoad(activeWebView, itemToLoad.url, browserSettings)

                            val updatedTab =
                                tabs[activeTabIndex.intValue].copy(
                                    historyState = history.copy(
                                        currentIndex = newIndex
                                    )
                                )
                            tabs[activeTabIndex.intValue] =
                                updatedTab
                            saveTrigger++

                        }
                }
            }

            GestureNavAction.REFRESH -> {
                isNavigateInProgress = true

                activeWebView?.reload()
            }

            GestureNavAction.FORWARD -> if (canGoForward) {
                tabs[activeTabIndex.intValue].historyState?.let { history ->
                    val newIndex =
                        history.currentIndex + 1
                    Log.i("Web View Navigation", "FORWARD")
                    history.items.getOrNull(newIndex)
                        ?.let { itemToLoad ->
                            isNavigateInProgress = true
//                            activeWebView?.loadUrl(itemToLoad.url)
                            webViewLoad(activeWebView, itemToLoad.url, browserSettings)

                            val updatedTab =
                                tabs[activeTabIndex.intValue].copy(
                                    historyState = history.copy(
                                        currentIndex = newIndex
                                    )
                                )
                            tabs[activeTabIndex.intValue] =
                                updatedTab
                            saveTrigger++

                        }
                }
            }

            GestureNavAction.CLOSE_TAB -> {
                if (tabs.size > 1) {
                    val tabToRemoveIndex = activeTabIndex.intValue
                    val tabToRemove = tabs[tabToRemoveIndex]
                    webViewManager.destroyWebView(tabToRemove)
                    tabs.removeAt(tabToRemoveIndex)

                    // Determine the next active tab
                    val nextTabIndex = if (tabToRemoveIndex >= tabs.size) {
                        tabs.lastIndex
                    } else {
                        tabToRemoveIndex
                    }

                    activeTabIndex.intValue = nextTabIndex
                    tabs[nextTabIndex].state = TabState.ACTIVE

                    val urlToLoad = tabs[nextTabIndex].currentUrl ?: browserSettings.defaultUrl
//                    activeWebView?.loadUrl(urlToLoad)
                    webViewLoad(activeWebView, urlToLoad, browserSettings)

                    textFieldValue = TextFieldValue(urlToLoad, TextRange(urlToLoad.length))
                    saveTrigger++
                } else {

                    // 1. Remove the last tab from the list.
                    tabs.clear()

                    // 2. Save the now-empty tab list.
                    tabManager.clearAllTabs()


                    // 3. Finish the activity to close the app.
                    activity?.finishAndRemoveTask()

                    exitProcess(0)

                }
            }

            GestureNavAction.NEW_TAB -> {

                val newIndex = activeTabIndex.intValue + 1
                createNewTab(newIndex)
            }


            GestureNavAction.NONE -> { /* Do nothing */
            }
        }
    }

    val handleOpenDownloadsFolder = {
        val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            val genericFileManagerIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                val downloadsUri = android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI
                setDataAndType(downloadsUri, "*/*")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(genericFileManagerIntent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "Could not find a file manager app.", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
    // --- NEW: Handler to delete a specific file ---
    val handleDeleteFile = { item: DownloadItem ->
        if (item.isBlobDownload) {
            // It's a blob file we saved manually. Delete it from the filesystem.
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, item.filename)
            if (file.exists()) {
                if (file.delete()) {
                    // Also notify the MediaStore that the file is gone.
                    MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
                    Log.d("DeleteFile", "Blob file deleted successfully.")
                } else {
                    Log.w("DeleteFile", "Failed to delete blob file.")
                }
            }
        } else {
            // It's a standard download. Use the DownloadManager to remove it.
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.remove(item.id)
        }
        downloads.remove(item) // Removes from our UI list
        downloadTracker.saveDownloads(downloads) // Saves the change
        Toast.makeText(context, "${item.filename} deleted.", Toast.LENGTH_SHORT).show()
    }

    // --- NEW: Handler to clear the list (but not the files) ---
    val handleClearAll = {
        downloads.clear() // Clears our UI list
        downloadTracker.saveDownloads(downloads) // Saves the empty list
        Toast.makeText(context, "Download list cleared.", Toast.LENGTH_SHORT).show()
    }

    val handleOpenFile = { item: DownloadItem ->
        if (item.status == DownloadStatus.SUCCESSFUL) {

            val fileUri: Uri? = if (item.isBlobDownload) {
                // It's a blob file we saved manually. Use FileProvider.
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, item.filename)
                if (file.exists()) {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider", // Must match authority in manifest
                        file
                    )
                } else {
                    null
                }
            } else {
                // It's a standard download. Use the DownloadManager.
                val downloadManager =
                    context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.getUriForDownloadedFile(item.id)
            }

            if (fileUri != null) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, item.mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "No app found to open this file type.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(context, "File not found or has been deleted.", Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            Toast.makeText(context, "Download has not completed.", Toast.LENGTH_SHORT).show()
        }
    }


    val lastPollData = remember { mutableMapOf<Long, PollData>() }
    val backgroundColor = remember { mutableStateOf(Color.Transparent) }


    //endregion


    // This effect now ONLY handles the very first restoration of state.


    //region LaunchedEffect

    LaunchedEffect(isSettingsPanelVisible) {
        if (!isSettingsPanelVisible) {
            backgroundColor.value = Color.Transparent
        }
    }

    LaunchedEffect(isCursorMode) {

        isCursorPadVisible = isCursorMode
        if (isCursorMode) {
            isUrlBarVisible = false

        }
    }

    LaunchedEffect(Unit) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // This loop runs for the entire lifecycle of the screen
        while (true) {
            // Find downloads that need monitoring IN THIS CURRENT ITERATION
            val activeDownloads =
                downloads.filter { it.status == DownloadStatus.RUNNING || it.status == DownloadStatus.PENDING }

            if (activeDownloads.isEmpty()) {
                // If there's nothing to do, clear the tracker and just wait.
                // This prevents stale data if the app is backgrounded and resumed.
                if (lastPollData.isNotEmpty()) {
                    lastPollData.clear()
                }
            } else {
                // There are active downloads, so we poll them.
                val currentTimeMs = System.currentTimeMillis()
                var changed = false

                activeDownloads.forEach { item ->
                    val query = DownloadManager.Query().setFilterById(item.id)
                    downloadManager.query(query)?.use { cursor ->
                        if (cursor.moveToFirst()) {


                            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            val downloadedBytesIndex =
                                cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            val totalBytesIndex =
                                cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                            if (statusIndex == -1 || downloadedBytesIndex == -1 || totalBytesIndex == -1) {
                                Log.e(
                                    "DownloadPolling",
                                    "A required DownloadManager column is missing."
                                )
                                return@use
                            }

                            val downloadedBytes = cursor.getLong(downloadedBytesIndex)
                            val totalBytes = cursor.getLong(totalBytesIndex)
                            val statusInt = cursor.getInt(statusIndex)


                            var speedBps = item.downloadSpeedBps
                            var etrMs = item.timeRemainingMs

                            val lastData = lastPollData[item.id]

                            if (lastData != null) {
                                // Check if the actual downloaded bytes have changed since our last check
                                if (downloadedBytes > lastData.bytesDownloaded) {
                                    val timeDeltaMs = currentTimeMs - lastData.timestampMs
                                    val bytesDelta = downloadedBytes - lastData.bytesDownloaded

                                    if (timeDeltaMs > 0) {
                                        speedBps = (bytesDelta * 1000f) / timeDeltaMs
                                        if (totalBytes > 0) {
                                            val bytesRemaining = totalBytes - downloadedBytes
                                            etrMs = ((bytesRemaining / speedBps) * 1000).toLong()
                                        }
                                        // Update the tracker with the new data
                                        lastPollData[item.id] =
                                            PollData(currentTimeMs, downloadedBytes, speedBps)
                                    }
                                } else {
                                    // NO CHANGE in bytes. Keep displaying the last known good speed.
                                    // We also check if too much time has passed since the last update.
                                    // If so, we reset speed to 0, assuming a stall.
                                    if ((currentTimeMs - lastData.timestampMs) > 2000) { // 2 seconds threshold
                                        speedBps = 0f
                                        etrMs = 0L
                                        // Update the tracker so we don't keep resetting
                                        lastPollData[item.id] = lastData.copy(lastSpeedBps = 0f)
                                    }
                                }
                            } else {
                                // First poll, initialize
                                lastPollData[item.id] = PollData(currentTimeMs, downloadedBytes)
                            }

                            val status = when (statusInt) {
                                DownloadManager.STATUS_RUNNING -> DownloadStatus.RUNNING
                                DownloadManager.STATUS_PAUSED -> DownloadStatus.PAUSED
                                DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.SUCCESSFUL
                                DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                                else -> item.status
                            }
                            val progress =
                                if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt() else 0
                            val itemIndex = downloads.indexOfFirst { it.id == item.id }
                            Log.i("DownloadPolling", "Item index: $itemIndex")
                            Log.i("DownloadPolling", "speedBps: $speedBps")
                            Log.i("DownloadPolling", "etrMs: $etrMs")

                            if (itemIndex != -1) {
                                val updatedItem = downloads[itemIndex].copy(
                                    status = status,
                                    progress = progress,
                                    downloadedBytes = downloadedBytes,
                                    totalBytes = totalBytes
                                )
                                // 2. Manually set the transient data on the new object
                                updatedItem.downloadSpeedBps = speedBps
                                updatedItem.timeRemainingMs = etrMs

                                // 3. Replace the old item in the list. THIS TRIGGERS THE UI UPDATE.
                                downloads[itemIndex] = updatedItem
                                changed = true
                            }

                            if (status != DownloadStatus.RUNNING && status != DownloadStatus.PENDING) {
                                lastPollData.remove(item.id)
                            }
                        }
                    }
                }
                if (changed) {
                    downloadTracker.saveDownloads(downloads)
                }
            }

            // The delay is now at the end of the main while loop
            delay(100L)
        }
    }



    LaunchedEffect(inspectingTabId) {
        if (inspectingTabId == null) {
            isTabDataPanelVisible = false
        }
    }

    LaunchedEffect(activeWebView) {
        activeWebView?.let { webView ->

            val tab = activeTab
            val jsFaviconDiscovery = """
        (function() {
            let icon = document.querySelector("link[rel='apple-touch-icon']") ||
                       document.querySelector("link[rel='icon']") ||
                       document.querySelector("link[rel='shortcut icon']");
            WebAppFavicon.passFaviconUrl(icon ? icon.href : null);
        })();
        """.trimIndent()

            // Set up all the clients for the *current* active WebView.
            webViewManager.setWebViewClients(
                webView = webView,
                tab = tab, // Pass the active tab
                siteSettingsManager = siteSettingsManager,
                siteSettings = siteSettings,
                onFaviconChanged = { tabId, faviconUrl ->
                    // Find the index of the tab that fired this event.
                    val tabIndex = tabs.indexOfFirst { it.id == tabId }
                    if (tabIndex == -1) return@setWebViewClients

                    val targetTab = tabs[tabIndex]
                    val currentHistory = targetTab.historyState ?: return@setWebViewClients

                    // Get the index of the current page in that tab's history.
                    val historyIndex = currentHistory.currentIndex
                    if (historyIndex !in currentHistory.items.indices) return@setWebViewClients

                    // Get the specific history item that needs updating.
                    val itemToUpdate = currentHistory.items[historyIndex]

                    // Check if an update is even needed to prevent unnecessary recompositions.
                    if (faviconUrl.isNotBlank()) {
                        Log.d(
                            "Favicon",
                            "Update new favicon for ${itemToUpdate.url} to $faviconUrl"
                        )

                        val updatedItem = itemToUpdate.copy(faviconUrl = faviconUrl)
                        val updatedItems = currentHistory.items.toMutableList().apply {
                            this[historyIndex] = updatedItem
                        }
                        val updatedHistory = currentHistory.copy(items = updatedItems)
                        tabs[tabIndex] = targetTab.copy(historyState = updatedHistory)
                        saveTrigger++
                    } else {
                        Log.d(
                            "FaviconUpdate",
                            "Skipping favicon update for ${itemToUpdate.url}. An icon already exists or the new one is invalid."
                        )
                    }
                },
                onJsAlert = { message -> jsDialogState = JsAlert(message) },
                onJsConfirm = { message, onResult -> jsDialogState = JsConfirm(message, onResult) },
                onJsPrompt = { message, default, onResult ->
                    jsDialogState = JsPrompt(message, default, onResult)
                },
                onPermissionRequest = { request -> pendingPermissionRequest = request },
                setCustomViewCallback = { callback -> customViewCallback = callback },
                setOriginalOrientation = { orientation -> originalOrientation = orientation },
                resetCustomView = {
                    activity?.requestedOrientation = originalOrientation

                    customViewCallback?.onCustomViewHidden()
                    customViewCallback = null
                },
                onPageStartedFun = { view, url, favicon ->
                    pendingPermissionRequest?.let { request ->
                        // Check if the new URL's host is DIFFERENT from the origin of the permission request.
                        val newHost = url?.toUri()?.host
                        val requestHost = request.origin.toUri().host

                        if (newHost != requestHost) {
                            // The user is navigating away, so clear the old permission request.
                            Log.d(
                                "Permission Panel",
                                "Navigating away from permission origin. Clearing request."
                            )
                            pendingPermissionRequest = null
                        }
                    }
                    isLoading = true


                },
                onPageFinishedFun = { view, currentUrlString ->
                    isLoading = false
                    isNavigateInProgressWithTabDataPanel = false


                    view.evaluateJavascript(JS_HOVER_SIMULATOR.trimIndent().replace("\n", ""), null)

                },
                onDoUpdateVisitedHistoryFun = { view, url, isReload ->
                    Log.i("doUpdateVisitedHistory", "<<<<<<<<<<<<<<<<")
                    Log.i("doUpdateVisitedHistory", "<<<<<<<<<<<<<<<<")
                    Log.i("doUpdateVisitedHistory", "URL updated: $url")
                    Log.i("doUpdateVisitedHistory", "isReload: $isReload")
                    if (!isFocusOnTextField) view.url?.let {
                        textFieldValue = TextFieldValue(it, TextRange(it.length))
                    }

                    if (url == null) return@setWebViewClients


                    tabs[activeTabIndex.intValue].let { tab ->

                        var databaseHistory = tabs[activeTabIndex.intValue].historyState

                        if (databaseHistory == null) {
                            val items =
                                List(1) { SerializableHistoryItem(browserSettings.defaultUrl, "") }
                            databaseHistory = SerializableBackForwardList(
                                items = items,
                                currentIndex = 0
                            )
                        }
                        var updatedIndex: Int = databaseHistory.currentIndex


                        val realtimeHistory = view.copyBackForwardList()

                        if (realtimeHistory.size <= 1 && databaseHistory.items.size == 1 &&
                            (realtimeHistory.currentItem?.url == null || realtimeHistory.currentItem?.url == "about:blank")
                        ) {
                            Log.d(
                                "doUpdateVisitedHistory",
                                "Ignoring initial empty history update."
                            )
                            return@let
                        }


                        // LOG
                        Log.w("doUpdateVisitedHistory", "Realtime History:")
                        Log.i(
                            "doUpdateVisitedHistory",
                            "Current Index ${realtimeHistory.currentIndex}"
                        )
                        for (i in 0 until realtimeHistory.size) {
                            val item = realtimeHistory.getItemAtIndex(i)
                            val marker =
                                if (realtimeHistory.currentIndex == i) " << Current" else " "
                            Log.i(
                                "doUpdateVisitedHistory",
                                "$i. URL: ${item.url} << ${item.title} >> $marker"
                            )
                        }

                        Log.w("doUpdateVisitedHistory", "Database History:")
                        Log.i(
                            "doUpdateVisitedHistory",
                            "Current Index ${databaseHistory.currentIndex}"
                        )

                        for (i in 0 until databaseHistory.items.size) {
                            val item = databaseHistory.items[i]
                            val marker =
                                if (databaseHistory.currentIndex == i) " << Current" else " "
                            Log.i("doUpdateVisitedHistory", "$i. URL: ${item.url} $marker")
                        }
                        Log.i("doUpdateVisitedHistory", "")

                        val databaseCurrentItemUrl =
                            databaseHistory.items[databaseHistory.currentIndex].url
                        val realtimeCurrentItemUrl =
                            realtimeHistory.getItemAtIndex(realtimeHistory.currentIndex).url

                        var updatedHistoryItems = databaseHistory.items.toMutableList()

                        if (databaseCurrentItemUrl == realtimeCurrentItemUrl) {
                            Log.e("doUpdateVisitedHistory", "Same URl - Do Nothing")
                            isNavigateInProgress = false
                            return@let
                        } else {
                            var realtimePreviousItemUrl = " marcinlowercase "

                            if (realtimeHistory.currentIndex != 0) {
                                realtimePreviousItemUrl =
                                    realtimeHistory.getItemAtIndex(realtimeHistory.currentIndex - 1).url
                            }

                            if (databaseCurrentItemUrl == realtimePreviousItemUrl) {
                                Log.e("doUpdateVisitedHistory", "Add new url to database")
                                if (databaseHistory.currentIndex < databaseHistory.items.lastIndex) {
                                    updatedHistoryItems = databaseHistory.items.subList(
                                        0,
                                        databaseHistory.currentIndex + 1
                                    ).toMutableList()
                                }
                                Log.e(
                                    "doUpdateVisitedHistory",
                                    "Title ${realtimeHistory.currentItem?.title}"
                                )
                                updatedHistoryItems.add(
                                    SerializableHistoryItem(
                                        url = realtimeCurrentItemUrl,
                                        title = realtimeHistory.currentItem?.title ?: "oo"
                                    )
                                )
                                updatedIndex++
                                databaseCurrentIndexHolder = updatedIndex

                            } else {

                                Log.e(
                                    "doUpdateVisitedHistory",
                                    "realTimePreviousItemUrl: $realtimePreviousIndexHolder"
                                )
                                Log.e(
                                    "doUpdateVisitedHistory",
                                    "databaseCurrentItemUrl: ${realtimeHistory.currentIndex}"
                                )
                                if (realtimePreviousIndexHolder > realtimeHistory.currentIndex) {
                                    Log.e("doUpdateVisitedHistory", "Back by Webview")

                                    updatedIndex--
                                } else {
                                    Log.e(
                                        "doUpdateVisitedHistory",
                                        "Update existing url in database"
                                    )
                                    updatedHistoryItems[updatedIndex] = SerializableHistoryItem(
                                        url = realtimeCurrentItemUrl,
                                        title = realtimeHistory.currentItem?.title ?: "oo"
                                    )
                                }


                            }
                            val updatedHistoryState = SerializableBackForwardList(
                                items = updatedHistoryItems,
                                currentIndex = updatedIndex
                            )
                            if (databaseHistory != updatedHistoryState) {

                                tabs[activeTabIndex.intValue] =
                                    tab.copy(historyState = updatedHistoryState)
                                saveTrigger++


                                val newDatabaseHistory = tabs[activeTabIndex.intValue].historyState
                                if (newDatabaseHistory == null) {
                                    return@let
                                }
                                Log.w("doUpdateVisitedHistory", "NEW Database History:")
                                Log.i(
                                    "doUpdateVisitedHistory",
                                    "Current Index ${newDatabaseHistory.currentIndex}"
                                )

                                for (i in 0 until newDatabaseHistory.items.size) {
                                    val item = newDatabaseHistory.items[i]
                                    val marker =
                                        if (newDatabaseHistory.currentIndex == i) " << Current" else " "
                                    Log.i("doUpdateVisitedHistory", "$i. URL: ${item.url} $marker")
                                }
                                Log.i("doUpdateVisitedHistory", "")

                            }
                        }


                        Log.i("doUpdateVisitedHistory", ">>>>>>>>>>>>>>>")
                        Log.i("doUpdateVisitedHistory", "")
                        Log.i("doUpdateVisitedHistory", "")

                        realtimePreviousIndexHolder = realtimeHistory.currentIndex
                    }
                },
                onTitleReceived = { view, url, title ->
                    val tabIndex = tabs.indexOfFirst { it.id == activeTab.id }
                    if (tabIndex == -1) return@setWebViewClients

                    val targetTab = tabs[tabIndex]
                    val currentHistory = targetTab.historyState ?: return@setWebViewClients

                    // Find the specific history item that matches this URL.
                    val historyIndexToUpdate = currentHistory.items.indexOfFirst { it.url == url }
                    if (historyIndexToUpdate == -1) return@setWebViewClients

                    val itemToUpdate = currentHistory.items[historyIndexToUpdate]

                    // Only update if the title has actually changed to prevent loops
                    if (itemToUpdate.title != title) {
                        Log.d("onReceivedTitle", "Updating title for '$url' to '$title'")
                        val updatedItem = itemToUpdate.copy(title = title)
                        val updatedItems = currentHistory.items.toMutableList().apply {
                            this[historyIndexToUpdate] = updatedItem
                        }
                        val updatedHistory = currentHistory.copy(items = updatedItems)
                        tabs[tabIndex] = targetTab.copy(historyState = updatedHistory)
                        saveTrigger++
                    }

                    view.evaluateJavascript(jsFaviconDiscovery, null)


                },

                onBlobDownloadRequested = { base64Data, filename, mimeType ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    try {
                        val fileData = Base64.decode(base64Data, Base64.DEFAULT)
                        val downloadsDir =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        if (!downloadsDir.exists()) downloadsDir.mkdirs()

                        // 1. Generate a unique filename
                        val finalFilename = generateUniqueFilename(filename, downloads)
                        val file = File(downloadsDir, finalFilename)

                        // 2. Save the file
                        FileOutputStream(file).use { it.write(fileData) }

                        // 3. Notify the MediaStore
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(file.absolutePath),
                            arrayOf(mimeType),
                            null
                        )

                        // 1. Build the intent and notification first
                        val fileUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val openIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(fileUri, mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            openIntent,
                            PendingIntent.FLAG_IMMUTABLE
                        )

                        val notification = NotificationCompat.Builder(context, "download_channel")
                            .setSmallIcon(R.drawable.ic_download_done)
                            .setContentTitle(finalFilename)
                            .setContentText("download complete")
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build()

                        // 2. Check for permission BEFORE calling .notify()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            // For Android 13 and above
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                // We have permission, so we can safely show the notification
                                NotificationManagerCompat.from(context)
                                    .notify(System.currentTimeMillis().toInt(), notification)
                            } else {
                                // We don't have permission, so we request it.
                                // The notification will NOT be shown this time, but will work on the next download if granted.
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                // Optionally, show a toast to inform the user that the file was saved.
                                Toast.makeText(
                                    context,
                                    "Downloaded: $finalFilename",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            // For older versions, no permission is needed
                            NotificationManagerCompat.from(context)
                                .notify(System.currentTimeMillis().toInt(), notification)
                        }
                        // 4. CRUCIAL: Create a DownloadItem and add it to your UI state list
                        val newDownload = DownloadItem(
                            id = System.currentTimeMillis(), // Use timestamp for ID as there's no DownloadManager ID
                            url = "blob:...", // You can store a placeholder URL
                            filename = finalFilename,
                            mimeType = mimeType,
                            status = DownloadStatus.SUCCESSFUL,// It's instantly successful
                            isBlobDownload = true,
                            progress = 100,
                            totalBytes = fileData.size.toLong(),
                            downloadedBytes = fileData.size.toLong()
                        )
                        downloads.add(0, newDownload)
                        downloadTracker.saveDownloads(downloads) // Save the updated list

                        Toast.makeText(context, "Downloaded: $finalFilename", Toast.LENGTH_LONG)
                            .show()

                    } catch (e: Exception) {
                        Log.e("BlobDownloader", "Failed to save blob file from callback", e)
                        Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show()
                    }
                },
                onDownloadRequested = { url, userAgent, contentDisposition, mimeType, contentLength ->

                    if (url.startsWith("blob:")) {
                        val filename = getBestGuessFilename(url, contentDisposition, mimeType)

                        // This JavaScript reads the blob, converts it to Base64, and calls our Kotlin interface.
                        val js = """
            javascript:
            (async () => {
                const response = await fetch('$url');
                const blob = await response.blob();
                const reader = new FileReader();
                reader.onload = () => {
                    // The result includes the Base64 prefix, so we remove it.
                    const base64Data = reader.result.split(',')[1];
                    BlobDownloader.downloadBase64File(base64Data, '$filename', '$mimeType');
                };
                reader.readAsDataURL(blob);
            })();
        """.trimIndent()


                        // Execute the JavaScript in the WebView
                        webView.evaluateJavascript(js, null)

                    } else {
                        val initialFilename =
                            getBestGuessFilename(url, contentDisposition, mimeType)

                        // 2. Generate a guaranteed unique filename using our helper
                        val finalFilename = generateUniqueFilename(initialFilename, downloads)


//                        Toast.makeText(context, "Downloading $finalFilename", Toast.LENGTH_SHORT)
//                            .show()

                        // 3. Use the final, unique filename for the DownloadManager request
                        val request = DownloadManager.Request(url.toUri())
                            .setTitle(finalFilename) // Use unique name
                            .setDescription("Downloading...")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                finalFilename
                            ) // Use unique name
                            .addRequestHeader("User-Agent", userAgent)

                        val downloadManager =
                            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val downloadId = downloadManager.enqueue(request)

                        // 4. Use the final, unique filename for our internal state object
                        val newDownload = DownloadItem(
                            id = downloadId,
                            url = url,
                            filename = finalFilename, // Use unique name
                            mimeType = mimeType,
                            status = DownloadStatus.PENDING
                        )
                        downloads.add(0, newDownload)
                        downloadTracker.saveDownloads(downloads)
                    }

                },
            )
            webView.onWebViewTouch = {
                if (isUrlBarVisible) isUrlBarVisible = false
            }
        }
    }

    LaunchedEffect(jsDialogState) {
        if (jsDialogState != null) {
            promptComponentDisplayState = jsDialogState
        }
    }
    LaunchedEffect(isUrlBarVisible) {
        if (!isUrlBarVisible) {
            isOptionsPanelVisible = false
            isTabDataPanelVisible = false
            isTabsPanelVisible = false
            isSettingsPanelVisible = false
        } else {
            if (tabsPanelLock) isTabsPanelVisible = true
            if (isCursorMode) isCursorMode = false
        }
    }
    LaunchedEffect(jsDialogState) {
        isPromptPanelVisible = jsDialogState != null
    }

    LaunchedEffect(
        isUrlBarVisible,
        isPermissionPanelVisible,
        isPromptPanelVisible,
        confirmationState
    ) {
        isBottomPanelVisible =
            isUrlBarVisible || isPermissionPanelVisible || isPromptPanelVisible || (confirmationState != null)
//        Log.i("VisibleState", "isBottomPanelVisible: $isBottomPanelVisible")
    }
    LaunchedEffect(isTabsPanelVisible) {
        if (!isTabsPanelVisible) {
            isTabDataPanelVisible = false
        }
    }


    suspend fun triggerBlinkEffect() {
        squareAlpha.snapTo(0.7f)

        // b. Wait a moment so the user can see it before it blinks.
        delay(400)

        // c. Blink twice.
        repeat(2) {
            // Fade out
            squareAlpha.animateTo(0f, animationSpec = tween(durationMillis = 300))
            // Fade back in
            squareAlpha.animateTo(0.7f, animationSpec = tween(durationMillis = 300))
        }
        squareAlpha.animateTo(0f, animationSpec = tween(durationMillis = 400))

    }
    LaunchedEffect(pendingPermissionRequest) {
        Log.i("Permission Panel", "pendingPermissionRequest: $pendingPermissionRequest")
        isPermissionPanelVisible = pendingPermissionRequest != null
    }
    // This effect will re-launch whenever isBottomPanelVisible changes.
    LaunchedEffect(isBottomPanelVisible, squareAlignment) {
        if (!isBottomPanelVisible) {
            // -- The URL bar has just been hidden. Start the "show and blink" sequence. --

            // a. Instantly appear with 0.6 opacity.
            if (!isCursorMode) triggerBlinkEffect()

            // d. After blinking, fade out completely.
        } else {
            // -- The URL bar is visible. Ensure the square is fully transparent. --
            squareAlpha.snapTo(0f)
        }
    }
    // This effect runs once and whenever isDarkTheme changes.
    LaunchedEffect(isDarkTheme) {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)

        // true for light theme (dark icons), false for dark theme (light icons)
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme
    }

    LaunchedEffect(overlayHeightPx) {
        // We only want to act the first time the height is measured (it changes from 0f to a positive value).
        // The `offsetY.value == 0f` check is an extra safeguard to ensure we only do this once on startup.
        if (overlayHeightPx > 0f && offsetY.value == 0f) {
            // Instantly "snap" the overlay to its hidden position without any animation.
            // The hidden position is its full height negated, moving it off-screen upwards.
            offsetY.snapTo(-overlayHeightPx * 2)
        }
    }

//    LaunchedEffect(browserSettings.isDesktopMode) {
//        if (browserSettings.isDesktopMode) {
//            activeWebView?.settings?.userAgentString = desktopUserAgent
//            activeWebView?.settings?.useWideViewPort = true
//            activeWebView?.settings?.loadWithOverviewMode = true
//            webViewLoad(activeWebView, activeWebView?.url ?: "", browserSettings)
//        } else {
//            activeWebView?.settings?.userAgentString = mobileUserAgent
//            activeWebView?.settings?.useWideViewPort = false
//            activeWebView?.settings?.loadWithOverviewMode = false
//            activeWebView?.reload()
//        }
//
//    }
    LaunchedEffect(Unit) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        // Hide the system bars permanently
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Configure the swipe-to-reveal behavior
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    LaunchedEffect(saveTrigger) {
        if (saveTrigger > 0) {
            tabManager.saveTabs(tabs, activeTabIndex.intValue)
            saveTrigger = 0
        }
    }

    LaunchedEffect(Unit) {
        newUrlFlow.collect { url ->
            if (url != null) {
                Log.d("BrowserScreen", "New URL collected from flow: $url")

                // When a new URL arrives, create a new tab for it.
                // We'll insert it right after the current active tab.
                val insertIndex = (activeTabIndex.intValue + 1).coerceAtMost(tabs.size)
                createNewTab(insertIndex, url)

                // IMPORTANT: Consume the event by setting the flow back to null
                (context as MainActivity).newUrlFromIntent.update { null }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!initialLoadDone) {

            val urlToLoad = tabs[activeTabIndex.intValue].currentUrl ?: browserSettings.defaultUrl
//            activeWebView?.loadUrl(urlToLoad)
            webViewLoad(activeWebView, urlToLoad, browserSettings)
            initialLoadDone = true

        }
    }

    // The LaunchedEffect now saves the entire settings object (or individual fields)
    LaunchedEffect(browserSettings) {
        sharedPrefs.edit {
            putFloat("padding_dp", browserSettings.paddingDp)
            putFloat("corner_radius_dp", browserSettings.deviceCornerRadius)
            putString("default_url", browserSettings.defaultUrl)
            putFloat("animation_speed", browserSettings.animationSpeed)
            putFloat("single_line_height", browserSettings.singleLineHeight)
//            putInt("desktop_mode_width", browserSettings.desktopModeWidth)
            putBoolean("is_sharp_mode", browserSettings.isSharpMode)
//            putFloat("top_sharp_edge", browserSettings.topSharpEdge)
//            putFloat("bottom_sharp_edge", browserSettings.bottomSharpEdge)
            putFloat("cursor_container_size", browserSettings.cursorContainerSize)
            putFloat("cursor_pointer_size", browserSettings.cursorPointerSize)
            putFloat("cursor_tracking_speed", browserSettings.cursorTrackingSpeed)


        }
    }

    LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }


    LaunchedEffect(canGoBack, canGoForward) {
        Log.i("Web View Navigation", "canGoBack $canGoBack")
        Log.i("Web View Navigation", "canGoForward $canGoForward")
    }

    //endregion
    BackHandler(enabled = !isBottomPanelVisible || canGoBack) {
        when {
            // Priority 1: Exit fullscreen video if it's active.
            customView != null -> {
                customViewCallback?.onCustomViewHidden()

            }
            // Priority 2: Navigate back in the WebView.
            canGoBack -> {
                tabs[activeTabIndex.intValue].historyState?.let { history ->
                    val newIndex =
                        history.currentIndex - 1
                    Log.i("Web View Navigation", "NACK")
                    databaseCurrentIndexHolder = newIndex
                    history.items.getOrNull(newIndex)
                        ?.let { itemToLoad ->
                            isNavigateInProgress = true
//                            activeWebView?.loadUrl(itemToLoad.url)

                            webViewLoad(activeWebView, itemToLoad.url, browserSettings)

                            val updatedTab =
                                tabs[activeTabIndex.intValue].copy(
                                    historyState = history.copy(
                                        currentIndex = newIndex
                                    )
                                )
                            tabs[activeTabIndex.intValue] =
                                updatedTab
                            saveTrigger++
                        }
                }
            }

            else -> {
            }
        }
    }


    //
    //
    // LAYOUT
    //
    //
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor.value)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
//                .padding(top = cutoutTop, bottom = cutoutBottom)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(top = webViewTopPadding, bottom = webViewBottomPadding)


            ) {


                // Webview Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(
                            RoundedCornerShape(
                                animatedCornerRadius
                            )
                        )
                        .testTag("WebViewContainer")

                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()

                    ) {


                        AndroidView(
                            // The factory now ONLY creates the container. It's simple.
                            factory = { context ->
                                FrameLayout(context)
                            },
                            // --- THIS UPDATE BLOCK IS THE FIX ---
                            // It's called on the initial composition AND every time
                            // 'activeWebView' changes.
                            update = { frameLayout ->
                                // Check if the correct WebView is already being shown.
                                // This prevents unnecessary add/remove operations.
                                if (frameLayout.getChildAt(0) != activeWebView) {
                                    // 1. Safely remove the new WebView from its old parent, if any.
                                    (activeWebView?.parent as? ViewGroup)?.removeView(activeWebView)
                                    // 2. Clear out the old WebView from our container.
                                    frameLayout.removeAllViews()
                                    // 3. Add the new, correct WebView to our container.
                                    frameLayout.addView(
                                        activeWebView,
                                        FrameLayout.LayoutParams(
                                            FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT
                                        ).apply {
                                            gravity = Gravity.CENTER
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LoadingOverlay(isLoading = isLoading, colorScheme = colorScheme)
                }
            }


            CursorPointer(
                isCursorPadVisible = isCursorPadVisible,
                position = cursorPointerPosition.value,
                browserSettings = browserSettings,
            )



            BottomPanel(
                confirmationPopup = ::confirmationPopup,
                resetBrowserSettings = resetBrowserSettings,
                backgroundColor = backgroundColor,
                isSettingsPanelVisible = isSettingsPanelVisible,
                setIsSettingsPanelVisible = { isSettingsPanelVisible = it },
                urlBarFocusRequester = urlBarFocusRequester,
                isCursorPadVisible = isCursorPadVisible,
                isCursorMode = isCursorMode,
                setIsCursorMode = {
                    isCursorMode = it

                },
                setIsOptionsPanelVisible = { isOptionsPanelVisible = it },
                setIsTabsPanelVisible = { isTabsPanelVisible = it },
                setIsDownloadPanelVisible = { isDownloadPanelVisible = it },
                setIsTabDataPanelVisible = { isTabDataPanelVisible = it },
                savedPanelState = savedPanelState,
                setSavedPanelState = { savedPanelState = it },
                confirmationState = confirmationState,
                confirmationDisplayState = confirmationDisplayState,
                onPermissionDeny = {
                    pendingPermissionRequest?.let { request ->
                        // --- SAVE THE DENIAL (NEW) ---
                        siteSettingsManager.getDomain(request.origin)?.let { domain ->
                            val deniedPermissions =
                                request.permissionsToRequest.associateWith { false }
                            savePermissionDecision(domain, deniedPermissions)
                        }
                        // --- END OF NEW LOGIC ---
                        request.onResult.invoke(emptyMap())
                    }
                    pendingPermissionRequest = null
                },
                tabsPanelLock = tabsPanelLock,
                updateInspectingTab = { tab ->
                    if (tab.id != 0.toLong()) inspectingTabId = tab.id else {
                        isTabDataPanelVisible = false
                    }

                },
                isTabDataPanelVisible = isTabDataPanelVisible,
                inspectingTab = currentInspectingTab,
                handleCloseInspectedTab = handleCloseInspectedTab,
                handleClearInspectedTabData = handleClearInspectedTabData,
                handlePermissionToggle = handlePermissionToggle,
                siteSettings = siteSettings,
                onTabDataPanelDismiss = { isTabDataPanelVisible = false },

                onTabLongPressed = { tab ->
                    isTabDataPanelVisible = !isTabDataPanelVisible
                    if (inspectingTabId == null) inspectingTabId = tab.id
                },
                onDownloadRowClicked = handleOpenFile,
                onDeleteClicked = handleDeleteFile,
                onOpenFolderClicked = handleOpenDownloadsFolder,
                onClearAllClicked = handleClearAll,
                downloads = downloads,
                isDownloadPanelVisible = isDownloadPanelVisible,
                activeWebView = activeWebView,
                isUrlOverlayBoxVisible = isUrlOverlayBoxVisible,
                setIsUrlOverlayBoxVisible = { isUrlOverlayBoxVisible = it },
                onNewTabClicked = { index ->
                    createNewTab(index)
                },
                toggleIsTabsPanelVisible = {
                    isTabsPanelVisible = !isTabsPanelVisible
                    tabsPanelLock = !tabsPanelLock
                },
                isTabsPanelVisible = isTabsPanelVisible,
                onTabSelected = { newIndex ->
                    if (activeTabIndex.intValue != newIndex) {
                        tabs[activeTabIndex.intValue].state = TabState.BACKGROUND
                        tabs[newIndex].state = TabState.ACTIVE

                        activeTabIndex.intValue = newIndex

                        val urlToLoad = tabs[newIndex].currentUrl ?: browserSettings.defaultUrl
//                        activeWebView?.loadUrl(urlToLoad)
                        textFieldValue = TextFieldValue(urlToLoad, TextRange(urlToLoad.length))
                        saveTrigger++

                        inspectingTabId = tabs[newIndex].id
                    }

                },
                navigateWebView = {
                    navigateWebView()
                },
                hapticFeedback = hapticFeedback,
                setIsNavPanelVisible = { isNavPanelVisible = it },
                setActiveNavAction = { activeNavAction = it },

                canGoBack = canGoBack,
                canGoForward = canGoForward,
                isNavPanelVisible = isNavPanelVisible,
                activeNavAction = activeNavAction,

                state = if (jsDialogState != null) jsDialogState!! else null,
                promptComponentDisplayState = if (promptComponentDisplayState != null) promptComponentDisplayState else null,
                onDismiss = { jsDialogState = null },
                isPromptPanelVisible = isPromptPanelVisible,

                isPermissionPanelVisible = isPermissionPanelVisible,
                permissionLauncher = permissionLauncher,
                pendingPermissionRequest = pendingPermissionRequest,
                modifier = Modifier
                    // This aligns the panel to the bottom center of the Box
                    .windowInsetsPadding(WindowInsets.ime)
                    .align(Alignment.BottomCenter),
                activeTabIndex = activeTabIndex,
                tabs = tabs,
                isUrlBarVisible = isUrlBarVisible,
                isBottomPanelVisible = isBottomPanelVisible,
                isOptionsPanelVisible = isOptionsPanelVisible,
                browserSettings = browserSettings,
                updateBrowserSettings = updateBrowserSettings,
                textFieldValue = textFieldValue,
//                        url = url,
                focusManager = focusManager,
                keyboardController = keyboardController,
                toggleOptionsPanel = { isOptionsPanelVisible = it },
                changeTextFieldValue = { textFieldValue = it },
                onNewUrl = { newUrl ->
                    webViewLoad(activeWebView, newUrl, browserSettings)

                },
                setIsFocusOnTextField = { isFocusOnTextField = it },
                handleHistoryNavigation = handleHistoryNavigation,


                )



            CursorPad(
                urlBarFocusRequester = urlBarFocusRequester,
                screenSize = screenSize,
                isCursorPadVisible = isCursorPadVisible,
                setIsCursorPadVisible = { isCursorMode = it },
                browserSettings = browserSettings,
                coroutineScope = coroutineScope,
                activeWebView = activeWebView,
                cursorPointerPosition = cursorPointerPosition,
                webViewTopPadding = webViewTopPadding,
                hapticFeedback = hapticFeedback,
                setIsUrlBarVisible = { isUrlBarVisible = it },
                isLongPressDrag = isLongPressDrag,
                cursorPadHeight = cursorPadHeight,

                )

            LaunchedEffect(screenSize) {
                Log.i("BackSquare", "Screen Size : $screenSize")

            }
            // BackSquare
            AnimatedVisibility(
                visible = !isBottomPanelVisible,
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        screenSize = it
                        with(density) {
                            screenSizeDp = IntSize(
                                it.width.toDp().value.roundToInt(),
                                it.height.toDp().value.roundToInt()
                            )
                        }
                        Log.d(
                            "BackSquare",
                            "Screen Size: ${screenSize.width}x${screenSize.height} px | ${screenSizeDp.width}x${screenSizeDp.height} dp"
                        )
                    },
                enter = slideInHorizontally(
                    animationSpec = tween(
                        animationSpeedForLayer(
                            0,
                            browserSettings.animationSpeed
                        )
                    ),
                    initialOffsetX = { if (squareAlignment == Alignment.BottomEnd) it else (-it) }
                ) + fadeIn(
                    animationSpec = tween(
                        animationSpeedForLayer(
                            0,
                            browserSettings.animationSpeed
                        )
                    )
                ),
                exit =
                    slideOutHorizontally(
                        animationSpec = tween(
                            animationSpeedForLayer(
                                0,
                                browserSettings.animationSpeed
                            )

                        ),
                        targetOffsetX = { if (squareAlignment == Alignment.BottomEnd) it else (-it) }
                    )
                            + fadeOut(
                        animationSpec = tween(
                            animationSpeedForLayer(
                                0,
                                browserSettings.animationSpeed
                            )
                        )
                    )
            ) {


                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {

                    val squareBoxSmallHeight =
                        heightForLayer(
                            1,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp,
                            browserSettings.singleLineHeight,
                        ).dp

                    Box(
                        modifier = Modifier
                            .align(squareAlignment)

                            .animateContentSize(tween(animationSpeedForLayer(1, browserSettings.animationSpeed)))
                            .height(squareBoxSmallHeight)
                            .width(screenSizeDp.width.dp * 0.45f)
                            .graphicsLayer {
                                alpha = squareAlpha.value
                            }
                            .pointerInput(Unit, isCursorMode) {

                                if (!isCursorMode) awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)

                                    val longPressJob = coroutineScope.launch {
                                        delay(viewConfiguration.longPressTimeoutMillis)
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isCursorPadVisible = true
                                        squareAlpha.snapTo(0f)

                                        val initialCursorX =
                                            if (squareAlignment == Alignment.BottomEnd) (
                                                    screenSize.width - ((screenSize.width * 0.45f) + browserSettings.paddingDp.dp.toPx()) + down.position.x
                                                    )
                                            else browserSettings.paddingDp.dp.toPx() + down.position.x


                                        val initialCursorY =
                                            screenSize.height - (squareBoxSmallHeight.toPx() - browserSettings.paddingDp.dp.toPx()) + down.position.y - ((screenSize.height - cutoutTop.toPx()) / 2)



                                        cursorPointerPosition.value =
                                            Offset(initialCursorX, initialCursorY)


                                    }

                                    val drag = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                                        if (longPressJob.isActive) {
                                            longPressJob.cancel()
                                        }
                                        change.consume()
                                    }

                                    if (longPressJob.isCompleted && !longPressJob.isCancelled) {
                                        // --- LONG-PRESS PATH ---
                                        if (drag != null) {
                                            drag(drag.id) { change ->
                                                change.consume()


                                                val changeSpaceX =
                                                    (change.position.x - change.previousPosition.x) * browserSettings.cursorTrackingSpeed
                                                val changeSpaceY =
                                                    (change.position.y - change.previousPosition.y) * browserSettings.cursorTrackingSpeed

//                                                val newCursorX =
//                                                    cursorPointerPosition.value.x + changeSpaceX
//
//                                                val newCursorY =
//                                                    (cursorPointerPosition.value.y + changeSpaceY)
//                                                cursorPointerPosition.value =
//                                                    Offset(newCursorX, newCursorY)
                                                var newX =
                                                    cursorPointerPosition.value.x + changeSpaceX
                                                var newY =
                                                    cursorPointerPosition.value.y + changeSpaceY
                                                if (newX < 0) newX = 0f
                                                if (newX > screenSize.width) newX =
                                                    screenSize.width.toFloat()
                                                if (newY < 0) newY = 0f
                                                if (newY > screenSize.height) newY =
                                                    screenSize.height.toFloat()
                                                cursorPointerPosition.value = Offset(newX, newY)

                                            }
                                        }
                                        // This code now ONLY runs after a long-press-drag has finished.
                                        isCursorPadVisible = false

                                        // --- SIMULATE CLICK AT CURSOR POSITION ---


                                        activeWebView?.let { webView ->
                                            Log.i(
                                                "BackSquare",
                                                "Click at cursor position: $cursorPointerPosition"
                                            )
                                            val downTime = System.currentTimeMillis()
                                            val downEvent = MotionEvent.obtain(
                                                downTime,
                                                downTime,
                                                MotionEvent.ACTION_DOWN,
                                                cursorPointerPosition.value.x,
                                                cursorPointerPosition.value.y - webViewTopPadding.toPx(),
                                                0
                                            )
                                            val upEvent = MotionEvent.obtain(
                                                downTime,
                                                downTime + 10,
                                                MotionEvent.ACTION_UP,
                                                cursorPointerPosition.value.x,
                                                cursorPointerPosition.value.y - webViewTopPadding.toPx(),
                                                0
                                            )
                                            webView.dispatchTouchEvent(downEvent)
                                            webView.dispatchTouchEvent(upEvent)
                                        }

                                        coroutineScope.launch {
                                            squareAlpha.snapTo(0f)
                                        }
                                    } else {
                                        // --- TAP OR SHORT-DRAG PATH ---
                                        if (drag != null) {
                                            // SHORT-DRAG
                                            var horizontalDragAccumulator = 0f
                                            var verticalDragAccumulator = 0f
                                            drag(drag.id) { change ->
                                                change.consume()
                                                horizontalDragAccumulator += change.position.x - change.previousPosition.x
                                                verticalDragAccumulator += change.position.y - change.previousPosition.y

                                                if (abs(horizontalDragAccumulator) > abs(
                                                        verticalDragAccumulator
                                                    )
                                                ) {
                                                    squareAlignment =
                                                        if (horizontalDragAccumulator < 0) Alignment.BottomStart else Alignment.BottomEnd
                                                } else {
                                                    if (!isUrlBarVisible) {
                                                        isUrlBarVisible = true
                                                    }
                                                }
                                            }
                                        } else {
                                            // TAP
                                            if (longPressJob.isActive) {
                                                longPressJob.cancel()
                                                coroutineScope.launch {
                                                    triggerBlinkEffect()
                                                }
                                            }
                                        }
                                    }
                                }
                                else awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)


                                    val drag = awaitTouchSlopOrCancellation(down.id) { change, _ ->

                                        change.consume()
                                    }

                                    if (drag != null) {
                                        // SHORT-DRAG
                                        var horizontalDragAccumulator = 0f
                                        var verticalDragAccumulator = 0f
                                        drag(drag.id) { change ->
                                            change.consume()
                                            horizontalDragAccumulator += change.position.x - change.previousPosition.x
                                            verticalDragAccumulator += change.position.y - change.previousPosition.y

                                            if (abs(horizontalDragAccumulator) > abs(
                                                    verticalDragAccumulator
                                                )
                                            ) {
                                                squareAlignment =
                                                    if (horizontalDragAccumulator < 0) Alignment.BottomStart else Alignment.BottomEnd
                                            } else {
                                                if (!isUrlBarVisible) {
                                                    isUrlBarVisible = true
                                                }
                                            }
                                        }
                                    } else {
                                        // TAP
                                        coroutineScope.launch {
                                            triggerBlinkEffect()
                                        }
                                    }
                                }
                            }
                            .padding(
                                end = browserSettings.paddingDp.dp,
                                start = browserSettings.paddingDp.dp, // Add start padding for when it's on the left
                                bottom = browserSettings.paddingDp.dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    cornerRadiusForLayer(
                                        1,
                                        browserSettings.deviceCornerRadius,
                                        browserSettings.paddingDp
                                    ).dp
                                )
                            )
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(
                                2.dp,
                                Color.White.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(
                                    cornerRadiusForLayer(
                                        1,
                                        browserSettings.deviceCornerRadius,
                                        browserSettings.paddingDp
                                    ).dp
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_link),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            }


        }

        // This appears on top of everything when customView is not null.
        if (customView != null) {
            AndroidView(
                factory = { customView!! as ViewGroup },
                // This onRelease block is the KEY to preventing the crash.
                // When this view is removed from composition (because customView becomes null),
                // it guarantees the view is detached from its parent.
                onRelease = { view ->
                    (view.parent as? ViewGroup)?.removeView(view)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }


}

@Composable
fun BottomPanel(
    confirmationPopup: (message: String, onConfirm: () -> Unit, onCancel: () -> Unit) -> Unit,
    resetBrowserSettings: () -> Int,
    backgroundColor: MutableState<Color>,
    setIsSettingsPanelVisible: (Boolean) -> Unit,
    isSettingsPanelVisible: Boolean,
//    setIsSettingsPanelVisible: (Boolean) -> Unit,
    urlBarFocusRequester: FocusRequester,
    isCursorPadVisible: Boolean,
    isCursorMode: Boolean,
    setIsCursorMode: (Boolean) -> Unit,
    setIsOptionsPanelVisible: (Boolean) -> Unit,
    setIsTabsPanelVisible: (Boolean) -> Unit,
    setIsDownloadPanelVisible: (Boolean) -> Unit,
    setIsTabDataPanelVisible: (Boolean) -> Unit,
    savedPanelState: PanelVisibilityState?,
    setSavedPanelState: (PanelVisibilityState?) -> Unit,
    confirmationDisplayState: ConfirmationDialogState?, // Add this

    confirmationState: ConfirmationDialogState?,
    onPermissionDeny: () -> Unit,
    tabsPanelLock: Boolean,
    updateInspectingTab: (Tab) -> Unit,
    isTabDataPanelVisible: Boolean,
    inspectingTab: Tab?,
    handleHistoryNavigation: (tab: Tab, index: Int) -> Unit,
    handleCloseInspectedTab: () -> Unit,
    handleClearInspectedTabData: () -> Unit,
    handlePermissionToggle: (domain: String?, permission: String, isGranted: Boolean) -> Unit,
    siteSettings: Map<String, SiteSettings>,
    onTabDataPanelDismiss: () -> Unit,
    onTabLongPressed: (Tab) -> Unit,

    onDownloadRowClicked: (DownloadItem) -> Unit,
    onDeleteClicked: (DownloadItem) -> Unit,
    onOpenFolderClicked: () -> Unit,
    onClearAllClicked: () -> Unit,
    isDownloadPanelVisible: Boolean,
    downloads: List<DownloadItem>,

    activeWebView: CustomWebView?,
    isUrlOverlayBoxVisible: Boolean,
    setIsUrlOverlayBoxVisible: (Boolean) -> Unit,
    onNewTabClicked: (Int) -> Unit,

    isTabsPanelVisible: Boolean,
    onTabSelected: (Int) -> Unit,
    navigateWebView: () -> Unit,
    hapticFeedback: HapticFeedback,
    setActiveNavAction: (GestureNavAction) -> Unit,
    setIsNavPanelVisible: (Boolean) -> Unit,
    canGoBack: Boolean,
    canGoForward: Boolean,
    isNavPanelVisible: Boolean,
    activeNavAction: GestureNavAction,
    isPromptPanelVisible: Boolean = false,
    state: JsDialogState?,
    promptComponentDisplayState: JsDialogState?,
    onDismiss: () -> Unit,
    isPermissionPanelVisible: Boolean = false,
    permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
    pendingPermissionRequest: CustomPermissionRequest?,
    modifier: Modifier,
    activeTabIndex: MutableState<Int>,
    tabs: List<Tab>,
    isUrlBarVisible: Boolean,
    isBottomPanelVisible: Boolean,
    isOptionsPanelVisible: Boolean,
    browserSettings: BrowserSettings,
    updateBrowserSettings: (BrowserSettings) -> Int,
    textFieldValue: TextFieldValue,
//    url: String,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    toggleOptionsPanel: (Boolean) -> Unit = {},
    toggleIsTabsPanelVisible: () -> Unit = {},
    changeTextFieldValue: (TextFieldValue) -> Unit = {},
    onNewUrl: (String) -> Unit = {},
    setTextFieldHeightPx: (Int) -> Unit = {},
    setIsFocusOnTextField: (Boolean) -> Unit = {},
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isBottomPanelVisible,
        enter = slideInVertically(
            animationSpec = tween(
                animationSpeedForLayer(
                    0,
                    browserSettings.animationSpeed
                )
            ),
            initialOffsetY = { it }
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                animationSpeedForLayer(
                    1,
                    browserSettings.animationSpeed
                )
            ),
            targetOffsetY = { it }
        )
    ) {

        Column(
            modifier = Modifier
                .padding(browserSettings.paddingDp.dp)


                .clip(
                    RoundedCornerShape(
                        cornerRadiusForLayer(
                            1,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp
                        ).dp
                    )
                )

                .background(
                    Color.Black,
                )
                .border(
                    color = Color.White,
                    width = 1.dp,
                    shape = RoundedCornerShape(
                        cornerRadiusForLayer(
                            1,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp
                        ).dp
                    )
                )

        ) {

            NavigationPanel(
                isNavPanelVisible = isNavPanelVisible,
                browserSettings = browserSettings,
                activeAction = activeNavAction,
                canGoBack = canGoBack, // Make sure to pass these down from BrowserScreen
                canGoForward = canGoForward // And this one too
            )
            DownloadPanel(
                isDownloadPanelVisible = isDownloadPanelVisible,
                downloads = downloads,
                browserSettings = browserSettings,
                onDownloadRowClicked = onDownloadRowClicked,
                onDeleteClicked = onDeleteClicked,
                onOpenFolderClicked = onOpenFolderClicked,
                onClearAllClicked = onClearAllClicked
            )
            PromptPanel(
                isUrlBarVisible = isUrlBarVisible,
                activeWebView = activeWebView,
                browserSettings = browserSettings,
                //                modifier = modifier,
                isPromptPanelVisible = isPromptPanelVisible,
                onDismiss = onDismiss,
                state = state,
                promptComponentDisplayState = promptComponentDisplayState,

                )
            SettingsPanel(
                isSettingsPanelVisible = isSettingsPanelVisible,
                browserSettings = browserSettings,
                updateBrowserSettings = updateBrowserSettings,
                backgroundColor = backgroundColor,
                resetBrowserSettings = resetBrowserSettings,
                confirmationPopup = confirmationPopup,
                setIsSettingsPanelVisible = setIsSettingsPanelVisible

            )
            TabDataPanel(
                isTabDataPanelVisible = isTabDataPanelVisible,
                inspectingTab = inspectingTab,
                onDismiss = onTabDataPanelDismiss,
                browserSettings = browserSettings,
                siteSettings = siteSettings,
                onPermissionToggle = handlePermissionToggle,
                onClearSiteData = handleClearInspectedTabData,
                onCloseTab = handleCloseInspectedTab,
                onHistoryItemClicked = handleHistoryNavigation
            )
            TabsPanel(
                inspectingTab = inspectingTab,

                isTabsPanelVisible = isTabsPanelVisible,
                tabs = tabs,
                activeTabIndex = activeTabIndex.value,
                browserSettings = browserSettings,
                onTabSelected = onTabSelected,
                onNewTabClicked = onNewTabClicked,
                hapticFeedback = hapticFeedback,
                onTabLongPressed = onTabLongPressed,
                updateInspectingTab = updateInspectingTab,
            )
            PermissionPanel(
                isUrlBarVisible = isUrlBarVisible,
                isPermissionPanelVisible = isPermissionPanelVisible,
                browserSettings = browserSettings,
                request = pendingPermissionRequest,
                onAllow = {
                    // When user clicks allow, launch the system dialog with the permissions
                    // stored in our request object.

                    pendingPermissionRequest?.let {
                        permissionLauncher.launch(it.permissionsToRequest.toTypedArray())
                    }
                },
                onDeny = {
                    // When user clicks deny, immediately invoke the stored onResult callback
                    // with an empty map (signifying denial) and clear the request.
                    onPermissionDeny()
                }
            )
            ConfirmationPanel(
                isUrlBarVisible = isUrlBarVisible,
                browserSettings = browserSettings,
                state = confirmationDisplayState, // Use the display state here
                isConfirmationPanelVisible = confirmationState != null // Visibility is controlled by the primary state
            )

            // URL BAR
            AnimatedVisibility(
                modifier = modifier
                    .pointerInput(Unit) {
                        // The long press on the UrlBar will activate the gesture

                    },
                visible = isUrlBarVisible,
                enter = fadeIn(
                    tween(
                        animationSpeedForLayer(1, browserSettings.animationSpeed)
                    )
                ),
                exit = fadeOut(
                    tween(
                        animationSpeedForLayer(1, browserSettings.animationSpeed)
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dragAmount ->
                                    // dragAmount is the change in the Y-axis.
                                    // A negative value means the finger has moved UP.
                                    if (dragAmount < 0) {
                                        toggleOptionsPanel(true)
                                    }
                                    // A positive value means the finger has moved DOWN.
                                    else if (dragAmount > 0) {
                                        toggleOptionsPanel(false)
                                    }
                                })
                        }
                ) {

                    OutlinedTextField(
                        modifier = Modifier
                            .height(
                                heightForLayer(
                                    1,
                                    browserSettings.deviceCornerRadius,
                                    browserSettings.paddingDp,
                                    browserSettings.singleLineHeight,
                                ).dp
                            )
                            .padding(browserSettings.paddingDp.dp)
                            .onSizeChanged { size ->
                                setTextFieldHeightPx(size.height)
                            }
                            .fillMaxWidth()
                            .focusRequester(urlBarFocusRequester)
                            //                            .padding(horizontal = browserSettings.paddingDp.dp, vertical = browserSettings.paddingDp.dp / 2)
                            .onFocusChanged {
                                val resetUrl = tabs[activeTabIndex.value].currentUrl ?: ""
                                setIsFocusOnTextField(it.isFocused)
                                if (it.isFocused) {


                                    setSavedPanelState(
                                        PanelVisibilityState(
                                            options = isOptionsPanelVisible,
                                            tabs = isTabsPanelVisible,
                                            downloads = isDownloadPanelVisible,
                                            tabData = isTabDataPanelVisible,
                                            nav = isNavPanelVisible
                                        )
                                    )
                                    setIsOptionsPanelVisible(false)
                                    setIsTabsPanelVisible(false)
                                    setIsDownloadPanelVisible(false)
                                    setIsTabDataPanelVisible(false)
                                    setIsNavPanelVisible(false)
                                    setIsSettingsPanelVisible(false)

                                    if (textFieldValue.text == resetUrl) {

                                        changeTextFieldValue(TextFieldValue("", TextRange(0)))
                                    }
                                } else {

                                    savedPanelState?.let { savedState ->
                                        setIsOptionsPanelVisible(savedState.options)
                                        setIsTabsPanelVisible(savedState.tabs)
                                        setIsDownloadPanelVisible(savedState.downloads)
                                        setIsTabDataPanelVisible(savedState.tabData)
                                        setIsNavPanelVisible(savedState.nav)
                                        setSavedPanelState(null) // Clear the saved state
                                    }

                                    if (textFieldValue.text.isBlank()) {
                                        changeTextFieldValue(
                                            TextFieldValue(
                                                resetUrl,
                                                TextRange(resetUrl.length)
                                            )
                                        )
                                    }
                                    setIsUrlOverlayBoxVisible(true)
                                }
                            }
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    if (dragAmount > 0) {
                                        val resetUrl =
                                            tabs[activeTabIndex.value].currentUrl ?: ""
                                        changeTextFieldValue(
                                            TextFieldValue(
                                                resetUrl,
                                                selection = TextRange(resetUrl.length)
                                            )
                                        )
                                    }
                                }
                            }
                            .clip(
                                RoundedCornerShape(
                                    cornerRadiusForLayer(
                                        2,
                                        browserSettings.deviceCornerRadius,
                                        browserSettings.paddingDp
                                    ).dp
                                )
                            ),
                        value = textFieldValue.text,
                        onValueChange = { newValue ->
                            changeTextFieldValue(
                                TextFieldValue(
                                    newValue,
                                    selection = TextRange(newValue.length)
                                )
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                val input = textFieldValue.text.trim()
                                val resetUrl = tabs[activeTabIndex.value].currentUrl ?: ""

                                if (input.isBlank()) {
                                    changeTextFieldValue(
                                        TextFieldValue(
                                            resetUrl,
                                            TextRange(resetUrl.length)
                                        )
                                    )
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    return@KeyboardActions
                                }
                                val isUrl = try {
                                    Patterns.WEB_URL.matcher(input).matches() ||
                                            (input.contains(".") && !input.contains(" "))
                                } catch (_: Exception) {
                                    false
                                }

                                val finalUrl = if (isUrl) {
                                    if (input.startsWith("http://") || input.startsWith("https://")) {
                                        input
                                    } else {
                                        "https://$input"
                                    }
                                } else {
                                    val encodedQuery =
                                        URLEncoder.encode(
                                            input,
                                            StandardCharsets.UTF_8.toString()
                                        )
                                    "https://www.google.com/search?q=$encodedQuery"
                                }

                                onNewUrl(finalUrl)

                                focusManager.clearFocus()
                                keyboardController?.hide()

                            }
                        ),
//                        shape = CircleShape,
                        shape = RoundedCornerShape(
                            cornerRadiusForLayer(
                                2,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black, // Background when focused
                            unfocusedContainerColor = Color.Black, // Background when unfocused
                            cursorColor = Color.White,
                            disabledContainerColor = Color.White, // Background when disabled
                            errorContainerColor = Color.Red, // Background when in error state.
                            focusedIndicatorColor = Color.White,      // Outline color when focused
                            unfocusedIndicatorColor = Color.White,    // Outline color when unfocused
                            disabledIndicatorColor = Color.White, // Outline color when disabled
                            errorIndicatorColor = Color.Red,          // Outline color on error
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                        )
                    )

                    if (isUrlOverlayBoxVisible) Box(
                        modifier = Modifier
                            .background(
                                Color.Transparent, shape = RoundedCornerShape(
                                    cornerRadiusForLayer(
                                        1,
                                        browserSettings.deviceCornerRadius,
                                        browserSettings.paddingDp
                                    ).dp
                                )
                            )
                            .clip(
                                RoundedCornerShape(
                                    cornerRadiusForLayer(
                                        1,
                                        browserSettings.deviceCornerRadius,
                                        browserSettings.paddingDp
                                    ).dp
                                )
                            )
                            .matchParentSize()
                            .pointerInput(Unit, canGoBack, canGoForward) {
                                // 1. CAPTURE the CoroutineScope provided by pointerInput
                                val coroutineScope = CoroutineScope(coroutineContext)
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)

                                    // 2. USE the captured scope to launch the long press job
                                    val longPressJob = coroutineScope.launch {
                                        delay(viewConfiguration.longPressTimeoutMillis)

                                        // LONG PRESS CONFIRMED
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        focusManager.clearFocus(true)
                                        setIsNavPanelVisible(true)
                                        setActiveNavAction(GestureNavAction.NONE)

                                    }

                                    val drag =
                                        awaitTouchSlopOrCancellation(down.id) { change, _ ->
                                            if (longPressJob.isActive) {
                                                longPressJob.cancel()
                                            }
                                            change.consume()
                                        }

                                    if (longPressJob.isCompleted && !longPressJob.isCancelled) {
                                        if (drag != null) {
                                            var horizontalDragAccumulator = 0f
                                            var verticalDragAccumulator = 0f
                                            var previousAction = GestureNavAction.REFRESH
                                            val horizontalDragThreshold =
                                                with(density) { 40.dp.toPx() }

                                            val verticalCancelThreshold =
                                                with(density) { -40.dp.toPx() }


                                            drag(drag.id) { change ->
                                                change.consume()
                                                horizontalDragAccumulator += change.position.x - change.previousPosition.x
                                                verticalDragAccumulator += change.position.y - change.previousPosition.y

                                                val newAction = when {
                                                    verticalDragAccumulator < verticalCancelThreshold -> {
                                                        when {
                                                            horizontalDragAccumulator < -horizontalDragThreshold -> GestureNavAction.CLOSE_TAB
                                                            horizontalDragAccumulator > horizontalDragThreshold -> GestureNavAction.NEW_TAB
                                                            else -> GestureNavAction.REFRESH
                                                        }
                                                    }

                                                    horizontalDragAccumulator < -horizontalDragThreshold -> if (canGoBack) GestureNavAction.BACK else GestureNavAction.NONE
                                                    horizontalDragAccumulator > horizontalDragThreshold -> if (canGoForward) GestureNavAction.FORWARD else GestureNavAction.NONE
                                                    else -> GestureNavAction.NONE
                                                }

                                                if (newAction != previousAction) {
                                                    hapticFeedback.performHapticFeedback(
                                                        HapticFeedbackType.TextHandleMove
                                                    )
                                                    previousAction = newAction
                                                }
                                                //                                                    activeNavAction = newAction
                                                setActiveNavAction(newAction)
                                            }

                                            navigateWebView()
                                        }
                                    } else {
                                        if (drag != null) {
                                            // IT'S A VERTICAL SWIPE to open OptionsPanel
                                            var vAccumulator = 0f
                                            drag(drag.id) { change ->
                                                vAccumulator += change.position.y - change.previousPosition.y
                                            }
                                            // Check the final drag direction
                                            if (vAccumulator < 0) toggleOptionsPanel(true) // Swipe Up
                                            else toggleOptionsPanel(false) // Swipe Down

                                        } else {
                                            // Gesture is fully over
                                            if (longPressJob.isActive) {
                                                longPressJob.cancel()
                                                // This was a tap
                                                urlBarFocusRequester.requestFocus()
                                                setIsUrlOverlayBoxVisible(false)
                                            }
                                        }
                                    }

                                    //                                        // Gesture is fully over
                                    //                                        if (longPressJob.isActive) {
                                    //                                            longPressJob.cancel()
                                    //                                            // This was a tap
                                    //                                            focusRequester.requestFocus()
                                    //                                        }

                                    // Reset the UI state
                                    //                                        isNavPanelVisible = false
                                    setIsNavPanelVisible(false)
                                    //                                        activeNavAction = GestureNavAction.NONE
                                    setActiveNavAction(GestureNavAction.NONE)
                                }
                            }
                    ) {
                    }
                }
            }


            // SETTING OPTIONS
            OptionsPanel(
                isSettingsPanelVisible = isSettingsPanelVisible,
                isOptionsPanelVisible = isOptionsPanelVisible,
                toggleOptionsPanel = toggleOptionsPanel,
                updateBrowserSettings = updateBrowserSettings,
                browserSettings = browserSettings,
                toggleIsTabsPanelVisible = toggleIsTabsPanelVisible,
                tabs = tabs,
                tabsPanelLock = tabsPanelLock,
                isDownloadPanelVisible = isDownloadPanelVisible,
                setIsDownloadPanelVisible = setIsDownloadPanelVisible,
                isCursorPadVisible = isCursorPadVisible,
                isCursorMode = isCursorMode,
                setIsCursorMode = setIsCursorMode,
                setIsSettingsPanelVisible = setIsSettingsPanelVisible,
            )
        }

    }
}

// --- REPLACE THE ENTIRE OLD PermissionPanel WITH THIS ---

@Composable
fun PermissionPanel(
    isUrlBarVisible: Boolean,
    browserSettings: BrowserSettings,
    // The pending request, which also controls visibility. Null means hidden.
    request: CustomPermissionRequest?,
    // Event for when the user clicks "Allow" on our panel.
    onAllow: () -> Unit,
    // Event for when the user clicks "Deny" on our panel.
    onDeny: () -> Unit,
    isPermissionPanelVisible: Boolean = false,

    ) {
    var requestToShow by remember { mutableStateOf(request) }

    LaunchedEffect(request) {
        if (request != null) {
            // If there's a new request, update immediately.
            Log.i("Permission Panel", "New request received")
            requestToShow = request
        }
    }

    AnimatedVisibility(
        visible = isPermissionPanelVisible,
        enter = expandVertically(animationSpec = tween(browserSettings.animationSpeed.roundToInt())) + fadeIn(
            tween(browserSettings.animationSpeed.roundToInt())
        ),
        exit = shrinkVertically(animationSpec = tween(browserSettings.animationSpeed.roundToInt())) + fadeOut(
            tween(browserSettings.animationSpeed.roundToInt())
        )
    ) {

        val currentRequest = requestToShow
        if (currentRequest == null) return@AnimatedVisibility

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = browserSettings.paddingDp.dp)
                .padding(
                    bottom = if (!isUrlBarVisible) browserSettings.paddingDp.dp else 0.dp,
                    top = browserSettings.paddingDp.dp,
                )


                .background(
                    color = Color.Black.copy(0.3f),
                    shape = RoundedCornerShape(
                        cornerRadiusForLayer(
                            2,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp
                        ).dp
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(browserSettings.paddingDp.dp)
            ) {
                // --- Deny Button ---
                IconButton(
                    onClick = onDeny,
                    modifier = Modifier
                        .weight(1f)
                        .height(
                            heightForLayer(
                                2,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp,
                                browserSettings.singleLineHeight
                            ).dp
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(
                                cornerRadiusForLayer(
                                    2,
                                    browserSettings.deviceCornerRadius,
                                    browserSettings.paddingDp
                                ).dp
                            )
                        ),

                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent
                    ),

                    ) {
                    Icon(
                        painter = painterResource(id = currentRequest.iconResDeny), // You can make this icon generic too
                        contentDescription = "Deny Permission",
                        tint = Color.White
                    )
                }

                // --- Allow Button ---
                IconButton(
                    onClick = onAllow,
                    modifier = Modifier
                        .weight(1f)
                        .height(
                            heightForLayer(
                                2,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp,
                                browserSettings.singleLineHeight,
                            ).dp
                        ),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(id = currentRequest.iconResAllow), // You can make this icon generic too
                        contentDescription = "Allow Permission",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}


@Composable
fun OptionsPanel(
    setIsSettingsPanelVisible: (Boolean) -> Unit,
    isSettingsPanelVisible: Boolean,
    setIsDownloadPanelVisible: (Boolean) -> Unit,

    isOptionsPanelVisible: Boolean = false,
    toggleOptionsPanel: (Boolean) -> Unit = {},
    toggleIsTabsPanelVisible: () -> Unit,
    updateBrowserSettings: (BrowserSettings) -> Int,
    browserSettings: BrowserSettings,
    tabs: List<Tab>,
    tabsPanelLock: Boolean,
    isDownloadPanelVisible: Boolean,
    isCursorPadVisible: Boolean,
    isCursorMode: Boolean,
    setIsCursorMode: (Boolean) -> Unit,
) {


    // This remains the same
    val allOptions =
        remember(
            browserSettings,
            tabsPanelLock,
            isDownloadPanelVisible,
            isCursorPadVisible,
            isSettingsPanelVisible
        ) {
            listOf(
                OptionItem(
                    R.drawable.ic_mouse_cursor, // You'll need a download icon
                    "Show Cursor Pad",
                    isCursorPadVisible,
                ) {
                    Log.e("isCursorMode", "isCursorMode: $isCursorMode")

                    setIsCursorMode(!isCursorMode)
                    toggleOptionsPanel(false)
                },
//                OptionItem(
//                    if (browserSettings.isDesktopMode) R.drawable.ic_mobile else R.drawable.ic_desktop,
//                    "Desktop layout",
//                    browserSettings.isDesktopMode
//                ) {
//                    updateBrowserSettings(browserSettings.copy(isDesktopMode = !browserSettings.isDesktopMode))
//                },
                OptionItem(
                    R.drawable.ic_tabs, // You'll need an icon for this
                    "Show Tabs Panel", // Display the number of open tabs
                    tabsPanelLock
                ) {
                    toggleIsTabsPanelVisible()
                    toggleOptionsPanel(false)

                },
                OptionItem(
                    if (browserSettings.isSharpMode) R.drawable.ic_rounded_corner else R.drawable.ic_sharp_corner,
                    "Toggle Sharp Corners",
                    browserSettings.isSharpMode,
                ) {
                    updateBrowserSettings(browserSettings.copy(isSharpMode = !browserSettings.isSharpMode))
                    toggleOptionsPanel(false)

                },



                OptionItem(
                    R.drawable.ic_download, // You'll need a download icon
                    "Show Downloads",
                    isDownloadPanelVisible
                ) {
                    setIsDownloadPanelVisible(!isDownloadPanelVisible)
                },

                OptionItem(
                    R.drawable.ic_settings, // You'll need a settings icon
                    "Settings",
                    isSettingsPanelVisible,
                ) {
                    // When clicked, show the settings panel and hide this one.
                    setIsSettingsPanelVisible(!isSettingsPanelVisible)
                    toggleOptionsPanel(false)
                },


                OptionItem(R.drawable.ic_bug, "logBrowserSettings", false) {
                    Log.e("BROWSER SETTINGS", browserSettings.toString())
                    Log.e("Tabs List", tabs.toString())
                },
//                OptionItem(R.drawable.ic_fullscreen, "Button 4", false) { /* ... */ },
//                OptionItem(R.drawable.ic_fullscreen, "Button 5", false) { /* ... */ },
//                OptionItem(R.drawable.ic_fullscreen, "Button 6", false) { /* ... */ },
//                OptionItem(R.drawable.ic_fullscreen, "Button 7", false) { /* ... */ },
//                OptionItem(R.drawable.ic_fullscreen, "Button 8", false) { /* ... */ }
            )
        }

// --- NEW: Group the options into pages of 4 ---
    val optionPages = remember(allOptions) {
        allOptions.chunked(4)
    }

    // --- Pager State ---
    // The pagerState remembers the current page and handles scroll animations.
    val pagerState = rememberPagerState(pageCount = { optionPages.size })

    AnimatedVisibility(
        visible = isOptionsPanelVisible,
        enter = expandVertically(
            tween(
                animationSpeedForLayer(
                    1,
                    browserSettings.animationSpeed
                )
            )
        ) + fadeIn(
            tween(
                animationSpeedForLayer(1, browserSettings.animationSpeed)
            )
        ),
        exit = shrinkVertically(
            tween(
                animationSpeedForLayer(
                    1,
                    browserSettings.animationSpeed
                )
            )
        ) + fadeOut(
            tween(
                animationSpeedForLayer(1, browserSettings.animationSpeed)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = browserSettings.paddingDp.dp)
                .padding(bottom = browserSettings.paddingDp.dp)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        cornerRadiusForLayer(
                            2,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp
                        ).dp
                    )
                )
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            // dragAmount is the change in the Y-axis.
                            // A negative value means the finger has moved UP.
                            if (dragAmount < 0) {
                                toggleOptionsPanel(true)
                            }
                            // A positive value means the finger has moved DOWN.
                            else if (dragAmount > 0) {
                                toggleOptionsPanel(false)
                            }
                        })
                }

        ) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { pageIndex ->
                // This composable block is called for each page.

                // A Row holds the 4 buttons for the current page.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(
                                cornerRadiusForLayer(
                                    2,
                                    browserSettings.deviceCornerRadius,
                                    browserSettings.paddingDp
                                ).dp
                            )
                        ),

                    horizontalArrangement = Arrangement.spacedBy(browserSettings.paddingDp.dp)
                ) {
                    // Get the options for the current page
                    val pageOptions = optionPages[pageIndex]

                    // Create an IconButton for each option on the page
                    pageOptions.forEach { option ->
                        IconButton(
                            onClick = option.onClick,
                            // Use weight to make the buttons share space equally
                            modifier = Modifier
                                .weight(1f)
                                .height(
                                    heightForLayer(
                                        2,
                                        browserSettings.deviceCornerRadius,
                                        browserSettings.paddingDp,
                                        browserSettings.singleLineHeight,
                                    ).dp
                                )
                                .background(
                                    if (option.enabled) Color.White else Color.Black,
                                    shape = RoundedCornerShape(
                                        cornerRadiusForLayer(
                                            2,
                                            browserSettings.deviceCornerRadius,
                                            browserSettings.paddingDp
                                        ).dp
                                    )
                                ),


                            ) {
                            Icon(
                                painter = painterResource(id = option.iconRes),
                                contentDescription = option.contentDescription,
                                tint = if (option.enabled) Color.Black else Color.White
                            )
                        }
                    }

//                    // If a page has fewer than 4 items, we add spacers to keep the layout consistent.
//                    repeat(4 - pageOptions.size) {
//                        Spacer(modifier = Modifier.weight(1f))
//                    }
                }
            }

        }
    }
}

@Composable
fun LoadingOverlay(isLoading: Boolean, modifier: Modifier = Modifier, colorScheme: ColorScheme) {
    // Animate the appearance and disappearance of the overlay.
    AnimatedVisibility(
        visible = isLoading,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Use a theme-aware scrim color for a professional look.
                .background(colorScheme.backgroundColor.copy(alpha = 0.7f))
                // CRITICAL: This consumes all touch events, preventing the user
                // from interacting with the WebView while it's loading.
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                // Use a contrasting color that works well on the dark scrim.
                color = colorScheme.foregroundColor,
                strokeWidth = 6.dp
            )
        }
    }
}

@Composable
fun PromptPanel(
    isUrlBarVisible: Boolean,
    activeWebView: CustomWebView?,
    browserSettings: BrowserSettings,
    isPromptPanelVisible: Boolean,
    state: JsDialogState?,
    promptComponentDisplayState: JsDialogState?,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
//        modifier = modifier,
        visible = isPromptPanelVisible,
        enter = fadeIn(tween(animationSpeedForLayer(1, browserSettings.animationSpeed))),
        exit = shrinkVertically(
            tween(
                animationSpeedForLayer(
                    1,
                    browserSettings.animationSpeed
                )
            )
        ) + fadeOut(
            tween(
                animationSpeedForLayer(1, browserSettings.animationSpeed)
            )
        )
    ) {
        var textInput by remember(state) {
            mutableStateOf(if (state is JsPrompt) state.defaultValue else "")
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = browserSettings.paddingDp.dp)
                .padding(
                    top = browserSettings.paddingDp.dp,
                    bottom = if (isUrlBarVisible) 0.dp else browserSettings.paddingDp.dp
                )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = browserSettings.paddingDp.dp)
                    .padding(horizontal = browserSettings.paddingDp.dp * 3)
                    .clip(
                        RoundedCornerShape(
                            cornerRadiusForLayer(
                                2,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    )
                    .background(
                        Color.Black
                    )
                    .border(
                        1.dp,
                        Color.White,
                        shape = RoundedCornerShape(
                            cornerRadiusForLayer(
                                2,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    ),

                verticalAlignment = Alignment.CenterVertically // Keeps text aligned nicely
            ) {
                // "from" Text - Fixed Size
                Row(
                    modifier = Modifier
                        .padding(browserSettings.paddingDp.dp)
                ) {
                    Text(
                        text = "from ", // Added a space for better readability
                        color = Color.White.copy(alpha = 0.7f), // Subtly de-emphasize
                        maxLines = 1, // Ensure it doesn't wrap
                    )

                    // URL Text - Scrollable and takes up remaining space
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = activeWebView?.url
                                ?: "the current page", // Safely handle null URL
                            color = Color.White,
                            maxLines = 1, // Crucial for horizontal scrolling
                            overflow = TextOverflow.Ellipsis, // Good practice, though scrolling will hide it
                            modifier = Modifier
//                                .weight(1f) // Takes all available remaining space
                                .horizontalScroll(rememberScrollState()) // THIS MAKES IT SCROLLABLE
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.Black,
                        shape = RoundedCornerShape(
                            cornerRadiusForLayer(
                                2,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(
                            cornerRadiusForLayer(
                                2,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    )
            )
            {

                val textModifier = Modifier
                    .padding(browserSettings.paddingDp.dp)
                Column(
                    modifier = Modifier
                        .padding(browserSettings.paddingDp.dp)
                        .background(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(
                                cornerRadiusForLayer(
                                    3,
                                    browserSettings.deviceCornerRadius,
                                    browserSettings.paddingDp
                                ).dp
                            )
                        )
                ) {
                    when (promptComponentDisplayState) {
                        is JsAlert -> Text(
                            text = promptComponentDisplayState.message,
                            modifier = textModifier
                        )

                        is JsConfirm -> Text(
                            text = promptComponentDisplayState.message,
                            modifier = textModifier
                        )

                        is JsPrompt -> {
                            Text(
                                text = promptComponentDisplayState.message,
                                modifier = textModifier
                            )
                            Spacer(Modifier.height(browserSettings.paddingDp.dp))
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                maxLines = 6, //hardcode
                                modifier = Modifier
                                    .height(IntrinsicSize.Min)
                                    .fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        activeWebView?.requestFocus()
                                        promptComponentDisplayState.onResult(textInput)
                                        onDismiss()
                                    }
                                ),
                                shape = RoundedCornerShape(
                                    cornerRadiusForLayer(
                                        3,
                                        browserSettings.deviceCornerRadius,
                                        browserSettings.paddingDp
                                    ).dp
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Black.copy(0.95f), // Background when focused
                                    unfocusedContainerColor = Color.Black.copy(0.8f), // Background when unfocused
                                    cursorColor = Color.White,
                                    disabledContainerColor = Color.White, // Background when disabled
                                    errorContainerColor = Color.Red, // Background when in error state.
                                    focusedIndicatorColor = Color.White.copy(0.95f),      // Outline color when focused
                                    unfocusedIndicatorColor = Color.White.copy(0.8f),    // Outline color when unfocused
                                    disabledIndicatorColor = Color.White, // Outline color when disabled
                                    errorIndicatorColor = Color.Red,          // Outline color on error
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White.copy(0.8f),
                                )
                            )
                        }

                        null -> {

                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(browserSettings.paddingDp.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        browserSettings.paddingDp.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Dismiss/Cancel Button (only for confirm/prompt)
                    if (promptComponentDisplayState is JsConfirm || promptComponentDisplayState is JsPrompt) {
                        Button(
                            modifier = buttonModifierForLayer(
                                3,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp,
                                browserSettings.singleLineHeight
                            )
                                .weight(1f)
                                .border(
                                    1.dp, Color.White, shape = RoundedCornerShape(
                                        cornerRadiusForLayer(
                                            3,
                                            browserSettings.deviceCornerRadius,
                                            browserSettings.paddingDp,
                                        ).dp
                                    )
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black
                            ),
                            shape = RoundedCornerShape(
                                cornerRadiusForLayer(
                                    3,
                                    browserSettings.deviceCornerRadius,
                                    browserSettings.paddingDp,
                                ).dp
                            ),

                            onClick = {
                                activeWebView?.requestFocus()
                                when (state) {
                                    is JsConfirm -> state.onResult(false)
                                    is JsPrompt -> state.onResult(null)
                                    else -> {}
                                }
                                onDismiss()
                            },

                            ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                tint = Color.White,
                                contentDescription = "Dismiss",
                            )
                        }
                    }


                    // Confirm Button
                    Button(
                        modifier = buttonModifierForLayer(
                            3,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp,
                            browserSettings.singleLineHeight
                        )
                            .weight(1f)
                            .background(
                                Color.White, shape = RoundedCornerShape(
                                    cornerRadiusForLayer(
                                        3,
                                        browserSettings.deviceCornerRadius,
                                        browserSettings.paddingDp
                                    ).dp
                                )
                            ),
                        shape = RoundedCornerShape(
                            cornerRadiusForLayer(
                                2,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp,
                            ).dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        onClick = {
                            activeWebView?.requestFocus()
                            when (state) {
                                is JsAlert -> { /* Just dismiss */
                                }

                                is JsConfirm -> state.onResult(true)
                                is JsPrompt -> state.onResult(textInput)
                                null -> {

                                }
                            }
                            onDismiss()
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "Confirm",
                        )
                    }
                }


            }


        }

    }
}


@Composable
fun NavigationPanel(
    isNavPanelVisible: Boolean,
    modifier: Modifier = Modifier,
    browserSettings: BrowserSettings,
    activeAction: GestureNavAction,
    canGoBack: Boolean,
    canGoForward: Boolean
) {
    AnimatedVisibility(
        visible = isNavPanelVisible,
        enter = expandVertically(
            tween(
                animationSpeedForLayer(
                    1,
                    browserSettings.animationSpeed
                )
            )
        ) + fadeIn(
            tween(
                animationSpeedForLayer(1, browserSettings.animationSpeed)
            )
        ),
        exit = shrinkVertically(
            tween(
                animationSpeedForLayer(
                    1,
                    browserSettings.animationSpeed
                )
            )
        ) + fadeOut(
            tween(
                animationSpeedForLayer(1, browserSettings.animationSpeed)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = browserSettings.paddingDp.dp)
                .padding(top = browserSettings.paddingDp.dp)
        ) {
            Column(
                modifier = modifier

                    .clip(
                        RoundedCornerShape(
                            cornerRadiusForLayer(
                                2,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    )
                    .background(Color.Black.copy(0.3f)),

                ) {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = browserSettings.paddingDp.dp)
                        .padding(horizontal = browserSettings.paddingDp.dp),


                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.CLOSE_TAB,
                        actionIcon = painterResource(R.drawable.ic_close),
                        browserSettings = browserSettings,
                    )


                    // Refresh Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.REFRESH,
                        actionIcon = painterResource(R.drawable.ic_refresh),
                        browserSettings = browserSettings,
                    )

                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.NEW_TAB,
                        actionIcon = painterResource(R.drawable.ic_add),
                        browserSettings = browserSettings,
                    )
                }
                Row(
                    modifier = modifier
                        .fillMaxWidth()

                        .padding(browserSettings.paddingDp.dp),
                    horizontalArrangement = Arrangement.spacedBy(browserSettings.paddingDp.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.BACK,
                        actionIcon = painterResource(R.drawable.ic_arrow_back),
                        visibility = canGoBack,
                        browserSettings = browserSettings,

                        )

                    // Cancel Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.NONE,
                        actionIcon = painterResource(R.drawable.ic_minimize),
                        browserSettings = browserSettings,
                    )

                    // Forward Icon
                    // Back Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.FORWARD,
                        actionIcon = painterResource(R.drawable.ic_arrow_forward),
                        visibility = canGoForward,
                        browserSettings = browserSettings,

                        )
                }
            }
        }
    }
}


@Composable
fun NavigationItem(
    modifier: Modifier,
    activeAction: GestureNavAction,
    gestureNavAction: GestureNavAction,
    actionIcon: Painter,
    visibility: Boolean = true,
    browserSettings: BrowserSettings
) {
    // Cancel Icon
    val refreshColor by animateColorAsState(if (activeAction == gestureNavAction) Color.White else Color.Transparent)
    Box(
        modifier = modifier
            .height(
                heightForLayer(
                    3,
                    browserSettings.deviceCornerRadius,
                    browserSettings.paddingDp,
                    browserSettings.singleLineHeight,
                ).dp
            )
            .clip(
                RoundedCornerShape(
                    cornerRadiusForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp
                    ).dp
                )
            )
            .background(refreshColor)
    ) {
        if (visibility) {
            Icon(
                actionIcon,
                "Refresh",
                Modifier.align(Alignment.Center),
                tint = if (activeAction == gestureNavAction) Color.Black else Color.White
            )
        }

    }

}

@Composable
fun TabsPanel(
    inspectingTab: Tab?,

    isTabsPanelVisible: Boolean,
    modifier: Modifier = Modifier,
    tabs: List<Tab>,
    activeTabIndex: Int,
    browserSettings: BrowserSettings,
    onTabSelected: (Int) -> Unit,
    onNewTabClicked: (Int) -> Unit,
    onTabLongPressed: (Tab) -> Unit,
    updateInspectingTab: (Tab) -> Unit,
    hapticFeedback: HapticFeedback = LocalHapticFeedback.current
) {
    if (tabs.isEmpty()) return

    val pagerState =
        rememberPagerState(initialPage = activeTabIndex + 1, pageCount = { tabs.size + 2 })

    // This effect is still useful to sync the pager if a new tab is created
    LaunchedEffect(activeTabIndex, tabs.size) {
        Log.d("UpdateTab", "${inspectingTab?.currentUrl}")

        if (pagerState.currentPage != activeTabIndex + 1) {
            pagerState.animateScrollToPage(activeTabIndex + 1)
        }
    }


    LaunchedEffect(pagerState.currentPage) {
        Log.e("UpdateTab", "Current Page ${pagerState.currentPage}")
        if (pagerState.currentPage in 1..tabs.size) updateInspectingTab(tabs[pagerState.currentPage - 1])
        else {
            updateInspectingTab(Tab.createEmpty())
        }
    }


    AnimatedVisibility(
        visible = isTabsPanelVisible,
        enter = expandVertically(
            tween(
                animationSpeedForLayer(
                    1,
                    browserSettings.animationSpeed
                )
            )
        ) + fadeIn(
            tween(
                animationSpeedForLayer(1, browserSettings.animationSpeed)
            )
        ),
        exit = shrinkVertically(
            tween(
                animationSpeedForLayer(
                    1,
                    browserSettings.animationSpeed
                )
            )
        ) + fadeOut(
            tween(
                animationSpeedForLayer(1, browserSettings.animationSpeed)
            )
        )

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = browserSettings.paddingDp.dp)
                .padding(horizontal = browserSettings.paddingDp.dp)

                .clip(
                    RoundedCornerShape(
                        cornerRadiusForLayer(
                            2,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp
                        ).dp
                    )
                )
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = modifier
                    .fillMaxWidth(),
                // We use a smaller content padding so the active tab is larger
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = browserSettings.paddingDp.dp / 2
            ) { pageIndex ->

                when (pageIndex) {
                    0 -> {
                        // This is the FIRST page: New Tab button on the left
                        NewTabButton(
                            browserSettings = browserSettings,
                            onClick = { onNewTabClicked(0) } // Request new tab at index 0
                        )
                    }

                    in 1..tabs.size -> {
                        // This is a regular tab page. Map pageIndex back to tabIndex.
                        val tabIndex = pageIndex - 1

                        val tab = tabs[tabIndex]

                        val currentHistory = tab.historyState
                        val currentItem =
                            currentHistory?.items?.getOrNull(currentHistory.currentIndex)

                        val title =
                            tab.historyState?.items?.getOrNull(tab.historyState!!.currentIndex)?.title
                                ?: "New Tab"
                        val faviconUrl = currentItem?.faviconUrl ?: getFaviconUrlFromGoogleServer(
                            tab.currentUrl ?: ""
                        )
                        Log.w("Favicon", faviconUrl)
                        TabItem(
                            faviconUrl = faviconUrl,
                            title = title,
                            isActive = pagerState.currentPage == pageIndex,
                            browserSettings = browserSettings,
                            onClick = {

                                onTabSelected(tabIndex)
                            },
                            hapticFeedback = hapticFeedback,
                            onLongClick = {
                                onTabLongPressed(tab)
                                Log.e("UpdateTab", "$tabIndex")
                            }
                        )
                    }

                    else -> {
                        // This is the LAST page: New Tab button on the right
                        NewTabButton(
                            browserSettings = browserSettings,
                            onClick = { onNewTabClicked(tabs.size) } // Request new tab at the end
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabItem(
    faviconUrl: String,
    title: String,
    isActive: Boolean,
    browserSettings: BrowserSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    hapticFeedback: HapticFeedback = LocalHapticFeedback.current
) {
    Box(

        modifier = Modifier
            .padding(browserSettings.paddingDp.dp)
            .clip(
                RoundedCornerShape(
                    cornerRadiusForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp
                    ).dp
                )
            )
    ) {
        Row(
            modifier = Modifier
                // Make the entire item clickable
//                .clickable(onClick = onClick)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClick()
                        }
                    )
                }
                .height(
                    heightForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp,
                        browserSettings.singleLineHeight,
                    ).dp
                )
                .clip(
                    RoundedCornerShape(
                        cornerRadiusForLayer(
                            3,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp
                        ).dp
                    )
                )
                .background(if (isActive) Color.White else Color.White.copy(alpha = 0.5f)) // Different background for inactive
                .padding(horizontal = browserSettings.paddingDp.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(browserSettings.paddingDp.dp)

            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp),
//                        .clip(RoundedCornerShape(browserSettings.paddingDp.dp / 2)),
//                        .background(Color.White.copy(alpha = 0.2f)),
//                        .background(if (isActive) Color.White else Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    val imageSizePx = with(LocalDensity.current) {
                        (24.dp * 3).roundToPx()
                    }

                    // 1. Build the same robust ImageRequest as before.
                    val imageRequest = ImageRequest.Builder(LocalContext.current)
                        .data(faviconUrl)
                        .size(imageSizePx) // Explicitly set size
                        .crossfade(true)
                        .placeholder(R.drawable.ic_language)
                        .error(R.drawable.ic_language)
                        .build()

                    // 2. Use the painter to handle loading and state.
                    val painter = rememberAsyncImagePainter(model = imageRequest)

                    // 3. Use the NATIVE Image composable for display. It's more stable.
                    Image(
                        painter = painter,
                        contentDescription = "Favicon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
//                            .padding(3.dp)
                    )
                }

                Spacer(Modifier.width(browserSettings.paddingDp.dp))
                Text(
                    text = title,
                    color = if (isActive) Color.Black else Color.Black.copy(alpha = 0.7f), // Dim the text for inactive
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

        }
    }
}

@Composable
fun NewTabButton(
    modifier: Modifier = Modifier,
    browserSettings: BrowserSettings,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier.padding(browserSettings.paddingDp.dp)
    )
    {
        Box(
            modifier = modifier

                .padding(horizontal = browserSettings.paddingDp.dp)
                .clip(
                    RoundedCornerShape(
                        cornerRadiusForLayer(
                            3,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp
                        ).dp
                    )
                )
                .clickable(onClick = onClick)
                .background(Color.Black.copy(alpha = 0.2f))
                .height(
                    heightForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp,
                        browserSettings.singleLineHeight,
                    ).dp
                )
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "New Tab",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}


@Composable
fun DownloadPanel(
    isDownloadPanelVisible: Boolean,
    downloads: List<DownloadItem>,
    browserSettings: BrowserSettings,
    onDownloadRowClicked: (DownloadItem) -> Unit, // Renamed for clarity
    onDeleteClicked: (DownloadItem) -> Unit,      // New callback for single delete
    onOpenFolderClicked: () -> Unit,              // New callback for folder button
    onClearAllClicked: () -> Unit                 // New callback for clear all button
) {
    AnimatedVisibility(
        visible = isDownloadPanelVisible,
        enter = expandVertically(
            tween(
                animationSpeedForLayer(
                    1,
                    browserSettings.animationSpeed
                )
            )
        ) + fadeIn(
            tween(animationSpeedForLayer(1, browserSettings.animationSpeed))
        ),
        exit = shrinkVertically(
            tween(
                animationSpeedForLayer(
                    1,
                    browserSettings.animationSpeed
                )
            )
        ) + fadeOut(
            tween(animationSpeedForLayer(1, browserSettings.animationSpeed))
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = browserSettings.paddingDp.dp)
                .padding(top = browserSettings.paddingDp.dp)

                .fillMaxWidth()


                .heightIn(
                    max = 300.dp
                ) // Set a max height to prevent it from getting too tall
                .clip(
                    RoundedCornerShape(
                        cornerRadiusForLayer(
                            2,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp
                        ).dp
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(
                        cornerRadiusForLayer(
                            2,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp
                        ).dp
                    )
                )
        ) {
            if (downloads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = browserSettings.paddingDp.dp)
                        .padding(top = browserSettings.paddingDp.dp)
                        .background(Color.Transparent)
                        .clip(
                            RoundedCornerShape(
                                cornerRadiusForLayer(
                                    3,
                                    browserSettings.deviceCornerRadius,
                                    browserSettings.paddingDp
                                ).dp
                            )
                        )
                        .height(
                            heightForLayer(
                                3,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp,
                                browserSettings.singleLineHeight,
                            ).dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No downloads yet.", color = Color.White)
                }


            } else {
                val itemHeight = cornerRadiusForLayer(
                    3,
                    browserSettings.deviceCornerRadius,
                    browserSettings.paddingDp
                ).dp * 2 + browserSettings.paddingDp.dp
                LazyColumn(
                    modifier = Modifier
//                        .weight(1f)
                        .heightIn(max = itemHeight * 2.5f)

                        .padding(top = browserSettings.paddingDp.dp)
                        .padding(horizontal = browserSettings.paddingDp.dp)

                        .clip(
                            RoundedCornerShape(
                                cornerRadiusForLayer(
                                    3,
                                    browserSettings.deviceCornerRadius,
                                    browserSettings.paddingDp
                                ).dp
                            )
                        ),
                    reverseLayout = true,
                ) {


                    items(downloads.size, key = { downloads[it].id }) { index ->
                        DownloadRow(
                            index = index,
                            item = downloads[index],
                            browserSettings = browserSettings,
                            onClick = { onDownloadRowClicked(downloads[index]) },
                            onDeleteClicked = { onDeleteClicked(downloads[index]) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(browserSettings.paddingDp.dp)
                    .clip(
                        RoundedCornerShape(
                            cornerRadiusForLayer(
                                3,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    )
                    .height(
                        heightForLayer(
                            3,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp,
                            browserSettings.singleLineHeight,
                        ).dp
                    )
//                                .padding(bottom = browserSettings.paddingDp.dp),
            ) {
                //  Show Download Folder Button
                IconButton(
                    onClick = onOpenFolderClicked,
                    modifier = buttonModifierForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp,
                        browserSettings.singleLineHeight

                    ).weight(1f)

                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_folder), // You can make this icon generic too
                        contentDescription = "Download Folder",
                        tint = Color.Black
                    )
                }
                if (downloads.isNotEmpty()) Spacer(modifier = Modifier.width(browserSettings.paddingDp.dp))
                if (downloads.isNotEmpty()) IconButton(
                    onClick = onClearAllClicked,
                    modifier = buttonModifierForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp,
                        browserSettings.singleLineHeight
                    ).weight(1f)

                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clear_all), // You can make this icon generic too
                        contentDescription = "Download Folder",
                        tint = Color.Black
                    )
                }
            }

        }
    }
}


@SuppressLint("DefaultLocale")
@Composable
fun DownloadRow(
    index: Int,
    item: DownloadItem,
    browserSettings: BrowserSettings,
    onClick: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // 1. The root is now a Box to allow layering.
    // The clip and overall modifier are applied here.
    Box(
        modifier = Modifier
            .padding(bottom = if (index != 0) browserSettings.paddingDp.dp else 0.dp)

            .fillMaxWidth()

            .height(
                heightForLayer(
                    3,
                    browserSettings.deviceCornerRadius,
                    browserSettings.paddingDp,
                    browserSettings.singleLineHeight,
                ).dp
            )

            .clip(
                RoundedCornerShape(
                    cornerRadiusForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp
                    ).dp
                )
            )
            .clickable(enabled = item.status == DownloadStatus.SUCCESSFUL) {
                onClick()
            }
            .background(Color.Black.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = Color.White,
                shape = RoundedCornerShape(
                    cornerRadiusForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp
                    ).dp
                )
            )

            .pointerInput(item.status) { // Re-read when status changes
                detectTapGestures(
                    onTap = {
                        if (item.status == DownloadStatus.SUCCESSFUL) {
                            onClick()
                        }
                    },
                    onLongPress = {
                        showDeleteConfirm = true // Show the delete confirmation
                    }
                )
            }


    ) {
        // --- LAYER 1: The Progress Background ---


        AnimatedVisibility(
            visible = item.status == DownloadStatus.RUNNING,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    // This is the key: fillMaxWidth takes a fraction from 0.0 to 1.0.
                    // We calculate this from the item's progress (0-100).
                    .fillMaxWidth(fraction = item.progress / 100f)
                    .background(Color.White.copy(alpha = 0.3f))
                // A semi-transparent color for the progress
            )
        }

        // --- LAYER 2: The Content Foreground ---

        // Your original content column. It now sits on top of the progress indicator.
        // It has a transparent background.
        Column(
            modifier = Modifier
                .fillMaxWidth()

                .padding(browserSettings.paddingDp.dp)

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(
                        heightForLayer(
                            4,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp,
                            browserSettings.singleLineHeight,
                        ).dp
                    )
                    .padding(horizontal = browserSettings.paddingDp.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        painter = painterResource(
                            id =
                                when (item.status) {
                                    DownloadStatus.RUNNING -> R.drawable.ic_downloading
                                    DownloadStatus.SUCCESSFUL -> R.drawable.ic_download_done
                                    DownloadStatus.CANCELLED -> R.drawable.ic_file_download_off
                                    else -> R.drawable.ic_download
                                }
                        ),
                        contentDescription = "Download Icon",
                        // Change the tint based on status for better visual feedback
                        tint = Color.Black,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp)

                    )
                }
                Spacer(Modifier.width(browserSettings.paddingDp.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.filename,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // A more descriptive status text
                    val statusText = when (item.status) {
                        DownloadStatus.RUNNING -> {
                            val downloadedMb =
                                String.format("%.1f", item.downloadedBytes / 1024f / 1024f)
                            val totalMb = String.format("%.1f", item.totalBytes / 1024f / 1024f)
                            val sizeInfo =
                                if (item.totalBytes > 0) "$downloadedMb MB / $totalMb MB" else "Running..."

                            val speedInfo =
                                if (item.downloadSpeedBps > 0) formatSpeed(item.downloadSpeedBps) else ""
                            val timeInfo =
                                if (item.timeRemainingMs > 0) formatTimeRemaining(item.timeRemainingMs) else ""

                            // Combine all parts, filtering out empty ones
                            listOf(sizeInfo, speedInfo, timeInfo).filter { it.isNotBlank() }
                                .joinToString(" - ")
                        }

                        else -> ""
                    }
                    if (statusText.isNotBlank()) {
                        Text(
                            statusText,
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }

                }
            }


        }

        // --- LAYER 3: The Delete Confirmation Overlay ---
        AnimatedVisibility(
            visible = showDeleteConfirm,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.8f))
                    // 3. This pointerInput is ONLY on the delete overlay.
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onDeleteClicked()
                                showDeleteConfirm = false // Hide after deleting
                            },
                            onLongPress = {
                                showDeleteConfirm = false // Long press to cancel/hide
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete_forever),
                    contentDescription = "Confirm Delete",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun TabDataPanel(
    isTabDataPanelVisible: Boolean,
    inspectingTab: Tab?,
    onDismiss: () -> Unit,
    browserSettings: BrowserSettings,
    siteSettings: Map<String, SiteSettings>,
    onPermissionToggle: (domain: String?, permission: String, isGranted: Boolean) -> Unit,
    onClearSiteData: () -> Unit,
    onCloseTab: () -> Unit,
    onHistoryItemClicked: (tab: Tab, index: Int) -> Unit
) {

    // 1. Local state to hold the tab being displayed.

    var currentView by remember { mutableStateOf(TabDataPanelView.MAIN) }

    // Effect to reset the view to MAIN when the panel is hidden
    LaunchedEffect(isTabDataPanelVisible) {
        if (!isTabDataPanelVisible) {
            delay(300) // Wait for exit animation to finish before resetting state
            currentView = TabDataPanelView.MAIN
        }
    }

    // 2. Effect to update the local state.
    // This ensures `displayTab` only gets updated with non-null values,
    // holding onto the last valid tab during the exit animation.

    // This AnimatedVisibility controls the entire panel's appearance
    AnimatedVisibility(
        visible = isTabDataPanelVisible,
        enter = fadeIn(tween(browserSettings.animationSpeed.roundToInt())) + expandVertically(
            expandFrom = Alignment.Bottom
        ),
        exit = shrinkVertically(
            tween(
                animationSpeedForLayer(
                    1,
                    browserSettings.animationSpeed
                )
            )
        ) + fadeOut(
            tween(animationSpeedForLayer(1, browserSettings.animationSpeed))
        )

    ) {
        // A Box to handle clicking outside to dismiss
        Box(
            modifier = Modifier
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            // This inner Column prevents the dismiss click from propagating
            Column(
                modifier = Modifier
                    .clickable(enabled = false, onClick = {}) // Block clicks
                    .padding(top = browserSettings.paddingDp.dp)
                    .padding(horizontal = browserSettings.paddingDp.dp)
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .clip(
                        RoundedCornerShape(
                            cornerRadiusForLayer(
                                2,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    )
                    .background(Color.Black)
                    .border(
                        1.dp,
                        Color.White,
                        RoundedCornerShape(
                            cornerRadiusForLayer(
                                2,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    )
            ) {
                val tab = inspectingTab ?: return@Column

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(tween(animationSpeedForLayer(1, browserSettings.animationSpeed))) // Smoothly animates size changes
                ) {

                    val maxLazyColumnHeight = (heightForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp,
                        browserSettings.singleLineHeight,
                    ).dp + browserSettings.paddingDp.dp) * 2.5f

                    val domain =
                        SiteSettingsManager(LocalContext.current).getDomain(tab.currentUrl)
                    val settings = if (domain != null) siteSettings[domain] else null

                    when (currentView) {
                        TabDataPanelView.MAIN -> {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = browserSettings.paddingDp.dp)
                                    .padding(top = browserSettings.paddingDp.dp),
                                verticalArrangement = Arrangement.spacedBy(browserSettings.paddingDp.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // History Button
                                IconButton(
                                    onClick = { currentView = TabDataPanelView.HISTORY },
                                    modifier = buttonModifierForLayer(
                                        3,
                                        browserSettings.deviceCornerRadius,
                                        browserSettings.paddingDp,
                                        browserSettings.singleLineHeight,
                                        false
                                    )
                                        .fillMaxWidth()
                                        .background(Color.Transparent)
                                        .border(
                                            width = 1.dp,
                                            color = Color.White,
                                            shape = RoundedCornerShape(
                                                cornerRadiusForLayer(
                                                    3,
                                                    browserSettings.deviceCornerRadius,
                                                    browserSettings.paddingDp
                                                ).dp
                                            )
                                        )
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_history),
                                        contentDescription = "History",
                                        tint = Color.White
                                    )
                                }

                                // Permissions Button

                                if (settings != null && settings.permissionDecisions.isNotEmpty()) {
                                    IconButton(
                                        onClick = { currentView = TabDataPanelView.PERMISSIONS },
                                        modifier = buttonModifierForLayer(
                                            3,
                                            browserSettings.deviceCornerRadius,
                                            browserSettings.paddingDp,
                                            browserSettings.singleLineHeight,
                                            false
                                        )
                                            .fillMaxWidth()
                                            .background(Color.Transparent)
                                            .border(
                                                width = 1.dp,
                                                color = Color.White,
                                                shape = RoundedCornerShape(
                                                    cornerRadiusForLayer(
                                                        3,
                                                        browserSettings.deviceCornerRadius,
                                                        browserSettings.paddingDp
                                                    ).dp
                                                )
                                            )
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_shield_toggle),
                                            contentDescription = "Permissions",
                                            tint = Color.White
                                        )
                                    }
                                }

                            }
                        }

                        TabDataPanelView.HISTORY -> {
                            val lazyListState = rememberLazyListState()
                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier
                                    .heightIn(
                                        max = maxLazyColumnHeight
                                    )
                                    .padding(
                                        top = browserSettings.paddingDp.dp,
                                        start = browserSettings.paddingDp.dp,
                                        end = browserSettings.paddingDp.dp
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            cornerRadiusForLayer(
                                                3,
                                                browserSettings.deviceCornerRadius,
                                                browserSettings.paddingDp
                                            ).dp
                                        )
                                    )
                            ) {
                                val history = tab.historyState
                                if (history != null) {
                                    items(history.items.size) { index ->
                                        val item = history.items[index]
                                        HistoryRow(
                                            item = item,
                                            isLast = index == history.items.size - 1,
                                            isCurrent = index == history.currentIndex,
                                            browserSettings = browserSettings,
                                            onClick = { onHistoryItemClicked(tab, index) }
                                        )
                                    }
                                }
                            }
                            LaunchedEffect(tab.historyState?.currentIndex) {
                                tab.historyState?.let {
                                    if (it.currentIndex in 0 until it.items.size) {
                                        lazyListState.animateScrollToItem(index = it.currentIndex)
                                    }
                                }
                            }
                        }

                        TabDataPanelView.PERMISSIONS -> {
                            val domain =
                                SiteSettingsManager(LocalContext.current).getDomain(tab.currentUrl)
                            val settings = if (domain != null) siteSettings[domain] else null

                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                if (settings != null && settings.permissionDecisions.isNotEmpty()) {
                                    settings.permissionDecisions.forEach { (permission, isGranted) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                permission.substringAfterLast('.'),
                                                color = Color.White,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Switch(
                                                checked = isGranted,
                                                onCheckedChange = {
                                                    onPermissionToggle(
                                                        domain,
                                                        permission,
                                                        it
                                                    )
                                                })
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No permissions requested yet.", color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                // This Row contains the action buttons at the bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(browserSettings.paddingDp.dp),
                    horizontalArrangement = Arrangement.spacedBy(browserSettings.paddingDp.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (currentView == TabDataPanelView.MAIN) {
                                onDismiss()
                            } else {
                                currentView = TabDataPanelView.MAIN
                            }
                        },
                        modifier = buttonModifierForLayer(
                            3,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp,
                            browserSettings.singleLineHeight
                        ).weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    if (tab.state != TabState.FROZEN) {
                        IconButton(
                            onClick = onClearSiteData,
                            modifier = buttonModifierForLayer(
                                3,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp,
                                browserSettings.singleLineHeight
                            ).weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_database_off),
                                contentDescription = "Clear Site Data",
                                tint = Color.Black
                            )
                        }
                    }

                    IconButton(
                        onClick = onCloseTab,
                        modifier = buttonModifierForLayer(
                            3,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp,
                            browserSettings.singleLineHeight
                        ).weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_tab_close),
                            contentDescription = "Close Tab",
                            tint = Color.Black
                        )
                    }
                }
//                // --- PART 1: History List ---
//
//                val itemHeight = cornerRadiusForLayer(
//                    3,
//                    browserSettings.deviceCornerRadius,
//                    browserSettings.paddingDp
//                ).dp * 2 + browserSettings.paddingDp.dp
//
//                val lazyListState = rememberLazyListState()
//                val history = tab.historyState
//
//
//                LazyColumn(
//                    state = lazyListState,
//                    modifier = Modifier
//                        .heightIn(max = itemHeight * 2.5f)
//                        .animateContentSize(animationSpec = tween(durationMillis = browserSettings.animationSpeed))
//                        .padding(
//                            top = browserSettings.paddingDp.dp
//                        )
//                        .padding(
//                            horizontal = browserSettings.paddingDp.dp
//                        )
//                        .clip(
//                            RoundedCornerShape(
//                                cornerRadiusForLayer(
//                                    3,
//                                    browserSettings.deviceCornerRadius,
//                                    browserSettings.paddingDp
//                                ).dp
//                            )
//                        )
//                ) {
//                    val history = tab.historyState
//                    if (history != null) {
//                        items(history.items.size) { index ->
//                            val item = history.items[index]
//
//                            // --- REPLACE the old Box/Text with the new HistoryRow ---
//                            HistoryRow(
//                                item = item,
//                                isLast = index == history.items.size - 1,
//                                isCurrent = index == history.currentIndex,
//                                browserSettings = browserSettings,
//                                onClick = {
//                                    onHistoryItemClicked(tab, index)
//                                }
//                            )
//                        }
//                    }
//                }
//                LaunchedEffect(history?.currentIndex) {
//                    if (history != null && history.currentIndex in 0 until history.items.size) {
//                        // Animate scroll will smoothly bring the item into view.
//                        // It's efficient and won't scroll if the item is already visible.
//                        lazyListState.animateScrollToItem(index = history.currentIndex)
//                    }
//                }
//
//                // --- PART 2: Permissions and Actions ---
//                val domain =
//                    SiteSettingsManager(LocalContext.current).getDomain(tab.currentUrl)
//                val settings = if (domain != null) siteSettings[domain] else null
//
//                if (settings != null) {
//                    Text(
//                        "Permissions for $domain",
//                        color = Color.White,
//                        style = MaterialTheme.typography.titleMedium,
//                        modifier = Modifier.padding(16.dp)
//                    )
//                    // Permissions List
//                    if (settings.permissionDecisions.isEmpty()) {
//                        Text(
//                            "No permissions requested yet.",
//                            color = Color.Gray,
//                            modifier = Modifier.padding(horizontal = 16.dp)
//                        )
//                    } else {
//                        settings.permissionDecisions.forEach { (permission, isGranted) ->
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(horizontal = 16.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(
//                                    permission.substringAfterLast('.'),
//                                    color = Color.White,
//                                    modifier = Modifier.weight(1f)
//                                )
//                                Switch(
//                                    checked = isGranted,
//                                    onCheckedChange = { newGrantState ->
//                                        onPermissionToggle(domain, permission, newGrantState)
//                                    }
//                                )
//                            }
//                        }
//                    }
//                    Spacer(Modifier.height(16.dp))
//                }
//
//                // Action Buttons
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(browserSettings.paddingDp.dp),
//                    horizontalArrangement = Arrangement.spacedBy(browserSettings.paddingDp.dp)
//                ) {
//                    // Show Clear Data button only if the tab is active
//                    if (tab.state != TabState.FROZEN) {
//                        IconButton(
//                            onClick = onClearSiteData,
//                            modifier = buttonModifierForLayer(
//                                3,
//                                browserSettings.deviceCornerRadius,
//                                browserSettings.paddingDp
//                            ).weight(1f)
//                        ) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.ic_database_off),
//                                tint = Color.Black,
//                                contentDescription = "Clear Site Data"
//                            )
//                        }
//                    }
//                    IconButton(
//                        onClick = onCloseTab,
//                        modifier = buttonModifierForLayer(
//                            3,
//                            browserSettings.deviceCornerRadius,
//                            browserSettings.paddingDp
//                        ).weight(1f)
//
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_tab_close),
//                            tint = Color.Black,
//                            contentDescription = "Close Tab"
//                        )
//                    }
//                }
            }
        }
    }
}


@Composable
fun HistoryRow(
    item: SerializableHistoryItem,
    isLast: Boolean,
    isCurrent: Boolean,
    browserSettings: BrowserSettings,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (!isLast) browserSettings.paddingDp.dp else 0.dp)
            .height(
                heightForLayer(
                    3,
                    browserSettings.deviceCornerRadius,
                    browserSettings.paddingDp,
                    browserSettings.singleLineHeight,
                ).dp
            )
            .clip(
                RoundedCornerShape(
                    cornerRadiusForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp
                    ).dp
                )
            )
            .background(if (isCurrent) Color.White else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isCurrent) Color.Transparent else Color.White,
                shape = RoundedCornerShape(
                    cornerRadiusForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp
                    ).dp
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = browserSettings.paddingDp.dp * 2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Favicon ---
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            val imageRequest = ImageRequest.Builder(LocalContext.current)
                // Use the item's saved faviconUrl, or fall back to the Google service
                .data(item.faviconUrl ?: getFaviconUrlFromGoogleServer(item.url))
                .crossfade(true)
                .placeholder(R.drawable.ic_language)
                .error(R.drawable.ic_language)
                .build()

            val painter = rememberAsyncImagePainter(model = imageRequest)

            Image(
                painter = painter,
                contentDescription = "Favicon for ${item.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.width(browserSettings.paddingDp.dp))

        // --- Title ---
        Text(
            text = item.title.ifBlank { item.url },
            color = if (isCurrent) Color.Black else Color.White,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ConfirmationPanel(
    isConfirmationPanelVisible: Boolean,
    isUrlBarVisible: Boolean,
    browserSettings: BrowserSettings,
    state: ConfirmationDialogState?,
) {
    AnimatedVisibility(
        visible = isConfirmationPanelVisible,
        enter = expandVertically(tween(animationSpeedForLayer(1, browserSettings.animationSpeed))),
        exit = shrinkVertically(tween(animationSpeedForLayer(1, browserSettings.animationSpeed))) +
                fadeOut(tween(animationSpeedForLayer(1, browserSettings.animationSpeed)))
    ) {

        if (state == null) return@AnimatedVisibility
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = browserSettings.paddingDp.dp)
                .padding(
                    top = browserSettings.paddingDp.dp,
                    bottom = if (isUrlBarVisible) 0.dp else browserSettings.paddingDp.dp
                )
                .background(
                    Color.Black,
                    shape = RoundedCornerShape(
                        cornerRadiusForLayer(
                            2,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp
                        ).dp
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(
                        cornerRadiusForLayer(
                            2,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp
                        ).dp
                    )
                )
        ) {

            Text(
                text = state.message,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(browserSettings.paddingDp.dp),
                horizontalArrangement = Arrangement.spacedBy(browserSettings.paddingDp.dp)
            ) {
                // Cancel Button
                Button(
                    modifier = buttonModifierForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp,
                        browserSettings.singleLineHeight,
                    )
                        .weight(1f)
                        .border(
                            1.dp, Color.White, shape = RoundedCornerShape(
                                cornerRadiusForLayer(
                                    3,
                                    browserSettings.deviceCornerRadius,
                                    browserSettings.paddingDp,
                                ).dp
                            )
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(
                        cornerRadiusForLayer(
                            3,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp,
                        ).dp
                    ),
                    onClick = state.onCancel
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        tint = Color.White,
                        contentDescription = "Cancel",
                    )
                }

                // Confirm Button
                Button(
                    modifier = buttonModifierForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp,
                        browserSettings.singleLineHeight,
                    )
                        .weight(1f)
                        .background(
                            Color.White, shape = RoundedCornerShape(
                                cornerRadiusForLayer(
                                    3,
                                    browserSettings.deviceCornerRadius,
                                    browserSettings.paddingDp
                                ).dp
                            )
                        ),
                    shape = RoundedCornerShape(
                        cornerRadiusForLayer(
                            2,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp,
                        ).dp
                    ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    onClick = state.onConfirm
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "Confirm",
                    )
                }
            }
        }
    }
}

@Composable
fun CursorPointer(
    isCursorPadVisible: Boolean,
    position: Offset,
    browserSettings: BrowserSettings
) {
    AnimatedVisibility(
        visible = isCursorPadVisible,
        enter = fadeIn(tween(browserSettings.animationSpeed.roundToInt())),
        exit = fadeOut(tween(browserSettings.animationSpeed.roundToInt())),
        modifier = Modifier
    ) {
        val cursorContainerSize = browserSettings.cursorContainerSize.dp
        val pointerSize = cursorContainerSize / 2
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        position.x.roundToInt() - pointerSize.toPx().toInt(), // Center the icon
                        position.y.roundToInt() - pointerSize.toPx().toInt()
                    )
                }
                .size(cursorContainerSize)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .border(2.dp, Color.White, CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_dot), // Ensure you have this drawable
                contentDescription = "Quick Cursor",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(browserSettings.cursorPointerSize.dp)
            )
        }
    }
}

@Composable
fun CursorPad(
    urlBarFocusRequester: FocusRequester,
    isLongPressDrag: MutableState<Boolean>,
    isCursorPadVisible: Boolean,
    setIsCursorPadVisible: (Boolean) -> Unit,
    browserSettings: BrowserSettings,
    screenSize: IntSize,
    coroutineScope: CoroutineScope,
    activeWebView: CustomWebView?,
    cursorPointerPosition: MutableState<Offset>,
    webViewTopPadding: Dp,
    hapticFeedback: HapticFeedback,
    setIsUrlBarVisible: (Boolean) -> Unit,
    cursorPadHeight: Dp,


    ) {
    AnimatedVisibility(
        modifier = Modifier
            .fillMaxSize(),
        visible = isCursorPadVisible,
        enter = slideInVertically(
            initialOffsetY = { it }, // Start from the bottom
            animationSpec = tween(durationMillis = browserSettings.animationSpeed.roundToInt())
        ) + fadeIn(tween(browserSettings.animationSpeed.roundToInt())),
        exit = fadeOut(tween(browserSettings.animationSpeed.roundToInt()))
    ) {


        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.ime)


        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        cursorPadHeight
                    )
                    .align(Alignment.BottomCenter)
                    .pointerInput(Unit) {
                        // This is the correct "main loop". It handles one gesture at a time
                        // and then automatically resets to wait for the next one.
                        awaitEachGesture {
                            // 1. Wait for the first finger to touch down.
                            val down = awaitFirstDown(requireUnconsumed = false)


                            var longPressDownTime = System.currentTimeMillis()


                            val longPressJob = coroutineScope.launch {
                                delay(viewConfiguration.longPressTimeoutMillis)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                                longPressDownTime = System.currentTimeMillis()

                            }


                            // 2. Wait for the user to start dragging.
                            val drag = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                                change.consume()
                                if (longPressJob.isActive) {
                                    longPressJob.cancel()
                                }
                            }


                            if (longPressJob.isCompleted && !longPressJob.isCancelled) {

                                Log.i("CursorLongPress", "Activated")

                                activeWebView?.let { webView ->
//                                    longPressDownTime = System.currentTimeMillis()
                                    val longPressDownEvent = MotionEvent.obtain(
                                        longPressDownTime,
                                        longPressDownTime,
                                        MotionEvent.ACTION_DOWN,
                                        cursorPointerPosition.value.x,
                                        cursorPointerPosition.value.y - webViewTopPadding.toPx(),
                                        0
                                    )
                                    webView.dispatchTouchEvent(longPressDownEvent)
                                    Log.i("CursorLongPress", "Down")

                                }
                                if (drag != null) {
                                    Log.i("CursorPad", "LONG PRESS Drag")

                                    drag(drag.id) { change ->
                                        change.consume()

                                        isLongPressDrag.value = true
                                        // Check for multiple fingers DURING the drag
                                        val event = currentEvent // Get the current pointer event


//                                        Log.e("CursorPad", "Event changes size : ${event.changes.size}")
                                        when (event.changes.size) {
                                            1 -> {
                                                // This is a single-finger drag, move the cursor
                                                val changeDelta =
                                                    change.position - change.previousPosition
                                                val changeSpaceX =
                                                    changeDelta.x * browserSettings.cursorTrackingSpeed
                                                val changeSpaceY =
                                                    changeDelta.y * browserSettings.cursorTrackingSpeed

//
//
//                                                Log.i("CursorPad", "changeSpaceX  $changeSpaceX")
//
//                                                Log.i("CursorPad", "changeSpaceY  $changeSpaceY")

                                                var newX =
                                                    cursorPointerPosition.value.x + changeSpaceX
                                                var newY =
                                                    cursorPointerPosition.value.y + changeSpaceY
                                                if (newX < 0) newX = 0f
                                                if (newX > screenSize.width) newX =
                                                    screenSize.width.toFloat()
                                                if (newY < 0) newY = 0f
                                                if (newY > screenSize.height) newY =
                                                    screenSize.height.toFloat()
                                                cursorPointerPosition.value = Offset(newX, newY)



                                                activeWebView?.let { webView ->
                                                    val moveEvent = MotionEvent.obtain(
                                                        System.currentTimeMillis(),
                                                        System.currentTimeMillis(),
                                                        MotionEvent.ACTION_MOVE,
                                                        cursorPointerPosition.value.x,
                                                        cursorPointerPosition.value.y - webViewTopPadding.toPx(),
                                                        0
                                                    )
                                                    webView.dispatchTouchEvent(moveEvent)
                                                    Log.i("CursorLongPress", "Move")

                                                }

//                                            cursorPointerPosition.value += Offset(
//                                                changeSpaceX,
//                                                changeSpaceY
//                                            )
                                            }

//                                            2 -> {
//                                                Log.i("CursorPad", "Two fingers detected during drag")
//
//                                                val changeDelta =
//                                                    change.position - change.previousPosition
//                                                var changeSpaceY = changeDelta.y
//
//
//                                                if (activeWebView != null) {
//
//                                                    if (!activeWebView.canScrollVertically(1) && changeSpaceY < 0) changeSpaceY =
//                                                        0f
//                                                    if (!activeWebView.canScrollVertically(-1) && changeSpaceY > 0) changeSpaceY =
//                                                        0f
//                                                    Log.i("CursorPad", "changeSpaceY  $changeSpaceY")
//
//                                                    // We negate the value for "natural" scrolling (fingers down -> content up).
//                                                    activeWebView.scrollBy(
//                                                        0,
//                                                        -changeSpaceY.roundToInt()
//                                                    )
//                                                }
//
//
//                                                // 3. Consume the changes to prevent single-finger logic from also running.
//                                                event.changes.forEach { it.consume() }
//                                            }
//
//                                            3 -> {
//                                                Log.i("CursorPad", "3 fingers detected during drag")
//                                                val changeDelta =
//                                                    change.position - change.previousPosition
//                                                val changeSpaceY = changeDelta.y
//
//                                                if (changeSpaceY < 0) {
//                                                    setIsCursorPadVisible(false)
//                                                    setIsUrlBarVisible(true)
//                                                }
//                                            }
                                        }
                                    }
                                }

                                isLongPressDrag.value = false

                                activeWebView?.let { webView ->
                                    val upEvent = MotionEvent.obtain(
                                        longPressDownTime,
                                        System.currentTimeMillis(),
                                        MotionEvent.ACTION_UP,
                                        cursorPointerPosition.value.x,
                                        cursorPointerPosition.value.y - webViewTopPadding.toPx(),
                                        0
                                    )
                                    webView.dispatchTouchEvent(upEvent)
                                    Log.i("CursorLongPress", "Up")

                                }

                            } else {

                                Log.e("CursorPad", "TOUCH DETECTED")
                                if (drag != null) {
                                    // This is the high-level function that consumes the rest of the drag gesture.
                                    // It will finish when the user lifts their finger.
                                    drag(drag.id) { change ->
                                        change.consume()

                                        // Check for multiple fingers DURING the drag
                                        val event = currentEvent // Get the current pointer event


//                                        Log.e("CursorPad", "Event changes size : ${event.changes.size}")
                                        when (event.changes.size) {
                                            1 -> {
                                                // This is a single-finger drag, move the cursor
                                                val changeDelta =
                                                    change.position - change.previousPosition
                                                val changeSpaceX =
                                                    changeDelta.x * browserSettings.cursorTrackingSpeed
                                                val changeSpaceY =
                                                    changeDelta.y * browserSettings.cursorTrackingSpeed



                                                Log.i("CursorPad", "changeSpaceX  $changeSpaceX")

                                                Log.i("CursorPad", "changeSpaceY  $changeSpaceY")

                                                var newX =
                                                    cursorPointerPosition.value.x + changeSpaceX
                                                var newY =
                                                    cursorPointerPosition.value.y + changeSpaceY
                                                if (newX < 0) newX = 0f
                                                if (newX > screenSize.width) newX =
                                                    screenSize.width.toFloat()
                                                if (newY < 0) newY = 0f
                                                if (newY > screenSize.height) newY =
                                                    screenSize.height.toFloat()

                                                cursorPointerPosition.value = Offset(newX, newY)
                                                activeWebView?.evaluateJavascript(
                                                    "window.simulateHover($newX, $newY)",
                                                    null
                                                )
//                                            cursorPointerPosition.value += Offset(
//                                                changeSpaceX,
//                                                changeSpaceY
//                                            )
                                            }

                                            2 -> {
                                                Log.i(
                                                    "CursorPad",
                                                    "Two fingers detected during drag"
                                                )

                                                val changeDelta =
                                                    change.position - change.previousPosition
                                                var changeSpaceY = changeDelta.y


                                                if (activeWebView != null) {

                                                    if (!activeWebView.canScrollVertically(1) && changeSpaceY < 0) changeSpaceY =
                                                        0f
                                                    if (!activeWebView.canScrollVertically(-1) && changeSpaceY > 0) changeSpaceY =
                                                        0f
                                                    Log.i(
                                                        "CursorPad",
                                                        "changeSpaceY  $changeSpaceY"
                                                    )

                                                    // We negate the value for "natural" scrolling (fingers down -> content up).
                                                    activeWebView.scrollBy(
                                                        0,
                                                        -changeSpaceY.roundToInt()
                                                    )
                                                }


                                                // 3. Consume the changes to prevent single-finger logic from also running.
                                                event.changes.forEach { it.consume() }
                                            }

                                            3 -> {
                                                Log.i("CursorPad", "3 fingers detected during drag")
                                                val changeDelta =
                                                    change.position - change.previousPosition
                                                val changeSpaceY = changeDelta.y
                                                setIsCursorPadVisible(false)
                                                setIsUrlBarVisible(true)
                                                if (changeSpaceY < 0) {
                                                    urlBarFocusRequester.requestFocus()

                                                    // get focus to the url bar
                                                    // use focusRequester to get focus to the url bar

                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Log.i("CursorPad", "TAP")

//                                 Work but cannot click under the cursor pad
                                    // -> use for 2 finger capture?
//                                            CursorAccessibilityService.instance?.performClick(
//                                                cursorPointerPosition.value.x,
//                                                cursorPointerPosition.value.y
//                                            )

                                    activeWebView?.let { webView ->
                                        Log.i(
                                            "BackSquare",
                                            "Click at cursor position: $cursorPointerPosition"
                                        )
                                        val downTime = System.currentTimeMillis()
                                        val downEvent = MotionEvent.obtain(
                                            downTime,
                                            downTime,
                                            MotionEvent.ACTION_DOWN,
                                            cursorPointerPosition.value.x,
                                            cursorPointerPosition.value.y - webViewTopPadding.toPx(),
                                            0
                                        )
                                        val upEvent = MotionEvent.obtain(
                                            downTime,
                                            downTime + 10,
                                            MotionEvent.ACTION_UP,
                                            cursorPointerPosition.value.x,
                                            cursorPointerPosition.value.y - webViewTopPadding.toPx(),
                                            0
                                        )
                                        webView.dispatchTouchEvent(downEvent)
                                        webView.dispatchTouchEvent(upEvent)
                                    }
                                }
                            }
                            // 3. If a drag was detected, enter the drag-handling logic.

                            // 4. After the drag is over (finger lifted), this block finishes.
                            // The `awaitEachGesture` loop will now start over from the top,
                            // ready to `awaitFirstDown` for the next gesture.
                        }
                    }

                    .padding(
                        end = browserSettings.paddingDp.dp,
                        start = browserSettings.paddingDp.dp, // Add start padding for when it's on the left
                        bottom = browserSettings.paddingDp.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            cornerRadiusForLayer(
                                1,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    )
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(
                        2.dp,
                        Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(
                            cornerRadiusForLayer(
                                1,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_trackpad_input),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SettingsPanel(
    backgroundColor: MutableState<Color>,
    isSettingsPanelVisible: Boolean,
    setIsSettingsPanelVisible: (Boolean) -> Unit,
    browserSettings: BrowserSettings,
    // Add other parameters like updateBrowserSettings as needed
    updateBrowserSettings: (BrowserSettings) -> Int,
    confirmationPopup: (String, () -> Unit, () -> Unit) -> Unit,
    resetBrowserSettings: () -> Int,
) {

    var currentView by remember { mutableStateOf(SettingPanelView.MAIN) }

    // This state will hold the current value of the slider.
//    var sliderValue by remember { mutableStateOf(browserSettings.deviceCornerRadius) }

    LaunchedEffect(currentView) {
        if (currentView == SettingPanelView.CORNER_RADIUS) {
            backgroundColor.value = Color.Red
        } else {
            backgroundColor.value = Color.Transparent
        }

    }

    // Effect to reset the view and slider value when the panel is hidden
    LaunchedEffect(isSettingsPanelVisible) {
        if (!isSettingsPanelVisible) {
            delay(browserSettings.animationSpeed.toLong()) // Wait for exit animation
            currentView = SettingPanelView.MAIN
        }
    }

    // Placeholder options for the settings panel
    val allSettingsOptions = remember(browserSettings) {
        listOf(
            OptionItem(
                R.drawable.ic_adjust_corner_radius,
                "Adjust corner radius",
                false
            ) {
                currentView = SettingPanelView.CORNER_RADIUS
            },
            OptionItem(R.drawable.ic_link, "Default URL") {
                currentView = SettingPanelView.DEFAULT_URL
            },
            OptionItem(R.drawable.ic_animation, "Animation Speed") {
                currentView = SettingPanelView.ANIMATION_SPEED

            },
            OptionItem(R.drawable.ic_padding, "Padding") {
                currentView = SettingPanelView.PADDING
            },
            OptionItem(R.drawable.ic_cursor_size, "Cursor Size") {
                currentView = SettingPanelView.CURSOR_CONTAINER_SIZE
            },
            OptionItem(R.drawable.ic_cursor_speed, "Cursor Speed") {
                currentView = SettingPanelView.CURSOR_TRACKING_SPEED
            },

            OptionItem(R.drawable.ic_reset_settings, "Reset Settings", false) {

                confirmationPopup(
                    "reset all settings?",
                    {
                        resetBrowserSettings()
                        setIsSettingsPanelVisible(false)
                    },
                    {}
                )
            },
            OptionItem(R.drawable.ic_info, "About", false) {
                currentView = SettingPanelView.INFO
            },
        )
    }

    val optionPages = remember(allSettingsOptions) {
        allSettingsOptions.chunked(4)
    }
    val pagerState = rememberPagerState(pageCount = { optionPages.size })

    AnimatedVisibility(
        visible = isSettingsPanelVisible,
        enter = expandVertically(tween(animationSpeedForLayer(1, browserSettings.animationSpeed))),
        exit = shrinkVertically(tween(animationSpeedForLayer(1, browserSettings.animationSpeed)))
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = browserSettings.paddingDp.dp)
                .padding(top = browserSettings.paddingDp.dp)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        cornerRadiusForLayer(
                            2,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp
                        ).dp
                    )
                )
                .animateContentSize(tween(animationSpeedForLayer(1, browserSettings.animationSpeed)))
                .border(
                    1.dp,
                    Color.White,
                    RoundedCornerShape(
                        cornerRadiusForLayer(
                            2,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp
                        ).dp
                    )
                )
        ) {

            when (currentView) {
                SettingPanelView.MAIN -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { pageIndex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.Black.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(
                                        cornerRadiusForLayer(
                                            2,
                                            browserSettings.deviceCornerRadius,
                                            browserSettings.paddingDp
                                        ).dp
                                    )
                                ),
                            horizontalArrangement = Arrangement.spacedBy(browserSettings.paddingDp.dp)
                        ) {
                            val pageOptions = optionPages[pageIndex]
                            pageOptions.forEach { option ->
                                IconButton(
                                    onClick = option.onClick,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(
                                            heightForLayer(
                                                2,
                                                browserSettings.deviceCornerRadius,
                                                browserSettings.paddingDp,
                                                browserSettings.singleLineHeight,
                                            ).dp
                                        )
                                        .background(
                                            if (option.enabled) Color.White else Color.Black,
                                            shape = RoundedCornerShape(
                                                cornerRadiusForLayer(
                                                    2,
                                                    browserSettings.deviceCornerRadius,
                                                    browserSettings.paddingDp
                                                ).dp
                                            )
                                        )
                                ) {
                                    Icon(
                                        painter = painterResource(id = option.iconRes),
                                        contentDescription = option.contentDescription,
                                        tint = if (option.enabled) Color.Black else Color.White
                                    )
                                }
                            }
                            repeat(4 - pageOptions.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                SettingPanelView.CORNER_RADIUS -> {

                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->
                            updateBrowserSettings(
                                browserSettings.copy(deviceCornerRadius = newValue)
                            )
                        },
                        onBackClick = { currentView = SettingPanelView.MAIN },
                        valueRange = 0f..60f,
                        steps = 5999,
                        currentSettingOriginalValue = browserSettings.deviceCornerRadius,
                        textFieldValueFun = { src ->
                            src.substring(0, 2) + "." + src.substring(2, 4)
                        },
                        iconID = R.drawable.ic_adjust_corner_radius,
                    )
                }

                SettingPanelView.ANIMATION_SPEED -> {

                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->

                            updateBrowserSettings(
                                browserSettings.copy(animationSpeed = newValue)
                            )
                        },
                        onBackClick = { currentView = SettingPanelView.MAIN },
                        valueRange = 0f..1000f,
                        steps = 999,
                        currentSettingOriginalValue = browserSettings.animationSpeed,
                        textFieldValueFun = { src ->
                            src
                        },
                        afterDecimal = false,
                        iconID = R.drawable.ic_animation
                    )
                }

                SettingPanelView.PADDING -> {

                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->

                            updateBrowserSettings(
                                browserSettings.copy(paddingDp = newValue)
                            )
                        },
                        onBackClick = { currentView = SettingPanelView.MAIN },
                        valueRange = 3f..11f,
                        steps = 7,
                        currentSettingOriginalValue = browserSettings.paddingDp,
                        textFieldValueFun = { src ->
                            src
                        },
                        afterDecimal = false,
                        iconID = R.drawable.ic_padding,
                        digitCount = 2,
                    )
                }


                SettingPanelView.CURSOR_CONTAINER_SIZE -> {
                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->

                            updateBrowserSettings(
                                browserSettings.copy(cursorContainerSize = newValue)
                            )
                        },
                        onBackClick = { currentView = SettingPanelView.MAIN },
                        valueRange = 20f..70f,
                        steps = 49,
                        currentSettingOriginalValue = browserSettings.cursorContainerSize,
                        textFieldValueFun = { src ->
                            src
                        },
                        afterDecimal = false,
                        iconID = R.drawable.ic_cursor_size,
                        digitCount = 2,
                    )
                }

                SettingPanelView.CURSOR_TRACKING_SPEED -> {
                    SliderSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->

                            updateBrowserSettings(
                                browserSettings.copy(cursorTrackingSpeed = newValue)
                            )
                            Log.e(
                                "CursorSpeed",
                                "Updated to ${browserSettings.cursorTrackingSpeed}"
                            )
                        },
                        onBackClick = { currentView = SettingPanelView.MAIN },
                        valueRange = 0.5f..2f,
                        steps = 29,
                        currentSettingOriginalValue = browserSettings.cursorTrackingSpeed,
                        textFieldValueFun = { src ->
                            src.substring(1, 2) + "." + src.substring(2, 4)
                        },
                        iconID = R.drawable.ic_cursor_speed,
                        digitCount = 4,
                    )
                }

                SettingPanelView.DEFAULT_URL -> {
                    TextSetting(
                        browserSettings = browserSettings,
                        updateBrowserSettingsForSpecificValue = { newValue ->
                            updateBrowserSettings(browserSettings.copy(defaultUrl = newValue))
                        },
                        onBackClick = { currentView = SettingPanelView.MAIN },
                        iconID = R.drawable.ic_link,
                        currentSettingOriginalValue = browserSettings.defaultUrl
                    )
                }

                SettingPanelView.INFO -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    heightForLayer(
                                        2,
                                        browserSettings.deviceCornerRadius,
                                        browserSettings.paddingDp,
                                        browserSettings.singleLineHeight,
                                    ).dp
                                ),
                        contentAlignment = Alignment.Center
                    ) {

                        Text("make by marcinlowercase")
                    }
                }
            }

        }
    }
}

@Composable
fun SliderSetting(
    browserSettings: BrowserSettings,
    updateBrowserSettingsForSpecificValue: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onBackClick: () -> Unit,
    textFieldValueFun: (String) -> String,
    afterDecimal: Boolean = true,
    iconID: Int,
    digitCount: Int = 4,
    currentSettingOriginalValue: Float,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var digits by remember {
        mutableStateOf(
            ((currentSettingOriginalValue * if (afterDecimal) 100f else 1f).roundToInt())
                .toString()
                .padStart(digitCount, '0')

        )
    }

    // 1. The raw digits are the single source of truth.
    // Initialize it from the global browserSettings.


    val sliderValue = (digits.toIntOrNull() ?: 0) / if (afterDecimal) 100f else 1f


    val commitTextFieldValue = {
        val parsedValue = (textFieldValueFun(digits).toFloatOrNull() ?: 0f)
        val coercedValue = parsedValue.coerceIn(valueRange)

        // Update the global settings with the coerced value.
        updateBrowserSettingsForSpecificValue(coercedValue)

        // CRUCIAL: Update the 'digits' state based on the coerced value.
        // This forces the TextField to display the corrected number (e.g., "60.00").
        digits = ((coercedValue * if (afterDecimal) 100 else 1).roundToInt())
            .toString()
            .padStart(digitCount, '0')
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(
                    cornerRadiusForLayer(
                        2,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp
                    ).dp
                )
            )
            .padding(browserSettings.paddingDp.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    heightForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp,
                        browserSettings.singleLineHeight,
                    ).dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Back button to return to the main settings view
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            cornerRadiusForLayer(
                                3,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    )
                    .fillMaxHeight()
                    .background(Color.White)
                    .defaultMinSize(
                        minWidth = heightForLayer(
                            3,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp,
                            browserSettings.singleLineHeight,
                        ).dp
                    )


            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back to Settings",
                    tint = Color.Black
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center // 2. Center the content of the Box
            ) {
                BasicTextField(
                    value = textFieldValueFun(digits),
                    onValueChange = {},
                    modifier = Modifier

                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyUp) {
                                // --- THIS IS THE CORRECTED LOGIC ---

                                // 1. Get the unicode character as an Int.
                                val unicodeChar = event.nativeKeyEvent.unicodeChar

                                // 2. Check if it's a valid character (not 0) and then convert it to a Char.
                                if (unicodeChar != 0) {
                                    val typedChar = unicodeChar.toChar()

                                    // 3. Now, safely call digitToIntOrNull() on the Char.
                                    val digit = typedChar.digitToIntOrNull()

                                    if (digit != null) {
                                        // Append new digit and keep the last 4 characters.
                                        digits = (digits + digit.toString()).takeLast(digitCount)
                                        return@onKeyEvent true // Event handled
                                    }
                                }
                                // --- END OF CORRECTION ---

                                // Check for the Backspace key
                                if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DEL) {
                                    digits = ("0$digits").take(digitCount)
                                    return@onKeyEvent true // Event handled
                                }
                            }
                            false // Event not handled
                        }
                        .onFocusChanged {

                            commitTextFieldValue()
                        },
                    cursorBrush = SolidColor(Color.Transparent),

                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, // Show the number pad
                        imeAction = ImeAction.Done // Show a "Done" button
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {

//                            updateBrowserSettingsForSpecificValue(sliderValue.coerceIn(valueRange))
                            commitTextFieldValue()
                            // When the user presses "Done", hide the keyboard and clear focus.
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true
                )
            }
            IconButton(
                onClick = { },
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            cornerRadiusForLayer(
                                3,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    )
                    .fillMaxHeight()
                    .defaultMinSize(
                        minWidth = heightForLayer(
                            3,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp,
                            browserSettings.singleLineHeight,
                        ).dp
                    )


            ) {
                Icon(
                    painter = painterResource(id = iconID),
                    contentDescription = "Back to Settings",
                    tint = Color.White
                )
            }
        }
        Slider(
            value = sliderValue,
            onValueChange = { newSliderValue ->

                val finalValue = newSliderValue.coerceIn(valueRange)

                // 2. Update the digits string based on this final, clean value.
                digits = ((finalValue * if (afterDecimal) 100 else 1).roundToInt())
                    .toString()
                    .padStart(digitCount, '0')

                // 3. Immediately pass the NEW, CORRECT finalValue to your update function.
                updateBrowserSettingsForSpecificValue(finalValue)
            },
            valueRange = valueRange,
            steps = steps,

            modifier = Modifier
                .fillMaxWidth()
                .padding(top = browserSettings.paddingDp.dp)
                .height(
                    heightForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp,
                        browserSettings.singleLineHeight,
                    ).dp
                )
                .padding(browserSettings.paddingDp.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.Black,
                inactiveTickColor = Color.White,
                activeTickColor = Color.Black,


                )
        )

    }
}

@Composable
fun TextSetting(
    browserSettings: BrowserSettings,
    updateBrowserSettingsForSpecificValue: (String) -> Unit, // Takes a String now
    onBackClick: () -> Unit,
    iconID: Int,
    currentSettingOriginalValue: String,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // State to hold the text being edited.
    var textValue by remember { mutableStateOf(currentSettingOriginalValue) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(
                    cornerRadiusForLayer(
                        2,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp
                    ).dp
                )
            )
            .padding(browserSettings.paddingDp.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- TOP ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    heightForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp,
                        browserSettings.singleLineHeight,
                    ).dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button (same as before)
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            cornerRadiusForLayer(
                                3,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    )
                    .fillMaxHeight()
                    .background(Color.White)
                    .defaultMinSize(
                        minWidth = heightForLayer(
                            3,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp,
                            browserSettings.singleLineHeight,
                        ).dp
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back to Settings",
                    tint = Color.Black
                )
            }

            // --- SPACER (as requested) ---
            Spacer(modifier = Modifier.weight(1f))

            // Right Icon (same as before)
            IconButton(
                onClick = { /* No action */ },
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            cornerRadiusForLayer(
                                3,
                                browserSettings.deviceCornerRadius,
                                browserSettings.paddingDp
                            ).dp
                        )
                    )
                    .fillMaxHeight()
                    .defaultMinSize(
                        minWidth = heightForLayer(
                            3,
                            browserSettings.deviceCornerRadius,
                            browserSettings.paddingDp,
                            browserSettings.singleLineHeight,
                        ).dp
                    )
            ) {
                Icon(
                    painter = painterResource(id = iconID),
                    contentDescription = "Setting Icon",
                    tint = Color.White
                )
            }
        }

        // --- OutlinedTextField (replaces Slider) ---
        OutlinedTextField(
            value = textValue,
            onValueChange = { textValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = browserSettings.paddingDp.dp)
                .height(
                    heightForLayer(
                        3,
                        browserSettings.deviceCornerRadius,
                        browserSettings.paddingDp,
                        browserSettings.singleLineHeight,
                    ).dp
                ),
            shape = RoundedCornerShape(
                cornerRadiusForLayer(
                    3,
                    browserSettings.deviceCornerRadius,
                    browserSettings.paddingDp
                ).dp
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Black.copy(0.95f),
                unfocusedContainerColor = Color.Black.copy(0.8f),
                cursorColor = Color.White,
                focusedIndicatorColor = Color.White.copy(0.95f),
                unfocusedIndicatorColor = Color.White.copy(0.8f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White.copy(0.8f),
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri, // Good for URLs
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    updateBrowserSettingsForSpecificValue(textValue)
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            singleLine = true
        )
    }
}


//endregion
