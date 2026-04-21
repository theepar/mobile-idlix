package com.example.watchmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.watchmobile.ui.screens.HomeScreen
import com.example.watchmobile.ui.screens.PlayerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WatchMobileApp()
                }
            }
        }
    }
}

@Composable
fun WatchMobileApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onMovieClick = { slug ->
                    // Simulasi: Mengarahkan ke player dengan video dummy Big Buck Bunny
                    // Dalam implementasi nyata, di sini Anda harus fetch detail movie dulu untuk dapat Iframe, 
                    // lalu jalankan MediaResolver, baru buka PlayerScreen.
                    
                    val dummyVideo = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
                    // Mengenkode URL agar aman dikirim sebagai argumen navigasi
                    val encodedUrl = java.net.URLEncoder.encode(dummyVideo, "UTF-8")
                    navController.navigate("player/$encodedUrl")
                }
            )
        }
        
        composable(
            route = "player/{videoUrl}",
            arguments = listOf(
                navArgument("videoUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
            // Decode kembali URL
            val decodedUrl = java.net.URLDecoder.decode(videoUrl, "UTF-8")
            
            PlayerScreen(
                videoUrl = decodedUrl,
                subtitleUrl = null // Tidak ada subtitle dummy untuk saat ini
            )
        }
    }
}
