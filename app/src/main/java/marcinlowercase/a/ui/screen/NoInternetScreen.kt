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