package marcinlowercase.a.core.function

import marcinlowercase.a.core.custom_class.CustomWebView
import marcinlowercase.a.core.data_class.BrowserSettings
import org.mozilla.geckoview.GeckoSession


fun webViewLoad(view: CustomWebView?, url: String, browserSettings: BrowserSettings) {
    val headerinlowercase = mutableMapOf<String, String>()

    // i want it send the string of "device_corner_radius" with the value of browserSettings.deviceCornerRadius
    headerinlowercase["device_corner_radius"] = browserSettings.deviceCornerRadius.toString()

    view?.loadUrl(url, headerinlowercase)
}


fun webViewLoad(session: GeckoSession?, url: String, browserSettings: BrowserSettings) {
    session?.load(GeckoSession.Loader()
        .uri(url)
//        .data(browserSettings.deviceCornerRadius.toString(), "text/plain")
    )
}