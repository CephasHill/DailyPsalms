package com.peter.dailypsalms

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AboutScreen(
    currentTrack: ReadingTrack,
    currentGraceDay: GraceDayOption,
    onTrackChange: (ReadingTrack) -> Unit,
    onGraceDayChange: (GraceDayOption) -> Unit,
    onReplayTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCreditsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ==========================================
        // 1. HEADER SECTION
        // ==========================================
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

        // ==========================================
        // 2. SETTINGS & SUPPORT SECTION
        // ==========================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Settings & Support",
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

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )

                    // Feedback & Bug Reports
                    Text(
                        text = "Feedback",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Found a bug or have a suggestion?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:p.hill.businesses@gmail.com".toUri()
                                putExtra(Intent.EXTRA_SUBJECT, "Daily Psalms App Feedback")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send Email"))
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Send Email",
                            modifier = Modifier.padding(end = 8.dp).size(18.dp)
                        )
                        Text("Contact Developer")
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )

                    // App Tour & Credits
                    TextButton(
                        onClick = onReplayTutorial,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Replay App Tour")
                    }

                    TextButton(
                        onClick = { showCreditsDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Credits & Copyrights")
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // ==========================================
        // 3. TRANSLATIONS SECTION
        // ==========================================
        item {
            TranslationsInfoSection(context)
            HorizontalDivider(modifier = Modifier.padding(vertical = 32.dp))
        }
    }

    // ==========================================
    // 5. CREDITS DIALOG OVERLAY
    // ==========================================
    if (showCreditsDialog) {
        AlertDialog(
            onDismissRequest = { showCreditsDialog = false },
            title = {
                Text("Credits & Attributions", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Public Domain Texts",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        "The KJV, WEB, Douay-Rheims, Geneva Bible, Vulgate, Septuagint (LXX), and Westminster Leningrad Codex (WLC) are in the public domain.\n\nThe Berean Standard Bible (BSB) text is dedicated to the public domain.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        "NET Bible",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        "NET Bible® copyright ©1996-2017 by Biblical Studies Press, L.L.C. http://netbible.com All rights reserved.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        "Open Source Data",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        "Morphological tagging and lexicon data provided by the Open Scriptures Hebrew Bible project and used under a Creative Commons (CC-BY-SA) license.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCreditsDialog = false }) {
                    Text("Close")
                }
            }
        )
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
    showFootnoteColors: Boolean,
    showHeadings: Boolean,
    activeLexicon: Map<String, String>,
    onFootnoteStyleChange: (FootnoteStyle) -> Unit,
    onBibleVersionChange: (BibleVersion, Int) -> Unit,
    onFontSizeChange: (FontSizeOption) -> Unit,
    onGrammarColorsChange: (Boolean) -> Unit,
    onFootnoteColorsChange: (Boolean) -> Unit,
    onShowHeadingsChange: (Boolean) -> Unit,
    onToggleComplete: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFootnote by remember { mutableStateOf<Footnote?>(null) }
    var formatMenuExpanded by remember { mutableStateOf(false) }
    var versionMenuExpanded by remember { mutableStateOf(false) }
    var settingsMenuExpanded by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(
        initialPage = readerContext.initialIndex,
        pageCount = { readerContext.playlist.size }
    )

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
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(18.dp).padding(end = 4.dp)
                        )
                        Text("Back")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.End
                    ) {

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

                                val groupedVersions = availableVersions.groupBy { it.category }
                                val categoryList = groupedVersions.keys.toList()

                                groupedVersions.forEach { (category, versions) ->
                                    // Category Header
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

                                    // Add a visual divider between categories
                                    if (category != categoryList.last()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }

                        // Unified Settings Dropdown
                        val showFootnoteOptions = hasFootnotes && currentBibleVersion != BibleVersion.HEB && currentBibleVersion != BibleVersion.LXX && currentBibleVersion != BibleVersion.VULGATE

                        Box {
                            TextButton(
                                onClick = { settingsMenuExpanded = true },
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text("Settings ▼", fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = settingsMenuExpanded,
                                onDismissRequest = { settingsMenuExpanded = false }
                            ) {
                                // ALWAYS VISIBLE: Text Size
                                Text(
                                    text = "Text Size",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
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
                                            settingsMenuExpanded = false
                                        }
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                // Conditional: Only show if translation supports footnotes
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
                                                settingsMenuExpanded = false
                                            }
                                        )
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    Text(
                                        text = "Footnote Tools",
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
                                                Text("Footnote Colors")
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Switch(
                                                    checked = showFootnoteColors,
                                                    onCheckedChange = null,
                                                    modifier = Modifier.scale(0.8f)
                                                )
                                            }
                                        },
                                        onClick = {
                                            onFootnoteColorsChange(!showFootnoteColors)
                                            formatMenuExpanded = false
                                        }
                                    )
                                }

                                // Conditional: Only show if Original Languages are selected
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
                                            settingsMenuExpanded = false
                                        }
                                    )
                                }

                                if (showFootnoteOptions || currentBibleVersion == BibleVersion.HEB || currentBibleVersion == BibleVersion.LXX) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }

                                // ALWAYS VISIBLE: Layout Toggle
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
                                        settingsMenuExpanded = false
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

                // Calculate the exact key and completion status for this specific page
                val pageChapterKey = readerContext.dailyKeys?.get(page) ?: "${chapter.normalizedBook}_${chapter.chapter}"
                val isPageCompleted = completedChapters.contains(pageChapterKey)

                ChapterRenderer(
                    chapter = chapter,
                    footnoteStyle = effectiveFootnoteStyle,
                    fontSizeOption = currentFontSize,
                    showGrammarColors = showGrammarColors,
                    showFootnoteColors = showFootnoteColors,
                    showHeadings = showHeadings,
                    onFootnoteClick = { marker ->
                        val existingFootnote = chapter.footnotes.find { it.marker == marker }
                        if (existingFootnote != null) {
                            selectedFootnote = existingFootnote
                        } else if (activeLexicon.isNotEmpty()) {
                            val definition = activeLexicon[marker] ?: "Definition not found in lexicon."

                            val formattedText = when (currentBibleVersion) {
                                BibleVersion.VULGATE -> "${marker.uppercase()}: $definition"
                                else -> definition
                            }

                            selectedFootnote = Footnote(marker = marker, text = formattedText)
                        }
                    },
                    isDailyMode = readerContext.isDailyMode,
                    isCompleted = isPageCompleted,
                    onToggleComplete = { onToggleComplete(pageChapterKey) }
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
fun OnboardingScreen(onFinish: (ReadingTrack, GraceDayOption, BibleVersion) -> Unit) {
    // Increased page count to 6 to accommodate the new Translations page
    val pagerState = rememberPagerState(pageCount = { 6 })
    val coroutineScope = rememberCoroutineScope()

    var selectedTrack by remember { mutableStateOf(ReadingTrack.CLASSIC) }
    var selectedGraceDay by remember { mutableStateOf(GraceDayOption.SUNDAY) }
    var selectedVersion by remember { mutableStateOf(BibleVersion.BSB) }

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
                        repeat(6) { index ->
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

                    if (pagerState.currentPage == 5) {
                        Button(onClick = { onFinish(selectedTrack, selectedGraceDay, selectedVersion) }) {
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
                        description = "Each day, you are assigned 1 Proverb and 5 Psalms spaced exactly 30 chapters apart.\n\nFor example, on the 5th of the month, you will read Proverbs 5 alongside Psalms 5, 35, 65, 95, and 125. (On the 31st, we pair Proverbs 31 with the 176-verse-long Psalm 119!)\n\nYou can customize your reading plan in the About menu."
                    )
                    2 -> OnboardingPage(
                        iconPainter = androidx.compose.ui.graphics.vector.rememberVectorPainter(image = Icons.Default.Info),
                        title = "Deep Study Tools",
                        description = "Explore a vast library of translations, from the modern BSB to the ancient Greek Septuagint.\n\nFor supported texts, you have access to footnotes, grammar color-coding, and integrated lexicons."
                    )
                    3 -> OnboardingPage(
                        iconPainter = androidx.compose.ui.graphics.vector.rememberVectorPainter(image = Icons.Default.Favorite),
                        title = "Stay Consistent",
                        description = "Build a lasting scripture habit! Track your progress with the built-in streak system, and add our Home Screen Widget to check your daily reading status at a glance."
                    )
                    4 -> {
                        // NEW PAGE: Translation Selection
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp).padding(bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Choose Your Text",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "Choose a popular translation to get started. You can easily switch versions in the Settings menu and learn more about our full library on the About page.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            val startingVersions = listOf(BibleVersion.BSB, BibleVersion.NET, BibleVersion.WEB, BibleVersion.KJV)

                            startingVersions.forEach { version ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { selectedVersion = version }.padding(vertical = 8.dp)
                                ) {
                                    androidx.compose.material3.RadioButton(
                                        selected = selectedVersion == version,
                                        onClick = { selectedVersion = version }
                                    )
                                    Column {
                                        Text(version.displayName, fontWeight = FontWeight.Bold)
                                        Text(version.description, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                    5 -> {
                        // UPDATED PAGE: Customization with Grace Day Explanation
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp).padding(bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Customize Your Plan",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Track Selector
                            Text("1. Select Your Reading Track", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Start))
                            ReadingTrack.entries.forEach { track ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { selectedTrack = track }.padding(vertical = 6.dp)
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

                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                            // Grace Day Explanation & Selector
                            Text("2. Set a Grace Day", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Start))
                            Text(
                                text = "Missed a reading? No problem. Incomplete chapters will roll over to the next day. As long as you finish your weekly chapters by your Grace Day, your streak is perfectly safe!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                                var graceMenuExpanded by remember { mutableStateOf(false) }
                                TextButton(onClick = { graceMenuExpanded = true }, contentPadding = PaddingValues(0.dp)) {
                                    Text("${selectedGraceDay.displayName} ▼", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                DropdownMenu(expanded = graceMenuExpanded, onDismissRequest = { graceMenuExpanded = false }) {
                                    GraceDayOption.entries.forEach { day ->
                                        DropdownMenuItem(
                                            text = { Text(day.displayName) },
                                            onClick = { selectedGraceDay = day; graceMenuExpanded = false }
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Note: You can change these preferences or disable the Grace Day at any time in the About page.",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )
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