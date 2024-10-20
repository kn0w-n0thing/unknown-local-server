package org.chronusartcenter.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.unit.dp
import org.chronusartcenter.text2image.ModelsLabImageClient

@Composable
fun ModelsLabConfigBox(
    config: ModelsLabImageClient.Config,
    onNegativePromptChange: (String) -> Unit,
    onModelTypeChange: (ModelsLabImageClient.ModelType) -> Unit,
    onEnhanceTypeChange: (ModelsLabImageClient.EnhanceType?) -> Unit,
    onModelIdChange: (ModelsLabImageClient.ModelId?) -> Unit
) {
    var modelType by remember { mutableStateOf(ModelsLabImageClient.ModelType.COMMUNITY_API) }
    var negativePrompt by remember { mutableStateOf(config.negativePrompt) }
    var enhanceType by remember { mutableStateOf<ModelsLabImageClient.EnhanceType?>(null) }
    var modleld by remember { mutableStateOf<ModelsLabImageClient.ModelId?>(ModelsLabImageClient.ModelId.FLUX) }

    Box {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = CenterVertically) {
                Text("Model Type")

                val list = ModelsLabImageClient.ModelType.values().map { it.name }
                ComboBox(list) {
                    modelType = ModelsLabImageClient.ModelType.fromOrdinal(it) ?: ModelsLabImageClient.ModelType.COMMUNITY_API
                    if (modelType != ModelsLabImageClient.ModelType.COMMUNITY_API) {
                        modleld = null
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
                        modleld =
                            ModelsLabImageClient.ModelId.fromOrdinal(it) ?: ModelsLabImageClient.ModelId.FLUX
                        onModelIdChange(modleld)
                    }
                }
            }

        }
    }
}