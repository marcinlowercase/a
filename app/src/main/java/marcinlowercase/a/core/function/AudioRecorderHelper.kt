package marcinlowercase.a.core.function

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class AudioRecorderHelper(private val context: Context) {

    private var recorder: MediaRecorder? = null
    var currentAudioFile: File? = null
        private set
    var isRecording: Boolean = false
        private set

    fun startRecording(): Boolean {
        return try {

            val audioFile = File(context.cacheDir, "voice_input.m4a")
            if (audioFile.exists()) audioFile.delete()
            currentAudioFile = audioFile

            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            newRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1)               // <-- Mono (cuts file size in half)
                setAudioSamplingRate(16000)       // <-- 16 kHz (speech standard)
                setAudioEncodingBitRate(32000)    // <-- 32 kbps (drastically shrinks upload size)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            recorder = newRecorder
            isRecording = true
            true
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start recording", e)
            isRecording = false
            false
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) return null
        val startTime = System.currentTimeMillis()
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            isRecording = false

            val file = currentAudioFile
            val stopDuration = System.currentTimeMillis() - startTime
            val fileSizeKb = (file?.length() ?: 0L) / 1024

            // --- DIAGNOSTIC LOG ---
            Log.i("AudioTimer", "Recording Stopped in ${stopDuration}ms | File Size: ${fileSizeKb} KB | Path: ${file?.name}")

            file
        } catch (e: Exception) {
            Log.e("AudioTimer", "Failed to stop recording", e)
            recorder = null
            isRecording = false
            null
        }
    }
}