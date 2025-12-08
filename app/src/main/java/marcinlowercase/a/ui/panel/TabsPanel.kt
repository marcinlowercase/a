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
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.function.getFaviconUrlFromGoogleServer

@Composable
fun NewTabButton(
    modifier: Modifier = Modifier,
    browserSettings: BrowserSettings,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier.padding(browserSettings.padding.dp)
    )
    {
        Box(
            modifier = modifier

                .padding(horizontal = browserSettings.padding.dp)
                .clip(
                    RoundedCornerShape(
                        browserSettings.cornerRadiusForLayer(3).dp
                    )
                )
                .clickable(onClick = onClick)
                .background(Color.Black.copy(alpha = 0.2f))
                .height(
                    browserSettings.heightForLayer(3).dp
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
    browserSettings: BrowserSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val hapticFeedback  = LocalHapticFeedback.current
    Box(

        modifier = Modifier
            .padding(browserSettings.padding.dp)
            .clip(
                RoundedCornerShape(
                    browserSettings.cornerRadiusForLayer(3).dp
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
                    browserSettings.heightForLayer(3).dp
                )
                .clip(
                    RoundedCornerShape(
                        browserSettings.cornerRadiusForLayer(3).dp
                    )
                )
                .background(if (isActive) Color.White else Color.White.copy(alpha = 0.5f)) // Different background for inactive
                .padding(horizontal = browserSettings.padding.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(browserSettings.padding.dp)

            ) {

                Box(
                    modifier = Modifier
                        .size(24.dp),

//                        .clip(RoundedCornerShape(browserSettings.padding.dp / 2)),
//                        .background(Color.White.copy(alpha = 0.2f)),
//                        .background(if (isActive) Color.White else Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    val imageSizePx = with(LocalDensity.current) {
                        (24.dp * 3).roundToPx()
                    }
                    Log.i("Favicon", "24dp * 3 -> ${imageSizePx}px")

                    // 1. Build the same robust ImageRequest as before.
                    val imageRequest = ImageRequest.Builder(LocalContext.current)
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

                Spacer(Modifier.width(browserSettings.padding.dp))
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
    tabs: List<Tab>,
    activeTabIndex: Int,
    browserSettings: BrowserSettings,
    onTabSelected: (Int) -> Unit,
    onNewTabClicked: (Int) -> Unit,
    onTabLongPressed: (Tab) -> Unit,
    updateInspectingTab: (Tab) -> Unit,
) {
    if (tabs.isEmpty()) return

    val pagerState =
        rememberPagerState(initialPage = activeTabIndex + 1, pageCount = { tabs.size + 2 })

    // This effect is still useful to sync the pager if a new tab is created
    LaunchedEffect(activeTabIndex, tabs.size) {
        if (pagerState.currentPage != activeTabIndex + 1) {
            pagerState.animateScrollToPage(activeTabIndex + 1)
        }
    }


    LaunchedEffect(pagerState.currentPage) {
        Log.e("UpdateTab", "Current Page ${pagerState.currentPage}")
        if (pagerState.currentPage in 1..tabs.size) updateInspectingTab(tabs[pagerState.currentPage - 1])
        else {
            updateInspectingTab(Tab.createEmpty())
        }
    }


    AnimatedVisibility(
        visible = isTabsPanelVisible,
        enter = expandVertically(
            tween(
                browserSettings.animationSpeedForLayer(1)
            )
        ) + fadeIn(
            tween(
                browserSettings.animationSpeedForLayer(1)
            )
        ),
        exit = shrinkVertically(
            tween(
                browserSettings.animationSpeedForLayer(1)
            )
        ) + fadeOut(
            tween(
                browserSettings.animationSpeedForLayer(1)
            )
        )

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = browserSettings.padding.dp)
                .padding(horizontal = browserSettings.padding.dp)

                .clip(
                    RoundedCornerShape(
                        browserSettings.cornerRadiusForLayer(2).dp
                    )
                )
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = modifier
                    .fillMaxWidth(),
                // We use a smaller content padding so the active tab is larger
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = browserSettings.padding.dp / 2
            ) { pageIndex ->

                when (pageIndex) {
                    0 -> {
                        // This is the FIRST page: New Tab button on the left
                        NewTabButton(
                            browserSettings = browserSettings,
                            onClick = { onNewTabClicked(0) } // Request new tab at index 0
                        )
                    }

                    in 1..tabs.size -> {
                        // This is a regular tab page. Map pageIndex back to tabIndex.
                        val tabIndex = pageIndex - 1

                        val tab = tabs[tabIndex]

                        val title = tab.currentTitle

                        val faviconUrl = tab.currentFaviconUrl.ifBlank {
                            getFaviconUrlFromGoogleServer(
                                tab.currentURL
                            )
                        }
                        TabItem(
                            faviconUrl = faviconUrl,
                            title = title,
                            isActive = pagerState.currentPage == pageIndex,
                            browserSettings = browserSettings,
                            onClick = {

                                onTabSelected(tabIndex)
                            },
                            onLongClick = {
                                onTabLongPressed(tab)
                                Log.e("UpdateTab", "$tabIndex")
                            }
                        )
                    }

                    else -> {
                        // This is the LAST page: New Tab button on the right
                        NewTabButton(
                            browserSettings = browserSettings,
                            onClick = { onNewTabClicked(tabs.size) } // Request new tab at the end
                        )
                    }
                }
            }
        }
    }
}