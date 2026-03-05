package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.function.buttonSettingsForLayer
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel

@Composable
fun AppsEditPanel () {

    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    val uiState = viewModel.uiState.collectAsState()
    AnimatedVisibility(
        visible = viewModel.inspectingAppId.longValue > 0L && viewModel.apps.isNotEmpty(),
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = settings.value.padding.dp)
                .padding(horizontal = settings.value.padding.dp)
                .clip(
                    RoundedCornerShape(
                        settings.value.cornerRadiusForLayer(
                            2
                        ).dp
                    )
                )
        ) {

            val currentIndex =
                viewModel.apps.indexOfFirst { it.id == viewModel.inspectingAppId.longValue }
            Box(
                modifier = Modifier
                    .buttonSettingsForLayer(
                        2,
                        settings.value,
                        currentIndex < viewModel.apps.lastIndex && currentIndex >= 0
                    )
                    .weight(1f)
                    .clickable {

                        if (currentIndex < viewModel.apps.lastIndex) {
                            viewModel.swapApps(
                                currentIndex,
                                currentIndex + 1
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_downward),
                    contentDescription = "edit pin",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(settings.value.padding.dp))

            Box(
                modifier = Modifier
                    .buttonSettingsForLayer(
                        2,
                        settings.value,
                        false
                    )
                    .weight(1f)
                    .combinedClickable(
                        onClick = {
                            // Short click: Delete the pinned app
                            viewModel.removeApp(viewModel.inspectingAppId.longValue)
                            viewModel.inspectingAppId.longValue = 0L
                        },
                        onLongClick = {
                            // Long press: Delete the active profile (Space)
                            viewModel.deleteProfile(viewModel.activeProfileId.value)

                            // Reset the inspection state just in case
                            viewModel.inspectingAppId.longValue = 0L
                        }
                    )
                    .background(Color(settings.value.highlightColor)),

                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete_forever),
                    contentDescription = "delete pin",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(settings.value.padding.dp))
            Box(
                modifier = Modifier
                    .buttonSettingsForLayer(
                        2,
                        settings.value,
                        currentIndex > 0
                    )
                    .weight(1f)
                    .clickable {
                        if (currentIndex > 0) {
                            viewModel.swapApps(
                                currentIndex,
                                currentIndex - 1
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_upward),
                    contentDescription = "edit pin",
                    tint = Color.Black
                )
            }

        }
    }

}