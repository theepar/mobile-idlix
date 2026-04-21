package com.example.watchmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.watchmobile.ui.theme.IdlixRed

sealed class BottomNavItem(val route: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Filled.Home, Icons.Outlined.Home, "Home")
    object Discover : BottomNavItem("discover", Icons.Filled.Explore, Icons.Outlined.Explore, "Discover")
    object Downloads : BottomNavItem("downloads", Icons.Filled.Download, Icons.Outlined.Download, "Downloads")
    object Profile : BottomNavItem("profile", Icons.Filled.Person, Icons.Outlined.Person, "Profile")
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
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            tint = if (selected) IdlixRed else Color.Gray
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
