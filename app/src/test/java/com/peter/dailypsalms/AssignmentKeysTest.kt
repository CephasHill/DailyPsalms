package com.peter.dailypsalms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDate

class AssignmentKeysTest {

    private val date = LocalDate.of(2026, 9, 21)

    @Test
    fun ordinaryAssignment_includesBookChapterAndDate() {
        val assignment = AssignedChapter(
            book = "Psalms",
            chapter = 7,
            assignedDate = date
        )

        assertEquals("Psalms_7_2026-09-21", assignmentKey(assignment))
    }

    @Test
    fun psalm119Parts_haveDifferentKeys() {
        val partOne = AssignedChapter("Psalms", 119, date, partId = 1)
        val partTwo = AssignedChapter("Psalms", 119, date, partId = 2)

        assertEquals("Psalms_119_part1_2026-09-21", assignmentKey(partOne))
        assertEquals("Psalms_119_part2_2026-09-21", assignmentKey(partTwo))
        assertNotEquals(assignmentKey(partOne), assignmentKey(partTwo))
    }

    @Test
    fun sameChapterOnDifferentDates_hasDifferentKeys() {
        val monday = AssignedChapter("Psalms", 7, date)
        val tuesday = AssignedChapter("Psalms", 7, date.plusDays(1))

        assertNotEquals(assignmentKey(monday), assignmentKey(tuesday))
    }

    @Test
    fun dailyReadingKey_matchesAssignedChapterKey() {
        val chapter = ChapterData(
            book = "Psalms",
            chapter = 119,
            chapterFootnote = null,
            content = emptyList(),
            footnotes = emptyList()
        )
        val reading = DailyReading(
            chapterData = chapter,
            assignedDate = date,
            isCatchUp = true,
            partId = 3
        )

        assertEquals(
            assignmentKey(AssignedChapter("Psalms", 119, date, partId = 3)),
            reading.uniqueKey
        )
    }
}
