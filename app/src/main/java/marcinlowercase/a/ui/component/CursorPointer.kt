import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import marcinlowercase.a.R
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import kotlin.math.roundToInt

@Composable
fun CursorPointer(
    isCursorPadVisible: Boolean,
    position: Offset,
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()

    // Track the recent history of coordinates for the tail
    var trailPoints by remember { mutableStateOf(listOf<Offset>()) }
    val maxTailLength = 15 // Increase to make the tail longer

    // Ripple state for the "click" animation
    var rippleRadius by remember { mutableFloatStateOf(0f) }
    var rippleAlpha by remember { mutableFloatStateOf(0f) }
    var clickOffset by remember { mutableStateOf(Offset.Zero) }

    // Squeeze scale for the pointer (shrinks when clicking, pops up when appearing)
    val pointerScale by animateFloatAsState(
        targetValue = if (isCursorPadVisible) 1f else 0.4f,
        animationSpec = if (isCursorPadVisible) {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        } else {
            tween(settings.value.animationSpeed.roundToInt())
        },
        label = "CursorSqueeze"
    )

    // Trigger the ripple expansion when the user lifts their finger (tap occurs)
    LaunchedEffect(isCursorPadVisible) {
        if (!isCursorPadVisible) {
            clickOffset = position // Anchor the wave exactly where they clicked

            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(settings.value.animationSpeed.roundToInt())
            ) { value, _ ->
                rippleRadius = value * 120f // Ripple expands outwards
                rippleAlpha = 0.8f * (1f - value) // Fades out smoothly as it expands
            }
        } else {
            // Reset state when the cursor reappears
            rippleAlpha = 0f
            rippleRadius = 0f
        }
    }

    // Continuously record positions and decay them when the cursor stops
    LaunchedEffect(position, isCursorPadVisible) {
        if (!isCursorPadVisible) {
            // Return early instead of clearing the list, so the tail freezes
            // in place and gracefully fades out with the exit animation!
            return@LaunchedEffect
        }

        // Add the current position to our trail
        trailPoints = (trailPoints + position).takeLast(maxTailLength)

        // Wait a short duration; if the cursor moves again, this coroutine resets
        delay(30)

        // If movement stops, gracefully shrink the tail frame-by-frame
        while (trailPoints.isNotEmpty()) {
            delay(16) // roughly 1 frame at 60fps
            trailPoints = trailPoints.drop(1)
        }
    }

    AnimatedVisibility(
        visible = isCursorPadVisible,
        enter = fadeIn(tween(settings.value.animationSpeed.roundToInt())),
        exit = fadeOut(tween(settings.value.animationSpeed.roundToInt())),
        modifier = Modifier.fillMaxSize() // Force max size so we can draw the trail anywhere
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val cursorContainerSize = settings.value.cursorContainerSize.dp
            val pointerSize = cursorContainerSize / 2

            // --- Draw the Tail and Click Ripple ---
            Canvas(modifier = Modifier.fillMaxSize()) {

                // Draw Click Ripple Wave
                if (rippleAlpha > 0f) {
                    drawCircle(
                        color = Color(settings.value.highlightColor).copy(alpha = rippleAlpha),
                        radius = rippleRadius,
                        center = clickOffset,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                if (trailPoints.size > 1) {
                    // Connect the dots line-by-line so we can taper thickness and alpha
                    for (i in 0 until trailPoints.size - 1) {
                        val start = trailPoints[i]
                        val end = trailPoints[i + 1]

                        // Fraction ranges from ~0.0 (oldest point/tip of tail) to 1.0 (newest point/base)
                        val fraction = (i + 1) / trailPoints.size.toFloat()

                        // Taper the trail line thickness and fade out the oldest points
                        val strokeWidth = fraction * 8.dp.toPx()
                        val alpha = fraction * 0.8f

                        drawLine(
                            color = Color(settings.value.highlightColor).copy(alpha = alpha),
                            start = start,
                            end = end,
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // --- Draw the Pointer Circle ---
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            position.x.roundToInt() - pointerSize.toPx().toInt(), // Center the icon
                            position.y.roundToInt() - pointerSize.toPx().toInt()
                        )
                    }
                    .size(cursorContainerSize)
                    .scale(pointerScale) // <--- APPLIES SQUEEZE/POP ANIMATION
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dot),
                    contentDescription = "Quick Cursor",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(settings.value.cursorPointerSize.dp)
                )
            }
        }
    }
}