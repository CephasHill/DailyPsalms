package com.peter.dailypsalms

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class ChapterData(
    val book: String,
    val chapter: Int,
    @SerializedName("chapter_footnote") val chapterFootnote: String?,
    val content: List<ContentItem>,
    val footnotes: List<Footnote>
)

val ChapterData.normalizedBook: String
    get() = if (book.contains("Psalm", ignoreCase = true)) "Psalms" else "Proverbs"


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
    val type: String? = null,
    val text: String
)

enum class FontSizeOption(val displayName: String, val scale: Float) {
    SMALL("Small", 0.85f),
    MEDIUM("Medium", 1.0f),
    LARGE("Large", 1.2f)
}

enum class FootnoteStyle(val displayName: String) {
    BRACKETED("Bracketed [a]"),
    INLINE("Inline ᵃWord"),
    HIDDEN("Hidden")
}

enum class BibleCategory(val displayName: String) {
    MODERN("Modern"),
    HISTORICAL("Historical"),
    ANCIENT("Original Languages")
}

enum class BibleVersion(val code: String, val displayName: String, val description: String, val category: BibleCategory) {
    BSB("bsb", "BSB", "Berean Standard Bible (2016). A modern translation that balances strict accuracy to the original texts with high readability.", BibleCategory.MODERN),
    DRA("dra", "Douay-Rheims", "Douay-Rheims American Edition (1899). The traditional English Catholic Bible, translated directly from the Latin Vulgate.", BibleCategory.HISTORICAL),
    GNV("gnv", "Geneva", "Geneva Bible (1599). The Bible of the Protestant Reformation, famous for its extensive historical and theological study notes.", BibleCategory.HISTORICAL),
    KJV("kjv", "KJV", "King James Version (1611). The most influential English translation in history, known for its majestic and poetic language.", BibleCategory.HISTORICAL),
    NABRE("nabre", "NABRE", "New American Bible Revised Edition (2011). The modern English translation used in the Catholic liturgy in the United States.", BibleCategory.MODERN),
    NET("net", "NET", "New English Translation (2005). A modern, highly readable translation renowned for its transparency and extensive translator's notes.", BibleCategory.MODERN),
    NLT("nlt", "NLT", "New Living Translation (1996). A highly readable, dynamic translation focused on conveying the original meaning in natural, everyday English.", BibleCategory.MODERN),
    WEB("web", "WEB", "World English Bible (2000). A modern, public-domain update to the ASV, prioritizing clear contemporary English. Translates the name of God as Yahweh or Yah.", BibleCategory.MODERN),
    HEB("heb", "WLC (Hebrew)", "Westminster Leningrad Codex. The oldest complete manuscript of the Hebrew Bible, serving as the definitive source text.", BibleCategory.ANCIENT),
    LXX("lxx", "LXX (Greek)", "The Septuagint. The ancient Greek translation of the Old Testament, widely used by the early Christian Church.", BibleCategory.ANCIENT),
    VULGATE("vulgate", "Vulgate (Latin)", "The Clementine Vulgate. The historic Latin translation of the Bible that served as the standard for the Western Church for over a millennium.", BibleCategory.ANCIENT)
}

enum class ReadingTrack(val displayName: String, val description: String) {
    CLASSIC("Classic (5+1)", "5 Psalms & 1 Proverb daily (1 Month)"),
    PACED("Paced (1+1)", "1 Psalm & 1 Proverb daily (5 Months)"),
    PSALMS_ONLY("Psalms Only", "1 Psalm daily (150 Days)"),
    PROVERBS_ONLY("Proverbs Only", "1 Proverb daily (31 Days)")
}

enum class GraceDayOption(val displayName: String) {
    NONE("No Grace Day"),
    SUNDAY("Sunday"),
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday")
}

data class AssignedChapter(
    val book: String,
    val chapter: Int,
    val assignedDate: LocalDate,
    val partId: Int? = null
)

data class DailyReading(
    val chapterData: ChapterData,
    val assignedDate: LocalDate,
    val isCatchUp: Boolean,
    val partId: Int? = null
) {
    val uniqueKey: String get() = "${chapterData.normalizedBook}_${chapterData.chapter}${if (partId != null) "_part$partId" else ""}_$assignedDate"
}

sealed class NavigationTab {
    object Daily : NavigationTab()
    object Library : NavigationTab()
    object About : NavigationTab()
}

data class ReaderContext(
    val playlist: List<ChapterData>,
    val initialIndex: Int,
    val isDailyMode: Boolean,
    val dailyKeys: List<String>? = null
)

