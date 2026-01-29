package com.example.passwordlessauth

import android.app.Application
import timber.log.Timber

class PasswordlessAuthApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        // Only plant debug tree in debug builds
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("PasswordlessAuth Application initialized")
    }
}