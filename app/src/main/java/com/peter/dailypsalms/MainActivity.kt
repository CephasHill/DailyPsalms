package com.peter.dailypsalms

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
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
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import com.android.billingclient.api.ProductDetails
import com.peter.dailypsalms.ui.theme.DailyPsalmsTheme
import com.peter.dailypsalms.ui.theme.DailyPsalmsWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AboutScreen(
    billingManager: BillingManager,
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
                imageVector = Icons.Default.Book,
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

// Global pre-compiled Regex to avoid recompiling it inside active composable loops
private val footnoteRegex = Regex("""\^\[(.*?)]\^""")

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

enum class BibleVersion(val code: String, val displayName: String) {
    ASV("asv", "ASV"),
    DARBY("darby", "Darby"),
    DRA("dra", "Douay-Rheims"),
    KJV("kjv", "KJV"),
    NABRE("nabre", "NABRE"),
    WEB("web", "WEB"),
    YLT("ylt", "YLT"),
    HEB("heb", "WLC (Hebrew)"),
    LXX("lxx", "LXX (Greek)"),
    VULGATE("vulgate", "Vulgate (Latin)")
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
                MainAppContainer()
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
    val last100DateKey = stringPreferencesKey("last_100_date")
    val streakKey = intPreferencesKey("streak")

    val preferredVersionCode = prefs[bibleVersionKey] ?: BibleVersion.KJV.code
    val safeVersionCode = if (isAssetExists(context, "psalms_$preferredVersionCode.json")) {
        preferredVersionCode
    } else {
        BibleVersion.KJV.code
    }

    val currentBibleVersion = BibleVersion.entries.find { it.code == safeVersionCode } ?: BibleVersion.WEB
    val repo = remember(currentBibleVersion) { BibleRepository(context, currentBibleVersion.code) }

    // --- START OF CHANGES ---

    // 1. Convert static variables to State variables
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

    // 2. Load the data on a background thread whenever the version changes
    LaunchedEffect(currentBibleVersion) {
        isLoading = true
        withContext(Dispatchers.IO) {
            // Fetch heavy JSON files on the background thread
            val loadedPsalms = repo.loadPsalms()
            val loadedProverbs = repo.loadProverbs()

            withContext(Dispatchers.Main) {
                // Pass data back to the main thread and update state
                allPsalms = loadedPsalms
                allProverbs = loadedProverbs

                // Automatically update the active reader playlist if the user was reading
                readerContext?.let { oldContext ->
                    val currentChap = oldContext.playlist.getOrNull(oldContext.initialIndex)

                    val newTodayPsalms = loadedPsalms.filter { it.chapter in psalmsTargets }
                    val newTodayProverb = loadedProverbs.find { it.chapter == today }
                    val newTodayPlaylist = newTodayPsalms + listOfNotNull(newTodayProverb)

                    val activePlaylist = if (oldContext.isDailyMode) newTodayPlaylist else {
                        if (currentChap?.book == "Proverbs") loadedProverbs else loadedPsalms
                    }
                    val newIndex = activePlaylist.indexOfFirst { it.book == currentChap?.book && it.chapter == currentChap.chapter }.takeIf { it >= 0 } ?: 0

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
    // --- END OF DATA LOADING CHANGES ---

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

    val todayPlaylistKeys = todayPlaylist.map { "${it.book}_${it.chapter}" }.toSet()
    val completedChapters = rawCompletedChapters.intersect(todayPlaylistKeys)

    val currentFootnoteStyle = try {
        FootnoteStyle.valueOf(prefs[footnoteStyleKey] ?: FootnoteStyle.INLINE.name)
    } catch (_: Exception) { FootnoteStyle.INLINE }

    val currentFontSizeOption = try {
        FontSizeOption.valueOf(prefs[fontSizeKey] ?: FontSizeOption.MEDIUM.name)
    } catch (_: Exception) { FontSizeOption.MEDIUM }

    val last100Date = prefs[last100DateKey] ?: ""
    val actualStreak = prefs[streakKey] ?: 0

    val displayStreak = when (last100Date) {
        todayStr -> actualStreak
        yesterdayStr -> actualStreak
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
                                !completedChapters.contains("${it.book}_${it.chapter}")
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
                            val last100 = p[last100DateKey]
                            val currentStreak = p[streakKey] ?: 0

                            if (last100 != todayStr) {
                                if (last100 == yesterdayStr) {
                                    p[streakKey] = currentStreak + 1
                                } else {
                                    p[streakKey] = 1
                                }
                                p[last100DateKey] = todayStr
                            }
                        } else {
                            val last100 = p[last100DateKey]
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

            // --- START OF UI LOADING CHANGES ---
            if (isLoading) {
                // Display a loading spinner in the center of the screen while parsing JSON
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
                    onFootnoteStyleChange = { updateFootnoteStyle(it) },
                    onBibleVersionChange = { updateBibleVersion(it) },
                    onFontSizeChange = { updateFontSize(it) },
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
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
            // --- END OF UI LOADING CHANGES ---

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
            val key = "${chapter.book}_${chapter.chapter}"
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
    onFootnoteStyleChange: (FootnoteStyle) -> Unit,
    onBibleVersionChange: (BibleVersion) -> Unit,
    onFontSizeChange: (FontSizeOption) -> Unit,
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
    val currentChapterKey = "${currentChapter.book}_${currentChapter.chapter}"
    val isCurrentChapterCompleted = completedChapters.contains(currentChapterKey)

    // Dynamically detect if any loaded chapter in this playlist contains footnotes
    val hasFootnotes = remember(readerContext.playlist) {
        readerContext.playlist.any { it.footnotes.isNotEmpty() }
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                            onBibleVersionChange(version)
                                            versionMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Format Selector Dropdown (Only visible if the version includes footnotes)
                        if (hasFootnotes) {
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
                    footnoteStyle = currentFootnoteStyle,
                    fontSizeOption = currentFontSize,
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
                    // 1. HEADER ROW (Title & Close Button)
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

                    // 2. DIVIDER
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // 3. SCROLLABLE BODY TEXT
                    Text(
                        text = selectedFootnote?.text ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .weight(1f) // Forces the text to fill remaining space
                            .verticalScroll(rememberScrollState()) // Enables vertical scrolling
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
    onFootnoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    linkEntirePhrase: Boolean = false
) {
    val annotatedString = buildAnnotatedString {
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
                        append(fullPhrase)
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
                            append(fullPhrase)
                        }
                        pop()
                        append(trailingSpaces)
                    }
                    FootnoteStyle.HIDDEN -> {
                        pushLink(link)
                        append(fullPhrase)
                        pop()
                        append(trailingSpaces)
                    }
                }
            } else {
                val trimmedPreceding = precedingText.trimEnd()
                val trailingSpaces = precedingText.substring(trimmedPreceding.length)

                val lastSpace = trimmedPreceding.lastIndexOf(' ')
                val targetStartIndex = if (lastSpace == -1) 0 else lastSpace + 1

                val beforeTarget = trimmedPreceding.substring(0, targetStartIndex)
                val targetWord = trimmedPreceding.substring(targetStartIndex)

                append(beforeTarget)

                when (footnoteStyle) {
                    FootnoteStyle.BRACKETED -> {
                        append(targetWord)
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
                            append(targetWord)
                        }
                        pop()
                        append(trailingSpaces)
                    }
                    FootnoteStyle.HIDDEN -> {
                        pushLink(link)
                        append(targetWord)
                        pop()
                        append(trailingSpaces)
                    }
                }
            }
            lastIndex = match.range.last + 1
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
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
    onFootnoteClick: (String) -> Unit
) {
    val scale = fontSizeOption.scale

    // LazyColumn handles rendering blocks like Psalm 119 dynamically and smoothly
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            if (chapter.chapterFootnote != null) {
                FormattedTextWithFootnotes(
                    text = "${chapter.book} ${chapter.chapter} ${chapter.chapterFootnote}",
                    footnoteStyle = footnoteStyle,
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
                                .width((28 * scale).dp) // Expanded to support 3-digit verses like Psalm 119
                        )
                        Column {
                            item.lines?.forEach { line ->
                                FormattedTextWithFootnotes(
                                    text = line.text,
                                    footnoteStyle = footnoteStyle,
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
        List(150) {
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
        // Read the frame state to force the Canvas to redraw every single tick
        val currentFrame = frame

        val w = size.width
        val h = size.height
        for (p in particles) {
            withTransform({
                translate(left = p.x * w, top = p.y * h)
                rotate(p.rotation)
            }) {
                // Use the frame count to calculate a nice fade-out effect!
                val alpha = 1f - (currentFrame / 120f)
                drawRect(color = p.color.copy(alpha = alpha.coerceIn(0f, 1f)), size = Size(24f, 24f))
            }
        }
    }
}