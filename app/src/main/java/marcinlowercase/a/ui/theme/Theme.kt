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
package marcinlowercase.a.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LightGray,       // Color for focused TextField border, progress bar
    background = Black,    // App background
    surface = Black,       // Surface color for components like TextField
    onPrimary = Black,     // Color for text/icons on top of the primary color
    onBackground = White,  // Color for text/icons on top of the background
    onSurface = White,      // Color for text/icons on top of surfaces
)

private val LightColorScheme = lightColorScheme(
    primary = DarkGray,        // Color for focused TextField border, progress bar
    background = White,    // App background
    surface = White,       // Surface color for components like TextField
    onPrimary = White,     // Color for text/icons on top of the primary color
    onBackground = Black,  // Color for text/icons on top of the background
    onSurface = Black      // Color for text/icons on top of surfaces


)

@Composable
fun Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context).copy(
                    background = Color.Black,
                    surface = Color.Black
                )
            } else {
                dynamicLightColorScheme(context).copy(
                    background = Color.White,
                    surface = Color.White
                )
            }
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }


    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Optional: Ensure the status bar background is transparent so content shows through
            // window.statusBarColor = Color.Transparent.toArgb()

            // Control the icons:
            // true  = Black Icons
            // false = White Icons
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}