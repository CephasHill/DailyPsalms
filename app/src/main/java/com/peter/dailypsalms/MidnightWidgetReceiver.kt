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

        if (action !in setOf(
                Intent.ACTION_DATE_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_USER_PRESENT
            )
        ) {
            return
        }

        // Keep the receiver alive until the Glance update has finished. Launching
        // work after onReceive() returns is not reliable because Android may kill
        // the process immediately afterward.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DailyPsalmsWidget().updateAll(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
