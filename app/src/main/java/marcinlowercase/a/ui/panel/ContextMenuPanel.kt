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

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import marcinlowercase.a.R
import marcinlowercase.a.core.enum_class.ContextMenuType
import marcinlowercase.a.core.function.copyImageToClipboard
import marcinlowercase.a.ui.viewmodel.LocalBrowserViewModel
import androidx.compose.runtime.collectAsState

//import marcinlowercase.a.core.function.shareImage
import kotlin.math.roundToInt

@Composable
fun ContextMenuPanel(
    onDownload: (String) -> Unit,
) {
    val viewModel = LocalBrowserViewModel.current
    val settings = viewModel.browserSettings.collectAsState()
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current


    AnimatedVisibility(
        visible = viewModel.contextMenuData.value != null,
        enter = expandVertically(tween(settings.value.animationSpeed.roundToInt())) + fadeIn(),
        exit = shrinkVertically(tween(settings.value.animationSpeed.roundToInt())) + fadeOut()
    ) {


        val data = viewModel.contextMenuDisplayData.value?: return@AnimatedVisibility

        // Determine if the target is an image (IMAGE_TYPE) or a Link (SRC_ANCHOR / SRC_IMAGE_ANCHOR)
        // Note: SRC_IMAGE_ANCHOR usually provides the Link URL in 'extra', not the image source.
        // Pure images return IMAGE_TYPE.
//        val isImage = data.type == WebView.HitTestResult.IMAGE_TYPE

        data.type == ContextMenuType.LINK

        Column(
            modifier = Modifier
                .padding(top = settings.value.padding.dp)
                .fillMaxWidth()
        ) {
            // 1. Header (URL or "Image")

            val actionIcon = when (data.type) {
                ContextMenuType.LINK -> R.drawable.ic_link
                ContextMenuType.IMAGE, ContextMenuType.IMAGE_LINK -> R.drawable.ic_image
                ContextMenuType.VIDEO -> R.drawable.ic_video_camera_back
                else -> R.drawable.ic_bug
            }
            var targetUrl = ""
            var secondTargetUrl = ""


            val coroutineScope = rememberCoroutineScope()




            // 2. Action Buttons
            val actions = mutableListOf<Triple<Int, String, () -> Unit>>()
            val secondActions = mutableListOf<Triple<Int, String, () -> Unit>>()

            var isOnlyOneUrl = true
            if (data.srcUrl == null) {
                // only link

                targetUrl = data.linkUrl?: ""

                if(!viewModel.isStandaloneMode.value){
                    actions.add(Triple(R.drawable.ic_add, "open link in new tab") {
                        viewModel.createNewTab(viewModel.activeTabIndex.value + 1, targetUrl)
                        viewModel.contextMenuData.value = null
                    })
                }
                actions.add(Triple(R.drawable.ic_content_copy, "copy link") {
                    val clip = ClipData.newPlainText("Link", targetUrl)
                    clipboard.nativeClipboard.setPrimaryClip(clip)
                    viewModel.contextMenuData.value = null
                })

            } else {
                if (data.linkUrl != null) {
                    // media + link

                    isOnlyOneUrl = false

                    targetUrl = data.linkUrl
                    if(!viewModel.isStandaloneMode.value) actions.add(Triple(R.drawable.ic_add, "open link in new tab") {
                        viewModel.createNewTab(viewModel.activeTabIndex.value + 1, targetUrl)
                        viewModel.contextMenuData.value = null
                    })
//                    actions.add(Triple(R.drawable.ic_share, "share link") {
//                        val intent = Intent(Intent.ACTION_SEND).apply {
//                            type = "text/plain"
//                            putExtra(Intent.EXTRA_TEXT, targetUrl)
//                        }
//                        context.startActivity(Intent.createChooser(intent, "Share Link"))
//                        viewModel.contextMenuData.value = null
//                    })
                    actions.add(Triple(R.drawable.ic_content_copy, "copy link") {
                        val clip = ClipData.newPlainText("Link", targetUrl)
                        clipboard.nativeClipboard.setPrimaryClip(clip)
                        viewModel.contextMenuData.value = null
                    })
                    secondTargetUrl = data.srcUrl
                    if(!viewModel.isStandaloneMode.value) secondActions.add(Triple(R.drawable.ic_add, "open media in new tab") {
                        viewModel.createNewTab(viewModel.activeTabIndex.value + 1, secondTargetUrl)
                        viewModel.contextMenuData.value = null
                    })
                    secondActions.add(Triple(R.drawable.ic_content_copy, "copy media link") {
                        val clip = ClipData.newPlainText("Link", secondTargetUrl)
                        clipboard.nativeClipboard.setPrimaryClip(clip)
                        viewModel.contextMenuData.value = null
                    })
                    secondActions.add(Triple(R.drawable.ic_download, "download media file") {
                        onDownload(secondTargetUrl)
                    })
                    secondActions.add(Triple(R.drawable.ic_file_copy, "copy image") {
                        viewModel.contextMenuData.value = null
                        coroutineScope.launch {
                            copyImageToClipboard(context, targetUrl)
                        }
                    })
//                    secondActions.add(Triple(R.drawable.ic_share, "share image") {
////                        val intent = Intent(Intent.ACTION_SEND).apply {
////                            type = "text/plain"
////                            putExtra(Intent.EXTRA_TEXT, secondTargetUrl)
////                        }
////                        context.startActivity(Intent.createChooser(intent, "Share Link"))
//                        viewModel.contextMenuData.value = null
//                        coroutineScope.launch {
//                            shareImage(context, secondTargetUrl)
//                        }
//                    })

                } else {
                    // only media
                    targetUrl = data.srcUrl
                    if(!viewModel.isStandaloneMode.value) actions.add(Triple(R.drawable.ic_add, "open media in new tab") {
                        viewModel.createNewTab(viewModel.activeTabIndex.value + 1, targetUrl)
                        viewModel.contextMenuData.value = null
                    })
                    actions.add(Triple(R.drawable.ic_content_copy, "copy media link") {
                        val clip = ClipData.newPlainText("Link", targetUrl)
                        clipboard.nativeClipboard.setPrimaryClip(clip)
                        viewModel.contextMenuData.value = null
                    })
//                    actions.add(Triple(R.drawable.ic_share, "share link") {
//                        val intent = Intent(Intent.ACTION_SEND).apply {
//                            type = "text/plain"
//                            putExtra(Intent.EXTRA_TEXT, targetUrl)
//                        }
//                        context.startActivity(Intent.createChooser(intent, "Share Link"))
//                        viewModel.contextMenuData.value = null
//                    })
                    actions.add(Triple(R.drawable.ic_download, "download media file") {
                        onDownload(targetUrl)
                    })
                    if (data.type == ContextMenuType.IMAGE) {
                        actions.add(Triple(R.drawable.ic_file_copy, "copy image") {
                            viewModel.contextMenuData.value = null

                            coroutineScope.launch {
                                copyImageToClipboard(context, targetUrl)
                            }
                        })
                    }
                }

            }

            val pageCount = if (secondActions.isNotEmpty()) 2 else 1
            val contextMenuPanelPagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount})

            // LAYOUT
            if (data.srcUrl != null && data.linkUrl != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    Box (
                        modifier = Modifier
                            .weight(1f)
//                            .height(settings.value.heightForLayer(2).dp)

                            ,
                        contentAlignment = Alignment.CenterEnd
                    ){
                        Box(
                            modifier = Modifier
                                .padding(settings.value.padding.dp)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    color = if (contextMenuPanelPagerState.currentPage == 0) Color.White else Color.Black,
                                    shape = CircleShape
                                )
                                .clickable(onClick = {
                                    coroutineScope.launch {
                                        contextMenuPanelPagerState.animateScrollToPage(0)
                                    }
                                })
                            ,
                            contentAlignment = Alignment.Center

                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_link),
                                tint = if (contextMenuPanelPagerState.currentPage == 1) Color.White else Color.Black,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
//                            .height(settings.value.heightForLayer(2).dp)
                           ,
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(settings.value.padding.dp)
                                .size(30.dp)
                                .clip(CircleShape)

                                .background(
                                    color = if (contextMenuPanelPagerState.currentPage == 1) Color.White else Color.Black,
                                    shape = CircleShape
                                )
                                .clickable(onClick = {
                                    coroutineScope.launch {
                                        contextMenuPanelPagerState.animateScrollToPage(1)
                                    }
                                })
                            ,
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(actionIcon),
                                tint = if (contextMenuPanelPagerState.currentPage == 0) Color.White else Color.Black,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)


                            )
                        }
                    }
                }
            }
            HorizontalPager(state = contextMenuPanelPagerState,
                modifier = Modifier
            ) { pageIndex ->
            // URL
                val urlSrc =if (pageIndex == 0) targetUrl else secondTargetUrl
                val buttonSrc = if (pageIndex == 0) actions else secondActions
                Column (
                    modifier = Modifier.fillMaxWidth()
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = settings.value.padding.dp)
                            .padding(horizontal = settings.value.cornerRadiusForLayer(1).dp)
                            .clip(
                                RoundedCornerShape(
                                    settings.value.cornerRadiusForLayer(2).dp
                                )
                            )
//                            .clickable(onClick = {
//                                val clip = ClipData.newPlainText("Link", urlSrc)
//                                clipboard.nativeClipboard.setPrimaryClip(clip)
//                                viewModel.contextMenuData.value = null
//                            })
                        ,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if(isOnlyOneUrl){
                            Icon(
                                painter = painterResource(actionIcon),
                                //                    painter = painterResource(if(isImage) R.drawable.ic_image else R.drawable.ic_link),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(settings.value.padding.dp))
                        }
                        Text(
                            text = urlSrc,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState())
                        )
                    }


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    settings.value.cornerRadiusForLayer(2).dp
                                )
                            )
                            .padding(horizontal = settings.value.padding.dp)
                            .padding(bottom = settings.value.padding.dp)
                        ,
                        horizontalArrangement = Arrangement.spacedBy(settings.value.padding.dp)
                    ) {
                        buttonSrc.forEach { (icon, desc, action) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(
                                        settings.value.heightForLayer(2).dp
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            settings.value.cornerRadiusForLayer(2).dp
                                        )
                                    )
                                    .background(Color.White)
                                    //                            .clickable(onClick = action)
                                    .pointerInput(Unit) {
                                        // 1. CAPTURE the CoroutineScope provided by pointerInput
                                        val coroutineScope = CoroutineScope(
                                            currentCoroutineContext()
                                        )
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)

                                            // 2. USE the captured scope to launch the long press job
                                            val longPressJob = coroutineScope.launch {
                                                delay(viewConfiguration.longPressTimeoutMillis)

                                                // LONG PRESS CONFIRMED
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
                                                viewModel.descriptionContent.value = desc


                                            }
                                            val drag =
                                                awaitTouchSlopOrCancellation(down.id) { change, _ ->
                                                    if (longPressJob.isActive) {
                                                        longPressJob.cancel()
                                                    }
                                                    change.consume()
                                                }



                                            if (!(longPressJob.isCompleted && !longPressJob.isCancelled)) {
                                                if (drag == null) {
                                                    if (longPressJob.isActive) {
                                                        longPressJob.cancel()
                                                        // This was a tap
                                                        action()

                                                    }
                                                }
                                            }


                                            viewModel.descriptionContent.value = ""

                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(icon),
                                    contentDescription = desc,
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}