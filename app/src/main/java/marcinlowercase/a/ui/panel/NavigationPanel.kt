/*
 * Copyright (C) 2026 marcinlowercase
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package marcinlowercase.a.ui.panel

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.enum_class.GestureNavAction
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import org.mozilla.geckoview.GeckoSession
import kotlin.system.exitProcess

@Composable
fun NavigationPanel(
    activeSession: GeckoSession,
    modifier: Modifier = Modifier,
) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()
    AnimatedVisibility(
        visible = uiState.value.isNavPanelVisible,
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
activeSession = activeSession,

                        gestureNavAction = GestureNavAction.CLOSE_TAB,
                        actionIcon = painterResource(R.drawable.ic_close),
                    )


                    // Refresh Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
activeSession = activeSession,

                        gestureNavAction = GestureNavAction.REFRESH,
                        actionIcon = painterResource(R.drawable.ic_refresh),
                    )

                    NavigationItem(
                        modifier = Modifier.weight(1f),
activeSession = activeSession,

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
activeSession = activeSession,

                        gestureNavAction = GestureNavAction.BACK,
                        actionIcon = painterResource(R.drawable.ic_arrow_back),
                        visibility = viewModel.activeTab!!.canGoBack,
                    )

                    // Cancel Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
activeSession = activeSession,
                        gestureNavAction = GestureNavAction.NONE,
                        actionIcon = painterResource(R.drawable.ic_minimize),
                    )

                    // Forward Icon
                    NavigationItem(
                        modifier = Modifier.weight(1f),
activeSession = activeSession,

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
    activeSession: GeckoSession,
    gestureNavAction: GestureNavAction,
    actionIcon: Painter,
    visibility: Boolean = true,

    ) {
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()
    val activeTabIndex by viewModel.activeTabIndex.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity

    // Cancel Icon
    val refreshColor by animateColorAsState(if (viewModel.activeNavAction.value == gestureNavAction) Color.White else Color.Transparent)
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
            .clickable{
                when (gestureNavAction) {
                    GestureNavAction.BACK -> if (viewModel.activeTab!!.canGoBack) {
                        activeSession.goBack(true)
                    }

                    GestureNavAction.REFRESH -> {
                        activeSession.reload()
                    }

                    GestureNavAction.FORWARD -> if (viewModel.activeTab!!.canGoForward) {
                        activeSession.goForward(true)
                    }

                    GestureNavAction.CLOSE_TAB -> {
                        if (uiState.value.isLoading) viewModel.updateUI { it.copy(isLoading = false) }
                        viewModel.closeActiveTab {
                            activity.finishAndRemoveTask()
                            exitProcess(0)

                        }
                    }
                    GestureNavAction.NEW_TAB -> {
                        val newIndex = activeTabIndex + 1
                        viewModel.createNewTab(newIndex, "")
                    }
                    GestureNavAction.NONE -> { /* Do nothing */
                    }
                }
                viewModel.updateUI { it.copy(isNavPanelVisible = false) }
            }
    ) {
        if (visibility) {
            Icon(
                actionIcon,
                "Refresh",
                Modifier.align(Alignment.Center),
                tint = if (viewModel.activeNavAction.value == gestureNavAction) Color.Black else Color.White
            )
        }

    }

}