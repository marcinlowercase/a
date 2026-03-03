package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.function.getFaviconUrlFromGoogleServer
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun NewTabButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    Box(
        modifier = modifier.padding(settings.value.padding.dp)
    )
    {
        Box(
            modifier = modifier

                .padding(horizontal = settings.value.padding.dp)
                .clip(
                    RoundedCornerShape(
                        settings.value.cornerRadiusForLayer(3).dp
                    )
                )
                .clickable(onClick = onClick)
                .background(Color.Black.copy(alpha = 0.2f))
                .height(
                    settings.value.heightForLayer(3).dp
                )
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "New Tab",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun TabItem(
    faviconUrl: String,
    title: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current
    Box(

        modifier = Modifier
            .padding(settings.value.padding.dp)
            .clip(
                RoundedCornerShape(
                    settings.value.cornerRadiusForLayer(3).dp
                )
            )
    ) {
        Row(
            modifier = Modifier
                // Make the entire item clickable
//                .clickable(onClick = onClick)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClick()
                        }
                    )
                }
                .height(
                    settings.value.heightForLayer(3).dp
                )
                .clip(
                    RoundedCornerShape(
                        settings.value.cornerRadiusForLayer(3).dp
                    )
                )
                .background(if (isActive) Color.White else Color.White.copy(alpha = 0.5f)) // Different background for inactive
                .padding(horizontal = settings.value.padding.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(settings.value.padding.dp)

            ) {

                Box(
                    modifier = Modifier
                        .size(24.dp),

//                        .clip(RoundedCornerShape(settings.value.padding.dp / 2)),
//                        .background(Color.White.copy(alpha = 0.2f)),
//                        .background(if (isActive) Color.White else Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    val imageSizePx = with(LocalDensity.current) {
                        (24.dp * 3).roundToPx()
                    }

                    // 1. Build the same robust ImageRequest as before.
                    val imageRequest = ImageRequest.Builder(LocalContext.current)
                        .addHeader(
                            "User-Agent",
                            "Mozilla/5.0 (Android 14; Mobile; rv:130.0) Gecko/130.0 Firefox/130.0"
                        )
                        .data(faviconUrl)
                        .size(imageSizePx) // Explicitly set size
                        .crossfade(true)
                        .placeholder(R.drawable.ic_language)
                        .error(R.drawable.ic_language)
                        .build()

                    // 2. Use the painter to handle loading and state.
                    val painter = rememberAsyncImagePainter(model = imageRequest)

                    // 3. Use the NATIVE Image composable for display. It's more stable.
                    Image(
                        painter = painter,
                        contentDescription = "Favicon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                Spacer(Modifier.width(settings.value.padding.dp))
                Text(
                    text = title,
                    color = if (isActive) Color.Black else Color.Black.copy(alpha = 0.7f), // Dim the text for inactive
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

        }
    }
}

@Composable
fun TabsPanel(
    isTabsPanelVisible: Boolean,
    modifier: Modifier = Modifier,
    onTabLongPressed: (Tab) -> Unit,
    updateInspectingTab: (Tab) -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
    if (viewModel.tabs.isEmpty()) return

    val settings = viewModel.browserSettings.collectAsState()
    val activeTabIndex = viewModel.activeTabIndex.collectAsState()
    val tabsSize = viewModel.tabs.size

    val pagerState = rememberPagerState(
        initialPage = activeTabIndex.value + 1,
        pageCount = { tabsSize + 2 }
    )

    LaunchedEffect(activeTabIndex.value, tabsSize) {
        val targetPage = activeTabIndex.value + 1
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage, animationSpec = tween(settings.value.animationSpeedForLayer(1)))
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage in 1..tabsSize) {
            // Safely get the tab, avoiding crashes if it was just removed
            val tab = viewModel.tabs.getOrNull(pagerState.currentPage - 1)
            if (tab != null) {
                updateInspectingTab(tab)
            } else {
                updateInspectingTab(Tab.createEmpty(0L))
            }
        } else {
            updateInspectingTab(Tab.createEmpty(0L))
        }
    }

    AnimatedVisibility(
        visible = isTabsPanelVisible,
        enter = expandVertically(tween(settings.value.animationSpeedForLayer(1))) +
                fadeIn(tween(settings.value.animationSpeedForLayer(1))),
        exit = shrinkVertically(tween(settings.value.animationSpeedForLayer(1))) +
                fadeOut(tween(settings.value.animationSpeedForLayer(1)))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = settings.value.padding.dp)
                .padding(horizontal = settings.value.padding.dp)
                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
        ) {
            HorizontalPager(
                state = pagerState,
                key = { pageIndex ->
                    when (pageIndex) {
                        0 -> "new_tab_left"
                        in 1..tabsSize -> {
                            // THE FIX: Use getOrNull() so if the item was just deleted, it doesn't crash
                            viewModel.tabs.getOrNull(pageIndex - 1)?.id ?: "animating_out_$pageIndex"
                        }
                        else -> "new_tab_right_$pageIndex"
                    }
                },
                modifier = modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = settings.value.padding.dp * 4),
                pageSpacing = settings.value.padding.dp / 2
            ) { pageIndex ->

                when {
                    pageIndex == 0 -> {
                        NewTabButton(
                            onClick = { viewModel.createNewTab(0, "") }
                        )
                    }

                    pageIndex in 1..tabsSize -> {
                        val tabIndex = pageIndex - 1

                        // THE FIX: Use getOrNull() here too!
                        val tab = viewModel.tabs.getOrNull(tabIndex)

                        if (tab != null) {
                            val title = tab.currentTitle
                            val faviconUrl = tab.currentFaviconUrl.ifBlank {
                                getFaviconUrlFromGoogleServer(tab.currentURL)
                            }

                            TabItem(
                                faviconUrl = faviconUrl,
                                title = title,
                                isActive = pagerState.currentPage == pageIndex,
                                onClick = { viewModel.selectTab(tabIndex) },
                                onLongClick = { onTabLongPressed(tab) }
                            )
                        } else {
                            // If the tab is null, it means it's currently animating away
                            // after being closed. Render an empty Box to fill the space temporarily.
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }

                    else -> {
                        NewTabButton(
                            onClick = { viewModel.createNewTab(tabsSize, "") }
                        )
                    }
                }
            }
        }
    }
}