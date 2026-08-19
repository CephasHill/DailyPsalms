package com.peter.dailypsalms

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.zIndex
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import com.peter.dailypsalms.ui.theme.DailyPsalmsTheme
import com.peter.dailypsalms.ui.theme.DailyPsalmsWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

@Composable
fun AboutScreen(
    billingManager: BillingManager,
    currentTrack: ReadingTrack,
    currentGraceDay: GraceDayOption,
    onTrackChange: (ReadingTrack) -> Unit,
    onGraceDayChange: (GraceDayOption) -> Unit,
    onReplayTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Observe the products loaded from Google Play
    val products by billingManager.products.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. HEADER SECTION
        item {
            Icon(
                painter = painterResource(id = R.drawable.ic_kinnor),
                contentDescription = "Logo",
                modifier = Modifier.size(72.dp).padding(top = 24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Daily Psalms",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 32.dp))
        }

        // 2. SETTINGS & PREFERENCES SECTION
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Settings & Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Track Selector
                    var trackMenuExpanded by remember { mutableStateOf(false) }
                    Text(text = "Schedule Track", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Box {
                        TextButton(onClick = { trackMenuExpanded = true }, contentPadding = PaddingValues(0.dp)) {
                            Text("${currentTrack.displayName} ▼", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        DropdownMenu(expanded = trackMenuExpanded, onDismissRequest = { trackMenuExpanded = false }) {
                            ReadingTrack.entries.forEach { track ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(track.displayName, fontWeight = FontWeight.Bold)
                                            Text(track.description, style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = { onTrackChange(track); trackMenuExpanded = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grace Day Selector
                    var graceMenuExpanded by remember { mutableStateOf(false) }
                    Text(text = "Catch-Up / Grace Day", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Box {
                        TextButton(onClick = { graceMenuExpanded = true }, contentPadding = PaddingValues(0.dp)) {
                            Text("${currentGraceDay.displayName} ▼", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        DropdownMenu(expanded = graceMenuExpanded, onDismissRequest = { graceMenuExpanded = false }) {
                            GraceDayOption.entries.forEach { day ->
                                DropdownMenuItem(
                                    text = { Text(day.displayName) },
                                    onClick = { onGraceDayChange(day); graceMenuExpanded = false }
                                )
                            }
                        }
                    }

                    // A subtle divider to separate reading settings from app settings
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )

                    // Replay Tutorial Button
                    TextButton(
                        onClick = onReplayTutorial,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Replay App Tour")
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 32.dp))
        }

        // 3. TRANSLATIONS SECTION
        item {
            TranslationsInfoSection(context)
            HorizontalDivider(modifier = Modifier.padding(vertical = 32.dp))
            Spacer(modifier = Modifier.height(32.dp))
        }

        // 4. TIP JAR SECTION
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Support",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(32.dp)
                    )

                    Text(
                        text = "Support the Developer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                    )

                    Text(
                        text = "Daily Psalms is completely free and ad-free. If this app has been a blessing to you, consider leaving a tip to support future updates!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Render buttons dynamically based on what Google Play returns
                    if (products.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        Text("Loading tip jar...", fontSize = 12.sp)
                    } else {
                        products.forEach { product ->
                            val price = product.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                            Button(
                                onClick = {
                                    if (activity != null) {
                                        billingManager.launchBillingFlow(activity, product)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text("Tip $price")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TranslationsInfoSection(context: Context) {
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

                                    // Custom footnote legend exclusively for the NET Bible
                                    if (version.code == "net") {
                                        Text(
                                            text = "Footnote Guide:\n" +
                                                    "• tn (Translator's Note): Explains translation decisions, grammar, and literal phrasing.\n" +
                                                    "• sn (Study Note): Provides historical, theological, or cultural context.\n" +
                                                    "• tc (Text-Critical Note): Discusses ancient manuscript variations.",
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

val Context.dataStore by preferencesDataStore(name = "settings")

// Global pre-compiled Regex objects
private val footnoteRegex = Regex("""\^\[(.*?)]\^""")

// Safely normalize book names across different JSON sources (e.g. "Psalm" vs "Psalms")
val ChapterData.normalizedBook: String
    get() = if (book.contains("Psalm", ignoreCase = true)) "Psalms" else "Proverbs"

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
    ASV("asv", "ASV", "American Standard Version (1901). A highly literal, word-for-word translation rooted in the King James tradition.", BibleCategory.HISTORICAL),
    BSB("bsb", "BSB", "Berean Standard Bible (2016). A modern translation that balances strict accuracy to the original texts with high readability.", BibleCategory.MODERN),
    DRA("dra", "Douay-Rheims", "Douay-Rheims American Edition (1899). The traditional English Catholic Bible, translated directly from the Latin Vulgate.", BibleCategory.HISTORICAL),
    GNV("gnv", "Geneva", "Geneva Bible (1599). The Bible of the Protestant Reformation, famous for its extensive historical and theological study notes.", BibleCategory.HISTORICAL),
    KJV("kjv", "KJV", "King James Version (1611). The most influential English translation in history, known for its majestic and poetic language.", BibleCategory.HISTORICAL),
    NABRE("nabre", "NABRE", "New American Bible Revised Edition (2011). The modern English translation used in the Catholic liturgy in the United States.", BibleCategory.MODERN),
    NET("net", "NET", "New English Translation (2005). A modern, highly readable translation renowned for its transparency and extensive translator's notes.", BibleCategory.MODERN),
    WEB("web", "WEB", "World English Bible (2000). A modern, public-domain update to the ASV, prioritizing clear contemporary English.", BibleCategory.MODERN),
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

sealed class NavigationTab {
    object Daily : NavigationTab()
    object Library : NavigationTab()
    object About : NavigationTab()
}

// Updated to track Daily keys specifically for checking off catches
data class ReaderContext(
    val playlist: List<ChapterData>,
    val initialIndex: Int,
    val isDailyMode: Boolean,
    val dailyKeys: List<String>? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DailyPsalmsTheme {
                val context = LocalContext.current
                val prefs by context.dataStore.data.collectAsState(initial = null)
                val coroutineScope = rememberCoroutineScope()

                // Wait for DataStore to load to prevent flashing the wrong screen
                if (prefs != null) {
                    val hasSeenOnboardingKey = booleanPreferencesKey("has_seen_onboarding")
                    val hasSeen = prefs!![hasSeenOnboardingKey] ?: false

                    if (hasSeen) {
                        MainAppContainer()
                    } else {
                        OnboardingScreen(
                            onFinish = { selectedTrack, selectedGraceDay ->
                                coroutineScope.launch {
                                    context.dataStore.edit { p ->
                                        p[hasSeenOnboardingKey] = true

                                        val readingTrackKey = stringPreferencesKey("reading_track")
                                        val graceDayKey = stringPreferencesKey("grace_day")
                                        p[readingTrackKey] = selectedTrack.name
                                        p[graceDayKey] = selectedGraceDay.name
                                        p[stringPreferencesKey("plan_start_date")] = LocalDate.now().toString()
                                    }
                                }
                            }
                        )
                    }
                } else {
                    // Show a simple loading state while checking preferences
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

fun isAssetExists(context: Context, fileName: String): Boolean {
    return try {
        context.assets.list("")?.contains(fileName) == true
    } catch (_: Exception) {
        false
    }
}

@Composable
fun MainAppContainer() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val billingManager = remember { BillingManager(context) }
    val prefs by context.dataStore.data.collectAsState(initial = emptyPreferences())

    // DataStore Keys
    val checkmarksDateKey = stringPreferencesKey("checkmarks_date")
    val completedChaptersKey = stringSetPreferencesKey("completed_chapters")
    val footnoteStyleKey = stringPreferencesKey("footnote_style")
    val bibleVersionKey = stringPreferencesKey("bible_version")
    val fontSizeKey = stringPreferencesKey("font_size")
    val showGrammarColorsKey = booleanPreferencesKey("show_grammar_colors")
    val last100DateKey = stringPreferencesKey("last_100_date") // Standardized
    val legacyLast100DateKey = stringPreferencesKey("last_100Date_key") // Fallback
    val streakKey = intPreferencesKey("streak")
    val readingTrackKey = stringPreferencesKey("reading_track")
    val graceDayKey = stringPreferencesKey("grace_day")
    val hasSeenOnboardingKey = booleanPreferencesKey("has_seen_onboarding")
    val planStartDateKey = stringPreferencesKey("plan_start_date")
    val showHeadingsKey = booleanPreferencesKey("show_headings")

    // Read the current states (with defaults)
    val currentTrack = try {
        ReadingTrack.valueOf(prefs[readingTrackKey] ?: ReadingTrack.CLASSIC.name)
    } catch (_: Exception) { ReadingTrack.CLASSIC }

    val currentGraceDay = try {
        GraceDayOption.valueOf(prefs[graceDayKey] ?: GraceDayOption.NONE.name)
    } catch (_: Exception) { GraceDayOption.NONE }

    // DEFAULT BIBLE VERSION
    val preferredVersionCode = prefs[bibleVersionKey] ?: BibleVersion.BSB.code
    val safeVersionCode = if (isAssetExists(context, "psalms_$preferredVersionCode.json")) {
        preferredVersionCode
    } else {
        BibleVersion.BSB.code
    }

    val currentBibleVersion = BibleVersion.entries.find { it.code == safeVersionCode } ?: BibleVersion.WEB
    val repo = remember(currentBibleVersion) { BibleRepository(context, currentBibleVersion.code) }

    var allPsalms by remember { mutableStateOf<List<ChapterData>>(emptyList()) }
    var allProverbs by remember { mutableStateOf<List<ChapterData>>(emptyList()) }
    var activeLexicon by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    var showExplosion by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf<NavigationTab>(NavigationTab.Daily) }
    var readerContext by remember { mutableStateOf<ReaderContext?>(null) }

    val todayDate = remember { LocalDate.now() }
    val todayStr = todayDate.toString()
    val yesterdayStr = todayDate.minusDays(1).toString()

    val cycleStartDate = remember(todayDate, currentGraceDay) {
        getCycleStartDate(todayDate, currentGraceDay)
    }
    val cycleStartStr = cycleStartDate.toString()

    val planStartStr = prefs[planStartDateKey]
    val planStartDate = remember(planStartStr, todayDate) {
        try {
            planStartStr?.let { LocalDate.parse(it) } ?: todayDate
        } catch (_: Exception) { todayDate }
    }

    // Save the anchor date immediately for existing testers upgrading
    LaunchedEffect(planStartStr) {
        if (planStartStr == null) {
            context.dataStore.edit { p -> p[planStartDateKey] = todayStr }
        }
    }

    // Generate all dates, but NEVER go further back than the planStartDate!
    val cycleDates = remember(cycleStartDate, todayDate, planStartDate) {
        val effectiveStart = if (cycleStartDate.isBefore(planStartDate)) planStartDate else cycleStartDate

        generateSequence(effectiveStart) { d ->
            if (d.isBefore(todayDate)) d.plusDays(1) else null
        }.toList()
    }

    val checkmarksDate = prefs[checkmarksDateKey] ?: ""
    val rawCompletedChapters = if (checkmarksDate == cycleStartStr) {
        prefs[completedChaptersKey] ?: emptySet()
    } else {
        emptySet()
    }

    var todayPlaylist by remember { mutableStateOf<List<DailyReading>>(emptyList()) }

    LaunchedEffect(currentBibleVersion, currentTrack, currentGraceDay, todayStr) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val loadedPsalms = repo.loadPsalms()
            val loadedProverbs = repo.loadProverbs()

            val activeLexiconFileName = when (currentBibleVersion) {
                BibleVersion.VULGATE -> "latin_lexicon.json"
                BibleVersion.LXX -> "greek_lexicon.json"
                BibleVersion.HEB -> "hebrew_lexicon.json"
                else -> null
            }

            val loadedLexicon = if (activeLexiconFileName != null && isAssetExists(context, activeLexiconFileName)) {
                try {
                    val jsonString = context.assets.open(activeLexiconFileName).bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(jsonString)
                    val map = mutableMapOf<String, String>()
                    jsonObject.keys().forEach { key ->
                        map[key] = jsonObject.getString(key)
                    }
                    map
                } catch (_: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }

            // 1. Get all assignments for the whole cycle up to today, filtering out COMPLETED past days
            val fullCycleAssignments = cycleDates.flatMap { date ->
                getAssignedChapters(date, currentTrack).filter { assignment ->
                    if (date.isBefore(todayDate)) {
                        // Reconstruct the key to check if it's already done
                        val book = if (assignment.book.contains("Psalm", true)) "Psalms" else "Proverbs"
                        val partSuffix = if (assignment.partId != null) "_part${assignment.partId}" else ""
                        val key = "${book}_${assignment.chapter}${partSuffix}_${assignment.assignedDate}"

                        // Keep it ONLY if it hasn't been checked off yet!
                        !rawCompletedChapters.contains(key)
                    } else {
                        // Always show today's chapters, whether done or not
                        true
                    }
                }
            }

            // 2. Map them to actual Bible data and determine if they are catch-up chapters
            val generatedPlaylist = fullCycleAssignments.mapNotNull { assignment ->
                val sourceList = if (assignment.book == "Psalms") loadedPsalms else loadedProverbs
                sourceList.find { it.chapter == assignment.chapter }?.let { chapData ->

                    // Intercept and slice the chapter if a partId is present!
                    val finalChapData = if (assignment.partId != null) {
                        slicePsalm119(chapData, assignment.partId)
                    } else {
                        chapData
                    }

                    DailyReading(
                        chapterData = finalChapData,
                        assignedDate = assignment.assignedDate,
                        isCatchUp = assignment.assignedDate.isBefore(todayDate),
                        partId = assignment.partId
                    )
                }
            }

            withContext(Dispatchers.Main) {
                allPsalms = loadedPsalms
                allProverbs = loadedProverbs
                todayPlaylist = generatedPlaylist
                activeLexicon = loadedLexicon

                // Keep reader context stable if it was already open
                readerContext?.let { oldContext ->
                    val newPlaylist = if (oldContext.isDailyMode) {
                        generatedPlaylist.map { it.chapterData }
                    } else {
                        // Check if the Library was looking at Psalms or Proverbs, and grab the NEW version's list!
                        val isPsalms = oldContext.playlist.firstOrNull()?.normalizedBook == "Psalms"
                        if (isPsalms) loadedPsalms else loadedProverbs
                    }

                    readerContext = oldContext.copy(
                        playlist = newPlaylist,
                        dailyKeys = if (oldContext.isDailyMode) generatedPlaylist.map { it.uniqueKey } else null
                    )
                }

                val activeDoneCount = rawCompletedChapters.intersect(generatedPlaylist.map { it.uniqueKey }.toSet()).size
                context.dataStore.edit { p ->
                    p[intPreferencesKey("widget_done_count")] = activeDoneCount
                    p[intPreferencesKey("widget_total_count")] = generatedPlaylist.size
                }
                DailyPsalmsWidget().updateAll(context)

                isLoading = false
            }
        }
    }

    val todayPlaylistKeys = todayPlaylist.map { it.uniqueKey }.toSet()
    val completedChapters = rawCompletedChapters.intersect(todayPlaylistKeys)

    LaunchedEffect(Unit) {
        while (true) {
            delay(1.minutes)
            val liveTodayStr = LocalDate.now().toString()
            if (liveTodayStr != todayStr) {
                DailyPsalmsWidget().updateAll(context)
            }
        }
    }

    LaunchedEffect(Unit) {
        DailyPsalmsWidget().updateAll(context)
    }

    val currentFootnoteStyle = try {
        FootnoteStyle.valueOf(prefs[footnoteStyleKey] ?: FootnoteStyle.INLINE.name)
    } catch (_: Exception) { FootnoteStyle.INLINE }

    val currentFontSizeOption = try {
        FontSizeOption.valueOf(prefs[fontSizeKey] ?: FontSizeOption.MEDIUM.name)
    } catch (_: Exception) { FontSizeOption.MEDIUM }

    val currentShowGrammar = prefs[showGrammarColorsKey] ?: true
    val currentShowHeadings = prefs[showHeadingsKey] ?: true

    // Recover date from either key structure to ensure synchronization with the widget
    val last100Date = prefs[last100DateKey] ?: prefs[legacyLast100DateKey] ?: ""
    val actualStreak = prefs[streakKey] ?: 0

    val displayStreak = when (last100Date) {
        todayStr -> actualStreak
        yesterdayStr -> actualStreak
        "" -> actualStreak // Recover widget-only streaks where the date key was missing or mismatched
        else -> 0
    }

    BackHandler(enabled = readerContext != null) {
        readerContext = null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (readerContext == null) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab is NavigationTab.Daily,
                        onClick = { selectedTab = NavigationTab.Daily },
                        icon = { Icon(Icons.Default.Today, contentDescription = "Daily") },
                        label = { Text("Daily") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            val firstUnreadIndex = todayPlaylist.indexOfFirst {
                                !completedChapters.contains(it.uniqueKey)
                            }.takeIf { it >= 0 } ?: 0

                            readerContext = ReaderContext(
                                playlist = todayPlaylist.map { it.chapterData },
                                initialIndex = firstUnreadIndex,
                                isDailyMode = true,
                                dailyKeys = todayPlaylist.map { it.uniqueKey }
                            )
                        },
                        icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Read") },
                        label = { Text("Read Now") }
                    )
                    NavigationBarItem(
                        selected = selectedTab is NavigationTab.Library,
                        onClick = { selectedTab = NavigationTab.Library },
                        icon = { Icon(Icons.Default.Book, contentDescription = "Library") },
                        label = { Text("Library") }
                    )
                    NavigationBarItem(
                        selected = selectedTab is NavigationTab.About,
                        onClick = { selectedTab = NavigationTab.About },
                        icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                        label = { Text("About") }
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            val toggleChapterCompletion = { key: String ->
                val currentValidChapters = rawCompletedChapters.intersect(todayPlaylistKeys)
                val was100 = currentValidChapters.size == todayPlaylist.size
                val newValidChapters = if (currentValidChapters.contains(key)) currentValidChapters - key else currentValidChapters + key
                val isNow100 = newValidChapters.size == todayPlaylist.size

                if (isNow100 && !was100) {
                    showExplosion = true
                }

                coroutineScope.launch {
                    context.dataStore.edit { p ->
                        p[checkmarksDateKey] = cycleStartStr
                        p[completedChaptersKey] = newValidChapters

                        p[intPreferencesKey("widget_done_count")] = newValidChapters.size
                        p[intPreferencesKey("widget_total_count")] = todayPlaylist.size

                        if (isNow100) {
                            val last100 = p[last100DateKey] ?: p[legacyLast100DateKey] ?: ""
                            val currentStreak = p[streakKey] ?: 0

                            if (last100 != todayStr) {
                                if (last100 == yesterdayStr || (last100 == "" && currentStreak > 0)) {
                                    p[streakKey] = currentStreak + 1
                                } else {
                                    p[streakKey] = 1
                                }
                                p[last100DateKey] = todayStr
                            }
                        } else {
                            val last100 = p[last100DateKey] ?: p[legacyLast100DateKey] ?: ""
                            if (last100 == todayStr) {
                                val currentStreak = p[streakKey] ?: 1
                                p[streakKey] = maxOf(0, currentStreak - 1)
                                p[last100DateKey] = yesterdayStr
                            }
                        }
                    }
                    DailyPsalmsWidget().updateAll(context)
                }
            }

            val updateFootnoteStyle = { style: FootnoteStyle ->
                coroutineScope.launch { context.dataStore.edit { p -> p[footnoteStyleKey] = style.name } }
            }

            val updateFontSize = { size: FontSizeOption ->
                coroutineScope.launch { context.dataStore.edit { p -> p[fontSizeKey] = size.name } }
            }

            val updateGrammarColors = { show: Boolean ->
                coroutineScope.launch { context.dataStore.edit { p -> p[showGrammarColorsKey] = show } }
            }

            val updateShowHeadings = { show: Boolean ->
                coroutineScope.launch { context.dataStore.edit { p -> p[showHeadingsKey] = show } }
            }

            val updateBibleVersion = { version: BibleVersion ->
                coroutineScope.launch {
                    context.dataStore.edit { p ->
                        p[bibleVersionKey] = version.code
                        if (version == BibleVersion.LXX || version == BibleVersion.HEB || version == BibleVersion.VULGATE) {
                            p[footnoteStyleKey] = FootnoteStyle.HIDDEN.name
                        } else if (p[footnoteStyleKey] == FootnoteStyle.HIDDEN.name) {
                            p[footnoteStyleKey] = FootnoteStyle.INLINE.name
                        }
                    }
                }
            }

            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading ${currentBibleVersion.displayName}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (readerContext != null) {
                ActiveChapterReaderScreen(
                    readerContext = readerContext!!,
                    completedChapters = completedChapters,
                    currentFootnoteStyle = currentFootnoteStyle,
                    currentBibleVersion = currentBibleVersion,
                    currentFontSize = currentFontSizeOption,
                    showGrammarColors = currentShowGrammar,
                    showHeadings = currentShowHeadings,
                    activeLexicon = activeLexicon,
                    onFootnoteStyleChange = { updateFootnoteStyle(it) },
                    onBibleVersionChange = { version, currentPage ->
                        readerContext = readerContext?.copy(initialIndex = currentPage)
                        updateBibleVersion(version)
                    },
                    onFontSizeChange = { updateFontSize(it) },
                    onGrammarColorsChange = { updateGrammarColors(it) },
                    onShowHeadingsChange = { updateShowHeadings(it) },
                    onToggleComplete = { key -> toggleChapterCompletion(key) },
                    onBack = { readerContext = null },
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                when (selectedTab) {
                    is NavigationTab.Daily -> {
                        DailyDashboardScreen(
                            playlist = todayPlaylist,
                            completedChapters = completedChapters,
                            streakCount = displayStreak,
                            onToggleComplete = { key -> toggleChapterCompletion(key) },
                            onChapterClick = { index ->
                                readerContext = ReaderContext(
                                    playlist = todayPlaylist.map { it.chapterData },
                                    initialIndex = index,
                                    isDailyMode = true,
                                    dailyKeys = todayPlaylist.map { it.uniqueKey }
                                )
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                    is NavigationTab.Library -> {
                        LibraryScreen(
                            allPsalms = allPsalms,
                            allProverbs = allProverbs,
                            onChapterClick = { playlist, index ->
                                readerContext = ReaderContext(
                                    playlist = playlist,
                                    initialIndex = index,
                                    isDailyMode = false,
                                    dailyKeys = null
                                )
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                    is NavigationTab.About -> {
                        AboutScreen(
                            billingManager = billingManager,
                            currentTrack = currentTrack,
                            currentGraceDay = currentGraceDay,
                            onTrackChange = { newTrack ->
                                coroutineScope.launch {
                                    context.dataStore.edit { p ->
                                        p[readingTrackKey] = newTrack.name
                                        p[planStartDateKey] = todayStr
                                    }
                                }
                            },
                            onGraceDayChange = { newDay ->
                                coroutineScope.launch {
                                    context.dataStore.edit { p ->
                                        p[graceDayKey] = newDay.name
                                        p[planStartDateKey] = todayStr
                                    }
                                }
                            },
                            onReplayTutorial = {
                                coroutineScope.launch {
                                    context.dataStore.edit { p ->
                                        p[hasSeenOnboardingKey] = false
                                    }
                                }
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }

            if (showExplosion) {
                ParticleExplosion(
                    modifier = Modifier.fillMaxSize().zIndex(10f),
                    onFinished = { showExplosion = false }
                )
            }
        }
    }
}

@Composable
fun DailyDashboardScreen(
    playlist: List<DailyReading>,
    completedChapters: Set<String>,
    streakCount: Int,
    onToggleComplete: (String) -> Unit,
    onChapterClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = playlist.size
    val doneCount = completedChapters.size

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = if (totalCount > 0) doneCount.toFloat() / totalCount.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "$doneCount of $totalCount Chapters", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(text = "$streakCount Day Streak 🔥", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        item {
            val formattedDate = remember {
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d"))
            }

            Text(
                text = "Assigned Readings for $formattedDate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(playlist.size) { index ->
            val reading = playlist[index]
            val chapter = reading.chapterData
            val key = reading.uniqueKey
            val isDone = completedChapters.contains(key)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onChapterClick(index) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        val titleText = if (reading.partId != null) {
                            "${chapter.book} ${chapter.chapter} (Part ${reading.partId})"
                        } else {
                            "${chapter.book} ${chapter.chapter}"
                        }

                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (reading.isCatchUp && !isDone) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Catch-Up",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Checkbox(
                        checked = isDone,
                        onCheckedChange = { onToggleComplete(key) }
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(
    allPsalms: List<ChapterData>,
    allProverbs: List<ChapterData>,
    onChapterClick: (List<ChapterData>, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMainBook by remember { mutableStateOf("Psalms") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedMainBook) {
        listState.scrollToItem(0)
    }

    val currentPsalmBook by remember {
        derivedStateOf {
            val index = listState.firstVisibleItemIndex
            when {
                index < 41 -> 1
                index < 72 -> 2
                index < 89 -> 3
                index < 106 -> 4
                else -> 5
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedMainBook == "Psalms",
                onClick = { selectedMainBook = "Psalms" },
                label = { Text("Psalms") }
            )
            FilterChip(
                selected = selectedMainBook == "Proverbs",
                onClick = { selectedMainBook = "Proverbs" },
                label = { Text("Proverbs") }
            )
        }

        if (selectedMainBook == "Psalms") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val psalmBooks = listOf(
                    1 to Pair("Book I (1-41)", 0),
                    2 to Pair("Book II (42-72)", 41),
                    3 to Pair("Book III (73-89)", 72),
                    4 to Pair("Book IV (90-106)", 89),
                    5 to Pair("Book V (107-150)", 106)
                )

                psalmBooks.forEach { (bookNum, data) ->
                    val (label, targetIndex) = data
                    FilterChip(
                        selected = currentPsalmBook == bookNum,
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(targetIndex)
                            }
                        },
                        label = { Text(label) }
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        val activePlaylist = if (selectedMainBook == "Psalms") allPsalms else allProverbs

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(activePlaylist.size) { index ->
                val chapter = activePlaylist[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onChapterClick(activePlaylist, index)
                        }
                ) {
                    Text(
                        text = "${chapter.book} ${chapter.chapter}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveChapterReaderScreen(
    readerContext: ReaderContext,
    completedChapters: Set<String>,
    currentFootnoteStyle: FootnoteStyle,
    currentBibleVersion: BibleVersion,
    currentFontSize: FontSizeOption,
    showGrammarColors: Boolean,
    showHeadings: Boolean,
    activeLexicon: Map<String, String>,
    onFootnoteStyleChange: (FootnoteStyle) -> Unit,
    onBibleVersionChange: (BibleVersion, Int) -> Unit,
    onFontSizeChange: (FontSizeOption) -> Unit,
    onGrammarColorsChange: (Boolean) -> Unit,
    onShowHeadingsChange: (Boolean) -> Unit,
    onToggleComplete: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFootnote by remember { mutableStateOf<Footnote?>(null) }
    var formatMenuExpanded by remember { mutableStateOf(false) }
    var versionMenuExpanded by remember { mutableStateOf(false) }
    var sizeMenuExpanded by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(
        initialPage = readerContext.initialIndex,
        pageCount = { readerContext.playlist.size }
    )

    val currentChapter = readerContext.playlist[pagerState.currentPage]

    val currentChapterKey = readerContext.dailyKeys?.get(pagerState.currentPage) ?: "${currentChapter.normalizedBook}_${currentChapter.chapter}"
    val isCurrentChapterCompleted = completedChapters.contains(currentChapterKey)

    val hasFootnotes = remember(readerContext.playlist) {
        readerContext.playlist.any { it.footnotes.isNotEmpty() }
    }

    // Force hidden footnotes for original language texts so the screen isn't flooded with Strong's numbers
    val effectiveFootnoteStyle = if (currentBibleVersion == BibleVersion.HEB || currentBibleVersion == BibleVersion.LXX || currentBibleVersion == BibleVersion.VULGATE) {
        FootnoteStyle.HIDDEN
    } else {
        currentFootnoteStyle
    }

    BackHandler {
        if (selectedFootnote != null) {
            selectedFootnote = null
        } else {
            onBack()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            Surface(tonalElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onBack,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("← Back")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (readerContext.isDailyMode) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { onToggleComplete(currentChapterKey) }
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text("Done", style = MaterialTheme.typography.labelLarge)
                                Checkbox(
                                    checked = isCurrentChapterCompleted,
                                    onCheckedChange = { onToggleComplete(currentChapterKey) },
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }

                        Box {
                            TextButton(
                                onClick = { sizeMenuExpanded = true },
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text("Size ▼", fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = sizeMenuExpanded,
                                onDismissRequest = { sizeMenuExpanded = false }
                            ) {
                                FontSizeOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option.displayName,
                                                fontWeight = if (option == currentFontSize) FontWeight.Bold else FontWeight.Normal,
                                                color = if (option == currentFontSize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            onFontSizeChange(option)
                                            sizeMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Box {
                            TextButton(
                                onClick = { versionMenuExpanded = true },
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text("${currentBibleVersion.displayName} ▼", fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = versionMenuExpanded,
                                onDismissRequest = { versionMenuExpanded = false }
                            ) {
                                val availableVersions = remember {
                                    BibleVersion.entries.filter { version ->
                                        isAssetExists(context, "psalms_${version.code}.json")
                                    }
                                }

                                val groupedVersions = availableVersions.groupBy { it.category }
                                val categoryList = groupedVersions.keys.toList()

                                groupedVersions.forEach { (category, versions) ->
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )

                                    versions.forEach { version ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = version.displayName,
                                                    fontWeight = if (version == currentBibleVersion) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (version == currentBibleVersion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            onClick = {
                                                onBibleVersionChange(version, pagerState.currentPage)
                                                versionMenuExpanded = false
                                            }
                                        )
                                    }

                                    if (category != categoryList.last()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }

                        val showFootnoteOptions = hasFootnotes && currentBibleVersion != BibleVersion.HEB && currentBibleVersion != BibleVersion.LXX && currentBibleVersion != BibleVersion.VULGATE

                        Box {
                            TextButton(
                                onClick = { formatMenuExpanded = true },
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text("Format ▼", fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = formatMenuExpanded,
                                onDismissRequest = { formatMenuExpanded = false }
                            ) {
                                if (showFootnoteOptions) {
                                    Text(
                                        text = "Footnote Style",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                    FootnoteStyle.entries.forEach { style ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = style.displayName,
                                                    fontWeight = if (style == currentFootnoteStyle) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (style == currentFootnoteStyle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            onClick = {
                                                onFootnoteStyleChange(style)
                                                formatMenuExpanded = false
                                            }
                                        )
                                    }
                                }

                                if (currentBibleVersion == BibleVersion.HEB || currentBibleVersion == BibleVersion.LXX) {
                                    if (showFootnoteOptions) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                    Text(
                                        text = "Language Tools",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Grammar Colors")
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Switch(
                                                    checked = showGrammarColors,
                                                    onCheckedChange = null,
                                                    modifier = Modifier.scale(0.8f)
                                                )
                                            }
                                        },
                                        onClick = {
                                            onGrammarColorsChange(!showGrammarColors)
                                            formatMenuExpanded = false
                                        }
                                    )
                                }

                                if (showFootnoteOptions || currentBibleVersion == BibleVersion.HEB || currentBibleVersion == BibleVersion.LXX) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }

                                Text(
                                    text = "Layout",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Section Headings")
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Switch(
                                                checked = showHeadings,
                                                onCheckedChange = null,
                                                modifier = Modifier.scale(0.8f)
                                            )
                                        }
                                    },
                                    onClick = {
                                        onShowHeadingsChange(!showHeadings)
                                        formatMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val chapter = readerContext.playlist[page]

                ChapterRenderer(
                    chapter = chapter,
                    footnoteStyle = effectiveFootnoteStyle,
                    fontSizeOption = currentFontSize,
                    showGrammarColors = showGrammarColors,
                    showHeadings = showHeadings,
                    onFootnoteClick = { marker ->
                        val existingFootnote = chapter.footnotes.find { it.marker == marker }
                        if (existingFootnote != null) {
                            selectedFootnote = existingFootnote
                        } else if (activeLexicon.isNotEmpty()) {
                            val definition = activeLexicon[marker] ?: "Definition not found in lexicon."

                            // For Latin, the raw dictionary string is just the definition (e.g. "to be, exist")
                            // so we prepend the tapped marker. For Greek/Hebrew, the definition already
                            // contains the word itself (e.g. "μακάριος: blessed; prosperous").
                            val formattedText = when (currentBibleVersion) {
                                BibleVersion.VULGATE -> "${marker.uppercase()}: $definition"
                                else -> definition
                            }

                            selectedFootnote = Footnote(marker = marker, text = formattedText)
                        }
                    }
                )
            }
        }

        if (selectedFootnote != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                    .clickable { selectedFootnote = null }
            )
        }

        AnimatedVisibility(
            visible = selectedFootnote != null,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.8f),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Footnote [${selectedFootnote?.marker ?: ""}]",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { selectedFootnote = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = selectedFootnote?.text ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

@Composable
fun FormattedTextWithFootnotes(
    text: String,
    footnoteStyle: FootnoteStyle,
    showGrammarColors: Boolean,
    onFootnoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    linkEntirePhrase: Boolean = false
) {
    val annotatedString = buildAnnotatedString {
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
                            withStyle(currentStyle) { append(chunk) }
                        } else {
                            append(chunk)
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
                                    "red" -> Color(0xFFD32F2F)
                                    "blue" -> Color(0xFF1976D2)
                                    "green" -> Color(0xFF388E3C)
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
                        withStyle(currentStyle) { append(chunk) }
                    } else {
                        append(chunk)
                    }
                }
            } else {
                val grammarRegex = Regex("""\[([a-z_]+)](.*?)\[/\1]""")
                var localLastIndex = 0
                for (match in grammarRegex.findAll(str)) {
                    append(str.substring(localLastIndex, match.range.first))
                    val tag = match.groupValues[1]
                    val content = match.groupValues[2]

                    if (showGrammarColors) {
                        val color = when (tag) {
                            "n" -> Color(0xFF1976D2)
                            "v", "v_imp", "ptc" -> Color(0xFFD32F2F)
                            "prep" -> Color(0xFF388E3C)
                            "conj" -> Color(0xFFF57C00)
                            "a" -> Color(0xFF7B1FA2)
                            "adv" -> Color(0xFF0097A7)
                            else -> Color.Unspecified
                        }
                        withStyle(SpanStyle(color = color)) {
                            append(content)
                        }
                    } else {
                        append(content)
                    }
                    localLastIndex = match.range.last + 1
                }
                if (localLastIndex < str.length) {
                    append(str.substring(localLastIndex))
                }
            }
        }

        var lastIndex = 0

        for (match in footnoteRegex.findAll(text)) {
            val marker = match.groupValues[1]
            val precedingText = text.substring(lastIndex, match.range.first)

            val linkStyles = TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.None))
            val link = LinkAnnotation.Clickable(marker, styles = linkStyles) { _ -> onFootnoteClick(marker) }

            if (linkEntirePhrase) {
                val fullPhrase = precedingText.trimEnd()

                when (footnoteStyle) {
                    FootnoteStyle.BRACKETED -> {
                        appendParsedText(fullPhrase)
                        pushLink(link)
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 0.7.em, baselineShift = BaselineShift.Superscript)) {
                            append("[$marker]")
                        }
                        pop()
                    }
                    FootnoteStyle.INLINE -> {
                        pushLink(link)
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontSize = 0.7.em, baselineShift = BaselineShift.Superscript)) {
                                append(marker)
                            }
                            appendParsedText(fullPhrase)
                        }
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
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 0.7.em, baselineShift = BaselineShift.Superscript)) {
                            append("[$marker]")
                        }
                        pop()
                    }
                    FootnoteStyle.INLINE -> {
                        pushLink(link)
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontSize = 0.7.em, baselineShift = BaselineShift.Superscript)) {
                                append(marker)
                            }
                            appendParsedText(targetWord)
                        }
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

@Composable
fun ChapterRenderer(
    chapter: ChapterData,
    footnoteStyle: FootnoteStyle,
    fontSizeOption: FontSizeOption,
    showGrammarColors: Boolean,
    showHeadings: Boolean,
    onFootnoteClick: (String) -> Unit
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
                        footnoteStyle = footnoteStyle,
                        showGrammarColors = showGrammarColors,
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
                                footnoteStyle = footnoteStyle,
                                showGrammarColors = showGrammarColors,
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
                                        footnoteStyle = footnoteStyle,
                                        showGrammarColors = showGrammarColors,
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
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
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

@Composable
fun OnboardingScreen(onFinish: (ReadingTrack, GraceDayOption) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()

    var selectedTrack by remember { mutableStateOf(ReadingTrack.CLASSIC) }
    var selectedGraceDay by remember { mutableStateOf(GraceDayOption.SUNDAY) }

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(5) { index ->
                            val color = if (pagerState.currentPage == index) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color, shape = androidx.compose.foundation.shape.CircleShape)
                            )
                        }
                    }

                    if (pagerState.currentPage == 4) {
                        Button(onClick = { onFinish(selectedTrack, selectedGraceDay) }) {
                            Text("Get Started")
                        }
                    } else {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (page) {
                    0 -> OnboardingPage(
                        iconPainter = painterResource(id = R.drawable.ic_kinnor),
                        title = "Welcome to Daily Psalms",
                        description = "Your distraction-free companion for reading through the books of Psalms and Proverbs every single month."
                    )
                    1 -> OnboardingPage(
                        iconPainter = androidx.compose.ui.graphics.vector.rememberVectorPainter(image = Icons.Default.Today),
                        title = "The Reading Plan",
                        description = "Each day, you are assigned 1 Proverb and 5 Psalms spaced exactly 30 chapters apart.\n\nFor example, on the 5th of the month, you will read Proverbs 5 alongside Psalms 5, 35, 65, 95, and 125. (On the 31st, we pair Proverbs 31 with the 176-verse-long Psalm 119!)"
                    )
                    2 -> OnboardingPage(
                        iconPainter = androidx.compose.ui.graphics.vector.rememberVectorPainter(image = Icons.Default.Info),
                        title = "Deep Study Tools",
                        description = "Tap the 'Version' menu to explore a vast library of translations, from the modern BSB to the ancient Greek Septuagint.\n\nFor supported texts, a 'Format' menu will automatically appear, giving you access to footnotes, grammar color-coding, and integrated lexicons."
                    )
                    3 -> OnboardingPage(
                        iconPainter = androidx.compose.ui.graphics.vector.rememberVectorPainter(image = Icons.Default.Favorite),
                        title = "Stay Consistent",
                        description = "Build a lasting scripture habit! Track your progress with the built-in streak system, and add our Home Screen Widget to check your daily reading status at a glance."
                    )
                    4 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp).padding(bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Customize Your Plan",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            Text("Select Your Reading Track:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            ReadingTrack.entries.forEach { track ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { selectedTrack = track }.padding(vertical = 8.dp)
                                ) {
                                    androidx.compose.material3.RadioButton(
                                        selected = selectedTrack == track,
                                        onClick = { selectedTrack = track }
                                    )
                                    Column {
                                        Text(track.displayName, fontWeight = FontWeight.Bold)
                                        Text(track.description, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(
    iconPainter: androidx.compose.ui.graphics.painter.Painter,
    title: String,
    description: String
) {
    Icon(
        painter = iconPainter,
        contentDescription = null,
        modifier = Modifier
            .size(100.dp)
            .padding(bottom = 32.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    Text(
        text = description,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 24.sp
    )
}

fun Modifier.simpleVerticalScrollbar(
    state: androidx.compose.foundation.lazy.LazyListState,
    width: androidx.compose.ui.unit.Dp = 4.dp,
    baseColor: Color = Color.Gray
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 0.5f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration),
        label = "scrollbar_alpha"
    )

    drawWithContent {
        drawContent()

        if (alpha > 0f) {
            val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index

            if (firstVisibleElementIndex != null && state.layoutInfo.totalItemsCount > state.layoutInfo.visibleItemsInfo.size) {
                val totalItemsCount = state.layoutInfo.totalItemsCount
                val visibleItemsInfo = state.layoutInfo.visibleItemsInfo

                val firstItem = visibleItemsInfo.first()
                val firstItemOffset = firstItem.offset
                val firstItemSize = firstItem.size

                val fractionalFirstIndex = if (firstItemSize > 0) {
                    firstVisibleElementIndex + (abs(firstItemOffset).toFloat() / firstItemSize.toFloat())
                } else {
                    firstVisibleElementIndex.toFloat()
                }

                val scrollPercentage = fractionalFirstIndex / totalItemsCount.toFloat()
                val scrollbarHeight = size.height * (visibleItemsInfo.size.toFloat() / totalItemsCount.toFloat())
                val scrollbarY = (scrollPercentage * size.height).coerceIn(0f, size.height - scrollbarHeight)

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