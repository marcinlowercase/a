package marcinlowercase.a.core.manager

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.WindowManager

class MediaGestureManager(private val activity: Activity) {
    private val audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // BRIGHTNESS

    fun ensureFullscreenBrightness() {
        val params = activity.window.attributes
        // If brightness is currently Auto (-1.0), snap it to 0.5 (50%)
        if (params.screenBrightness < 0) {
            params.screenBrightness = 0.5f
            activity.window.attributes = params
        }
    }
    fun setBrightness(stepChange: Int) {
        val params = activity.window.attributes

        // 1. Get current brightness as a Float.
        // If -1 (Auto), assume 0.5 (50%) as a starting point.
        val currentFloat = if (params.screenBrightness < 0) 0.5f else params.screenBrightness

        // 2. Convert to Integer Step (0-100)
        val currentStep = (currentFloat * 100).toInt()

        // 3. Calculate new Step
        val newStep = (currentStep + stepChange).coerceIn(0, 100)

        // 4. Convert back to Float (0.0 to 1.0)
        // Note: We use 0.01f as a minimum to prevent the screen from turning completely black/off on some devices
        val newFloat = (newStep / 100f).coerceAtLeast(0.01f)

        params.screenBrightness = newFloat
        activity.window.attributes = params
    }

    // Returns Pair(CurrentStep, MaxStep) -> e.g., (50, 100)
    fun getBrightnessSteps(): Pair<Int, Int> {
        val params = activity.window.attributes
        val currentFloat = if (params.screenBrightness < 0) 0.5f else params.screenBrightness

        val currentStep = (currentFloat * 100).toInt()
        return Pair(currentStep, 100)
    }
    fun resetBrightness() {
        val params = activity.window.attributes
        // -1.0f tells Android: "Stop overriding, use system setting"
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        activity.window.attributes = params
    }



    // VOLUME: 0 to Max
    fun setVolume(stepChange: Int) {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        // Calculate new volume and ensure it stays within 0 to Max
        val newVolume = (current + stepChange).coerceIn(0, max)

        // Flag 0 hides the system UI, Flag 1 shows it
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
    }

    fun getVolumeSteps(): Pair<Int, Int> {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return Pair(current, max)
    }
}