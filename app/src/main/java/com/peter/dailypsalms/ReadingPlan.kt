package com.peter.dailypsalms

import java.time.LocalDate

fun getCycleDates(
    today: LocalDate,
    graceDay: GraceDayOption,
    planStartDate: LocalDate
): List<LocalDate> {
    val cycleStart = getCycleStartDate(today, graceDay)

    val start = if (cycleStart.isBefore(planStartDate)) {
        planStartDate
    } else {
        cycleStart
    }

    if (start.isAfter(today)) return emptyList()

    return generateSequence(start) { current ->
        current.plusDays(1).takeIf { !it.isAfter(today) }
    }.toList()
}