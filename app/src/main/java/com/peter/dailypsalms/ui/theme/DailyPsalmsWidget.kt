package com.peter.dailypsalms.ui.theme

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
        val prefs = context.dataStore.data.first()

        val checkmarksDateKey = stringPreferencesKey("checkmarks_date")
        val completedChaptersKey = stringSetPreferencesKey("completed_chapters")
        val last100DateKey = stringPreferencesKey("last_100_date")
        val streakKey = intPreferencesKey("streak")

        val todayStr = LocalDate.now().toString()
        val yesterdayStr = LocalDate.now().minusDays(1).toString()

        val checkmarksDate = prefs[checkmarksDateKey] ?: ""
        val completedChapters = if (checkmarksDate == todayStr) {
            prefs[completedChaptersKey] ?: emptySet()
        } else {
            emptySet()
        }

        val last100Date = prefs[last100DateKey] ?: ""
        val actualStreak = prefs[streakKey] ?: 0

        val displayStreak = when (last100Date) {
            todayStr -> actualStreak
            yesterdayStr -> actualStreak
            else -> 0
        }

        // Calculate today's total target chapters
        val today = LocalDate.now().dayOfMonth
        val psalmsTargets = listOf(today, today + 30, today + 60, today + 90, today + 120).filter { it <= 150 }
        val totalCount = psalmsTargets.size + 1 // Psalms + Proverbs
        val isDoneToday = completedChapters.size >= totalCount && totalCount > 0

        provideContent {
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
                Text(
                    text = if (isDoneToday) "✅ Done Today" else "📖 Read Today",
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