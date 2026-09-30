package com.peter.dailypsalms

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The streak-related values that are changed when a day's playlist becomes
 * complete or incomplete.
 */
data class StreakState(
    val streak: Int,
    val lastCompletedDate: LocalDate?,
    val historicalDates: Set<LocalDate>,
    val recordStreak: Int,
    val streakResetDate: LocalDate?
)

/**
 * The exact state before a completion credit. It lets an undo restore the
 * calendar even when the credit filled several missed dates at once.
 */
data class StreakUndoSnapshot(
    val date: LocalDate,
    val streak: Int,
    val lastCompletedDate: LocalDate?,
    val historicalDates: Set<LocalDate>,
    val streakResetDate: LocalDate?
)

enum class CompletionSnapshotAction {
    KEEP,
    SAVE,
    CLEAR
}

data class StreakTransitionInput(
    val today: LocalDate,
    val cycleStartDate: LocalDate,
    val cycleDates: List<LocalDate>,
    val wasComplete: Boolean,
    val isComplete: Boolean,
    val state: StreakState,
    val undoSnapshot: StreakUndoSnapshot? = null
)

data class StreakTransitionResult(
    val state: StreakState,
    val snapshotAction: CompletionSnapshotAction,
    val snapshotToSave: StreakUndoSnapshot? = null,
    val shouldShowCompletionAnimation: Boolean = false
)

/**
 * Pure streak transition logic. DataStore and UI code stay outside this
 * object; this object only decides the next streak state.
 */
object StreakEngine {

    fun apply(input: StreakTransitionInput): StreakTransitionResult {
        return when {
            input.isComplete && !input.wasComplete -> credit(input)
            !input.isComplete && input.wasComplete -> undo(input)
            else -> StreakTransitionResult(
                state = input.state,
                snapshotAction = CompletionSnapshotAction.KEEP
            )
        }
    }

    private fun credit(input: StreakTransitionInput): StreakTransitionResult {
        val stateBeforeCredit = input.state
        val snapshot = StreakUndoSnapshot(
            date = input.today,
            streak = stateBeforeCredit.streak,
            lastCompletedDate = stateBeforeCredit.lastCompletedDate,
            historicalDates = stateBeforeCredit.historicalDates,
            streakResetDate = stateBeforeCredit.streakResetDate
        )

        val wasResetForCycle = stateBeforeCredit.streakResetDate == input.cycleStartDate
        val isWithinCycle = if (wasResetForCycle) {
            false
        } else {
            stateBeforeCredit.lastCompletedDate?.let { lastDate ->
                !lastDate.isBefore(input.cycleStartDate.minusDays(1))
            } ?: (stateBeforeCredit.streak > 0)
        }

        val gapDates: Set<LocalDate>
        val newStreak: Int
        val lastDate = stateBeforeCredit.lastCompletedDate

        if (wasResetForCycle) {
            // A previous cycle expired. Start again from this cycle's dates,
            // including today's normal reading (including on Grace Day).
            gapDates = (input.cycleDates + input.today).toSet()
            newStreak = gapDates.size.coerceAtLeast(1)
        } else if (
            lastDate != null &&
            isWithinCycle &&
            lastDate != input.today
        ) {
            gapDates = datesFromExclusive(lastDate, input.today)
            val daysToAdd = ChronoUnit.DAYS
                .between(lastDate, input.today)
                .toInt()
                .coerceAtLeast(1)
            newStreak = stateBeforeCredit.streak + daysToAdd
        } else {
            gapDates = setOf(input.today)
            newStreak = 1
        }

        val nextState = stateBeforeCredit.copy(
            streak = newStreak,
            lastCompletedDate = input.today,
            historicalDates = stateBeforeCredit.historicalDates + gapDates,
            recordStreak = maxOf(stateBeforeCredit.recordStreak, newStreak),
            streakResetDate = null
        )

        return StreakTransitionResult(
            state = nextState,
            snapshotAction = CompletionSnapshotAction.SAVE,
            snapshotToSave = snapshot,
            shouldShowCompletionAnimation = true
        )
    }

    private fun undo(input: StreakTransitionInput): StreakTransitionResult {
        val snapshot = input.undoSnapshot
        if (snapshot != null && snapshot.date == input.today) {
            // Keep the record: undoing a personal best should not erase the
            // historical record, matching the existing app behavior.
            val restoredState = StreakState(
                streak = snapshot.streak,
                lastCompletedDate = snapshot.lastCompletedDate,
                historicalDates = snapshot.historicalDates,
                recordStreak = input.state.recordStreak,
                streakResetDate = snapshot.streakResetDate
            )
            return StreakTransitionResult(
                state = restoredState,
                snapshotAction = CompletionSnapshotAction.CLEAR
            )
        }

        // Compatibility path for completions made before undo snapshots were
        // stored. It deliberately leaves any unrelated snapshot untouched.
        if (input.state.lastCompletedDate == input.today) {
            return StreakTransitionResult(
                state = input.state.copy(
                    streak = maxOf(0, input.state.streak - 1),
                    lastCompletedDate = input.today.minusDays(1),
                    historicalDates = input.state.historicalDates - input.today
                ),
                snapshotAction = CompletionSnapshotAction.KEEP
            )
        }

        return StreakTransitionResult(
            state = input.state,
            snapshotAction = CompletionSnapshotAction.KEEP
        )
    }

    private fun datesFromExclusive(
        start: LocalDate,
        endInclusive: LocalDate
    ): Set<LocalDate> {
        val dates = mutableSetOf<LocalDate>()
        var current = start.plusDays(1)
        while (!current.isAfter(endInclusive)) {
            dates += current
            current = current.plusDays(1)
        }
        return dates
    }
}
