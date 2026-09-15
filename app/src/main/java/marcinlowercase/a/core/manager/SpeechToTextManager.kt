package marcinlowercase.a.core.manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechToTextManager(
    private val context: Context,
    private val onSegmentResult: (String) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    var isListening = false
        private set

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        isListening = true
        onListeningStateChanged(true)
        startListeningInternal()
    }

    private fun startListeningInternal() {
        if (!isListening) return

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}

                    override fun onError(error: Int) {
                        // If user hasn't tapped Stop, silently restart on pause/silence timeouts
                        if (isListening && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                            startListeningInternal()
                        } else if (error != SpeechRecognizer.ERROR_CLIENT) {
                            Log.w("STT", "Speech recognition error: $error")
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onSegmentResult(matches[0])
                        }
                        // CONTINUOUS: Re-arm so user can pause and keep talking
                        if (isListening) {
                            startListeningInternal()
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                // Extend silence tolerance to 5 seconds before committing a segment
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("STT", "Failed to start speech listener", e)
            stopListening()
        }
    }

    fun stopListening() {
        isListening = false
        onListeningStateChanged(false)
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }
}