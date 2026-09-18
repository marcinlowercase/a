package marcinlowercase.a.core.data_class

data class ConfirmationDialogState(
    val message: Int,
    val url: String = "",
    val onConfirm: () -> Unit,
    val onCancel: () -> Unit
)
