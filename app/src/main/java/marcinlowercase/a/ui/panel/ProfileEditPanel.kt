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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel

@Composable
fun ProfileEditPanel(
    isVisible: Boolean,
    onMoveLeftClick: () -> Unit,
    onImageClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveRightClick: () -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = settings.value.padding.dp)
                .padding(bottom = settings.value.padding.dp)
                .clip(RoundedCornerShape(settings.value.cornerRadiusForLayer(2).dp))
                .padding(settings.value.padding.dp),
            horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
        ) {
            // 1st Button (Move Left)
            CustomIconButton(
                layer = 3,
                modifier = Modifier.weight(1f),
                onTap = onMoveLeftClick,
                buttonDescription = "move profile left",
                painterId = R.drawable.ic_arrow_back,
            )

            // 2nd Button (Upload Photo)
            CustomIconButton(
                layer = 3,
                modifier = Modifier.weight(1f),
                onTap = onImageClick,
                buttonDescription = "upload profile image",
                painterId = R.drawable.ic_add_a_photo,
            )

            // 3rd Button (Delete)
            CustomIconButton(
                layer = 3,
                modifier = Modifier.weight(1f),
                onTap = onDeleteClick,
                buttonDescription = "delete profile",
                painterId = R.drawable.ic_delete_forever, // Or ic_delete_forever
            )

            // 4th Button (Move Right)
            CustomIconButton(
                layer = 3,
                modifier = Modifier.weight(1f),
                onTap = onMoveRightClick,
                buttonDescription = "move profile right",
                painterId = R.drawable.ic_arrow_forward,
            )
        }
    }
}