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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.custom_class.CustomWebView
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.enum_class.GestureNavAction

@Composable
fun NavigationPanel(
    activeWebView: CustomWebView?,
    isNavPanelVisible: Boolean,
    modifier: Modifier = Modifier,
    browserSettings: MutableState<BrowserSettings>,
    activeAction: GestureNavAction,

    ) {
    AnimatedVisibility(
        visible = isNavPanelVisible,
        enter = expandVertically(
            tween(
                browserSettings.value.animationSpeedForLayer(1)
            )
        ) + fadeIn(
            tween(
                browserSettings.value.animationSpeedForLayer(1)
            )
        ),
        exit = shrinkVertically(
            tween(
                browserSettings.value.animationSpeedForLayer(1)
            )
        ) + fadeOut(
            tween(
                browserSettings.value.animationSpeedForLayer(1)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = browserSettings.value.padding.dp)
                .padding(top = browserSettings.value.padding.dp)
        ) {
            Column(
                modifier = modifier

                    .clip(
                        RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(2).dp
                        )
                    )
                    .background(Color.Black.copy(0.3f)),

                ) {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = browserSettings.value.padding.dp)
                        .padding(horizontal = browserSettings.value.padding.dp),


                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.CLOSE_TAB,
                        actionIcon = painterResource(R.drawable.ic_close),
                        browserSettings = browserSettings,
                    )


                    // Refresh Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.REFRESH,
                        actionIcon = painterResource(R.drawable.ic_refresh),
                        browserSettings = browserSettings,
                    )

                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.NEW_TAB,
                        actionIcon = painterResource(R.drawable.ic_add),
                        browserSettings = browserSettings,
                    )
                }
                Row(
                    modifier = modifier
                        .fillMaxWidth()

                        .padding(browserSettings.value.padding.dp),
                    horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.BACK,
                        actionIcon = painterResource(R.drawable.ic_arrow_back),
                        visibility = activeWebView?.canGoBack() ?: false,
                        browserSettings = browserSettings,

                        )

                    // Cancel Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.NONE,
                        actionIcon = painterResource(R.drawable.ic_minimize),
                        browserSettings = browserSettings,
                    )

                    // Forward Icon
                    // Back Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
                        activeAction = activeAction,
                        gestureNavAction = GestureNavAction.FORWARD,
                        actionIcon = painterResource(R.drawable.ic_arrow_forward),
                        visibility = activeWebView?.canGoForward() ?: false,
                        browserSettings = browserSettings,

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
    browserSettings: MutableState<BrowserSettings>
) {
    // Cancel Icon
    val refreshColor by animateColorAsState(if (activeAction == gestureNavAction) Color.White else Color.Transparent)
    Box(
        modifier = modifier
            .height(
                browserSettings.value.heightForLayer(3).dp
            )
            .clip(
                RoundedCornerShape(
                    browserSettings.value.cornerRadiusForLayer(3).dp
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