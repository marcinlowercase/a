package marcinlowercase.a.core.function

import android.util.Log
import marcinlowercase.a.core.custom_class.CustomWebView
import marcinlowercase.a.core.data_class.BrowserSettings
import org.mozilla.geckoview.GeckoSession

fun webViewLoad(session: GeckoSession?, url: String, browserSettings: BrowserSettings) {
    Log.i("marcW", "webViewLoad: $url")
    session?.load(GeckoSession.Loader()
        .uri(url)
        .flags(GeckoSession.LOAD_FLAGS_NONE)

//        .data(browserSettings.deviceCornerRadius.toString(), "text/plain")
    )
}