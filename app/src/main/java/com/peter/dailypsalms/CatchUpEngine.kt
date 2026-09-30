package com.peter.dailypsalms

import java.time.LocalDate

data class CatchUpReconciliationInput(
    val today: LocalDate,
    val track: ReadingTrack,
    val graceDay: GraceDayOption,
    val planStartDate: LocalDate,
    val storedPendingDates: Set<LocalDate>,
    val lastReconciledDate: LocalDate?,
    val storedActiveCatchUpKeys: Set<String>?,
    val cycleMarkerDate: LocalDate?,
    val completedKeys: Set<String>,
    val containsInvalidPendingDate: Boolean = false
)

data class CatchUpReconciliationResult(
    val pendingDates: Set<LocalDate>,
    val activeCatchUpKeys: Set<String>,
    val cycleStartDate: LocalDate,
    val expiredPreviousCycle: Boolean
)

/**
 * Applies the date-transition rules for catch-up readings.
 *
 * This does not change the streak itself. It only reports whether the caller
 * must reset the streak because an earlier cycle expired with unread work.
 */
object CatchUpEngine {

    fun reconcile(input: CatchUpReconciliationInput): CatchUpReconciliationResult {
        val cycleStartDate = getCycleStartDate(input.today, input.graceDay)
        val nextPendingDates = input.storedPendingDates.toMutableSet()

        fun assignmentsFor(date: LocalDate): List<AssignedChapter> =
            getAssignedChapters(date, input.track, input.planStartDate)

        fun hasIncompleteAssignment(date: LocalDate): Boolean =
            assignmentsFor(date).any { assignment ->
                !input.completedKeys.contains(assignmentKey(assignment))
            }

        fun incompleteKeysForDates(dates: Set<LocalDate>): Set<String> =
            dates.flatMap { date ->
                assignmentsFor(date)
                    .filter { assignment ->
                        !input.completedKeys.contains(assignmentKey(assignment))
                    }
                    .map(::assignmentKey)
            }.toSet()

        var expiredPreviousCycle = input.containsInvalidPendingDate ||
                input.storedPendingDates.any { date ->
                    date.isBefore(cycleStartDate) && hasIncompleteAssignment(date)
                }

        // When the date changes, completed pending dates no longer need to be
        // stored. On the same day, preserve them so checked catch-up remains
        // visible until tomorrow.
        if (input.lastReconciledDate != input.today) {
            nextPendingDates.removeAll { date ->
                !hasIncompleteAssignment(date)
            }
        }

        // If the cycle marker moved forward, scan the previous cycle through
        // the grace day. That grace-day assignment is part of the deadline.
        val previousCycleStart = input.cycleMarkerDate
        if (previousCycleStart != null &&
            previousCycleStart != cycleStartDate &&
            previousCycleStart.isBefore(cycleStartDate)
        ) {
            val previousScanStart = if (previousCycleStart.isBefore(input.planStartDate)) {
                input.planStartDate
            } else {
                previousCycleStart
            }
            val previousScanEnd = cycleStartDate.minusDays(1)

            if (!previousScanStart.isAfter(previousScanEnd)) {
                var date = previousScanStart
                while (!date.isAfter(previousScanEnd)) {
                    if (hasIncompleteAssignment(date)) {
                        expiredPreviousCycle = true
                        break
                    }
                    date = date.plusDays(1)
                }
            }
        }

        if (expiredPreviousCycle) {
            nextPendingDates.removeAll { date -> date.isBefore(cycleStartDate) }
        }

        // Missed assignments from the current cycle become durable catch-up
        // dates once their day has passed.
        getCycleDates(input.today, input.graceDay, input.planStartDate)
            .filter { it.isBefore(input.today) }
            .forEach { date ->
                if (hasIncompleteAssignment(date)) {
                    nextPendingDates += date
                }
            }

        val activeCatchUpKeys = if (
            input.lastReconciledDate != input.today ||
            input.storedActiveCatchUpKeys == null
        ) {
            incompleteKeysForDates(nextPendingDates)
        } else {
            input.storedActiveCatchUpKeys
        }

        return CatchUpReconciliationResult(
            pendingDates = nextPendingDates,
            activeCatchUpKeys = activeCatchUpKeys,
            cycleStartDate = cycleStartDate,
            expiredPreviousCycle = expiredPreviousCycle
        )
    }
}
