package marcinlowercase.a

import android.content.Context
import android.graphics.Rect
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import org.mozilla.geckoview.GeckoView

class SafeGeckoView(context: Context) : GeckoView(context) {
    var allowInputConnection = true
    private var suppressFocusRequest = false

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        if (suppressFocusRequest) return false
        return super.requestFocus(direction, previouslyFocusedRect)
    }

    fun wakeCompositor(preserveExternalFocus: Boolean = true) {
        post {
            if (preserveExternalFocus) suppressFocusRequest = true
            try {
                val now = SystemClock.uptimeMillis()
                val downEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, 0f, 0f, 0).apply {
                    source = InputDevice.SOURCE_UNKNOWN
                }
                val cancelEvent = MotionEvent.obtain(now, now + 10, MotionEvent.ACTION_CANCEL, 0f, 0f, 0).apply {
                    source = InputDevice.SOURCE_UNKNOWN
                }
                dispatchTouchEvent(downEvent)
                dispatchTouchEvent(cancelEvent)
                downEvent.recycle()
                cancelEvent.recycle()
            } finally {
                suppressFocusRequest = false
            }
            requestLayout()
            invalidate()
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child?.requestLayout()
                child?.invalidate()
                if (child is ViewGroup) {
                    for (j in 0 until child.childCount) {
                        child.getChildAt(j)?.invalidate()
                    }
                }
            }
        }
    }

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