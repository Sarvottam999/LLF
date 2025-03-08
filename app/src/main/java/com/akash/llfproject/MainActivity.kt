package com.akash.llfproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.akash.llfproject.ui.theme.LLFProjectTheme
import com.akash.llfproject.ui.navigation.Screen
import com.akash.llfproject.ui.navigation.BottomNavBar
import com.akash.llfproject.ui.auth.LoginScreen
import com.akash.llfproject.ui.auth.RegisterScreen
import com.akash.llfproject.ui.dashboard.DashboardScreen
import com.akash.llfproject.ui.machine.MachineListScreen
import com.akash.llfproject.ui.machine.MachineDetailScreen
import com.akash.llfproject.ui.machine.AddMachineScreen
import com.akash.llfproject.ui.inspection.InspectionListScreen
import com.akash.llfproject.ui.inspection.InspectionDetailScreen
import com.akash.llfproject.ui.inspection.CreateInspectionScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LLFProjectTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Only show bottom navigation on main screens
                        if (currentRoute in listOf(
                                Screen.Dashboard.route,
                                Screen.Machines.route,
                                Screen.Inspections.route,
                                Screen.Profile.route
                            )) {
                            BottomNavBar(navController = navController, currentRoute = currentRoute)
                        }
                    }
                ) { innerPadding ->
                    LLFNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LLFNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        // Auth screens
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        
        // Main screens
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        
        // Machine screens
        composable(Screen.Machines.route) {
            MachineListScreen(navController = navController)
        }
        composable("${Screen.MachineDetail.route}/{machineId}") { backStackEntry ->
            val machineId = backStackEntry.arguments?.getString("machineId") ?: ""
            MachineDetailScreen(navController = navController, machineId = machineId)
        }
        composable(Screen.AddMachine.route) {
            AddMachineScreen(navController = navController)
        }
        
        // Inspection screens
        composable(Screen.Inspections.route) {
            InspectionListScreen(navController = navController)
        }
        composable("${Screen.InspectionDetail.route}/{inspectionId}") { backStackEntry ->
            val inspectionId = backStackEntry.arguments?.getString("inspectionId") ?: ""
            InspectionDetailScreen(navController = navController, inspectionId = inspectionId)
        }
        composable("${Screen.CreateInspection.route}/{machineId}") { backStackEntry ->
            val machineId = backStackEntry.arguments?.getString("machineId") ?: ""
            CreateInspectionScreen(navController = navController, machineId = machineId)
        }
    }
}