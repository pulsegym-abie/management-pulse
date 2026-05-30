package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.receiver.TaskReminderReceiver
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface SyncUiState {
    object Idle : SyncUiState
    object Loading : SyncUiState
    data class Success(val message: String) : SyncUiState
    data class Error(val error: String) : SyncUiState
}

class TaskViewModel(
    private val repository: TaskRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    // Login Preferences State
    val currentDivision: StateFlow<String> = preferenceManager.currentDivision
    val currentUserRole: StateFlow<String> = preferenceManager.currentUserRole
    val gasUrl: StateFlow<String> = preferenceManager.gasUrl
    val reminderEnabled: StateFlow<Boolean> = preferenceManager.reminderEnabled

    // Tasks Flows
    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered tasks for logged-in division or selected calendar day
    private val _selectedCalendarDate = MutableStateFlow("")
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    // Sync status indicators
    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    init {
        // Run seed on startup to make sure there are initial tasks for beautiful display
        viewModelScope.launch {
            repository.seedDefaultTasks()
        }
    }

    fun setSelectedDate(date: String) {
        _selectedCalendarDate.value = date
    }

    fun setGasUrl(url: String) {
        preferenceManager.setGasUrl(url)
    }

    fun setReminderEnabled(enabled: Boolean) {
        preferenceManager.setReminderEnabled(enabled)
    }

    fun login(division: String, role: String) {
        preferenceManager.login(division, role)
    }

    fun logout() {
        preferenceManager.logout()
    }

    fun addTask(
        title: String,
        description: String,
        division: String,
        priority: String,
        dueDate: String,
        dueTime: String,
        reminderMinutes: Int,
        assignedTo: String,
        context: Context
    ) {
        viewModelScope.launch {
            val task = Task(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                division = division,
                status = "PENDING",
                priority = priority,
                dueDate = dueDate,
                dueTime = dueTime,
                reminderMinutes = reminderMinutes,
                assignedTo = assignedTo,
                lastUpdated = System.currentTimeMillis(),
                isSynced = false
            )
            repository.insert(task)
            
            // Schedule Alarm if enabled
            if (preferenceManager.reminderEnabled.value) {
                TaskReminderReceiver.scheduleReminder(context, task)
            }
            
            // Auto sync if GAS is configured
            if (preferenceManager.gasUrl.value.isNotBlank()) {
                syncTasksSilently()
            }
        }
    }

    fun editTask(
        id: String,
        title: String,
        description: String,
        division: String,
        priority: String,
        dueDate: String,
        dueTime: String,
        reminderMinutes: Int,
        assignedTo: String,
        status: String,
        context: Context
    ) {
        viewModelScope.launch {
            val oldTask = repository.getTaskById(id)
            if (oldTask != null) {
                TaskReminderReceiver.cancelReminder(context, oldTask)
            }

            val updatedTask = Task(
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
                lastUpdated = System.currentTimeMillis(),
                isSynced = false
            )
            repository.insert(updatedTask)

            if (preferenceManager.reminderEnabled.value && status != "COMPLETED") {
                TaskReminderReceiver.scheduleReminder(context, updatedTask)
            }

            if (preferenceManager.gasUrl.value.isNotBlank()) {
                syncTasksSilently()
            }
        }
    }

    fun updateTaskStatus(task: Task, newStatus: String, context: Context) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                status = newStatus,
                lastUpdated = System.currentTimeMillis(),
                isSynced = false
            )
            repository.insert(updatedTask)

            if (newStatus == "COMPLETED") {
                TaskReminderReceiver.cancelReminder(context, task)
            } else if (preferenceManager.reminderEnabled.value) {
                TaskReminderReceiver.scheduleReminder(context, updatedTask)
            }

            if (preferenceManager.gasUrl.value.isNotBlank()) {
                syncTasksSilently()
            }
        }
    }

    fun deleteTask(task: Task, context: Context) {
        viewModelScope.launch {
            repository.delete(task.id)
            TaskReminderReceiver.cancelReminder(context, task)
            
            if (preferenceManager.gasUrl.value.isNotBlank()) {
                syncTasksSilently()
            }
        }
    }

    fun syncTasks() {
        viewModelScope.launch {
            _syncState.value = SyncUiState.Loading
            val url = preferenceManager.gasUrl.value
            val result = repository.syncWithSheets(url)
            result.onSuccess { msg ->
                _syncState.value = SyncUiState.Success(msg)
            }
            result.onFailure { exc ->
                _syncState.value = SyncUiState.Error(exc.localizedMessage ?: "Gagal terhubung dengan server Google Apps Script.")
            }
        }
    }

    private fun syncTasksSilently() {
        viewModelScope.launch {
            val url = preferenceManager.gasUrl.value
            repository.syncWithSheets(url)
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncUiState.Idle
    }
}

class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
