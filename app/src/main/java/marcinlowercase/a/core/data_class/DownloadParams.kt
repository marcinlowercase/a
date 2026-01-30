package marcinlowercase.a.core.data_class

data class DownloadParams(
    val url: String,
    val userAgent: String,
    val contentDisposition: String?,
    val mimeType: String?
)