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

// A sealed interface to represent any type of JS Dialog
sealed interface JsDialogState

// Represents the "OK" button dialog from window.alert()
data class JsAlert(val message: String) : JsDialogState

// Represents the "OK" / "Cancel" dialog from window.confirm()
data class JsConfirm(val message: String, val onResult: (Boolean) -> Unit) : JsDialogState

// Represents the text input dialog from window.prompt()
data class JsPrompt(
    val message: String,
    val defaultValue: String,
    val onResult: (String?) -> Unit
) : JsDialogState
