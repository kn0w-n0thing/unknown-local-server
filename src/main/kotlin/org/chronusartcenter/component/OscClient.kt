package org.chronusartcenter.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OscClient(id: Int, ip: String, port: Int) {
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        Image()
        Text(text = "Osc client $id")
        OutlinedTextField(
            value = ip,
            onValueChange = {},
            modifier = Modifier.width(180.dp)
        )
        OutlinedTextField(
            value = "$port",
            onValueChange = {},
            modifier = Modifier.width(180.dp)
        )
    }
}