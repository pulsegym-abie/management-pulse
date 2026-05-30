package com.example.data

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksByDivision(division: String): Flow<List<Task>> {
        return taskDao.getTasksByDivision(division)
    }

    suspend fun getTaskById(id: String): Task? = withContext(Dispatchers.IO) {
        taskDao.getTaskById(id)
    }

    suspend fun insert(task: Task) = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        taskDao.clearAllTasks()
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val apiService = Retrofit.Builder()
        .baseUrl("https://script.google.com/") 
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GasApiService::class.java)

    suspend fun syncWithSheets(gasUrl: String): Result<String> = withContext(Dispatchers.IO) {
        if (gasUrl.isBlank()) {
            return@withContext Result.failure(Exception("URL Google Apps Script belum dikonfigurasi di Pengaturan."))
        }

        try {
            // 1. Pull remote tasks
            val pullResponse = apiService.pullTasks(gasUrl)
            if (!pullResponse.isSuccessful) {
                return@withContext Result.failure(Exception("Gagal menghubungi Google Sheet (HTTP ${pullResponse.code()})"))
            }

            val remoteContainer = pullResponse.body()
            if (remoteContainer == null || !remoteContainer.success) {
                return@withContext Result.failure(Exception("Gagal: " + (remoteContainer?.message ?: "Data respons kosong")))
            }

            val remoteTasks = remoteContainer.tasks ?: emptyList()

            // 2. Load all local tasks
            val localTasks = taskDao.getAllTasks().first()

            // Merge maps by ID using 'lastUpdated' to resolve conflicts
            val mergedMap = mutableMapOf<String, Task>()
            
            // Put all local tasks
            localTasks.forEach { mergedMap[it.id] = it }
            
            // Merge remote tasks
            remoteTasks.forEach { remote ->
                val local = mergedMap[remote.id]
                if (local == null || remote.lastUpdated > local.lastUpdated) {
                    mergedMap[remote.id] = remote.toTask()
                }
            }

            val finalizedList = mergedMap.values.toList()

            // 3. Push complete merged list back to remote
            val pushResponse = apiService.pushTasks(gasUrl, finalizedList.map { it.toGasTask() })
            if (!pushResponse.isSuccessful) {
                // Save locally so changes are stored even if push fails
                taskDao.insertTasks(finalizedList)
                return@withContext Result.success("Data berhasil disinkronisasi ke DB lokal, tapi gagal mengunggah ke Google Sheet (HTTP ${pushResponse.code()})")
            }

            val pushContainer = pushResponse.body()
            if (pushContainer != null && pushContainer.success) {
                // 4. Overwrite local with finalized synced set
                val syncedAndUpdated = finalizedList.map { it.copy(isSynced = true) }
                taskDao.clearAllTasks()
                taskDao.insertTasks(syncedAndUpdated)
                return@withContext Result.success("Sinkronisasi 2-arah sukses! Total ${syncedAndUpdated.size} tugas terintegrasi dengan Google Sheets.")
            } else {
                taskDao.insertTasks(finalizedList)
                return@withContext Result.success("Data disinkronisasi lokal. Server GAS melaporkan error: ${pushContainer?.message}")
            }

        } catch (e: Exception) {
            Log.e("TaskRepository", "Sync failed", e)
            return@withContext Result.failure(e)
        }
    }

    suspend fun seedDefaultTasks() = withContext(Dispatchers.IO) {
        val currentTasks = taskDao.getAllTasks().first()
        if (currentTasks.isEmpty()) {
            val now = System.currentTimeMillis()
            val dummyList = listOf(
                // Marketing
                Task(
                    id = "seed_mkt_1",
                    title = "Susun Strategi Digital Campaign Q3",
                    description = "Menganalisis performa ads Google & Instagram dan menyiapkan budgeting serta visual draft untuk peluncuran kampanye Q3.",
                    division = "Marketing",
                    status = "PENDING",
                    priority = "HIGH",
                    dueDate = "2026-06-05",
                    dueTime = "10:00",
                    reminderMinutes = 30,
                    assignedTo = "Sarah (Marketing Lead)",
                    lastUpdated = now
                ),
                Task(
                    id = "seed_mkt_2",
                    title = "Review Copywriting Landing Page baru",
                    description = "Pemeriksaan tata bahasa, penawaran produk, CTA, serta SEO metadata landing page pre-order.",
                    division = "Marketing",
                    status = "IN_PROGRESS",
                    priority = "MEDIUM",
                    dueDate = "2026-06-03",
                    dueTime = "14:00",
                    reminderMinutes = 15,
                    assignedTo = "Rian (SEO / Content)",
                    lastUpdated = now
                ),
                // HR
                Task(
                    id = "seed_hr_1",
                    title = "Review CV Kandidat Android Developer",
                    description = "Menyeleksi 10 resume terbaik yang masuk di JobStreet/LinkedIn untuk posisi Jetpack Compose engineer.",
                    division = "HR",
                    status = "PENDING",
                    priority = "HIGH",
                    dueDate = "2026-06-02",
                    dueTime = "09:00",
                    reminderMinutes = 30,
                    assignedTo = "Dewi (Talent Acquisition)",
                    lastUpdated = now
                ),
                Task(
                    id = "seed_hr_2",
                    title = "Distribusi Form Evaluasi Bulanan Karyawan",
                    description = "Mengirimkan link Google Form penilaian KPI bulanan ke semua manajer divisi.",
                    division = "HR",
                    status = "COMPLETED",
                    priority = "LOW",
                    dueDate = "2026-05-28",
                    dueTime = "17:00",
                    reminderMinutes = 0,
                    assignedTo = "Budi (HR Generalist)",
                    lastUpdated = now
                ),
                // IT
                Task(
                    id = "seed_it_1",
                    title = "Security Patch Update & Server Reboot",
                    description = "Melakukan apply patch keamanan tahunan pada cluster Kubernetes dan database backup sebelum reboot pada jam sepi.",
                    division = "IT",
                    status = "PENDING",
                    priority = "HIGH",
                    dueDate = "2026-06-01",
                    dueTime = "23:00",
                    reminderMinutes = 60,
                    assignedTo = "Andi (System Admin)",
                    lastUpdated = now
                ),
                Task(
                    id = "seed_it_2",
                    title = "Migrasi API Endpoint v2",
                    description = "Membuat migrasi retrofitable endpoint di server staging untuk mendukung login departemen baru.",
                    division = "IT",
                    status = "IN_PROGRESS",
                    priority = "MEDIUM",
                    dueDate = "2026-06-04",
                    dueTime = "16:30",
                    reminderMinutes = 30,
                    assignedTo = "Yusuf (Backend Dev)",
                    lastUpdated = now
                ),
                // Finance
                Task(
                    id = "seed_fin_1",
                    title = "Rekonsiliasi Laporan Invoice Vendor",
                    description = "Mencocokkan nota penagihan vendor logistik dengan transaksi keluar perbankan periode Mei 2026.",
                    division = "Finance",
                    status = "PENDING",
                    priority = "HIGH",
                    dueDate = "2026-06-02",
                    dueTime = "11:00",
                    reminderMinutes = 30,
                    assignedTo = "Santi (Finance Specialist)",
                    lastUpdated = now
                ),
                Task(
                    id = "seed_fin_2",
                    title = "Penyusunan SPT Masa Pajak PPN",
                    description = "Melaporkan input pajak masukan dan keluaran e-faktur sebelum akhir periode pelaporan.",
                    division = "Finance",
                    status = "IN_PROGRESS",
                    priority = "HIGH",
                    dueDate = "2026-05-31",
                    dueTime = "15:00",
                    reminderMinutes = 15,
                    assignedTo = "Mega (Tax Acc)",
                    lastUpdated = now
                )
            )
            taskDao.insertTasks(dummyList)
        }
    }
}
