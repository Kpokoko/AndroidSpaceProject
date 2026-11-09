package com.example.androidspaceproject.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.androidspaceproject.MainActivity
import com.example.androidspaceproject.R
import java.util.Calendar

class ClassReminder(private val context: Context) {
    private val alarms = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()

    fun notificationsEnabled(): Boolean {
        createChannel(context)
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return false
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < 26 || context.getSystemService(NotificationManager::class.java)
            .getNotificationChannel(CHANNEL_ID).importance != NotificationManager.IMPORTANCE_NONE
    }

    fun schedule(timeMillis: Long, fullName: String) {
        check(canScheduleExact())
        check(notificationsEnabled())
        require(timeMillis > System.currentTimeMillis())
        val intent = Intent(context, ClassReminderReceiver::class.java)
            .putExtra(EXTRA_NAME, fullName)
            .putExtra(EXTRA_TIME, timeMillis)
            .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val showReminder = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarms.setAlarmClock(AlarmManager.AlarmClockInfo(timeMillis, showReminder), pendingIntent)
        Log.i(TAG, "Scheduled for $timeMillis; now=${System.currentTimeMillis()}")
    }

    companion object {
        const val CHANNEL_ID = "class_reminders"
        const val EXTRA_NAME = "full_name"
        const val EXTRA_TIME = "trigger_at"
        const val TAG = "ClassReminder"

        fun isValidTime(value: String): Boolean = Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(value)

        fun todayAt(value: String): Long {
            require(isValidTime(value))
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, value.substring(0, 2).toInt())
                set(Calendar.MINUTE, value.substring(3, 5).toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= 26) {
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Начало любимой пары", NotificationManager.IMPORTANCE_HIGH)
                )
            }
        }
    }
}

class ClassReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val now = System.currentTimeMillis()
        val scheduledAt = intent.getLongExtra(ClassReminder.EXTRA_TIME, now)
        Log.i(ClassReminder.TAG, "Received at $now; scheduled=$scheduledAt; delayMs=${now - scheduledAt}")
        if (!ClassReminder(context).notificationsEnabled()) {
            Log.w(ClassReminder.TAG, "Notification blocked by permission or channel settings")
            return
        }
        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val name = intent.getStringExtra(ClassReminder.EXTRA_NAME).orEmpty()
        val notification = NotificationCompat.Builder(context, ClassReminder.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_class_reminder)
            .setContentTitle("Время любимой пары")
            .setContentText(if (name.isBlank()) "Ваша любимая пара начинается!" else "$name, ваша любимая пара начинается!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setWhen(scheduledAt)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        try {
            context.getSystemService(NotificationManager::class.java)
                .notify(scheduledAt.toString(), 1, notification)
            Log.i(ClassReminder.TAG, "Notification posted at ${System.currentTimeMillis()}")
        } catch (error: SecurityException) {
            Log.w(ClassReminder.TAG, "Notification permission revoked", error)
        }
    }
}
