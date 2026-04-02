package marcinlowercase.a.core.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class RequestCodePayload(val email: String)

@Serializable
data class VerifyCodePayload(val email: String, val code: String)

@Serializable
data class AuthResponse(val token: String? = null, val message: String)

object SyncApi {
    // 10.0.2.2 routes to your Mac's localhost from the Android Emulator
    // Use your Mac's IP address if testing on a physical device.
    private const val BASE_URL = "http://40.233.118.232:8080/api/v1"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun requestCode(email: String): Boolean {
        Log.i("marcSync", "requestCode: $email")

        val payload = json.encodeToString(RequestCodePayload(email))
        return makePostRequest("$BASE_URL/auth/request-code", payload) != null
    }

    suspend fun verifyCode(email: String, code: String): AuthResponse? {
        val payload = json.encodeToString(VerifyCodePayload(email, code))
        return makePostRequest("$BASE_URL/auth/verify-code", payload)
    }

    private suspend fun makePostRequest(urlString: String, jsonPayload: String): AuthResponse? {
        Log.i("marcSync", "makePostRequest: $urlString")
        return withContext(Dispatchers.IO) {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                connection.outputStream.use { os ->
                    val input = jsonPayload.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                if (connection.responseCode in 200..299) {
                    val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                    json.decodeFromString<AuthResponse>(responseString)
                } else {
                    null // Request failed (e.g. wrong code)
                }
            } catch (e: Exception) {
                null
            } finally {
                connection.disconnect()
            }
        }
    }
}