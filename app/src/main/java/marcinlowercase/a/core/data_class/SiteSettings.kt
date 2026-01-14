package marcinlowercase.a.core.data_class

import kotlinx.serialization.Serializable

@Serializable
data class SiteSettings(
    val domain: String,
    // Map of <PermissionConstant, isGranted> e.g., <"android.permission.CAMERA", true>
    val permissionDecisions: MutableMap<String, Boolean> = mutableMapOf()
)
