package marcinlowercase.a.core.data_class

import kotlinx.serialization.Serializable

@Serializable
data class SyncPayload(
    val timestamp: Long,
    val profiles: List<ProfileSyncDTO>
)

@Serializable
data class ProfileSyncDTO(
    val id: String,
    val name: String,
    val settings: ProfileSettingsSyncDTO,
    val pinnedApps: List<AppSyncDTO>,
    val visitedUrls: List<VisitedUrlSyncDTO>
)

@Serializable
data class ProfileSettingsSyncDTO(
    // NOTE: Global settings (Padding, Corner Radius, isSync, etc.) are excluded intentionally!
    val defaultUrl: String,
    val animationSpeed: Float,
    val isSharpMode: Boolean,
    val cursorContainerSize: Float,
    val cursorPointerSize: Float,
    val cursorTrackingSpeed: Float,
    val showSuggestions: Boolean,
    val closedTabHistorySize: Float,
    val backSquareOffsetX: Float,
    val backSquareOffsetY: Float,
    val backSquareIdleOpacity: Float,
    val searchEngine: Int,
    val isFullscreenMode: Boolean,
    val highlightColor: Int,
    val isAdBlockEnabled: Boolean,
    val isGuideModeEnabled: Boolean,
    val isDesktopMode: Boolean,
    val isEnabledMediaControl: Boolean,
    val isEnabledOutSync: Boolean,
    val optionsOrder: String,
    val settingsOrder: String,
    val hiddenOptions: String
)

@Serializable
data class AppSyncDTO(
    val id: Long,
    val label: String,
    val url: String,
    val iconUrl: String
)

@Serializable
data class VisitedUrlSyncDTO(
    val url: String,
    val title: String
)