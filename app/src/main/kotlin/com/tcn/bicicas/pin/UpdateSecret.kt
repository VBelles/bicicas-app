package com.tcn.bicicas.pin

import com.tcn.bicicas.data.datasource.Store

class UpdateSecret(val secretStore: Store<String>){

    suspend fun invoke(secret: String){
        cipher.encode(secret)
    }
}