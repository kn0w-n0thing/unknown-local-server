package org.chronusartcenter

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.launch
import org.chronusartcenter.ServiceManager.SERVICE_TYPE
import org.chronusartcenter.model.MainWindow
import org.chronusartcenter.osc.OscService

fun main() = application {
    val context = ServiceManager.getInstance().context

    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch {
        val oscService = ServiceManager.getInstance().getService(SERVICE_TYPE.OSC_SERVICE) as OscService
        oscService.start()
    }

    MainWindow(
        onCloseRequest = ::exitApplication,
        title = "Unknown",
        state = rememberWindowState(width = 1440.dp, height = 1080.dp),
        context = context
    )
}

