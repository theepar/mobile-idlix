package com.example.watchmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import coil.compose.AsyncImage
import com.example.watchmobile.ui.theme.DarkSurface
import com.example.watchmobile.ui.theme.IdlixRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = Color.White) },
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Profile Header
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuD79W_d4CTjAqjEl4IThEVI2LdMCdWwSy5tFzhSQOsB9DxdndngtbgwMxCUrVD-jXJ3rdA5LWqhrcKO4uXPfMaQrP9Cl6jv_-0sdZq_cVgQdMouVvFduUP4QkazBK2jPrXeI0HW_oAwSBup9b-qTvF5cHuUzYJtN_pWTGootCreg4iQMM2JqB9qGVl7o0QAQ1JXjpAY4pfgaHf30ymh7_hhIBfDXbh8YfqyTcNv9C5FxN3VTMDWigg-DsF8p3yryjraTn_k5k3XNB_i",
                    contentDescription = "Profile Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .border(4.dp, DarkSurface, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(IdlixRed)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✎", color = Color.White) // Placeholder icon edit
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Alex Carter", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("★", color = IdlixRed, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("PREMIUM MEMBER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Settings List
            SettingsItem("Account Settings")
            SettingsItem("Watchlist")
            SettingsItem("Subscription Plan", subtitle = "4K Ultra HD")
            SettingsItem("App Settings")
            
            Spacer(modifier = Modifier.height(16.dp))
            // Logout Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF121212))
                    .border(1.dp, DarkSurface, RoundedCornerShape(12.dp))
                    .clickable { }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text("L", color = Color.Red)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Logout", fontSize = 16.sp, color = Color.Red)
            }
        }
    }
}

@Composable
fun SettingsItem(title: String, subtitle: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121212))
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Text("⚙", color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 16.sp, color = Color.White)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
        Text(">", color = Color.Gray, fontSize = 20.sp)
    }
}
