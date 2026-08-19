package com.peter.dailypsalms.ui.theme

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.*
import com.peter.dailypsalms.MainActivity
import com.peter.dailypsalms.dataStore
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class DailyPsalmsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 1. Grab the exact state perfectly the first time the widget is created
        val initialPrefs = context.dataStore.data.first()

        provideContent {
            // 2. Make the DataStore reactive! This automatically fetches new data every time updateAll() is called
            val prefs by context.dataStore.data.collectAsState(initial = initialPrefs)

            val last100DateKey = stringPreferencesKey("last_100_date")
            val streakKey = intPreferencesKey("streak")

            // Grab the pre-calculated numbers directly from the main app
            val widgetDoneKey = intPreferencesKey("widget_done_count")
            val widgetTotalKey = intPreferencesKey("widget_total_count")

            val todayStr = LocalDate.now().toString()
            val yesterdayStr = LocalDate.now().minusDays(1).toString()

            val last100Date = prefs[last100DateKey] ?: ""
            val actualStreak = prefs[streakKey] ?: 0

            val displayStreak = when (last100Date) {
                todayStr -> actualStreak
                yesterdayStr -> actualStreak
                else -> 0
            }

            val doneCount = prefs[widgetDoneKey] ?: 0
            val totalCount = prefs[widgetTotalKey] ?: 0
            val isDoneToday = totalCount > 0 && doneCount >= totalCount

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF005B8E)) // Rich blue background
                    .padding(10.dp)
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔥 $displayStreak Days",
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))

                // Now displays the exact fraction instead of just "Read Today"
                Text(
                    text = if (isDoneToday) "✅ Done Today" else "📖 $doneCount / $totalCount Chapters",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFFA3E4D7), night = Color(0xFFA3E4D7)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

class DailyPsalmsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyPsalmsWidget()
}