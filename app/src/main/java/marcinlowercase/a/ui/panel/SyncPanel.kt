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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel

@Composable
fun SyncPanel(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onPush: () -> Unit,
    onPull: () -> Unit,
    onMerge: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()

    // Calculate Half-Padding to achieve pixel-perfect alignment
    val padding = settings.value.padding.dp
    val halfPadding = padding / 2

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(tween(settings.value.animationSpeedForLayer(1))),
        exit = shrinkVertically(tween(settings.value.animationSpeedForLayer(1)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(padding)
        ) {
            // ROW 1: Header / Status & Logout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(settings.value.heightForLayer(3).dp),
                // REMOVED Arrangement.spacedBy here, relying on halfPadding below
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(3f)
                        .padding(end = halfPadding) // Align inner box boundary
                        .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp))
                        .border(
                            width = 1.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp)
                        ),
                    horizontalArrangement = Arrangement.spacedBy(padding), // Inside spacing is fine
                ) {
                    // Status Text
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .height(settings.value.heightForLayer(3).dp)
                            .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(3).dp))
                            .padding(horizontal = padding)
                            .padding(horizontal = settings.value.cornerRadiusForLayer(3).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.getLoggedInEmail(),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        )
                    }
                    // Delete Button
                    CustomIconButton(
                        layer = 3,
                        modifier = Modifier.weight(1f),
                        onTap = {
                            onDeleteAccount()
                            onDismiss()
                        },
                        buttonDescription = stringResource(R.string.desc_delete_account),
                        painterId = R.drawable.ic_account_circle_off,
                        isWhite = !isColorDark(Color.Red.toArgb()),
                        otherColor = Color.Red
                    )
                }

                // Logout Button
                CustomIconButton(
                    layer = 3,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = halfPadding), // Align to right boundary
                    onTap = {
                        onLogout()
                        onDismiss()
                    },
                    buttonDescription = stringResource(R.string.desc_logout),
                    painterId = R.drawable.ic_logout,
                )
            }

            // ROW 2: Actions (How to Sync)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(settings.value.heightForLayer(3).dp),
                // REMOVED Arrangement.spacedBy here, relying on halfPadding below
            ) {
                // PUSH (Backup to Cloud)
                CustomIconButton(
                    layer = 3,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = halfPadding), // Align to left boundary
                    onTap = {
                        onPush()
                        onDismiss()
                    },
                    buttonDescription = stringResource(R.string.desc_sync_push),
                    painterId = R.drawable.ic_arrow_upward
                )

                // MERGE (Smart Merge)
                CustomIconButton(
                    layer = 3,
                    modifier = Modifier
                        .weight(2f)
                        .padding(start = halfPadding, end = halfPadding), // Keep centered
                    onTap = {
                        onMerge()
                        onDismiss()
                    },
                    buttonDescription = stringResource(R.string.desc_sync_merge),
                    painterId = R.drawable.ic_sync,
                    otherColor = Color(settings.value.highlightColor),
                    isWhite = !isColorDark(settings.value.highlightColor)
                )

                // PULL (Restore from Cloud)
                CustomIconButton(
                    layer = 3,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = halfPadding), // Align to right boundary
                    onTap = {
                        onPull()
                        onDismiss()
                    },
                    buttonDescription = stringResource(R.string.desc_sync_pull),
                    painterId = R.drawable.ic_arrow_downward
                )
            }
        }
    }
}