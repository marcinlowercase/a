package studio.oo1.browser.core.data_class

data class PollData(
    val timestampMs: Long,
    val bytesDownloaded: Long,
    val lastSpeedBps: Float = 0f
)
