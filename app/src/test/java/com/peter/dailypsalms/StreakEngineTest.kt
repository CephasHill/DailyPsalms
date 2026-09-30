package com.peter.dailypsalms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StreakEngineTest {

    private val monday = LocalDate.of(2026, 9, 21)

    @Test
    fun firstCompletion_startsStreakAndSavesUndoSnapshot() {
        val result = StreakEngine.apply(
            input(
                today = monday,
                cycleStartDate = monday,
                cycleDates = listOf(monday),
                wasComplete = false,
                isComplete = true,
                state = emptyState()
            )
        )

        assertEquals(1, result.state.streak)
        assertEquals(monday, result.state.lastCompletedDate)
        assertEquals(setOf(monday), result.state.historicalDates)
        assertEquals(1, result.state.recordStreak)
        assertEquals(CompletionSnapshotAction.SAVE, result.snapshotAction)
        assertEquals(0, result.snapshotToSave?.streak)
        assertTrue(result.shouldShowCompletionAnimation)
    }

    @Test
    fun completionAfterMissedDays_creditsEveryCalendarDate() {
        val wednesday = monday.plusDays(2)
        val result = StreakEngine.apply(
            input(
                today = wednesday,
                cycleStartDate = monday,
                cycleDates = listOf(monday, monday.plusDays(1), wednesday),
                wasComplete = false,
                isComplete = true,
                state = emptyState().copy(
                    streak = 1,
                    lastCompletedDate = monday,
                    historicalDates = setOf(monday),
                    recordStreak = 1
                )
            )
        )

        assertEquals(3, result.state.streak)
        assertEquals(
            setOf(monday, monday.plusDays(1), wednesday),
            result.state.historicalDates
        )
    }

    @Test
    fun completionAfterExpiredCycle_startsNewStreakWithoutErasingCalendarHistory() {
        val tuesday = monday.plusDays(1)
        val previousSunday = monday.minusDays(1)
        val result = StreakEngine.apply(
            input(
                today = tuesday,
                cycleStartDate = monday,
                cycleDates = listOf(monday, tuesday),
                wasComplete = false,
                isComplete = true,
                state = StreakState(
                    streak = 0,
                    lastCompletedDate = previousSunday,
                    historicalDates = setOf(previousSunday),
                    recordStreak = 4,
                    streakResetDate = monday
                )
            )
        )

        assertEquals(2, result.state.streak)
        assertEquals(
            setOf(previousSunday, monday, tuesday),
            result.state.historicalDates
        )
        assertEquals(4, result.state.recordStreak)
        assertEquals(null, result.state.streakResetDate)
    }

    @Test
    fun undoWithSnapshot_restoresExactStateButKeepsRecord() {
        val today = monday.plusDays(2)
        val before = StreakState(
            streak = 2,
            lastCompletedDate = monday.plusDays(1),
            historicalDates = setOf(monday, monday.plusDays(1)),
            recordStreak = 5,
            streakResetDate = null
        )
        val credit = StreakEngine.apply(
            input(
                today = today,
                cycleStartDate = monday,
                cycleDates = listOf(monday, monday.plusDays(1), today),
                wasComplete = false,
                isComplete = true,
                state = before
            )
        )

        val undo = StreakEngine.apply(
            input(
                today = today,
                cycleStartDate = monday,
                cycleDates = listOf(monday, monday.plusDays(1), today),
                wasComplete = true,
                isComplete = false,
                state = credit.state,
                undoSnapshot = credit.snapshotToSave
            )
        )

        assertEquals(before, undo.state)
        assertEquals(CompletionSnapshotAction.CLEAR, undo.snapshotAction)
    }

    @Test
    fun legacyUndoWithoutSnapshot_removesTodayOnly() {
        val today = monday.plusDays(2)
        val state = StreakState(
            streak = 3,
            lastCompletedDate = today,
            historicalDates = setOf(monday, monday.plusDays(1), today),
            recordStreak = 5,
            streakResetDate = null
        )

        val result = StreakEngine.apply(
            input(
                today = today,
                cycleStartDate = monday,
                cycleDates = listOf(monday, monday.plusDays(1), today),
                wasComplete = true,
                isComplete = false,
                state = state
            )
        )

        assertEquals(2, result.state.streak)
        assertEquals(today.minusDays(1), result.state.lastCompletedDate)
        assertEquals(setOf(monday, monday.plusDays(1)), result.state.historicalDates)
        assertEquals(CompletionSnapshotAction.KEEP, result.snapshotAction)
    }

    @Test
    fun unrelatedToggle_doesNotChangeStreak() {
        val state = emptyState().copy(
            streak = 4,
            lastCompletedDate = monday,
            historicalDates = setOf(monday),
            recordStreak = 4
        )

        val result = StreakEngine.apply(
            input(
                today = monday.plusDays(1),
                cycleStartDate = monday,
                cycleDates = listOf(monday, monday.plusDays(1)),
                wasComplete = false,
                isComplete = false,
                state = state
            )
        )

        assertEquals(state, result.state)
        assertEquals(CompletionSnapshotAction.KEEP, result.snapshotAction)
    }

    private fun emptyState() = StreakState(
        streak = 0,
        lastCompletedDate = null,
        historicalDates = emptySet(),
        recordStreak = 0,
        streakResetDate = null
    )

    private fun input(
        today: LocalDate,
        cycleStartDate: LocalDate,
        cycleDates: List<LocalDate>,
        wasComplete: Boolean,
        isComplete: Boolean,
        state: StreakState,
        undoSnapshot: StreakUndoSnapshot? = null
    ) = StreakTransitionInput(
        today = today,
        cycleStartDate = cycleStartDate,
        cycleDates = cycleDates,
        wasComplete = wasComplete,
        isComplete = isComplete,
        state = state,
        undoSnapshot = undoSnapshot
    )
}
