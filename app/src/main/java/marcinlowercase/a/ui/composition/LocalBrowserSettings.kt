package marcinlowercase.a.ui.composition

import androidx.compose.runtime.staticCompositionLocalOf
import marcinlowercase.a.core.data_class.BrowserSettings
import marcinlowercase.a.core.enum_class.BrowserSettingField

data class BrowserSettingsController(
    val current: BrowserSettings,
    val update: (BrowserSettings) -> Unit,
    val updateField: (BrowserSettingField, Any) -> Unit,
    val reset: () -> Unit
)

val LocalBrowserSettings = staticCompositionLocalOf<BrowserSettingsController> {
    error("No BrowserSettings provided")
}