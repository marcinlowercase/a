package marcinlowercase.a.ui.component

import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.R
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun CursorPad(
    urlBarFocusRequester: FocusRequester,
    coroutineScope: CoroutineScope,
    activeSession: GeckoSession,
    webViewPaddingValue: PaddingValues,
    floatingPanelStartPadding: Dp,
    floatingPanelEndPadding: Dp,
    floatingPanelBottomPadding: Dp,
    cursorPadHeight: Dp,
    geckoViewRef: MutableState<GeckoView?>,

    ) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    AnimatedVisibility(
        modifier = Modifier
            .fillMaxSize(),
        visible = uiState.value.isCursorPadVisible,
        enter = slideInVertically(
            initialOffsetY = { it }, // Start from the bottom
            animationSpec = tween(durationMillis = settings.value.animationSpeed.roundToInt())
        ) + fadeIn(tween(settings.value.animationSpeed.roundToInt())),
        exit = fadeOut(tween(settings.value.animationSpeed.roundToInt()))
    ) {

        val hapticFeedback = LocalHapticFeedback.current
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        val currentWebViewStartPadPx by rememberUpdatedState(
            with(density) { webViewPaddingValue.calculateLeftPadding(layoutDirection).toPx() }
        )
        val currentWebViewTopPadPx by rememberUpdatedState(
            with(density) { webViewPaddingValue.calculateTopPadding().toPx() }
        )

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = floatingPanelStartPadding,
                        top = webViewPaddingValue.calculateTopPadding(),
                        end = floatingPanelEndPadding,
                        bottom = floatingPanelBottomPadding
                    )
                    .windowInsetsPadding(WindowInsets.ime)


        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        cursorPadHeight
                    )
                    .align(Alignment.BottomCenter)
                    .pointerInput(activeSession, uiState.value.isCursorMode) {
                        // This is the correct "main loop". It handles one gesture at a time
                        // and then automatically resets to wait for the next one.
                        if (uiState.value.isCursorMode) awaitEachGesture {
                            // 1. Wait for the first finger to touch down.
                            val down = awaitFirstDown(requireUnconsumed = false)


                            var longPressDownTime = SystemClock.uptimeMillis()


                            val longPressJob = coroutineScope.launch {
                                delay(viewConfiguration.longPressTimeoutMillis.milliseconds)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                                longPressDownTime = SystemClock.uptimeMillis()

                            }


                            // 2. Wait for the user to start dragging.
                            val drag = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                                change.consume()
                                if (longPressJob.isActive) {
                                    longPressJob.cancel()
                                }
                            }

                            if (longPressJob.isActive) {
                                longPressJob.cancel()
                            }
                            if (longPressJob.isCompleted && !longPressJob.isCancelled) {


                                activeSession.let { _ ->
                                    val touchX = (viewModel.cursorPointerPosition.value.x - currentWebViewStartPadPx)
                                        .coerceIn(0f, (viewModel.screenSize.value.width.toFloat() - 1f).coerceAtLeast(0f))
                                    val touchY = (viewModel.cursorPointerPosition.value.y - currentWebViewTopPadPx)
                                        .coerceIn(0f, (viewModel.screenSize.value.height.toFloat() - 1f).coerceAtLeast(0f))
                                    val longPressDownEvent = MotionEvent.obtain(
                                        longPressDownTime,
                                        longPressDownTime,
                                        MotionEvent.ACTION_DOWN,
                                        touchX,
                                        touchY,
                                        0
                                    )
                                    activeSession.panZoomController.onTouchEvent(longPressDownEvent)
                                    longPressDownEvent.recycle()

                                }
                                if (drag != null) {

                                    drag(drag.id) { change ->
                                        change.consume()

                                        viewModel.updateUI { it.copy(isLongPressDrag = true) }
                                        // Check for multiple fingers DURING the drag
                                        val event = currentEvent // Get the current pointer event


                                        when (event.changes.size) {
                                            1 -> {
                                                // This is a single-finger drag, move the cursor
                                                val changeDelta =
                                                    change.position - change.previousPosition
                                                val changeSpaceX =
                                                    changeDelta.x * settings.value.cursorTrackingSpeed
                                                val changeSpaceY =
                                                    changeDelta.y * settings.value.cursorTrackingSpeed


                                                val minCursorX = currentWebViewStartPadPx
                                                val maxCursorX = currentWebViewStartPadPx + viewModel.screenSize.value.width.toFloat()
                                                val minCursorY = currentWebViewTopPadPx
                                                val maxCursorY = currentWebViewTopPadPx + viewModel.screenSize.value.height.toFloat()
                                                val newX = (viewModel.cursorPointerPosition.value.x + changeSpaceX)
                                                    .coerceIn(minCursorX, maxCursorX)
                                                val newY = (viewModel.cursorPointerPosition.value.y + changeSpaceY)
                                                    .coerceIn(minCursorY, maxCursorY)
                                                viewModel.cursorPointerPosition.value = Offset(newX, newY)



                                                activeSession.let { _ ->
                                                    val touchX = (viewModel.cursorPointerPosition.value.x - currentWebViewStartPadPx)
                                                        .coerceIn(0f, (viewModel.screenSize.value.width.toFloat() - 1f).coerceAtLeast(0f))
                                                    val touchY = (viewModel.cursorPointerPosition.value.y - currentWebViewTopPadPx)
                                                        .coerceIn(0f, (viewModel.screenSize.value.height.toFloat() - 1f).coerceAtLeast(0f))
                                                    val moveEvent = MotionEvent.obtain(
                                                        longPressDownTime, // this the value to define when we triggered the move
                                                        SystemClock.uptimeMillis(),
                                                        MotionEvent.ACTION_MOVE,
                                                        touchX,
                                                        touchY,
                                                        0
                                                    )

                                                    activeSession.panZoomController.onTouchEvent(
                                                        moveEvent
                                                    )
                                                }
                                            }



//                                            2 -> {
//
//                                                val changeDelta =
//                                                    change.position - change.previousPosition
//                                                var changeSpaceY = changeDelta.y
//
//
//                                                if (activeWebView != null) {
//
//                                                    if (!activeWebView.canScrollVertically(1) && changeSpaceY < 0) changeSpaceY =
//                                                        0f
//                                                    if (!activeWebView.canScrollVertically(-1) && changeSpaceY > 0) changeSpaceY =
//                                                        0f
//
//                                                    // We negate the value for "natural" scrolling (fingers down -> content up).
//                                                    activeWebView.scrollBy(
//                                                        0,
//                                                        -changeSpaceY.roundToInt()
//                                                    )
//                                                }
//
//
//                                                // 3. Consume the changes to prevent single-finger logic from also running.
//                                                event.changes.forEach { it.consume() }
//                                            }
//
//                                            3 -> {
//                                                val changeDelta =
//                                                    change.position - change.previousPosition
//                                                val changeSpaceY = changeDelta.y
//
//                                                if (changeSpaceY < 0) {
//                                                    setIsCursorPadVisible(false)
//                                                    setIsUrlBarVisible(true)
//                                                }
//                                            }
                                        }
                                    }
                                }

                                viewModel.updateUI { it.copy(isLongPressDrag = false) }


                                activeSession.let { _ ->
                                    val touchX = (viewModel.cursorPointerPosition.value.x - currentWebViewStartPadPx)
                                        .coerceIn(0f, (viewModel.screenSize.value.width.toFloat() - 1f).coerceAtLeast(0f))
                                    val touchY = (viewModel.cursorPointerPosition.value.y - currentWebViewTopPadPx)
                                        .coerceIn(0f, (viewModel.screenSize.value.height.toFloat() - 1f).coerceAtLeast(0f))
                                    val upEvent = MotionEvent.obtain(
                                        longPressDownTime,
                                        SystemClock.uptimeMillis(),
                                        MotionEvent.ACTION_UP,
                                        touchX,
                                        touchY,
                                        0
                                    )
                                    activeSession.panZoomController.onTouchEvent(upEvent)

                                }

                            } else {

                                if (drag != null) {

                                    var horizontalOverscrollAccumulator = 0f
                                    var hasNavigatedInThisGesture = false
                                    val navThreshold = 70.dp.toPx()
                                    // This is the high-level function that consumes the rest of the drag gesture.
                                    // It will finish when the user lifts their finger.
                                    drag(drag.id) { change ->
                                        change.consume()

                                        // Check for multiple fingers DURING the drag
                                        val event = currentEvent // Get the current pointer event


                                        when (event.changes.size) {
                                            1 -> {
                                                // This is a single-finger drag, move the cursor
                                                val changeDelta =
                                                    change.position - change.previousPosition
                                                val changeSpaceX =
                                                    changeDelta.x * settings.value.cursorTrackingSpeed
                                                val changeSpaceY =
                                                    changeDelta.y * settings.value.cursorTrackingSpeed


                                                val minCursorX = currentWebViewStartPadPx
                                                val maxCursorX = currentWebViewStartPadPx + viewModel.screenSize.value.width.toFloat()
                                                val minCursorY = currentWebViewTopPadPx
                                                val maxCursorY = currentWebViewTopPadPx + viewModel.screenSize.value.height.toFloat()
                                                val newX = (viewModel.cursorPointerPosition.value.x + changeSpaceX)
                                                    .coerceIn(minCursorX, maxCursorX)
                                                val newY = (viewModel.cursorPointerPosition.value.y + changeSpaceY)
                                                    .coerceIn(minCursorY, maxCursorY)

                                                viewModel.cursorPointerPosition.value = Offset(newX, newY)
                                            }

                                            2 -> {
                                                // --- 2 FINGERS: SCROLL & NAVIGATE ---
                                                val changeDelta = change.position - change.previousPosition
                                                val dx = changeDelta.x
                                                val dy = changeDelta.y

                                                val scrollSpeedMultiplier = 4.0
                                                val scrollAmountY = -dy.toDouble() * scrollSpeedMultiplier
                                                var scrollAmountX = 0.0

                                                // 1. Horizontal scroll vs Navigation Check
                                                if (dx > 0) {
                                                    // Fingers moving RIGHT (Pan left, or go Back)
                                                    if (geckoViewRef.value?.canScrollHorizontally(-1) == true) {
                                                        scrollAmountX = -dx.toDouble() * scrollSpeedMultiplier
                                                        horizontalOverscrollAccumulator = 0f // Reset if we are scrolling
                                                    } else {
                                                        horizontalOverscrollAccumulator += dx
                                                    }
                                                } else
                                                    if (dx < 0) {
                                                    // Fingers moving LEFT (Pan right, or go Forward)
                                                    if (geckoViewRef.value?.canScrollHorizontally(1) == true) {
                                                        scrollAmountX = -dx.toDouble() * scrollSpeedMultiplier
                                                        horizontalOverscrollAccumulator = 0f // Reset if we are scrolling
                                                    } else {
                                                        horizontalOverscrollAccumulator += dx
                                                    }
                                                }

                                                // 2. Apply Page Scroll
                                                if (scrollAmountX != 0.0 || scrollAmountY != 0.0) {
                                                    activeSession.panZoomController.scrollBy(
                                                        org.mozilla.geckoview.ScreenLength.fromPixels(scrollAmountX),
                                                        org.mozilla.geckoview.ScreenLength.fromPixels(scrollAmountY),
                                                        org.mozilla.geckoview.PanZoomController.SCROLL_BEHAVIOR_SMOOTH
                                                    )
                                                }

                                                // 3. Trigger History Navigation (if edge is hit and swiped far enough)
                                                if (!hasNavigatedInThisGesture) {
                                                    if (horizontalOverscrollAccumulator > navThreshold) {
                                                        if (viewModel.activeTab!!.canGoBack) {
                                                            activeSession.goBack(true)
                                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            hasNavigatedInThisGesture = true
                                                        }
                                                    } else if (horizontalOverscrollAccumulator < -navThreshold) {
                                                        if (viewModel.activeTab!!.canGoForward) {
                                                            activeSession.goForward(true)
                                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            hasNavigatedInThisGesture = true
                                                        }
                                                    }
                                                }

                                                event.changes.forEach { it.consume() }
                                            }


                                            3 -> {
                                                val changeDelta =
                                                    change.position - change.previousPosition
                                                val changeSpaceY = changeDelta.y
                                                viewModel.updateUI { it.copy(isCursorMode = false) }
                                                viewModel.updateUI { it.copy(isUrlBarVisible = true) }
                                                if (changeSpaceY < 0) {
                                                    urlBarFocusRequester.requestFocus()

                                                    // get focus to the url bar
                                                    // use focusRequester to get focus to the url bar

                                                }
                                            }
                                        }
                                    }
                                } else {

//                                 Work but cannot click under the cursor pad
                                    // -> use for 2 finger capture?
//                                            CursorAccessibilityService.instance?.performClick(
//                                                viewModel.cursorPointerPosition.value.x,
//                                                viewModel.cursorPointerPosition.value.y
//                                            )

                                    Log.e("marcCursor", "long press canceled")

                                    activeSession.let { _ ->
                                        val downTime = SystemClock.uptimeMillis()
                                        val touchX = (viewModel.cursorPointerPosition.value.x - currentWebViewStartPadPx)
                                            .coerceIn(0f, (viewModel.screenSize.value.width.toFloat() - 1f).coerceAtLeast(0f))
                                        val touchY = (viewModel.cursorPointerPosition.value.y - currentWebViewTopPadPx)
                                            .coerceIn(0f, (viewModel.screenSize.value.height.toFloat() - 1f).coerceAtLeast(0f))
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
                                            SystemClock.uptimeMillis(),
                                            MotionEvent.ACTION_UP,
                                            touchX,
                                            touchY,
                                            0
                                        )
                                        activeSession.panZoomController.onTouchEvent(downEvent)
                                        activeSession.panZoomController.onTouchEvent(upEvent)
                                    }
                                }
                            }
                            // 3. If a drag was detected, enter the drag-handling logic.

                            // 4. After the drag is over (finger lifted), this block finishes.
                            // The `awaitEachGesture` loop will now start over from the top,
                            // ready to `awaitFirstDown` for the next gesture.
                        }
                    }

                    .padding(
                        end = settings.value.padding.dp,
                        start = settings.value.padding.dp, // Add start padding for when it's on the left
                        bottom = settings.value.padding.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(1).dp
                        )
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_trackpad_input),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
