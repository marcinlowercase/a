package marcinlowercase.a.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel

@Composable
fun TextEditPanel(
    isVisible: Boolean,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
    onDismiss: () -> Unit,
    activeWebViewTitle: String,
    onAddToHomeScreen: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = LocalBrowserViewModel.current
    val uiState = viewModel.uiState.collectAsState()
    val settings = viewModel.browserSettings.collectAsState()

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            animationSpec = tween(settings.value.animationSpeedForLayer(2))
        ) + fadeIn(tween(settings.value.animationSpeedForLayer(2))),
        exit = shrinkVertically(
            animationSpec = tween(settings.value.animationSpeedForLayer(2))
        ) + fadeOut(tween(settings.value.animationSpeedForLayer(2)))
    ) {
        // Use a fixed 3-column grid so buttons always map to exact slots.
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = settings.value.padding.dp)
                .padding(bottom = settings.value.padding.dp)
                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
                .height(settings.value.heightForLayer(3).dp + (settings.value.padding.dp * 2)) // Lock height to 1 row + padding
                .padding(settings.value.padding.dp),
            horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp),
            userScrollEnabled = false
        ) {
            // --- SLOT 1: ALWAYS THE BACK / DISMISS BUTTON ---
            item(key = "dismiss_btn") {
                CustomIconButton(
                    layer = 3,
                    modifier = Modifier.fillMaxSize().animateItem(),
                    onTap = onDismiss,
                    buttonDescription = stringResource(R.string.desc_cancel),
                    painterId = R.drawable.ic_arrow_back,
                    isWhite = false,
                )
            }

            // --- SLOT 2 & 3 Logic ---
            when {




                uiState.value.isPinningApp || uiState.value.isCreatingProfile || uiState.value.isRenamingProfile  -> {
//                    // Slot 2: Add to Home Screen (or Empty if not pinning)
                    //TODO
//                    if (uiState.value.isPinningApp) {
//                        item(key = "add_to_home_btn") {
//                            CustomIconButton(
//                                layer = 3,
//                                modifier = Modifier.fillMaxSize().animateItem(),
//                                onTap = onAddToHomeScreen,
//                                buttonDescription = stringResource(R.string.desc_install_web_app),
//                                painterId = R.drawable.ic_browser_updated,
//                            )
//                        }
//                    } else {
//                        item(key = "empty_pin_2") { Spacer(modifier = Modifier.fillMaxSize().animateItem()) }
//                    }
//                    Slot 2 always empty, cause we temporary disable the add to homescreen
                    item(key = "empty_pin_2") { Spacer(modifier = Modifier.fillMaxSize().animateItem()) }

                    // Slot 3: Edit Button
                    if (activeWebViewTitle.isNotBlank()) {
                        item(key = "edit_app_name_btn") {
                            CustomIconButton(
                                layer = 3,
                                modifier = Modifier.fillMaxSize().animateItem(),
                                onTap = onEditClick,
                                buttonDescription = stringResource(R.string.desc_edit_app_name),
                                painterId = R.drawable.ic_edit,
                            )
                        }
                    } else {
                        item(key = "empty_pin_3") { Spacer(modifier = Modifier.fillMaxSize().animateItem()) }
                    }
                }

                else -> {
                    // Slot 2: Copy URL
                    item(key = "copy_url_btn") {
                        CustomIconButton(
                            layer = 3,
                            modifier = Modifier.fillMaxSize().animateItem(),
                            onTap = onCopyClick,
                            buttonDescription = stringResource(R.string.desc_copy_current_url),
                            painterId = R.drawable.ic_content_copy,
                        )
                    }

                    // Slot 3: Edit URL
                    item(key = "edit_url_btn") {
                        CustomIconButton(
                            layer = 3,
                            modifier = Modifier.fillMaxSize().animateItem(),
                            onTap = onEditClick,
                            buttonDescription = stringResource(R.string.desc_edit_current_url),
                            painterId = R.drawable.ic_edit,
                        )
                    }
                }
            }
        }
    }
}