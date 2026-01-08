package marcinlowercase.a

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marcinlowercase.a.core.constant.inject_corner_radius
import marcinlowercase.a.core.constant.generic_location_permission
import marcinlowercase.a.core.constant.default_url
import marcinlowercase.a.core.constant.favicon_discovery
import marcinlowercase.a.core.constant.pixel_9_corner_radius
import marcinlowercase.a.core.custom_class.CustomWebView
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.ConfirmationDialogState
import marcinlowercase.a.core.data_class.ContextMenuData
import marcinlowercase.a.core.data_class.CustomPermissionRequest
import marcinlowercase.a.core.data_class.DownloadItem
import marcinlowercase.a.core.data_class.JsAlert
import marcinlowercase.a.core.data_class.JsConfirm
import marcinlowercase.a.core.data_class.JsDialogState
import marcinlowercase.a.core.data_class.JsPrompt
import marcinlowercase.a.core.data_class.PanelVisibilityState
import marcinlowercase.a.core.data_class.PollData
import marcinlowercase.a.core.data_class.SiteSettings
import marcinlowercase.a.core.data_class.Suggestion
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.data_class.App
import marcinlowercase.a.core.enum_class.BottomPanelMode
import marcinlowercase.a.core.enum_class.DownloadStatus
import marcinlowercase.a.core.enum_class.GestureNavAction
import marcinlowercase.a.core.enum_class.RevealState
import marcinlowercase.a.core.enum_class.SearchEngine
import marcinlowercase.a.core.enum_class.SuggestionSource
import marcinlowercase.a.core.enum_class.TabState
import marcinlowercase.a.core.function.createNotificationChannel
import marcinlowercase.a.core.function.rememberAnchoredDraggableState
import marcinlowercase.a.core.function.toDomain
import marcinlowercase.a.core.function.webViewLoad
import marcinlowercase.a.core.manager.BrowserDownloadManager
import marcinlowercase.a.core.manager.SiteSettingsManager
import marcinlowercase.a.core.manager.TabManager
import marcinlowercase.a.core.manager.AppManager
import marcinlowercase.a.core.manager.VisitedUrlManager
import marcinlowercase.a.core.manager.WebViewManager
import marcinlowercase.a.core.view_model.AppViewModel
import marcinlowercase.a.ui.panel.BottomPanel
import marcinlowercase.a.ui.panel.SettingPanelView
import marcinlowercase.a.ui.panel.SettingsPanel
import marcinlowercase.a.ui.screen.NoInternetScreen
import marcinlowercase.a.ui.theme.Theme
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.regex.Pattern
import kotlin.String
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.system.exitProcess


//endregion


//region Composable


class MainActivity : ComponentActivity() {

    private val tabManager by lazy { TabManager(this) }
    private val webViewManager by lazy { WebViewManager(this) }
    val newUrlFromIntent = MutableStateFlow<String?>(null)

    private val appViewModel: AppViewModel by viewModels()


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)



        createNotificationChannel(this) // Call it here
        setContent {
            val isOnline by appViewModel.isOnline.collectAsStateWithLifecycle()
            Theme {
//                // HIDE SYSTEM BARS
//                Surface(modifier = Modifier.fillMaxSize()) {
//                    BrowserScreen(
//                        isOnline = isOnline,
//                        newUrlFlow = newUrlFromIntent,
//                        tabManager = tabManager,
//                        webViewManager = webViewManager
//                    )
//                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BrowserScreen(
                        innerPadding = innerPadding,
                        modifier = Modifier,

                        isOnline = isOnline,
                        newUrlFlow = newUrlFromIntent,
                        tabManager = tabManager,
                        webViewManager = webViewManager
                    )
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

        // HIDE SYSTEM BARS
//        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
//        // Hide the system bars.
//        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
//        // Configure the behavior for revealing them temporarily.
//        windowInsetsController.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.dataString?.let { urlFromIntent ->
                // Instead of creating the tab here, we just emit the URL.
                // The Composable will react to this emission.
                newUrlFromIntent.update { urlFromIntent }
            }
        }
    }

    override fun onStop() {
        super.onStop()


        val activeWebView = webViewManager.activeWebView
        val tabs = tabManager.loadTabs() // Load current tabs to modify them
        val activeIndex = tabs.indexOfFirst { it.state == TabState.ACTIVE }

        if (activeWebView != null && activeIndex != -1) {
            val outState = Bundle()
            activeWebView.saveState(outState)
            val stateBytes = outState.getByteArray("WEBVIEW_CHROMIUM_STATE")

            if (stateBytes != null) {
                // Encode the byte array to a Base64 string for storage
                val encodedState = Base64.encodeToString(stateBytes, Base64.DEFAULT)
                tabs[activeIndex].savedState = encodedState
                // Now save the modified tabs list back
                tabManager.saveTabs(tabs, activeIndex)
            }
        }


        // When the app goes to the background, freeze everything.
        tabManager.freezeAllTabs()
    }
}


@Composable
fun BrowserScreen(
    innerPadding: PaddingValues,
    isOnline: Boolean,
    newUrlFlow: StateFlow<String?>,
    tabManager: TabManager,
    webViewManager: WebViewManager,
    modifier: Modifier = Modifier
) {


    //region Variables
    val context = LocalContext.current
    var saveTrigger by remember { mutableIntStateOf(0) }
    val sharedPrefs =
        remember { context.getSharedPreferences("BrowserPrefs", Context.MODE_PRIVATE) }
//    var browserSettings by remember {
//        mutableStateOf(
//            BrowserSettings(
//                isFirstAppLoad = sharedPrefs.getBoolean("is_first_app_load", true),
//                padding = sharedPrefs.getFloat("padding", 8f),
//                deviceCornerRadius = sharedPrefs.getFloat(
//                    "device_corner_radius",
//                    pixel_9_corner_radius
//                ),
//                defaultUrl = sharedPrefs.getString("default_url", default_url)
//                    ?: default_url,
//                animationSpeed = sharedPrefs.getFloat("animation_speed", 300f),
//                singleLineHeight = sharedPrefs.getFloat("single_line_height", 100f),
////                isDesktopMode = sharedPrefs.getBoolean("is_desktop_mode", false),
////                desktopModeWidth = sharedPrefs.getInt("desktop_mode_width", 820),
//                isSharpMode = sharedPrefs.getBoolean("is_sharp_mode", false),
////                topSharpEdge = sharedPrefs.getFloat("top_sharp_edge", 65.90476f),
////                bottomSharpEdge = sharedPrefs.getFloat("bottom_sharp_edge", 65.90476f),
////                topSharpEdge = sharedPrefs.getFloat("top_sharp_edge", pixel_9_corner_radius),
////                bottomSharpEdge = sharedPrefs.getFloat("bottom_sharp_edge", pixel_9_corner_radius),
//                cursorContainerSize = sharedPrefs.getFloat(
//                    "cursor_container_size",
//                    50f
//                ),
//                cursorPointerSize = sharedPrefs.getFloat("cursor_pointer_size", 5f),
//                cursorTrackingSpeed = sharedPrefs.getFloat("cursor_tracking_speed", 1.75f),
//                showSuggestions = sharedPrefs.getBoolean("show_suggestions", false),
//                closedTabHistorySize = sharedPrefs.getFloat("closed_tab_history_size", 2f),
//                backSquareOffsetX = sharedPrefs.getFloat(
//                    "back_square_offset_x",
//                    -1f
//                ), // Use -1f as a flag for "not set"
//                backSquareOffsetY = sharedPrefs.getFloat("back_square_offset_y", -1f),
//                backSquareIdleOpacity = sharedPrefs.getFloat("back_square_idle_opacity", 0.2f),
//                maxListHeight = sharedPrefs.getFloat("max_list_height", 2.5f),
//                searchEngine = sharedPrefs.getInt("search_engine", SearchEngine.GOOGLE.ordinal),
//            )
//        )
//    }
    val browserSettings = remember {
        mutableStateOf(
            BrowserSettings(
                isFirstAppLoad = sharedPrefs.getBoolean("is_first_app_load", true),
                padding = sharedPrefs.getFloat("padding", 8f),
                deviceCornerRadius = sharedPrefs.getFloat(
                    "device_corner_radius",
                    pixel_9_corner_radius
                ),
                defaultUrl = sharedPrefs.getString("default_url", default_url)
                    ?: default_url,
                animationSpeed = sharedPrefs.getFloat("animation_speed", 300f),
                singleLineHeight = sharedPrefs.getFloat("single_line_height", 100f),
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
                showSuggestions = sharedPrefs.getBoolean("show_suggestions", false),
                closedTabHistorySize = sharedPrefs.getFloat("closed_tab_history_size", 2f),
                backSquareOffsetX = sharedPrefs.getFloat(
                    "back_square_offset_x",
                    -1f
                ), // Use -1f as a flag for "not set"
                backSquareOffsetY = sharedPrefs.getFloat("back_square_offset_y", -1f),
                backSquareIdleOpacity = sharedPrefs.getFloat("back_square_idle_opacity", 0.2f),
                maxListHeight = sharedPrefs.getFloat("max_list_height", 2.5f),
                searchEngine = sharedPrefs.getInt("search_engine", SearchEngine.GOOGLE.ordinal),
                isFullscreenMode = sharedPrefs.getBoolean("is_fullscreen_mode", false),
            )
        )
    }

    var savedPanelState by remember { mutableStateOf<PanelVisibilityState?>(null) }
    var initialLoadDone by rememberSaveable { mutableStateOf(false) }
    val tabs = remember {
        mutableStateListOf<Tab>().apply {
            addAll(tabManager.loadTabs())
        }
    }

    val appManager = remember { AppManager(context) }
    val apps = remember { mutableStateListOf<App>().apply { addAll(appManager.loadApps()) } }


    val activeTabIndex = remember {
        mutableIntStateOf(tabs.indexOfFirst { it.state == TabState.ACTIVE }.coerceAtLeast(0))
    }
    val textFieldState =
        rememberTextFieldState(webViewManager.getWebView(tabs[activeTabIndex.intValue]).url ?: "")
    val recentlyClosedTabs = remember { mutableStateListOf<Tab>() }
    val activeTab = tabs.getOrNull(activeTabIndex.intValue)
    val activeWebView = activeTab?.let { tab ->
        webViewManager.getWebView(tab).apply {
            // If the tab was frozen, its WebView was just created and needs to load its URL
            if (tab.state == TabState.FROZEN || this.url == null) {


                if (tab.savedState != null) {
                    try {
                        // 1. Decode the Base64 string back to a byte array
                        val stateBytes = Base64.decode(tab.savedState, Base64.DEFAULT)
                        val restoreBundle = Bundle()
                        // 2. Put the bytes back into a new Bundle with the correct key
                        restoreBundle.putByteArray("WEBVIEW_CHROMIUM_STATE", stateBytes)
                        // 3. Restore the state into the WebView
                        this.restoreState(restoreBundle)



                        initialLoadDone = true
                        this.url?.let { restoredUrl ->
                            textFieldState.setTextAndPlaceCursorAtEnd(restoredUrl.toDomain())
//                            textFieldValue =
//                                TextFieldValue(restoredUrl, TextRange(restoredUrl.length))
                        }


                        // 4. CRITICAL: Clear the state so it's not restored again on config change
                        tab.savedState = null
                        saveTrigger++ // Trigger a save to persist the cleared state

                    } catch (_: Exception) {
                        // Fallback to loading the URL if restore fails
//                        webViewLoad(this, browserSettings.value.defaultUrl, browserSettings)
                        val urlToLoad =
                            tab.currentURL.ifBlank { browserSettings.value.defaultUrl }
                        webViewLoad(this, urlToLoad, browserSettings.value)
                    }
                } else {
                    // No saved state, just load the URL normally
                    webViewLoad(this, browserSettings.value.defaultUrl, browserSettings.value)
                }
            }
        }
    }
    val siteSettingsManager = remember { SiteSettingsManager(context) }
    val siteSettings = remember {
        mutableStateMapOf<String, SiteSettings>().apply {
            putAll(siteSettingsManager.loadSettings())
        }
    }
    var isLoading by remember { mutableStateOf(false) }
    var isFocusOnTextField by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isUrlBarVisible by rememberSaveable { mutableStateOf(true) }
    var isUrlOverlayBoxVisible by rememberSaveable { mutableStateOf(true) }
    var isPermissionPanelVisible by rememberSaveable { mutableStateOf(false) }
    var isBottomPanelVisible by rememberSaveable { mutableStateOf(true) }
    var isPromptPanelVisible by rememberSaveable { mutableStateOf(false) }
    var isTabsPanelVisible by remember { mutableStateOf(false) }
    var tabsPanelLock by remember { mutableStateOf(false) }
    val descriptionContent = remember { mutableStateOf("") }
    var isNavPanelVisible by remember { mutableStateOf(false) }
    val isLongPressDrag = remember { mutableStateOf(false) }
    var activeNavAction by remember { mutableStateOf(GestureNavAction.REFRESH) }
    var isTabDataPanelVisible by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
//    var isOptionsPanelVisible by rememberSaveable { mutableStateOf(false) }
    val isSettingsPanelVisible = remember { mutableStateOf(browserSettings.value.isFirstAppLoad) }
    val isBottomPanelLock = remember { mutableStateOf(false) }
    val isAppsPanelVisible = remember { mutableStateOf(false) }
    val resetBottomPanelTrigger = remember { mutableStateOf(false) }
    val isSettingCornerRadius = remember { mutableStateOf(true) }


    val offsetY = remember { Animatable(0f) }
    var overlayHeightPx by remember { mutableFloatStateOf(0f) }
    val animatedCornerRadius by animateDpAsState(
        targetValue = if (!browserSettings.value.isSharpMode) browserSettings.value.deviceCornerRadius.dp else 0.dp,
        label = "Corner Radius Animation",
    )
    // Get the raw cutout padding values.
    val cutoutPaddingValues = WindowInsets.displayCutout.asPaddingValues()
    val cutoutTop = cutoutPaddingValues.calculateTopPadding()
    val cutoutBottom = cutoutPaddingValues.calculateBottomPadding()
    cutoutPaddingValues.calculateBottomPadding()
//    val webViewTopPaddingFullscreen by animateDpAsState(
//        targetValue = if (browserSettings.value.isSharpMode) (
//                if (cutoutTop >= browserSettings.value.deviceCornerRadius.dp) cutoutTop else browserSettings.value.deviceCornerRadius.dp
//                ) else cutoutTop,
//        label = "WebView Top Padding Animation"
//    )
//    val webViewTopPaddingRegular by animateDpAsState(
//        targetValue = if (browserSettings.value.isSharpMode) (
//                browserSettings.value.deviceCornerRadius.dp
//                ) else 0.dp,
//        label = "WebView Top Padding Animation"
//    )


    val webViewTopPaddingFullscreen = if (browserSettings.value.isSharpMode) {
        maxOf(cutoutTop, browserSettings.value.deviceCornerRadius.dp)
    } else {
        cutoutTop
    }
    val webViewTopPaddingRegular = if (browserSettings.value.isSharpMode) {
//            browserSettings.value.deviceCornerRadius.dp
        maxOf(
            maxOf(cutoutTop, browserSettings.value.deviceCornerRadius.dp),
            innerPadding.calculateTopPadding()
        )

    } else {
        innerPadding.calculateTopPadding()
    }
    val webViewTopPaddingNormalScreen = if (browserSettings.value.isFullscreenMode) {
        webViewTopPaddingFullscreen
    } else {
        webViewTopPaddingRegular
    }


    val webViewBottomPaddingFullscreen = if (browserSettings.value.isSharpMode) {
        maxOf(cutoutBottom, browserSettings.value.deviceCornerRadius.dp)
    } else {
        cutoutBottom
    }
    val webViewBottomPaddingRegular = if (browserSettings.value.isSharpMode) {
        maxOf(
            maxOf(cutoutBottom, browserSettings.value.deviceCornerRadius.dp),
            innerPadding.calculateBottomPadding()
        )
    } else {
        innerPadding.calculateBottomPadding()

    }


    val webViewBottomPaddingNormalScreen = if (browserSettings.value.isFullscreenMode) {
        webViewBottomPaddingFullscreen
    } else {
        webViewBottomPaddingRegular
    }


    val targetWebViewTopPadding =
        if (isSettingCornerRadius.value) 0.dp else webViewTopPaddingNormalScreen
    val targetWebViewBottomPadding =
        if (isSettingCornerRadius.value) 0.dp else webViewBottomPaddingNormalScreen

    val webViewTopPadding by animateDpAsState(
        targetValue = targetWebViewTopPadding,
    )


    val webViewBottomPadding by animateDpAsState(
        targetValue = targetWebViewBottomPadding,
    )
    val webViewPaddingValue = PaddingValues(
        start = 0.dp,
        top = webViewTopPadding,
        end = 0.dp,
        bottom = webViewBottomPadding
    )
    val imeInsets = WindowInsets.ime.asPaddingValues()
    val keyboardHeight = imeInsets.calculateBottomPadding()
    val isKeyboardVisible = keyboardHeight > 0.dp

    val floatingPanelBottomPaddingNoKeyboard = if (browserSettings.value.isFullscreenMode) {
        webViewBottomPaddingFullscreen
    } else {
        webViewBottomPaddingRegular
    }
    val floatingPanelBottomPadding by animateDpAsState(
        targetValue = if (isKeyboardVisible) (
                0.dp
                ) else (floatingPanelBottomPaddingNoKeyboard),
        animationSpec = tween(browserSettings.value.animationSpeedForLayer(1)),
        label = "Floating Panel Padding Animation"
    )


    var pendingPermissionRequest by remember {
        mutableStateOf<CustomPermissionRequest?>(null)
    }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var originalOrientation by remember { mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) }
    val activity = context as? Activity // Get the activity reference
    val isDarkTheme = isSystemInDarkTheme()
    val view = LocalView.current // Get the underlying view
    val savePermissionDecision = { domain: String, permissions: Map<String, Boolean> ->
        val currentSettings = siteSettings.getOrPut(domain) { SiteSettings(domain = domain) }
        val updatedDecisions = currentSettings.permissionDecisions.toMutableMap()
        // First, add all results from the system dialog
        updatedDecisions.putAll(permissions)

        // Now, check if any location permission was part of the request
        if (updatedDecisions.containsKey(Manifest.permission.ACCESS_FINE_LOCATION) ||
            updatedDecisions.containsKey(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            // Determine if location was granted (either fine or coarse is enough)
            val isGranted = updatedDecisions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    updatedDecisions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            // Remove the specific, detailed permissions
            updatedDecisions.remove(Manifest.permission.ACCESS_FINE_LOCATION)
            updatedDecisions.remove(Manifest.permission.ACCESS_COARSE_LOCATION)

            // Add our single, generic permission entry
            updatedDecisions[generic_location_permission] = isGranted
        }

        // Proceed with saving the consolidated map
        val newSettings = currentSettings.copy(permissionDecisions = updatedDecisions)
        siteSettings[domain] = newSettings // Trigger state update
        siteSettingsManager.saveSettings(siteSettings)
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

    val squareAlpha = remember { Animatable(0f) }

    //  hold the currently active dialog
    var jsDialogState by remember { mutableStateOf<JsDialogState?>(null) }
    var promptComponentDisplayState by remember { mutableStateOf<JsDialogState?>(null) }

    val downloadTracker = remember { BrowserDownloadManager(context) }
    val downloads =
        remember { mutableStateListOf<DownloadItem>().apply { addAll(downloadTracker.loadDownloads()) } }
    val isDownloadPanelVisible = remember { mutableStateOf(false) }
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

    val cursorPadHeight by animateDpAsState(
        targetValue = if (isKeyboardVisible) ((screenSizeDp.height.dp - webViewTopPadding) / 8
                ) else (screenSizeDp.height.dp - webViewTopPadding) / 2,
        label = "Cursor Pad Height Animation"
    )
    val urlBarFocusRequester = remember { FocusRequester() } // <-- CREATE IT HERE
    val isFindInPageVisible = remember { mutableStateOf(false) }
    val findInPageText = remember { mutableStateOf("") }
    val findInPageResult = remember { mutableStateOf(0 to 0) }
    val visitedUrlManager = remember { VisitedUrlManager(context) }
    val visitedUrlMap =
        remember { mutableStateMapOf<String, String>().apply { putAll(visitedUrlManager.loadUrlMap()) } }
    val suggestions = remember { mutableStateListOf<Suggestion>() }
    val initialX =
        if (browserSettings.value.backSquareOffsetX != -1f) browserSettings.value.backSquareOffsetX else 0f
    val initialY =
        if (browserSettings.value.backSquareOffsetY != -1f) browserSettings.value.backSquareOffsetY else 0f
    val backSquareOffsetX = remember { Animatable(initialX) }
    val backSquareOffsetY = remember { Animatable(initialY) }
    var isBackSquareInitialized by remember {
        mutableStateOf(browserSettings.value.backSquareOffsetX != -1f)
    }

    var contextMenuData by remember { mutableStateOf<ContextMenuData?>(null) }
    var displayContextMenuData by remember { mutableStateOf<ContextMenuData?>(null) }

    val bottomPanelPagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    val inspectingAppId = remember { mutableLongStateOf(0L) }


    var initialSettingPanelView by remember { mutableStateOf(SettingPanelView.MAIN) }


//    val currentSearchEngine = remember { mutableStateOf(SearchEngine.GOOGLE) }
    val isPinningApp = remember { mutableStateOf(false) }


    val optionsPanelHeight =
        (browserSettings.value.heightForLayer(2) + browserSettings.value.padding).dp
    val optionsPanelHeightPx = with(density) { optionsPanelHeight.toPx() }

    // 2. Define Anchors
    // Hidden = 0px, Visible = -200px (moving UP means negative Y)
    val anchors = remember(optionsPanelHeightPx) {
        DraggableAnchors {
            RevealState.Hidden at 0f
            RevealState.Visible at -optionsPanelHeightPx
        }
    }
    val draggableState = rememberAnchoredDraggableState(
        initialValue = RevealState.Hidden,
        anchors = anchors
    )

    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = draggableState,
        positionalThreshold = { distance -> distance * 0.5f },

        )

    //endregion

    //region Functions
    val setIsOptionsPanelVisible = { setToVisible: Boolean ->

        coroutineScope.launch {
            if (setToVisible) {
                draggableState.animateTo(
                    targetValue = RevealState.Visible,
                    animationSpec = tween(
                        durationMillis = browserSettings.value.animationSpeedForLayer(1),
                    )
                )
            } else {
                draggableState.animateTo(
                    targetValue = RevealState.Hidden,
                    animationSpec = tween(
                        durationMillis = browserSettings.value.animationSpeedForLayer(1),
                    )
                )
            }
            delay((browserSettings.value.animationSpeedForLayer(1) * 2).toLong())

        }

    }

    val closeAllTabs = {
        tabs.forEach { tab ->
            webViewManager.destroyWebView(tab)
        }

        // 2. Clear the in-memory list
        tabs.clear()

        // 3. Clear the persisted list in SharedPreferences
        tabManager.clearAllTabs()

        // 4. Close the application
        activity?.finishAndRemoveTask()
        exitProcess(0)
    }
    val removeSuggestionFromHistory = { suggestionToRemove: Suggestion ->
        if (suggestionToRemove.source == SuggestionSource.HISTORY) {
            // Remove from persistent storage
            visitedUrlManager.removeUrl(suggestionToRemove.url)

            // Remove from our in-memory state maps
            visitedUrlMap.remove(suggestionToRemove.url)

            // Remove from the currently displayed suggestion list for immediate UI feedback
            suggestions.remove(suggestionToRemove)
        }
    }

    val reopenClosedTab = {
        // Check if there are any tabs to reopen.
        if (recentlyClosedTabs.isNotEmpty()) {
            // Get the last closed tab and remove it from the stack.
            val tabToReopen = recentlyClosedTabs.removeAt(recentlyClosedTabs.lastIndex)

            // Deactivate the current tab.
            if (activeTabIndex.intValue in tabs.indices) {
                tabs[activeTabIndex.intValue].state = TabState.BACKGROUND
            }

            // Add the reopened tab back to the list, usually at the end or a specific index.
            // Let's add it at the end for simplicity.
            tabs.add(tabToReopen)

            // Make the reopened tab the new active tab.
            activeTabIndex.intValue = tabs.lastIndex
            tabToReopen.state = TabState.ACTIVE

            // Trigger a save.
            saveTrigger++
        }
    }

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


    val handleHistoryNavigation =
        { tabToNavigate: Tab, historyIndex: Int, webViewManager: WebViewManager ->
            val tabIndexInMainList = tabs.indexOf(tabToNavigate)

            // --- Use a positive case check ---
            if (tabIndexInMainList != -1) {

                isUrlBarVisible = false

//                webViewManager.getWebView(tabToNavigate).goBackOrForward(1)

                val webViewToNavigate = webViewManager.getWebView(tabToNavigate)

                val stepsToNavigate =
                    -1 * (webViewToNavigate.copyBackForwardList().currentIndex - historyIndex)


                if (stepsToNavigate != 0 && webViewToNavigate.canGoBackOrForward(stepsToNavigate)) {

                    // --- All checks passed, proceed with the logic ---

                    // 1. Update the history state of the target tab


                    // 2. Make the target tab the active tab


                    webViewToNavigate.goBackOrForward(stepsToNavigate)

                    val newUrl = webViewToNavigate.url ?: ""
                    textFieldState.setTextAndPlaceCursorAtEnd(newUrl.toDomain())
//                    textFieldValue = TextFieldValue(newUrl, TextRange(newUrl.length))

                }
                if (activeTabIndex.intValue != tabIndexInMainList) {
                    tabs[activeTabIndex.intValue].state = TabState.BACKGROUND
                    activeTabIndex.intValue = tabIndexInMainList
                    tabToNavigate.state = TabState.ACTIVE
                }
                // If the inner 'if' fails (bad history), nothing happens, which is correct.
            }
            // If the outer 'if' fails (tab not found), nothing happens, which is correct.


        }


    val handlePermissionToggle = { domain: String?, permission: String, isGranted: Boolean ->


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
                message = "refresh ?",
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
                message = "close tab ?",
                onConfirm = {
                    val indexToClose = tabs.indexOf(tabToClose)

                    if (tabs.size > 1) {

                        recentlyClosedTabs.add(tabToClose)
                        val limit = browserSettings.value.closedTabHistorySize.roundToInt()
                        while (recentlyClosedTabs.size > limit) {
                            // Remove the oldest tab from the bottom of the list.
                            recentlyClosedTabs.removeAt(0)
                        }

                        webViewManager.destroyWebView(tabToClose)
                        tabs.removeAt(indexToClose)

                        // Determine the next active tab
                        if (indexToClose == activeTabIndex.intValue) {
                            val nextTabIndex = if (indexToClose >= tabs.size) {
                                tabs.lastIndex
                            } else {
                                indexToClose
                            }


                            tabs[nextTabIndex].state = TabState.ACTIVE
                            inspectingTabId = tabs[nextTabIndex].id
                            activeTabIndex.intValue = nextTabIndex

//                    val urlToLoad = tabs[nextTabIndex].currentUrl ?: browserSettings.value.defaultUrl
//                    textFieldValue = TextFieldValue(urlToLoad, TextRange(urlToLoad.length))
//
//                    activeWebView?.loadUrl(urlToLoad)
                        } else
                            if (indexToClose < activeTabIndex.intValue) {
                                activeTabIndex.intValue -= 1
                            }


//                val urlToLoad = tabs[nextTabIndex].currentUrl ?: browserSettings.value.defaultUrl
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
            message = "clear site data ?",
            onConfirm = {
                val inspectingTab = currentInspectingTab
                val inspectingWebView =
                    webViewManager.getWebView(inspectingTab ?: Tab.createEmpty())
                if (inspectingTab != null) {
                    val domain = siteSettingsManager.getDomain(inspectingWebView.url ?: "")
                    if (domain != null) {
                        // 1. Remove the settings for this domain from the state map
                        siteSettings.remove(domain)
                        // 2. Save the updated map to persistent storage
                        siteSettingsManager.saveSettings(siteSettings)
                    }

                    if (inspectingTab.state != TabState.FROZEN) {
                        val webView = webViewManager.getWebView(inspectingTab)
                        CookieManager.getInstance().removeAllCookies(null)
                        CookieManager.getInstance().flush()
                        WebStorage.getInstance().deleteOrigin(webView.url)
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
                    } catch (_: Exception) {
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
        } catch (_: Exception) {
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


    val startDownload =
        { url: String, userAgent: String, contentDisposition: String?, mimeType: String? ->
            if (!isUrlBarVisible) isUrlBarVisible = true
            if (!isDownloadPanelVisible.value) isDownloadPanelVisible.value = true

            // (Reuse your existing filename/unique logic here)
            val initialFilename = getBestGuessFilename(url, contentDisposition, mimeType)
            val finalFilename = generateUniqueFilename(initialFilename, downloads)

            val request = DownloadManager.Request(url.toUri())
                .setTitle(finalFilename)
                .setDescription("Downloading...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, finalFilename)
                .addRequestHeader("User-Agent", userAgent)

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val newDownload = DownloadItem(
                id = downloadId,
                url = url,
                filename = finalFilename,
                mimeType = mimeType ?: "application/octet-stream",
                status = DownloadStatus.PENDING
            )
            downloads.add(0, newDownload)
            downloadTracker.saveDownloads(downloads)
        }

    // This function will be our single, safe way to update settings.
    val updateBrowserSettings = { newSettings: BrowserSettings ->
        browserSettings.value = newSettings
    }

    val resetBrowserSettings = {
        updateBrowserSettings(
            browserSettings.value.copy(
                padding = 8f,
                deviceCornerRadius = pixel_9_corner_radius,
                defaultUrl = default_url,
                animationSpeed = 300f,
                singleLineHeight = 100f,
                isSharpMode = false,
                cursorContainerSize = 50f,
                cursorPointerSize = 5f,
                cursorTrackingSpeed = 1.75f,
                backSquareIdleOpacity = 0.2f
            )
        )
    }

    fun createNewTab(insertAtIndex: Int, url: String = browserSettings.value.defaultUrl) {
        if (activeTabIndex.intValue in tabs.indices) {
            tabs[activeTabIndex.intValue].state = TabState.BACKGROUND
        }

        val newTab = Tab(
            state = TabState.ACTIVE,
        )

        tabs.add(insertAtIndex, newTab)

        val newWebView = webViewManager.getWebView(newTab)
//        newWebView.loadUrl(url)
        webViewLoad(newWebView, url, browserSettings.value)

        inspectingTabId = newTab.id

        activeTabIndex.intValue = insertAtIndex


//        textFieldValue = TextFieldValue(url, TextRange(url.length))
        textFieldState.setTextAndPlaceCursorAtEnd(url.toDomain())
        saveTrigger++


    }


    fun navigateWebView() {
        when (activeNavAction) {
            GestureNavAction.BACK -> if (activeWebView?.canGoBack() ?: false) {
                activeWebView.goBack()
            }

            GestureNavAction.REFRESH -> {
                activeWebView?.reload()
            }

            GestureNavAction.FORWARD -> if (activeWebView?.canGoForward() ?: false) {
                activeWebView.goForward()
            }

            GestureNavAction.CLOSE_TAB -> {
                if (tabs.size > 1) {
                    val tabToRemoveIndex = activeTabIndex.intValue
                    val tabToRemove = tabs[tabToRemoveIndex]
                    recentlyClosedTabs.add(tabToRemove)

                    val limit = browserSettings.value.closedTabHistorySize.roundToInt()
                    while (recentlyClosedTabs.size > limit) {
                        // Remove the oldest tab from the bottom of the list.
                        recentlyClosedTabs.removeAt(0)
                    }

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


                    val urlToLoad = webViewManager.getWebView(tabs[nextTabIndex]).url
                        ?: browserSettings.value.defaultUrl
                    webViewLoad(activeWebView, urlToLoad, browserSettings.value)
                    textFieldState.setTextAndPlaceCursorAtEnd(urlToLoad.toDomain())
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
//        Toast.makeText(context, "download list cleared.", Toast.LENGTH_SHORT).show()
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
    val backgroundColor = remember { mutableStateOf(Color.Black) }


    //endregion


    // This effect now ONLY handles the very first restoration of state.


    //region LaunchedEffect

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // 1. Make the status bar transparent

            // 2. FORCE White Icons (set to false)
            insetsController.isAppearanceLightStatusBars = false
        }
    }
    LaunchedEffect(isSettingsPanelVisible.value) {
        if (!isSettingsPanelVisible.value) updateBrowserSettings(
            browserSettings.value.copy(
                isFirstAppLoad = false
            )
        )
    }
    LaunchedEffect(inspectingAppId.longValue) {
        descriptionContent.value = apps.find { it.id == inspectingAppId.longValue }?.label ?: ""

    }

    LaunchedEffect(apps.size) {
        appManager.saveApps(apps)
        if (apps.isEmpty()) {
            resetBottomPanelTrigger.value = !resetBottomPanelTrigger.value
        }
    }


    LaunchedEffect(
        bottomPanelPagerState.settledPage,
        bottomPanelPagerState.currentPage,
        isUrlOverlayBoxVisible
    ) {

        if (bottomPanelPagerState.currentPage == BottomPanelMode.SEARCH.ordinal) {
            isUrlOverlayBoxVisible = true
            if (isAppsPanelVisible.value) isAppsPanelVisible.value = false
            if (tabsPanelLock && !isFocusOnTextField) isTabsPanelVisible = true
            if (inspectingAppId.longValue != 0L) inspectingAppId.longValue = 0L
        } else {
            isFocusOnTextField = false
            isDownloadPanelVisible.value = false
            isNavPanelVisible = false
            isTabsPanelVisible = false
            setIsOptionsPanelVisible(false)
            isSettingsPanelVisible.value = false

            isFindInPageVisible.value = false
            keyboardController?.hide()
            if (bottomPanelPagerState.currentPage == BottomPanelMode.APPS.ordinal) {
                isAppsPanelVisible.value = true
            }

        }
        if (bottomPanelPagerState.settledPage != BottomPanelMode.SEARCH.ordinal) {
            focusManager.clearFocus()
        }

    }

    val insetsController = activity?.let {
        WindowCompat.getInsetsController(
            it.window,
            it.window.decorView
        )
    }

    LaunchedEffect(bottomPanelPagerState.currentPage) {
        if (bottomPanelPagerState.currentPage == BottomPanelMode.LOCK.ordinal) {
//            isBottomPanelLock.value = !isBottomPanelLock.value
            browserSettings.value =
                browserSettings.value.copy(isFullscreenMode = !browserSettings.value.isFullscreenMode)
        }
    }
    LaunchedEffect(resetBottomPanelTrigger.value) {
        if (bottomPanelPagerState.settledPage != BottomPanelMode.SEARCH.ordinal) {
            bottomPanelPagerState.animateScrollToPage(BottomPanelMode.SEARCH.ordinal)
        }
    }
    LaunchedEffect(bottomPanelPagerState.settledPage) {
        if (bottomPanelPagerState.settledPage == BottomPanelMode.LOCK.ordinal) {
            bottomPanelPagerState.animateScrollToPage(BottomPanelMode.SEARCH.ordinal)
        } else if (
            bottomPanelPagerState.settledPage == BottomPanelMode.APPS.ordinal
        ) {
            if (apps.isEmpty()) {
                bottomPanelPagerState.animateScrollToPage(BottomPanelMode.SEARCH.ordinal)

            }
        }
    }
    LaunchedEffect(screenSize) {
        if (screenSize.width > 0 && !isBackSquareInitialized) {
            val buttonSize = with(density) {
                browserSettings.value.heightForLayer(1).dp.toPx()
            }

            val defaultX =
                screenSize.width - buttonSize - with(density) { browserSettings.value.padding.dp.toPx() }
            val defaultY =
                screenSize.height - buttonSize - with(density) { browserSettings.value.padding.dp.toPx() }

            backSquareOffsetX.snapTo(defaultX)
            backSquareOffsetY.snapTo(defaultY)
        }
    }

    LaunchedEffect(textFieldState.text, isFocusOnTextField) {

        if (!browserSettings.value.showSuggestions || (textFieldState.text as String) == activeWebView?.url || textFieldState.text.isBlank() || isPinningApp.value) {
            suggestions.clear()
            return@LaunchedEffect
        }

        val query = (textFieldState.text as String).trim()

        if (query.isNotBlank() && isFocusOnTextField) {
//            delay(50L) // Debounce
            if (query != textFieldState.text.trim()) return@LaunchedEffect

            // --- COMBINED SUGGESTION LOGIC ---
            val finalSuggestions = mutableListOf<Suggestion>()
            val addedHistoryUrls = mutableSetOf<String>()

            // A. Process History (ranked by match type and recency)
            val historyMatches = visitedUrlMap.entries
                .filter { (url, title) ->
                    url.contains(query, ignoreCase = true) || title.contains(
                        query,
                        ignoreCase = true
                    )
                }
                .map { (url, title) ->
                    // Determine which part matched for ranking
                    val urlStartsWith = url.startsWith(query, ignoreCase = true)
                    val titleStartsWith = title.startsWith(query, ignoreCase = true)

                    // Create a rank: URL starts > Title starts > URL contains > Title contains
                    val rank = when {
                        urlStartsWith -> 1
                        titleStartsWith -> 2
                        url.contains(query, ignoreCase = true) -> 3
                        else -> 4
                    }
                    // The text displayed is the title, and the payload is the URL
                    Triple(
                        Suggestion(text = title, source = SuggestionSource.HISTORY, url = url),
                        rank,
                        url
                    )
                }
                .sortedBy { it.second } // Sort by the rank
                .map { it.first } // Get just the Suggestion object

//            // Rank "starts with" matches higher
//            val (startsWithHistory, containsHistory) = historyMatches.partition {
//                it.text.startsWith(query, ignoreCase = true)
//            }
//            finalSuggestions.addAll(startsWithHistory)
//            finalSuggestions.addAll(containsHistory)
//            addedHistoryTexts.addAll(historyMatches.map { it.text })

            finalSuggestions.addAll(historyMatches)
            addedHistoryUrls.addAll(historyMatches.map { it.url })

            // B. Fetch and process Google Suggestions
            try {
                withContext(Dispatchers.IO) {
                    val encodedQuery = URLEncoder.encode(query, "UTF-8")
//                    val url = currentSearchEngine.value.getSuggestionUrl(encodedQuery)
                    val url =
                        SearchEngine.entries[browserSettings.value.searchEngine].getSuggestionUrl(
                            encodedQuery
                        )
//                        "https://suggestqueries.google.com/complete/search?client=chrome&ie=UTF-8&oe=UTF-8&q=$encodedQuery"
//                        "https://duckduckgo.com/ac/?type=list&q=$encodedQuery"
//                        "https://api.bing.com/osjson.aspx?query=$encodedQuery"
                    val result = URL(url).readText(Charsets.UTF_8)

                    val jsonArray = JSONArray(result)
                    val suggestionsArray = jsonArray.getJSONArray(1)
                    val googleSuggestions =
                        List(suggestionsArray.length()) { suggestionsArray.getString(it) }

                    googleSuggestions.forEach { suggestionText ->
                        // Add Google suggestion only if it's not already in our history list
                        if (!addedHistoryUrls.contains(suggestionText)) {
//                            val searchUrl = "https://www.google.com/search?q=${
//                            val searchUrl = "https://duckduckgo.com/?q=${
//                            val searchUrl = "https://www.bing.com/search?q=${
//                            val searchUrl = "https://www.bing.com/search?q=${
//                                URLEncoder.encode(
//                                    suggestionText,
//                                    "UTF-8"
//                                )
//                            }"
                            val searchUrl =
                                SearchEngine.entries[browserSettings.value.searchEngine].getSearchUrl(
                                    URLEncoder.encode(
                                        suggestionText,
                                        "UTF-8"
                                    )
                                )
                            finalSuggestions.add(
                                Suggestion(
                                    text = suggestionText,
                                    source = SuggestionSource.GOOGLE,
                                    url = searchUrl
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e

            }

            // C. Update the UI state
            suggestions.clear()
            if (textFieldState.text.isNotEmpty() && isFocusOnTextField)
                suggestions.addAll(finalSuggestions.take(10)) // Limit to a reasonable number
        } else {
            suggestions.clear()
        }
    }
    LaunchedEffect(isFindInPageVisible.value) {
        if (!isFindInPageVisible.value) {
            findInPageText.value = ""
            findInPageResult.value = 0 to 0
        }
    }

    LaunchedEffect(isSettingsPanelVisible.value) {
        if (!isSettingsPanelVisible.value) {
            backgroundColor.value = Color.Black
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


            // Set up all the clients for the *current* active WebView.
            webViewManager.setWebViewClients(
                browserSettings = browserSettings.value,
                webView = webView,
                tab = activeTab, // Pass the active tab
                siteSettingsManager = siteSettingsManager,
                siteSettings = siteSettings,
                onFaviconChanged = { tabId, faviconUrl ->
                    // Find the index of the tab that fired this event.
                    val tabIndex = tabs.indexOfFirst { it.id == tabId }
                    if (tabIndex == -1) return@setWebViewClients

                    val targetTab = tabs[tabIndex]


                    // Check if an update is even needed to prevent unnecessary recompositions.
                    if (faviconUrl.isNotBlank()) {
                        tabs[tabIndex] = targetTab.copy(currentFaviconUrl = faviconUrl)
                        saveTrigger++
                    }
                },
                onJsAlert = { message -> jsDialogState = JsAlert(message) },
                onJsConfirm = { message, onResult -> jsDialogState = JsConfirm(message, onResult) },
                onJsPrompt = { message, default, onResult ->
                    jsDialogState = JsPrompt(message, default, onResult)
                },
                onPermissionRequest = { request -> pendingPermissionRequest = request },
                setCustomViewCallback = { callback -> customViewCallback = callback },
                setOriginalOrientation = { _ -> },
                resetCustomView = {
                    activity?.requestedOrientation = originalOrientation

                    customViewCallback?.onCustomViewHidden()
                    val insetsController = activity?.let {
                        WindowCompat.getInsetsController(
                            it.window,
                            it.window.decorView
                        )
                    }
                    insetsController?.hide(WindowInsetsCompat.Type.systemBars())
                    customViewCallback = null
                },
                onPageStartedFun = { _, url, _ ->
                    pendingPermissionRequest?.let { request ->
                        // Check if the new URL's host is DIFFERENT from the origin of the permission request.
                        val newHost = url?.toUri()?.host
                        val requestHost = request.origin.toUri().host

                        if (newHost != requestHost) {
                            // The user is navigating away, so clear the old permission request.
                            pendingPermissionRequest = null
                        }
                    }


                    isLoading = true


                },
                onPageFinishedFun = { view, currentUrlString ->
                    isLoading = false


//                    view.evaluateJavascript(JS_HOVER_SIMULATOR.trimIndent().replace("\n", ""), null)
                    val jsInjectCornerRadius = inject_corner_radius.replace(
                        "___corner-radius___",
                        browserSettings.value.deviceCornerRadius.toString()
                    )
                    view.evaluateJavascript(jsInjectCornerRadius, null)


                    if (currentUrlString != null) {
                        // Pass both the URL and the title to the manager
                        visitedUrlManager.addUrl(currentUrlString, view.title)
                        // Update our in-memory map
                        view.title?.let { title ->
                            if (title.isNotBlank()) {
                                visitedUrlMap[currentUrlString] = title
                            }
                        }
                    }

                },
                onDoUpdateVisitedHistoryFun = { view, url, _ ->
                    if (!isFocusOnTextField) view.url?.let {
                        textFieldState.setTextAndPlaceCursorAtEnd(it.toDomain())
                    }
                    if (url != null && activeTab.currentURL != url) {
                        tabs[activeTabIndex.intValue] =
                            tabs[activeTabIndex.intValue].copy(currentURL = url)
                    }
                },
                onTitleReceived = { view, url, title ->
                    val tabIndex = tabs.indexOfFirst { it.id == activeTab.id }
                    if (tabIndex == -1) return@setWebViewClients

                    val oldTab = tabs[tabIndex]
                    if (oldTab.currentTitle != title || oldTab.currentURL != url) {
                        // Create a new Tab instance and replace the old one
                        tabs[tabIndex] = oldTab.copy(currentTitle = title)
                        saveTrigger++
                    }


                    view.evaluateJavascript(favicon_discovery, null)


                    // Pass both the URL and the title to the manager
                    visitedUrlManager.addUrl(url, view.title)
                    // Update our in-memory map
                    view.title?.let { title ->
                        if (title.isNotBlank()) {
                            visitedUrlMap[url] = title
                        }
                    }
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

                    } catch (_: Exception) {
                        Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show()
                    }
                },
                onDownloadRequested = { url, userAgent, contentDisposition, mimeType, _ ->

                    if (!isUrlBarVisible) isUrlBarVisible = true
                    if (!isDownloadPanelVisible.value) isDownloadPanelVisible.value = true
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
                onFindResultReceived = { activeIndex, numberOfMatches, _ ->
                    // The listener gives 1-based index, we can use it directly for display
                    findInPageResult.value = (activeIndex + 1) to numberOfMatches
                },

                onContextMenu = { data ->
                    contextMenuData = data
                    displayContextMenuData = contextMenuData
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }

            )
            webView.onWebViewTouch = {
                if (isUrlBarVisible && !isBottomPanelLock.value) isUrlBarVisible = false
                if (contextMenuData != null) contextMenuData = null
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
            setIsOptionsPanelVisible(false)
            isTabDataPanelVisible = false
            isTabsPanelVisible = false
            isSettingsPanelVisible.value = false
//            if (downloads.isEmpty()) isDownloadPanelVisible = false
            isDownloadPanelVisible.value = false
            coroutineScope.launch {
                bottomPanelPagerState.animateScrollToPage(BottomPanelMode.SEARCH.ordinal)
            }
        } else {
            if (tabsPanelLock && bottomPanelPagerState.currentPage == BottomPanelMode.SEARCH.ordinal) isTabsPanelVisible =
                true
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
        confirmationState,
        contextMenuData
    ) {
        isBottomPanelVisible =
            isUrlBarVisible || isPermissionPanelVisible || isPromptPanelVisible || (confirmationState != null || (contextMenuData != null))
    }
    LaunchedEffect(isTabsPanelVisible) {
        if (!isTabsPanelVisible) {
            isTabDataPanelVisible = false
        }
    }


    suspend fun hideBackSquare(blinkEffect: Boolean = true) {
        val idle = browserSettings.value.backSquareIdleOpacity
        if (blinkEffect) {
            val peak = 1f

            squareAlpha.animateTo(peak)

            // b. Wait a moment so the user can see it before it blinks.
            delay(400)

            // c. Blink twice.
            repeat(2) {
                squareAlpha.animateTo(idle, animationSpec = tween(300))
                squareAlpha.animateTo(peak, animationSpec = tween(300))
            }

        } else {
            delay(200)
        }
        squareAlpha.animateTo(idle, animationSpec = tween(400))

    }


    LaunchedEffect(browserSettings.value.isFullscreenMode) {
        Log.d("FULLSCRFEEN", "isFullscreenMode: ${browserSettings.value.isFullscreenMode}")
//        Log.d("FULLSCRFEEN", "isSharpMode: ${browserSettings.value.isSharpMode}")
//        Log.d("FULLSCRFEEN", "topPadding: ${webViewTopPadding}")
//        Log.d("FULLSCRFEEN", "botPadding: ${webViewBottomPadding}")
        if (browserSettings.value.isFullscreenMode) {
            insetsController?.hide(WindowInsetsCompat.Type.systemBars())
            insetsController?.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
            insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }


    }

    LaunchedEffect(screenSize) {
        val squareBoxSize = browserSettings.value.heightForLayer(1).dp

        val squareBoxSizePx = with(density) { squareBoxSize.toPx() }
        val paddingPx = with(density) { browserSettings.value.padding.dp.toPx() }


        Log.d("FULLSCRFEEN", "currentY: ${backSquareOffsetY.value}")
        Log.d("FULLSCRFEEN", "maxy: ${screenSize.height.toFloat() - squareBoxSizePx - paddingPx}")


        if (screenSize.height.toFloat() > (squareBoxSizePx - paddingPx)
            && backSquareOffsetY.value > screenSize.height.toFloat() - squareBoxSizePx - paddingPx
        ) {
            // Auto Fit

            // Clamp Y to screen bounds
            val targetY = backSquareOffsetY.value.coerceIn(
                paddingPx,
                screenSize.height.toFloat() - squareBoxSizePx - paddingPx
            )
            coroutineScope.launch {

                launch {
                    backSquareOffsetY.animateTo(
                        targetY,
                        spring()
                    )
                }

                updateBrowserSettings(
                    browserSettings.value.copy(
                        backSquareOffsetY = targetY
                    )
                )
                hideBackSquare(false)
            }
        }

    }
    DisposableEffect(Unit) {
        val shakeDetector = ShakeDetector(context) {
            // This code runs when a shake is detected
            coroutineScope.launch {
                // Only trigger if the URL bar isn't already visible to avoid conflicts
                if (!isBottomPanelVisible) {
                    hideBackSquare()
                }
            }
        }

        shakeDetector.start()

        // Clean up when the Composable is destroyed (e.g. app closed)
        onDispose {
            shakeDetector.stop()
        }
    }
    LaunchedEffect(pendingPermissionRequest) {
        isPermissionPanelVisible = pendingPermissionRequest != null
    }
    // This effect will re-launch whenever isBottomPanelVisible changes.
    LaunchedEffect(isBottomPanelVisible) {
        if (!isBottomPanelVisible) {
            // -- The URL bar has just been hidden. Start the "show and blink" sequence. --

            // a. Instantly appear with 0.6 opacity.
            if (!isCursorMode) hideBackSquare()

            // d. After blinking, fade out completely.
        } else {
            // -- The URL bar is visible. Ensure the square is fully transparent. --
            hideBackSquare(false)
        }
    }
    LaunchedEffect(browserSettings.value.backSquareIdleOpacity) {
        if (!isBottomPanelVisible && !isCursorPadVisible) {
            squareAlpha.animateTo(browserSettings.value.backSquareIdleOpacity)
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


    // HIDE SYSTEM BARS
//    LaunchedEffect(Unit) {
//        val window = (context as? Activity)?.window ?: return@LaunchedEffect
//        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
//
//        // Hide the system bars permanently
//        insetsController.hide(WindowInsetsCompat.Type.systemBars())
//
//        // Configure the swipe-to-reveal behavior
//        insetsController.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//    }
    LaunchedEffect(saveTrigger) {
        if (saveTrigger > 0) {
            tabManager.saveTabs(tabs, activeTabIndex.intValue)
            saveTrigger = 0
        }
    }

    LaunchedEffect(Unit) {
        newUrlFlow.collect { url ->
            if (url != null) {

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

            val urlToLoad = browserSettings.value.defaultUrl
//            activeWebView?.loadUrl(urlToLoad)
            webViewLoad(activeWebView, urlToLoad, browserSettings.value)

        }
    }

    // The LaunchedEffect now saves the entire settings object (or individual fields)
    LaunchedEffect(browserSettings.value) {
        sharedPrefs.edit {
            putBoolean("is_first_app_load", browserSettings.value.isFirstAppLoad)
            putFloat("padding", browserSettings.value.padding)
            putFloat("device_corner_radius", browserSettings.value.deviceCornerRadius)
            putString("default_url", browserSettings.value.defaultUrl)
            putFloat("animation_speed", browserSettings.value.animationSpeed)
            putFloat("single_line_height", browserSettings.value.singleLineHeight)
//            putInt("desktop_mode_width", browserSettings.value.desktopModeWidth)
            putBoolean("is_sharp_mode", browserSettings.value.isSharpMode)
//            putFloat("top_sharp_edge", browserSettings.value.topSharpEdge)
//            putFloat("bottom_sharp_edge", browserSettings.value.bottomSharpEdge)
            putFloat("cursor_container_size", browserSettings.value.cursorContainerSize)
            putFloat("cursor_pointer_size", browserSettings.value.cursorPointerSize)
            putFloat("cursor_tracking_speed", browserSettings.value.cursorTrackingSpeed)
            putBoolean("show_suggestions", browserSettings.value.showSuggestions)
            putFloat("closed_tab_history_size", browserSettings.value.closedTabHistorySize)
            putFloat("back_square_offset_x", browserSettings.value.backSquareOffsetX)
            putFloat("back_square_offset_y", browserSettings.value.backSquareOffsetY)
            putFloat("back_square_idle_opacity", browserSettings.value.backSquareIdleOpacity)
            putFloat("max_list_height", browserSettings.value.maxListHeight)
            putInt("search_engine", browserSettings.value.searchEngine)
            putBoolean("is_fullscreen_mode", browserSettings.value.isFullscreenMode)
        }
    }

    LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }

    LaunchedEffect(isOnline) {
        activeWebView?.reload()
    }
    //endregion
    BackHandler(enabled = !isBottomPanelVisible || activeWebView?.canGoBack() ?: false) {
        when {
            // Priority 1: Exit fullscreen video if it's active.
            customView != null -> {
                customViewCallback?.onCustomViewHidden()

            }
            // Priority 2: Navigate back in the WebView.
            activeWebView?.canGoBack() ?: false -> {
                activeWebView.goBack()
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

        // No Internet Screen
        AnimatedVisibility(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.ime)
                .padding(webViewPaddingValue)

                .align(Alignment.BottomCenter),
            visible = !browserSettings.value.isFirstAppLoad && !isOnline,
            enter = slideInVertically(
                animationSpec = tween(browserSettings.value.animationSpeedForLayer(0) * 4),
                initialOffsetY = { it }
            ),
            exit = slideOutVertically(
                animationSpec = tween(browserSettings.value.animationSpeedForLayer(0) * 4),
                targetOffsetY = { -it }
            ),
        ) {
            NoInternetScreen(
                webViewTopPadding = webViewTopPadding,
                webViewBottomPadding = webViewBottomPadding,
                browserSettings = browserSettings.value,
            )
        }

        // Adjust Device Corner Radius Screen
        AnimatedVisibility(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.ime)
                .align(Alignment.BottomCenter),
            visible = browserSettings.value.isFirstAppLoad,
            enter = slideInVertically(
                animationSpec = tween(browserSettings.value.animationSpeedForLayer(0) * 4),
                initialOffsetY = { it }
            ),
            exit = slideOutVertically(
                animationSpec = tween(browserSettings.value.animationSpeedForLayer(0) * 4),
                targetOffsetY = { -it }
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(0).dp
                        )
                    )
                    .background(Color.White),
                contentAlignment = Alignment.Center

            ) {

                Text(
                    text = "adjust device corner radius",
                    color = Color.Black
                )

            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.ime)
                .align(Alignment.BottomCenter),
            visible = browserSettings.value.isFirstAppLoad,
            enter = fadeIn(tween(browserSettings.value.animationSpeedForLayer(0))),
            exit = fadeOut(animationSpec = tween(browserSettings.value.animationSpeedForLayer(0) * 4))
        ) {
            Box(
                modifier = Modifier
                    .padding(browserSettings.value.padding.dp)
                    .padding(webViewPaddingValue)
                    .clip(
                        RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(1).dp
                        )
                    )
                    .windowInsetsPadding(WindowInsets.ime)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)

                    .background(Color.Black)
                    .padding(bottom = browserSettings.value.padding.dp)
            ) {
                SettingsPanel(
                    descriptionContent = descriptionContent,
                    backgroundColor = backgroundColor,
                    isSettingsPanelVisible = isSettingsPanelVisible,
                    browserSettings = browserSettings,
                    updateBrowserSettings = updateBrowserSettings,
                    confirmationPopup = ::confirmationPopup,
                    resetBrowserSettings = resetBrowserSettings,
                    targetSetting = SettingPanelView.CORNER_RADIUS,
                    isSettingCornerRadius = isSettingCornerRadius
                )
            }
        }

        // WebView
        AnimatedVisibility(
            visible = !browserSettings.value.isFirstAppLoad && isOnline,
            enter = slideInVertically(
                animationSpec = tween(browserSettings.value.animationSpeedForLayer(0) * 4),
                initialOffsetY = { it }
            ),
            exit = slideOutVertically(
                animationSpec = tween(browserSettings.value.animationSpeedForLayer(0) * 4),
                targetOffsetY = { -it }
            )
        ) {

            Box(
                modifier = modifier
                    .fillMaxSize()

//                .padding(top = cutoutTop, bottom = cutoutBottom)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(webViewPaddingValue),


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


                            @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
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
                                        (activeWebView?.parent as? ViewGroup)?.removeView(
                                            activeWebView
                                        )
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
                        LoadingIndicator(isLoading = isLoading, browserSettings = browserSettings)
                    }
                }


                CursorPointer(
                    isCursorPadVisible = isCursorPadVisible,
                    position = cursorPointerPosition.value,
                    browserSettings = browserSettings,
                )



                BottomPanel(
                    isSettingCornerRadius = isSettingCornerRadius,
                    floatingPanelBottomPadding = floatingPanelBottomPadding,
                    optionsPanelHeightPx = optionsPanelHeightPx,
                    draggableState = draggableState,
                    flingBehavior = flingBehavior,
                    isPinningApp = isPinningApp,

                    initialSettingPanelView = initialSettingPanelView,
                    appManager = appManager,
                    inspectingAppId = inspectingAppId,
                    resetBottomPanelTrigger = resetBottomPanelTrigger,
                    setIsBottomPanelVisible = { isBottomPanelVisible = it },
                    setIsUrlBarVisible = { isUrlBarVisible = it },
                    isAppsPanelVisible = isAppsPanelVisible,
                    apps = apps,

                    isBottomPanelLock = isBottomPanelLock,
                    bottomPanelPagerState = bottomPanelPagerState,
                    onOpenInNewTab = { url ->
//                   createNewTab(tabs.size, url)
                        createNewTab(activeTabIndex.intValue + 1, url)

                    },
                    onDownloadImage = { url ->
                        // Simple generic download for images found via context menu
                        startDownload(
                            url,
                            activeWebView?.settings?.userAgentString ?: "",
                            null,
                            "image/*"
                        )
                    },
                    contextMenuData = contextMenuData,
                    displayContextMenuData = displayContextMenuData,
                    onDismissContextMenu = { contextMenuData = null },
                    isFocusOnTextField = isFocusOnTextField,
                    textFieldState = textFieldState,
                    onCloseAllTabs = {
                        confirmationPopup(
                            message = "close all tabs and exit ? ",
                            onConfirm = {
                                closeAllTabs()
                            },
                            onCancel = {}
                        )
                    },
                    suggestions = suggestions,
                    onSuggestionClick = { suggestion ->
                        webViewLoad(activeWebView, suggestion.url, browserSettings.value)
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                    onRemoveSuggestion = { suggestionToRemove ->
                        confirmationPopup(
                            message = "remove suggestion from history ? ",
                            onConfirm = {
                                removeSuggestionFromHistory(suggestionToRemove)
                            },
                            onCancel = {}
                        )
                    },

                    webViewManager = webViewManager,
                    activeWebView = activeWebView,

                    findInPageResult = findInPageResult,
                    findInPageText = findInPageText,
//                    onAddToHomeScreen = {
//                        addToHomeScreen(
//                            context = context,
//                            coroutineScope = coroutineScope,
//                            tab = currentInspectingTab,
//                            activeWebView = activeWebView,
//                        )
//                    }
//                    ,
                    descriptionContent = descriptionContent,
                    recentlyClosedTabs = recentlyClosedTabs,
                    reopenClosedTab = reopenClosedTab,
                    confirmationPopup = ::confirmationPopup,
                    resetBrowserSettings = resetBrowserSettings,
                    backgroundColor = backgroundColor,
                    isSettingsPanelVisible = isSettingsPanelVisible,
                    setIsSettingsPanelVisible = { isSettingsPanelVisible.value = it },
                    urlBarFocusRequester = urlBarFocusRequester,
                    isCursorPadVisible = isCursorPadVisible,
                    isCursorMode = isCursorMode,
                    setIsCursorMode = {
                        isCursorMode = it

                    },
                    setIsTabsPanelVisible = { isTabsPanelVisible = it },
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
                        if (tab.id != 0L) inspectingTabId = tab.id else {
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

                            // save old tab state
                            val oldTab = tabs.getOrNull(activeTabIndex.intValue)
                            // We use the webViewManager's pool to find the correct WebView instance
                            val oldWebView = oldTab?.let { webViewManager.getWebView(it) }

                            if (oldWebView != null) {
                                val outState = Bundle()
                                oldWebView.saveState(outState)
                                val stateBytes = outState.getByteArray("WEBVIEW_CHROMIUM_STATE")
                                if (stateBytes != null) {
                                    oldTab.savedState =
                                        Base64.encodeToString(stateBytes, Base64.DEFAULT)
                                }
                            }




                            tabs[activeTabIndex.intValue].state = TabState.BACKGROUND
                            tabs[newIndex].state = TabState.ACTIVE

                            activeTabIndex.intValue = newIndex

                            val urlToLoad = webViewManager.getWebView(tabs[newIndex]).url
                                ?: browserSettings.value.defaultUrl
//                        activeWebView?.loadUrl(urlToLoad)
                            textFieldState.setTextAndPlaceCursorAtEnd(urlToLoad.toDomain())
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
                    browserSettings = browserSettings,
                    updateBrowserSettings = updateBrowserSettings,
//                        url = url,
                    focusManager = focusManager,
                    keyboardController = keyboardController,
                    setIsOptionsPanelVisible = setIsOptionsPanelVisible,
                    onNewUrl = { newUrl ->
                        webViewLoad(activeWebView, newUrl, browserSettings.value)

                    },
                    setIsFocusOnTextField = { isFocusOnTextField = it },
                    handleHistoryNavigation = handleHistoryNavigation,
                    isFindInPageVisible = isFindInPageVisible,

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
                    webViewPaddingValue = webViewPaddingValue,
                    setIsUrlBarVisible = { isUrlBarVisible = it },
                    isLongPressDrag = isLongPressDrag,
                    cursorPadHeight = cursorPadHeight,

                    )

                LaunchedEffect(screenSize) {

                }
                // BackSquare
                AnimatedVisibility(
                    visible = !isBottomPanelVisible,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(webViewPaddingValue)
                        .onSizeChanged {
                            screenSize = it
                            with(density) {
                                @Suppress("AssignedValueIsNeverRead")
                                screenSizeDp = IntSize(
                                    it.width.toDp().value.roundToInt(),
                                    it.height.toDp().value.roundToInt()
                                )
                            }
                        },
                    enter = fadeIn(tween(browserSettings.value.animationSpeedForLayer(0))),
                    exit = fadeOut(tween(browserSettings.value.animationSpeedForLayer(0)))
                ) {


                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                    ) {


                        val squareBoxSize = browserSettings.value.heightForLayer(1).dp

                        val squareBoxSizePx = with(density) { squareBoxSize.toPx() }
                        val paddingPx = with(density) { browserSettings.value.padding.dp.toPx() }

                        Box(
                            modifier = Modifier
//                            .align(squareAlignment)
                                .offset {
                                    IntOffset(
                                        backSquareOffsetX.value.roundToInt(),
                                        backSquareOffsetY.value.roundToInt()
                                    )
                                }

                                .animateContentSize(
                                    tween(
                                        browserSettings.value.animationSpeedForLayer(1)
                                    )
                                )
                                .size(squareBoxSize)
//                            .height(squareBoxSmallHeight)
//                            .width(screenSizeDp.width.dp * 0.45f)
                                .graphicsLayer {
                                    alpha = squareAlpha.value
                                }
                                .pointerInput(Unit, isCursorMode) {

                                    if (!isCursorMode) awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        coroutineScope.launch {
                                            squareAlpha.animateTo(1f)
                                        }

                                        val longPressJob = coroutineScope.launch {
                                            delay(viewConfiguration.longPressTimeoutMillis)
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            isCursorPadVisible = true
                                            squareAlpha.snapTo(0f)

//
//                                        val initialCursorX =
//                                            if (squareAlignment == Alignment.BottomEnd) (
//                                                    screenSize.width - ((screenSize.width * 0.45f) + browserSettings.value.padding.dp.toPx()) + down.position.x
//                                                    )
//                                            else browserSettings.value.padding.dp.toPx() + down.position.x


                                            val initialCursorX =
                                                backSquareOffsetX.value + browserSettings.value.padding + down.position.x

//                                        val initialCursorY = screenSize.height - (squareBoxSmallHeight.toPx() - browserSettings.value.padding.dp.toPx()) + down.position.y - ((screenSize.height - cutoutTop.toPx()) / 2)
                                            val initialCursorY =
                                                ((screenSize.height - cutoutTop.toPx()) / 2) - (screenSize.height - backSquareOffsetY.value) + down.position.y + cutoutTop.toPx()


                                            cursorPointerPosition.value =
                                                Offset(initialCursorX, initialCursorY)


                                        }

                                        val drag =
                                            awaitTouchSlopOrCancellation(down.id) { change, _ ->
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
                                                        (change.position.x - change.previousPosition.x) * browserSettings.value.cursorTrackingSpeed
                                                    val changeSpaceY =
                                                        (change.position.y - change.previousPosition.y) * browserSettings.value.cursorTrackingSpeed

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
                                                val downTime = System.currentTimeMillis()
                                                val downEvent = MotionEvent.obtain(
                                                    downTime,
                                                    downTime,
                                                    MotionEvent.ACTION_DOWN,
                                                    cursorPointerPosition.value.x,
//                                                    cursorPointerPosition.value.y - webViewTopPadding.toPx(),
                                                    cursorPointerPosition.value.y - innerPadding.calculateTopPadding()
                                                        .toPx(),
                                                    0
                                                )
                                                val upEvent = MotionEvent.obtain(
                                                    downTime,
                                                    downTime + 10,
                                                    MotionEvent.ACTION_UP,
                                                    cursorPointerPosition.value.x,
                                                    cursorPointerPosition.value.y - innerPadding.calculateTopPadding()
                                                        .toPx(),
                                                    0
                                                )
                                                webView.dispatchTouchEvent(downEvent)
                                                webView.dispatchTouchEvent(upEvent)
                                            }

                                            coroutineScope.launch {
                                                squareAlpha.animateTo(browserSettings.value.backSquareIdleOpacity)
                                            }
                                        } else {
                                            // --- TAP OR SHORT-DRAG PATH ---
                                            if (drag != null) {
                                                // SHORT-DRAG
                                                drag(drag.id) { change ->
                                                    change.consume()
                                                    val newX =
                                                        backSquareOffsetX.value + change.position.x - change.previousPosition.x
                                                    val newY =
                                                        backSquareOffsetY.value + change.position.y - change.previousPosition.y

                                                    coroutineScope.launch {
                                                        backSquareOffsetX.snapTo(newX)
                                                        backSquareOffsetY.snapTo(newY)
                                                    }


//                                                horizontalDragAccumulator += change.position.x - change.previousPosition.x
//                                                verticalDragAccumulator += change.position.y - change.previousPosition.y
//
//                                                if (abs(horizontalDragAccumulator) > abs(
//                                                        verticalDragAccumulator
//                                                    )
//                                                ) {
//                                                    squareAlignment =
//                                                        if (horizontalDragAccumulator < 0) Alignment.BottomStart else Alignment.BottomEnd
//                                                } else {
//                                                    if (!isUrlBarVisible) {
//                                                        isUrlBarVisible = true
//                                                    }
//                                                }
                                                }

                                                // --- SNAP LOGIC (Release) ---
                                                val screenWidth = screenSize.width.toFloat()
                                                val currentX = backSquareOffsetX.value

                                                // Determine snap target (Left or Right edge)
                                                val targetX =
                                                    if (currentX + (squareBoxSizePx / 2) < screenWidth / 2) {
                                                        paddingPx // Snap Left
                                                    } else {
                                                        screenWidth - squareBoxSizePx - paddingPx // Snap Right
                                                    }

                                                // Clamp Y to screen bounds
                                                val targetY = backSquareOffsetY.value.coerceIn(
                                                    paddingPx,
                                                    screenSize.height.toFloat() - squareBoxSizePx - paddingPx
                                                )

                                                coroutineScope.launch {
                                                    // Animate snapping
                                                    launch {
                                                        backSquareOffsetX.animateTo(
                                                            targetX,
                                                            spring()
                                                        )
                                                    }
                                                    launch {
                                                        backSquareOffsetY.animateTo(
                                                            targetY,
                                                            spring()
                                                        )
                                                    }

                                                    updateBrowserSettings(
                                                        browserSettings.value.copy(
                                                            backSquareOffsetX = targetX,
                                                            backSquareOffsetY = targetY
                                                        )
                                                    )
                                                    // Fade out after snap
                                                    hideBackSquare(false)
                                                }
                                            } else {
                                                // TAP
                                                if (longPressJob.isActive) {
                                                    longPressJob.cancel()
                                                    coroutineScope.launch {
                                                        if (!isUrlBarVisible) isUrlBarVisible = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        coroutineScope.launch {
                                            squareAlpha.animateTo(1f)
                                        }

                                        val drag =
                                            awaitTouchSlopOrCancellation(down.id) { change, _ ->

                                                change.consume()
                                            }

                                        if (drag != null) {
                                            // SHORT-DRAG
                                            drag(drag.id) { change ->
                                                change.consume()
//                                            horizontalDragAccumulator += change.position.x - change.previousPosition.x
//                                            verticalDragAccumulator += change.position.y - change.previousPosition.y
//
//                                            if (abs(horizontalDragAccumulator) > abs(
//                                                    verticalDragAccumulator
//                                                )
//                                            ) {
//                                                squareAlignment =
//                                                    if (horizontalDragAccumulator < 0) Alignment.BottomStart else Alignment.BottomEnd
//                                            } else {
//                                                if (!isUrlBarVisible) {
//                                                    isUrlBarVisible = true
//                                                }
//                                            }
                                            }
                                        } else {
                                            // TAP
                                            coroutineScope.launch {
                                                if (!isUrlBarVisible) isUrlBarVisible = true
                                            }
                                        }
                                    }
                                }

                                .clip(
                                    RoundedCornerShape(
                                        browserSettings.value.cornerRadiusForLayer(1).dp
                                    )
                                )
                                .background(Color.Black.copy(alpha = 0.5f))
                                .background(Color.White.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {

//                        Icon(
//                            painter = painterResource(R.drawable.ic_circle),
//                            contentDescription = "Back",
//                            tint = Color.White
//                        )
                        }
                    }
                }


            }
        }
    }


    // This appears on top of everything when customView is not null.
    if (customView != null) {
        @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
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

@Composable
fun LoadingIndicator(
    isLoading: Boolean,
    browserSettings: MutableState<BrowserSettings>,
    modifier: Modifier = Modifier
) {
    // Animate the appearance and disappearance of the overlay.
    AnimatedVisibility(
        visible = isLoading,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .padding(browserSettings.value.padding.dp)
                .clip(
                    RoundedCornerShape(
                        browserSettings.value.cornerRadiusForLayer(1).dp
                    )
                )
                .size(browserSettings.value.heightForLayer(1).dp)
                .background(
                    Color.Black.copy(alpha = 0.5f),
                )


        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(browserSettings.value.padding.dp)
                    .size(browserSettings.value.heightForLayer(1).dp),
                // Use a contrasting color that works well on the dark scrim.
                color = Color.White,
                strokeWidth = browserSettings.value.padding.dp
            )
        }
    }
}


@Composable
fun CursorPointer(
    isCursorPadVisible: Boolean,
    position: Offset,
    browserSettings: MutableState<BrowserSettings>
) {
    AnimatedVisibility(
        visible = isCursorPadVisible,
        enter = fadeIn(tween(browserSettings.value.animationSpeed.roundToInt())),
        exit = fadeOut(tween(browserSettings.value.animationSpeed.roundToInt())),
        modifier = Modifier
    ) {
        val cursorContainerSize = browserSettings.value.cursorContainerSize.dp
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
//                .border(2.dp, Color.White, CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_dot), // Ensure you have this drawable
                contentDescription = "Quick Cursor",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(browserSettings.value.cursorPointerSize.dp)
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
    browserSettings: MutableState<BrowserSettings>,
    screenSize: IntSize,
    coroutineScope: CoroutineScope,
    activeWebView: CustomWebView?,
    cursorPointerPosition: MutableState<Offset>,
    webViewPaddingValue: PaddingValues,
    setIsUrlBarVisible: (Boolean) -> Unit,
    cursorPadHeight: Dp,


    ) {
    AnimatedVisibility(
        modifier = Modifier
            .fillMaxSize(),
        visible = isCursorPadVisible,
        enter = slideInVertically(
            initialOffsetY = { it }, // Start from the bottom
            animationSpec = tween(durationMillis = browserSettings.value.animationSpeed.roundToInt())
        ) + fadeIn(tween(browserSettings.value.animationSpeed.roundToInt())),
        exit = fadeOut(tween(browserSettings.value.animationSpeed.roundToInt()))
    ) {

        val hapticFeedback = LocalHapticFeedback.current

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(webViewPaddingValue)
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


                                activeWebView?.let { webView ->
//                                    longPressDownTime = System.currentTimeMillis()
                                    val longPressDownEvent = MotionEvent.obtain(
                                        longPressDownTime,
                                        longPressDownTime,
                                        MotionEvent.ACTION_DOWN,
                                        cursorPointerPosition.value.x,
                                        cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding()
                                            .toPx(),
                                        0
                                    )
                                    webView.dispatchTouchEvent(longPressDownEvent)

                                }
                                if (drag != null) {

                                    drag(drag.id) { change ->
                                        change.consume()

                                        isLongPressDrag.value = true
                                        // Check for multiple fingers DURING the drag
                                        val event = currentEvent // Get the current pointer event


                                        when (event.changes.size) {
                                            1 -> {
                                                // This is a single-finger drag, move the cursor
                                                val changeDelta =
                                                    change.position - change.previousPosition
                                                val changeSpaceX =
                                                    changeDelta.x * browserSettings.value.cursorTrackingSpeed
                                                val changeSpaceY =
                                                    changeDelta.y * browserSettings.value.cursorTrackingSpeed


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
                                                        cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding()
                                                            .toPx(),
                                                        0
                                                    )
                                                    webView.dispatchTouchEvent(moveEvent)

                                                }

//                                            cursorPointerPosition.value += Offset(
//                                                changeSpaceX,
//                                                changeSpaceY
//                                            )
                                            }

//                                            2 -> {
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
                                        cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding()
                                            .toPx(),
                                        0
                                    )
                                    webView.dispatchTouchEvent(upEvent)

                                }

                            } else {

                                if (drag != null) {
                                    // This is the high-level function that consumes the rest of the drag gesture.
                                    // It will finish when the user lifts their finger.
                                    drag(drag.id) { change ->
                                        change.consume()

                                        // Check for multiple fingers DURING the drag
                                        val event = currentEvent // Get the current pointer event


                                        when (event.changes.size) {
                                            1 -> {
                                                // This is a single-finger drag, move the cursor
                                                val changeDelta =
                                                    change.position - change.previousPosition
                                                val changeSpaceX =
                                                    changeDelta.x * browserSettings.value.cursorTrackingSpeed
                                                val changeSpaceY =
                                                    changeDelta.y * browserSettings.value.cursorTrackingSpeed


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

                                                val changeDelta =
                                                    change.position - change.previousPosition
                                                var changeSpaceY = changeDelta.y


                                                if (activeWebView != null) {

                                                    if (!activeWebView.canScrollVertically(1) && changeSpaceY < 0) changeSpaceY =
                                                        0f
                                                    if (!activeWebView.canScrollVertically(-1) && changeSpaceY > 0) changeSpaceY =
                                                        0f

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

//                                 Work but cannot click under the cursor pad
                                    // -> use for 2 finger capture?
//                                            CursorAccessibilityService.instance?.performClick(
//                                                cursorPointerPosition.value.x,
//                                                cursorPointerPosition.value.y
//                                            )

                                    activeWebView?.let { webView ->
                                        val downTime = System.currentTimeMillis()
                                        val downEvent = MotionEvent.obtain(
                                            downTime,
                                            downTime,
                                            MotionEvent.ACTION_DOWN,
                                            cursorPointerPosition.value.x,
                                            cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding()
                                                .toPx(),
                                            0
                                        )
                                        val upEvent = MotionEvent.obtain(
                                            downTime,
                                            downTime + 10,
                                            MotionEvent.ACTION_UP,
                                            cursorPointerPosition.value.x,
                                            cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding()
                                                .toPx(),
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
                        end = browserSettings.value.padding.dp,
                        start = browserSettings.value.padding.dp, // Add start padding for when it's on the left
                        bottom = browserSettings.value.padding.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(1).dp
                        )
                    )
                    .background(Color.Black.copy(alpha = 0.4f))
//                    .border(
//                        2.dp,
//                        Color.White.copy(alpha = 0.5f),
//                        shape = RoundedCornerShape(
//                            cornerRadiusForLayer(
//                                1,
//                                browserSettings.value.deviceCornerRadius,
//                                browserSettings.value.padding
//                            ).dp
//                        )
//                    )
                ,
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


class ShakeDetector(context: Context, private val onShake: () -> Unit) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Threshold: acceleration > 2.7g (Earth gravity is 1.0g)
    // Adjust this: Lower = more sensitive, Higher = harder shake required
    private val shakeThresholdGravity = 2.7F
    private val shakeSlopTimeMs = 500 // Minimum time between shakes
    private var shakeTimestamp: Long = 0

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            // Calculate gForce (vector magnitude)
            val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

            if (gForce > shakeThresholdGravity) {
                val now = System.currentTimeMillis()
                // Ignore shakes that happen too close together
                if (shakeTimestamp + shakeSlopTimeMs > now) {
                    return
                }

                shakeTimestamp = now
                onShake()
            }
        }
    }
}

//endregion
