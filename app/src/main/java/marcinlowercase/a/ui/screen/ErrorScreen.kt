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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.ErrorState
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import org.mozilla.geckoview.WebRequestError

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    errorState: ErrorState
) {

    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()

    val noConnectionString = stringResource(R.string.error_no_connection)
    val notSecureString = stringResource(R.string.error_not_secure)
    val cannotBeReachedString = stringResource(R.string.error_cannot_be_reached)
    val cannotBeDisplayedString = stringResource(R.string.error_cannot_be_displayed)
    val genericErrorString = "Error" // Or use another stringResource

// 2. Apply them to your logic
    val userMessage = when (errorState.error.category) {
        WebRequestError.ERROR_CATEGORY_NETWORK -> noConnectionString
        WebRequestError.ERROR_CATEGORY_SECURITY -> notSecureString
        WebRequestError.ERROR_CATEGORY_URI -> cannotBeReachedString
        WebRequestError.ERROR_CATEGORY_CONTENT -> cannotBeDisplayedString
        else -> genericErrorString
    }

    when (errorState.error.category) {
        WebRequestError.ERROR_CATEGORY_NETWORK -> R.drawable.ic_signal_wifi_off
        WebRequestError.ERROR_CATEGORY_SECURITY -> R.drawable.ic_gpp_bad
        WebRequestError.ERROR_CATEGORY_URI -> R.drawable.ic_link_off
        WebRequestError.ERROR_CATEGORY_CONTENT -> R.drawable.ic_dangerous
        else -> R.drawable.ic_bug
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(enabled = false) {} // Prevent clicking through to WebView
            .clip(RoundedCornerShape(settings.value.deviceCornerRadius.dp))
            .background(Color.White) // Or dynamic theme color
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Message
            Text(
                text = userMessage,
//                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(settings.value.padding.dp))

            // URL (Optional, make it smaller)
            Text(
                text = errorState.failingUrl,
//                style = MaterialTheme.typography.labelSmall,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

//            Spacer(modifier = Modifier.height(32.dp))

//            // Buttons
//            Row {
//                Button(onClick = onHome) {
//                    Text("Home")
//                }
//                Spacer(modifier = Modifier.width(16.dp))
//                Button(onClick = onRetry) {
//                    Text("Try Again")
//                }
//            }
        }
    }
}