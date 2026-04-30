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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.App
import marcinlowercase.a.core.data_class.activeOnHighlight
import marcinlowercase.a.core.enum_class.RevealState
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import kotlin.math.ceil
import kotlin.math.round

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppsPanel(
    onAppClick: (App) -> Unit = {},
    addAppToPin: () -> Unit,
    createNewProfile: () -> Unit,
    renameProfile: () -> Unit,
    deleteProfile: () -> Unit,
    draggableState: AnchoredDraggableState<RevealState>,

    ) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    // Panel Height Calculation
    val fullyDisplayRow = round(settings.value.maxListHeight)
    val numberOfPaddings = fullyDisplayRow + if (ceil(settings.value.maxListHeight) > settings.value.maxListHeight) 2 else 1
    val maxPanelHeight =
        (
                (settings.value.heightForLayer(3) * settings.value.maxListHeight) +
                settings.value.padding * numberOfPaddings
                ).dp

    val profiles = viewModel.profiles
    val realPageCount = profiles.size
    val currentProfileIdx =
        profiles.indexOfFirst { it.id == viewModel.activeProfileId.value }.coerceAtLeast(0)

    val initialPage = remember(realPageCount) {
        if (realPageCount <= 1) 0 else (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % realPageCount) + currentProfileIdx
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { if (realPageCount <= 1) 1 else Int.MAX_VALUE }
    )

    // Helper: Only allow clicks if the panel is fully resting in the "Revealed" state
    // and not currently being swiped/dragged or scrolling.
    val isInteractive: () -> Boolean = {
        uiState.value.isAppsPanelVisible
                && !draggableState.isAnimationRunning
                && !pagerState.isScrollInProgress
    }

    // Pager Logic
    LaunchedEffect(pagerState.settledPage) {
        val targetIndex = pagerState.settledPage % realPageCount
        val selectedProfile = profiles[targetIndex]
        if (selectedProfile.id != viewModel.activeProfileId.value) {
            viewModel.switchProfile(selectedProfile.id)
        }
    }
    LaunchedEffect(viewModel.activeProfileId.value, realPageCount) {
        val targetIndex =
            profiles.indexOfFirst { it.id == viewModel.activeProfileId.value }.coerceAtLeast(0)
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
        val activeApps = viewModel.apps.toList()

        val pageApps = if (pageProfile.id == viewModel.activeProfileId.value) {
            activeApps
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
                    .background(MaterialTheme.colorScheme.inverseSurface)
                    .padding(settings.value.padding.dp)
                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp)),
                horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
                verticalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
            ) {
                var visualItemCount = 0

                if (viewModel.isSortingButtons.value) {
                    val opt = viewModel.inspectingOption.value
                    val canLeft = opt != null && viewModel.canMoveOptionLeft(opt, settings.value)
                    val canRight = opt != null && viewModel.canMoveOptionRight(opt, settings.value)
                    val isHidden = opt != null && viewModel.isOptionHidden(opt, settings.value)
                    val canHide = opt != null &&
                            opt != marcinlowercase.a.core.enum_class.BrowserOption.SETTINGS &&
                            opt != marcinlowercase.a.core.enum_class.BrowserOption.SORT_BUTTONS
                    // 1. Move Left
                    if (canLeft) {
                        item(key = "sort_left", span = { GridItemSpan(1) }) {
                            PlaceholderIcon(
                                modifier = Modifier.animateItem(), // Smoothly slides when appearing/disappearing
                                iconRes = R.drawable.ic_arrow_back,
                                onClick = { viewModel.moveOptionLeft() },
                                buttonDescription = stringResource(R.string.desc_move_button_left)
                            )
                        }
                        visualItemCount++
                    }

                    // 2. Hide / Show
                    if (canHide) {
                        item(key = "sort_hide", span = { GridItemSpan(1) }) {
                            PlaceholderIcon(
                                modifier = Modifier.animateItem(),
                                iconRes = if (isHidden) R.drawable.ic_visibility_off else R.drawable.ic_visibility,
                                onClick = { viewModel.toggleOptionVisibility() },
                                buttonDescription = stringResource(if (isHidden) R.string.desc_show_button else R.string.desc_hide_button)
                            )
                        }
                        visualItemCount++
                    }

                    // 3. Move Right
                    if (canRight) {
                        item(key = "sort_right", span = { GridItemSpan(1) }) {
                            PlaceholderIcon(
                                modifier = Modifier.animateItem(),
                                iconRes = R.drawable.ic_arrow_forward,
                                onClick = { viewModel.moveOptionRight() },
                                buttonDescription = stringResource(R.string.desc_move_button_right)
                            )
                        }
                        visualItemCount++
                    }

                    // 4. Done Button (Always visible when sorting)
                    item(key = "sort_done", span = { GridItemSpan(1) }) {
                        PlaceholderIcon(
                            modifier = Modifier.animateItem(),
                            iconRes = R.drawable.ic_check,
                            onClick = {
                                viewModel.isSortingButtons.value = false
                                viewModel.inspectingOption.value = null
                                viewModel.updateUI { it.copy(isAppsPanelVisible = false) }
                            },
                            buttonDescription = stringResource(R.string.desc_done_sorting),
                            otherColor = Color(settings.value.highlightColor)
                        )
                    }
                    visualItemCount++
                } else {
                    val inspectingId = viewModel.inspectingAppId.longValue

                    // We loop manually to inject items.
                    pageApps.forEachIndexed { index, app ->
                        val isInspectingThisApp = (inspectingId == app.id)

                        // 1. Move Backward Button
                        if (isInspectingThisApp && index > 0) {
                            item(key = "prev_${app.id}", contentType = "action_button") {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
                                    modifier = Modifier.animateItem()
                                ) {
                                    PlaceholderIcon(
                                        iconRes = R.drawable.ic_arrow_upward,
                                        onClick = {
                                            if (isInteractive()) {
                                                val currentIndex =
                                                    viewModel.apps.indexOfFirst { it.id == app.id }
                                                if (currentIndex > 0) {
                                                    viewModel.swapApps(
                                                        currentIndex,
                                                        currentIndex - 1
                                                    )
                                                }
                                            }
                                        },
                                        buttonDescription = stringResource(R.string.desc_move_pin_up)
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
                                    if (isInteractive()) {
                                        if (inspectingId != 0L && inspectingId != app.id) {
                                            viewModel.inspectingAppId.longValue = app.id
                                        } else {
                                            onAppClick(app)
                                        }
                                    }
                                },
                                onDoubleClick = {
                                    if (isInteractive()) {
                                        viewModel.createNewTab(
                                            viewModel.activeTabIndex.value + 1,
                                            app.url
                                        )
                                        viewModel.updateUI {
                                            it.copy(
                                                isSettingsPanelVisible = false,
                                                isUrlBarVisible = false
                                            )
                                        }
                                    }
                                },
                                onLongClick = {
                                    if (isInteractive()) {

                                        viewModel.inspectingAppId.longValue =
                                            if (inspectingId != app.id) app.id else 0L
                                    }
                                },
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
                                        onClick = {
                                            if (isInteractive()) {
                                                val currentIndex =
                                                    viewModel.apps.indexOfFirst { it.id == app.id }
                                                if (currentIndex != -1 && currentIndex < pageApps.size - 1) {
                                                    viewModel.swapApps(
                                                        currentIndex,
                                                        currentIndex + 1
                                                    )
                                                }
                                            }
                                        },
                                        buttonDescription = stringResource(R.string.desc_move_pin_down)
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
                                        onClick = {
                                            if (isInteractive()) {
                                                viewModel.removeApp(app.id)
                                                viewModel.inspectingAppId.longValue = 0L
                                            }
                                        },
                                        buttonDescription = stringResource(R.string.desc_delete_pin)
                                    )
                                }
                            }
                            visualItemCount++
                        }

                        // 4. Done Button
                        if (isInspectingThisApp) {
                            item(key = "done_${app.id}", contentType = "action_button") {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
                                    modifier = Modifier.animateItem()
                                ) {
                                    PlaceholderIcon(
                                        iconRes = R.drawable.ic_check,
                                        onClick = {
                                            if (isInteractive()) {
                                                viewModel.inspectingAppId.longValue = 0L
                                            }
                                        },
                                        buttonDescription = stringResource(R.string.desc_done_editing)
                                    )
                                }
                            }
                            visualItemCount++
                        }
                    }

                    // --- FOOTER / PLACEHOLDERS ---

                    item(
                        span = { GridItemSpan(1) },
                        key = "pin_tab_${pageProfile.id}",
                        contentType = "action_button"
                    ) {
                        PlaceholderIcon(
                            iconRes = R.drawable.ic_keep,
                            onClick = { if (isInteractive()) addAppToPin() },
                            modifier = Modifier.animateItem(),
                            buttonDescription = stringResource(R.string.desc_pin_current_tab)
                        )
                    }
                    visualItemCount++
                }

                val remainder = visualItemCount % 4
                val needsGapFiller = remainder == 3
                if (needsGapFiller) {
                    item(
                        span = { GridItemSpan(1) },
                        key = "gap_filler_page_${pageProfile.id}",
                        contentType = "placeholder"
                    ) {
                        PlaceholderIcon(modifier = Modifier.animateItem())
                    }
                    visualItemCount++
                }
                // 1. Profile Name (Span 2)
                item(
                    span = { GridItemSpan(2) },
                    key = "profile_name_${pageProfile.id}",
                    contentType = "profile_header"
                ) {
                    PlaceholderIcon(
                        text = pageProfile.name,
                        modifier = Modifier.animateItem(),
                        buttonDescription = stringResource(R.string.desc_rename_profile),
                        onClick = {
                            if (isInteractive()) {
                                renameProfile()
                            }
                        })
                }
                visualItemCount += 2



                item(
                    span = { GridItemSpan(1) },
                    key = "new_profile_${pageProfile.id}",
                    contentType = "action_button"
                ) {
                    PlaceholderIcon(
                        iconRes = R.drawable.ic_person_add,
                        onClick = { if (isInteractive()) createNewProfile() },
                        modifier = Modifier.animateItem(),
                        buttonDescription = stringResource(R.string.desc_new_profile)
                    )
                }
                visualItemCount++

                // 4. Delete Profile
                if (profiles.size > 1) {
                    item(
                        span = { GridItemSpan(1) },
                        key = "delete_profile_${pageProfile.id}",
                        contentType = "action_button"
                    ) {
                        PlaceholderIcon(
                            iconRes = R.drawable.ic_person_off,
                            onClick = {
                                if (profiles.size > 1 && isInteractive()) {
                                    deleteProfile()
                                }
                            },
                            modifier = Modifier.animateItem(),
                            buttonDescription = stringResource(R.string.desc_delete_profile)
                        )
                    }
                    visualItemCount++
                }
                val minRows = ceil(settings.value.maxListHeight).toInt()
                val currentRows = ceil(visualItemCount / 4f).toInt()
                val targetRows = maxOf(minRows, currentRows)
                val remainingPlaceholders = (targetRows * 4) - visualItemCount

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
    buttonDescription: String? = null,
    text: String? = null,
    iconRes: Int? = null,
    onClick: (() -> Unit)? = null,
    otherColor: Color? = null
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp))
            .heightIn(min = settings.value.heightForLayer(3).dp)
            .background(
                otherColor
                    ?: if (!isSystemInDarkTheme() && settings.value.isMaterialYou()) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainer.copy(
                        settings.value.backSquareIdleOpacity * 0.2f
                    )
            )
            .then(
                if (onClick != null && buttonDescription != null)
                    Modifier.pointerInput(buttonDescription) {
                        coroutineScope {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)

                                val longPressJob = launch {
                                    delay(viewConfiguration.longPressTimeoutMillis)
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.descriptionContent.value = buttonDescription
                                }

                                var isTap = true

                                // Safely wait without consuming the event so the parent Draggable can still claim it
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)

                                    // If a parent element consumed the swipe (i.e. swiping to reveal the panel)
                                    if (event.changes.any { it.isConsumed }) {
                                        isTap = false
                                        break
                                    }

                                    // If all fingers are lifted off the screen
                                    if (event.changes.all { !it.pressed }) {
                                        break
                                    }
                                }

                                // If the user didn't hold it long enough, it's a click
                                if (longPressJob.isActive) {
                                    longPressJob.cancel()
                                    if (isTap) {
                                        onClick()
                                    }
                                }

                                // Always clear the description on release or cancellation
                                if (viewModel.descriptionContent.value == buttonDescription) viewModel.descriptionContent.value =
                                    ""
                            }
                        }
                    }
                else Modifier
            )
            .padding(settings.value.padding.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                color = if (otherColor != null) Color(settings.value.activeOnHighlight()
                ) else {
                    if (!isSystemInDarkTheme() && settings.value.isMaterialYou())MaterialTheme.colorScheme.onSurface  else
                        MaterialTheme.colorScheme.surfaceContainer
                },
                maxLines = 1,
                overflow = TextOverflow.Visible,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
            )
        } else if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = if (otherColor != null) Color(settings.value.activeOnHighlight()
                ) else {
                    if (!isSystemInDarkTheme() && settings.value.isMaterialYou())MaterialTheme.colorScheme.onSurface  else
                    MaterialTheme.colorScheme.surfaceContainer
                },
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
            .heightIn(min = settings.value.heightForLayer(3).dp)
            .background(
                if (!isSystemInDarkTheme() && settings.value.isMaterialYou()) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainer.copy(
                    settings.value.backSquareIdleOpacity * 0.2f
                )
            )
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
                        .addHeader(
                            "User-Agent",
                            "Mozilla/5.0 (Android 14; Mobile; rv:130.0) Gecko/130.0 Firefox/130.0"
                        )
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