package com.peter.dailypsalms

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MinActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val appContext = InstrumentationRegistry
        .getInstrumentation()
        .targetContext

    @Before
    fun resetAppPreferences() {
        runBlocking {
            appContext.dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }

    @After
    fun cleanUpAppPreferences() {
        runBlocking {
            appContext.dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }

    @Test
    fun activity_canShowOnboarding() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule
                    .onNodeWithText("Welcome to Daily Psalms")
                    .assertIsDisplayed()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }
}
