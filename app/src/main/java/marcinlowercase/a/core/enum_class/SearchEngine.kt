package marcinlowercase.a.core.enum_class


enum class SearchEngine(
    val title: String,
    val baseUrl: String,
    val suggestionUrl: String // New field
) {
    GOOGLE(
        "Google",
        "https://www.google.com/search?q=",
        "https://suggestqueries.google.com/complete/search?client=chrome&ie=UTF-8&oe=UTF-8&q="
    ),
    BING(
        "Bing",
        "https://www.bing.com/search?q=",
        "https://api.bing.com/osjson.aspx?query="
    ),
    DUCKDUCKGO(
        "DuckDuckGo",
        "https://duckduckgo.com/?q=",
        "https://duckduckgo.com/ac/?type=list&q="
    ),
    YAHOO(
        "Yahoo",
        "https://search.yahoo.com/search?p=",
        "https://search.yahoo.com/sugg/gossip/gossip-us-ura/?output=json&command="
    ),
    ECOSIA(
        "Ecosia",
        "https://www.ecosia.org/search?q=",
        "https://ac.ecosia.org/autocomplete?type=list&q="
    ),
    YANDEX(
        "Yandex",
        "https://yandex.com/search/?text=",
        "https://suggest.yandex.com/suggest-ya.cgi?v=4&part="
    ),
    ;

    fun getSearchUrl(query: String) = "$baseUrl$query"
    fun getSuggestionUrl(query: String) = "$suggestionUrl$query"
}