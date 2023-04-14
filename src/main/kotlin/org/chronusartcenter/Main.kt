package org.chronusartcenter

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.chronusartcenter.model.MainWindow

fun main() = application {
    println("hello world")
    MainWindow(
        onCloseRequest = ::exitApplication,
        title = "Unknown",
        state = rememberWindowState(width = 1440.dp, height = 1080.dp)
    )
    println("bye")
}
