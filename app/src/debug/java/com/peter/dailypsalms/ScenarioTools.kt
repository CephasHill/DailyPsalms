package com.peter.dailypsalms

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import com.peter.dailypsalms.ui.theme.DailyPsalmsWidget
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.scenarioDataStore by preferencesDataStore(name = "debug_scenarios")
private val scenarioEnabledKey = booleanPreferencesKey("debug_scenario_enabled")
private val scenarioDateKey = stringPreferencesKey("debug_scenario_date")
private val scenarioRevisionKey = longPreferencesKey("debug_scenario_revision")
private val LocalScenarioController = staticCompositionLocalOf<ScenarioController?> { null }

/** This entire implementation is compiled only for debug build variants. */
object ScenarioTools {
    @Composable
    fun Host(content: @Composable () -> Unit) {
        val context = LocalContext.current.applicationContext
        val controller = remember(context) {
            ScenarioController(context.dataStore, context.scenarioDataStore)
        }
        val scenarioPrefs by controller.scenarios.data.collectAsState(initial = null)
        val normalPrefs by controller.normal.data.collectAsState(initial = null)
        val loadedScenario = scenarioPrefs
        val loadedNormal = normalPrefs
        if (loadedScenario == null || loadedNormal == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        val active = loadedScenario[scenarioEnabledKey] == true
        val store = if (active) controller.scenarios else controller.normal
        CompositionLocalProvider(LocalScenarioController provides controller) {
            // Remount after seeding or moving the date, cancelling old UI effects.
            key(active, loadedScenario[scenarioRevisionKey] ?: 0L) {
                CompositionLocalProvider(LocalProgressDataStore provides store) { content() }
            }
            if (controller.showTools) {
                ScenarioEditor(
                    controller = controller,
                    active = active,
                    currentPrefs = if (active) loadedScenario else loadedNormal,
                    savedScenario = loadedScenario
                )
            }
        }
    }

    @Composable
    fun Entry() {
        val controller = LocalScenarioController.current ?: return
        TextButton(onClick = { controller.showTools = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Test scenarios & screenshot presets")
        }
    }

    fun dateOverride(prefs: Preferences): LocalDate? =
        if (prefs[scenarioEnabledKey] == true) parseLocalDate(prefs[scenarioDateKey]) else null

    fun isCurrentSnapshot(before: Preferences, current: Preferences): Boolean =
        before[scenarioEnabledKey] == current[scenarioEnabledKey] &&
                before[scenarioRevisionKey] == current[scenarioRevisionKey]

    suspend fun updateWidgets(context: Context, prefs: Preferences) {
        // Widgets still use the normal settings file and the Android system date.
        // A simulated session must not refresh/mutate that independent progress.
        if (prefs[scenarioEnabledKey] != true) DailyPsalmsWidget().updateAll(context)
    }
}

private class ScenarioController(
    val normal: DataStore<Preferences>,
    val scenarios: DataStore<Preferences>
) {
    var showTools by mutableStateOf(false)

    suspend fun load(spec: DebugScenarioSpec) {
        val fixture = DebugScenarioFixtures.build(spec)
        val normalPrefs = normal.data.first()
        scenarios.edit { p ->
            val nextRevision = (p[scenarioRevisionKey] ?: 0L) + 1
            p.clear()

            // Carry over appearance/translation settings, never real reading progress.
            fun <T> copySetting(key: Preferences.Key<T>) {
                normalPrefs[key]?.let { p[key] = it }
            }
            listOf("footnote_style", "bible_version", "font_size").forEach {
                copySetting(stringPreferencesKey(it))
            }
            listOf("show_grammar_colors", "show_footnote_colors", "show_headings").forEach {
                copySetting(booleanPreferencesKey(it))
            }

            p[scenarioEnabledKey] = true
            p[scenarioDateKey] = spec.today.toString()
            p[scenarioRevisionKey] = nextRevision
            p[booleanPreferencesKey("has_seen_onboarding")] = true
            p[stringPreferencesKey("reading_track")] = spec.track.name
            p[stringPreferencesKey("grace_day")] = spec.graceDay.name
            p[stringPreferencesKey("plan_start_date")] = spec.effectivePlanStartDate.toString()
            p[stringPreferencesKey("checkmarks_date")] = fixture.cycleMarkerDate.toString()
            p[stringSetPreferencesKey("completed_chapters")] = fixture.completedKeys
            p[intPreferencesKey("streak")] = fixture.streakState.streak
            p[intPreferencesKey("record_streak")] = fixture.streakState.recordStreak
            p[stringSetPreferencesKey("historical_dates")] = fixture.streakState.historicalDates
                .map { it.toString() }.toSet()
            fixture.streakState.lastCompletedDate?.let {
                p[stringPreferencesKey("last_100_date")] = it.toString()
            }
            fixture.streakState.streakResetDate?.let {
                p[stringPreferencesKey("streak_reset_date")] = it.toString()
            }
            p[stringSetPreferencesKey("pending_catch_up_dates")] = fixture.pendingDates
                .map { it.toString() }.toSet()
            p[stringPreferencesKey("pending_catch_up_date")] = spec.today.toString()
            p[stringSetPreferencesKey("active_catch_up_keys")] = fixture.activeCatchUpKeys
            p[intPreferencesKey("widget_done_count")] = fixture.doneCount
            p[intPreferencesKey("widget_total_count")] = fixture.visibleAssignments.size
            fixture.undoSnapshot?.let { snapshot ->
                p[stringPreferencesKey("completion_snapshot_date")] = snapshot.date.toString()
                p[intPreferencesKey("completion_snapshot_streak")] = snapshot.streak
                p[stringPreferencesKey("completion_snapshot_last_100_date")] =
                    snapshot.lastCompletedDate?.toString() ?: ""
                p[stringSetPreferencesKey("completion_snapshot_historical_dates")] =
                    snapshot.historicalDates.map { it.toString() }.toSet()
                p[stringPreferencesKey("completion_snapshot_streak_reset_date")] =
                    snapshot.streakResetDate?.toString() ?: ""
            }
        }
        showTools = false
    }

    suspend fun advanceTo(date: LocalDate) {
        scenarios.edit { p ->
            require(p[scenarioEnabledKey] == true) { "Load or resume a scenario first." }
            val current = requireNotNull(parseLocalDate(p[scenarioDateKey]))
            require(date.isAfter(current)) { "Choose a later date; load a new scenario to move backwards." }
            p[scenarioDateKey] = date.toString()
            p[scenarioRevisionKey] = (p[scenarioRevisionKey] ?: 0L) + 1
            // Preserve yesterday's markers and checks. MainActivity reconciles
            // the transition just as it does after days away from the app.
        }
        showTools = false
    }

    suspend fun resume() {
        scenarios.edit { p ->
            require(parseLocalDate(p[scenarioDateKey]) != null) { "Load a scenario first." }
            p[scenarioEnabledKey] = true
            p[scenarioRevisionKey] = (p[scenarioRevisionKey] ?: 0L) + 1
        }
        showTools = false
    }

    suspend fun leave() {
        scenarios.edit { p ->
            p[scenarioEnabledKey] = false
            p[scenarioRevisionKey] = (p[scenarioRevisionKey] ?: 0L) + 1
        }
        showTools = false
    }
}

@Composable
private fun ScenarioEditor(
    controller: ScenarioController,
    active: Boolean,
    currentPrefs: Preferences,
    savedScenario: Preferences
) {
    val scope = rememberCoroutineScope()
    val initialDate = ScenarioTools.dateOverride(currentPrefs) ?: LocalDate.now()
    var dateText by remember { mutableStateOf(initialDate.toString()) }
    var streakText by remember { mutableStateOf("20") }
    var daysText by remember { mutableStateOf("1") }
    var anchorText by remember { mutableStateOf("") }
    var track by remember {
        mutableStateOf(runCatching {
            ReadingTrack.valueOf(currentPrefs[stringPreferencesKey("reading_track")] ?: "CLASSIC")
        }.getOrDefault(ReadingTrack.CLASSIC))
    }
    var graceDay by remember {
        mutableStateOf(runCatching {
            GraceDayOption.valueOf(currentPrefs[stringPreferencesKey("grace_day")] ?: "SUNDAY")
        }.getOrDefault(GraceDayOption.SUNDAY))
    }
    var assignedChecks by remember { mutableStateOf(emptySet<String>()) }
    var catchUpChecks by remember { mutableStateOf(emptySet<String>()) }
    var preset by remember { mutableStateOf(DebugScenarioPreset.STREAK_20) }
    var busy by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }

    fun resetChecks() {
        assignedChecks = emptySet()
        catchUpChecks = emptySet()
    }
    fun perform(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        actionError = null
        scope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                actionError = e.message ?: "Could not load this scenario."
            } finally {
                busy = false
            }
        }
    }
    fun toggleAssigned(key: String) {
        assignedChecks = if (key in assignedChecks) assignedChecks - key else assignedChecks + key
    }

    val selectedDate = parseLocalDate(dateText)?.takeIf { it.year in 1900..2200 }
    val initialStreak = streakText.toIntOrNull()
    val days = daysText.toIntOrNull()
    val anchor = parseLocalDate(anchorText)
    val spec = if (
        selectedDate != null && selectedDate.year in 1900..2200 &&
        initialStreak != null && initialStreak in 0..3650 &&
        days != null && days in 1..31 && (anchorText.isBlank() || anchor != null)
    ) {
        DebugScenarioSpec(selectedDate, track, graceDay, initialStreak, days, anchor, assignedChecks, catchUpChecks)
    } else null
    val previewResult = remember(spec) { spec?.let { runCatching { DebugScenarioFixtures.build(it) } } }
    val preview = previewResult?.getOrNull()
    val validationError = if (spec == null) {
        "Use a date from 1900–2200, a streak of 0–3650, and 1–31 editable days. Dates use YYYY-MM-DD."
    } else previewResult?.exceptionOrNull()?.message
    val simulatedDate = ScenarioTools.dateOverride(currentPrefs)
    val dayFormat = remember { DateTimeFormatter.ofPattern("EEE, MMM d") }

    Dialog(
        onDismissRequest = { if (!busy) controller.showTools = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Test scenarios", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        TextButton(onClick = { controller.showTools = false }, enabled = !busy) { Text("Close") }
                    }
                    Text("Separate test progress. Presets and date changes return you to Daily for screenshots.",
                        style = MaterialTheme.typography.bodySmall)
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (active) "Simulation: $simulatedDate" else "Normal progress · real date",
                                fontWeight = FontWeight.Bold)
                            if (active && simulatedDate != null) {
                                Text("Current streak: ${currentPrefs[intPreferencesKey("streak")] ?: 0}")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { perform { controller.advanceTo(simulatedDate.plusDays(1)) } }, enabled = !busy) {
                                        Text("Next day")
                                    }
                                    TextButton(onClick = { perform { controller.advanceTo(simulatedDate.plusDays(7)) } }, enabled = !busy) {
                                        Text("Skip 7 days")
                                    }
                                }
                                val currentGrace = runCatching {
                                    GraceDayOption.valueOf(currentPrefs[stringPreferencesKey("grace_day")] ?: "NONE")
                                }.getOrDefault(GraceDayOption.NONE)
                                TextButton(onClick = {
                                    val boundary = if (currentGrace == GraceDayOption.NONE) simulatedDate.plusDays(1)
                                    else getCycleStartDate(simulatedDate, currentGrace).plusDays(7)
                                    perform { controller.advanceTo(boundary) }
                                }, enabled = !busy) { Text("Cross Grace Day deadline") }
                                TextButton(onClick = { perform { controller.leave() } }, enabled = !busy) {
                                    Text("Return to normal progress")
                                }
                            } else if (parseLocalDate(savedScenario[scenarioDateKey]) != null) {
                                TextButton(onClick = { perform { controller.resume() } }, enabled = !busy) {
                                    Text("Resume saved scenario")
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it; resetChecks() },
                        label = { Text("Scenario date · YYYY-MM-DD") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !busy
                    )
                    if (active && selectedDate != null && simulatedDate != null && selectedDate.isAfter(simulatedDate)) {
                        TextButton(onClick = { perform { controller.advanceTo(selectedDate) } }, enabled = !busy) {
                            Text("Advance existing scenario to $selectedDate")
                        }
                    }
                }
                item {
                    ChoiceField("Reading track", track, ReadingTrack.entries.toList(), { it.displayName }, !busy) {
                        track = it; resetChecks()
                    }
                    ChoiceField("Grace Day", graceDay, GraceDayOption.entries.toList(), { it.displayName }, !busy) {
                        graceDay = it; resetChecks()
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    ChoiceField("Quick preset", preset, DebugScenarioPreset.entries.toList(), { it.title }, !busy) { preset = it }
                    Button(onClick = {
                        selectedDate?.let { date -> perform { controller.load(preset.spec(date, track, graceDay)) } }
                    }, enabled = !busy && selectedDate != null, modifier = Modifier.fillMaxWidth()) {
                        Text("Load preset")
                    }
                    Text("Missed-day presets use Wednesday or Sunday and Sunday Grace Day.",
                        style = MaterialTheme.typography.bodySmall)
                }
                item {
                    HorizontalDivider()
                    Text("Build a new scenario", style = MaterialTheme.typography.titleMedium)
                    Text("Begin with a completed streak, then choose what was read on the editable dates.",
                        style = MaterialTheme.typography.bodySmall)
                }
                item {
                    OutlinedTextField(
                        value = streakText, onValueChange = { streakText = it; resetChecks() },
                        label = { Text("Streak before editable dates") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !busy
                    )
                }
                item {
                    OutlinedTextField(
                        value = daysText, onValueChange = { daysText = it; resetChecks() },
                        label = { Text("Editable days including today · 1–31") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !busy
                    )
                }
                item {
                    OutlinedTextField(
                        value = anchorText, onValueChange = { anchorText = it; resetChecks() },
                        label = { Text("Plan start date · blank for automatic") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !busy
                    )
                    Text("The plan start controls which chapters paced tracks assign.", style = MaterialTheme.typography.bodySmall)
                }
                if (preview != null && spec != null) {
                    item {
                        Text("New scenario preview: ${preview.streakState.streak}-day streak, " +
                                "${preview.doneCount}/${preview.visibleAssignments.size} readings checked, " +
                                "${preview.visibleAssignments.count { it.assignedDate.isBefore(spec.today) }} catch-up readings.",
                            fontWeight = FontWeight.Bold)
                        Button(onClick = { perform { controller.load(spec) } }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                            Text("Load custom scenario")
                        }
                        Text("Checks below mean chapters read on their assigned date. Streak credit follows the app's catch-up rules.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    items(spec.editableDates(), key = { it.toString() }) { date ->
                        val assignments = getAssignedChapters(date, track, spec.effectivePlanStartDate)
                        val keys = assignments.map(::assignmentKey).toSet()
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(date.format(dayFormat) + if (date == spec.today) " · today" else "", fontWeight = FontWeight.Bold)
                                Row {
                                    TextButton(onClick = { assignedChecks = assignedChecks + keys }, enabled = !busy) { Text("All read") }
                                    TextButton(onClick = { assignedChecks = assignedChecks - keys }, enabled = !busy) { Text("None read") }
                                }
                                assignments.forEach { assignment ->
                                    val chapterKey = assignmentKey(assignment)
                                    ChapterCheck(assignment, chapterKey in assignedChecks, !busy) { toggleAssigned(chapterKey) }
                                }
                            }
                        }
                    }
                    val catchUps = preview.visibleAssignments.filter { it.assignedDate.isBefore(spec.today) }
                    if (catchUps.isNotEmpty()) {
                        item {
                            Text("Catch-up read today", style = MaterialTheme.typography.titleMedium)
                            Text("These keep their catch-up labels today after you check them.", style = MaterialTheme.typography.bodySmall)
                            Row {
                                TextButton(onClick = { catchUpChecks = catchUps.map(::assignmentKey).toSet() }, enabled = !busy) { Text("All read") }
                                TextButton(onClick = { catchUpChecks = emptySet() }, enabled = !busy) { Text("None read") }
                            }
                        }
                        items(catchUps, key = { "catchup_${assignmentKey(it)}" }) { assignment ->
                            val chapterKey = assignmentKey(assignment)
                            ChapterCheck(assignment, chapterKey in catchUpChecks, !busy, showDate = true) {
                                catchUpChecks = if (chapterKey in catchUpChecks) catchUpChecks - chapterKey else catchUpChecks + chapterKey
                            }
                        }
                    }
                }
                if (validationError != null || actionError != null) {
                    item { Text(actionError ?: validationError.orEmpty(), color = MaterialTheme.colorScheme.error) }
                }
                if (busy) item { CircularProgressIndicator() }
            }
        }
    }
}

@Composable
private fun <T> ChoiceField(
    label: String,
    selected: T,
    choices: List<T>,
    title: (T) -> String,
    enabled: Boolean,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Text(label, style = MaterialTheme.typography.labelMedium)
    Box {
        TextButton(onClick = { expanded = true }, enabled = enabled) { Text("${title(selected)} ▼") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            choices.forEach { choice ->
                DropdownMenuItem(text = { Text(title(choice)) }, onClick = { expanded = false; onSelect(choice) })
            }
        }
    }
}

@Composable
private fun ChapterCheck(
    assignment: AssignedChapter,
    checked: Boolean,
    enabled: Boolean,
    showDate: Boolean = false,
    onToggle: () -> Unit
) {
    val part = assignment.partId?.let { " · part $it" } ?: ""
    val date = if (showDate) " · ${assignment.assignedDate}" else ""
    Row(
        Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
        Text("${assignment.book} ${assignment.chapter}$part$date", style = MaterialTheme.typography.bodyMedium)
    }
}
