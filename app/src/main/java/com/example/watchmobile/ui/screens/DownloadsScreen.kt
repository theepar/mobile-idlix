package com.example.watchmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.remember
import coil.compose.AsyncImage
import com.example.watchmobile.ui.theme.DarkSurface
import com.example.watchmobile.ui.theme.IdlixRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen() {
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
        }
    }
}
