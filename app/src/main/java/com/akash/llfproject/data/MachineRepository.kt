package com.akash.llfproject.data

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MachineRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val machinesCollection = firestore.collection("machines")
    
    // Add a new machine
    suspend fun addMachine(
        name: String,
        category: MachineCategory,
        section: MachineSection,
        subCategory: String,
        inspectionFrequency: InspectionFrequency,
        imageUri: Uri,
        userId: String
    ): Result<Machine> {
        return try {
            // Upload image first
            val imageId = UUID.randomUUID().toString()
            val imageRef = storage.reference.child("machine_images/$imageId")
            val uploadTask = imageRef.putFile(imageUri).await()
            val imageUrl = imageRef.downloadUrl.await().toString()
            
            // Calculate next inspection due date based on frequency
            val now = Timestamp.now()
            val nextInspectionDue = calculateNextInspectionDate(now, inspectionFrequency)
            
            // Create machine document
            val machineId = UUID.randomUUID().toString()
            val machine = Machine(
                id = machineId,
                name = name,
                category = category,
                section = section,
                subCategory = subCategory,
                imageUrl = imageUrl,
                inspectionFrequency = inspectionFrequency,
                lastInspection = null,
                nextInspectionDue = nextInspectionDue,
                createdAt = now,
                createdBy = userId,
                isActive = true
            )
            
            // Save to Firestore
            machinesCollection.document(machineId).set(machine).await()
            Result.success(machine)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get a machine by ID
    suspend fun getMachine(machineId: String): Result<Machine> {
        return try {
            val documentSnapshot = machinesCollection.document(machineId).get().await()
            val machine = documentSnapshot.toObject(Machine::class.java)
            if (machine != null) {
                Result.success(machine)
            } else {
                Result.failure(Exception("Machine not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update machine details
    suspend fun updateMachine(
        machineId: String,
        name: String,
        category: MachineCategory,
        section: MachineSection,
        subCategory: String,
        inspectionFrequency: InspectionFrequency,
        imageUri: Uri?
    ): Result<Machine> {
        return try {
            // Get current machine data
            val machineResult = getMachine(machineId)
            if (machineResult.isFailure) {
                return Result.failure(machineResult.exceptionOrNull() ?: Exception("Failed to get machine"))
            }
            
            val currentMachine = machineResult.getOrThrow()
            var imageUrl = currentMachine.imageUrl
            
            // Upload new image if provided
            if (imageUri != null) {
                val imageId = UUID.randomUUID().toString()
                val imageRef = storage.reference.child("machine_images/$imageId")
                val uploadTask = imageRef.putFile(imageUri).await()
                imageUrl = imageRef.downloadUrl.await().toString()
            }
            
            // Update machine document
            val updatedMachine = currentMachine.copy(
                name = name,
                category = category,
                section = section,
                subCategory = subCategory,
                inspectionFrequency = inspectionFrequency,
                imageUrl = imageUrl
            )
            
            // Save to Firestore
            machinesCollection.document(machineId).set(updatedMachine).await()
            Result.success(updatedMachine)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete a machine
    suspend fun deleteMachine(machineId: String): Result<Unit> {
        return try {
            // Get machine to delete its image
            val machineResult = getMachine(machineId)
            if (machineResult.isSuccess) {
                val machine = machineResult.getOrThrow()
                // Delete image from storage if it exists
                if (machine.imageUrl.isNotEmpty()) {
                    try {
                        val imageRef = storage.getReferenceFromUrl(machine.imageUrl)
                        imageRef.delete().await()
                    } catch (e: Exception) {
                        // Continue even if image deletion fails
                    }
                }
            }
            
            // Delete from Firestore
            machinesCollection.document(machineId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get all machines
    suspend fun getAllMachines(): Result<List<Machine>> {
        return try {
            val querySnapshot = machinesCollection
                .whereEqualTo("isActive", true)
                .orderBy("name")
                .get()
                .await()
                
            val machines = querySnapshot.documents.mapNotNull { 
                it.toObject(Machine::class.java) 
            }
            Result.success(machines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get machines by category
    suspend fun getMachinesByCategory(category: MachineCategory): Result<List<Machine>> {
        return try {
            val querySnapshot = machinesCollection
                .whereEqualTo("category", category)
                .whereEqualTo("isActive", true)
                .orderBy("name")
                .get()
                .await()
                
            val machines = querySnapshot.documents.mapNotNull { 
                it.toObject(Machine::class.java) 
            }
            Result.success(machines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get machines by section
    suspend fun getMachinesBySection(section: MachineSection): Result<List<Machine>> {
        return try {
            val querySnapshot = machinesCollection
                .whereEqualTo("section", section)
                .whereEqualTo("isActive", true)
                .orderBy("name")
                .get()
                .await()
                
            val machines = querySnapshot.documents.mapNotNull { 
                it.toObject(Machine::class.java) 
            }
            Result.success(machines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get machines due for inspection
    suspend fun getMachinesDueForInspection(): Result<List<Machine>> {
        return try {
            val now = Timestamp.now()
            val querySnapshot = machinesCollection
                .whereEqualTo("isActive", true)
                .whereLessThanOrEqualTo("nextInspectionDue", now)
                .orderBy("nextInspectionDue")
                .get()
                .await()
                
            val machines = querySnapshot.documents.mapNotNull { 
                it.toObject(Machine::class.java) 
            }
            Result.success(machines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Search machines by name
    suspend fun searchMachines(query: String): Result<List<Machine>> {
        return try {
            // Firebase doesn't support direct text search, so we'll fetch all and filter
            val allMachinesResult = getAllMachines()
            if (allMachinesResult.isFailure) {
                return Result.failure(allMachinesResult.exceptionOrNull() ?: Exception("Failed to get machines"))
            }
            
            val allMachines = allMachinesResult.getOrThrow()
            val filteredMachines = allMachines.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.id.contains(query, ignoreCase = true) || 
                it.subCategory.contains(query, ignoreCase = true)
            }
            
            Result.success(filteredMachines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update machine inspection status
    suspend fun updateMachineInspectionStatus(
        machineId: String, 
        lastInspection: Timestamp
    ): Result<Machine> {
        return try {
            // Get current machine
            val machineResult = getMachine(machineId)
            if (machineResult.isFailure) {
                return Result.failure(machineResult.exceptionOrNull() ?: Exception("Failed to get machine"))
            }
            
            val machine = machineResult.getOrThrow()
            
            // Calculate next inspection due date
            val nextInspectionDue = calculateNextInspectionDate(lastInspection, machine.inspectionFrequency)
            
            // Update machine
            val updatedMachine = machine.copy(
                lastInspection = lastInspection,
                nextInspectionDue = nextInspectionDue
            )
            
            // Save to Firestore
            machinesCollection.document(machineId).set(updatedMachine).await()
            Result.success(updatedMachine)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper function to calculate next inspection date based on frequency
    private fun calculateNextInspectionDate(
        fromDate: Timestamp,
        frequency: InspectionFrequency
    ): Timestamp {
        val secondsInDay = 86400L
        val currentSeconds = fromDate.seconds
        
        val daysToAdd = when (frequency) {
            InspectionFrequency.DAILY -> 1
            InspectionFrequency.WEEKLY -> 7
            InspectionFrequency.MONTHLY -> 30
        }
        
        val newSeconds = currentSeconds + (daysToAdd * secondsInDay)
        return Timestamp(newSeconds, 0)
    }
}