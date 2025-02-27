package com.example.androidclient

import android.app.Application
import android.util.Log
import com.example.androidclient.core.TAG

class MyApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "init")
        container = AppContainer(this)
    }
}