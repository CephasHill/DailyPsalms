package com.peter.dailypsalms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CatchUpEngineTest {

    private val monday = LocalDate.of(2026, 9, 21)
    private val sunday = monday.plusDays(6)
    private val nextMonday = monday.plusDays(7)

    @Test
    fun missedPreviousDay_becomesPendingCatchUp() {
        val assignment = getAssignedChapters(monday, ReadingTrack.PSALMS_ONLY, monday).single()
        val result = CatchUpEngine.reconcile(
            input = input(
                today = monday.plusDays(1),
                lastReconciledDate = monday
            )
        )

        assertEquals(setOf(monday), result.pendingDates)
        assertTrue(result.activeCatchUpKeys.contains(assignmentKey(assignment)))
        assertFalse(result.expiredPreviousCycle)
    }

    @Test
    fun checkedCatchUp_isPreservedWhenReconciledOnTheSameDay() {
        val assignment = getAssignedChapters(monday, ReadingTrack.PSALMS_ONLY, monday).single()
        val key = assignmentKey(assignment)

        val result = CatchUpEngine.reconcile(
            input = input(
                today = monday.plusDays(1),
                storedPendingDates = setOf(monday),
                lastReconciledDate = monday.plusDays(1),
                storedActiveCatchUpKeys = setOf(key),
                completedKeys = setOf(key)
            )
        )

        assertEquals(setOf(monday), result.pendingDates)
        assertEquals(setOf(key), result.activeCatchUpKeys)
    }

    @Test
    fun completedCatchUp_isRemovedWhenTheNextDayBegins() {
        val mondayAssignment = getAssignedChapters(monday, ReadingTrack.PSALMS_ONLY, monday).single()
        val tuesdayAssignment = getAssignedChapters(monday.plusDays(1), ReadingTrack.PSALMS_ONLY, monday).single()

        val result = CatchUpEngine.reconcile(
            input = input(
                today = monday.plusDays(2),
                storedPendingDates = setOf(monday),
                lastReconciledDate = monday.plusDays(1),
                completedKeys = setOf(assignmentKey(mondayAssignment))
            )
        )

        assertEquals(setOf(monday.plusDays(1)), result.pendingDates)
        assertTrue(result.activeCatchUpKeys.contains(assignmentKey(tuesdayAssignment)))
    }

    @Test
    fun unreadGraceDay_assignmentExpiresAtTheNextRollover() {
        val completedBeforeGrace = (0L..5L)
            .map { monday.plusDays(it) }
            .flatMap { date ->
                getAssignedChapters(date, ReadingTrack.PSALMS_ONLY, monday)
            }
            .map(::assignmentKey)
            .toSet()

        val result = CatchUpEngine.reconcile(
            input = input(
                today = nextMonday,
                storedPendingDates = emptySet(),
                lastReconciledDate = sunday,
                cycleMarkerDate = monday,
                completedKeys = completedBeforeGrace
            )
        )

        assertTrue(result.expiredPreviousCycle)
        assertTrue(result.pendingDates.none { it.isBefore(nextMonday) })
        assertTrue(result.activeCatchUpKeys.isEmpty())
    }

    @Test
    fun fullyCompletedPreviousCycle_doesNotExpire() {
        val completedKeys = (0L..6L)
            .map { monday.plusDays(it) }
            .flatMap { date ->
                getAssignedChapters(date, ReadingTrack.PSALMS_ONLY, monday)
            }
            .map(::assignmentKey)
            .toSet()

        val result = CatchUpEngine.reconcile(
            input = input(
                today = nextMonday,
                storedPendingDates = setOf(sunday),
                lastReconciledDate = sunday,
                cycleMarkerDate = monday,
                completedKeys = completedKeys
            )
        )

        assertFalse(result.expiredPreviousCycle)
        assertTrue(result.pendingDates.isEmpty())
    }

    private fun input(
        today: LocalDate,
        storedPendingDates: Set<LocalDate> = emptySet(),
        lastReconciledDate: LocalDate? = null,
        storedActiveCatchUpKeys: Set<String>? = null,
        cycleMarkerDate: LocalDate? = null,
        completedKeys: Set<String> = emptySet()
    ) = CatchUpReconciliationInput(
        today = today,
        track = ReadingTrack.PSALMS_ONLY,
        graceDay = GraceDayOption.SUNDAY,
        planStartDate = monday,
        storedPendingDates = storedPendingDates,
        lastReconciledDate = lastReconciledDate,
        storedActiveCatchUpKeys = storedActiveCatchUpKeys,
        cycleMarkerDate = cycleMarkerDate,
        completedKeys = completedKeys
    )
}
