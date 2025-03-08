package com.akash.llfproject.data

import com.google.firebase.Timestamp

enum class InspectionFrequency {
    DAILY, WEEKLY, MONTHLY
}

enum class MachineCategory {
    FANS, BLOWERS, PUMPS, ROLLERS, HYDRAULIC_PRESS, STATIC_EQUIPMENT, OTHER
}

enum class MachineSection {
    SPINNING, AUXILIARY, VISCOSE, ENERGY_CENTERS, CS2_PLANT, ACID_PLANT, ETP, OTHER
}

data class Machine(
    val id: String = "", // Unique machine ID
    val name: String = "", // Machine name
    val category: MachineCategory = MachineCategory.OTHER,
    val section: MachineSection = MachineSection.OTHER,
    val subCategory: String = "", // Additional categorization if needed
    val imageUrl: String = "", // URL to the machine image in Firebase Storage
    val inspectionFrequency: InspectionFrequency = InspectionFrequency.WEEKLY,
    val lastInspection: Timestamp? = null, // When was the last inspection performed
    val nextInspectionDue: Timestamp? = null, // When is the next inspection due
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String = "", // User ID who created this machine entry
    val isActive: Boolean = true // Whether the machine is active or decommissioned
) {
    // Calculate if inspection is due based on current time
    fun isInspectionDue(): Boolean {
        if (nextInspectionDue == null) return true
        return Timestamp.now().seconds >= nextInspectionDue.seconds
    }
    
    // Calculate days remaining until next inspection
    fun daysUntilNextInspection(): Int {
        if (nextInspectionDue == null) return 0
        val currentTimeSeconds = Timestamp.now().seconds
        val dueTimeSeconds = nextInspectionDue.seconds
        val secondsDifference = dueTimeSeconds - currentTimeSeconds
        
        // Convert seconds to days (86400 seconds in a day)
        return (secondsDifference / 86400).toInt()
    }
}