package marcinlowercase.a.core.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import marcinlowercase.a.core.data_class.SyncPayload
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class RequestCodePayload(val email: String)

@Serializable
data class VerifyCodePayload(val email: String, val code: String)

@Serializable
data class AuthResponse(val token: String? = null, val message: String)

object SyncApi {
    private const val BASE_URL = "https://browser-sync.oo1.studio/api/v1"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun requestCode(email: String): Boolean {
        val payload = json.encodeToString(RequestCodePayload(email))
        return makeRequest<AuthResponse>("$BASE_URL/auth/request-code", "POST", payload) != null
    }

    suspend fun verifyCode(email: String, code: String): AuthResponse? {
        val payload = json.encodeToString(VerifyCodePayload(email, code))
        return makeRequest<AuthResponse>("$BASE_URL/auth/verify-code", "POST", payload)
    }

    suspend fun pushSyncData(payload: SyncPayload, token: String): Boolean {
        val jsonPayload = json.encodeToString(payload)
        return makeRequest<AuthResponse>("$BASE_URL/sync/push", "POST", jsonPayload, token) != null
    }

    suspend fun pullSyncData(token: String): SyncPayload? {
        return makeRequest<SyncPayload>("$BASE_URL/sync/pull", "GET", null, token)
    }

    suspend fun deleteAccount(token: String): Boolean {
        // We bypass makeRequest and JSON parsing here because DELETE requests
        // can cause stream parsing exceptions. We only care if the HTTP code is 200 OK!
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/sync/account")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "DELETE"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                connection.disconnect()

                // If the server returns 200, 201, or 204, it was successful!
                responseCode in 200..299
            } catch (e: Exception) {
                false
            }
        }
    }

    // 100% Native Android HTTP Request Engine
    private suspend inline fun <reified T> makeRequest(
        urlString: String,
        method: String,
        jsonPayload: String? = null,
        token: String? = null
    ): T? {
        return withContext(Dispatchers.IO) {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = method
                connection.setRequestProperty("Accept", "application/json")

                if (token != null) {
                    connection.setRequestProperty("Authorization", "Bearer $token")
                }

                if (jsonPayload != null && (method == "POST" || method == "PUT")) {
                    connection.setRequestProperty("Content-Type", "application/json; utf-8")
                    connection.doOutput = true
                    connection.outputStream.use { os ->
                        val input = jsonPayload.toByteArray(Charsets.UTF_8)
                        os.write(input, 0, input.size)
                    }
                } else {
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                }

                if (connection.responseCode in 200..299) {
                    val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                    json.decodeFromString<T>(responseString)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            } finally {
                connection.disconnect()
            }
        }
    }
}