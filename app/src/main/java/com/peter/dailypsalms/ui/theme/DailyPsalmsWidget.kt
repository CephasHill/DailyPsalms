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
import com.peter.dailypsalms.*
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class DailyPsalmsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val initialPrefs = context.dataStore.data.first()

        provideContent {
            val prefs by context.dataStore.data.collectAsState(initial = initialPrefs)

            // Keys
            val readingTrackKey = stringPreferencesKey("reading_track")
            val graceDayKey = stringPreferencesKey("grace_day")
            val checkmarksDateKey = stringPreferencesKey("checkmarks_date")
            val completedChaptersKey = stringSetPreferencesKey("completed_chapters")
            val last100DateKey = stringPreferencesKey("last_100_date")
            val streakKey = intPreferencesKey("streak")

            // Current date context
            val todayDate = LocalDate.now()
            val todayStr = todayDate.toString()
            val yesterdayStr = todayDate.minusDays(1).toString()

            // User preferences
            val currentTrack = try {
                ReadingTrack.valueOf(prefs[readingTrackKey] ?: ReadingTrack.CLASSIC.name)
            } catch (_: Exception) { ReadingTrack.CLASSIC }

            val currentGraceDay = try {
                GraceDayOption.valueOf(prefs[graceDayKey] ?: GraceDayOption.NONE.name)
            } catch (_: Exception) { GraceDayOption.NONE }

            val cycleStartDate = getCycleStartDate(todayDate, currentGraceDay)
            val cycleStartStr = cycleStartDate.toString()

            // Calculate completed items for the active cycle
            val checkmarksDate = prefs[checkmarksDateKey] ?: ""
            val rawCompleted = if (checkmarksDate == cycleStartStr) {
                prefs[completedChaptersKey] ?: emptySet()
            } else {
                emptySet()
            }

            // Calculate today's chapters dynamically
            val assignedToday = getAssignedChapters(todayDate, currentTrack)
            val todayKeys = assignedToday.map { assignment ->
                val book = if (assignment.book.contains("Psalm", true)) "Psalms" else "Proverbs"
                val partSuffix = if (assignment.partId != null) "_part${assignment.partId}" else ""
                "${book}_${assignment.chapter}${partSuffix}_${assignment.assignedDate}"
            }.toSet()

            val doneCount = rawCompleted.intersect(todayKeys).size
            val totalCount = assignedToday.size
            val isDoneToday = totalCount in 1..doneCount

            // Streak logic
            val last100Date = prefs[last100DateKey] ?: ""
            val actualStreak = prefs[streakKey] ?: 0
            val displayStreak = when (last100Date) {
                todayStr -> actualStreak
                yesterdayStr -> actualStreak
                else -> 0
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                action = "ACTION_OPEN_READ_NOW"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF005B8E))
                    .padding(10.dp)
                    .clickable(actionStartActivity(launchIntent)),
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