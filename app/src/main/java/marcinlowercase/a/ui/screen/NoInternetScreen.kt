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
package marcinlowercase.a.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import marcinlowercase.a.core.data_class.BrowserSettings

@Composable
fun NoInternetScreen(
    webViewTopPadding: Dp,
    webViewBottomPadding: Dp,
    browserSettings: BrowserSettings,
) {
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(top = webViewTopPadding, bottom =  webViewBottomPadding)
            .clip(RoundedCornerShape(browserSettings.cornerRadiusForLayer(0).dp))
            .background(Color.White),
        contentAlignment = Alignment.Center,

    ) {
        Text("no connection", color = Color.Black)
    }

}