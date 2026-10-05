package com.peter.dailypsalms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DebugScenarioFixturesTest {
    private val wednesday = LocalDate.of(2026, 10, 14)

    private fun preset(value: DebugScenarioPreset): DebugScenarioFixture = DebugScenarioFixtures.build(
        value.spec(wednesday, ReadingTrack.CLASSIC, GraceDayOption.SUNDAY)
    )

    private fun reconcile(fixture: DebugScenarioFixture, date: LocalDate): CatchUpReconciliationResult =
        CatchUpEngine.reconcile(
            CatchUpReconciliationInput(
                today = date,
                track = fixture.spec.track,
                graceDay = fixture.spec.graceDay,
                planStartDate = fixture.spec.effectivePlanStartDate,
                storedPendingDates = fixture.pendingDates,
                lastReconciledDate = fixture.spec.today,
                storedActiveCatchUpKeys = fixture.activeCatchUpKeys,
                cycleMarkerDate = fixture.cycleMarkerDate,
                completedKeys = fixture.completedKeys
            )
        )

    private fun complete(fixture: DebugScenarioFixture): StreakTransitionResult = StreakEngine.apply(
        StreakTransitionInput(
            today = fixture.spec.today,
            cycleStartDate = maxOf(fixture.cycleMarkerDate, fixture.spec.effectivePlanStartDate),
            cycleDates = getCycleDates(fixture.spec.today, fixture.spec.graceDay, fixture.spec.effectivePlanStartDate),
            wasComplete = false,
            isComplete = true,
            state = fixture.streakState
        )
    )

    @Test
    fun screenshotPresetHasTwentyCompletedDaysAndUnreadToday() {
        val fixture = preset(DebugScenarioPreset.STREAK_20)
        assertEquals(20, fixture.streakState.streak)
        assertEquals(20, fixture.streakState.historicalDates.size)
        assertEquals(wednesday.minusDays(1), fixture.streakState.lastCompletedDate)
        assertEquals(6, fixture.visibleAssignments.size)
        assertEquals(0, fixture.doneCount)
        assertFalse(wednesday in fixture.streakState.historicalDates)
        assertTrue(fixture.pendingDates.isEmpty())
        assertFalse(reconcile(fixture, wednesday).expiredPreviousCycle)
    }

    @Test
    fun completedScreenshotPresetIncludesTodayAndCanUndoToNineteen() {
        val fixture = preset(DebugScenarioPreset.STREAK_20_COMPLETE)
        assertEquals(20, fixture.streakState.streak)
        assertEquals(6, fixture.doneCount)
        assertTrue(wednesday in fixture.streakState.historicalDates)
        assertNotNull(fixture.undoSnapshot)
        val undo = StreakEngine.apply(
            StreakTransitionInput(
                today = wednesday,
                cycleStartDate = fixture.cycleMarkerDate,
                cycleDates = getCycleDates(wednesday, fixture.spec.graceDay, fixture.spec.effectivePlanStartDate),
                wasComplete = true,
                isComplete = false,
                state = fixture.streakState,
                undoSnapshot = fixture.undoSnapshot
            )
        )
        assertEquals(19, undo.state.streak)
        assertFalse(wednesday in undo.state.historicalDates)
        assertEquals(20, undo.state.recordStreak)
    }

    @Test
    fun partialTodayDoesNotCreditToday() {
        val fixture = preset(DebugScenarioPreset.STREAK_20_PARTIAL)
        assertEquals(3, fixture.doneCount)
        assertEquals(20, fixture.streakState.streak)
        assertFalse(wednesday in fixture.streakState.historicalDates)
    }

    @Test
    fun twoMissedDaysProduceTwelveCatchUpChaptersAndFinishingCreditsAllThreeDays() {
        val fixture = preset(DebugScenarioPreset.TWO_MISSED_DAYS)
        assertEquals(18, fixture.visibleAssignments.size)
        assertEquals(12, fixture.visibleAssignments.count { it.assignedDate.isBefore(wednesday) })
        assertEquals(setOf(wednesday.minusDays(2), wednesday.minusDays(1)), fixture.pendingDates)
        assertEquals(20, fixture.streakState.streak)
        val completion = complete(fixture)
        assertEquals(23, completion.state.streak)
        assertTrue(completion.state.historicalDates.containsAll(listOf(wednesday.minusDays(2), wednesday.minusDays(1), wednesday)))
    }

    @Test
    fun finishingOnlyTodayLeavesCatchUpAndStreakUnchanged() {
        val fixture = preset(DebugScenarioPreset.TODAY_DONE_CATCH_UP_LEFT)
        assertEquals(18, fixture.visibleAssignments.size)
        assertEquals(6, fixture.doneCount)
        assertEquals(20, fixture.streakState.streak)
        assertFalse(wednesday in fixture.streakState.historicalDates)
        assertEquals(null, fixture.undoSnapshot)
    }

    @Test
    fun checkedCatchUpStaysVisibleTodayAndDropsOutTomorrow() {
        val fixture = preset(DebugScenarioPreset.PARTIAL_CATCH_UP)
        val checkedCatchUp = fixture.spec.readCatchUpToday
        assertEquals(3, checkedCatchUp.size)
        assertEquals(3, fixture.doneCount)
        assertTrue(fixture.activeCatchUpKeys.containsAll(checkedCatchUp))
        assertTrue(reconcile(fixture, wednesday).activeCatchUpKeys.containsAll(checkedCatchUp))
        val next = reconcile(fixture, wednesday.plusDays(1))
        assertTrue(next.activeCatchUpKeys.intersect(checkedCatchUp).isEmpty())
        assertEquals(15, next.activeCatchUpKeys.size)
    }

    @Test
    fun finishingAllCatchUpTodayCreditsAndSupportsUndoOfAllAddedCalendarDates() {
        val base = DebugScenarioPreset.TWO_MISSED_DAYS.spec(wednesday, ReadingTrack.CLASSIC, GraceDayOption.SUNDAY)
        val fixture = DebugScenarioFixtures.build(base.copy(
            readOnAssignedDate = base.assignments().filter { it.assignedDate == wednesday }.map(::assignmentKey).toSet(),
            readCatchUpToday = base.assignments().filter { it.assignedDate.isBefore(wednesday) }.map(::assignmentKey).toSet()
        ))
        assertEquals(23, fixture.streakState.streak)
        assertEquals(18, fixture.doneCount)
        assertEquals(12, fixture.activeCatchUpKeys.size)
        val snapshot = requireNotNull(fixture.undoSnapshot)
        assertEquals(20, snapshot.streak)
        assertFalse(wednesday.minusDays(2) in snapshot.historicalDates)
        assertTrue(wednesday.minusDays(2) in fixture.streakState.historicalDates)
    }

    @Test
    fun sundayDeadlineExpiresOnMondayAndKeepsHistoricalRecord() {
        val sunday = LocalDate.of(2026, 10, 18)
        val fixture = DebugScenarioFixtures.build(
            DebugScenarioPreset.GRACE_DEADLINE.spec(sunday, ReadingTrack.CLASSIC, GraceDayOption.SUNDAY)
        )
        assertEquals(20, fixture.streakState.streak)
        assertEquals(12, fixture.visibleAssignments.size)
        val monday = sunday.plusDays(1)
        val next = reconcile(fixture, monday)
        assertTrue(next.expiredPreviousCycle)
        assertTrue(next.pendingDates.isEmpty())
        val resetState = fixture.streakState.copy(streak = 0, streakResetDate = next.cycleStartDate)
        val finish = StreakEngine.apply(StreakTransitionInput(
            today = monday,
            cycleStartDate = next.cycleStartDate,
            cycleDates = getCycleDates(monday, fixture.spec.graceDay, fixture.spec.effectivePlanStartDate),
            wasComplete = false,
            isComplete = true,
            state = resetState
        ))
        assertEquals(1, finish.state.streak)
        assertEquals(20, finish.state.recordStreak)
        assertTrue(finish.state.historicalDates.containsAll(fixture.streakState.historicalDates))
    }

    @Test
    fun noGraceDayExpiresMissedReadingsInsteadOfCreatingCatchUp() {
        val fixture = DebugScenarioFixtures.build(DebugScenarioSpec(
            today = wednesday, graceDay = GraceDayOption.NONE, editableDays = 3
        ))
        assertEquals(0, fixture.streakState.streak)
        assertEquals(20, fixture.streakState.recordStreak)
        assertEquals(6, fixture.visibleAssignments.size)
        assertTrue(fixture.pendingDates.isEmpty())
    }

    @Test
    fun pacedTrackHonorsExplicitPlanAnchor() {
        val fixture = DebugScenarioFixtures.build(DebugScenarioSpec(
            today = wednesday, track = ReadingTrack.PACED, planStartDate = wednesday.minusDays(40)
        ))
        assertEquals(listOf(41, 10), fixture.visibleAssignments.map { it.chapter })
        assertEquals(20, fixture.streakState.streak)
    }

    @Test
    fun psalm119PartsUseDistinctStoredKeys() {
        val date = LocalDate.of(2026, 10, 31)
        val fixture = DebugScenarioFixtures.build(
            DebugScenarioPreset.STREAK_20_COMPLETE.spec(date, ReadingTrack.CLASSIC, GraceDayOption.SUNDAY)
        )
        val parts = fixture.visibleAssignments.filter { it.book == "Psalms" }
        assertEquals(listOf(1, 2, 3, 4, 5), parts.map { it.partId })
        assertEquals(5, parts.map(::assignmentKey).toSet().size)
        assertEquals(6, fixture.doneCount)
        assertEquals(20, fixture.streakState.streak)
    }

    @Test
    fun freshReaderHasNoUnscheduledCatchUpOrStreak() {
        val fixture = preset(DebugScenarioPreset.NEW_READER)
        assertEquals(0, fixture.streakState.streak)
        assertTrue(fixture.streakState.historicalDates.isEmpty())
        assertTrue(fixture.pendingDates.isEmpty())
        assertEquals(wednesday, fixture.spec.effectivePlanStartDate)
        assertEquals(6, fixture.visibleAssignments.size)
    }

    @Test
    fun catchUpBeforeEditableWindowCanStillBeReadToday() {
        val base = DebugScenarioSpec(today = wednesday, initialStreak = 0, planStartDate = wednesday.minusDays(10))
        val assignments = getCycleDates(wednesday, base.graceDay, base.effectivePlanStartDate)
            .flatMap { getAssignedChapters(it, base.track, base.effectivePlanStartDate) }
        val fixture = DebugScenarioFixtures.build(base.copy(
            readOnAssignedDate = assignments.filter { it.assignedDate == wednesday }.map(::assignmentKey).toSet(),
            readCatchUpToday = assignments.filter { it.assignedDate.isBefore(wednesday) }.map(::assignmentKey).toSet()
        ))
        assertEquals(18, fixture.visibleAssignments.size)
        assertEquals(18, fixture.doneCount)
        assertEquals(12, fixture.activeCatchUpKeys.size)
        assertNotNull(fixture.undoSnapshot)
    }

    @Test(expected = IllegalArgumentException::class)
    fun planAnchorCannotContradictSeededReadingHistory() {
        DebugScenarioFixtures.build(DebugScenarioSpec(today = wednesday, planStartDate = wednesday))
    }
}
