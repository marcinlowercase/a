package marcinlowercase.a.core.data_class

data class PanelVisibilityState(
    val options: Boolean,
    val apps: Boolean = false,
    val tabs: Boolean,
    val downloads: Boolean,
    val tabData: Boolean,
    val nav: Boolean
)
