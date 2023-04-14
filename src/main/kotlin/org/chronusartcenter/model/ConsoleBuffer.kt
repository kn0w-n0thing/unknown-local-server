package org.chronusartcenter.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

class ConsoleBuffer(initial: MutableList<String>) {
    private var content: MutableState<MutableList<String>>

    init {
        content = mutableStateOf(initial)
    }

    fun append(content: String) {
        this.content.value.add(content)
    }

    fun getContent(): List<String> {
        return content.value
    }
}