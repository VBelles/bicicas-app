package com.tcn.bicicas.data.datasource.remote.serialization

import com.tcn.bicicas.data.model.Station
import com.tcn.bicicas.data.model.Token
import com.tcn.bicicas.data.model.TwoFactorAuth
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class ApiConverter : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return when (getRawType(type)) {
            Token::class.java -> TokenConverter()
            TwoFactorAuth::class.java -> TwoFactorAuthConverter()
            Array<Station>::class.java -> StationConverter()
            else -> null
        }
    }


}