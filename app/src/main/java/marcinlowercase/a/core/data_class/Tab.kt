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
package marcinlowercase.a.core.data_class

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import marcinlowercase.a.core.constant.DefaultSettingValues
import marcinlowercase.a.core.enum_class.TabState

@Serializable
data class Tab(
    val id: Long = System.currentTimeMillis(),
    var state: TabState = TabState.BACKGROUND,
    var currentURL: String,
    var currentTitle: String = "",
    var currentFaviconUrl: String = currentURL,
    var savedState: String? = null,
    var profileId: String,
    var isStandalone: Boolean = false,

    val faviconCache: Map<String, String> = emptyMap(),


    @Transient var canGoBack: Boolean = false,
    @Transient var canGoForward: Boolean = false,
    @Transient var errorState: ErrorState? = null

) {
    companion object {
        fun createEmpty(profileId: String,id: Long = System.currentTimeMillis()): Tab {
            return Tab(profileId = profileId, id = id, currentURL = DefaultSettingValues.URL)
        }
    }
}
