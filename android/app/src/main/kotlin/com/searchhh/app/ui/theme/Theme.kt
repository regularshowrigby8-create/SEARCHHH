package com.searchhh.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Forest700,
    onPrimary = Lime400,
    primaryContainer = Lime300,
    onPrimaryContainer = Forest900,
    secondary = Forest600,
    onSecondary = Sand50,
    secondaryContainer = Sand100,
    onSecondaryContainer = Forest900,
    tertiary = Lime700,
    onTertiary = Sand50,
    background = Sand50,
    onBackground = Charcoal900,
    surface = Sand50,
    onSurface = Charcoal900,
    surfaceVariant = Sand100,
    onSurfaceVariant = Forest800,
    outline = Forest600,
    error = Clay500,
)

private val DarkColors = darkColorScheme(
    primary = Lime400,
    onPrimary = Forest900,
    primaryContainer = Forest700,
    onPrimaryContainer = Lime300,
    secondary = Lime300,
    onSecondary = Forest900,
    secondaryContainer = Charcoal700,
    onSecondaryContainer = Sand50,
    tertiary = Sky500,
    onTertiary = Sand50,
    background = Charcoal900,
    onBackground = Sand50,
    surface = Charcoal800,
    onSurface = Sand50,
    surfaceVariant = Charcoal700,
    onSurfaceVariant = Sand100,
    outline = Forest600,
    error = Clay500,
)

/**
 * Material 3 theme with dynamic colour (Android 12+) and a brand fallback.
 *
 * @param dynamicColor set false to force the brand palette (screenshots/tests).
 */
@Composable
fun SearchhhTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SearchhhTypography,
        content = content,
    )
}
