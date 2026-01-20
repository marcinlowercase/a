package marcinlowercase.a.core.data_class

import org.mozilla.geckoview.WebRequestError

data class ErrorState(

    val failingUrl: String,
    val error: WebRequestError,
)
