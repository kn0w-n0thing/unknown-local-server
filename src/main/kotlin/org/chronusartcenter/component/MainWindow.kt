package org.chronusartcenter.model

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import com.alibaba.fastjson2.JSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.logging.log4j.kotlin.logger
import org.chronusartcenter.Context
import org.chronusartcenter.ServiceManager
import org.chronusartcenter.cache.CacheService
import org.chronusartcenter.component.ConnectIndicator
import org.chronusartcenter.component.Console
import org.chronusartcenter.component.OscClient
import org.chronusartcenter.dalle.DalleService
import org.chronusartcenter.news.NewsService
import org.chronusartcenter.osc.OscService

val INITIAL_CONTENT = mutableListOf(
    ".##..##..##..##..##..##..##..##...####...##...##..##..##.\n",
    ".##..##..###.##..##.##...###.##..##..##..##...##..###.##.\n",
    ".##..##..##.###..####....##.###..##..##..##.#.##..##.###.\n",
    ".##..##..##..##..##.##...##..##..##..##..#######..##..##.\n",
    "..####...##..##..##..##..##..##...####....##.##...##..##.\n",
    "\n"
)

@Composable
fun MainWindow(
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(),
    title: String = "Untitled",
    context: Context
) {
    Window(
        onCloseRequest = onCloseRequest,
        title = title,
        state = state
    ) {
        val coroutineScope = rememberCoroutineScope()

        val dalleService = DalleService(context)
        val (dalleStatus, setDalleStatus) = remember { mutableStateOf(dalleService.check()) }
        val (isProcessing, setProcessing) = remember { mutableStateOf(false) }

        val (requestInterval, setRequestInterval) = remember { mutableStateOf(3) }

        val consoleBuffer = ConsoleBuffer(INITIAL_CONTENT)
        val (console, setConsole) = remember { mutableStateOf(consoleBuffer.getContent(), neverEqualPolicy()) }

        val oscService = remember {ServiceManager.getInstance().getService(ServiceManager.SERVICE_TYPE.OSC_SERVICE) as OscService}
        val oscClientConfigs = remember { oscService.readClientConfig(context) }

        val newService = remember { NewsService(context) }
        var cacheService = remember { CacheService(context) }

        val logger = remember { logger() }

        context.addGuiConsoleListener { message ->
            consoleBuffer.append(message)
            setConsole(consoleBuffer.getContent())
        }

        MaterialTheme {
            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(10.dp)) {
                Row(Modifier.wrapContentSize().padding(10.dp), Arrangement.spacedBy(5.dp)) {
                    ConnectIndicator(
                        name = "local-server",
                        isConnected = true
                    )
                    ConnectIndicator(
                        modifier = Modifier.clickable (
                            onClick = { setDalleStatus(dalleService.check()) }
                        ),
                        name = "dalle-mini-server",
                        isConnected = dalleStatus
                    )
                }

                Console(Modifier.width(720.dp).height(540.dp), console)

                Row(
                    Modifier.wrapContentSize().padding(10.dp),
                    Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Request interval: ")
                    OutlinedTextField(
                        value = requestInterval.toString(),
                        onValueChange = {
                            // TODO: to be modified
                            if (it.isNotBlank()) {
                                try {
                                    setRequestInterval(it.toInt())
                                } catch (e: NumberFormatException) {
                                    // Do nothing
                                }
                            } else {
                                setRequestInterval(0)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(100.dp)
                    )
                    Text("hour(s).")
                    Button(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                setProcessing(true)
                                val headlines = newService.translateHeadlines(newService.fetchHeadlines())

                                if (headlines == null || headlines.size == 0) {
                                    val message = "Failed to get headlines!"
                                    consoleBuffer.append(message)
                                    setConsole(consoleBuffer.getContent())
                                    logger.info(message)
                                    setProcessing(false)
                                    return@launch
                                }

                                headlines.forEachIndexed{ index, headlineModel ->
                                    val image = dalleService.generateImage(headlineModel.translation, 1)
                                    headlineModel.index = index
                                    cacheService.saveImage(index.toString() + "." + image.right, image.left.get(0))
                                    cacheService.saveHeadline(headlineModel)
                                }

                                val message = "Processing completed."
                                consoleBuffer.append(message)
                                setConsole(consoleBuffer.getContent())
                                logger.info(message)
                                setProcessing(false)
                            }
                        },
                        enabled = !isProcessing && dalleStatus,
                    ) {
                        Text("Start")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    oscClientConfigs?.forEach{ oscClientConfig -> OscClient(oscClientConfig) }
                }


                Button(onClick = {
                    logger.info(oscClientConfigs.fold(StringBuilder()) {
                        str: StringBuilder,
                        oscClientConfig: OscClientConfig -> str.append(JSON.toJSONString(oscClientConfig))
                    })
                    consoleBuffer.append("Save osc clients' config.\n\n")
                    setConsole(consoleBuffer.getContent())
                }) {
                    Text("Save")
                }
            }

        }
    }
}