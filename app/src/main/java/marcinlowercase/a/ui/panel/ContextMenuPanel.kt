package marcinlowercase.a.ui.panel

import android.content.ClipData
import android.content.Intent
import android.webkit.WebView
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.ContextMenuData
import marcinlowercase.a.core.enum_class.ContextMenuType
import marcinlowercase.a.core.function.copyImageToClipboard
import kotlin.math.roundToInt

@Composable
fun ContextMenuPanel(
    descriptionContent: MutableState<String>,
    isVisible: Boolean,
    data: ContextMenuData?,
    browserSettings: MutableState<BrowserSettings>,
    onDismiss: () -> Unit,
    onOpenInNewTab: (String) -> Unit,
    onDownload: (String) -> Unit,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current


    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(tween(browserSettings.value.animationSpeed.roundToInt())) + fadeIn(),
        exit = shrinkVertically(tween(browserSettings.value.animationSpeed.roundToInt())) + fadeOut()
    ) {


        if (data == null) return@AnimatedVisibility

        // Determine if the target is an image (IMAGE_TYPE) or a Link (SRC_ANCHOR / SRC_IMAGE_ANCHOR)
        // Note: SRC_IMAGE_ANCHOR usually provides the Link URL in 'extra', not the image source.
        // Pure images return IMAGE_TYPE.
//        val isImage = data.type == WebView.HitTestResult.IMAGE_TYPE

        data.type == ContextMenuType.LINK

        Column(
            modifier = Modifier
                .padding(top = browserSettings.value.padding.dp)
                .fillMaxWidth()
        ) {
            // 1. Header (URL or "Image")

            var actionIcon = when (data.type) {
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

                actions.add(Triple(R.drawable.ic_add, "open link in new tab") {
                    onOpenInNewTab(targetUrl)
                })
                actions.add(Triple(R.drawable.ic_content_copy, "copy link") {
                    val clip = ClipData.newPlainText("Link", targetUrl)
                    clipboard.nativeClipboard.setPrimaryClip(clip)
                    onDismiss()
                })

            } else {
                if (data.linkUrl != null) {
                    // media + link

                    isOnlyOneUrl = false

                    targetUrl = data.linkUrl?: ""
                    actions.add(Triple(R.drawable.ic_add, "open link in new tab") {
                        onOpenInNewTab(targetUrl)
                    })
                    actions.add(Triple(R.drawable.ic_content_copy, "copy link") {
                        val clip = ClipData.newPlainText("Link", targetUrl)
                        clipboard.nativeClipboard.setPrimaryClip(clip)
                        onDismiss()
                    })
                    secondTargetUrl = data.srcUrl?: ""
                    secondActions.add(Triple(R.drawable.ic_add, "open media in new tab") {
                        onOpenInNewTab(secondTargetUrl)
                    })
                    secondActions.add(Triple(R.drawable.ic_content_copy, "copy media link") {
                        val clip = ClipData.newPlainText("Link", secondTargetUrl)
                        clipboard.nativeClipboard.setPrimaryClip(clip)
                        onDismiss()
                    })
                    secondActions.add(Triple(R.drawable.ic_download, "download media file") {
                        onDownload(secondTargetUrl)
                    })
                    secondActions.add(Triple(R.drawable.ic_search, "copy image") {
                        onDismiss()
                        coroutineScope.launch {
                            copyImageToClipboard(context, targetUrl)
                        }
                    })

                } else {
                    // only media
                    targetUrl = data.srcUrl ?: ""
                    actions.add(Triple(R.drawable.ic_add, "open media in new tab") {
                        onOpenInNewTab(targetUrl)
                    })
                    actions.add(Triple(R.drawable.ic_content_copy, "copy media link") {
                        val clip = ClipData.newPlainText("Link", targetUrl)
                        clipboard.nativeClipboard.setPrimaryClip(clip)
                        onDismiss()
                    })
                    actions.add(Triple(R.drawable.ic_download, "download media file") {
                        onDownload(targetUrl)
                    })
                    if (data.type == ContextMenuType.IMAGE) {
                        actions.add(Triple(R.drawable.ic_search, "copy image") {
                            onDismiss()

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
//                            .height(browserSettings.value.heightForLayer(2).dp)

                            ,
                        contentAlignment = Alignment.CenterEnd
                    ){
                        Box(
                            modifier = Modifier
                                .padding(browserSettings.value.padding.dp)
                                .size(30.dp)
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
//                            .height(browserSettings.value.heightForLayer(2).dp)
                           ,
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(browserSettings.value.padding.dp)
                                .size(30.dp)
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
                            .padding(bottom = browserSettings.value.padding.dp)
                            .clip(
                                RoundedCornerShape(
                                    browserSettings.value.cornerRadiusForLayer(2).dp
                                )
                            )
                            .padding(browserSettings.value.padding.dp * 2),
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
                            Spacer(Modifier.width(browserSettings.value.padding.dp))
                        }
                        Text(
                            text = urlSrc,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    browserSettings.value.cornerRadiusForLayer(2).dp
                                )
                            )
                            .padding(horizontal = browserSettings.value.padding.dp)
                            .padding(bottom = browserSettings.value.padding.dp)
                        ,
                        horizontalArrangement = Arrangement.spacedBy(browserSettings.value.padding.dp)
                    ) {
                        buttonSrc.forEach { (icon, desc, action) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(
                                        browserSettings.value.heightForLayer(2).dp
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            browserSettings.value.cornerRadiusForLayer(2).dp
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
                                                descriptionContent.value =
                                                    desc


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


                                            descriptionContent.value = ""
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