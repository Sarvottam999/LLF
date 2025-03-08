package com.akash.llfproject.ui.machine

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.akash.llfproject.data.InspectionFrequency
import com.akash.llfproject.data.MachineCategory
import com.akash.llfproject.data.MachineSection
import com.akash.llfproject.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMachineScreen(
    navController: NavController,
    viewModel: MachineViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var subCategory by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(MachineCategory.ELECTRICAL) }
    var selectedSection by remember { mutableStateOf(MachineSection.SECTION_A) }
    var selectedFrequency by remember { mutableStateOf(InspectionFrequency.DAILY) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Category dropdown
    var showCategoryDropdown by remember { mutableStateOf(false) }
    
    // Section dropdown
    var showSectionDropdown by remember { mutableStateOf(false) }
    
    // Frequency dropdown
    var showFrequencyDropdown by remember { mutableStateOf(false) }
    
    // Image picker
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Machine") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Machine image
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Machine Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = "Add Photo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Add Machine Image",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Machine name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Machine Name") },
                leadingIcon = {
                    Icon(Icons.Filled.Build, contentDescription = "Machine Name")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Machine category
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory.name.replace('_', ' ').lowercase().capitalize(),
                    onValueChange = { },
                    label = { Text("Category") },
                    leadingIcon = {
                        Icon(Icons.Filled.Category, contentDescription = "Category")
                    },
                    trailingIcon = {
                        IconButton(onClick = { showCategoryDropdown = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Show categories")
                        }
                    },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                DropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    MachineCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name.replace('_', ' ').lowercase().capitalize()) },
                            onClick = {
                                selectedCategory = category
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Machine section
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedSection.name.replace('_', ' ').lowercase().capitalize(),
                    onValueChange = { },
                    label = { Text("Section") },
                    leadingIcon = {
                        Icon(Icons.Filled.Domain, contentDescription = "Section")
                    },
                    trailingIcon = {
                        IconButton(onClick = { showSectionDropdown = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Show sections")
                        }
                    },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                DropdownMenu(
                    expanded = showSectionDropdown,
                    onDismissRequest = { showSectionDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    MachineSection.values().forEach { section ->
                        DropdownMenuItem(
                            text = { Text(section.name.replace('_', ' ').lowercase().capitalize()) },
                            onClick = {
                                selectedSection = section
                                showSectionDropdown = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Machine sub-category
            OutlinedTextField(
                value = subCategory,
                onValueChange = { subCategory = it },
                label = { Text("Sub-Category") },
                leadingIcon = {
                    Icon(Icons.Filled.Label, contentDescription = "Sub-Category")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Inspection frequency
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedFrequency.displayName,
                    onValueChange = { },
                    label = { Text("Inspection Frequency") },
                    leadingIcon = {
                        Icon(Icons.Filled.Schedule, contentDescription = "Frequency")
                    },
                    trailingIcon = {
                        IconButton(onClick = { showFrequencyDropdown = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Show frequencies")
                        }
                    },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                DropdownMenu(
                    expanded = showFrequencyDropdown,
                    onDismissRequest = { showFrequencyDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    InspectionFrequency.values().forEach { frequency ->
                        DropdownMenuItem(
                            text = { Text(frequency.displayName) },
                            onClick = {
                                selectedFrequency = frequency
                                showFrequencyDropdown = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
            
            // Add machine button
            Button(
                onClick = {
                    if (validateInputs(name, imageUri)) {
                        isLoading = true
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        
                        viewModel.addMachine(
                            name = name,
                            category = selectedCategory,
                            section = selectedSection,
                            subCategory = subCategory,
                            inspectionFrequency = selectedFrequency,
                            imageUri = imageUri!!,
                            userId = userId,
                            onSuccess = {
                                isLoading = false
                                navController.navigate(Screen.Machines.route) {
                                    popUpTo(Screen.AddMachine.route) { inclusive = true }
                                }
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        )
                    } else {
                        errorMessage = "Please fill all required fields and add an image"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Machine")
                }
            }
        }
    }
}

private fun validateInputs(name: String, imageUri: Uri?): Boolean {
    return name.isNotBlank() && imageUri != null
}