package marcinlowercase.a.core.data_class

import org.mozilla.geckoview.GeckoSession.PromptDelegate.DateTimePrompt
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession.PromptDelegate.PromptResponse

data class JsDateTimeState(
    val prompt: DateTimePrompt,
    val result: GeckoResult<PromptResponse>
)