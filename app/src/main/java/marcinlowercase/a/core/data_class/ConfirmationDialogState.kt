package marcinlowercase.a.core.data_class

data class ConfirmationDialogState(
    val message: String,
    val onConfirm: () -> Unit,
    val onCancel: () -> Unit
)
