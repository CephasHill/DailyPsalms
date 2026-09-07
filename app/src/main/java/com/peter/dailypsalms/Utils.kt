package com.peter.dailypsalms

import android.content.Context
import java.time.LocalDate
import kotlin.collections.contains

fun isAssetExists(context: Context, fileName: String): Boolean {
    return try {
        context.assets.list("")?.contains(fileName) == true
    } catch (_: Exception) {
        false
    }
}

fun getCycleStartDate(today: LocalDate, graceDay: GraceDayOption): LocalDate {
    if (graceDay == GraceDayOption.NONE) return today

    val targetLastDay = when (graceDay) {
        GraceDayOption.SUNDAY -> java.time.DayOfWeek.SUNDAY
        GraceDayOption.MONDAY -> java.time.DayOfWeek.MONDAY
        GraceDayOption.TUESDAY -> java.time.DayOfWeek.TUESDAY
        GraceDayOption.WEDNESDAY -> java.time.DayOfWeek.WEDNESDAY
        GraceDayOption.THURSDAY -> java.time.DayOfWeek.THURSDAY
        GraceDayOption.FRIDAY -> java.time.DayOfWeek.FRIDAY
        GraceDayOption.SATURDAY -> java.time.DayOfWeek.SATURDAY
    }

    var start = today
    // Go backward day by day until the day BEFORE our start day is the Grace Day
    while (start.minusDays(1).dayOfWeek != targetLastDay) {
        start = start.minusDays(1)
    }
    return start
}

fun getAssignedChapters(date: LocalDate, track: ReadingTrack): List<AssignedChapter> {
    val dayOfYear = date.dayOfYear
    val dayOfMonth = date.dayOfMonth
    val list = mutableListOf<AssignedChapter>()

    when (track) {
        ReadingTrack.CLASSIC -> {
            if (dayOfMonth == 31) {
                // Request 5 specific chunks instead of 1 chapter
                list.add(AssignedChapter("Psalms", 119, date, partId = 1))
                list.add(AssignedChapter("Psalms", 119, date, partId = 2))
                list.add(AssignedChapter("Psalms", 119, date, partId = 3))
                list.add(AssignedChapter("Psalms", 119, date, partId = 4))
                list.add(AssignedChapter("Psalms", 119, date, partId = 5))
            } else {
                listOf(dayOfMonth, dayOfMonth + 30, dayOfMonth + 60, dayOfMonth + 90, dayOfMonth + 120)
                    .filter { it <= 150 && (dayOfMonth != 19 || it != 119) }
                    .forEach { list.add(AssignedChapter("Psalms", it, date)) }
            }

            list.add(AssignedChapter("Proverbs", dayOfMonth, date))
        }
        ReadingTrack.PACED -> {
            val pChap = (dayOfYear % 150).takeIf { it != 0 } ?: 150
            val prChap = (dayOfYear % 31).takeIf { it != 0 } ?: 31
            list.add(AssignedChapter("Psalms", pChap, date))
            list.add(AssignedChapter("Proverbs", prChap, date))
        }
        ReadingTrack.PSALMS_ONLY -> {
            val pChap = (dayOfYear % 150).takeIf { it != 0 } ?: 150
            list.add(AssignedChapter("Psalms", pChap, date))
        }
        ReadingTrack.PROVERBS_ONLY -> {
            val prChap = (dayOfYear % 31).takeIf { it != 0 } ?: 31
            list.add(AssignedChapter("Proverbs", prChap, date))
        }
    }
    return list
}

fun slicePsalm119(fullChapter: ChapterData, partId: Int): ChapterData {
    // 1. Define our 5 stanza blocks
    val (startVerse, endVerse) = when(partId) {
        1 -> 1 to 40
        2 -> 41 to 80
        3 -> 81 to 112
        4 -> 113 to 144
        else -> 145 to 176
    }

    // 2. Find where these verses live in the JSON array
    val startIndex = fullChapter.content.indexOfFirst { it.type == "verse" && it.number == startVerse }
    val endIndex = fullChapter.content.indexOfLast { it.type == "verse" && it.number == endVerse }

    if (startIndex == -1 || endIndex == -1) return fullChapter // Safety fallback

    // 3. Walk backwards slightly to grab the Hebrew Stanza headings (Aleph, Beth, etc.)
    var actualStart = startIndex
    while (actualStart > 0 && fullChapter.content[actualStart - 1].type != "verse") {
        actualStart--
    }

    // 4. Create a new truncated list of content and return it
    val newContent = fullChapter.content.subList(actualStart, endIndex + 1)

    return fullChapter.copy(content = newContent)
}
