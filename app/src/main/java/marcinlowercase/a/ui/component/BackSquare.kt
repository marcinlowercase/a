package marcinlowercase.a.ui.component

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.R
import marcinlowercase.a.core.function.isBubbleMode
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BackSquare(
    modifier: Modifier = Modifier,
    activeSession: GeckoSession,
    geckoViewRef: MutableState<GeckoView?>,
//    backSquareOffsetX: Animatable<Float, AnimationVector1D>,
//    backSquareOffsetY: Animatable<Float, AnimationVector1D>,
    squareAlpha: Animatable<Float, AnimationVector1D>,
    cutoutTop: Dp,
    webViewPaddingValue: PaddingValues,
    backSquarePaddingValue: PaddingValues = webViewPaddingValue,
    hideBackSquare: suspend (Boolean) -> Unit
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val viewConfiguration = LocalViewConfiguration.current

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var hasSizedBackSquare by remember { mutableStateOf(false) }

    val screenWidth = if (containerSize.width > 0) containerSize.width.toFloat() else viewModel.screenSize.value.width.toFloat()
    val screenHeight = if (containerSize.height > 0) containerSize.height.toFloat() else viewModel.screenSize.value.height.toFloat()

    val backsquareSizePx = with(density) { settings.value.heightForLayer(1).dp.toPx() }
    val paddingPx = with(density) { settings.value.padding.dp.toPx() }

    val bsVisibleWidth = (screenWidth - backsquareSizePx - (paddingPx * 2)).coerceAtLeast(0f)
    val bsVisibleHeight = (screenHeight - backsquareSizePx - (paddingPx * 2)).coerceAtLeast(0f)

    val x = bsVisibleWidth * settings.value.backSquareX + paddingPx
    val y = bsVisibleHeight * settings.value.backSquareY + paddingPx

    val backSquareOffsetX = remember { Animatable(x) }
    val backSquareOffsetY = remember { Animatable(y) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val imeInsets = WindowInsets.ime.asPaddingValues()
    val keyboardHeight = imeInsets.calculateBottomPadding()
    val isKeyboardVisible = keyboardHeight > 0.dp
    val keyboardBottomLine = remember { mutableFloatStateOf(0f) }

    var isDragging by remember { mutableStateOf(false) }

    val layoutDirection = LocalLayoutDirection.current
    val currentWebViewStartPadPx by rememberUpdatedState(
        with(density) { webViewPaddingValue.calculateLeftPadding(layoutDirection).toPx() }
    )
    val currentWebViewTopPadPx by rememberUpdatedState(
        with(density) { webViewPaddingValue.calculateTopPadding().toPx() }
    )
    val currentBackSquareStartPadPx by rememberUpdatedState(
        with(density) { backSquarePaddingValue.calculateLeftPadding(layoutDirection).toPx() }
    )
    val currentBackSquareTopPadPx by rememberUpdatedState(
        with(density) { backSquarePaddingValue.calculateTopPadding().toPx() }
    )

    var activePrimaryPointerId by remember { mutableStateOf<PointerId?>(null) }
    var isCursorMouseDown by remember { mutableStateOf(false) }
    var hasUsedSecondaryMouse by remember { mutableStateOf(false) }
    var cursorMouseDownTime by remember { mutableLongStateOf(0L) }

    val context = LocalContext.current
    LaunchedEffect(uiState.value.isBottomPanelVisible, containerSize) {
        if (!((imeInsets.calculateBottomPadding().value > 0) && isBubbleMode(context as Activity))) {
            val containerWidth = containerSize.width.toFloat()
            val containerHeight = containerSize.height.toFloat()
            if (containerWidth <= 0f || containerHeight <= 0f) return@LaunchedEffect

            val curSizePx = with(density) { settings.value.heightForLayer(1).dp.toPx() }
            val curPadPx = with(density) { settings.value.padding.dp.toPx() }

            val curVisibleWidth = (containerWidth - curSizePx - (curPadPx * 2)).coerceAtLeast(0f)
            val curVisibleHeight = (containerHeight - curSizePx - (curPadPx * 2)).coerceAtLeast(0f)

            val targetX = curVisibleWidth * settings.value.backSquareX + curPadPx
            val targetY = curVisibleHeight * settings.value.backSquareY + curPadPx

            if (!hasSizedBackSquare) {
                hasSizedBackSquare = true
                backSquareOffsetX.snapTo(targetX)
                backSquareOffsetY.snapTo(targetY)
            } else {
                backSquareOffsetX.animateTo(targetX)
                backSquareOffsetY.animateTo(targetY)
            }
        }
    }

    LaunchedEffect(isKeyboardVisible, keyboardHeight.value, settings.value.backSquareY, containerSize) {
        if (isDragging) return@LaunchedEffect
        val containerHeight = containerSize.height.toFloat()
        if (containerHeight <= 0f) return@LaunchedEffect

        val curSizePx = with(density) { settings.value.heightForLayer(1).dp.toPx() }
        val curPadPx = with(density) { settings.value.padding.dp.toPx() }
        val curVisibleHeight = (containerHeight - curSizePx - (curPadPx * 2)).coerceAtLeast(0f)
        val settingY = curVisibleHeight * settings.value.backSquareY + curPadPx
        val keyboardHeightPx = with(density) { keyboardHeight.toPx() }
        val containerBottomPadPx = with(density) { backSquarePaddingValue.calculateBottomPadding().toPx() }
        val imeOverlapPx = (keyboardHeightPx - containerBottomPadPx).coerceAtLeast(0f)

        val bottomY = curVisibleHeight - imeOverlapPx + curPadPx

        if (isKeyboardVisible) {
            keyboardBottomLine.floatValue =
                containerHeight - imeOverlapPx - curSizePx - curPadPx
            if (settingY > keyboardBottomLine.floatValue) {
                backSquareOffsetY.animateTo(
                    if (isBubbleMode(context as Activity)) bottomY else keyboardBottomLine.floatValue,
                    spring()
                )
            }
        } else if (hasSizedBackSquare && kotlin.math.abs(backSquareOffsetY.value - settingY) > 1f) {
            backSquareOffsetY.animateTo(settingY, spring())
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(backSquarePaddingValue)
            .onSizeChanged { containerSize = it }
    ) {
    AnimatedVisibility(
        visible = !uiState.value.isBottomPanelVisible && !uiState.value.isOnFullscreenVideo && !uiState.value.isOtherPanelVisible && !viewModel.isStandaloneMode.value,
        modifier = Modifier.fillMaxSize(),
        enter = fadeIn(tween(settings.value.animationSpeedForLayer(0))),
        exit = fadeOut(tween(settings.value.animationSpeedForLayer(0)))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            backSquareOffsetX.value.roundToInt(),
                            backSquareOffsetY.value.roundToInt()
                        )
                    }
                    .animateContentSize(tween(settings.value.animationSpeedForLayer(1)))
                    .size(settings.value.heightForLayer(1).dp)
                    .graphicsLayer {
                        alpha = squareAlpha.value
                    }
                    .pointerInput(activeSession, uiState.value.isCursorMode) {
                        if (!uiState.value.isCursorMode) awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            activePrimaryPointerId = down.id
                            isCursorMouseDown = false
                            hasUsedSecondaryMouse = false
                            coroutineScope.launch {
                                squareAlpha.animateTo(1f)
                            }

                            val longPressJob = coroutineScope.launch {
                                delay(viewConfiguration.longPressTimeoutMillis.milliseconds)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.updateUI { it.copy(isCursorPadVisible = true) }
                                squareAlpha.snapTo(0f)

                                val minCursorX = currentWebViewStartPadPx
                                val maxCursorX = currentWebViewStartPadPx + viewModel.screenSize.value.width.toFloat()
                                val minCursorY = currentWebViewTopPadPx
                                val maxCursorY = currentWebViewTopPadPx + viewModel.screenSize.value.height.toFloat()
                                val initialCursorX =
                                    (currentBackSquareStartPadPx + backSquareOffsetX.value + settings.value.padding + down.position.x)
                                        .coerceIn(minCursorX, maxCursorX)
                                val initialCursorY =
                                    (((viewModel.screenSize.value.height - cutoutTop.toPx()) / 2) -
                                        (viewModel.screenSize.value.height - (currentBackSquareTopPadPx + backSquareOffsetY.value - currentWebViewTopPadPx)) +
                                        down.position.y + cutoutTop.toPx())
                                        .coerceIn(minCursorY, maxCursorY)

                                viewModel.cursorPointerPosition.value =
                                    Offset(initialCursorX, initialCursorY)
                            }

                            try {
                            val drag = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                                if (longPressJob.isActive) {
                                    longPressJob.cancel()
                                }
                                change.consume()
                            }

                            if (longPressJob.isCompleted && !longPressJob.isCancelled) {
                                // long-press path
                                if (drag != null) {
                                    drag(drag.id) { change ->
                                        change.consume()
                                        val changeSpaceX =
                                            (change.position.x - change.previousPosition.x) * settings.value.cursorTrackingSpeed
                                        val changeSpaceY =
                                            (change.position.y - change.previousPosition.y) * settings.value.cursorTrackingSpeed

                                        val minCursorX = currentWebViewStartPadPx
                                        val maxCursorX = currentWebViewStartPadPx + viewModel.screenSize.value.width.toFloat()
                                        val minCursorY = currentWebViewTopPadPx
                                        val maxCursorY = currentWebViewTopPadPx + viewModel.screenSize.value.height.toFloat()
                                        val newX = (viewModel.cursorPointerPosition.value.x + changeSpaceX)
                                            .coerceIn(minCursorX, maxCursorX)
                                        val newY = (viewModel.cursorPointerPosition.value.y + changeSpaceY)
                                            .coerceIn(minCursorY, maxCursorY)

                                        viewModel.cursorPointerPosition.value = Offset(newX, newY)

                                        if (isCursorMouseDown) {
                                            val touchX = (newX - currentWebViewStartPadPx)
                                                .coerceIn(0f, (viewModel.screenSize.value.width.toFloat() - 1f).coerceAtLeast(0f))
                                            val touchY = (newY - currentWebViewTopPadPx)
                                                .coerceIn(0f, (viewModel.screenSize.value.height.toFloat() - 1f).coerceAtLeast(0f))
                                            val moveEvent = MotionEvent.obtain(
                                                cursorMouseDownTime,
                                                SystemClock.uptimeMillis(),
                                                MotionEvent.ACTION_MOVE,
                                                touchX,
                                                touchY,
                                                0
                                            )
                                            activeSession.panZoomController.onTouchEvent(moveEvent)
                                            moveEvent.recycle()
                                        }
                                    }
                                }

                                // release mouse drag if secondary finger tapped down, otherwise emit click
                                viewModel.updateUI { it.copy(isCursorPadVisible = false, isLongPressDrag = false) }

                                val touchX = (viewModel.cursorPointerPosition.value.x - currentWebViewStartPadPx)
                                    .coerceIn(0f, (viewModel.screenSize.value.width.toFloat() - 1f).coerceAtLeast(0f))
                                val touchY = (viewModel.cursorPointerPosition.value.y - currentWebViewTopPadPx)
                                    .coerceIn(0f, (viewModel.screenSize.value.height.toFloat() - 1f).coerceAtLeast(0f))

                                if (isCursorMouseDown) {
                                    val upEvent = MotionEvent.obtain(
                                        cursorMouseDownTime,
                                        SystemClock.uptimeMillis(),
                                        MotionEvent.ACTION_UP,
                                        touchX,
                                        touchY,
                                        0
                                    )
                                    activeSession.panZoomController.onTouchEvent(upEvent)
                                    upEvent.recycle()
                                    isCursorMouseDown = false
                                } else if (!hasUsedSecondaryMouse) {
                                    val downTime = SystemClock.uptimeMillis()
                                    val downEvent = MotionEvent.obtain(
                                        downTime,
                                        downTime,
                                        MotionEvent.ACTION_DOWN,
                                        touchX,
                                        touchY,
                                        0
                                    )
                                    val upEvent = MotionEvent.obtain(
                                        downTime,
                                        downTime + 10,
                                        MotionEvent.ACTION_UP,
                                        touchX,
                                        touchY,
                                        0
                                    )

                                    activeSession.panZoomController.onTouchEvent(downEvent)
                                    activeSession.panZoomController.onTouchEvent(upEvent)
                                    downEvent.recycle()
                                    upEvent.recycle()
                                }

                                coroutineScope.launch {
                                    squareAlpha.animateTo(settings.value.backSquareIdleOpacity)
                                }
                            } else {
                                // --- TAP OR SHORT-DRAG PATH ---
                                if (drag != null) {
                                    // SHORT-DRAG
                                    drag(drag.id) { change ->
                                        isDragging = true

                                        change.consume()
                                        val newX =
                                            backSquareOffsetX.value + change.position.x - change.previousPosition.x
                                        val newY =
                                            backSquareOffsetY.value + change.position.y - change.previousPosition.y


//                                        val newY = if (imeInsets.calculateBottomPadding().value > 0 && keyboardBottomLine.floatValue < fingerY) keyboardBottomLine.value else fingerY
                                        coroutineScope.launch {
                                            backSquareOffsetX.snapTo(newX)
                                            backSquareOffsetY.snapTo(newY)
                                        }
                                    }

                                    // snap logic across all four edges
                                    val currentScreenWidth = containerSize.width.toFloat()
                                    val currentScreenHeight = containerSize.height.toFloat()
                                    val maxX = (currentScreenWidth - backsquareSizePx - paddingPx).coerceAtLeast(paddingPx)
                                    val maxY = (currentScreenHeight - backsquareSizePx - paddingPx).coerceAtLeast(paddingPx)
                                    val bottomLimit = if (imeInsets.calculateBottomPadding().value > 0) {
                                        keyboardBottomLine.floatValue.coerceIn(paddingPx, maxY)
                                    } else {
                                        maxY
                                    }

                                    val distLeft = kotlin.math.abs(backSquareOffsetX.value - paddingPx)
                                    val distRight = kotlin.math.abs(maxX - backSquareOffsetX.value)
                                    val minHorizontalDist = minOf(distLeft, distRight)

                                    val distTop = kotlin.math.abs(backSquareOffsetY.value - paddingPx)
                                    val distBottom = kotlin.math.abs(bottomLimit - backSquareOffsetY.value)
                                    val minVerticalDist = minOf(distTop, distBottom)

                                    val isSnapLeftRight = minHorizontalDist < minVerticalDist

                                    val targetX = if (isSnapLeftRight) {
                                        if (distLeft <= distRight) paddingPx else maxX
                                    } else {
                                        backSquareOffsetX.value.coerceIn(paddingPx, maxX)
                                    }

                                    val targetY = if (isSnapLeftRight) {
                                        backSquareOffsetY.value.coerceIn(paddingPx, bottomLimit)
                                    } else {
                                        if (distTop <= distBottom) paddingPx else bottomLimit
                                    }

                                    val currentVisibleWidth = (currentScreenWidth - backsquareSizePx - (paddingPx * 2)).coerceAtLeast(1f)
                                    val currentVisibleHeight = (currentScreenHeight - backsquareSizePx - (paddingPx * 2)).coerceAtLeast(1f)
                                    val xPercentage = ((targetX - paddingPx) / currentVisibleWidth).coerceIn(0f, 1f)
                                    val yPercentage = if (!isSnapLeftRight && imeInsets.calculateBottomPadding().value > 0 && distBottom < distTop) {
                                        1f
                                    } else {
                                        ((targetY - paddingPx) / currentVisibleHeight).coerceIn(0f, 1f)
                                    }

                                    coroutineScope.launch {
                                        // animate snap in
                                        launch {
                                            backSquareOffsetX.animateTo(targetX, spring())
                                        }
                                        launch {
                                            backSquareOffsetY.animateTo(targetY, spring())
                                        }

                                        if (imeInsets.calculateBottomPadding().value > 0 && isSnapLeftRight) {
                                            viewModel.updateSettings {
                                                it.copy(backSquareX = xPercentage)
                                            }
                                        } else {
                                            viewModel.updateSettings {
                                                it.copy(
                                                    backSquareX = xPercentage,
                                                    backSquareY = yPercentage
                                                )
                                            }
                                        }
                                        // fade out after snap
                                        hideBackSquare(false)
                                        isDragging = false
                                    }
                                } else {
                                    // TAP
                                    if (longPressJob.isActive) {
                                        longPressJob.cancel()
                                        coroutineScope.launch {
                                            geckoViewRef.value?.let { gv ->
                                                gv.clearFocus()
                                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                                                imm?.hideSoftInputFromWindow(gv.windowToken, 0)
                                            }

                                            viewModel.updateUI {
                                                it.copy(isUrlBarVisible = true)
                                            }
                                        }
                                    }
                                }
                            }
                            } finally {
                                if (longPressJob.isActive) {
                                    longPressJob.cancel()
                                }
                                if (isCursorMouseDown) {
                                    val touchX = (viewModel.cursorPointerPosition.value.x - currentWebViewStartPadPx)
                                        .coerceIn(0f, (viewModel.screenSize.value.width.toFloat() - 1f).coerceAtLeast(0f))
                                    val touchY = (viewModel.cursorPointerPosition.value.y - currentWebViewTopPadPx)
                                        .coerceIn(0f, (viewModel.screenSize.value.height.toFloat() - 1f).coerceAtLeast(0f))
                                    val cancelEvent = MotionEvent.obtain(
                                        cursorMouseDownTime,
                                        SystemClock.uptimeMillis(),
                                        MotionEvent.ACTION_CANCEL,
                                        touchX,
                                        touchY,
                                        0
                                    )
                                    activeSession.panZoomController.onTouchEvent(cancelEvent)
                                    cancelEvent.recycle()
                                }
                                isCursorMouseDown = false
                                hasUsedSecondaryMouse = false
                                activePrimaryPointerId = null
                                if (viewModel.uiState.value.isCursorPadVisible || viewModel.uiState.value.isLongPressDrag) {
                                    viewModel.updateUI { it.copy(isCursorPadVisible = false, isLongPressDrag = false) }
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
                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
//                    .border(
//                        width = 0.5.dp,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
//                        shape = RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp)
//                    )
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                // 1. Put all 8 of your static frame images inside res/drawable
                val frames = remember {
                    listOf(
                        R.drawable.lob2_a00,
                        R.drawable.lob2_a01,
                        R.drawable.lob2_a02,
                        R.drawable.lob2_a03,
                        R.drawable.lob2_a04,
                        R.drawable.lob2_a05,
                        R.drawable.lob2_a06,
                        R.drawable.lob2_a07,
                    )
                }

                var currentFrameIndex by remember { mutableIntStateOf(0) }

                // 1. Animatable delay to smoothly ramp the speed up and down
                val currentDelayMillis = remember { Animatable(100f) }

                // 2. Smoothly ease between normal (100ms) and fast (10ms)
                LaunchedEffect(uiState.value.isLoading) {
                    val targetDelay = if (uiState.value.isLoading) 4f else 100f
                    currentDelayMillis.animateTo(
                        targetValue = targetDelay,
                        animationSpec = tween(durationMillis = if (uiState.value.isLoading) 40 else 5000) // adjust duration if you want it faster/slower to ramp
                    )
                }

                // 3. Continuously advance frames using the dynamically changing delay
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(currentDelayMillis.value.toLong().milliseconds)
                        currentFrameIndex = (currentFrameIndex + 1) % frames.size
                    }
                }

                Icon(
                    painter = painterResource(id = frames[currentFrameIndex]),
                    contentDescription = "Open Menu",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(settings.value.heightForLayer(3).dp)
                )
            }
        }
    }
    }

    // listen anywhere on screen for a secondary finger tap to latch mouse down while primary finger drags cursor
    if (uiState.value.isCursorPadVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(activeSession) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            for (change in event.changes) {
                                if (change.id == activePrimaryPointerId) continue
                                if (change.pressed && !change.previousPressed) {
                                    change.consume()
                                    hasUsedSecondaryMouse = true
                                    val touchX = (viewModel.cursorPointerPosition.value.x - currentWebViewStartPadPx)
                                        .coerceIn(0f, (viewModel.screenSize.value.width.toFloat() - 1f).coerceAtLeast(0f))
                                    val touchY = (viewModel.cursorPointerPosition.value.y - currentWebViewTopPadPx)
                                        .coerceIn(0f, (viewModel.screenSize.value.height.toFloat() - 1f).coerceAtLeast(0f))

                                    if (!isCursorMouseDown) {
                                        isCursorMouseDown = true
                                        cursorMouseDownTime = SystemClock.uptimeMillis()
                                        viewModel.updateUI { it.copy(isLongPressDrag = true) }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                                        val downEvent = MotionEvent.obtain(
                                            cursorMouseDownTime,
                                            cursorMouseDownTime,
                                            MotionEvent.ACTION_DOWN,
                                            touchX,
                                            touchY,
                                            0
                                        )
                                        activeSession.panZoomController.onTouchEvent(downEvent)
                                        downEvent.recycle()
                                    } else {
                                        isCursorMouseDown = false
                                        viewModel.updateUI { it.copy(isLongPressDrag = false) }
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                                        val upEvent = MotionEvent.obtain(
                                            cursorMouseDownTime,
                                            SystemClock.uptimeMillis(),
                                            MotionEvent.ACTION_UP,
                                            touchX,
                                            touchY,
                                            0
                                        )
                                        activeSession.panZoomController.onTouchEvent(upEvent)
                                        upEvent.recycle()
                                    }
                                } else {
                                    change.consume()
                                }
                            }
                        }
                    }
                }
        )
    }
    }
}