package com.peter.dailypsalms

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.random.Random

@Composable
fun TranslationsInfoSection(context: Context) {
    val isDark = isSystemInDarkTheme()
    val availableVersions = remember {
        BibleVersion.entries.filter { version ->
            isAssetExists(context, "psalms_${version.code}.json")
        }
    }

    // Group the versions by their category
    val groupedVersions = availableVersions.groupBy { it.category }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "About the Translations",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Iterate through each category and its associated list of versions
        groupedVersions.forEach { (category, versions) ->
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    versions.forEachIndexed { index, version ->
                        var isExpanded by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded }
                                .padding(16.dp)
                        ) {
                            Text(
                                text = version.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            AnimatedVisibility(visible = isExpanded) {
                                Column {
                                    Text(
                                        text = version.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )

                                    // 1. Custom Footnote Legend for NET Bible
                                    if (version.code == "net") {
                                        val netLegend = buildAnnotatedString {
                                            append("Footnote Guide:\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFF64B5F6) else Color(0xFF1976D2))) { append("• tn (Translator's Note):") }
                                            append(" Explains translation decisions, grammar, and literal phrasing.\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFF81C784) else Color(0xFF388E3C))) { append("• sn (Study Note):") }
                                            append(" Provides historical, theological, or cultural context.\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFFE57373) else Color(0xFFD32F2F))) { append("• tc (Text-Critical Note):") }
                                            append(" Discusses ancient manuscript variations.")
                                        }
                                        Text(
                                            text = netLegend,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(top = 12.dp, start = 8.dp)
                                        )
                                    }

                                    // 2. Custom Grammar Legend for LXX (Greek)
                                    if (version.code == "lxx") {
                                        val lxxLegend = buildAnnotatedString {
                                            append("Grammar Color Guide:\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFFE57373) else Color(0xFFD32F2F))) { append("• Verbs") }
                                            append(" are Red\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFF64B5F6) else Color(0xFF1976D2))) { append("• Nouns") }
                                            append(" are Blue\n")
                                            append("• Participles are ")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFFE57373) else Color(0xFFD32F2F))) { append("Red") }
                                            append(" with ")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFF64B5F6) else Color(0xFF1976D2))) { append("Blue") }
                                            append(" endings\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFF81C784) else Color(0xFF388E3C))) { append("• Prepositions & Conjunctions") }
                                            append(" are Green\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFF64B5F6) else Color(0xFF1976D2), textDecoration = TextDecoration.Underline)) { append("• Adjectives") }
                                            append(" are underlined in Blue\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFFE57373) else Color(0xFFD32F2F), textDecoration = TextDecoration.Underline)) { append("• Adverbs") }
                                            append(" are underlined in Red\n")
                                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("• Bold text") }
                                            append(" indicates emphasis (e.g., imperative commands, personal/reflexive pronouns, and strong negations like οὐ μή).")
                                        }
                                        Text(
                                            text = lxxLegend,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(top = 12.dp, start = 8.dp)
                                        )
                                    }

                                    // 3. Custom Grammar Legend for WLC (Hebrew)
                                    if (version.code == "heb") {
                                        val hebLegend = buildAnnotatedString {
                                            append("Grammar Color Guide:\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFFE57373) else Color(0xFFD32F2F))) { append("• Verbs & Participles") }
                                            append(" are Red\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFF64B5F6) else Color(0xFF1976D2))) { append("• Nouns") }
                                            append(" are Blue\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFFBA68C8) else Color(0xFF7B1FA2))) { append("• Adjectives") }
                                            append(" are Purple\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFF4DD0E1) else Color(0xFF0097A7))) { append("• Adverbs") }
                                            append(" are Cyan\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFF81C784) else Color(0xFF388E3C))) { append("• Prepositions") }
                                            append(" are Green\n")
                                            withStyle(SpanStyle(color = if (isDark) Color(0xFFFFB74D) else Color(0xFFF57C00))) { append("• Conjunctions") }
                                            append(" are Orange")
                                        }
                                        Text(
                                            text = hebLegend,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(top = 12.dp, start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (index < versions.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterRenderer(
    chapter: ChapterData,
    footnoteStyle: FootnoteStyle,
    fontSizeOption: FontSizeOption,
    showGrammarColors: Boolean,
    showFootnoteColors: Boolean,
    showHeadings: Boolean,
    onFootnoteClick: (String) -> Unit,
    isDailyMode: Boolean = false,
    isCompleted: Boolean = false,
    onToggleComplete: () -> Unit = {}
) {
    val scale = fontSizeOption.scale
    val listState = rememberLazyListState()

    SelectionContainer {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .simpleVerticalScrollbar(
                    state = listState,
                    baseColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                if (chapter.chapterFootnote != null) {
                    FormattedTextWithFootnotes(
                        text = "${chapter.book} ${chapter.chapter} ${chapter.chapterFootnote}",
                        footnotes = chapter.footnotes,
                        footnoteStyle = footnoteStyle,
                        showGrammarColors = showGrammarColors,
                        showFootnoteColors = showFootnoteColors,
                        onFootnoteClick = onFootnoteClick,
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (32 * scale).sp,
                            lineHeight = (38 * scale).sp
                        ),
                        linkEntirePhrase = true,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    Text(
                        text = "${chapter.book} ${chapter.chapter}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = (32 * scale).sp,
                            lineHeight = (38 * scale).sp
                        ),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            var lastPrintedVerseStr = ""

            items(chapter.content) { item ->
                when (item.type) {
                    "chapter_summary" -> {
                        Text(
                            text = item.text ?: "",
                            fontStyle = FontStyle.Italic,
                            fontSize = (15 * scale).sp,
                            lineHeight = (22 * scale).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        )
                    }
                    "book_division" -> {
                        if (showHeadings) {
                            Text(
                                text = item.text ?: "",
                                fontStyle = FontStyle.Italic,
                                fontSize = (14 * scale).sp,
                                lineHeight = (18 * scale).sp,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                    }
                    "heading" -> {
                        if (showHeadings) {
                            FormattedTextWithFootnotes(
                                text = item.text ?: "",
                                footnotes = chapter.footnotes,
                                footnoteStyle = footnoteStyle,
                                showGrammarColors = showGrammarColors,
                                showFootnoteColors = showFootnoteColors,
                                onFootnoteClick = onFootnoteClick,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (18 * scale).sp,
                                    lineHeight = (24 * scale).sp
                                ),
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                    }
                    "stanza" -> {
                        Text(
                            text = item.text ?: "",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (14 * scale).sp,
                            lineHeight = (18 * scale).sp,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    "verse" -> {
                        val currentVerseStr = item.number.toString()
                        val showNumber = currentVerseStr != lastPrintedVerseStr
                        if (showNumber) {
                            lastPrintedVerseStr = currentVerseStr
                        }

                        Row(modifier = Modifier.padding(bottom = 4.dp)) {
                            DisableSelection {
                                Text(
                                    text = if (showNumber) currentVerseStr else "",
                                    fontSize = (10 * scale).sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(end = 8.dp, top = (4 * scale).dp)
                                        .width((28 * scale).dp)
                                )
                            }
                            Column {
                                item.lines?.forEach { line ->
                                    FormattedTextWithFootnotes(
                                        text = line.text,
                                        footnotes = chapter.footnotes,
                                        footnoteStyle = footnoteStyle,
                                        showGrammarColors = showGrammarColors,
                                        showFootnoteColors = showFootnoteColors,
                                        onFootnoteClick = onFootnoteClick,
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = (16 * scale).sp,
                                            lineHeight = (22 * scale).sp
                                        ),
                                        modifier = Modifier.padding(
                                            start = if (line.indent == 1) 16.dp else 0.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                if (isDailyMode) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 16.dp)
                            .clickable { onToggleComplete() },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isCompleted,
                                onCheckedChange = { onToggleComplete() }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCompleted) "Completed" else "Mark as Done",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

// Global pre-compiled Regex objects
private val footnoteRegex = Regex("""\^\[(.*?)]\^""")

@Composable
fun FormattedTextWithFootnotes(
    text: String,
    footnotes: List<Footnote>,
    footnoteStyle: FootnoteStyle,
    showGrammarColors: Boolean,
    showFootnoteColors: Boolean,
    onFootnoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    linkEntirePhrase: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val annotatedString = buildAnnotatedString {

        // 1. Helper to dynamically style LORD and GOD
        val divineNameRegex = Regex("""\b(LORD|GOD)\b""")
        fun appendWithSmallCaps(str: String) {
            var last = 0
            for (match in divineNameRegex.findAll(str)) {
                // Append everything before the match
                append(str.substring(last, match.range.first))

                // Append the first letter normally (L or G), shrink the rest (ORD or OD)
                val word = match.value
                append(word.substring(0, 1))
                withStyle(SpanStyle(fontSize = 0.8.em)) {
                    append(word.substring(1))
                }
                last = match.range.last + 1
            }
            if (last < str.length) {
                append(str.substring(last))
            }
        }

        // 2. Unified parser that handles both HTML (Greek) and Bracket tags (Hebrew)
        fun appendParsedText(str: String) {
            if (str.contains("<span") || str.contains("<b>") || str.contains("<u>")) {
                val htmlRegex = Regex("""</?(span[^>]*|b|u)>""")
                var currentIndex = 0
                val styles = mutableListOf<SpanStyle>()

                for (match in htmlRegex.findAll(str)) {
                    if (match.range.first > currentIndex) {
                        val chunk = str.substring(currentIndex, match.range.first)
                        if (showGrammarColors && styles.isNotEmpty()) {
                            var currentStyle = SpanStyle()
                            styles.forEach { currentStyle = currentStyle.merge(it) }
                            withStyle(currentStyle) { appendWithSmallCaps(chunk) }
                        } else {
                            appendWithSmallCaps(chunk)
                        }
                    }

                    val tag = match.value
                    if (tag.startsWith("</")) {
                        if (styles.isNotEmpty()) styles.removeAt(styles.lastIndex)
                    } else {
                        when {
                            tag.startsWith("<span") -> {
                                val colorStr = Regex("""color:([a-z]+)""").find(tag)?.groupValues?.get(1)
                                val color = when (colorStr) {
                                    "red" -> if (isDark) Color(0xFFE57373) else Color(0xFFD32F2F)
                                    "blue" -> if (isDark) Color(0xFF64B5F6) else Color(0xFF1976D2)
                                    "green" -> if (isDark) Color(0xFF81C784) else Color(0xFF388E3C)
                                    else -> Color.Unspecified
                                }
                                styles.add(SpanStyle(color = color))
                            }
                            tag == "<b>" -> styles.add(SpanStyle(fontWeight = FontWeight.Bold))
                            tag == "<u>" -> styles.add(SpanStyle(textDecoration = TextDecoration.Underline))
                        }
                    }
                    currentIndex = match.range.last + 1
                }

                if (currentIndex < str.length) {
                    val chunk = str.substring(currentIndex)
                    if (showGrammarColors && styles.isNotEmpty()) {
                        var currentStyle = SpanStyle()
                        styles.forEach { currentStyle = currentStyle.merge(it) }
                        withStyle(currentStyle) { appendWithSmallCaps(chunk) }
                    } else {
                        appendWithSmallCaps(chunk)
                    }
                }
            } else {
                val grammarRegex = Regex("""\[([a-z_]+)](.*?)\[/\1]""")
                var localLastIndex = 0
                for (match in grammarRegex.findAll(str)) {
                    appendWithSmallCaps(str.substring(localLastIndex, match.range.first))
                    val tag = match.groupValues[1]
                    val content = match.groupValues[2]

                    if (showGrammarColors) {
                        val color = when (tag) {
                            "n" -> if (isDark) Color(0xFF64B5F6) else Color(0xFF1976D2)
                            "v", "v_imp", "ptc" -> if (isDark) Color(0xFFE57373) else Color(0xFFD32F2F)
                            "prep" -> if (isDark) Color(0xFF81C784) else Color(0xFF388E3C)
                            "conj" -> if (isDark) Color(0xFFFFB74D) else Color(0xFFF57C00)
                            "a" -> if (isDark) Color(0xFFBA68C8) else Color(0xFF7B1FA2)
                            "adv" -> if (isDark) Color(0xFF4DD0E1) else Color(0xFF0097A7)
                            else -> Color.Unspecified
                        }
                        withStyle(SpanStyle(color = color)) {
                            appendWithSmallCaps(content)
                        }
                    } else {
                        appendWithSmallCaps(content)
                    }
                    localLastIndex = match.range.last + 1
                }
                if (localLastIndex < str.length) {
                    appendWithSmallCaps(str.substring(localLastIndex))
                }
            }
        }

        var lastIndex = 0

        for (match in footnoteRegex.findAll(text)) {
            val marker = match.groupValues[1]

            val noteType = footnotes.find { it.marker == marker }?.type ?: ""

            val linkColor = when {
                footnoteStyle == FootnoteStyle.HIDDEN -> Color.Unspecified
                !showFootnoteColors -> MaterialTheme.colorScheme.primary
                noteType == "tn" -> if (isDark) Color(0xFF64B5F6) else Color(0xFF1976D2)
                noteType == "sn" -> if (isDark) Color(0xFF81C784) else Color(0xFF388E3C)
                noteType == "tc" -> if (isDark) Color(0xFFE57373) else Color(0xFFD32F2F)
                else -> MaterialTheme.colorScheme.primary
            }

            val precedingText = text.substring(lastIndex, match.range.first)

            val linkStyles = TextLinkStyles(style = SpanStyle(color = linkColor, textDecoration = TextDecoration.None))
            val link = LinkAnnotation.Clickable(marker, styles = linkStyles) { _ -> onFootnoteClick(marker) }

            if (linkEntirePhrase) {
                val fullPhrase = precedingText.trimEnd()

                when (footnoteStyle) {
                    FootnoteStyle.BRACKETED -> {
                        appendParsedText(fullPhrase)
                        pushLink(link)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 0.7.em, baselineShift = BaselineShift.Superscript)) {
                            append("[$marker]")
                        }
                        pop()
                    }
                    FootnoteStyle.INLINE -> {
                        pushLink(link)
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontSize = 0.7.em, baselineShift = BaselineShift.Superscript)) {
                            append(marker)
                        }
                        appendParsedText(fullPhrase)
                        pop()
                    }
                    FootnoteStyle.HIDDEN -> {
                        pushLink(link)
                        appendParsedText(fullPhrase)
                        pop()
                    }
                }
            } else {
                val trimmedPreceding = precedingText.trimEnd()

                var lastSpace = -1
                var insideHtml = false
                var insideBracket = false
                for (i in trimmedPreceding.indices) {
                    val c = trimmedPreceding[i]
                    when (c) {
                        '<' -> insideHtml = true
                        '>' -> insideHtml = false
                        '[' -> insideBracket = true
                        ']' -> insideBracket = false
                        ' ' if !insideHtml && !insideBracket -> {
                            lastSpace = i
                        }
                    }
                }

                val targetStartIndex = if (lastSpace == -1) 0 else lastSpace + 1
                val beforeTarget = trimmedPreceding.substring(0, targetStartIndex)
                val targetWord = trimmedPreceding.substring(targetStartIndex)

                appendParsedText(beforeTarget)

                when (footnoteStyle) {
                    FootnoteStyle.BRACKETED -> {
                        appendParsedText(targetWord)
                        pushLink(link)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 0.7.em, baselineShift = BaselineShift.Superscript)) {
                            append("[$marker]")
                        }
                        pop()
                    }
                    FootnoteStyle.INLINE -> {
                        pushLink(link)
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontSize = 0.7.em, baselineShift = BaselineShift.Superscript)) {
                            append(marker)
                        }
                        appendParsedText(targetWord)
                        pop()
                    }
                    FootnoteStyle.HIDDEN -> {
                        pushLink(link)
                        appendParsedText(targetWord)
                        pop()
                    }
                }
            }
            lastIndex = match.range.last + 1
        }

        if (lastIndex < text.length) {
            appendParsedText(text.substring(lastIndex))
        }
    }

    Text(
        text = annotatedString,
        style = textStyle.copy(color = MaterialTheme.colorScheme.onBackground),
        modifier = modifier
    )
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun ParticleExplosion(modifier: Modifier = Modifier, onFinished: () -> Unit) {
    val particles = remember {
        List(250) {
            Particle(
                x = 0.5f,
                y = 0.4f,
                vx = (Random.nextFloat() - 0.5f) * 0.08f,
                vy = -(Random.nextFloat() - 0.5f) * 0.08f - 0.02f,
                color = listOf(Color(0xFFE2B714), Color(0xFFF1515E), Color(0xFF4885ED), Color(0xFF39C27C)).random(),
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 30f
            )
        }
    }

    var frame by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        var lastTime = withFrameMillis { it }
        var elapsedTime = 0L
        val animationDurationMs = 3000L

        while (elapsedTime < animationDurationMs) {
            withFrameMillis { currentTime ->
                val deltaMs = currentTime - lastTime
                lastTime = currentTime
                elapsedTime += deltaMs

                val timeStep = (deltaMs / 16.6f) * 0.4f

                for (p in particles) {
                    p.x += p.vx * timeStep
                    p.y += p.vy * timeStep
                    p.vy += 0.004f * timeStep
                    p.vx *= (1f - (0.02f * timeStep))
                    p.rotation += p.rotationSpeed * timeStep
                }

                frame = ((elapsedTime.toFloat() / animationDurationMs) * 120).toInt()
            }
        }
        onFinished()
    }

    Canvas(modifier = modifier) {
        val currentFrame = frame

        val w = size.width
        val h = size.height
        for (p in particles) {
            withTransform({
                translate(left = p.x * w, top = p.y * h)
                rotate(p.rotation)
            }) {
                val alpha = 1f - (currentFrame / 120f)
                drawRect(color = p.color.copy(alpha = alpha.coerceIn(0f, 1f)), size = Size(24f, 24f))
            }
        }
    }
}

fun Modifier.simpleVerticalScrollbar(
    state: androidx.compose.foundation.lazy.LazyListState,
    width: androidx.compose.ui.unit.Dp = 4.dp,
    baseColor: Color = Color.Gray
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 0.5f else 0f
    val duration = if (state.isScrollInProgress) 150 else 1200

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration),
        label = "scrollbar_alpha"
    )

    drawWithContent {
        drawContent()

        if (alpha > 0f) {
            val totalItemsCount = state.layoutInfo.totalItemsCount
            val visibleItemsInfo = state.layoutInfo.visibleItemsInfo

            // Only draw if there are items, and they don't all fit on the screen[cite: 8]
            if (totalItemsCount > 0 && visibleItemsInfo.isNotEmpty() && totalItemsCount > visibleItemsInfo.size) {
                val firstItem = visibleItemsInfo.first()
                val lastItem = visibleItemsInfo.last()

                val firstSize = firstItem.size.coerceAtLeast(1)
                val lastSize = lastItem.size.coerceAtLeast(1)

                // 1. Calculate the exact fractional index of the top of the screen
                val viewportTopIndex = firstItem.index + (kotlin.math.abs(firstItem.offset).toFloat() / firstSize)

                // 2. Calculate the exact fractional index of the bottom of the screen
                val viewportEnd = state.layoutInfo.viewportEndOffset
                val lastItemBottom = lastItem.offset + lastItem.size

                val bottomFraction = if (lastItemBottom > viewportEnd) {
                    (viewportEnd - lastItem.offset).toFloat() / lastSize
                } else {
                    1f
                }
                val viewportBottomIndex = lastItem.index + bottomFraction

                // 3. How many "items" currently fit on screen (fractional)
                val visibleItemsCount = viewportBottomIndex - viewportTopIndex
                val totalItems = totalItemsCount.toFloat()

                // 4. Stable Thumb Height
                val scrollbarHeight = (size.height * (visibleItemsCount / totalItems))
                    .coerceIn(40.dp.toPx(), size.height * 0.5f)

                // 5. Calculate smooth progress from 0.0 to 1.0
                val maxTopIndex = (totalItems - visibleItemsCount).coerceAtLeast(0f)
                val scrollPercentage = if (maxTopIndex > 0f) {
                    (viewportTopIndex / maxTopIndex).coerceIn(0f, 1f)
                } else {
                    0f
                }

                val scrollbarY = scrollPercentage * (size.height - scrollbarHeight)

                drawRoundRect(
                    color = baseColor.copy(alpha = alpha),
                    topLeft = Offset(size.width - width.toPx(), scrollbarY),
                    size = Size(width.toPx(), scrollbarHeight),
                    cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2)
                )
            }
        }
    }
}