package marcinlowercase.a.ui.state

import marcinlowercase.a.core.data_class.ContextMenuData
import marcinlowercase.a.core.data_class.ErrorState
import marcinlowercase.a.core.data_class.JsChoiceState
import marcinlowercase.a.core.data_class.JsColorState
import marcinlowercase.a.core.data_class.JsDateTimeState
import marcinlowercase.a.core.data_class.JsDialogState
import marcinlowercase.a.core.enum_class.ActivePanel

/**
 * A single class that represents the entire "look" of the browser.
 * If you print this object, you know exactly what is on screen.
 */
data class BrowserUiState(
    // Global Visibility
    val isUrlBarVisible: Boolean = true,
    val isBottomPanelVisible: Boolean = true,
    val isLoading: Boolean = false,

    // The Traffic Controller: Which panel is currently "Active" and claiming focus?
    // Using your existing ActivePanel enum (Apps, Downloads, Settings, etc.)
    val activePanel: ActivePanel? = null,

    // Text Field State (The text inside the URL bar)
    val urlBarText: String = "",
    val isFocusOnUrlTextField: Boolean = false,

    // Search / Find in Page
    val findInPageQuery: String = "",
    val findInPageResult: Pair<Int, Int> = 0 to 0,

    // Transient Overlays (Prompts, Context Menus)
    // These block interaction with the rest of the app
    val jsDialogState: JsDialogState? = null,
    val choiceState: JsChoiceState? = null,
    val colorState: JsColorState? = null,
    val dateTimeState: JsDateTimeState? = null,
    val contextMenuData: ContextMenuData? = null,

    // Error Screen
    val errorState: ErrorState? = null,

    // Modes
    val isCursorMode: Boolean = false,
    val isFullscreenVideo: Boolean = false
)