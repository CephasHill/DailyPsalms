package com.peter.dailypsalms

import com.google.gson.annotations.SerializedName

data class ChapterData(
    val book: String,
    val chapter: Int,
    @SerializedName("chapter_footnote") val chapterFootnote: String?,
    val content: List<ContentItem>,
    val footnotes: List<Footnote>
)

data class ContentItem(
    val type: String,
    val text: String? = null,
    val number: Int? = null,
    val lines: List<Line>? = null
)

data class Line(
    val text: String,
    val indent: Int
)

data class Footnote(
    val marker: String,
    val text: String
)