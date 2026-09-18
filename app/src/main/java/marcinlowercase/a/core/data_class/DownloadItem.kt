package marcinlowercase.a.core.data_class

import kotlinx.serialization.Serializable
import marcinlowercase.a.core.enum_class.DownloadStatus

@Serializable
data class DownloadItem(
    val id: Long, // This ID comes from Android's DownloadManager
    val url: String,
    val filename: String,
    val mimeType: String,
    var status: DownloadStatus = DownloadStatus.PENDING,
    var progress: Int = 0, // Progress from 0 to 100
    var totalBytes: Long = 0,
    var downloadedBytes: Long = 0,
    val isBlobDownload: Boolean = false,
    @kotlinx.serialization.Transient var downloadSpeedBps: Float = 0f, // Bytes per second
    @kotlinx.serialization.Transient var timeRemainingMs: Long = 0L    // Milliseconds
)
