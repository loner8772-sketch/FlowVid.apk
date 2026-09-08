package com.flowvid.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.flowvid.app.domain.model.ThemeMode

private val FlowVidDarkColors = darkColorScheme(
    primary = FlowVidAmber,
    onPrimary = FlowVidBackgroundDark,
    secondary = FlowVidAmberDim,
    background = FlowVidBackgroundDark,
    onBackground = FlowVidOnDark,
    surface = FlowVidSurfaceDark,
    onSurface = FlowVidOnDark,
    surfaceVariant = FlowVidSurfaceDarkElevated,
    onSurfaceVariant = FlowVidOnDarkMuted,
    error = FlowVidError,
)

private val FlowVidLightColors = lightColorScheme(
    primary = FlowVidAmberDim,
    onPrimary = FlowVidBackgroundLight,
    secondary = FlowVidAmber,
    background = FlowVidBackgroundLight,
    onBackground = FlowVidOnLight,
    surface = FlowVidSurfaceLight,
    onSurface = FlowVidOnLight,
    surfaceVariant = FlowVidSurfaceLight,
    onSurfaceVariant = FlowVidOnLightMuted,
    error = FlowVidError,
)

@Composable
fun FlowVidTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (useDark) FlowVidDarkColors else FlowVidLightColors,
        typography = FlowVidTypography,
        content = content,
    )
}

/** Always-dark scheme for the immersive Feed screen, independent of the user's theme setting. */
@Composable
fun FlowVidFeedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FlowVidDarkColors,
        typography = FlowVidTypography,
        content = content,
    )
}
