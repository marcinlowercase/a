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
import androidx.compose.runtime.collectAsState
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
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel

@Composable
fun AppsPanel(
    onAppClick: (App) -> Unit = {},
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()


    val maxPanelHeight =
        (settings.value.heightForLayer(2).dp * 2.5f) + (settings.value.padding.dp * 2)

    AnimatedVisibility(
        visible = uiState.value.isAppsPanelVisible,
        modifier = Modifier.fillMaxWidth()

    ) {
        if (viewModel.apps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(settings.value.heightForLayer(2).dp)
                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp)),
                contentAlignment = Alignment.Center


            ) {
                Text(
                    text ="no app pinned .",
                    color = Color.White
                    )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    // This ensures it grows up to 3 rows, then becomes scrollable
                    .heightIn(
                        min = settings.value.heightForLayer(2).dp,
                        max = maxPanelHeight
                    )
                    .padding(top = if (viewModel.apps.isNotEmpty()) settings.value.padding.dp else 0.dp)
                    .padding(horizontal = settings.value.padding.dp)
                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))

//                .background(Color.Magenta)

                ,
//            contentPadding = PaddingValues(settings.value.padding.dp),
                horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
                verticalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
                reverseLayout = true
            ) {

                items(viewModel.apps) { app ->
                    AppIcon(
                        app = app,
                        onClick = {
                            if (viewModel.inspectingAppId.longValue != 0L && viewModel.inspectingAppId.longValue != app.id) {
                                viewModel.inspectingAppId.longValue = app.id
                            } else {
                                onAppClick(app)

                            }
                        },
                        onDoubleClick = {
                            viewModel.createNewTab(viewModel.activeTabIndex.value + 1, app.url)
                            viewModel.updateUI { it.copy(isSettingsPanelVisible = false) }
                            viewModel.updateUI { it.copy(isUrlBarVisible = false) }
                        },
                        onLongClick = {
                            viewModel.inspectingAppId.longValue =
                                if (viewModel.inspectingAppId.longValue != app.id) app.id else 0
                        }

                    )
                }
            }
        }

    }
}

@Composable
fun AppIcon(
    app: App,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {}
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()


    Box(
        modifier = modifier
            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
            .height(settings.value.heightForLayer(2).dp)
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
                color = if (viewModel.inspectingAppId.longValue == app.id) Color.Red else Color.Transparent,
                shape = RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp)
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
                        .addHeader("User-Agent", "Mozilla/5.0 (Android 14; Mobile; rv:130.0) Gecko/130.0 Firefox/130.0")
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