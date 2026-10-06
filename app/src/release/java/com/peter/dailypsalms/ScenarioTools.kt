package com.peter.dailypsalms

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.updateAll
import com.peter.dailypsalms.ui.theme.DailyPsalmsWidget
import java.time.LocalDate

/** 
 * No-op implementation for the release build variant.
 * Real scenario tools are only compiled in debug builds.
 */
object ScenarioTools {
    @Composable
    fun Host(content: @Composable () -> Unit) {
        content()
    }

    @Composable
    fun Entry() {
        // No development tools entry point in release builds
    }

    @Suppress("UNUSED_PARAMETER", "SameReturnValue")
    fun dateOverride(prefs: Preferences): LocalDate? = null

    @Suppress("UNUSED_PARAMETER", "SameReturnValue")
    fun isCurrentSnapshot(before: Preferences, current: Preferences): Boolean = true

    @Suppress("UNUSED_PARAMETER")
    suspend fun updateWidgets(context: Context, prefs: Preferences) {
        DailyPsalmsWidget().updateAll(context)
    }
}

