package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GasSyncResponse(
    val success: Boolean = false,
    val message: String? = null,
    val tasks: List<GasTask>? = null
)

@JsonClass(generateAdapter = true)
data class GasTask(
    val id: String,
    val title: String,
    val description: String,
    val division: String,
    val status: String,
    val priority: String,
    val dueDate: String,
    val dueTime: String,
    val reminderMinutes: Int,
    val assignedTo: String,
    val lastUpdated: Long
) {
    fun toTask(): Task = Task(
        id = id,
        title = title,
        description = description,
        division = division,
        status = status,
        priority = priority,
        dueDate = dueDate,
        dueTime = dueTime,
        reminderMinutes = reminderMinutes,
        assignedTo = assignedTo,
        lastUpdated = lastUpdated,
        isSynced = true
    )
}

fun Task.toGasTask(): GasTask = GasTask(
    id = id,
    title = title,
    description = description,
    division = division,
    status = status,
    priority = priority,
    dueDate = dueDate,
    dueTime = dueTime,
    reminderMinutes = reminderMinutes,
    assignedTo = assignedTo,
    lastUpdated = lastUpdated
)
