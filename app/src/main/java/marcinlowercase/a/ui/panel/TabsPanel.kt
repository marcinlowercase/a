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
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
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
//                .padding(horizontal = settings.value.padding.dp)
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = settings.value.cornerRadiusForLayer(3).dp)
                    .padding(vertical = settings.value.padding.dp),
                horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)

            ) {

                Box(
                    modifier = Modifier
                        .size(24.dp)
                    ,
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

                Text(
                    text = title,
                    color = if (isActive) Color.Black else Color.Black.copy(alpha = 0.7f), // Dim the text for inactive
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            repeatDelayMillis = 0,
                            initialDelayMillis = 3000
                        )
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
    if (viewModel.tabs.isEmpty()) return
    val settings = viewModel.browserSettings.collectAsState()

    val activeTabIndex = viewModel.activeTabIndex.collectAsState().value
    val pagerState = rememberPagerState(
        initialPage = activeTabIndex + 1,
        pageCount = { viewModel.tabs.size + 2 }
    )

    LaunchedEffect(activeTabIndex, viewModel.tabs.size, viewModel.activeProfileId.value) {
        if (pagerState.currentPage != activeTabIndex + 1) {
            if (uiState.value.isTabsPanelVisible) pagerState.animateScrollToPage(activeTabIndex + 1)
            else pagerState.requestScrollToPage(activeTabIndex + 1)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage in 1..viewModel.tabs.size) {
            updateInspectingTab(viewModel.tabs[pagerState.currentPage - 1])
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

                    in 1..viewModel.tabs.size -> {
                        val tabIndex = pageIndex - 1
                        val tab = viewModel.tabs[tabIndex]

                        val title = if (tab.currentURL == "about:blank") {
                            stringResource(R.string.placeholder_blank_page)
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
                            onClick = { viewModel.selectTab(tabIndex) },
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