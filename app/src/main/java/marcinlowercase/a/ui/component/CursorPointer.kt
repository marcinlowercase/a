import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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


    AnimatedVisibility(
        visible = isCursorPadVisible,
        enter = fadeIn(tween(settings.value.animationSpeed.roundToInt())),
        exit = fadeOut(tween(settings.value.animationSpeed.roundToInt())),
        modifier = Modifier
    ) {
        val cursorContainerSize = settings.value.cursorContainerSize.dp
        val pointerSize = cursorContainerSize / 2
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        position.x.roundToInt() - pointerSize.toPx().toInt(), // Center the icon
                        position.y.roundToInt() - pointerSize.toPx().toInt()
                    )
                }
                .size(cursorContainerSize)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
//                .border(2.dp, Color.White, CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_dot), // Ensure you have this drawable
                contentDescription = "Quick Cursor",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(settings.value.cursorPointerSize.dp)
            )
        }
    }
}