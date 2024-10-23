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
import org.chronusartcenter.component.ComboBox
import org.chronusartcenter.component.ModelsLabConfigBox
import org.chronusartcenter.text2image.ModelsLabImageClient
import org.chronusartcenter.text2image.OpenAIImageClient
import org.jetbrains.skia.Image
import java.util.*

val dotenv = dotenv()

enum class ModelType {
    DALL_E_3,
    MODELS_LAB,
}

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
        var imageUrl by remember { mutableStateOf("https://oaidalleapiprodscus.blob.core.windows.net/private/org-jsVRxHKpVzDRuxdrNICQhXGS/user-6kxuYTQmwOkhch4yitiTpETM/img-NSbMlGz9T0b2P82jMCpQ51aL.png?st=2024-10-12T06%3A56%3A24Z&se=2024-10-12T08%3A56%3A24Z&sp=r&sv=2024-08-04&sr=b&rscd=inline&rsct=image/png&skoid=d505667d-d6c1-4a0a-bac7-5c84a87759f8&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2024-10-11T23%3A20%3A01Z&ske=2024-10-12T23%3A20%3A01Z&sks=b&skv=2024-08-04&sig=nB8UuonhRLpcBW12M4WXdjXydmNhf9ROInaUSdmJhsU%3D") }
        var isLoading by remember { mutableStateOf(false) }
        var currentModel by remember { mutableStateOf(ModelType.DALL_E_3) }

        var image by remember { mutableStateOf<ImageBitmap?>(null) }

        val openAIImageClient = remember { OpenAIImageClient(openaiApiKey) }
        val modelsLabImageClient = remember { ModelsLabImageClient(modelsLabApiKey) }
        var modelsLabConfig by remember { mutableStateOf(ModelsLabImageClient.Config(modelType = ModelsLabImageClient.ModelType.REALTIME_API)) }

        val apiList = listOf("DallE 3", "Models Lab")

        LaunchedEffect(imageUrl) {
            isLoading = true
            image = loadImageFromUrl(imageUrl)
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
                            when (currentModel) {
                                ModelType.DALL_E_3 -> {
                                    openAIImageClient.generateImage(promptText) { result, error ->
                                        isLoading = false

                                        error?.run {
                                            println("Error: ${error.message}")
                                        } ?: let {
                                            result?.let { value ->
                                                imageUrl = value
                                            } ?: run {
                                                println("No image generated")
                                            }
                                        }
                                    }
                                }

                                ModelType.MODELS_LAB -> {
                                    modelsLabImageClient.generateImage(
                                        promptText,
                                        modelsLabConfig
                                    ) { result, error ->
                                        isLoading = false

                                        error?.run {
                                            println("Error: ${error.message}")
                                        } ?: let {
                                            result?.let { value ->
                                                imageUrl = value
                                            } ?: run {
                                                println("No image generated")
                                            }
                                        }
                                    }
                                }
                            }

                        },
                        enabled = !isLoading
                    ) {
                        Text("Generate")
                    }
                }

                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("API type")

                    ComboBox(apiList) { index ->
                        currentModel = ModelType.values()[index]
                    }
                }

                if (currentModel == ModelType.MODELS_LAB) {
                    ModelsLabConfigBox(modelsLabConfig,
                        onNegativePromptChange = { modelsLabConfig = modelsLabConfig.copy(negativePrompt = it) },
                        onModelTypeChange = { modelsLabConfig = modelsLabConfig.copy(modelType = it) },
                        onEnhanceTypeChange = { modelsLabConfig = modelsLabConfig.copy(enhanceType = it) },
                        onModelIdChange = { modelsLabConfig = modelsLabConfig.copy(modelId = it)},
                        onWithChange = { modelsLabConfig = modelsLabConfig.copy(width = it)},
                        onHeightChange = { modelsLabConfig = modelsLabConfig.copy(height = it)},
                    )
                }
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