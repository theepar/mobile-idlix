package com.example.watchmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Storage Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Storage", color = Color.Gray, fontSize = 14.sp)
                Text("45 GB free", color = Color.Gray, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = 0.7f,
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
                Text("Used: 80 GB", color = Color.Gray, fontSize = 10.sp)
                Text("Total: 125 GB", color = Color.Gray, fontSize = 10.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Downloads", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dummy Download Items
            DownloadItem(
                title = "Interstellar Voyager",
                subtitle = "Movie • 2.4 GB",
                badge = "4K HDR",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAnIMIC370aP7MMRyfLMwT5Vyr6w3Y3ew3-CWS8yjZGq51kqR5W9-LWgh6cM8n7i5pcsgJQZRqjiLiLr7ceqvjpt8QVdS_FzPoVWoPT5RvA6lh6yamQp-mFUBYpxJ2h75WJWqexkGbcVbPFeN83PeSetyTlsF09-scCYwWvti59J9mZg5IHduEpQ7Z9T8ogt5z7_l8VL1Jlgttk16l6IIdNiqFk7bFH3iCHadh6Jw661iFYJjHLJUWY0adkThxGpB-pbKIg7sRaK6Y5"
            )
            DownloadItem(
                title = "The Midnight Caller",
                subtitle = "Series • 3 Episodes • 1.8 GB",
                badge = "1080p",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAhBwCSDaYOWrlShFwW3gVM4pSEQDmgpXt98-S2ptS-WfFRh1mj6oTRGZyxvj51H-B3HNqdkh_B8gEqGOF2CDsU9MY61WYN9pSorEqUlMtnPRkRjG8twDHsoOPOtl37y1-j-ejhYJ1hcKDM-6uLBOS7pdUK_eOBuna6F4X-b1LlnJO8RvdS6dLulXO9QxoodGSisqT9Mbozk_6BhZkh_N2A4p61A6Nu1YknkNs2ymR4F7HD1gQnJimr5ynoWID-vTgnGQeCYry4JWWo"
            )
        }
    }
}

@Composable
fun DownloadItem(title: String, subtitle: String, badge: String, imageUrl: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121212))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(80.dp)
                .height(112.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DarkSurface)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Downloaded",
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DarkSurface)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            Text("▶", color = Color.White)
        }
    }
}
