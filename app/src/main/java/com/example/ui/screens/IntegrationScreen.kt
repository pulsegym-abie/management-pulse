package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.SyncUiState
import com.example.viewmodel.TaskViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun IntegrationScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gasUrl by viewModel.gasUrl.collectAsState()
    val reminderEnabled by viewModel.reminderEnabled.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    var inputUrl by remember { mutableStateOf(gasUrl) }

    // Apps Script code content
    val appsScriptCode = """// Kode Google Apps Script untuk Google Sheets Database utama
function doGet(e) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  var data = sheet.getDataRange().getValues();
  var tasks = [];
  
  for (var i = 1; i < data.length; i++) {
    tasks.push({
      id: String(data[i][0]),
      title: String(data[i][1]),
      description: String(data[i][2]),
      division: String(data[i][3]),
      status: String(data[i][4]),
      priority: String(data[i][5]),
      dueDate: String(data[i][6]),
      dueTime: String(data[i][7]),
      reminderMinutes: Number(data[i][8]),
      assignedTo: String(data[i][9]),
      lastUpdated: Number(data[i][10])
    });
  }
  
  return ContentService.createTextOutput(JSON.stringify({
    success: true,
    message: "Data pulled successfully",
    tasks: tasks
  })).setMimeType(ContentService.MimeType.JSON);
}

function doPost(e) {
  try {
    var rawData = e.postData.contents;
    var remoteTasks = JSON.parse(rawData);
    var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
    
    sheet.clear();
    sheet.appendRow(["id", "title", "description", "division", "status", "priority", "dueDate", "dueTime", "reminderMinutes", "assignedTo", "lastUpdated"]);
    
    for (var i = 0; i < remoteTasks.length; i++) {
      var t = remoteTasks[i];
      sheet.appendRow([
        t.id,
        t.title,
        t.description,
        t.division,
        t.status,
        t.priority,
        t.dueDate,
        t.dueTime,
        t.reminderMinutes,
        t.assignedTo,
        t.lastUpdated
      ]);
    }
    
    return ContentService.createTextOutput(JSON.stringify({
      success: true,
      message: "Sync 2-arah sukses! Total " + remoteTasks.length + " tugas terintegrasi."
    })).setMimeType(ContentService.MimeType.JSON);
    
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({
      success: false,
      message: "Error sync: " + err.toString()
    })).setMimeType(ContentService.MimeType.JSON);
  }
}"""

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Integrasi Google Sheets",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Sinkronisasi Data Real-time & Cloud Storage",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Automatic Reminders toggle switcher card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pengingat Otomatis Alarm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Aktifkan alarm perangkat lokal untuk info tugas tim harian sebelum jatuh tempo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { viewModel.setReminderEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = CorePrimary)
                )
            }
        }

        // Configuration Card input box
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth().testTag("integration_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Konfigurasi Spreadsheet Database",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    label = { Text("URL Google Apps Script Web App") },
                    placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                    leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth().testTag("gas_url_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.setGasUrl(inputUrl)
                        viewModel.syncTasks()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("sync_now_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan & Sinkronisasi Sekarang", fontWeight = FontWeight.Bold)
                }

                // Sync status response cards
                AnimatedVisibility(visible = syncState != SyncUiState.Idle) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        when (syncState) {
                            is SyncUiState.Loading -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Menghubungkan ke GAS & sinkronisasi data...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            is SyncUiState.Success -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ColorFinance.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = ColorFinance)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text((syncState as SyncUiState.Success).message, style = MaterialTheme.typography.bodyMedium, color = ColorFinance)
                                    }
                                }
                            }
                            is SyncUiState.Error -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text((syncState as SyncUiState.Error).error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        // Integration Steps Guide Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Panduan Setup Google Sheets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                StepRow(number = "1", text = "Buat Google Sheets baru di Google Drive Anda.")
                StepRow(number = "2", text = "Di menu atas Sheet, klik Ekstensi (Extensions) -> Apps Script.")
                StepRow(number = "3", text = "Salin seluruh kode script di bawah ini dengan tombol salin.")
                StepRow(number = "4", text = "Tempel di pengetikan Apps Script, gantikan semua kode bawaan.")
                StepRow(number = "5", text = "Klik Terapkan (Deploy) -> Penerapan Baru (New Deployment) -> Pilih jenis 'Web App'.")
                StepRow(number = "6", text = "Setel 'Jalankan sebagai': Saya, dan 'Siapa yang memiliki akses': Siapa saja (Anyone). Klik Deploy.")
                StepRow(number = "7", text = "Salin URL Web App yang disediakan, lalu tempel di kolom input di atas.")

                Spacer(modifier = Modifier.height(20.dp))

                // Copy Code Section
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kode Google Apps Script",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("DiviTask Apps Script Code", appsScriptCode)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Kode berhasil disalin ke papan klip!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin Kode", fontSize = 12.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = appsScriptCode,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun StepRow(
    number: String,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(bottom = 1.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}
