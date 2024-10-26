package org.chronusartcenter

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.chronusartcenter.component.ModelsLabConfigBox
import org.chronusartcenter.service.ImageGenerationService.Companion.getBase64FromUrl
import org.chronusartcenter.text2image.ModelsLabImageClient
import org.chronusartcenter.text2image.OpenAIImageClient
import org.jetbrains.skia.Image
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

val dotenv = dotenv()

@Composable
fun SpinnerAnimation(
    modifier: Modifier = Modifier.size(100.dp),
) {
    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val painter: Painter = painterResource("tube_spinner.svg")
    Image(
        painter = painter,
        contentDescription = "SVG Image",
        modifier = modifier.rotate(angle)
    )
}

suspend fun loadImageFromUrl(url: String): ImageBitmap? {
    val client = HttpClient(CIO)
    return withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = client.get(url)
            val bytes = response.readBytes()
            Image.makeFromEncoded(bytes).toComposeImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            client.close()
        }
    }
}

fun loadImageFromBase64(base64String: String): ImageBitmap {
    val imageBytes = Base64.getDecoder().decode(base64String)
    return Image.makeFromEncoded(imageBytes).toComposeImageBitmap()
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Test Main Window",
        state = rememberWindowState(width = 1440.dp, height = 1080.dp),
    ) {
        val openaiApiKey = dotenv["OPENAI_API_KEY"] ?: ""
        val modelsLabApiKey = dotenv["MODELS_LAB_API_KEY"] ?: ""

        var promptText by remember { mutableStateOf("") }
        var imageBase64Url by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        var image by remember { mutableStateOf<ImageBitmap?>(null) }

        val modelsLabImageClient = remember { ModelsLabImageClient(modelsLabApiKey) }
        var modelsLabConfig by remember { mutableStateOf(ModelsLabImageClient.Config(modelType = ModelsLabImageClient.ModelType.REALTIME_API)) }

        LaunchedEffect(imageBase64Url) {
            isLoading = true
            if (imageBase64Url.isNotBlank()) {
                val base64String = getBase64FromUrl(imageBase64Url)
                image = loadImageFromBase64(base64String?:"")
            }
            isLoading = false
        }

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Prompt")

                    TextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                    )

                    Button(
                        onClick = {
                            isLoading = true
                            modelsLabImageClient.generateImage(
                                promptText,
                                modelsLabConfig
                            ) { result, error ->
                                isLoading = false

                                error?.run {
                                    println("Error: ${error.message}")
                                } ?: let {
                                    result?.let { value ->
                                        imageBase64Url = value
                                    } ?: run {
                                        println("No image generated")
                                    }
                                }
                            }

                        },
                        enabled = !isLoading
                    ) {
                        Text("Generate")
                    }
                }

                ModelsLabConfigBox(modelsLabConfig,
                    onNegativePromptChange = { modelsLabConfig = modelsLabConfig.copy(negativePrompt = it) },
                    onModelTypeChange = { modelsLabConfig = modelsLabConfig.copy(modelType = it) },
                    onEnhanceTypeChange = { modelsLabConfig = modelsLabConfig.copy(enhanceType = it) },
                    onModelIdChange = { modelsLabConfig = modelsLabConfig.copy(modelId = it)},
                    onWithChange = { modelsLabConfig = modelsLabConfig.copy(width = it)},
                    onHeightChange = { modelsLabConfig = modelsLabConfig.copy(height = it)},
                )
            }

            Box (
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                if (image != null && !isLoading) {
                    Image(
                        bitmap = image!!,
                        contentDescription = "Loaded image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else if (isLoading) {
                    SpinnerAnimation()
                }
            }
        }
    }
}