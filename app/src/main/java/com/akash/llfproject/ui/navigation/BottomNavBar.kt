package com.akash.llfproject.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String?) {
    val navItems = listOf(
        NavItem(
            name = "Dashboard",
            route = Screen.Dashboard.route,
            icon = Icons.Filled.Dashboard
        ),
        NavItem(
            name = "Machines",
            route = Screen.Machines.route,
            icon = Icons.Outlined.Build
        ),
        NavItem(
            name = "Inspections",
            route = Screen.Inspections.route,
            icon = Icons.Outlined.Assignment
        ),
        NavItem(
            name = "Profile",
            route = Screen.Profile.route,
            icon = Icons.Filled.Person
        )
    )
    
    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.name) },
                label = { Text(item.name) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

data class NavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)