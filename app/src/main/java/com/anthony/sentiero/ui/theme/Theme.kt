// UI layer: wraps the whole app in the DESIGN.md colors, type and shapes.
// Dynamic color is deliberately not used: wallpaper colors would erase the blaze's meaning.
package com.anthony.sentiero.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun SentieroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SentieroTypography,
        shapes = SentieroShapes,
        content = content,
    )
}
