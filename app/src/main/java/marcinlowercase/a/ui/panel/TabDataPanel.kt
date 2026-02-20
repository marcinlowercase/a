package marcinlowercase.a.ui.panel

import android.Manifest
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import marcinlowercase.a.R
import marcinlowercase.a.core.constant.generic_location_permission
import marcinlowercase.a.core.enum_class.TabState
import marcinlowercase.a.core.function.toDomain
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import kotlin.math.roundToInt

private enum class TabDataPanelView {
    MAIN,
//    HISTORY,
    PERMISSIONS
}

@Composable
fun TabDataPanel(
    isTabDataPanelVisible: Boolean,
    onDismiss: () -> Unit,
    onPermissionToggle: (domain: String?, permission: String, isGranted: Boolean) -> Unit,
    onClearSiteData: () -> Unit,
    onCloseTab: () -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
    val browserSettings = viewModel.browserSettings.collectAsState()

    // 1. Local state to hold the tab being displayed.

    var currentView by remember { mutableStateOf(TabDataPanelView.MAIN) }

    // Effect to reset the view to MAIN when the panel is hidden
    LaunchedEffect(isTabDataPanelVisible) {
        if (!isTabDataPanelVisible) {
            delay(300) // Wait for exit animation to finish before resetting state
//            currentView = TabDataPanelView.MAIN
            currentView = TabDataPanelView.PERMISSIONS
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
                Log.d("TabDataPanel", "inspectingTab.value: ${viewModel.currentInspectingTab?.currentURL}")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            tween(
                                browserSettings.value.animationSpeedForLayer(1)
                            )
                        )
                ) {

//                    val viewModel.siteSettings = currentSiteSettings
//                    viewModel.siteSettings?.domain
//                    val domain =
//                        SiteSettingsManager(LocalContext.current).getDomain(
//                            webViewManager.getWebView(
//                                tab
//                            ).url ?: browserSettings.value.defaultUrl
//                        )
                    val domain = viewModel.currentInspectingTab?.currentURL?.toDomain()
                    val settings = viewModel.siteSettings[domain]
                    Log.i("TabDataPanel", "domain $domain")
                    Log.i("TabDataPanel", "settings $settings")


//                    val history = webViewManager.getWebView(tab).copyBackForwardList()

                    val isStillHaveOptions =
                        (settings != null && settings.permissionDecisions.isNotEmpty())
//                                || history.size > 0

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
//                                if (history.size > 0)
//                                    CustomIconButton(
//                                        layer = 3,
//                                        browserSettings = browserSettings,
//                                        modifier = Modifier.fillMaxWidth(),
//                                        onTap = { currentView = TabDataPanelView.HISTORY },
//                                        
//                                        buttonDescription = "history list",
//                                        painterId = R.drawable.ic_history,
//                                        isWhite = false,
//                                        )



                                // Permissions Button

                                if (settings != null && settings.permissionDecisions.isNotEmpty()) {
                                    CustomIconButton(
//                                        currentRotation = currentRotation,
                                        layer = 3,
                                        modifier = Modifier.fillMaxWidth(),
                                        onTap = { currentView = TabDataPanelView.PERMISSIONS },
                                        
                                        buttonDescription = "permission list",
                                        painterId = R.drawable.ic_shield_toggle,
                                        isWhite = false,
                                    )
                                }

                            }
                        }

//                        TabDataPanelView.HISTORY -> {
//                            val lazyListState = rememberLazyListState()
//                            if (history.size > 0) {
//                                LazyColumn(
//                                    state = lazyListState,
//                                    modifier = Modifier
//                                        .heightIn(
////                                            max = maxLazyColumnHeight
//                                            max = browserSettings.value.maxContainerSizeForLayer(3).dp
//                                        )
//                                        .padding(
//                                            top = browserSettings.value.padding.dp,
//                                            start = browserSettings.value.padding.dp,
//                                            end = browserSettings.value.padding.dp
//                                        )
//                                        .clip(
//                                            RoundedCornerShape(
//                                                browserSettings.value.cornerRadiusForLayer(3).dp
//                                            )
//                                        )
//                                ) {
//                                    val history =
//                                        webViewManager.getWebView(tab).copyBackForwardList()
//                                    items(history.size) { index ->
//                                        val item = history.getItemAtIndex(index)
//                                        HistoryRow(
//                                            item = item,
//                                            isLast = index == history.size - 1,
//                                            isCurrent = index == history.currentIndex,
//                                            browserSettings = browserSettings,
//                                            onClick = {
//                                                onHistoryItemClicked(
//                                                    tab,
//                                                    index,
//                                                    webViewManager
//                                                )
//                                            }
//                                        )
//                                    }
//                                }
//                                LaunchedEffect(
//                                    webViewManager.getWebView(tab)
//                                        .copyBackForwardList().currentIndex
//                                ) {
//                                    webViewManager.getWebView(tab).copyBackForwardList().let {
//                                        if (it.currentIndex in 0 until it.size) {
//                                            lazyListState.animateScrollToItem(index = it.currentIndex)
//
//                                        }
//                                    }
//                                }
//                            } else {
//                                Box(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .height(
//                                            browserSettings.value.heightForLayer(3).dp
//                                        )
//                                        .padding(top = browserSettings.value.padding.dp),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text("This tab is not active", color = Color.Gray)
//                                }
//                            }
//
//                        }

                        TabDataPanelView.PERMISSIONS -> {
//                            val domain =
//                                SiteSettingsManager(LocalContext.current).getDomain(
//                                    webViewManager.getWebView(tab).url ?: browserSettings.value.defaultUrl
//                                )
                            val domain = viewModel.currentInspectingTab?.currentURL?.toDomain()
                            val settings = viewModel.siteSettings[domain]

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
                                        Log.i("PermissionRelated", "permission: $permission")
                                        val (iconRes, name) = when (permission) {
                                            generic_location_permission -> R.drawable.ic_location_on to "location"
                                            Manifest.permission.CAMERA -> R.drawable.ic_camera_on to "camera"
                                            Manifest.permission.RECORD_AUDIO -> R.drawable.ic_mic_on to "microphone"
                                            else -> R.drawable.ic_bug to "unknown"
                                        }

                                        CustomIconButton(
//                                            currentRotation = currentRotation,
                                            layer = 3,
                                            modifier = Modifier.weight(1f),
                                            onTap = { onPermissionToggle(domain, permission, !isGranted)},
                                            
                                            buttonDescription = name,
                                            painterId = iconRes,
                                            isWhite = isGranted,
                                        )
//
                                    }
                                }
                            }
//                            else {
//                                // Same fallback UI for when no permissions are set
//                                Box(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .height(
//                                            browserSettings.value.heightForLayer(3).dp
//                                        )
//                                        .padding(top = browserSettings.value.padding.dp),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text("No permissions requested yet.", color = Color.Gray)
//                                }
//                            }
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
//                    CustomIconButton(
//                        layer = 3,
//                        browserSettings = browserSettings,
//                        modifier = Modifier.weight(1f),
//                        onTap = {
//                            onDismiss()
////                            if (currentView == TabDataPanelView.MAIN) {
////                                onDismiss()
////                            } else {
////                                currentView = TabDataPanelView.MAIN
////                            }
//                        },
//                        
//                        buttonDescription = "back",
//                        painterId = R.drawable.ic_arrow_back,
//                        isWhite = false
//                    )


                    if (viewModel.currentInspectingTab?.state != TabState.FROZEN) {
                        CustomIconButton(
//                            currentRotation = currentRotation,
                            layer = 3,
                            modifier = Modifier.weight(1f),
                            onTap = onClearSiteData,
                            
                            buttonDescription = "clear site data",
                            painterId = R.drawable.ic_database_off
                        )

                    }

                    // DUPLICATE
                    CustomIconButton(
//                        currentRotation = currentRotation,
                        layer = 3,
                        modifier = Modifier.weight(1f),
                        onTap = { viewModel.duplicateInspectedTab() },
                        
                        buttonDescription = "duplicate tab",
                        painterId = R.drawable.ic_tab_duplicate
                    )

                    CustomIconButton(
//                        currentRotation = currentRotation,
                        layer = 3,
                        modifier = Modifier.weight(1f),
                        onTap = onCloseTab,
                        
                        buttonDescription = "close tab",
                        painterId = R.drawable.ic_tab_close
                    )

                    // TODO
//                    IconButton(
//                        onClick = onAddToHomeScreen,
//                        modifier = Modifier.buttonSettingsForLayer(
//                            3,
//                            browserSettings
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