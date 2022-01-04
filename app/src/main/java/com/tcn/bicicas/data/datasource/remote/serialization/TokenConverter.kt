package com.tcn.bicicas.data.datasource.remote.serialization

import com.tcn.bicicas.data.model.Token
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Converter

class TokenConverter : Converter<ResponseBody, Token> {

    override fun convert(value: ResponseBody): Token {
        return JSONObject(value.string())
            .run { Token(getString("access_token")) }
    }

}