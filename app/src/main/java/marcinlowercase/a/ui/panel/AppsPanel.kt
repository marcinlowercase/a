package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
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
    browserSettings: MutableState<BrowserSettings>,
    visibility: MutableState<Boolean>,
    apps: MutableList<App>,
    onAppClick: (App) -> Unit = {},
    inspectingAppId: MutableState<Long>

) {


    val maxPanelHeight =
        (browserSettings.value.heightForLayer(2).dp * 2.5f) + (browserSettings.value.padding.dp * 2)

    AnimatedVisibility(
        visible = visibility.value,
        modifier = Modifier.fillMaxWidth()

    ) {
        if (apps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(browserSettings.value.heightForLayer(2).dp)
                    .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(2).dp)),
                contentAlignment = Alignment.Center


            ) {
                Text("no app pinned .")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    // This ensures it grows up to 3 rows, then becomes scrollable
                    .heightIn(
                        min = browserSettings.value.heightForLayer(2).dp,
                        max = maxPanelHeight
                    )
                    .padding(top = if (apps.isNotEmpty()) browserSettings.value.padding.dp else 0.dp)
                    .padding(horizontal = browserSettings.value.padding.dp)
                    .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(2).dp))

//                .background(Color.Magenta)

                ,
//            contentPadding = PaddingValues(browserSettings.value.padding.dp),
                horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp),
                verticalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp),
                reverseLayout = true
            ) {

                items(apps) { app ->
                    AppIcon(
                        app = app,
                        browserSettings = browserSettings,
                        inspectingAppId = inspectingAppId,

                        onClick = {
                            if (inspectingAppId.value != 0L && inspectingAppId.value != app.id) {
                                inspectingAppId.value = app.id
                            } else {
                                onAppClick(app)

                            }
                        },
                        onDoubleClick = {
                            apps.indexOfFirst { it.id == app.id }
                                .takeIf { it >= 0 }
                                ?.let { apps.removeAt(it) }
                            if (inspectingAppId.value == app.id) inspectingAppId.value = 0L
                        },
                        onLongClick = {
                            inspectingAppId.value =
                                if (inspectingAppId.value != app.id) app.id else 0
                        }

                    )
                }
            }
        }

    }
}

@Composable
fun AppIcon(
    inspectingAppId: MutableState<Long>,
    app: App,
    browserSettings: MutableState<BrowserSettings>,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(2).dp))
            .height(browserSettings.value.heightForLayer(2).dp)
            .background(Color.White)
            .padding(2.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick,

                )
            .border(
                width = 2.dp,
                color = if (inspectingAppId.value == app.id) Color.Red else Color.Transparent,
                shape = RoundedCornerShape(browserSettings.value.cornerRadiusForLayer(2).dp)
            ),
        contentAlignment = Alignment.Center

    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
        ) {
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
}