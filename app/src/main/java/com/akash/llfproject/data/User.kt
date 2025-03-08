package com.akash.llfproject.data

import com.google.firebase.Timestamp

enum class UserRole {
    MANAGEMENT, // UH/FH/LH - Can view all reports & statistics and perform LLF if needed
    DEPARTMENT_HEAD, // DH - Approve engineers, receive notifications
    SECTION_HEAD, // SH - Approve engineers, receive notifications
    AREA_HEAD, // AH - Approve engineers, receive notifications
    ENGINEER, // Can conduct LLF inspections & close abnormalities
    WORKMAN // Can conduct LLF rounds but cannot close abnormalities
}

data class User(
    val id: String = "", // Firebase Auth UID
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.WORKMAN,
    val department: String = "",
    val section: String = "",
    val area: String = "",
    val isApproved: Boolean = false, // For engineers requiring approval
    val createdAt: Timestamp = Timestamp.now(),
    val profilePhotoUrl: String? = null
) {
    // Check if user has admin privileges (Management, DH, SH, AH)
    fun isAdmin(): Boolean {
        return role == UserRole.MANAGEMENT ||
               role == UserRole.DEPARTMENT_HEAD ||
               role == UserRole.SECTION_HEAD ||
               role == UserRole.AREA_HEAD
    }
    
    // Check if user can approve engineers
    fun canApproveEngineers(): Boolean {
        return role == UserRole.DEPARTMENT_HEAD ||
               role == UserRole.SECTION_HEAD ||
               role == UserRole.AREA_HEAD ||
               role == UserRole.MANAGEMENT
    }
    
    // Check if user can close abnormalities
    fun canCloseAbnormalities(): Boolean {
        return role == UserRole.ENGINEER || role == UserRole.MANAGEMENT
    }
}