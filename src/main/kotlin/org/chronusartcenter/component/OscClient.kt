package org.chronusartcenter.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.chronusartcenter.model.OscClientConfig

@Composable
fun OscClient(oscClientConfig: OscClientConfig) {
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val (ip, setIp) = remember{ mutableStateOf(oscClientConfig.ip) }
        val (port, setPort) = remember { mutableStateOf(oscClientConfig.port) }
//        val oscClientConfig = remember { mutableStateOf(oscClientConfig) }
//        Image()
        Text(text = "Osc client ${oscClientConfig.id}")
        OutlinedTextField(
            value = ip,
            onValueChange = { setIp(it) },
            modifier = Modifier.width(180.dp)
        )
        OutlinedTextField(
            value = "$port",
            onValueChange = {setPort(it.toInt())},
            modifier = Modifier.width(180.dp)
        )
    }
}