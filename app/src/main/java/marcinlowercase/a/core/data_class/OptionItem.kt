package studio.oo1.browser.core.data_class

data class OptionItem(
    val iconRes: Int, // The drawable resource ID for the icon
    val contentDescription: String,
    val enabled: Boolean = false,
    val onClick: () -> Unit,
)

