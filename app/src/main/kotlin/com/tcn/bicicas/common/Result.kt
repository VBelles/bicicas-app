package com.tcn.bicicas.common

import io.ktor.client.statement.HttpStatement
import kotlin.coroutines.cancellation.CancellationException


inline fun <T, R> Result<T>.andThen(transform: (T) -> Result<R>): Result<R> {
    return fold(
        onSuccess = { value -> transform(value) },
        onFailure = { exception -> Result.failure(exception) }
    )
}

suspend inline fun <reified T, R> HttpStatement.result(
    crossinline block: suspend (response: T) -> R
): Result<R> = try {
    Result.success(body(block))
} catch (e: Exception) {
    if (e is CancellationException) throw e
    Result.failure(e)
}
