package org.chronusartcenter.model

import androidx.compose.runtime.Immutable

@Immutable
data class OscClientConfig @Throws(IllegalArgumentException::class)
constructor(val id: Int, val ip: String, val port: Int)