package com.ebchat

import android.app.Application
import com.ebchat.data.LocalCache

class EBChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LocalCache.init(this)
    }
}
