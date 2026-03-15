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
package marcinlowercase.a.core.js_interface

import android.webkit.JavascriptInterface

class FaviconJavascriptInterface(
    private val onFaviconUrlFound: (String) -> Unit
) {
    @Suppress("unused")
    @JavascriptInterface
    fun passFaviconUrl(absoluteIconUrl: String?) {
//

        if (absoluteIconUrl != null) {
            onFaviconUrlFound(absoluteIconUrl)
        } else {
            // Here you can decide on a fallback. Maybe do nothing and let Coil show a placeholder.
            // Or you can try the /favicon.ico, but it's less reliable.
        }
    }
}