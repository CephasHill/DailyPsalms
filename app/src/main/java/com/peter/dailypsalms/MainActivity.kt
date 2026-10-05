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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.peter.dailypsalms.ui.theme.DailyPsalmsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes

val Context.dataStore by preferencesDataStore(name = "settings")

// ScenarioTools provides an isolated store in debug builds. Direct previews
// and tests can still fall back to the normal settings store.
val LocalProgressDataStore = staticCompositionLocalOf<DataStore<Preferences>?> { null }

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

        setContent {
            DailyPsalmsTheme {
                ScenarioTools.Host {
                    val context = LocalContext.current
                    val progressStore = LocalProgressDataStore.current ?: context.dataStore
                    val prefs by progressStore.data.collectAsState(initial = null)
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
                                        progressStore.edit { p ->
                                            p[hasSeenOnboardingKey] = true

                                            val readingTrackKey = stringPreferencesKey("reading_track")
                                            val graceDayKey = stringPreferencesKey("grace_day")
                                            val bibleVersionKey = stringPreferencesKey("bible_version")

                                            p[readingTrackKey] = selectedTrack.name
                                            p[graceDayKey] = selectedGraceDay.name
                                            p[bibleVersionKey] = selectedVersion.code
                                            p[stringPreferencesKey("plan_start_date")] =
                                                (ScenarioTools.dateOverride(p) ?: LocalDate.now()).toString()
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
}

@Composable
fun MainAppContainer(
    openReadNow: Boolean = false,
    onReadNowConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val progressStore = LocalProgressDataStore.current ?: context.dataStore
    val coroutineScope = rememberCoroutineScope()

    val loadedPrefs by progressStore.data.collectAsState(initial = null)
    val prefs = loadedPrefs ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

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
    val historicalDatesKey = stringSetPreferencesKey("historical_dates")
    val recordStreakKey = intPreferencesKey("record_streak")
    val pendingCatchUpDatesKey = stringSetPreferencesKey("pending_catch_up_dates")
    val pendingCatchUpDateKey = stringPreferencesKey("pending_catch_up_date")
    val activeCatchUpKeysKey = stringSetPreferencesKey("active_catch_up_keys")
    val streakResetDateKey = stringPreferencesKey("streak_reset_date")
    val completionSnapshotDateKey = stringPreferencesKey("completion_snapshot_date")
    val completionSnapshotStreakKey = intPreferencesKey("completion_snapshot_streak")
    val completionSnapshotLast100DateKey = stringPreferencesKey("completion_snapshot_last_100_date")
    val completionSnapshotHistoricalDatesKey = stringSetPreferencesKey("completion_snapshot_historical_dates")
    val completionSnapshotStreakResetDateKey = stringPreferencesKey("completion_snapshot_streak_reset_date")


    // Read the current states (with defaults)
    val currentTrack = try {
        ReadingTrack.valueOf(prefs[readingTrackKey] ?: ReadingTrack.CLASSIC.name)
    } catch (_: Exception) { ReadingTrack.CLASSIC }

    val historicalDates = prefs[historicalDatesKey] ?: emptySet()
    val recordStreak = prefs[recordStreakKey] ?: 0

    val currentGraceDay = try {
        GraceDayOption.valueOf(prefs[graceDayKey] ?: GraceDayOption.NONE.name)
    } catch (_: Exception) { GraceDayOption.NONE }

    // DEFAULT BIBLE VERSION
    val preferredVersionCode = prefs[bibleVersionKey] ?: BibleVersion.BSB.code
    val safeVersionCode = if (areTranslationAssetsAvailable(context, preferredVersionCode)) {
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

    val todayDateState = remember { mutableStateOf(LocalDate.now()) }
    val todayDate = ScenarioTools.dateOverride(prefs) ?: todayDateState.value
    val todayStr = todayDate.toString()
    val yesterdayStr = todayDate.minusDays(1).toString()

    val cycleStartDate = remember(todayDate, currentGraceDay) {
        getCycleStartDate(todayDate, currentGraceDay)
    }
    val cycleStartStr = cycleStartDate.toString()

    val checkmarksDate = prefs[checkmarksDateKey] ?: ""
    val rawCompletedChapters = prefs[completedChaptersKey] ?: emptySet()
    val planStartStr = prefs[planStartDateKey]
    val planStartDate = inferPlanStartDate(
        today = todayDate,
        graceDay = currentGraceDay,
        storedPlanStartDate = planStartStr,
        cycleMarkerDate = checkmarksDate,
        lastCompletedDate = prefs[last100DateKey],
        legacyLastCompletedDate = prefs[legacyLast100DateKey],
        historicalDates = historicalDates,
        completedKeys = rawCompletedChapters
    )

    // 1.1.9 did not reliably persist the plan anchor for every user. Persist
    // the reconstructed value only when the stored value is missing/invalid;
    // the composable already uses the reconstructed value during this first
    // composition, so catch-up recovery does not wait for this write.
    LaunchedEffect(planStartStr, planStartDate) {
        if (parseLocalDate(planStartStr) == null) {
            progressStore.edit { p ->
                if (!ScenarioTools.isCurrentSnapshot(prefs, p)) return@edit
                if (parseLocalDate(p[planStartDateKey]) == null) {
                    p[planStartDateKey] = planStartDate.toString()
                }
            }
        }
    }

    // Ignore stale pending dates immediately when a new weekly cycle begins.
    // The migration effect below also removes them from DataStore and resets
    // the streak if the previous cycle ended with unread chapters.
    val storedPendingCatchUpDates = prefs[pendingCatchUpDatesKey] ?: emptySet()
    val activeCatchUpKeys = prefs[activeCatchUpKeysKey] ?: emptySet()
    val pendingCatchUpDates = storedPendingCatchUpDates.filter { dateString ->
        try {
            !LocalDate.parse(dateString).isBefore(cycleStartDate)
        } catch (_: Exception) {
            false
        }
    }.toSet()

    // Generate every scheduled date in the active cycle through today. The
    // Grace Day still has its normal assignment; it is also the final day for
    // completing that week's catch-up chapters.
    val cycleDates = remember(todayDate, planStartDate, currentGraceDay) {
        getCycleDates(todayDate, currentGraceDay, planStartDate)
    }

    // A track change starts a new plan today. Do not let streak credit infer
    // missed days before that new plan began.
    val streakCycleStartDate = if (planStartDate.isAfter(cycleStartDate)) {
        planStartDate
    } else {
        cycleStartDate
    }

    // Completion keys include their assignment date, so they can safely be
    // retained across weekly cycle boundaries. This is also what lets us
    // recognize a catch-up chapter from the previous cycle as completed.

    // Reconcile catch-ups at every date transition. Unfinished assignments
    // stay available through the selected Grace Day, but expire at the start
    // of the next cycle and break the streak if they were still unread.
    LaunchedEffect(todayStr, currentTrack, currentGraceDay, planStartDate, cycleStartStr, checkmarksDate) {
        fun parseDate(value: String?): LocalDate? = value
            ?.takeIf { it.isNotEmpty() }
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

        val storedPendingDateStrings = prefs[pendingCatchUpDatesKey] ?: emptySet()
        val storedPendingDates = storedPendingDateStrings
            .mapNotNull(::parseDate)
            .toSet()
        val containsInvalidPendingDate = storedPendingDateStrings.any { parseDate(it) == null }

        val reconciliation = CatchUpEngine.reconcile(
            CatchUpReconciliationInput(
                today = todayDate,
                track = currentTrack,
                graceDay = currentGraceDay,
                planStartDate = planStartDate,
                storedPendingDates = storedPendingDates,
                lastReconciledDate = parseDate(prefs[pendingCatchUpDateKey]),
                storedActiveCatchUpKeys = prefs[activeCatchUpKeysKey],
                cycleMarkerDate = parseDate(checkmarksDate),
                completedKeys = prefs[completedChaptersKey] ?: emptySet(),
                containsInvalidPendingDate = containsInvalidPendingDate
            )
        )

        progressStore.edit { p ->
            if (!ScenarioTools.isCurrentSnapshot(prefs, p)) return@edit
            p[pendingCatchUpDatesKey] = reconciliation.pendingDates
                .map { it.toString() }
                .toSet()
            p[pendingCatchUpDateKey] = todayStr
            p[activeCatchUpKeysKey] = reconciliation.activeCatchUpKeys
            p[checkmarksDateKey] = reconciliation.cycleStartDate.toString()

            if (reconciliation.expiredPreviousCycle) {
                p[streakKey] = 0
                p[streakResetDateKey] = reconciliation.cycleStartDate.toString()
            }

            if (p[completionSnapshotDateKey] != todayStr) {
                p.remove(completionSnapshotDateKey)
                p.remove(completionSnapshotStreakKey)
                p.remove(completionSnapshotLast100DateKey)
                p.remove(completionSnapshotHistoricalDatesKey)
                p.remove(completionSnapshotStreakResetDateKey)
            }
        }
    }

    var todayPlaylist by remember { mutableStateOf<List<DailyReading>>(emptyList()) }

    LaunchedEffect(currentBibleVersion, currentTrack, currentGraceDay, planStartDate, todayStr, pendingCatchUpDates, activeCatchUpKeys) {
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

            val dailyPlan = ReadingEngine.buildDailyPlan(
                DailyPlanInput(
                    today = todayDate,
                    track = currentTrack,
                    graceDay = currentGraceDay,
                    planStartDate = planStartDate,
                    pendingCatchUpDates = pendingCatchUpDates
                        .mapNotNull { dateString ->
                            runCatching { LocalDate.parse(dateString) }.getOrNull()
                        }
                        .toSet(),
                    activeCatchUpKeys = activeCatchUpKeys,
                    completedKeys = rawCompletedChapters
                )
            )

            val fullCycleAssignments = dailyPlan.assignments

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
                progressStore.edit { p ->
                    if (!ScenarioTools.isCurrentSnapshot(prefs, p)) return@edit
                    p[intPreferencesKey("widget_done_count")] = activeDoneCount
                    p[intPreferencesKey("widget_total_count")] = generatedPlaylist.size
                }
                ScenarioTools.updateWidgets(context, prefs)

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
            val liveToday = LocalDate.now()
            if (liveToday != todayDateState.value) {
                todayDateState.value = liveToday
                ScenarioTools.updateWidgets(context, prefs)
            }
        }
    }

    LaunchedEffect(Unit) {
        ScenarioTools.updateWidgets(context, prefs)
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

    val hasUnfinishedCatchUp = cycleDates
        .filter { it.isBefore(todayDate) }
        .any { date ->
            getAssignedChapters(date, currentTrack, planStartDate).any { assignment ->
                !rawCompletedChapters.contains(assignmentKey(assignment))
            }
        }

    val streakWasReset = prefs[streakResetDateKey] == cycleStartStr
    val displayStreak = if (streakWasReset) {
        0
    } else {
        when (last100Date) {
            todayStr -> actualStreak
            yesterdayStr -> actualStreak
            "" -> actualStreak // Recover widget-only streaks where the date key was missing or mismatched
            else -> if (
                actualStreak > 0 &&
                (pendingCatchUpDates.isNotEmpty() ||
                        isGraceDay(todayDate, currentGraceDay) ||
                        hasUnfinishedCatchUp)
            ) actualStreak else 0
        }
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
                coroutineScope.launch {
                    var shouldShowCompletionAnimation = false

                    progressStore.edit { p ->
                        if (!ScenarioTools.isCurrentSnapshot(prefs, p)) return@edit
                        // Read the latest set inside DataStore's serialized
                        // transaction. Two rapid taps must build on each
                        // other's result instead of both reading the same
                        // Compose snapshot and overwriting one completion.
                        val currentCompletedChapters = p[completedChaptersKey] ?: emptySet()
                        val newCompletedChapters = if (currentCompletedChapters.contains(key)) {
                            currentCompletedChapters - key
                        } else {
                            currentCompletedChapters + key
                        }

                        // Catch-up chapters count toward today's completion
                        // requirement.
                        val activeDoneCount = newCompletedChapters
                            .intersect(todayPlaylistKeys)
                            .size
                        val isNow100 = todayPlaylist.isNotEmpty() &&
                                activeDoneCount == todayPlaylist.size
                        val was100 = todayPlaylist.isNotEmpty() &&
                                currentCompletedChapters.intersect(todayPlaylistKeys).size == todayPlaylist.size
                        shouldShowCompletionAnimation = isNow100 && !was100

                        p[checkmarksDateKey] = cycleStartStr
                        p[completedChaptersKey] = newCompletedChapters

                        p[intPreferencesKey("widget_done_count")] = activeDoneCount
                        p[intPreferencesKey("widget_total_count")] = todayPlaylist.size

                        val currentHistorical = p[historicalDatesKey] ?: emptySet()
                        val currentState = StreakState(
                            streak = p[streakKey] ?: 0,
                            lastCompletedDate = parseLocalDate(
                                p[last100DateKey] ?: p[legacyLast100DateKey]
                            ),
                            historicalDates = currentHistorical.mapNotNull(::parseLocalDate).toSet(),
                            recordStreak = p[recordStreakKey] ?: 0,
                            streakResetDate = parseLocalDate(p[streakResetDateKey])
                        )

                        val undoSnapshot = parseLocalDate(p[completionSnapshotDateKey])?.let { snapshotDate ->
                            StreakUndoSnapshot(
                                date = snapshotDate,
                                streak = p[completionSnapshotStreakKey] ?: 0,
                                lastCompletedDate = parseLocalDate(p[completionSnapshotLast100DateKey]),
                                historicalDates = (p[completionSnapshotHistoricalDatesKey] ?: emptySet())
                                    .mapNotNull(::parseLocalDate)
                                    .toSet(),
                                streakResetDate = parseLocalDate(p[completionSnapshotStreakResetDateKey])
                            )
                        }

                        val streakTransition = StreakEngine.apply(
                            StreakTransitionInput(
                                today = todayDate,
                                cycleStartDate = streakCycleStartDate,
                                cycleDates = cycleDates,
                                wasComplete = was100,
                                isComplete = isNow100,
                                state = currentState,
                                undoSnapshot = undoSnapshot
                            )
                        )

                        if (streakTransition.state != currentState) {
                            val nextState = streakTransition.state
                            p[streakKey] = nextState.streak
                            p[historicalDatesKey] = nextState.historicalDates
                                .map { it.toString() }
                                .toSet()

                            if (nextState.lastCompletedDate == null) {
                                p.remove(last100DateKey)
                                p.remove(legacyLast100DateKey)
                            } else {
                                p[last100DateKey] = nextState.lastCompletedDate.toString()
                                p.remove(legacyLast100DateKey)
                            }

                            if (nextState.streakResetDate == null) {
                                p.remove(streakResetDateKey)
                            } else {
                                p[streakResetDateKey] = nextState.streakResetDate.toString()
                            }

                            if (nextState.recordStreak != currentState.recordStreak) {
                                p[recordStreakKey] = nextState.recordStreak
                            }
                        }

                        when (streakTransition.snapshotAction) {
                            CompletionSnapshotAction.SAVE -> {
                                val snapshot = requireNotNull(streakTransition.snapshotToSave)
                                p[completionSnapshotDateKey] = snapshot.date.toString()
                                p[completionSnapshotStreakKey] = snapshot.streak
                                p[completionSnapshotLast100DateKey] =
                                    snapshot.lastCompletedDate?.toString() ?: ""
                                p[completionSnapshotHistoricalDatesKey] = snapshot.historicalDates
                                    .map { it.toString() }
                                    .toSet()
                                p[completionSnapshotStreakResetDateKey] =
                                    snapshot.streakResetDate?.toString() ?: ""
                            }

                            CompletionSnapshotAction.CLEAR -> {
                                p.remove(completionSnapshotDateKey)
                                p.remove(completionSnapshotStreakKey)
                                p.remove(completionSnapshotLast100DateKey)
                                p.remove(completionSnapshotHistoricalDatesKey)
                                p.remove(completionSnapshotStreakResetDateKey)
                            }

                            CompletionSnapshotAction.KEEP -> Unit
                        }
                    }

                    if (shouldShowCompletionAnimation) {
                        showExplosion = true
                    }
                    ScenarioTools.updateWidgets(context, prefs)
                }
            }

            val updateFootnoteStyle = { style: FootnoteStyle ->
                coroutineScope.launch {
                    progressStore.edit { p ->
                        if (!ScenarioTools.isCurrentSnapshot(prefs, p)) return@edit
                        p[footnoteStyleKey] = style.name
                    }
                }
            }

            val updateFontSize = { size: FontSizeOption ->
                coroutineScope.launch {
                    progressStore.edit { p ->
                        if (!ScenarioTools.isCurrentSnapshot(prefs, p)) return@edit
                        p[fontSizeKey] = size.name
                    }
                }
            }

            val updateGrammarColors = { show: Boolean ->
                coroutineScope.launch {
                    progressStore.edit { p ->
                        if (!ScenarioTools.isCurrentSnapshot(prefs, p)) return@edit
                        p[showGrammarColorsKey] = show
                    }
                }
            }

            val updateFootnoteColors = { show: Boolean ->
                coroutineScope.launch {
                    progressStore.edit { p ->
                        if (!ScenarioTools.isCurrentSnapshot(prefs, p)) return@edit
                        p[showFootnoteColorsKey] = show
                    }
                }
            }

            val updateShowHeadings = { show: Boolean ->
                coroutineScope.launch {
                    progressStore.edit { p ->
                        if (!ScenarioTools.isCurrentSnapshot(prefs, p)) return@edit
                        p[showHeadingsKey] = show
                    }
                }
            }

            val updateBibleVersion = { version: BibleVersion ->
                coroutineScope.launch {
                    progressStore.edit { p ->
                        if (!ScenarioTools.isCurrentSnapshot(prefs, p)) return@edit
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
                            today = todayDate,
                            playlist = todayPlaylist,
                            completedChapters = completedChapters,
                            streakCount = displayStreak,
                            historicalDates = historicalDates,
                            recordStreak = recordStreak,
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
                                    progressStore.edit { p ->
                                        if (!ScenarioTools.isCurrentSnapshot(prefs, p)) return@edit
                                        p[readingTrackKey] = newTrack.name
                                        p[planStartDateKey] = todayStr
                                        p[checkmarksDateKey] = cycleStartStr
                                        p[completedChaptersKey] = emptySet()
                                        p[pendingCatchUpDatesKey] = emptySet()
                                        p[pendingCatchUpDateKey] = todayStr
                                        p[activeCatchUpKeysKey] = emptySet()
                                        p.remove(streakResetDateKey)
                                        p.remove(completionSnapshotDateKey)
                                        p.remove(completionSnapshotStreakKey)
                                        p.remove(completionSnapshotLast100DateKey)
                                        p.remove(completionSnapshotHistoricalDatesKey)
                                        p.remove(completionSnapshotStreakResetDateKey)
                                    }
                                }
                            },
                            onGraceDayChange = { newDay ->
                                coroutineScope.launch {
                                    progressStore.edit { p ->
                                        if (!ScenarioTools.isCurrentSnapshot(prefs, p)) return@edit
                                        p[graceDayKey] = newDay.name
                                        // Grace Day changes the catch-up
                                        // deadline, not the reading-plan
                                        // phase. Preserve plan_start_date so
                                        // paced tracks do not silently restart.
                                        p[checkmarksDateKey] = getCycleStartDate(todayDate, newDay).toString()
                                        p[pendingCatchUpDatesKey] = emptySet()
                                        p[pendingCatchUpDateKey] = todayStr
                                        p[activeCatchUpKeysKey] = emptySet()
                                        p.remove(streakResetDateKey)
                                        p.remove(completionSnapshotDateKey)
                                        p.remove(completionSnapshotStreakKey)
                                        p.remove(completionSnapshotLast100DateKey)
                                        p.remove(completionSnapshotHistoricalDatesKey)
                                        p.remove(completionSnapshotStreakResetDateKey)
                                    }
                                }
                            },
                            developmentTools = { ScenarioTools.Entry() },
                            onReplayTutorial = {
                                coroutineScope.launch {
                                    progressStore.edit { p ->
                                        if (!ScenarioTools.isCurrentSnapshot(prefs, p)) return@edit
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
