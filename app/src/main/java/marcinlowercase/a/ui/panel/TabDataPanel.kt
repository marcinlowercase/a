package marcinlowercase.a.ui.panel

import android.Manifest
import android.webkit.WebHistoryItem
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import marcinlowercase.a.R
import marcinlowercase.a.core.constant.generic_location_permission
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.SiteSettings
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.enum_class.TabState
import marcinlowercase.a.core.function.getFaviconUrlFromGoogleServer
import marcinlowercase.a.core.manager.SiteSettingsManager
import marcinlowercase.a.core.manager.WebViewManager
import marcinlowercase.a.ui.component.CustomIconButton
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.roundToInt

private enum class TabDataPanelView {
    MAIN,
    HISTORY,
    PERMISSIONS
}

@Composable
fun HistoryRow(
    item: WebHistoryItem,
    isLast: Boolean,
    isCurrent: Boolean,
    browserSettings: MutableState<BrowserSettings>,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (!isLast) browserSettings.value.padding.dp else 0.dp)
            .height(
                browserSettings.value.heightForLayer(3).dp
            )
            .clip(
                RoundedCornerShape(
                    browserSettings.value.cornerRadiusForLayer(3).dp
                )
            )
            .background(if (isCurrent) Color.White else Color.Transparent)
//            .border(
//                width = 1.dp,
//                color = if (isCurrent) Color.Transparent else Color.White,
//                shape = RoundedCornerShape(
//                    cornerRadiusForLayer(
//                        3,
//                        browserSettings.value.deviceCornerRadius,
//                        browserSettings.value.padding
//                    ).dp
//                )
//            )
            .clickable(onClick = onClick)
            .padding(horizontal = browserSettings.value.padding.dp * 2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Favicon ---
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            val imageRequest = ImageRequest.Builder(LocalContext.current)
                // Use the item's saved faviconUrl, or fall back to the Google service
                .data(getFaviconUrlFromGoogleServer(item.url))
                .crossfade(true)
                .placeholder(R.drawable.ic_language)
                .error(R.drawable.ic_language)
                .build()

            val painter = rememberAsyncImagePainter(model = imageRequest)

            Image(
                painter = painter,
                contentDescription = "Favicon for ${item.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.width(browserSettings.value.padding.dp))

        // --- Title ---
        Text(
            text = item.title.ifBlank { item.url },
            color = if (isCurrent) Color.Black else Color.White,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TabDataPanel(
    descriptionContent: MutableState<String>,
    webViewManager: WebViewManager,
    isTabDataPanelVisible: Boolean,
    inspectingTab: Tab?,
    onDismiss: () -> Unit,
    browserSettings: MutableState<BrowserSettings>,
    siteSettings: Map<String, SiteSettings>,
    onPermissionToggle: (domain: String?, permission: String, isGranted: Boolean) -> Unit,
    onClearSiteData: () -> Unit,
    onCloseTab: () -> Unit,
//    onAddToHomeScreen: () -> Unit,
    onHistoryItemClicked: (tab: Tab, index: Int, webViewManager: WebViewManager) -> Unit
) {

    // 1. Local state to hold the tab being displayed.

    var currentView by remember { mutableStateOf(TabDataPanelView.MAIN) }

    // Effect to reset the view to MAIN when the panel is hidden
    LaunchedEffect(isTabDataPanelVisible) {
        if (!isTabDataPanelVisible) {
            delay(300) // Wait for exit animation to finish before resetting state
            currentView = TabDataPanelView.MAIN
        }
    }

    // 2. Effect to update the local state.
    // This ensures `displayTab` only gets updated with non-null values,
    // holding onto the last valid tab during the exit animation.

    // This AnimatedVisibility controls the entire panel's appearance
    AnimatedVisibility(
        visible = isTabDataPanelVisible,
        enter = fadeIn(tween(browserSettings.value.animationSpeed.roundToInt())) + expandVertically(
            expandFrom = Alignment.Bottom
        ),
        exit = shrinkVertically(
            tween(
                browserSettings.value.animationSpeedForLayer(1)
            )
        ) + fadeOut(
            tween(browserSettings.value.animationSpeedForLayer(1))
        )

    ) {
        // A Box to handle clicking outside to dismiss
        Box(
            modifier = Modifier
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            // This inner Column prevents the dismiss click from propagating
            Column(
                modifier = Modifier
                    .clickable(enabled = false, onClick = {}) // Block clicks
                    .padding(top = browserSettings.value.padding.dp)
                    .padding(horizontal = browserSettings.value.padding.dp)
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .clip(
                        RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(2).dp
                        )
                    )
                    .background(Color.Black)
            ) {
                val tab = inspectingTab ?: return@Column

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            tween(
                                browserSettings.value.animationSpeedForLayer(1)
                            )
                        )
                ) {

                    val domain =
                        SiteSettingsManager(LocalContext.current).getDomain(
                            webViewManager.getWebView(
                                tab
                            ).url ?: browserSettings.value.defaultUrl
                        )
                    val settings = if (domain != null) siteSettings[domain] else null
                    val history = webViewManager.getWebView(tab).copyBackForwardList()

                    val isStillHaveOptions =
                        (settings != null && settings.permissionDecisions.isNotEmpty()) || history.size > 0

                    when (currentView) {
                        TabDataPanelView.MAIN -> {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = browserSettings.value.padding.dp)
                                    .padding(top = if (isStillHaveOptions) browserSettings.value.padding.dp else 0.dp),
                                verticalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // History Button
                                if (history.size > 0)
                                    CustomIconButton(
                                        layer = 3,
                                        browserSettings = browserSettings,
                                        modifier = Modifier.fillMaxWidth(),
                                        onTap = { currentView = TabDataPanelView.HISTORY },
                                        descriptionContent = descriptionContent,
                                        buttonDescription = "history list",
                                        painterId = R.drawable.ic_history,
                                        isWhite = false,
                                        )



                                // Permissions Button

                                if (settings != null && settings.permissionDecisions.isNotEmpty()) {
                                    CustomIconButton(
                                        layer = 3,
                                        browserSettings = browserSettings,
                                        modifier = Modifier.fillMaxWidth(),
                                        onTap = { currentView = TabDataPanelView.PERMISSIONS },
                                        descriptionContent = descriptionContent,
                                        buttonDescription = "permission list",
                                        painterId = R.drawable.ic_shield_toggle,
                                        isWhite = false,
                                    )
                                }

                            }
                        }

                        TabDataPanelView.HISTORY -> {
                            val lazyListState = rememberLazyListState()
                            if (history.size > 0) {
                                LazyColumn(
                                    state = lazyListState,
                                    modifier = Modifier
                                        .heightIn(
//                                            max = maxLazyColumnHeight
                                            max = browserSettings.value.maxContainerSizeForLayer(3).dp
                                        )
                                        .padding(
                                            top = browserSettings.value.padding.dp,
                                            start = browserSettings.value.padding.dp,
                                            end = browserSettings.value.padding.dp
                                        )
                                        .clip(
                                            RoundedCornerShape(
                                                browserSettings.value.cornerRadiusForLayer(3).dp
                                            )
                                        )
                                ) {
                                    val history =
                                        webViewManager.getWebView(tab).copyBackForwardList()
                                    items(history.size) { index ->
                                        val item = history.getItemAtIndex(index)
                                        HistoryRow(
                                            item = item,
                                            isLast = index == history.size - 1,
                                            isCurrent = index == history.currentIndex,
                                            browserSettings = browserSettings,
                                            onClick = {
                                                onHistoryItemClicked(
                                                    tab,
                                                    index,
                                                    webViewManager
                                                )
                                            }
                                        )
                                    }
                                }
                                LaunchedEffect(
                                    webViewManager.getWebView(tab)
                                        .copyBackForwardList().currentIndex
                                ) {
                                    webViewManager.getWebView(tab).copyBackForwardList().let {
                                        if (it.currentIndex in 0 until it.size) {
                                            lazyListState.animateScrollToItem(index = it.currentIndex)

                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(
                                            browserSettings.value.heightForLayer(3).dp
                                        )
                                        .padding(top = browserSettings.value.padding.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("This tab is not active", color = Color.Gray)
                                }
                            }

                        }

                        TabDataPanelView.PERMISSIONS -> {
                            val domain =
                                SiteSettingsManager(LocalContext.current).getDomain(
                                    webViewManager.getWebView(tab).url ?: browserSettings.value.defaultUrl
                                )
                            val settings = if (domain != null) siteSettings[domain] else null

                            // Check if there are any permissions to display
                            if (settings != null && settings.permissionDecisions.isNotEmpty()) {
                                // A single row that can scroll horizontally if needed
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = browserSettings.value.padding.dp)
                                        .padding(top = browserSettings.value.padding.dp),
                                    horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
                                ) {
                                    settings.permissionDecisions.forEach { (permission, isGranted) ->
                                        // Determine the correct icon and name for the button
                                        val (iconRes, name) = when (permission) {
                                            generic_location_permission -> R.drawable.ic_location_on to "location"
                                            Manifest.permission.CAMERA -> R.drawable.ic_camera_on to "camera"
                                            Manifest.permission.RECORD_AUDIO -> R.drawable.ic_mic_on to "microphone"
                                            else -> R.drawable.ic_bug to "unknown"
                                        }

                                        CustomIconButton(
                                            layer = 3,
                                            browserSettings = browserSettings,
                                            modifier = Modifier.weight(1f),
                                            onTap = { onPermissionToggle(domain, permission, !isGranted)},
                                            descriptionContent = descriptionContent,
                                            buttonDescription = name,
                                            painterId = iconRes,
                                            isWhite = isGranted,
                                        )
//
                                    }
                                }
                            } else {
                                // Same fallback UI for when no permissions are set
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(
                                            browserSettings.value.heightForLayer(3).dp
                                        )
                                        .padding(top = browserSettings.value.padding.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No permissions requested yet.", color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(browserSettings.value.padding.dp),
                    horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
                ) {
                    CustomIconButton(
                        layer = 3,
                        browserSettings = browserSettings,
                        modifier = Modifier.weight(1f),
                        onTap = {
                            if (currentView == TabDataPanelView.MAIN) {
                                onDismiss()
                            } else {
                                currentView = TabDataPanelView.MAIN
                            }
                        },
                        descriptionContent = descriptionContent,
                        buttonDescription = "back",
                        painterId = R.drawable.ic_arrow_back,
                        isWhite = false
                    )


                    if (tab.state != TabState.FROZEN) {
                        CustomIconButton(
                            layer = 3,
                            browserSettings = browserSettings,
                            modifier = Modifier.weight(1f),
                            onTap = onClearSiteData,
                            descriptionContent = descriptionContent,
                            buttonDescription = "clear site data",
                            painterId = R.drawable.ic_database_off
                        )

                    }

                    CustomIconButton(
                        layer = 3,
                        browserSettings = browserSettings,
                        modifier = Modifier.weight(1f),
                        onTap = onCloseTab,
                        descriptionContent = descriptionContent,
                        buttonDescription = "close tab",
                        painterId = R.drawable.ic_tab_close
                    )

                    // TODO
//                    IconButton(
//                        onClick = onAddToHomeScreen,
//                        modifier = Modifier.buttonSettingsForLayer(
//                            3,
//                            browserSettings.value
//                        ).weight(1f)
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_install_desktop),
//                            contentDescription = "Add to Home Screen",
//                            tint = Color.Black
//                        )
//                    }
                }
            }
        }
    }
}