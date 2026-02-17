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
import marcinlowercase.a.core.data_class.JsChoiceState
import marcinlowercase.a.core.data_class.JsColorState
import marcinlowercase.a.core.data_class.JsDateTimeState
import kotlin.math.abs

private const val UBLOCK_ID = "uBlock0@raymondhill.net"
private const val UBLOCK_NAME = "ublock_origin"
private const val FAVICON_ID = "favicon@marcinlowercase" // Must match manifest.json
private var faviconExtension: WebExtension? = null

private const val INIT = -1.0
private const val RESET = -2.0

class GeckoManager(private val context: Context) {
    var activeGeckoMediaSession: MediaSession? = null

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

    private val engineManagedSessionIds = mutableSetOf<Long>()



    private val sessionPool = mutableMapOf<Long, GeckoSession>()
    private val killedSessionIds = mutableSetOf<Long>()

    private var faviconExtensionFuture: GeckoResult<WebExtension>? = null

    init {
        runtime.settings.consoleOutputEnabled = true
        setupExtensionPrompts()
        setupExtension(UBLOCK_NAME, UBLOCK_ID)
        installFaviconFetcher()

    }

    fun tickLivePosition() {
        if (lastPositionSnapshot.doubleValue >= 0.0) {
            val now = System.currentTimeMillis()

            // 1. Calculate how much time passed since the last tick (in seconds)
            val elapsedMillis = now - lastSnapshotTime.longValue
            val elapsedSeconds = elapsedMillis / 1000.0

            // 2. If playing, advance the position snapshot
            if (lastPlaybackRate.doubleValue != 0.0) {
                val newPos =
                    lastPositionSnapshot.doubleValue + (elapsedSeconds * lastPlaybackRate.doubleValue)
                // Update the snapshot and clamp it so it doesn't exceed duration
                lastPositionSnapshot.doubleValue = newPos.coerceAtLeast(0.0)
            }

            // 3. CRITICAL: Update the snapshot time to "now"
            // This ensures the NEXT tick only calculates the delta from this moment
            lastSnapshotTime.longValue = now
        }
    }

    private fun installFaviconFetcher() {
        val uri = "resource://android/assets/extensions/favicon_fetcher/"

        faviconExtensionFuture = runtime.webExtensionController
            .ensureBuiltIn(uri, FAVICON_ID)

//        runtime.webExtensionController
//            .ensureBuiltIn(uri, FAVICON_ID)
        faviconExtensionFuture?.accept(
            { ext ->
                Log.i("GeckoExt", "Favicon Fetcher Installed")
                // SAVE THE REFERENCE
                faviconExtension = ext
            },
            { e -> Log.e("GeckoExt", "Favicon Fetcher Failed", e) }
        )
    }

    private fun setupExtensionPrompts() {
        runtime.webExtensionController.setPromptDelegate(object :
            WebExtensionController.PromptDelegate {

            // 1. MATCHING THE NEW SIGNATURE
            override fun onInstallPromptRequest(
                extension: WebExtension,
                permissions: Array<String>,
                origins: Array<String>,
                dataCollectionPermissions: Array<String>
            ): GeckoResult<WebExtension.PermissionPromptResponse> {

                Log.i(
                    "GeckoExt", "Auto-confirming install for: ${
                        extension.metaData
                            .name
                    }"
                )

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

    // use genetic function to add more extension later
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
            Log.i(
                "GeckoExt",
                "XPI Ready at: ${extensionFile.absolutePath} (${extensionFile.length()} bytes)"
            )
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
            val isManagedByEngine = engineManagedSessionIds.contains(tab.id)

            if (isManagedByEngine) {
                Log.d("GeckoManager", "Skipping manual open for engine-managed session: ${tab.id}")

            } else {
                try {
                    session.open(runtime)
                } catch (e: IllegalStateException) {
                    Log.d("NewTabFlow", "Session already opening or attached: ${e.message}")
                }
            }
        }
        return session
    }


    fun closeSession(tab: Tab) {
        sessionPool.remove(tab.id)?.apply {
            close()
        }
        engineManagedSessionIds.remove(tab.id)
    }

    fun isSessionKilled(tabId: Long): Boolean {
        return killedSessionIds.contains(tabId)
    }

    // TODO use for PiP mode later
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
        // if the active session is killed, reload immediately.
        sessionPool.remove(tabId)
        session.close()
        // But for background tabs:
        killedSessionIds.add(tabId)
    }


    fun startReset() {
        // Save exactly where Video A was when we left the page
        if (!isInit) {
            if (lastDuration.doubleValue != RESET) exitDuration = lastDuration.doubleValue
            if (lastPositionSnapshot.doubleValue != INIT) exitPosition =
                lastPositionSnapshot.doubleValue
            lastDuration.doubleValue = RESET
            lastPositionSnapshot.doubleValue = INIT
        } else {
            isInit = false
        }
    }

    fun sendVideoCommand(command: String, value: Double = 0.0) {
        val controller = activeGeckoMediaSession ?: return

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
        browserSettings: androidx.compose.runtime.State<BrowserSettings>,
        onFaviconChanged: (Long, String) -> Unit,
        onTitleChangeFun: (Long, GeckoSession, String) -> Unit,
        onNewSessionFunWithId: (id: Long, uri: String) -> Unit,
        onProgressChange: (Int) -> Unit,
        onLocationChangeFun: (eventTabId: Long, session: GeckoSession, url: String?, perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>, userGesture: Boolean) -> Unit,
        onFullScreenFun: (Boolean) -> Unit,
        onChoicePromptFun: (JsChoiceState) -> Unit,
        onHistoryStateChangeFun: (
            eventTabId: Long,
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

        onPageStartFun: (eventTabId: Long, session: GeckoSession, url: String) -> Unit,
        onPageStopFun: (session: GeckoSession, success: Boolean) -> Unit,
        onContextMenuFun: (data: ContextMenuData) -> Unit,
        onDownloadRequested: (url: String, userAgent: String, contentDisposition: String?, mimeType: String?) -> Unit,
        onJsAlert: (String) -> Unit,
        onJsConfirm: (String, (Boolean) -> Unit) -> Unit,
        onJsPrompt: (String, String, (String?) -> Unit) -> Unit,
        onLoadErrorFun: (session: GeckoSession, uri: String?, error: WebRequestError) -> Unit,
        onSessionCrash: () -> Unit,

        onFilePromptFun: (prompt: GeckoSession.PromptDelegate.FilePrompt, result: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>) -> Unit,
        onColorPromptFun: (JsColorState) -> Unit,
        onDateTimePromptFun: (JsDateTimeState) -> Unit,
        onCloseTabFun: (Long) -> Unit,

        ) {
        Log.i("NewTabFlow", "setupDelegates")
        val eventTabId = tab.value.id

        if (killedSessionIds.contains(eventTabId)) {
            Log.i("GeckoManager", "Resurrecting killed session: $eventTabId")

            // ensure session is open (in case the whole session closed)
            if (!session.isOpen) {
                session.open(runtime)
            }

            // try to restore exact state from our memory cache first
            val cachedState = stateCache[eventTabId]
            if (cachedState != null) {
                session.restoreState(cachedState)
            } else if (tab.value.savedState != null) {
                restoreStateFromString(tab.value.savedState!!)?.let {
                    session.restoreState(it)
                }
            } else {
                // fallback: If no cache, just reload the URL
                session.reload()
            }

            // remove from the kill list so we don't restore loop
            killedSessionIds.remove(eventTabId)
        }

        // message delegate to get favicon message from the web

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

        @SuppressLint("WrongThread")
        fun ensureDelegateAttached() {
            // Use the future we stored in init.
            // If installed: runs immediately.
            // If installing: runs when done.
            faviconExtensionFuture?.accept { ext ->
                if (ext != null) {
                    // Critical: Attach to the current session provided in the argument
                    session.webExtensionController.setMessageDelegate(
                        ext,
                        messageDelegate,
                        "browser"
                    )
                    Log.i(
                        "GeckoFavicon",
                        "Delegate successfully attached to session ${session.isOpen}"
                    )
                }
            }
        }

        ensureDelegateAttached()

        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                userGesture: Boolean
            ) {
                onLocationChangeFun(eventTabId, session, url, perms, userGesture)
            }

            override fun onNewSession(
                session: GeckoSession,
                uri: String
            ): GeckoResult<GeckoSession> {
                Log.d("NewTabFlow", "onNewSession")
                val newTabId = System.currentTimeMillis()


                val settings = GeckoSessionSettings.Builder()
                    .usePrivateMode(false)
                    .userAgentMode(GeckoSessionSettings.USER_AGENT_MODE_MOBILE)
                    .suspendMediaWhenInactive(false)
                    .allowJavascript(true)
                    .build()

                val newSession = GeckoSession(settings)

                // This ensures that even if the UI is slow to load the new tab,
                // the session can still close itself.
                newSession.contentDelegate = object : GeckoSession.ContentDelegate {
                    override fun onCloseRequest(s: GeckoSession) {
                        Log.d("NewTabFlow", "onCloseRequest")

                        onCloseTabFun(newTabId)
                    }
                }
                engineManagedSessionIds.add(newTabId)

                // Register the session in the pool before telling the UI
                sessionPool[newTabId] = newSession

                MainScope().launch {
                    // Pass the ID to the UI so it doesn't generate a random one
                    onNewSessionFunWithId(newTabId, uri)
                }

                return GeckoResult.fromValue(newSession)
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
                onLoadErrorFun(session, uri, error)
                return GeckoResult.fromValue("about:blank")
            }
        }

        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            // EQUIVALENT TO onPageStarted on WebView
            override fun onPageStart(session: GeckoSession, url: String) {
                Log.d("GeckoView", "Page Started: $url")

                // ignore javascript injection
                if (url.startsWith("javascript:")) return

                onPageStartFun(eventTabId, session, url)
            }


            // EQUIVALENT TO onPageFinished
            override fun onPageStop(session: GeckoSession, success: Boolean) {

                if (success) {
                    // inject js for design value
                    val js = """
                            javascript:(function(){
                            document.documentElement.style.setProperty('--device-corner-radius', '${browserSettings.value.deviceCornerRadius}px');
                            document.documentElement.style.setProperty('--padding', '${browserSettings.value.padding}px');
                            document.documentElement.style.setProperty('--single-line-height', '${browserSettings.value.singleLineHeight}px');
                            window.deviceCornerRadius = ${browserSettings.value.deviceCornerRadius};
                            if (typeof window.render === 'function') window.render($browserSettings.value.deviceCornerRadius);
                            alert("marc_console_log: Injection Success! Radius is " + window.deviceCornerRadius);
                            })()
                            """
                        .trimIndent()
                        .replace("\n", " ")

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
                // save state to restore every session state change
                stateCache[eventTabId] = state

                onSessionStateChangeFun(session, state)
            }
        }

        session.contentDelegate = object : GeckoSession.ContentDelegate {

            override fun onCloseRequest(session: GeckoSession) {
                // This fires when JS window.close() is called
                Log.i("GeckoNav", "Website requested to close tab: $eventTabId")
                onCloseTabFun(eventTabId)
            }

            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                onFullScreenFun(fullScreen)
            }

            // handle download request
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

                onDownloadRequested(url, userAgent, contentDisposition, mimeType)
            }

            override fun onTitleChange(session: GeckoSession, title: String?) {
                title?.let { title ->
                    onTitleChangeFun(eventTabId, session, title)
                }
            }

            // FavIcon from Manifest
            override fun onWebAppManifest(session: GeckoSession, manifest: JSONObject) {
                try {
                    if (manifest.has("icons")) {
                        val icons = manifest.getJSONArray("icons")
                        if (icons.length() > 0) {
                            val iconObj = icons.getJSONObject(0)
                            val iconPath = iconObj.getString("src")

                            val fullUrl = if (iconPath.startsWith("http")) {
                                iconPath
                            } else {
                                java.net.URI(
                                    session.navigationDelegate?.toString() ?: ""
                                )
                                iconPath
                            }
                            // pass the favicon url out
                            onFaviconChanged(eventTabId, fullUrl)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GeckoIcons", "Error parsing manifest", e)
                }
            }


            // This is the latest reason why We change from webview to geckoview
            override fun onContextMenu(
                session: GeckoSession,
                screenX: Int,
                screenY: Int,
                element: GeckoSession.ContentDelegate.ContextElement
            ) {
                val linkUri = element.linkUri
                val srcUri = element.srcUri
                val mediaType = element.type

                // determine the Type
                val myType = when (mediaType) {
                    GeckoSession.ContentDelegate.ContextElement.TYPE_VIDEO ->
                        ContextMenuType.VIDEO

                    GeckoSession.ContentDelegate.ContextElement.TYPE_IMAGE -> {
                        if (!linkUri.isNullOrEmpty()) ContextMenuType.IMAGE_LINK
                        else ContextMenuType.IMAGE
                    }

                    GeckoSession.ContentDelegate.ContextElement.TYPE_AUDIO ->
                        ContextMenuType.VIDEO

                    else -> {
                        if (!linkUri.isNullOrEmpty()) ContextMenuType.LINK
                        else ContextMenuType.NONE
                    }
                }

                if (myType != ContextMenuType.NONE) {
                    val data = ContextMenuData(
                        type = myType,
                        linkUrl = linkUri,
                        srcUrl = srcUri
                    )

                    onContextMenuFun(data)
                }
            }

            override fun onCrash(session: GeckoSession) {
                Log.e("GeckoManager", "Session $session Crashed: ${session.isOpen}")
                // TODO handle auto crash session
                handleSessionDeath(session, eventTabId)
                onSessionCrash()

            }

            override fun onKill(session: GeckoSession) {
                // TODO handle auto crash session
                Log.e("GeckoManager", "Session $session Killed by OS")
                handleSessionDeath(session, eventTabId)
                onSessionCrash()
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

            override fun onDateTimePrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.DateTimePrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                onDateTimePromptFun(JsDateTimeState(prompt, result))
                return result
            }

            override fun onColorPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.ColorPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()

                // Pass the prompt to the UI layer
                onColorPromptFun(JsColorState(prompt, result))

                return result
            }


            override fun onFilePrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.FilePrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()

                // Pass the request to the Activity to handle the Intent
                onFilePromptFun(prompt, result)

                return result
            }

            override fun onChoicePrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.ChoicePrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {

                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()


                onChoicePromptFun(JsChoiceState(prompt, result))
                return result
            }

            override fun onSharePrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.SharePrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {

                val uri = prompt.uri
                val text = prompt.text
                val title = prompt.title

                // link or text
                val isLink =
                    !uri.isNullOrBlank() || (text != null && android.webkit.URLUtil.isNetworkUrl(
                        text
                    ))
                val shareContent = uri ?: text ?: ""

                val displayTitle = title ?: if (isLink) "Share Link" else "Share Text"

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, displayTitle)
                    putExtra(Intent.EXTRA_TEXT, shareContent)
                }

                try {
                    val chooser = Intent.createChooser(shareIntent, null)
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    chooser.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                    context.startActivity(chooser)
                } catch (e: Exception) {
                    Log.e("GeckoShare", "Failed to start share sheet", e)
                }
                return GeckoResult.fromValue(prompt.dismiss())
            }

            override fun onAlertPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.AlertPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val message = prompt.message ?: ""


                // Current way to shows log, just TEMPORARY
                if (message.startsWith("marc_console_log:")) {
                    // print to Android Logcat
                    val logContent = message.removePrefix("marc_console_log:")
                    Log.d("marc_console_log", logContent)

                    // Dismiss immediately without showing UI
                    return GeckoResult.fromValue(prompt.dismiss())
                }

                // Show UI
                onJsAlert(message)

                return GeckoResult.fromValue(prompt.dismiss())
            }

            override fun onButtonPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.ButtonPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val message = prompt.message ?: ""

                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()

                onJsConfirm(message) { confirmed ->
                    val buttonType = if (confirmed)
                        GeckoSession.PromptDelegate.ButtonPrompt.Type.POSITIVE
                    else
                        GeckoSession.PromptDelegate.ButtonPrompt.Type.NEGATIVE
                    result.complete(prompt.confirm(buttonType))
                }

                // Return the pending result to GeckoView
                // Gecko will PAUSE JavaScript execution until this result completes!
                return result
            }

            override fun onTextPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.TextPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {

                val message = prompt.message ?: ""
                val defaultValue = prompt.defaultValue ?: ""

                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()

                onJsPrompt(message, defaultValue) { input ->
                    if (input != null) {
                        result.complete(prompt.confirm(input))
                    } else {
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
                // website started playing media! Start the background service.
                val intent = Intent(context, MediaPlaybackService::class.java)
                context.startForegroundService(intent)
            }

            override fun onDeactivated(session: GeckoSession, mediaSession: MediaSession) {
                activeGeckoMediaSession = null
                isActiveMediaSessionPaused = true

                // media stopped. Stop the background service.
                context.stopService(Intent(context, MediaPlaybackService::class.java))
            }

            // this is the primary way to know when user change video
            override fun onMetadata(
                session: GeckoSession,
                mediaSession: MediaSession,
                metadata: MediaSession.Metadata
            ) {
                Log.i("marcMedia", "Playing: ${metadata.title} by ${metadata.artist}")

                if (metadata.title != null && metadata.title != lastTitle.value) {
                    lastTitle.value = metadata.title.toString()
                    startReset()
                    mediaSession.pause()
                    mediaSession.seekTo(0.0, true)
                    mediaSession.play()

                }

                // pass value to android media control on notification panel
                val intent = Intent(context, MediaPlaybackService::class.java).apply {
                    putExtra("TITLE", metadata.title)
                    putExtra("ARTIST", metadata.artist ?: "Web Browser")
                }
                context.startService(intent)
            }

            // not work
//            override fun onFeatures(
//                session: GeckoSession,
//                mediaSession: MediaSession,
//                features: Long
//            ) {
//                val isPaused = (features and MediaSession.Feature.PAUSE) == 0L
//                if (isPaused) mediaSession.pause() else mediaSession.play()
//                Log.d("marcMedia", "Is Paused: $isPaused")
//                context.startService(Intent(context, MediaPlaybackService::class.java).apply {
//                    putExtra("IS_PAUSED", isPaused)
//                })
//            }

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
                if (lastPositionSnapshot.doubleValue == INIT) {
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


            override fun onPositionState(
                session: GeckoSession,
                mediaSession: MediaSession,
                state: MediaSession.PositionState
            ) {
                // checking if user change from video A -> B
                if (lastDuration.doubleValue == RESET) {
                    // is the duration different?
                    val durationChanged = abs(state.duration - exitDuration) > 0.0001

                    // is the position at the very beginning? (Video B just started)
                    val isAtStart = state.position < 1.0

                    // did the position jump backwards significantly? (Video A was at 59s, now we are at 0s)
                    val positionJumpedBack = state.position < (exitPosition - 1.0)

                    // VALIDATION
                    val isDataFresh = durationChanged || isAtStart || positionJumpedBack

                    if (!isDataFresh) {
                        // the state now is identical to Video A's last state or a continuation of it.
                        // ignore it.
                        return
                    }
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
                    if (exitDuration != state.duration) exitDuration = state.duration
                }

            }
        }
    }
}



