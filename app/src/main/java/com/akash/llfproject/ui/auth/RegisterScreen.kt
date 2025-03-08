package com.akash.llfproject.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.akash.llfproject.data.UserRole
import com.akash.llfproject.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedRole by remember { mutableStateOf(UserRole.WORKMAN) }
    var showRoleDropdown by remember { mutableStateOf(false) }
    
    LaunchedEffect(viewModel.authState.value) {
        when (val state = viewModel.authState.value) {
            is AuthState.Success -> {
                isLoading = false
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                isLoading = false
                errorMessage = state.message
            }
            is AuthState.Loading -> {
                isLoading = true
            }
            else -> {}
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 24.dp)
        )
        
        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            leadingIcon = {
                Icon(Icons.Filled.Person, contentDescription = "Name")
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true
        )
        
        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Filled.Email, contentDescription = "Email")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true
        )
        
        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
                Icon(Icons.Filled.Lock, contentDescription = "Password")
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true
        )
        
        // Confirm Password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(Icons.Filled.Lock, contentDescription = "Confirm Password")
            },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true
        )
        
        // Role selection
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedRole.name.replace('_', ' ').lowercase().capitalize(),
                onValueChange = { },
                label = { Text("Role") },
                leadingIcon = {
                    Icon(Icons.Filled.Work, contentDescription = "Role")
                },
                trailingIcon = {
                    IconButton(onClick = { showRoleDropdown = true }) {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Show roles")
                    }
                },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true
            )
            
            DropdownMenu(
                expanded = showRoleDropdown,
                onDismissRequest = { showRoleDropdown = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                UserRole.values().forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role.name.replace('_', ' ').lowercase().capitalize()) },
                        onClick = {
                            selectedRole = role
                            showRoleDropdown = false
                        }
                    )
                }
            }
        }
        
        // Department field
        OutlinedTextField(
            value = department,
            onValueChange = { department = it },
            label = { Text("Department") },
            leadingIcon = {
                Icon(Icons.Filled.Business, contentDescription = "Department")
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true
        )
        
        // Section field
        OutlinedTextField(
            value = section,
            onValueChange = { section = it },
            label = { Text("Section") },
            leadingIcon = {
                Icon(Icons.Filled.Domain, contentDescription = "Section")
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true
        )
        
        // Area field
        OutlinedTextField(
            value = area,
            onValueChange = { area = it },
            label = { Text("Area") },
            leadingIcon = {
                Icon(Icons.Filled.Place, contentDescription = "Area")
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
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
        
        // Register button
        Button(
            onClick = {
                errorMessage = validateInputs(email, password, confirmPassword, name, department, section, area)
                if (errorMessage == null) {
                    viewModel.register(
                        email = email,
                        password = password,
                        name = name,
                        role = selectedRole,
                        department = department,
                        section = section,
                        area = area
                    )
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
                Text("Register")
            }
        }
        
        // Login link
        TextButton(
            onClick = { navController.navigate(Screen.Login.route) },
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
        ) {
            Text("Already have an account? Login")
        }
    }
}

private fun validateInputs(
    email: String,
    password: String,
    confirmPassword: String,
    name: String,
    department: String,
    section: String,
    area: String
): String? {
    if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || 
        name.isBlank() || department.isBlank() || section.isBlank() || area.isBlank()) {
        return "All fields are required"
    }
    
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return "Please enter a valid email address"
    }
    
    if (password.length < 6) {
        return "Password must be at least 6 characters long"
    }
    
    if (password != confirmPassword) {
        return "Passwords do not match"
    }
    
    return null
}