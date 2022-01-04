package com.tcn.bicicas.data.datasource.remote.serialization

import com.tcn.bicicas.data.model.TwoFactorAuth
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Converter

class TwoFactorAuthConverter : Converter<ResponseBody, TwoFactorAuth> {

    override fun convert(value: ResponseBody): TwoFactorAuth {
        return JSONObject(value.string())
            .getJSONObject("dashboard")
            .getJSONObject("twofactor")
            .run { TwoFactorAuth(getString("user"), getString("secret")) }
    }

}