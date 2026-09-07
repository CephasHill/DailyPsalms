package com.peter.dailypsalms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import kotlin.time.Duration.Companion.minutes

val Context.dataStore by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {

    // 1. Add state to track the widget action
    private var widgetAction by mutableStateOf(false)

    // 2. Catch the widget click if the app is already open in the background
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == "ACTION_OPEN_READ_NOW") {
            widgetAction = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 3. Catch the widget click if the app starts fresh
        if (intent.action == "ACTION_OPEN_READ_NOW") {
            widgetAction = true
        }

        scheduleMidnightWidgetUpdate(this)

        setContent {
            DailyPsalmsTheme {
                val context = LocalContext.current
                val prefs by context.dataStore.data.collectAsState(initial = null)
                val coroutineScope = rememberCoroutineScope()

                if (prefs != null) {
                    val hasSeenOnboardingKey = booleanPreferencesKey("has_seen_onboarding")
                    val hasSeen = prefs!![hasSeenOnboardingKey] ?: false

                    if (hasSeen) {
                        // 4. Pass the widget action and a reset callback down to the container
                        MainAppContainer(
                            openReadNow = widgetAction,
                            onReadNowConsumed = { widgetAction = false }
                        )
                    } else {
                        OnboardingScreen(
                            onFinish = { selectedTrack, selectedGraceDay, selectedVersion ->
                                coroutineScope.launch {
                                    context.dataStore.edit { p ->
                                        p[hasSeenOnboardingKey] = true

                                        val readingTrackKey = stringPreferencesKey("reading_track")
                                        val graceDayKey = stringPreferencesKey("grace_day")
                                        val bibleVersionKey = stringPreferencesKey("bible_version")

                                        p[readingTrackKey] = selectedTrack.name
                                        p[graceDayKey] = selectedGraceDay.name
                                        p[bibleVersionKey] = selectedVersion.code
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

@Composable
fun MainAppContainer(
    openReadNow: Boolean = false,
    onReadNowConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val prefs by context.dataStore.data.collectAsState(initial = emptyPreferences())

    // DataStore Keys
    val checkmarksDateKey = stringPreferencesKey("checkmarks_date")
    val completedChaptersKey = stringSetPreferencesKey("completed_chapters")
    val footnoteStyleKey = stringPreferencesKey("footnote_style")
    val bibleVersionKey = stringPreferencesKey("bible_version")
    val fontSizeKey = stringPreferencesKey("font_size")
    val showGrammarColorsKey = booleanPreferencesKey("show_grammar_colors")
    val showFootnoteColorsKey = booleanPreferencesKey("show_footnote_colors")
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

    LaunchedEffect(openReadNow, isLoading) {
        // Wait until data is loaded, then trigger the same logic as your bottom bar button
        if (openReadNow && !isLoading && todayPlaylist.isNotEmpty()) {
            val firstUnreadIndex = todayPlaylist.indexOfFirst {
                !completedChapters.contains(it.uniqueKey)
            }.takeIf { it >= 0 } ?: 0

            readerContext = ReaderContext(
                playlist = todayPlaylist.map { it.chapterData },
                initialIndex = firstUnreadIndex,
                isDailyMode = true,
                dailyKeys = todayPlaylist.map { it.uniqueKey }
            )
            onReadNowConsumed() // Reset the flag so it doesn't keep refiring
        }
    }

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
    val currentShowFootnoteColors = prefs[showFootnoteColorsKey] ?: true
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

            val updateFootnoteColors = { show: Boolean ->
                coroutineScope.launch { context.dataStore.edit { p -> p[showFootnoteColorsKey] = show } }
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
                    showFootnoteColors = currentShowFootnoteColors,
                    showHeadings = currentShowHeadings,
                    activeLexicon = activeLexicon,
                    onFootnoteStyleChange = { updateFootnoteStyle(it) },
                    onBibleVersionChange = { version, currentPage ->
                        readerContext = readerContext?.copy(initialIndex = currentPage)
                        updateBibleVersion(version)
                    },
                    onFontSizeChange = { updateFontSize(it) },
                    onGrammarColorsChange = { updateGrammarColors(it) },
                    onFootnoteColorsChange = { updateFootnoteColors(it) },
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