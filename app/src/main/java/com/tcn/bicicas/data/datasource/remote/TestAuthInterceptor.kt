package com.tcn.bicicas.data.datasource.remote

import okhttp3.*

class TestAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url().encodedPath() == "/oauth/token"
            && request.url().queryParameter("username") == "test"
            && request.url().queryParameter("password") == "test"
        ) {
            return request.buildResponse("""{"access_token": "test_token"}""")
        } else if (request.url().encodedPath() == "/dashboard"
            && request.header("Authorization") == "Bearer test_token"
        ) {
            return request.buildResponse("""{"dashboard":{"twofactor":{"user":"0000","secret":"TESTTESTTESTTEST"}}}""")
        }
        return chain.proceed(request)
    }

    private fun Request.buildResponse(json: String): Response {
        return Response.Builder()
            .protocol(Protocol.HTTP_1_1)
            .request(this)
            .body(ResponseBody.create(MediaType.get("application/json"), json))
            .message("Ok")
            .code(200)
            .build()
    }

}