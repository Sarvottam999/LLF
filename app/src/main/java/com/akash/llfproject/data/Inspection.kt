package com.akash.llfproject.data

import com.google.firebase.Timestamp

enum class InspectionStatus {
    OK, NOT_OK
}

enum class AbnormalityStatus {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED
}

data class Inspection(
    val id: String = "", // Unique inspection ID
    val machineId: String = "", // Reference to the machine being inspected
    val inspectedBy: String = "", // User ID who performed the inspection
    val inspectionDate: Timestamp = Timestamp.now(),
    
    // LLF Parameters
    val lookStatus: InspectionStatus = InspectionStatus.OK,
    val lookNotes: String = "",
    val lookImageUrl: String? = null,
    
    val listenStatus: InspectionStatus = InspectionStatus.OK,
    val listenNotes: String = "",
    val listenImageUrl: String? = null,
    
    val feelStatus: InspectionStatus = InspectionStatus.OK,
    val feelNotes: String = "",
    val feelImageUrl: String? = null,
    
    // Abnormality tracking
    val hasAbnormality: Boolean = false,
    val abnormalityStatus: AbnormalityStatus = AbnormalityStatus.OPEN,
    val abnormalityClosedBy: String? = null,
    val abnormalityClosedDate: Timestamp? = null,
    val abnormalityResolutionNotes: String = "",
    val abnormalityResolutionImageUrl: String? = null,
    
    // Draft status
    val isDraft: Boolean = false,
    val lastUpdated: Timestamp = Timestamp.now()
) {
    // Check if this inspection has any abnormalities
    fun hasAnyAbnormality(): Boolean {
        return lookStatus == InspectionStatus.NOT_OK ||
               listenStatus == InspectionStatus.NOT_OK ||
               feelStatus == InspectionStatus.NOT_OK
    }
    
    // Check if this inspection is complete
    fun isComplete(): Boolean {
        return !isDraft
    }
    
    // Check if abnormality is closed
    fun isAbnormalityClosed(): Boolean {
        return abnormalityStatus == AbnormalityStatus.CLOSED
    }
    
    // Check if this inspection requires engineer attention
    fun requiresEngineerAttention(): Boolean {
        return hasAnyAbnormality() && 
               (abnormalityStatus == AbnormalityStatus.OPEN || 
                abnormalityStatus == AbnormalityStatus.IN_PROGRESS)
    }
}