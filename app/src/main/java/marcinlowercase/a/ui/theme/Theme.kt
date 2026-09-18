 
package marcinlowercase.a.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // 1. IF ENABLED: Use pure Material You dynamic colors
        val context = LocalContext.current

        // Surface Variant is basically surface but will be affected by dark/light mode
        // surface container is the surface but if not enable dynamic color, it always black
        if (darkTheme) {
            dynamicDarkColorScheme(context).copy(
                surfaceVariant = dynamicDarkColorScheme(context).surface,
                onSurfaceVariant = dynamicDarkColorScheme(context).onSurface,
            )
        } else {
            dynamicLightColorScheme(context).copy(
//                surfaceVariant = Color.White,
                surfaceVariant = dynamicLightColorScheme(context).surface,
                onSurfaceVariant = dynamicLightColorScheme(context).onSurface,
                inverseSurface = dynamicLightColorScheme(context).surface,
            )
        }
    } else {
        // 2. IF DISABLED: Use default scheme but override exactly the 5 colors you requested


        val finalTheme = darkColorScheme().copy(
            surfaceContainer = Color.Black,
//            surfaceVariant = Color.Transparent,
            onSurface = Color.White,
            secondaryContainer = Color.White,
            onSecondaryContainer = Color.Black,
            inverseSurface = Color.White,

            )

        if (darkTheme) {
            finalTheme.copy(surfaceVariant = Color.Black, onSurfaceVariant = Color.White)
        } else {
            finalTheme.copy(surfaceVariant = Color.White, onSurfaceVariant = Color.Black)

        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Control the icons: false = White Icons
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}