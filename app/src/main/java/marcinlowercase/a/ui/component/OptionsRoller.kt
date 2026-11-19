package marcinlowercase.a.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import kotlin.math.abs

@Composable
fun <T> OptionsRoller(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    itemDescription: (T) -> String = itemLabel,
    layer: Int = 2,
    expandedWidth: Dp? = null,
) {
    if (items.isEmpty()) return

    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var isExpanded by remember { mutableStateOf(false) }

    val containerHeight = settings.value.heightForLayer(layer).dp
    val itemSize = settings.value.heightForLayer(layer + 1).dp
    val targetExpandedWidth = (expandedWidth ?: (containerHeight * 2)).coerceAtLeast(containerHeight)
    val animatedWidth by animateDpAsState(
        targetValue = if (isExpanded) targetExpandedWidth else containerHeight,
        animationSpec = tween(settings.value.animationSpeedForLayer(layer)),
        label = "OptionsRollerWidth"
    )
    // keep the selected inner item centered throughout collapse and expand animation
    val dynamicSidePadding = ((animatedWidth - itemSize) / 2).coerceAtLeast(0.dp)

    val initialIndex = remember(items) {
        items.indexOf(selectedItem).coerceAtLeast(0)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Center
    )

    val currentItems by rememberUpdatedState(items)
    val currentSelectedItem by rememberUpdatedState(selectedItem)
    val currentOnItemSelected by rememberUpdatedState(onItemSelected)
    val currentIsExpanded by rememberUpdatedState(isExpanded)

    val centeredIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                initialIndex
            } else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItems.minByOrNull { item ->
                    val itemCenter = item.offset + item.size / 2
                    abs(itemCenter - viewportCenter)
                }?.index ?: initialIndex
            }
        }
    }
    val currentCenteredIndex by rememberUpdatedState(centeredIndex)

    var lastHapticIndex by remember { mutableIntStateOf(initialIndex) }

    LaunchedEffect(centeredIndex) {
        if (centeredIndex != lastHapticIndex) {
            lastHapticIndex = centeredIndex
            if (isExpanded) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    // commit selection once snapping settles
    LaunchedEffect(centeredIndex, listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val item = currentItems.getOrNull(centeredIndex)
            if (item != null && item != currentSelectedItem) {
                currentOnItemSelected(item)
            }
        }
    }

    // auto-collapse back to single button after 2.5s of stillness
    LaunchedEffect(isExpanded, listState.isScrollInProgress, centeredIndex) {
        if (isExpanded && !listState.isScrollInProgress) {
            delay(2500)
            isExpanded = false
        }
    }

    // ensure centered item stays cleanly aligned when collapsing
    LaunchedEffect(isExpanded) {
        if (!isExpanded && !listState.isScrollInProgress) {
            val targetIndex = currentItems.indexOf(currentSelectedItem).coerceAtLeast(0)
            listState.scrollToItem(targetIndex)
        }
    }

    // sync scroll position when selectedItem changes externally
    LaunchedEffect(selectedItem) {
        val targetIndex = items.indexOf(selectedItem)
        if (targetIndex != -1 && targetIndex != centeredIndex && !listState.isScrollInProgress) {
            if (isExpanded) {
                listState.animateScrollToItem(targetIndex)
            } else {
                listState.scrollToItem(targetIndex)
            }
        }
    }

    val highlightColor = Color(settings.value.highlightColor)
    val animatedDividerColor by animateColorAsState(
        targetValue = if (isExpanded) highlightColor else highlightColor.copy(alpha = 0f),
        animationSpec = tween(settings.value.animationSpeedForLayer(layer)),
        label = "OptionsRollerDividers"
    )

    Box(
        modifier = modifier
            .width(animatedWidth)
            .height(containerHeight)
            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(layer).dp))
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center
    ) {
        // two straight vertical dividers framing the centered target slot
        Box(
            modifier = Modifier
                .width(itemSize + settings.value.padding.dp)
                .height(itemSize * 0.6f)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(2.dp)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(animatedDividerColor)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(2.dp)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(animatedDividerColor)
            )
        }

        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            userScrollEnabled = isExpanded,
            contentPadding = PaddingValues(horizontal = dynamicSidePadding),
            horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(items) { index, item ->
                val isCentered = index == centeredIndex
                CustomTextButton(
                    layer = layer + 1,
                    isSquare = true,
                    text = itemLabel(item),
                    buttonDescription = itemDescription(item),
                    isWhite = false,
                    containerColor = Color.Transparent,
                    contentColor = if (!isExpanded || isCentered) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    },
                    useLongPress = false,
                    onTap = {
                        if (!currentIsExpanded) {
                            isExpanded = true
                        } else if (index == currentCenteredIndex) {
                            isExpanded = false
                        } else {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                        }
                    }
                )
            }
        }
    }
}