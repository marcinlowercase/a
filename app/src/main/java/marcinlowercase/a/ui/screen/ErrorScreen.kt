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
import org.mozilla.geckoview.WebRequestError

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    errorState: ErrorState,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {

    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState().value
val settings = viewModel.browserSettings.collectAsState().value
    
    val userMessage = when (errorState.error.category) {
        WebRequestError.ERROR_CATEGORY_NETWORK -> "no connection"
        WebRequestError.ERROR_CATEGORY_SECURITY -> "this site is not secure"
        WebRequestError.ERROR_CATEGORY_URI -> "this site cannot be reached"
        WebRequestError.ERROR_CATEGORY_CONTENT -> "this site cannot be displayed"
        else -> "error"
    }

    val errorIcon = when (errorState.error.category) {
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
            .clip(RoundedCornerShape(settings.deviceCornerRadius.dp))
            .background(Color.White) // Or dynamic theme color
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            // Icon
//            Icon(
//                painter = painterResource(id = errorIcon), // Need an icon
//                contentDescription = null,
//                tint = Color.Black,
//                modifier = Modifier.size(64.dp)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))


            // Message
            Text(
                text = userMessage,
//                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(settings.padding.dp))

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