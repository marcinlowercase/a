/*
 * Copyright (C) 2026 marcinlowercase
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
    SYNC(OptionType.OPENS_PANEL),

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
    SUGGESTIONS(OptionType.TOGGLE_ACTION),
    MEDIA_CONTROL(OptionType.TOGGLE_ACTION),
    OUT_SYNC(OptionType.TOGGLE_ACTION),
    OPTIMIZE_MEMORY(OptionType.TOGGLE_ACTION),
    MEMORY_USAGE(OptionType.OPENS_SETTING),
    CHANGE_ICON(OptionType.TOGGLE_ACTION),
}