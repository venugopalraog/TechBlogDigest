package com.engblog.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Indigo = Color(0xFF3F51B5)
private val IndigoContainer = Color(0xFFDEE1FF)
private val Teal = Color(0xFF00695C)
private val TealContainer = Color(0xFFB2DFDB)

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = Color(0xFF00105C),
    secondary = Teal,
    secondaryContainer = TealContainer,
    background = Color(0xFFFBFAFF),
    surface = Color(0xFFFBFAFF),
    surfaceVariant = Color(0xFFE3E1F5),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBEC2FF),
    onPrimary = Color(0xFF00157F),
    primaryContainer = Color(0xFF2A3092),
    onPrimaryContainer = IndigoContainer,
    secondary = Color(0xFF80CBC4),
    secondaryContainer = Color(0xFF00473F),
    background = Color(0xFF121218),
    surface = Color(0xFF121218),
    surfaceVariant = Color(0xFF46464F),
)

@Composable
fun EngBlogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
