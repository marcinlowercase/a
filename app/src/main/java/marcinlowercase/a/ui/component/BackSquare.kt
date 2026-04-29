package marcinlowercase.a.ui.component

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.R
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import kotlin.math.roundToInt

@Composable
fun BackSquare(
    modifier: Modifier = Modifier,
    activeSession: GeckoSession,
    geckoViewRef: MutableState<GeckoView?>,
    backSquareOffsetX: Animatable<Float, AnimationVector1D>,
    backSquareOffsetY: Animatable<Float, AnimationVector1D>,
    squareAlpha: Animatable<Float, AnimationVector1D>,
    cutoutTop: Dp,
    webViewPaddingValue: PaddingValues,
    hideBackSquare: suspend (Boolean) -> Unit
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState().value

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val viewConfiguration = LocalViewConfiguration.current

    AnimatedVisibility(
        visible = !uiState.value.isBottomPanelVisible && !uiState.value.isLandscape && !uiState.value.isOtherPanelVisible && !viewModel.isStandaloneMode.value,
        modifier = modifier
            .fillMaxSize()
            .padding(webViewPaddingValue)
            .onSizeChanged {
                viewModel.screenSize.value = it
                with(density) {
                    viewModel.screenSizeDp.value = IntSize(
                        it.width.toDp().value.roundToInt(),
                        it.height.toDp().value.roundToInt()
                    )
                }
            },
        enter = fadeIn(tween(settings.animationSpeedForLayer(0))),
        exit = fadeOut(tween(settings.animationSpeedForLayer(0)))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            val squareBoxSize = settings.heightForLayer(1).dp
            val squareBoxSizePx = with(density) { squareBoxSize.toPx() }
            val paddingPx = with(density) { settings.padding.dp.toPx() }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            backSquareOffsetX.value.roundToInt(),
                            backSquareOffsetY.value.roundToInt()
                        )
                    }
                    .animateContentSize(tween(settings.animationSpeedForLayer(1)))
                    .size(squareBoxSize)
                    .graphicsLayer {
                        alpha = squareAlpha.value
                    }
                    .pointerInput(activeSession, uiState.value.isCursorMode) {
                        if (!uiState.value.isCursorMode) awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            coroutineScope.launch {
                                squareAlpha.animateTo(1f)
                            }

                            val longPressJob = coroutineScope.launch {
                                delay(viewConfiguration.longPressTimeoutMillis)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.updateUI { it.copy(isCursorPadVisible = true) }
                                squareAlpha.snapTo(0f)

                                val initialCursorX = backSquareOffsetX.value + settings.padding + down.position.x
                                val initialCursorY = ((viewModel.screenSize.value.height - cutoutTop.toPx()) / 2) - (viewModel.screenSize.value.height - backSquareOffsetY.value) + down.position.y + cutoutTop.toPx()

                                viewModel.cursorPointerPosition.value = Offset(initialCursorX, initialCursorY)
                            }

                            val drag = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                                if (longPressJob.isActive) {
                                    longPressJob.cancel()
                                }
                                change.consume()
                            }

                            if (longPressJob.isCompleted && !longPressJob.isCancelled) {
                                // --- LONG-PRESS PATH ---
                                if (drag != null) {
                                    drag(drag.id) { change ->
                                        change.consume()
                                        val changeSpaceX = (change.position.x - change.previousPosition.x) * settings.cursorTrackingSpeed
                                        val changeSpaceY = (change.position.y - change.previousPosition.y) * settings.cursorTrackingSpeed

                                        var newX = viewModel.cursorPointerPosition.value.x + changeSpaceX
                                        var newY = viewModel.cursorPointerPosition.value.y + changeSpaceY
                                        if (newX < 0) newX = 0f
                                        if (newX > viewModel.screenSize.value.width) newX = viewModel.screenSize.value.width.toFloat()
                                        if (newY < 0) newY = 0f
                                        if (newY > viewModel.screenSize.value.height) newY = viewModel.screenSize.value.height.toFloat()

                                        viewModel.cursorPointerPosition.value = Offset(newX, newY)
                                    }
                                }

                                // This code now ONLY runs after a long-press-drag has finished.
                                viewModel.updateUI { it.copy(isCursorPadVisible = false) }

                                // --- SIMULATE CLICK AT CURSOR POSITION ---
                                activeSession.let { _ ->
                                    val downTime = System.currentTimeMillis()
                                    val downEvent = MotionEvent.obtain(
                                        downTime,
                                        downTime,
                                        MotionEvent.ACTION_DOWN,
                                        viewModel.cursorPointerPosition.value.x,
                                        viewModel.cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding().toPx(),
                                        0
                                    )
                                    val upEvent = MotionEvent.obtain(
                                        downTime,
                                        downTime + 10,
                                        MotionEvent.ACTION_UP,
                                        viewModel.cursorPointerPosition.value.x,
                                        viewModel.cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding().toPx(),
                                        0
                                    )

                                    activeSession.panZoomController.onTouchEvent(downEvent)
                                    activeSession.panZoomController.onTouchEvent(upEvent)
                                    downEvent.recycle()
                                    upEvent.recycle()
                                }

                                coroutineScope.launch {
                                    squareAlpha.animateTo(settings.backSquareIdleOpacity)
                                }
                            } else {
                                // --- TAP OR SHORT-DRAG PATH ---
                                if (drag != null) {
                                    // SHORT-DRAG
                                    drag(drag.id) { change ->
                                        change.consume()
                                        val newX = backSquareOffsetX.value + change.position.x - change.previousPosition.x
                                        val newY = backSquareOffsetY.value + change.position.y - change.previousPosition.y

                                        coroutineScope.launch {
                                            backSquareOffsetX.snapTo(newX)
                                            backSquareOffsetY.snapTo(newY)
                                        }
                                    }

                                    // snap logic
                                    val screenWidth = viewModel.screenSize.value.width.toFloat()
                                    val currentX = backSquareOffsetX.value

                                    // snap back square to left or right side of the screen
                                    val targetX = if (currentX + (squareBoxSizePx / 2) < screenWidth / 2) {
                                        paddingPx // Snap Left
                                    } else {
                                        screenWidth - squareBoxSizePx - paddingPx // Snap Right
                                    }

                                    // Clamp Y to screen bounds
                                    val targetY = backSquareOffsetY.value.coerceIn(
                                        paddingPx,
                                        viewModel.screenSize.value.height.toFloat() - squareBoxSizePx - paddingPx
                                    )

                                    coroutineScope.launch {
                                        // Animate snap in
                                        launch {
                                            backSquareOffsetX.animateTo(targetX, spring())
                                        }
                                        launch {
                                            backSquareOffsetY.animateTo(targetY, spring())
                                        }

                                        viewModel.updateSettings {
                                            it.copy(
                                                backSquareOffsetX = targetX,
                                                backSquareOffsetY = targetY
                                            )
                                        }
                                        // Fade out after snap
                                        hideBackSquare(false)
                                    }
                                } else {
                                    // TAP
                                    if (longPressJob.isActive) {
                                        longPressJob.cancel()
                                        coroutineScope.launch {
                                            geckoViewRef.value?.clearFocus()
                                            viewModel.updateUI {
                                                it.copy(isUrlBarVisible = true)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // cursor mode
                        else awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            coroutineScope.launch {
                                squareAlpha.animateTo(1f)
                            }

                            val drag = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                                change.consume()
                            }

                            if (drag != null) {
                                // SHORT-DRAG
                                drag(drag.id) { change ->
                                    change.consume()
                                }
                            } else {
                                // TAP
                                coroutineScope.launch {
                                    viewModel.updateUI { it.copy(isUrlBarVisible = true) }
                                }
                            }
                        }
                    }
                    .clip(RoundedCornerShape(settings.cornerRadiusForLayer(1).dp))
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(settings.cornerRadiusForLayer(1).dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                // Animate the appearance and disappearance of the overlay.
//                Box(
//                    modifier = Modifier
//                        .padding(settings.padding.dp * 2)
//                        .size(settings.heightForLayer(3).dp)
//                        .clip(CircleShape)
//                        .background(MaterialTheme.colorScheme.onSurface.copy(0.3f))
//
//                ){
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_search),
//                        contentDescription = "Open Menu",
//                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), // Subtle tint so it blends well
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
                AnimatedVisibility(
                    visible = !uiState.value.isLoading,
                    enter = fadeIn(animationSpec = tween(settings.animationSpeed.roundToInt())),
                    exit = fadeOut(animationSpec = tween(settings.animationSpeed.roundToInt()))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo_bold),
                        contentDescription = "Open Menu",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), // Subtle tint so it blends well
                        modifier = Modifier.size(settings.heightForLayer(3).dp)
                    )
                }

                AnimatedVisibility(
                    visible = uiState.value.isLoading,
                    modifier = modifier,
                    enter = fadeIn(animationSpec = tween(settings.animationSpeed.roundToInt())),
                    exit = fadeOut(animationSpec = tween(settings.animationSpeed.roundToInt()))
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(settings.padding.dp)
                            .size(settings.heightForLayer(4).dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}