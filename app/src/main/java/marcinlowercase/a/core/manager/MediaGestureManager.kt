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
package marcinlowercase.a.core.manager

import android.app.Activity
import android.content.Context
import android.media.AudioManager

class MediaGestureManager(private val activity: Activity) {
    private val audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // BRIGHTNESS

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