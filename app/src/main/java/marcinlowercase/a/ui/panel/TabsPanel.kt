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
package marcinlowercase.a.ui.panel

import android.util.Log
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
import marcinlowercase.a.core.function.toDomain

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
    modifier: Modifier = Modifier,
    updateInspectingTab: (Tab) -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    // 1. Filter out Standalone tabs and keep track of their ORIGINAL indices
    val visibleTabs = viewModel.tabs.mapIndexedNotNull { index, tab ->
        if (!tab.isStandalone) index to tab else null
    }

    val activeTabIndex = viewModel.activeTabIndex.collectAsState().value
    val activeVisibleIndex = visibleTabs.indexOfFirst { it.first == activeTabIndex }.coerceAtLeast(0)

    val pagerState = rememberPagerState(
        initialPage = activeVisibleIndex + 1,
        pageCount = { visibleTabs.size + 2 }
    )

    LaunchedEffect(activeTabIndex, visibleTabs.size, viewModel.activeProfileId.value) {
        val newActiveVisibleIndex = visibleTabs.indexOfFirst { it.first == activeTabIndex }.coerceAtLeast(0)
        if (pagerState.currentPage != newActiveVisibleIndex + 1) {
            if (uiState.value.isTabsPanelVisible) pagerState.animateScrollToPage(newActiveVisibleIndex + 1)
            else pagerState.requestScrollToPage(newActiveVisibleIndex + 1)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage in 1..visibleTabs.size) {
            updateInspectingTab(visibleTabs[pagerState.currentPage - 1].second)
        } else {
            updateInspectingTab(Tab.createEmpty(viewModel.activeProfileId.value, 0L))
        }
    }

    AnimatedVisibility(
        visible = uiState.value.isTabsPanelVisible,
        enter = expandVertically(tween(settings.value.animationSpeedForLayer(1))) + fadeIn(tween(settings.value.animationSpeedForLayer(1))),
        exit = shrinkVertically(tween(settings.value.animationSpeedForLayer(1))) + fadeOut(tween(settings.value.animationSpeedForLayer(1)))
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
                modifier = modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = settings.value.padding.dp * 4),
                pageSpacing = settings.value.padding.dp / 2
            ) { pageIndex ->

                when (pageIndex) {
                    0 -> {
                        NewTabButton(onClick = { viewModel.createNewTab(0, "") })
                    }

                    in 1..visibleTabs.size -> {
                        // We extract the real global index and the tab object
                        val (realIndex, tab) = visibleTabs[pageIndex - 1]

                        val title = if (tab.currentURL == "about:blank") {
                            "blank page"
                        } else {
                            tab.currentTitle.ifBlank { tab.currentURL.toDomain() }
                        }
                        val faviconUrl = tab.currentFaviconUrl.ifBlank {
                            getFaviconUrlFromGoogleServer(tab.currentURL)
                        }
                        TabItem(
                            faviconUrl = faviconUrl,
                            title = title,
                            isActive = pagerState.currentPage == pageIndex,
                            onClick = { viewModel.selectTab(realIndex) },
                            onLongClick = {
                                if (uiState.value.inspectingTabId == null) viewModel.updateUI { it.copy(inspectingTabId = tab.id) }
                                viewModel.updateUI { it.copy(isTabDataPanelVisible = !it.isTabDataPanelVisible) }
                            }
                        )
                    }

                    else -> {
                        NewTabButton(onClick = { viewModel.createNewTab(viewModel.tabs.size, "") })
                    }
                }
            }
        }
    }
}