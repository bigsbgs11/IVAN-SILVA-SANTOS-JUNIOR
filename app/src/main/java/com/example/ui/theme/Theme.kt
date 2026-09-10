package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Blue400,
    onPrimary = SurfaceWhite,
    primaryContainer = Blue800,
    onPrimaryContainer = Blue100,
    secondary = Gray400,
    onSecondary = Gray900,
    secondaryContainer = Gray800,
    onSecondaryContainer = Gray200,
    background = Gray900,
    onBackground = Gray100,
    surface = Gray800,
    onSurface = Gray100,
    surfaceVariant = Gray700,
    onSurfaceVariant = Gray300,
    outline = Gray600
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Blue600,
    onPrimary = SurfaceWhite,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue900,
    secondary = Gray600,
    onSecondary = SurfaceWhite,
    secondaryContainer = Gray100,
    onSecondaryContainer = Gray800,
    background = BackgroundLight,
    onBackground = Gray900,
    surface = SurfaceWhite,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    outline = Gray200
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
