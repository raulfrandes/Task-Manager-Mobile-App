package com.example.androidclient.auth.data

import android.util.Log
import com.example.androidclient.auth.data.remote.AuthDataSource
import com.example.androidclient.auth.data.remote.TokenHolder
import com.example.androidclient.auth.data.remote.User
import com.example.androidclient.core.TAG
import com.example.androidclient.core.data.remote.Api

class AuthRepository(private val authDataSource: AuthDataSource) {
    init {
        Log.d(TAG, "init")
    }

    suspend fun login(username: String, password: String): Result<TokenHolder> {
        val user = User(username, password)
        val result = authDataSource.login(user)
        if (result.isSuccess) {
            Api.tokenInterceptor.token = result.getOrNull()?.token
        }
        return result
    }
}