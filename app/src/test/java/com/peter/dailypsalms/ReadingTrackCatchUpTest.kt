package com.peter.dailypsalms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReadingTrackCatchUpTest {

    private val monday = LocalDate.of(2026, 9, 21)

    @Test
    fun missedDay_isShownWithCurrentDay_forEveryTrack() {
        val tuesday = monday.plusDays(1)

        ReadingTrack.entries.forEach { track ->
            val missedAssignments = getAssignedChapters(monday, track, monday)
            val currentAssignments = getAssignedChapters(tuesday, track, monday)

            val plan = ReadingEngine.buildDailyPlan(
                DailyPlanInput(
                    today = tuesday,
                    track = track,
                    graceDay = GraceDayOption.SUNDAY,
                    planStartDate = monday,
                    pendingCatchUpDates = setOf(monday),
                    activeCatchUpKeys = missedAssignments.map(::assignmentKey).toSet(),
                    completedKeys = emptySet()
                )
            )

            assertEquals(
                track.name,
                missedAssignments.size + currentAssignments.size,
                plan.assignments.size
            )
            assertEquals(
                track.name,
                setOf(monday, tuesday),
                plan.assignments.map { it.assignedDate }.toSet()
            )
        }
    }

    @Test
    fun checkedCatchUp_staysVisibleOnTheSameDay_forEveryTrack() {
        val tuesday = monday.plusDays(1)

        ReadingTrack.entries.forEach { track ->
            val missedAssignments = getAssignedChapters(monday, track, monday)
            val missedKeys = missedAssignments.map(::assignmentKey).toSet()

            val plan = ReadingEngine.buildDailyPlan(
                DailyPlanInput(
                    today = tuesday,
                    track = track,
                    graceDay = GraceDayOption.SUNDAY,
                    planStartDate = monday,
                    pendingCatchUpDates = setOf(monday),
                    activeCatchUpKeys = missedKeys,
                    completedKeys = missedKeys
                )
            )

            assertTrue(
                "$track should keep checked catch-up visible",
                missedKeys.all { key -> plan.assignments.any { assignmentKey(it) == key } }
            )
            assertTrue(
                "$track should include today's normal assignments",
                plan.assignments.any { it.assignedDate == tuesday }
            )
        }
    }
}
