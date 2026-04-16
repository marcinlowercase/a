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

import android.content.Intent
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
import androidx.core.net.toUri

@Composable
fun TextEditPanel(
    isVisible: Boolean,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
    onDismiss: () -> Unit,
    activeWebViewTitle: String,
    onAddToHomeScreen: () -> Unit,
    onResendCodeClick: () -> Unit
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
                uiState.value.isEnteringEmail -> {
                    // Empty Slot 2 & 3 for Enter Email
                    item(key = "empty_2") { Spacer(modifier = Modifier.fillMaxSize().animateItem()) }
                    item(key = "empty_3") { Spacer(modifier = Modifier.fillMaxSize().animateItem()) }
                }

                uiState.value.isEnteringLoginCode -> {
                    // Slot 2: Open Email App
                    item(key = "open_email_btn") {
                        CustomIconButton(
                            layer = 3,
                            modifier = Modifier.fillMaxSize().animateItem(),
                            onTap = {
                                val intent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_APP_EMAIL)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback if no specific email app is found
                                    val fallbackIntent = Intent(Intent.ACTION_VIEW,
                                        "mailto:".toUri())
                                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(fallbackIntent)
                                }
                            },
                            buttonDescription = stringResource(R.string.desc_open_email_app),
                            painterId = R.drawable.ic_mail,
                        )
                    }

                    // Slot 3: Resend Code
                    item(key = "resend_code_btn") {
                        CustomIconButton(
                            layer = 3,
                            modifier = Modifier.fillMaxSize().animateItem(),
                            onTap = onResendCodeClick,
                            buttonDescription = stringResource(R.string.desc_resend_email),
                            painterId = R.drawable.ic_email_resend,
                        )
                    }
                }

                uiState.value.isPinningApp || uiState.value.isCreatingProfile || uiState.value.isRenamingProfile || uiState.value.isCloningBrowser -> {
                    // Slot 2: Add to Home Screen (or Empty if not pinning)
                    if (uiState.value.isPinningApp) {
                        item(key = "add_to_home_btn") {
                            CustomIconButton(
                                layer = 3,
                                modifier = Modifier.fillMaxSize().animateItem(),
                                onTap = onAddToHomeScreen,
                                buttonDescription = stringResource(R.string.desc_install_web_app),
                                painterId = R.drawable.ic_browser_updated,
                            )
                        }
                    } else {
                        item(key = "empty_pin_2") { Spacer(modifier = Modifier.fillMaxSize().animateItem()) }
                    }

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