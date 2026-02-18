package marcinlowercase.a.ui.component

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.R
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.PanZoomController
import org.mozilla.geckoview.ScreenLength
import kotlin.math.roundToInt


@Composable
fun CursorPad(
    urlBarFocusRequester: FocusRequester,
    screenSize: IntSize,
    coroutineScope: CoroutineScope,
    activeSession: GeckoSession,
    cursorPointerPosition: MutableState<Offset>,
    webViewPaddingValue: PaddingValues,
    cursorPadHeight: Dp,
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState().value
    val settings = viewModel.browserSettings.collectAsState().value

    AnimatedVisibility(
        modifier = Modifier
            .fillMaxSize(),
        visible = uiState.isCursorPadVisible,
        enter = slideInVertically(
            initialOffsetY = { it }, // Start from the bottom
            animationSpec = tween(durationMillis = settings.animationSpeed.roundToInt())
        ) + fadeIn(tween(settings.animationSpeed.roundToInt())),
        exit = fadeOut(tween(settings.animationSpeed.roundToInt()))
    ) {

        val hapticFeedback = LocalHapticFeedback.current

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(webViewPaddingValue)
                    .windowInsetsPadding(WindowInsets.ime)


        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        cursorPadHeight
                    )
                    .align(Alignment.BottomCenter)
                    .pointerInput(Unit) {
                        // This is the correct "main loop". It handles one gesture at a time
                        // and then automatically resets to wait for the next one.
                        awaitEachGesture {
                            // 1. Wait for the first finger to touch down.
                            val down = awaitFirstDown(requireUnconsumed = false)


                            var longPressDownTime = System.currentTimeMillis()


                            val longPressJob = coroutineScope.launch {
                                delay(viewConfiguration.longPressTimeoutMillis)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                                longPressDownTime = System.currentTimeMillis()

                            }


                            // 2. Wait for the user to start dragging.
                            val drag = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                                change.consume()
                                if (longPressJob.isActive) {
                                    longPressJob.cancel()
                                }
                            }


                            if (longPressJob.isCompleted && !longPressJob.isCancelled) {


                                activeSession.let { _ ->
//                                    longPressDownTime = System.currentTimeMillis()
                                    val longPressDownEvent = MotionEvent.obtain(
                                        longPressDownTime,
                                        longPressDownTime,
                                        MotionEvent.ACTION_DOWN,
                                        cursorPointerPosition.value.x,
                                        cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding()
                                            .toPx(),
                                        0
                                    )
                                    activeSession.panZoomController.onTouchEvent(longPressDownEvent)

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
                                                    changeDelta.x * settings.cursorTrackingSpeed
                                                val changeSpaceY =
                                                    changeDelta.y * settings.cursorTrackingSpeed


                                                var newX =
                                                    cursorPointerPosition.value.x + changeSpaceX
                                                var newY =
                                                    cursorPointerPosition.value.y + changeSpaceY
                                                if (newX < 0) newX = 0f
                                                if (newX > screenSize.width) newX =
                                                    screenSize.width.toFloat()
                                                if (newY < 0) newY = 0f
                                                if (newY > screenSize.height) newY =
                                                    screenSize.height.toFloat()
                                                cursorPointerPosition.value = Offset(newX, newY)



                                                activeSession.let { _ ->
                                                    val moveEvent = MotionEvent.obtain(
                                                        System.currentTimeMillis(),
                                                        System.currentTimeMillis(),
                                                        MotionEvent.ACTION_MOVE,
                                                        cursorPointerPosition.value.x,
                                                        cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding()
                                                            .toPx(),
                                                        0
                                                    )

                                                    activeSession.panZoomController.onTouchEvent(
                                                        moveEvent
                                                    )


                                                }

//                                            cursorPointerPosition.value += Offset(
//                                                changeSpaceX,
//                                                changeSpaceY
//                                            )
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
                                    val upEvent = MotionEvent.obtain(
                                        longPressDownTime,
                                        System.currentTimeMillis(),
                                        MotionEvent.ACTION_UP,
                                        cursorPointerPosition.value.x,
                                        cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding()
                                            .toPx(),
                                        0
                                    )
                                    activeSession.panZoomController.onTouchEvent(upEvent)

                                }

                            } else {

                                if (drag != null) {
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
                                                    changeDelta.x * settings.cursorTrackingSpeed
                                                val changeSpaceY =
                                                    changeDelta.y * settings.cursorTrackingSpeed


                                                var newX =
                                                    cursorPointerPosition.value.x + changeSpaceX
                                                var newY =
                                                    cursorPointerPosition.value.y + changeSpaceY
                                                if (newX < 0) newX = 0f
                                                if (newX > screenSize.width) newX =
                                                    screenSize.width.toFloat()
                                                if (newY < 0) newY = 0f
                                                if (newY > screenSize.height) newY =
                                                    screenSize.height.toFloat()

                                                cursorPointerPosition.value = Offset(newX, newY)
//                                                activeWebView?.evaluateJavascript(
//                                                    "window.simulateHover($newX, $newY)",
//                                                    null
//                                                )
//                                            cursorPointerPosition.value += Offset(
//                                                changeSpaceX,
//                                                changeSpaceY
//                                            )
                                            }

                                            2 -> {

                                                val changeDelta =
                                                    change.position - change.previousPosition


                                                val scrollAmountY = -changeDelta.y.toDouble()

                                                // 3. Execute Scroll
                                                // SCROLL_METHOD_IMMEDIATE ensures it feels like a trackpad (1:1 movement)
                                                // SCROLL_METHOD_SMOOTH would add an animation (momentum), which feels laggy for a trackpad.
                                                activeSession.panZoomController.scrollBy(
                                                    ScreenLength.fromPixels(0.0),
                                                    ScreenLength.fromPixels(scrollAmountY * 2),
                                                    PanZoomController.SCROLL_BEHAVIOR_SMOOTH,
                                                )


                                                // 3. Consume the changes to prevent single-finger logic from also running.
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
//                                                cursorPointerPosition.value.x,
//                                                cursorPointerPosition.value.y
//                                            )

                                    activeSession.let { _ ->
                                        val downTime = System.currentTimeMillis()
                                        val downEvent = MotionEvent.obtain(
                                            downTime,
                                            downTime,
                                            MotionEvent.ACTION_DOWN,
                                            cursorPointerPosition.value.x,
                                            cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding()
                                                .toPx(),
                                            0
                                        )
                                        val upEvent = MotionEvent.obtain(
                                            downTime,
                                            downTime + 10,
                                            MotionEvent.ACTION_UP,
                                            cursorPointerPosition.value.x,
                                            cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding()
                                                .toPx(),
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
                        end = settings.padding.dp,
                        start = settings.padding.dp, // Add start padding for when it's on the left
                        bottom = settings.padding.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            settings.cornerRadiusForLayer(1).dp
                        )
                    )
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_trackpad_input),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
    }
}
