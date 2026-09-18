package marcinlowercase.a.core.data_class

import org.mozilla.geckoview.GeckoSession.PromptDelegate.ColorPrompt
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession.PromptDelegate.PromptResponse

data class JsColorState(
    val prompt: ColorPrompt,
    val result: GeckoResult<PromptResponse>
)
