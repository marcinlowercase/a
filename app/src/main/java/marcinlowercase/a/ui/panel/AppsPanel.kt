package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.App
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import kotlin.math.ceil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppsPanel(
    onAppClick: (App) -> Unit = {},
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()

    // Panel Height Calculation
    val maxPanelHeight =
        (settings.value.heightForLayer(3).dp * settings.value.maxListHeight) +
                (settings.value.padding.dp * 2) +
                (if (ceil(settings.value.maxListHeight).toInt() > 1) settings.value.padding.dp else 0.dp)

    val profiles = viewModel.profiles
    val realPageCount = profiles.size
    val currentProfileIdx = profiles.indexOfFirst { it.id == viewModel.activeProfileId.value }.coerceAtLeast(0)

    val initialPage = remember(realPageCount) {
        if (realPageCount <= 1) 0 else (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % realPageCount) + currentProfileIdx
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { if (realPageCount <= 1) 1 else Int.MAX_VALUE }
    )

    // Pager Logic
    LaunchedEffect(pagerState.settledPage) {
        val targetIndex = pagerState.settledPage % realPageCount
        val selectedProfile = profiles[targetIndex]
        if (selectedProfile.id != viewModel.activeProfileId.value) {
            viewModel.switchProfile(selectedProfile.id)
        }
    }
    LaunchedEffect(viewModel.activeProfileId.value, realPageCount) {
        val targetIndex = profiles.indexOfFirst { it.id == viewModel.activeProfileId.value }.coerceAtLeast(0)
        val currentIndex = pagerState.currentPage % realPageCount
        if (currentIndex != targetIndex) {
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

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
        ) {
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
                var visualItemCount = 0
                val inspectingId = viewModel.inspectingAppId.longValue

                // We loop manually to inject items.
                // CRITICAL: We provide unique keys for every item so animations work.
                pageApps.forEachIndexed { index, app ->
                    val isInspectingThisApp = (inspectingId == app.id)

                    // 1. Move Backward Button
                    if (isInspectingThisApp && index > 0) {
                        item(key = "prev_${app.id}", contentType = "action_button") {
                            // Wrapper for entry animation
                            AnimatedVisibility(
                                visible = true,
                                enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
                                modifier = Modifier.animateItem() // Slide layout
                            ) {
                                PlaceholderIcon(
                                    iconRes = R.drawable.ic_arrow_upward,
                                    onClick = { viewModel.swapApps(index, index - 1) }
                                )
                            }
                        }
                        visualItemCount++
                    }

                    // 2. The App Icon
                    item(key = "app_${app.id}", contentType = "app") {
                        AppIcon(
                            app = app,
                            onClick = {
                                if (!pagerState.isScrollInProgress) {
                                    if (inspectingId != 0L && inspectingId != app.id) {
                                        viewModel.inspectingAppId.longValue = app.id
                                    } else {
                                        onAppClick(app)
                                    }
                                }
                            },
                            onDoubleClick = {
                                if (!pagerState.isScrollInProgress) {
                                    viewModel.createNewTab(viewModel.activeTabIndex.value + 1, app.url)
                                    viewModel.updateUI { it.copy(isSettingsPanelVisible = false, isUrlBarVisible = false) }
                                }
                            },
                            onLongClick = {
                                if (!pagerState.isScrollInProgress) {
                                    viewModel.inspectingAppId.longValue = if (inspectingId != app.id) app.id else 0L
                                }
                            },
                            // Add layout animation modifier
                            modifier = Modifier.animateItem()
                        )
                    }
                    visualItemCount++

                    // 3. Move Forward Button
                    if (isInspectingThisApp && index < pageApps.size - 1) {
                        item(key = "next_${app.id}", contentType = "action_button") {
                            AnimatedVisibility(
                                visible = true,
                                enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
                                modifier = Modifier.animateItem()
                            ) {
                                PlaceholderIcon(
                                    iconRes = R.drawable.ic_arrow_downward,
                                    onClick = { viewModel.swapApps(index, index + 1) }
                                )
                            }
                        }
                        visualItemCount++
                    }

                    // 4. Delete Button
                    if (isInspectingThisApp) {
                        item(key = "del_${app.id}", contentType = "action_button") {
                            AnimatedVisibility(
                                visible = true,
                                enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
                                modifier = Modifier.animateItem()
                            ) {
                                PlaceholderIcon(
                                    iconRes = R.drawable.ic_delete_forever,
                                    isDestructive = true,
                                    onClick = {
                                        viewModel.removeApp(app.id)
                                        viewModel.inspectingAppId.longValue = 0L
                                    }
                                )
                            }
                        }
                        visualItemCount++
                    }
                }

                // --- FOOTER / PLACEHOLDERS ---

                val minSlots = ceil(settings.value.maxListHeight).toInt() * 4
                val columns = 4
                val remainder = visualItemCount % columns
                val needsGapFiller = remainder == 3

                if (needsGapFiller) {
                    item(
                        span = { GridItemSpan(1) },
                        key = "gap_filler_page_${pageProfile.id}", // Stable key
                        contentType = "placeholder"
                    ) {
                        PlaceholderIcon(modifier = Modifier.animateItem())
                    }
                    visualItemCount++
                }

                // Profile Name
                item(
                    span = { GridItemSpan(2) },
                    key = "profile_name_${pageProfile.id}", // Stable key
                    contentType = "profile_header"
                ) {
                    val profileName = "profile ${profileIndex + 1}"
                    PlaceholderIcon(text = profileName, modifier = Modifier.animateItem())
                }
                visualItemCount += 2

                // Remaining placeholders
                val remainingPlaceholders = (minSlots - visualItemCount).coerceAtLeast(0)

                // We use `items` with a key factory based on index to ensure stability
                items(
                    count = remainingPlaceholders,
                    key = { index -> "empty_slot_${pageProfile.id}_$index" },
                    contentType = { "placeholder" }
                ) {
                    PlaceholderIcon(modifier = Modifier.animateItem())
                }
            }
        }
    }
}

@Composable
fun PlaceholderIcon(
    modifier: Modifier = Modifier,
    text: String? = null,
    iconRes: Int? = null,
    onClick: (() -> Unit)? = null,
    isDestructive: Boolean = false
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()

    val backgroundColor = if (isDestructive) {
        Color(settings.value.highlightColor)
    } else {
        Color.Black.copy(settings.value.backSquareIdleOpacity * (if (text == null && iconRes == null) 0.5f else 1f))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp))
            .height(settings.value.heightForLayer(3).dp)
            .background(backgroundColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(
                start = if (text != null) settings.value.cornerRadiusForLayer(3).dp else 0.dp
            )
            .fillMaxWidth(),
        contentAlignment = if (text != null) Alignment.CenterStart else Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                color = Color.Black
            )
        } else if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = if (isDestructive) Color.White else Color.Black,
                modifier = Modifier.size(24.dp)
            )
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
            modifier = Modifier.size(20.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .addHeader("User-Agent", "Mozilla/5.0 (Android 14; Mobile; rv:130.0) Gecko/130.0 Firefox/130.0")
                        .data(app.iconUrl)
                        .size(100)
                        .crossfade(true)
                        .placeholder(R.drawable.ic_language)
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