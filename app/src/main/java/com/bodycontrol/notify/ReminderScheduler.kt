package com.bodycontrol.notify

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.bodycontrol.MainActivity
import com.bodycontrol.R
import com.bodycontrol.data.PracticeRepository
import com.bodycontrol.data.Reminder
import java.util.Calendar

/** 负责创建通知渠道、注册/取消每日提醒闹钟。 */
object ReminderScheduler {
    const val CHANNEL_ID = "practice_reminders"
    const val EXTRA_LABEL = "extra_label"

    fun ensureChannel(context: Context) {
        val mgr = context.getSystemService(NotificationManager::class.java) ?: return
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "练习提醒",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "每日练习提醒通知" }
            mgr.createNotificationChannel(channel)
        }
    }

    /** 根据提醒的启用状态注册或取消闹钟。 */
    fun schedule(context: Context, reminder: Reminder) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val pi = pendingIntent(context, reminder)
        if (!reminder.enabled) {
            am.cancel(pi)
            return
        }
        // 使用非精确重复闹钟：无需 SCHEDULE_EXACT_ALARM 权限，且更省电。
        am.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTriggerMillis(reminder.hour, reminder.minute),
            AlarmManager.INTERVAL_DAY,
            pi,
        )
    }

    fun cancel(context: Context, reminder: Reminder) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        am.cancel(pendingIntent(context, reminder))
    }

    /** 重启后重新注册全部启用的提醒。 */
    fun rescheduleAll(context: Context) {
        PracticeRepository.init(context)
        PracticeRepository.reminders.value.forEach { schedule(context, it) }
    }

    private fun pendingIntent(context: Context, reminder: Reminder): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_LABEL, reminder.label)
        }
        return PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now.timeInMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}

/** 闹钟触发时发出练习提醒通知。 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.ensureChannel(context)
        val label = intent.getStringExtra(ReminderScheduler.EXTRA_LABEL).orEmpty()
        val text = label.ifBlank { "该练习啦，花几分钟照顾一下身体吧。" }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("身体训练")
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        context.getSystemService(NotificationManager::class.java)
            ?.notify(System.currentTimeMillis().toInt(), notification)
    }
}

/** 设备重启后重新注册所有提醒（重复闹钟在重启后会被系统清除）。 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderScheduler.rescheduleAll(context)
        }
    }
}
