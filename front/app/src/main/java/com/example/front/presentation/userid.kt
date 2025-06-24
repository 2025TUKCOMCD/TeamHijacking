package com.example.front.presentation

import android.app.Application

class userid : Application() {

    var receivedMessage: String = ""
    var receivedTimestamp: Long = 0L

    override fun onCreate() {
        super.onCreate()
        // Initialize anything else here if needed
    }
}