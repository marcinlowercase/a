package marcinlowercase.a

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.os.Bundle

class DispatcherActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val browserIntent = Intent(this, MainActivity::class.java).apply {
            action = intent?.action ?: Intent.ACTION_VIEW

            // 1. Preserve both data URI and MIME type (e.g. application/pdf)
            setDataAndType(intent?.data, intent?.type)

            // 2. Forward the ClipData holding the URI permission grant
            clipData = intent?.clipData ?: intent?.data?.let { ClipData.newRawUri("", it) }

            intent?.extras?.let { putExtras(it) }

            // 3. Keep NEW_TASK and explicitly forward READ permission to MainActivity
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(browserIntent)

        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }
}