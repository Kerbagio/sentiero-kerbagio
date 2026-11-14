// UI layer: every color from DESIGN.md, as light and dark Material 3 color schemes.
// Colors are a look-and-feel decision, so they live with the theme, not in screens or data.
package com.anthony.sentiero.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// The blaze is not a Material role: it is the dot inside the "Forgot last time" tag (used later).
val Blaze = Color(0xFFE8590C)
val BlazeDark = Color(0xFFFF8A4C)

val LightColors = lightColorScheme(
    primary = Color(0xFF24473A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFE3D8),
    onPrimaryContainer = Color(0xFF0B2A1F),
    inversePrimary = Color(0xFF9DCFB6),
    secondary = Color(0xFF55635B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD8E2DB),
    onSecondaryContainer = Color(0xFF131E18),
    tertiary = Color(0xFFB5400B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDBCC),
    onTertiaryContainer = Color(0xFF5A1B00),
    background = Color(0xFFF4F4F0),
    onBackground = Color(0xFF1A231F),
    surface = Color(0xFFF4F4F0),
    onSurface = Color(0xFF1A231F),
    surfaceVariant = Color(0xFFDCE3DD),
    onSurfaceVariant = Color(0xFF4F5A54),
    surfaceTint = Color(0xFF24473A),
    surfaceDim = Color(0xFFD5D6D1),
    surfaceBright = Color(0xFFF4F4F0),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFEFEA),
    surfaceContainer = Color(0xFFE9EAE4),
    surfaceContainerHigh = Color(0xFFE3E4DE),
    surfaceContainerHighest = Color(0xFFDDDED8),
    inverseSurface = Color(0xFF2E3531),
    inverseOnSurface = Color(0xFFEEF1EC),
    outline = Color(0xFF747E78),
    outlineVariant = Color(0xFFC6CBC5),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    scrim = Color(0xFF000000),
)

val DarkColors = darkColorScheme(
    primary = Color(0xFF9DCFB6),
    onPrimary = Color(0xFF003825),
    primaryContainer = Color(0xFF1F4E3D),
    onPrimaryContainer = Color(0xFFB9EBD1),
    inversePrimary = Color(0xFF24473A),
    secondary = Color(0xFFBCCABF),
    onSecondary = Color(0xFF27332C),
    secondaryContainer = Color(0xFF3D4A42),
    onSecondaryContainer = Color(0xFFD8E6DB),
    tertiary = Color(0xFFFFB693),
    onTertiary = Color(0xFF561F00),
    tertiaryContainer = Color(0xFF7A3000),
    onTertiaryContainer = Color(0xFFFFDBCC),
    background = Color(0xFF111714),
    onBackground = Color(0xFFE2E6E1),
    surface = Color(0xFF111714),
    onSurface = Color(0xFFE2E6E1),
    surfaceVariant = Color(0xFF3F4943),
    onSurfaceVariant = Color(0xFFB9C3BC),
    surfaceTint = Color(0xFF9DCFB6),
    surfaceDim = Color(0xFF111714),
    surfaceBright = Color(0xFF363D39),
    surfaceContainerLowest = Color(0xFF0C110F),
    surfaceContainerLow = Color(0xFF171E1A),
    surfaceContainer = Color(0xFF1B2320),
    surfaceContainerHigh = Color(0xFF252D29),
    surfaceContainerHighest = Color(0xFF303834),
    inverseSurface = Color(0xFFE2E6E1),
    inverseOnSurface = Color(0xFF2E3531),
    outline = Color(0xFF87918B),
    outlineVariant = Color(0xFF3F4943),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
)
