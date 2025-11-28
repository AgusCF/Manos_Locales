package com.undef.manoslocales.util

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppContextProvider : Application() {
    init {
        instance = this
    }

    companion object {
        private lateinit var instance: AppContextProvider

        fun getContext(): Context {
            return instance.applicationContext
        }
    }
}
