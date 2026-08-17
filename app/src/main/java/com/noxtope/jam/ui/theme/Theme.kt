package com.noxtope.jam.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

// Nota: el tema real se define en MainActivity con los colores elegidos
// por el usuario (colorPrimario/colorSecundario). Estos esquemas se mantienen
// solo como referencia/fallback.

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)
