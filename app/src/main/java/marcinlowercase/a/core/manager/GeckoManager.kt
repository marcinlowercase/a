package marcinlowercase.a.core.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import marcinlowercase.a.core.custom_class.CustomPermissionDelegate
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.ContextMenuData
import marcinlowercase.a.core.data_class.CustomPermissionRequest
import marcinlowercase.a.core.data_class.SiteSettings
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.ContextMenuType
import marcinlowercase.a.core.service.MediaPlaybackService
import org.json.JSONObject
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.MediaSession
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtensionController
import org.mozilla.geckoview.WebRequestError
import org.mozilla.geckoview.WebResponse
import java.io.File
import java.io.FileOutputStream
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs

private const val UBLOCK_ID = "uBlock0@raymondhill.net"
private const val UBLOCK_NAME = "ublock_origin"
private const val FAVICON_ID = "favicon@marcinlowercase" // Must match manifest.json
private var faviconExtension: WebExtension? = null

private const val INIT = -1.0
private const val RESET = -2.0
class GeckoManager(private val context: Context) {
    var activeGeckoMediaSession: MediaSession? = null
//    var currentPosition: Double = 0.0
//    var duration: Double = 0.0

    var lastPositionSnapshot = mutableDoubleStateOf(INIT)
    var lastPositionSnapshotByGecko = mutableDoubleStateOf(INIT)
    var lastDuration = mutableDoubleStateOf(INIT)
    var lastPlaybackRate = mutableDoubleStateOf(1.0)
    var lastSnapshotTime = mutableLongStateOf(0L)

    private var exitDuration: Double = INIT
    private var exitPosition: Double = INIT
//    private var isWaitingForFreshMedia = false
    private var isInit = true

    var lastTitle = mutableStateOf("")

    var isActiveMediaSessionPaused by mutableStateOf(true)

    val runtime: GeckoRuntime by lazy {
        GeckoRuntime.getDefault(context)
    }

    private val stateCache = mutableMapOf<Long, GeckoSession.SessionState>()


    private val sessionPool = mutableMapOf<Long, GeckoSession>()
    private val killedSessionIds = mutableSetOf<Long>()


    init {
        runtime.settings.consoleOutputEnabled = true
        setupExtensionPrompts()
        setupExtension(UBLOCK_NAME, UBLOCK_ID)
        installFaviconFetcher()

    }

    fun tickLivePosition() {
        if(lastPositionSnapshot.value >= 0.0) {
            val now = System.currentTimeMillis()

            // 1. Calculate how much time passed since the last tick (in seconds)
            val elapsedMillis = now - lastSnapshotTime.value
            val elapsedSeconds = elapsedMillis / 1000.0

            // 2. If playing, advance the position snapshot
            if (lastPlaybackRate.value != 0.0) {
                val newPos = lastPositionSnapshot.value + (elapsedSeconds * lastPlaybackRate.value)
                // Update the snapshot and clamp it so it doesn't exceed duration
                lastPositionSnapshot.value = newPos.coerceAtLeast(0.0)
            }

            // 3. CRITICAL: Update the snapshot time to "now"
            // This ensures the NEXT tick only calculates the delta from this moment
            lastSnapshotTime.value = now
        }
    }

    private fun installFaviconFetcher() {
        val uri = "resource://android/assets/extensions/favicon_fetcher/"

        runtime.webExtensionController
            .ensureBuiltIn(uri, FAVICON_ID)
            .accept(
                { ext ->
                    Log.i("GeckoExt", "Favicon Fetcher Installed")
                    // SAVE THE REFERENCE
                    faviconExtension = ext
                },
                { e -> Log.e("GeckoExt", "Favicon Fetcher Failed", e) }
            )
    }
    private fun setupExtensionPrompts() {
        runtime.webExtensionController.setPromptDelegate(object : WebExtensionController.PromptDelegate {

            // 1. MATCHING THE NEW SIGNATURE
            override fun onInstallPromptRequest(
                extension: WebExtension,
                permissions: Array<String>,
                origins: Array<String>,
                dataCollectionPermissions: Array<String>
            ): GeckoResult<WebExtension.PermissionPromptResponse> {

                Log.i("GeckoExt", "Auto-confirming install for: ${extension.metaData
                    .name}")

                // 2. BUILD THE RESPONSE
                // true = Allow installation
                val response = WebExtension.PermissionPromptResponse(
                    true,
                    true,
                    true
                )

                // 3. RETURN GECKO RESULT
                return GeckoResult.fromValue(response)
            }

            override fun onUpdatePrompt(
                extension: WebExtension,
                permissions: Array<String>,
                origins: Array<String>,
                dataCollectionPermissions: Array<String>
            ): GeckoResult<AllowOrDeny> {
                Log.i("GeckoExt", "Auto-confirming update for: ${extension.metaData.name}")

                // AllowOrDeny.ALLOW = 1, DENY = 0
                // We return the integer value for ALLOW (usually 1) or use AllowOrDeny.ALLOW if available enum
                val result = AllowOrDeny.ALLOW
                return GeckoResult.fromValue(result)  // 1 = Allow
            }
        })

    }
    private fun setupExtension(extName: String, extId: String) {
        val extensionName = "$extName.xpi"
        val extensionFile = File(context.filesDir, extensionName)

        // 1. COPY FILE (Force overwrite to ensure clean state)
        try {
            context.assets.open("extensions/$extName/$extensionName").use { inputStream ->
                FileOutputStream(extensionFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            // Verify it exists
            if (!extensionFile.exists() || extensionFile.length() == 0L) {
                Log.e("GeckoExt", "CRITICAL: XPI file is empty or missing!")
                return
            }
            Log.i("GeckoExt", "XPI Ready at: ${extensionFile.absolutePath} (${extensionFile.length()} bytes)")
        } catch (e: Exception) {
            Log.e("GeckoExt", "Failed to copy asset", e)
            return
        }

        // 2. CHECK IF INSTALLED
        runtime.webExtensionController.list()
            .accept(
                { extensions ->
                    val existing = extensions?.find { it.id == extId }
                    if (existing != null) {
                        Log.i("GeckoExt", "uBlock already installed.")
                        configureExtension(existing)
                    } else {
                        Log.i("GeckoExt", "Installing fresh XPI...")
                        installXpi(extensionFile)
                    }
                },
                { e -> Log.e("GeckoExt", "List failed", e) }
            )
    }

    private fun installXpi(file: File) {
        // 3. FORCE CORRECT URI (Triple Slash)
        // File.toURI() gives "file:/", Gecko needs "file:///"
        val fileUri = "file://${file.absolutePath}"

        Log.i("GeckoExt", "Sending install command: $fileUri")

        runtime.webExtensionController
            .install(fileUri)
            .accept(
                { extension ->
                    Log.i("GeckoExt", "SUCCESS: Extension ID: ${extension?.id}")
                    if (extension != null) configureExtension(extension)
                },
                { e ->
                    // If this logs, we know WHY it failed
                    Log.e("GeckoExt", "INSTALL FAILURE", e)
                }
            )
    }

    private fun configureExtension(extension: WebExtension) {
        // 4. ENABLE (Critical)
        runtime.webExtensionController.enable(extension, WebExtensionController.EnableSource.APP)
        
    }

    fun getSession(tab: Tab): GeckoSession {
        val session = sessionPool.getOrPut(tab.id) {
            createAndConfigureSession(tab)
        }

        if (!session.isOpen) {
            session.open(runtime)
        }
        return session
    }


    fun closeSession(tab: Tab) {
        sessionPool.remove(tab.id)?.apply {
            close()
        }
    }

    fun isSessionKilled(tabId: Long): Boolean {
        return killedSessionIds.contains(tabId)
    }

    private var currentVideoWidth = 16
    private var currentVideoHeight = 9
    private fun createAndConfigureSession(tab: Tab): GeckoSession {
        val settings = GeckoSessionSettings.Builder()
            .usePrivateMode(false) // Set based on your Incognito logic
            .userAgentMode(GeckoSessionSettings.USER_AGENT_MODE_MOBILE)
            .suspendMediaWhenInactive(false)
            .allowJavascript(true)
            .build()

        val session = GeckoSession(settings)


        // RESTORE STATE
        if (tab.savedState != null) {
            val stateToRestore = restoreStateFromString(tab.savedState ?: "")
            if (stateToRestore != null)
                session.restoreState(stateToRestore)
        }

        // Connect the session to the engine
        session.open(runtime)
        session.setActive(true)


        // If it's a new tab without state, load the URL
        if (session.navigationDelegate == null && tab.savedState == null) {
            session.load(GeckoSession.Loader().uri(tab.currentURL))
        }

        return session
    }

    fun getSessionStateString(tabId: Long): String? {
        val state = stateCache[tabId] ?: return null
        return state.toString()
    }

    // 4. HELPER TO RESTORE FROM DISK
    fun restoreStateFromString(stateString: String): GeckoSession.SessionState? {
        return try {
            GeckoSession.SessionState.fromString(stateString)
        } catch (_: Exception) {
            null
        }
    }

    private fun handleSessionDeath(session: GeckoSession, tabId: Long) {
        // If the session is currently ACTIVE (visible), reload immediately.
        // The user is looking at it, so we must fix it now.
        // (Note: You might need to pass an 'isActive' flag to setupDelegates to know this for sure,
        // or check session.isOpen if you manage open/close strictly).

        // But for background tabs:
        killedSessionIds.add(tabId)
    }


    fun startReset() {
        // Save exactly where Video A was when we left the page
        if (!isInit) {
            if (lastDuration.doubleValue != RESET) exitDuration = lastDuration.doubleValue
            if (lastPositionSnapshot.doubleValue != INIT) exitPosition = lastPositionSnapshot.doubleValue
            lastDuration.doubleValue = RESET
            lastPositionSnapshot.doubleValue = INIT
        } else {
            isInit = false
        }
    }
    fun sendVideoCommand(command: String, value: Double = 0.0) {
        val controller = activeGeckoMediaSession?: return

        val currentLivePos = lastPositionSnapshot.doubleValue
        val totalDuration = lastDuration.doubleValue

        when (command) {
            "play" -> controller.play()
            "pause" -> controller.pause()
            "next_5" -> {
//                val target = (currentPosition + 5.0).coerceAtMost(duration)
                val target = (currentLivePos + 5.0).coerceAtMost(totalDuration)
                controller.seekTo(target, false)
                updateLocalSnapshotOptimistically(target)

            }
            "prev_5" -> {
                val target = (currentLivePos - 5.0).coerceAtLeast(0.0)
                controller.seekTo(target, false)
                updateLocalSnapshotOptimistically(target)

            }
            "seek_relative" -> {
                // value is the delta passed from the drag gesture
                val target = (currentLivePos + value).coerceAtLeast(0.0)
                controller.seekTo(target, true)
                updateLocalSnapshotOptimistically(target)

            }
//            "next_track" -> controller.nextTrack()
//            "prev_track" -> controller.previousTrack()

        }
    }
    private fun updateLocalSnapshotOptimistically(newTime: Double) {
        lastPositionSnapshot.doubleValue = newTime
        lastSnapshotTime.longValue = System.currentTimeMillis()
    }
    fun setupDelegates(
        session: GeckoSession,
        tab: MutableState<Tab>,
        siteSettingsManager: SiteSettingsManager,
        siteSettings: Map<String, SiteSettings>,
        browserSettings: MutableState<BrowserSettings>,
        onFaviconChanged: (Long, String) -> Unit,
        onTitleChangeFun: (Long,GeckoSession, String) -> Unit,
        onNewSessionFun: (session: GeckoSession, uri: String) -> Unit,
        onProgressChange: (Int) -> Unit,
        onLocationChangeFun: (eventTabId : Long, session: GeckoSession, url: String?, perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>, userGesture: Boolean) -> Unit,
        onFullScreenFun: (Boolean) -> Unit,
        onHistoryStateChangeFun: (
            eventTabId : Long,
            session: GeckoSession,
            realtimeHistory: GeckoSession.HistoryDelegate.HistoryList
        ) -> Unit,
        onSessionStateChangeFun: (
            session: GeckoSession,
            state: GeckoSession.SessionState
        ) -> Unit,
        onCanGoBackFun: (session: GeckoSession, canGoBack: Boolean) -> Unit,
        onCanGoForwardFun: (session: GeckoSession, canGoForward: Boolean) -> Unit,
        setPermissionDelegate: (request: CustomPermissionRequest) -> Unit,
        onShowAndroidRequest: (
            permissions: Array<out String>?,
            callback: GeckoSession.PermissionDelegate.Callback
        ) -> Unit,
        onPageStartFun: (eventTabId: Long, session: GeckoSession, url: String) -> Unit,
        onPageStopFun: (session: GeckoSession, success: Boolean) -> Unit,
        onContextMenuFun: (data: ContextMenuData) -> Unit,
        onDownloadRequested: (url: String, userAgent: String, contentDisposition: String?, mimeType: String?) -> Unit,
        onJsAlert: (String) -> Unit,
        onJsConfirm: (String, (Boolean) -> Unit) -> Unit,
        onJsPrompt: (String, String, (String?) -> Unit) -> Unit,
        onLoadErrorFun: (session: GeckoSession, uri: String?, error: WebRequestError) -> Unit,
    ) {
        val eventTabId = tab.value.id

        if (killedSessionIds.contains(eventTabId)) {
            Log.i("GeckoManager", "Resurrecting killed session: ${eventTabId}")

            // A. Ensure session is open (in case the whole session closed)
            if (!session.isOpen) {
                session.open(runtime)
            }

            // B. Try to restore exact state from our memory cache first
            val cachedState = stateCache[eventTabId]
            if (cachedState != null) {
                session.restoreState(cachedState)
            } else {
                // Fallback: If no cache, just reload the URL
                session.reload()
            }

            // C. Remove from the kill list so we don't restore loop
            killedSessionIds.remove(eventTabId)
        }

        // 1. Define the Message Delegate Logic (Keep this as is)
        val messageDelegate = object : WebExtension.MessageDelegate {
            override fun onMessage(
                nativeApp: String,
                message: Any,
                sender: WebExtension.MessageSender
            ): GeckoResult<Any>? {
                if (nativeApp == "browser" && message is JSONObject) {
                    val type = message.optString("type")
                    if (type == "favicon") {
                        val iconUrl = message.optString("url")
                        if (iconUrl.isNotEmpty()) {
                            onFaviconChanged(eventTabId, iconUrl)
                        }
                    }
                }
                return null
            }
        }

        // 2. ROBUST ATTACHMENT LOGIC
        // We wrap this in a lambda so we can call it recursively/asynchronously if needed
        @SuppressLint("WrongThread")
        fun ensureDelegateAttached() {
            if (faviconExtension != null) {
                // Best Case: Extension already loaded
                session.webExtensionController.setMessageDelegate(
                    faviconExtension!!,
                    messageDelegate,
                    "browser"
                )
            } else {
                // Race Condition: Extension installing. Fetch list.
                runtime.webExtensionController.list().accept { extensions ->
                    val ext = extensions?.find { it.id == FAVICON_ID }

                    if (ext != null) {
                        // Found it! Save for later tabs
                        faviconExtension = ext
                        // Attach to THIS session
                        session.webExtensionController.setMessageDelegate(
                            ext,
                            messageDelegate,
                            "browser"
                        )
                    } else {
                        // Still not installed??
                        // This shouldn't happen if installFaviconFetcher() was called in init.
                        // But we can verify installation here if needed.
                        Log.e("GeckoFavicon", "Extension not found in list!")
                    }
                }
            }
        }

        // 3. Call it immediately
        ensureDelegateAttached()

        // 1. Navigation Delegate (Url changes, History)
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                userGesture: Boolean
            ) {
                onLocationChangeFun(eventTabId,session, url, perms, userGesture)
            }

            override fun onNewSession(
                session: GeckoSession,
                uri: String
            ): GeckoResult<GeckoSession>? {
                // This is called when a link wants to open in a new tab.
                // 1. You can choose to open it in the CURRENT session (redirect):
                // return GeckoResult.fromValue(session)

                // 2. OR (Better) tell your UI to create a new tab:
                // We return 'null' to tell Gecko "We will handle the load ourselves manually"
                // and then we trigger your App's "New Tab" logic.

                // We run this on the main thread to interact with your Compose state
                MainScope().launch {
                    // CALL YOUR UI LOGIC HERE.
                    // Ideally pass a callback 'onNewTabRequested(uri)' into setupDelegates
                    // For now, let's assume you pass that callback.
                    onNewSessionFun(session, uri)
                }

                // Return null to prevent Gecko from creating a floating orphan session
                return null
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                onCanGoBackFun(session, canGoBack)
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                onCanGoForwardFun(session, canGoForward)
            }

            override fun onLoadError(
                session: GeckoSession,
                uri: String?,
                error: WebRequestError
            ): GeckoResult<String> {
                Log.e("GeckoNav", "Load Error: $uri (${error.category})")

                // 1. Notify the UI to show the error screen
                onLoadErrorFun(session, uri, error)

                // 2. Return "about:blank" to stop the engine from showing a partial page
                // or return null to just stop.
                return GeckoResult.fromValue("about:blank")
            }
        }

        // 2. Progress Delegate (Loading bar)
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            // 1. EQUIVALENT TO onPageStarted
            override fun onPageStart(session: GeckoSession, url: String) {
                // This fires when the page actually starts loading content.
                Log.d("GeckoView", "Page Started: $url")
                if (url.startsWith("javascript:")) return

                // Example: Show loading spinner, reset error states
                onPageStartFun(eventTabId,session, url)
            }


            // 2. EQUIVALENT TO onPageFinished
            override fun onPageStop(session: GeckoSession, success: Boolean) {

                if (success) {
                    val radius = browserSettings.value.deviceCornerRadius

                    val js = """
        javascript:(function(){
            document.documentElement.style.setProperty('--device-corner-radius', '${radius}px');
            window.deviceCornerRadius = ${radius};
            render(${radius});
            alert("marc_console_log: Injection Success! Radius is " + window.deviceCornerRadius);
        })()
    """.trimIndent().replace("\n", " ")

                    session.loadUri(js)
                }

                onPageStopFun(session, success)
                Log.d("GeckoView", "Page Finished. Success: $success")

            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                onProgressChange(progress)
            }

            override fun onSessionStateChange(
                session: GeckoSession,
                state: GeckoSession.SessionState
            ) {

                stateCache[eventTabId] = state
                Log.d("onSessionStateChange", " state ${state}")
                Log.i("onSessionStateChange", "saved state to session # ${eventTabId}")
                onSessionStateChangeFun(session, state)

            }

        }

        // 3. Content Delegate (Title, Fullscreen, Context Menu)
        session.contentDelegate = object : GeckoSession.ContentDelegate {

            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                onFullScreenFun(fullScreen)
            }
            override fun onExternalResponse(session: GeckoSession, response: WebResponse) {
                // Extract metadata from the WebResponse object
                val url = response.uri
                val contentDisposition = response.headers["Content-Disposition"]
                val mimeType = response.headers["Content-Type"]

                // Get the User Agent (needed for the system DownloadManager to match the browser's request)
                val userAgent = if (!session.settings.userAgentOverride.isNullOrBlank()) {
                    session.settings.userAgentOverride!!
                } else {
                    // A standard modern Android User Agent
                    "Mozill9a/5.0 (Android 14; Mobile; rv:131.0) Gecko/131.0 Firefox/131.0"
                }


                // Pass it up to the UI layer
                onDownloadRequested(url, userAgent, contentDisposition, mimeType)
            }

            override fun onTitleChange(session: GeckoSession, title: String?) {
                title?.let { title ->
                    onTitleChangeFun(eventTabId,session, title)
                }
            }

            // 1. Get High-Res Icons from Manifest
            override fun onWebAppManifest(session: GeckoSession, manifest: JSONObject) {
                try {
                    if (manifest.has("icons")) {
                        val icons = manifest.getJSONArray("icons")
                        // Grab the first icon (or loop to find the biggest one)
                        if (icons.length() > 0) {
                            val iconObj = icons.getJSONObject(0)
                            val iconPath = iconObj.getString("src")

                            // Resolve relative paths
                            val fullUrl = if (iconPath.startsWith("http")) {
                                iconPath
                            } else {
                                java.net.URI(
                                    session.navigationDelegate?.toString() ?: ""
                                ) // This is tricky, see Helper below
                                // Easier: Just pass the path up and resolve it in UI against current tab URL
                                iconPath
                            }

//                            onIconChangeFun(fullUrl)
                            onFaviconChanged(eventTabId, fullUrl)

                        }
                    }
                } catch (e: Exception) {
                    Log.e("GeckoIcons", "Error parsing manifest", e)
                }
            }

            override fun onContextMenu(
                session: GeckoSession,
                screenX: Int,
                screenY: Int,
                element: GeckoSession.ContentDelegate.ContextElement
            ) {
                // GeckoView handles context menu detection differently.
                // 'element' contains the type (IMAGE, VIDEO, LINK) and the URL.
                // You can map this to your ContextMenuData.

                val linkUri = element.linkUri
                val srcUri = element.srcUri
                val mediaType = element.type

                // 1. Determine the Type
                val myType = when (mediaType) {
                    GeckoSession.ContentDelegate.ContextElement.TYPE_VIDEO ->
                        ContextMenuType.VIDEO

                    GeckoSession.ContentDelegate.ContextElement.TYPE_IMAGE -> {
                        if (!linkUri.isNullOrEmpty()) ContextMenuType.IMAGE_LINK
                        else ContextMenuType.IMAGE
                    }

                    GeckoSession.ContentDelegate.ContextElement.TYPE_AUDIO ->
                        ContextMenuType.VIDEO // Treat Audio like Video for now (downloadable)

                    else -> {
                        if (!linkUri.isNullOrEmpty()) ContextMenuType.LINK
                        else ContextMenuType.NONE
                    }
                }

                if (myType != ContextMenuType.NONE) {
                    // 2. Create the clean data object
                    val data = ContextMenuData(
                        type = myType,
                        linkUrl = linkUri,
                        srcUrl = srcUri
                    )

                    onContextMenuFun(data)
                }
            }

            override fun onCrash(session: GeckoSession) {
                // The Content Process died (Crash or OOM Kill)
                Log.e("GeckoManager", "Session $session Crashed: ${session.isOpen}")

                // If this session is currently visible (Active), reload it immediately.
                // If it's in the background, GeckoView will usually auto-reload it
                // when it becomes active again (thanks to suspendMediaWhenInactive=true).
                handleSessionDeath(session, eventTabId)

            }

            override fun onKill(session: GeckoSession) {
                // Similar to onCrash, but specifically for OS kills
                Log.e("GeckoManager", "Session $session Killed by OS")
                handleSessionDeath(session, eventTabId)
            }

        }

        session.historyDelegate = object : GeckoSession.HistoryDelegate {
            override fun onHistoryStateChange(
                session: GeckoSession,
                realtimeHistory: GeckoSession.HistoryDelegate.HistoryList
            ) {
                onHistoryStateChangeFun(eventTabId, session, realtimeHistory)
            }
        }

        session.permissionDelegate = CustomPermissionDelegate(

            context = context,
            onShowRequest = { request ->
                setPermissionDelegate(request)
            },
            tab = tab,
            siteSettings = siteSettings,
            siteSettingsManager = siteSettingsManager,
        )

        session.promptDelegate = object : GeckoSession.PromptDelegate {

            // 1. ALERT
            override fun onAlertPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.AlertPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val message = prompt.message ?: ""


                if (message.startsWith("marc_console_log:")) {
                    // Strip the prefix and print to Android Logcat
                    val logContent = message.removePrefix("marc_console_log:")
                    Log.d("marc_console_log", logContent)

                    // Dismiss immediately without showing UI
                    return GeckoResult.fromValue(prompt.dismiss())
                }

                // Show UI
                onJsAlert(message)

                // GeckoView alerts are non-blocking in the UI sense,
                // but we need to return a result to acknowledge it.
                // We return 'dismiss()' immediately here because your Compose UI
                // will just show a message. If you want to block the web thread until
                // the user clicks OK, that's much harder in GeckoView's async model.
                // Standard practice: Show the UI, but tell the engine "OK" immediately.
                return GeckoResult.fromValue(prompt.dismiss())
            }

            // 2. CONFIRM
            override fun onButtonPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.ButtonPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val message = prompt.message ?: ""

                // We create a GeckoResult that will be completed LATER by the UI
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()

                // Pass the message and a callback to the UI
                onJsConfirm(message) { confirmed ->
                    val buttonType = if (confirmed)
                        GeckoSession.PromptDelegate.ButtonPrompt.Type.POSITIVE
                    else
                        GeckoSession.PromptDelegate.ButtonPrompt.Type.NEGATIVE

                    // Complete the result when user clicks a button
                    result.complete(prompt.confirm(buttonType))
                }

                // Return the pending result to GeckoView
                // Gecko will PAUSE JavaScript execution until this result completes!
                return result
            }

            // 3. PROMPT (Text Input)
            override fun onTextPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.TextPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {

                val message = prompt.message ?: ""
                val defaultValue = prompt.defaultValue ?: ""

                // NORMAL CASE: Real website prompt
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()

                onJsPrompt(message, defaultValue) { input ->
                    if (input != null) {
                        result.complete(prompt.confirm(input))
                    } else {
                        // User cancelled
                        result.complete(prompt.dismiss())
                    }
                }

                return result
            }
        }

        session.mediaSessionDelegate = object : MediaSession.Delegate {
            override fun onActivated(session: GeckoSession, mediaSession: MediaSession) {
                Log.i("marcMedia", "onActivated")
                activeGeckoMediaSession = mediaSession

                isActiveMediaSessionPaused = false
//                onActivatedFun()
                // 1. Website started playing media! Start the background service.
                val intent = Intent(context, MediaPlaybackService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }

            override fun onDeactivated(session: GeckoSession,  mediaSession: MediaSession) {
                activeGeckoMediaSession = null
                isActiveMediaSessionPaused = true

                // 2. Media stopped. Stop the background service.
                context.stopService(Intent(context, MediaPlaybackService::class.java))
            }

            override fun onMetadata(session: GeckoSession,  mediaSession: MediaSession, metadata: MediaSession.Metadata) {
                // 3. Update the Android Media Controls with Title/Artist from the website
                Log.i("marcMedia", "Playing: ${metadata.title} by ${metadata.artist}")
//                currentPosition = mediaSession.


                if (metadata.title != null && metadata.title != lastTitle.value) {
                    lastTitle.value = metadata.title.toString()
                    startReset()
                    mediaSession.pause()
                    mediaSession.seekTo(0.0, true)
                    mediaSession.play()

                }
                val intent = Intent(context, MediaPlaybackService::class.java).apply {
                    putExtra("TITLE", metadata.title)
                    putExtra("ARTIST", metadata.artist ?: "Web Browser")
                }
                context.startService(intent)
            }
            override fun onFeatures(session: GeckoSession, mediaSession: MediaSession, features: Long) {
                val isPaused = (features and MediaSession.Feature.PAUSE) == 0L
                if (isPaused) mediaSession.pause() else mediaSession.play()
                Log.d("marcMedia", "Is Paused: $isPaused")
                context.startService(Intent(context, MediaPlaybackService::class.java).apply {
                    putExtra("IS_PAUSED", isPaused)
                })
            }

            override fun onPause(session: GeckoSession, mediaSession: MediaSession) {
                activeGeckoMediaSession = mediaSession

                isActiveMediaSessionPaused = true
                Log.d("marcMedia", "onPause")
                context.startService(Intent(context, MediaPlaybackService::class.java).apply {
                    putExtra("IS_PAUSED", true)
                })
                super.onPause(session, mediaSession)
            }

            override fun onPlay(session: GeckoSession, mediaSession: MediaSession) {
                isActiveMediaSessionPaused = false
                activeGeckoMediaSession = mediaSession


                Log.d("marcMedia", "onPlay")
                if(lastPositionSnapshot.doubleValue == INIT) {
                    lastPositionSnapshot.doubleValue = 0.0
                }
                context.startService(Intent(context, MediaPlaybackService::class.java).apply {
                    putExtra("IS_PAUSED", false)
                })
                super.onPlay(session, mediaSession)
            }

            override fun onStop(session: GeckoSession, mediaSession: MediaSession) {
                activeGeckoMediaSession = null
                super.onStop(session, mediaSession)
            }


            override fun onPositionState(session: GeckoSession, mediaSession: MediaSession, state: MediaSession.PositionState) {
                if (lastDuration.doubleValue == RESET) {
                    // 1. Is the duration different? (Strongest Signal)
                    val durationChanged = abs(state.duration - exitDuration) > 0.0001

                    // 2. Is the position at the very beginning? (Video B just started)
                    val isAtStart = state.position < 1.0

                    // 3. Did the position jump backwards significantly? (Video A was at 59s, now we are at 0s)
                    val positionJumpedBack = state.position < (exitPosition - 1.0)

                    // VALIDATION
                    val isDataFresh = durationChanged || isAtStart || positionJumpedBack

                    if (!isDataFresh) {
                        // This is identical to Video A's last state or a continuation of it.
                        // Ignore it.
                        return
                    }

                    // UNLOCK
                }

                // Update values normally
                if (state.position != lastPositionSnapshotByGecko.doubleValue || lastPositionSnapshot.doubleValue == INIT) {
                    lastPositionSnapshotByGecko.doubleValue = state.position
                    lastPositionSnapshot.doubleValue = state.position
                }
                lastSnapshotTime.longValue = System.currentTimeMillis()

                lastPlaybackRate.doubleValue = state.playbackRate

                if (!state.duration.isNaN()) {
                    lastDuration.doubleValue = state.duration
                    if ( exitDuration != state.duration) exitDuration = state.duration
                }

            }
        }


    }
}



