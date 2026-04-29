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

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.DownloadItem
import marcinlowercase.a.core.enum_class.DownloadStatus
import marcinlowercase.a.core.function.formatSpeed
import marcinlowercase.a.core.function.formatTimeRemaining
import marcinlowercase.a.ui.component.CustomIconButton
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel

@Composable
fun DownloadPanel(
//    currentRotation: Float,
    confirmationPopup: (message: Int, url: String, onConfirm: () -> Unit, onCancel: () -> Unit) -> Unit,
    isDownloadPanelVisible: Boolean,
    onDownloadRowClicked: (DownloadItem) -> Unit,
    onOpenFolderClicked: () -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()

    // Cache the string resource for the popup lambda

    AnimatedVisibility(
        visible = isDownloadPanelVisible,
        enter = expandVertically(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ) + fadeIn(
            tween(settings.value.animationSpeedForLayer(1))
        ),
        exit = shrinkVertically(
            tween(
                settings.value.animationSpeedForLayer(1)
            )
        ) + fadeOut(
            tween(settings.value.animationSpeedForLayer(1))
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = settings.value.padding.dp)
                .padding(top = settings.value.padding.dp)

                .fillMaxWidth()


                .heightIn(
                    max = 300.dp
                ) // Set a max height to prevent it from getting too tall
                .clip(
                    RoundedCornerShape(
                        settings.value.cornerRadiusForLayer(2).dp
                    )
                )
        ) {
            if (!viewModel.downloads.isEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = settings.value.maxContainerSizeForLayer(3).dp)

                        .padding(top = settings.value.padding.dp)
                        .padding(horizontal = settings.value.padding.dp)

                        .clip(
                            RoundedCornerShape(
                                settings.value.cornerRadiusForLayer(3).dp
                            )
                        ),
                    reverseLayout = true,
                ) {
                    items(viewModel.downloads.size, key = { viewModel.downloads[it].id }) { index ->
                        DownloadRow(
                            index = index,
                            item = viewModel.downloads[index],
                            onClick = { onDownloadRowClicked(viewModel.downloads[index]) },
                            onDeleteClicked = {
                                viewModel.deleteDownload(viewModel.downloads[index])
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(settings.value.padding.dp)
                    .clip(
                        RoundedCornerShape(
                            settings.value.cornerRadiusForLayer(3).dp
                        )
                    )
                    .height(
                        settings.value.heightForLayer(3).dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
            ) {
                //  Show Download Folder Button
                CustomIconButton(
                    layer = 3,
                    modifier = Modifier.weight(1f),
                    onTap = onOpenFolderClicked,
                    buttonDescription = stringResource(R.string.desc_download_folder),
                    painterId = R.drawable.ic_folder,
//                    currentRotation = currentRotation,
                )
                if (viewModel.downloads.isNotEmpty())
                    CustomIconButton(
//                        currentRotation = currentRotation,
                        layer = 3,
                        modifier = Modifier.weight(1f),
                        onTap = {
                            confirmationPopup(
                                R.string.prompt_clear_download_list,
                                "",
                                {
                                    viewModel.clearDownloadList()
                                },
                                {}
                            )
                        },
                        buttonDescription = stringResource(R.string.desc_clear_download_list),
                        painterId = R.drawable.ic_clear_all,

                        )
            }

        }
    }
}


@SuppressLint("DefaultLocale")
@Composable
fun DownloadRow(
    index: Int,
    item: DownloadItem,
    onClick: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // 1. The root is now a Box to allow layering.
    // The clip and overall modifier are applied here.
    Box(
        modifier = Modifier
            .padding(bottom = if (index != 0) settings.value.padding.dp else 0.dp)

            .fillMaxWidth()

            .heightIn(min = settings.value.heightForLayer(3).dp)


            .clip(
                RoundedCornerShape(
                    settings.value.cornerRadiusForLayer(3).dp
                )
            )
            .clickable(enabled = item.status == DownloadStatus.SUCCESSFUL) {
                onClick()
            }
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))

            .pointerInput(item.status) { // Re-read when status changes
                detectTapGestures(
                    onTap = {
                        if (item.status == DownloadStatus.SUCCESSFUL) {
                            onClick()
                        }
                    },
                    onLongPress = {
                        showDeleteConfirm = true // Show the delete confirmation
                    }
                )
            }


    ) {

        // --- LAYER 2: The Content Foreground ---

        // Your original content column. It now sits on top of the progress indicator.
        // It has a transparent background.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(settings.value.padding.dp)

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .heightIn(min = settings.value.heightForLayer(4).dp)
                    .padding(horizontal = settings.value.padding.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.status == DownloadStatus.RUNNING) {
                        CircularProgressIndicator(
                            progress = { (item.progress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.matchParentSize(),
                            color = Color(settings.value.highlightColor),
                            strokeWidth = 2.dp,
                            trackColor = Color(settings.value.highlightColor).copy(alpha = 0.3f)
                        )
                    }

                    Icon(
                        painter = painterResource(
                            id =
                                when (item.status) {
                                    DownloadStatus.RUNNING -> R.drawable.ic_downloading
                                    DownloadStatus.SUCCESSFUL -> R.drawable.ic_download_done
                                    DownloadStatus.CANCELLED -> R.drawable.ic_file_download_off
                                    else -> R.drawable.ic_download
                                }
                        ),
                        contentDescription = stringResource(R.string.desc_download_icon),
                        // Change the tint based on status for better visual feedback
                        tint = if (item.status == DownloadStatus.RUNNING) Color(settings.value.highlightColor) else MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp)

                    )


                }
                Spacer(Modifier.width(settings.value.padding.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.filename,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                    )

                    // A more descriptive status text
                    val statusText = when (item.status) {
                        DownloadStatus.RUNNING -> {
                            val downloadedMb = String.format("%.1f", item.downloadedBytes / 1024f / 1024f)
                            val totalMb = String.format("%.1f", item.totalBytes / 1024f / 1024f)

                            // Show actual live progress if total is unknown
                            val sizeInfo = if (item.totalBytes > 0) {
                                stringResource(R.string.ui_download_progress_known, downloadedMb, totalMb)
                            } else if (item.downloadedBytes > 0) {
                                stringResource(R.string.ui_download_progress_unknown, downloadedMb)
                            } else {
                                stringResource(R.string.ui_download_starting)
                            }

                            val speedInfo = if (item.downloadSpeedBps > 0) formatSpeed(item.downloadSpeedBps) else ""
                            val timeInfo = if (item.timeRemainingMs > 0) formatTimeRemaining(item.timeRemainingMs) else ""

                            listOf(sizeInfo, speedInfo, timeInfo).filter { it.isNotBlank() }.joinToString(" - ")
                        }
                        else -> ""
                    }
                    if (statusText.isNotBlank()) {
                        Text(
                            statusText,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }

                }
            }


        }

        // --- LAYER 3: The Delete Confirmation Overlay ---
        AnimatedVisibility(
            modifier = Modifier.matchParentSize(),
            visible = showDeleteConfirm,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .matchParentSize()
                    .heightIn(min = settings.value.heightForLayer(4).dp)
                    .background(Color.Red.copy(alpha = 0.8f))
                    // 3. This pointerInput is ONLY on the delete overlay.
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onDeleteClicked()
                                showDeleteConfirm = false // Hide after deleting
                            },
                            onLongPress = {
                                showDeleteConfirm = false // Long press to cancel/hide
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete_forever),
                    contentDescription = stringResource(R.string.desc_confirm_delete),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}