package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("divitask_prefs", Context.MODE_PRIVATE)

    private val _currentDivision = MutableStateFlow(getCurrentDivisionInternal())
    val currentDivision: StateFlow<String> = _currentDivision.asStateFlow()

    private val _currentUserRole = MutableStateFlow(getCurrentUserRoleInternal())
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    private val _gasUrl = MutableStateFlow(getGasUrlInternal())
    val gasUrl: StateFlow<String> = _gasUrl.asStateFlow()

    private val _reminderEnabled = MutableStateFlow(getReminderEnabledInternal())
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    private fun getCurrentDivisionInternal(): String {
        return prefs.getString("current_division", "") ?: ""
    }

    private fun getCurrentUserRoleInternal(): String {
        return prefs.getString("current_user_role", "") ?: ""
    }

    private fun getGasUrlInternal(): String {
        return prefs.getString("gas_url", "") ?: ""
    }

    private fun getReminderEnabledInternal(): Boolean {
        return prefs.getBoolean("reminder_enabled", true)
    }

    fun login(division: String, role: String) {
        prefs.edit().apply {
            putString("current_division", division)
            putString("current_user_role", role)
            apply()
        }
        _currentDivision.value = division
        _currentUserRole.value = role
    }

    fun logout() {
        prefs.edit().apply {
            putString("current_division", "")
            putString("current_user_role", "")
            apply()
        }
        _currentDivision.value = ""
        _currentUserRole.value = ""
    }

    fun setGasUrl(url: String) {
        prefs.edit().putString("gas_url", url).apply()
        _gasUrl.value = url
    }

    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("reminder_enabled", enabled).apply()
        _reminderEnabled.value = enabled
    }
}
