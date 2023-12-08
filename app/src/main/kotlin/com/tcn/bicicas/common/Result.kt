package com.tcn.bicicas.common


inline fun <T, R> Result<T>.andThen(transform: (T) -> Result<R>): Result<R> {
    return fold(
        onSuccess = { value -> transform(value) },
        onFailure = { exception -> Result.failure(exception) }
    )

}