package com.example.watchmobile.services

import android.app.Notification
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import com.example.watchmobile.R
import com.example.watchmobile.utils.DownloadUtil

@UnstableApi
class DemoDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.exo_download_notification_channel_name,
    0
) {
    override fun getDownloadManager(): DownloadManager {
        return DownloadUtil.getDownloadManager(this)
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        return androidx.media3.exoplayer.offline.DownloadNotificationHelper(
            this,
            DOWNLOAD_NOTIFICATION_CHANNEL_ID
        ).buildProgressNotification(
            this,
            android.R.drawable.stat_sys_download,
            null,
            null,
            downloads,
            notMetRequirements
        )
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"
    }
}
