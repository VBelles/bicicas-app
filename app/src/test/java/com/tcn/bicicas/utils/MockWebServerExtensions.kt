package com.tcn.bicicas.utils

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.File

fun MockWebServer.enqueueJson(path: String, code: Int = 200) {
    val file = File(javaClass.getResource("/$path".replace("//", "/"))!!.path)
    val response = MockResponse()
        .setBody(file.readText())
        .setResponseCode(code)
    enqueue(response)
}