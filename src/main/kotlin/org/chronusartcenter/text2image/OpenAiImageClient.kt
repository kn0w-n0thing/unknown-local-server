package org.chronusartcenter.text2image

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.concurrent.TimeUnit

@Serializable
data class ImageGenerationRequest(
    val model: String,
    val prompt: String,
    val n: Int,
    val size: String
)

@Serializable
data class ImageGenerationResponse(
    val created: Long,
    val data: List<ImageData>
)

@Serializable
data class ImageData(
//    val revisedPrompt: String,
    val url: String
)

fun createOkHttpClientWithTimeouts(
    connectTimeout: Long = 10,
    readTimeout: Long = 30,
    writeTimeout: Long = 30,
    callTimeout: Long = 60
): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(connectTimeout, TimeUnit.SECONDS)
        .readTimeout(readTimeout, TimeUnit.SECONDS)
        .writeTimeout(writeTimeout, TimeUnit.SECONDS)
        .callTimeout(callTimeout, TimeUnit.SECONDS)
        .build()
}

class OpenAIImageClient(private val apiKey: String) {
    private val client = createOkHttpClientWithTimeouts()
    private val json = Json { ignoreUnknownKeys = true }

    fun generateImage(prompt: String, callback: (String?, Exception?) -> Unit) {
        val requestBody = ImageGenerationRequest(
            model = "dall-e-3",
            prompt = prompt,
            n = 1,
            size = "1024x1024"
        )

        val requestBodyJson = json.encodeToString(requestBody)

        val request = Request.Builder()
            .url("https://api.openai.com/v1/images/generations")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(null, IOException("Unexpected code $response"))
                    } else {
                        val responseBody = response.body?.string()
                        try {
                            val imageResponse = json.decodeFromString<ImageGenerationResponse>(responseBody ?: "")
                            val imageUrl = imageResponse.data.firstOrNull()?.url
                            callback(imageUrl, null)
                        } catch (e: Exception) {
                            callback(null, e)
                        }
                    }
                }
            }
        })
    }
}