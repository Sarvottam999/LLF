package com.akash.llfproject.ui.navigation

sealed class Screen(val route: String) {
    // Auth screens
    object Login : Screen("login")
    object Register : Screen("register")
    
    // Main screens
    object Dashboard : Screen("dashboard")
    object Machines : Screen("machines")
    object Inspections : Screen("inspections")
    object Profile : Screen("profile")
    
    // Machine screens
    object MachineDetail : Screen("machine_detail")
    object AddMachine : Screen("add_machine")
    
    // Inspection screens
    object InspectionDetail : Screen("inspection_detail")
    object CreateInspection : Screen("create_inspection")
}