package marcinlowercase.a.core.enum_class

enum class OptionType {
    OPENS_PANEL,     // Must stick to Options Panel
    OPENS_SETTING,   // Must stick to Settings Panel
    TOGGLE_ACTION    // Movable between both!
}

enum class BrowserOption(val type: OptionType) {
    // Options Panel Exclusives
    TABS_PANEL(OptionType.OPENS_PANEL),
    FIND_IN_PAGE(OptionType.OPENS_PANEL),
    DOWNLOAD_PANEL(OptionType.OPENS_PANEL),
    SETTINGS(OptionType.OPENS_PANEL),

    // Settings Panel Exclusives
    SEARCH_ENGINE(OptionType.OPENS_SETTING),
    DEFAULT_URL(OptionType.OPENS_SETTING),
    HISTORY_SIZE(OptionType.OPENS_SETTING),
    CORNER_RADIUS(OptionType.OPENS_SETTING),
    PADDING(OptionType.OPENS_SETTING),
    SINGLE_LINE_HEIGHT(OptionType.OPENS_SETTING),
    MAX_LIST_HEIGHT(OptionType.OPENS_SETTING),
    HIGHLIGHT_COLOR(OptionType.OPENS_SETTING),
    ANIMATION_SPEED(OptionType.OPENS_SETTING),
    CURSOR_SIZE(OptionType.OPENS_SETTING),
    CURSOR_SPEED(OptionType.OPENS_SETTING),
    BACK_SQUARE_OPACITY(OptionType.OPENS_SETTING),
    RESET_SETTINGS(OptionType.OPENS_SETTING),
    PRIVACY_POLICY(OptionType.OPENS_SETTING),
    SORT_BUTTONS(OptionType.OPENS_SETTING),

    // Toggles (Movable anywhere!)
    SHARP_MODE(OptionType.TOGGLE_ACTION),
    REOPEN_TAB(OptionType.TOGGLE_ACTION),
    FULLSCREEN(OptionType.TOGGLE_ACTION),
    DESKTOP_MODE(OptionType.TOGGLE_ACTION),
    ROTATE(OptionType.TOGGLE_ACTION),
    CLOSE_ALL_TABS(OptionType.TOGGLE_ACTION),
    ADBLOCK(OptionType.TOGGLE_ACTION),
    SUGGESTIONS(OptionType.TOGGLE_ACTION)
}