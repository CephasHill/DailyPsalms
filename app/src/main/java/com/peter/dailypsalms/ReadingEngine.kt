package com.peter.dailypsalms

import java.time.LocalDate

data class DailyPlanInput(
    val today: LocalDate,
    val track: ReadingTrack,
    val graceDay: GraceDayOption,
    val planStartDate: LocalDate,
    val pendingCatchUpDates: Set<LocalDate>,
    val activeCatchUpKeys: Set<String>,
    val completedKeys: Set<String>
)

data class DailyPlan(
    val assignments: List<AssignedChapter>
)

/**
 * Calculates the assignments that should be visible on the daily screen.
 * This function has no Android or DataStore dependencies so the rollover
 * behavior can be tested with ordinary fixed dates.
 */
object ReadingEngine {

    fun buildDailyPlan(input: DailyPlanInput): DailyPlan {
        val cycleDates = getCycleDates(
            today = input.today,
            graceDay = input.graceDay,
            planStartDate = input.planStartDate
        )

        val allAssignmentDates = (input.pendingCatchUpDates + cycleDates)
            .distinct()
            .sorted()

        val assignments = allAssignmentDates.flatMap { date ->
            getAssignedChapters(date, input.track, input.planStartDate)
                .filter { assignment ->
                    if (date.isBefore(input.today)) {
                        val key = assignmentKey(assignment)

                        // A checked catch-up stays visible for the current day
                        // when its key is part of the active catch-up set. On
                        // later days, only unread assignments carry forward.
                        (input.pendingCatchUpDates.contains(date) &&
                                input.activeCatchUpKeys.contains(key)) ||
                                !input.completedKeys.contains(key)
                    } else {
                        // Always show today's normal assignments, even after
                        // they have been checked.
                        true
                    }
                }
        }

        return DailyPlan(assignments = assignments)
    }
}
