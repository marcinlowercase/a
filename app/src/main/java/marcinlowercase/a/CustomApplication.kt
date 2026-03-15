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