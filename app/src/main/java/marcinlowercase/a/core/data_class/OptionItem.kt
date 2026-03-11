package marcinlowercase.a.core.data_class

import marcinlowercase.a.core.enum_class.BrowserOption

data class OptionItem(
    val id: BrowserOption,
    val iconRes: Int, // The drawable resource ID for the icon
    val contentDescription: String,
    val enabled: Boolean = false,
    val textIcon: String? = null,

    val onClick: () -> Unit,

    )

