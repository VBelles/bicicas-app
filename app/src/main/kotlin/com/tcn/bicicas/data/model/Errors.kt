package com.tcn.bicicas.data.model

import java.io.IOException

data class NetworkError(val exception: IOException) : Error()

data class HttpError(val code: Int, val errorMessage: String?) : Error()
