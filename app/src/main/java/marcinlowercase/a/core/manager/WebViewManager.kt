package marcinlowercase.a.core.manager

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import marcinlowercase.a.R
import marcinlowercase.a.core.constant.favicon_discovery
import marcinlowercase.a.core.constant.generic_location_permission
import marcinlowercase.a.core.custom_class.CustomWebView
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.ContextMenuData
import marcinlowercase.a.core.data_class.CustomPermissionRequest
import marcinlowercase.a.core.data_class.SiteSettings
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.function.webViewLoad
import marcinlowercase.a.core.js_interface.BlobDownloaderInterface
import marcinlowercase.a.core.js_interface.FaviconJavascriptInterface
import java.net.URISyntaxException
import kotlin.collections.get

class WebViewManager(private val context: Context) {

    private val webViewPool = mutableMapOf<Long, CustomWebView>()
    var activeWebView: CustomWebView? = null
    val activity = context as? Activity

    //region JS Code




    //endregion


    // This method gets the WebView for a given tab, creating one if it doesn't exist.
    fun getWebView(tab: Tab): CustomWebView {
        val webView = webViewPool.getOrPut(tab.id) {
            createAndConfigureWebView()
        }
        activeWebView = webView
        return webView
    }


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
        browserSettings: BrowserSettings,
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
        onFindResultReceived: (activeIndex: Int, numberOfMatches: Int, isDoneCounting: Boolean) -> Unit,

        onTitleReceived: (webView: WebView, url: String, title: String) -> Unit,
        onContextMenu: (ContextMenuData) -> Unit,
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
            "FaviconJavascriptInterface" // This name must match the JS
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
                val decision =
                    siteSettings[domain]?.permissionDecisions?.get(generic_location_permission)

//                val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
//                val decision = siteSettings[domain]?.permissionDecisions?.get(locationPermission)

                if (decision != null) {
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
                    // If no other Android permissions were added, you might want to deny or handle appropriately.
                    if (requestedAndroidPermissions.isEmpty()) {
                        request.deny()
                        return
                    }
                }

                if (requestedAndroidPermissions.isEmpty()) {
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
                    request.grant(request.resources)
                    return
                }


                val domain = siteSettingsManager.getDomain(request.origin.toString())
                if (domain != null && requestedAndroidPermissions.isNotEmpty()) {
                    val firstPermission =
                        requestedAndroidPermissions.first() // Assuming one permission per request for simplicity
                    val decision = siteSettings[domain]?.permissionDecisions?.get(firstPermission)

                    if (decision == true) {
                        request.grant(request.resources)
                        return // Stop further processing
                    } else if (decision == false) {
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
                                request.grant(resourcesToGrant.toTypedArray())
                            } else {
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
                    view.evaluateJavascript(favicon_discovery, null)

                    onPageFinishedFun(view, currentUrlString)
                }
            }


            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url ?: return false
                val urlString = url.toString()


                if (url.scheme == "http" || url.scheme == "https") {
                    webViewLoad(activeWebView, url.toString(), browserSettings)
//                    activeWebView?.loadUrl(url.toString())
                    return true
                }



                if (url.scheme == "intent") {
                    try {
                        val intent = Intent.parseUri(urlString, Intent.URI_INTENT_SCHEME)
                        view?.context?.startActivity(intent)
                    } catch (e: Exception) {
                        val packageName = try {
                            Intent.parseUri(urlString, Intent.URI_INTENT_SCHEME).`package`
                        } catch (parseEx: URISyntaxException) {
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

        webView.setFindListener { activeIndex, numberOfMatches, isDoneCounting ->
            onFindResultReceived(activeIndex, numberOfMatches, isDoneCounting)
        }
        webView.setOnLongClickListener { v ->
            val result = (v as WebView).hitTestResult

            val type = result.type

            // Check for Link (SRC_ANCHOR_TYPE) or Image Link (SRC_IMAGE_ANCHOR_TYPE)
            if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE ||
                type == WebView.HitTestResult.IMAGE_TYPE
            ) {
                val url = result.extra

                if (url != null) {
                    onContextMenu(ContextMenuData(url, type))
                    // Return TRUE to stop the event here (prevents default drag/selection)
                    return@setOnLongClickListener true
                }
            }
            // Return FALSE to let normal long presses (like text selection) continue
            false
        }
    }
}
