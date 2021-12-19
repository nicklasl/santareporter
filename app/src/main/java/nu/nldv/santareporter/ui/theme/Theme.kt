package nu.nldv.santareporter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color.Companion.Red

private val DarkColorPalette = darkColors(
    primary = ChristmasRed,
    primaryVariant = Redish,
    secondary = ChristmasGreen
)

private val LightColorPalette = lightColors(
    primary = ChristmasRed,
    primaryVariant = Redish,
    secondary = ChristmasGreen
)

@Composable
fun SantaReporterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}