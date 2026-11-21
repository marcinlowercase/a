package marcinlowercase.a.core.data_class

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

data class LandscapeInputSession(
    val sessionId: Long = System.nanoTime(),
    val initialText: String,
    val selectAllOnOpen: Boolean = false,
    val placeholder: String = "",
    val keyboardType: KeyboardType = KeyboardType.Text,
    val imeAction: ImeAction = ImeAction.Done,
    val isUrlBar: Boolean = false,
    val onTextChanged: ((String) -> Unit)? = null,
    val onCommit: (text: String, submitAction: Boolean) -> Unit,
    val onCancel: () -> Unit = {}
)