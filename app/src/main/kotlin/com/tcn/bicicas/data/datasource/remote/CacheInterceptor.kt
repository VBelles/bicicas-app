package com.tcn.bicicas.data.datasource.remote

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response


class CacheInterceptorRewriteRequest : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val cacheControl = when {
            request.header("force-cache") != null -> CacheControl.FORCE_CACHE
            else -> CacheControl.FORCE_NETWORK
        }

        return chain.proceed(
            request.newBuilder()
                .removeHeader("force-cache")
                .cacheControl(cacheControl)
                .build()
        )
    }
}

class CacheInterceptorRewriteResponse : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request()).newBuilder()
            .removeHeader("Cache-control")
            .removeHeader("Pragma")
            .header(
                "Cache-Control",
                "public, only-if-cached, max-stale=${Int.MAX_VALUE}, max-age=${Int.MAX_VALUE}"
            )
            .build()
    }


}