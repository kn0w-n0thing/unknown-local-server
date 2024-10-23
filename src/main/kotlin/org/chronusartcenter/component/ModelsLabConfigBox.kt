package org.chronusartcenter.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.chronusartcenter.text2image.ModelsLabImageClient

@Composable
fun IntegerTextField(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    val textState = remember(value) {
        mutableStateOf(if (value == 0) "" else value.toString())
    }

    TextField(
        value = textState.value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\$"))) {
                textState.value = newValue
                newValue.toIntOrNull()?.let { onValueChange(it) } ?: onValueChange(0)
            }
        },
        modifier = modifier,
        label = label,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        isError = isError,
        enabled = enabled
    )
}

@Composable
fun ModelsLabConfigBox(
    config: ModelsLabImageClient.Config,
    onNegativePromptChange: (String) -> Unit,
    onModelTypeChange: (ModelsLabImageClient.ModelType) -> Unit,
    onEnhanceTypeChange: (ModelsLabImageClient.EnhanceType?) -> Unit,
    onModelIdChange: (ModelsLabImageClient.ModelId?) -> Unit,
    onWithChange: (Int) -> Unit,
    onHeightChange: (Int) -> Unit,
    onOkClick: (() -> Unit) ? = null,
    modifier: Modifier = Modifier
) {
    var width by remember { mutableStateOf(1024) }
    var height by remember { mutableStateOf(1024) }
    var modelType by remember { mutableStateOf(ModelsLabImageClient.ModelType.COMMUNITY_API) }
    var negativePrompt by remember { mutableStateOf(config.negativePrompt) }
    var enhanceType by remember { mutableStateOf<ModelsLabImageClient.EnhanceType?>(null) }
    var modelId by remember { mutableStateOf<ModelsLabImageClient.ModelId?>(ModelsLabImageClient.ModelId.FLUX) }

    Box(modifier) {
        Column {
            Row (horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = CenterVertically) {
                Text(modifier = Modifier.width(120.dp), text = "Width: ")
                IntegerTextField(
                    modifier = Modifier.width(120.dp),
                    value = width,
                    onValueChange = {
                        width = it
                        onWithChange(width)
                    },
                )

                Text(modifier = Modifier.width(120.dp), text = "Height: ")
                IntegerTextField(
                    modifier = Modifier.width(120.dp),
                    value = height,
                    onValueChange = {
                        height = it
                        onHeightChange(height)
                    },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = CenterVertically) {
                Text("Model Type")

                val list = ModelsLabImageClient.ModelType.values().map { it.name }
                ComboBox(list) {
                    modelType = ModelsLabImageClient.ModelType.fromOrdinal(it) ?: ModelsLabImageClient.ModelType.COMMUNITY_API
                    if (modelType != ModelsLabImageClient.ModelType.COMMUNITY_API) {
                        modelId = null
                        onModelIdChange(null)
                    }
                    onModelTypeChange(modelType)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = CenterVertically) {
                Text("Negative Prompt")

                TextField(
                    value = negativePrompt ?: "",
                    onValueChange = {
                        negativePrompt = it
                        onNegativePromptChange(it)
                    },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = CenterVertically) {
                Text("Enhance prompt")

                val onOff = listOf("OFF", "ON")
                ComboBox(onOff) {
                    when (it) {
                        0 -> enhanceType = null

                        1 -> enhanceType = ModelsLabImageClient.EnhanceType.ENHANCE
                    }
                }
            }

            if (enhanceType != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = CenterVertically) {
                    Text("Enhance type")

                    val list = ModelsLabImageClient.EnhanceType.values().map { it.name }
                    ComboBox(list) {
                        enhanceType =
                            ModelsLabImageClient.EnhanceType.fromOrdinal(it) ?: ModelsLabImageClient.EnhanceType.ENHANCE
                        onEnhanceTypeChange(enhanceType)
                    }
                }
            }

            if (modelType == ModelsLabImageClient.ModelType.COMMUNITY_API) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = CenterVertically) {
                    Text("Model ID")

                    val list = ModelsLabImageClient.ModelId.values().map { it.name }
                    ComboBox(list) {
                        modelId =
                            ModelsLabImageClient.ModelId.fromOrdinal(it) ?: ModelsLabImageClient.ModelId.FLUX
                        onModelIdChange(modelId)
                    }
                }
            }

            Button(onClick = {
                onOkClick?.invoke()
            }) {
                Text("OK")
            }

        }
    }
}