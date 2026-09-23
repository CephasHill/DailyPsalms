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
            val completedChaptersKey = stringSetPreferencesKey("completed_chapters")
            val planStartDateKey = stringPreferencesKey("plan_start_date")
            val pendingCatchUpDatesKey = stringSetPreferencesKey("pending_catch_up_dates")
            val last100DateKey = stringPreferencesKey("last_100_date")
            val legacyLast100DateKey = stringPreferencesKey("last_100Date_key")
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

            val planStartDate = try {
                prefs[planStartDateKey]?.let { LocalDate.parse(it) } ?: todayDate
            } catch (_: Exception) {
                todayDate
            }

            val cycleStartDate = getCycleStartDate(todayDate, currentGraceDay)

            // Completion keys include their assignment date and are retained
            // across cycles, just like the app's daily playlist.
            val rawCompleted = prefs[completedChaptersKey] ?: emptySet()
            val pendingCatchUpDates = prefs[pendingCatchUpDatesKey] ?: emptySet()

            fun assignmentKey(assignment: AssignedChapter): String {
                val book = if (assignment.book.contains("Psalm", true)) "Psalms" else "Proverbs"
                val partSuffix = if (assignment.partId != null) "_part${assignment.partId}" else ""
                return "${book}_${assignment.chapter}${partSuffix}_${assignment.assignedDate}"
            }

            val cycleDates = generateSequence(
                if (cycleStartDate.isBefore(planStartDate)) planStartDate else cycleStartDate
            ) { date ->
                if (date.isBefore(todayDate)) date.plusDays(1) else null
            }.toList()
                .let { dates ->
                    if (!isGraceDay(todayDate, currentGraceDay)) dates + todayDate else dates
                }
                .filterNot { isGraceDay(it, currentGraceDay) }

            val allAssignmentDates = (pendingCatchUpDates.mapNotNull { dateString ->
                try { LocalDate.parse(dateString) } catch (_: Exception) { null }
            } + cycleDates).distinct().sorted()

            val activeAssignments = allAssignmentDates.flatMap { date ->
                getAssignedChapters(date, currentTrack, planStartDate).filter { assignment ->
                    if (pendingCatchUpDates.contains(date.toString())) {
                        true
                    } else if (date.isBefore(todayDate)) {
                        val key = assignmentKey(assignment)
                        !rawCompleted.contains(key)
                    } else {
                        true
                    }
                }
            }

            val activeKeys = activeAssignments.map(::assignmentKey).toSet()
            val doneCount = rawCompleted.intersect(activeKeys).size
            val totalCount = activeAssignments.size
            val isDoneToday = totalCount > 0 && doneCount == totalCount

            // Keep widget streak behavior consistent with the app while a
            // grace day or pending catch-up is still protecting the streak.
            val hasUnfinishedCatchUp = activeAssignments.any { assignment ->
                assignment.assignedDate.isBefore(todayDate) &&
                        !rawCompleted.contains(assignmentKey(assignment))
            }

            val last100Date = prefs[last100DateKey] ?: prefs[legacyLast100DateKey] ?: ""
            val actualStreak = prefs[streakKey] ?: 0
            val displayStreak = when (last100Date) {
                todayStr -> actualStreak
                yesterdayStr -> actualStreak
                "" -> actualStreak
                else -> if (
                    actualStreak > 0 &&
                    (pendingCatchUpDates.isNotEmpty() ||
                            isGraceDay(todayDate, currentGraceDay) ||
                            hasUnfinishedCatchUp)
                ) actualStreak else 0
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
