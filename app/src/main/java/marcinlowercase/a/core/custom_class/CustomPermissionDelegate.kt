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
package marcinlowercase.a.core.custom_class

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.core.content.ContextCompat
import marcinlowercase.a.R
import marcinlowercase.a.core.constant.generic_location_permission
import marcinlowercase.a.core.data_class.CustomPermissionRequest
import marcinlowercase.a.core.data_class.SiteSettings
import marcinlowercase.a.core.data_class.Tab
import marcinlowercase.a.core.function.toDomain
import marcinlowercase.a.core.manager.SiteSettingsManager
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import kotlin.collections.get

class CustomPermissionDelegate(
    // The callback to trigger the UI update in the Composable remains the same
    val context: android.content.Context,
    val tab: MutableState<Tab>,
    private val onShowRequest: (CustomPermissionRequest) -> Unit,
    private val siteSettingsManager: SiteSettingsManager,
    private val siteSettings: Map<String, SiteSettings>,
) : GeckoSession.PermissionDelegate {

    override fun onContentPermissionRequest(
        session: GeckoSession,
        perm: GeckoSession.PermissionDelegate.ContentPermission
    ): GeckoResult<Int?>? {



        if (perm.permission == GeckoSession.PermissionDelegate.PERMISSION_GEOLOCATION) {

            val decision = siteSettings[perm.uri.toDomain()]?.permissionDecisions?.get(generic_location_permission)

            if (decision == false) {
                return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY)

            }
//            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
//            ) {
//
//                val customRequest = CustomPermissionRequest(
//                    origin = tab.value.currentURL,
//                    title = "Location Request",
//                    rationale = "This site wants to use your device's location.",
//                    iconResAllow = R.drawable.ic_location_on, // Make sure you have these drawables
//                    iconResDeny = R.drawable.ic_location_off,
//                    permissionsToRequest = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
//                    onResult = { permissionsMap, pendingRequest ->
//                        // This is the final step, after the user interacts with the system dialog.
//                        if (permissionsMap.any { it.value }) {
//                            // Tell GeckoView the app permission was granted.
//                            GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW)
//
//
//                        } else {
//                            // Tell GeckoView the app permission was denied.
//                            GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY)
//                            pendingRequest.value = null
//                        }
//                    }
//                )
//                onShowRequest(customRequest)
//
//                return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_PROMPT)
//
//
//            }

            return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW)
        }

        if (perm.permission == GeckoSession.PermissionDelegate.PERMISSION_DESKTOP_NOTIFICATION) {
            val domain = perm.uri.toDomain()
            val permissionKey = "desktop_notification"

            // Check if the user has already allowed/denied notifications for this site
            val decision = siteSettings[domain]?.permissionDecisions?.get(permissionKey)
            if (decision == false) {
                return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY)
            } else if (decision == true) {
                return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW)
            }

            val result = GeckoResult<Int?>()

            // Prepare the Android system permissions to request based on the OS version
            val permissionsToAsk = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToAsk.add(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Dummy valid permission for older APIs to ensure the launcher returns a 'true' map
                // so we can distinguish between an 'Allow' click and a 'Deny' dismissal in the UI
                permissionsToAsk.add(Manifest.permission.INTERNET)
            }

            val customRequest = CustomPermissionRequest(
                origin = perm.uri,
                title = "Notifications Request",
                rationale = "This site wants to send you notifications.",
                // Feel free to replace ic_bug with an ic_notifications_on / ic_notifications_off drawable
                iconResAllow = R.drawable.ic_bug,
                iconResDeny = R.drawable.ic_bug,
                permissionsToRequest = permissionsToAsk,
                onResult = { permissionsMap, pendingRequest ->

                    // Determine if it was granted based on the Android API
                    val isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionsMap[Manifest.permission.POST_NOTIFICATIONS] == true
                    } else {
                        permissionsMap[Manifest.permission.INTERNET] == true
                    }

                    // Safely save the decision locally to SiteSettingsManager
                    val currentSettings = siteSettingsManager.loadSettings(tab.value.profileId)
                    val domainSettings = currentSettings[domain] ?: SiteSettings(domain)
                    val updatedDecisions = domainSettings.permissionDecisions.toMutableMap()
                    updatedDecisions[permissionKey] = isGranted
                    currentSettings[domain] = domainSettings.copy(permissionDecisions = updatedDecisions)
                    siteSettingsManager.saveSettings(tab.value.profileId, currentSettings)

                    // Give GeckoView the final resolution
                    if (isGranted) {
                        result.complete(GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW)
                    } else {
                        result.complete(GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY)
                    }

                    // Close the custom Prompt Panel
                    pendingRequest.value = null
                },
                isSystemRequest = false
            )

            // Show your Custom UI panel
            onShowRequest(customRequest)
            return result
        }

        return super.onContentPermissionRequest(session, perm)
    }


    override fun onAndroidPermissionsRequest(
        session: GeckoSession,
        permissions: Array<out String>?,
        callback: GeckoSession.PermissionDelegate.Callback
    ) {
        var requestTitle = "Default title"
        var requestRationale = "Default Rationale"
        var requestAllowIcon = R.drawable.ic_bug
        var requestDenyIcon = R.drawable.ic_bug

        if (permissions.isNullOrEmpty()) {
            callback.reject()
            return
        }
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }


        var decision: Boolean? = false
        val domain = tab.value.currentURL.toDomain()
        if (permissions.contains("android.permission.ACCESS_FINE_LOCATION") || permissions.contains(
                "android.permission.ACCESS_COARSE_LOCATION"
            )
        ) {
//            checkSavedSiteSettings("android.permission.CAMERA")
            decision = siteSettings[domain]?.permissionDecisions?.get(generic_location_permission)


            // Show the UI that will eventually trigger the system permission launcher
            requestTitle = "Location Request"
            requestRationale = "This site wants to use your device's location."
            requestAllowIcon = R.drawable.ic_location_on
            requestDenyIcon = R.drawable.ic_location_off
        }

        if (permissions.contains("android.permission.CAMERA")) {
            // Show the UI that will eventually trigger the system permission launcher
            decision = siteSettings[domain]?.permissionDecisions?.get(Manifest.permission.CAMERA)

            requestRationale = "This site wants to use your device's camera."
            requestAllowIcon = R.drawable.ic_camera_on
            requestDenyIcon = R.drawable.ic_camera_off

        }
        if (permissions.contains("android.permission.RECORD_AUDIO")) {

            decision = siteSettings[domain]?.permissionDecisions?.get(Manifest.permission.RECORD_AUDIO)

            // Show the UI that will eventually trigger the system permission launcher
            requestTitle = "Microphone Request"
            requestRationale = "This site wants to use your device's microphone."
            requestAllowIcon = R.drawable.ic_mic_on
            requestDenyIcon = R.drawable.ic_mic_off
        }


        if (decision == true && allGranted) { // && is granted android already
            callback.grant()
            return
        }
//        else if (decision == false) {
//            callback.reject()
//            return
//
//        }

        val customRequest = CustomPermissionRequest(
            origin = tab.value.currentURL,
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

        var videoSource : GeckoSession.PermissionDelegate.MediaSource? = null
        var audioSource : GeckoSession.PermissionDelegate.MediaSource? = null


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
        val domain = uri.toDomain()

        val videoDecision = siteSettings[domain]?.permissionDecisions?.get(Manifest.permission.CAMERA)
        val audioDecision = siteSettings[domain]?.permissionDecisions?.get(Manifest.permission.RECORD_AUDIO)





        var isRequestingVideo = !video.isNullOrEmpty()
        var isRequestingAudio = !audio.isNullOrEmpty()

//        if ( isRequestingVideo && isRequestingAudio && videoDecision == true && audioDecision == true) {
//            callback.grant(video?.first(), audio?.first())
//
//            return
//        }

        if (isRequestingVideo && !isRequestingAudio && videoDecision == false) {
            callback.reject()
            return
        } else if (isRequestingAudio && !isRequestingVideo && audioDecision == false) {
            callback.reject()
            return
        } else if (isRequestingVideo && isRequestingAudio && (videoDecision == false || audioDecision == false)) {
            callback.reject()
            return
        }

        if (videoDecision == true && isRequestingVideo) {

//            callback.grant(video?.first(), audio?.first())
            videoSource = video?.first()
//            isRequestingVideo = false

        }
        if (isRequestingAudio && audioDecision == true) {
//            callback.grant( null, audio?.first())
            audioSource = audio?.first()
//            isRequestingAudio = false

        }
     
        if (!isRequestingVideo && !isRequestingAudio) {
            callback.reject()
            return
        }


        // SCENARIO 1 & 2: Only one permission is requested (simple case)
        if (isRequestingVideo && !isRequestingAudio) {


            if (videoDecision == true) {
                callback.grant(videoSource, null)
                return
            }

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
            if (audioDecision == true) {
                callback.grant(null, audioSource)
                return
            }

            askForSinglePermission(uri, "microphone", Manifest.permission.RECORD_AUDIO) { granted ->
                val audioSource = if (granted) audio?.first() else null
                callback.grant(null, audioSource)
            }
            return
        }

        // SCENARIO 3: Both are requested (orchestrated case)
        if (isRequestingVideo && isRequestingAudio) {
            // Store the state needed for the multi-step flow

            if (videoDecision == true && audioDecision == true) {
                callback.grant(videoSource, audioSource)
                return
            }
            pendingMediaCallback = callback
            originalVideoSources = video
            originalAudioSources = audio
            cameraPermissionGranted = false // Reset state

            //Log.i("PermissionRelated", "onMediaPermissionRequest: BOTH")

            // Start the flow by asking for the camera first
            askForCameraThenMicrophone(uri, videoDecision, audioDecision)
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

    private fun askForCameraThenMicrophone(uri: String, videoDecision: Boolean?, audioDecision: Boolean?) {

        if (videoDecision == true ){
            this.cameraPermissionGranted = true
            askForMicrophone(uri, audioDecision)
            return

        }
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
                //Log.i("PermissionRelated", "invoke camera, then microphone")
                askForMicrophone(uri, audioDecision)
            },
            isSystemRequest = false,
        )

        onShowRequest(cameraRequest)
    }

    private fun askForMicrophone(uri: String, audioDecision: Boolean?) {
        if (audioDecision == true) {
            val microphonePermissionGranted = true

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
            return
        }
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