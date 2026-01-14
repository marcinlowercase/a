package marcinlowercase.a.core.manager

import android.content.Context
import androidx.compose.runtime.MutableState
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.Tab
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings

class GeckoManager(private val context: Context) {

    private val runtime: GeckoRuntime by lazy {
        GeckoRuntime.getDefault(context)
    }

    private val sessionPool = mutableMapOf<Long, GeckoSession>()




    fun getSession(tab: Tab): GeckoSession {
        return sessionPool.getOrPut(tab.id) {
            createAndConfigureSession(tab)
        }
    }

    fun closeSession(tab: Tab) {
        sessionPool.remove(tab.id)?.apply {
            close()
        }
    }
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
            // GeckoView state restoration logic
            // Note: GeckoView uses a SessionState object, not a generic Bundle/Byte array like WebView.
            // You might need to change your Tab data class to store this differently
            // or serialize GeckoSession.SessionState to a byte array manually.
        }

        // Connect the session to the engine
        session.open(runtime)

        // If it's a new tab without state, load the URL
        if (session.navigationDelegate == null && tab.savedState == null) {
            session.loadUri(tab.currentURL)
        }

        return session
    }


    fun setupDelegates(
        session: GeckoSession,
        tab: Tab,
        browserSettings: MutableState<BrowserSettings>,
        onTitleChange: (String) -> Unit,
        onProgressChange: (Int) -> Unit,
        onUrlChange: (String) -> Unit,
//        onJsAlert: (String) -> Unit,
//        onJsConfirm: (String, (Boolean) -> Unit) -> Unit,
//        onJsPrompt: (String, String, (String?) -> Unit) -> Unit,
        // Add other callbacks as needed...
    ) {

        // 1. Navigation Delegate (Url changes, History)
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(session: GeckoSession, url: String?, perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>, userGesture: Boolean) {
                url?.let { onUrlChange(it) }
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                // Update UI state if needed
            }
        }

        // 2. Progress Delegate (Loading bar)
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onProgressChange(session: GeckoSession, progress: Int) {
                onProgressChange(progress)
            }
        }

        // 3. Content Delegate (Title, Fullscreen, Context Menu)
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                title?.let { onTitleChange(it) }
            }

            override fun onContextMenu(session: GeckoSession, screenX: Int, screenY: Int, element: GeckoSession.ContentDelegate.ContextElement) {
                // GeckoView handles context menu detection differently.
                // 'element' contains the type (IMAGE, VIDEO, LINK) and the URL.
                // You can map this to your ContextMenuData.
            }
        }

    }
}
