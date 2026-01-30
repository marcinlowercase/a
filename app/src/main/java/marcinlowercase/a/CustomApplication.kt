package marcinlowercase.a

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.decode.BitmapFactoryDecoder
import coil.decode.SvgDecoder
import marcinlowercase.a.core.manager.GeckoManager

// REMOVE the "ImageLoaderFactory" implementation
class CustomApplication : Application() {
    val geckoManager by lazy { GeckoManager(this) }


    // The onCreate() method is the first safe place to access context.
    override fun onCreate() {
        super.onCreate() // Always call the superclass method first





        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
                add(BitmapFactoryDecoder.Factory())
            }
            .crossfade(true)
            .build()

        // Set this as the singleton ImageLoader for the entire app.
        Coil.setImageLoader(imageLoader)
    }
}