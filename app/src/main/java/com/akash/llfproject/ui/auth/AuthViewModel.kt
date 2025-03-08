package com.akash.llfproject.ui.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akash.llfproject.auth.AuthRepository
import com.akash.llfproject.data.User
import com.akash.llfproject.data.UserRole
import kotlinx.coroutines.launch

// Represents the different states of authentication
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    
    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState
    
    init {
        // Check if user is already logged in
        if (authRepository.isUserLoggedIn()) {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                getUserProfile(currentUser.uid)
            }
        }
    }
    
    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.loginUser(email, password)
            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()
                if (firebaseUser != null) {
                    getUserProfile(firebaseUser.uid)
                } else {
                    _authState.value = AuthState.Error("Login failed")
                }
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }
    
    fun register(
        email: String,
        password: String,
        name: String,
        role: UserRole,
        department: String,
        section: String,
        area: String
    ) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.registerUser(
                email = email,
                password = password,
                name = name,
                role = role,
                department = department,
                section = section,
                area = area
            )
            
            if (result.isSuccess) {
                val user = result.getOrNull()
                if (user != null) {
                    _authState.value = AuthState.Success(user)
                } else {
                    _authState.value = AuthState.Error("Registration failed")
                }
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }
    
    fun logout() {
        authRepository.logout()
        _authState.value = AuthState.Idle
    }
    
    private fun getUserProfile(userId: String) {
        viewModelScope.launch {
            val result = authRepository.getUserProfile(userId)
            if (result.isSuccess) {
                val user = result.getOrNull()
                if (user != null) {
                    // Check if user is approved (for engineers)
                    if (user.role == UserRole.ENGINEER && !user.isApproved) {
                        _authState.value = AuthState.Error("Your account is pending approval")
                    } else {
                        _authState.value = AuthState.Success(user)
                    }
                } else {
                    _authState.value = AuthState.Error("Failed to get user profile")
                }
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to get user profile")
            }
        }
    }
    
    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to send password reset email")
            }
        }
    }
}