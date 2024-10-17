package org.chronusartcenter.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import org.chronusartcenter.dotenv
import org.chronusartcenter.text2image.ModelsLabImageClient
import org.chronusartcenter.text2image.OpenAIImageClient
import java.net.URL
import java.util.*

class ImageGenerationService {
    private val modelsLabClient: ModelsLabImageClient
    private val openAiClient: OpenAIImageClient

    init {
        val modelsLabApiKey = dotenv["MODELS_LAB_API_KEY"] ?: ""
        modelsLabClient = ModelsLabImageClient(modelsLabApiKey)

        val openaiApiKey = dotenv["OPENAI_API_KEY"] ?: ""
        openAiClient = OpenAIImageClient(openaiApiKey)
    }

    suspend fun generateImage(prompt: String): String = withContext(Dispatchers.IO) {
        val channel = Channel<Result<String>>()

        modelsLabClient.generateImage(prompt, ModelsLabImageClient.ApiType.REALTIME_API) { imageUrl, error ->
            when {
                error != null -> channel.trySend(Result.failure(error))
                imageUrl != null -> channel.trySend(Result.success(imageUrl))
                else -> channel.trySend(Result.failure(IllegalStateException("No image URL generated and no error reported")))
            }
            channel.close()
        }

        channel.receive().getOrThrow()
    }

    suspend fun getBase64FromImageUrl(url: String): String = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()

            val bytes = inputStream.readBytes()
            inputStream.close()

            Base64.getEncoder().encodeToString(bytes)
        } catch (e: Exception) {
            throw Exception("Failed to fetch image from URL: ${e.message}")
        }
    }
}