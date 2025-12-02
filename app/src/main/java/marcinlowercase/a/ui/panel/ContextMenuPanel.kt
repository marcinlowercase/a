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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.data_class.ContextMenuData
import kotlin.math.roundToInt

@Composable
fun ContextMenuPanel(
    isVisible: Boolean,
    data: ContextMenuData?,
    browserSettings: BrowserSettings,
    onDismiss: () -> Unit,
    onOpenInNewTab: (String) -> Unit,
    onDownloadImage: (String) -> Unit
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(tween(browserSettings.animationSpeed.roundToInt())) + fadeIn(),
        exit = shrinkVertically(tween(browserSettings.animationSpeed.roundToInt())) + fadeOut()
    ) {
        if (data == null) return@AnimatedVisibility

        // Determine if the target is an image (IMAGE_TYPE) or a Link (SRC_ANCHOR / SRC_IMAGE_ANCHOR)
        // Note: SRC_IMAGE_ANCHOR usually provides the Link URL in 'extra', not the image source.
        // Pure images return IMAGE_TYPE.
        val isImage = data.type == WebView.HitTestResult.IMAGE_TYPE

        Column(
            modifier = Modifier
                .padding(horizontal = browserSettings.padding.dp)
                .padding(top = browserSettings.padding.dp)
                .fillMaxWidth()
        ) {
            // 1. Header (URL or "Image")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = browserSettings.padding.dp)
                    .clip(
                        RoundedCornerShape(
                            browserSettings.cornerRadiusForLayer(2).dp
                        )
                    )
                    .padding(browserSettings.padding.dp * 2)

                ,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(if(isImage) R.drawable.ic_image else R.drawable.ic_link),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(browserSettings.padding.dp))
                Text(
                    text = data.url,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // 2. Action Buttons
            val actions = mutableListOf<Triple<Int, String, () -> Unit>>()

            if (isImage) {
                // --- IMAGE ACTIONS ---
                actions.add(Triple(R.drawable.ic_download, "Download Image") {
                    onDownloadImage(data.url)
                    onDismiss()
                })
                actions.add(Triple(R.drawable.ic_add, "Open Image in New Tab") {
                    onOpenInNewTab(data.url)
                })
            } else {
                // --- LINK ACTIONS ---
                actions.add(Triple(R.drawable.ic_add, "Open in New Tab") {
                    onOpenInNewTab(data.url)
                })
                actions.add(Triple(R.drawable.ic_content_copy, "Copy Link") {
                    val clip = ClipData.newPlainText("Link", data.url)
                    clipboard.nativeClipboard.setPrimaryClip(clip)
                    onDismiss()
                })
                actions.add(Triple(R.drawable.ic_share, "Share Link") {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, data.url)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Link"))
                    onDismiss()
                })
            }

            // Common Cancel Action
            actions.add(Triple(R.drawable.ic_close, "Cancel") { onDismiss() })

            // Render Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            browserSettings.cornerRadiusForLayer(2).dp
                        )
                    )
                    .padding(bottom = browserSettings.padding.dp),
                horizontalArrangement = Arrangement.spacedBy(browserSettings.padding.dp)
            ) {
                actions.forEach { (icon, desc, action) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(
                                browserSettings.heightForLayer(2).dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    browserSettings.cornerRadiusForLayer(2).dp
                                )
                            )
                            .background(Color.White)
                            .clickable(onClick = action),
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