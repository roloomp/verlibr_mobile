package com.example.verlibr.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val VerlibrColors = lightColorScheme(
    primary = Amber,
    onPrimary = Cream,
    primaryContainer = Taupe,
    onPrimaryContainer = Cream,
    secondary = Moss,
    onSecondary = Cream,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = Fog,
    onSurfaceVariant = Stone,
    outline = Ink
)

@Composable
fun VerlibrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VerlibrColors,
        typography = Typography,
        content = content
    )
}
