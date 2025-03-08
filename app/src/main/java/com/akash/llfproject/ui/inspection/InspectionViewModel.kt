package com.akash.llfproject.ui.inspection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akash.llfproject.data.AbnormalityStatus
import com.akash.llfproject.data.Inspection
import com.akash.llfproject.data.InspectionRepository
import com.akash.llfproject.data.InspectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Represents the different states of the inspection list
sealed class InspectionState {
    object Loading : InspectionState()
    data class Success(val inspections: List<Inspection>) : InspectionState()
    data class Error(val message: String) : InspectionState()
}

class InspectionViewModel : ViewModel() {
    private val inspectionRepository = InspectionRepository()
    
    private val _inspectionsState = MutableStateFlow<InspectionState>(InspectionState.Loading)
    val inspectionsState: StateFlow<InspectionState> = _inspectionsState
    
    private val _selectedInspection = MutableStateFlow<Inspection?>(null)
    val selectedInspection: StateFlow<Inspection?> = _selectedInspection
    
    // Load all inspections
    fun loadInspections() {
        _inspectionsState.value = InspectionState.Loading
        viewModelScope.launch {
            try {
                val result = inspectionRepository.getAllInspections()
                if (result.isSuccess) {
                    _inspectionsState.value = InspectionState.Success(result.getOrThrow())
                } else {
                    _inspectionsState.value = InspectionState.Error(result.exceptionOrNull()?.message ?: "Failed to load inspections")
                }
            } catch (e: Exception) {
                _inspectionsState.value = InspectionState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    // Search inspections by machine name or ID
    fun searchInspections(query: String) {
        if (query.isBlank()) {
            loadInspections()
            return
        }
        
        _inspectionsState.value = InspectionState.Loading
        viewModelScope.launch {
            try {
                val result = inspectionRepository.searchInspections(query)
                if (result.isSuccess) {
                    _inspectionsState.value = InspectionState.Success(result.getOrThrow())
                } else {
                    _inspectionsState.value = InspectionState.Error(result.exceptionOrNull()?.message ?: "Failed to search inspections")
                }
            } catch (e: Exception) {
                _inspectionsState.value = InspectionState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    // Get an inspection by ID
    fun getInspection(inspectionId: String) {
        viewModelScope.launch {
            try {
                val result = inspectionRepository.getInspection(inspectionId)
                if (result.isSuccess) {
                    _selectedInspection.value = result.getOrThrow()
                } else {
                    // Set selected inspection to null and log the error
                    _selectedInspection.value = null
                    android.util.Log.e("InspectionViewModel", "Failed to get inspection: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                // Set selected inspection to null and log the exception
                _selectedInspection.value = null
                android.util.Log.e("InspectionViewModel", "Exception when getting inspection: ${e.message}")
            }
        }
    }
    
    // Create a new inspection
    fun createInspection(
        machineId: String,
        lookStatus: InspectionStatus,
        listenStatus: InspectionStatus,
        feelStatus: InspectionStatus,
        lookNotes: String,
        listenNotes: String,
        feelNotes: String,
        abnormalityNotes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = inspectionRepository.createInspection(
                    machineId = machineId,
                    lookStatus = lookStatus,
                    listenStatus = listenStatus,
                    feelStatus = feelStatus,
                    lookNotes = lookNotes,
                    listenNotes = listenNotes,
                    feelNotes = feelNotes,
                    abnormalityNotes = abnormalityNotes
                )
                
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to create inspection")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    // Update abnormality status
    fun updateAbnormalityStatus(
        inspectionId: String,
        newStatus: AbnormalityStatus,
        resolutionNotes: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = inspectionRepository.updateAbnormalityStatus(
                    inspectionId = inspectionId,
                    newStatus = newStatus,
                    resolutionNotes = resolutionNotes
                )
                
                if (result.isSuccess) {
                    // Refresh the selected inspection
                    getInspection(inspectionId)
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to update status")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }
}