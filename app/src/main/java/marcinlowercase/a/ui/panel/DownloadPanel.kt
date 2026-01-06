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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.DownloadItem
import marcinlowercase.a.core.enum_class.DownloadStatus
import marcinlowercase.a.core.function.formatSpeed
import marcinlowercase.a.core.function.formatTimeRemaining
import marcinlowercase.a.ui.component.CustomIconButton

@Composable
fun DownloadPanel(
    confirmationPopup: (message: String, onConfirm: () -> Unit, onCancel: () -> Unit) -> Unit,
    isDownloadPanelVisible: MutableState<Boolean>,
    downloads: List<DownloadItem>,
    browserSettings: MutableState<BrowserSettings>,
    onDownloadRowClicked: (DownloadItem) -> Unit,
    onDeleteClicked: (DownloadItem) -> Unit,
    onOpenFolderClicked: () -> Unit,
    onClearAllClicked: () -> Unit,
    descriptionContent: MutableState<String>
) {
    AnimatedVisibility(
        visible = isDownloadPanelVisible.value,
        enter = expandVertically(
            tween(
                browserSettings.value.animationSpeedForLayer(1)
            )
        ) + fadeIn(
            tween(browserSettings.value.animationSpeedForLayer(1))
        ),
        exit = shrinkVertically(
            tween(
                browserSettings.value.animationSpeedForLayer(1)
            )
        ) + fadeOut(
            tween(browserSettings.value.animationSpeedForLayer(1))
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = browserSettings.value.padding.dp)
                .padding(top = browserSettings.value.padding.dp)

                .fillMaxWidth()


                .heightIn(
                    max = 300.dp
                ) // Set a max height to prevent it from getting too tall
                .clip(
                    RoundedCornerShape(
                        browserSettings.value.cornerRadiusForLayer(2).dp
                    )
                )
        ) {
            if (downloads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = browserSettings.value.padding.dp)
                        .padding(top = browserSettings.value.padding.dp)
                        .background(Color.Transparent)
                        .clip(
                            RoundedCornerShape(
                                browserSettings.value.cornerRadiusForLayer(3).dp
                            )
                        )
                        .height(
                            browserSettings.value.heightForLayer(3).dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("no downloads .", color = Color.White)
                }


            } else {
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = browserSettings.value.maxContainerSizeForLayer(3).dp)

                        .padding(top = browserSettings.value.padding.dp)
                        .padding(horizontal = browserSettings.value.padding.dp)

                        .clip(
                            RoundedCornerShape(
                                browserSettings.value.cornerRadiusForLayer(3).dp
                            )
                        ),
                    reverseLayout = true,
                ) {
                    items(downloads.size, key = { downloads[it].id }) { index ->
                        DownloadRow(
                            index = index,
                            item = downloads[index],
                            browserSettings = browserSettings,
                            onClick = { onDownloadRowClicked(downloads[index]) },
                            onDeleteClicked = { onDeleteClicked(downloads[index]) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(browserSettings.value.padding.dp)
                    .clip(
                        RoundedCornerShape(
                            browserSettings.value.cornerRadiusForLayer(3).dp
                        )
                    )
                    .height(
                        browserSettings.value.heightForLayer(3).dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
            ) {
                //  Show Download Folder Button
                CustomIconButton(
                    layer = 3,
                    browserSettings = browserSettings,
                    modifier = Modifier.weight(1f),
                    onTap = onOpenFolderClicked,
                    descriptionContent = descriptionContent,
                    buttonDescription = "download folder",
                    painterId = R.drawable.ic_folder,
                )
                if (downloads.isNotEmpty())
                    CustomIconButton(
                        layer = 3,
                        browserSettings = browserSettings,
                        modifier = Modifier.weight(1f),
                        onTap = {
                            confirmationPopup(
                                "clear download list ?",
                                {
                                    onClearAllClicked()

                                },
                                {}
                            )
                        },
                        descriptionContent = descriptionContent,
                        buttonDescription = "clear download list",
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
    browserSettings: MutableState<BrowserSettings>,
    onClick: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // 1. The root is now a Box to allow layering.
    // The clip and overall modifier are applied here.
    Box(
        modifier = Modifier
            .padding(bottom = if (index != 0) browserSettings.value.padding.dp else 0.dp)

            .fillMaxWidth()

            .height(
                browserSettings.value.heightForLayer(3).dp
            )

            .clip(
                RoundedCornerShape(
                    browserSettings.value.cornerRadiusForLayer(3).dp
                )
            )
            .clickable(enabled = item.status == DownloadStatus.SUCCESSFUL) {
                onClick()
            }
            .background(Color.Black.copy(alpha = 0.5f))
//            .border(
//                width = 1.dp,
//                color = Color.White,
//                shape = RoundedCornerShape(
//                    cornerRadiusForLayer(
//                        3,
//                        browserSettings.value.deviceCornerRadius,
//                        browserSettings.value.padding
//                    ).dp
//                )
//            )

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
        // --- LAYER 1: The Progress Background ---


        AnimatedVisibility(
            visible = item.status == DownloadStatus.RUNNING,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    // This is the key: fillMaxWidth takes a fraction from 0.0 to 1.0.
                    // We calculate this from the item's progress (0-100).
                    .fillMaxWidth(fraction = item.progress / 100f)
                    .background(Color.White.copy(alpha = 0.3f))
                // A semi-transparent color for the progress
            )
        }

        // --- LAYER 2: The Content Foreground ---

        // Your original content column. It now sits on top of the progress indicator.
        // It has a transparent background.
        Column(
            modifier = Modifier
                .fillMaxWidth()

                .padding(browserSettings.value.padding.dp)

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(
                        browserSettings.value.heightForLayer(4).dp
                    )
                    .padding(horizontal = browserSettings.value.padding.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
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
                        contentDescription = "Download Icon",
                        // Change the tint based on status for better visual feedback
                        tint = Color.Black,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp)

                    )
                }
                Spacer(Modifier.width(browserSettings.value.padding.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.filename,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // A more descriptive status text
                    val statusText = when (item.status) {
                        DownloadStatus.RUNNING -> {
                            val downloadedMb =
                                String.format("%.1f", item.downloadedBytes / 1024f / 1024f)
                            val totalMb = String.format("%.1f", item.totalBytes / 1024f / 1024f)
                            val sizeInfo =
                                if (item.totalBytes > 0) "$downloadedMb MB / $totalMb MB" else "Running..."

                            val speedInfo =
                                if (item.downloadSpeedBps > 0) formatSpeed(item.downloadSpeedBps) else ""
                            val timeInfo =
                                if (item.timeRemainingMs > 0) formatTimeRemaining(item.timeRemainingMs) else ""

                            // Combine all parts, filtering out empty ones
                            listOf(sizeInfo, speedInfo, timeInfo).filter { it.isNotBlank() }
                                .joinToString(" - ")
                        }

                        else -> ""
                    }
                    if (statusText.isNotBlank()) {
                        Text(
                            statusText,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }

                }
            }


        }

        // --- LAYER 3: The Delete Confirmation Overlay ---
        AnimatedVisibility(
            visible = showDeleteConfirm,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                    contentDescription = "Confirm Delete",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}