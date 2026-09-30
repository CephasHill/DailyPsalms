package com.peter.dailypsalms

/**
 * Stable identity for one scheduled assignment.
 *
 * The assigned date is part of the key because the same chapter can be
 * assigned again in a later cycle. Psalm 119 parts also need their part ID so
 * the five parts cannot collide with one another.
 */
fun assignmentKey(assignment: AssignedChapter): String {
    val book = if (assignment.book.contains("Psalm", ignoreCase = true)) {
        "Psalms"
    } else {
        "Proverbs"
    }
    val partSuffix = assignment.partId?.let { "_part$it" } ?: ""
    return "${book}_${assignment.chapter}${partSuffix}_${assignment.assignedDate}"
}
