package org.chronusartcenter.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.chronusartcenter.model.OscClientConfig
import org.chronusartcenter.model.isIpAddressValid

@Composable
fun OscClient(oscClientConfig: OscClientConfig,
              onIpChanged: (String) -> Unit,
              onPortChanged: (Int) -> Unit,
              imagePath: String?) {
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val (ip, setIp) = remember { mutableStateOf(oscClientConfig.ip) }
        var isIpValid by remember { mutableStateOf(true) }
        val (port, setPort) = remember { mutableStateOf(oscClientConfig.port) }
        var isPortValid by remember { mutableStateOf(true) }

        Text(text = "Osc client ${oscClientConfig.id}")

        Image(
            painter = painterResource(imagePath?:"fallback.jpeg"),
            contentDescription = "",
            modifier = Modifier.size(80.dp))

        OutlinedTextField(
            value = ip,
            onValueChange = {
                setIp(it)
                isIpValid = isIpAddressValid(it)
                if (isIpValid) {
                    onIpChanged(it)
                }
            },
            isError = !isIpValid,
            singleLine = true,
            modifier = Modifier.width(180.dp)
        )
        Text(
            modifier = Modifier.alpha(if (!isIpValid) { 100.0F } else { 0.0F }),
            text = "Invalid IP Address!",
            color = Color.Red
        )

        OutlinedTextField(
            value = "$port",
            onValueChange = {
                try {
                    val tmpPort = it.toInt()
                    setPort(tmpPort)
                    isPortValid = tmpPort in 0..65535
                    if (isPortValid) {
                        onPortChanged(tmpPort)
                    }
                } catch (exception: NumberFormatException) {
                    setPort(0)
                }
            },
            isError = !isPortValid,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(180.dp)
        )
        Text(
            modifier = Modifier.alpha(if (!isPortValid) { 100.0F } else { 0.0F }),
            text = "Invalid Port!",
            color = Color.Red
        )
    }
}