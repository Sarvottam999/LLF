package com.akash.llfproject.ui.machine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akash.llfproject.data.Machine
import com.akash.llfproject.data.MachineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Represents the different states of the machine list
sealed class MachineState {
    object Loading : MachineState()
    data class Success(val machines: List<Machine>) : MachineState()
    data class Error(val message: String) : MachineState()
}

class MachineViewModel : ViewModel() {
    private val machineRepository = MachineRepository()
    
    private val _machinesState = MutableStateFlow<MachineState>(MachineState.Loading)
    val machinesState: StateFlow<MachineState> = _machinesState
    
    private val _selectedMachine = MutableStateFlow<Machine?>(null)
    val selectedMachine: StateFlow<Machine?> = _selectedMachine
    
    // Load all machines
    fun loadMachines() {
        _machinesState.value = MachineState.Loading
        viewModelScope.launch {
            try {
                val result = machineRepository.getAllMachines()
                if (result.isSuccess) {
                    _machinesState.value = MachineState.Success(result.getOrThrow())
                } else {
                    _machinesState.value = MachineState.Error(result.exceptionOrNull()?.message ?: "Failed to load machines")
                }
            } catch (e: Exception) {
                _machinesState.value = MachineState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    // Search machines by name or ID
    fun searchMachines(query: String) {
        if (query.isBlank()) {
            loadMachines()
            return
        }
        
        _machinesState.value = MachineState.Loading
        viewModelScope.launch {
            try {
                val result = machineRepository.searchMachines(query)
                if (result.isSuccess) {
                    _machinesState.value = MachineState.Success(result.getOrThrow())
                } else {
                    _machinesState.value = MachineState.Error(result.exceptionOrNull()?.message ?: "Failed to search machines")
                }
            } catch (e: Exception) {
                _machinesState.value = MachineState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    // Get a machine by ID
    fun getMachine(machineId: String) {
        viewModelScope.launch {
            try {
                val result = machineRepository.getMachine(machineId)
                if (result.isSuccess) {
                    _selectedMachine.value = result.getOrThrow()
                } else {
                    // Set selected machine to null and log the error
                    _selectedMachine.value = null
                    android.util.Log.e("MachineViewModel", "Failed to get machine: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                // Set selected machine to null and log the exception
                _selectedMachine.value = null
                android.util.Log.e("MachineViewModel", "Exception when getting machine: ${e.message}")
            }
        }
    }
    
    // Add a new machine
    fun addMachine(
        name: String,
        category: com.akash.llfproject.data.MachineCategory,
        section: com.akash.llfproject.data.MachineSection,
        subCategory: String,
        inspectionFrequency: com.akash.llfproject.data.InspectionFrequency,
        imageUri: android.net.Uri,
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = machineRepository.addMachine(
                    name = name,
                    category = category,
                    section = section,
                    subCategory = subCategory,
                    inspectionFrequency = inspectionFrequency,
                    imageUri = imageUri,
                    userId = userId
                )
                
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to add machine")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    // Update an existing machine
    fun updateMachine(
        machineId: String,
        name: String,
        category: com.akash.llfproject.data.MachineCategory,
        section: com.akash.llfproject.data.MachineSection,
        subCategory: String,
        inspectionFrequency: com.akash.llfproject.data.InspectionFrequency,
        imageUri: android.net.Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = machineRepository.updateMachine(
                    machineId = machineId,
                    name = name,
                    category = category,
                    section = section,
                    subCategory = subCategory,
                    inspectionFrequency = inspectionFrequency,
                    imageUri = imageUri
                )
                
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to update machine")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    // Delete a machine
    fun deleteMachine(
        machineId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = machineRepository.deleteMachine(machineId)
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to delete machine")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }
}