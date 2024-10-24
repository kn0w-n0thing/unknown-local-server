package org.chronusartcenter.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ComboBox(items: List<String>, selectId: Int = 0, onSelect: (index: Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(selectId) }

    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(Modifier.height(8.dp))
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(items[selectedIndex])
                Spacer(Modifier.width(8.dp))
                if (!expanded) {
                    Image(
                        painter = painterResource("down_arrow.svg"),
                        contentDescription = "Description of your image",
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Image(
                        painter = painterResource("up_arrow.svg"),
                        contentDescription = "Description of your image",
                        modifier = Modifier.size(16.dp)
                    )
                }

            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                items.forEachIndexed { index, item ->
                    DropdownMenuItem(onClick = {
                        selectedIndex = index
                        expanded = false
                        onSelect(index)
                    }) {
                        Text(item)
                    }
                }
            }
        }
    }
}