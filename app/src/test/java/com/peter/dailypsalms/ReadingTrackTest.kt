package com.peter.dailypsalms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReadingTrackTest {

    private val monday = LocalDate.of(2026, 9, 21)

    @Test
    fun everyTrack_hasTheExpectedDailyShape() {
        val expectedCounts = mapOf(
            ReadingTrack.CLASSIC to mapOf("Psalms" to 5, "Proverbs" to 1),
            ReadingTrack.CLASSIC_PSALMS_ONLY to mapOf("Psalms" to 5),
            ReadingTrack.PACED to mapOf("Psalms" to 1, "Proverbs" to 1),
            ReadingTrack.PSALMS_ONLY to mapOf("Psalms" to 1),
            ReadingTrack.PROVERBS_ONLY to mapOf("Proverbs" to 1)
        )

        expectedCounts.forEach { (track, expected) ->
            val actual = getAssignedChapters(
                date = monday,
                track = track,
                planStartDate = monday
            ).groupingBy { it.book }.eachCount()

            assertEquals(track.name, expected, actual)
        }
    }

    @Test
    fun graceDay_hasNormalReading_forEveryTrack() {
        val sunday = monday.plusDays(6)

        ReadingTrack.entries.forEach { track ->
            val assignments = getAssignedChapters(
                date = sunday,
                track = track,
                planStartDate = monday
            )

            assertTrue("$track should have a Grace Day assignment", assignments.isNotEmpty())
            assertTrue(
                "$track assignments should belong to Sunday",
                assignments.all { it.assignedDate == sunday }
            )
        }
    }

    @Test
    fun pacedTracks_advanceByPlanDay_afterGraceDay() {
        val sunday = monday.plusDays(6)
        val nextMonday = monday.plusDays(7)

        val sundayAssignments = getAssignedChapters(
            date = sunday,
            track = ReadingTrack.PACED,
            planStartDate = monday
        )
        val mondayAssignments = getAssignedChapters(
            date = nextMonday,
            track = ReadingTrack.PACED,
            planStartDate = monday
        )

        assertEquals(listOf(7), sundayAssignments.filter { it.book == "Psalms" }.map { it.chapter })
        assertEquals(listOf(8), mondayAssignments.filter { it.book == "Psalms" }.map { it.chapter })
        assertEquals(listOf(7), sundayAssignments.filter { it.book == "Proverbs" }.map { it.chapter })
        assertEquals(listOf(8), mondayAssignments.filter { it.book == "Proverbs" }.map { it.chapter })
    }
}
