package marcinlowercase.a.core.data_class

import kotlinx.serialization.Serializable
import marcinlowercase.a.core.enum_class.SharpMode

@Serializable
data class SiteSettings(
    val domain: String,
    // map of <PermissionConstant, isGranted> e.g., <"android.permission.CAMERA", true>
    val permissionDecisions: MutableMap<String, Boolean> = mutableMapOf(),
    val isDesktopMode: Boolean = false,
    val sharpMode: Int? = null
) {
    fun currentSharpMode(): SharpMode = sharpMode?.let {
        SharpMode.entries.getOrElse(it) { SharpMode.ROUND }
    } ?: SharpMode.ROUND
}
