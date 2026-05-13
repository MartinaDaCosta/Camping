@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.lab.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AquaGreen = Color(0xFF4DB6AC)
val AquaGreenDark = Color(0xFF00897B)
val AquaGreenLight = Color(0xFFB2DFDB)
val AquaSurface = Color(0xFFEAF7F6)
val White = Color(0xFFFFFFFF)

private val AquaColorScheme = lightColorScheme(
    primary = AquaGreen,
    onPrimary = White,
    secondary = AquaGreenDark,
    onSecondary = White,
    background = AquaSurface,
    onBackground = Color(0xFF0F1F1E),
    surface = AquaSurface,
    onSurface = Color(0xFF0F1F1E),
    primaryContainer = AquaGreenLight,
    onPrimaryContainer = Color(0xFF0B2B28),
    secondaryContainer = Color(0xFFCDEBE8),
    onSecondaryContainer = Color(0xFF0B2B28)
)

@Composable
fun AppTopBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.primary,
    titleContentColor = MaterialTheme.colorScheme.onPrimary,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
)

@Composable
fun LABTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AquaColorScheme,
        typography = Typography,
        content = content
    )
}