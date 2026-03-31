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
package marcinlowercase.a.core.manager

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import marcinlowercase.a.R
import marcinlowercase.a.core.custom_class.CustomPermissionDelegate
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.ContextMenuData
import marcinlowercase.a.core.data_class.CustomPermissionRequest
import marcinlowercase.a.core.data_class.JsChoiceState
import marcinlowercase.a.core.data_class.JsColorState
import marcinlowercase.a.core.data_class.JsDateTimeState
import marcinlowercase.a.core.data_class.SiteSettings
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.ContextMenuType
import marcinlowercase.a.core.function.formatArgbToCss
import marcinlowercase.a.core.function.toDomain
import marcinlowercase.a.core.service.MediaPlaybackService
import org.json.JSONObject
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.MediaSession
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtensionController
import org.mozilla.geckoview.WebNotification
import org.mozilla.geckoview.WebNotificationDelegate
import org.mozilla.geckoview.WebRequestError
import org.mozilla.geckoview.WebResponse
import kotlin.math.abs

private const val UBLOCK_ID = "uBlock0@raymondhill.net"
private const val FAVICON_ID = "browser_core_extension@marcinlowercase"
private var coreExtension: WebExtension? = null

private const val INIT = -1.0
private const val RESET = -2.0

class GeckoManager(private val context: Context) {
    var activeGeckoMediaSession: MediaSession? = null
    var activeMediaGeckoSession: GeckoSession? = null

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

//    val runtime: GeckoRuntime by lazy {
//        GeckoRuntime.getDefault(context)
//    }

    val runtime: GeckoRuntime by lazy {
        val settings = GeckoRuntimeSettings.Builder()
            .inputAutoZoomEnabled(false)
            .consoleOutput(true) // Forces console logs ON immediately
            .build()
        GeckoRuntime.create(context, settings)
    }
    private val stateCache = mutableMapOf<Long, GeckoSession.SessionState>()

    private val engineManagedSessionIds = mutableSetOf<Long>()


    private val sessionPool = mutableStateMapOf<Long, GeckoSession>()
    val sessionPoolSize: Int
        get() = sessionPool.size
    private val killedSessionIds = mutableSetOf<Long>()

    private var coreExtensionFuture: GeckoResult<WebExtension>? = null

    private var uBlockExtension: WebExtension? = null
    var isAdBlockEnabledTarget = true
    private val webNotificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "marcinlowercase.a.WEB_NOTIFICATION_DISMISS") {
                try {
                    val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra("web_notification", WebNotification::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra<WebNotification>("web_notification")
                    }
                    // Tells the website JavaScript that the user dismissed/swiped it away
                    notification?.dismiss()
                } catch (e: Exception) {
                    Log.e("GeckoExt", "Failed to dismiss WebNotification", e)
                }
            }
        }
    }
    init {

        ContextCompat.registerReceiver(
            context,
            webNotificationReceiver,
            IntentFilter("marcinlowercase.a.WEB_NOTIFICATION_DISMISS"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        setupExtensionPrompts()
        installFaviconFetcher()
        ensureUblockOrigin()

        setupWebNotifications()

    }


    private fun setupWebNotifications() {
        // Ensure Android Notification Channel Exists
        val channel = NotificationChannel(
            "web_notifications",
            "Web Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        runtime.webNotificationDelegate = object : WebNotificationDelegate {
            @SuppressLint("WrongThread")
            override fun onShowNotification(notification: WebNotification) {
                // Ensure a unique tag/ID for stacking notifications
                val tag = notification.tag?.ifEmpty { notification.title } ?: notification.title
                val notifId = tag.hashCode()

                // 1. Click Intent (Launches MainActivity)
                val clickIntent = Intent().apply {
                    setClassName(context, "marcinlowercase.a.MainActivity")
                    action = "marcinlowercase.a.WEB_NOTIFICATION_CLICK"
                    putExtra("web_notification", notification)
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

                val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                val pendingClick = PendingIntent.getActivity(context, notifId, clickIntent, pendingFlags)

                // 2. Dismiss Intent (Hits our BroadcastReceiver)
                val deleteIntent = Intent("marcinlowercase.a.WEB_NOTIFICATION_DISMISS").apply {
                    setPackage(context.packageName)
                    putExtra("web_notification", notification)
                }
                val pendingDelete = PendingIntent.getBroadcast(context, notifId, deleteIntent, pendingFlags)


                var siteDomain = "web"
                try {
                    // 'source' is the URL of the page/ServiceWorker that triggered this (e.g., "https://example.com/...")
                    val sourceUrl = notification.source
                    siteDomain = sourceUrl?.toDomain() ?: "web"
                } catch (e: Exception) {
                    Log.e("GeckoExt", "Failed to parse notification source URL", e)
                }

                // 3. Build the Android Notification
                val builder = NotificationCompat.Builder(context, "web_notifications")
                    .setSmallIcon(marcinlowercase.a.R.drawable.ic_empty_logo) // Use your icon here
                    .setContentTitle(notification.title)
                    .setContentText(notification.text)
                    .setContentIntent(pendingClick)
                    .setSubText(siteDomain)
                    .setDeleteIntent(pendingDelete)
                    .setAutoCancel(true)

                val manager = NotificationManagerCompat.from(context)
                val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

                // 4. Send the Notification to the OS
                if (hasPermission) {
                    manager.notify(tag, notifId, builder.build())
                    try {
                        notification.show() // VERY IMPORTANT: Tells JS the notification successfully appeared
                    } catch (e: Exception) {}
                } else {
                    try {
                        notification.dismiss() // Tells JS the notification was blocked by OS
                    } catch (e: Exception) {}
                }
            }

            @SuppressLint("WrongThread")
            override fun onCloseNotification(notification: WebNotification) {
                // Fires if the website's JS calls `notification.close()`
                val tag = notification.tag?.ifEmpty { notification.title } ?: notification.title
                val manager = NotificationManagerCompat.from(context)
                manager.cancel(tag, tag.hashCode()) // Close the native Android popup

                try {
                    notification.dismiss() // Clean up the GeckoView IPC resource
                } catch (e: Exception) {}
            }
        }
    }

    private fun ensureUblockOrigin() {
        runtime.webExtensionController.list().accept(
            { extensions ->
                val existing = extensions?.find { it.id == UBLOCK_ID }
                if (existing != null) {
                    // 1. It's already installed! Just configure its enabled/disabled state.
                    // (GeckoView automatically handles updating it in the background)
                    configureExtension(existing)
                } else {
                    // 2. Not installed yet. Fetch the absolute latest version from Mozilla Add-ons (AMO).
                    val latestUblockUrl =
                        "https://addons.mozilla.org/firefox/downloads/latest/ublock-origin/latest.xpi"
                    installRemoteExtension(latestUblockUrl)
                }
            },
            { e -> Log.e("GeckoExt", "Failed to list extensions", e) }
        )
    }

    private fun installRemoteExtension(url: String) {
        runtime.webExtensionController.install(url).accept(
            { extension ->
                if (extension != null) {
                    Log.i(
                        "GeckoExt",
                        "Successfully downloaded and installed: ${extension.metaData.name}"
                    )
                    configureExtension(extension)
                }
            },
            { e ->
                Log.e("GeckoExt", "Failed to install uBlock from web", e)
            }
        )
    }

    fun setAdBlockEnabled(isEnabled: Boolean) {
        isAdBlockEnabledTarget = isEnabled
        uBlockExtension?.let { ext ->
            if (isEnabled) {
                runtime.webExtensionController.enable(ext, WebExtensionController.EnableSource.APP)
            } else {
                runtime.webExtensionController.disable(ext, WebExtensionController.EnableSource.APP)
            }
        }
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
        val uri = "resource://android/assets/extensions/core/"

        coreExtensionFuture = runtime.webExtensionController
            .ensureBuiltIn(uri, FAVICON_ID)

//        runtime.webExtensionController
//            .ensureBuiltIn(uri, FAVICON_ID)
        coreExtensionFuture?.accept(
            { ext ->
                //Log.i("GeckoExt", "Favicon Fetcher Installed")
                // SAVE THE REFERENCE
                coreExtension = ext
            },
            { _ -> //Log.e("GeckoExt", "Favicon Fetcher Failed", e)
            }
        )
    }

    fun wipeProfileData(profileId: String) {
        // Tells GeckoView to permanently delete all cookies/data for this specific partition
        runtime.storageController.clearDataForSessionContext(profileId)
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

                //Log.i(
//                    "GeckoExt", "Auto-confirming install for: ${
//                        extension.metaData
//                            .name
//                    }"
//                )

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
                //Log.i("GeckoExt", "Auto-confirming update for: ${extension.metaData.name}")

                // AllowOrDeny.ALLOW = 1, DENY = 0
                // We return the integer value for ALLOW (usually 1) or use AllowOrDeny.ALLOW if available enum
                val result = AllowOrDeny.ALLOW
                return GeckoResult.fromValue(result)  // 1 = Allow
            }
        })

    }

    private fun configureExtension(extension: WebExtension) {

        if (extension.id == UBLOCK_ID) {
            uBlockExtension = extension
            if (isAdBlockEnabledTarget) {
                runtime.webExtensionController.enable(
                    extension,
                    WebExtensionController.EnableSource.APP
                )
            } else {
                runtime.webExtensionController.disable(
                    extension,
                    WebExtensionController.EnableSource.APP
                )
            }
        } else {
            // Default behavior for Favicon or other extensions
            runtime.webExtensionController.enable(
                extension,
                WebExtensionController.EnableSource.APP
            )
        }

    }

    fun optimizeMemoryExcept(activeTabId: Long) {
        // Find all sessions currently eating RAM except the active one
        val idsToClose = sessionPool.keys.filter { it != activeTabId }

        for (id in idsToClose) {
            val sessionToClose = sessionPool.remove(id)
            sessionToClose?.apply {
                if (this == activeMediaGeckoSession) {
                    context.stopService(Intent(context, MediaPlaybackService::class.java))
                    activeGeckoMediaSession = null
                    activeMediaGeckoSession = null
                }
                close()
            }
            engineManagedSessionIds.remove(id)
        }
    }

    fun getSession(tab: Tab, isDesktopMode: Boolean = false): GeckoSession {
        val session = sessionPool.getOrPut(tab.id) {
            createAndConfigureSession(tab, isDesktopMode)
        }

        if (!session.isOpen) {
            val isManagedByEngine = isEngineManaged(tab.id)

            if (isManagedByEngine) {
                //Log.d("GeckoManager", "Skipping manual open for engine-managed session: ${tab.id}")

            } else {
                try {
                    session.open(runtime)
                } catch (_: IllegalStateException) {
                    //Log.d("NewTabFlow", "Session already opening or attached: ${e.message}")
                }
            }
        }

        // If the session was requested, it is actively alive. Remove from killed list.
        killedSessionIds.remove(tab.id)

        return session
    }

    fun pauseSessionIfExists(tabId: Long) {
        val session = sessionPool[tabId]
        if (session != null) {
            session.setActive(false)
            //TODO make this take a value of isUseBackground AUDIO
//            if (activeMediaGeckoSession == session) {
//                activeGeckoMediaSession?.pause()
//            }
        }
    }

    fun closeSession(tab: Tab) {
        val sessionToClose = sessionPool.remove(tab.id)
        sessionToClose?.apply {
            // If the tab we are closing holds the active media, stop the service
            if (this == activeMediaGeckoSession) {
                context.stopService(Intent(context, MediaPlaybackService::class.java))
                activeGeckoMediaSession = null
                activeMediaGeckoSession = null
            }
            close()
        }
        engineManagedSessionIds.remove(tab.id)
    }

    private fun getSessionSettings(tab: Tab, isDesktopMode: Boolean): GeckoSessionSettings {
        return GeckoSessionSettings.Builder()
            .usePrivateMode(false) // Set based on your Incognito logic
            .userAgentMode(if (isDesktopMode) GeckoSessionSettings.USER_AGENT_MODE_DESKTOP else GeckoSessionSettings.USER_AGENT_MODE_MOBILE)
            .viewportMode(if (isDesktopMode) GeckoSessionSettings.VIEWPORT_MODE_DESKTOP else GeckoSessionSettings.VIEWPORT_MODE_MOBILE)
            .suspendMediaWhenInactive(false)
            .allowJavascript(true)
            .contextId(tab.profileId)
            .build()
    }

    private fun createAndConfigureSession(tab: Tab, isDesktopMode: Boolean): GeckoSession {

        val session = GeckoSession(getSessionSettings(tab, isDesktopMode))


        // RESTORE STATE - Only do this ONCE upon session creation
        val cachedState = stateCache[tab.id]
        if (cachedState != null) {
            Log.d("marcRestore", "restore state from cache in createAndConfigureSession")
            session.restoreState(cachedState)
        } else if (tab.savedState != null) {
            val stateToRestore = restoreStateFromString(tab.savedState ?: "")
            if (stateToRestore != null) {
                Log.d("marcRestore", "restore state from string in createAndConfigureSession")
                session.restoreState(stateToRestore)
            }
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
        if (session == activeMediaGeckoSession) {
            context.stopService(Intent(context, MediaPlaybackService::class.java))
            activeGeckoMediaSession = null
            activeMediaGeckoSession = null
        }
        // if the active session is killed, reload immediately.
        sessionPool.remove(tabId)
        session.close()
        // But for background tabs:
        killedSessionIds.add(tabId)
    }


    fun forceKillSession(tabId: Long) {
        val session = sessionPool.remove(tabId)

        if (session == activeMediaGeckoSession) {
            context.stopService(Intent(context, MediaPlaybackService::class.java))
            activeGeckoMediaSession = null
            activeMediaGeckoSession = null
        }
        if (session?.isOpen == true) {
            session.close()
        }
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
        }
    }

    private fun updateLocalSnapshotOptimistically(newTime: Double) {
        lastPositionSnapshot.doubleValue = newTime
        lastSnapshotTime.longValue = System.currentTimeMillis()
    }

    private fun isExternalScheme(uri: String): Boolean {
        if (uri.isBlank()) return false

        val lowerUri = uri.lowercase()
        // These are standard web schemes. Everything else should likely be handled by an external app.
        return !(lowerUri.startsWith("http://") ||
                lowerUri.startsWith("https://") ||
                lowerUri.startsWith("file://") ||
                lowerUri.startsWith("content://") ||
                lowerUri.startsWith("about:") ||
                lowerUri.startsWith("resource://") ||
                lowerUri.startsWith("javascript:") ||
                lowerUri.startsWith("blob:") ||
                lowerUri.startsWith("data:") ||
                lowerUri.startsWith("moz-extension://") || // <-- THE FIX: Allow uBlock Origin's block pages!
                lowerUri.startsWith("chrome-extension://") ||
                lowerUri.startsWith("extension://")) // <-- THE FIX: Allow uBlock Origin's block pages!
    }

    fun isEngineManaged(tabId: Long): Boolean {
        return engineManagedSessionIds.contains(tabId)
    }

    fun setupDelegates(
        session: GeckoSession,
        tab: MutableState<Tab>,
        isStandaloneMode: Boolean,
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
            eventTabId: Long,
            session: GeckoSession,
            state: GeckoSession.SessionState
        ) -> Unit,
        onCanGoBackFun: (session: GeckoSession, canGoBack: Boolean) -> Unit,
        onCanGoForwardFun: (session: GeckoSession, canGoForward: Boolean) -> Unit,
        setPermissionDelegate: (request: CustomPermissionRequest) -> Unit,

        onPageStartFun: (eventTabId: Long, session: GeckoSession, url: String) -> Unit,
        onPageStopFun: (session: GeckoSession, success: Boolean) -> Unit,
        onContextMenuFun: (data: ContextMenuData) -> Unit,
        onDownloadRequested: (url: String, userAgent: String, contentDisposition: String?, mimeType: String?, stream: java.io.InputStream?) -> Unit,
        onJsAlert: (String) -> Unit,
        onJsConfirm: (String, (Boolean) -> Unit) -> Unit,
        onJsPrompt: (String, String, (String?) -> Unit) -> Unit,
        onLoadErrorFun: (eventTabId: Long, session: GeckoSession, uri: String?, error: WebRequestError) -> Unit,
        onSessionCrash: () -> Unit,

        onFilePromptFun: (prompt: GeckoSession.PromptDelegate.FilePrompt, result: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>) -> Unit,
        onColorPromptFun: (JsColorState) -> Unit,
        onDateTimePromptFun: (JsDateTimeState) -> Unit,
        onCloseTabFun: (Long) -> Unit,
        onExternalAppRequest: (String) -> Unit


    ) {
        //Log.i("NewTabFlow", "setupDelegates")
        val eventTabId = tab.value.id

//

        // message delegate to get favicon message from the web

        val messageDelegate = object : WebExtension.MessageDelegate {
            override fun onMessage(
                nativeApp: String,
                message: Any,
                sender: WebExtension.MessageSender
            ): GeckoResult<Any> {

                Log.d("OutSyncNative", "Message received from JS! Payload: $message")

                if (nativeApp == "browser") {
                    try {
                        val type = when (message) {
                            is JSONObject -> message.optString("type")
                            is Map<*, *> -> message["type"] as? String
                            else -> null
                        }

                        if (type == "favicon") {
                            val iconUrl = when (message) {
                                is JSONObject -> message.optString("url")
                                is Map<*, *> -> message["url"] as? String
                                else -> ""
                            }
                            if (!iconUrl.isNullOrEmpty()) {
                                onFaviconChanged(eventTabId, iconUrl)
                            }
                            // BYPASS BUG: Return a simple primitive String
                            return GeckoResult.fromValue("OK")
                        } else if (type == "getSettings") {
                            // BYPASS THE STALE CONTEXT:
                            // Get the absolute LIVE screen dimensions from the Android System
                            val displayMetrics =
                                android.content.res.Resources.getSystem().displayMetrics
                            val liveScreenWidthDp =
                                displayMetrics.widthPixels / displayMetrics.density.toDouble()

                            val responseObj = JSONObject().apply {
                                put("enabled", browserSettings.value.isEnabledOutSync)
                                put("radius", browserSettings.value.deviceCornerRadius.toDouble())
                                put("padding", browserSettings.value.padding.toDouble())
                                put("lineHeight", browserSettings.value.singleLineHeight.toDouble())
                                put(
                                    "color",
                                    formatArgbToCss(browserSettings.value.highlightColor.toHexString())
                                )
                                put("isDesktop", browserSettings.value.isDesktopMode)
                                // Send the LIVE width to JavaScript
                                put("screenWidth", liveScreenWidthDp)
                            }

                            val responseString = responseObj.toString()
                            return GeckoResult.fromValue(responseString)
                        }

                    } catch (e: Exception) {
                        Log.e("OutSyncNative", "Error parsing JS message", e)
                    }
                }

                // Fallback: return primitive string instead of object
                return GeckoResult.fromValue("IGNORE")
            }
        }

        @SuppressLint("WrongThread")
        fun ensureDelegateAttached() {
            // Note: This prints to the MAIN App Process logcat, not the Isolated Web Content logcat!
            Log.d("OutSyncNative", "Attempting to attach message delegate to session...")

            // CRITICAL SPEED FIX: If the extension is already loaded, attach INSTANTLY!
            if (coreExtension != null) {
                session.webExtensionController.setMessageDelegate(
                    coreExtension!!,
                    messageDelegate,
                    "browser"
                )
                Log.d("OutSyncNative", "SUCCESS: Message delegate attached instantly!")
            } else {
                // Fallback if it's still loading
                coreExtensionFuture?.accept { ext ->
                    if (ext != null) {
                        session.webExtensionController.setMessageDelegate(
                            ext,
                            messageDelegate,
                            "browser"
                        )
                        Log.d("OutSyncNative", "SUCCESS: Message delegate attached via future.")
                    }
                }
            }
        }

        ensureDelegateAttached()
        runtime.webExtensionController.list().accept(
            { extensions ->
                extensions?.forEach { ext ->
                    session.webExtensionController.setTabDelegate(
                        ext,
                        object : WebExtension.SessionTabDelegate {
                            // Triggered when uBlock intercepts a tracking link and redirects
                            // to its "Strict Blocking" moz-extension:// warning page.
                            override fun onUpdateTab(
                                extension: WebExtension,
                                sess: GeckoSession,
                                details: WebExtension.UpdateTabDetails
                            ): GeckoResult<AllowOrDeny> {
                                // Returning ALLOW tells GeckoView to automatically load the requested URL
                                return GeckoResult.fromValue(AllowOrDeny.ALLOW)
                            }

                            // Optional: Triggered if an extension tries to close the current tab
                            override fun onCloseTab(
                                source: WebExtension?,
                                sess: GeckoSession
                            ): GeckoResult<AllowOrDeny> {
                                onCloseTabFun(eventTabId)
                                return GeckoResult.fromValue(AllowOrDeny.ALLOW)
                            }
                        }
                    )
                }
            },
            { e -> Log.e("GeckoManager", "Failed to list extensions", e) }
        )
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                userGesture: Boolean
            ) {
                //Log.i("GeckoNav", "onLocationChange")
                onLocationChangeFun(eventTabId, session, url, perms, userGesture)
            }

            override fun onNewSession(
                session: GeckoSession,
                uri: String
            ): GeckoResult<GeckoSession> {


                //Log.d("NewTabFlow", "onNewSession")
                val newTabId = System.currentTimeMillis()

                val newSession =
                    GeckoSession(getSessionSettings(tab.value, browserSettings.value.isDesktopMode))

                // This ensures that even if the UI is slow to load the new tab,
                // the session can still close itself.
                newSession.contentDelegate = object : GeckoSession.ContentDelegate {
                    override fun onCloseRequest(s: GeckoSession) {
                        //Log.d("NewTabFlow", "onCloseRequest")

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
            ): GeckoResult<String>? {
                onLoadErrorFun(eventTabId, session, uri, error)
//                return GeckoResult.fromValue("about:blank")
                return null

            }

            override fun onLoadRequest(
                session: GeckoSession,
                request: GeckoSession.NavigationDelegate.LoadRequest
            ): GeckoResult<AllowOrDeny> {
                val uri = request.uri
                Log.i("marcEE", "uri onLoadRequest $uri")

                // 1. Check if this is a "Special" scheme (mailto, tel, intent, market)
                if (isExternalScheme(uri)) {
                    //Log.i("GeckoNav", "External scheme detected: $uri")

                    // Trigger the UI to launch the intent
                    onExternalAppRequest(uri)

                    // Tell Gecko NOT to load this in the webview
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }

                if (request.target == GeckoSession.NavigationDelegate.TARGET_WINDOW_NEW && isStandaloneMode) {

                    // Manually load the URL in the CURRENT session instead
                    session.loadUri(uri)

                    // DENY the request so GeckoView completely aborts the creation of a new session!
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }


                // 2. Allow normal web pages to load
                return GeckoResult.fromValue(AllowOrDeny.ALLOW)
            }
        }

        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            // EQUIVALENT TO onPageStarted on WebView
            override fun onPageStart(session: GeckoSession, url: String) {
                // ignore javascript injection
                if (url.startsWith("javascript:")) return

                onPageStartFun(eventTabId, session, url)
            }


            // EQUIVALENT TO onPageFinished
            override fun onPageStop(session: GeckoSession, success: Boolean) {

//                Log.d("marcW", "highlightColor ${browserSettings.value.highlightColor.toHexString()}")
//
//                if (browserSettings.value.isEnabledOutSync && success) {
//                    // inject js for design value
////                    val js = """
////                            javascript:void((function(){
////                            document.documentElement.style.setProperty('--device-corner-radius', '${browserSettings.value.deviceCornerRadius}px');
////                            document.documentElement.style.setProperty('--padding', '${browserSettings.value.padding}px');
////                            document.documentElement.style.setProperty('--single-line-height', '${browserSettings.value.singleLineHeight}px');
////                            document.documentElement.style.setProperty('--highlight-color', '${formatArgbToCss(browserSettings.value.highlightColor.toHexString())}');
////                            window.deviceCornerRadius = ${browserSettings.value.deviceCornerRadius};
////                            if (typeof window.render === 'function') window.render(${browserSettings.value.deviceCornerRadius});
////
////
////                            console.log("Injection Success! Radius is " + window.deviceCornerRadius);
////                            })());
////                            """
////                        .trimIndent()
////                        .replace("\n", " ")
////                    session.loadUri(js)
//
//                    val js = """
//                            javascript:void((function(){
//                                let scale = 1.0;
//                                let isDesktop = ${browserSettings.value.isDesktopMode};
//
//                                if (isDesktop) {
//
//                                    let screenW = window.screen.width;
//                                    if (!screenW || screenW >= 980) { screenW = 390; }
//
//                                    let viewportW = document.documentElement.clientWidth || window.innerWidth || 980;
//
//                                    if (viewportW > screenW) {
//                                        scale = viewportW / screenW;
//                                    }
//                                }
//
//                                let scaledRadius = ${browserSettings.value.deviceCornerRadius} * scale;
//                                let scaledPadding = ${browserSettings.value.padding} * scale;
//                                let scaledLineHeight = ${browserSettings.value.singleLineHeight} * scale;
//
//                                document.documentElement.style.setProperty('--device-corner-radius', scaledRadius + 'px');
//                                document.documentElement.style.setProperty('--padding', scaledPadding + 'px');
//                                document.documentElement.style.setProperty('--single-line-height', scaledLineHeight + 'px');
//                                document.documentElement.style.setProperty('--highlight-color', '${formatArgbToCss(browserSettings.value.highlightColor.toHexString())}');
//
//                                window.deviceCornerRadius = scaledRadius;
//                                if (typeof window.render === 'function') window.render(scaledRadius);
//
//                                console.log("Injection Success! Scaled Radius is " + scaledRadius + "px (Scale: " + scale + ")");
//                            })());
//                            """
//                        .trimIndent()
//                        .replace("\n", " ")
//                    session.loadUri(js)
//                }

                onPageStopFun(session, success)
                //Log.d("GeckoView", "Page Finished. Success: $success")

            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                onProgressChange(progress)
            }

            override fun onSessionStateChange(
                session: GeckoSession,
                state: GeckoSession.SessionState
            ) {

                //Log.d("marcState", "onSessionStateChange triggered")
                // save state to restore every session state change
                stateCache[eventTabId] = state

                onSessionStateChangeFun(eventTabId, session, state)
            }
        }

        session.contentDelegate = object : GeckoSession.ContentDelegate {

            override fun onCloseRequest(session: GeckoSession) {
                // This fires when JS window.close() is called
                //Log.i("GeckoNav", "Website requested to close tab: $eventTabId")
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

                onDownloadRequested(url, userAgent, contentDisposition, mimeType, response.body)
            }

            override fun onTitleChange(session: GeckoSession, title: String?) {
                title?.let { title ->
                    onTitleChangeFun(eventTabId, session, title)
                    if (session == activeMediaGeckoSession) {
                        context.startService(Intent(context, MediaPlaybackService::class.java).apply {
                            putExtra("TITLE", title)
                            putExtra("IS_PAUSED", isActiveMediaSessionPaused)
                        })
                    }
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

                            val iconUrl = if (iconPath.startsWith("http")) {
                                iconPath
                            } else {
                                java.net.URI(
                                    session.navigationDelegate?.toString() ?: ""
                                )
                                iconPath
                            }
                            // pass the favicon url out
                            onFaviconChanged(eventTabId, iconUrl)
                        }
                    }


                } catch (_: Exception) {
                    //Log.e("GeckoIcons", "Error parsing manifest", e)
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
                //Log.e("GeckoManager", "Session $session Crashed: ${session.isOpen}")
                // TODO handle auto crash session
                handleSessionDeath(session, eventTabId)
                onSessionCrash()

            }

            override fun onKill(session: GeckoSession) {
                // TODO handle auto crash session
                //Log.e("GeckoManager", "Session $session Killed by OS")
                handleSessionDeath(session, eventTabId)
                onSessionCrash()
            }

        }

        session.historyDelegate = object : GeckoSession.HistoryDelegate {
            override fun onHistoryStateChange(
                session: GeckoSession,
                realtimeHistory: GeckoSession.HistoryDelegate.HistoryList
            ) {
                //Log.i("GeckoNav","onHistoryStateChange")
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
            override fun onPopupPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.PopupPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {

                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                val targetUrl = prompt.targetUri ?: ""

                // We use onJsPrompt instead to give the user an editable OutlinedTextField!
                onJsPrompt(
                    context.getString(R.string.confirm_open_pop_up),
                    targetUrl
                ) { editedUrl ->

                    if (editedUrl != null) {
                        if (editedUrl == targetUrl) {
                            // 1. User kept the original URL.
                            // We ALLOW the native popup so it keeps its connection to the parent tab
                            // (crucial so things like "Sign in with Google" popups don't break).
                            result.complete(prompt.confirm(AllowOrDeny.ALLOW))
                        } else {
                            // 2. User typed a custom URL!
                            // We DENY the native popup, and trick the app into opening a brand new tab with their URL.
                            result.complete(prompt.confirm(AllowOrDeny.DENY))

                            val dummyId = System.currentTimeMillis()
                            MainScope().launch {
                                // This triggers MainActivity to open a new tab seamlessly
                                onNewSessionFunWithId(dummyId, editedUrl)
                            }
                        }
                    } else {
                        // 3. User clicked Dismiss.
                        result.complete(prompt.confirm(AllowOrDeny.DENY))
                    }
                }

                return result
            }
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
                } catch (_: Exception) {
                    //Log.e("GeckoShare", "Failed to start share sheet", e)
                }
                return GeckoResult.fromValue(prompt.dismiss())
            }

            override fun onAlertPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.AlertPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse> {
                val message = prompt.message ?: ""

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
                //Log.i("marcMedia", "onActivated")
                activeGeckoMediaSession = mediaSession
                activeMediaGeckoSession = session
                isActiveMediaSessionPaused = false
                val title = tab.value.currentTitle.ifBlank { tab.value.currentURL.toDomain() }

                // website started playing media! Start the background service.
                val intent = Intent(context, MediaPlaybackService::class.java).apply {
                    putExtra("TITLE", title)
                    putExtra("IS_PAUSED", false)
                }
                context.startForegroundService(intent)
            }

            override fun onDeactivated(session: GeckoSession, mediaSession: MediaSession) {
                if (activeGeckoMediaSession == mediaSession) {
                    activeGeckoMediaSession = null
                    activeMediaGeckoSession = null
                    isActiveMediaSessionPaused = true
                    context.stopService(Intent(context, MediaPlaybackService::class.java))
                }
            }

            // this is the primary way to know when user change video
            override fun onMetadata(
                session: GeckoSession,
                mediaSession: MediaSession,
                metadata: MediaSession.Metadata
            ) {
                //Log.i("marcMedia", "Playing: ${metadata.title} by ${metadata.artist}")

                if (metadata.title != null && metadata.title != lastTitle.value) {
                    lastTitle.value = metadata.title.toString()
                    startReset()
                    mediaSession.pause()
                    mediaSession.seekTo(0.0, true)
                    mediaSession.play()

                }
            }

            // not work
//            override fun onFeatures(
//                session: GeckoSession,
//                mediaSession: MediaSession,
//                features: Long
//            ) {
//                val isPaused = (features and MediaSession.Feature.PAUSE) == 0L
//                if (isPaused) mediaSession.pause() else mediaSession.play()
//                //Log.d("marcMedia", "Is Paused: $isPaused")
//                context.startService(Intent(context, MediaPlaybackService::class.java).apply {
//                    putExtra("IS_PAUSED", isPaused)
//                })
//            }

            override fun onPause(session: GeckoSession, mediaSession: MediaSession) {
                activeGeckoMediaSession = mediaSession

                isActiveMediaSessionPaused = true
                val title = tab.value.currentTitle.ifBlank { tab.value.currentURL.toDomain() }

                context.startService(Intent(context, MediaPlaybackService::class.java).apply {
                    putExtra("TITLE", title)
                    putExtra("IS_PAUSED", true)
                })
                super.onPause(session, mediaSession)
            }

            override fun onPlay(session: GeckoSession, mediaSession: MediaSession) {
                isActiveMediaSessionPaused = false
                activeGeckoMediaSession = mediaSession


                //Log.d("marcMedia", "onPlay")
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



