package com.peter.dailypsalms

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CatchUpTrackTest {

    private val monday = LocalDate.of(2026, 9, 21)
    private val sunday = monday.plusDays(6)
    private val nextMonday = monday.plusDays(7)

    @Test
    fun unreadGraceDay_expiresAtNextCycle_forEveryTrack() {
        ReadingTrack.entries.forEach { track ->
            val completedBeforeGrace = (0L..5L)
                .map { monday.plusDays(it) }
                .flatMap { date ->
                    getAssignedChapters(date, track, monday)
                }
                .map(::assignmentKey)
                .toSet()

            val result = CatchUpEngine.reconcile(
                CatchUpReconciliationInput(
                    today = nextMonday,
                    track = track,
                    graceDay = GraceDayOption.SUNDAY,
                    planStartDate = monday,
                    storedPendingDates = emptySet(),
                    lastReconciledDate = sunday,
                    storedActiveCatchUpKeys = null,
                    cycleMarkerDate = monday,
                    completedKeys = completedBeforeGrace
                )
            )

            assertTrue(
                "$track should expire an unread Grace Day assignment",
                result.expiredPreviousCycle
            )
            assertTrue(
                "$track should not carry expired dates into the new cycle",
                result.pendingDates.none { it.isBefore(nextMonday) }
            )
            assertTrue(
                "$track should have no active expired catch-up keys",
                result.activeCatchUpKeys.isEmpty()
            )
        }
    }

    @Test
    fun completedGraceDay_doesNotExpire_forEveryTrack() {
        ReadingTrack.entries.forEach { track ->
            val completedKeys = (0L..6L)
                .map { monday.plusDays(it) }
                .flatMap { date ->
                    getAssignedChapters(date, track, monday)
                }
                .map(::assignmentKey)
                .toSet()

            val result = CatchUpEngine.reconcile(
                CatchUpReconciliationInput(
                    today = nextMonday,
                    track = track,
                    graceDay = GraceDayOption.SUNDAY,
                    planStartDate = monday,
                    storedPendingDates = setOf(sunday),
                    lastReconciledDate = sunday,
                    storedActiveCatchUpKeys = null,
                    cycleMarkerDate = monday,
                    completedKeys = completedKeys
                )
            )

            assertFalse(
                "$track should not expire when every assignment is complete",
                result.expiredPreviousCycle
            )
            assertTrue(result.pendingDates.isEmpty())
            assertTrue(result.activeCatchUpKeys.isEmpty())
        }
    }
}
