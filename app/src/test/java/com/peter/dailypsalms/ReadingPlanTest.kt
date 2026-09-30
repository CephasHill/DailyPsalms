package com.peter.dailypsalms

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ReadingPlanTest {

    private val monday = LocalDate.of(2026, 9, 21)

    @Test
    fun pacedTrack_assigns_normal_reading_on_grace_day() {
        val sunday = monday.plusDays(6)

        val assignments = getAssignedChapters(
            date = sunday,
            track = ReadingTrack.PACED,
            planStartDate = monday
        )

        assertEquals(
            listOf(7),
            assignments
                .filter { it.book == "Psalms" }
                .map { it.chapter }
        )

        assertEquals(
            listOf(7),
            assignments
                .filter { it.book == "Proverbs" }
                .map { it.chapter }
        )
    }

    @Test
    fun pacedTrack_advances_after_grace_day() {
        val nextMonday = monday.plusDays(7)

        val assignments = getAssignedChapters(
            date = nextMonday,
            track = ReadingTrack.PACED,
            planStartDate = monday
        )

        assertEquals(
            listOf(8),
            assignments
                .filter { it.book == "Psalms" }
                .map { it.chapter }
        )
    }

    @Test
    fun sundayGrace_cycleIncludesMondayThroughSunday() {
        val sunday = monday.plusDays(6)

        val dates = getCycleDates(
            today = sunday,
            graceDay = GraceDayOption.SUNDAY,
            planStartDate = monday
        )

        assertEquals(
            (0..6).map { monday.plusDays(it.toLong()) },
            dates
        )
    }

    @Test
    fun mondayAfterGraceDay_startsANewCycle() {
        val nextMonday = monday.plusDays(7)

        val dates = getCycleDates(
            today = nextMonday,
            graceDay = GraceDayOption.SUNDAY,
            planStartDate = monday
        )

        assertEquals(listOf(nextMonday), dates)
    }
}