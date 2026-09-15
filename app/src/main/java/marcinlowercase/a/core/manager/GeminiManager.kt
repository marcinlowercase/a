package marcinlowercase.a.core.manager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import android.util.Base64
import marcinlowercase.a.BuildConfig


class GeminiManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("GeminiPrefs", Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // Default target model
    private val modelName = "gemini-3.8-flash"
    private val fallbackModelName = "gemini-3.7-flash"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    // ==========================================
    // API KEY MANAGEMENT
    // ==========================================
    fun getApiKey(): String {
        val userSavedKey = prefs.getString("gemini_api_key", "").orEmpty()
        return userSavedKey.ifBlank { BuildConfig.GEMINI_API_KEY }
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key.trim()).apply()
    }

    // ==========================================
    // MULTI-TURN CHAT / INTERVIEW (STAGE 1)
    // ==========================================
    /**
     * Sends the conversation history to Gemini 3.8 Flash.
     * @param history List of Pair("user" | "model", text)
     * @param systemPrompt The consultative architect prompt
     * @param jsonMode If true, forces structured JSON output
     */
    suspend fun sendChatMessage(
        history: List<Pair<String, String>>,
        systemPrompt: String,
        jsonMode: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("API_KEY_MISSING"))
        }
        val totalStart = System.currentTimeMillis()
        var stepStart = totalStart
        try {
            val url = "$baseUrl/$modelName:generateContent?key=$apiKey"

            // 1. Build Payload
            val payload = JSONObject().apply {
                // System instructions
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })

                // Conversation history
                put("contents", JSONArray().apply {
                    history.forEach { (role, message) ->
                        put(JSONObject().apply {
                            put("role", role)
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", message) })
                            })
                        })
                    }
                })

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", if (jsonMode) 0.1 else 0.4)
                    if (jsonMode) {
                        put("response_mime_type", "application/json")
                    }
                })
            }

            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            Log.d("GeminiTimer", "[Chat] Payload constructed in ${System.currentTimeMillis() - stepStart}ms")

            var attempts = 0
            var currentModel = modelName
            var rawResponse = ""

            while (attempts < 3) {
                attempts++
                val netStart = System.currentTimeMillis()
                val targetUrl = "$baseUrl/$currentModel:generateContent?key=$apiKey"
                val callRequest = Request.Builder().url(targetUrl).post(body).build()

                Log.d("GeminiTimer", "[Chat] Attempt $attempts ($currentModel) starting HTTP call...")
                val response = httpClient.newCall(callRequest).execute()
                val netDuration = System.currentTimeMillis() - netStart
                rawResponse = response.body?.string().orEmpty()

                Log.d("GeminiTimer", "[Chat] Attempt $attempts finished in ${netDuration}ms with code: ${response.code}")

                if (response.isSuccessful) {
                    break
                } else if (response.code == 503 || response.code == 429) {
                    Log.w("GeminiTimer", "[Chat] Overloaded (${response.code}). Backing off for ${1000L * attempts}ms...")
                    if (attempts == 2) currentModel = fallbackModelName
                    kotlinx.coroutines.delay(1000L * attempts)
                } else {
                    return@withContext Result.failure(Exception("HTTP_${response.code}"))
                }
            }

            stepStart = System.currentTimeMillis()
            if (rawResponse.isBlank() || !rawResponse.contains("candidates")) {
                return@withContext Result.failure(Exception("HTTP_503_OVERLOADED"))
            }

            // 2. Parse response text
            val jsonResponse = JSONObject(rawResponse)
            val candidate = jsonResponse.optJSONArray("candidates")?.optJSONObject(0)
            val contentPart = candidate
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
            val replyText = contentPart?.optString("text").orEmpty()
            Log.d("GeminiTimer", "[Chat] JSON parsed in ${System.currentTimeMillis() - stepStart}ms")
            Log.i("GeminiTimer", "=== [Chat] TOTAL ROUNDTRIP: ${System.currentTimeMillis() - totalStart}ms ===")
            Result.success(replyText)
        } catch (e: Exception) {
            Log.e("GeminiManager", "Request failed", e)
            Result.failure(e)
        }
    }



    suspend fun sendVoiceInit(audioFile: File): Result<JSONObject> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("API_KEY_MISSING"))
        }

        try {
            val audioBytes = audioFile.readBytes()
            val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)

            val url = "$baseUrl/$modelName:generateContent?key=$apiKey"

            val systemPrompt = """
            You are the Software Architect for "the browser of oo1 studio".
            The user will provide an audio recording describing their mobile app idea.
            
            CONSTRAINTS:
            - Components live on a 4-Column Mobile Grid at Layer 1.
            - "Everything is a button" (display surfaces, inputs, and action triggers are buttons).
            - Available hardware actions: "scan_barcode", "save_drive", "play_audio", "none".
            
            You must output ONLY a valid JSON object matching this schema:
            {
              "transcription": "Polished, clean transcript resolving self-corrections and hesitations",
              "blueprint": {
                "appTitle": "Short App Title",
                "grid": { "columns": 4, "rowHeight": "70px" },
                "state": { "varName": "initialValue" },
                "components": [
                  {
                    "id": "btn_unique_id",
                    "type": "button",
                    "grid": { "col": 1, "row": 1, "colSpan": 2, "rowSpan": 2 },
                    "style": { "bg": "CSS color (e.g. #ef4444 or var(--highlight-color))", "color": "#ffffff" },
                    "content": { "text": "Label", "placeholder": "Text", "bindState": "varName" },
                    "events": { "onTap": { "action": "scan_barcode | save_drive | play_audio | none", "bindResultTo": "varName" } }
                  }
                ]
              },
              "nextQuestion": {
                "question": "Clarifying question to refine the design",
                "options": ["Option A", "Option B", "Option C"]
              }
            }
        """.trimIndent()

            val payload = JSONObject().apply {
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("inline_data", JSONObject().apply {
                                    put("mime_type", "audio/mp4")
                                    put("data", base64Audio)
                                })
                            })
                            put(JSONObject().apply {
                                put("text", "Listen to this audio idea and generate the initial app blueprint, clean transcription, and follow-up question.")
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("response_mime_type", "application/json") // Enforces strict JSON grammar
                })
            }

            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            val response = httpClient.newCall(request).execute()
            val rawResponse = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP_${response.code}"))
            }

            val jsonResponse = JSONObject(rawResponse)
            val rawText = jsonResponse.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")
                .orEmpty()
                .trim()

// Strip ```json or ``` code blocks if present
            val cleanJsonString = rawText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            Result.success(JSONObject(cleanJsonString))
        } catch (e: Exception) {
            Log.e("GeminiManager", "Voice-to-Action failed", e)
            Result.failure(e)
        }
    }

//    // Gemini 3.8 took a long time to process audio
//    // So I think we will use on device speech to text instead
//    // we don't need to waste token for AI to detect text
//    suspend fun transcribeAudioWithGemini(audioFile: File): Result<String> = withContext(Dispatchers.IO) {
//        val apiKey = getApiKey()
//        if (apiKey.isBlank()) {
//            return@withContext Result.failure(Exception("API_KEY_MISSING"))
//        }
//
//        val totalStart = System.currentTimeMillis()
//
//        try {
//            // 1. Encode 16kHz mono audio (~2ms)
//            val audioBytes = audioFile.readBytes()
//            val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
//
//            // 2. Target Flash Model (1,500 RPD Free Tier Quota)
//            val audioModel = "gemini-3.8-flash"
//            val url = "$baseUrl/$audioModel:generateContent?key=$apiKey"
//
//            val payload = JSONObject().apply {
//                put("contents", JSONArray().apply {
//                    put(JSONObject().apply {
//                        put("role", "user")
//                        put("parts", JSONArray().apply {
//                            // Audio waveform
//                            put(JSONObject().apply {
//                                put("inline_data", JSONObject().apply {
//                                    put("mime_type", "audio/mp4")
//                                    put("data", base64Audio)
//                                })
//                            })
//                            // Single-pass transcription + self-correction cleanup
//                            put(JSONObject().apply {
//                                put("text", "Transcribe this audio into a clean, well-punctuated statement. Resolve all self-corrections (e.g. if speaker says 'City A, no wait City B', output 'City B'). Remove all stutters, false starts, and filler words. Output ONLY the clean resolved sentence.")
//                            })
//                        })
//                    })
//                })
//                put("generationConfig", JSONObject().apply {
//                    put("temperature", 0.0)
//                    // Disable thinking to keep latency minimal
//                    put("thinking_config", JSONObject().apply {
//                        put("thinking_budget", 0)
//                    })
//                })
//            }
//
//            val body = payload.toString().toRequestBody("application/json".toMediaType())
//            val request = Request.Builder().url(url).post(body).build()
//
//            val networkStart = System.currentTimeMillis()
//            val response = httpClient.newCall(request).execute()
//            val networkDuration = System.currentTimeMillis() - networkStart
//            val rawResponse = response.body?.string().orEmpty()
//
//            Log.i("GeminiTimer", "Response (${networkDuration}ms): $rawResponse")
//
//            if (!response.isSuccessful) {
//                return@withContext Result.failure(Exception("HTTP_${response.code}"))
//            }
//
//            // 3. Parse clean text directly from standard Flash response
//            val jsonResponse = JSONObject(rawResponse)
//            val text = jsonResponse.optJSONArray("candidates")
//                ?.optJSONObject(0)
//                ?.optJSONObject("content")
//                ?.optJSONArray("parts")
//                ?.optJSONObject(0)
//                ?.optString("text")
//                .orEmpty()
//                .trim()
//                .removeSurrounding("\"")
//
//            val totalDuration = System.currentTimeMillis() - totalStart
//            Log.i("GeminiTimer", "=== Total Pipeline: ${totalDuration}ms | Text: '$text' ===")
//
//            Result.success(text)
//        } catch (e: Exception) {
//            Log.e("GeminiTimer", "Transcription failed", e)
//            Result.failure(e)
//        }
//    }


//    // Gemini 3.5 Transcribe only have 25 request per day --> need more
//    suspend fun transcribeAudioWithGemini(audioFile: File): Result<String> = withContext(Dispatchers.IO) {
//        val apiKey = getApiKey()
//        if (apiKey.isBlank()) {
//            return@withContext Result.failure(Exception("API_KEY_MISSING"))
//        }
//
//        val totalStart = System.currentTimeMillis()
//
//        try {
//            // ==========================================
//            // 1. BASE64 AUDIO ENCODING (~2ms)
//            // ==========================================
//            val audioBytes = audioFile.readBytes()
//            val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
//
//            // ==========================================
//            // 2. ACOUSTIC TRANSCRIPTION (gemini-3.5-transcribe, ~1.8s)
//            // ==========================================
//            val transcribeUrl = "$baseUrl/gemini-3.5-transcribe:generateContent?key=$apiKey"
//            val transcribePayload = JSONObject().apply {
//                put("contents", JSONArray().apply {
//                    put(JSONObject().apply {
//                        put("role", "user")
//                        put("parts", JSONArray().apply {
//                            put(JSONObject().apply {
//                                put("inline_data", JSONObject().apply {
//                                    put("mime_type", "audio/mp4")
//                                    put("data", base64Audio)
//                                })
//                            })
//                        })
//                    })
//                })
//                put("generationConfig", JSONObject().apply {
//                    put("temperature", 0.0)
//                })
//            }
//
//            val transcribeBody = transcribePayload.toString().toRequestBody("application/json".toMediaType())
//            val transcribeRequest = Request.Builder().url(transcribeUrl).post(transcribeBody).build()
//
//            val transcribeStart = System.currentTimeMillis()
//            val transcribeResponse = httpClient.newCall(transcribeRequest).execute()
//            val transcribeDuration = System.currentTimeMillis() - transcribeStart
//            val rawTranscribeBody = transcribeResponse.body?.string().orEmpty()
//
//            Log.i("GeminiTimer", "Step 2: 3.5-transcribe responded in ${transcribeDuration}ms")
//
//            if (!transcribeResponse.isSuccessful) {
//                Log.e("GeminiTimer", "3.5-transcribe error: $rawTranscribeBody")
//                return@withContext Result.failure(Exception("HTTP_${transcribeResponse.code}"))
//            }
//
//            val jsonResponse = JSONObject(rawTranscribeBody)
//            val partObj = jsonResponse.optJSONArray("candidates")
//                ?.optJSONObject(0)
//                ?.optJSONObject("content")
//                ?.optJSONArray("parts")
//                ?.optJSONObject(0)
//
//            val rawTranscript = partObj?.optJSONObject("audioTranscription")?.optString("text")
//                ?: partObj?.optString("text")
//                    .orEmpty()
//                    .trim()
//
//            if (rawTranscript.isBlank()) {
//                return@withContext Result.success("")
//            }
//
//            Log.i("GeminiTimer", "Step 3: Raw Transcript: '$rawTranscript'")
//
//            // ==========================================
//            // 3. FAST TEXT CLEANUP PASS (~300ms, NO THINKING DELAY)
//            // Uses gemini-3.5-flash-lite for instant text-only cleanup without 503s
//            // ==========================================
//            val cleanupModel = "gemini-3.5-flash-lite"
//            val cleanupUrl = "$baseUrl/$cleanupModel:generateContent?key=$apiKey"
//
//            val cleanupPrompt = """
//            Convert this raw transcribed speech into a clean, finalized statement.
//            Resolve all self-corrections (e.g. if speaker says "City A, no wait City B", output "City B").
//            Resolve spelling corrections (e.g. "I am Thai, t-h-a-i" -> "I am Thai").
//            Remove all hesitations, stutters, and false starts.
//            Output ONLY the final polished sentence with no quotes or commentary.
//
//            Raw: "$rawTranscript"
//        """.trimIndent()
//
//            val cleanupPayload = JSONObject().apply {
//                put("contents", JSONArray().apply {
//                    put(JSONObject().apply {
//                        put("role", "user")
//                        put("parts", JSONArray().apply {
//                            put(JSONObject().apply { put("text", cleanupPrompt) })
//                        })
//                    })
//                })
//                put("generationConfig", JSONObject().apply {
//                    put("temperature", 0.0)
//                })
//            }
//
//            val cleanupBody = cleanupPayload.toString().toRequestBody("application/json".toMediaType())
//            val cleanupRequest = Request.Builder().url(cleanupUrl).post(cleanupBody).build()
//
//            val cleanupStart = System.currentTimeMillis()
//            var finalCleanText = rawTranscript
//
//            try {
//                val cleanupResponse = httpClient.newCall(cleanupRequest).execute()
//                val cleanupDuration = System.currentTimeMillis() - cleanupStart
//                val rawCleanupBody = cleanupResponse.body?.string().orEmpty()
//
//                if (cleanupResponse.isSuccessful) {
//                    val cleanupJson = JSONObject(rawCleanupBody)
//                    val cleanedText = cleanupJson.optJSONArray("candidates")
//                        ?.optJSONObject(0)
//                        ?.optJSONObject("content")
//                        ?.optJSONArray("parts")
//                        ?.optJSONObject(0)
//                        ?.optString("text")
//                        .orEmpty()
//                        .trim()
//                        .removeSurrounding("\"")
//
//                    if (cleanedText.isNotBlank()) {
//                        finalCleanText = cleanedText
//                    }
//                    Log.i("GeminiTimer", "Step 4: Cleanup responded in ${cleanupDuration}ms: '$finalCleanText'")
//                } else {
//                    Log.w("GeminiTimer", "Cleanup fallback used (HTTP ${cleanupResponse.code})")
//                }
//            } catch (e: Exception) {
//                Log.w("GeminiTimer", "Cleanup failed, using raw transcript: ${e.message}")
//            }
//
//            val totalDuration = System.currentTimeMillis() - totalStart
//            Log.i("GeminiTimer", "=== Total Pipeline: ${totalDuration}ms | Final Text: '$finalCleanText' ===")
//
//            Result.success(finalCleanText)
//        } catch (e: Exception) {
//            Log.e("GeminiTimer", "Transcription pipeline failed", e)
//            Result.failure(e)
//        }
//    }
}