package com.example.watchmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Environment
import android.os.StatFs
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import com.example.watchmobile.utils.DownloadUtil
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.animateContentSize
import com.example.watchmobile.ui.theme.IdlixRed
import com.example.watchmobile.ui.theme.DarkSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen() {
    val context = LocalContext.current
    val downloadManager = remember { DownloadUtil.getDownloadManager(context) }
    val downloads = remember { mutableStateListOf<Download>() }

    // Helper to refresh downloads
    val refreshDownloads = {
        val cursor = downloadManager.downloadIndex.getDownloads()
        val currentDownloads = mutableListOf<Download>()
        while (cursor.moveToNext()) {
            currentDownloads.add(cursor.download)
        }
        cursor.close()
        downloads.clear()
        downloads.addAll(currentDownloads)
    }

    DisposableEffect(downloadManager) {
        val listener = object : DownloadManager.Listener {
            override fun onDownloadChanged(
                downloadManager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                refreshDownloads()
            }
            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                refreshDownloads()
            }
        }
        downloadManager.addListener(listener)
        refreshDownloads() // Initial load

        onDispose {
            downloadManager.removeListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IDLIX", fontWeight = FontWeight.Bold, color = IdlixRed) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(Color.Black.copy(alpha = 0.7f))
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .animateContentSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Calculate Storage using StatFs
            val stat = remember { StatFs(Environment.getDataDirectory().path) }
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalSpaceGB = (totalBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
            val freeSpaceGB = (availableBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
            val usedSpaceGB = totalSpaceGB - freeSpaceGB
            val progress = (usedSpaceGB / totalSpaceGB).toFloat()

            // Storage Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Internal Storage", color = Color.Gray, fontSize = 14.sp)
                Text(String.format("%.1f GB free", freeSpaceGB), color = Color.Gray, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50)),
                color = IdlixRed,
                trackColor = DarkSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(String.format("Used: %.1f GB", usedSpaceGB), color = Color.Gray, fontSize = 10.sp)
                Text(String.format("Total: %.1f GB", totalSpaceGB), color = Color.Gray, fontSize = 10.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Downloads", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (downloads.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada unduhan",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(downloads.toList()) { download ->
                        DownloadItem(download = download, onRemove = {
                            downloadManager.removeDownload(download.request.id)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadItem(download: Download, onRemove: () -> Unit) {
    val stateText = when (download.state) {
        Download.STATE_QUEUED -> "Queued"
        Download.STATE_STOPPED -> "Stopped"
        Download.STATE_DOWNLOADING -> "Downloading ${download.percentDownloaded.toInt()}%"
        Download.STATE_COMPLETED -> "Completed"
        Download.STATE_FAILED -> "Failed"
        Download.STATE_REMOVING -> "Removing..."
        Download.STATE_RESTARTING -> "Restarting..."
        else -> "Unknown"
    }
    
    val title = androidx.media3.common.util.Util.fromUtf8Bytes(download.request.data)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (title.isEmpty()) download.request.id else title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stateText,
                color = if (download.state == Download.STATE_COMPLETED) Color.Green else Color.Gray,
                fontSize = 14.sp
            )
        }
        
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = IdlixRed
            )
        }
    }
}
