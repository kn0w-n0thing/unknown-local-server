package org.chronusartcenter.text2image

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

fun createOkHttpClientWithTimeouts(
    connectTimeout: Long = 10,
    readTimeout: Long = 30,
    writeTimeout: Long = 30,
    callTimeout: Long = 60
): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(connectTimeout, TimeUnit.SECONDS)
        .readTimeout(readTimeout, TimeUnit.SECONDS)
        .writeTimeout(writeTimeout, TimeUnit.SECONDS)
        .callTimeout(callTimeout, TimeUnit.SECONDS)
        .build()
}