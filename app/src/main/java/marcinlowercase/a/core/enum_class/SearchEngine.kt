/*
 * Copyright (C) 2026 marcinlowercase
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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