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
    val settings: String,
    val pinnedApps: List<AppSyncDTO>,
    val visitedUrls: List<VisitedUrlSyncDTO>
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