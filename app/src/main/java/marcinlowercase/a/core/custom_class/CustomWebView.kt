package marcinlowercase.a.core.custom_class

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.webkit.WebView

class CustomWebView(context: Context) : WebView(context) {

    var onWebViewTouch: (() -> Unit)? = null

    /**
     * This method is called for every touch event on the WebView.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // 2. We only care about the beginning of a touch gesture.
        if (event?.action == MotionEvent.ACTION_DOWN) {
            // 3. If the user starts touching the screen, invoke our callback.
            onWebViewTouch?.invoke()
        }
        // 4. IMPORTANT: We must call super to let the WebView handle scrolling,
        // clicking, and other gestures normally.
        return super.onTouchEvent(event)
    }

}
