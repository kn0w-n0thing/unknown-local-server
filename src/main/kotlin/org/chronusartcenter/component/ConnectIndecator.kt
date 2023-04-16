package org.chronusartcenter.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ConnectIndicator(modifier: Modifier = Modifier, name: String, isConnected: Boolean) {
    Row(
        modifier = modifier.padding(5.dp)
            .wrapContentSize(Alignment.Center),
        Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = modifier
                .size(size = 5.dp)
                .padding(2.dp),
        ) {
            drawCircle(
                color = if (isConnected) Color.Green else Color.Red,
                radius = 5.dp.toPx()
            )
        }

        Text(text = name + ": " + if (isConnected) "connected." else "disconnected.")
    }
}