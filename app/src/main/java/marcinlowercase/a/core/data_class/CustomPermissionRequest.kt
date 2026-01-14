package marcinlowercase.a.core.data_class

data class CustomPermissionRequest(
    val origin: String,
    val title: String,
    val rationale: String,
    val iconResAllow: Int,
    val iconResDeny: Int,
    val permissionsToRequest: List<String>,
    val onResult: (Map<String, Boolean>) -> Unit
)