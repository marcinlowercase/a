package marcinlowercase.a

import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import org.mozilla.geckoview.GeckoView

class SafeGeckoView(context: Context) : GeckoView(context) {
    var allowInputConnection = true

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        if (!allowInputConnection) {
            // Forcefully return null to sever the connection between Gecko and the Keyboard
            return null
        }
        return super.onCreateInputConnection(outAttrs)
    }

    /**
     * Call this when the app goes to the background.
     */
    fun detachKeyboard() {
        allowInputConnection = false
        clearFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
        // Force the system to realize the connection is dead
        imm.restartInput(this)
    }

    /**
     * Call this when the app comes back to the foreground.
     */
    fun reattachKeyboard() {
        allowInputConnection = true
        // Tell the system it can ask for an InputConnection again if needed
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.restartInput(this)
    }
}