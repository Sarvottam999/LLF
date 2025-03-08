package com.akash.llfproject.data

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class InspectionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val inspectionsCollection = firestore.collection("inspections")
    
    // Create a new inspection (can be saved as draft)
    suspend fun createInspection(
        machineId: String,
        userId: String,
        lookStatus: InspectionStatus,
        lookNotes: String,
        lookImageUri: Uri?,
        listenStatus: InspectionStatus,
        listenNotes: String,
        listenImageUri: Uri?,
        feelStatus: InspectionStatus,
        feelNotes: String,
        feelImageUri: Uri?,
        isDraft: Boolean
    ): Result<Inspection> {
        return try {
            // Upload images if provided
            var lookImageUrl: String? = null
            var listenImageUrl: String? = null
            var feelImageUrl: String? = null
            
            if (lookImageUri != null) {
                val imageId = UUID.randomUUID().toString()
                val imageRef = storage.reference.child("inspection_images/$imageId")
                imageRef.putFile(lookImageUri).await()
                lookImageUrl = imageRef.downloadUrl.await().toString()
            }
            
            if (listenImageUri != null) {
                val imageId = UUID.randomUUID().toString()
                val imageRef = storage.reference.child("inspection_images/$imageId")
                imageRef.putFile(listenImageUri).await()
                listenImageUrl = imageRef.downloadUrl.await().toString()
            }
            
            if (feelImageUri != null) {
                val imageId = UUID.randomUUID().toString()
                val imageRef = storage.reference.child("inspection_images/$imageId")
                imageRef.putFile(feelImageUri).await()
                feelImageUrl = imageRef.downloadUrl.await().toString()
            }
            
            // Determine if there's an abnormality
            val hasAbnormality = lookStatus == InspectionStatus.NOT_OK ||
                                listenStatus == InspectionStatus.NOT_OK ||
                                feelStatus == InspectionStatus.NOT_OK
            
            // Create inspection document
            val inspectionId = UUID.randomUUID().toString()
            val now = Timestamp.now()
            val inspection = Inspection(
                id = inspectionId,
                machineId = machineId,
                inspectedBy = userId,
                inspectionDate = now,
                lookStatus = lookStatus,
                lookNotes = lookNotes,
                lookImageUrl = lookImageUrl,
                listenStatus = listenStatus,
                listenNotes = listenNotes,
                listenImageUrl = listenImageUrl,
                feelStatus = feelStatus,
                feelNotes = feelNotes,
                feelImageUrl = feelImageUrl,
                hasAbnormality = hasAbnormality,
                abnormalityStatus = if (hasAbnormality) AbnormalityStatus.OPEN else AbnormalityStatus.CLOSED,
                isDraft = isDraft,
                lastUpdated = now
            )
            
            // Save to Firestore
            inspectionsCollection.document(inspectionId).set(inspection).await()
            
            // Update machine's last inspection date if not a draft
            if (!isDraft) {
                val machineRepository = MachineRepository()
                machineRepository.updateMachineInspectionStatus(machineId, now)
            }
            
            Result.success(inspection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get an inspection by ID
    suspend fun getInspection(inspectionId: String): Result<Inspection> {
        return try {
            val documentSnapshot = inspectionsCollection.document(inspectionId).get().await()
            val inspection = documentSnapshot.toObject(Inspection::class.java)
            if (inspection != null) {
                Result.success(inspection)
            } else {
                Result.failure(Exception("Inspection not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update an existing inspection
    suspend fun updateInspection(
        inspectionId: String,
        lookStatus: InspectionStatus,
        lookNotes: String,
        lookImageUri: Uri?,
        listenStatus: InspectionStatus,
        listenNotes: String,
        listenImageUri: Uri?,
        feelStatus: InspectionStatus,
        feelNotes: String,
        feelImageUri: Uri?,
        isDraft: Boolean
    ): Result<Inspection> {
        return try {
            // Get current inspection
            val inspectionResult = getInspection(inspectionId)
            if (inspectionResult.isFailure) {
                return Result.failure(inspectionResult.exceptionOrNull() ?: Exception("Failed to get inspection"))
            }
            
            val currentInspection = inspectionResult.getOrThrow()
            var lookImageUrl = currentInspection.lookImageUrl
            var listenImageUrl = currentInspection.listenImageUrl
            var feelImageUrl = currentInspection.feelImageUrl
            
            // Upload new images if provided
            if (lookImageUri != null) {
                val imageId = UUID.randomUUID().toString()
                val imageRef = storage.reference.child("inspection_images/$imageId")
                imageRef.putFile(lookImageUri).await()
                lookImageUrl = imageRef.downloadUrl.await().toString()
            }
            
            if (listenImageUri != null) {
                val imageId = UUID.randomUUID().toString()
                val imageRef = storage.reference.child("inspection_images/$imageId")
                imageRef.putFile(listenImageUri).await()
                listenImageUrl = imageRef.downloadUrl.await().toString()
            }
            
            if (feelImageUri != null) {
                val imageId = UUID.randomUUID().toString()
                val imageRef = storage.reference.child("inspection_images/$imageId")
                imageRef.putFile(feelImageUri).await()
                feelImageUrl = imageRef.downloadUrl.await().toString()
            }
            
            // Determine if there's an abnormality
            val hasAbnormality = lookStatus == InspectionStatus.NOT_OK ||
                                listenStatus == InspectionStatus.NOT_OK ||
                                feelStatus == InspectionStatus.NOT_OK
            
            // Update inspection
            val now = Timestamp.now()
            val updatedInspection = currentInspection.copy(
                lookStatus = lookStatus,
                lookNotes = lookNotes,
                lookImageUrl = lookImageUrl,
                listenStatus = listenStatus,
                listenNotes = listenNotes,
                listenImageUrl = listenImageUrl,
                feelStatus = feelStatus,
                feelNotes = feelNotes,
                feelImageUrl = feelImageUrl,
                hasAbnormality = hasAbnormality,
                // Only update abnormality status if it's changing from OK to NOT_OK
                abnormalityStatus = if (hasAbnormality && !currentInspection.hasAbnormality) {
                    AbnormalityStatus.OPEN
                } else {
                    currentInspection.abnormalityStatus
                },
                isDraft = isDraft,
                lastUpdated = now
            )
            
            // Save to Firestore
            inspectionsCollection.document(inspectionId).set(updatedInspection).await()
            
            // Update machine's last inspection date if completing a draft
            if (!isDraft && currentInspection.isDraft) {
                val machineRepository = MachineRepository()
                machineRepository.updateMachineInspectionStatus(currentInspection.machineId, now)
            }
            
            Result.success(updatedInspection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update abnormality status (for engineers to resolve issues)
    suspend fun updateAbnormalityStatus(
        inspectionId: String,
        status: AbnormalityStatus,
        userId: String,
        resolutionNotes: String,
        resolutionImageUri: Uri?
    ): Result<Inspection> {
        return try {
            // Get current inspection
            val inspectionResult = getInspection(inspectionId)
            if (inspectionResult.isFailure) {
                return Result.failure(inspectionResult.exceptionOrNull() ?: Exception("Failed to get inspection"))
            }
            
            val currentInspection = inspectionResult.getOrThrow()
            var resolutionImageUrl = currentInspection.abnormalityResolutionImageUrl
            
            // Upload resolution image if provided
            if (resolutionImageUri != null) {
                val imageId = UUID.randomUUID().toString()
                val imageRef = storage.reference.child("resolution_images/$imageId")
                imageRef.putFile(resolutionImageUri).await()
                resolutionImageUrl = imageRef.downloadUrl.await().toString()
            }
            
            // Update inspection
            val now = Timestamp.now()
            val updatedInspection = currentInspection.copy(
                abnormalityStatus = status,
                abnormalityClosedBy = if (status == AbnormalityStatus.CLOSED) userId else currentInspection.abnormalityClosedBy,
                abnormalityClosedDate = if (status == AbnormalityStatus.CLOSED) now else currentInspection.abnormalityClosedDate,
                abnormalityResolutionNotes = resolutionNotes,
                abnormalityResolutionImageUrl = resolutionImageUrl,
                lastUpdated = now
            )
            
            // Save to Firestore
            inspectionsCollection.document(inspectionId).set(updatedInspection).await()
            Result.success(updatedInspection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get all inspections for a machine
    suspend fun getInspectionsForMachine(machineId: String): Result<List<Inspection>> {
        return try {
            val querySnapshot = inspectionsCollection
                .whereEqualTo("machineId", machineId)
                .whereEqualTo("isDraft", false)
                .orderBy("inspectionDate", Query.Direction.DESCENDING)
                .get()
                .await()
                
            val inspections = querySnapshot.documents.mapNotNull { 
                it.toObject(Inspection::class.java) 
            }
            Result.success(inspections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get draft inspections for a user
    suspend fun getDraftInspections(userId: String): Result<List<Inspection>> {
        return try {
            val querySnapshot = inspectionsCollection
                .whereEqualTo("inspectedBy", userId)
                .whereEqualTo("isDraft", true)
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .get()
                .await()
                
            val inspections = querySnapshot.documents.mapNotNull { 
                it.toObject(Inspection::class.java) 
            }
            Result.success(inspections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get inspections with open abnormalities
    suspend fun getInspectionsWithOpenAbnormalities(): Result<List<Inspection>> {
        return try {
            val querySnapshot = inspectionsCollection
                .whereEqualTo("hasAbnormality", true)
                .whereNotEqualTo("abnormalityStatus", AbnormalityStatus.CLOSED)
                .whereEqualTo("isDraft", false)
                .orderBy("abnormalityStatus")
                .orderBy("inspectionDate")
                .get()
                .await()
                
            val inspections = querySnapshot.documents.mapNotNull { 
                it.toObject(Inspection::class.java) 
            }
            Result.success(inspections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get inspections by section (for area heads)
    suspend fun getInspectionsBySection(section: MachineSection): Result<List<Inspection>> {
        return try {
            // First get all machines in this section
            val machineRepository = MachineRepository()
            val machinesResult = machineRepository.getMachinesBySection(section)
            
            if (machinesResult.isFailure) {
                return Result.failure(machinesResult.exceptionOrNull() ?: Exception("Failed to get machines"))
            }
            
            val machines = machinesResult.getOrThrow()
            val machineIds = machines.map { it.id }
            
            // Then get all inspections for these machines
            val inspections = mutableListOf<Inspection>()
            
            for (machineId in machineIds) {
                val inspectionsResult = getInspectionsForMachine(machineId)
                if (inspectionsResult.isSuccess) {
                    inspections.addAll(inspectionsResult.getOrThrow())
                }
            }
            
            // Sort by date, newest first
            val sortedInspections = inspections.sortedByDescending { it.inspectionDate.seconds }
            Result.success(sortedInspections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete a draft inspection
    suspend fun deleteDraftInspection(inspectionId: String): Result<Unit> {
        return try {
            // Verify it's a draft
            val inspectionResult = getInspection(inspectionId)
            if (inspectionResult.isFailure) {
                return Result.failure(inspectionResult.exceptionOrNull() ?: Exception("Failed to get inspection"))
            }
            
            val inspection = inspectionResult.getOrThrow()
            if (!inspection.isDraft) {
                return Result.failure(Exception("Cannot delete a completed inspection"))
            }
            
            // Delete any associated images
            try {
                if (inspection.lookImageUrl != null) {
                    val imageRef = storage.getReferenceFromUrl(inspection.lookImageUrl)
                    imageRef.delete().await()
                }
                if (inspection.listenImageUrl != null) {
                    val imageRef = storage.getReferenceFromUrl(inspection.listenImageUrl)
                    imageRef.delete().await()
                }
                if (inspection.feelImageUrl != null) {
                    val imageRef = storage.getReferenceFromUrl(inspection.feelImageUrl)
                    imageRef.delete().await()
                }
            } catch (e: Exception) {
                // Continue even if image deletion fails
            }
            
            // Delete from Firestore
            inspectionsCollection.document(inspectionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}