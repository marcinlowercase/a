package marcinlowercase.a

import android.app.Application
import android.os.Build
import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.decode.BitmapFactoryDecoder
import coil.decode.SvgDecoder
import marcinlowercase.a.core.manager.GeckoManager
import marcinlowercase.a.core.server.LocalAppServer

class CustomApplication : Application() {
    val geckoManager by lazy { GeckoManager(this) }

    // Server only exists in the main process
    var localAppServer: LocalAppServer? = null
        private set

    override fun onCreate() {
        super.onCreate()

        // 1. ONLY start the loopback server in the main process
        if (isMainProcess()) {
            localAppServer = LocalAppServer(this).apply { start() }
        }

        // Custom imageLoader to load SVG
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
                add(BitmapFactoryDecoder.Factory())
            }
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)
    }

    private fun isMainProcess(): Boolean {
        return getProcessName() == packageName
    }
}