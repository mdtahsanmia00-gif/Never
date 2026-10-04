package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueTimestamp: Long,
    val isCompleted: Boolean = false,
    val completedTimestamp: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    val category: TaskCategory = TaskCategory.WORK,
    val isPermanentNotification: Boolean = true,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationRadiusMeters: Float = 200f,
    val locationTrigger: LocationTrigger = LocationTrigger.NONE,
    val createdAt: Long = System.currentTimeMillis(),
    val isLocationTriggered: Boolean = false
) {
    val isOverdue: Boolean
        get() = !isCompleted && dueTimestamp > 0 && dueTimestamp < System.currentTimeMillis()

    val hasLocation: Boolean
        get() = !locationName.isNullOrBlank() && latitude != null && longitude != null && locationTrigger != LocationTrigger.NONE
}
