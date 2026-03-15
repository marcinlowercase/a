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
package marcinlowercase.a.core.function

import android.util.Log
import marcinlowercase.a.core.custom_class.CustomWebView
import marcinlowercase.a.core.data_class.BrowserSettings
import org.mozilla.geckoview.GeckoSession

fun webViewLoad(session: GeckoSession?, url: String, browserSettings: BrowserSettings) {
    session?.load(GeckoSession.Loader()
        .uri(url)
        .flags(GeckoSession.LOAD_FLAGS_NONE)

//        .data(browserSettings.deviceCornerRadius.toString(), "text/plain")
    )
}