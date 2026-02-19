package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.enum_class.GestureNavAction
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel

@Composable
fun NavigationPanel(
    isNavPanelVisible: Boolean,
    modifier: Modifier = Modifier,
    activeAction: GestureNavAction,

    ) {
    val viewModel = LocalBrowserViewModel.current
val settings = viewModel.browserSettings.collectAsState()
    AnimatedVisibility(
        visible = isNavPanelVisible,
        enter = expandVertically(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ) + fadeIn(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ),
        exit = shrinkVertically(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ) + fadeOut(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = settings.value.padding.dp)
                .padding(top = settings.value.padding.dp)
        ) {
            Column(
                modifier = modifier

                    .clip(
                        RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(2).dp
                        )
                    )
                    .background(Color.Black.copy(0.3f)),

                ) {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = settings.value.padding.dp)
                        .padding(horizontal = settings.value.padding.dp),


                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.CLOSE_TAB,
                        actionIcon = painterResource(R.drawable.ic_close),
                    )


                    // Refresh Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.REFRESH,
                        actionIcon = painterResource(R.drawable.ic_refresh),
                    )

                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.NEW_TAB,
                        actionIcon = painterResource(R.drawable.ic_add),
                    )
                }
                Row(
                    modifier = modifier
                        .fillMaxWidth()

                        .padding(settings.value.padding.dp),
                    horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.BACK,
                        actionIcon = painterResource(R.drawable.ic_arrow_back),
                        visibility = viewModel.activeTab!!.canGoBack,
                        )

                    // Cancel Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.NONE,
                        actionIcon = painterResource(R.drawable.ic_minimize),
                    )

                    // Forward Icon
                    // Back Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.FORWARD,
                        actionIcon = painterResource(R.drawable.ic_arrow_forward),
                        visibility = viewModel.activeTab!!.canGoForward,
                        )
                }
            }
        }
    }
}
@Composable
fun NavigationItem(
    modifier: Modifier,
    activeAction: GestureNavAction,
    gestureNavAction: GestureNavAction,
    actionIcon: Painter,
    visibility: Boolean = true,

    ) {
    val viewModel = LocalBrowserViewModel.current
val settings = viewModel.browserSettings.collectAsState()
    // Cancel Icon
    val refreshColor by animateColorAsState(if (activeAction == gestureNavAction) Color.White else Color.Transparent)
    Box(
        modifier = modifier
            .height(
                settings.value.heightForLayer(3).dp
            )
            .clip(
                RoundedCornerShape(
                    settings.value.cornerRadiusForLayer(3).dp
                )
            )
            .background(refreshColor)
    ) {
        if (visibility) {
            Icon(
                actionIcon,
                "Refresh",
                Modifier.align(Alignment.Center),
                tint = if (activeAction == gestureNavAction) Color.Black else Color.White
            )
        }

    }

}