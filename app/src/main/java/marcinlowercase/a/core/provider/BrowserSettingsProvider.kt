package marcinlowercase.a.core.provider

import android.os.Build
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import marcinlowercase.a.core.constant.DefaultSettingValues
import marcinlowercase.a.core.enum_class.SharpMode

class BrowserSettingsProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val ctx = context ?: return null

        val globalPrefs = ctx.getSharedPreferences("BrowserPrefs", Context.MODE_PRIVATE)
        val profilePrefs = ctx.getSharedPreferences("BrowserProfiles", Context.MODE_PRIVATE)
        val activeProfileId = profilePrefs.getString("active_profile_id", null)

        val prefsToUse = if (activeProfileId != null) {
            val activeProfilePrefs = ctx.getSharedPreferences("BrowserPrefs_$activeProfileId", Context.MODE_PRIVATE)
            if (activeProfilePrefs.contains("default_url")) activeProfilePrefs else globalPrefs
        } else {
            globalPrefs
        }

        val sharpMode = if (prefsToUse.contains("sharp_mode")) {
            prefsToUse.getInt("sharp_mode", DefaultSettingValues.SHARP_MODE)
        } else {
            if (prefsToUse.getBoolean("is_sharp_mode", false)) SharpMode.SHARP.ordinal else SharpMode.ROUND.ordinal
        }

        val currentCornerRadius = globalPrefs.getFloat("current_corner_radius", DefaultSettingValues.CORNER_RADIUS)
        val hardwareCornerRadius = globalPrefs.getFloat("hardware_corner_radius", DefaultSettingValues.CORNER_RADIUS)
        val padding = globalPrefs.getFloat("padding", DefaultSettingValues.PADDING)

        val isMaterialYou = prefsToUse.getBoolean(
            "is_enabled_material_you",
            DefaultSettingValues.IS_ENABLED_MATERIAL_YOU
        ) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val highlightColor = prefsToUse.getInt("highlight_color", DefaultSettingValues.HIGHLIGHT_COLOR)

        val columns = arrayOf(
            "sharp_mode",
            "current_corner_radius",
            "hardware_corner_radius",
            "padding",
            "is_material_you",
            "highlight_color"
        )
        return MatrixCursor(columns, 1).apply {
            addRow(
                arrayOf<Any>(
                    sharpMode,
                    currentCornerRadius,
                    hardwareCornerRadius,
                    padding,
                    if (isMaterialYou) 1 else 0,
                    highlightColor
                )
            )
            setNotificationUri(ctx.contentResolver, uri)
        }
    }

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}