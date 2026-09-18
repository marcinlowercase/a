package marcinlowercase.a.core.enum_class

import kotlinx.serialization.Serializable

@Serializable
enum class DownloadStatus {
    PENDING,
    RUNNING,
    PAUSED,
    SUCCESSFUL,
    FAILED,
    CANCELLED
}