package com.example.ui.screens

import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allTasks by viewModel.allTasks.collectAsState()
    val activeDate by viewModel.selectedCalendarDate.collectAsState()

    // Date calculations
    val calendar = remember { Calendar.getInstance() }
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) } // 0-11

    // Prepopulate active date if empty
    LaunchedEffect(Unit) {
        if (activeDate.isEmpty()) {
            val nowStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            viewModel.setSelectedDate(nowStr)
        }
    }

    // Helper functions for month name
    val monthNames = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    // Calculate days of the selected month
    val daysInMonth = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, currentYear)
        cal.set(Calendar.MONTH, currentMonth)
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, currentYear)
        cal.set(Calendar.MONTH, currentMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val day = cal.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Monday = 2...
        // Convert to 0-indexed (Monday = 0 ... Sunday = 6) for our grid
        if (day == Calendar.SUNDAY) 6 else day - 2
    }

    // Tasks map categorized by date strings (YYYY-MM-DD)
    val tasksByDate = remember(allTasks) {
        allTasks.groupBy { it.dueDate }
    }

    // Selected day tasks
    val filteredTasks = remember(allTasks, activeDate) {
        allTasks.filter { it.dueDate == activeDate }
    }

    // Task editing form state
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var showForm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Calendar Control Layout
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Year/Month Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (currentMonth == 0) {
                            currentMonth = 11
                            currentYear -= 1
                        } else {
                            currentMonth -= 1
                        }
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan Sebelumnya")
                    }

                    Text(
                        text = "${monthNames[currentMonth]} $currentYear",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(onClick = {
                        if (currentMonth == 11) {
                            currentMonth = 0
                            currentYear += 1
                        } else {
                            currentMonth += 1
                        }
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Bulan Selanjutnya")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Days of the week header labels
                val dayLabels = listOf("S", "S", "R", "K", "J", "S", "M")
                Row(modifier = Modifier.fillMaxWidth()) {
                    dayLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Grid items
                val totalSectors = 42 // 6 rows of 7 days
                var currentDayNum = 1

                for (row in 0 until 6) {
                    if (currentDayNum > daysInMonth) break

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 7) {
                            val sectorIdx = row * 7 + col
                            val isEmptySector = sectorIdx < firstDayOfWeek || currentDayNum > daysInMonth

                            if (isEmptySector) {
                                Box(modifier = Modifier.weight(1f)) {}
                            } else {
                                val thisDay = currentDayNum
                                val formattedMonth = String.format(Locale.getDefault(), "%02d", currentMonth + 1)
                                val formattedDay = String.format(Locale.getDefault(), "%02d", thisDay)
                                val dateStr = "$currentYear-$formattedMonth-$formattedDay"

                                val isSelected = dateStr == activeDate
                                val hasTasks = tasksByDate.containsKey(dateStr)
                                val tasksForThisDate = tasksByDate[dateStr] ?: emptyList()

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            viewModel.setSelectedDate(dateStr)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = thisDay.toString(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White
                                                    else MaterialTheme.colorScheme.onSurface
                                        )

                                        if (hasTasks) {
                                            Row(
                                                modifier = Modifier.padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                // Mapped dots representing department assignments
                                                tasksForThisDate.take(3).forEach { t ->
                                                    val dotColor = when (t.division) {
                                                        "Marketing" -> ColorMarketing
                                                        "HR" -> ColorHR
                                                        "IT" -> ColorIT
                                                        else -> ColorFinance
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color.White else dotColor)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                currentDayNum++
                            }
                        }
                    }
                }
            }
        }

        // Active Tasks Header for selected date
        val displayDate = remember(activeDate) {
            try {
                if (activeDate.isEmpty()) "" else {
                    val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val d = sdfIn.parse(activeDate)
                    val sdfOut = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
                    d?.let { sdfOut.format(it) } ?: activeDate
                }
            } catch(e: Exception) {
                activeDate
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Jadwal Tugas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            TextButton(
                onClick = { showForm = true },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah")
            }
        }

        // Tasks list
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tidak Ada Tugas Terjadwal",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Kolaborasi tim Anda tenang hari ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("calendar_tasks_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    val colorAccent = when (task.division) {
                        "Marketing" -> ColorMarketing
                        "HR" -> ColorHR
                        "IT" -> ColorIT
                        else -> ColorFinance
                    }
                    val iconAcc = when (task.division) {
                        "Marketing" -> Icons.Default.Campaign
                        "HR" -> Icons.Default.Groups
                        "IT" -> Icons.Default.Terminal
                        else -> Icons.Default.TrendingUp
                    }

                    val isDark = isSystemInDarkTheme()
                    val strokeColor = if (isDark) Color(0xFF43474E) else Color(0xFFC1C7CE)

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, strokeColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                taskToEdit = task
                                showForm = true
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(colorAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconAcc,
                                    contentDescription = task.division,
                                    tint = colorAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "PIC: ${task.assignedTo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(task.status, fontSize = 10.sp) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            labelColor = colorAccent
                                        )
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(task.dueTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    // Export directly to system Google Calendar
                                    try {
                                        // Parse event values
                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                        val startCalendar = Calendar.getInstance()
                                        sdf.parse("${task.dueDate} ${task.dueTime}")?.let {
                                            startCalendar.time = it
                                        }
                                        val endCalendar = (startCalendar.clone() as Calendar).apply {
                                            add(Calendar.HOUR, 1) // 1 Hour duration by default
                                        }

                                        val intent = Intent(Intent.ACTION_INSERT)
                                            .setData(CalendarContract.Events.CONTENT_URI)
                                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCalendar.timeInMillis)
                                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCalendar.timeInMillis)
                                            .putExtra(CalendarContract.Events.TITLE, "[${task.division}] ${task.title}")
                                            .putExtra(CalendarContract.Events.DESCRIPTION, "${task.description}\n\nPenanggung Jawab (PIC): ${task.assignedTo}")
                                            .putExtra(CalendarContract.Events.EVENT_LOCATION, "Kantor Pusat Divisi")
                                            .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)

                                        context.startActivity(intent)
                                        Toast.makeText(context, "Membuka Google Calendar...", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal meluncurkan aplikasi Kalender: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = "Ekspor ke Google Calendar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        TaskFormDialog(
            taskToEdit = taskToEdit,
            defaultDivision = viewModel.currentDivision.value,
            onDismiss = {
                showForm = false
                taskToEdit = null
            },
            viewModel = viewModel
        )
    }
}
