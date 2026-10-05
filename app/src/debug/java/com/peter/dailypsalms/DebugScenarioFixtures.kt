package com.peter.dailypsalms

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Inputs describe when chapters were read; replay uses the real app engines. */
data class DebugScenarioSpec(
    val today: LocalDate,
    val track: ReadingTrack = ReadingTrack.CLASSIC,
    val graceDay: GraceDayOption = GraceDayOption.SUNDAY,
    val initialStreak: Int = 20,
    val editableDays: Int = 1,
    val planStartDate: LocalDate? = null,
    val readOnAssignedDate: Set<String> = emptySet(),
    val readCatchUpToday: Set<String> = emptySet()
) {
    val firstEditableDate: LocalDate
        get() = today.minusDays(editableDays.toLong() - 1)

    val firstBaselineDate: LocalDate
        get() = firstEditableDate.minusDays(initialStreak.toLong())

    val effectivePlanStartDate: LocalDate
        get() = planStartDate ?: firstBaselineDate

    fun editableDates(): List<LocalDate> =
        (0 until editableDays).map { firstEditableDate.plusDays(it.toLong()) }

    fun assignments(): List<AssignedChapter> = editableDates().flatMap { date ->
        getAssignedChapters(date, track, effectivePlanStartDate)
    }
}

data class DebugScenarioFixture(
    val spec: DebugScenarioSpec,
    val completedKeys: Set<String>,
    val streakState: StreakState,
    val pendingDates: Set<LocalDate>,
    val activeCatchUpKeys: Set<String>,
    val cycleMarkerDate: LocalDate,
    val undoSnapshot: StreakUndoSnapshot?,
    val visibleAssignments: List<AssignedChapter>
) {
    val doneCount: Int
        get() = visibleAssignments.count { assignmentKey(it) in completedKeys }
}

object DebugScenarioFixtures {
    fun build(spec: DebugScenarioSpec): DebugScenarioFixture {
        require(spec.initialStreak in 0..3650) { "Starting streak must be 0–3650." }
        require(spec.editableDays in 1..31) { "Editable days must be 1–31." }
        require(!spec.effectivePlanStartDate.isAfter(spec.firstBaselineDate)) {
            "Plan start must be on or before ${spec.firstBaselineDate}. Leave it blank for automatic."
        }

        val baselineDates = (0 until spec.initialStreak)
            .map { spec.firstBaselineDate.plusDays(it.toLong()) }
            .toSet()
        var completedKeys = baselineDates.flatMap { date ->
            getAssignedChapters(date, spec.track, spec.effectivePlanStartDate).map(::assignmentKey)
        }.toSet()

        var state = StreakState(
            streak = spec.initialStreak,
            lastCompletedDate = baselineDates.maxOrNull(),
            historicalDates = baselineDates,
            recordStreak = spec.initialStreak,
            streakResetDate = null
        )
        var pendingDates = emptySet<LocalDate>()
        var activeCatchUpKeys = emptySet<String>()
        var lastReconciledDate = state.lastCompletedDate
        var cycleMarkerDate = lastReconciledDate?.let { getCycleStartDate(it, spec.graceDay) }
        var todaySnapshot: StreakUndoSnapshot? = null

        val editableAssignments = spec.assignments()
        for (date in spec.editableDates()) {
            val reconciliation = CatchUpEngine.reconcile(
                CatchUpReconciliationInput(
                    today = date,
                    track = spec.track,
                    graceDay = spec.graceDay,
                    planStartDate = spec.effectivePlanStartDate,
                    storedPendingDates = pendingDates,
                    lastReconciledDate = lastReconciledDate,
                    storedActiveCatchUpKeys = activeCatchUpKeys,
                    cycleMarkerDate = cycleMarkerDate,
                    completedKeys = completedKeys
                )
            )
            pendingDates = reconciliation.pendingDates
            activeCatchUpKeys = reconciliation.activeCatchUpKeys
            cycleMarkerDate = reconciliation.cycleStartDate
            lastReconciledDate = date

            if (reconciliation.expiredPreviousCycle) {
                state = state.copy(streak = 0, streakResetDate = reconciliation.cycleStartDate)
            }

            val playlist = ReadingEngine.buildDailyPlan(
                DailyPlanInput(
                    today = date,
                    track = spec.track,
                    graceDay = spec.graceDay,
                    planStartDate = spec.effectivePlanStartDate,
                    pendingCatchUpDates = pendingDates,
                    activeCatchUpKeys = activeCatchUpKeys,
                    completedKeys = completedKeys
                )
            ).assignments
            val playlistKeys = playlist.map(::assignmentKey).toSet()
            val wasComplete = playlistKeys.isNotEmpty() && completedKeys.containsAll(playlistKeys)

            completedKeys = completedKeys + editableAssignments
                .filter { it.assignedDate == date }
                .map(::assignmentKey)
                .filter { it in spec.readOnAssignedDate }
            if (date == spec.today) {
                // Only currently visible catch-up can actually be checked today.
                val validCatchUpKeys = playlist
                    .filter { it.assignedDate.isBefore(date) }
                    .map(::assignmentKey)
                    .toSet()
                completedKeys = completedKeys + spec.readCatchUpToday
                    .intersect(validCatchUpKeys)
            }
            val isComplete = playlistKeys.isNotEmpty() && completedKeys.containsAll(playlistKeys)

            val transition = StreakEngine.apply(
                StreakTransitionInput(
                    today = date,
                    cycleStartDate = maxOf(reconciliation.cycleStartDate, spec.effectivePlanStartDate),
                    cycleDates = getCycleDates(date, spec.graceDay, spec.effectivePlanStartDate),
                    wasComplete = wasComplete,
                    isComplete = isComplete,
                    state = state
                )
            )
            state = transition.state
            if (date == spec.today) todaySnapshot = transition.snapshotToSave
        }

        val visibleAssignments = ReadingEngine.buildDailyPlan(
            DailyPlanInput(
                today = spec.today,
                track = spec.track,
                graceDay = spec.graceDay,
                planStartDate = spec.effectivePlanStartDate,
                pendingCatchUpDates = pendingDates,
                activeCatchUpKeys = activeCatchUpKeys,
                completedKeys = completedKeys
            )
        ).assignments

        return DebugScenarioFixture(
            spec = spec,
            completedKeys = completedKeys,
            streakState = state,
            pendingDates = pendingDates,
            activeCatchUpKeys = activeCatchUpKeys,
            cycleMarkerDate = requireNotNull(cycleMarkerDate),
            undoSnapshot = todaySnapshot,
            visibleAssignments = visibleAssignments
        )
    }
}

enum class DebugScenarioPreset(val title: String) {
    STREAK_20("20-day streak · ready to read"),
    STREAK_20_COMPLETE("20-day streak · completed today"),
    STREAK_20_PARTIAL("20-day streak · partial reading"),
    TWO_MISSED_DAYS("Two missed days · Wednesday"),
    PARTIAL_CATCH_UP("Some catch-up checked · Wednesday"),
    TODAY_DONE_CATCH_UP_LEFT("Today done, catch-up unfinished · Wednesday"),
    GRACE_DEADLINE("Unread catch-up · Sunday deadline"),
    NEW_READER("New reader · no history");

    fun spec(
        date: LocalDate,
        track: ReadingTrack,
        graceDay: GraceDayOption
    ): DebugScenarioSpec {
        val base = when (this) {
            TWO_MISSED_DAYS, PARTIAL_CATCH_UP, TODAY_DONE_CATCH_UP_LEFT -> DebugScenarioSpec(
                today = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.WEDNESDAY)),
                track = track,
                graceDay = GraceDayOption.SUNDAY,
                editableDays = 3
            )
            GRACE_DEADLINE -> DebugScenarioSpec(
                today = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)),
                track = track,
                graceDay = GraceDayOption.SUNDAY,
                editableDays = 2
            )
            STREAK_20_COMPLETE -> DebugScenarioSpec(date, track, graceDay, initialStreak = 19)
            NEW_READER -> DebugScenarioSpec(date, track, graceDay, initialStreak = 0)
            else -> DebugScenarioSpec(date, track, graceDay)
        }
        val todayKeys = base.assignments().filter { it.assignedDate == base.today }.map(::assignmentKey)
        return when (this) {
            STREAK_20_COMPLETE, TODAY_DONE_CATCH_UP_LEFT -> base.copy(readOnAssignedDate = todayKeys.toSet())
            STREAK_20_PARTIAL -> base.copy(readOnAssignedDate = todayKeys.take(todayKeys.size / 2).toSet())
            PARTIAL_CATCH_UP -> base.copy(
                readCatchUpToday = base.assignments()
                    .filter { it.assignedDate == base.firstEditableDate }
                    .map(::assignmentKey)
                    .take(3)
                    .toSet()
            )
            else -> base
        }
    }
}
