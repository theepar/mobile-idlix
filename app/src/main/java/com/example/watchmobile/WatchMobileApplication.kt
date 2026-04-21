package com.example.watchmobile

import android.app.Application
import androidx.media3.common.util.UnstableApi
import com.example.watchmobile.utils.DownloadUtil

@UnstableApi
class WatchMobileApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize download manager and cache when app starts
        DownloadUtil.getDownloadManager(this)
    }
}
