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
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import marcinlowercase.a.core.enum_class.DownloadStatus
import marcinlowercase.a.core.enum_class.GestureNavAction
import marcinlowercase.a.core.enum_class.MediaControlOption
import marcinlowercase.a.core.enum_class.RevealState
import marcinlowercase.a.core.function.createNotificationChannel
import marcinlowercase.a.core.function.rememberAnchoredDraggableState
import marcinlowercase.a.core.function.toDomain
import marcinlowercase.a.core.function.webViewLoad
import marcinlowercase.a.core.manager.MediaGestureManager
import marcinlowercase.a.core.service.ShakeDetector
import marcinlowercase.a.ui.component.CursorPad
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
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.StorageController
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.system.exitProcess
import androidx.compose.ui.res.stringResource


//region Composable

class MainActivity : ComponentActivity() {

    companion object {
        // Keeps track of the ONE allowed Full Browser window globally
        var currentFullBrowserInstance: java.lang.ref.WeakReference<MainActivity>? = null
    }
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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        window.isNavigationBarContrastEnforced = false


        super.onCreate(savedInstanceState)
        val isPwa = intent?.getBooleanExtra("is_pwa", false) == true
        val isClonedBrowser = intent?.getBooleanExtra("is_cloned_browser", false) == true

        if (!isTaskRoot && !isPwa && !isClonedBrowser) {
            // We are trapped inside another app's task!
            // Clone the intent, add the NEW_TASK flag, and fire it to our Main Browser task!
            val bounceIntent = Intent(intent).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(bounceIntent)

            // Immediately kill this ghost window so it disappears from Google Keep
            finish()
            return
        }

        handleIntent(intent, isColdStart = true)


        lifecycleScope.launch(Dispatchers.IO) {
            cacheDir.listFiles()?.forEach { it.delete() }
        }
//        GeckoRuntime.getDefault(applicationContext)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        createNotificationChannel(this)

        val startupViewModel: BrowserViewModel by viewModels()
        if (startupViewModel.browserSettings.value.isFullscreenMode) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            Theme {

                val viewModel: BrowserViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BrowserScreen(
                        innerPadding = innerPadding,
                        modifier = Modifier,
                        newUrlFlow = newUrlFromIntent,
                        initialIntentUrl = if (viewModel.isStandaloneMode.value) null else intent?.dataString,
                        viewModel = viewModel
                    )
                }
            }
        }

    }

    override fun onStop() {
        super.onStop()
        val viewModel: BrowserViewModel by viewModels()
        if (isInPictureInPictureMode
            || isPipMode || isEnteringPip
        ) return
        if (!viewModel.isStandaloneMode.value) {
            viewModel.tabManager.freezeAllTabs(viewModel.activeProfileId.value)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up the Highlander reference if this instance is destroyed natively
        if (currentFullBrowserInstance?.get() == this) {
            currentFullBrowserInstance = null
        }
    }


    //region Intent
    //region Intent
//region Intent
//region Intent
    private fun handleIntent(intent: Intent?, isColdStart: Boolean = false) {
        val viewModel: BrowserViewModel by viewModels()

        if (intent?.action == "marcinlowercase.a.WEB_NOTIFICATION_CLICK") {
            try {
                val webNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("web_notification", org.mozilla.geckoview.WebNotification::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<org.mozilla.geckoview.WebNotification>("web_notification")
                }

                // This natively triggers the JavaScript `notification.onclick` event!
                webNotification?.click()
            } catch (e: Exception) {
                Log.e("GeckoExt", "Failed to click WebNotification", e)
            }
            // It's safe to return early if the intent was purely a notification click
            return
        }

        if (intent?.action == Intent.ACTION_VIEW) {
            intent.dataString?.let { urlFromIntent ->

                val isPwa = intent.getBooleanExtra("is_pwa", false)
                val targetProfileId = intent.getStringExtra("profileId")

                // Activate Standalone Mode if launched from the WebAPK
                viewModel.isStandaloneMode.value = isPwa

                if (isPwa) {
                    viewModel.initPwa(urlFromIntent, targetProfileId ?: viewModel.activeProfileId.value)
                } else {
                    // Normal Browser Mode: Rely on TabManager for cold starts, Flow for hot starts
                    if (!isColdStart) {
                        newUrlFromIntent.update { urlFromIntent }
                    }
                }
            }
        } else {
            viewModel.isStandaloneMode.value = false
        }

        if (!viewModel.isStandaloneMode.value) {
            val oldInstance = currentFullBrowserInstance?.get()
            if (oldInstance != null && oldInstance != this@MainActivity) {
                Log.i("MultiWindow", "Killing old full browser instance to prevent state collision!")
                try {
                    val caller = oldInstance.callingActivity
                    Log.i("marcCloned", "caller ${caller?.packageName}")
                    if (caller != null) {
                        // 1. It is a Cloned Browser sitting on a Shell App!
                        // We shoot a "Kill" Intent down to the exact Shell App that spawned it.
                        // CLEAR_TOP instantly incinerates the Cloned Browser sitting above it.
                        // SINGLE_TOP wakes up the Shell App so it can call finishAndRemoveTask().
                        val killIntent = Intent()

                        killIntent.component = caller
                        killIntent.putExtra("kill_shell", true)
                        killIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

                        // Fire the missile from the current foreground window!
                        startActivity(killIntent)
                    } else {
                        // 2. It is the Original Browser. It owns its own task.
                        // We can safely obliterate it directly!
                        oldInstance.finishAndRemoveTask()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // Crown this window as the new active Full Browser
            currentFullBrowserInstance = java.lang.ref.WeakReference(this@MainActivity)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Hot start!
        setIntent(intent)
        handleIntent(intent, isColdStart = false)
    }
    //endregion

    //region Pip
    // currently not working for YouTube yet, other platform work fine
    fun updatePipParams(isDataFullscreen: Boolean) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            isEnteringPip = true
//            val params = android.app.PictureInPictureParams.Builder()
//                // If video is fullscreen, allow Auto-Enter (Swipe up to PiP)
//                .setAutoEnterEnabled(isDataFullscreen)
//                .setAspectRatio(android.util.Rational(16, 9)) // Default to 16:9
//                .build()
//            setPictureInPictureParams(params)
//        }
    }

    var isCurrentlyFullscreen by mutableStateOf(false)
    var isEnteringPip by mutableStateOf(false)

    private fun hideKeyboardAndClearFocus() {
        try {
            // 1. Hide the keyboard at the window level
            val view = window.decorView
            val imm =
                getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            // 2. Clear focus from the current view (GeckoView)
            currentFocus?.clearFocus()

            // 3. Give focus to the root layout so GeckoView stops trying to talk to the IME
            view.clearFocus()
        } catch (e: Exception) {
        }
    }

    override fun onUserLeaveHint() {

        hideKeyboardAndClearFocus()

//        if (isCurrentlyFullscreen) {
//
//            isEnteringPip = true
//
//
//            // Only call enterPip() manually on older Androids.
//            // Android 12+ handles it automatically via updatePipParams/setAutoEnterEnabled.
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//                enterPip()
//            }
//        }

        super.onUserLeaveHint()
    }

//    private fun enterPip() {
//
//        // Use 16:9 as a standard fallback since the delegate is missing
//        val params = android.app.PictureInPictureParams.Builder()
//            .setAspectRatio(android.util.Rational(16, 9))
//            .build()
//        enterPictureInPictureMode(params)
//    }

    var isPipMode by mutableStateOf(false)

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {

        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipMode = isInPictureInPictureMode
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

        hideKeyboardAndClearFocus()

        if (isCurrentlyFullscreen) isEnteringPip = true
        val viewModel: BrowserViewModel by viewModels()

        // If entering PiP, we MUST keep the session active and prevent Gecko from
        // interpreting this as a background event that stops media.
//        if (isInPictureInPictureMode || isEnteringPip) {
//            viewModel.tabManager.loadTabs(viewModel.activeProfileId.value, null)
//            val index = viewModel.tabManager.getActiveTabIndex(viewModel.activeProfileId.value)
//            if (viewModel.tabs.isNotEmpty() && index in viewModel.tabs.indices) {
//                val activeTab = viewModel.tabs[index]
//                // Force the session to remain active
//                viewModel.geckoManager.getSession(activeTab).setActive(true)
//
//            }
//        }

        if (isInPictureInPictureMode || isEnteringPip) {
            viewModel.activeTab?.let {
                // Safely keep the current tab (Browser or PWA) fully awake!
                viewModel.geckoManager.getSession(it).setActive(true)
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
    val uiState = viewModel.uiState.collectAsState()

    viewModel.initializeTabs(initialIntentUrl)
    val activeTabIndex by viewModel.activeTabIndex.collectAsState()

    //endregion
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current


    val textFieldState = rememberTextFieldState(viewModel.activeTab!!.currentURL)

    val activeSession =
        remember(viewModel.activeTab!!.id, viewModel.sessionRefreshTrigger.intValue) {
            viewModel.geckoManager.getSession(viewModel.activeTab!!, settings.isDesktopMode)
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

    val isEffectivelyFullscreen = settings.isFullscreenMode || uiState.value.isOnFullscreenVideo || uiState.value.isLandscapeByButton

    // Top Padding

    val webViewTopPaddingFullscreen = if (settings.isSharpMode && !uiState.value.isLandscape) {
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

    val webViewTopPaddingNormalScreen = if (isEffectivelyFullscreen) {
        webViewTopPaddingFullscreen
    } else {
        webViewTopPaddingRegular
    }

    val targetWebViewTopPadding =
        if (uiState.value.isSettingCornerRadius
            || isPipMode
        ) 0.dp else webViewTopPaddingNormalScreen

    val webViewTopPadding by animateDpAsState(
        targetValue = targetWebViewTopPadding,
        animationSpec = paddingAnimationSpec,
        label = "WebView Top Padding Animation",
    )
    // Bottom Padding
    val webViewBottomPaddingFullscreen =
        if (settings.isSharpMode && !uiState.value.isLandscape) {
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
    val webViewBottomPaddingNormalScreen = if (isEffectivelyFullscreen) {
        webViewBottomPaddingFullscreen
    } else {
        webViewBottomPaddingRegular
    }

    val targetWebViewBottomPadding =
        if (uiState.value.isSettingCornerRadius
            || isPipMode
        ) {
            0.dp
        } else if (isKeyboardVisible && !uiState.value.isFocusOnTextField) {
            settings.padding.dp
        } else webViewBottomPaddingNormalScreen


    val webViewBottomPadding by animateDpAsState(
        targetValue = targetWebViewBottomPadding,
        animationSpec = paddingAnimationSpec,
        label = "WebView Bottom Padding Animation",
    )

    // Start Padding

    val webViewStartPaddingFullscreen =
        if (settings.isSharpMode && uiState.value.isLandscape) {
            maxOf(cutoutLeft, settings.deviceCornerRadius.dp)
        } else {
            cutoutLeft
        }
    val targetWebViewStartPadding =
        if (uiState.value.isSettingCornerRadius
            || isPipMode
        ) 0.dp else webViewStartPaddingFullscreen
    val webViewStartPadding by animateDpAsState(
        targetValue = targetWebViewStartPadding,
        animationSpec = paddingAnimationSpec,
        label = "WebView Start Padding Animation"
    )

    // End Padding

    val webViewEndPaddingFullscreen = if (settings.isSharpMode && uiState.value.isLandscape) {
        maxOf(cutoutRight, settings.deviceCornerRadius.dp)
    } else {
        cutoutRight
    }
    val targetWebViewEndPadding =
        if (uiState.value.isSettingCornerRadius
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
    val floatingPanelBottomPaddingNoKeyboard = if (isEffectivelyFullscreen) {
        webViewBottomPaddingFullscreen
    } else {
        webViewBottomPaddingRegular
    }
    val floatingPanelBottomPadding by animateDpAsState(
        targetValue = if (isKeyboardVisible) (
                if (!uiState.value.isFocusOnTextField) settings.padding.dp else 0.dp
                ) else (floatingPanelBottomPaddingNoKeyboard),
        animationSpec = tween(settings.animationSpeedForLayer(1)),
        label = "Floating Panel Padding Animation"
    )
    //endregion


    //region Permissions Handle

    val pendingWebAuthnResult = remember { mutableStateOf<GeckoResult<Intent>?>(null) }
    val webAuthnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                // Pass the FIDO payload back to GeckoView
                pendingWebAuthnResult.value?.complete(result.data)
            } else {
                // Fails the Javascript Promise if the user cancels the FIDO prompt
                pendingWebAuthnResult.value?.completeExceptionally(Exception("WebAuthn canceled or failed"))
            }
            pendingWebAuthnResult.value = null
        }
    )

    // Attach the ActivityDelegate to your GeckoRuntime so it can launch the FIDO Intents
    LaunchedEffect(viewModel.geckoManager.runtime) {
        viewModel.geckoManager.runtime.setActivityDelegate(object : org.mozilla.geckoview.GeckoRuntime.ActivityDelegate {
            override fun onStartActivityForResult(intent: android.app.PendingIntent): GeckoResult<Intent>? {
                val geckoResult = GeckoResult<Intent>()
                pendingWebAuthnResult.value = geckoResult

                try {
                    // Start the Google Play Services FIDO dialog
                    val request = androidx.activity.result.IntentSenderRequest.Builder(intent.intentSender).build()
                    webAuthnLauncher.launch(request)
                } catch (e: Exception) {
                    geckoResult.completeExceptionally(e)
                    pendingWebAuthnResult.value = null
                }

                return geckoResult
            }
        })
    }

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
    LaunchedEffect(initialX, initialY) {
        if (settings.backSquareOffsetX != initialX || settings.backSquareOffsetY != initialY) {
            viewModel.updateSettings {
                it.copy(
                    backSquareOffsetX = initialX,
                    backSquareOffsetY = initialY
                )
            }
        }
    }

    val geckoViewRef = remember { mutableStateOf<GeckoView?>(null) }

    val insetsController = activity.let {
        WindowCompat.getInsetsController(
            it.window,
            it.window.decorView
        )
    }


    //region OptionsPanel Drag State
    val optionsPanelHeight = (settings.heightForLayer(2) + settings.padding).dp

    val appsPanelHeight =
        (settings.heightForLayer(3).dp * settings.maxListHeight) + (settings.padding.dp * 2) + (if (ceil(
                settings.maxListHeight
            ).toInt() > 1
        ) settings.padding.dp else 0.dp)

    val totalRevealHeight = optionsPanelHeight + settings.padding.dp + appsPanelHeight
    viewModel.updateUI {
        it.copy(
            optionsPanelHeightPx = with(density) { (optionsPanelHeight).toPx() },
            appsPanelHeightPx = with(density) { appsPanelHeight.toPx() },
            totalRevealHeightPx = with(density) { totalRevealHeight.toPx() }
        )
    }

    // define anchors for options panel
    val anchors = remember(uiState.value.optionsPanelHeightPx, uiState.value.totalRevealHeightPx) {
        DraggableAnchors {
            RevealState.Hidden at 0f
            RevealState.Visible at -uiState.value.optionsPanelHeightPx
            RevealState.Expanded at -uiState.value.totalRevealHeightPx
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

    LaunchedEffect(draggableState.targetValue) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove) // Or HapticFeedbackType.LongPress
    }

//    LaunchedEffect(draggableState.settledValue) {
//        val isVisible = draggableState.settledValue == RevealState.Visible
//
//        if (uiState.value.isOptionsPanelVisible != isVisible) {
//            viewModel.updateUI { it.copy(isOptionsPanelVisible = isVisible) }
//        }
//    }
//
//

    LaunchedEffect(draggableState.settledValue) {
        val isOptionsVisible =
            draggableState.settledValue == RevealState.Visible || draggableState.settledValue == RevealState.Expanded
        val isAppsVisible = draggableState.settledValue == RevealState.Expanded

        if (uiState.value.isOptionsPanelVisible != isOptionsVisible || uiState.value.isAppsPanelVisible != isAppsVisible) {
            viewModel.updateUI {
                it.copy(
                    isOptionsPanelVisible = isOptionsVisible,
                    isAppsPanelVisible = isAppsVisible
                )
            }
        }
    }
    //endregion


    fun confirmationPopup(
        message: Int,
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


//    val setIsOptionsPanelVisible = { setToVisible: Boolean ->
//
//        coroutineScope.launch {
//            if (setToVisible) {
//                draggableState.animateTo(
//                    targetValue = RevealState.Visible,
//                    animationSpec = tween(
//                        durationMillis = settings.animationSpeedForLayer(1),
//                    )
//                )
//            } else {
//                draggableState.animateTo(
//                    targetValue = RevealState.Hidden,
//                    animationSpec = tween(
//                        durationMillis = settings.animationSpeedForLayer(1),
//                    )
//                )
//            }
//            delay((settings.animationSpeedForLayer(1) * 2).toLong())
//
//        }
//
//    }
//    LaunchedEffect(uiState.value.isOptionsPanelVisible) {
//        setIsOptionsPanelVisible(uiState.value.isOptionsPanelVisible)
//    }
// This effect acts as the "Engine" that moves the panel whenever the ViewModel state changes programmatically
    // (e.g., clicking a button to open Apps Panel, or URL bar forcing the panel to close).
    LaunchedEffect(uiState.value.isOptionsPanelVisible, uiState.value.isAppsPanelVisible) {
        val targetState = when {
            uiState.value.isAppsPanelVisible -> RevealState.Expanded
            uiState.value.isOptionsPanelVisible -> RevealState.Visible
            else -> RevealState.Hidden
        }

        // Only animate if the panel is not already at the target destination
        if (draggableState.currentValue != targetState) {
            coroutineScope.launch {
                draggableState.animateTo(
                    targetValue = targetState,
                    animationSpec = tween(
                        durationMillis = settings.animationSpeedForLayer(1),
                    )
                )
            }
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
                message = R.string.confirm_close_tab,
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
        viewModel.tabManager.clearAllTabs(viewModel.activeProfileId.value)

        // close the application
        activity.finishAndRemoveTask()
        exitProcess(0)
    }
    val handleClearInspectedTabData = {
        confirmationPopup(
            message = R.string.confirm_clear_site_data,
            onConfirm = {
                val inspectingTab = viewModel.currentInspectingTab

                if (inspectingTab != null) {
                    val domain = viewModel.siteSettingsManager.getDomain(inspectingTab.currentURL)

                    if (domain != null) {
                        viewModel.clearDomainData(domain)
                        val runtime = viewModel.geckoManager.runtime
                        val flags = StorageController.ClearFlags.ALL
                        runtime.storageController.clearDataFromHost(domain, flags).then {
                            runOnUiThread {
                                // loop through ALL viewModel.tabs to find matches
                                viewModel.tabs.forEachIndexed { _, tab ->
                                    val tabDomain =
                                        viewModel.siteSettingsManager.getDomain(tab.currentURL)

                                    // check if this tab belongs to the domain just cleared
                                    if (tabDomain == domain) {
                                        viewModel.updateTabById(tab.id) { t ->
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
    val startDownload =
        { url: String, userAgent: String, contentDisposition: String?, mimeType: String?, stream: java.io.InputStream? ->
            if (url.startsWith("blob:") || url.startsWith("data:")) {
                if (stream != null) {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            // 1. Let Android automatically extract the name AND append the correct extension!
                            var fileName = android.webkit.URLUtil.guessFileName(
                                url,
                                contentDisposition,
                                mimeType
                            )

                            // 2. If it couldn't find a name and defaulted to "downloadfile.bin", make it unique
                            if (fileName.startsWith("downloadfile")) {
                                val ext = android.webkit.MimeTypeMap.getSingleton()
                                    .getExtensionFromMimeType(mimeType) ?: "bin"
                                fileName = "download_${System.currentTimeMillis()}.$ext"
                            }

                            // 3. Write directly to the Downloads folder
                            var fileSize = 0L
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val resolver = context.contentResolver
                                val contentValues = android.content.ContentValues().apply {
                                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                                    put(
                                        MediaStore.Downloads.MIME_TYPE,
                                        mimeType ?: "application/octet-stream"
                                    )
                                    put(
                                        MediaStore.Downloads.RELATIVE_PATH,
                                        Environment.DIRECTORY_DOWNLOADS
                                    )
                                }
                                val uri = resolver.insert(
                                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                    contentValues
                                )
                                if (uri != null) {
                                    resolver.openOutputStream(uri)?.use { output ->
                                        stream.use { input -> fileSize = input.copyTo(output) }
                                    }
                                }
                            } else {
                                val downloadsDir =
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                                val file = File(downloadsDir, fileName)
                                FileOutputStream(file).use { output ->
                                    stream.use { input -> fileSize = input.copyTo(output) }
                                }
                            }

                            // 4. Finished! Now update the ViewModel and pull up the Download Panel
                            withContext(Dispatchers.Main) {
                                val downloadId = System.currentTimeMillis()
                                val completedItem = DownloadItem(
                                    id = downloadId,
                                    url = url,
                                    filename = fileName, // <-- Now guarantees "hima.gif"
                                    mimeType = mimeType ?: "application/octet-stream",
                                    status = DownloadStatus.SUCCESSFUL,
                                    progress = 100,
                                    totalBytes = fileSize,
                                    downloadedBytes = fileSize,
                                    isBlobDownload = true
                                )

                                // Add to list and persist
                                viewModel.downloads.add(0, completedItem)
                                viewModel.downloadTracker.saveDownloads(viewModel.downloads)

                                // Open the Download Panel
                                viewModel.updateUI {
                                    it.copy(
                                        isUrlBarVisible = true,
                                        isDownloadPanelVisible = true,
                                        isTabsPanelVisible = false,
                                        isTabsPanelLock = false,
                                        isSettingsPanelVisible = false,
                                        isAppsPanelVisible = false,
                                        isFindInPageVisible = false,
                                        isNavPanelVisible = false,
                                        savedPanelState = null
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Blob download failed", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } finally {
                            try {
                                stream.close()
                            } catch (e: Exception) {
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Error: Data stream is empty", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                // Standard HTTP/HTTPS download using Android's DownloadManager
                val params = DownloadParams(url, userAgent, contentDisposition, mimeType)
                val needsPermission = false
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

                if (needsPermission && !hasPermission) {
                    viewModel.pendingDownload = params
                    storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    viewModel.performDownloadEnqueue(params)
                }
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
                if (uiState.value.isLoading) viewModel.updateUI { it.copy(isLoading = false) }
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


    LaunchedEffect(viewModel.suggestions) {
        if (viewModel.suggestions.isNotEmpty()) activeMainPanel = ActivePanel.SUGGESTIONS
    }
    LaunchedEffect(uiState.value.isSyncPanelVisible) {
        if (uiState.value.isSyncPanelVisible) activeMainPanel = ActivePanel.SYNC
    }

    LaunchedEffect(uiState.value.isDownloadPanelVisible) {
        if (uiState.value.isDownloadPanelVisible) activeMainPanel = ActivePanel.DOWNLOADS
    }
    LaunchedEffect(uiState.value.isAppsPanelVisible) {
        if (uiState.value.isAppsPanelVisible) activeMainPanel = ActivePanel.APPS
    }
    LaunchedEffect(viewModel.contextMenuData.value) {
        if (viewModel.contextMenuData.value != null) activeMainPanel = ActivePanel.CONTEXT_MENU
    }
    LaunchedEffect(uiState.value.isFindInPageVisible) {
        if (uiState.value.isFindInPageVisible) activeMainPanel = ActivePanel.FIND_IN_PAGE
    }
    LaunchedEffect(viewModel.jsDialogState.value) {
        if (viewModel.jsDialogState.value != null) activeMainPanel = ActivePanel.PROMPT
    }
    LaunchedEffect(uiState.value.isSettingsPanelVisible) {
        if (uiState.value.isSettingsPanelVisible) activeMainPanel = ActivePanel.SETTINGS
    }
    LaunchedEffect(viewModel.pendingPermissionRequest.value) {
        if (viewModel.pendingPermissionRequest.value != null) activeMainPanel =
            ActivePanel.PERMISSION
    }
    LaunchedEffect(uiState.value.isTabsPanelVisible) {
        // When TabsPanel opens, it becomes the main panel.
        if (uiState.value.isTabsPanelVisible) activeMainPanel = ActivePanel.TABS
    }
    LaunchedEffect(uiState.value.isTabDataPanelVisible) {
        // When TabDataPanel opens, it ensures Tabs is the main panel and forces it open.
        if (uiState.value.isTabDataPanelVisible) {
            activeMainPanel = ActivePanel.TABS
            viewModel.updateUI { it.copy(isTabsPanelVisible = true) }
        }
    }

    // 3. Enforcer for MAIN panels: When focus changes, close all other main panels.
    LaunchedEffect(activeMainPanel) {
        val current = activeMainPanel // Capture the current state

        if (current != ActivePanel.TABS && current != ActivePanel.APPS && uiState.value.isAppsPanelVisible) viewModel.updateUI {
            it.copy(
                isAppsPanelVisible = false
            )
        }
        if (current != ActivePanel.DOWNLOADS && uiState.value.isDownloadPanelVisible) viewModel.updateUI {
            it.copy(
                isDownloadPanelVisible = false
            )
        }
        if (current != ActivePanel.SYNC && uiState.value.isSyncPanelVisible) viewModel.updateUI {
            it.copy(
                isSyncPanelVisible = false
            )
        }

        if (current != ActivePanel.CONTEXT_MENU && viewModel.contextMenuData.value != null) viewModel.contextMenuData.value =
            null
        if (current != ActivePanel.FIND_IN_PAGE && uiState.value.isFindInPageVisible) viewModel.updateUI {
            it.copy(
                isFindInPageVisible = false
            )
        }
        if (current != ActivePanel.PROMPT && viewModel.jsDialogState.value != null) viewModel.jsDialogState.value =
            null
        if (current != ActivePanel.SETTINGS && uiState.value.isSettingsPanelVisible && !(current == ActivePanel.APPS && viewModel.isSortingButtons.value)) {
            viewModel.updateUI { it.copy(isSettingsPanelVisible = false) }
        }

        if (current != ActivePanel.PERMISSION && viewModel.pendingPermissionRequest.value != null) viewModel.pendingPermissionRequest.value =
            null

        if (current != ActivePanel.SUGGESTIONS && viewModel.suggestions.isNotEmpty()) viewModel.suggestions.clear()

        // If the active panel is NOT TABS, close both TABS and TAB_DATA.
        if (current != ActivePanel.APPS && current != ActivePanel.TABS) {
            if (uiState.value.isTabsPanelVisible) viewModel.updateUI { it.copy(isTabsPanelVisible = false) }

            if (uiState.value.isTabsPanelLock) viewModel.updateUI { it.copy(isTabsPanelLock = false) }

            if (uiState.value.isTabDataPanelVisible) viewModel.updateUI {
                it.copy(
                    isTabDataPanelVisible = false
                )
            }

        }
    }

    // 4. Exception Rule: If TabsPanel is closed, TabDataPanel must also close.
    // This enforces their parent-child relationship.
    LaunchedEffect(uiState.value.isTabsPanelVisible) {
        if (!uiState.value.isTabsPanelVisible && uiState.value.isTabDataPanelVisible) {
            viewModel.updateUI { it.copy(isTabDataPanelVisible = false) }
        }
    }
    //endregion


    CompositionLocalProvider(
        LocalBrowserViewModel provides viewModel
    ) {
        //region LaunchedEffect

        LaunchedEffect(viewModel.activeProfileId.value) {
            if (viewModel.inspectingOption.value != null || viewModel.isSortingButtons.value) {
                viewModel.isSortingButtons.value = false
                viewModel.inspectingOption.value = null
            }
        }
        LaunchedEffect(viewModel.isSortingButtons.value) {
            if (!viewModel.isSortingButtons.value) viewModel.updateUI { it.copy(isAppsPanelVisible = false) }
        }

        LaunchedEffect(settings.isDesktopMode, activeSession) {
            val targetUserAgent = if (settings.isDesktopMode) {
                GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
            } else {
                GeckoSessionSettings.USER_AGENT_MODE_MOBILE
            }
            val targetViewportMode = if (settings.isDesktopMode) {
                GeckoSessionSettings.VIEWPORT_MODE_DESKTOP
            } else {
                GeckoSessionSettings.VIEWPORT_MODE_MOBILE
            }

            // If the user agent mode differs from the current setting, update and reload
            if (activeSession.settings.userAgentMode != targetUserAgent || activeSession.settings.viewportMode != targetViewportMode) {
                activeSession.settings.userAgentMode = targetUserAgent
                activeSession.settings.viewportMode = targetViewportMode

                val currentUrl = viewModel.activeTab!!.currentURL
                if (currentUrl.isNotBlank() && currentUrl != "about:blank") {
                    // THE FIX: Use LOAD_FLAGS_REPLACE_HISTORY.
                    // This replaces the current page in the history stack with itself.
                    // It preserves the "Back" button history, but forces Gecko to discard the old Zoom level!
                    val loader = GeckoSession.Loader()
                        .uri(currentUrl)
                        .flags(GeckoSession.LOAD_FLAGS_REPLACE_HISTORY)

                    activeSession.load(loader)
                } else {
                    activeSession.reload()
                }
            }
        }

        LaunchedEffect(viewModel.activeTab?.id, viewModel.activeProfileId.value) {
            val currentUrl = viewModel.activeTab?.currentURL ?: ""
            Log.i("marcUrl", "current ${currentUrl}")
            if (!uiState.value.isFocusOnUrlTextField) {
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
        LaunchedEffect(uiState.value.isOtherPanelVisible) {
            if (uiState.value.isOtherPanelVisible) viewModel.updateUI { it.copy(isBottomPanelVisible = false) }
        }
        LaunchedEffect(
            uiState.value.isFocusOnSettingTextField,
            uiState.value.isFocusOnUrlTextField,
            uiState.value.isFocusOnFindTextField,
            uiState.value.isFocusOnProfileTextField,
            uiState.value.isFocusOnIconUrlTextField,
        ) {
            viewModel.updateUI {
                it.copy(
                    isFocusOnTextField = uiState.value.isFocusOnFindTextField || uiState.value.isFocusOnUrlTextField || uiState.value.isFocusOnSettingTextField || uiState.value.isFocusOnProfileTextField || uiState.value.isFocusOnIconUrlTextField
                )
            }
        }
        LaunchedEffect(
            uiState.value.isFocusOnUrlTextField,
            uiState.value.isTabsPanelVisible,
            uiState.value.isOptionsPanelVisible,
            uiState.value.isAppsPanelVisible,
            uiState.value.isDownloadPanelVisible,
            uiState.value.isSyncPanelVisible,
            uiState.value.isSettingsPanelVisible,
            uiState.value.isFindInPageVisible,
            uiState.value.isTabDataPanelVisible
        ) {
            // If the user is currently typing in the URL bar...
            if (uiState.value.isFocusOnUrlTextField) {

                // Check if any panel somehow managed to sneak open
                val hasInvalidPanelOpen = uiState.value.isTabsPanelVisible ||
                        uiState.value.isOptionsPanelVisible ||
                        uiState.value.isAppsPanelVisible ||
                        uiState.value.isDownloadPanelVisible ||
                        uiState.value.isSettingsPanelVisible ||
                        uiState.value.isFindInPageVisible ||
                        uiState.value.isSyncPanelVisible ||
                        uiState.value.isTabDataPanelVisible

                // If one did, forcefully shut them all down
                if (hasInvalidPanelOpen) {
                    viewModel.updateUI {
                        it.copy(
                            isTabsPanelVisible = false,
                            isTabsPanelLock = false,
                            isOptionsPanelVisible = false,
                            isAppsPanelVisible = false,
                            isDownloadPanelVisible = false,
                            isSyncPanelVisible = false,
                            isSettingsPanelVisible = false,
                            isFindInPageVisible = false,
                            isTabDataPanelVisible = false
                        )
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            newUrlFlow.collect { url ->
                if (url != null) {
                    // Normal Browser Mode: Always open a new tab safely
                    val insertIndex = (activeTabIndex + 1).coerceAtMost(viewModel.tabs.size)
                    viewModel.createNewTab(insertIndex, url)

                    context.newUrlFromIntent.update { null }
                }
            }
        }
        LaunchedEffect(isEffectivelyFullscreen, uiState.value.isFullscreenPreview) {
            // Unify observation for Global Mode, Video Fullscreen, and Landscape Button Mode
            val shouldHideBars = isEffectivelyFullscreen || uiState.value.isFullscreenPreview

            if (shouldHideBars) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
        LaunchedEffect(uiState.value.isLandscapeByButton, uiState.value.isOnFullscreenVideo) {
            viewModel.updateUI { it.copy(isLandscape = uiState.value.isLandscapeByButton || uiState.value.isOnFullscreenVideo) }
        }
        LaunchedEffect(uiState.value.isLandscapeByButton) {
            if (uiState.value.isLandscapeByButton) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                viewModel.updateUI { it.copy(isBottomPanelVisible = false) }
                viewModel.updateUI { it.copy(isUrlBarVisible = false) }
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

//            viewModel.updateSettings { it.copy(isFullscreenMode = uiState.value.isLandscapeByButton) }
        }
        LaunchedEffect(uiState.value.isOnFullscreenVideo) {
            viewModel.updateUI { it.copy(isMediaControlPanelVisible = uiState.value.isOnFullscreenVideo) }

//            viewModel.updateSettings { it.copy(isFullscreenMode = uiState.value.isOnFullscreenVideo) }
            if (uiState.value.isOnFullscreenVideo) {
//                gestureManager.ensureFullscreenBrightness()
            } else {
                if (activity.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }

        }
        LaunchedEffect(uiState.value.isFocusOnTextField, uiState.value.isPromptPanelVisible) {
            if ((!uiState.value.isFocusOnTextField && !uiState.value.isPromptPanelVisible)) {
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
        LaunchedEffect(uiState.value.isSettingsPanelVisible) {
            if (!uiState.value.isSettingsPanelVisible)
                viewModel.updateSettings { it.copy(isFirstAppLoad = false) }
        }
        LaunchedEffect(viewModel.inspectingAppId.longValue) {
            if (viewModel.inspectingAppId.longValue > 0L) viewModel.descriptionContent.value =
                viewModel.apps.find { it.id == viewModel.inspectingAppId.longValue }?.label
                    ?: "hello"
            else viewModel.descriptionContent.value = ""
//            if (viewModel.inspectingAppId.longValue > 0L) textFieldState.setTextAndPlaceCursorAtEnd(viewModel.apps.find { it.id == viewModel.inspectingAppId.longValue }?.label ?: viewModel.activeTab!!.currentURL.toDomain())
//            else if (textFieldState.text != viewModel.activeTab!!.currentURL && !uiState.value.isFocusOnUrlTextField) textFieldState.setTextAndPlaceCursorAtEnd(viewModel.activeTab!!.currentURL.toDomain() )
        }
        LaunchedEffect(viewModel.apps.size) {
            if (viewModel.apps.isEmpty()) {
                viewModel.resetBottomPanelTrigger.value = !viewModel.resetBottomPanelTrigger.value
            }
        }
//
        LaunchedEffect(viewModel.screenSize.value) {
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

                viewModel.updateSettings {
                    it.copy(
                        backSquareOffsetX = defaultX,
                        backSquareOffsetY = defaultY
                    )
                }
            }
        }
        LaunchedEffect(textFieldState.text, uiState.value.isFocusOnUrlTextField) {
            if (uiState.value.isFocusOnUrlTextField) {
                viewModel.fetchSuggestions(
                    query = textFieldState.text.toString(),
                    isPinning = uiState.value.isPinningApp
                )
            } else {
                viewModel.suggestions.clear()
            }
        }
        LaunchedEffect(uiState.value.isFindInPageVisible) {
            if (!uiState.value.isFindInPageVisible) {
                viewModel.findInPageText.value = ""
                viewModel.findInPageResult.value = 0 to 0
            }
        }
        LaunchedEffect(uiState.value.isSettingsPanelVisible) {
            if (!uiState.value.isSettingsPanelVisible) {
                viewModel.backgroundColor.value = Color.Black
            }
        }
        LaunchedEffect(uiState.value.isCursorMode) {
            viewModel.updateUI { it.copy(isCursorPadVisible = uiState.value.isCursorMode) }
            if (uiState.value.isCursorMode) {
                viewModel.updateUI { it.copy(isUrlBarVisible = false) }
            }
        }
        LaunchedEffect(uiState.value.inspectingTabId) {
            if (uiState.value.inspectingTabId == null) {
                viewModel.updateUI { it.copy(isTabDataPanelVisible = false) }
            }
        }
        LaunchedEffect(uiState.value.isAppsPanelVisible) {
            if (!uiState.value.isAppsPanelVisible) {
                viewModel.inspectingAppId.longValue = 0L
                if (viewModel.inspectingOption.value != null || viewModel.isSortingButtons.value) {
                    viewModel.isSortingButtons.value = false
                    viewModel.inspectingOption.value = null
                }
            } else {
                activeSession.flushSessionState()
            }
        }
        val errorTitle = stringResource(R.string.error_failed_to_load)

        LaunchedEffect(activeSession) {
            activeSession.setActive(true)

            if (!activeSession.isOpen) {
                try {
                    activeSession.open(viewModel.geckoManager.runtime)
                } catch (e: Exception) {

                }
            }
            viewModel.geckoManager.setupDelegates(
                session = activeSession,
//                tab = viewModel.activeTab!!,
                tab = object : MutableState<Tab> { // Bridge for the old delegate code
                    override var value: Tab
                        // CRITICAL FIX: Dynamically fetch activeTab so Gecko uses pwaTab instead of Tab 2!
                        get() = viewModel.activeTab
                            ?: Tab.createEmpty(viewModel.activeProfileId.value)
                        set(newTab) {
                            viewModel.updateTabById(newTab.id) { newTab }
                        }

                    override fun component1() = value
                    override fun component2(): (Tab) -> Unit = { value = it }
                },
                isStandaloneMode = viewModel.isStandaloneMode.value,
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
                        && !url.startsWith("javascript:")
                    ) {
                        val isGhostBlank =
                            url == "about:blank" && viewModel.activeTab!!.currentURL != "about:blank" && viewModel.activeTab!!.currentURL.isNotBlank()

                        if (!isGhostBlank) {
                            if (!uiState.value.isFocusOnUrlTextField) {
                                textFieldState.setTextAndPlaceCursorAtEnd(url.toDomain())
                            }
                            viewModel.updateTabById(eventTabId) { tab ->
                                val cachedIcon = tab.faviconCache[url] ?: ""

                                tab.copy(
                                    currentURL = url,
                                    currentFaviconUrl = cachedIcon
                                )
                            }
                        }
                    }
                },

                onNewSessionFunWithId = { id, uri ->
                    viewModel.handleNewSession(id, uri)
                },
                onHistoryStateChangeFun = { _, _, _ -> },
                onSessionStateChangeFun = { eventTabId, _, _ ->
                    val stateToSave =
                        viewModel.geckoManager.getSessionStateString(eventTabId)
                    if (stateToSave != null) {
                        viewModel.updateTabById(eventTabId) { it.copy(savedState = stateToSave) }
                        viewModel.tabManager.saveTabs(
                            viewModel.activeProfileId.value,
                            viewModel.tabs,
                            activeTabIndex
                        )
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

                    val tab = viewModel.tabs.find { it.id == eventTabId }
                    val isGhostBlank =
                        url == "about:blank" && tab?.currentURL != "about:blank" && !tab?.currentURL.isNullOrBlank()

                    if (!isGhostBlank) {
                        if (tab?.errorState != null) {
                            viewModel.updateTabById(eventTabId) { it.copy(errorState = null) }
                        }
                    }

                    if (eventTabId == viewModel.activeTab!!.id) {
                        if (!isGhostBlank) {
                            viewModel.updateUI { it.copy(isLoading = true) }

                            if (!uiState.value.isFocusOnUrlTextField) textFieldState.setTextAndPlaceCursorAtEnd(
                                url.toDomain()
                            )
                        }
                    }

                },
                onPageStopFun = { _, _ ->
                    viewModel.updateUI {
                        it.copy(
                            isLoading = false,
                            isFirstLoadPWA = false,
                        )
                    }

                },
                onFaviconChanged = { tabId, faviconUrl ->
                    if (faviconUrl.isNotBlank()) {
                        // CRITICAL FIX: Use updateTabById so it cleanly routes to the PWA tab if needed!
                        viewModel.updateTabById(tabId) { targetTab ->
                            val newCache = targetTab.faviconCache.toMutableMap().apply {
                                put(targetTab.currentURL, faviconUrl)
                            }
                            targetTab.copy(
                                currentFaviconUrl = faviconUrl,
                                faviconCache = newCache
                            )
                        }
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
                onDownloadRequested = { url, userAgent, contentDisposition, mimeType, stream ->
                    // Trigger your existing download logic
                    confirmationPopup(
                        message = R.string.confirm_download_file_on,
                        url = url,
                        onConfirm = {
                            startDownload(url, userAgent, contentDisposition, mimeType, stream)
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

                onLoadErrorFun = { eventTabId, session, uri, error ->
                    val failingUrl = uri ?: viewModel.tabs.find { it.id == eventTabId }?.currentURL ?: ""

                    if (session == activeSession) {
                        viewModel.updateUI { it.copy(isLoading = false) }
                        if (!uiState.value.isFocusOnUrlTextField) {
                            textFieldState.setTextAndPlaceCursorAtEnd(failingUrl.toDomain())
                        }
                    }

                    val newError = ErrorState(
                        error = error,
                        failingUrl = failingUrl
                    )

                    // Update the Tab object
                    viewModel.updateTabById(eventTabId) {
                        it.copy(
                            errorState = newError,
                            currentURL = failingUrl,
                            currentTitle = errorTitle,
                            currentFaviconUrl = ""
                        )
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

                    } else {
                        // Normal behavior for all other cases
                        mainActivity.isCurrentlyFullscreen = isFullscreen
                        mainActivity.updatePipParams(isFullscreen)

                        if (isFullscreen) {
                            activity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            insetsController.hide(WindowInsetsCompat.Type.systemBars())
                            insetsController.systemBarsBehavior =
                                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            if (uiState.value.isBottomPanelVisible) viewModel.updateUI {
                                it.copy(
                                    isBottomPanelVisible = false
                                )
                            }
                        } else {
                            // Only exit landscape/immersive if NOT in PiP
                            if (!inPip) {

                                if (uiState.value.isLandscapeByButton) viewModel.updateUI {
                                    it.copy(
                                        isLandscapeByButton = false
                                    )
                                }
//                                gestureManager.resetBrightness()

                                activity.requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                if (!isEffectivelyFullscreen) {
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
                },
                onSessionCrash = {
                    viewModel.geckoManager.forceKillSession(viewModel.activeTab!!.id)
                    viewModel.sessionRefreshTrigger.intValue++
                    viewModel.updateUI { it.copy(isLoading = false) }

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

                onExternalAppRequest = { url ->
                    viewModel.handleExternalIntent(activity, url)
                }
            )

            if (!uiState.value.initialLoadDone && initialIntentUrl != null && viewModel.activeTab!!.currentURL == initialIntentUrl) {
                webViewLoad(activeSession, initialIntentUrl, settings)
                viewModel.updateUI { it.copy(initialLoadDone = true) }
            } else {
                val stateToRestore = viewModel.activeTab!!.savedState?.let {
                    viewModel.geckoManager.restoreStateFromString(it)
                }
                if (stateToRestore == null && !viewModel.geckoManager.isEngineManaged(viewModel.activeTab!!.id)) {
                    val baseLoad = viewModel.activeTab!!.currentURL.ifBlank { settings.defaultUrl }

                    val urlToLoad = baseLoad.ifBlank { "about:blank" }
                    Log.d("marcBlank", "load blank from lanched effect, $urlToLoad")
                    webViewLoad(activeSession, urlToLoad, settings)
                }
            }
        }
        var isBrowserVisible by remember { mutableStateOf(true) }

        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        val isNewAndroid = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

        // 1. Keep a constantly updated reference to the active session.
        // This allows the lifecycle observer to use the correct session without restarting!
        val currentActiveSession by rememberUpdatedState(activeSession)

        // 2. ONLY key on the lifecycleOwner. Tab switches will no longer restart this effect!
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                when (event) {
                    androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                        isBrowserVisible = true

                        coroutineScope.launch {
                            delay(250)

                            val currentGeckoView = geckoViewRef.value
                            val safeGeckoView = currentGeckoView as? SafeGeckoView

                            // Use currentActiveSession here!
                            currentActiveSession.setActive(true)
                            safeGeckoView?.reattachKeyboard()

                            currentGeckoView?.let { gv ->
                                val now = android.os.SystemClock.uptimeMillis()
                                val downEvent =
                                    MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
                                        .apply {
                                            // 3. Mark this tap as synthetic so our touch listener can ignore it
                                            source = android.view.InputDevice.SOURCE_UNKNOWN
                                        }
                                val cancelEvent = MotionEvent.obtain(
                                    now,
                                    now + 10,
                                    MotionEvent.ACTION_CANCEL,
                                    0f,
                                    0f,
                                    0
                                ).apply {
                                    source = android.view.InputDevice.SOURCE_UNKNOWN
                                }

                                gv.dispatchTouchEvent(downEvent)
                                gv.dispatchTouchEvent(cancelEvent)

                                downEvent.recycle()
                                cancelEvent.recycle()

                                gv.requestLayout()
                                gv.invalidate()
                            }
                        }
                    }

//                    androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                        val isPip = mainActivity.isPipMode || mainActivity.isEnteringPip
                        if (!isPip) {
                            val currentGeckoView = geckoViewRef.value
                            val safeGeckoView = currentGeckoView as? SafeGeckoView

                            safeGeckoView?.detachKeyboard()
                            keyboardController?.hide()
                            currentActiveSession.setActive(false)

                            if (isNewAndroid) {
                                isBrowserVisible = false
                            }
                        }
                    }

                    else -> {}
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            // Safe to use currentActiveSession for the initial setup
            currentActiveSession.setActive(true)
            isBrowserVisible = true

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                // We do NOT call setActive(false) here. If Compose is just
                // temporarily disposing this part of the tree, we don't want to kill the engine.
                // ON_PAUSE will handle actual backgrounding.
            }
        }

        LaunchedEffect(viewModel.jsDialogState.value) {
            if (viewModel.jsDialogState.value != null) {
                viewModel.jsDialogDisplayState.value = viewModel.jsDialogState.value
            }
        }
        LaunchedEffect(uiState.value.isUrlBarVisible) {
            if (!uiState.value.isUrlBarVisible) {
//                setIsOptionsPanelVisible(false)
                viewModel.updateUI {
                    it.copy(
                        isTabDataPanelVisible = false,
                        isTabsPanelVisible = false,
                        isDownloadPanelVisible = false,
                    )
                }
                if (!viewModel.isSortingButtons.value) {
                    viewModel.updateUI {
                        it.copy(
                            isOptionsPanelVisible = false,
                            isAppsPanelVisible = false,
                            isSettingsPanelVisible = false,
                        )
                    }
                }
            } else {
                if (uiState.value.isTabsPanelLock) viewModel.updateUI {
                    it.copy(
                        isTabsPanelVisible = true
                    )
                }
                if (uiState.value.isCursorMode) viewModel.updateUI { it.copy(isCursorMode = false) }
            }
        }
        LaunchedEffect(uiState.value.isTabsPanelLock) {
            if (!uiState.value.isTabsPanelLock) {
                viewModel.updateUI { it.copy(isTabsPanelVisible = false) }
            }
        }
        LaunchedEffect(viewModel.jsDialogState.value) {
            viewModel.updateUI { it.copy(isPromptPanelVisible = viewModel.jsDialogState.value != null) }
        }
        LaunchedEffect(
            uiState.value.isUrlBarVisible,
            uiState.value.isPermissionPanelVisible,
            uiState.value.isPromptPanelVisible,
            viewModel.confirmationState.value,
            viewModel.contextMenuData.value,
            viewModel.choiceState.value
        ) {
            viewModel.updateUI {
                it.copy(
                    isBottomPanelVisible = uiState.value.isUrlBarVisible
                            || uiState.value.isPermissionPanelVisible
                            || uiState.value.isPromptPanelVisible
                            || viewModel.confirmationState.value != null
                            || viewModel.contextMenuData.value != null
                            || viewModel.choiceState.value != null
                )
            }
        }

        LaunchedEffect(viewModel.screenSize.value) {
            val squareBoxSize = settings.heightForLayer(1).dp

            val squareBoxSizePx = with(density) { squareBoxSize.toPx() }
            val paddingPx = with(density) { settings.padding.dp.toPx() }


            if (viewModel.screenSize.value.height.toFloat() > (squareBoxSizePx - paddingPx)
                && backSquareOffsetY.value > viewModel.screenSize.value.height.toFloat() - squareBoxSizePx - paddingPx
                && !uiState.value.isLandscape
                && !uiState.value.isBottomPanelVisible
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

        LaunchedEffect(isKeyboardVisible, keyboardHeight, settings.backSquareOffsetY) {
            val squareBoxSize = settings.heightForLayer(1).dp
            val squareBoxSizePx = with(density) { squareBoxSize.toPx() }
            val paddingPx = with(density) { settings.padding.dp.toPx() }
            val screenHeight = viewModel.screenSize.value.height.toFloat()

            if (isKeyboardVisible) {
                val keyboardHeightPx = with(density) { keyboardHeight.toPx() }

                // Calculate where the top of the BackSquare should be to sit exactly on top of the keyboard + padding
                val targetY = screenHeight - keyboardHeightPx - squareBoxSizePx - paddingPx

                // Only move it if the saved position is actually LOWER (visually below) the keyboard top
                if (settings.backSquareOffsetY > targetY) {
                    backSquareOffsetY.animateTo(targetY, spring())
                }
            } else {
                // Keyboard is hidden.
                // If the square is currently not at its saved position (meaning it was moved by the keyboard logic),
                // put it back where the user left it.
                // We use a small threshold (1f) to avoid floating point comparison issues
                if (kotlin.math.abs(backSquareOffsetY.value - settings.backSquareOffsetY) > 1f) {
                    backSquareOffsetY.animateTo(settings.backSquareOffsetY, spring())
                }
            }
        }
        DisposableEffect(Unit) {
            val shakeDetector = ShakeDetector(context) {
                // This code runs when a shake is detected
                coroutineScope.launch {
                    // Only trigger if the URL bar isn't already visible to avoid conflicts
                    if (!uiState.value.isBottomPanelVisible) {
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
        LaunchedEffect(uiState.value.isBottomPanelVisible) {
            if (!uiState.value.isBottomPanelVisible) {
                // -- The URL bar has just been hidden. Start the "show and blink" sequence. --

                // a. Instantly appear with 0.6 opacity.
                if (!uiState.value.isCursorMode) hideBackSquare()


                // d. After blinking, fade out completely.
            } else {
                // -- The URL bar is visible. Ensure the square is fully transparent. --
                hideBackSquare(false)
                geckoViewRef.value?.clearFocus()
            }

        }
        LaunchedEffect(settings.backSquareIdleOpacity) {
            if (!uiState.value.isBottomPanelVisible && !uiState.value.isCursorPadVisible) {
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
                viewModel.tabManager.saveTabs(
                    viewModel.activeProfileId.value,
                    viewModel.tabs,
                    activeTabIndex
                )
                saveTrigger = 0
            }
        }

        LaunchedEffect(Unit) {
            newUrlFlow.collect { url ->
                if (url != null) {
                    val insertIndex = (activeTabIndex + 1).coerceAtMost(viewModel.tabs.size)
                    viewModel.createNewTab(insertIndex, url)
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
                uiState.value.isOnFullscreenVideo -> activeSession.exitFullScreen()

                // landscape mode but not video
                activity.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    if (uiState.value.isLandscapeByButton) {
                        viewModel.updateUI { it.copy(isLandscapeByButton = false) }
                    }
                }

                viewModel.confirmationState.value != null -> {
                    viewModel.confirmationState.value = null
                }

                // back the webview
                viewModel.activeTab!!.canGoBack -> {
                    activeSession.goBack(true)
                }

                !viewModel.activeTab!!.canGoBack -> {
                    if (viewModel.isStandaloneMode.value) {
                        activity.moveTaskToBack(true)

                    } else {
                        // In Normal Browser Mode, ask the user


                        if (viewModel.tabs.size > 1) {
                            confirmationPopup(
                                message = R.string.confirm_beginning_of_history,
                                onConfirm = {
                                    viewModel.closeActiveTab {
                                        activity.finishAndRemoveTask()
                                        exitProcess(0)
                                    }
                                },
                            )
                        } else {
                            activity.moveTaskToBack(true)
                        }

                    }
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
            Box(
                modifier = Modifier
                    .size(1.dp)
                    .alpha(0f)
                    .focusable() // CRITICAL: It must be focusable
            )

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
                        onCloseAllTabs = {
                            confirmationPopup(
                                message = R.string.confirm_close_all_tab,
                                onConfirm = {
                                    closeAllTabs()
                                },
                                onCancel = {}
                            )
                        },
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
                        key(viewModel.sessionRefreshTrigger.intValue) {
                            if (viewModel.activeTab!!.errorState == null) {
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
                                        if (isBrowserVisible) {
                                            AndroidView(
                                                modifier = Modifier.fillMaxSize(),
                                                factory = { context ->
                                                    // Create the View ONCE.
                                                    // We never need to recreate this View during tab switching.
                                                    SafeGeckoView(context).apply {
                                                        layoutParams = ViewGroup.LayoutParams(
                                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                                            ViewGroup.LayoutParams.MATCH_PARENT
                                                        )
                                                        isSaveEnabled = false
                                                        activityContextDelegate = GeckoView.ActivityContextDelegate { context }
                                                        setOnTouchListener { _, event ->

                                                            if (event.source == android.view.InputDevice.SOURCE_UNKNOWN) {
                                                                return@setOnTouchListener false
                                                            }

                                                            if (event.action == MotionEvent.ACTION_DOWN) {
                                                                // The user touched the web content
                                                                if (uiState.value.isUrlBarVisible) {
                                                                    viewModel.updateUI {
                                                                        it.copy(
                                                                            isUrlBarVisible = false
                                                                        )
                                                                    }
                                                                }
                                                                if (uiState.value.isMediaControlPanelVisible) viewModel.updateUI {
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

                                                                    viewModel.colorState.value =
                                                                        null
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
                                                    //                                                geckoView.setSession(activeSession)

                                                    if (geckoView.session != activeSession) {
                                                        geckoView.releaseSession()
                                                        geckoView.setSession(activeSession)
                                                    }
                                                    geckoViewRef.value = geckoView

                                                }
                                            )
                                        }

                                        // --- PWA NATIVE SPLASH SCREEN ---

                                        // --- PWA NATIVE SPLASH SCREEN ---
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            AnimatedVisibility(
                                                visible = uiState.value.isFirstLoadPWA && viewModel.isStandaloneMode.value,
                                                enter = fadeIn(
                                                    tween(
                                                        settings.animationSpeedForLayer(
                                                            0
                                                        )
                                                    )
                                                ),
                                                exit = fadeOut(
                                                    tween(
                                                        settings.animationSpeedForLayer(
                                                            0
                                                        )
                                                    )
                                                )
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.White),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    // Grab the exact high-res URL passed from the WebAPK shell
                                                    val pwaIconUrl =
                                                        activity.intent?.getStringExtra("pwa_icon_url")
                                                            ?: ""

                                                    val imageSizePx =
                                                        with(density) { 192.dp.roundToPx() }

                                                    // --- NEW: Handle raw SVG text for the Splash Screen ---
                                                    val fallbackUrl = viewModel.activeTab?.currentURL ?: ""
                                                    val dataPayload: Any = if (pwaIconUrl.trim().startsWith("<svg", ignoreCase = true)) {
                                                        java.nio.ByteBuffer.wrap(pwaIconUrl.toByteArray(Charsets.UTF_8))
                                                    } else {
                                                        pwaIconUrl.ifBlank { fallbackUrl }
                                                    }

                                                    val imageRequest =
                                                        coil.request.ImageRequest.Builder(context)
                                                            .addHeader(
                                                                "User-Agent",
                                                                "Mozilla/5.0 (Android 14; Mobile; rv:130.0) Gecko/130.0 Firefox/130.0"
                                                            )
                                                            .data(dataPayload)
                                                            .size(imageSizePx)
                                                            .crossfade(true)
                                                            .build()

                                                    val painter =
                                                        coil.compose.rememberAsyncImagePainter(
                                                            model = imageRequest,
                                                            imageLoader = coil.Coil.imageLoader(
                                                                context
                                                            ) // Use global loader for SVG support!
                                                        )

                                                    androidx.compose.foundation.Image(
                                                        painter = painter,
                                                        contentDescription = "PWA Splash Screen",
                                                        modifier = Modifier.size(96.dp) // Large center icon
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
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
                            isCursorPadVisible = uiState.value.isCursorPadVisible,
                            position = viewModel.cursorPointerPosition.value,
                        )


                        // 3. The Error Overlay (NEW)

                        val targetState = viewModel.activeTab!!.errorState
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
                            draggableState = draggableState,
                            flingBehavior = flingBehavior,

                            onDownload = { url ->
                                // Simple generic download for images found via context menu
                                confirmationPopup(
                                    message = R.string.confirm_download_file_on,
                                    url = url,
                                    onConfirm = {
                                        startDownload(
                                            url,
                                            activeSession.settings.userAgentOverride ?: "",
                                            null,
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
                                    message = R.string.confirm_close_all_tab,
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
                                    message = R.string.confirm_remove_suggestion,
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
                            isTabDataPanelVisible = uiState.value.isTabDataPanelVisible,
                            handleCloseInspectedTab = handleCloseInspectedTab,
                            handleClearInspectedTabData = handleClearInspectedTabData,
                            handlePermissionToggle = handlePermissionToggle,


                            onDownloadRowClicked = handleOpenFile,
                            onOpenFolderClicked = handleOpenDownloadsFolder,


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
                            geckoViewRef = geckoViewRef
                        )

                        // BackSquare
                        AnimatedVisibility(
                            visible = !uiState.value.isBottomPanelVisible && !uiState.value.isLandscape && !uiState.value.isOtherPanelVisible && !viewModel.isStandaloneMode.value,
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
                                        .pointerInput(activeSession, uiState.value.isCursorMode) {

                                            if (!uiState.value.isCursorMode) awaitEachGesture {
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
                                                            viewModel.cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding().toPx(),
                                                            0
                                                        )
                                                        val upEvent = MotionEvent.obtain(
                                                            downTime,
                                                            downTime + 10,
                                                            MotionEvent.ACTION_UP,
                                                            viewModel.cursorPointerPosition.value.x,
                                                            viewModel.cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding().toPx(),
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
                                                        val screenWidth =
                                                            viewModel.screenSize.value.width.toFloat()
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
                                            // cursor mode
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
                                        // 2. ADD BORDER: A faint white ring guarantees visibility even on pitch-black websites
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(
                                                settings.cornerRadiusForLayer(
                                                    1
                                                ).dp
                                            )
                                        )
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {

                                    // Animate the appearance and disappearance of the overlay.
                                    AnimatedVisibility(
                                        visible = uiState.value.isLoading,
                                        modifier = modifier,
                                        enter = fadeIn(animationSpec = tween(300)),
                                        exit = fadeOut(animationSpec = tween(300))
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .padding(settings.padding.dp)
                                                .size(settings.heightForLayer(4).dp),
                                            // Use a contrasting color that works well on the dark scrim.
                                            color = Color.White,
                                            strokeWidth = 3.dp
                                        )
                                    }
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


//endregion
