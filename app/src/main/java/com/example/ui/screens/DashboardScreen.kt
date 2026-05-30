package com.example.ui.screens

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.theme.*
import com.example.viewmodel.TaskViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allTasks by viewModel.allTasks.collectAsState()
    val currentDiv by viewModel.currentDivision.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()

    var selectedFilterDivision by remember { mutableStateOf(currentDiv) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var showForm by remember { mutableStateOf(false) }

    // Sync selected filter division on login changes
    LaunchedEffect(currentDiv) {
        selectedFilterDivision = currentDiv
    }

    // Filter tasks for division
    val divisionTasks = remember(allTasks, selectedFilterDivision) {
        allTasks.filter { it.division == selectedFilterDivision }
    }

    // Stats calculations
    val totalCount = divisionTasks.size
    val pendingCount = divisionTasks.count { it.status == "PENDING" }
    val progressCount = divisionTasks.count { it.status == "IN_PROGRESS" }
    val completedCount = divisionTasks.count { it.status == "COMPLETED" }

    val completeProgress = remember(divisionTasks) {
        if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
    }

    // Color definitions
    val divThemeColor = remember(selectedFilterDivision) {
        when(selectedFilterDivision) {
            "Marketing" -> ColorMarketing
            "HR" -> ColorHR
            "IT" -> ColorIT
            else -> ColorFinance
        }
    }

    val divThemeIcon = remember(selectedFilterDivision) {
        when(selectedFilterDivision) {
            "Marketing" -> Icons.Default.Campaign
            "HR" -> Icons.Default.Groups
            "IT" -> Icons.Default.Terminal
            else -> Icons.Default.TrendingUp
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DiviTask",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    letterSpacing = (-0.5).sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(divThemeColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$currentRole • Divisi $currentDiv",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Polished JD-Style initials avatar block
                val initials = currentRole.split(" ").filter { it.firstOrNull()?.isLetter() == true }.map { it.first() }.take(2).joinToString("").uppercase()
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials.ifEmpty { "ME" },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { 
                        viewModel.logout()
                        Toast.makeText(context, "Berhasil Keluar Divisi.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout, 
                        contentDescription = "Keluar akun", 
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Stats Card with visual progress bars
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth().testTag("stats_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Progress stats description
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(divThemeColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(divThemeIcon, contentDescription = null, tint = divThemeColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Dashboard Kelompok $selectedFilterDivision",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${(completeProgress * 100).toInt()}% Selesai",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = divThemeColor
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = completeProgress,
                    color = divThemeColor,
                    trackColor = divThemeColor.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Numerical stats columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                        levelValue = totalCount.toString(),
                        descLabel = "Beban Tugas",
                        valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        levelValue = pendingCount.toString(),
                        descLabel = "Tertunda",
                        valueColor = ColorMarketing,
                        containerColor = ColorMarketing.copy(alpha = 0.08f),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        levelValue = progressCount.toString(),
                        descLabel = "Diproses",
                        valueColor = ColorIT,
                        containerColor = ColorIT.copy(alpha = 0.08f),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        levelValue = completedCount.toString(),
                        descLabel = "Selesai",
                        valueColor = ColorFinance,
                        containerColor = ColorFinance.copy(alpha = 0.08f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Department fast explorer filter chips
        Column {
            Text(
                text = "Tinjau Divisi Lain",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Marketing", "HR", "IT", "Finance").forEach { div ->
                    val isSelected = selectedFilterDivision == div
                    val dColor = when(div) {
                        "Marketing" -> ColorMarketing
                        "HR" -> ColorHR
                        "IT" -> ColorIT
                        else -> ColorFinance
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surface
                                else Color.Transparent
                            )
                            .clickable { selectedFilterDivision = div }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(dColor)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(
                                text = div,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) dColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // List of Active Tasks Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tugas Aktif (${divisionTasks.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Add task trigger button
            if (selectedFilterDivision == currentDiv) {
                Button(
                    onClick = { showForm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = divThemeColor),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delegasikan", fontSize = 13.sp)
                }
            }
        }

        // Tasks Scroll Nodes
        if (divisionTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Belum Ada Tugas",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("dashboard_tasks_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(divisionTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        borderColor = divThemeColor,
                        onCheckChange = { isChecked ->
                            val nStat = if (isChecked) "COMPLETED" else "PENDING"
                            viewModel.updateTaskStatus(task, nStat, context)
                        },
                        onClick = {
                            if (currentDiv == task.division) {
                                taskToEdit = task
                                showForm = true
                            } else {
                                Toast.makeText(context, "Hanya anggota divisi ${task.division} yang bisa menyunting tugas ini.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = {
                            if (currentDiv == task.division) {
                                viewModel.deleteTask(task, context)
                                Toast.makeText(context, "Tugas berhasil dihapus.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Anda tidak memiliki akses untuk menghapus tugas divisi lain.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    if (showForm) {
        TaskFormDialog(
            taskToEdit = taskToEdit,
            defaultDivision = selectedFilterDivision,
            onDismiss = {
                showForm = false
                taskToEdit = null
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun StatBox(
    levelValue: String,
    descLabel: String,
    valueColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = descLabel,
                style = MaterialTheme.typography.labelSmall,
                color = valueColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = levelValue,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    borderColor: Color,
    onClick: () -> Unit,
    onCheckChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = task.status == "COMPLETED"
    val isInProgress = task.status == "IN_PROGRESS"

    val textDecorationAlpha = if (isCompleted) 0.5f else 1.0f

    val prioColor = when(task.priority) {
        "HIGH" -> PriorityHigh
        "MEDIUM" -> PriorityMedium
        else -> PriorityLow
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
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task status Checkbox
            Checkbox(
                checked = isCompleted,
                onCheckedChange = onCheckChange,
                colors = CheckboxDefaults.colors(checkedColor = borderColor)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Task Title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = textDecorationAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Task Details
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textDecorationAlpha * 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // PIC tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PIC: " + task.assignedTo.split(" ").first(),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Priority tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(prioColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = prioColor
                        )
                    }

                    // Due Date
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${task.dueDate} ${task.dueTime}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Sync Indicator Or Delete trigger icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (task.isSynced) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Synced",
                        tint = ColorFinance,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus tugas",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
