package marcinlowercase.a.core.custom_class

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import marcinlowercase.a.R
import marcinlowercase.a.core.data_class.CustomPermissionRequest
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession

class CustomPermissionDelegate(
    // The callback to trigger the UI update in the Composable remains the same
    val context: android.content.Context,
    private val onShowRequest: (CustomPermissionRequest) -> Unit,
    private val onShowAndroidRequest: (
        permissions: Array<out String>?,
        callback: GeckoSession.PermissionDelegate.Callback
    ) -> Unit,
) : GeckoSession.PermissionDelegate {

    companion object {
        const val ANDROID_PERMISSION_REQUEST_CODE: Int = 11111
    }

    private var mCallback: GeckoSession.PermissionDelegate.Callback? = null

    fun onAndroidRequestPermissionsResult(
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val cb = mCallback ?: return
        mCallback = null

        // Check if every permission in the list was granted
        val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

        if (allGranted) {
            // Tell Gecko: "The OS gave us permission, proceed!"
            cb.grant()
        } else {
            // Tell Gecko: "The OS denied permission, cancel the request."
            cb.reject()
        }
    }

    override fun onContentPermissionRequest(
        session: GeckoSession,
        perm: GeckoSession.PermissionDelegate.ContentPermission
    ): GeckoResult<Int?>? {


        // We only handle geolocation in this example.
        if (perm.permission == GeckoSession.PermissionDelegate.PERMISSION_GEOLOCATION) {
            Log.e("PermissionRelated", "onContentPermissionRequest: $perm")
            return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW)
        }
        return super.onContentPermissionRequest(session, perm)
    }


    override fun onAndroidPermissionsRequest(
        session: GeckoSession,
        permissions: Array<out String>?,
        callback: GeckoSession.PermissionDelegate.Callback
    ) {
        Log.e("PermissionRelated", "onAndroidPermissionsRequest: $permissions")
        Log.e("PermissionRelated", "onAndroidPermissionsRequest: ${permissions?.joinToString()}")

        var requestTitle = "Default title"
        var requestRationale = "Default Rationale"
        var requestAllowIcon = R.drawable.ic_bug
        var requestDenyIcon = R.drawable.ic_bug

        if (permissions.isNullOrEmpty()) {
            callback.reject()
            return
        }


        if (permissions.contains("android.permission.ACCESS_FINE_LOCATION") || permissions.contains(
                "android.permission.ACCESS_COARSE_LOCATION"
            )
        ) {
            // Show the UI that will eventually trigger the system permission launcher
            requestTitle = "Location Request"
            requestRationale = "This site wants to use your device's location."
            requestAllowIcon = R.drawable.ic_location_on
            requestDenyIcon = R.drawable.ic_location_off
        }

        if (permissions.contains("android.permission.CAMERA")) {
            // Show the UI that will eventually trigger the system permission launcher
            requestTitle = "Camera Request"
            requestRationale = "This site wants to use your device's camera."
            requestAllowIcon = R.drawable.ic_camera_on
            requestDenyIcon = R.drawable.ic_camera_off

        }
        if (permissions.contains("android.permission.RECORD_AUDIO")) {
            // Show the UI that will eventually trigger the system permission launcher
            requestTitle = "Microphone Request"
            requestRationale = "This site wants to use your device's microphone."
            requestAllowIcon = R.drawable.ic_mic_on
            requestDenyIcon = R.drawable.ic_mic_off
        }


        val customRequest = CustomPermissionRequest(
            origin = "",
            title = requestTitle,
            rationale = requestRationale,
            iconResAllow = requestAllowIcon, // Make sure you have these drawables
            iconResDeny = requestDenyIcon,
            permissionsToRequest = permissions.toList(),
            onResult = { permissionsMap, pendingRequest ->
                // This is the final step, after the user interacts with the system dialog.
                if (permissionsMap.any { it.value }) {
                    // Tell GeckoView the app permission was granted.
                    callback.grant()
                } else {
                    // Tell GeckoView the app permission was denied.
                    callback.reject()
                    pendingRequest.value = null
                }
            }
        )
        onShowRequest(customRequest)


    }

    private var pendingMediaCallback: GeckoSession.PermissionDelegate.MediaCallback? = null
    private var originalVideoSources: Array<out GeckoSession.PermissionDelegate.MediaSource>? = null
    private var originalAudioSources: Array<out GeckoSession.PermissionDelegate.MediaSource>? = null
    private var cameraPermissionGranted: Boolean = false


    override fun onMediaPermissionRequest(
        session: GeckoSession,
        uri: String,
        video: Array<out GeckoSession.PermissionDelegate.MediaSource>?,
        audio: Array<out GeckoSession.PermissionDelegate.MediaSource>?,
        callback: GeckoSession.PermissionDelegate.MediaCallback
    ) {

        val context = context
        val hasAudio = if (audio != null) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        val hasVideo = if (video != null) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        if (!hasAudio || !hasVideo) {
            // We don't have OS permission, so we can't give Site permission.
            callback.reject()
            return
        }


        val isRequestingVideo = !video.isNullOrEmpty()
        val isRequestingAudio = !audio.isNullOrEmpty()
        Log.i("PermissionRelated", "isRequestingVideo $isRequestingVideo")
        Log.i("PermissionRelated", "isRequestingAudio $isRequestingAudio")

        if (!isRequestingVideo && !isRequestingAudio) {
            callback.reject()
            return
        }

        // SCENARIO 1 & 2: Only one permission is requested (simple case)
        if (isRequestingVideo && !isRequestingAudio) {
            askForSinglePermission(
                uri,
                "camera",
                Manifest.permission.CAMERA
            ) { granted ->
                val videoSource = if (granted) video?.first() else null
                callback.grant(videoSource, null)
            }
            return
        }
        if (!isRequestingVideo && isRequestingAudio) {
            askForSinglePermission(uri, "microphone", Manifest.permission.RECORD_AUDIO) { granted ->
                val audioSource = if (granted) audio?.first() else null
                callback.grant(null, audioSource)
            }
            return
        }

        // SCENARIO 3: Both are requested (orchestrated case)
        if (isRequestingVideo && isRequestingAudio) {
            // Store the state needed for the multi-step flow
            pendingMediaCallback = callback
            originalVideoSources = video
            originalAudioSources = audio
            cameraPermissionGranted = false // Reset state

            Log.i("PermissionRelated", "onMediaPermissionRequest: BOTH")

            // Start the flow by asking for the camera first
            askForCameraThenMicrophone(uri)
        }
    }

    private fun askForSinglePermission(
        uri: String,
        type: String,
        permission: String,
        onResult: (Boolean) -> Unit
    ) {
        var allowIcon = R.drawable.ic_camera_on
        var denyIcon = R.drawable.ic_camera_off
        if (type == "microphone") {
            allowIcon = R.drawable.ic_mic_on
            denyIcon = R.drawable.ic_mic_off
        }
        val customRequest = CustomPermissionRequest(
            origin = uri,
            title = "Media Request",
            rationale = "$uri wants to use your $type.",
            iconResAllow = allowIcon,
            iconResDeny = denyIcon,
            permissionsToRequest = listOf(permission),
            onResult = { permissionsMap, pendingRequest ->
                onResult(permissionsMap.any { it.value })
                pendingRequest.value = null
            },
            isSystemRequest = false,
        )
        onShowRequest(customRequest)
    }

    private fun askForCameraThenMicrophone(uri: String) {
        val cameraRequest = CustomPermissionRequest(
            origin = uri,
            title = "Camera Access",
            rationale = "$uri wants to use your camera.",
            iconResAllow = R.drawable.ic_camera_on,
            iconResDeny = R.drawable.ic_camera_off,
            permissionsToRequest = listOf(Manifest.permission.CAMERA),
            onResult = { permissionsMap, pendingRequest ->
                // Step 1 Result: Camera permission granted or denied
                this.cameraPermissionGranted = permissionsMap.any { it.value }

                // Step 2: Now ask for the microphone
                Log.i("PermissionRelated", "invoke camera, then microphone")
                askForMicrophone(uri)
            },
            isSystemRequest = false,
        )
        onShowRequest(cameraRequest)
    }

    private fun askForMicrophone(uri: String) {
        val microphoneRequest = CustomPermissionRequest(
            origin = uri,
            title = "Microphone Access",
            rationale = "$uri wants to use your microphone.",
            iconResAllow = R.drawable.ic_mic_on,
            iconResDeny = R.drawable.ic_mic_off,
            permissionsToRequest = listOf(Manifest.permission.RECORD_AUDIO),
            onResult = { permissionsMap, pendingRequest ->
                // Step 2 Result: Microphone permission granted or denied
                val microphonePermissionGranted = permissionsMap.any { it.value }

                // FINAL STEP: Combine results and call the original callback
                val videoSource =
                    if (cameraPermissionGranted) originalVideoSources?.first() else null
                val audioSource =
                    if (microphonePermissionGranted) originalAudioSources?.first() else null

                if (videoSource != null || audioSource != null) {
                    pendingMediaCallback?.grant(videoSource, audioSource)
                } else {
                    pendingMediaCallback?.reject()
                }

                // Clean up state to prevent memory leaks
                pendingMediaCallback = null
                originalVideoSources = null
                originalAudioSources = null

                pendingRequest.value = null
            },
            isSystemRequest = false,
        )
        onShowRequest(microphoneRequest)
    }

}