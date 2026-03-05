package marcinlowercase.a.ui.panel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlin.math.ceil
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppsPanel(
    onAppClick: (App) -> Unit = {},
    confirmationPopup: (message: String, url: String, onConfirm: () -> Unit, onCancel: () -> Unit) -> Unit,

    ) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()


    val maxPanelHeight = (settings.value.heightForLayer(3).dp * settings.value.maxListHeight) + (settings.value.padding.dp * 2) + (if ( ceil(settings.value.maxListHeight).toInt() > 1) settings.value.padding.dp else 0.dp)
    val profiles = viewModel.profiles

    val realPageCount = profiles.size

    val currentProfileIdx =
        profiles.indexOfFirst { it.id == viewModel.activeProfileId.value }.coerceAtLeast(0)

    val initialPage = remember(realPageCount) {
        if (realPageCount <= 1) {
            0
        } else {
            (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % realPageCount) + currentProfileIdx
        }
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { if (realPageCount <= 1) 1 else Int.MAX_VALUE }
    )
    // When the user swipes and settles on a new page, trigger the profile switch in the backend
    LaunchedEffect(pagerState.settledPage) {
        val targetIndex = pagerState.settledPage % realPageCount
        val selectedProfile = profiles[targetIndex]
        if (selectedProfile.id != viewModel.activeProfileId.value) {
            viewModel.switchProfile(selectedProfile.id)
        }
    }
    // Keep pager visually in sync if the profile is switched externally (e.g. a new profile is created)
    LaunchedEffect(viewModel.activeProfileId.value, realPageCount) {
        val targetIndex =
            profiles.indexOfFirst { it.id == viewModel.activeProfileId.value }.coerceAtLeast(0)
        val currentIndex = pagerState.currentPage % realPageCount
        if (currentIndex != targetIndex) {
            // Find shortest animation path
            var diff = targetIndex - currentIndex
            if (diff > realPageCount / 2) diff -= realPageCount
            if (diff < -realPageCount / 2) diff += realPageCount
            pagerState.animateScrollToPage(pagerState.currentPage + diff)
        }
    }

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = realPageCount > 1

    ) { page ->
        val profileIndex = page % realPageCount
        val pageProfile = profiles[profileIndex]

        val pageApps = if (pageProfile.id == viewModel.activeProfileId.value) {
            viewModel.apps
        } else {
            remember(pageProfile.id) { viewModel.appManager.loadApps(pageProfile.id) }
        }

        val isDeletingProfile = remember { mutableStateOf(false) }
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
        ) {

            if (pageApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
//                            .height(settings.value.heightForLayer(2).dp)
                        .height(maxPanelHeight)
                        .padding(horizontal = settings.value.padding.dp)
                        .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
                        .background( when {
                            profiles.size <= 1 && isDeletingProfile.value->  Color.Gray
                            isDeletingProfile.value -> Color(settings.value.highlightColor)
                            else -> Color.Transparent
                        }
                        )
                        .clickable {
                            if (isDeletingProfile.value && profiles.size > 1) viewModel.deleteProfile(pageProfile.id)
                            if (isDeletingProfile.value && profiles.size <= 1)isDeletingProfile.value = false
                            else isDeletingProfile.value = true


                        },
                    contentAlignment = Alignment.Center


                ) {
                    Text(
                        text = when {
                            profiles.size <= 1 && isDeletingProfile.value->  "cannot delete profile ${profileIndex + 1} . "
                            isDeletingProfile.value -> "delete profile ${profileIndex + 1} ? "
                            else -> "profile ${profileIndex + 1}"
                        },
                        color = Color.White
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxPanelHeight)
                        .padding(horizontal = settings.value.padding.dp)
                        .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
                        .background(Color.White)
                        .padding(settings.value.padding.dp)
                        .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp)),
                    horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
                    verticalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
                ) {

                    items(pageApps) { app ->
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
                                viewModel.createNewTab(
                                    viewModel.activeTabIndex.value + 1,
                                    app.url
                                )
                                viewModel.updateUI { it.copy(isSettingsPanelVisible = false) }
                                viewModel.updateUI { it.copy(isUrlBarVisible = false) }
                            },
                            onLongClick = {
                                viewModel.inspectingAppId.longValue =
                                    if (viewModel.inspectingAppId.longValue != app.id) app.id else 0
                            }

                        )
                    }
                    val minSlots =  ceil(settings.value.maxListHeight).toInt() * 4
                    val placeholdersNeeded = (minSlots - pageApps.size).coerceAtLeast(0)

                    items(placeholdersNeeded) {
                        PlaceholderIcon()
                    }
                }


            }


        }

    }


}
@Composable
fun PlaceholderIcon(
    modifier: Modifier = Modifier,
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp))
            .height(settings.value.heightForLayer(3).dp)
            // Use same opacity as AppIcon background for consistency, or slightly lower to indicate "empty"
            .background(Color.Black.copy(settings.value.backSquareIdleOpacity * 0.5f))
            .fillMaxWidth()
        // No border, no click listeners
    )
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
            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp))
            .height(settings.value.heightForLayer(3).dp)
            .background(Color.Black.copy(settings.value.backSquareIdleOpacity))
            .padding(2.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick,

                )
            .border(
                width = 2.dp,
                color = if (viewModel.inspectingAppId.longValue == app.id) Color(settings.value.highlightColor) else Color.Transparent,
                shape = RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp)
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
                        .addHeader(
                            "User-Agent",
                            "Mozilla/5.0 (Android 14; Mobile; rv:130.0) Gecko/130.0 Firefox/130.0"
                        )
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