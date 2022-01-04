package com.tcn.bicicas.data

import com.tcn.bicicas.data.model.HttpError
import com.tcn.bicicas.data.model.NetworkError
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException


inline fun <T, R> Result<T>.andThen(transform: (T) -> Result<R>): Result<R> {
    return fold(
        onSuccess = { value -> transform(value) },
        onFailure = { exception -> Result.failure(exception) }
    )

}


suspend fun <T : Any> resultOf(call: suspend () -> Response<T>): Result<Pair<Response<T>, T>> {
    return try {
        val response = call()
        when {
            !response.isSuccessful ->
                Result.failure(HttpError(response.code(), response.raw().body?.toString()))
            response.body() != null -> Result.success(response to response.body()!!)
            else -> Result.failure(UnknownError())
        }
    } catch (t: Throwable) {
        if (t is CancellationException) throw t
        t.printStackTrace()
        when (t) {
            is HttpException ->
                Result.failure(HttpError(t.code(), t.response()?.errorBody()?.toString()))
            is IOException -> Result.failure(NetworkError(t))
            else -> Result.failure(t)
        }
    }
}