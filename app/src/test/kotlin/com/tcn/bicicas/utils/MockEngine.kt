package com.tcn.bicicas.utils

import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

fun MockRequestHandleScope.respondJson(
    json: String,
    code: Int = 200,
) = respond(
    content = json,
    status = HttpStatusCode.fromValue(code),
    headers = headersOf(HttpHeaders.ContentType, "application/json")
)