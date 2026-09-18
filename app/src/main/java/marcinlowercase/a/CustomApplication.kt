package marcinlowercase.a

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.decode.BitmapFactoryDecoder
import coil.decode.SvgDecoder
import marcinlowercase.a.core.manager.GeckoManager

class CustomApplication : Application() {
    val geckoManager by lazy { GeckoManager(this) }

    override fun onCreate() {
        super.onCreate()

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
}