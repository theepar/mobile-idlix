package com.example.watchmobile.utils

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import java.io.File
import java.util.concurrent.Executor

@UnstableApi
object DownloadUtil {
    private var downloadCache: Cache? = null
    private var downloadManager: DownloadManager? = null
    private var databaseProvider: StandaloneDatabaseProvider? = null

    @Synchronized
    fun getDownloadCache(context: Context): Cache {
        if (downloadCache == null) {
            val downloadContentDirectory = File(context.getExternalFilesDir(null), "downloads")
            downloadCache = SimpleCache(
                downloadContentDirectory,
                NoOpCacheEvictor(),
                getDatabaseProvider(context)
            )
        }
        return downloadCache!!
    }

    @Synchronized
    private fun getDatabaseProvider(context: Context): StandaloneDatabaseProvider {
        if (databaseProvider == null) {
            databaseProvider = StandaloneDatabaseProvider(context.applicationContext)
        }
        return databaseProvider!!
    }

    @Synchronized
    fun getDownloadManager(context: Context): DownloadManager {
        if (downloadManager == null) {
            val executor = Executor { it.run() }
            val downloadManager = DownloadManager(
                context.applicationContext,
                getDatabaseProvider(context),
                getDownloadCache(context),
                DefaultHttpDataSource.Factory(),
                executor
            )
            downloadManager.maxParallelDownloads = 3
            this.downloadManager = downloadManager
        }
        return downloadManager!!
    }
}
