package com.peter.dailypsalms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReadingEngineTest {

    private val monday = LocalDate.of(2026, 9, 21)

    @Test
    fun missedDays_areIncludedInTheDailyPlan() {
        val wednesday = monday.plusDays(2)

        val plan = ReadingEngine.buildDailyPlan(
            input = DailyPlanInput(
                today = wednesday,
                track = ReadingTrack.CLASSIC,
                graceDay = GraceDayOption.SUNDAY,
                planStartDate = monday,
                pendingCatchUpDates = emptySet(),
                activeCatchUpKeys = emptySet(),
                completedKeys = emptySet()
            )
        )

        // Classic assigns six chapters per day.
        assertEquals(18, plan.assignments.size)
        assertEquals(setOf(monday, monday.plusDays(1), wednesday), plan.assignments.map { it.assignedDate }.toSet())
    }

    @Test
    fun checkedCatchUp_remainsVisibleOnTheSameDay() {
        val tuesday = monday.plusDays(1)
        val mondayAssignment = getAssignedChapters(
            date = monday,
            track = ReadingTrack.PSALMS_ONLY,
            planStartDate = monday
        ).single()
        val mondayKey = assignmentKey(mondayAssignment)

        val plan = ReadingEngine.buildDailyPlan(
            input = DailyPlanInput(
                today = tuesday,
                track = ReadingTrack.PSALMS_ONLY,
                graceDay = GraceDayOption.SUNDAY,
                planStartDate = monday,
                pendingCatchUpDates = setOf(monday),
                activeCatchUpKeys = setOf(mondayKey),
                completedKeys = setOf(mondayKey)
            )
        )

        assertTrue(plan.assignments.any { it.assignedDate == monday && assignmentKey(it) == mondayKey })
        assertTrue(plan.assignments.any { it.assignedDate == tuesday })
    }

    @Test
    fun completedCatchUp_dropsOnTheNextDay() {
        val tuesday = monday.plusDays(1)
        val wednesday = monday.plusDays(2)
        val mondayAssignment = getAssignedChapters(
            date = monday,
            track = ReadingTrack.PSALMS_ONLY,
            planStartDate = monday
        ).single()
        val mondayKey = assignmentKey(mondayAssignment)

        val plan = ReadingEngine.buildDailyPlan(
            input = DailyPlanInput(
                today = wednesday,
                track = ReadingTrack.PSALMS_ONLY,
                graceDay = GraceDayOption.SUNDAY,
                planStartDate = monday,
                pendingCatchUpDates = setOf(tuesday),
                activeCatchUpKeys = emptySet(),
                completedKeys = setOf(mondayKey)
            )
        )

        assertTrue(plan.assignments.none { assignmentKey(it) == mondayKey })
        assertEquals(setOf(tuesday, wednesday), plan.assignments.map { it.assignedDate }.toSet())
    }

    @Test
    fun graceDay_includesItsNormalAssignment() {
        val sunday = monday.plusDays(6)

        val plan = ReadingEngine.buildDailyPlan(
            input = DailyPlanInput(
                today = sunday,
                track = ReadingTrack.PSALMS_ONLY,
                graceDay = GraceDayOption.SUNDAY,
                planStartDate = monday,
                pendingCatchUpDates = emptySet(),
                activeCatchUpKeys = emptySet(),
                completedKeys = emptySet()
            )
        )

        assertEquals(7, plan.assignments.size)
        assertTrue(plan.assignments.any { it.assignedDate == sunday })
    }
}
