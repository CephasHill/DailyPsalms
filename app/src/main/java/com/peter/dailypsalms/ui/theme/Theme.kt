package com.peter.dailypsalms.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    secondary = BlueSecondaryDark,
    tertiary = BlueTertiaryDark,
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4F6),
    secondaryContainer = Color(0xFF30404D),
    onSecondaryContainer = Color(0xFFD9E3EC),
    tertiaryContainer = Color(0xFF00497D),
    onTertiaryContainer = Color(0xFFD1E4F6),
    surfaceVariant = Color(0xFF192D3E),
    onSurfaceVariant = Color(0xFFB0BCC7),
    background = Color(0xFF0C1B2A),
    surface = Color(0xFF0C1B2A),
    surfaceTint = BluePrimaryDark,
    surfaceContainerLowest = Color(0xFF08121C),
    surfaceContainerLow = Color(0xFF0C1B2A),
    surfaceContainer = Color(0xFF102337),
    surfaceContainerHigh = Color(0xFF142B44),
    surfaceContainerHighest = Color(0xFF183351),
    surfaceBright = Color(0xFF203B5C),
    surfaceDim = Color(0xFF08121C),
    onPrimary = Color(0xFF003258),
    onSecondary = Color(0xFF192D3E),
    onTertiary = Color(0xFF003258),
    onBackground = Color(0xFFE2E2E2),
    onSurface = Color(0xFFE2E2E2),
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimaryLight,
    secondary = BlueSecondaryLight,
    tertiary = BlueTertiaryLight,
    primaryContainer = Color(0xFFD1E4F6),
    onPrimaryContainer = Color(0xFF001D36),
    secondaryContainer = Color(0xFFD9E3EC),
    onSecondaryContainer = Color(0xFF001F31),
    tertiaryContainer = Color(0xFFD1E4F6),
    onTertiaryContainer = Color(0xFF001D36),
    surfaceVariant = Color(0xFFDEE3E9),
    onSurfaceVariant = Color(0xFF42474E),
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFF8F9FA),
    surfaceTint = BluePrimaryLight,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F7FA),
    surfaceContainer = Color(0xFFE9F0F6),
    surfaceContainerHigh = Color(0xFFE0E9F2),
    surfaceContainerHighest = Color(0xFFD8E3ED),
    surfaceBright = Color(0xFFF8F9FA),
    surfaceDim = Color(0xFFD9E2E9),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun DailyPsalmsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}