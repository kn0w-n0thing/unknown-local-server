package org.chronusartcenter.text2image

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException


class ModelsLabImageClient(private val apiKey: String) {
    val COMMUNITY_API_URL = "https://modelslab.com/api/v6/images/text2img"
    val REALTIME_API_URL = "https://modelslab.com/api/v6/realtime/text2img"

    enum class ApiType {
        COMMUNITY_API,
        REALTIME_API,
    }

    @Serializable
    data class ImageGenerationRequest(
        val key: String,
        val prompt: String,
    )

    @Serializable
    data class ImageGenerationResponse(
        val status: String? = null,
        val generationTime: Double? = null,
        val id: Long? = null,
        val output: List<String> = emptyList(),
        val proxy_links: List<String> = emptyList(),
        val meta: Meta? = null
    )

    @Serializable
    data class Meta(
        val base64: String,
        val enhance_prompt: String,
        val enhance_style: String?,
        val file_prefix: String,
        val guidance_scale: Int,
        val height: Int,
        val instant_response: String,
        val n_samples: Int,
        val negative_prompt: String,
        val opacity: Double,
        val outdir: String,
        val padding_down: Int,
        val padding_right: Int,
        val pag_scale: Double,
        val prompt: String,
        val rescale: String,
        val safety_checker: String,
        val safety_checker_type: String,
        val scale_down: Int,
        val seed: Long,
        val temp: String,
        val watermark: String,
        val width: Int
    )

    private val client = createOkHttpClientWithTimeouts()
    private val json = Json { ignoreUnknownKeys = true }


    fun generateImage(prompt: String, apiType: ApiType, callback: (String?, Exception?) -> Unit) {
        val requestBody = ImageGenerationRequest(
            key = apiKey,
            prompt = prompt,
        )

        val requestBodyJson = json.encodeToString(requestBody)

        val url = when (apiType) {
            ApiType.REALTIME_API -> REALTIME_API_URL
            ApiType.COMMUNITY_API -> COMMUNITY_API_URL
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
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
                            val imageUrl = imageResponse.output.firstOrNull()
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