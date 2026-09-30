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
            val pendingCatchUpDateKey = stringPreferencesKey("pending_catch_up_date")
            val activeCatchUpKeysKey = stringSetPreferencesKey("active_catch_up_keys")
            val last100DateKey = stringPreferencesKey("last_100_date")
            val legacyLast100DateKey = stringPreferencesKey("last_100Date_key")
            val streakKey = intPreferencesKey("streak")
            val streakResetDateKey = stringPreferencesKey("streak_reset_date")

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
            val cycleStartStr = cycleStartDate.toString()

            // Completion keys include their assignment date and are retained
            // across cycles, just like the app's daily playlist.
            val rawCompleted = prefs[completedChaptersKey] ?: emptySet()
            val storedPendingCatchUpDates = prefs[pendingCatchUpDatesKey] ?: emptySet()
            val storedActiveCatchUpKeys = prefs[activeCatchUpKeysKey]
            val pendingSetReconciledToday = prefs[pendingCatchUpDateKey] == todayStr

            fun hasIncompleteAssignment(date: LocalDate): Boolean {
                return getAssignedChapters(date, currentTrack, planStartDate)
                    .any { assignment -> !rawCompleted.contains(assignmentKey(assignment)) }
            }

            fun incompleteKeysForDates(dates: Set<String>): Set<String> {
                return dates.flatMap { dateString ->
                    val date = try { LocalDate.parse(dateString) } catch (_: Exception) { null }
                    if (date == null) {
                        emptyList()
                    } else {
                        getAssignedChapters(date, currentTrack, planStartDate)
                            .filter { assignment -> !rawCompleted.contains(assignmentKey(assignment)) }
                            .map(::assignmentKey)
                    }
                }.toSet()
            }

            fun allKeysForDates(dates: Set<String>): Set<String> {
                return dates.flatMap { dateString ->
                    val date = try { LocalDate.parse(dateString) } catch (_: Exception) { null }
                    if (date == null) emptyList()
                    else getAssignedChapters(date, currentTrack, planStartDate).map(::assignmentKey)
                }.toSet()
            }

            // The Grace Day has its normal daily assignment and is also the
            // final day for completing that week's catch-up chapters.
            val cycleDates = getCycleDates(todayDate, currentGraceDay, planStartDate)

            // Reconstruct missed dates as well as reading the durable pending
            // set. This keeps the widget correct even if the app has not been
            // opened yet today to run its date-transition reconciliation.
            val inferredPendingCatchUpDates = cycleDates
                .filter { it.isBefore(todayDate) && hasIncompleteAssignment(it) }
                .map { it.toString() }
                .toSet()
            val storedCurrentPendingDates = storedPendingCatchUpDates
                .filter { dateString ->
                    try {
                        !LocalDate.parse(dateString).isBefore(cycleStartDate)
                    } catch (_: Exception) {
                        false
                    }
                }
                .toSet()

            val pendingCatchUpDates = (if (pendingSetReconciledToday) {
                storedCurrentPendingDates
            } else {
                storedCurrentPendingDates.filter { dateString ->
                    try { hasIncompleteAssignment(LocalDate.parse(dateString)) } catch (_: Exception) { false }
                }.toSet()
            }) + inferredPendingCatchUpDates

            val visibleCatchUpKeys = when {
                pendingSetReconciledToday && storedActiveCatchUpKeys != null ->
                    storedActiveCatchUpKeys + incompleteKeysForDates(inferredPendingCatchUpDates)
                pendingSetReconciledToday -> allKeysForDates(pendingCatchUpDates)
                else -> incompleteKeysForDates(pendingCatchUpDates)
            }

            val expiredPreviousCycle = storedPendingCatchUpDates.any { dateString ->
                try {
                    val date = LocalDate.parse(dateString)
                    date.isBefore(cycleStartDate) && hasIncompleteAssignment(date)
                } catch (_: Exception) {
                    true
                }
            } || getCycleDates(cycleStartDate.minusDays(1), currentGraceDay, planStartDate)
                .any(::hasIncompleteAssignment)

            val allAssignmentDates = (pendingCatchUpDates.mapNotNull { dateString ->
                try { LocalDate.parse(dateString) } catch (_: Exception) { null }
            } + cycleDates).distinct().sorted()

            val activeAssignments = allAssignmentDates.flatMap { date ->
                getAssignedChapters(date, currentTrack, planStartDate).filter { assignment ->
                    if (date.isBefore(todayDate)) {
                        val key = assignmentKey(assignment)
                        (pendingCatchUpDates.contains(date.toString()) && visibleCatchUpKeys.contains(key)) ||
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
            val streakWasReset = prefs[streakResetDateKey] == cycleStartStr
            val displayStreak = if (expiredPreviousCycle || streakWasReset) {
                0
            } else {
                when (last100Date) {
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
