package com.example.watchmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.watchmobile.ui.theme.IdlixRed

sealed class BottomNavItem(val route: String, val icon: String, val title: String) {
    object Home : BottomNavItem("home", "Home", "Home")
    object Discover : BottomNavItem("discover", "Discover", "Discover")
    object Downloads : BottomNavItem("downloads", "Downloads", "Downloads")
    object Profile : BottomNavItem("profile", "Profile", "Profile")
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Discover,
        BottomNavItem.Downloads,
        BottomNavItem.Profile
    )
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Tampilkan hanya jika rute saat ini adalah salah satu rute root
    if (items.any { it.route == currentRoute }) {
        NavigationBar(
            containerColor = Color.Black.copy(alpha = 0.8f),
            contentColor = Color.White,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.8f))
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    icon = {
                        Text(
                            text = if (selected) "★" else "☆", // Placeholder icon, bisa diganti dengan Icon sungguhan
                            color = if (selected) IdlixRed else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            color = if (selected) IdlixRed else Color.Gray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IdlixRed,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = IdlixRed,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
