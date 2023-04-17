package org.chronusartcenter.model

fun isIpAddressValid(address: String): Boolean {
    val regex =
        "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|localhost\$".toRegex()
    return regex.matches(input = address)
}

data class OscClientConfig @Throws(IllegalArgumentException::class)
constructor(val id: Int, var ip: String, var port: Int) {

    init {
        if (port < 0 || port > 65535) {
            throw IllegalArgumentException("Socket port ranges [0, 65535], but got $port")
        }

        if (!isIpAddressValid(address = ip)) {
            throw IllegalArgumentException("Invalid ip address [$ip].")
        }
    }

}