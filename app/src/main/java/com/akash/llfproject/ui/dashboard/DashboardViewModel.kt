package com.akash.llfproject.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akash.llfproject.auth.AuthRepository
import com.akash.llfproject.data.Inspection
import com.akash.llfproject.data.InspectionRepository
import com.akash.llfproject.data.Machine
import com.akash.llfproject.data.MachineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Represents the different states of the dashboard
sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(
        val machinesDue: List<Machine>,
        val abnormalities: List<Inspection>
    ) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val machineRepository = MachineRepository()
    private val inspectionRepository = InspectionRepository()
    
    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState
    
    fun loadDashboardData() {
        _dashboardState.value = DashboardState.Loading
        viewModelScope.launch {
            try {
                // Get current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _dashboardState.value = DashboardState.Error("User not logged in")
                    return@launch
                }
                
                // Get user profile to determine role and section
                val userProfileResult = authRepository.getUserProfile(currentUser.uid)
                if (userProfileResult.isFailure) {
                    _dashboardState.value = DashboardState.Error("Failed to load user profile")
                    return@launch
                }
                
                val userProfile = userProfileResult.getOrThrow()
                
                // Load machines due for inspection
                val machinesDueResult = machineRepository.getMachinesDueForInspection()
                if (machinesDueResult.isFailure) {
                    _dashboardState.value = DashboardState.Error("Failed to load machines due for inspection")
                    return@launch
                }
                
                val machinesDue = machinesDueResult.getOrThrow()
                
                // Load abnormalities based on user role
                val abnormalitiesResult = if (userProfile.isAdmin()) {
                    // Admins see all abnormalities
                    inspectionRepository.getInspectionsWithOpenAbnormalities()
                } else {
                    // Section heads see abnormalities in their section
                    inspectionRepository.getInspectionsBySection(userProfile.section)
                }
                
                if (abnormalitiesResult.isFailure) {
                    _dashboardState.value = DashboardState.Error("Failed to load abnormalities")
                    return@launch
                }
                
                val allInspections = abnormalitiesResult.getOrThrow()

                // Filter to only show inspections with abnormalities
                val abnormalities = allInspections.filter { it.hasAbnormality && !it.isAbnormalityClosed() }

                _dashboardState.value = DashboardState.Success(
                    machinesDue = machinesDue,
                    abnormalities = abnormalities
                )
                
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}