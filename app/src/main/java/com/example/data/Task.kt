package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val division: String, // "Marketing", "HR", "IT", "Finance"
    val status: String,    // "PENDING", "IN_PROGRESS", "COMPLETED"
    val priority: String,  // "HIGH", "MEDIUM", "LOW"
    val dueDate: String,   // YYYY-MM-DD
    val dueTime: String,   // HH:mm
    val reminderMinutes: Int, // minutes before dueDate & dueTime
    val assignedTo: String,
    val lastUpdated: Long,
    val isSynced: Boolean = false
) : Serializable
