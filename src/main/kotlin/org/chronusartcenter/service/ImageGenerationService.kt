package org.chronusartcenter.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.logging.log4j.kotlin.logger
import org.chronusartcenter.dotenv
import org.chronusartcenter.text2image.ModelsLabImageClient
import org.chronusartcenter.text2image.OpenAIImageClient
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

class ImageGenerationService {
    private val modelsLabClient: ModelsLabImageClient
    private val openAiClient: OpenAIImageClient

    init {
        val modelsLabApiKey = dotenv["MODELS_LAB_API_KEY"] ?: ""
        modelsLabClient = ModelsLabImageClient(modelsLabApiKey)

        val openaiApiKey = dotenv["OPENAI_API_KEY"] ?: ""
        openAiClient = OpenAIImageClient(openaiApiKey)
    }

    suspend fun generateImage(prompt: String, config: ModelsLabImageClient.Config): String = withContext(Dispatchers.IO) {
        val channel = Channel<Result<String>>()

        modelsLabClient.generateImage(prompt, config) { imageUrl, error ->
            when {
                error != null -> channel.trySend(Result.failure(error))
                imageUrl != null -> channel.trySend(Result.success(imageUrl))
                else -> channel.trySend(Result.failure(IllegalStateException("No image URL generated and no error reported")))
            }
            channel.close()
        }

        channel.receive().getOrThrow()
    }

    companion object {
        suspend fun getBase64FromUrl(url: String): String? = withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .build()

            try {
                val request = Request.Builder()
                    .url(url)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        body
                    } else {
                        throw Exception("HTTP ${response.code}: ${response.message}")
                    }
                }
            } catch (e: IOException) {
                throw Exception("Network error: ${e.message}", e)
            } catch (e: Exception) {
                throw Exception("Unexpected error: ${e.message}, url: $url", e)
            }
        }
    }
}