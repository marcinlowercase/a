package marcinlowercase.a

import CursorPointer
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marcinlowercase.a.core.constant.generic_location_permission
import marcinlowercase.a.core.data_class.ConfirmationDialogState
import marcinlowercase.a.core.data_class.CustomPermissionRequest
import marcinlowercase.a.core.data_class.DownloadItem
import marcinlowercase.a.core.data_class.DownloadParams
import marcinlowercase.a.core.data_class.ErrorState
import marcinlowercase.a.core.data_class.JsAlert
import marcinlowercase.a.core.data_class.JsConfirm
import marcinlowercase.a.core.data_class.JsPrompt
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.ActivePanel
import marcinlowercase.a.core.enum_class.BottomPanelMode
import marcinlowercase.a.core.enum_class.DownloadStatus
import marcinlowercase.a.core.enum_class.GestureNavAction
import marcinlowercase.a.core.enum_class.MediaControlOption
import marcinlowercase.a.core.enum_class.RevealState
import marcinlowercase.a.core.function.createNotificationChannel
import marcinlowercase.a.core.function.rememberAnchoredDraggableState
import marcinlowercase.a.core.function.toDomain
import marcinlowercase.a.core.function.webViewLoad
import marcinlowercase.a.core.manager.MediaGestureManager
import marcinlowercase.a.ui.component.CursorPad
import marcinlowercase.a.ui.component.LoadingIndicator
import marcinlowercase.a.ui.panel.BottomPanel
import marcinlowercase.a.ui.panel.ChoicePanel
import marcinlowercase.a.ui.panel.ColorPickerPanel
import marcinlowercase.a.ui.panel.DateTimePickerPanel
import marcinlowercase.a.ui.panel.MediaControlPanel
import marcinlowercase.a.ui.panel.SettingPanelView
import marcinlowercase.a.ui.panel.SettingsPanel
import marcinlowercase.a.ui.panel.VideoStatusPanel
import marcinlowercase.a.ui.screen.ErrorScreen
import marcinlowercase.a.ui.theme.Theme
import marcinlowercase.a.ui.viewmodel.BrowserViewModel
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import org.mozilla.gecko.util.ThreadUtils.runOnUiThread
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.StorageController
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.system.exitProcess


//region Composable

class MainActivity : ComponentActivity() {

    val newUrlFromIntent = MutableStateFlow<String?>(null)

    private var pendingFilePrompt: GeckoSession.PromptDelegate.FilePrompt? = null
    private var pendingFileResult: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? = null

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val prompt = pendingFilePrompt
        val geckoResult = pendingFileResult

        if (result.resultCode == RESULT_OK && prompt != null && geckoResult != null) {
            val data = result.data
            val uri = data?.data
            val clipData = data?.clipData

            // We use a Coroutine to handle file copying so we don't block the UI thread
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = when {
                        clipData != null -> {
                            val uris = mutableListOf<Uri>()
                            for (i in 0 until clipData.itemCount) {
                                // Copy each file to cache
                                val localUri = copyFileToCache(clipData.getItemAt(i).uri)
                                if (localUri != null) uris.add(localUri)
                            }
                            prompt.confirm(this@MainActivity, uris.toTypedArray())
                        }

                        uri != null -> {
                            // Copy single file to cache
                            val localUri = copyFileToCache(uri)
                            if (localUri != null) {
                                prompt.confirm(this@MainActivity, localUri)
                            } else {
                                prompt.dismiss()
                            }
                        }

                        else -> prompt.dismiss()
                    }

                    withContext(Dispatchers.Main) {
                        geckoResult.complete(response)
                    }
                } catch (e: Exception) {
                    Log.e("FilePicker", "Error processing file", e)
                    withContext(Dispatchers.Main) {
                        geckoResult.complete(prompt.dismiss())
                    }
                }
            }
        } else {
            geckoResult?.complete(prompt?.dismiss())
        }
        pendingFilePrompt = null
        pendingFileResult = null
    }

    /**
     * Copies a content:// URI to a local file in the app cache and returns a file:// URI.
     * This is required because GeckoView often fails to resolve modern Android content URIs.
     */
    private fun copyFileToCache(contentUri: Uri): Uri? {
        return try {
            val fileName = getFileName(contentUri)
            val tempFile = File(cacheDir, fileName)

            contentResolver.openInputStream(contentUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            Log.e("FilePicker", "Failed to cache file", e)
            null
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "upload_${System.currentTimeMillis()}"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    fun openFilePicker(
        prompt: GeckoSession.PromptDelegate.FilePrompt,
        result: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>
    ) {
        pendingFilePrompt = prompt
        pendingFileResult = result

        // Use GET_CONTENT for widest compatibility with standard web uploads
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = if (!prompt.mimeTypes.isNullOrEmpty()) prompt.mimeTypes!![0] else "*/*"

            if (prompt.mimeTypes != null && prompt.mimeTypes!!.size > 1) {
                putExtra(Intent.EXTRA_MIME_TYPES, prompt.mimeTypes)
            }
            if (prompt.type == GeckoSession.PromptDelegate.FilePrompt.Type.MULTIPLE) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"))
    }

    @SuppressLint(
        "SetJavaScriptEnabled",
//        configure orientation manually
        "SourceLockedOrientationActivity"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            cacheDir.listFiles()?.forEach { it.delete() }
        }
        GeckoRuntime.getDefault(applicationContext)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        createNotificationChannel(this)

        setContent {
            Theme {

                val viewModel: BrowserViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BrowserScreen(
                        innerPadding = innerPadding,
                        modifier = Modifier,
                        newUrlFlow = newUrlFromIntent,
                        initialIntentUrl = intent?.dataString,
                        viewModel = viewModel
                    )
                }
            }
        }

    }

    override fun onStop() {
        super.onStop()
        val viewModel: BrowserViewModel by viewModels()
        Log.e("marcPip", "onStop")
        if (isInPictureInPictureMode
            || isPipMode || isEnteringPip
        ) return
        viewModel.tabManager.freezeAllTabs()
    }


    //region Intent
    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.dataString?.let { urlFromIntent ->
                // Instead of creating the tab here, we just emit the URL.
                // The Composable will react to this emission.
                newUrlFromIntent.update { urlFromIntent }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    //endregion

    //region Pip
    // currently not working for YouTube yet, other platform work fine
    fun updatePipParams(isDataFullscreen: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            isEnteringPip = true
            val params = android.app.PictureInPictureParams.Builder()
                // If video is fullscreen, allow Auto-Enter (Swipe up to PiP)
                .setAutoEnterEnabled(isDataFullscreen)
                .setAspectRatio(android.util.Rational(16, 9)) // Default to 16:9
                .build()
            setPictureInPictureParams(params)
        }
    }

    var isCurrentlyFullscreen by mutableStateOf(false)
    var isEnteringPip by mutableStateOf(false)

    override fun onUserLeaveHint() {
        Log.i("marcPip", "onUserLeaveHint")
        if (isCurrentlyFullscreen) {
            Log.i("marcPip", "isCurrentlyFullscreen $isCurrentlyFullscreen")

            isEnteringPip = true


            // Only call enterPip() manually on older Androids.
            // Android 12+ handles it automatically via updatePipParams/setAutoEnterEnabled.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                enterPip()
            }
        }
        super.onUserLeaveHint()
    }

    private fun enterPip() {
        Log.i("marcPip", "enterPip")

        // Use 16:9 as a standard fallback since the delegate is missing
        val params = android.app.PictureInPictureParams.Builder()
            .setAspectRatio(android.util.Rational(16, 9))
            .build()
        enterPictureInPictureMode(params)
    }

    var isPipMode by mutableStateOf(false)

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        Log.i("marcPip", "onPictureInPictureModeChanged")

        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipMode = isInPictureInPictureMode
        Log.i("marcPip", "isInPictureInPictureMode $isInPictureInPictureMode")
        if (isInPictureInPictureMode) {
            // Ensure orientation is correct for the small window
            isEnteringPip = false
        } else {
            isEnteringPip = false

            // If we are still conceptually in fullscreen video, restore landscape
            if (isCurrentlyFullscreen) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
    }


    override fun onPause() {
        super.onPause()
        if (isCurrentlyFullscreen) isEnteringPip = true
        val viewModel: BrowserViewModel by viewModels()

        Log.e("marcPip", "onPause")
        Log.d("marcPip", "isInPictureInPictureMode: $isInPictureInPictureMode")
        Log.d("marcPip", "isEnteringPip: $isEnteringPip")
        Log.d("marcPip", "isCurrentlyFullscreen: $isCurrentlyFullscreen")
        // If entering PiP, we MUST keep the session active and prevent Gecko from
        // interpreting this as a background event that stops media.
        if (isInPictureInPictureMode || isEnteringPip) {
            viewModel.tabManager.loadTabs(null)
            val index = viewModel.tabManager.getActiveTabIndex()
            if (viewModel.tabs.isNotEmpty() && index in viewModel.tabs.indices) {
                val activeTab = viewModel.tabs[index]
                // Force the session to remain active
                viewModel.geckoManager.getSession(activeTab).setActive(true)
                Log.e("marcPip", "set session to active")

            }
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        // Force Landscape orientation report to Gecko if we are Fullscreen + PiP
        // This prevents Gecko from exiting fullscreen due to "Portrait" home screen signals
        if (isCurrentlyFullscreen && (isEnteringPip || isInPictureInPictureMode)) {
            newConfig.orientation = android.content.res.Configuration.ORIENTATION_LANDSCAPE
        }
        super.onConfigurationChanged(newConfig)
    }
    //endregion
}

@SuppressLint("SourceLockedOrientationActivity", "ClickableViewAccessibility")
@Composable
fun BrowserScreen(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    newUrlFlow: StateFlow<String?>,
    initialIntentUrl: String? = null,
    viewModel: BrowserViewModel = viewModel()
) {

    //region Variables
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val mainActivity = context as MainActivity

    val isPipMode = mainActivity.isPipMode
    var saveTrigger by remember { mutableIntStateOf(0) }

    val imeInsets = WindowInsets.ime.asPaddingValues()
    val keyboardHeight = imeInsets.calculateBottomPadding()
    val isKeyboardVisible = keyboardHeight > 0.dp

    //region Load from ViewModel

    val settingsState = viewModel.browserSettings.collectAsState()
    val settings = settingsState.value
    val uiState by viewModel.uiState.collectAsState()

    viewModel.initializeTabs(initialIntentUrl)
    val activeTabIndex by viewModel.activeTabIndex.collectAsState()

    //endregion
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current

    
    val textFieldState = rememberTextFieldState(viewModel.activeTab!!.currentURL)
    val activeSession = remember(viewModel.activeTab!!.id, viewModel.sessionRefreshTrigger.intValue) {
        viewModel.geckoManager.getSession(viewModel.activeTab!!)
    }



    val offsetY = remember { Animatable(0f) }
    val animatedCornerRadius by animateDpAsState(
        targetValue = if (settings.isSharpMode

        ) 0.dp else settings.deviceCornerRadius.dp,
        label = "Corner Radius Animation",
    )


    //region Container Shape
    // Device Cutout
    // get the raw cutout padding values
    val cutoutPaddingValues = WindowInsets.displayCutout.asPaddingValues()
    val cutoutTop = cutoutPaddingValues.calculateTopPadding()
    val cutoutBottom = cutoutPaddingValues.calculateBottomPadding()
    val cutoutLeft = cutoutPaddingValues.calculateLeftPadding(LocalLayoutDirection.current)
    val cutoutRight = cutoutPaddingValues.calculateRightPadding(LocalLayoutDirection.current)

    val paddingAnimationSpec = if (isPipMode) {
        snap()
    } else {
        spring(visibilityThreshold = Dp.VisibilityThreshold)
    }
    // Top Padding

    val webViewTopPaddingFullscreen = if (settings.isSharpMode && !uiState.isLandscape) {
        maxOf(cutoutTop, settings.deviceCornerRadius.dp)
    } else {
        cutoutTop
    }

    val webViewTopPaddingRegular = if (settings.isSharpMode) {
        maxOf(
            maxOf(cutoutTop, settings.deviceCornerRadius.dp),
            innerPadding.calculateTopPadding()
        )
    } else {
        innerPadding.calculateTopPadding()
    }

    val webViewTopPaddingNormalScreen = if (settings.isFullscreenMode) {
        webViewTopPaddingFullscreen
    } else {
        webViewTopPaddingRegular
    }

    val targetWebViewTopPadding =
        if (uiState.isSettingCornerRadius
            || isPipMode
        ) 0.dp else webViewTopPaddingNormalScreen

    val webViewTopPadding by animateDpAsState(
        targetValue = targetWebViewTopPadding,
        animationSpec = paddingAnimationSpec,
        label = "WebView Top Padding Animation",
    )
    // Bottom Padding
    val webViewBottomPaddingFullscreen =
        if (settings.isSharpMode && !uiState.isLandscape) {
            maxOf(cutoutBottom, settings.deviceCornerRadius.dp)
        } else {
            cutoutBottom
        }
    val webViewBottomPaddingRegular = if (settings.isSharpMode) {
        maxOf(
            maxOf(cutoutBottom, settings.deviceCornerRadius.dp),
            innerPadding.calculateBottomPadding()
        )
    } else {
        innerPadding.calculateBottomPadding()

    }
    val webViewBottomPaddingNormalScreen = if (settings.isFullscreenMode) {
        webViewBottomPaddingFullscreen
    } else {
        webViewBottomPaddingRegular
    }

    val targetWebViewBottomPadding =
        if (uiState.isSettingCornerRadius
            || isPipMode
        ) {
            0.dp
        } else if (isKeyboardVisible && !uiState.isFocusOnTextField) {
            settings.padding.dp
        } else webViewBottomPaddingNormalScreen


    val webViewBottomPadding by animateDpAsState(
        targetValue = targetWebViewBottomPadding,
        animationSpec = paddingAnimationSpec,
        label = "WebView Bottom Padding Animation",
    )

    // Start Padding

    val webViewStartPaddingFullscreen =
        if (settings.isSharpMode && uiState.isLandscape) {
            maxOf(cutoutLeft, settings.deviceCornerRadius.dp)
        } else {
            cutoutLeft
        }
    val targetWebViewStartPadding =
        if (uiState.isSettingCornerRadius
            || isPipMode
        ) 0.dp else webViewStartPaddingFullscreen
    val webViewStartPadding by animateDpAsState(
        targetValue = targetWebViewStartPadding,
        animationSpec = paddingAnimationSpec,
        label = "WebView Start Padding Animation"
    )

    // End Padding

    val webViewEndPaddingFullscreen = if (settings.isSharpMode && uiState.isLandscape) {
        maxOf(cutoutRight, settings.deviceCornerRadius.dp)
    } else {
        cutoutRight
    }
    val targetWebViewEndPadding =
        if (uiState.isSettingCornerRadius
            || isPipMode
        ) 0.dp else webViewEndPaddingFullscreen

    val webViewEndPadding by animateDpAsState(
        targetValue = targetWebViewEndPadding,
        animationSpec = paddingAnimationSpec,
        label = "WebView End Padding Animation"
    )

    val webViewPaddingValue = PaddingValues(
        start = webViewStartPadding,
        top = webViewTopPadding,
        end = webViewEndPadding,
        bottom = webViewBottomPadding
    )

    //endregion

    //region Panel Paddings
    // If keyboard is displayed, only need the regular padding, no need extra padding
    val floatingPanelBottomPaddingNoKeyboard = if (settings.isFullscreenMode) {
        webViewBottomPaddingFullscreen
    } else {
        webViewBottomPaddingRegular
    }
    val floatingPanelBottomPadding by animateDpAsState(
        targetValue = if (isKeyboardVisible) (
                0.dp
                ) else (floatingPanelBottomPaddingNoKeyboard),
        animationSpec = tween(settings.animationSpeedForLayer(1)),
        label = "Floating Panel Padding Animation"
    )
    //endregion


    //region Permissions Handle
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            // this result run when user click on the android dialog to result

            if (permissions.contains(Manifest.permission.CAMERA) || permissions.contains(Manifest.permission.RECORD_AUDIO)) {
                // if it is media permission, use the stored viewModel.pendingMediaPermissionRequest we stored later to display the app permission panel
                viewModel.pendingMediaPermissionRequest.value?.onResult?.invoke(
                    permissions,
                    viewModel.pendingMediaPermissionRequest
                )

            } else {
                // if it is location we have displayed the app permission panel before the android panel so just grant the result from android level
                viewModel.pendingPermissionRequest.value?.let { request: CustomPermissionRequest ->
                    viewModel.siteSettingsManager.getDomain(request.origin)?.let { domain ->
                        viewModel.savePermissionDecision(domain, permissions)
                    }
                }

                viewModel.pendingPermissionRequest.value?.onResult?.invoke(
                    permissions,
                    viewModel.pendingPermissionRequest
                )

                // clear the request to hide the panel.
                viewModel.pendingPermissionRequest.value = null
            }

        }
    )

    //endregion

    val activity = context as Activity
    val gestureManager = remember { MediaGestureManager(activity) }
    val isDarkTheme = isSystemInDarkTheme()
    val view = LocalView.current
    val density = LocalDensity.current


    val squareAlpha = remember { Animatable(0f) }
    

    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )
    val cursorPadHeight by animateDpAsState(
        targetValue = if (isKeyboardVisible) ((viewModel.screenSizeDp.value.height.dp - webViewTopPadding) / 8
                ) else (viewModel.screenSizeDp.value.height.dp - webViewTopPadding) / 2,
        label = "Cursor Pad Height Animation"
    )
    val urlBarFocusRequester = remember { FocusRequester() }
    
    val initialX =
        if (settings.backSquareOffsetX != -1f) settings.backSquareOffsetX else 0f
    val initialY =
        if (settings.backSquareOffsetY != -1f) settings.backSquareOffsetY else 0f
    val backSquareOffsetX = remember { Animatable(initialX) }
    val backSquareOffsetY = remember { Animatable(initialY) }

    val geckoViewRef = remember { mutableStateOf<GeckoView?>(null) }

    val insetsController = activity.let {
        WindowCompat.getInsetsController(
            it.window,
            it.window.decorView
        )
    }


    val bottomPanelPagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    //region OptionsPanel Drag State
    val optionsPanelHeight =
        (settings.heightForLayer(2) + settings.padding).dp
    val optionsPanelHeightPx = with(density) { optionsPanelHeight.toPx() }

    // define anchors for options panel
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









    fun confirmationPopup(
        message: String,
        url: String = "",
        onConfirm: () -> Unit,
        onCancel: () -> Unit = {}
    ) {
        viewModel.confirmationState.value = ConfirmationDialogState(
            message = message,
            url = url,
            onConfirm = {
                onConfirm()
                viewModel.confirmationState.value = null // Automatically dismiss after action
            },
            onCancel = {
                onCancel()
                viewModel.confirmationState.value = null // Automatically dismiss after action
            }
        )
        viewModel.confirmationDisplayState.value = viewModel.confirmationState.value
    }




    // region Top Function
    suspend fun hideBackSquare(blinkEffect: Boolean = true) {
        val idle = settings.backSquareIdleOpacity
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

    val setIsOptionsPanelVisible = { setToVisible: Boolean ->

        coroutineScope.launch {
            if (setToVisible) {
                draggableState.animateTo(
                    targetValue = RevealState.Visible,
                    animationSpec = tween(
                        durationMillis = settings.animationSpeedForLayer(1),
                    )
                )
            } else {
                draggableState.animateTo(
                    targetValue = RevealState.Hidden,
                    animationSpec = tween(
                        durationMillis = settings.animationSpeedForLayer(1),
                    )
                )
            }
            delay((settings.animationSpeedForLayer(1) * 2).toLong())

        }

    }

    val handlePermissionToggle = { domain: String?, permission: String, isGranted: Boolean ->
        if (domain != null) {
            // 1. Get the current settings for the domain, or create a new one if it doesn't exist.

            if (permission.contains(generic_location_permission)
                && isGranted
                && !(ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }

            viewModel.toggleSitePermission(domain, permission)
        }

    }
    val handleCloseInspectedTab = {
        val tabToClose = viewModel.currentInspectingTab
        if (tabToClose != null && viewModel.tabs.indexOf(tabToClose) > -1) {
            confirmationPopup(
                message = "close tab ?",
                onConfirm = {
                    viewModel.closeInspectedTab {
                        activity.finishAndRemoveTask()
                        exitProcess(0)
                    }
                }
            )
        }
    }
    val closeAllTabs = {


        viewModel.tabs.clear()

        // clear the persisted list in SharedPreferences
        viewModel.tabManager.clearAllTabs()

        // close the application
        activity.finishAndRemoveTask()
        exitProcess(0)
    }

    val handleClearInspectedTabData = {
        confirmationPopup(
            message = "clear site data ?",
            onConfirm = {
                val inspectingTab = viewModel.currentInspectingTab

                if (inspectingTab != null) {
                    val domain = viewModel.siteSettingsManager.getDomain(inspectingTab.currentURL)

                    if (domain != null) {
                        viewModel.clearDomainData(domain)
                        val runtime = viewModel.geckoManager.runtime
                        val flags = StorageController.ClearFlags.ALL
                        runtime.storageController.clearData(flags).then {
                            runOnUiThread {
                                // loop through ALL viewModel.tabs to find matches
                                viewModel.tabs.forEachIndexed { _, tab ->
                                    val tabDomain = viewModel.siteSettingsManager.getDomain(tab.currentURL)

                                    // check if this tab belongs to the domain just cleared
                                    if (tabDomain == domain) {
                                        viewModel.updateTabById(tab.id) {t ->
                                            t.copy(savedState = null)
                                        }

                                        if (tab.id == viewModel.activeTab!!.id) {
                                            viewModel.geckoManager.closeSession(tab)
                                            viewModel.sessionRefreshTrigger.intValue++
                                        } else {
                                            viewModel.geckoManager.forceKillSession(tab.id)
                                        }
                                    }
                                }
                            }
                            // Return a result to satisfy the chain
                            GeckoResult.fromValue(it)
                        }
                    }

                    viewModel.updateUI { it.copy(isTabDataPanelVisible = false) }
                }

            }
        )
    }
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.pendingDownload?.let {
                viewModel.performDownloadEnqueue(it)
            }
        } else {
            Toast.makeText(context, "Storage permission denied", Toast.LENGTH_LONG).show()
        }
        // not use but still need to keep here to reset the pending download
        viewModel.pendingDownload = null
    }
    val startDownload = { url: String, userAgent: String, contentDisposition: String?, mimeType: String? ->
            val params = DownloadParams(url, userAgent, contentDisposition, mimeType)

//            val needsPermission = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
            val needsPermission = false
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (needsPermission && !hasPermission) {
                viewModel.pendingDownload = params
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                // Call the standard function directly
                viewModel.performDownloadEnqueue(params)
            }
        }
    fun navigateWebView() {
        when (viewModel.activeNavAction.value) {
            GestureNavAction.BACK -> if (viewModel.activeTab!!.canGoBack) {
                activeSession.goBack(true)
            }

            GestureNavAction.REFRESH -> {
                activeSession.reload()
            }

            GestureNavAction.FORWARD -> if (viewModel.activeTab!!.canGoForward) {
                activeSession.goForward(true)
            }

            GestureNavAction.CLOSE_TAB -> {
                if (uiState.isLoading) viewModel.updateUI { it.copy(isLoading = false) }
                viewModel.closeActiveTab {
                    activity.finishAndRemoveTask()
                    exitProcess(0)

                }
            }
            GestureNavAction.NEW_TAB -> {
                val newIndex = activeTabIndex + 1
                viewModel.createNewTab(newIndex, "")
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

                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                setDataAndType(MediaStore.Downloads.EXTERNAL_CONTENT_URI, "*/*")
            }
            try {
                context.startActivity(genericFileManagerIntent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "Could not find a file manager app.", Toast.LENGTH_LONG)
                    .show()
            }
        }
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
    // endregion
    


    //endregion
    

    //region Single Panel


    // 1. State to track the single "main" active panel
    var activeMainPanel by remember { mutableStateOf<ActivePanel?>(null) }

    // 2. Observers for MAIN panels: When a main panel opens, it claims focus.
    LaunchedEffect(uiState.isAppsPanelVisible) {
        if (uiState.isAppsPanelVisible) activeMainPanel = ActivePanel.APPS
    }
    LaunchedEffect(viewModel.suggestions) {
        if (viewModel.suggestions.isNotEmpty()) activeMainPanel = ActivePanel.SUGGESTIONS
    }

    LaunchedEffect(uiState.isDownloadPanelVisible) {
        if (uiState.isDownloadPanelVisible) activeMainPanel = ActivePanel.DOWNLOADS
    }
    LaunchedEffect(viewModel.contextMenuData.value) {
        if (viewModel.contextMenuData.value != null) activeMainPanel = ActivePanel.CONTEXT_MENU
    }
    LaunchedEffect(uiState.isFindInPageVisible) {
        if (uiState.isFindInPageVisible) activeMainPanel = ActivePanel.FIND_IN_PAGE
    }
    LaunchedEffect(viewModel.jsDialogState.value) {
        if (viewModel.jsDialogState.value != null) activeMainPanel = ActivePanel.PROMPT
    }
    LaunchedEffect(uiState.isSettingsPanelVisible) {
        if (uiState.isSettingsPanelVisible) activeMainPanel = ActivePanel.SETTINGS
    }
    LaunchedEffect(viewModel.pendingPermissionRequest.value) {
        if (viewModel.pendingPermissionRequest.value != null) activeMainPanel = ActivePanel.PERMISSION
    }
    LaunchedEffect(uiState.isTabsPanelVisible) {
        // When TabsPanel opens, it becomes the main panel.
        if (uiState.isTabsPanelVisible) activeMainPanel = ActivePanel.TABS
    }
    LaunchedEffect(uiState.isTabDataPanelVisible) {
        // When TabDataPanel opens, it ensures Tabs is the main panel and forces it open.
        if (uiState.isTabDataPanelVisible) {
            activeMainPanel = ActivePanel.TABS
            viewModel.updateUI { it.copy(isTabsPanelVisible = true) }
        }
    }

    // 3. Enforcer for MAIN panels: When focus changes, close all other main panels.
    LaunchedEffect(activeMainPanel) {
        val current = activeMainPanel // Capture the current state

        if (current != ActivePanel.APPS && uiState.isAppsPanelVisible) {
            viewModel.updateUI { it.copy(isAppsPanelVisible = false) }
        }
        if (current != ActivePanel.DOWNLOADS && uiState.isDownloadPanelVisible) viewModel.updateUI {
            it.copy(
                isDownloadPanelVisible = false
            )
        }
        if (current != ActivePanel.CONTEXT_MENU && viewModel.contextMenuData.value != null) viewModel.contextMenuData.value = null
        if (current != ActivePanel.FIND_IN_PAGE && uiState.isFindInPageVisible) viewModel.updateUI {
            it.copy(
                isFindInPageVisible = false
            )
        }
        if (current != ActivePanel.PROMPT && viewModel.jsDialogState.value != null) viewModel.jsDialogState.value = null
        if (current != ActivePanel.SETTINGS && uiState.isSettingsPanelVisible) {
            viewModel.updateUI { it.copy(isSettingsPanelVisible = false) }
        }

        if (current != ActivePanel.PERMISSION && viewModel.pendingPermissionRequest.value != null) viewModel.pendingPermissionRequest.value =
            null

        if (current != ActivePanel.SUGGESTIONS && viewModel.suggestions.isNotEmpty()) viewModel.suggestions.clear()

        // If the active panel is NOT TABS, close both TABS and TAB_DATA.
        if (current != ActivePanel.TABS) {
            if (uiState.isTabsPanelVisible) viewModel.updateUI { it.copy(isTabsPanelVisible = false) }

            if (uiState.isTabsPanelLock) viewModel.updateUI { it.copy(isTabsPanelLock = false) }

            if (uiState.isTabDataPanelVisible) viewModel.updateUI { it.copy(isTabDataPanelVisible = false) }

        }
    }

    // 4. Exception Rule: If TabsPanel is closed, TabDataPanel must also close.
    // This enforces their parent-child relationship.
    LaunchedEffect(uiState.isTabsPanelVisible) {
        if (!uiState.isTabsPanelVisible && uiState.isTabDataPanelVisible) {
            viewModel.updateUI { it.copy(isTabDataPanelVisible = false) }
        }
    }
    //endregion


    CompositionLocalProvider(
        LocalBrowserViewModel provides viewModel
    ) {
        //region LaunchedEffect
        LaunchedEffect(activeTabIndex) {
            val currentUrl = viewModel.tabs.getOrNull(activeTabIndex)?.currentURL ?: ""
            if (!uiState.isFocusOnUrlTextField) {
                textFieldState.setTextAndPlaceCursorAtEnd(currentUrl.toDomain())
            }
        }
        LaunchedEffect(viewModel.choiceState.value) {
            if (viewModel.choiceState.value != null)
                viewModel.choiceDisplayState.value = viewModel.choiceState.value
        }
        LaunchedEffect(viewModel.colorState.value) {
            if (viewModel.colorState.value != null)
                viewModel.colorDisplayState.value = viewModel.colorState.value
        }

        LaunchedEffect(viewModel.dateTimeState.value) {
            if (viewModel.dateTimeState.value != null)
                viewModel.dateTimeDisplayState.value = viewModel.dateTimeState.value
        }

        LaunchedEffect(
            viewModel.choiceState.value,
            viewModel.colorState.value,
            viewModel.dateTimeState.value,
        ) {
            viewModel.updateUI {
                it.copy(
                    isOtherPanelVisible = viewModel.choiceState.value != null
                            || viewModel.colorState.value != null
                            || viewModel.dateTimeState.value != null
                )
            }

        }
        LaunchedEffect(uiState.isOtherPanelVisible) {
            if (uiState.isOtherPanelVisible) viewModel.updateUI { it.copy(isBottomPanelVisible = false) }
        }
        LaunchedEffect(uiState.isFocusOnSettingTextField, uiState.isFocusOnUrlTextField, uiState.isFocusOnFindTextField) {
            viewModel.updateUI {
                it.copy(
                    isFocusOnTextField = uiState.isFocusOnFindTextField || uiState.isFocusOnUrlTextField || uiState.isFocusOnSettingTextField
                )
            }
        }
        LaunchedEffect(Unit) {
            newUrlFlow.collect { urlFromIntent ->
                if (urlFromIntent != null) {

                    // 1. Calculate the index: Current Active + 1
                    // If list is empty, index is 0.
                    val currentActive = activeTabIndex
                    val nextIndex = if (viewModel.tabs.isEmpty()) 0 else currentActive + 1

                    // 2. Perform creation
                    viewModel.createNewTab(nextIndex, urlFromIntent)

                    // 3. Reset the flow so we don't re-open it on configuration change
                    context.newUrlFromIntent.value = null
                }
            }

        }
        LaunchedEffect(uiState.isLandscapeByButton, uiState.isOnFullscreenVideo) {
            viewModel.updateUI { it.copy(isLandscape = uiState.isLandscapeByButton || uiState.isOnFullscreenVideo) }
        }
        LaunchedEffect(uiState.isLandscapeByButton) {
            if (uiState.isLandscapeByButton) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                viewModel.updateUI { it.copy(isBottomPanelVisible = false) }
                viewModel.updateUI { it.copy(isUrlBarVisible = false) }
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            viewModel.updateSettings { it.copy(isFullscreenMode = uiState.isLandscapeByButton) }
        }
        LaunchedEffect(uiState.isOnFullscreenVideo) {
            viewModel.updateUI { it.copy(isMediaControlPanelVisible = uiState.isOnFullscreenVideo) }

            viewModel.updateSettings { it.copy(isFullscreenMode = uiState.isOnFullscreenVideo) }
            if (uiState.isOnFullscreenVideo) {
                gestureManager.ensureFullscreenBrightness()
            } else {
                if (activity.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }

        }
        LaunchedEffect(uiState.isFocusOnTextField, uiState.isPromptPanelVisible) {
            if (!uiState.isFocusOnTextField && !uiState.isPromptPanelVisible) {
                delay(300)
                viewModel.isApplyImePaddingToWebView.value = true
            } else {
                viewModel.isApplyImePaddingToWebView.value = false
            }

        }
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                val insetsController = WindowCompat.getInsetsController(window, view)

                // 1. Make the status bar transparent

                // 2. FORCE White Icons (set to false)
                insetsController.isAppearanceLightStatusBars = false
            }
        }
        LaunchedEffect(uiState.isSettingsPanelVisible) {
            if (!uiState.isSettingsPanelVisible)
                viewModel.updateSettings { it.copy(isFirstAppLoad = false) }
        }
        LaunchedEffect(viewModel.inspectingAppId.longValue) {
            viewModel.descriptionContent.value = viewModel.apps.find { it.id == viewModel.inspectingAppId.longValue }?.label ?: ""

        }
        LaunchedEffect(viewModel.apps.size) {
            if (viewModel.apps.isEmpty()) {
                viewModel.resetBottomPanelTrigger.value = !viewModel.resetBottomPanelTrigger.value
            }
        }
        LaunchedEffect(bottomPanelPagerState.settledPage, bottomPanelPagerState.currentPage, uiState.isUrlOverlayBoxVisible) {

            if (bottomPanelPagerState.currentPage == BottomPanelMode.SEARCH.ordinal) {
                viewModel.updateUI { it.copy(isUrlOverlayBoxVisible = true) }
                if (uiState.isAppsPanelVisible) {
                    viewModel.updateUI { it.copy(isAppsPanelVisible = false) }
                }
                if (uiState.isTabsPanelLock && !uiState.isFocusOnUrlTextField) viewModel.updateUI {
                    it.copy(
                        isTabsPanelVisible = true
                    )
                }
                if (viewModel.inspectingAppId.longValue != 0L) viewModel.inspectingAppId.longValue = 0L
            } else {
                viewModel.updateUI {
                    it.copy(
                        isFocusOnUrlTextField = false,
                        isDownloadPanelVisible = false,
                        isNavPanelVisible = false,
                        isSettingsPanelVisible = false,
                        isTabsPanelVisible = false,
                        isFindInPageVisible = false,
                    )
                }
                setIsOptionsPanelVisible(false)

                keyboardController?.hide()
                if (bottomPanelPagerState.currentPage == BottomPanelMode.APPS.ordinal) {
                    viewModel.updateUI { it.copy(isAppsPanelVisible = true) }

                }

            }
            if (bottomPanelPagerState.settledPage != BottomPanelMode.SEARCH.ordinal) {
                focusManager.clearFocus()
            }

        }
        LaunchedEffect(bottomPanelPagerState.currentPage) {
            if (bottomPanelPagerState.currentPage == BottomPanelMode.LOCK.ordinal) {
                viewModel.updateSettings { it.copy(isFullscreenMode = !settings.isFullscreenMode) }
            }
        }
        LaunchedEffect(viewModel.resetBottomPanelTrigger.value) {
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
                if (viewModel.apps.isEmpty()) {
                    bottomPanelPagerState.animateScrollToPage(BottomPanelMode.SEARCH.ordinal)

                }
            }
        }
        LaunchedEffect(viewModel.screenSize.value) {
            Log.i("marcPip", "onscreenSize")
            if (viewModel.screenSize.value.width > 0 && !viewModel.isBackSquareInitialized.value && !isPipMode) {
                val buttonSize = with(density) {
                    settings.heightForLayer(1).dp.toPx()
                }

                val defaultX =
                    viewModel.screenSize.value.width - buttonSize - with(density) { settings.padding.dp.toPx() }
                val defaultY =
                    viewModel.screenSize.value.height - buttonSize - with(density) { settings.padding.dp.toPx() }

                backSquareOffsetX.snapTo(defaultX)
                backSquareOffsetY.snapTo(defaultY)
                viewModel.isBackSquareInitialized.value = true
            }
        }
        LaunchedEffect(textFieldState.text, uiState.isFocusOnUrlTextField) {
            if (uiState.isFocusOnUrlTextField) {
                viewModel.fetchSuggestions(
                    query = textFieldState.text.toString(),
                    isPinning = uiState.isPinningApp
                )
            } else {
                viewModel.suggestions.clear()
            }
        }
        LaunchedEffect(uiState.isFindInPageVisible) {
            if (!uiState.isFindInPageVisible) {
                viewModel.findInPageText.value = ""
                viewModel.findInPageResult.value = 0 to 0
            }
        }
        LaunchedEffect(uiState.isSettingsPanelVisible) {
            if (!uiState.isSettingsPanelVisible) {
                viewModel.backgroundColor.value = Color.Black
            }
        }
        LaunchedEffect(uiState.isCursorMode) {
            viewModel.updateUI { it.copy(isCursorPadVisible = uiState.isCursorMode) }
            if (uiState.isCursorMode) {
                viewModel.updateUI { it.copy(isUrlBarVisible = false) }
            }
        }
        LaunchedEffect(uiState.inspectingTabId) {
            if (uiState.inspectingTabId == null) {
                viewModel.updateUI { it.copy(isTabDataPanelVisible = false) }
            }
        }
        LaunchedEffect(activeSession) {

            activeSession.setActive(true)

            if (!activeSession.isOpen) {
                try {
                    Log.d("InitFlow", "open active session")
                    activeSession.open(viewModel.geckoManager.runtime)
                } catch (e: Exception) {
                    Log.w(
                        "BrowserScreen",
                        "Session open ignored (likely engine-managed): ${e.message}"
                    )
                }
            }
            viewModel.geckoManager.setupDelegates(
                session = activeSession,
//                tab = viewModel.activeTab!!,
                tab = object : MutableState<Tab> { // Bridge for the old delegate code
                    override var value: Tab
                        get() = viewModel.tabs.getOrElse(activeTabIndex) { Tab.createEmpty() }
                        set(newTab) {
//                            viewModel.tabs[activeTabIndex] = newTab
                            viewModel.updateTabById(newTab.id) { newTab }
                        }

                    override fun component1() = value
                    override fun component2(): (Tab) -> Unit = { value = it }
                },
                browserSettings = settingsState,
                onTitleChangeFun = { eventTabId, _, title ->

                    viewModel.updateTabById(eventTabId) { it.copy(currentTitle = title) }


                    val url = viewModel.activeTab!!.currentURL
                    viewModel.addHistory(url, title)


                },
                onProgressChange = { int ->
                    viewModel.updateUI { it.copy(isLoading = (int < 100)) }
                },
                onLocationChangeFun = { eventTabId, _, url, _, _ ->
                    if (eventTabId == viewModel.activeTab!!.id
                        && url != null
                        && url != "about:blank"
                        && !url.startsWith("javascript:")
                    ) {
//                        if (!uiState.isFocusOnUrlTextField) {
//                            textFieldState.setTextAndPlaceCursorAtEnd(url.toDomain())
//                        }
//
//                        // Update the Tab data (this is safe to do for active tab)
//                        if (viewModel.activeTab!!.currentURL != url) {
//                            viewModel.updateTabById(eventTabId) { it.copy(currentURL = url) }
//                        }

                        if (eventTabId == viewModel.activeTab?.id && !uiState.isFocusOnUrlTextField) {
                            textFieldState.setTextAndPlaceCursorAtEnd(url.toDomain())
                        }
                        Log.d("TabFLow", "onLocationChange")

                        viewModel.updateTabById(eventTabId) { it.copy(currentURL = url) }
                    }
                },

                onNewSessionFunWithId = { id, uri ->
                    viewModel.handleNewSession(id, uri)
                },
                onHistoryStateChangeFun = { eventTabId, _, realtimeHistory ->

                    val url = realtimeHistory[realtimeHistory.lastIndex].uri

                    // fe:  change  tab A -> B, the textbox changed to A.url
//                if (session == activeSession) {
                    if (eventTabId == viewModel.activeTab!!.id && url.isNotBlank() && url != "about:blank") {
                        if (!uiState.isFocusOnUrlTextField) {
                            textFieldState.setTextAndPlaceCursorAtEnd(url.toDomain())
                        }

                        Log.d("TabFLow", "onHistoryStateChange")
                        viewModel.updateTabById(eventTabId) { tab ->
                            val cachedIcon = tab.faviconCache[url] ?: ""
                            tab.copy(currentURL = url, currentFaviconUrl = cachedIcon)
                        }
//                        if (viewModel.activeTab!!.currentURL != url) {
//                            // Get the current tab state
//                            val currentTab = viewModel.tabs[activeTabIndex]
//
//                            // TRY TO RESTORE ICON FROM THIS TAB'S CACHE
//                            val cachedIcon = currentTab.faviconCache[url] ?: ""
//
//                            // Update URL and Icon immediately
//                            viewModel.tabs[activeTabIndex] = currentTab.copy(
//                                currentURL = url,
//                                currentFaviconUrl = cachedIcon
//                            )
//                            viewModel.updateTabById(eventTabId) {tab ->
//                                val cachedIcon = tab.faviconCache[url] ?: ""
//                                tab.copy(currentURL = url, currentFaviconUrl = cachedIcon)
//
//                            }
//                        }
                    }
                },
                onSessionStateChangeFun = { _, _ ->
                    val stateToSave =
                        viewModel.geckoManager.getSessionStateString(viewModel.activeTab!!.id)
                    if (stateToSave != null) {
                        viewModel.updateTabById(viewModel.activeTab!!.id) { it.copy(savedState = stateToSave) }
                        viewModel.tabManager.saveTabs(viewModel.tabs, activeTabIndex)
                    }
                },
                onCanGoBackFun = { _, canGoBack ->
                    viewModel.updateTabById(viewModel.activeTab!!.id) { it.copy(canGoBack = canGoBack) }
                },
                onCanGoForwardFun = { _, canGoForward ->
                    viewModel.updateTabById(viewModel.activeTab!!.id) { it.copy(canGoForward = canGoForward) }
                },
                setPermissionDelegate = { request ->
                    if (request.permissionsToRequest.contains(Manifest.permission.CAMERA) || request.permissionsToRequest.contains(
                            Manifest.permission.RECORD_AUDIO
                        )
                    ) {
                        // this is the problem
                        if (request.isSystemRequest) {
                            viewModel.pendingMediaPermissionRequest.value = request
                            permissionLauncher.launch(request.permissionsToRequest.toTypedArray())

//                        val hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
//
//
//                        val hasVideo = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
//
//
//                        if (!hasAudio || !hasVideo) {
//                            // We don't have OS permission, so we can't give Site permission.
////                            viewModel.pendingMediaPermissionRequest = request
//                            permissionLauncher.launch(request.permissionsToRequest.toTypedArray())
//                        } else {
//
//                            viewModel.pendingPermissionRequest.value = request
//                        }
                        } else {
                            viewModel.pendingPermissionRequest.value = request
                        }

                    } else {
                        viewModel.pendingPermissionRequest.value = request
                    }
                },
                onPageStartFun = { eventTabId, _, url ->
                    viewModel.pendingPermissionRequest.value?.let { request ->
                        // Check if the new URL's host is DIFFERENT from the origin of the permission request.
                        val newHost = url.toUri().host
                        val requestHost = request.origin.toUri().host

                        if (newHost != requestHost) {
                            // The user is navigating away, so clear the old permission request.
                            viewModel.pendingPermissionRequest.value = null
                        }
                    }
                    if (eventTabId == viewModel.activeTab!!.id && url != "about:blank") {
                        viewModel.updateUI { it.copy(isLoading = true) }
                        if (viewModel.activeTab!!.errorState != null) {
                            viewModel.updateTabById(eventTabId) { it.copy(errorState = null) }
                        }

                        if (!uiState.isFocusOnUrlTextField) textFieldState.setTextAndPlaceCursorAtEnd(
                            url.toDomain()
                        )

                    }

                },
                onPageStopFun = { _, _ ->
                    viewModel.updateUI { it.copy(isLoading = false) }
                },
                onFaviconChanged = { tabId, faviconUrl ->
                    // Find the index of the tab that fired this event.
                    val tabIndex = viewModel.tabs.indexOfFirst { it.id == tabId }
                    if (tabIndex == -1) return@setupDelegates

                    val targetTab = viewModel.tabs[tabIndex]

                    // Check if an update is even needed to prevent unnecessary recompositions.
                    if (faviconUrl.isNotBlank()) {
                        val newCache = targetTab.faviconCache.toMutableMap().apply {
                            put(targetTab.currentURL, faviconUrl)
                        }

                        // Update the tab with the new Icon AND the new Cache
                        viewModel.tabs[tabIndex] = targetTab.copy(
                            currentFaviconUrl = faviconUrl,
                            faviconCache = newCache
                        )
                        saveTrigger++
                    }
                },
                onContextMenuFun = { data ->
                    if (data.linkUrl.isNullOrBlank() && data.srcUrl.isNullOrBlank()) {
                        // do nothing
                    } else {
                        viewModel.contextMenuData.value = data
                        viewModel.contextMenuDisplayData.value = viewModel.contextMenuData.value
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }

                },
                onDownloadRequested = { url, userAgent, contentDisposition, mimeType ->
                    // Trigger your existing download logic
                    confirmationPopup(
                        message = "download file on",
                        url = url,
                        onConfirm = {
                            startDownload(url, userAgent, contentDisposition, mimeType)
                        },
                        onCancel = {
                        }
                    )
                },
                onJsAlert = { message ->
                    viewModel.jsDialogState.value = JsAlert(message)
                },
                onJsConfirm = { message, callback ->
                    viewModel.jsDialogState.value = JsConfirm(message, callback)
                },
                onJsPrompt = { message, defaultValue, callback ->
                    viewModel.jsDialogState.value = JsPrompt(message, defaultValue, callback)
                },

                onLoadErrorFun = { session, uri, error ->
                    // Only show if it's the active tab
                    if (session == activeSession) {
                        viewModel.updateUI { it.copy(isLoading = false) }

                        val newError = ErrorState(
                            error = error,
                            failingUrl = uri ?: viewModel.activeTab!!.currentURL
                        )

                        // Update the Tab object
                        viewModel.updateTabById(viewModel.activeTab!!.id) { it.copy(errorState = newError) }

                    }
                },
                siteSettings = viewModel.siteSettings,
                siteSettingsManager = viewModel.siteSettingsManager,


                onFullScreenFun = { isFullscreen ->
                    viewModel.updateUI {
                        it.copy(
                            isMediaControlPanelDisplayed = isFullscreen,
                            isMediaControlPanelVisible = isFullscreen,
                            isOnFullscreenVideo = isFullscreen
                        )
                    }

                    val inPip = mainActivity.isPipMode || mainActivity.isEnteringPip

                    if (inPip && !isFullscreen) {
                        Log.i(
                            "marcPip",
                            "Ignoring Gecko Fullscreen Exit (Keep UI in Fullscreen for PiP)"
                        )

                    } else {
                        // Normal behavior for all other cases
                        viewModel.updateUI { it.copy(isOnFullscreenVideo = isFullscreen) }
                        mainActivity.isCurrentlyFullscreen = isFullscreen
                        mainActivity.updatePipParams(isFullscreen)

                        if (isFullscreen) {
                            activity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            insetsController.hide(WindowInsetsCompat.Type.systemBars())
                            insetsController.systemBarsBehavior =
                                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            if (uiState.isBottomPanelVisible) viewModel.updateUI {
                                it.copy(
                                    isBottomPanelVisible = false
                                )
                            }
                        } else {
                            // Only exit landscape/immersive if NOT in PiP
                            if (!inPip) {

                                if (uiState.isLandscapeByButton) viewModel.updateUI {
                                    it.copy(
                                        isLandscapeByButton = false
                                    )
                                }
                                gestureManager.resetBrightness()

                                activity.requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                if (!settings.isFullscreenMode) {
                                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                                    insetsController.systemBarsBehavior =
                                        WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                                }
                                coroutineScope.launch {
                                    hideBackSquare(true)
                                }
                            }
                        }
                    }
                    mainActivity.updatePipParams(isFullscreen)
                    if (isFullscreen) {
                        // When video is fullscreen, UNLOCK rotation (allow Landscape)
                        // Use SCREEN_ORIENTATION_SENSOR to let the user rotate the phone naturally.
                        // Or use SCREEN_ORIENTATION_SENSOR_LANDSCAPE if you want to FORCE it sideways immediately.
                        activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

                        insetsController.hide(WindowInsetsCompat.Type.systemBars())
                        insetsController.systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                        if (uiState.isBottomPanelVisible) viewModel.updateUI {
                            it.copy(
                                isBottomPanelVisible = false
                            )
                        }


                    } else {
                        // When exiting fullscreen, LOCK back to Portrait
                        Log.i("marcPip", "exit full screen")

                        if (inPip) {
                            Log.i("marcPip", "Ignored Fullscreen Exit - Transitioning to/in PiP")
                        } else {
                            Log.i("marcPip", "Normal Fullscreen Exit")
                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                            if (!settings.isFullscreenMode) {
                                insetsController.show(WindowInsetsCompat.Type.systemBars())
                                insetsController.systemBarsBehavior =
                                    WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                            }
                            coroutineScope.launch {
                                hideBackSquare(true)
                            }
                        }

                    }

                },
                onSessionCrash = {
                    viewModel.sessionRefreshTrigger.intValue++
                },
                onChoicePromptFun = { viewModel.choiceState.value = it },
                onFilePromptFun = { prompt, result ->
                    mainActivity.openFilePicker(prompt, result)
                },
                onColorPromptFun = {
                    viewModel.colorState.value = it
                },
                onDateTimePromptFun = { viewModel.dateTimeState.value = it },
                onCloseTabFun = { id ->
                    viewModel.closeTabById(id)
                },


                )

            if (!uiState.initialLoadDone && initialIntentUrl != null && viewModel.activeTab!!.currentURL == initialIntentUrl) {
                webViewLoad(activeSession, initialIntentUrl, settings)
                viewModel.updateUI { it.copy(initialLoadDone = true) }
            }
            else {
                // If the session is empty (no navigation history) and not being restored, load the URL.
                // This covers "New Tab" clicks and "Target Blank" where engine didn't auto-load.
                if (viewModel.activeTab!!.savedState == null) {
                    val urlToLoad = viewModel.activeTab!!.currentURL.ifBlank { settings.defaultUrl }
                    // Avoid reloading if it's already on that page (prevents loop)
                    // But since historyState is null, we are safe to load.
                    webViewLoad(activeSession, urlToLoad, settings)
                }
            }
        }
        DisposableEffect(activeSession) {
            Log.i("marcPip", "DisposableEffect")
            activeSession.setActive(true)

            onDispose {
                activeSession.setActive(false)
            }
        }
        LaunchedEffect(viewModel.jsDialogState.value) {
            if (viewModel.jsDialogState.value != null) {
                viewModel.jsDialogDisplayState.value = viewModel.jsDialogState.value
            }
        }
        LaunchedEffect(uiState.isUrlBarVisible) {
            if (!uiState.isUrlBarVisible) {
                setIsOptionsPanelVisible(false)
                viewModel.updateUI { it.copy(isTabDataPanelVisible = false) }
                viewModel.updateUI { it.copy(isTabsPanelVisible = false) }
                viewModel.updateUI { it.copy(isSettingsPanelVisible = false) }

//            if (downloads.isEmpty()) isDownloadPanelVisible = false
                viewModel.updateUI { it.copy(isDownloadPanelVisible = false) }
                coroutineScope.launch {
                    bottomPanelPagerState.animateScrollToPage(BottomPanelMode.SEARCH.ordinal)
                }
            } else {
                if (uiState.isTabsPanelLock && bottomPanelPagerState.currentPage == BottomPanelMode.SEARCH.ordinal) viewModel.updateUI {
                    it.copy(
                        isTabsPanelVisible = true
                    )
                }
                if (uiState.isCursorMode) viewModel.updateUI { it.copy(isCursorMode = false) }
            }
        }
        LaunchedEffect(viewModel.jsDialogState.value) {
            viewModel.updateUI { it.copy(isPromptPanelVisible = viewModel.jsDialogState.value != null) }
        }
        LaunchedEffect(
            uiState.isUrlBarVisible,
            uiState.isPermissionPanelVisible,
            uiState.isPromptPanelVisible,
            viewModel.confirmationState.value,
            viewModel.contextMenuData.value,
            viewModel.choiceState.value
        ) {
            viewModel.updateUI {
                it.copy(
                    isBottomPanelVisible = uiState.isUrlBarVisible
                            || uiState.isPermissionPanelVisible
                            || uiState.isPromptPanelVisible
                            || viewModel.confirmationState.value != null
                            || viewModel.contextMenuData.value != null
                            || viewModel.choiceState.value != null
                )
            }
        }
        LaunchedEffect(settings.isFullscreenMode) {
            if (settings.isFullscreenMode) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }


        }
        LaunchedEffect(viewModel.screenSize.value) {
            val squareBoxSize = settings.heightForLayer(1).dp

            val squareBoxSizePx = with(density) { squareBoxSize.toPx() }
            val paddingPx = with(density) { settings.padding.dp.toPx() }


            if (viewModel.screenSize.value.height.toFloat() > (squareBoxSizePx - paddingPx)
                && backSquareOffsetY.value > viewModel.screenSize.value.height.toFloat() - squareBoxSizePx - paddingPx
                && !uiState.isLandscape
                && !uiState.isBottomPanelVisible
            ) {
                // Clamp Y to screen bounds
                val targetY = backSquareOffsetY.value.coerceIn(
                    paddingPx,
                    viewModel.screenSize.value.height.toFloat() - squareBoxSizePx - paddingPx
                )
                coroutineScope.launch {

                    launch {
                        backSquareOffsetY.animateTo(
                            targetY,
                            spring()
                        )
                    }

                    viewModel.updateSettings {
                        it.copy(
                            backSquareOffsetY = targetY
                        )
                    }

                    hideBackSquare(false)
                }
            }

        }
        DisposableEffect(Unit) {
            val shakeDetector = ShakeDetector(context) {
                // This code runs when a shake is detected
                coroutineScope.launch {
                    // Only trigger if the URL bar isn't already visible to avoid conflicts
                    if (!uiState.isBottomPanelVisible) {
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
        LaunchedEffect(viewModel.pendingPermissionRequest.value) {
            viewModel.updateUI { it.copy(isPermissionPanelVisible = viewModel.pendingPermissionRequest.value != null) }
        }
        // This effect will re-launch whenever isBottomPanelVisible changes.
        LaunchedEffect(uiState.isBottomPanelVisible) {
            if (!uiState.isBottomPanelVisible) {
                // -- The URL bar has just been hidden. Start the "show and blink" sequence. --

                // a. Instantly appear with 0.6 opacity.
                if (!uiState.isCursorMode) hideBackSquare()


                // d. After blinking, fade out completely.
            } else {
                // -- The URL bar is visible. Ensure the square is fully transparent. --
                hideBackSquare(false)
                geckoViewRef.value?.clearFocus()
            }

        }
        LaunchedEffect(settings.backSquareIdleOpacity) {
            if (!uiState.isBottomPanelVisible && !uiState.isCursorPadVisible) {
                squareAlpha.animateTo(settings.backSquareIdleOpacity)
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

        LaunchedEffect(viewModel.overlayHeightPx.floatValue) {
            // We only want to act the first time the height is measured (it changes from 0f to a positive value).
            // The `offsetY.value == 0f` check is an extra safeguard to ensure we only do this once on startup.
            if (viewModel.overlayHeightPx.floatValue > 0f && offsetY.value == 0f) {
                // Instantly "snap" the overlay to its hidden position without any animation.
                // The hidden position is its full height negated, moving it off-screen upwards.
                offsetY.snapTo(-viewModel.overlayHeightPx.floatValue * 2)
            }
        }

        LaunchedEffect(saveTrigger) {
            if (saveTrigger > 0) {
                viewModel.tabManager.saveTabs(viewModel.tabs, activeTabIndex)
                saveTrigger = 0
            }
        }

        LaunchedEffect(Unit) {
            newUrlFlow.collect { url ->
                if (url != null) {

                    // When a new URL arrives, create a new tab for it.
                    // We'll insert it right after the current active tab.
                    val insertIndex = (activeTabIndex + 1).coerceAtMost(viewModel.tabs.size)
                    viewModel.createNewTab(insertIndex, url)

                    // IMPORTANT: Consume the event by setting the flow back to null
                    context.newUrlFromIntent.update { null }
                }
            }
        }

        LaunchedEffect(Unit) {
            focusManager.clearFocus()
        }
        //endregion

        BackHandler(enabled = true) {
            when {

                // when full screen video ( 100 % landscape mode)
                uiState.isOnFullscreenVideo -> activeSession.exitFullScreen()

                // landscape mode but not video
                activity.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    if (uiState.isLandscapeByButton) {
                        viewModel.updateUI { it.copy(isLandscapeByButton = false) }
                    }
                }


                // back the webview
                viewModel.activeTab!!.canGoBack -> {
                    activeSession.goBack(true)
                }

                else -> {
                    // not back to home screen
                }
            }
        }


        //region Layout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(viewModel.backgroundColor.value)
        ) {


            // Adjust Device Corner Radius Screen
            AnimatedVisibility(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.ime)
                    .align(Alignment.BottomCenter),
                visible = settings.isFirstAppLoad,
                enter = slideInVertically(
                    animationSpec = tween(settings.animationSpeedForLayer(0) * 4),
                    initialOffsetY = { it }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(settings.animationSpeedForLayer(0) * 4),
                    targetOffsetY = { -it }
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(
                                settings.cornerRadiusForLayer(0).dp
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
                visible = settings.isFirstAppLoad,
                enter = fadeIn(tween(settings.animationSpeedForLayer(0))),
                exit = fadeOut(animationSpec = tween(settings.animationSpeedForLayer(0) * 4))
            ) {
                Box(
                    modifier = Modifier
                        .padding(settings.padding.dp)
                        .padding(webViewPaddingValue)
                        .clip(
                            RoundedCornerShape(
                                settings.cornerRadiusForLayer(1).dp
                            )
                        )
                        .windowInsetsPadding(WindowInsets.ime)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)

                        .background(Color.Black)
                        .padding(bottom = settings.padding.dp)
                ) {
                    SettingsPanel(
                        confirmationPopup = ::confirmationPopup,
                        targetSetting = SettingPanelView.CORNER_RADIUS,
                    )
                }
            }

            // WebView
            AnimatedVisibility(
                visible = !settings.isFirstAppLoad,
                enter = slideInVertically(
                    animationSpec = tween(settings.animationSpeedForLayer(0) * 4),
                    initialOffsetY = { it }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(settings.animationSpeedForLayer(0) * 4),
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
                        AnimatedVisibility(
                            visible = viewModel.activeTab!!.errorState == null,
                            enter = slideInVertically(
                                animationSpec = tween(settings.animationSpeedForLayer(0) * 4),
                                initialOffsetY = { it }
                            ),
                            exit = slideOutVertically(
                                animationSpec = tween(settings.animationSpeedForLayer(0) * 4),
                                targetOffsetY = { -it }
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .run {
                                        if (viewModel.isApplyImePaddingToWebView.value) {
                                            this.windowInsetsPadding(WindowInsets.ime)
                                        } else {
                                            this
                                        }
                                    }
                                    .clip(
                                        RoundedCornerShape(
                                            animatedCornerRadius
                                        )
                                    )
                                    //                            .background(color = Color.White)
                                    .testTag("WebViewContainer")


                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()

                                ) {
                                    AndroidView(
                                        modifier = Modifier.fillMaxSize(),
                                        factory = { context ->
                                            // Create the View ONCE.
                                            // We never need to recreate this View during tab switching.
                                            GeckoView(context).apply {
                                                layoutParams = ViewGroup.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.MATCH_PARENT
                                                )

                                                setOnTouchListener { _, event ->
                                                    if (event.action == MotionEvent.ACTION_DOWN) {
                                                        // The user touched the web content
                                                        if (uiState.isUrlBarVisible) {
                                                            viewModel.updateUI {
                                                                it.copy(
                                                                    isUrlBarVisible = false
                                                                )
                                                            }
                                                        }
                                                        if (uiState.isMediaControlPanelVisible) viewModel.updateUI {
                                                            it.copy(
                                                                isMediaControlPanelVisible = false
                                                            )
                                                        }

                                                        if (viewModel.contextMenuData.value != null) viewModel.contextMenuData.value =
                                                            null
                                                        if (viewModel.choiceState.value != null) viewModel.choiceState.value =
                                                            null
                                                        if (viewModel.colorState.value != null) {
                                                            viewModel.colorState.value?.result?.complete(
                                                                viewModel.colorState.value?.prompt?.dismiss()
                                                            )

                                                            viewModel.colorState.value = null
                                                        }
                                                    }
                                                    false
                                                }
                                                geckoViewRef.value = this
                                            }
                                        },
                                        update = { geckoView ->
                                            // 4. THE SWITCH: Just swap the session!
                                            // When 'activeSession' changes, this block runs automatically.
                                            // GeckoView handles detaching the old one and attaching the new one.
                                            geckoView.setSession(activeSession)
                                            geckoViewRef.value = geckoView
                                        }
                                    )
                                }

                                if (!isPipMode) LoadingIndicator()
                            }
                        }
                    }

                    val isControlsOnRight = remember { mutableStateOf(false) }
                    val isStatusAtTop = remember { mutableStateOf(false) }

                    if (!isPipMode) {


                        AnimatedVisibility(
                            visible = viewModel.dateTimeState.value != null,
                            enter = slideInVertically { (it * 1.5).toInt() },
                            exit = slideOutVertically { (it * 1.5).toInt() },
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.ime)
                                .align(Alignment.BottomCenter)
                                .padding(bottom = settings.padding.dp)
                                .padding(bottom = floatingPanelBottomPadding)

                        ) {
                            DateTimePickerPanel()
                        }

                        AnimatedVisibility(
                            visible = viewModel.choiceState.value != null,
                            enter = slideInVertically { (it * 1.5).toInt() },
                            exit = slideOutVertically { (it * 1.5).toInt() },
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.ime)
                                .align(Alignment.BottomCenter)
                                .padding(bottom = settings.padding.dp)
                                .padding(bottom = floatingPanelBottomPadding)

                        ) {
                            ChoicePanel()
                        }

                        AnimatedVisibility(
                            visible = viewModel.colorState.value != null,
                            enter = slideInVertically { (it * 1.5).toInt() },
                            exit = slideOutVertically { (it * 1.5).toInt() },
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.ime)
                                .align(Alignment.BottomCenter)
                                .padding(bottom = settings.padding.dp)
                                .padding(bottom = floatingPanelBottomPadding)
                        ) {
                            ColorPickerPanel()

                        }

                        val controlOption = remember { mutableStateOf(MediaControlOption.TIME) }
                        val pendingSeekSeconds = remember { mutableDoubleStateOf(0.0) }
                        val interactionTrigger = remember { mutableIntStateOf(0) }

                        LaunchedEffect(pendingSeekSeconds.doubleValue) {
                            Log.i(
                                "pendingSeekSeconds",
                                "pendingSeekSeconds: ${pendingSeekSeconds.doubleValue}"
                            )
                        }
                        VideoStatusPanel(
                            modifier = Modifier
                                .align(
                                    when {
                                        isStatusAtTop.value && isControlsOnRight.value -> Alignment.TopStart
                                        isStatusAtTop.value && !isControlsOnRight.value -> Alignment.TopEnd
                                        !isStatusAtTop.value && isControlsOnRight.value -> Alignment.BottomStart
                                        !isStatusAtTop.value && !isControlsOnRight.value -> Alignment.BottomEnd
                                        else -> Alignment.TopStart
                                    }
                                )
                                .padding(webViewPaddingValue), // Apply same inset padding as WebView
                            gestureManager = gestureManager,
                            controlOption = controlOption,
                            pendingSeekSeconds = pendingSeekSeconds,
                            interactionTrigger = interactionTrigger,
                            onSwapLayout = { isControlsOnRight.value = !isControlsOnRight.value },
                            isStatusAtTop = isStatusAtTop,
                        )

                        MediaControlPanel(
                            modifier = Modifier
                                .align(if (isControlsOnRight.value) Alignment.CenterEnd else Alignment.CenterStart)
                                .padding(webViewPaddingValue),
                            hapticFeedback = hapticFeedback,
                            onExitFullscreen = {
                                activeSession.exitFullScreen()
                            },
                            gestureManager = gestureManager,
                            controlOption = controlOption,
                            pendingSeekSeconds = pendingSeekSeconds,
                            interactionTrigger = interactionTrigger,
                        )
                        CursorPointer(
                            isCursorPadVisible = uiState.isCursorPadVisible,
                            position = viewModel.cursorPointerPosition.value,
                        )


                        // 3. The Error Overlay (NEW)
                        AnimatedContent(
                            targetState = viewModel.activeTab!!.errorState,
                            transitionSpec = {
                                if (targetState != null) {
                                    // Error Appears: Slide In from Bottom
                                    slideInVertically(
                                        animationSpec = tween(
                                            settings.animationSpeedForLayer(
                                                0
                                            ) * 4
                                        ),
                                        initialOffsetY = { it }
                                    ) togetherWith ExitTransition.None
                                } else {
                                    (EnterTransition.None togetherWith slideOutVertically(
                                        animationSpec = tween(
                                            settings.animationSpeedForLayer(
                                                0
                                            ) * 4
                                        ),
                                        targetOffsetY = { -it }
                                    )).using(
                                        // FIX IS HERE:
                                        // 1. clip = false: Lets the error screen draw outside the container as it shrinks
                                        // 2. sizeAnimationSpec: Matches the duration so the container doesn't vanish too fast
                                        SizeTransform(clip = false) { _, _ ->
                                            tween(settings.animationSpeedForLayer(0) * 4)
                                        }
                                    )
                                }
                            },
                            label = "ErrorAnimation"
                        ) { targetState ->
                            // CRITICAL: Check 'targetState', NOT 'errorState'
                            // targetState is the "captured" value for this specific animation frame
                            if (targetState != null) {
                                ErrorScreen(
                                    modifier = Modifier
                                        .padding(webViewPaddingValue),
                                    errorState = targetState, // Safe, no !! needed
                                    onRetry = {
                                        val urlToReload = targetState.failingUrl
                                        webViewLoad(activeSession, urlToReload, settings)
                                    },
                                    onHome = {
                                        webViewLoad(
                                            activeSession,
                                            settings.defaultUrl,
                                            settings
                                        )
                                    }
                                )
                            }
                        }
                        BottomPanel(
                            modifier = Modifier
                                .padding(
                                    PaddingValues(
                                        start = webViewStartPadding,
                                        end = webViewEndPadding,
                                        bottom = 0.dp,
                                        top = 0.dp
                                    )
                                )
                                .windowInsetsPadding(WindowInsets.ime)
                                .align(Alignment.BottomCenter),

                            geckoViewRef = geckoViewRef,
                            floatingPanelBottomPadding = floatingPanelBottomPadding,
                            optionsPanelHeightPx = optionsPanelHeightPx,
                            draggableState = draggableState,
                            flingBehavior = flingBehavior,

                            bottomPanelPagerState = bottomPanelPagerState,
                            onDownload = { url ->
                                // Simple generic download for images found via context menu
                                confirmationPopup(
                                    message = "download file on",
                                    url = url,
                                    onConfirm = {
                                        startDownload(
                                            url,
                                            activeSession.settings.userAgentOverride ?: "",
                                            null,
                                            null,
                                        )
                                    },
                                    onCancel = {
                                    }
                                )


                            },
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
                            onSuggestionClick = { suggestion ->
                                webViewLoad(activeSession, suggestion.url, settings)
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            },
                            onRemoveSuggestion = { suggestionToRemove ->
                                confirmationPopup(
                                    message = "remove suggestion from history ? ",
                                    onConfirm = {
                                        viewModel.removeSuggestionFromHistory(suggestionToRemove)
                                    },
                                    onCancel = {}
                                )
                            },
                            activeSession = activeSession,
                            confirmationPopup = ::confirmationPopup,
                            urlBarFocusRequester = urlBarFocusRequester,

                            updateInspectingTab = { tab ->
                                if (tab.id != 0L) {
                                    viewModel.updateUI { it.copy(inspectingTabId = tab.id) }
                                } else {
                                    viewModel.updateUI { it.copy(isTabDataPanelVisible = false) }
                                }

                            },
                            isTabDataPanelVisible = uiState.isTabDataPanelVisible,
                            handleCloseInspectedTab = handleCloseInspectedTab,
                            handleClearInspectedTabData = handleClearInspectedTabData,
                            handlePermissionToggle = handlePermissionToggle,


                            onTabLongPressed = { tab ->
                                viewModel.updateUI { it.copy(isTabDataPanelVisible = !it.isTabDataPanelVisible) }
                                if (uiState.inspectingTabId == null) viewModel.updateUI {
                                    it.copy(
                                        inspectingTabId = tab.id
                                    )
                                }
                            },
                            onDownloadRowClicked = handleOpenFile,
                            onOpenFolderClicked = handleOpenDownloadsFolder,


                            isTabsPanelVisible = uiState.isTabsPanelVisible,

                            navigateWebView = {
                                navigateWebView()
                            },
                            hapticFeedback = hapticFeedback,
                            setActiveNavAction = { viewModel.activeNavAction.value = it },

                            state = if (viewModel.jsDialogState.value != null) viewModel.jsDialogState.value!! else null,
                            onDismiss = { viewModel.jsDialogState.value = null },

                            permissionLauncher = permissionLauncher,

                            focusManager = focusManager,
                            keyboardController = keyboardController,
                            setIsOptionsPanelVisible = setIsOptionsPanelVisible,
                            onNewUrl = { newUrl ->
                                webViewLoad(activeSession, newUrl, settings)
                            },

                            )
                        CursorPad(
                            urlBarFocusRequester = urlBarFocusRequester,
                            coroutineScope = coroutineScope,
                            activeSession = activeSession,
                            webViewPaddingValue = webViewPaddingValue,
                            cursorPadHeight = cursorPadHeight,
                        )

                        // BackSquare
                        AnimatedVisibility(
                            visible = !uiState.isBottomPanelVisible && !uiState.isLandscape && !uiState.isOtherPanelVisible,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(webViewPaddingValue)
                                .onSizeChanged {
                                    viewModel.screenSize.value = it
                                    with(density) {
                                        viewModel.screenSizeDp.value = IntSize(
                                            it.width.toDp().value.roundToInt(),
                                            it.height.toDp().value.roundToInt()
                                        )
                                    }
                                },
                            enter = fadeIn(tween(settings.animationSpeedForLayer(0))),
                            exit = fadeOut(tween(settings.animationSpeedForLayer(0)))
                        ) {


                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                            ) {
                                val squareBoxSize = settings.heightForLayer(1).dp
                                val squareBoxSizePx = with(density) { squareBoxSize.toPx() }
                                val paddingPx =
                                    with(density) { settings.padding.dp.toPx() }

                                Box(
                                    modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                backSquareOffsetX.value.roundToInt(),
                                                backSquareOffsetY.value.roundToInt()
                                            )
                                        }

                                        .animateContentSize(
                                            tween(
                                                settings.animationSpeedForLayer(1)
                                            )
                                        )
                                        .size(squareBoxSize)
                                        .graphicsLayer {
                                            alpha = squareAlpha.value
                                        }
                                        .pointerInput(Unit, uiState.isCursorMode) {

                                            if (!uiState.isCursorMode) awaitEachGesture {
                                                val down = awaitFirstDown(requireUnconsumed = false)
                                                coroutineScope.launch {
                                                    squareAlpha.animateTo(1f)
                                                }

                                                val longPressJob = coroutineScope.launch {
                                                    delay(viewConfiguration.longPressTimeoutMillis)
                                                    hapticFeedback.performHapticFeedback(
                                                        HapticFeedbackType.LongPress
                                                    )
                                                    viewModel.updateUI { it.copy(isCursorPadVisible = true) }
                                                    squareAlpha.snapTo(0f)


                                                    val initialCursorX =
                                                        backSquareOffsetX.value + settings.padding + down.position.x

                                                    val initialCursorY =
                                                        ((viewModel.screenSize.value.height - cutoutTop.toPx()) / 2) - (viewModel.screenSize.value.height - backSquareOffsetY.value) + down.position.y + cutoutTop.toPx()


                                                    viewModel.cursorPointerPosition.value =
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
                                                                (change.position.x - change.previousPosition.x) * settings.cursorTrackingSpeed
                                                            val changeSpaceY =
                                                                (change.position.y - change.previousPosition.y) * settings.cursorTrackingSpeed

                                                            //                                                val newCursorX =
                                                            //                                                    viewModel.cursorPointerPosition.value.x + changeSpaceX
                                                            //
                                                            //                                                val newCursorY =
                                                            //                                                    (viewModel.cursorPointerPosition.value.y + changeSpaceY)
                                                            //                                                viewModel.cursorPointerPosition.value =
                                                            //                                                    Offset(newCursorX, newCursorY)
                                                            var newX =
                                                                viewModel.cursorPointerPosition.value.x + changeSpaceX
                                                            var newY =
                                                                viewModel.cursorPointerPosition.value.y + changeSpaceY
                                                            if (newX < 0) newX = 0f
                                                            if (newX > viewModel.screenSize.value.width) newX =
                                                                viewModel.screenSize.value.width.toFloat()
                                                            if (newY < 0) newY = 0f
                                                            if (newY > viewModel.screenSize.value.height) newY =
                                                                viewModel.screenSize.value.height.toFloat()
                                                            viewModel.cursorPointerPosition.value =
                                                                Offset(newX, newY)

                                                        }
                                                    }
                                                    // This code now ONLY runs after a long-press-drag has finished.
                                                    viewModel.updateUI { it.copy(isCursorPadVisible = false) }

                                                    // --- SIMULATE CLICK AT CURSOR POSITION ---


                                                    activeSession.let { _ ->
                                                        val downTime = System.currentTimeMillis()
                                                        val downEvent = MotionEvent.obtain(
                                                            downTime,
                                                            downTime,
                                                            MotionEvent.ACTION_DOWN,
                                                            viewModel.cursorPointerPosition.value.x,
                                                            //                                                    viewModel.cursorPointerPosition.value.y - webViewTopPadding.toPx(),
                                                            viewModel.cursorPointerPosition.value.y - innerPadding.calculateTopPadding()
                                                                .toPx(),
                                                            0
                                                        )
                                                        val upEvent = MotionEvent.obtain(
                                                            downTime,
                                                            downTime + 10,
                                                            MotionEvent.ACTION_UP,
                                                            viewModel.cursorPointerPosition.value.x,
                                                            viewModel.cursorPointerPosition.value.y - innerPadding.calculateTopPadding()
                                                                .toPx(),
                                                            0
                                                        )

                                                        activeSession.panZoomController.onTouchEvent(
                                                            downEvent
                                                        )
                                                        activeSession.panZoomController.onTouchEvent(
                                                            upEvent
                                                        )
                                                        downEvent.recycle()
                                                        upEvent.recycle()
                                                        //                                                webView.dispatchTouchEvent(downEvent)
                                                        //                                                webView.dispatchTouchEvent(upEvent)
                                                    }

                                                    coroutineScope.launch {
                                                        squareAlpha.animateTo(settings.backSquareIdleOpacity)
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
                                                        }

                                                        // snap logic
                                                        val screenWidth = viewModel.screenSize.value.width.toFloat()
                                                        val currentX = backSquareOffsetX.value

                                                        // snap back square to left or right side of the screen
                                                        val targetX =
                                                            if (currentX + (squareBoxSizePx / 2) < screenWidth / 2) {
                                                                paddingPx // Snap Left
                                                            } else {
                                                                screenWidth - squareBoxSizePx - paddingPx // Snap Right
                                                            }

                                                        // Clamp Y to screen bounds
                                                        val targetY =
                                                            backSquareOffsetY.value.coerceIn(
                                                                paddingPx,
                                                                viewModel.screenSize.value.height.toFloat() - squareBoxSizePx - paddingPx
                                                            )

                                                        coroutineScope.launch {
                                                            // Animate snap in

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
                                                            viewModel.updateSettings {
                                                                it.copy(
                                                                    backSquareOffsetX = targetX,
                                                                    backSquareOffsetY = targetY
                                                                )
                                                            }
                                                            // Fade out after snap
                                                            hideBackSquare(false)
                                                        }
                                                    } else {
                                                        // TAP
                                                        if (longPressJob.isActive) {
                                                            longPressJob.cancel()
                                                            coroutineScope.launch {
                                                                geckoViewRef.value?.clearFocus()
                                                                viewModel.updateUI {
                                                                    it.copy(
                                                                        isUrlBarVisible = true
                                                                    )
                                                                }
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
                                                    }
                                                } else {
                                                    // TAP
                                                    coroutineScope.launch {
                                                        viewModel.updateUI { it.copy(isUrlBarVisible = true) }
                                                    }
                                                }
                                            }
                                        }

                                        .clip(
                                            RoundedCornerShape(
                                                settings.cornerRadiusForLayer(1).dp
                                            )
                                        )
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .background(Color.White.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                }
                            }
                        }
                    }
                }
            }
        }
        //endregion
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
