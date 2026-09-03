package marcinlowercase.a.ui.component

import android.app.Activity
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
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
    hideBackSquare: suspend (Boolean) -> Unit
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val viewConfiguration = LocalViewConfiguration.current


    val screenWidth = viewModel.screenSize.value.width.toFloat()
    val screenHeight = viewModel.screenSize.value.height.toFloat()

    val backsquareSizePx = with(density) { settings.value.heightForLayer(1).dp.toPx() }
    val paddingPx = with(density) { settings.value.padding.dp.toPx() }

    // need to update realtime, that's why it appears in launched effects
    // backsquareVisibleHeight/Width -> bsVisibleHeight/Width
    val bsVisibleWidth = screenWidth - (backsquareSizePx / 2 * 2) - (paddingPx * 2)
    val bsVisibleHeight = screenHeight - (backsquareSizePx / 2 * 2) - (paddingPx * 2)

    val x =
        bsVisibleWidth * settings.value.backSquareX + paddingPx + backsquareSizePx / 2 - backsquareSizePx / 2
    val y =
        bsVisibleHeight * settings.value.backSquareY + paddingPx + backsquareSizePx / 2 - backsquareSizePx / 2


    val backSquareOffsetX = remember { Animatable(x) }
    val backSquareOffsetY = remember { Animatable(y) }


    val imeInsets = WindowInsets.ime.asPaddingValues()
    val keyboardHeight = imeInsets.calculateBottomPadding()
    val isKeyboardVisible = keyboardHeight > 0.dp
    val keyboardBottomLine = remember { mutableFloatStateOf(0f) }

    var isDragging by remember { mutableStateOf(false) }

    val context = LocalContext.current
    // BackSquare Display
    LaunchedEffect(uiState.value.isBottomPanelVisible, viewModel.screenSize.value) {
        if (
//            !uiState.value.isBottomPanelVisible &&
            !((imeInsets.calculateBottomPadding().value > 0) && isBubbleMode(context as Activity))
            ) {
           
            val screenWidth = viewModel.screenSize.value.width.toFloat()
            val screenHeight = viewModel.screenSize.value.height.toFloat()
            if (screenWidth <= 0f || screenHeight <= 0f) return@LaunchedEffect

            val backsquareSizePx = with(density) { settings.value.heightForLayer(1).dp.toPx() }
            val paddingPx = with(density) { settings.value.padding.dp.toPx() }

            // backsquareVisibleHeight/Width -> bsVisibleHeight/Width
            val bsVisibleWidth = screenWidth - (backsquareSizePx / 2 * 2) - (paddingPx * 2)
            val bsVisibleHeight = screenHeight - (backsquareSizePx / 2 * 2) - (paddingPx * 2)

            val x =
                bsVisibleWidth * settings.value.backSquareX + paddingPx + backsquareSizePx / 2 - backsquareSizePx / 2
            val y =
                bsVisibleHeight * settings.value.backSquareY + paddingPx + backsquareSizePx / 2 - backsquareSizePx / 2
            
            backSquareOffsetX.animateTo(x)
            backSquareOffsetY.animateTo(y)
        }
    }


    // Keyboard Detect
    LaunchedEffect(isKeyboardVisible, keyboardHeight.value, settings.value.backSquareY) {
        if (isDragging) return@LaunchedEffect // Skip state syncing during user interaction

//        val squareBoxSize = settings.value.heightForLayer(1).dp
        val backsquareSizePx = with(density) { settings.value.heightForLayer(1).dp.toPx() }


        val paddingPx = with(density) { settings.value.padding.dp.toPx() }
        val screenHeight = viewModel.screenSize.value.height.toFloat()
        Log.i("BackSquare", "screenHeight: $screenHeight")
        val bsVisibleHeight = screenHeight - (backsquareSizePx / 2 * 2) - (paddingPx * 2)
        val settingY =
            bsVisibleHeight * settings.value.backSquareY + paddingPx + backsquareSizePx / 2 - backsquareSizePx / 2
        val keyboardHeightPx = with(density) { keyboardHeight.toPx() }

//        val bottomY = bsVisibleHeight + paddingPx + backsquareSizePx / 2 - backsquareSizePx / 2
        val bottomY = bsVisibleHeight - keyboardHeightPx + paddingPx

        if (isKeyboardVisible) {

            // Calculate where the top of the BackSquare should be to sit exactly on top of the keyboard + padding
            keyboardBottomLine.floatValue =
                screenHeight - keyboardHeightPx - backsquareSizePx - paddingPx
            // Only move it if the saved position is actually LOWER (visually below) the keyboard top

            if (settingY > keyboardBottomLine.floatValue) {
                backSquareOffsetY.animateTo(if (isBubbleMode(context as Activity)) bottomY else keyboardBottomLine.floatValue, spring())
            }
        } else {
            // Keyboard is hidden.
            // If the square is currently not at its saved position (meaning it was moved by the keyboard logic),
            // put it back where the user left it.
            // We use a small threshold (1f) to avoid floating point comparison issues
            if (kotlin.math.abs(backSquareOffsetY.value - settingY) > 1f) {

                backSquareOffsetY.animateTo(settingY, spring())
            }
        }
    }


    AnimatedVisibility(
        visible = !uiState.value.isBottomPanelVisible && !uiState.value.isLandscape && !uiState.value.isOtherPanelVisible && !viewModel.isStandaloneMode.value,
        modifier = modifier
            .fillMaxSize()
            .padding(webViewPaddingValue)
//            .onSizeChanged {
//                viewModel.screenSize.value = it
//                with(density) {
//                    viewModel.screenSizeDp.value = IntSize(
//                        it.width.toDp().value.roundToInt(),
//                        it.height.toDp().value.roundToInt()
//                    )
//                }
//            }
        ,
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
                            coroutineScope.launch {
                                squareAlpha.animateTo(1f)
                            }

                            val longPressJob = coroutineScope.launch {
                                delay(viewConfiguration.longPressTimeoutMillis.milliseconds)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.updateUI { it.copy(isCursorPadVisible = true) }
                                squareAlpha.snapTo(0f)

                                val initialCursorX =
                                    backSquareOffsetX.value + settings.value.padding + down.position.x
                                val initialCursorY =
                                    ((viewModel.screenSize.value.height - cutoutTop.toPx()) / 2) - (viewModel.screenSize.value.height - backSquareOffsetY.value) + down.position.y + cutoutTop.toPx()

                                viewModel.cursorPointerPosition.value =
                                    Offset(initialCursorX, initialCursorY)
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
                                        val changeSpaceX =
                                            (change.position.x - change.previousPosition.x) * settings.value.cursorTrackingSpeed
                                        val changeSpaceY =
                                            (change.position.y - change.previousPosition.y) * settings.value.cursorTrackingSpeed

                                        var newX =
                                            viewModel.cursorPointerPosition.value.x + changeSpaceX
                                        var newY =
                                            viewModel.cursorPointerPosition.value.y + changeSpaceY
                                        if (newX < 0) newX = 0f
                                        if (newX > viewModel.screenSize.value.width) newX =
                                            viewModel.screenSize.value.width.toFloat()
                                        if (newY < 0) newY = 0f
                                        if (newY > viewModel.screenSize.value.height) newY =
                                            viewModel.screenSize.value.height.toFloat() + backsquareSizePx / 2

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
                                        viewModel.cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding()
                                            .toPx(),
                                        0
                                    )
                                    val upEvent = MotionEvent.obtain(
                                        downTime,
                                        downTime + 10,
                                        MotionEvent.ACTION_UP,
                                        viewModel.cursorPointerPosition.value.x,
                                        viewModel.cursorPointerPosition.value.y - webViewPaddingValue.calculateTopPadding()
                                            .toPx(),
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

                                    // snap logic
                                    val screenWidth = viewModel.screenSize.value.width.toFloat()


                                    // snap back square to left or right side of the screen if Y is between 10% and 90%


                                    // USE this block if we want the back square go around all edges
//                                    val xCompareValue =  if (backSquareOffsetX.value + (backsquareSizePx / 2) < screenWidth / 2) {
//                                        backSquareOffsetX.value // Take Left
//                                    } else
//                                        screenWidth - backSquareOffsetX.value // Take Right
//
//                                    val yCompareValue =  if (backSquareOffsetY.value + (backsquareSizePx / 2) < screenHeight / 2) {
//                                        backSquareOffsetY.value // Snap Top
//                                    } else
//                                        screenHeight - backSquareOffsetY.value // Snap Bottom
//
//                                    val isSnapLeftRight = xCompareValue < yCompareValue
                                    val isSnapLeftRight = true

                                    val targetX =
                                        if (isSnapLeftRight) {
                                            if (backSquareOffsetX.value + (backsquareSizePx / 2) < screenWidth / 2) {
                                                paddingPx // Snap Left
                                            } else
                                                screenWidth - backsquareSizePx - paddingPx // Snap Right

                                        } else {
                                            backSquareOffsetX.value.coerceIn(
                                                paddingPx,
                                                viewModel.screenSize.value.width.toFloat() - backsquareSizePx - paddingPx
                                            )
                                        }


                                    // Clamp Y to screen bounds / keyboard bounds
                                    val targetY =
                                        if (isSnapLeftRight) {
                                            backSquareOffsetY.value.coerceIn(
                                                paddingPx,
                                                if (imeInsets.calculateBottomPadding().value > 0) keyboardBottomLine.floatValue else viewModel.screenSize.value.height.toFloat() - backsquareSizePx - paddingPx
                                            )
                                        } else {
                                            if (backSquareOffsetY.value + (backsquareSizePx / 2) < screenHeight / 2) {
                                                paddingPx // Snap Top
                                            } else
                                                screenHeight - backsquareSizePx - paddingPx // Snap Bottom
                                        }


                                    val xPercentage =
                                        (targetX - backsquareSizePx / 2 - paddingPx + backsquareSizePx / 2) / bsVisibleWidth
                                    val yPercentage =
                                        (targetY - backsquareSizePx / 2 - paddingPx + backsquareSizePx / 2) / bsVisibleHeight

                                    coroutineScope.launch {
                                        // Animate snap in
                                        launch {
                                            backSquareOffsetX.animateTo(targetX, spring())
                                        }
                                        launch {
                                            backSquareOffsetY.animateTo(targetY, spring())
                                        }



                                        if (imeInsets.calculateBottomPadding().value > 0) {
                                            viewModel.updateSettings {
                                                it.copy(
                                                    backSquareX = xPercentage,
//                                                    backSquareY = yPercentage
                                                )
                                            }
                                        } else {
                                            viewModel.updateSettings {
                                                it.copy(
                                                    backSquareX = xPercentage,
                                                    backSquareY = yPercentage
                                                )
                                            }
                                        }
                                        // Fade out after snap
                                        hideBackSquare(false)
                                        isDragging = false

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
                    .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(settings.value.cornerRadiusForLayer(1).dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = !uiState.value.isLoading,
                    enter = fadeIn(animationSpec = tween(settings.value.animationSpeed.roundToInt())),
                    exit = fadeOut(animationSpec = tween(settings.value.animationSpeed.roundToInt()))
                ) {
                    // 1. Put all 8 of your static frame images inside res/drawable
                    // and list them here in order.
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

                    // 2. Keep track of which frame index is currently showing
                    var currentFrameIndex by remember { mutableIntStateOf(0) }

                    // 3. Set how long each individual frame stays on screen (e.g., 100ms)
                    val frameDurationMillis = 100L

                    // 4. Drive the loop strictly forward: 0, 1, 2, 3, 4, 5, 6, 7 -> 0
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(frameDurationMillis.milliseconds)
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

                AnimatedVisibility(
                    visible = uiState.value.isLoading,
                    modifier = modifier,
                    enter = fadeIn(animationSpec = tween(settings.value.animationSpeed.roundToInt())),
                    exit = fadeOut(animationSpec = tween(settings.value.animationSpeed.roundToInt()))
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(settings.value.padding.dp)
                            .size(settings.value.heightForLayer(4).dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        strokeWidth = 0.5.dp
                    )
                }
            }
        }
    }
}