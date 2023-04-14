package org.chronusartcenter.model

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import org.chronusartcenter.component.ConnectIndicator
import org.chronusartcenter.component.Console
import org.chronusartcenter.component.OscClient

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
    title: String = "Untitled"
) {
    Window(
        onCloseRequest = onCloseRequest,
        title = title,
        state = state
    ) {
        val (dalleStatus, setDalleStatus) = remember { mutableStateOf(false) }
        val (requestInterval, setRequestInterval) = remember { mutableStateOf(3) }

        val consoleBuffer = ConsoleBuffer(INITIAL_CONTENT)
        val (console, setConsole) = remember { mutableStateOf(consoleBuffer.getContent(), neverEqualPolicy()) }

        MaterialTheme {
            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(10.dp)) {
                Row(Modifier.wrapContentSize().padding(10.dp), Arrangement.spacedBy(5.dp)) {
                    ConnectIndicator(
                        name = "local-server",
                        isConnected = true
                    )
                    ConnectIndicator(
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
                        onClick = {},
                        enabled = dalleStatus,
                    ) {
                        Text("Start")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    for (i in 1..6) {
                        OscClient(i, "127.0.0.1", 5001)
                    }
                }


                Button(onClick = {
                    consoleBuffer.append("Hello world\n\n")
                    setConsole(consoleBuffer.getContent())
                }) {
                    Text("Save")
                }
            }

        }
    }
}