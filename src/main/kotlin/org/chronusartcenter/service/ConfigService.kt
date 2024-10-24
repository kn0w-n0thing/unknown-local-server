package org.chronusartcenter.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.apache.logging.log4j.kotlin.logger
import org.chronusartcenter.text2image.ModelsLabImageClient
import java.io.File

class ConfigService {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val log = logger()

    fun readModelsLabConfig(file: File): ModelsLabImageClient.Config? {
        return try {
            val jsonString = file.readText()
            val rootObject = json.parseToJsonElement(jsonString).jsonObject
            rootObject["modelsLabConfig"]?.let {
                json.decodeFromJsonElement<ModelsLabImageClient.Config>(it)
            }
        } catch (e: Exception) {
            log.error("Error reading config: ${e.message}")
            null
        }
    }

    fun writeModelsLabConfig(file: File, config: ModelsLabImageClient.Config) {
        try {
            val rootObject = if (file.exists()) {
                json.parseToJsonElement(file.readText()).jsonObject
            } else {
                JsonObject(emptyMap())
            }

            val updatedObject = JsonObject(rootObject + mapOf(
                "modelsLabConfig" to json.encodeToJsonElement(config)
            ))

            file.writeText(json.encodeToString(updatedObject))
        } catch (e: Exception) {
            log.error("Error writing config: ${e.message}")
        }
    }
}
