package com.example.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("TASK_ID") ?: return
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Tugas Tim Hari Ini"
        val taskDesc = intent.getStringExtra("TASK_DESC") ?: "Periksa detail tugas harian Anda."
        val taskDiv = intent.getStringExtra("TASK_DIV") ?: "Harian"

        Log.d("TaskReminderReceiver", "Reminder received for task: $taskTitle")
        showNotification(context, taskId.hashCode(), "[$taskDiv] $taskTitle", taskDesc)
    }

    private fun showNotification(context: Context, id: Int, title: String, content: String) {
        val channelId = "task_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Tugas Harian",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Saluran untuk pengingat otomatis jadwal tugas tim"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }

    companion object {
        fun scheduleReminder(context: Context, task: Task) {
            if (task.reminderMinutes <= 0) return

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            try {
                val dateStr = "${task.dueDate} ${task.dueTime}"
                val targetDate = sdf.parse(dateStr) ?: return
                
                val calendar = Calendar.getInstance()
                calendar.time = targetDate
                calendar.add(Calendar.MINUTE, -task.reminderMinutes)

                val triggerTime = calendar.timeInMillis
                if (triggerTime <= System.currentTimeMillis()) {
                    return
                }

                val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                    putExtra("TASK_ID", task.id)
                    putExtra("TASK_TITLE", task.title)
                    putExtra("TASK_DESC", task.description)
                    putExtra("TASK_DIV", task.division)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    task.id.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                Log.d("TaskReminderReceiver", "Scheduled reminder for ${task.title} at ${calendar.time}")
            } catch (e: Exception) {
                Log.e("TaskReminderReceiver", "Error scheduling reminder", e)
            }
        }

        fun cancelReminder(context: Context, task: Task) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, TaskReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
