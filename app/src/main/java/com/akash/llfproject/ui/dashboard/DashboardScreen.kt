package com.akash.llfproject.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.akash.llfproject.data.Inspection
import com.akash.llfproject.data.Machine
import com.akash.llfproject.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        when (val state = dashboardState) {
            is DashboardState.Loading -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is DashboardState.Error -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.loadDashboardData() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is DashboardState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        DashboardSummaryCard(
                            machinesDueCount = state.machinesDue.size,
                            abnormalitiesCount = state.abnormalities.size,
                            onClick = { /* TODO: Navigate to statistics */ }
                        )
                    }
                    
                    item {
                        Text(
                            text = "Machines Due for Inspection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    
                    if (state.machinesDue.isEmpty()) {
                        item {
                            Text(
                                text = "No machines due for inspection",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(state.machinesDue.take(5)) { machine ->
                            MachineDueCard(
                                machine = machine,
                                onClick = {
                                    navController.navigate("${Screen.MachineDetail.route}/${machine.id}")
                                }
                            )
                        }
                        
                        if (state.machinesDue.size > 5) {
                            item {
                                TextButton(
                                    onClick = { navController.navigate(Screen.Machines.route) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("View all ${state.machinesDue.size} machines due for inspection")
                                }
                            }
                        }
                    }
                    
                    item {
                        Text(
                            text = "Open Abnormalities",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    
                    if (state.abnormalities.isEmpty()) {
                        item {
                            Text(
                                text = "No open abnormalities",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(state.abnormalities.take(5)) { inspection ->
                            AbnormalityCard(
                                inspection = inspection,
                                onClick = {
                                    navController.navigate("${Screen.InspectionDetail.route}/${inspection.id}")
                                }
                            )
                        }
                        
                        if (state.abnormalities.size > 5) {
                            item {
                                TextButton(
                                    onClick = { navController.navigate(Screen.Inspections.route) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("View all ${state.abnormalities.size} abnormalities")
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardSummaryCard(
    machinesDueCount: Int,
    abnormalitiesCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "LLF Inspection Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    icon = Icons.Filled.Build,
                    count = machinesDueCount,
                    label = "Machines Due",
                    color = MaterialTheme.colorScheme.primary
                )
                
                SummaryItem(
                    icon = Icons.Filled.Warning,
                    count = abnormalitiesCount,
                    label = "Abnormalities",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun MachineDueCard(
    machine: Machine,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Build,
                contentDescription = "Machine",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = machine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${machine.category.name.replace('_', ' ')} - ${machine.section.name.replace('_', ' ')}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            val daysText = when {
                machine.daysUntilNextInspection() < 0 -> "Overdue by ${-machine.daysUntilNextInspection()} days"
                machine.daysUntilNextInspection() == 0 -> "Due today"
                else -> "Due in ${machine.daysUntilNextInspection()} days"
            }
            
            val daysColor = when {
                machine.daysUntilNextInspection() < 0 -> MaterialTheme.colorScheme.error
                machine.daysUntilNextInspection() <= 1 -> MaterialTheme.colorScheme.error
                machine.daysUntilNextInspection() <= 3 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            }
            
            Text(
                text = daysText,
                style = MaterialTheme.typography.bodyMedium,
                color = daysColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AbnormalityCard(
    inspection: Inspection,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Assignment,
                contentDescription = "Inspection",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Inspection #${inspection.id.takeLast(6)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                val abnormalityText = when {
                    inspection.lookStatus == com.akash.llfproject.data.InspectionStatus.NOT_OK -> "Look: ${inspection.lookNotes.take(20)}${if (inspection.lookNotes.length > 20) "..." else ""}"
                    inspection.listenStatus == com.akash.llfproject.data.InspectionStatus.NOT_OK -> "Listen: ${inspection.listenNotes.take(20)}${if (inspection.listenNotes.length > 20) "..." else ""}"
                    inspection.feelStatus == com.akash.llfproject.data.InspectionStatus.NOT_OK -> "Feel: ${inspection.feelNotes.take(20)}${if (inspection.feelNotes.length > 20) "..." else ""}"
                    else -> "Abnormality detected"
                }
                
                Text(
                    text = abnormalityText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            val statusColor = when (inspection.abnormalityStatus) {
                com.akash.llfproject.data.AbnormalityStatus.OPEN -> MaterialTheme.colorScheme.error
                com.akash.llfproject.data.AbnormalityStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
                com.akash.llfproject.data.AbnormalityStatus.RESOLVED -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.secondary
            }
            
            Text(
                text = inspection.abnormalityStatus.name,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}