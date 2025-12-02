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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.painterResource
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
import marcinlowercase.a.core.function.buttonSettingsForLayer
import marcinlowercase.a.core.function.getFaviconUrlFromGoogleServer
import marcinlowercase.a.core.manager.SiteSettingsManager
import marcinlowercase.a.core.manager.WebViewManager
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
    browserSettings: BrowserSettings,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (!isLast) browserSettings.padding.dp else 0.dp)
            .height(
                browserSettings.heightForLayer(3).dp
            )
            .clip(
                RoundedCornerShape(
                    browserSettings.cornerRadiusForLayer(3).dp
                )
            )
            .background(if (isCurrent) Color.White else Color.Transparent)
//            .border(
//                width = 1.dp,
//                color = if (isCurrent) Color.Transparent else Color.White,
//                shape = RoundedCornerShape(
//                    cornerRadiusForLayer(
//                        3,
//                        browserSettings.deviceCornerRadius,
//                        browserSettings.padding
//                    ).dp
//                )
//            )
            .clickable(onClick = onClick)
            .padding(horizontal = browserSettings.padding.dp * 2),
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

        Spacer(Modifier.width(browserSettings.padding.dp))

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
    webViewManager: WebViewManager,
    isTabDataPanelVisible: Boolean,
    inspectingTab: Tab?,
    onDismiss: () -> Unit,
    browserSettings: BrowserSettings,
    siteSettings: Map<String, SiteSettings>,
    onPermissionToggle: (domain: String?, permission: String, isGranted: Boolean) -> Unit,
    onClearSiteData: () -> Unit,
    onCloseTab: () -> Unit,
    onAddToHomeScreen: () -> Unit,
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
        enter = fadeIn(tween(browserSettings.animationSpeed.roundToInt())) + expandVertically(
            expandFrom = Alignment.Bottom
        ),
        exit = shrinkVertically(
            tween(
                browserSettings.animationSpeedForLayer(1)
            )
        ) + fadeOut(
            tween(browserSettings.animationSpeedForLayer(1))
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
                    .padding(top = browserSettings.padding.dp)
                    .padding(horizontal = browserSettings.padding.dp)
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .clip(
                        RoundedCornerShape(
                            browserSettings.cornerRadiusForLayer(2).dp
                        )
                    )
                    .background(Color.Black)
//                    .border(
//                        1.dp,
//                        Color.White,
//                        RoundedCornerShape(
//                            cornerRadiusForLayer(
//                                2,
//                                browserSettings.deviceCornerRadius,
//                                browserSettings.padding
//                            ).dp
//                        )
//                    )
            ) {
                val tab = inspectingTab ?: return@Column

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            tween(
                                browserSettings.animationSpeedForLayer(1)
                            )
                        ) // Smoothly animates size changes
                ) {

                    val maxLazyColumnHeight = (browserSettings.heightForLayer(3).dp + browserSettings.padding.dp) * 2.5f

                    val domain =
                        SiteSettingsManager(LocalContext.current).getDomain(
                            webViewManager.getWebView(
                                tab
                            ).url ?: browserSettings.defaultUrl
                        )
                    val settings = if (domain != null) siteSettings[domain] else null
                    val history = webViewManager.getWebView(tab).copyBackForwardList()

                    val isStillHaveOptions =
                        (settings != null && settings.permissionDecisions.isNotEmpty()) || history.size > 0

                    when (currentView) {
                        TabDataPanelView.MAIN -> {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = browserSettings.padding.dp)
                                    .padding(top = if (isStillHaveOptions) browserSettings.padding.dp else 0.dp),
                                verticalArrangement = Arrangement.spacedBy(browserSettings.padding.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // History Button
                                if (history.size > 0)
                                    IconButton(
                                        onClick = { currentView = TabDataPanelView.HISTORY },
                                        modifier = Modifier
                                            .buttonSettingsForLayer(
                                            3,
                                            browserSettings,
                                            false
                                        )
                                            .fillMaxWidth()
                                            .background(Color.Transparent)
//                                            .border(
//                                                width = 1.dp,
//                                                color = Color.White,
//                                                shape = RoundedCornerShape(
//                                                    cornerRadiusForLayer(
//                                                        3,
//                                                        browserSettings.deviceCornerRadius,
//                                                        browserSettings.padding
//                                                    ).dp
//                                                )
//                                            )
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_history),
                                            contentDescription = "History",
                                            tint = Color.White
                                        )
                                    }


                                // Permissions Button

                                if (settings != null && settings.permissionDecisions.isNotEmpty()) {
                                    IconButton(
                                        onClick = { currentView = TabDataPanelView.PERMISSIONS },
                                        modifier = Modifier
                                            .buttonSettingsForLayer(
                                            3,
                                            browserSettings,
                                            false
                                        )

                                            .fillMaxWidth()
                                            .background(Color.Transparent)
//                                            .border(
//                                                width = 1.dp,
//                                                color = Color.White,
//                                                shape = RoundedCornerShape(
//                                                    cornerRadiusForLayer(
//                                                        3,
//                                                        browserSettings.deviceCornerRadius,
//                                                        browserSettings.padding
//                                                    ).dp
//                                                )
//                                            )
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_shield_toggle),
                                            contentDescription = "Permissions",
                                            tint = Color.White
                                        )
                                    }
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
                                            max = maxLazyColumnHeight
                                        )
                                        .padding(
                                            top = browserSettings.padding.dp,
                                            start = browserSettings.padding.dp,
                                            end = browserSettings.padding.dp
                                        )
                                        .clip(
                                            RoundedCornerShape(
                                                browserSettings.cornerRadiusForLayer(3).dp
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
                                           browserSettings.heightForLayer(3).dp
                                        )
                                        .padding(top = browserSettings.padding.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("This tab is not active", color = Color.Gray)
                                }
                            }

                        }

//                        TabDataPanelView.PERMISSIONS -> {
//                            val domain =
//                                SiteSettingsManager(LocalContext.current).getDomain(
//                                    webViewManager.getWebView(
//                                        tab
//                                    ).url ?: browserSettings.defaultUrl
//                                )
//                            val settings = if (domain != null) siteSettings[domain] else null
//
//                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
//                                if (settings != null && settings.permissionDecisions.isNotEmpty()) {
//                                    settings.permissionDecisions.forEach { (permission, isGranted) ->
//                                        val permissionName = when (permission) {
//                                            generic_location_permission -> "Location"
//                                            Manifest.permission.CAMERA -> "Camera"
//                                            Manifest.permission.RECORD_AUDIO -> "Microphone"
//                                            else -> permission.substringAfterLast('.') // Fallback
//                                        }
//
//                                        Row(
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .padding(horizontal = browserSettings.padding.dp * 3),
//                                            verticalAlignment = Alignment.CenterVertically
//                                        ) {
//                                            Text(
//                                                permissionName,
//                                                color = Color.White,
//                                                modifier = Modifier.weight(1f)
//                                            )
//                                            Switch(
//                                                checked = isGranted,
//                                                onCheckedChange = {
//                                                    onPermissionToggle(
//                                                        domain,
//                                                        permission,
//                                                        it
//                                                    )
//                                                })
//                                        }
//                                    }
//                                } else {
//                                    Box(
//                                        modifier = Modifier.fillMaxSize()
//                                            .padding(top = browserSettings.padding.dp),
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        Text("No permissions requested yet.", color = Color.Gray)
//                                    }
//                                }
//                            }
//                        }

                        TabDataPanelView.PERMISSIONS -> {
                            val domain =
                                SiteSettingsManager(LocalContext.current).getDomain(
                                    webViewManager.getWebView(tab).url ?: browserSettings.defaultUrl
                                )
                            val settings = if (domain != null) siteSettings[domain] else null

                            // Check if there are any permissions to display
                            if (settings != null && settings.permissionDecisions.isNotEmpty()) {
                                // A single row that can scroll horizontally if needed
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = browserSettings.padding.dp)
                                        .padding(top = browserSettings.padding.dp),
                                    horizontalArrangement = Arrangement.spacedBy(browserSettings.padding.dp)
                                ) {
                                    settings.permissionDecisions.forEach { (permission, isGranted) ->
                                        // Determine the correct icon and name for the button
                                        val (iconRes, name) = when (permission) {
                                            generic_location_permission -> R.drawable.ic_location_on to "Location"
                                            Manifest.permission.CAMERA -> R.drawable.ic_camera_on to "Camera"
                                            Manifest.permission.RECORD_AUDIO -> R.drawable.ic_mic_on to "Microphone"
                                            else -> R.drawable.ic_bug to "Unknown" // Fallback
                                        }

                                        // Create the toggleable IconButton
                                        IconButton(
                                            onClick = {
                                                // Toggle the permission state when clicked
                                                onPermissionToggle(domain, permission, !isGranted)
                                            },
                                            // The `white` parameter controls the background
                                            modifier = Modifier.buttonSettingsForLayer(
                                                layer = 3,
                                                browserSettings,
                                                white = isGranted
                                            )
                                                .weight(1f),
                                            colors = IconButtonDefaults.iconButtonColors(
                                                // The `contentColor` controls the icon tint
                                                contentColor = if (isGranted) Color.Black else Color.White
                                            )
                                        ) {
                                            Icon(
                                                painter = painterResource(id = iconRes),
                                                contentDescription = name
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Same fallback UI for when no permissions are set
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(
                                            browserSettings.heightForLayer(3).dp
                                        )
                                        .padding(top = browserSettings.padding.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No permissions requested yet.", color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // This Row contains the action buttons at the bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(browserSettings.padding.dp),
                    horizontalArrangement = Arrangement.spacedBy(browserSettings.padding.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (currentView == TabDataPanelView.MAIN) {
                                onDismiss()
                            } else {
                                currentView = TabDataPanelView.MAIN
                            }
                        },
                        modifier = Modifier
                            .buttonSettingsForLayer(
                            3,
                            browserSettings
                        ).weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    if (tab.state != TabState.FROZEN) {
                        IconButton(
                            onClick = onClearSiteData,
                            modifier = Modifier.buttonSettingsForLayer(
                                3,
                                browserSettings
                            ).weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_database_off),
                                contentDescription = "Clear Site Data",
                                tint = Color.Black
                            )
                        }
                    }

                    IconButton(
                        onClick = onCloseTab,
                        modifier = Modifier.buttonSettingsForLayer(
                            3,
                            browserSettings
                        ).weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_tab_close),
                            contentDescription = "Close Tab",
                            tint = Color.Black
                        )
                    }

                    IconButton(
                        onClick = onAddToHomeScreen,
                        modifier = Modifier.buttonSettingsForLayer(
                            3,
                            browserSettings
                        ).weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_install_desktop),
                            contentDescription = "Add to Home Screen",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}