 
package marcinlowercase.a.core.function

import org.mozilla.geckoview.GeckoSession

fun webViewLoad(session: GeckoSession?, url: String) {
    session?.load(GeckoSession.Loader()
        .uri(url)
        .flags(GeckoSession.LOAD_FLAGS_NONE)

//        .data(browserSettings.currentCornerRadius.toString(), "text/plain")
    )
}