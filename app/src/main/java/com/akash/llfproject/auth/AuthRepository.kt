package com.akash.llfproject.auth

import com.akash.llfproject.data.User
import com.akash.llfproject.data.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    
    // Get current logged in user
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    // Check if user is logged in
    fun isUserLoggedIn(): Boolean = auth.currentUser != null
    
    // Register a new user
    suspend fun registerUser(
        email: String, 
        password: String, 
        name: String,
        role: UserRole,
        department: String,
        section: String,
        area: String
    ): Result<User> {
        return try {
            // Create authentication account
            val authResult = auth.createUserWithEmailAndPassword(email, password).await() 
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Create user profile in Firestore
                val user = User(
                    id = firebaseUser.uid,
                    email = email,
                    name = name,
                    role = role,
                    department = department,
                    section = section,
                    area = area,
                    // Engineers need approval, other roles are auto-approved
                    isApproved = role != UserRole.ENGINEER,
                    createdAt = Timestamp.now()
                )
                
                // Save user to Firestore
                usersCollection.document(firebaseUser.uid).set(user).await()
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to create user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Login user
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get user profile from Firestore
    suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            val user = documentSnapshot.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update user approval status (for admins to approve engineers)
    suspend fun updateUserApprovalStatus(userId: String, isApproved: Boolean): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update("isApproved", isApproved)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get all engineers pending approval
    suspend fun getEngineersPendingApproval(): Result<List<User>> {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("role", UserRole.ENGINEER)
                .whereEqualTo("isApproved", false)
                .get()
                .await()
                
            val pendingEngineers = querySnapshot.documents.mapNotNull { 
                it.toObject(User::class.java) 
            }
            Result.success(pendingEngineers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Logout user
    fun logout() {
        auth.signOut()
    }
    
    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}