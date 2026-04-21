package com.example.watchmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.watchmobile.ui.components.BottomNavBar
import com.example.watchmobile.ui.screens.*
import com.example.watchmobile.ui.theme.WatchMobileTheme

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.watchmobile.utils.CloudflareBypasser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initial bypass attempt to get valid cookies before UI loads data initially
        lifecycleScope.launch {
            CloudflareBypasser.bypass(this@MainActivity, BuildConfig.BASE_URL)
        }

        setContent {
            WatchMobileTheme {
                WatchMobileApp()
            }
        }
    }
}

@Composable
fun WatchMobileApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onMovieClick = { slug ->
                        navController.navigate("detail/$slug")
                    },
                    onSearchClick = {
                        navController.navigate("discover")
                    }
                )
            }
            
            composable("discover") {
                DiscoverScreen(
                    onMovieClick = { slug ->
                        navController.navigate("detail/$slug")
                    }
                )
            }

            composable("downloads") {
                DownloadsScreen()
            }

            composable("profile") {
                ProfileScreen()
            }

            composable(
                route = "detail/{slug}",
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug") ?: ""
                MovieDetailScreen(
                    slug = slug,
                    onPlayClick = { videoUrl ->
                        val encodedUrl = java.net.URLEncoder.encode(videoUrl, "UTF-8")
                        navController.navigate("player/$encodedUrl")
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(
                route = "player/{videoUrl}",
                arguments = listOf(navArgument("videoUrl") { type = NavType.StringType })
            ) { backStackEntry ->
                val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
                val decodedUrl = java.net.URLDecoder.decode(videoUrl, "UTF-8")
                
                PlayerScreen(
                    videoUrl = decodedUrl,
                    subtitleUrl = null
                )
            }
        }
    }
}
