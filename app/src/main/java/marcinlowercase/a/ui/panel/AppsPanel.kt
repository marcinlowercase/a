package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.App
import marcinlowercase.a.core.data_class.BrowserSettings

@Composable
fun AppsPanel(
    browserSettings: BrowserSettings,
    visibility: MutableState<Boolean>,
    apps: List<App>,
    onAppClick: (App) -> Unit = {} // Added callback
) {



    val maxPanelHeight = (browserSettings.heightForLayer(2).dp * 2.5f) +  (browserSettings.padding.dp * 2)

    AnimatedVisibility(
        visible = visibility.value,
        modifier = Modifier.fillMaxWidth()

    ) {


        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                // This ensures it grows up to 3 rows, then becomes scrollable
                .heightIn(max = maxPanelHeight)
                .padding(top = if (apps.isNotEmpty()) browserSettings.padding.dp else 0.dp)
                .padding(horizontal = browserSettings.padding.dp)
                .clip(RoundedCornerShape(browserSettings.cornerRadiusForLayer(2).dp))

//                .background(Color.Magenta)

            ,
//            contentPadding = PaddingValues(browserSettings.padding.dp),
            horizontalArrangement = Arrangement.spacedBy(browserSettings.padding.dp),
            verticalArrangement = Arrangement.spacedBy(browserSettings.padding.dp),
            reverseLayout = true
        ) {
            items(apps) { app ->
                AppIcon(
                    app = app,
                    browserSettings = browserSettings,

                    onClick = { onAppClick(app) },

                )
            }
        }
    }
}

@Composable
fun AppIcon(
    app: App,
    browserSettings: BrowserSettings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(browserSettings.cornerRadiusForLayer(2).dp))
                .height(browserSettings.heightForLayer(2).dp)
                .background(Color.White)
                .padding(2.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center

        ) {
            Box (
                modifier = Modifier
                    .size(20.dp)
            ){
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(app.iconUrl)
                            .size(100)
                            .crossfade(true)
                            .placeholder(R.drawable.ic_language) // Default icon
                            .error(R.drawable.ic_language)
                            .build()
                    ),
                    contentDescription = app.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

//        Spacer(modifier = Modifier.height(4.dp))
//
//        Text(
//            text = app.label,
//            color = Color.White,
//            maxLines = 1,
//            fontSize = 10.sp,
//            overflow = TextOverflow.Ellipsis,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.fillMaxWidth()
//        )
    }
}