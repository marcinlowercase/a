package marcinlowercase.a.core.data_class
import org.mozilla.geckoview.GeckoSession.PromptDelegate.ChoicePrompt
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession.PromptDelegate.PromptResponse

data class JsChoiceState(
    val prompt: ChoicePrompt,
    val result: GeckoResult<PromptResponse>
)