package marcinlowercase.a.core.function

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import marcinlowercase.a.MainActivity
import marcinlowercase.a.core.constant.default_url
import marcinlowercase.a.core.custom_class.CustomWebView
import marcinlowercase.a.core.data_class.Tab

fun addToHomeScreen(
    context: Context,
    coroutineScope: CoroutineScope,
    tab: Tab?,
    activeWebView: CustomWebView?,
) {
    if (tab == null) return

    // 1. Check for API level support


    val shortcutManager = context.getSystemService<ShortcutManager>()
    if (shortcutManager == null || !shortcutManager.isRequestPinShortcutSupported) {
        Toast.makeText(context, "Launcher does not support pinning", Toast.LENGTH_SHORT).show()
        return
    }

    val url = activeWebView?.url ?: default_url
    val title = activeWebView?.title ?: "Shortcut"
    val faviconUrl = getFaviconUrlFromGoogleServer(url)


    // 2. Fetch the icon using Coil
    coroutineScope.launch {
        val imageRequest = ImageRequest.Builder(context)
            .data(faviconUrl)
            .transformations(CircleCropTransformation())
            .build()

        val iconBitmap = context.imageLoader.execute(imageRequest).drawable?.toBitmap()

        if (iconBitmap == null) {
            Toast.makeText(context, "Could not load icon", Toast.LENGTH_SHORT).show()
            return@launch
        }

        // 3. Create the Intent
        val shortcutIntent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // 4. Build the ShortcutInfo
        val shortcut = ShortcutInfo.Builder(context, url)
            .setShortLabel(title)
            .setLongLabel(title)
            .setIcon(android.graphics.drawable.Icon.createWithBitmap(iconBitmap))
            .setIntent(shortcutIntent)
            .build()

        // 5. Request pinning
        shortcutManager.requestPinShortcut(shortcut, null)
//        Toast.makeText(context, "Adding shortcut to home screen...", Toast.LENGTH_SHORT).show()
    }
}