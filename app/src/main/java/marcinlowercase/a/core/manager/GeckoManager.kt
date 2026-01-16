package marcinlowercase.a.core.manager

import android.content.Context
import android.os.Parcel
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import marcinlowercase.a.core.custom_class.CustomPermissionDelegate
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.CustomPermissionRequest
import marcinlowercase.a.core.data_class.Tab
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings

class GeckoManager(private val context: Context) {

    val runtime: GeckoRuntime by lazy {
        GeckoRuntime.getDefault(context)
    }
    private val stateCache = mutableMapOf<Long, GeckoSession.SessionState>()


    private val sessionPool = mutableMapOf<Long, GeckoSession>()
    private val killedSessionIds = mutableSetOf<Long>()



    fun getSession(tab: Tab): GeckoSession {
        val session =  sessionPool.getOrPut(tab.id) {
            createAndConfigureSession(tab)
        }

        if(!session.isOpen){
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

    // )
    private fun createAndConfigureSession(tab: Tab): GeckoSession {
        val settings = GeckoSessionSettings.Builder()
            .usePrivateMode(false) // Set based on your Incognito logic
            .userAgentMode(GeckoSessionSettings.USER_AGENT_MODE_MOBILE)
            .suspendMediaWhenInactive(true)
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
        } catch (e: Exception) {
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


    fun setupDelegates(
        session: GeckoSession,
        tab: Tab,
        browserSettings: MutableState<BrowserSettings>,
        onTitleChangeFun: (GeckoSession, String) -> Unit,
        onNewSessionFun: (session: GeckoSession, uri: String) -> Unit,
        onProgressChange: (Int) -> Unit,
        onLocationChangeFun: (session: GeckoSession, url: String?, perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>, userGesture: Boolean) -> Unit,

        onHistoryStateChangeFun: (
            session: GeckoSession,
            realtimeHistory: GeckoSession.HistoryDelegate.HistoryList
        ) -> Unit,
        onSessionStateChangeFun: (
            session: GeckoSession,
            state: GeckoSession.SessionState
        ) -> Unit,
        onCanGoBackFun: (session: GeckoSession, canGoBack: Boolean)-> Unit,
        onCanGoForwardFun: (session: GeckoSession, canGoForward: Boolean)-> Unit,
        setPermissionDelegate: (request: CustomPermissionRequest )-> Unit,
        onShowAndroidRequest: (
            permissions: Array<out String>?,
            callback: GeckoSession.PermissionDelegate.Callback
        ) -> Unit,
    ) {

        if (killedSessionIds.contains(tab.id)) {
            Log.i("GeckoManager", "Resurrecting killed session: ${tab.id}")

            // A. Ensure session is open (in case the whole session closed)
            if (!session.isOpen) {
                session.open(runtime)
            }

            // B. Try to restore exact state from our memory cache first
            val cachedState = stateCache[tab.id]
            if (cachedState != null) {
                session.restoreState(cachedState)
            } else {
                // Fallback: If no cache, just reload the URL
                session.reload()
            }

            // C. Remove from the kill list so we don't restore loop
            killedSessionIds.remove(tab.id)
        }

        // 1. Navigation Delegate (Url changes, History)
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                userGesture: Boolean
            ) {
                onLocationChangeFun(session, url, perms, userGesture)
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
        }

        // 2. Progress Delegate (Loading bar)
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onProgressChange(session: GeckoSession, progress: Int) {
                onProgressChange(progress)
            }

            override fun onSessionStateChange(
                session: GeckoSession,
                state: GeckoSession.SessionState
            ) {

                stateCache[tab.id] = state
                Log.e("onSessionStateChange", " state ${state}")
                Log.i("onSessionStateChange", "saved state to session # ${tab.id}")
                onSessionStateChangeFun(session, state)

            }

        }

        // 3. Content Delegate (Title, Fullscreen, Context Menu)
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                title?.let { title ->
                    onTitleChangeFun(session, title)
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
            }
            override fun onCrash(session: GeckoSession) {
                // The Content Process died (Crash or OOM Kill)
                Log.e("GeckoManager", "Session $session Crashed: ${session.isOpen}")

                // If this session is currently visible (Active), reload it immediately.
                // If it's in the background, GeckoView will usually auto-reload it
                // when it becomes active again (thanks to suspendMediaWhenInactive=true).
                handleSessionDeath(session, tab.id)

            }

            override fun onKill(session: GeckoSession) {
                // Similar to onCrash, but specifically for OS kills
                Log.e("GeckoManager", "Session $session Killed by OS")
                handleSessionDeath(session, tab.id)
            }

        }

        session.historyDelegate = object : GeckoSession.HistoryDelegate {
            override fun onHistoryStateChange(
                session: GeckoSession,
                realtimeHistory: GeckoSession.HistoryDelegate.HistoryList
            ) {
                onHistoryStateChangeFun(session, realtimeHistory)
            }
        }

        session.permissionDelegate = CustomPermissionDelegate (

            context = context,
            onShowRequest = { request ->
                setPermissionDelegate(request)
            },
            onShowAndroidRequest = onShowAndroidRequest
        )

    }
}


