package marcinlowercase.a.core.data_class

import marcinlowercase.a.core.enum_class.SuggestionSource

data class Suggestion(
    val text: String,
    val source: SuggestionSource,
    val url: String // For history, this is the direct URL. For Google, it's the search query URL.
)
