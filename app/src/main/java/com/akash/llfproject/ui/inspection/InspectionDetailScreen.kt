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
import com.akash.llfproject.data.Inspection
import com.akash.llfproject.data.InspectionStatus
import com.akash.llfproject.data.AbnormalityStatus
import com.akash.llfproject.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionDetailScreen(
    navController: NavController,
    inspectionId: String,
    viewModel: InspectionViewModel = viewModel()
) {
    val selectedInspection by viewModel.selectedInspection.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(inspectionId) {
        viewModel.getInspection(inspectionId)
    }
    
    LaunchedEffect(selectedInspection) {
        isLoading = selectedInspection == null
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inspection Details") },
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
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: $errorMessage",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.getInspection(inspectionId) }) {
                            Text("Retry")
                        }
                    }
                }
                selectedInspection == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Inspection not found",
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
                    InspectionDetailContent(
                        inspection = selectedInspection!!,
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun InspectionDetailContent(
    inspection: Inspection,
    navController: NavController,
    viewModel: InspectionViewModel
) {
    var showStatusDialog by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Inspection header
        Text(
            text = "Inspection #${inspection.id.takeLast(6)}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Machine: ${inspection.machineName}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Date: ${inspection.date}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Inspector: ${inspection.inspectorName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // LLF Inspection Results
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LLF Inspection Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Look
                InspectionResultItem(
                    title = "Look",
                    status = inspection.lookStatus,
                    notes = inspection.lookNotes
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Listen
                InspectionResultItem(
                    title = "Listen",
                    status = inspection.listenStatus,
                    notes = inspection.listenNotes
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Feel
                InspectionResultItem(
                    title = "Feel",
                    status = inspection.feelStatus,
                    notes = inspection.feelNotes
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Abnormality section (only shown if there's an abnormality)
        if (inspection.hasAbnormality()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (inspection.abnormalityStatus) {
                        AbnormalityStatus.OPEN -> MaterialTheme.colorScheme.errorContainer
                        AbnormalityStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer
                        AbnormalityStatus.RESOLVED -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Abnormality Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val statusColor = when (inspection.abnormalityStatus) {
                            AbnormalityStatus.OPEN -> MaterialTheme.colorScheme.error
                            AbnormalityStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
                            AbnormalityStatus.RESOLVED -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.secondary
                        }
                        
                        Chip(
                            onClick = { showStatusDialog = true },
                            colors = ChipDefaults.chipColors(
                                containerColor = statusColor.copy(alpha = 0.1f),
                                labelColor = statusColor
                            )
                        ) {
                            Text(inspection.abnormalityStatus.name)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (inspection.abnormalityNotes.isNotBlank()) {
                        Text(
                            text = "Notes:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = inspection.abnormalityNotes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    if (inspection.abnormalityStatus == AbnormalityStatus.RESOLVED && 
                        inspection.resolutionNotes.isNotBlank()) {
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Resolution:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = inspection.resolutionNotes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Resolved by: ${inspection.resolvedBy ?: "Unknown"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Text(
                            text = "Resolved on: ${inspection.resolvedOn ?: "Unknown"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Update abnormality button
            Button(
                onClick = { showStatusDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Update Status")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Abnormality Status")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // View machine button
        OutlinedButton(
            onClick = { navController.navigate("${Screen.MachineDetail.route}/${inspection.machineId}") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Build, contentDescription = "View Machine")
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Machine")
        }
    }
    
    // Status update dialog
    if (showStatusDialog) {
        // Implementation of status update dialog would go here
        // This would include options to change the abnormality status
        // and add notes for resolution
    }
}

@Composable
fun InspectionResultItem(
    title: String,
    status: InspectionStatus,
    notes: String
) {
    val statusColor = when (status) {
        InspectionStatus.OK -> MaterialTheme.colorScheme.primary
        InspectionStatus.NOT_OK -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Chip(
                onClick = { },
                colors = ChipDefaults.chipColors(
                    containerColor = statusColor.copy(alpha = 0.1f),
                    labelColor = statusColor
                )
            ) {
                Text(status.name)
            }
        }
        
        if (notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}