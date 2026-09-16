package marcinlowercase.a.ui.panel

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import marcinlowercase.a.core.enum_class.AppState
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ChatView(
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    val uiState = viewModel.uiState.collectAsState()

    val messages = viewModel.buildChatHistory
    val isThinking = viewModel.isChatThinking.value
    val layer = 4
    val density = LocalDensity.current
    val layoutInfo = listState.layoutInfo
    val lastUserIndex = messages.indexOfLast { it.first == "user" }
    var cachedSpacerHeight by remember { mutableStateOf(0.dp) }
    var viewingPromptText by remember { mutableStateOf<String?>(null) }
// 1. Continuously keeps the spacer height reactive as messages stream in
    LaunchedEffect(messages.size, isThinking) {
        snapshotFlow { listState.layoutInfo }.collect { info ->
            val userPromptCount = messages.count { it.first == "user" }
            if (userPromptCount <= 1) {
                cachedSpacerHeight = 0.dp
                return@collect
            }

            val viewportHeight = info.viewportSize.height
            val topPad = info.beforeContentPadding
            val bottomPad = info.afterContentPadding
            val innerViewportHeight = viewportHeight - topPad - bottomPad

            if (lastUserIndex != -1 && innerViewportHeight > 0) {
                val lastTurnItems = info.visibleItemsInfo.filter {
                    it.index >= lastUserIndex && it.key != "trailing_scroll_runway"
                }
                if (lastTurnItems.isNotEmpty()) {
                    val firstItem = lastTurnItems.minByOrNull { it.offset }!!
                    val lastItem = lastTurnItems.maxByOrNull { it.offset + it.size }!!
                    val lastTurnHeight = (lastItem.offset + lastItem.size) - firstItem.offset

                    // Subtract settings.value.padding.dp to offset Arrangement.spacedBy
                    cachedSpacerHeight = with(density) {
                        maxOf(0.dp, (innerViewportHeight - lastTurnHeight).toDp() - settings.value.padding.dp * layer)
                    }
                }
            }
        }
    }

// 2. Waits for Prompt 2 to physically exist in layout, THEN animates
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && messages.lastOrNull()?.first == "user") {
            val targetIndex = messages.indexOfLast { it.first == "user" }
            if (targetIndex != -1) {
                // Wait until the LazyColumn actually lays out the new item
                snapshotFlow { listState.layoutInfo }
                    .filter { info ->
                        info.visibleItemsInfo.any { it.index == targetIndex }
                    }
                    .first()

                // Wait 1 frame for the spacer height to be applied to the scroll bounds
                kotlinx.coroutines.delay(16.milliseconds)

                listState.animateScrollToItem(index = targetIndex, scrollOffset = 0)
            }
        }
    }
    val isChatMode = uiState.value.appState == AppState.BUILD && !uiState.value.isBuildPreview
    AnimatedVisibility(
        visible =isChatMode,
        modifier = Modifier.fillMaxSize(),
//        enter = fadeIn(tween(settings.value.animationSpeedForLayer(1))),
//        exit = fadeOut(tween(settings.value.animationSpeedForLayer(1)))
        enter = slideInHorizontally(tween(settings.value.animationSpeedForLayer(1))) { it },
        exit = slideOutHorizontally(tween(settings.value.animationSpeedForLayer(1))) { it }
    ) {
        Box(
            modifier = Modifier
//                .padding(settings.value.padding.dp)
                .fillMaxSize()
//                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Do nothing - consume tap */ }
//                .padding(bottom = settings.value.heightForLayer(1).dp + settings.value.padding.dp * 4)
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = settings.value.padding.dp * layer,
                    start = settings.value.padding.dp * layer,
                    end = settings.value.padding.dp * layer,
                    bottom = (settings.value.heightForLayer(1) * 2).dp,

                    ),
                modifier = modifier
                    .fillMaxWidth()
//                    .padding(settings.value.padding.dp * layer)
                ,
                verticalArrangement = Arrangement.spacedBy(settings.value.padding.dp * layer)
            ) {
                items(messages) { (role, text) ->
                    val isUser = role == "user"
                    var hasOverflow by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = if (isUser) settings.value.heightForLayer(layer).dp else 0.dp),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        if (isUser) settings.value.cornerRadiusForLayer(layer).dp else 0.dp
                                    )
                                )
                                .then(
                                    if (isUser && hasOverflow) {
                                        Modifier.clickable { viewingPromptText = text }
                                    } else Modifier
                                )
                                .heightIn(min = settings.value.heightForLayer(layer).dp)
                                .background(if (isUser) MaterialTheme.colorScheme.onSurfaceVariant else Color.Transparent)
                                .padding(
                                    horizontal = if (isUser) settings.value.cornerRadiusForLayer(layer).dp else 0.dp,
                                    vertical = settings.value.padding.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = text,
                                maxLines = if (isUser) 4 else Int.MAX_VALUE,
                                overflow = if (isUser) TextOverflow.Ellipsis else TextOverflow.Clip,
                                onTextLayout = { result ->
                                    if (isUser) {
                                        hasOverflow = result.hasVisualOverflow
                                    }
                                },
                                color = if (isUser) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (isThinking) {
                    item {
                        Text(
                            text = "Gemini is typing...",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                item(key = "trailing_scroll_runway") {
                    Spacer(modifier = Modifier.height(cachedSpacerHeight))
                }
            }
        }
        // Intercept system Back gesture when viewer is active
        BackHandler(enabled = viewingPromptText != null) {
            viewingPromptText = null
        }

// Fullscreen Prompt Overlay
        AnimatedVisibility(
            visible = viewingPromptText != null,
            enter = fadeIn(tween(settings.value.animationSpeedForLayer(1))),
            exit = fadeOut(tween(settings.value.animationSpeedForLayer(1)))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { viewingPromptText = null } // Tap anywhere to dismiss
                    .padding(settings.value.padding.dp * layer)
                    .padding(bottom = (settings.value.heightForLayer(1) * 2).dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(layer).dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                        .padding(settings.value.padding.dp )
                        .padding(horizontal = settings.value.cornerRadiusForLayer(4).dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = viewingPromptText.orEmpty(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}