package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.theme.*
import com.example.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormDialog(
    taskToEdit: Task? = null,
    defaultDivision: String = "IT",
    onDismiss: () -> Unit,
    viewModel: TaskViewModel
) {
    val context = LocalContext.current
    val isEditMode = taskToEdit != null

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var division by remember { mutableStateOf(taskToEdit?.division ?: defaultDivision) }
    var priority by remember { mutableStateOf(taskToEdit?.priority ?: "MEDIUM") }
    var dueDate by remember { mutableStateOf(taskToEdit?.dueDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var dueTime by remember { mutableStateOf(taskToEdit?.dueTime ?: "09:00") }
    var reminderMinutes by remember { mutableStateOf(taskToEdit?.reminderMinutes ?: 30) }
    var assignedTo by remember { mutableStateOf(taskToEdit?.assignedTo ?: "") }
    var status by remember { mutableStateOf(taskToEdit?.status ?: "PENDING") }

    var showError by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()

    // Division options
    val divisions = listOf("Marketing", "HR", "IT", "Finance")

    // Priority configuration
    val priorities = listOf("HIGH" to PriorityHigh, "MEDIUM" to PriorityMedium, "LOW" to PriorityLow)

    // Reminders intervals
    val reminders = listOf(
        0 to "Tanpa Pengingat",
        15 to "15 Menit Sebelum",
        30 to "30 Menit Sebelum",
        60 to "1 Jam Sebelum"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "Sunting Tugas Tim" else "Tugas Kolaborasi Baru",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; showError = false },
                    label = { Text("Nama / Judul Tugas") },
                    leadingIcon = { Icon(Icons.Default.Task, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("form_title_input"),
                    singleLine = true
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Instruksi / Detail Tugas") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("form_desc_input"),
                    maxLines = 4
                )

                // Assigned Person
                OutlinedTextField(
                    value = assignedTo,
                    onValueChange = { assignedTo = it; showError = false },
                    label = { Text("Penanggung Jawab (Penerima Tugas)") },
                    leadingIcon = { Icon(Icons.Default.AssignmentInd, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("form_assigned_input"),
                    singleLine = true,
                    placeholder = { Text("Contoh: Yusuf (Backend Dev)") }
                )

                // Division Selector
                Column {
                    Text(
                        text = "Divisi Penyelenggara",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        divisions.forEach { div ->
                            val isDivSelected = division == div
                            val divColor = when(div) {
                                "Marketing" -> ColorMarketing
                                "HR" -> ColorHR
                                "IT" -> ColorIT
                                else -> ColorFinance
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDivSelected) divColor else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { division = div }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = div,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isDivSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isDivSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Priority Selection
                Column {
                    Text(
                        text = "Tingkat Prioritas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        priorities.forEach { (level, color) ->
                            val isSelected = priority == level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { priority = level }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = level,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Status Selection (Only when editing)
                if (isEditMode) {
                    Column {
                        Text(
                            text = "Status Penyelesaian",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("PENDING", "IN_PROGRESS", "COMPLETED").forEach { stat ->
                                val isSelected = status == stat
                                val statColor = when(stat) {
                                    "PENDING" -> ColorMarketing
                                    "IN_PROGRESS" -> ColorIT
                                    else -> ColorFinance
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) statColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { status = stat }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stat,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) statColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Due Date & Time Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date picker action card
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tenggat Tanggal",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val cal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, y)
                                                set(Calendar.MONTH, m)
                                                set(Calendar.DAY_OF_MONTH, d)
                                            }
                                            dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(dueDate, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Time Picker action card
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tenggat Waktu",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    val parts = dueTime.split(":")
                                    val hr = parts.firstOrNull()?.toIntOrNull() ?: 9
                                    val min = parts.lastOrNull()?.toIntOrNull() ?: 0
                                    TimePickerDialog(
                                        context,
                                        { _, selectedHr, selectedMin ->
                                            dueTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHr, selectedMin)
                                        },
                                        hr,
                                        min,
                                        true
                                    ).show()
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(dueTime, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Automatic Reminder Dropdown card set
                Column {
                    Text(
                        text = "Pengingat Otomatis Alarm",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("reminder_row"),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        reminders.forEach { (minutes, label) ->
                            val isSelected = reminderMinutes == minutes
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { reminderMinutes = minutes }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label.replace(" Sebelum", "\nSebelum"),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }

                if (showError) {
                    Text(
                        text = "Harap isi nama tugas dan penanggung jawab.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || assignedTo.isBlank()) {
                        showError = true
                    } else {
                        if (isEditMode) {
                            viewModel.editTask(
                                id = taskToEdit!!.id,
                                title = title,
                                description = description,
                                division = division,
                                priority = priority,
                                dueDate = dueDate,
                                dueTime = dueTime,
                                reminderMinutes = reminderMinutes,
                                assignedTo = assignedTo,
                                status = status,
                                context = context
                            )
                        } else {
                            viewModel.addTask(
                                title = title,
                                description = description,
                                division = division,
                                priority = priority,
                                dueDate = dueDate,
                                dueTime = dueTime,
                                reminderMinutes = reminderMinutes,
                                assignedTo = assignedTo,
                                context = context
                            )
                        }
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_form_button")
            ) {
                Text(if (isEditMode) "Simpan Judul" else "Delegasikan Tugas")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
