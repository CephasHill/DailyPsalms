package com.peter.dailypsalms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import com.peter.dailypsalms.ui.theme.DailyPsalmsWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class MidnightWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Trigger widget update
        CoroutineScope(Dispatchers.IO).launch {
            DailyPsalmsWidget().updateAll(context)
        }

        // Schedule the next midnight update
        scheduleMidnightWidgetUpdate(context)
    }
}

fun scheduleMidnightWidgetUpdate(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, MidnightWidgetReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        2001,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 5) // 5 seconds past midnight
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, 1) // Target the upcoming midnight
    }

    // Use a standard, inexact alarm. No special permissions required!
    // The OS will trigger this during a battery-friendly maintenance window around midnight.
    alarmManager.set(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}