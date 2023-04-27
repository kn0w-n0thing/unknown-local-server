package org.chronusartcenter.model

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.logging.log4j.kotlin.logger
import org.chronusartcenter.Context
import org.chronusartcenter.Context.GuiListener
import org.chronusartcenter.ServiceManager
import org.chronusartcenter.cache.CacheService
import org.chronusartcenter.component.ConnectIndicator
import org.chronusartcenter.component.Console
import org.chronusartcenter.component.OscClient
import org.chronusartcenter.dalle.DalleService
import org.chronusartcenter.news.NewsService
import org.chronusartcenter.osc.OscService
import java.util.*
import java.util.concurrent.TimeUnit

val INITIAL_CONTENT = mutableListOf(
    ".##..##..##..##..##..##..##..##...####...##...##..##..##.\n",
    ".##..##..###.##..##.##...###.##..##..##..##...##..###.##.\n",
    ".##..##..##.###..####....##.###..##..##..##.#.##..##.###.\n",
    ".##..##..##..##..##.##...##..##..##..##..#######..##..##.\n",
    "..####...##..##..##..##..##..##...####....##.##...##..##.\n",
    "\n"
)

@OptIn(ExperimentalMaterialApi::class)
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

        val (requestIntervalMinute, setRequestIntervalMinute) = remember { mutableStateOf(180) }

        val consoleBuffer = ConsoleBuffer(INITIAL_CONTENT)
        val (console, setConsole) = remember { mutableStateOf(consoleBuffer.getContent(), neverEqualPolicy()) }
        fun consolePrintln(message: String) {
            consoleBuffer.append(message + "\n")
            setConsole(consoleBuffer.getContent())
        }

        val oscService =
            remember { ServiceManager.getInstance().getService(ServiceManager.SERVICE_TYPE.OSC_SERVICE) as OscService }
        val oscClientConfigs by remember { mutableStateOf(oscService.readClientConfig(context)) }
        val (oscImagePathMap, setOscImagePathMap) = remember {
            mutableStateOf(
                mutableMapOf<Int, String>(),
                neverEqualPolicy()
            )
        }

        val newService = remember { NewsService(context) }
        val cacheService = remember { CacheService(context) }

        var showShutdownDialog by remember { mutableStateOf(false) }
        if (showShutdownDialog) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = {
                    TextButton(onClick = {
                        oscService.shutDownOscClients()
                        consolePrintln("Shutdown all the OSC clients.")
                        showShutdownDialog = false
                    }) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showShutdownDialog = false
                    }) {
                        Text(text = "Cancel")
                    }
                },
                title = { Text(text = "Please confirm") },
                text = { Text(text = "Are you sure to shutdown all the OSC clients?") },
                modifier = Modifier.width(500.dp).padding(15.dp)
            )
        }

        val logger = remember { logger() }

        context.addGuiListener(
            /* listener = */ object : GuiListener {
                override fun onMessage(message: String) {
                    consoleBuffer.append(message)
                    setConsole(consoleBuffer.getContent())
                }

                override fun onOscImage(oscId: Int, imagePath: String) {
                    oscImagePathMap[oscId] = imagePath
                    setOscImagePathMap(oscImagePathMap)
                }
            })

        fun requestForNews() {
            if (isProcessing) run {
                val message = "It's in the process of requesting, please try it later."
                logger.info(message)
                consoleBuffer.append(message)
                setConsole(consoleBuffer.getContent())
                return
            }

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

                headlines.forEachIndexed generateImages@{ index, headlineModel ->
                    val image = dalleService.generateImage(headlineModel.translation, 1) ?: return@generateImages
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
        }

        var requestTimerOn = remember { false }
        var requestTimer = remember { Timer() }

        MaterialTheme {
            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(10.dp)) {
                Row(Modifier.wrapContentSize().padding(10.dp), Arrangement.spacedBy(5.dp)) {
                    ConnectIndicator(
                        name = "local-server",
                        isConnected = true
                    )
                    ConnectIndicator(
                        modifier = Modifier.clickable(
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
                    TextField(
                        value = requestIntervalMinute.toString(),
                        onValueChange = {
                            // TODO: to be modified
                            if (it.isNotBlank()) {
                                try {
                                    setRequestIntervalMinute(it.toInt())
                                } catch (e: NumberFormatException) {
                                    // Do nothing
                                }
                            } else {
                                setRequestIntervalMinute(0)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(100.dp).padding(3.dp)
                    )
                    Text("minute(s).")
                    Button(
                        onClick = {
                            if (requestTimerOn) {
                                requestTimer.cancel()
                                requestTimer.purge()
                                requestTimer = Timer()
                            }

                            requestTimer.schedule(
                                object : TimerTask() {
                                    override fun run() {
                                        requestForNews()
                                    }
                                },
                                0,
                                TimeUnit.MINUTES.toMillis(requestIntervalMinute.toLong())
                            )
                            requestTimerOn = true
                        },
                        enabled = !isProcessing && dalleStatus,
                    ) {
                        Text("Start")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    oscClientConfigs?.forEach { oscClientConfig ->
                        OscClient(
                            oscClientConfig = oscClientConfig,
                            onIpChanged = { ip ->
                                oscClientConfig.ip = ip
                            },
                            onPortChanged = { port ->
                                oscClientConfig.port = port
                            },
                            imagePath = oscImagePathMap.getOrDefault(oscClientConfig.id, null)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        oscService.saveClientConfig(context, oscClientConfigs)
                        consoleBuffer.append("Save osc client config and restart the all the clients.\n")
                        setConsole(consoleBuffer.getContent())
                    }) {
                        Text("Save")
                    }

                    Button(onClick = {
                        showShutdownDialog = true
                    }) {
                        Text("Shutdown")
                    }
                }
            }

        }
    }
}