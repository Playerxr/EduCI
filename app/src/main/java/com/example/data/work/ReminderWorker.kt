package com.example.data.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.local.EduDatabase
import kotlinx.coroutines.flow.first

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val sharedPrefs = applicationContext.getSharedPreferences("edu_ci_prefs", Context.MODE_PRIVATE)
        val isEnabled = sharedPrefs.getBoolean("pref_reminders_enabled", false)
        
        if (!isEnabled) {
            return Result.success()
        }

        val hour = sharedPrefs.getInt("pref_reminder_hour", 18)
        val minute = sharedPrefs.getInt("pref_reminder_minute", 0)

        // Show the local notification
        showNotification()

        // Reschedule for the next day at the configured hour and minute
        ReminderScheduler.scheduleDailyReminder(applicationContext, hour, minute)

        return Result.success()
    }

    private suspend fun showNotification() {
        val database = EduDatabase.getDatabase(applicationContext)
        val selectedSubjects = database.subjectDao().getSelectedSubjects().first()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "revision_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rappels de Révision",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Rappels quotidiens personnalisés pour réviser le BAC"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val subjectsText = if (selectedSubjects.isNotEmpty()) {
            selectedSubjects.joinToString(", ") { it.name }
        } else {
            "tes matières sélectionnées"
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("📚 C'est l'heure de réviser !")
            .setContentText("Aujourd'hui, concentre-toi sur : $subjectsText.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Salut ! 👋 C'est l'heure de ta session de révision quotidienne.\n\n" +
                "Aujourd'hui, concentre-toi sur : $subjectsText.\n\n" +
                "Chaque session de révision de quelques minutes te rapproche de la réussite au BAC ! 🇨🇮 Clique pour commencer."
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
