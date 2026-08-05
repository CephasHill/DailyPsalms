package com.peter.dailypsalms

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

@Composable
fun AboutScreen(
    billingManager: BillingManager,
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
        item {
            Icon(
                painter = painterResource(id = R.drawable.ic_kinnor), // Uses painter parameter
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
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 32.dp))
        }

        item {
            TranslationsInfoSection(context)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "App Settings & Help",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TextButton(
                        onClick = onReplayTutorial,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Replay App Tour")
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

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

enum class BibleVersion(val code: String, val displayName: String, val description: String) {
    ASV("asv", "ASV", "American Standard Version (1901). A highly literal, word-for-word translation rooted in the King James tradition."),
    BSB("bsb", "BSB", "Berean Standard Bible (2016). A modern translation that balances strict accuracy to the original texts with high readability."),
    DARBY("darby", "Darby", "Darby Translation (1890). A literal translation by John Nelson Darby, known for its careful attention to original Greek and Hebrew nuances."),
    DRA("dra", "Douay-Rheims", "Douay-Rheims American Edition (1899). The traditional English Catholic Bible, translated directly from the Latin Vulgate."),
    JPS("jps", "JPS Tanakh", "Jewish Publication Society (1917). The classic English translation of the Hebrew Bible, reflecting traditional Jewish scholarship."),
    KJV("kjv", "KJV", "King James Version (1611). The most influential English translation in history, known for its majestic and poetic language."),
    NABRE("nabre", "NABRE", "New American Bible Revised Edition (2011). The modern English translation used in the Catholic liturgy in the United States."),
    WEB("web", "WEB", "World English Bible (2000). A modern, public-domain update to the ASV, prioritizing clear contemporary English."),
    YLT("ylt", "YLT", "Young's Literal Translation (1898). A strictly literal translation that strictly preserves the original Hebrew and Greek verb tenses."),
    HEB("heb", "WLC (Hebrew)", "Westminster Leningrad Codex. The oldest complete manuscript of the Hebrew Bible, serving as the definitive source text."),
    LXX("lxx", "LXX (Greek)", "The Septuagint. The ancient Greek translation of the Old Testament, widely used by the early Christian Church."),
    VULGATE("vulgate", "Vulgate (Latin)", "The Clementine Vulgate. The historic Latin translation of the Bible that served as the standard for the Western Church for over a millennium.")
}

sealed class NavigationTab {
    object Daily : NavigationTab()
    object Library : NavigationTab()
    object About : NavigationTab()
}

data class ReaderContext(
    val playlist: List<ChapterData>,
    val initialIndex: Int,
    val isDailyMode: Boolean
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
                            onFinish = {
                                coroutineScope.launch {
                                    context.dataStore.edit { p ->
                                        p[hasSeenOnboardingKey] = true
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
    val hasSeenOnboardingKey = booleanPreferencesKey("has_seen_onboarding")

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
    var isLoading by remember { mutableStateOf(true) }

    var showExplosion by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf<NavigationTab>(NavigationTab.Daily) }
    var readerContext by remember { mutableStateOf<ReaderContext?>(null) }

    val today = LocalDate.now().dayOfMonth
    val psalmsTargets = if (today == 31) {
        listOf(119)
    } else {
        listOf(today, today + 30, today + 60, today + 90, today + 120)
            .filter { it <= 150 && (today != 19 || it != 119) }
    }

    LaunchedEffect(currentBibleVersion) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val loadedPsalms = repo.loadPsalms()
            val loadedProverbs = repo.loadProverbs()

            withContext(Dispatchers.Main) {
                allPsalms = loadedPsalms
                allProverbs = loadedProverbs

                readerContext?.let { oldContext ->
                    val currentChap = oldContext.playlist.getOrNull(oldContext.initialIndex)

                    val newTodayPsalms = loadedPsalms.filter { it.chapter in psalmsTargets }
                    val newTodayProverb = loadedProverbs.find { it.chapter == today }
                    val newTodayPlaylist = newTodayPsalms + listOfNotNull(newTodayProverb)

                    val activePlaylist = if (oldContext.isDailyMode) newTodayPlaylist else {
                        if (currentChap?.normalizedBook == "Proverbs") loadedProverbs else loadedPsalms
                    }

                    val newIndex = activePlaylist.indexOfFirst {
                        it.normalizedBook == currentChap?.normalizedBook && it.chapter == currentChap.chapter
                    }.takeIf { it >= 0 } ?: 0

                    readerContext = ReaderContext(
                        playlist = activePlaylist,
                        initialIndex = newIndex,
                        isDailyMode = oldContext.isDailyMode
                    )
                }
                isLoading = false
            }
        }
    }

    val todayPsalms = allPsalms.filter { it.chapter in psalmsTargets }
    val todayProverb = allProverbs.find { it.chapter == today }
    val todayPlaylist = todayPsalms + listOfNotNull(todayProverb)

    val todayStr = LocalDate.now().toString()
    val yesterdayStr = LocalDate.now().minusDays(1).toString()

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

    val checkmarksDate = prefs[checkmarksDateKey] ?: ""
    val rawCompletedChapters = if (checkmarksDate == todayStr) {
        prefs[completedChaptersKey] ?: emptySet()
    } else {
        emptySet()
    }

    val todayPlaylistKeys = todayPlaylist.map { "${it.normalizedBook}_${it.chapter}" }.toSet()
    val completedChapters = rawCompletedChapters.intersect(todayPlaylistKeys)

    val currentFootnoteStyle = try {
        FootnoteStyle.valueOf(prefs[footnoteStyleKey] ?: FootnoteStyle.INLINE.name)
    } catch (_: Exception) { FootnoteStyle.INLINE }

    val currentFontSizeOption = try {
        FontSizeOption.valueOf(prefs[fontSizeKey] ?: FontSizeOption.MEDIUM.name)
    } catch (_: Exception) { FontSizeOption.MEDIUM }

    val currentShowGrammar = prefs[showGrammarColorsKey] ?: true

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
                                !completedChapters.contains("${it.normalizedBook}_${it.chapter}")
                            }.takeIf { it >= 0 } ?: 0

                            readerContext = ReaderContext(
                                playlist = todayPlaylist,
                                initialIndex = firstUnreadIndex,
                                isDailyMode = true
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
                        p[checkmarksDateKey] = todayStr
                        p[completedChaptersKey] = newValidChapters

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

            val updateBibleVersion = { version: BibleVersion ->
                coroutineScope.launch {
                    context.dataStore.edit { p ->
                        p[bibleVersionKey] = version.code
                        if (version == BibleVersion.LXX || version == BibleVersion.HEB) {
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
                    onFootnoteStyleChange = { updateFootnoteStyle(it) },
                    onBibleVersionChange = { version, currentPage ->
                        readerContext = readerContext?.copy(initialIndex = currentPage)
                        updateBibleVersion(version)
                    },
                    onFontSizeChange = { updateFontSize(it) },
                    onGrammarColorsChange = { updateGrammarColors(it) },
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
                                    playlist = todayPlaylist,
                                    initialIndex = index,
                                    isDailyMode = true
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
                                    isDailyMode = false
                                )
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                    is NavigationTab.About -> {
                        AboutScreen(
                            billingManager = billingManager,
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
fun TranslationsInfoSection(context: Context) {
    val availableVersions = remember {
        BibleVersion.entries.filter { version ->
            isAssetExists(context, "psalms_${version.code}.json")
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "About the Translations",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                availableVersions.forEachIndexed { index, version ->
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
                            Text(
                                text = version.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    if (index < availableVersions.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyDashboardScreen(
    playlist: List<ChapterData>,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Today's Devotional",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Streak: 🔥 $streakCount Days | Progress: $doneCount / $totalCount Chapters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) doneCount.toFloat() / totalCount else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                    )
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
            val chapter = playlist[index]
            val key = "${chapter.normalizedBook}_${chapter.chapter}"
            val isDone = completedChapters.contains(key)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onChapterClick(index) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${chapter.book} ${chapter.chapter}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
    onFootnoteStyleChange: (FootnoteStyle) -> Unit,
    onBibleVersionChange: (BibleVersion, Int) -> Unit,
    onFontSizeChange: (FontSizeOption) -> Unit,
    onGrammarColorsChange: (Boolean) -> Unit,
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
    val currentChapterKey = "${currentChapter.normalizedBook}_${currentChapter.chapter}"
    val isCurrentChapterCompleted = completedChapters.contains(currentChapterKey)

    val hasFootnotes = remember(readerContext.playlist) {
        readerContext.playlist.any { it.footnotes.isNotEmpty() }
    }

    // Force hidden footnotes for original language texts so the screen isn't flooded with Strong's numbers
    val effectiveFootnoteStyle = if (currentBibleVersion == BibleVersion.HEB || currentBibleVersion == BibleVersion.LXX) {
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

                        // Size Selector Dropdown
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

                        // Version Selector Dropdown
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
                                availableVersions.forEach { version ->
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
                            }
                        }

                        // Format Selector Dropdown
                        val showFootnoteOptions = hasFootnotes && currentBibleVersion != BibleVersion.HEB && currentBibleVersion != BibleVersion.LXX
                        if (showFootnoteOptions || currentBibleVersion == BibleVersion.HEB || currentBibleVersion == BibleVersion.LXX) {
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
                                }
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
                    onFootnoteClick = { marker ->
                        selectedFootnote = chapter.footnotes.find { it.marker == marker }
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

        // Unified parser that handles both HTML (Greek) and Bracket tags (Hebrew)
        fun appendParsedText(str: String) {
            if (str.contains("<span") || str.contains("<b>") || str.contains("<u>")) {
                // --- HTML PARSER (For LXX) ---
                val htmlRegex = Regex("""</?(span[^>]*|b|u)>""")
                var currentIndex = 0
                val styles = mutableListOf<SpanStyle>()

                for (match in htmlRegex.findAll(str)) {
                    // Append text before the tag
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

                    // Handle the tag itself
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

                // Append remaining text
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
                // --- BRACKET PARSER (For Hebrew) ---
                val grammarRegex = Regex("""\[([a-z_]+)](.*?)\[/\1]""")
                var localLastIndex = 0
                for (match in grammarRegex.findAll(str)) {
                    append(str.substring(localLastIndex, match.range.first))
                    val tag = match.groupValues[1]
                    val content = match.groupValues[2]

                    if (showGrammarColors) {
                        val color = when (tag) {
                            "n" -> Color(0xFF1976D2) // Blue
                            "v", "v_imp", "ptc" -> Color(0xFFD32F2F) // Red
                            "prep" -> Color(0xFF388E3C) // Green
                            "conj" -> Color(0xFFF57C00) // Orange
                            "a" -> Color(0xFF7B1FA2) // Purple
                            "adv" -> Color(0xFF0097A7) // Cyan
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
                val trailingSpaces = precedingText.substring(fullPhrase.length)

                when (footnoteStyle) {
                    FootnoteStyle.BRACKETED -> {
                        appendParsedText(fullPhrase)
                        append(trailingSpaces)
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
                        append(trailingSpaces)
                    }
                    FootnoteStyle.HIDDEN -> {
                        pushLink(link)
                        appendParsedText(fullPhrase)
                        pop()
                        append(trailingSpaces)
                    }
                }
            } else {
                val trimmedPreceding = precedingText.trimEnd()
                val trailingSpaces = precedingText.substring(trimmedPreceding.length)

                // SMART SPACE FINDER: Ignores spaces inside HTML or Bracket tags
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
                        append(trailingSpaces)
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
                        append(trailingSpaces)
                    }
                    FootnoteStyle.HIDDEN -> {
                        pushLink(link)
                        appendParsedText(targetWord)
                        pop()
                        append(trailingSpaces)
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
    onFootnoteClick: (String) -> Unit
) {
    val scale = fontSizeOption.scale

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                "book_division" -> {
                    Text(
                        text = item.text ?: "",
                        fontStyle = FontStyle.Italic,
                        fontSize = (14 * scale).sp,
                        lineHeight = (18 * scale).sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                "heading" -> {
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
                        Text(
                            text = if (showNumber) currentVerseStr else "",
                            fontSize = (10 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(end = 8.dp, top = (4 * scale).dp)
                                .width((28 * scale).dp)
                        )
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
        while (frame < 120) {
            withFrameNanos {
                for (p in particles) {
                    p.x += p.vx
                    p.y += p.vy
                    p.vy += 0.004f
                    p.vx *= 0.98f
                    p.rotation += p.rotationSpeed
                }
                frame++
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
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 24.dp), // Padding for gesture navigation bars
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Page Indicators (Dots)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(4) { index ->
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

                    // Navigation Button
                    if (pagerState.currentPage == 3) {
                        Button(onClick = onFinish) {
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
                        // Use painterResource for your custom local graphic
                        iconPainter = painterResource(id = R.drawable.ic_kinnor),
                        title = "Welcome to Daily Psalms",
                        description = "Your distraction-free companion for reading through the books of Psalms and Proverbs every single month."
                    )
                    1 -> OnboardingPage(
                        // Use rememberVectorPainter to convert the standard Material icons
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
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(
    iconPainter: androidx.compose.ui.graphics.painter.Painter, // Changed to Painter
    title: String,
    description: String
) {
    Icon(
        painter = iconPainter, // Changed from imageVector to painter
        contentDescription = null,
        modifier = Modifier
            .size(100.dp)
            .padding(bottom = 32.dp),
        tint = MaterialTheme.colorScheme.primary // Your kinnor will be automatically tinted to match the theme here!
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