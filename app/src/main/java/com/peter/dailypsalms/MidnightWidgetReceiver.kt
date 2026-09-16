package com.peter.dailypsalms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import com.peter.dailypsalms.ui.theme.DailyPsalmsWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MidnightWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        // Use Intent.ACTION_TIME_CHANGED instead of Intent.ACTION_TIME_SET
        if (action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_USER_PRESENT) {

            CoroutineScope(Dispatchers.IO).launch {
                DailyPsalmsWidget().updateAll(context)
            }
        }
    }
}