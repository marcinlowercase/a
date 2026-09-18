package marcinlowercase.a.core.function

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> rememberAnchoredDraggableState(
    initialValue: T,
    anchors: DraggableAnchors<T>,
): AnchoredDraggableState<T> {

    return remember(initialValue, anchors) {
        AnchoredDraggableState(
            initialValue = initialValue,
            anchors = anchors,

        )
    }
}
