package com.akash.llfproject.ui.inspection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.akash.llfproject.data.InspectionStatus
import com.akash.llfproject.ui.machine.MachineViewModel
import com.akash.llfproject.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInspectionScreen(
    navController: NavController,
    machineId: String,
    inspectionViewModel: InspectionViewModel = viewModel(),
    machineViewModel: MachineViewModel = viewModel()
) {
    val selectedMachine by machineViewModel.selectedMachine.collectAsState()
    var lookStatus by remember { mutableStateOf(InspectionStatus.OK) }
    var listenStatus by remember { mutableStateOf(InspectionStatus.OK) }
    var feelStatus by remember { mutableStateOf(InspectionStatus.OK) }
    var lookNotes by remember { mutableStateOf("") }
    var listenNotes by remember { mutableStateOf("") }
    var feelNotes by remember { mutableStateOf("") }
    var abnormalityNotes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load machine details
    LaunchedEffect(machineId) {
        machineViewModel.getMachine(machineId)
    }
    
    LaunchedEffect(selectedMachine) {
        isLoading = selectedMachine == null
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Inspection") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                selectedMachine == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Machine not found",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.navigateUp() }) {
                            Text("Go Back")
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Machine info
                        Text(
                            text = selectedMachine!!.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${selectedMachine!!.category.name.replace('_', ' ')} - ${selectedMachine!!.section.name.replace('_', ' ')}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // LLF Inspection form
                        Text(
                            text = "LLF Inspection",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Look section
                        InspectionSection(
                            title = "Look",
                            status = lookStatus,
                            onStatusChange = { lookStatus = it },
                            notes = lookNotes,
                            onNotesChange = { lookNotes = it }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Listen section
                        InspectionSection(
                            title = "Listen",
                            status = listenStatus,
                            onStatusChange = { listenStatus = it },
                            notes = listenNotes,
                            onNotesChange = { listenNotes = it }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Feel section
                        InspectionSection(
                            title = "Feel",
                            status = feelStatus,
                            onStatusChange = { feelStatus = it },
                            notes = feelNotes,
                            onNotesChange = { feelNotes = it }
                        )
                        
                        // Abnormality notes (only shown if any status is NOT_OK)
                        if (lookStatus == InspectionStatus.NOT_OK || 
                            listenStatus == InspectionStatus.NOT_OK || 
                            feelStatus == InspectionStatus.NOT_OK) {
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "Abnormality Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = abnormalityNotes,
                                onValueChange = { abnormalityNotes = it },
                                label = { Text("Additional Notes") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                        }
                        
                        // Error message
                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Submit button
                        Button(
                            onClick = {
                                if (validateInputs(lookStatus, listenStatus, feelStatus, lookNotes, listenNotes, feelNotes, abnormalityNotes)) {
                                    isSubmitting = true
                                    inspectionViewModel.createInspection(
                                        machineId = machineId,
                                        lookStatus = lookStatus,
                                        listenStatus = listenStatus,
                                        feelStatus = feelStatus,
                                        lookNotes = lookNotes,
                                        listenNotes = listenNotes,
                                        feelNotes = feelNotes,
                                        abnormalityNotes = abnormalityNotes,
                                        onSuccess = {
                                            isSubmitting = false
                                            navController.navigate(Screen.Inspections.route) {
                                                popUpTo(Screen.CreateInspection.route) { inclusive = true }
                                            }
                                        },
                                        onError = { error ->
                                            isSubmitting = false
                                            errorMessage = error
                                        }
                                    )
                                } else {
                                    errorMessage = "Please provide notes for any abnormality detected"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !isSubmitting
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Submit Inspection")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InspectionSection(
    title: String,
    status: InspectionStatus,
    onStatusChange: (InspectionStatus) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Row {
                    // OK option
                    FilterChip(
                        selected = status == InspectionStatus.OK,
                        onClick = { onStatusChange(InspectionStatus.OK) },
                        label = { Text("OK") },
                        leadingIcon = {
                            if (status == InspectionStatus.OK) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // NOT OK option
                    FilterChip(
                        selected = status == InspectionStatus.NOT_OK,
                        onClick = { onStatusChange(InspectionStatus.NOT_OK) },
                        label = { Text("NOT OK") },
                        leadingIcon = {
                            if (status == InspectionStatus.NOT_OK) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            selectedLabelColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}

private fun validateInputs(
    lookStatus: InspectionStatus,
    listenStatus: InspectionStatus,
    feelStatus: InspectionStatus,
    lookNotes: String,
    listenNotes: String,
    feelNotes: String,
    abnormalityNotes: String
): Boolean {
    // If any status is NOT_OK, require notes for that section
    if (lookStatus == InspectionStatus.NOT_OK && lookNotes.isBlank()) {
        return false
    }
    
    if (listenStatus == InspectionStatus.NOT_OK && listenNotes.isBlank()) {
        return false
    }
    
    if (feelStatus == InspectionStatus.NOT_OK && feelNotes.isBlank()) {
        return false
    }
    
    // If any abnormality is detected, require abnormality notes
    if ((lookStatus == InspectionStatus.NOT_OK || 
         listenStatus == InspectionStatus.NOT_OK || 
         feelStatus == InspectionStatus.NOT_OK) && 
        abnormalityNotes.isBlank()) {
        return false
    }
    
    return true
}